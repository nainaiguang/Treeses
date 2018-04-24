package com.nng.lexical_analysis.analysis.mean_analyzer.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 分页长度标记对象.
 */
@RequiredArgsConstructor
@Getter
public final class RowCountToken implements SQLToken {
    /**
     * SQL 所在开始位置
     */
    private final int beginPosition;
    /**
     * 行数
     */
    private final int rowCount;
}
