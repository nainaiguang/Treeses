package com.nng.lexical_analysis.analysis.word_analyzer.analyzer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 字符类型.
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CharType {
    /**
     * 输入结束符.
     */
    public static final byte EOI = 0x1A;

    /**
     * 判断是否为空格.
     *
     * @param ch 待判断的字符
     * @return 是否为空格
     */
    public static boolean isWhitespace(final char ch) {
        return ch <= 32 && EOI != ch || 160 == ch || ch >= 0x7F && ch <= 0xA0;
    }

    /**
     * 判断是否输入结束.
     *
     * @param ch 待判断的字符
     * @return 是否输入结束
     */
    public static boolean isEndOfInput(final char ch) {
        return ch == 0x1A;
    }

    /**
     * 判断是否为字母.
     *
     * @param ch 待判断的字符
     * @return 是否为字母
     */
    public static boolean isAlphabet(final char ch) {
        return ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z';
    }

    /**
     * 判断是否为数字.
     *
     * @param ch 待判断的字符
     * @return 是否为数字
     */
    public static boolean isDigital(final char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * 判断是否为符号.
     *
     * @param ch 待判断的字符
     * @return 是否为符号
     */
    public static boolean isSymbol(final char ch) {
        return '(' == ch || ')' == ch || '[' == ch || ']' == ch || '{' == ch || '}' == ch || '+' == ch || '-' == ch || '*' == ch || '/' == ch || '%' == ch || '^' == ch || '=' == ch
                || '>' == ch || '<' == ch || '~' == ch || '!' == ch || '?' == ch || '&' == ch || '|' == ch || '.' == ch || ':' == ch || '#' == ch || ',' == ch || ';' == ch;
    }
}
