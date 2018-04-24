package com.nng.DBS.document_control.ddl.create;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;

public class createControl {
    private static createControl Obj;

    /**
     * 单例模式
     * 获取本类实体
     * @return
     */
    public static createControl getInstance()
    {
        if(Obj==null)
        {
            Obj=new createControl();
        }
        return Obj;
    }

    public void create_table(DDLStatement create) throws Exception {
        XMLCreate.getInstance().creat_Table(create);
        documentCreate.getInstance().creat_Table(create.getTables().getSingleTableName());
    }
}
