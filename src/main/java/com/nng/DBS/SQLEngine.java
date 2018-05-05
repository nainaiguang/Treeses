package com.nng.DBS;

import com.nng.DBS.document_control.ddl.create.createControl;
import com.nng.DBS.document_control.ddl.drop.dropControl;
import com.nng.DBS.document_control.dml.delete.deleteControl;
import com.nng.DBS.document_control.dml.insert.InsertControl;
import com.nng.DBS.document_control.dml.update.updateControl;
import com.nng.DBS.document_control.dql.select.selectControl;
import com.nng.DBS.log.logControl.LogEngine;
import com.nng.DBS.softParse.parseResult;
import com.nng.DBS.softParse.softParse;
import com.nng.lexical_analysis.analysis.SQLparsingEngine;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.update.updateStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.select.SelectStatement;
import com.nng.lexical_analysis.contact.controlType;
import lombok.Getter;

import java.util.List;

public class SQLEngine {
    @Getter
    String result=null;
    public void runSQLEngine(String sql)throws Exception
    {

        SQLparsingEngine a=new SQLparsingEngine(sql);
        long startTime=System.currentTimeMillis();//获取开始时间

        SQLStatement b;
        b=softParse.getInstance().getStatementFromExitsResult(sql);
        if(b==null)//是否执行软解析
        { b= a.parse();}


            if (b.getControlType() == controlType.CREATE) {
                try {
                    createControl createControls = new createControl();
                    createControls.create_table((DDLStatement) b);
                    result = createControls.getResult();
                    (new LogEngine()).addNewLog(controlType.CREATE, sql, true);
                    //只添加正确的软解析
                    softParse.getInstance().addparseResult(sql,b);
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
                    //只添加正确的软解析
                    softParse.getInstance().addparseResult(sql,b);
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
                    //只添加正确的软解析
                    softParse.getInstance().addparseResult(sql,b);
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
                    //只添加正确的软解析
                    softParse.getInstance().addparseResult(sql,b);
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
                    //只添加正确的软解析
                    softParse.getInstance().addparseResult(sql,b);
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
                    //只添加正确的软解析
                    softParse.getInstance().addparseResult(sql,b);
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



        List<parseResult> o=softParse.getInstance().getParseResults();
//        for(parseResult results:o)
//        {
//            System.out.println(results.getSql());
//        }
    }


}
