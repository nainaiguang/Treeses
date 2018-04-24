package com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.drop;


import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Keyword;

/**
 * Drop语句解析器.
 */
public final class TreesesDropParser extends AbstractDropParser {
    
    public TreesesDropParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    protected Keyword[] getSkipWordsBetweenKeywordAndTableName() {
        return new Keyword[] {};
    }
}
