package com.nng.DBS.document_control.dql.select.temporarytype;

import com.nng.DBS.dictionary.type.Tabletype;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * From表与表内容
 */
public class Table_contact extends Tabletype{
    @Getter
    private List<columnsType> columns_content=new ArrayList<>();//列内容

    @Getter
    @Setter
    private String AliaOnSelect=null;

    public void add_columns_content(columnsType one)
    {
        columns_content.add(one);
    }

}
