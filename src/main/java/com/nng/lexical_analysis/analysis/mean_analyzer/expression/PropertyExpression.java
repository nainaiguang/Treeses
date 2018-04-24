package com.nng.lexical_analysis.analysis.mean_analyzer.expression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class PropertyExpression implements Expression{
    /**
     * 表名
      */
  private final IdentifierExpression table_name;
    /**
     * 有可能写的额外的名字
     */
  private final String new_name;
}
