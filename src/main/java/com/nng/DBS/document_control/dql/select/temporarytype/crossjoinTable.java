package com.nng.DBS.document_control.dql.select.temporarytype;

import com.google.common.collect.Range;
import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.exception.AggregateException;
import com.nng.exception.SQLDictionaryException;
import com.nng.exception.documentException;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.OrderItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Condition;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.AggregationSelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.SelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Table;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Tables;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import com.nng.lexical_analysis.api.ShardingValue;
import com.nng.lexical_analysis.contact.AggregationType;
import com.nng.lexical_analysis.contact.ShardingOperator;
import com.nng.unit.Aggregate;
import com.nng.unit.sort.util.SortUtil;
import com.nng.unit.sort.util.factory.SortUtilFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.ss.formula.functions.T;

import java.util.*;

/**
 * 笛卡尔积表联合 注意深拷贝浅拷贝问题
 */
@Getter
@NoArgsConstructor
public class crossjoinTable {
    //每一个表结构
    private List<table_structure> table_structures=new ArrayList<>();
    //每一行
    @Setter
    private List<columnsType> columnsContent=new ArrayList<>();

    //结果表的每一个
    private List<selectItemResult> resultTable_structures=new ArrayList<>();


    private GroupbyResult groupbyResults;

    private Boolean Aggregat=false;
    @Setter
    private Boolean Groupby=false;

    //不同表同名情况也不怕，靠在笛卡尔中列的位置来辨认
    public void addTable(Table_contact table_contact)
    {
        //添加一个表结构
        table_structures.add(new table_structure(table_contact.getTable_name(),table_contact.getColumn(),table_contact.getAttribute()));

        if(columnsContent.size()==0)//第一次添加行直接复制
        {
            columnsContent=table_contact.getColumns_content();
            return;
        }

        List<columnsType> newColumnsContent=new LinkedList<>();

        for(columnsType A:this.columnsContent)
        {
            for(columnsType B:table_contact.getColumns_content())
            {
                columnsType newone=new columnsType();

                List<Object> AA=new LinkedList<>();//深拷贝对象
                List<Object>BB=new LinkedList<>();//深拷贝对象

                for(int i=0;i<A.getItem().size();i++)//深拷贝
                {
                    AA.add(A.getItem().get(i));
                }

                for(int i=0;i<B.getItem().size();i++)//深拷贝
                {
                    BB.add(B.getItem().get(i));
                }

                newone.setItem(AA);
                newone.getItem().addAll(BB);
                newColumnsContent.add(newone);
            }
        }

        columnsContent.clear();
        columnsContent=newColumnsContent;//把原来的覆盖

    }

    /**
     * 两列相等的条件
     * @param TablenameA
     * @param columnnameA
     * @param TablenameB
     * @param columnnameB
     * @throws Exception
     */
    public void ColumnEQColumn(String TablenameA,String columnnameA,String TablenameB,String columnnameB) throws Exception {
        //string 写的类型
        String typeA= TablerParser.getInstance().get_column_type(TablenameA,columnnameA);
        String typeB=TablerParser.getInstance().get_column_type(TablenameB,columnnameB);

        //类型
        DefaultKeyword defaultKeywordA=changeStringToDefault(typeA);
        DefaultKeyword defaultKeywordB=changeStringToDefault(typeB);

        //当数据类型匹配不正确
        if(!codidtionTypeMatch(defaultKeywordA,defaultKeywordB))
        {
            throw new documentException(defaultKeywordA.toString(),defaultKeywordB.toString(),1);
        }

        int cloumnplaceA=getColumnPlace(TablenameA,columnnameA);
        int cloumnplaceB=getColumnPlace(TablenameB,columnnameB);

        List<Integer> move=new ArrayList<>();

        /**
         * 获取不符合条件的列
         */
        if(typeA.equals("VARCHAR")&&typeB.equals("VAECHAR"))
        {
            for(int i=0;i<this.columnsContent.size();i++)
            {
                columnsType temp=columnsContent.get(i);

                if(!((String)temp.getItem().get(cloumnplaceA)).equals((String)temp.getItem().get(cloumnplaceB)))
                {
                    move.add(i);
                }
            }
        }
        else if(typeA.equals("CHAR")&&typeB.equals("CHAR"))
        {
            for(int i=0;i<this.columnsContent.size();i++)
            {
                columnsType temp=columnsContent.get(i);
                if(!(((char)temp.getItem().get(cloumnplaceA))==((char)temp.getItem().get(cloumnplaceB))))
                {
                    move.add(i);
                }
            }
        }
        else if((typeA.equals("INT")||typeA.equals("DOUBLE")||typeA.equals("FLOAT"))&&(typeB.equals("INT")||typeB.equals("DOUBLE")||typeB.equals("FLOAT")))
        {
            for(int i=0;i<this.columnsContent.size();i++)
            {
                columnsType temp=columnsContent.get(i);
                if(!(((double)temp.getItem().get(cloumnplaceA))==((double)temp.getItem().get(cloumnplaceB))))
                {
                    move.add(i);
                }
            }
        }

        /**
         * 逆向移除不符合的行
         */
        for(int i=move.size()-1;i>=0;i--)
        {
            columnsContent.remove(columnsContent.get(move.get(i)));
        }



    }

    /**
     * 等于或不是等于某个条件，但要提前判断好是什么符号，in 与between不能用  列  符号  值   不可以相反
     * @param tablename
     * @param columnname
     * @param condition
     * @throws Exception
     */
    public void ColumnEQorNotEQValues(String tablename,String columnname, Condition condition) throws Exception {
        //string 写的类型
        String dicTypes= TablerParser.getInstance().get_column_type(tablename,columnname);

        int columnplace=getColumnPlace(tablename,columnname);//在笛卡尔表中第几列

        Symbol symbol =condition.getSymbol();

        //用处不大
        List<String> haha=new ArrayList<>();
        haha.add(columnname);

        ShardingValue value=condition.getShardingValue(Collections.singletonList(haha));

        Comparable<T> A=value.getValue();
        String type=getType(A);//获取用户输入的类型

        catchType(type,dicTypes);//判定用户输入的数据类型是否与字典里的相同

        type=type.toUpperCase();//转成大写
        List<Integer> move=new ArrayList<>();//用于存储不符合的列
        if(type.equals("CLASS JAVA.LANG.INTEGER")||type.equals("CLASS JAVA.LANG.FLOAT")||type.equals("CLASS JAVA.LANG.DOUBLE"))//如果用户类型为这些类型
        {
            if(symbol==Symbol.EQ) {
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) == (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) == (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) == (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
            }
            if(symbol==Symbol.LT_EQ){
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) >= ((Double.parseDouble(temp.getItem().get(columnplace).toString()))))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) >= (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) >= (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
            }
            if(symbol==Symbol.LT){
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) > (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                          if (!(Float.parseFloat(A.toString()) > (Double.parseDouble(temp.getItem().get(columnplace).toString())))){
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) > (Double.parseDouble(temp.getItem().get(columnplace).toString())))){
                            move.add(i);
                        }
                    }
                }
            }
            if(symbol==Symbol.GT_EQ){
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) <= (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) <= (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) <= (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
            }
            if(symbol==Symbol.GT){
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) < (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) < (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) < (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
            }
            if(symbol==Symbol.BANG_EQ){
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) != (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) != (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) != (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
            }
            //其他符号再进行处理
        }
        else if(type.equals("CLASS JAVA.LANG.STRING"))//如果用户类型为这些类型
        {
            if(!((symbol==Symbol.EQ)||(symbol==Symbol.BANG_EQ)))//varchar,char只能是等于
            {
                throw new documentException(Symbol.EQ,symbol);
            }
            if(symbol==Symbol.EQ) {
                for (int i = 0; i < this.columnsContent.size(); i++) {
                    columnsType temp = columnsContent.get(i);
                    if (!(temp.getItem().get(columnplace) + "").equals(A.toString())) {
                        move.add(i);
                    }
                }
            }
            else if(symbol==Symbol.BANG_EQ){
                for (int i = 0; i < this.columnsContent.size(); i++) {
                    columnsType temp = columnsContent.get(i);
                    if ((temp.getItem().get(columnplace) + "").equals(A.toString())) {
                        move.add(i);
                    }
                }
            }
        }
        else if(type.equals("CLASS JAVA.LANG.CHARACTER"))//如果用户类型为这些类型
        {
            if(!((symbol==Symbol.EQ)||(symbol==Symbol.BANG_EQ)))//varchar,char只能是等于
            {
                throw new documentException(Symbol.EQ,symbol);
            }
            if(symbol==Symbol.EQ) {
                for (int i = 0; i < this.columnsContent.size(); i++) {
                    columnsType temp = columnsContent.get(i);
                    if (!((String) temp.getItem().get(columnplace)).equals((A.toString().charAt(0)) + "")) {
                        move.add(i);
                    }
                }
            }
            else if(symbol==Symbol.BANG_EQ) {
                for (int i = 0; i < this.columnsContent.size(); i++) {
                    columnsType temp = columnsContent.get(i);
                    if (((String) temp.getItem().get(columnplace)).equals((A.toString().charAt(0)) + "")) {
                        move.add(i);
                    }
                }
            }
            }


            /**
             * 逆向移除不符合的行
             */
            for(int i=move.size()-1;i>=0;i--)
            {
                columnsContent.remove(columnsContent.get(move.get(i)));
            }

        }


    /**
     * IN条件的判定
      * @param tablename
     * @param columnname
     * @param condition
     * @throws Exception
     */
    public void ColumnInValues(String tablename,String columnname, Condition condition)throws Exception
    {
        //string 写的类型
        String dicTypes= TablerParser.getInstance().get_column_type(tablename,columnname);

        //用处不大
        List<String> haha=new ArrayList<>();
        haha.add(columnname);

        ShardingValue value=condition.getShardingValue(Collections.singletonList(haha));//数据包

        int columnplace=getColumnPlace(tablename,columnname);//在笛卡尔表中第几列

        Collection<T> values=value.getValues();//获取用户数集

        Iterator it =values.iterator();

        List<String> types=new ArrayList<>();//用户输入类型集
        List<Object> userContent=new ArrayList<>();
        while (it.hasNext())
        {
            Object p=it.next();
           String type=p.getClass().toString();//获取用户输入的类型
            userContent.add(p);
            types.add(type);
        }

        ListTypesright(types);//判断用户输入类型是否完全一致

        String type=types.get(0);//获取第一个用户类型。
        catchType(type,dicTypes);//判定用户输入的数据类型是否与字典里的可以进行比较

        List<Integer> move=new ArrayList<>();//用于存储不符合的列

        type=type.toUpperCase();
        if(type.equals("CLASS JAVA.LANG.INTEGER")||type.equals("CLASS JAVA.LANG.FLOAT")||type.equals("CLASS JAVA.LANG.DOUBLE"))//如果用户类型为这些类型
        {
            List<Double> newuserContent=new ArrayList<>();//用户存用户数据
            for(Object o:userContent)
            {
                newuserContent.add(Double.parseDouble(o.toString()));
            }

                for (int i = 0; i < this.columnsContent.size(); i++) {
                    columnsType temp = columnsContent.get(i);
                    boolean check=false;
                      for(Double a:newuserContent)
                      {
                          if(a==Double.parseDouble(temp.getItem().get(columnplace).toString()))
                          {
                              check=true;
                              break;
                          }
                      }
                      if(!check)
                      {
                          move.add(i);
                      }
                }
            }
        else if(type.equals("CLASS JAVA.LANG.STRING")||type.equals("CLASS JAVA.LANG.CHARACTER"))
        {
            List<String> newuserContent=new ArrayList<>();//用户存用户数据
            for(Object o:userContent)
            {
                newuserContent.add(o.toString());
            }

            for (int i = 0; i < this.columnsContent.size(); i++) {
                columnsType temp = columnsContent.get(i);
                boolean check=false;
                for(String a:newuserContent)
                {
                    if(a.equals(temp.getItem().get(columnplace).toString()))
                    {
                        check=true;
                        break;
                    }
                }
                if(!check)
                {
                    move.add(i);
                }
            }
        }

        /**
         * 逆向移除不符合的行
         */
        for(int i=move.size()-1;i>=0;i--)
        {
            columnsContent.remove(columnsContent.get(move.get(i)));
        }

    }



    /**
     * between条件的判定
     * @param tablename
     * @param columnname
     * @param condition
     * @throws Exception
     */
    public void ColumnBetweenValues(String tablename,String columnname, Condition condition)throws Exception
    {
        //string 写的类型
        String dicTypes= TablerParser.getInstance().get_column_type(tablename,columnname);//字典里这一列的类型

        if(dicTypes.equals("VARCHAR")||dicTypes.equals("CHAR"))//char和varchar不支持between,仅判断了字典里的，还没有判断用户数据
        {
            throw new documentException(condition.getOperator(),dicTypes);
        }

        int columnplace=getColumnPlace(tablename,columnname);//在笛卡尔表中第几列

        //用处不大
        List<String> haha=new ArrayList<>();
        haha.add(columnname);

        ShardingValue value=condition.getShardingValue(Collections.singletonList(haha));//数据包

        Range range=value.getValueRange();//数据范围

        List<Integer> move=new ArrayList<>();//用于存储不符合的列
        Comparable<T> uservalues=null;//用户输入值的一个边界值

        try {//获取用户输入的值的边界值
            uservalues= range.lowerEndpoint();
        }
        catch (IllegalStateException e)
        {
            try {
                uservalues=range.upperEndpoint();
            }
            catch (IllegalStateException o)
            {
                throw new IllegalStateException();
            }
        }
        String type=uservalues.getClass().toString();//用户输入的数据类型

        type=type.toUpperCase();//变成大写
        if(type.equals("CLASS JAVA.LANG.STRING")||type.equals("CLASS JAVA.LANG.CHARACTER"))//between不支持char和varchar
        {
            throw new documentException(condition.getOperator(),type);
        }

        for (int i = 0; i < this.columnsContent.size(); i++) {
            columnsType temp = columnsContent.get(i);

            if(type.equals("CLASS JAVA.LANG.INTEGER"))//用户类型是int，因为如果表里的类型如果是double直接使用方法contains可能导致类型转换出问题
            {
                Comparable<T> lowerEndpoint=range.lowerEndpoint();
                Comparable<T> upperEndpoint=range.upperEndpoint();
                double a=Double.parseDouble(temp.getItem().get(columnplace).toString());
                if(!((a>=Double.parseDouble(lowerEndpoint.toString()))&&(a<=Double.parseDouble(upperEndpoint.toString()))))
                {
                    move.add(i);
                }
            }
            else if(type.equals("CLASS JAVA.LANG.FLOAT"))
            {
                double a=Double.parseDouble(temp.getItem().get(columnplace).toString());
                if(!range.contains(a))
                {
                    move.add(i);
                }
            }
            else if(type.equals("CLASS JAVA.LANG.DOUBLE"))
            {
                double a=Double.parseDouble(temp.getItem().get(columnplace).toString());
                if(!range.contains(a))
                {
                    move.add(i);
                }
            }

        }


        /**
         * 逆向移除不符合的行
         */
        for(int i=move.size()-1;i>=0;i--)
        {
            columnsContent.remove(columnsContent.get(move.get(i)));
        }
    }

    /**
     * 判断in的类型是否完全一致
     * @param types
     * @throws Exception
     */
    private void ListTypesright(List<String> types) throws Exception
    {
        String match=null;
        for(int i=0;i<types.size();i++)
        {
            if(i==0)
            {
                match=types.get(0);
            }
            else
            {
                if(!match.equals(types.get(i)))
                {
                    throw new documentException(ShardingOperator.IN);
                }
            }
        }

    }

    /**
     * 获取某个表的某个列在笛卡尔表中的第几列，找不到返回-1,从0开始算
     * @param tablename
     * @param columnname
     * @return
     */
    public   int getColumnPlace(String tablename,String columnname)
    {
        int result=-1;
        for(int i=0;i<table_structures.size();i++)
        {
            table_structure tt=table_structures.get(i);
            if(!tt.getTable_name().equals(tablename))//表名不符合
            {
                continue;
            }
            else//表名符合
            {
                for(int j=0;j<tt.getColumns_name().size();j++)
                {
                    if(tt.getColumns_name().get(j).equals(columnname))//列名符合
                    {
                        result=0;
                        for(int z=0;z<i;z++)
                        {
                           result= table_structures.get(z).getColumns_name().size()+result;
                        }
                        result=result+j;
                        return result;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 把String 转换成 Default
     * @param typeA
     * @return
     */
    private DefaultKeyword changeStringToDefault(String typeA)
    {
        DefaultKeyword defaultKeywordA=DefaultKeyword.NO;
        if(typeA.equals("INT"))
        {
            defaultKeywordA=DefaultKeyword.INT;
        }
        else if(typeA.equals("DOUBLE"))
        {
            defaultKeywordA=DefaultKeyword.DOUBLE;
        }
        else if(typeA.equals("FLOAT"))
        {
            defaultKeywordA=DefaultKeyword.FLOAT;
        }
        else if(typeA.equals("CHAR"))
        {
            defaultKeywordA=DefaultKeyword.CHAR;
        }
        else if(typeA.equals("VARCHAR"))
        {
            defaultKeywordA=DefaultKeyword.VARCHAR;
        }
        return defaultKeywordA;
    }

    /**
     * 判断条件中的类型是否相符合，包括列条件是否可以相互匹配/**
     * 列的类型是否可以项比对
     * 1.列相等条件
     * 2.普通条件(包括 =，in，between，>=,等)
     */
    private boolean codidtionTypeMatch(DefaultKeyword columnA, DefaultKeyword columnB)
    {
        int A=littleMatch(columnA);
        int B=littleMatch(columnB);
        if(A==B)
            return true;
        else
            return false;

    }
    private int littleMatch(DefaultKeyword column)
    {

        if(column==DefaultKeyword.CHAR)
            return 1;
        else if(column==DefaultKeyword.VARCHAR)
            return 2;
        else  if(column==DefaultKeyword.INT)
            return 3;
        else if(column==DefaultKeyword.DOUBLE)
            return 3;
        else if(column==DefaultKeyword.FLOAT)
            return 3;
        else
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
     * 获取变量类型方法
     * @param o
     * @return
     */
    private String getType(Object o){
        return o.getClass().toString();
    }

    /**
     * 判定用户类型与字典里的类型是否可以比较
     */
    private void catchType(String userType,String dictionType)
    {
        String type=userType.toUpperCase();
        if(type.equals("CLASS JAVA.LANG.INTEGER")||type.equals("CLASS JAVA.LANG.DOUBLE")||type.equals("CLASS JAVA.LANG.FLOAT"))
        {
            if(!((dictionType.equals("INT"))||(dictionType.equals("DOUBLE"))||(dictionType.equals("FLOAT"))))
            {
                throw new documentException(dictionType,userType,1);
            }
        }
        if(type.equals("CLASS JAVA.LANG.CHARACTER"))
        {
            if(!dictionType.equals("CHAR"))
            {
                throw new documentException(dictionType,userType,1);
            }
        }
        if(type.equals("CLASS JAVA.LANG.STRING"))
        {
            if(!(dictionType.equals("VARCHAR")||dictionType.equals("CHAR")))
            {
                throw new documentException(dictionType,userType,1);
            }
        }

    }


    /**
     * 对Groupby进行处理，只支持一个组
     * 使用前需要首先判断不为空
     * 如果没有表名  判断from的表
     * 1.列没有重复，
     * 2.列存在
     * 3.表是否存在
     * 4.别名是否存在在表中
     * @param orderItem
     * @throws Exception
     */
    public void GroupBy(Tables tables,OrderItem orderItem) throws Exception
    {
        String tablename=orderItem.getOwner().orNull();
        String columnname=orderItem.getName().orNull();

        Table lingshi=tables.find(tablename).orNull();//通过表名或者别名找出表 4.
        if(lingshi!=null)
        {
            tablename=lingshi.getName();
        }


        List<String> tablenames=new ArrayList<>();//所有表的名称
        //所有查询表的表名称
        for(String table:tables.getTableNames())
        {
            tablenames.add(table);
        }



        int checkcolumnIntableNumber=0;//判断列在不同表中一共出现了几次
        //1，2
        if(tablename==null) {
            String columnInWhatTable=null;
            for (String tableN : tablenames) {
                if (existcolumn(columnname, TablerParser.getInstance().get_column(tableN))) {//列是否存在在某个表内
                    checkcolumnIntableNumber++;
                    columnInWhatTable = tableN;
                }
            }

            if (checkcolumnIntableNumber == 0) {
                throw new SQLDictionaryException(columnname);
            } else if (checkcolumnIntableNumber > 1) {
                throw new SQLDictionaryException(columnname, 'i');
            }
            tablename = columnInWhatTable;
        }
        if(tablenames.indexOf(tablename)==-1)//3
        {
            throw new SQLDictionaryException(tablename);
        }


        int columnplace=getColumnPlace(tablename,columnname);


        List<Object> results=new ArrayList<>();
        for(int i=0;i<this.columnsContent.size();i++) {
            columnsType temp=columnsContent.get(i);
            if(results.indexOf(temp.getItem().get(columnplace))==-1)
            {
                results.add(temp.getItem().get(columnplace));
            }
        }
        this.groupbyResults=new GroupbyResult(tablename,columnname,columnplace,results);
        Groupby=true;//设置有groupby项
    }




    public void checkAv(List<SelectItem> items)
    {
        for(int i=0;i<items.size();i++)//看看有没有聚合
        {
            if(items.get(i).getClass().toString().equals("class com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.AggregationSelectItem"))
            {
                Aggregat=true;
            }
        }
    }
    /**
     * select item
     * 1.没有聚合，没有group by
     * 2.有聚合，没有group by
     * 3.没聚合,有group by
     * 4.有聚合，有group by
     * 5.*
     *
     * class com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.AggregationSelectItem
     * class com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.CommonSelectItem
     * @param items
     * @throws Exception
     */
    //结果表的每一个
    //resultTable_structures
    public void Selectitem(List<SelectItem> items,boolean star,Tables tables) throws Exception
    {
        /**
         * 判断别名有没有重复
         */
        List<String> alia=new ArrayList<>();//所有别名
        for(SelectItem o:items)//取存在的别名
        {
            if (strToArray(o.getExpression())[strToArray(o.getExpression()).length-1].equals("*")&&(o.getAlias().orNull()!=null))
            {
                throw new Exception("SQL error ,* can not have alia");
            }
            if(o.getAlias().isPresent()) {
                alia.add(o.getAlias().orNull());
            }
        }
        int aliasize=alia.size();
        alia=removeDuplicateWithOrder(alia);
        if(aliasize!=aliasize)
        {
            throw new Exception("Duplicate alia name");
        }


        for(int i=0;i<items.size();i++)//看看有没有聚合
        {
            if(items.get(i).getClass().toString().equals("class com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.AggregationSelectItem"))
            {
                Aggregat=true;
            }
        }

        if(Groupby==false&&Aggregat==false)//无group by，无聚合
        {
            List<Map<String, String>> resultColumnName = getTableandColumnFromselectItem(items, tables);//输出的item


            List<Integer> place=new ArrayList<>();//要查询项的位置

            for(Map<String,String> tc:resultColumnName)
            {
                String tname=null;
                String cname=null;

                //取表和列的键值对
                Iterator iter = tc.entrySet().iterator();
                while (iter.hasNext()) {
                 Map.Entry entry = (Map.Entry) iter.next();
                    tname = (String) entry.getKey();
                    cname = (String) entry.getValue();
                break;
                 }
                 place.add(getColumnPlace(tname,cname));
            }

         //   System.out.println(place);//place为在笛卡尔表中的位置


            for(int pl:place)
            {
                int i=0;
                for(table_structure tt:table_structures)
                {
                    for(String mm:tt.getColumns_name())
                    {
                        if(i==pl)
                        {
                            selectItemResult ItemResult= new selectItemResult(tt.getTable_name(),mm);

                            /**
                             * 添加别名
                             */
                            for(SelectItem temp:items)
                            {
                                if(temp.getExpression().equals(tt.getTable_name()+"."+mm))
                                {
                                    ItemResult.setItemname(temp.getAlias().orNull());
                                }
                            }
                            for(columnsType col:this.columnsContent)
                            {
                                ItemResult.getColumn_content().add(col.getItem().get(pl));
                            }
                            this.resultTable_structures.add(ItemResult);

                        }
                            i++;
                    }
                }
            }

        }

        else if(Groupby==true&&Aggregat==false)//有group by，无聚合
        {
            List<Map<String, String>> resultColumnName = getTableandColumnFromselectItem(items, tables);//输出的item
            for(Map<String,String> tc:resultColumnName)
            {
                String tname=null;
                String cname=null;
                //取表和列的键值对
                Iterator iter = tc.entrySet().iterator();
                while (iter.hasNext()) {

                    Map.Entry entry = (Map.Entry) iter.next();
                    tname = (String) entry.getKey();
                    cname = (String) entry.getValue();
                    break;
                }
                if(!(tname.equals(groupbyResults.getTable_name())&&cname.equals(groupbyResults.getColumn_name())))
                {
                    throw new Exception("SQL exception ,select item is not in group by");
                }
                selectItemResult ItemResult= new selectItemResult(groupbyResults.getTable_name(),groupbyResults.getColumn_name());

               if(items.get(0).getAlias().isPresent())
               {
                   ItemResult.setItemname(items.get(0).getAlias().orNull());
               }
                for(Object obj:this.groupbyResults.getGroupbyResult())
                {
                    ItemResult.getColumn_content().add(obj);
                }
                this.resultTable_structures.add(ItemResult);
            }

        }

        else if(Groupby==false&&Aggregat==true)//无group by，有聚合
        {
            //有聚合不能有其他项
            for(SelectItem selectItem:items)
            {
                if(selectItem.getClass().toString().equals("class com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.CommonSelectItem"))
                {
                    throw new Exception("SQL error,In Aggregated without GROUP BY");
                }
            }

            for(SelectItem selectItem:items)
            {
                String tblname=null;
                String colname=null;
                String innerExpress=((AggregationSelectItem)selectItem).getInnerExpression();
                innerExpress=innerExpress.substring(1,innerExpress.length()-1);//去除括号
              //  System.out.println(innerExpress);
                String[] inner=strToArray(innerExpress);
                if(inner.length==1)
                {
                    tblname=getTablenameByColumn(inner[0]);
            //        System.out.println(inner[0]+"::::::");
                    colname=inner[0];
                }
                else if(inner.length==2)
                {
                    if(tables.find(inner[0]).isPresent())//表是否存在
                    {
                        tblname=tables.find(inner[0]).orNull().getName();
                        colname=inner[1];
                    }
                    else
                    {
                        throw new documentException(inner[0], 1);
                    }
                }

                if(((AggregationSelectItem)selectItem).getType().equals(AggregationType.SUM))
                {
                    List<Double> sum=SUM(tblname,colname,groupbyResults);
                    selectItemResult ItemResult= new selectItemResult(tblname,colname);
                    ItemResult.setItemname(selectItem.getAlias().orNull());
                    ItemResult.setColumn_content(Collections.singletonList(sum));
                    this.resultTable_structures.add(ItemResult);
                }
                else if(((AggregationSelectItem)selectItem).getType().equals(AggregationType.COUNT))
                {
                    List<Integer> count=COUNT(tblname,colname,groupbyResults);
                    selectItemResult ItemResult= new selectItemResult(tblname,colname);
                    ItemResult.setItemname(selectItem.getAlias().orNull());
                    ItemResult.setColumn_content(Collections.singletonList(count));
                    this.resultTable_structures.add(ItemResult);
                }
                else if(((AggregationSelectItem)selectItem).getType().equals(AggregationType.MAX))
                {
                    List<Object> max=MAX(tblname,colname,groupbyResults);
                    selectItemResult ItemResult= new selectItemResult(tblname,colname);
                    ItemResult.setItemname(selectItem.getAlias().orNull());
                    ItemResult.setColumn_content(Collections.singletonList(max));
                    this.resultTable_structures.add(ItemResult);
                }
                else if(((AggregationSelectItem)selectItem).getType().equals(AggregationType.MIN))
                {
                    List<Object> min=MIN(tblname,colname,groupbyResults);
                    selectItemResult ItemResult= new selectItemResult(tblname,colname);
                    ItemResult.setItemname(selectItem.getAlias().orNull());
                    ItemResult.setColumn_content(Collections.singletonList(min));
                    this.resultTable_structures.add(ItemResult);
                }
            }

        }

        else if(Groupby==true&&Aggregat==true)//有group by，有聚合
        {

            for(SelectItem selectItem:items) {
                System.out.println(selectItem.getClass().toString());
                if (selectItem.getClass().toString().equals("class com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.CommonSelectItem")) {
                    String tblname = null;
                    String colname = null;
                    String innerExpress = (selectItem).getExpression();
                    String[] inner = strToArray(innerExpress);
                    if (inner.length == 1) {
                        tblname = getTablenameByColumn(inner[0]);
                        colname = inner[0];
                    } else if (inner.length == 2) {
                        if (tables.find(inner[0]).isPresent())//表是否存在
                        {
                            tblname = tables.find(inner[0]).orNull().getName();
                            colname = inner[1];
                        } else {
                            throw new documentException(inner[0], 1);
                        }
                    }
                    if(!(tblname.equals(groupbyResults.getTable_name())&&colname.equals(groupbyResults.getColumn_name())))
                    {
                        throw new Exception("SQL error,In Aggregated without GROUP BY");
                    }

                    selectItemResult ItemResult= new selectItemResult(groupbyResults.getTable_name(),groupbyResults.getColumn_name());
                    ItemResult.setItemname(selectItem.getAlias().orNull());
                    ItemResult.setColumn_content(groupbyResults.getGroupbyResult());
                    this.resultTable_structures.add(ItemResult);


                } else if (selectItem.getClass().toString().equals("class com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.AggregationSelectItem")) {


                    String tblname = null;
                    String colname = null;
                    /**
                     * 设置tblname 和 colname
                     */
                    String innerExpress = ((AggregationSelectItem) selectItem).getInnerExpression();
                    innerExpress = innerExpress.substring(1, innerExpress.length() - 1);//去除括号
                    String[] inner = strToArray(innerExpress);
                    if (inner.length == 1) {
                        tblname = getTablenameByColumn(inner[0]);
                        colname = inner[0];
                    } else if (inner.length == 2) {
                        if (tables.find(inner[0]).isPresent())//表是否存在
                        {
                            tblname = tables.find(inner[0]).orNull().getName();
                            colname = inner[1];
                        } else {
                            throw new documentException(inner[0], 1);
                        }
                    }
                    if (((AggregationSelectItem) selectItem).getType().equals(AggregationType.SUM)) {
                        List<Double> sum = SUM(tblname, colname, groupbyResults);
                        selectItemResult ItemResult = new selectItemResult(tblname, colname);
                        ItemResult.setItemname("SUM("+ItemResult.getItemname()+")");
                        ItemResult.setItemname(selectItem.getAlias().orNull());
                        for(int i=0;i<sum.size();i++)
                        {
                            ItemResult.getColumn_content().add(sum.get(i));
                        }
                        //ItemResult.setColumn_content(Collections.singletonList(sum));
                        this.resultTable_structures.add(ItemResult);
                    } else if (((AggregationSelectItem) selectItem).getType().equals(AggregationType.COUNT)) {
                        List<Integer> count = COUNT(tblname, colname, groupbyResults);
                        selectItemResult ItemResult = new selectItemResult(tblname, colname);
                        ItemResult.setItemname("COUNT("+ItemResult.getItemname()+")");
                        ItemResult.setItemname(selectItem.getAlias().orNull());
                        for(int i=0;i<count.size();i++)
                        {
                            ItemResult.getColumn_content().add(count.get(i));
                        }
                       // ItemResult.setColumn_content(Collections.singletonList(count));
                        this.resultTable_structures.add(ItemResult);
                    } else if (((AggregationSelectItem) selectItem).getType().equals(AggregationType.MAX)) {
                        List<Object> max = MAX(tblname, colname, groupbyResults);
                        selectItemResult ItemResult = new selectItemResult(tblname, colname);
                        ItemResult.setItemname("MAX("+ItemResult.getItemname()+")");
                        ItemResult.setItemname(selectItem.getAlias().orNull());
                        for(int i=0;i<max.size();i++)
                        {
                            ItemResult.getColumn_content().add(max.get(i));
                        }
                      //  ItemResult.setColumn_content(Collections.singletonList(max));
                        this.resultTable_structures.add(ItemResult);
                    } else if (((AggregationSelectItem) selectItem).getType().equals(AggregationType.MIN)) {
                        List<Object> min = MIN(tblname, colname, groupbyResults);
                        selectItemResult ItemResult = new selectItemResult(tblname, colname);
                        ItemResult.setItemname("MIN("+ItemResult.getItemname()+")");
                        ItemResult.setItemname(selectItem.getAlias().orNull());
                        for(int i=0;i<min.size();i++)
                        {
                            ItemResult.getColumn_content().add(min.get(i));
                        }
                       // ItemResult.setColumn_content(Collections.singletonList(min));
                        this.resultTable_structures.add(ItemResult);
                    } else if (((AggregationSelectItem) selectItem).getType().equals(AggregationType.AVG)) {
                        List<Double> agv = AGV(tblname, colname, groupbyResults);
                        selectItemResult ItemResult = new selectItemResult(tblname, colname);
                        ItemResult.setItemname("AGV("+ItemResult.getItemname()+")");
                       ItemResult.setItemname(selectItem.getAlias().orNull());
                        for(int i=0;i<agv.size();i++)
                        {
                            ItemResult.getColumn_content().add(agv.get(i));
                        }
                       // ItemResult.setColumn_content(Collections.singletonList(agv));
                        this.resultTable_structures.add(ItemResult);
                    }
                }
            }
        }

    }


    /**
     * 配套表名和列名
     * 1.同一个表的同一个列不能重复
     * SelectItem项只能是common的
     * @return Map<表名，列名>
     * @throws Exception
     */
    private List<Map<String,String>> getTableandColumnFromselectItem(List<SelectItem> items,Tables tables)throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        for (SelectItem selectItem : items) {

            String[] fenkai = strToArray(selectItem.getExpression());
            if (fenkai[fenkai.length - 1].equals("*"))//如果是*的情况
            {
                if (fenkai.length == 1)//长度为1，添加所有表所有列
                {
                    for (table_structure structure : this.table_structures) {
                        for (String columnname : structure.getColumns_name()) {
                            Map<String, String> one = new HashMap<String, String>();
                            one.put(structure.getTable_name(), columnname);
                            result.add(one);
                        }
                    }
                } else if (fenkai.length == 2) {//判断表存在，添加这个表的所有列
                    if (tables.find(fenkai[0]).isPresent())//如果表存在
                    {
                        String tablename=tables.find(fenkai[0]).orNull().getName();
                        for (table_structure structure : this.table_structures) {
                            if (tablename.equals(structure.getTable_name())) {//把这个表的全部列添加进来
                                for (String columnname : structure.getColumns_name()) {
                                    Map<String, String> one = new HashMap<String, String>();
                                    one.put(structure.getTable_name(), columnname);
                                    result.add(one);
                                }
                            }
                        }
                    } else {
                        throw new documentException(fenkai[0], 1);
                    }
                }
            } else {
                String tablename = null;
                if (fenkai.length == 1) {
                    tablename = getTablenameByColumn(fenkai[0]);
                    if (tablename == null) {
                        throw new documentException(fenkai[0], 1);
                    }
                } else if (fenkai.length == 2) {
                    //判断表是否存在
                    if (tables.find(fenkai[0]).isPresent()) {
                        tablename = tables.find(fenkai[0]).orNull().getName();
                        if (tablename == null) {
                            throw new documentException(fenkai[1], 1);
                        }
                    } else {
                        throw new documentException(fenkai[0], 1);
                    }
                }

                Map<String, String> one = new HashMap<String, String>();
                if(fenkai.length == 1) {
                    one.put(tablename, fenkai[0]);
                }
                if(fenkai.length==2)
                {
                    one.put(tablename, fenkai[1]);
                }
                result.add(one);
            }

        }

        boolean right=true;//判断是否有重复值
        int length1=result.size();

        result=removeDuplicateWithOrder(result);

        int length2=result.size();

        if(length1!=length2)
        {
            throw new Exception("SQL error ,Duplicate selectitem.");
        }

        return  result;
    }

    // 删除ArrayList中重复元素，保持顺序
    private List removeDuplicateWithOrder(List list) {
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (set.add(element))
                newList.add(element);
        }
        list.clear();
        list.addAll(newList);
        return list;
    }

    /**
     * 返回列在哪个表中，如果是多个表都有则返回null,有 表.列不可用
     * @param columnname
     * @return
     */
    private String getTablenameByColumn(String columnname)
    {
        int count=0;//计数
        String resultTable=null;
        for(table_structure structure:this.table_structures)
        {
            for(String obj:structure.getColumns_name())
            {
                if(columnname.equals(obj))
                {
                    count++;
                    resultTable=structure.getTable_name();
                }
            }
        }
        if(count>1)
        {
            return null;
        }
        return  resultTable;
    }


    //分开表名与项
    public String[] strToArray(String str) {
        StringTokenizer st = new StringTokenizer(str, ".");
        String[] strArray = new String[st.countTokens()];
        int strLeng = st.countTokens();
        for (int i=0; i<strLeng; i++) {
            strArray[i] = st.nextToken();
        }
        return strArray;
    }


    /**
     * 获取第几列的所有元素
     * @param i
     * @return
     */
    private List<Object> getcontentbyIndex(int i)
    {
        List<Object> result=new ArrayList<>();
        for(columnsType columnsTypes:this.columnsContent)
        {
            result.add(columnsTypes.getItem().get(i));
        }
        return result;
    }
    /**
     *聚合函数群
     */
    public  List<Double> SUM(String tablename,String columnname,GroupbyResult groupbyResult) throws Exception {
        List<Double> result=new ArrayList<>();//最后的结果
        int AGplace=getColumnPlace(tablename,columnname);
        String AGtype=TablerParser.getInstance().get_column_type(tablename,columnname);
        if(AGtype.equals("VARCHAR")||AGtype.equals(("CHAR")))//判断支持类型
        {
            throw new AggregateException("SUM",AGtype);
        }

        if(groupbyResult!=null) {
            String GType = TablerParser.getInstance().get_column_type(groupbyResult.getTable_name(), groupbyResult.getColumn_name());//group by 的类型需要判断
            if (GType.equals("INT") || GType.equals("DOUBLE") || GType.equals("FLOAT")) {
                for (Object gResults : groupbyResult.getGroupbyResult())//一个个取出group by中的值
                {
                    Double gResult = Double.parseDouble(gResults.toString());
                    List<Object> intable = getcontentbyIndex(groupbyResult.getPlace());//取出笛卡尔中group里的那一列
                    List<Integer> columnWhathang = new ArrayList<>();
                    for (int i = 0; i < intable.size(); i++) {
                        if (Double.parseDouble(intable.get(i).toString()) == gResult)//和group by 的某个值相等，则放入他在第几列
                        {
                            columnWhathang.add(i);
                        }
                    }

                    List<Object> aglist = getcontentbyIndex(AGplace);
                    List<Object> putInAG = new ArrayList<>();
                    for (int i : columnWhathang) {
                        putInAG.add(aglist.get(i));
                    }

                    Double res = Aggregate.SUM(putInAG);
                    result.add(res);
                }
            }
            else if(GType.equals("CHAR") || GType.equals("VARCHAR"))
            {
                for (Object gResults : groupbyResult.getGroupbyResult())//一个个取出group by中的值
                {
                    String gResult = gResults.toString();
                    List<Object> intable = getcontentbyIndex(groupbyResult.getPlace());//取出笛卡尔中group里的那一列
                    List<Integer> columnWhathang = new ArrayList<>();
                    for (int i = 0; i < intable.size(); i++) {
                        if (intable.get(i).toString().equals(gResult))//和group by 的某个值相等，则放入他在第几列
                        {
                            columnWhathang.add(i);
                        }
                    }
                    List<Object> aglist = getcontentbyIndex(AGplace);
                    List<Object> putInAG = new ArrayList<>();
                    for (int i : columnWhathang) {
                        putInAG.add(aglist.get(i));
                    }
                    Double res = Aggregate.SUM(putInAG);
                    result.add(res);
                }
            }
        }
        else //无聚合
        {
            List<Object> aglist = getcontentbyIndex(AGplace);
            Double res = Aggregate.SUM(aglist);
            result.add(res);
        }

        return result;
    }

    public  List<Integer> COUNT(String tablename,String columnname,GroupbyResult groupbyResult) throws Exception
    {
        List<Integer> result=new ArrayList<>();//最后的结果
        int AGplace=getColumnPlace(tablename,columnname);

        if(groupbyResult!=null) {
            String GType = TablerParser.getInstance().get_column_type(groupbyResult.getTable_name(), groupbyResult.getColumn_name());//group by 的类型需要判断
            if (GType.equals("INT") || GType.equals("DOUBLE") || GType.equals("FLOAT")) {
                for (Object gResults : groupbyResult.getGroupbyResult())//一个个取出group by中的值
                {
                    Double gResult = Double.parseDouble(gResults.toString());
                    List<Object> intable = getcontentbyIndex(groupbyResult.getPlace());//取出笛卡尔中group里的那一列
                    List<Integer> columnWhathang = new ArrayList<>();
                    for (int i = 0; i < intable.size(); i++) {
                        if (Double.parseDouble(intable.get(i).toString()) == gResult)//和group by 的某个值相等，则放入他在第几列
                        {
                            columnWhathang.add(i);
                        }
                    }

                    List<Object> aglist = getcontentbyIndex(AGplace);
                    List<Object> putInAG = new ArrayList<>();
                    for (int i : columnWhathang) {
                        putInAG.add(aglist.get(i));
                    }

                    int res = Aggregate.COUNT(putInAG);
                    result.add(res);
                }
            }
            else if(GType.equals("CHAR") || GType.equals("VARCHAR"))
            {
                for (Object gResults : groupbyResult.getGroupbyResult())//一个个取出group by中的值
                {
                    String gResult = gResults.toString();
                    List<Object> intable = getcontentbyIndex(groupbyResult.getPlace());//取出笛卡尔中group里的那一列
                    List<Integer> columnWhathang = new ArrayList<>();
                    for (int i = 0; i < intable.size(); i++) {
                        if (intable.get(i).toString().equals(gResult))//和group by 的某个值相等，则放入他在第几列
                        {
                            columnWhathang.add(i);
                        }
                    }
                    List<Object> aglist = getcontentbyIndex(AGplace);
                    List<Object> putInAG = new ArrayList<>();
                    for (int i : columnWhathang) {
                        putInAG.add(aglist.get(i));
                    }
                    int res = Aggregate.COUNT(putInAG);
                    result.add(res);
                }
            }
        }
        else //无聚合
        {
            List<Object> aglist = getcontentbyIndex(AGplace);
            int res = Aggregate.COUNT(aglist);
            result.add(res);
        }

        return result;
    }

    public  List<Object> MAX(String tablename,String columnname,GroupbyResult groupbyResult) throws Exception
    {
        List<Object> result=new ArrayList<>();//最后的结果
        int AGplace=getColumnPlace(tablename,columnname);
        String AGtype=TablerParser.getInstance().get_column_type(tablename,columnname);
        if(AGtype.equals("VARCHAR")||AGtype.equals(("CHAR")))//判断支持类型
        {
            throw new AggregateException("MAX",AGtype);
        }

        if(groupbyResult!=null) {
            String GType = TablerParser.getInstance().get_column_type(groupbyResult.getTable_name(), groupbyResult.getColumn_name());//group by 的类型需要判断
            if (GType.equals("INT") || GType.equals("DOUBLE") || GType.equals("FLOAT")) {
                for (Object gResults : groupbyResult.getGroupbyResult())//一个个取出group by中的值
                {
                    Double gResult = Double.parseDouble(gResults.toString());
                    List<Object> intable = getcontentbyIndex(groupbyResult.getPlace());//取出笛卡尔中group里的那一列
                    List<Integer> columnWhathang = new ArrayList<>();
                    for (int i = 0; i < intable.size(); i++) {
                        if (Double.parseDouble(intable.get(i).toString()) == gResult)//和group by 的某个值相等，则放入他在第几列
                        {
                            columnWhathang.add(i);
                        }
                    }

                    List<Object> aglist = getcontentbyIndex(AGplace);
                    List<Object> putInAG = new ArrayList<>();
                    for (int i : columnWhathang) {
                        putInAG.add(aglist.get(i));
                    }

                    Object res = Aggregate.MAX(putInAG);
                    result.add(res);
                }
            }
            else if(GType.equals("CHAR") || GType.equals("VARCHAR"))
            {
                for (Object gResults : groupbyResult.getGroupbyResult())//一个个取出group by中的值
                {
                    String gResult = gResults.toString();
                    List<Object> intable = getcontentbyIndex(groupbyResult.getPlace());//取出笛卡尔中group里的那一列
                    List<Integer> columnWhathang = new ArrayList<>();
                    for (int i = 0; i < intable.size(); i++) {
                        if (intable.get(i).toString().equals(gResult))//和group by 的某个值相等，则放入他在第几列
                        {
                            columnWhathang.add(i);
                        }
                    }
                    List<Object> aglist = getcontentbyIndex(AGplace);
                    List<Object> putInAG = new ArrayList<>();
                    for (int i : columnWhathang) {
                        putInAG.add(aglist.get(i));
                    }
                    Object res = Aggregate.MAX(putInAG);
                    result.add(res);
                }
            }
        }
        else //无聚合
        {
            List<Object> aglist = getcontentbyIndex(AGplace);
            Object res = Aggregate.MAX(aglist);
            result.add(res);
        }

        return result;
    }

    public  List<Object> MIN(String tablename,String columnname,GroupbyResult groupbyResult) throws Exception
    {
        List<Object> result=new ArrayList<>();//最后的结果
        int AGplace=getColumnPlace(tablename,columnname);
        String AGtype=TablerParser.getInstance().get_column_type(tablename,columnname);
        if(AGtype.equals("VARCHAR")||AGtype.equals(("CHAR")))//判断支持类型
        {
            throw new AggregateException("MIN",AGtype);
        }

        if(groupbyResult!=null) {
            String GType = TablerParser.getInstance().get_column_type(groupbyResult.getTable_name(), groupbyResult.getColumn_name());//group by 的类型需要判断
            if (GType.equals("INT") || GType.equals("DOUBLE") || GType.equals("FLOAT")) {
                for (Object gResults : groupbyResult.getGroupbyResult())//一个个取出group by中的值
                {
                    Double gResult = Double.parseDouble(gResults.toString());
                    List<Object> intable = getcontentbyIndex(groupbyResult.getPlace());//取出笛卡尔中group里的那一列
                    List<Integer> columnWhathang = new ArrayList<>();
                    for (int i = 0; i < intable.size(); i++) {
                        if (Double.parseDouble(intable.get(i).toString()) == gResult)//和group by 的某个值相等，则放入他在第几列
                        {
                            columnWhathang.add(i);
                        }
                    }

                    List<Object> aglist = getcontentbyIndex(AGplace);
                    List<Object> putInAG = new ArrayList<>();
                    for (int i : columnWhathang) {
                        putInAG.add(aglist.get(i));
                    }

                    Object res = Aggregate.MIN(putInAG);
                    result.add(res);
                }
            }
            else if(GType.equals("CHAR") || GType.equals("VARCHAR"))
            {
                for (Object gResults : groupbyResult.getGroupbyResult())//一个个取出group by中的值
                {
                    String gResult = gResults.toString();
                    List<Object> intable = getcontentbyIndex(groupbyResult.getPlace());//取出笛卡尔中group里的那一列
                    List<Integer> columnWhathang = new ArrayList<>();
                    for (int i = 0; i < intable.size(); i++) {
                        if (intable.get(i).toString().equals(gResult))//和group by 的某个值相等，则放入他在第几列
                        {
                            columnWhathang.add(i);
                        }
                    }
                    List<Object> aglist = getcontentbyIndex(AGplace);
                    List<Object> putInAG = new ArrayList<>();
                    for (int i : columnWhathang) {
                        putInAG.add(aglist.get(i));
                    }
                    Object res = Aggregate.MIN(putInAG);
                    result.add(res);
                }
            }
        }
        else //无聚合
        {
            List<Object> aglist = getcontentbyIndex(AGplace);
            Object res = Aggregate.MIN(aglist);
            result.add(res);
        }

        return result;

    }

    public  List<Double> AGV(String tablename,String columnname,GroupbyResult groupbyResult) throws Exception
    {
        List<Double> result=new ArrayList<>();//最后的结果
        int AGplace=getColumnPlace(tablename,columnname);
        String AGtype=TablerParser.getInstance().get_column_type(tablename,columnname);
        if(AGtype.equals("VARCHAR")||AGtype.equals(("CHAR")))//判断支持类型
        {
            throw new AggregateException("SUM",AGtype);
        }

        if(groupbyResult!=null) {
            String GType = TablerParser.getInstance().get_column_type(groupbyResult.getTable_name(), groupbyResult.getColumn_name());//group by 的类型需要判断
            if (GType.equals("INT") || GType.equals("DOUBLE") || GType.equals("FLOAT")) {
                for (Object gResults : groupbyResult.getGroupbyResult())//一个个取出group by中的值
                {
                    Double gResult = Double.parseDouble(gResults.toString());
                    List<Object> intable = getcontentbyIndex(groupbyResult.getPlace());//取出笛卡尔中group里的那一列
                    List<Integer> columnWhathang = new ArrayList<>();
                    for (int i = 0; i < intable.size(); i++) {
                        if (Double.parseDouble(intable.get(i).toString()) == gResult)//和group by 的某个值相等，则放入他在第几列
                        {
                            columnWhathang.add(i);
                        }
                    }

                    List<Object> aglist = getcontentbyIndex(AGplace);
                    List<Object> putInAG = new ArrayList<>();
                    for (int i : columnWhathang) {
                        putInAG.add(aglist.get(i));
                    }

                    Double res = Aggregate.AGV(putInAG);
                    result.add(res);
                }
            }
            else if(GType.equals("CHAR") || GType.equals("VARCHAR"))
            {
                for (Object gResults : groupbyResult.getGroupbyResult())//一个个取出group by中的值
                {
                    String gResult = gResults.toString();
                    List<Object> intable = getcontentbyIndex(groupbyResult.getPlace());//取出笛卡尔中group里的那一列
                    List<Integer> columnWhathang = new ArrayList<>();
                    for (int i = 0; i < intable.size(); i++) {
                        if (intable.get(i).toString().equals(gResult))//和group by 的某个值相等，则放入他在第几列
                        {
                            columnWhathang.add(i);
                        }
                    }
                    List<Object> aglist = getcontentbyIndex(AGplace);
                    List<Object> putInAG = new ArrayList<>();
                    for (int i : columnWhathang) {
                        putInAG.add(aglist.get(i));
                    }
                    Double res = Aggregate.AGV(putInAG);
                    result.add(res);
                }
            }
        }
        else //无聚合
        {
            List<Object> aglist = getcontentbyIndex(AGplace);
            Double res = Aggregate.AGV(aglist);
            result.add(res);
        }

        return result;
    }


    /**
     * 处理order by
     * 只允许一处
     * 如果没有表名  判断from的表
     * 1.列没有重复，
     * 2.列存在
     * 3.表是否存在
     * 4.别名是否存在在表中
     * @throws Exception
     */
    public void deal_orderby(Tables tables,OrderItem orderItem,boolean perface)throws Exception
    {
        String tablename=orderItem.getOwner().orNull();
        String columnname=orderItem.getName().orNull();
        Table lingshi=tables.find(tablename).orNull();//通过表名或者别名找出表 4.
        if(lingshi!=null)
        {
            tablename=lingshi.getName();
        }
        List<String> tablenames=new ArrayList<>();//所有表的名称
        //所有查询表的表名称
        for(String table:tables.getTableNames())
        {
            tablenames.add(table);
        }

        int checkcolumnIntableNumber=0;//判断列在不同表中一共出现了几次
        //1，2
        if(tablename==null) {
            String columnInWhatTable=null;
            for (String tableN : tablenames) {
                if (existcolumn(columnname, TablerParser.getInstance().get_column(tableN))) {//列是否存在在某个表内
                    checkcolumnIntableNumber++;
                    columnInWhatTable = tableN;
                }
            }

            if (checkcolumnIntableNumber == 0) {
                throw new SQLDictionaryException(columnname);
            } else if (checkcolumnIntableNumber > 1) {
                throw new SQLDictionaryException(columnname, 'i');
            }
            tablename = columnInWhatTable;
        }
        if(tablenames.indexOf(tablename)==-1)//3
        {
            throw new SQLDictionaryException(tablename);
        }

        /**
         * 正片开始
         */

        if((Aggregat==false &&Groupby==false)) {
            int columnplace = getColumnPlace(tablename, columnname);
            if (columnplace == -1)//列不存在
            {
                throw new documentException(columnname);
            }

            String type = TablerParser.getInstance().get_column_type(tablename, columnname);
            if (type.equals("VARCHAR")) {
                SortUtil<String> stringSortUtil = (SortUtil<String>) SortUtilFactory.createSortUtil("string");
                Map<Integer, String> sort = new HashMap<>();
                for (int i = 0; i < columnsContent.size(); i++)//把要排序的列写成map(i,object)
                {
                    sort.put(i, columnsContent.get(i).getItem().get(columnplace).toString());
                }
                Map<Integer, String> resultMap = stringSortUtil.rank(sort, perface);

                List<Integer> newplace = new ArrayList<>();
                for (Map.Entry<Integer, String> entry : resultMap.entrySet()) {
                    newplace.add(entry.getKey());
                }
                //结果表的每一个
                for (selectItemResult selectItemResults : this.resultTable_structures) {
                    List<Object> temp = new ArrayList<>();
                    for (int place : newplace) {
                        temp.add(selectItemResults.getColumn_content().get(place));
                    }
                    selectItemResults.setColumn_content(temp);
                }

            } else if (type.equals("CHAR")) {
                SortUtil<Character> stringSortUtil = (SortUtil<Character>) SortUtilFactory.createSortUtil("char");
                Map<Integer, Character> sort = new HashMap<>();
                for (int i = 0; i < columnsContent.size(); i++)//把要排序的列写成map(i,object)
                {
                    sort.put(i, columnsContent.get(i).getItem().get(columnplace).toString().charAt(0));
                }
                Map<Integer, Character> resultMap = stringSortUtil.rank(sort, perface);

                List<Integer> newplace = new ArrayList<>();
                for (Map.Entry<Integer, Character> entry : resultMap.entrySet()) {
                    newplace.add(entry.getKey());
                }
                //结果表的每一个
                for (selectItemResult selectItemResults : this.resultTable_structures) {
                    List<Object> temp = new ArrayList<>();
                    for (int place : newplace) {
                        temp.add(selectItemResults.getColumn_content().get(place));
                    }
                    selectItemResults.setColumn_content(temp);
                }
            } else if (type.equals("INT")) {
                SortUtil<Integer> stringSortUtil = (SortUtil<Integer>) SortUtilFactory.createSortUtil("int");
                Map<Integer, Integer> sort = new HashMap<>();
                for (int i = 0; i < columnsContent.size(); i++)//把要排序的列写成map(i,object)
                {
                    sort.put(i, Integer.parseInt(columnsContent.get(i).getItem().get(columnplace).toString()));
                }
                Map<Integer, Integer> resultMap = stringSortUtil.rank(sort, perface);

                List<Integer> newplace = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : resultMap.entrySet()) {
                    newplace.add(entry.getKey());
                }
                //结果表的每一个
                for (selectItemResult selectItemResults : this.resultTable_structures) {
                    List<Object> temp = new ArrayList<>();
                    for (int place : newplace) {
                        temp.add(selectItemResults.getColumn_content().get(place));
                    }
                    selectItemResults.setColumn_content(temp);
                }
            } else if (type.equals("DOUBLE")) {
                SortUtil<Double> stringSortUtil = (SortUtil<Double>) SortUtilFactory.createSortUtil("double");
                Map<Integer, Double> sort = new HashMap<>();
                for (int i = 0; i < columnsContent.size(); i++)//把要排序的列写成map(i,object)
                {
                    sort.put(i, Double.parseDouble(columnsContent.get(i).getItem().get(columnplace).toString()));
                }
                Map<Integer, Double> resultMap = stringSortUtil.rank(sort, perface);

                List<Integer> newplace = new ArrayList<>();
                for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
                    newplace.add(entry.getKey());
                }
                //结果表的每一个
                for (selectItemResult selectItemResults : this.resultTable_structures) {
                    List<Object> temp = new ArrayList<>();
                    for (int place : newplace) {
                        temp.add(selectItemResults.getColumn_content().get(place));
                    }
                    selectItemResults.setColumn_content(temp);
                }
            } else if (type.equals("FLOAT")) {
                SortUtil<Float> stringSortUtil = (SortUtil<Float>) SortUtilFactory.createSortUtil("float");
                Map<Integer, Float> sort = new HashMap<>();
                for (int i = 0; i <columnsContent.size(); i++)//把要排序的列写成map(i,object)
                {
                    sort.put(i, Float.parseFloat(columnsContent.get(i).getItem().get(columnplace).toString()));
                }
                Map<Integer, Float> resultMap = stringSortUtil.rank(sort, perface);

                List<Integer> newplace = new ArrayList<>();
                for (Map.Entry<Integer, Float> entry : resultMap.entrySet()) {
                    newplace.add(entry.getKey());
                }
                //结果表的每一个
                for (selectItemResult selectItemResults : this.resultTable_structures) {
                    List<Object> temp = new ArrayList<>();
                    for (int place : newplace) {
                        temp.add(selectItemResults.getColumn_content().get(place));
                    }
                    selectItemResults.setColumn_content(temp);
                }
            }


        }



        if((Aggregat==true &&Groupby==true)) {
            int columnplace = getColumnPlace(tablename, columnname);
            if (columnplace == -1)//列不存在
            {
                throw new documentException(columnname);
            }

            String type = TablerParser.getInstance().get_column_type(tablename, columnname);
            if (type.equals("VARCHAR")) {
                SortUtil<String> stringSortUtil = (SortUtil<String>) SortUtilFactory.createSortUtil("string");
                Map<Integer, String> sort = new HashMap<>();
                for (int i = 0; i < columnsContent.size(); i++)//把要排序的列写成map(i,object)
                {
                    sort.put(i, columnsContent.get(i).getItem().get(columnplace).toString());
                }
                Map<Integer, String> resultMap = stringSortUtil.rank(sort, perface);

                List<Integer> newplace = new ArrayList<>();
                for (Map.Entry<Integer, String> entry : resultMap.entrySet()) {
                    newplace.add(entry.getKey());
                }
                //结果表的每一个
                List<columnsType> temp=new ArrayList<>();
                for(Integer i:newplace)
                {
                    temp.add(columnsContent.get(i));
                }
                columnsContent=temp;

            } else if (type.equals("CHAR")) {
                SortUtil<Character> stringSortUtil = (SortUtil<Character>) SortUtilFactory.createSortUtil("char");
                Map<Integer, Character> sort = new HashMap<>();
                for (int i = 0; i < columnsContent.size(); i++)//把要排序的列写成map(i,object)
                {
                    sort.put(i, columnsContent.get(i).getItem().get(columnplace).toString().charAt(0));
                }
                Map<Integer, Character> resultMap = stringSortUtil.rank(sort, perface);

                List<Integer> newplace = new ArrayList<>();
                for (Map.Entry<Integer, Character> entry : resultMap.entrySet()) {
                    newplace.add(entry.getKey());
                }
                //结果表的每一个
                List<columnsType> temp=new ArrayList<>();
                for(Integer i:newplace)
                {
                    temp.add(columnsContent.get(i));
                }
                columnsContent=temp;

            } else if (type.equals("INT")) {
                SortUtil<Integer> stringSortUtil = (SortUtil<Integer>) SortUtilFactory.createSortUtil("int");
                Map<Integer, Integer> sort = new HashMap<>();
                for (int i = 0; i < columnsContent.size(); i++)//把要排序的列写成map(i,object)
                {
                    sort.put(i, Integer.parseInt(columnsContent.get(i).getItem().get(columnplace).toString()));
                }
                Map<Integer, Integer> resultMap = stringSortUtil.rank(sort, perface);

                List<Integer> newplace = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : resultMap.entrySet()) {
                    newplace.add(entry.getKey());
                }
                //结果表的每一个
                List<columnsType> temp=new ArrayList<>();
                for(Integer i:newplace)
                {
                    temp.add(columnsContent.get(i));
                }
                columnsContent=temp;
            } else if (type.equals("DOUBLE")) {
                SortUtil<Double> stringSortUtil = (SortUtil<Double>) SortUtilFactory.createSortUtil("double");
                Map<Integer, Double> sort = new HashMap<>();
                for (int i = 0; i < columnsContent.size(); i++)//把要排序的列写成map(i,object)
                {
                    sort.put(i, Double.parseDouble(columnsContent.get(i).getItem().get(columnplace).toString()));
                }
                Map<Integer, Double> resultMap = stringSortUtil.rank(sort, perface);

                List<Integer> newplace = new ArrayList<>();
                for (Map.Entry<Integer, Double> entry : resultMap.entrySet()) {
                    newplace.add(entry.getKey());
                }
                //结果表的每一个
                List<columnsType> temp=new ArrayList<>();
                for(Integer i:newplace)
                {
                    temp.add(columnsContent.get(i));
                }
                columnsContent=temp;
            } else if (type.equals("FLOAT")) {
                SortUtil<Float> stringSortUtil = (SortUtil<Float>) SortUtilFactory.createSortUtil("float");
                Map<Integer, Float> sort = new HashMap<>();
                for (int i = 0; i <columnsContent.size(); i++)//把要排序的列写成map(i,object)
                {
                    sort.put(i, Float.parseFloat(columnsContent.get(i).getItem().get(columnplace).toString()));
                }
                Map<Integer, Float> resultMap = stringSortUtil.rank(sort, perface);

                List<Integer> newplace = new ArrayList<>();
                for (Map.Entry<Integer, Float> entry : resultMap.entrySet()) {
                    newplace.add(entry.getKey());
                }
                //结果表的每一个
                List<columnsType> temp=new ArrayList<>();
                for(Integer i:newplace)
                {
                    temp.add(columnsContent.get(i));
                }
                columnsContent=temp;
            }


        }

    }





    /**
     *某个字段是否在另外字段里
     * @param columnname
     * @param columns
     * @return
     */
    private Boolean existcolumn(String columnname,List<String> columns)
    {
        if(columns.indexOf(columnname)==-1)
            return false;
        else return true;
    }


    // private List<columnsType> columnsContent=new ArrayList<>();
    public List<Integer> updatechangeContent(String tablename,String columnname, Condition condition) throws Exception {
        //string 写的类型
        String dicTypes= TablerParser.getInstance().get_column_type(tablename,columnname);

        int columnplace=getColumnPlace(tablename,columnname);//在笛卡尔表中第几列

        Symbol symbol =condition.getSymbol();

        //用处不大
        List<String> haha=new ArrayList<>();
        haha.add(columnname);

        ShardingValue value=condition.getShardingValue(Collections.singletonList(haha));

        Comparable<T> A=value.getValue();
        String type=getType(A);//获取用户输入的类型

        catchType(type,dicTypes);//判定用户输入的数据类型是否与字典里的相同

        type=type.toUpperCase();//转成大写
        List<Integer> move=new ArrayList<>();//用于存储不符合的列
        if(type.equals("CLASS JAVA.LANG.INTEGER")||type.equals("CLASS JAVA.LANG.FLOAT")||type.equals("CLASS JAVA.LANG.DOUBLE"))//如果用户类型为这些类型
        {
            if(symbol==Symbol.EQ) {
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) == (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) == (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) == (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
            }
            if(symbol==Symbol.LT_EQ){
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) >= ((Double.parseDouble(temp.getItem().get(columnplace).toString()))))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) >= (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) >= (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
            }
            if(symbol==Symbol.LT){
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) > (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) > (Double.parseDouble(temp.getItem().get(columnplace).toString())))){
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) > (Double.parseDouble(temp.getItem().get(columnplace).toString())))){
                            move.add(i);
                        }
                    }
                }
            }
            if(symbol==Symbol.GT_EQ){
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) <= (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) <= (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) <= (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
            }
            if(symbol==Symbol.GT){
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) < (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) < (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) < (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
            }
            if(symbol==Symbol.BANG_EQ){
                if(type.equals("CLASS JAVA.LANG.INTEGER")) {//如果是int类型处理
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Integer.parseInt(A.toString()) != (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.FLOAT"))//如果是float类型处理
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Float.parseFloat(A.toString()) != (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
                else if(type.equals("CLASS JAVA.LANG.DOUBLE"))//double
                {
                    for (int i = 0; i < this.columnsContent.size(); i++) {
                        columnsType temp = columnsContent.get(i);
                        if (!(Double.parseDouble(A.toString()) != (Double.parseDouble(temp.getItem().get(columnplace).toString())))) {
                            move.add(i);
                        }
                    }
                }
            }
            //其他符号再进行处理
        }
        else if(type.equals("CLASS JAVA.LANG.STRING"))//如果用户类型为这些类型
        {
            if(!((symbol==Symbol.EQ)||(symbol==Symbol.BANG_EQ)))//varchar,char只能是等于
            {
                throw new documentException(Symbol.EQ,symbol);
            }
            if(symbol==Symbol.EQ) {
                for (int i = 0; i < this.columnsContent.size(); i++) {
                    columnsType temp = columnsContent.get(i);
                    if (!(temp.getItem().get(columnplace) + "").equals(A.toString())) {
                        move.add(i);
                    }
                }
            }
            else if(symbol==Symbol.BANG_EQ){
                for (int i = 0; i < this.columnsContent.size(); i++) {
                    columnsType temp = columnsContent.get(i);
                    if ((temp.getItem().get(columnplace) + "").equals(A.toString())) {
                        move.add(i);
                    }
                }
            }
        }
        else if(type.equals("CLASS JAVA.LANG.CHARACTER"))//如果用户类型为这些类型
        {
            if(!((symbol==Symbol.EQ)||(symbol==Symbol.BANG_EQ)))//varchar,char只能是等于
            {
                throw new documentException(Symbol.EQ,symbol);
            }
            if(symbol==Symbol.EQ) {
                for (int i = 0; i < this.columnsContent.size(); i++) {
                    columnsType temp = columnsContent.get(i);
                    if (!((String) temp.getItem().get(columnplace)).equals((A.toString().charAt(0)) + "")) {
                        move.add(i);
                    }
                }
            }
            else if(symbol==Symbol.BANG_EQ) {
                for (int i = 0; i < this.columnsContent.size(); i++) {
                    columnsType temp = columnsContent.get(i);
                    if (((String) temp.getItem().get(columnplace)).equals((A.toString().charAt(0)) + "")) {
                        move.add(i);
                    }
                }
            }
        }


       return move;

    }
}
