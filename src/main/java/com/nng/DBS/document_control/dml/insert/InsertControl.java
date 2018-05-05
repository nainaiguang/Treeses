package com.nng.DBS.document_control.dml.insert;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.insert.InsertStatement;
import lombok.Getter;

/**
 * 做插入的总管理
 */

public class InsertControl {
    @Getter
    String result=null;
    public void insert_table(DMLStatement insert) throws Exception {
        doucumentInsert.getInstance().insert_table((InsertStatement) insert);
        result="insert column into table"+insert.getTables().getSingleTableName()+" success";
    }

}
