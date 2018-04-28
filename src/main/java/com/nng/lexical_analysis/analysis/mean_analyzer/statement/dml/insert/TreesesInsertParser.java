package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.insert;


import com.google.common.collect.Sets;
import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.*;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Column;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Condition;
import com.nng.lexical_analysis.analysis.word_analyzer.token.*;
import com.nng.unit.SQLUtil;

import java.util.Set;

/**
 * TreesesSQL Insert语句解析器.
 */
public final class TreesesInsertParser extends AbstractInsertParser {
    
    public TreesesInsertParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    protected void parseCustomizedInsert() {
        parseInsertSet();
    }

    /**
     * 解析第二种插入SQL：INSERT SET
     * 例如：
     *  INSERT INTO test SET id = 4  ON DUPLICATE KEY UPDATE name = 'doubi', name = 'hehe';
     *  INSERT INTO test SET id = 4, name = 'hehe';
     */
    private void parseInsertSet() {
        do {
            getSqlParser().getLexer().nextToken();
            // 插入字段
            Column column = new Column(SQLUtil.getExactlyValue(getSqlParser().getLexer().getCurrentToken().getLiterals()), getInsertStatement().getTables().getSingleTableName());
            getSqlParser().getLexer().nextToken();
            // 等号
            getSqlParser().accept(Symbol.EQ);
            // 【值】表达式
            Expression sqlExpression;
            if (getSqlParser().equalAny(Literals.INT)) {
                sqlExpression = new NumberExpression(Integer.parseInt(getSqlParser().getLexer().getCurrentToken().getLiterals()));
            } else if (getSqlParser().equalAny(Literals.FLOAT)) {
                sqlExpression = new NumberExpression(Double.parseDouble(getSqlParser().getLexer().getCurrentToken().getLiterals()));
            } else if (getSqlParser().equalAny(Literals.CHARS)) {
                sqlExpression = new TextExpression(getSqlParser().getLexer().getCurrentToken().getLiterals());
            } else if (getSqlParser().equalAny(DefaultKeyword.NULL)) {
                sqlExpression = new IgnoreExpression();
            } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
                sqlExpression = new PlaceholderExpression(getSqlParser().getParametersIndex());
                getSqlParser().increaseParametersIndex();
            } else {
                throw new UnsupportedOperationException("");
            }
            getSqlParser().getLexer().nextToken();
            // Condition
            if (getSqlParser().equalAny(Symbol.COMMA, DefaultKeyword.ON, Assist.END)) {
                getInsertStatement().getConditions().add(new Condition(column, sqlExpression));
            } else {
                getSqlParser().skipUntil(Symbol.COMMA, DefaultKeyword.ON);
            }
        } while (getSqlParser().equalAny(Symbol.COMMA)); // 字段以 "," 分隔
    }
    
    @Override
    protected Set<TokenType> getSkippedKeywordsBetweenTableAndValues() {
        return Sets.<TokenType>newHashSet(TreeseKeyword.PARTITION);
    }
    
    @Override
    protected Set<TokenType> getValuesKeywords() {
        return Sets.<TokenType>newHashSet(DefaultKeyword.VALUES, TreeseKeyword.VALUE);
    }
    
    @Override
    protected Set<TokenType> getCustomizedInsertKeywords() {
        return Sets.<TokenType>newHashSet(DefaultKeyword.SET);
    }
}
