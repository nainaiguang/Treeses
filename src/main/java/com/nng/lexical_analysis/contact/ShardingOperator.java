package com.nng.lexical_analysis.contact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 分片运算符.
 */
@RequiredArgsConstructor
@Getter
public enum ShardingOperator {
    
    EQUAL("="), 
    BETWEEN("BETWEEN"), 
    IN("IN");
    
    private final String expression;
}
