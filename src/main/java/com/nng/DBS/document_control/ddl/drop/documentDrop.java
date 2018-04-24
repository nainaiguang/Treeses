package com.nng.DBS.document_control.ddl.drop;

import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.exception.TreesesException;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * 单例模式
 * 在文件路径上删除一个文件
 *
 */
@NoArgsConstructor
public class documentDrop {

    private static documentDrop document_Drop;

    public static documentDrop getInstance()
    {
        if(document_Drop==null)
        {
            document_Drop=new documentDrop();
        }
        return document_Drop;
    }

    public void drop_table(String table_name) throws Exception {
        String table_path= TablerParser.getInstance().get_address(table_name);
        File myDelFile = new File(table_path);
         try {
                myDelFile.delete();
         }
        catch (Exception e) {
            throw new TreesesException("删除文件操作出错");
         }

    }

}
