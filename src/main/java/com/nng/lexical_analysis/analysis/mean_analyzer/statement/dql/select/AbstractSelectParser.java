package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.select;

import com.google.common.base.Optional;
import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.exception.SQLParsingUnsupportedException;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.Expression;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.IdentifierExpression;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.NumberExpression;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.PropertyExpression;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.OrderItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Column;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.columnCondition;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.AggregationSelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.CommonSelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.SelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Table;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Tables;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatementParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.token.ItemsToken;
import com.nng.lexical_analysis.analysis.mean_analyzer.token.OrderByToken;
import com.nng.lexical_analysis.analysis.mean_analyzer.token.TableToken;
import com.nng.lexical_analysis.analysis.word_analyzer.token.*;
import com.nng.lexical_analysis.contact.AggregationType;
import com.nng.lexical_analysis.contact.OrderType;
import com.nng.unit.SQLUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractSelectParser implements SQLStatementParser {
    
    private static final String DERIVED_COUNT_ALIAS = "AVG_DERIVED_COUNT_%s";
    
    private static final String DERIVED_SUM_ALIAS = "AVG_DERIVED_SUM_%s";
    
    private static final String ORDER_BY_DERIVED_ALIAS = "ORDER_BY_DERIVED_%s";
    
    private static final String GROUP_BY_DERIVED_ALIAS = "GROUP_BY_DERIVED_%s";
    
    private final SQLParser sqlParser;
    
    private final SelectStatement selectStatement;
    
    @Setter
    private int parametersIndex;
    
    private boolean appendDerivedColumnsFlag;
    
    public AbstractSelectParser(final SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        selectStatement = new SelectStatement();
    }
    
    @Override
    public final SelectStatement parse() {
        query();
        parseOrderBy();
        appendDerivedColumns();
        appendDerivedOrderBy();
        return selectStatement;
    }

    protected abstract void query();

    /**
     * 解析 DISTINCT、DISTINCTROW、UNION、ALL
     * 此处的 DISTINCT 和 DISTINCT(字段) 不同，它是针对某行的。
     * 例如 SELECT DISTINCT user_id FROM t_order 。此时即使一个用户有多个订单，这个用户也智慧返回一个 user_id。
     */
    protected final void parseDistinct() {
        if (sqlParser.equalAny(DefaultKeyword.DISTINCT, DefaultKeyword.DISTINCTROW, DefaultKeyword.UNION)) {
            selectStatement.setDistinct(true);
            sqlParser.getLexer().nextToken();
            if (hasDistinctOn() && sqlParser.equalAny(DefaultKeyword.ON)) { // PostgreSQL 独有语法： DISTINCT ON
                sqlParser.getLexer().nextToken();
                sqlParser.skipParentheses();
            }
        } else if (sqlParser.equalAny(DefaultKeyword.ALL)) {
            sqlParser.getLexer().nextToken();
        }
    }
    
    protected boolean hasDistinctOn() {
        return false;
    }

    /**
     * 解析所有选择项
     */
    protected final void parseSelectList() {
        do {
            // 解析 选择项
            parseSelectItem();
        } while (sqlParser.skipIfEqual(Symbol.COMMA));
        // 设置 最后一个查询项下一个 Token 的开始位置
        selectStatement.setSelectListLastPosition(sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length());
    }

    /**
     * 解析单个选择项
     */
    private void parseSelectItem() {
        // 第四种情况，SQL Server 独有
        if (isRowNumberSelectItem()) {
            selectStatement.getItems().add(parseRowNumberSelectItem());
            return;
        }
        sqlParser.skipIfEqual(DefaultKeyword.CONNECT_BY_ROOT); // Oracle 独有：https://docs.oracle.com/cd/B19306_01/server.102/b14200/operators004.htm
        String literals = sqlParser.getLexer().getCurrentToken().getLiterals();
        // 第一种情况，* 通用选择项，SELECT *
        if (isStarSelectItem(literals)) {
            selectStatement.getItems().add(parseStarSelectItem());
            return;
        }
        // 第二种情况，聚合选择项
        if (isAggregationSelectItem()) {
            selectStatement.getItems().add(parseAggregationSelectItem(literals));
            return;
        }
        // 第三种情况，非 * 通用选择项
        StringBuilder expression = new StringBuilder();
        Token lastToken = null;
        while (!sqlParser.equalAny(DefaultKeyword.AS) && !sqlParser.equalAny(Symbol.COMMA) && !sqlParser.equalAny(DefaultKeyword.FROM) && !sqlParser.equalAny(Assist.END)) {
            String value = sqlParser.getLexer().getCurrentToken().getLiterals();
            int position = sqlParser.getLexer().getCurrentToken().getEndPosition() - value.length();
            expression.append(value);
            lastToken = sqlParser.getLexer().getCurrentToken();
            sqlParser.getLexer().nextToken();
            if (sqlParser.equalAny(Symbol.DOT)) {
                selectStatement.getSqlTokens().add(new TableToken(position, value));
            }
        }
        // 不带 AS，并且有别名，并且别名不等于自己（tips：这里重点看。判断这么复杂的原因：防止substring操作截取结果错误）
        if (hasAlias(expression, lastToken)) {
            selectStatement.getItems().add(parseSelectItemWithAlias(expression, lastToken));
            return;
        }
        // 带 AS（例如，SELECT user_id AS userId） 或者 无别名（例如，SELECT user_id）
        selectStatement.getItems().add(new CommonSelectItem(SQLUtil.getExactlyValue(expression.toString()), sqlParser.parseAlias()));
    }
    
    protected boolean isRowNumberSelectItem() {
        return false;
    }
    
    protected SelectItem parseRowNumberSelectItem() {
        throw new UnsupportedOperationException("Cannot support special select item.");
    }

    private boolean isStarSelectItem(final String literals) {
        return sqlParser.equalAny(Symbol.STAR) || Symbol.STAR.getLiterals().equals(SQLUtil.getExactlyValue(literals));
    }

    private SelectItem parseStarSelectItem() {
        sqlParser.getLexer().nextToken();
        selectStatement.setContainStar(true);
        return new CommonSelectItem(Symbol.STAR.getLiterals(), sqlParser.parseAlias());
    }

    private boolean isAggregationSelectItem() {
        return sqlParser.skipIfEqual(DefaultKeyword.MAX, DefaultKeyword.MIN, DefaultKeyword.SUM, DefaultKeyword.AVG, DefaultKeyword.COUNT);
    }

    private SelectItem parseAggregationSelectItem(final String literals) {
        return new AggregationSelectItem(AggregationType.valueOf(literals.toUpperCase()), sqlParser.skipParentheses(), sqlParser.parseAlias());
    }

    private boolean hasAlias(final StringBuilder expression, final Token lastToken) {
        return null != lastToken && Literals.IDENTIFIER == lastToken.getType()
                && !isSQLPropertyExpression(expression, lastToken) // 过滤掉，别名是自己的情况【1】（例如，SELECT u.user_id u.user_id FROM t_user）
                && !expression.toString().equals(lastToken.getLiterals()); // 过滤掉，无别名的情况【2】（例如，SELECT user_id FROM t_user）
    }

    /**
     * 是否 表达式 以 "." + lastToken 结尾。
     * 目前用于判断：SELECT u.user_id u.user_id FROM t_user 情况
     *
     * @param expression 表达式
     * @param lastToken 最后 Token
     * @return 是否
     */
    private boolean isSQLPropertyExpression(final StringBuilder expression, final Token lastToken) {
        return expression.toString().endsWith(Symbol.DOT.getLiterals() + lastToken.getLiterals());
    }
    
    private CommonSelectItem parseSelectItemWithAlias(final StringBuilder expression, final Token lastToken) {
        return new CommonSelectItem(SQLUtil.getExactlyValue(expression.substring(0, expression.lastIndexOf(lastToken.getLiterals()))), Optional.of(lastToken.getLiterals()));
    }

    protected void queryRest() {
        if (sqlParser.equalAny(DefaultKeyword.UNION, DefaultKeyword.EXCEPT, DefaultKeyword.INTERSECT, DefaultKeyword.MINUS)) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
    }
    
    protected final void parseWhere() {
        if (selectStatement.getTables().isEmpty()) {
            return;
        }
        sqlParser.parseWhere(selectStatement);
        parametersIndex = sqlParser.getParametersIndex();
    }
    
    protected final void parseOrderBy() {
        if (!sqlParser.skipIfEqual(DefaultKeyword.ORDER)) {
            return;
        }
        List<OrderItem> result = new LinkedList<>();
        sqlParser.skipIfEqual(DefaultKeyword.SIBLINGS);
        sqlParser.accept(DefaultKeyword.BY);
        do {
            // 解析单个 OrderBy
            Optional<OrderItem> orderItem = parseSelectOrderByItem();
            if (orderItem.isPresent() && !selectStatement.isContainSubQuery()) {
                result.add(orderItem.get());
            }
        }
        while (sqlParser.skipIfEqual(Symbol.COMMA));
        // OrderItem
        selectStatement.getOrderByItems().addAll(result);
    }

    /**
     * 解析单个排序项
     *
     * @return 排序项
     */
    protected Optional<OrderItem> parseSelectOrderByItem() {
        // ASC / DESC
        Expression sqlExpression = sqlParser.parseExpression(selectStatement);
        OrderType orderByType = OrderType.ASC;
        if (sqlParser.skipIfEqual(DefaultKeyword.ASC)) {
            orderByType = OrderType.ASC;
        } else if (sqlParser.skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderType.DESC;
        }
        // 解析 OrderItem
        OrderItem result;
        if (sqlExpression instanceof NumberExpression) { // ORDER BY 数字 的 数字代表的是第几个字段。
            result = new OrderItem(((NumberExpression) sqlExpression).getNumber().intValue(), orderByType);
        } else if (sqlExpression instanceof IdentifierExpression) {
            result = new OrderItem(
                    SQLUtil.getExactlyValue(((IdentifierExpression) sqlExpression).getName()), orderByType, getAlias(SQLUtil.getExactlyValue(((IdentifierExpression) sqlExpression).getName())));
        } else if (sqlExpression instanceof PropertyExpression) {
            PropertyExpression sqlPropertyExpression = (PropertyExpression) sqlExpression;
            result = new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getTable_name().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getNew_name()), orderByType,
                    getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getTable_name().getName()) + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getNew_name())));
        } else {
            return Optional.absent();
        }
        return Optional.of(result);
    }

    /**
     * 解析 Group By 和 Having（暂时不支持）
     */
    protected void parseGroupBy() {
        if (sqlParser.skipIfEqual(DefaultKeyword.GROUP)) {
            sqlParser.accept(DefaultKeyword.BY);
            // 解析 Group By 每个字段
            while (true) {
                addGroupByItem(sqlParser.parseExpression(selectStatement));
                if (!sqlParser.equalAny(Symbol.COMMA)) {
                    break;
                }
                sqlParser.getLexer().nextToken();
            }
            while (sqlParser.equalAny(DefaultKeyword.WITH) || sqlParser.getLexer().getCurrentToken().getLiterals().equalsIgnoreCase("ROLLUP")) {
                sqlParser.getLexer().nextToken();
            }
            // Having（暂时不支持）
            if (sqlParser.skipIfEqual(DefaultKeyword.HAVING)) {
                throw new UnsupportedOperationException("Cannot support Having");
            }
            selectStatement.setGroupByLastPosition(sqlParser.getLexer().getCurrentToken().getEndPosition());
        } else if (sqlParser.skipIfEqual(DefaultKeyword.HAVING)) {
            throw new UnsupportedOperationException("Cannot support Having");
        }
    }

    /**
     * 解析 Group By 单个字段
     * Group By 条件是带有排序功能，默认ASC
     *
     * @param sqlExpression 表达式
     */
    protected final void addGroupByItem(final Expression sqlExpression) {
        // Group By 字段 DESC / ASC / ;默认是 ASC。
        OrderType orderByType = OrderType.ASC;
        if (sqlParser.equalAny(DefaultKeyword.ASC)) {
            sqlParser.getLexer().nextToken();
        } else if (sqlParser.skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderType.DESC;
        }
        // 解析 OrderItem
        OrderItem orderItem;
        if (sqlExpression instanceof PropertyExpression) {
            PropertyExpression sqlPropertyExpression = (PropertyExpression) sqlExpression;
            orderItem = new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getTable_name().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getNew_name()), orderByType,
                    getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getTable_name() + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getNew_name()))));
        } else if (sqlExpression instanceof IdentifierExpression) {
            IdentifierExpression sqlIdentifierExpression = (IdentifierExpression) sqlExpression;
            orderItem = new OrderItem(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName()), orderByType, getAlias(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName())));
        } else {
            return;
        }
        if (!selectStatement.isContainSubQuery()) {
            selectStatement.getGroupByItems().add(orderItem);
        }
    }

    /**
     * 字段在查询项里的别名
     *
     * @param name 字段
     * @return 别名
     */
    private Optional<String> getAlias(final String name) {
        if (selectStatement.isContainStar()) {
            return Optional.absent();
        }
        String rawName = SQLUtil.getExactlyValue(name);
        for (SelectItem each : selectStatement.getItems()) {
            if (rawName.equalsIgnoreCase(SQLUtil.getExactlyValue(each.getExpression()))) {
                return each.getAlias();
            }
            if (rawName.equalsIgnoreCase(each.getAlias().orNull())) {
                return Optional.of(rawName);
            }
        }
        return Optional.absent();
    }

    /**
     * 解析所有表名和表别名
     */
    public final void parseFrom() {
        if (sqlParser.skipIfEqual(DefaultKeyword.FROM)) {
            parseTable();
        }
    }

    /**
     * 解析所有表名和表别名
     */
    public void parseTable() {
        // 解析子查询
        if (sqlParser.skipIfEqual(Symbol.LEFT_PAREN)) {
            if (!selectStatement.getTables().isEmpty()) {
                throw new UnsupportedOperationException("Cannot support subquery for nested tables.");
            }
            //把包含子查询设置为true
            selectStatement.setContainSubQuery(true);
            // TODO 疑问
            selectStatement.setContainStar(false);
            // 去掉子查询左括号
            sqlParser.skipUselessParentheses();
            // 解析子查询 SQL
            parse();
            // 去掉子查询右括号
            sqlParser.skipUselessParentheses();
            //
            if (!selectStatement.getTables().isEmpty()) {
                return;
            }
        }
        // 解析当前表
        parseTableFactor();
        // 解析下一个表
        parseJoinTable();
    }

    /**
     * 解析单个表名和表别名
     */
    protected final void parseTableFactor() {
        int beginPosition = sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length();
        String literals = sqlParser.getLexer().getCurrentToken().getLiterals();
        sqlParser.getLexer().nextToken();
        // TODO 包含Schema解析
        if (sqlParser.skipIfEqual(Symbol.DOT)) { // https://dev.mysql.com/doc/refman/5.7/en/information-schema.html ：SELECT table_name, table_type, engine FROM information_schema.tables
            sqlParser.getLexer().nextToken();
            sqlParser.parseAlias();
            return;
        }
        // FIXME 根据shardingRule过滤table
        selectStatement.getSqlTokens().add(new TableToken(beginPosition, literals));
        // 表 以及 表别名
        selectStatement.getTables().add(new Table(SQLUtil.getExactlyValue(literals), sqlParser.parseAlias()));
    }

    /**
     * 解析 Join Table 或者 FROM 下一张 Table
     */
    protected void parseJoinTable() {
        if (sqlParser.skipJoin()) {
            // 这里调用 parseJoinTable() 而不是 parseTableFactor() ：下一个 Table 可能是子查询
            // 例如：SELECT * FROM t_order JOIN (SELECT * FROM t_order_item JOIN t_order_other ON ) .....
            parseTable();
            if (sqlParser.skipIfEqual(DefaultKeyword.ON)) { // JOIN 表时 ON 条件

            sqlParser.parseConditions(selectStatement);

            } else if (sqlParser.skipIfEqual(DefaultKeyword.USING)) { // JOIN 表时 USING 为使用两表相同字段相同时对 ON 的简化。例如以下两条 SQL 等价：
                                                                        // SELECT * FROM t_order o JOIN t_order_item i USING (order_id);
                                                                        // SELECT * FROM t_order o JOIN t_order_item i ON o.order_id = i.order_id
                sqlParser.skipParentheses();
            }
            parseJoinTable(); // 继续递归
        }

    }

    /**
     * 解析 ON 条件里的 TableToken
     *
     * @param startPosition 开始位置
     */
    private PropertyExpression parseTableCondition(final int startPosition) {
        Expression sqlExpression = sqlParser.parseExpression();
        if (!(sqlExpression instanceof PropertyExpression)) {
            return null;
        }

        PropertyExpression sqlPropertyExpression = (PropertyExpression) sqlExpression;
        if (selectStatement.getTables().getTableNames().contains(SQLUtil.getExactlyValue(sqlPropertyExpression.getTable_name().getName()))) {
            selectStatement.getSqlTokens().add(new TableToken(startPosition, sqlPropertyExpression.getTable_name().getName()));
            return sqlPropertyExpression;
        }
        return null;
    }

    /**
     * 增加推导字段
     */
    private void appendDerivedColumns() {
        if (appendDerivedColumnsFlag) {
            return;
        }
        appendDerivedColumnsFlag = true;
        ItemsToken itemsToken = new ItemsToken(selectStatement.getSelectListLastPosition());
        // AVG 聚合字段
        appendAvgDerivedColumns(itemsToken);
        // ORDER BY
        appendDerivedOrderColumns(itemsToken, selectStatement.getOrderByItems(), ORDER_BY_DERIVED_ALIAS);
        // GROUP BY
        appendDerivedOrderColumns(itemsToken, selectStatement.getGroupByItems(), GROUP_BY_DERIVED_ALIAS);
        if (!itemsToken.getItems().isEmpty()) {
            selectStatement.getSqlTokens().add(itemsToken);
        }
    }

    /**
     * 针对 AVG 聚合字段，增加推导字段
     * AVG 改写成 SUM + COUNT 查询，内存计算出 AVG 结果。
     *
     * @param itemsToken 选择项标记对象
     */
    private void appendAvgDerivedColumns(final ItemsToken itemsToken) {
        int derivedColumnOffset = 0;
        for (SelectItem each : selectStatement.getItems()) {
            if (!(each instanceof AggregationSelectItem) || AggregationType.AVG != ((AggregationSelectItem) each).getType()) {
                continue;
            }
            AggregationSelectItem avgItem = (AggregationSelectItem) each;
            // COUNT 字段
            String countAlias = String.format(DERIVED_COUNT_ALIAS, derivedColumnOffset);
            AggregationSelectItem countItem = new AggregationSelectItem(AggregationType.COUNT, avgItem.getInnerExpression(), Optional.of(countAlias));
            // SUM 字段
            String sumAlias = String.format(DERIVED_SUM_ALIAS, derivedColumnOffset);
            AggregationSelectItem sumItem = new AggregationSelectItem(AggregationType.SUM, avgItem.getInnerExpression(), Optional.of(sumAlias));
            // AggregationSelectItem 设置
            avgItem.getDerivedAggregationSelectItems().add(countItem);
            avgItem.getDerivedAggregationSelectItems().add(sumItem);
            // TODO 将AVG列替换成常数，避免数据库再计算无用的AVG函数
            // ItemsToken
            itemsToken.getItems().add(countItem.getExpression() + " AS " + countAlias + " ");
            itemsToken.getItems().add(sumItem.getExpression() + " AS " + sumAlias + " ");
            //
            derivedColumnOffset++;
        }
    }

    /**
     * 针对 GROUP BY 或 ORDER BY 字段，增加推导字段
     * 如果该字段不在查询字段里，需要额外查询该字段，这样才能在内存里 GROUP BY 或 ORDER BY
     *
     * @param itemsToken 选择项标记对象
     * @param orderItems 排序字段
     * @param aliasPattern 别名模式
     */
    private void appendDerivedOrderColumns(final ItemsToken itemsToken, final List<OrderItem> orderItems, final String aliasPattern) {
        int derivedColumnOffset = 0;
        for (OrderItem each : orderItems) {
            if (!isContainsItem(each)) {
                String alias = String.format(aliasPattern, derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(each.getQualifiedName().get() + " AS " + alias + " ");
            }
        }
    }

    /**
     * 查询字段是否包含排序字段
     *
     * @param orderItem 排序字段
     * @return 是否
     */
    private boolean isContainsItem(final OrderItem orderItem) {
        if (selectStatement.isContainStar()) { // SELECT *
            return true;
        }
        for (SelectItem each : selectStatement.getItems()) {
            if (-1 != orderItem.getIndex()) { // ORDER BY 使用数字
                return true;
            }
            if (each.getAlias().isPresent() && orderItem.getAlias().isPresent() && each.getAlias().get().equalsIgnoreCase(orderItem.getAlias().get())) { // 字段别名比较
                return true;
            }
            if (!each.getAlias().isPresent() && orderItem.getQualifiedName().isPresent() && each.getExpression().equalsIgnoreCase(orderItem.getQualifiedName().get())) { // 字段原名比较
                return true;
            }
        }
        return false;
    }

    /**
     * 当无 Order By 条件时，使用 Group By 作为排序条件（数据库本身规则）
     */
    private void appendDerivedOrderBy() {
        if (!getSelectStatement().getGroupByItems().isEmpty() && getSelectStatement().getOrderByItems().isEmpty()) {
            getSelectStatement().getOrderByItems().addAll(getSelectStatement().getGroupByItems());
            getSelectStatement().getSqlTokens().add(new OrderByToken(getSelectStatement().getGroupByLastPosition()));
        }
    }


    /**
     * 借用
     */

    /**
     *
     *
     * @param tables 表
     * @param sqlExpression SqlExpression
     * @return 列
     */
    private Optional<Column> find(final Tables tables, final Expression sqlExpression) {
        if (sqlExpression instanceof PropertyExpression) {
            return getColumnWithOwner(tables, (PropertyExpression) sqlExpression);
        }
        if (sqlExpression instanceof IdentifierExpression) {
            return getColumnWithoutOwner(tables, (IdentifierExpression) sqlExpression);
        }
        return Optional.absent();
    }

    /**
     * 获得列
     *
     * @param tables 表
     * @param propertyExpression SQLPropertyExpression
     * @return 列
     */
    private Optional<Column> getColumnWithOwner(final Tables tables, final PropertyExpression propertyExpression) {
        Optional<Table> table = tables.find(SQLUtil.getExactlyValue((propertyExpression.getTable_name()).getName()));
        return propertyExpression.getTable_name() instanceof IdentifierExpression && table.isPresent()
                ? Optional.of(new Column(SQLUtil.getExactlyValue(propertyExpression.getNew_name()), table.get().getName())) : Optional.<Column>absent();
    }

    /**
     * 获得列
     * 只有是单表的情况下，才能获得到列
     *
     * @param tables 表
     * @param identifierExpression SQLIdentifierExpression
     * @return 列
     */
    private Optional<Column> getColumnWithoutOwner(final Tables tables, final IdentifierExpression identifierExpression) {
        return tables.isSingleTable() ? Optional.of(new Column(SQLUtil.getExactlyValue(identifierExpression.getName()), tables.getSingleTableName())) : Optional.<Column>absent();
    }
}
