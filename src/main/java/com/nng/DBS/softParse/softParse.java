package com.nng.DBS.softParse;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class softParse {

    private static softParse Obj;

    /**
     * 单例模式
     * 获取本类实体
     * @return
     */
    public static softParse getInstance()
    {
        if(Obj==null)
        {
            Obj=new softParse();
        }
        return Obj;
    }

    @Getter
    private List<parseResult> parseResults=new ArrayList<>();
    private int index=0;

    public void addparseResult(String sql,SQLStatement sqlStatement)
    {
        for(parseResult result:parseResults)//不添加重复的声明
        {
            if(result.getHash()==sql.hashCode())
            {
                return;
            }
        }

        if(parseResults.size()<=30){
            parseResults.add(new parseResult(sql,sqlStatement,sql.hashCode()));
        }
        else
        {
            parseResults.set(index,new parseResult(sql,sqlStatement,sql.hashCode()));
        }
        index++;
        if(index>29)
        {
            index=0;
        }
    }
    public SQLStatement getStatementFromExitsResult(String sql)
    {
        for(parseResult result:parseResults)
        {
            if(result.getHash()==sql.hashCode())
            {
             //   System.out.println(result.getHash());
                return result.getSqlStatement();
            }
        }
        return null;
    }

}
