package com.nng.lexical_analysis.analysis.mean_analyzer.token;

import com.nng.unit.SQLUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Index token.
 */
@RequiredArgsConstructor
@ToString
public final class IndexToken implements SQLToken {
    
    @Getter
    private final int beginPosition;
    
    @Getter
    private final String originalLiterals;
    
    private final String tableName;
    
    /**
     * Get index name.
     * 
     * @return index name
     */
    public String getIndexName() {
        return SQLUtil.getExactlyValue(originalLiterals);
    }
    
    /**
     * Get table name.
     *
     * @return table name
     */
    public String getTableName() {
        return SQLUtil.getExactlyValue(tableName);
    }
}
