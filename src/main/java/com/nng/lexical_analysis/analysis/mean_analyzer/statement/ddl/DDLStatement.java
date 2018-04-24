
package com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl;


import com.nng.lexical_analysis.analysis.mean_analyzer.statement.AbstractSQLStatement;
import com.nng.lexical_analysis.contact.SQLType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

/**
 * DDL语句对象.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class DDLStatement extends AbstractSQLStatement {

    List<Map<String,String>> list;
    public DDLStatement() {
        super(SQLType.DDL);
        list=new ArrayList<>();
    }

    public void setColumn(String column,String literal)
    {
        Map<String,String> temp=new HashMap<>();
        temp.put(column,literal);
        list.add(temp);
    }

}
