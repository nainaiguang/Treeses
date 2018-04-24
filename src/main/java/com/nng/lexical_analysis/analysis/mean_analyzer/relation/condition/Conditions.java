package com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 条件对象集合.
 */
@RequiredArgsConstructor
@ToString
public final class Conditions {

    @Getter
    private final Map<Column, Condition> conditions = new LinkedHashMap<>();

    // TODO 引入mockito时去掉该方法
    public void add(final Condition condition) {
        conditions.put(condition.getColumn(), condition);
    }
    
    /**
     * 判断条件对象是否为空.
     * 
     * @return 条件对象是否为空
     */
    public boolean isEmpty() {
        return conditions.isEmpty();
    }
    
    /**
     * 查找条件对象.
     *
     * @param column 列对象
     * @return 条件对象
     */
    public Optional<Condition> find(final Column column) {
        return Optional.fromNullable(conditions.get(column));
    }
}
