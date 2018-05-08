package com.nng.DBS.document_control.dql.select;

import com.google.common.base.Optional;
import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.DBS.document_control.dql.select.temporarytype.selectItemResult;
import com.nng.exception.SQLDictionaryException;
import com.nng.exception.documentException;
import com.nng.DBS.document_control.dql.select.temporarytype.Table_contact;
import com.nng.DBS.document_control.dql.select.temporarytype.columnsType;
import com.nng.DBS.document_control.dql.select.temporarytype.crossjoinTable;
import com.nng.exception.TreesesException;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.OrderItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.*;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.limit.Limit;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.AggregationSelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.CommonSelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.SelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Table;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.select.SelectStatement;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import com.nng.lexical_analysis.contact.OrderType;
import com.nng.lexical_analysis.contact.ShardingOperator;
import com.nng.unit.readJson;
import com.nng.unit.removeDuplicate;
import lombok.Getter;

import java.io.*;
import java.util.*;

//    SELECT
//    [ALL | DISTINCT | DISTINCTROW ]
//            [HIGH_PRIORITY]
//            [STRAIGHT_JOIN]
//            [SQL_SMALL_RESULT] [SQL_BIG_RESULT] [SQL_BUFFER_RESULT]
//            [SQL_CACHE | SQL_NO_CACHE] [SQL_CALC_FOUND_ROWS]
//            select_expr [, select_expr ...]
//            [FROM table_references
//              [PARTITION partition_list]
//            [WHERE where_condition]
//            [GROUP BY {col_name | expr | position}
//              [ASC | DESC], ... [WITH ROLLUP]]
//            [HAVING where_condition]
//            [ORDER BY {col_name | expr | position}
//              [ASC | DESC], ...]
//            [LIMIT {[offset,] row_count | row_count OFFSET offset}]
//            [PROCEDURE procedure_name(argument_list)]
//            [INTO OUTFILE 'file_name'
//               [CHARACTER SET charset_name]
//               export_options
//              | INTO DUMPFILE 'file_name'
//              | INTO var_name [, var_name]]
//            [FOR UPDATE | LOCK IN SHARE MODE]]


/**
 * select 处理顺序
 * 1、FORM: 对FROM左边的表和右边的表计算笛卡尔积，产生虚表VT1。
 * 2、ON: 对虚表VT1进行ON过滤，只有那些符合<join-condition>的行才会被记录在虚表VT2中。
 * 3、JOIN： 如果指定了OUTER JOIN（比如left join、 right join），那么保留表中未匹配的行就会作为外部行添加到虚拟表VT2中，产生虚拟表VT3。
 * 4、WHERE： 对虚拟表VT3进行WHERE条件过滤。只有符合<where-condition>的记录才会被插入到虚拟表VT4中。
 * 5、GROUP BY: 根据group by子句中的列，对VT4中的记录进行分组操作，产生VT5。
 * 6、HAVING： 对虚拟表VT5应用having过滤，只有符合<having-condition>的记录才会被 插入到虚拟表VT6中。
 * 7、SELECT： 执行select操作，选择指定的列，插入到虚拟表VT7中。
 * 8、DISTINCT： 对VT7中的记录进行去重。产生虚拟表VT8.
 * 9、ORDER BY: 将虚拟表VT8中的记录按照<order_by_list>进行排序操作，产生虚拟表VT9.
 * 10、LIMIT：取出指定行的记录，产生虚拟表VT10, 并将结果返回。
 */

//select 的 item 有两种，一种是普通，一种是聚合函数，需要注意
public class selectControl {
    @Getter
    String res="";
    /**
     * 解析之后的结果
     */
    SelectStatement selectStatement;
    /**
     * 是否去除重复项
     */
    Boolean distinct;
    /**
     * 是否是*
     */
    Boolean star;
    /**
     * from的表名字
     */
    List<String> tables_name=new ArrayList<>();
    /**
     * 普通运算的条件
     */
    Conditions conditions;
    /**
     * 列相等的条件
     */
    ColumnConditions columnConditions;
    /**
     * 查询项,聚合函数处理要注意
     */
    private  List<SelectItem> items;
    /**
     * 分组项
     */
    private  List<OrderItem> groupByItems;
    /**
     * 排序项
     */
    private  List<OrderItem> orderByItems;
    /**
     * 分页
     */
    private Limit limit;
    /**
     * from的表内容
     */
    List<Table_contact> table_contacts=new ArrayList<>();

    /**
     * 笛卡尔积结果
     */
    crossjoinTable crossjoinTables=new crossjoinTable();

    public selectControl(SelectStatement selectStatement) throws Exception {
        //先进行语法语义内容判断

        this.selectStatement=selectStatement;
        //是否去除重复列
        this.distinct=selectStatement.isDistinct();
        //是否为*
        this.star=selectStatement.isContainStar();
        //所有查询表的表名称
        for(String table:selectStatement.getTables().getTableNames())
        {
            this.tables_name.add(table);
        }
        //添加所有查询项
        this.items=selectStatement.getItems();
        //添加普通条件
        this.conditions=selectStatement.getConditions();
        //添加列相等查询条件
        this.columnConditions=selectStatement.getColumnConditions();
        //添加分组条件
        this.groupByItems=selectStatement.getGroupByItems();
        //添加order By条件
         this.orderByItems=selectStatement.getOrderByItems();
        //添加分页限制条件
        this.limit=selectStatement.getLimit();


        /**
         * 进行处理
         */
        //先进行语法判断
        judge();
        //处理from中的事务
        deal_from();

        deal_where();

        deal_groupby();


        deal_selectitem();

        deal_orderby();

        tidyResult(crossjoinTables);
//    for(columnsType oo:crossjoinTables.getColumnsContent())
//    {
//        for(Object pp:oo.getItem())
//        {
//            System.out.print(pp+"\t");
//        }
//        System.out.println();
//    }
//
//    for(selectItemResult sl:crossjoinTables.getResultTable_structures())
//    {
//        System.out.printf(sl.getItemname()+":");
//        for(Object obj:sl.getColumn_content())
//        {
//            System.out.printf(obj+"\t");
//        }
//        System.out.println();
//    }

    }
    private void tidyResult(crossjoinTable crossjoinTables)
    {
        String result="";
        List<String> kongge=new ArrayList<>();

        if(crossjoinTables.getResultTable_structures().size()>0) {
            for(selectItemResult sl:crossjoinTables.getResultTable_structures())
            {
                result=result+sl.getItemname()+"\t";
                String tempkongge="";
                for(int i=1;i<sl.getItemname().length();i++)
                {
                    tempkongge=tempkongge+" ";
                }
                kongge.add(tempkongge);
            }
            result=result+"\n";
            for (int i = 0; i < crossjoinTables.getResultTable_structures().get(0).getColumn_content().size();i++)
            {
                for(int j=0;j<crossjoinTables.getResultTable_structures().size();j++)
                {
                    selectItemResult sl=crossjoinTables.getResultTable_structures().get(j);
                    result=result+sl.getColumn_content().get(i)+kongge.get(j)+"\t";
                }
                result=result+"\n";
            }
        }
        res=result;
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
     * 处理where条件
     * @throws Exception
     */
    public void deal_where() throws Exception
    {
        //处理列相等的事务
        deal_on();
        deal_equalORNotequal();
        crossjoinTables.checkAv(items);
        if(groupByItems.size()!=0)
        {
            crossjoinTables.setGroupby(true);
        }
        deal_orderby();
        crossjoinTables.setGroupby(false);
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
        for(columnCondition columnCondition:columnCs)
        {
            //当符号不是等于的情况
            if(columnCondition.getSymbol()!=Symbol.EQ)
            {
                throw new documentException("sorry,It not support the condition when item"+columnCondition.getSymbol().toString()+"item");
            }

            //表名与列名
            String TablenameA=columnCondition.getA().getTableName();
            String nameA=columnCondition.getA().getName();
            String TablenameB=columnCondition.getB().getTableName();
            String nameB=columnCondition.getB().getName();

            this.crossjoinTables.ColumnEQColumn(TablenameA,nameA,TablenameB,nameB);
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
                this.crossjoinTables.ColumnEQorNotEQValues(key.getTableName(), key.getName(), val);
            }
            else{//是in或between
                if(val.getOperator().equals(ShardingOperator.IN))
                {

                    this.crossjoinTables.ColumnInValues(key.getTableName(),key.getName(),val);
                }
                else if(val.getOperator().equals(ShardingOperator.BETWEEN))
                {
                    this.crossjoinTables.ColumnBetweenValues(key.getTableName(),key.getName(),val);
                }
            }
        }
    }

    private void deal_groupby()throws Exception
    {
        if(groupByItems.isEmpty())
        {
            return;
        }
        if(groupByItems.size()>1)
        {
            throw new Exception("sorry,group by only support 1 item now");
        }
        this.crossjoinTables.GroupBy(selectStatement.getTables(),groupByItems.get(0));

    }
    private void deal_orderby() throws Exception
    {
        if(orderByItems.isEmpty())
        {
            return;
        }
        if(orderByItems.size()>1)
        {
            throw new Exception("sorry,order by only support 1 item now");
        }
        Boolean paixu=false;
        if(orderByItems.get(0).getType()== OrderType.DESC)
        {
            paixu=false;
        }
        else if(orderByItems.get(0).getType()== OrderType.ASC)
        {
            paixu=true;
        }
        this.crossjoinTables.deal_orderby(selectStatement.getTables(),orderByItems.get(0),paixu);
    }

    private void deal_selectitem()throws Exception
    {
        this.crossjoinTables.Selectitem(items,this.star,selectStatement.getTables());
    }

    /**
     * 取出当前的column 在原来里面是第几个
     * 从0开始算
     */
    private int getColumnPosition(String table_name,String columnname) throws Exception {
        List<String> columns=TablerParser.getInstance().get_column(table_name);
        for(int i=0;i<columns.size();i++)
        {
            if(columns.get(i).equals(columnname))
                return i;
        }
       return -1;
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
     * 2.传回来的列，是否原表是否有，包括 别名.啥啥啥
     * 3.是否有重复别名
     * 4.where中的条件的项
     */
    private void judge() throws Exception {
        //1.
        Collection<String> tablessname=this.selectStatement.getTables().getTableNames();
        List<String> columns=new ArrayList<>();//存在的表的所有列
        for(String str:tablessname)
        {
            if(!TablerParser.getInstance().existTable(str))
            {
            throw new SQLDictionaryException(str);
            }
            columns.addAll(TablerParser.getInstance().get_column(str));
        }

        //2.
        for(SelectItem selectItem:selectStatement.getItems())
        {
            Boolean check=false;
            if(selectItem.getClass().getName().equals(CommonSelectItem.class.getName()))
            {
                String[] temp=strToArray(selectItem.getExpression());
                if(temp[temp.length-1].equals("*"))//星号则通过
                {
                    check=true;
                }
               for(String column_name:columns)
               {
                   String[] its= strToArray(selectItem.getExpression());
                   int itss=its.length;
                   if(itss>2)
                   {
                       throw new Exception("SQL statement error \""+selectItem.getExpression()+"\"");
                   }
                   if(column_name.equals(its[itss-1]))//如果列存在在表中，则没有错
                   {
                       check=true;
                   }
               }
            }
            else if(selectItem.getClass().getName().equals(AggregationSelectItem.class.getName()))//聚合函数直接给过
            {
                check=true;
            }

            if(!check)//列不存在在表中，则直接报错
            {
                throw new SQLDictionaryException(selectItem.getExpression());
            }
        }
        //3
        judge_repetition_alia();
        //4
        ConditionscolumnExists();
    }

    private String[] strToArray(String str) {
        StringTokenizer st = new StringTokenizer(str, ".");
        String[] strArray = new String[st.countTokens()];
        int strLeng = st.countTokens();
        for (int i=0; i<strLeng; i++) {
            strArray[i] = st.nextToken();
        }
        return strArray;
    }

    /**
     * 判断是否重复别名
     */
    private void judge_repetition_alia()
    {
        List<String> Alias = new ArrayList<>();

        //添加item别名
        for(SelectItem selectItem:this.selectStatement.getItems())
        {
            if(selectItem.getAlias().isPresent())
            {
                Alias.add(selectItem.getAlias().get());
            }
        }

        //添加表别名
        for(String tablename:this.selectStatement.getTables().getTableNames())
        {
            Table table=selectStatement.getTables().find(tablename).orNull();
            if(table!=null)
            {
                if(table.getAlias().isPresent())
                {
                    Alias.add(table.getAlias().orNull());
                }
            }
        }
        int size1=Alias.size();
        Alias= removeDuplicate.remove(Alias);
        if(size1!=Alias.size())
        {
            throw new TreesesException("it has Duplicate alias");
        }


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

            Optional<Table> tableA=this.selectStatement.getTables().find(TablenameA);
            Optional<Table> tableB=this.selectStatement.getTables().find(TablenameB);
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