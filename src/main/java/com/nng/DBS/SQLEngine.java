package com.nng.DBS;

import com.nng.DBS.document_control.ddl.create.createControl;
import com.nng.DBS.document_control.ddl.drop.dropControl;
import com.nng.DBS.document_control.dml.delete.deleteControl;
import com.nng.DBS.document_control.dml.insert.InsertControl;
import com.nng.DBS.document_control.dml.update.updateControl;
import com.nng.DBS.document_control.dql.select.selectControl;
import com.nng.DBS.log.logControl.LogEngine;
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
        long startTime=System.currentTimeMillis();//获取开始时间
        SQLStatement b= a.parse();
        System.out.println(b.getControlType());
            if (b.getControlType() == controlType.CREATE) {
                try {
                    createControl createControls = new createControl();
                    createControls.create_table((DDLStatement) b);
                    result = createControls.getResult();
                    (new LogEngine()).addNewLog(controlType.CREATE, sql, true);
                    }
                catch (Exception oo)
                {
                    (new LogEngine()).addNewLog(controlType.CREATE, sql, false);
                    throw oo;
                }

            } else if (b.getControlType() == controlType.ALTER) {
            } else if (b.getControlType() == controlType.DROP) {
                try {
               dropControl dropControls=new dropControl();
               dropControls.drop_Table((DDLStatement) b);
               result=dropControls.getResult();
                (new LogEngine()).addNewLog(controlType.DROP,sql,true);
                    }
                catch (Exception oo)
                {
                    (new LogEngine()).addNewLog(controlType.DROP, sql, false);
                    throw oo;
                }

            } else if (b.getControlType() == controlType.TRUNCATE) {
            } else if (b.getControlType() == controlType.DELETE) {
                try {
               deleteControl deleteControls= new deleteControl((DMLStatement)b);
               result=deleteControls.getRes();
                (new LogEngine()).addNewLog(controlType.DELETE,sql,true);
                }
                catch (Exception oo)
                {
                    (new LogEngine()).addNewLog(controlType.DELETE, sql, false);
                    throw oo;
                }

            } else if (b.getControlType() == controlType.INSERT) {
                try {
                InsertControl insertControl=new InsertControl();
                insertControl.insert_table((DMLStatement) b);
                result=insertControl.getResult();
                (new LogEngine()).addNewLog(controlType.INSERT,sql,true);
                }
                catch (Exception oo)
                {
                    (new LogEngine()).addNewLog(controlType.INSERT, sql, false);
                    throw oo;
                }

            } else if (b.getControlType() == controlType.UPDATE) {
                try {
               updateControl updateControls= new updateControl((updateStatement) b);
               result=updateControls.getRes();
                (new LogEngine()).addNewLog(controlType.UPDATE,sql,true);
                }
                catch (Exception oo)
                {
                    (new LogEngine()).addNewLog(controlType.UPDATE, sql, false);
                    throw oo;
                }

            } else if (b.getControlType() == controlType.SELECT) {
                try {
               selectControl selectControls= new selectControl((SelectStatement) b);
               result= selectControls.getRes();
                (new LogEngine()).addNewLog(controlType.SELECT,sql,true);
                }
                catch (Exception oo)
                {
                    (new LogEngine()).addNewLog(controlType.SELECT, sql, false);
                    throw oo;
                }
            }
            else if(b.getControlType()==controlType.LOG)
            {
                result=(new LogEngine()).readLog();
            }

        long endTime=System.currentTimeMillis(); //获取结束时间
        result=result+"\n"+Double.parseDouble((endTime-startTime)+"")/1000+"s";
    }
}
