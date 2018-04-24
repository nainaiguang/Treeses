package com.nng.lexical_analysis.analysis.mean_analyzer.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 分页偏移量标记对象.
 */
@RequiredArgsConstructor
@Getter
public final class OffsetToken implements SQLToken {
    /**
     * SQL 所在开始位置
     */
    private final int beginPosition;
    /**
     * 偏移值
     */
    private final int offset;
}
