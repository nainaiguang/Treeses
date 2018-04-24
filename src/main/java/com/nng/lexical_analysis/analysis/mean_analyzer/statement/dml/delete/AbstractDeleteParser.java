package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.delete;

import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatementParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Delete语句解析器.
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractDeleteParser implements SQLStatementParser {
    
    private final SQLParser sqlParser;
    
    private final DMLStatement deleteStatement;
    
    public AbstractDeleteParser(final SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        deleteStatement = new DMLStatement();
    }

// Single-Table Syntax ：
//    DELETE [LOW_PRIORITY] [QUICK] [IGNORE] FROM tbl_name
//    [PARTITION (partition_name,...)]
//            [WHERE where_condition]
//            [ORDER BY ...]
//            [LIMIT row_count]

// Multiple-Table Syntax ：
//    DELETE [LOW_PRIORITY] [QUICK] [IGNORE]
//    tbl_name[.*] [, tbl_name[.*]] ...
//    FROM table_references
//    [WHERE where_condition]
// OR
//    DELETE [LOW_PRIORITY] [QUICK] [IGNORE]
//    FROM tbl_name[.*] [, tbl_name[.*]] ...
//    USING table_references
//    [WHERE where_condition]

    @Override
    public DMLStatement parse() {
        sqlParser.getLexer().nextToken(); // 跳过 DELETE
        skipBetweenDeleteAndTable(); // // 跳过关键字，例如：MYSQL 里的 LOW_PRIORITY、IGNORE 和 FROM
        sqlParser.parseSingleTable(deleteStatement); // 解析表
        sqlParser.skipUntil(DefaultKeyword.WHERE); // 跳到 WHERE
        sqlParser.parseWhere(deleteStatement); // 解析 WHERE
        return deleteStatement;
    }
    
    protected abstract void skipBetweenDeleteAndTable();
}
