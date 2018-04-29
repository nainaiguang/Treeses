package com.nng.unit;

import com.google.common.base.CharMatcher;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * SQL工具类.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SQLUtil implements Util{

    /**
     * 去掉SQL表达式的特殊字符.
     *
     * @param value SQL表达式
     * @return 去掉SQL特殊字符的表达式
     */
    public static String getExactlyValue(final String value) {
        return null == value ? null : CharMatcher.anyOf("[]`'\"").removeFrom(value);
    }
}
