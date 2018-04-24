package com.nng.lexical_analysis.analysis.word_analyzer.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 词法标记.
 */
@AllArgsConstructor
@Getter
public final class Token {


    /**
     * 词法标记类型
     */
    private  TokenType type;
    /**
     * 词法字面量标记
     */
    private  String literals;
    /**
     * {@link #literals} 在 SQL 里的 结束位置
     */
    private  int endPosition;
}
