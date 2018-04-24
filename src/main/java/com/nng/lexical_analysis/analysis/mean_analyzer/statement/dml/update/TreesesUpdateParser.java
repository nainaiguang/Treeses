package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.update;

import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.word_analyzer.token.TreeseKeyword;

/**
 * MySQL Update语句解析器.
 */
public final class TreesesUpdateParser extends AbstractUpdateParser {
    
    public TreesesUpdateParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    protected void skipBetweenUpdateAndTable() {
        getSqlParser().skipAll(TreeseKeyword.LOW_PRIORITY, TreeseKeyword.IGNORE);
    }
}
