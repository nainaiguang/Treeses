package com.nng.lexical_analysis.analysis.mean_analyzer.statement;

/**
 * SQL语句解析器.
 */
public interface SQLStatementParser {
    
    /**
     * 解析SQL语句.
     *
     * @return 解析结果
     */
    SQLStatement parse();
}
