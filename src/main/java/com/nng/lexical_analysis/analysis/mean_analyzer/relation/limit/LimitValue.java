package com.nng.lexical_analysis.analysis.mean_analyzer.relation.limit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * 分页值对象.
 * 分成两种情况：
 * 1. 占位服
 * 2. 值
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LimitValue {
    /**
     * 值
     * 当 value == -1 时，为占位符
     */
    private int value;
    /**
     * 第几个占位符
     */
    private int index;
}
