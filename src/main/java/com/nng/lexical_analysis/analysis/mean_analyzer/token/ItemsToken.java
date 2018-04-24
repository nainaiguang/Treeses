package com.nng.lexical_analysis.analysis.mean_analyzer.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

/**
 * 选择项标记对象.
 * 目前有三个地方产生：
 * 1. AVG 查询额外 COUNT 和 SUM
 * 2. GROUP BY 不在 查询字段，额外查询该字段
 * 3. ORDER BY 不在 查询字段，额外查询该字段
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ItemsToken implements SQLToken {

    /**
     * SQL 开始位置
     */
    private final int beginPosition;
    /**
     * 字段名数组
     */
    private final List<String> items = new LinkedList<>();
}
