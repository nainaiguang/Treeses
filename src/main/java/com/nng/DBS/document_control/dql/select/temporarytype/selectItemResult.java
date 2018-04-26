package com.nng.DBS.document_control.dql.select.temporarytype;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Selectitem 每一个item的结果项，一定要按顺序
 */
@Getter
@Setter
public class selectItemResult {
    @Getter
    @Setter
    private String table_name;
    @Getter
    @Setter
    private String column_name;

    @Getter
    private String itemname;//显示的item的名字,如果有别名要自己设置
    @Getter
    @Setter
    private List<Object> column_content =new ArrayList<>();
    public selectItemResult(String table_name,String column_name)
    {
        this.table_name=table_name;
        this.column_name=column_name;
        itemname=table_name+"."+column_name;
    }
    public  int getCount()
    {
        return column_content.size();
    }
    public void setItemname(String itemname)
    {
        if(itemname!=null)
        {
            this.itemname=itemname;
        }
    }
}
