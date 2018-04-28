package com.nng.DBS;

import com.nng.DBS.document_control.ddl.create.createControl;
import com.nng.DBS.document_control.ddl.drop.dropControl;
import com.nng.DBS.document_control.dml.delete.deleteControl;
import com.nng.DBS.document_control.dml.insert.InsertControl;
import com.nng.DBS.document_control.dml.update.updateControl;
import com.nng.DBS.document_control.dql.select.selectControl;
import com.nng.lexical_analysis.analysis.SQLparsingEngine;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.select.SelectStatement;
import com.nng.lexical_analysis.contact.controlType;

public class SQLEngine {
    private static SQLEngine Obj;

    /**
     * 单例模式
     * 获取本类实体
     * @return
     */
    public static SQLEngine getInstance()
    {
        if(Obj==null)
        {
            Obj=new SQLEngine();
        }
        return Obj;
    }

    public void runSQLEngine(String sql)throws Exception
    {

        SQLparsingEngine a=new SQLparsingEngine(sql);
        SQLStatement b= a.parse();
        System.out.println(b.getControlType());
            if (b.getControlType() == controlType.CREATE) {
                createControl.getInstance().create_table((DDLStatement) b);
            } else if (b.getControlType() == controlType.ALTER) {
            } else if (b.getControlType() == controlType.DROP) {
                dropControl.getInstance().drop_Table((DDLStatement) b);
            } else if (b.getControlType() == controlType.TRUNCATE) {
            } else if (b.getControlType() == controlType.DELETE) {
                new deleteControl((DMLStatement)b);
            } else if (b.getControlType() == controlType.INSERT) {
                InsertControl.getInstance().insert_table((DMLStatement) b);
            } else if (b.getControlType() == controlType.UPDATE) {
                new updateControl((DMLStatement) b);
            } else if (b.getControlType() == controlType.SELECT) {
                new selectControl((SelectStatement) b);
            }
    }
}
