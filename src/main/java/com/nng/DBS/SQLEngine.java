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
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.update.updateStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.select.SelectStatement;
import com.nng.lexical_analysis.contact.controlType;
import lombok.Getter;

public class SQLEngine {
    @Getter
    String result=null;
    public void runSQLEngine(String sql)throws Exception
    {
        SQLparsingEngine a=new SQLparsingEngine(sql);
        SQLStatement b= a.parse();
        System.out.println(b.getControlType());
            if (b.getControlType() == controlType.CREATE) {
                createControl createControls=new createControl();
                createControls.create_table((DDLStatement) b);
                result=createControls.getResult();

            } else if (b.getControlType() == controlType.ALTER) {
            } else if (b.getControlType() == controlType.DROP) {
               dropControl dropControls=new dropControl();
               dropControls.drop_Table((DDLStatement) b);
               result=dropControls.getResult();
            } else if (b.getControlType() == controlType.TRUNCATE) {
            } else if (b.getControlType() == controlType.DELETE) {
               deleteControl deleteControls= new deleteControl((DMLStatement)b);
               result=deleteControls.getRes();
            } else if (b.getControlType() == controlType.INSERT) {
                InsertControl insertControl=new InsertControl();
                insertControl.insert_table((DMLStatement) b);
                result=insertControl.getResult();
            } else if (b.getControlType() == controlType.UPDATE) {
               updateControl updateControls= new updateControl((updateStatement) b);
               result=updateControls.getRes();
            } else if (b.getControlType() == controlType.SELECT) {

               selectControl selectControls= new selectControl((SelectStatement) b);
               result= selectControls.getRes();
            }
    }
}
