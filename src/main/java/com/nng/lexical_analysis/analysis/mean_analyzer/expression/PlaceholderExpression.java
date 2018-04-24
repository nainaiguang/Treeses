package com.nng.lexical_analysis.analysis.mean_analyzer.expression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 占位符
 */
@RequiredArgsConstructor
@Getter
public final class PlaceholderExpression implements Expression{
    private final int index;
}
