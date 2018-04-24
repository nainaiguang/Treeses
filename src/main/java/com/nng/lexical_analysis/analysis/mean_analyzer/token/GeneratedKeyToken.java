package com.nng.lexical_analysis.analysis.mean_analyzer.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 自增主键标记对象.
 */
@RequiredArgsConstructor
@Getter
public final class GeneratedKeyToken implements SQLToken {

    /**
     * 开始位置
     */
    private final int beginPosition;
}
