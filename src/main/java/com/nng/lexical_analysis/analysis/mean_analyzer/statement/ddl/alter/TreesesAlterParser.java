package com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.alter;


import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Keyword;

/**
 * Alter语句解析器.
 */
public final class TreesesAlterParser extends AbstractAlterParser {
    
    public TreesesAlterParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    protected Keyword[] getSkipWordsBetweenKeywordAndTableName() {
        return new Keyword[] {};
    }
}
