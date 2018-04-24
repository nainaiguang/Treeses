package com.nng.DBS.document_control.ddl.create;

import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.DBS.dictionary.type.Tabletype;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 单例模式
 * 在dictionary上XML建立一行表单
 */
@NoArgsConstructor
public class XMLCreate {

    private static XMLCreate xmlCreate;

    public static XMLCreate getInstance()
    {
        if(xmlCreate==null)
        {
            xmlCreate=new XMLCreate();
        }
        return xmlCreate;
    }


    public void creat_Table(DDLStatement create) throws Exception {

        String tablename=create.getTables().getSingleTableName();
        List<Map<String,String>> colunms=create.getList();
        List<String> co=get_columns(colunms);
        List<String> ty=get_types(colunms);

        Tabletype new_table=new Tabletype();
        new_table.setTable_name(tablename);
        new_table.setOwner("SYSTEM");
        new_table.setAddress("SYSTEM\\DATA\\USER\\SYSTEM\\TABLE\\"+tablename+".txt");

        new_table.setColumn(co);
        new_table.setAttribute(ty);

        if(create.getTables().find(create.getTables().getSingleTableName()).get().getAlias().isPresent())
        {
            new_table.setAlias(create.getTables().find(create.getTables().getSingleTableName()).get().getAlias().toString());
        }
        TablerParser.getInstance().add_table(new_table);
    }

    /**
     * 取出列名
     * @param colunms
     * @return
     */
    private List<String> get_columns(List<Map<String,String>>colunms)
    {
        List<String> colunm_name=new ArrayList<>();
        for(int i=0;i<colunms.size();i++)
        {
            Iterator it = colunms.get(i).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                Object key = entry.getKey();
               colunm_name.add(key.toString());
            }
        }
        return colunm_name;
    }

    /**
     * 取出type
     * @param colunms
     * @return
     */
    private List<String> get_types(List<Map<String,String>>colunms)
    {
        List<String> colunm_types=new ArrayList<>();
        for(int i=0;i<colunms.size();i++)
        {
            Iterator it = colunms.get(i).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                Object value = entry.getValue();
                colunm_types.add(value.toString());
            }
        }
        return colunm_types;
    }
}
