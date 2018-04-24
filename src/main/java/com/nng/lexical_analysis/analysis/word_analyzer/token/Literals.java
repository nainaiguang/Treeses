package com.nng.lexical_analysis.analysis.word_analyzer.token;

/**
 * 词法字面量标记.
 *
 */
public enum Literals implements TokenType {
    /**
     * 整数
     * 例如，SELECT 1
     */
    INT,
    /**
     * 浮点数
     * 例如，SELECT 1.0
     */
    FLOAT,
    /**
     * 十六进制
     * 例如，SELECT 0x1A
     */
    HEX,
    /**
     * 字符串
     * 例如：SELECT "123"
     */
    CHARS,
    /**
     * 词法关键词
     * 例如：表名，查询字段，函数 等等
     */
    IDENTIFIER,
    /**
     * 变量
     * 例如：SELECT @@VERSION
     */
    VARIABLE
}
