package com.nng.lexical_analysis.analysis.word_analyzer.token;

public enum Assist implements TokenType {
    /**
     * 分析结束
     */
    END,
    /**
     * 分析错误，无符合条件词法标记
     */
    ERROR
}
