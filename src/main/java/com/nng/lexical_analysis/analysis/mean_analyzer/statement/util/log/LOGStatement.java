package com.nng.lexical_analysis.analysis.mean_analyzer.statement.util.log;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.AbstractSQLStatement;
import com.nng.lexical_analysis.contact.SQLType;
import com.nng.lexical_analysis.contact.controlType;

public class LOGStatement extends AbstractSQLStatement {
    com.nng.lexical_analysis.contact.controlType controlType= com.nng.lexical_analysis.contact.controlType.LOG;
    public LOGStatement() {
        super(SQLType.DDL);
    }
}
