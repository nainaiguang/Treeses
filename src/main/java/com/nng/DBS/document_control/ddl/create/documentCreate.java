package com.nng.DBS.document_control.ddl.create;

import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.exception.TreesesException;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * 单例模式
 * 在文件路径上建立一个文件
 */
@NoArgsConstructor
public class documentCreate {

    private static documentCreate document_Create;

    public static documentCreate getInstance()
    {
        if(document_Create==null)
        {
            document_Create=new documentCreate();
        }
        return document_Create;
    }

    public void creat_Table(String table_name) throws Exception {
        String table_path= TablerParser.getInstance().get_address(table_name);

         //import java.io.*;
         File myFilePath = new File(table_path);
         try {
             if (!myFilePath.exists())
                 {
                     myFilePath.createNewFile();
                     myFilePath.setReadOnly();//设置只读
                 }
             }

         catch (Exception e)
         {
             throw new TreesesException("新建文件操作出错");
         }
    }
}
