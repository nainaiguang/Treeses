package com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;



public class ColumnConditions {
    @Getter
    private List<columnCondition> columnConditions;
    public ColumnConditions()
    {
        columnConditions=new LinkedList<>();
    }
    public void add(columnCondition columnCondition) {
        columnConditions.add(columnCondition);
    }
}
