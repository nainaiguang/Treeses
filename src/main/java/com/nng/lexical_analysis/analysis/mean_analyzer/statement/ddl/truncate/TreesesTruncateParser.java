package com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.truncate;


import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Keyword;

/**
 * Truncate语句解析器.
 */
public final class TreesesTruncateParser extends AbstractTruncateParser {
    
    public TreesesTruncateParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    protected Keyword[] getSkipWordsBetweenKeywordAndTableName() {
        return new Keyword[] {};
    }
}
