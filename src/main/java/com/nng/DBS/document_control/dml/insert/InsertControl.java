package com.nng.DBS.document_control.dml.insert;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.insert.InsertStatement;

/**
 * 做插入的总管理
 */

public class InsertControl {
    private static InsertControl Obj;

    /**
     * 单例模式
     * 获取本类实体
     * @return
     */
    public static InsertControl getInstance()
    {
        if(Obj==null)
        {
            Obj=new InsertControl();
        }
        return Obj;
    }

    public void insert_table(DMLStatement insert) throws Exception {
        doucumentInsert.getInstance().insert_table((InsertStatement) insert);
    }

}
