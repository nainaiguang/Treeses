package com.nng.lexical_analysis.analysis.word_analyzer;

import com.nng.lexical_analysis.analysis.word_analyzer.analyzer.Dictionary;
import com.nng.lexical_analysis.analysis.word_analyzer.token.TreeseKeyword;

/**
 * MySQL词法解析器.模仿MYSQL
 */
public final class TreesesLexer extends Lexer {

    /**
     * 字典
     */
    private static Dictionary dictionary = new Dictionary(TreeseKeyword.values());
    
    public TreesesLexer(final String input) {
        super(input, dictionary);
    }
    
    @Override
    protected boolean isHintBegin() {
        return '/' == getCurrentChar(0) && '*' == getCurrentChar(1) && '!' == getCurrentChar(2);
    }
    
    @Override
    protected boolean isCommentBegin() {
        return '#' == getCurrentChar(0) || super.isCommentBegin();
    }
    
    @Override
    protected boolean isVariableBegin() {
        return '@' == getCurrentChar(0);
    }
}
