package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.insert;

import com.nng.lexical_analysis.analysis.mean_analyzer.relation.GeneratedKey;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Column;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.contact.controlType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.LinkedList;

@Getter
@Setter
@ToString
public class InsertStatement extends DMLStatement{
    com.nng.lexical_analysis.contact.controlType controlType= com.nng.lexical_analysis.contact.controlType.INSERT;

    private final Collection<Column> columns = new LinkedList<>();
    /**
     * 自动生成键
     */
    private GeneratedKey generatedKey;
    /**
     * 插入字段 下一个Token 开始位置
     */
    private int columnsListLastPosition;
    /**
     * 值字段 下一个Token 开始位置
     */
    private int valuesListLastPosition;


}
