package com.nng.lexical_analysis.analysis;

import com.nng.lexical_analysis.analysis.mean_analyzer.exception.SQLParsingException;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.select.SelectStatement;
import com.nng.lexical_analysis.analysis.word_analyzer.Lexer;
import com.nng.lexical_analysis.analysis.word_analyzer.analyzer.Dictionary;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Assist;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Keyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.TokenType;
import lombok.RequiredArgsConstructor;

/**
 * SQL判断引擎.
 */
@RequiredArgsConstructor
public final class SQLJudgeEngine {
    
    private final String sql;
    
    /**
     * 判断并获取SQL类型.
     *
     * @return SQL语句对象
     */
    public SQLStatement judge() {
        Lexer lexer = new Lexer(sql, new Dictionary());
        lexer.nextToken();
        while (true) {
            TokenType tokenType = lexer.getCurrentToken().getType();
            if (tokenType instanceof Keyword) {
                if (DefaultKeyword.SELECT == tokenType) {
                    return new SelectStatement();
                } else if (DefaultKeyword.INSERT == tokenType || DefaultKeyword.UPDATE == tokenType || DefaultKeyword.DELETE == tokenType) {
                    return new DMLStatement();
                }
            }
            if (tokenType instanceof Assist && Assist.END == tokenType) {
                throw new SQLParsingException("Unsupported SQL statement: [%s]", sql);
            }
            lexer.nextToken();
        }
    }
}
