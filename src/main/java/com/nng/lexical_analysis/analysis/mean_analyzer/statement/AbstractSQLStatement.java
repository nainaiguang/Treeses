package com.nng.lexical_analysis.analysis.mean_analyzer.statement;

import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.ColumnConditions;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Conditions;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Tables;
import com.nng.lexical_analysis.analysis.mean_analyzer.token.SQLToken;
import com.nng.lexical_analysis.contact.SQLType;
import com.nng.lexical_analysis.contact.controlType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@ToString
public abstract class AbstractSQLStatement implements SQLStatement{
    /**
     * SQL 操作类型
     */
    private controlType controlType;
    /**
     * SQL 类型
     */
    private final SQLType type;
    /**
     * 表
     */
    private final Tables tables = new Tables();
    /**
     * 过滤条件。
     * 只有对路由结果有影响的条件，才添加进数组
     */
    private final Conditions conditions = new Conditions();
    /**
     * 过滤条件
     * 只有列相等的条件，目前还不支持列大于小于等操作
     */
    private final ColumnConditions columnConditions=new ColumnConditions();
    /**
     * SQL标记对象
     */
    private final List<SQLToken> sqlTokens = new LinkedList<>();

    @Override
    public final SQLType getType() {
        return type;
    }

    @Override
    public void setControlType(controlType controlType)
    {
        this.controlType=controlType;
    }
    @Override
    public int getParametersIndex() {
        return 0;
    }

    @Override
    public void setParametersIndex(int parametersIndex) {
    }

    @Override
    public void increaseParametersIndex() {
    }
}
