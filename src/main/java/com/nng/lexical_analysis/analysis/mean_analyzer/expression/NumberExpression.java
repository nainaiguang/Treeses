package com.nng.lexical_analysis.analysis.mean_analyzer.expression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class NumberExpression implements Expression{
    private final Number number;
}
