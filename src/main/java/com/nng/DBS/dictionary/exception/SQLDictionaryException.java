package com.nng.DBS.dictionary.exception;


import com.nng.exception.TreesesException;

/**
 * 字典报错情况
 */
public class SQLDictionaryException extends TreesesException {
    
    private static final long serialVersionUID = -5608790652103666563L;
    
    private static final String UNMATCH_MESSAGE = "SQL not find error, not find is '%s'";
    private static final String UNTABLE_MESSAGE = "SQL existed error, existed is '%s'";
    

    
    public SQLDictionaryException(final String message, final Object... args) {
        super(message, args);
    }
    
    public SQLDictionaryException(final String obj) {
            super(String.format(UNMATCH_MESSAGE, obj));
    }
    public SQLDictionaryException(final String obj,int i) {
        super(String.format(UNTABLE_MESSAGE, obj));
    }

}
