package com.nng.lexical_analysis.analysis.mean_analyzer;

import com.nng.lexical_analysis.analysis.word_analyzer.TreesesLexer;

/**
 * TreesesSQL解析器.
 */
public final class TreeseParser extends SQLParser {
    
    public TreeseParser(final String sql) {
        super(new TreesesLexer(sql));
    }
}
