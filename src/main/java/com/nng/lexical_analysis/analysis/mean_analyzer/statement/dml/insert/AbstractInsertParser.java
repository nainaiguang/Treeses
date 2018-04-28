package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.insert;

import com.google.common.collect.Sets;
import com.nng.exception.TreesesException;
import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.exception.SQLParsingUnsupportedException;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.Expression;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.NumberExpression;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.PlaceholderExpression;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.GeneratedKey;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Column;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Condition;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatementParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Assist;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import com.nng.lexical_analysis.analysis.word_analyzer.token.TokenType;
import com.nng.lexical_analysis.contact.controlType;
import com.nng.unit.SQLUtil;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractInsertParser implements SQLStatementParser {

    private final SQLParser sqlParser;

    private final InsertStatement insertStatement;
    /**
     * 自动生成键是第几个插入字段
     * index 从 0 开始
     */
    @Getter(AccessLevel.NONE)
    private int generateKeyColumnIndex = -1;

    public AbstractInsertParser(final SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        insertStatement = new InsertStatement();
        insertStatement.setControlType(controlType.INSERT);
    }

    // https://dev.mysql.com/doc/refman/5.7/en/insert.html

// 第一种
//    INSERT [LOW_PRIORITY | DELAYED | HIGH_PRIORITY] [IGNORE]
//            [INTO] tbl_name
//    [PARTITION (partition_name,...)]
//            [(col_name,...)]
//    {VALUES | VALUE} ({expr | DEFAULT},...),(...),...
//            [ ON DUPLICATE KEY UPDATE
//    col_name=expr
//        [, col_name=expr] ... ]

// 第二种
//    INSERT [LOW_PRIORITY | DELAYED | HIGH_PRIORITY] [IGNORE]
//            [INTO] tbl_name
//    [PARTITION (partition_name,...)]
//    SET col_name={expr | DEFAULT}, ...
//            [ ON DUPLICATE KEY UPDATE
//    col_name=expr
//        [, col_name=expr] ... ]

// 第三种
//    INSERT [LOW_PRIORITY | HIGH_PRIORITY] [IGNORE]
//            [INTO] tbl_name
//    [PARTITION (partition_name,...)]
//            [(col_name,...)]
//    SELECT ...
//            [ ON DUPLICATE KEY UPDATE
//    col_name=expr
//        [, col_name=expr] ... ]

    @Override
    public final DMLStatement parse() {
        sqlParser.getLexer().nextToken(); // 跳过 INSERT 关键字
        parseInto(); // 解析表
        parseColumns(); // 解析字段
        if (sqlParser.equalAny(DefaultKeyword.SELECT, Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        if (getValuesKeywords().contains(sqlParser.getLexer().getCurrentToken().getType())) { // 第一种插入SQL情况
            parseValues();
        } else if (getCustomizedInsertKeywords().contains(sqlParser.getLexer().getCurrentToken().getType())) { // 第二种插入SQL情况
            parseCustomizedInsert();
        }
        //没有自增主键
        return insertStatement;
    }

    /**
     * 解析表
     */
    private void parseInto() {
        // 例如，Oracle，INSERT FIRST/ALL 目前不支持
        if (getUnsupportedKeywords().contains(sqlParser.getLexer().getCurrentToken().getType())) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
        sqlParser.skipUntil(DefaultKeyword.INTO);
        sqlParser.getLexer().nextToken();
        // 解析表
        sqlParser.parseSingleTable(insertStatement);
        skipBetweenTableAndValues();
    }

    protected Set<TokenType> getUnsupportedKeywords() {
        return Collections.emptySet();
    }

    /**
     * 跳过 表 和 插入字段 中间的 Token
     * 例如 MySQL ：[PARTITION (partition_name,...)]
     */
    private void skipBetweenTableAndValues() {
        while (getSkippedKeywordsBetweenTableAndValues().contains(sqlParser.getLexer().getCurrentToken().getType())) {
            sqlParser.getLexer().nextToken();
            if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
                sqlParser.skipParentheses();
            }
        }
    }

    protected Set<TokenType> getSkippedKeywordsBetweenTableAndValues() {
        return Collections.emptySet();
    }

    /**
     * 解析插入字段
     */
    private void parseColumns() {
        Collection<Column> result = new LinkedList<>();
        if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
            String tableName = insertStatement.getTables().getSingleTableName();
           // Optional<String> generateKeyColumn = shardingRule.getGenerateKeyColumn(tableName); // 自动生成键信息
            int count = 0;
            do {
                // Column 插入字段
                sqlParser.getLexer().nextToken();
                String columnName = SQLUtil.getExactlyValue(sqlParser.getLexer().getCurrentToken().getLiterals());
                result.add(new Column(columnName, tableName));
                sqlParser.getLexer().nextToken();
                // 自动生成键
//                if (generateKeyColumn.isPresent() && generateKeyColumn.get().equalsIgnoreCase(columnName)) {
//                    generateKeyColumnIndex = count;
//                }
                count++;
            } while (!sqlParser.equalAny(Symbol.RIGHT_PAREN) && !sqlParser.equalAny(Assist.END));
            //
            insertStatement.setColumnsListLastPosition(sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length());
            //
            sqlParser.getLexer().nextToken();
        }
        insertStatement.getColumns().addAll(result);
    }



    protected Set<TokenType> getValuesKeywords() {
        return Sets.<TokenType>newHashSet(DefaultKeyword.VALUES);
    }

    /**
     * 解析值字段
     */
    private void parseValues() {
        boolean parsed = false;
        do {
            if (parsed) { // 只允许INSERT INTO 一条
                throw new UnsupportedOperationException("Cannot support multiple insert");
            }
            sqlParser.getLexer().nextToken();
            sqlParser.accept(Symbol.LEFT_PAREN);
            // 解析表达式
            List<Expression> sqlExpressions = new LinkedList<>();
            do {
                sqlExpressions.add(sqlParser.parseExpression());
            } while (sqlParser.skipIfEqual(Symbol.COMMA));
            //
            insertStatement.setValuesListLastPosition(sqlParser.getLexer().getCurrentToken().getEndPosition() - sqlParser.getLexer().getCurrentToken().getLiterals().length());
            // 解析值字段
            int count = 0;
            for (Column each : insertStatement.getColumns()) {
                Expression sqlExpression = sqlExpressions.get(count);
                insertStatement.getConditions().add(new Condition(each, sqlExpression));//缺了一个shardingRule
                if (generateKeyColumnIndex == count) { // 自动生成键
                    insertStatement.setGeneratedKey(createGeneratedKey(each, sqlExpression));
                }
                count++;
            }
            sqlParser.accept(Symbol.RIGHT_PAREN);
            parsed = true;
        }
        while (sqlParser.equalAny(Symbol.COMMA)); // 字段以 "," 分隔
    }

    /**
     * 创建 自动生成键
     *ok
     * @param column 字段
     * @param sqlExpression 表达式
     * @return 自动生成键
     */
    private GeneratedKey createGeneratedKey(final Column column, final Expression sqlExpression) {
        GeneratedKey result;
        if (sqlExpression instanceof PlaceholderExpression) { // 占位符
            result = new GeneratedKey(column.getName(), ((PlaceholderExpression) sqlExpression).getIndex(), null);
        } else if (sqlExpression instanceof NumberExpression) { // 数字
            result = new GeneratedKey(column.getName(), -1, ((NumberExpression) sqlExpression).getNumber());
        } else {
            throw new TreesesException("Generated key only support number.");
        }
        return result;
    }
    //ok
    protected Set<TokenType> getCustomizedInsertKeywords() {
        return Collections.emptySet();
    }

    //ok
    protected void parseCustomizedInsert() {
    }
}
