package com.nng.lexical_analysis.analysis.mean_analyzer.expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class TextExpression implements Expression{
    /**
     * 字符串
     */
    private final String text;
}
