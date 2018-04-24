package com.nng.lexical_analysis.analysis.mean_analyzer.exception;


import com.nng.lexical_analysis.analysis.word_analyzer.token.TokenType;
import com.nng.lexical_analysis.analysis.word_analyzer.Lexer;
import com.nng.exception.TreesesException;


public class SQLParsingException extends TreesesException {
    
    private static final long serialVersionUID = -6408790652103666096L;
    
    private static final String UNMATCH_MESSAGE = "SQL syntax error, expected token is '%s', actual token is '%s', literals is '%s'.";
    
    private static final String TOKEN_ERROR_MESSAGE = "SQL syntax error, token is '%s', literals is '%s'.";
    
    public SQLParsingException(final String message, final Object... args) {
        super(message, args);
    }
    
    public SQLParsingException(final Lexer lexer, final TokenType expectedTokenType) {
        super(String.format(UNMATCH_MESSAGE, expectedTokenType, lexer.getCurrentToken().getType(), lexer.getCurrentToken().getLiterals()));
    }
    
    public SQLParsingException(final Lexer lexer) {
        super(String.format(TOKEN_ERROR_MESSAGE, lexer.getCurrentToken().getType(), lexer.getCurrentToken().getLiterals()));
    }
}
