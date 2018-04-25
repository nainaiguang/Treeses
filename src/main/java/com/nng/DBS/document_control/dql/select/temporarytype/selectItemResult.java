package com.nng.DBS.document_control.dql.select.temporarytype;

import java.util.ArrayList;
import java.util.List;

/**
 * Selectitem 每一个item的结果项，一定要按顺序
 */
public class selectItemResult {
    private int count;//结果个数
    private String table_name;
    private String column_name;
    private List<Object> column_content =new ArrayList<>();
    public selectItemResult(String table_name,String column_name)
    {
        this.table_name=table_name;
        this.column_name=column_name;
    }
}
