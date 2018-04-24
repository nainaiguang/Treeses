package com.nng.exception;

/**
 * JDBC分片抛出的异常基类.
 * 
 * @author 乃乃光
 */
public class TreesesException extends RuntimeException {
    
    private static final long serialVersionUID = -1343739516839252250L;
    
    public TreesesException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }

    public TreesesException(final String message, final Exception cause) {
        super(message, cause);
    }
    
    public TreesesException(final Exception cause) {
        super(cause);
    }
}
