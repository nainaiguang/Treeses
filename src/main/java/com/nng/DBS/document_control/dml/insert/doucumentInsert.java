package com.nng.DBS.document_control.dml.insert;

import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.exception.documentException;
import com.nng.exception.TreesesException;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Column;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Condition;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Conditions;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.insert.InsertStatement;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import net.sf.json.JSONArray;

import java.io.*;
import java.util.*;

// 第一种
//    INSERT [LOW_PRIORITY | DELAYED | HIGH_PRIORITY] [IGNORE]
//            [INTO] tbl_name
//    [PARTITION (partition_name,...)]
//            [(col_name,...)]
//    {VALUES | VALUE} ({expr | DEFAULT},...),(...),...
//            [ ON DUPLICATE KEY UPDATE
//    col_name=expr
//        [, col_name=expr] ... ]

// 第二种
//    INSERT [LOW_PRIORITY | DELAYED | HIGH_PRIORITY] [IGNORE]
//            [INTO] tbl_name
//    [PARTITION (partition_name,...)]
//    SET col_name={expr | DEFAULT}, ...
//            [ ON DUPLICATE KEY UPDATE
//    col_name=expr
//        [, col_name=expr] ... ]

public class doucumentInsert {

    private static doucumentInsert xmlInsert;

    public static doucumentInsert getInstance()
    {
        if(xmlInsert==null)
        {
            xmlInsert=new doucumentInsert();
        }
        return xmlInsert;
    }

    /**
     * 返回完整的插入，即表中所有类的
     * @param insert
     * @return
     * @throws Exception
     */
    public <T> void insert_table(InsertStatement insert) throws Exception
    {
        /**
         * 4种报错情况
         * 1.存在列名写错了，表里没有的
         * 2.存在数据类型不匹配的
         * 3.列名多但插入数据少,解析是报错
         * 4.列名少，插入数据多
         */
        String table_name=insert.getTables().getSingleTableName();
        List<String> xml_column=getColumnsfromXML(table_name);
        LinkedList<String> doucument_column=getColumnsfromDDLStatement(insert);
        String temp=isAllRight(xml_column,doucument_column);


        //1.
        if(temp!=null)
        {
           throw new documentException(temp);
        }

        //获取
        Conditions conditions = insert.getConditions();

        LinkedList<Column> columns= (LinkedList<Column>) insert.getColumns();


        //获取对应列的数据
        List<Object> result= new ArrayList<>();
        for(int i=0;i<columns.size();i++)
        {
            Condition condition=conditions.find(columns.get(i)).orNull();
            List<String> a=new LinkedList(){};
            a.add(columns.get(i).getName());

            //2.
            if(!isTypeRight(table_name,condition.getColumn().getName(),condition.getShardingValue(Collections.singletonList(a)).getValue()))
            {
                throw  new documentException(TablerParser.getInstance().get_column_type(table_name,condition.getColumn().getName()),
                        getType(condition.getShardingValue(Collections.singletonList(a)).getValue()),
                       1);
            }
               Object temps=condition.getShardingValue(Collections.singletonList(a)).getValue();

            result.add(temps);
        }


        //4
        if(result.size()!=doucument_column.size())
        {
            throw new TreesesException("column and date number does not match");
        }

        JSONArray resultss = new JSONArray();
        for(int i=0;i<xml_column.size();i++)
        {
            boolean S=false;
            for(int j=0;j<doucument_column.size();j++)
            {
                if(xml_column.get(i).equals(doucument_column.get(j)))
                {
                    resultss.add(i,result.get(j));
                    S=true;
                }
            }
            if(!S)
            {
                resultss.add(i,null);//两点问题注意，1.如果用户存的数是NULL怎么办，2.留下一个是否可以为0的处理空间
            }
        }
        write_Table(table_name,resultss);

    }

    /**
     * 在XML文件中去除所有的列名
     * @param table_name
     * @return
     * @throws Exception
     */
    private List<String> getColumnsfromXML(String table_name) throws Exception {
        return TablerParser.getInstance().get_column(table_name);
    }


    /**
     * 在结果中取出所有的列名
     * @param insert
     * @return
     */
    private LinkedList<String> getColumnsfromDDLStatement(InsertStatement insert)
    {
        LinkedList<String> result=new LinkedList<>();
        LinkedList<Column> columns= (LinkedList<Column>) insert.getColumns();
        for(int i=0;i<columns.size();i++)
        {
            result.add(columns.get(i).getName());
        }
        return result;
    }

    /**
     *
     * @param xmlColumn
     * @param getColumn
     * @return
     * 暂留一个算法，判断一个字符串里有没有另外一个字符串
     */
    private String isAllRight(List<String> xmlColumn,LinkedList<String> getColumn)
    {
        String result=null;
        for (int i=0;i<getColumn.size();i++)
        {
            if(xmlColumn.indexOf(getColumn.get(i))==-1)
            {
                result=getColumn.get(i);
            }
        }
        return result;
    }

    /**
     * 判断类型与表类型相符合  创建表判断类型与是否存在。
     */
    private <T> boolean isTypeRight(String table_nbame,String column_name,T v) throws Exception {
       String table_column_type= TablerParser.getInstance().get_column_type(table_nbame,column_name);
       String type=getType(v);
       type=type.toUpperCase();
       
       Boolean result=false;
        if(type.equals("CLASS JAVA.LANG.INTEGER"))
        {
            if(table_column_type.equals(DefaultKeyword.INT.toString()))
            {
                result=true;
            }
        }
        if(type.equals("CLASS JAVA.LANG.CHARACTER"))
        {
            if(table_column_type.equals(DefaultKeyword.CHAR.toString()))
            {
                result=true;
            }
        }
        if(type.equals("CLASS JAVA.LANG.FLOAT"))
        {
            if(table_column_type.equals(DefaultKeyword.FLOAT.toString()))
            {
                result=true;
            }
        }
        if(type.equals("CLASS JAVA.LANG.STRING"))
        {
            if(table_column_type.equals(DefaultKeyword.VARCHAR.toString())||table_column_type.equals(DefaultKeyword.CHAR.toString()))
            {
                result=true;
            }

        }
        if(type.equals("CLASS JAVA.LANG.DOUBLE"))
        {
            if(table_column_type.equals(DefaultKeyword.DOUBLE.toString()))
            {
                result=true;
            }
        }
        return result;

    }

    /**
     * //获取变量类型方法
     * @param o
     * @return
     */
    private String getType(Object o){
        return o.getClass().toString();
    }

    private void write_Table(String table_name,JSONArray content) throws Exception {
        String strs = content.toString();
        String table_path= TablerParser.getInstance().get_address(table_name);
        File o=new File(table_path);
        o.setWritable(true);
        try {
            //构造函数中的第二个参数true表示以追加形式写文件
            FileWriter fw = new FileWriter(table_path,true);
            fw.write(strs);
            fw.write('\n');
            fw.close();
        } catch (IOException e) {
           // System.out.println("文件写入失败！" + e);
            throw e;
        }
        o.setWritable(false);
    }








}
