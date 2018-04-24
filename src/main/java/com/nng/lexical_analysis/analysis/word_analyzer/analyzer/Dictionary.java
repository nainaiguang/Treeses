package com.nng.lexical_analysis.analysis.word_analyzer.analyzer;

import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Keyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.TokenType;

import java.util.HashMap;
import java.util.Map;

/**
 * 词法标记字典.
 */
public final class Dictionary {
    /**
     * 词法关键词Map
     */
    private final Map<String, Keyword> tokens = new HashMap<>(1024);

    public Dictionary(final Keyword... dlWord) {
            fill(dlWord);
    }


    /**
     * 装上默认词法关键词 + 方言词法关键词
     * 不同的数据库有相同的默认词法关键词，有有不同的方言关键词
     *
     * @param dlKeywords 方言词法关键词
     */
    private void fill(final Keyword... dlKeywords) {
        for (DefaultKeyword each : DefaultKeyword.values()) {
            tokens.put(each.name(), each);
        }
        for (Keyword each : dlKeywords) {
            tokens.put(each.toString(), each);
        }
    }

    /**
     * 获得 词法字面量 对应的 词法字面量标记
     * 当不存在时，返回默认词法字面量标记
     *
     * @param literals 词法字面量
     * @param defaultTokenType 默认词法字面量标记
     * @return 词法字面量标记
     */
    TokenType findTokenType(final String literals, final TokenType defaultTokenType) {
        String key = null == literals ? null : literals.toUpperCase();
        return tokens.containsKey(key) ? tokens.get(key) : defaultTokenType; // TODO 直接get，然后判断是否为空
    }

    /**
     * 获得 词法字面量 对应的 词法字面量标记
     * 当不存在时，抛出 {@link IllegalArgumentException}
     *
     * @param literals 词法字面量
     * @return 词法字面量标记
     */
    TokenType findTokenType(final String literals) {
        String key = null == literals ? null : literals.toUpperCase();
        if (tokens.containsKey(key)) {
            return tokens.get(key);
        }
        throw new IllegalArgumentException();
    }
}
