package com.nng.lexical_analysis.analysis.word_analyzer;

import com.nng.lexical_analysis.analysis.word_analyzer.analyzer.CharType;
import com.nng.lexical_analysis.analysis.word_analyzer.analyzer.Dictionary;
import com.nng.lexical_analysis.analysis.word_analyzer.analyzer.Tokenizer;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Assist;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Token;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 词法解析器.
 */
@RequiredArgsConstructor
public class Lexer {
    @Getter
    private final String input;
    /**
     * 词法词典
     */
    private final Dictionary dictionary;
    /**
     * 解析到的位置
     */
    private  int offset;
    /**
     * 某个词的类型，标记
     */
    @Getter
    private Token currentToken;

    /**
     * 分析下一个词法标记.
     *
     * @see #currentToken
     * @see #offset
     */
    public final void nextToken(){
        skipIgnored();
        if (isVariableBegin()) { // 变量
            currentToken = new Tokenizer(input, dictionary, offset).scanVariable();
        } else if (isNCharBegin()) { // N\
            currentToken = new Tokenizer(input, dictionary, ++offset).scanChars();
        } else if (isIdentifierBegin()) { // Keyword + Literals.IDENTIFIER
            currentToken = new Tokenizer(input, dictionary, offset).scanIdentifier();
        } else if (isHexDecimalBegin()) { // 十六进制
            currentToken = new Tokenizer(input, dictionary, offset).scanHexDecimal();
        } else if (isNumberBegin()) { // 数字（整数+浮点数）
            currentToken = new Tokenizer(input, dictionary, offset).scanNumber();
        } else if (isSymbolBegin()) { // 符号
            currentToken = new Tokenizer(input, dictionary, offset).scanSymbol();
        } else if (isCharsBegin()) { // 字符串，例如："abc"
            currentToken = new Tokenizer(input, dictionary, offset).scanChars();
        } else if (isEnd()) { // 结束
            currentToken = new Token(Assist.END, "", offset);
        } else { // 分析错误，无符合条件的词法标记
            currentToken = new Token(Assist.ERROR, "", offset);
        }
        offset=currentToken.getEndPosition();
        System.out.println("| " + currentToken.getLiterals() + " | "
                +  currentToken.getType().getClass().getSimpleName() + " | " + currentToken.getType() + " | "
                + currentToken.getEndPosition() + " |");

    }
    /**
     * 跳过忽略的词法标记
     * 1. 空格
     * 2. SQL Hint
     * 3. SQL 注释
     */
    private void skipIgnored()
    {
        //空格
        offset=new Tokenizer(input,dictionary,offset).skipWhitespace();
        //sql注释
        while(isCommentBegin()) {
            offset = new Tokenizer(input,dictionary,offset).skipComment();
            offset=new Tokenizer(input,dictionary,offset).skipWhitespace();
        }
        // SQL Hint
        while (isHintBegin()) {
            offset = new Tokenizer(input, dictionary, offset).skipHint();
            offset = new Tokenizer(input, dictionary, offset).skipWhitespace();
        }

    }

    /**
     * 是否是 SQL 注释
     *
     * @return 是否
     */
    protected boolean isCommentBegin() {
        char current = getCurrentChar(0);
        char next = getCurrentChar(1);

        return ('/' == current && '/' == next )||( '-' == current && '-' == next) ||( '/' == current && '*' == next);
    }

    /**
     * 是否是 SQL Hint
     *
     * @return 是否
     */
    protected boolean isHintBegin() {
        return false;
    }

    /**
     * 是否是 变量
     * MySQL 与 SQL Server 支持
     *
     * @see Tokenizer#scanVariable()
     * @return 是否
     */
    protected boolean isVariableBegin() {
        return false;
    }

    protected boolean isSupportNChars() {
        return false;
    }

    /**
     * 是否 N\
     * 目前 SQLServer 独有：在 SQL Server 中處理 Unicode 字串常數時，必需為所有的 Unicode 字串加上前置詞 N
     *
     * @see Tokenizer#scanChars()
     * @return 是否
     */
    private boolean isNCharBegin() {
        return isSupportNChars() && 'N' == getCurrentChar(0) && '\'' == getCurrentChar(1);
    }

    /**
     * 是否是 Keyword + Literals.IDENTIFIER
     *
     * @see Tokenizer#scanIdentifier()
     * @return 是否
     */
    private boolean isIdentifierBegin() {
        return isIdentifierBegin(getCurrentChar(0));
    }

    private boolean isIdentifierBegin(final char ch) {
        return CharType.isAlphabet(ch) || '`' == ch || '_' == ch || '$' == ch;
    }

    /**
     * 是否是 十六进制
     *
     * @see Tokenizer#scanHexDecimal()
     * @return 是否
     */
    private boolean isHexDecimalBegin() {
        return '0' == getCurrentChar(0) && 'x' == getCurrentChar(1);
    }
    /**
     * 是否是 数字
     * '-' 需要特殊处理。".2" 被处理成省略0的小数，"-.2" 不能被处理成省略的小数，否则会出问题。
     * 例如说，"SELECT a-.2" 处理的结果是 "SELECT" / "a" / "-" / ".2"
     *
     * @return 是否
     */
    private boolean isNumberBegin() {
        return CharType.isDigital(getCurrentChar(0)) // 数字
                || ('.' == getCurrentChar(0) && CharType.isDigital(getCurrentChar(1)) && !isIdentifierBegin(getCurrentChar(-1)) // 浮点数，如果是第一位数，可能出错。
                || ('-' == getCurrentChar(0) && ('.' == getCurrentChar(0) || CharType.isDigital(getCurrentChar(1))))); // 负数
    }
    /**
     * 是否是 符号
     *
     * @see Tokenizer#scanSymbol()
     * @return 是否
     */
    private boolean isSymbolBegin() {
        return CharType.isSymbol(getCurrentChar(0));
    }
    /**
     * 是否是 字符或字符串
     *
     * @see Tokenizer#scanChars()
     * @return 是否
     */
    private boolean isCharsBegin() {
        return '\'' == getCurrentChar(0) || '\"' == getCurrentChar(0);
    }

    /**
     * 判断是否到达尾巴了
     * @return
     */
    public boolean isEnd() {
        return offset >= input.length();
    }
    protected final char getCurrentChar(final int offset) {
        return this.offset + offset >= input.length() ? (char) CharType.EOI : input.charAt(this.offset + offset);
    }
}
