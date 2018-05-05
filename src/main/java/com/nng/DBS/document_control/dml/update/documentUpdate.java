package com.nng.DBS.document_control.dml.update;

import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.DBS.document_control.dql.select.temporarytype.columnsType;
import net.sf.json.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class documentUpdate {

    public documentUpdate(String tablename,List<columnsType> columnsContent) throws Exception {
        List<JSONArray> resultss=new ArrayList<>();
        for(int i=0;i<columnsContent.size();i++)
        {
            JSONArray results = new JSONArray();
            for(int j=0;j<columnsContent.get(i).getItem().size();j++)
            {
                results.add(j,columnsContent.get(i).getItem().get(j));
            }
            resultss.add(results);
        }
        write_Table(tablename,resultss);
    }



    private void write_Table(String table_name,List<JSONArray> content) throws Exception {
        for(int i=0;i<content.size();i++)
        {
            String strs = content.get(i).toString();
            String table_path= TablerParser.getInstance().get_address(table_name);
            if(i==0)
            {
                try {
                    //构造函数中的第二个参数true表示以追加形式写文件
                    FileWriter fw = new FileWriter(table_path,false);
                    fw.write(strs);
                    fw.write('\n');
                    fw.close();
                } catch (IOException e) {
                   // System.out.println("文件写入失败！" + e);
                    throw e;
                }
            }
            else
            {
                try {
                    //构造函数中的第二个参数true表示以追加形式写文件
                    FileWriter fw = new FileWriter(table_path,true);
                    fw.write(strs);
                    fw.write('\n');
                    fw.close();
                } catch (IOException e) {
                  //  System.out.println("文件写入失败！" + e);
                    throw e;
                }

            }
        }



    }
}
