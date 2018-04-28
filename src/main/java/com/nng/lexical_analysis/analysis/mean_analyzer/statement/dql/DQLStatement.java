package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.AbstractSQLStatement;
import com.nng.lexical_analysis.contact.SQLType;
import com.nng.lexical_analysis.contact.controlType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
    /**
     * DQL语句对象.
     */
    @Getter
    @Setter
    @ToString(callSuper = true)
    public class DQLStatement extends AbstractSQLStatement {

        public DQLStatement() {
            super(SQLType.DQL);
        }


    }


