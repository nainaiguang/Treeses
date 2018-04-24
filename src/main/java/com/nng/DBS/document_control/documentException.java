package com.nng.DBS.document_control;

import com.nng.exception.TreesesException;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import com.nng.lexical_analysis.contact.ShardingOperator;

public class documentException extends TreesesException {
    private static final long serialVersionUID = -6608790660103666563L;

    private static final String UNMATCH_MESSAGE = "SQL not find error, not find is '%s'";
    private static final String UNTABLE_MESSAGE = "table existed error, existed is '%s'";
    private static final String UNTYPE_MESSAGE = "type not match error, right type is '%s',type is '%s'";
    private static final String UNSYMBOL_USES_MESSAGE= "Symbol is not available error ,the '%s' is available,not available symbol is '%s'";
    private static final String UNTYPE_NOTALLMATCH="Not all types are consistent by '%s'";
    private static final String LUANUGE_NOSUPPORT=" '%s' syntax does not support type '%s'";

    public documentException(final String message, final Object... args) {
        super(message, args);
    }

    public documentException(final String obj) {
        super(String.format(UNMATCH_MESSAGE, obj));
    }
    public documentException(final String obj,int i) {
        super(String.format(UNTABLE_MESSAGE, obj));
    }
    public documentException(String right, String wrong,int position) {
        super(String.format(UNTYPE_MESSAGE,right,wrong));
    }
    public documentException(Symbol right,Symbol wrong)
    {
        super(String.format(UNSYMBOL_USES_MESSAGE,right.toString(),wrong.toString()));
    }
    public documentException(ShardingOperator shardingOperator)
    {
        super(String.format(UNTYPE_NOTALLMATCH,shardingOperator.toString()));
    }
    public documentException(ShardingOperator shardingOperator, String type)
    {
        super(String.format(LUANUGE_NOSUPPORT,shardingOperator.toString(),type));
    }
}
