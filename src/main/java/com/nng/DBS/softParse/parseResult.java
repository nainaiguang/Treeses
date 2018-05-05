package com.nng.DBS.softParse;

import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
public class parseResult {
    private  String sql;
    private SQLStatement sqlStatement;
    private int hash;
}
