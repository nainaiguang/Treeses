package com.nng.DBS.document_control.ddl.create;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;
import lombok.Getter;

public class createControl {

    @Getter
    private String result;
    /**
     * 单例模式
     * 获取本类实体
     * @return
     */


    public void create_table(DDLStatement create) throws Exception {
        XMLCreate.getInstance().creat_Table(create);
        documentCreate.getInstance().creat_Table(create.getTables().getSingleTableName());
        result="create table "+create.getTables().getSingleTableName()+" success";
    }
}
