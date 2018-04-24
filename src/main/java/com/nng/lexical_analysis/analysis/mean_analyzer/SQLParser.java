package com.nng.lexical_analysis.analysis.mean_analyzer;

import com.google.common.base.Optional;
import com.nng.lexical_analysis.analysis.mean_analyzer.exception.SQLParsingUnsupportedException;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.*;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Column;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Condition;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.columnCondition;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.limit.Limit;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.limit.LimitValue;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.SelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Table;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Tables;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.select.SelectStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.token.OffsetToken;
import com.nng.lexical_analysis.analysis.mean_analyzer.token.RowCountToken;
import com.nng.lexical_analysis.analysis.mean_analyzer.token.TableToken;
import com.nng.lexical_analysis.analysis.word_analyzer.Lexer;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Literals;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import com.nng.unit.NumberUtil;
import com.nng.unit.SQLUtil;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * SQL解析器.
 */
@Getter
public class SQLParser extends AbstractParser {
    

    
    public SQLParser(final Lexer lexer) {
        super(lexer);
        getLexer().nextToken();
    }
    
    /**
     * 解析表达式.
     *
     * @param sqlStatement SQL语句对象
     * @return 表达式
     */
    public final Expression parseExpression(final SQLStatement sqlStatement) {
        // 【调试代码】
        StackTraceElement stack[] = (new Throwable()).getStackTrace();//StackTrace(堆栈轨迹)存放的就是方法调用栈的信息
//        System.out.println();
//        System.out.println(stack[1].getMethodName()); // 调用方法
//        System.out.println("begin：" + getLexer().getCurrentToken().getLiterals());

        int beginPosition = getLexer().getCurrentToken().getEndPosition();
        Expression result = parseExpression();
        if (result instanceof PropertyExpression) {
            setTableToken(sqlStatement, beginPosition, (PropertyExpression) result);
        }

//        // 【调试代码】
//        System.out.print("end：");
//        if (result instanceof IdentifierExpression) {
//            IdentifierExpression exp = (IdentifierExpression) result;
//            System.out.println(exp.getClass().getSimpleName() + "：" + exp.getName());
//        } else if (result instanceof IgnoreExpression) {
//            IgnoreExpression exp = (IgnoreExpression) result;
//            System.out.println(exp.getClass().getSimpleName() + "：");
//        } else if (result instanceof NumberExpression) {
//            NumberExpression exp = (NumberExpression) result;
//            System.out.println(exp.getClass().getSimpleName() + "：" + exp.getNumber());
//        } else if (result instanceof PropertyExpression) {
//            PropertyExpression exp = (PropertyExpression) result;
//            System.out.println(exp.getClass().getSimpleName() + "：" + exp.getTable_name().getName() + "\t" + exp.getNew_name());
//        } else if (result instanceof TextExpression) {
//            TextExpression exp = (TextExpression) result;
//            System.out.println(exp.getClass().getSimpleName() + "：" + exp.getText());
//        }
//        System.out.println();
//        System.out.println();
        return result;
    }

    /**
     * 解析表达式.
     *
     * @return 表达式
     */
    // TODO 完善Expression解析的各种场景
    public final Expression parseExpression() {
        if (!(new Throwable()).getStackTrace()[1].getMethodName().equals("parseExpression")) {
            System.out.println();
        }
        // 解析表达式
        String literals = getLexer().getCurrentToken().getLiterals();
        final Expression expression = getExpression(literals);
        // SQLIdentifierExpression 需要特殊处理。考虑自定义函数，表名.属性情况。
        if (skipIfEqual(Literals.IDENTIFIER)) {
            if (skipIfEqual(Symbol.DOT)) { // 例如，ORDER BY o.uid 中的 "o.uid"
                String property = getLexer().getCurrentToken().getLiterals();
                getLexer().nextToken();
                return skipIfCompositeExpression() ? new IgnoreExpression() : new PropertyExpression(new IdentifierExpression(literals), property);
            }
            if (equalAny(Symbol.LEFT_PAREN)) { // 例如，GROUP BY DATE(create_time) 中的 "DATE(create_time)"
                skipParentheses();
                skipRestCompositeExpression();
                return new IgnoreExpression();
            }
            return skipIfCompositeExpression() ? new IgnoreExpression() : expression;
        }
        getLexer().nextToken();
        return skipIfCompositeExpression() ? new IgnoreExpression() : expression;
    }

    /**
     * 获得 词法Token 对应的 SQLExpression
     *
     * @param literals 词法字面量标记
     * @return SQLExpression
     */
    private Expression getExpression(final String literals) {
        if (equalAny(Symbol.QUESTION)) {
            increaseParametersIndex();
            return new PlaceholderExpression(getParametersIndex() - 1);
        }
        if (equalAny(Literals.CHARS)) {
            return new TextExpression(literals);
        }
        if (equalAny(Literals.INT)) {
            return new NumberExpression(NumberUtil.getExactlyNumber(literals, 10));
        }
        if (equalAny(Literals.FLOAT)) {
            return new NumberExpression(Double.parseDouble(literals));
        }
        if (equalAny(Literals.HEX)) {
            return new NumberExpression(NumberUtil.getExactlyNumber(literals, 16));
        }
        if (equalAny(Literals.IDENTIFIER)) {
            return new IdentifierExpression(SQLUtil.getExactlyValue(literals));
        }
        return new IgnoreExpression();
    }

    /**
     * 如果是 复合表达式，跳过。
     *
     * @return 是否跳过
     */
    private boolean skipIfCompositeExpression() {
        if (equalAny(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT, Symbol.LEFT_PAREN)) {
            skipParentheses();
            skipRestCompositeExpression();
            return true;
        }
        return false;
    }

    /**
     * 跳过剩余复合表达式
     */
    private void skipRestCompositeExpression() {
        while (skipIfEqual(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT)) {
            if (equalAny(Symbol.QUESTION)) {
                increaseParametersIndex();
            }
            getLexer().nextToken();
            skipParentheses();
        }
    }

    private void setTableToken(final SQLStatement sqlStatement, final int beginPosition, final PropertyExpression propertyExpr) {
        String owner = propertyExpr.getTable_name().getName();
        if (!sqlStatement.getTables().isEmpty() && sqlStatement.getTables().getSingleTableName().equalsIgnoreCase(SQLUtil.getExactlyValue(owner))) {
            sqlStatement.getSqlTokens().add(new TableToken(beginPosition - owner.length(), owner));
        }
    }

    /**
     * 解析别名.不仅仅是字段的别名，也可以是表的别名。
     *
     * @return 别名
     */
    public Optional<String> parseAlias() {
        // 解析带 AS 情况
        if (skipIfEqual(DefaultKeyword.AS)) {
            if (equalAny(Symbol.values())) {
                return Optional.absent();
            }
            String result = SQLUtil.getExactlyValue(getLexer().getCurrentToken().getLiterals());
            getLexer().nextToken();
            return Optional.of(result);
        }
        // 解析别名
        // TODO 增加哪些数据库识别哪些关键字作为别名的配置
        if (equalAny(Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.USER, DefaultKeyword.END, DefaultKeyword.CASE, DefaultKeyword.KEY, DefaultKeyword.INTERVAL, DefaultKeyword.CONSTRAINT)) {
            String result = SQLUtil.getExactlyValue(getLexer().getCurrentToken().getLiterals());
            getLexer().nextToken();
            return Optional.of(result);
        }
        return Optional.absent();
    }

    /**
     * 解析单表.
     *
     * @param sqlStatement SQL语句对象
     */
    public final void parseSingleTable(final SQLStatement sqlStatement) {
        boolean hasParentheses = false;
        if (skipIfEqual(Symbol.LEFT_PAREN)) {
            if (equalAny(DefaultKeyword.SELECT)) { // multiple-update 或者 multiple-delete
                throw new UnsupportedOperationException("Cannot support subquery");
            }
            hasParentheses = true;
        }
        Table table;
        final int beginPosition = getLexer().getCurrentToken().getEndPosition() - getLexer().getCurrentToken().getLiterals().length();
        String literals = getLexer().getCurrentToken().getLiterals();
        getLexer().nextToken();
        if (skipIfEqual(Symbol.DOT)) {
            getLexer().nextToken();
            if (hasParentheses) {
                accept(Symbol.RIGHT_PAREN);
            }
            table = new Table(SQLUtil.getExactlyValue(literals), parseAlias());
        } else {
            if (hasParentheses) {
                accept(Symbol.RIGHT_PAREN);
            }
            table = new Table(SQLUtil.getExactlyValue(literals), parseAlias());
        }
        if (skipJoin()) { // multiple-update 或者 multiple-delete
            throw new UnsupportedOperationException("Cannot support Multiple-Table.");
        }
        sqlStatement.getSqlTokens().add(new TableToken(beginPosition, literals));
        sqlStatement.getTables().add(table);
    }

    /**
     * 跳过表关联词法.
     *
     * @return 是否表关联.
     */
    public final boolean skipJoin() {
        if (skipIfEqual(DefaultKeyword.LEFT, DefaultKeyword.RIGHT, DefaultKeyword.FULL)) {
            skipIfEqual(DefaultKeyword.OUTER);
            accept(DefaultKeyword.JOIN);
            return true;
        } else if (skipIfEqual(DefaultKeyword.INNER)) {
            accept(DefaultKeyword.JOIN);
            return true;
        } else if (skipIfEqual(DefaultKeyword.JOIN, Symbol.COMMA, DefaultKeyword.STRAIGHT_JOIN)) {
            return true;
        } else if (skipIfEqual(DefaultKeyword.CROSS)) {
            if (skipIfEqual(DefaultKeyword.JOIN, DefaultKeyword.APPLY)) {
                return true;
            }
        } else if (skipIfEqual(DefaultKeyword.OUTER)) {
            if (skipIfEqual(DefaultKeyword.APPLY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析查询条件.
     *
     * @param sqlStatement SQL语句对象
     */
    public final void parseWhere(final SQLStatement sqlStatement) {
        parseAlias();
        if (skipIfEqual(DefaultKeyword.WHERE)) {
            parseConditions(sqlStatement);
        }
    }

    /**
     * 解析所有查询条件。
     * 目前不支持 OR 条件。
     *
     * @param sqlStatement SQL
     */
    public void parseConditions(final SQLStatement sqlStatement) {
        // AND 查询
        do {
            parseComparisonCondition(sqlStatement);
        } while (skipIfEqual(DefaultKeyword.AND));
        // 目前不支持 OR 条件
        if (equalAny(DefaultKeyword.OR)) {
            throw new SQLParsingUnsupportedException(getLexer().getCurrentToken().getType());
        }
    }

    // TODO 解析组合expr
    /**
     * 解析单个查询条件
     *
     * @param sqlStatement SQL
     */
    public final void parseComparisonCondition(final SQLStatement sqlStatement) {
        skipIfEqual(Symbol.LEFT_PAREN);
        Expression left = parseExpression(sqlStatement);
        if (equalAny(Symbol.EQ)) {
            parseEqualCondition(sqlStatement, left);
            skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (equalAny(DefaultKeyword.IN)) {
            parseInCondition(sqlStatement, left);
            skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (equalAny(DefaultKeyword.BETWEEN)) {
            parseBetweenCondition(sqlStatement, left);
            skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (equalAny(Symbol.LT, Symbol.GT, Symbol.LT_EQ, Symbol.GT_EQ,Symbol.BANG_EQ)) {
            if (left instanceof IdentifierExpression && sqlStatement instanceof SelectStatement
                    && isRowNumberCondition((SelectStatement) sqlStatement, ((IdentifierExpression) left).getName())) {
                parseRowNumberCondition((SelectStatement) sqlStatement);
            } else if (left instanceof PropertyExpression && sqlStatement instanceof SelectStatement
                    && isRowNumberCondition((SelectStatement) sqlStatement, ((PropertyExpression) left).getNew_name())) {
                parseRowNumberCondition((SelectStatement) sqlStatement);
            } else {
                if(equalAny(Symbol.LT))
                {parseNotEqual(sqlStatement, left,Symbol.LT);}
                else if(equalAny(Symbol.GT))
                {parseNotEqual(sqlStatement, left,Symbol.GT);}
                else if(equalAny(Symbol.LT_EQ))
                {parseNotEqual(sqlStatement, left,Symbol.LT_EQ);}
                else if(equalAny(Symbol.GT_EQ))
                {parseNotEqual(sqlStatement, left,Symbol.GT_EQ);}
                else if(equalAny(Symbol.BANG_EQ))
                {parseNotEqual(sqlStatement, left,Symbol.BANG_EQ);}
                parseOtherCondition(sqlStatement);
            }
        } else if (equalAny(Symbol.LT_GT, DefaultKeyword.LIKE)) {
            parseOtherCondition(sqlStatement);
        }
        skipIfEqual(Symbol.RIGHT_PAREN);
    }



    /**
     * 解析 = 条件
     *
     * @param sqlStatement SQL
     * @param left 左SQLExpression
     */
    private void parseEqualCondition(final SQLStatement sqlStatement, final Expression left) {
        getLexer().nextToken();
        Expression right = parseExpression(sqlStatement);
        // 添加列
        // TODO 如果有多表,且找不到column是哪个表的,则不加入condition,以后需要解析binding table
        if ((sqlStatement.getTables().isSingleTable() || left instanceof PropertyExpression)
                // 只有对路由结果有影响的才会添加到 conditions。PropertyExpression 和 IdentifierExpression 无法判断，所以未加入 conditions
                && (right instanceof NumberExpression || right instanceof TextExpression || right instanceof PlaceholderExpression)) {
            Optional<Column> column = find(sqlStatement.getTables(), left);
            if (column.isPresent()) {
                Condition conditionss=new Condition(column.get(), right);
                conditionss.setSymbol(Symbol.EQ);
                sqlStatement.getConditions().add(conditionss);
            }
        }

        //PropertyExpression 和 IdentifierExpression 判断左右分别是表条件的情况
        if((left instanceof  PropertyExpression)&&(right instanceof  PropertyExpression))
        {
            Optional<Column> columnA = find(sqlStatement.getTables(), left);
            Optional<Column> columnB = find(sqlStatement.getTables(), right);
            if ((columnA.isPresent())&& (columnB.isPresent())) {
            sqlStatement.getColumnConditions().add(new columnCondition(columnA.get(), columnB.get(),Symbol.EQ));
        }
        }
    }

    /**
     * 当不是相等等等的条件时
     * @param sqlStatement
     * @param left
     * @param symbol
     */
    private void parseNotEqual(final SQLStatement sqlStatement, final Expression left,Symbol symbol)
    {
        getLexer().nextToken();
        Expression right = parseExpression(sqlStatement);
        if ((sqlStatement.getTables().isSingleTable() || left instanceof PropertyExpression)
                // 只有对路由结果有影响的才会添加到 conditions。PropertyExpression 和 IdentifierExpression 无法判断，所以未加入 conditions
                && (right instanceof NumberExpression || right instanceof TextExpression || right instanceof PlaceholderExpression)) {
            Optional<Column> column = find(sqlStatement.getTables(), left);
            if (column.isPresent()) {
                Condition conditions=new Condition(column.get(), right);
                conditions.setSymbol(symbol);
                sqlStatement.getConditions().add(conditions);
            }
        }

        //PropertyExpression 和 IdentifierExpression 判断左右分别是表条件的情况，赋予符号，然后留着后面报错用
        if((left instanceof  PropertyExpression)&&(right instanceof  PropertyExpression))
        {
            Optional<Column> columnA = find(sqlStatement.getTables(), left);
            Optional<Column> columnB = find(sqlStatement.getTables(), right);
            if ((columnA.isPresent())&& (columnB.isPresent())) {
                sqlStatement.getColumnConditions().add(new columnCondition(columnA.get(), columnB.get(),symbol));
            }
        }
    }

    /**
     * 解析 IN 条件
     *
     * @param sqlStatement SQL
     * @param left 左SQLExpression
     */
    private void parseInCondition(final SQLStatement sqlStatement, final Expression left) {
        // 解析 IN 条件
        getLexer().nextToken();
        accept(Symbol.LEFT_PAREN);
        List<Expression> rights = new LinkedList<>();
        do {
            if (equalAny(Symbol.COMMA)) {
                getLexer().nextToken();
            }
            rights.add(parseExpression(sqlStatement));
        } while (!equalAny(Symbol.RIGHT_PAREN));
        // 添加列
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            sqlStatement.getConditions().add(new Condition(column.get(), rights));
        }
        // 解析下一个 TOKEN
        getLexer().nextToken();
    }

    /**
     * 解析 BETWEEN 条件
     *
     * @param sqlStatement SQL
     * @param left 左SQLExpression
     */
    private void parseBetweenCondition(final SQLStatement sqlStatement, final Expression left) {
        // 解析 BETWEEN 条件
        getLexer().nextToken();
        List<Expression> rights = new LinkedList<>();
        rights.add(parseExpression(sqlStatement));
        accept(DefaultKeyword.AND);
        rights.add(parseExpression(sqlStatement));
        // 添加查询条件
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            sqlStatement.getConditions().add(new Condition(column.get(), rights.get(0), rights.get(1)));
        }
    }


    protected boolean isRowNumberCondition(final SelectStatement selectStatement, final String columnLabel) {
        return false;
    }

    private void parseRowNumberCondition(final SelectStatement selectStatement) {
        Symbol symbol = (Symbol) getLexer().getCurrentToken().getType();
        getLexer().nextToken();
        Expression sqlExpression = parseExpression(selectStatement);
        if (null == selectStatement.getLimit()) {
            selectStatement.setLimit(new Limit(false));
        }
        if (Symbol.LT == symbol || Symbol.LT_EQ == symbol) {
            if (sqlExpression instanceof NumberExpression) {
                int rowCount = ((NumberExpression) sqlExpression).getNumber().intValue();
                selectStatement.getLimit().setRowCount(new LimitValue(rowCount, -1));
                selectStatement.getSqlTokens().add(
                        new RowCountToken(getLexer().getCurrentToken().getEndPosition() - String.valueOf(rowCount).length() - getLexer().getCurrentToken().getLiterals().length(), rowCount));
            } else if (sqlExpression instanceof PlaceholderExpression) {
                selectStatement.getLimit().setRowCount(new LimitValue(-1, ((PlaceholderExpression) sqlExpression).getIndex()));
            }
        } else if (Symbol.GT == symbol || Symbol.GT_EQ == symbol) {
            if (sqlExpression instanceof NumberExpression) {
                int offset = ((NumberExpression) sqlExpression).getNumber().intValue();
                selectStatement.getLimit().setOffset(new LimitValue(offset, -1));
                selectStatement.getSqlTokens().add(
                        new OffsetToken(getLexer().getCurrentToken().getEndPosition() - String.valueOf(offset).length() - getLexer().getCurrentToken().getLiterals().length(), offset));
            } else if (sqlExpression instanceof PlaceholderExpression) {
                selectStatement.getLimit().setOffset(new LimitValue(-1, ((PlaceholderExpression) sqlExpression).getIndex()));
            }
        }
    }

    /**
     * 解析其他条件。目前其他条件包含 LIKE, <, <=, >, >=
     *
     * @param sqlStatement SQL
     */
    private void parseOtherCondition(final SQLStatement sqlStatement) {
        getLexer().nextToken();
        parseExpression(sqlStatement);
    }

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
