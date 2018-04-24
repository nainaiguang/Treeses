package com.nng.lexical_analysis.analysis.mean_analyzer.token;

import com.nng.unit.SQLUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 表标记对象.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class TableToken implements SQLToken {

    /**
     * 开始位置
     */
    private final int beginPosition;
    /**
     * 表达式
     */
    private final String originalLiterals;
    
    /**
     * 获取表名称.
     * 
     * @return 表名称
     */
    public String getTableName() {
        return SQLUtil.getExactlyValue(originalLiterals);
    }
}
