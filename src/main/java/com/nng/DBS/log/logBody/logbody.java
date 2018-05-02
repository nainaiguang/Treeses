package com.nng.DBS.log.logBody;

import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.contact.controlType;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@ToString
public class logbody implements Serializable {
    controlType sqlOperation;
    String sql;
    String success;
    Date date;
    public logbody(controlType sqlOperation, String sql, Boolean successs)
    {
        this.sqlOperation=sqlOperation;
        this.sql=sql;
        if(successs)
        {success="success";}
        else
        {
            success="fail";
        }
        this.date=new Date();
    }
}
