package com.nng.DBS.document_control.dql.select.temporarytype;

import com.google.common.collect.Range;
import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.DBS.document_control.documentException;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Condition;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import com.nng.lexical_analysis.api.ShardingValue;
import com.nng.lexical_analysis.contact.ShardingOperator;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private List<columnsType> columnsContent=new ArrayList<>();


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
            if(symbol!=Symbol.EQ)//varchar,char只能是等于
            {
                throw new documentException(Symbol.EQ,symbol);
            }
            for(int i=0;i<this.columnsContent.size();i++) {
                columnsType temp=columnsContent.get(i);
                if(!(temp.getItem().get(columnplace)+"").equals(A.toString()))
                {
                    move.add(i);
                }
            }
        }
        else if(type.equals("CLASS JAVA.LANG.CHARACTER"))//如果用户类型为这些类型
        {
            if(symbol!=Symbol.EQ)//varchar,char只能是等于
            {
                throw new documentException(Symbol.EQ,symbol);
            }
            for(int i=0;i<this.columnsContent.size();i++) {
                columnsType temp=columnsContent.get(i);
                if(!((String)temp.getItem().get(columnplace)).equals((A.toString().charAt(0))+""))
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
    private  int getColumnPlace(String tablename,String columnname)
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
}
