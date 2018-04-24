/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.create;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Table;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.AliasExpressionParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.BasicExpressionParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.token.TableToken;
import com.nng.lexical_analysis.analysis.word_analyzer.token.DefaultKeyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Keyword;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import com.nng.unit.SQLUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Table references clause parser.
 *
 * @author zhangliang
 */
public class TableReferencesClauseParser implements SQLClauseParser {
    

    
    @Getter
    private final SQLParser lexerEngine;
    
    private final AliasExpressionParser aliasExpressionParser;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public TableReferencesClauseParser(final SQLParser lexerEngine) {

        this.lexerEngine = lexerEngine;
        aliasExpressionParser = new AliasExpressionParser(lexerEngine);
        basicExpressionParser = new BasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse table references.
     *
     * @param sqlStatement SQL statement
     * @param isSingleTableOnly is parse single table only
     */
    public final void parse(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        do {
            parseTableReference(sqlStatement, isSingleTableOnly);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
    }
    
    protected void parseTableReference(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        parseTableFactor(sqlStatement, isSingleTableOnly);
    }
    
    protected final void parseTableFactor(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        final int beginPosition = lexerEngine.getLexer().getCurrentToken().getEndPosition() - lexerEngine.getLexer().getCurrentToken().getLiterals().length();
        String literals = lexerEngine.getLexer().getCurrentToken().getLiterals();
        lexerEngine.getLexer().nextToken();
        if (lexerEngine.equalAny(Symbol.DOT)) {
            throw new UnsupportedOperationException("Cannot support SQL for `schema.table`");
        }
        String tableName = SQLUtil.getExactlyValue(literals);
        if (Strings.isNullOrEmpty(tableName)) {
            return;
        }
        Optional<String> alias = aliasExpressionParser.parseTableAlias();
        if (isSingleTableOnly) {
            sqlStatement.getSqlTokens().add(new TableToken(beginPosition, literals));
            sqlStatement.getTables().add(new Table(tableName, alias));
        }
        parseJoinTable(sqlStatement);
        if (isSingleTableOnly && !sqlStatement.getTables().isSingleTable()) {
            throw new UnsupportedOperationException("Cannot support Multiple-Table.");
        }
    }
    
    private void parseJoinTable(final SQLStatement sqlStatement) {
        while (parseJoinType()) {
            if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
                throw new UnsupportedOperationException("Cannot support sub query for join table.");
            }
            parseTableFactor(sqlStatement, false);
            parseJoinCondition(sqlStatement);
        }
    }
    
    private boolean parseJoinType() {
        List<Keyword> joinTypeKeywords = new LinkedList<>();
        joinTypeKeywords.addAll(Arrays.asList(
                DefaultKeyword.INNER, DefaultKeyword.OUTER, DefaultKeyword.LEFT, DefaultKeyword.RIGHT, DefaultKeyword.FULL, DefaultKeyword.CROSS, DefaultKeyword.NATURAL, DefaultKeyword.JOIN));
        joinTypeKeywords.addAll(Arrays.asList(getKeywordsForJoinType()));
        Keyword[] joinTypeKeywordArrays = joinTypeKeywords.toArray(new Keyword[joinTypeKeywords.size()]);
        if (!lexerEngine.equalAny(joinTypeKeywordArrays)) {
            return false;
        }
        lexerEngine.skipAll(joinTypeKeywordArrays);
        return true;
    }
    
    protected Keyword[] getKeywordsForJoinType() {
        return new Keyword[0];
    }
    
    private void parseJoinCondition(final SQLStatement sqlStatement) {
        if (lexerEngine.skipIfEqual(DefaultKeyword.ON)) {
            do {
                basicExpressionParser.parse(sqlStatement);
                lexerEngine.accept(Symbol.EQ);
                basicExpressionParser.parse(sqlStatement);
            } while (lexerEngine.skipIfEqual(DefaultKeyword.AND));
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.USING)) {
            lexerEngine.skipParentheses();
        }
    }
    
    /**
     * Parse single table without alias.
     *
     * @param sqlStatement SQL statement
     */
    public final void parseSingleTableWithoutAlias(final SQLStatement sqlStatement) {
        int beginPosition = lexerEngine.getLexer().getCurrentToken().getEndPosition() - lexerEngine.getLexer().getCurrentToken().getLiterals().length();
        sqlStatement.getSqlTokens().add(new TableToken(beginPosition, lexerEngine.getLexer().getCurrentToken().getLiterals()));
        sqlStatement.getTables().add(new Table(lexerEngine.getLexer().getCurrentToken().getLiterals(), Optional.<String>absent()));
        lexerEngine.getLexer().nextToken();
    }
}
