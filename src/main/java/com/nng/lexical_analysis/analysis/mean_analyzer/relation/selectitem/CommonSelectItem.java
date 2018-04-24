package com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 通用选择项.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class CommonSelectItem implements SelectItem{
    /**
     * 表达式
     */
    private final String expression;
    /**
     * 别名
     */
    private final Optional<String> alias;
}
