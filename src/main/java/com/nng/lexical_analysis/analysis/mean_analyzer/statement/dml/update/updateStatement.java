package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.update;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.contact.controlType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class updateStatement extends DMLStatement{
    com.nng.lexical_analysis.contact.controlType controlType= com.nng.lexical_analysis.contact.controlType.UPDATE;
    @Getter
    List<String> setColumn=new ArrayList<>();
    @Getter
    List<String> setcontent=new ArrayList<>();//要根据类型自己判断是否可以转换
}
