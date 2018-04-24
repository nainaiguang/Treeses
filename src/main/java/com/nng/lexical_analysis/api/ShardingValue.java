package com.nng.lexical_analysis.api;

import java.util.Collection;
import java.util.Collections;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import com.google.common.collect.Range;

/**
 * 分片值.
 * 
 * <p>
 * 目前支持{@code =, IN, BETWEEN};
 * 不支持{@code , >, <=, >=, LIKE, NOT, NOT IN}.
 * </p>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public final class ShardingValue<T extends Comparable<?>> {
    
    private final String logicTableName;
    
    private final String columnName;
    
    private final T value;
    
    private final Collection<T> values;
    
    private final Range<T> valueRange;
    
    public ShardingValue(final String logicTableName, final String columnName, final T value) {
        this(logicTableName, columnName, value, Collections.<T>emptyList(), null);
    }
    
    public ShardingValue(final String logicTableName, final String columnName, final Collection<T> values) {
        this(logicTableName, columnName, null, values, null);
    }
    
    public ShardingValue(final String logicTableName, final String columnName, final Range<T> valueRange) {
        this(logicTableName, columnName, null, Collections.<T>emptyList(), valueRange);
    }
    
    /**
     * 获取分片值类型.
     * 
     * @return 分片值类型
     */
    public ShardingValueType getType() {
        if (null != value) {
            return ShardingValueType.SINGLE;
        }
        if (!values.isEmpty()) {
            return ShardingValueType.LIST;
        }
        return ShardingValueType.RANGE;
    }
    
    /**
     * 分片值类型.
     */
    public enum ShardingValueType {
        SINGLE, LIST, RANGE
    }
}
