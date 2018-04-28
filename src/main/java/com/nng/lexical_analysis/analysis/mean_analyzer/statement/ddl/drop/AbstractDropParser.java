package com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.drop;

import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.exception.SQLParsingUnsupportedException;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatementParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Keyword;
import com.nng.lexical_analysis.contact.controlType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Drop语句解析器.
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractDropParser implements SQLStatementParser {
    
    private final SQLParser sqlParser;
    
    private final dropStatement dropStatement;
    
    public AbstractDropParser(final SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        dropStatement = new dropStatement();
        dropStatement.setControlType(controlType.DROP);
    }
    
    @Override
    public DDLStatement parse() {
        sqlParser.getLexer().nextToken();
        if (!sqlParser.skipIfEqual(DefaultKeyword.TABLE)) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
        getSqlParser().skipAll(getSkipWordsBetweenKeywordAndTableName());
        sqlParser.parseSingleTable(dropStatement);
        return dropStatement;
    }
    
    protected abstract Keyword[] getSkipWordsBetweenKeywordAndTableName();
}
