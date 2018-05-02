package com.nng.lexical_analysis.analysis.mean_analyzer.statement.util.log;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatementParser;
import com.nng.lexical_analysis.contact.controlType;

public final class TreesesLogParser implements SQLStatementParser {
    @Override
    public SQLStatement parse() {
        LOGStatement statement=new LOGStatement();
        statement.setControlType(controlType.LOG);
        return statement;
    }
}
