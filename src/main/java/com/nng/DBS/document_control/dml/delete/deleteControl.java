package com.nng.DBS.document_control.dml.delete;

import com.google.common.base.Optional;
import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.DBS.document_control.dql.select.temporarytype.Table_contact;
import com.nng.DBS.document_control.dql.select.temporarytype.columnsType;
import com.nng.DBS.document_control.dql.select.temporarytype.crossjoinTable;
import com.nng.exception.SQLDictionaryException;
import com.nng.exception.TreesesException;
import com.nng.exception.documentException;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.*;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Table;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import com.nng.lexical_analysis.contact.ShardingOperator;
import com.nng.unit.readJson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class deleteControl {
   // DELETE FROM table_name [WHERE Clause]
    /**
     * from的表内容
     */
   private List<Table_contact> table_contacts=new ArrayList<>();
    /**
     * 普通运算的条件
     */
    private Conditions conditions;
    /**
     * 列相等的条件
     */
    private ColumnConditions columnConditions;

    /**
     * 所有表名
     */
    private List<String> tables_name=new ArrayList<>();
    /**
     * 笛卡尔积结果
     */
    private crossjoinTable crossjoinTables=new crossjoinTable();


    /**
     * 声明
     */
    private DMLStatement dmlStatement;

    public deleteControl(DMLStatement dmlStatement) throws Exception
    {
        if(dmlStatement.getTables().getTableNames().size()>1)
        {
            throw new Exception("delete only support one table in one time");
        }
        for(String table:dmlStatement.getTables().getTableNames())
        {
            this.tables_name.add(table);
        }
        //添加普通条件
        this.conditions=dmlStatement.getConditions();
        //添加列相等查询条件
        this.columnConditions=dmlStatement.getColumnConditions();
        this.dmlStatement=dmlStatement;
        judge();
        deal_from();
        deal_where();
        doucumentDelete dd=new doucumentDelete(tables_name.get(0),crossjoinTables.getColumnsContent());
    }


    /**
     * 读取select中的所有表的内容，留着排除用,处理from
     * 不会出现表重复的情况
     * @throws Exception
     */
    public void deal_from() throws Exception {
        //先读取表
        acceptTable();
        //对表做笛卡尔积
        for(int i=0;i<table_contacts.size();i++) {
            crossjoinTables.addTable(this.table_contacts.get(i));
        }
    }

    /**
     * 处理where条件
     * @throws Exception
     */
    public void deal_where() throws Exception
    {
        //处理列相等的事务
        deal_on();
        deal_equalORNotequal();
    }


    /**
     * 处理条件与条件相等的情况
     * 1.先找这个列的列名是否存在在表中 *
     * 2.如果存在直接执行排除 *
     * 3.如果不存在就在item里找别名，如果找得到就执行排除 *
     * 4.如果不存在就报错 *
     * 5.判断数据类型是否可以比较 *
     * 6.注意不止列的别名，还有表的别名
     * 7.判断符号，当出现不是相等的符号则报错 *
     * @throws Exception
     */
    private void deal_on() throws Exception
    {
        /**
         * 可能有一对多的相等条件，所以经过条件相等之后一定要新开一张表，同时注意表名问题。取两个条件中重复的条件
         */
        List<columnCondition> columnCs=columnConditions.getColumnConditions();
        if(columnCs.size()>0)
        {
            throw new Exception("SQL Not support,there are not support the equals between two column now");
        }
    }
    /**
     * 进行列等于值的判断
     * 1.等于的值数据类型可不可以比较
     * @throws Exception
     */
    private void deal_equalORNotequal()throws Exception
    {
        Iterator iter = conditions.getConditions().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Column key = (Column) entry.getKey();
            Condition val = (Condition) entry.getValue();
            if(val.getSymbol()!=Symbol.TILDE) {
                if(val.getSymbol()==Symbol.EQ)
                {
                    val.setSymbol(Symbol.BANG_EQ);
                }
                else  if(val.getSymbol()==Symbol.BANG_EQ)
                {
                    val.setSymbol(Symbol.EQ);
                }
                else  if(val.getSymbol()==Symbol.LT_EQ)
                {
                    val.setSymbol(Symbol.GT);
                }
                else  if(val.getSymbol()==Symbol.LT)
                {
                    val.setSymbol(Symbol.GT_EQ);
                }
                else  if(val.getSymbol()==Symbol.GT_EQ)
                {
                    val.setSymbol(Symbol.LT);
                }
                else  if(val.getSymbol()==Symbol.GT)
                {
                    val.setSymbol(Symbol.LT_EQ);
                }
                this.crossjoinTables.ColumnEQorNotEQValues(key.getTableName(), key.getName(), val);
            }
            else{//是in或between
                if(val.getOperator().equals(ShardingOperator.IN))
                {
                    throw new Exception("SQL Not support,DELETE  are not support the IN now");
                }
                else if(val.getOperator().equals(ShardingOperator.BETWEEN))
                {
                    throw new Exception("SQL Not support,DELETE  are not support the BETWEEN now");
                }
            }
        }
    }

    /**
     * 读取每一个表内容放进table_contacts中
     * @throws Exception
     */
    private void acceptTable() throws Exception {
        for(String tablename:this.tables_name)
        {
            Table_contact temp_one=new Table_contact();
            // System.out.println(selectStatement.getTables().find(tablename).orNull().getAlias().orNull());

            temp_one.setTable_name(tablename);//设置表名
            temp_one.setColumn(TablerParser.getInstance().get_column(tablename));//设置表原来的列列表

            List<String> columns_type=TablerParser.getInstance().get_columns_type(tablename); //每一个列原来的格式
            temp_one.setAttribute(columns_type);

            /**
             * 读取文件
             */
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(
                                    new File(TablerParser.getInstance().get_address(tablename)))
                            ,
                            "UTF-8"));
            String lineTxt = br.readLine() ; // 逐行读取数据
            while ((lineTxt!= null)&&(lineTxt!="")) //取出一行数
            {
                List<String> temps= readJson.change_format(lineTxt);//转化成一个个的字符串
                columnsType ones=new columnsType(); //一行元素的值的类
                //给每一个取出来的数转化格式
                for(int i=0;i<temps.size();i++) {
                    if (temps.get(i) != null) {
                        if (columns_type.get(i).equals(DefaultKeyword.INT.toString())) {
                            if (IsInt(temps.get(i))) {
                                int a = Integer.parseInt(temps.get(i));
                                ones.getItem().add(a);
                            }
                        }
                        if (columns_type.get(i).equals(DefaultKeyword.CHAR.toString())) {
                            char a = temps.get(i).charAt(0);
                            ones.getItem().add(a);
                        }
                        if (columns_type.get(i).equals(DefaultKeyword.FLOAT.toString())) {
                            if(Isfloat(temps.get(i)))
                            {
                                float a=Float.parseFloat(temps.get(i));
                                ones.getItem().add(a);
                            }
                        }
                        if (columns_type.get(i).equals(DefaultKeyword.VARCHAR.toString())) {
                            ones.getItem().add(temps.get(i));
                        }
                        if (columns_type.get(i).equals(DefaultKeyword.DOUBLE.toString())) {
                            if(IsDouble(temps.get(i)))
                            {
                                Double a=Double.parseDouble(temps.get(i));
                                ones.getItem().add(a);
                            }
                        }
                    }
                    else{
                        ones.getItem().add(null);
                    }
                }
                temp_one.add_columns_content(ones);
                lineTxt = br.readLine() ;
            }
            br.close();
            this.table_contacts.add(temp_one);
        }
    }


    /**
     * 判断是否可以转换成double
     */
    private Boolean IsDouble(String num)
    {
        try
        {
            Double.parseDouble(num);
        }
        catch (Exception exception)
        {
            return false;
        }
        return true;
    }

    /**
     * 判断是否可以转换成int
     */
    private Boolean IsInt(String num)
    {
        try
        {
            Integer.parseInt(num);
        }
        catch (Exception exception)
        {
            return false;
        }
        return true;
    }

    /**
     * 判断是否可以转换成float
     */
    private Boolean Isfloat(String num)
    {
        try
        {
            Float.parseFloat(num);
        }
        catch (Exception exception)
        {
            return false;
        }
        return true;
    }



    /**
     * 判定传回来的东西有没有语法错误
     * 1.表是否存在
     * 4.where中的条件的项
     */
    private void judge() throws Exception {
        //1.
        Collection<String> tablessname=this.dmlStatement.getTables().getTableNames();
        List<String> columns=new ArrayList<>();//存在的表的所有列
        for(String str:tablessname)
        {
            if(!TablerParser.getInstance().existTable(str))
            {
                throw new SQLDictionaryException(str);
            }
            columns.addAll(TablerParser.getInstance().get_column(str));
        }
        //4
        ConditionscolumnExists();
    }

    /**
     * 判断条件中的列或名字是否存在
     * 1.判断表是否存在
     * 2.判断列是否存在
     */
    private void ConditionscolumnExists() throws Exception {
        List<columnCondition> columnCs=columnConditions.getColumnConditions();
        for(columnCondition columnCondition:columnCs)
        {

            String TablenameA=columnCondition.getA().getTableName();
            String nameA=columnCondition.getA().getName();
            String TablenameB=columnCondition.getB().getTableName();
            String nameB=columnCondition.getB().getName();

            Optional<Table> tableA=this.dmlStatement.getTables().find(TablenameA);
            Optional<Table> tableB=this.dmlStatement.getTables().find(TablenameB);
            if(!tableA.isPresent())
            {
                throw new TreesesException(TablenameA+" not exist");
            }
            if(!tableB.isPresent())
            {
                throw new TreesesException(TablenameB+" not exist");
            }
            //判断列A存在
            List<String> columnA=TablerParser.getInstance().get_column(TablenameA);
            if(columnA.indexOf(nameA)==-1)
            {
                throw new TreesesException(nameA+" not exist");
            }
            //判断列B存在
            List<String> columnB=TablerParser.getInstance().get_column(TablenameB);
            if(columnB.indexOf(nameB)==-1)
            {
                throw new TreesesException(nameB+" not exist");
            }
        }
    }
}
