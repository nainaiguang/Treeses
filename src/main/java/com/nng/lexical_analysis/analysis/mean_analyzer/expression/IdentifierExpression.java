package com.nng.lexical_analysis.analysis.mean_analyzer.expression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 标识符表达式
 */
@RequiredArgsConstructor
@Getter
public final class IdentifierExpression implements Expression {
    private final String name;
}
