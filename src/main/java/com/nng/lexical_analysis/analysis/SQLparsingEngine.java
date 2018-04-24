package com.nng.lexical_analysis.analysis;

import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.exception.SQLParsingUnsupportedException;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.TreeseParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.alter.TreesesAlterParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.create.TreesesCreateParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.drop.TreesesDropParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.truncate.TreesesTruncateParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.delete.TreesesDeleteParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.insert.TreesesInsertParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.update.TreesesUpdateParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.select.TreesesSelectParser;
import com.nng.lexical_analysis.analysis.word_analyzer.LexerEngine;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SQLparsingEngine {
    /**
     * SQL
     */
    private final String sql;
    /**
     * 返回管理类以得到一个解析包
     * @return
     */
    public SQLStatement parse() {
        // 获取 SQL解析器
        SQLParser sqlParser = new TreeseParser(sql);
        sqlParser.skipIfEqual(Symbol.SEMI); // 跳过 ";"
        if (sqlParser.equalAny(DefaultKeyword.WITH)) { // WITH Syntax
            skipWith(sqlParser);
        }
        // 获取对应 SQL语句解析器 解析SQL
        if (sqlParser.equalAny(DefaultKeyword.SELECT)) {
            return new TreesesSelectParser(sqlParser).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.UPDATE)) {
            return new TreesesUpdateParser(sqlParser).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.DELETE)) {
            return new TreesesDeleteParser(sqlParser).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.CREATE)) {
            return new TreesesCreateParser(sqlParser).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.INSERT)) {
            return new TreesesInsertParser(sqlParser).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.ALTER)) {
            return new TreesesAlterParser(sqlParser).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.DROP)) {
            return new TreesesDropParser(sqlParser).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.TRUNCATE)) {
            return new TreesesTruncateParser(sqlParser).parse();
        }
        throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
    }

    private void skipWith(final SQLParser sqlParser) {
        sqlParser.getLexer().nextToken();
        do {
            sqlParser.skipUntil(DefaultKeyword.AS);
            sqlParser.accept(DefaultKeyword.AS);
            sqlParser.skipParentheses();
        } while (sqlParser.skipIfEqual(Symbol.COMMA));
    }
    }

