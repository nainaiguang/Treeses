package com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 列对象.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class Column {
    /**
     * 列名
     */
    private final String name;
    /**
     * 表名
     */
    private final String tableName;

    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Column column = (Column) obj;
        return Objects.equal(this.name.toUpperCase(), column.name.toUpperCase())
                && Objects.equal(this.tableName.toUpperCase(), column.tableName.toUpperCase());
    }

    public int hashCode() {
        return Objects.hashCode(name.toUpperCase(), tableName.toUpperCase());
    }
}
