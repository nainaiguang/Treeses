package com.nng.DBS.log.logControl;

import com.nng.DBS.log.logBody.logbody;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.contact.controlType;
import javassist.NotFoundException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LogEngine {
    private  List<logbody> log=new ArrayList<>();
    /**
     * 单例模式
     * 获取本类实体
     * @return
     */

    public void addNewLog(controlType sqlOperation, String sql, Boolean success) throws IOException {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("SYSTEM/LOG/Log.log"));
            List<logbody> temp = (List<logbody>) objectInputStream.readObject();
            this.log=temp;
        }
        catch (Exception e)
        {
            ObjectOutputStream objectOutputStreams = new ObjectOutputStream(new FileOutputStream("SYSTEM/LOG/Log.log"));
            objectOutputStreams.writeObject(log);
        }

        log.add(new logbody(sqlOperation,sql,success));
        ObjectOutputStream objectOutputStreams = new ObjectOutputStream(new FileOutputStream("SYSTEM/LOG/Log.log"));
        objectOutputStreams.writeObject(log);
    }

    public String readLog()throws IOException
    {
        String result="";
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("SYSTEM/LOG/Log.log"));
        try {
            List<logbody> temp = (List<logbody>) objectInputStream.readObject();
            this.log=temp;
            for(logbody lo:temp)
            {
                result=result+lo.getSqlOperation()+"\t"+lo.getSql()+"\t"+lo.getDate()+"\t"+lo.getSuccess()+"\n";
            }
        }
        catch (ClassNotFoundException e)
        {
            result="no log now";
        }
        return result;
    }
}
