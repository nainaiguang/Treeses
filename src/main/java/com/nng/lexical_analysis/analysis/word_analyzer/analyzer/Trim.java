package com.nng.lexical_analysis.analysis.word_analyzer.analyzer;

/**
 * 该类为语句整理类，去除多余的空格，多余的空格，回车与制表符
 */

/**
 使用方法:
 静态方法的调用
 String sql2=Trim.clear_up(String sql)
 sql2即为所得整理后的sql语句
 */
public class Trim {


    public static String clear_up(String sql)
    {
        /**
         * 总调用函数，直接清理
         */
        return clear_space(clear_t_n(sql));
    }

    //（1）
    private static String clear_t_n(String sql)
    {
        /**
         * 把制表符和换行符去除
         */
        sql=sql.replace("\r\n"," ");
        sql=sql.replace("\n"," ");
        sql=sql.replace("\t"," ");
        sql=sql.replace("\r\t"," ");
        return sql;
    }



    //(2)
    private static String clear_space(String sql)
    {
        /**
         * 去除多余的空格
         */
        int length=sql.length();

        for(int i=0;i<length;i++)
        {
            if(sql.charAt(i)==' ')
            {
                int n=1;

                while(i+n<length)
                {
                    if(sql.charAt(i+n)==' ')
                    {
                        n++;
                    }
                    else
                    {break;}
                }
                if(i+n==length)//最后一段是空格的情况
                {
                    sql=sql.substring(0,length-n);
                }
                else
                {
                    sql=sql.substring(0,i)+sql.substring(i+n-1,length);
                }
                length=length-n+1;
            }

        }
        return sql;

    }

}
