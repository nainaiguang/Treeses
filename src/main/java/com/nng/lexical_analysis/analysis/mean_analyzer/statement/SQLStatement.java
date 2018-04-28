package com.nng.lexical_analysis.analysis.mean_analyzer.statement;

import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.ColumnConditions;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition.Conditions;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Tables;
import com.nng.lexical_analysis.analysis.mean_analyzer.token.SQLToken;
import com.nng.lexical_analysis.contact.SQLType;
import com.nng.lexical_analysis.contact.controlType;

import java.util.List;

public interface SQLStatement {
    controlType getControlType();
    void setControlType(controlType controlType);
    /**
     * 获取SQL语句类型.
     *
     * @return SQL语句类型
     */
    SQLType getType();

    /**
     * 获取表解析对象集合.
     *
     * @return 表解析对象集合
     */
    Tables getTables();

    /**
     * 获取条件对象集合.
     *
     * @return 条件对象集合,不包含列相等对象
     */
    Conditions getConditions();

    /**
     * 列相等的条件集合
     * @return
     */
    ColumnConditions getColumnConditions();

    /**
     * 获取SQL标记集合.
     *
     * @return SQL标记集合
     */
    List<SQLToken> getSqlTokens();









    /**
     * Get index of parameters.
     *
     * @return index of parameters
     */
    int getParametersIndex();

    /**
     * Set index of parameters.
     *
     * @param parametersIndex index of parameters
     */
    void setParametersIndex(int parametersIndex);

    /**
     * Increase parameters index.
     *
     * @return increased parameters index
     */
    void increaseParametersIndex();
}
