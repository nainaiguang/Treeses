package com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.create;

import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.word_analyzer.LexerEngine;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Keyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.TreeseKeyword;

/**
 * Create语句解析器.
 */
public final class TreesesCreateParser extends AbstractCreateParser {
    
    public TreesesCreateParser(final SQLParser sqlParser) {
        super( sqlParser);
    }

    @Override
    protected Keyword[] getSkipWordsBetweenCreateAndKeyword() {
        return new Keyword[] {DefaultKeyword.TEMPORARY};
    }

    @Override
    protected Keyword[] getSkipWordsBetweenKeywordAndTableName() {
        return new Keyword[] {DefaultKeyword.IF, DefaultKeyword.NOT, DefaultKeyword.EXISTS};
    }
}
