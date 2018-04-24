package com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem;

import com.google.common.base.Optional;

public interface SelectItem {
    /**
     * 获取表达式.
     *
     * @return 表达式
     */
    String getExpression();

    /**
     * 获取别名.
     *
     * @return 别名
     */
    Optional<String> getAlias();
}
