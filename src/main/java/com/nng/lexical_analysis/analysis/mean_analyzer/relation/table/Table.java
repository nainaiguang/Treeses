package com.nng.lexical_analysis.analysis.mean_analyzer.relation.table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import com.google.common.base.Optional;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class Table {
    /**
     * 表
     */
    private final String name;
    /**
     * 别名
     */
    private final Optional<String> alias;
}
