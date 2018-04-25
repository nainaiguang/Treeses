package com.nng.exception;

import com.nng.exception.TreesesException;

public class AggregateException  extends TreesesException {
    private static final long serialVersionUID = -6608791230103666563L;
    private static final String NONUMBER= "Aggregate no number error, '%s' no number";
    private static final String NOSUPPORT= "Aggregate not support error, '%s' not support type '%s'";
    public AggregateException(final String obj) {
        super(String.format(NONUMBER, obj));
    }
    public AggregateException(final String agr,final String type) {
        super(String.format(NOSUPPORT, agr,type));
    }
}
