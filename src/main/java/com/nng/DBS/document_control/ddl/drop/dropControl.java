package com.nng.DBS.document_control.ddl.drop;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;

public class dropControl {
    private static dropControl Obj;

    /**
     * 单例模式
     * 获取本类实体
     * @return
     */
    public static dropControl getInstance()
    {
        if(Obj==null)
        {
            Obj=new dropControl();
        }
        return Obj;
    }

    public void drop_Table(DDLStatement drop) throws Exception {
        documentDrop.getInstance().drop_table(drop.getTables().getSingleTableName());
        XMLDrop.getInstance().drop_Table(drop);
    }

}
