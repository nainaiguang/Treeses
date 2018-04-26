package com.nng.unit;

import com.nng.exception.AggregateException;

import java.util.List;

/**
 * 聚合函数类
 */
public class Aggregate {

    public static double SUM(List<Object> values) throws Exception {
        if(values.size()<=0)
        {
            throw new AggregateException("sum");
        }
        //如果为string  或者char  无法比较
        if(values.get(0).getClass().toString().toUpperCase().equals("CLASS JAVA.LANG.CHARACTER")||values.get(0).getClass().toString().toUpperCase().equals("CLASS JAVA.LANG.STRING"))
        {
            throw new AggregateException("sum","VARCHAR");
        }

        double result=0;
        for(Object a:values)
        {
            result=result+Double.parseDouble(a.toString());
        }
        return result;
    }

    public static int COUNT(List<Object> values) throws Exception
    {
       return values.size();
    }

    public static Object MAX(List<Object> values) throws Exception
    {
        if(values.size()<=0)
        {
            throw new AggregateException("max");
        }
        String valuestype=values.get(0).getClass().toString();
        //如果为string  或者char  无法比较
        if(valuestype.toUpperCase().equals("CLASS JAVA.LANG.CHARACTER")||valuestype.toUpperCase().equals("CLASS JAVA.LANG.STRING"))
        {
            throw new AggregateException("max","VARCHAR");
        }
        double max=Double.parseDouble(values.get(0).toString());
        for(Object vl:values)
        {
            if(Double.parseDouble(vl.toString())>max)
                max=Double.parseDouble(vl.toString());
        }

        return max;

    }

    public static Object MIN(List<Object> values) throws Exception
    {
        if(values.size()<=0)
        {
            throw new AggregateException("min");
        }
        String valuestype=values.get(0).getClass().toString();
        //如果为string  或者char  无法比较
        if(valuestype.toUpperCase().equals("CLASS JAVA.LANG.CHARACTER")||valuestype.toUpperCase().equals("CLASS JAVA.LANG.STRING"))
        {
            throw new AggregateException("max","VARCHAR");
        }
        double mix=Double.parseDouble(values.get(0).toString());
        for(Object vl:values)
        {
            if(Double.parseDouble(vl.toString())<mix)
                mix=Double.parseDouble(vl.toString());
        }

        return mix;

    }

    public static double AGV(List<Object> values) throws Exception
    {
        if(values.size()<=0)
        {
            throw new AggregateException("agv");
        }
        //如果为string  或者char  无法比较
        if(values.get(0).getClass().toString().toUpperCase().equals("CLASS JAVA.LANG.CHARACTER")||values.get(0).getClass().toString().toUpperCase().equals("CLASS JAVA.LANG.STRING"))
        {
            throw new AggregateException("agv","VARCHAR");
        }

        int count=Aggregate.COUNT(values);
        double v=Aggregate.SUM(values);
        return v/count;



    }
}
