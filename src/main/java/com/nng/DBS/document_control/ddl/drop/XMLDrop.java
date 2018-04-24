package com.nng.DBS.document_control.ddl.drop;

import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;

/**
 * 单例模式
 * 在dictionary上XML删除一行表单
 */
public class XMLDrop {
    private static XMLDrop XML_Drop;

    public static XMLDrop getInstance()
    {
        if(XML_Drop==null)
        {
            XML_Drop=new XMLDrop();
        }
        return XML_Drop;
    }

    public void drop_Table(DDLStatement drop) throws Exception
    {
        String table_name=drop.getTables().getSingleTableName();
        TablerParser.getInstance().drop_table(table_name);
    }
}

