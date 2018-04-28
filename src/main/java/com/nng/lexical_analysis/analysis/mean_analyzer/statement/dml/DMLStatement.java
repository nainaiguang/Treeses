package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.AbstractSQLStatement;
import com.nng.lexical_analysis.contact.SQLType;
import com.nng.lexical_analysis.contact.controlType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DML语句对象.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class DMLStatement extends AbstractSQLStatement {
    public DMLStatement() {
        super(SQLType.DML);
    }
}
