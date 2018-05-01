package com.nng.DBS.document_control.ddl.drop;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;
import lombok.Getter;

public class dropControl {
    @Getter
    String result=null;

    public void drop_Table(DDLStatement drop) throws Exception {
        documentDrop.getInstance().drop_table(drop.getTables().getSingleTableName());
        XMLDrop.getInstance().drop_Table(drop);
        result="delete table"+drop.getTables().getSingleTableName()+" success";
    }

}
