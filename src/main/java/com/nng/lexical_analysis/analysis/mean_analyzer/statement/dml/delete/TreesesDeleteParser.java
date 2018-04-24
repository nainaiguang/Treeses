package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.delete;


import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.TreeseKeyword;

/**
 * Delete语句解析器.
 */
public final class TreesesDeleteParser extends AbstractDeleteParser {
    
    public TreesesDeleteParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    protected void skipBetweenDeleteAndTable() {
        getSqlParser().skipAll(TreeseKeyword.LOW_PRIORITY, TreeseKeyword.QUICK, TreeseKeyword.IGNORE);
        getSqlParser().skipIfEqual(DefaultKeyword.FROM);
    }
}
