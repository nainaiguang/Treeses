package com.nng.lexical_analysis.analysis.mean_analyzer.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 排序标记对象.
 */
@RequiredArgsConstructor
@Getter
public final class OrderByToken implements SQLToken {

    /**
     * SQL 所在开始位置
     */
    private final int beginPosition;
}
