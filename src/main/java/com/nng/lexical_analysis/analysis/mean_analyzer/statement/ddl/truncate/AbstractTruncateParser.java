package com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.truncate;


import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.exception.SQLParsingUnsupportedException;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatementParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Keyword;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Truncate语句解析器.
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractTruncateParser implements SQLStatementParser {
    
    private final SQLParser sqlParser;
    
    private final DDLStatement truncateStatement;
    
    public AbstractTruncateParser(final SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        truncateStatement = new DDLStatement();
    }
    
    @Override
    public DDLStatement parse() {
        sqlParser.getLexer().nextToken();
        if (!sqlParser.skipIfEqual(DefaultKeyword.TABLE)) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
        getSqlParser().skipAll(getSkipWordsBetweenKeywordAndTableName());
        sqlParser.parseSingleTable(truncateStatement);
        return truncateStatement;
    }
    
    protected abstract Keyword[] getSkipWordsBetweenKeywordAndTableName();
}
