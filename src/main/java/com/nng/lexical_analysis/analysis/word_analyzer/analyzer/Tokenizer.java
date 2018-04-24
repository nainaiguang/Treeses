package com.nng.lexical_analysis.analysis.word_analyzer.analyzer;

import com.nng.lexical_analysis.analysis.word_analyzer.token.*;
import lombok.RequiredArgsConstructor;

/**
 * 词法标记器.
 */
@RequiredArgsConstructor
public final class Tokenizer {

    private static final int MYSQL_SPECIAL_COMMENT_BEGIN_SYMBOL_LENGTH = 1;

    private static final int COMMENT_BEGIN_SYMBOL_LENGTH = 2;

    private static final int HINT_BEGIN_SYMBOL_LENGTH = 3;

    private static final int COMMENT_AND_HINT_END_SYMBOL_LENGTH = 2;

    private static final int HEX_BEGIN_SYMBOL_LENGTH = 2;

    /**
     * 输出字符串
     */
    private final String input;
    /**
     * 字典
     */
    private final Dictionary dictionary;
    /**
     * 输出字符串的开始分词位置
     */
    private final int offset;


    /**
     * 构造函数，初始化
     * @return
     */


    /**
     * 跳过空格
     */
    public int skipWhitespace() {
        int length = 0;
        while (CharType.isWhitespace(charAt(offset + length))) {
            length++;
        }
        return offset + length;
    }


    /**
     * 跳过注释.
     *
     * @return 跳过注释后的偏移量
     */
    public int skipComment() {
        char current = charAt(offset);
        char next = charAt(offset + 1);
        if (isSingleLineCommentBegin(current, next)) {
            return skipSingleLineComment(COMMENT_BEGIN_SYMBOL_LENGTH);
        } else if ('#' == current) {
            return skipSingleLineComment(MYSQL_SPECIAL_COMMENT_BEGIN_SYMBOL_LENGTH);
        } else if (isMultipleLineCommentBegin(current, next)) {
            return skipMultiLineComment();
        }
        return offset;
    }

    /**
     * 是否是单行的注释
     * @param ch
     * @param next
     * @return
     */
    private boolean isSingleLineCommentBegin(final char ch, final char next) {
        return '/' == ch && '/' == next || '-' == ch && '-' == next;
    }

    /**
     * 跳过单行的注释
     * @param commentSymbolLength
     * @return
     */
    private int skipSingleLineComment(final int commentSymbolLength) {
        int length = commentSymbolLength;
        while (!CharType.isEndOfInput(charAt(offset + length)) && '\n' != charAt(offset + length)) {
            length++;
        }
        return offset + length + 1;
    }

    /**
     * 是否是多行的注释
     * @param ch
     * @param next
     * @return
     */
    private boolean isMultipleLineCommentBegin(final char ch, final char next) {
        return '/' == ch && '*' == next;
    }

    private int skipMultiLineComment() {
        return untilCommentAndHintTerminateSign(COMMENT_BEGIN_SYMBOL_LENGTH);
    }

    /**
     * 跳过查询提示.
     *
     * @return 跳过查询提示后的偏移量
     */
    public int skipHint() {
        return untilCommentAndHintTerminateSign(HINT_BEGIN_SYMBOL_LENGTH);
    }

    private int untilCommentAndHintTerminateSign(final int beginSymbolLength) {
        int length = beginSymbolLength;
        while (!isMultipleLineCommentEnd(charAt(offset + length), charAt(offset + length + 1))) {
            if (CharType.isEndOfInput(charAt(offset + length))) {
                throw new ENDNotRightCharException("*/");
            }
            length++;
        }
        return offset + length + COMMENT_AND_HINT_END_SYMBOL_LENGTH;
    }

    private boolean isMultipleLineCommentEnd(final char ch, final char next) {
        return '*' == ch && '/' == next;
    }


    /**
     * 扫描变量.
     * 在 MySQL 里，@代表用户变量；@@代表系统变量。
     * 在 SQLServer 里，有 @@。
     *
     * @return 变量标记
     */
    public Token scanVariable() {
        int length = 1;
        if ('@' == charAt(offset + 1)) {
            length++;
        }
        while (isVariableChar(charAt(offset + length))) {
            length++;
        }
        return new Token(Literals.VARIABLE, input.substring(offset, offset + length), offset + length);
    }

    private boolean isVariableChar(final char ch) {
        return isIdentifierChar(ch) || '.' == ch;
    }



    /**
     * 扫描标识符.
     *
     * @return 标识符标记
     */
    public Token scanIdentifier() {
        // `字段`，例如：SELECT `id` FROM t_user 中的 `id`
        if ('`' == charAt(offset)) {
            int length = getLengthUntilTerminatedChar('`');
            return new Token(Literals.IDENTIFIER, input.substring(offset, offset + length), offset + length);
        }
        int length = 0;
        while (isIdentifierChar(charAt(offset + length))) {
            length++;
        }
        String literals = input.substring(offset, offset + length);
        // 处理 order / group 作为表名
        if (isAmbiguousIdentifier(literals)) {
            return new Token(processAmbiguousIdentifier(offset + length, literals), literals, offset + length);
        }
        // 从 词法关键词 查找是否是 Keyword，如果是，则返回 Keyword，否则返回 Literals.IDENTIFIER
        return new Token(dictionary.findTokenType(literals, Literals.IDENTIFIER), literals, offset + length);
    }

    /**
     * 计算到结束字符的长度
     *
     * @see #hasEscapeChar(char, int) 处理类似 SELECT a AS `b``c` FROM table。此处连续的 "``" 不是结尾，如果传递的是 "`" 会产生误判，所以加了这个判断
     * @param terminatedChar 结束字符
     * @return 长度
     */
    private int getLengthUntilTerminatedChar(final char terminatedChar) {
        int length = 1;
        while (terminatedChar != charAt(offset + length) || hasEscapeChar(terminatedChar, offset + length)) {
            if (offset + length >= input.length()) {
                throw new ENDNotRightCharException(terminatedChar);
            }
            if (hasEscapeChar(terminatedChar, offset + length)) {
                length++;
            }
            length++;
        }
        return length + 1;
    }

    /**
     * 是否是 Escape 字符
     *
     * @param charIdentifier 字符
     * @param offset 位置
     * @return 是否
     */
    private boolean hasEscapeChar(final char charIdentifier, final int offset) {
        return charIdentifier == charAt(offset) && charIdentifier == charAt(offset + 1);
    }

    private boolean isIdentifierChar(final char ch) {
        return CharType.isAlphabet(ch) || CharType.isDigital(ch) || '_' == ch || '$' == ch || '#' == ch;
    }

    /**
     * 是否是引起歧义的标识符
     * 例如 "SELECT * FROM group"，此时 "group" 代表的是表名，而非词法关键词
     *
     * @param literals 标识符
     * @return 是否
     */
    private boolean isAmbiguousIdentifier(final String literals) {
        return DefaultKeyword.ORDER.name().equalsIgnoreCase(literals) || DefaultKeyword.GROUP.name().equalsIgnoreCase(literals);
    }

    /**
     * 获取引起歧义的标识符对应的词法标记类型
     *
     * @param offset 位置
     * @param literals 标识符
     * @return 词法标记类型
     */
    private TokenType processAmbiguousIdentifier(final int offset, final String literals) {
        int i = 0;
        while (CharType.isWhitespace(charAt(offset + i))) {
            i++;
        }
        if (DefaultKeyword.BY.name().equalsIgnoreCase(String.valueOf(new char[] {charAt(offset + i), charAt(offset + i + 1)}))) {
            return dictionary.findTokenType(literals);
        }
        return Literals.IDENTIFIER;
    }

    /**
     * 扫描十六进制数.
     *
     * @return 十六进制数标记
     */
    public Token scanHexDecimal() {
        int length = HEX_BEGIN_SYMBOL_LENGTH;
        // 负数
        if ('-' == charAt(offset + length)) {
            length++;
        }
        while (isHex(charAt(offset + length))) {
            length++;
        }
        return new Token(Literals.HEX, input.substring(offset, offset + length), offset + length);
    }

    private boolean isHex(final char ch) {
        return ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f' || CharType.isDigital(ch);
    }

    /**
     * 扫描数字.
     * 解析数字的结果会有两种：整数 和 浮点数.
     *
     * @return 数字标记
     */
    public Token scanNumber() {
        int length = 0;
        // 负数
        if ('-' == charAt(offset + length)) {
            length++;
        }
        // 浮点数
        length += getDigitalLength(offset + length);
        boolean isFloat = false;
        if ('.' == charAt(offset + length)) {
            isFloat = true;
            length++;
            length += getDigitalLength(offset + length);
        }
        // 科学计数表示，例如：SELECT 7.823E5
        if (isScientificNotation(offset + length)) {
            isFloat = true;
            length++;
            if ('+' == charAt(offset + length) || '-' == charAt(offset + length)) {
                length++;
            }
            length += getDigitalLength(offset + length);
        }
        // 浮点数，例如：SELECT 1.333F
        if (isBinaryNumber(offset + length)) {
            isFloat = true;
            length++;
        }
        return new Token(isFloat ? Literals.FLOAT : Literals.INT, input.substring(offset, offset + length), offset + length);
    }

    /**
     * 获得 offset 开始连续数字数量
     *
     * @param offset 位置
     * @return 数字数量
     */
    private int getDigitalLength(final int offset) {
        int result = 0;
        while (CharType.isDigital(charAt(offset + result))) {
            result++;
        }
        return result;
    }

    private boolean isScientificNotation(final int offset) {
        char current = charAt(offset);
        return 'e' == current || 'E' == current;
    }

    private boolean isBinaryNumber(final int offset) {
        char current = charAt(offset);
        return 'f' == current || 'F' == current || 'd' == current || 'D' == current;
    }

    /**
     * 扫描字符串.
     *
     * @return 字符串标记
     */
    public Token scanChars() {
        return scanChars(charAt(offset));
    }

    private Token scanChars(final char terminatedChar) {
        int length = getLengthUntilTerminatedChar(terminatedChar);
        return new Token(Literals.CHARS, input.substring(offset + 1, offset + length - 1), offset + length);
    }

    /**
     * 扫描符号.
     *
     * @return 符号标记
     */
    public Token scanSymbol() {
        int length = 0;
        while (CharType.isSymbol(charAt(offset + length))) {
            length++;
        }
        String literals = input.substring(offset, offset + length);
        // 倒序遍历，查询符合条件的 符号。例如 literals = ";;"，会是拆分成两个 ";"。如果基于正序，literals = "<="，会被解析成 "<" + "="。
        Symbol symbol;
        while (null == (symbol = Symbol.literalsOf(literals))) {
            literals = input.substring(offset, offset + --length);
        }
        return new Token(symbol, literals, offset + length);
    }

    private char charAt(final int index) {
        return index >= input.length() ? (char) CharType.EOI : input.charAt(index);
    }
}
