package com.nng.lexical_analysis.analysis.mean_analyzer.exception;


import com.nng.lexical_analysis.analysis.word_analyzer.token.TokenType;
import com.nng.exception.TreesesException;

public class SQLParsingUnsupportedException extends TreesesException {
    
    private static final long serialVersionUID = -4968036951399076811L;
    
    private static final String MESSAGE = "Not supported token '%s'.";
    
    public SQLParsingUnsupportedException(final TokenType tokenType) {
        super(String.format(MESSAGE, tokenType.toString()));
    }
}
