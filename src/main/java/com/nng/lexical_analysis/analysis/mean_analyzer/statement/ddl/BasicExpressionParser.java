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

package com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl;

import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.*;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.token.TableToken;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Literals;
import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import com.nng.unit.NumberUtil;
import com.nng.unit.SQLUtil;
import lombok.RequiredArgsConstructor;

/**
 * Basic expression parser.
 */
@RequiredArgsConstructor
public final class BasicExpressionParser {
    
    private final SQLParser sqlparser;
    
    /**
     * Parse expression.
     * @param sqlStatement SQL statement
     * @return expression
     */
    public Expression parse(final SQLStatement sqlStatement) {
        int beginPosition = sqlparser.getLexer().getCurrentToken().getEndPosition();
        Expression result = parseExpression(sqlStatement);
        if (result instanceof PropertyExpression) {
            setTableToken(sqlStatement, beginPosition, (PropertyExpression) result);
        }
        return result;
    }
    
    // TODO complete more expression parse
    private Expression parseExpression(final SQLStatement sqlStatement) {
        String literals = sqlparser.getLexer().getCurrentToken().getLiterals();
        final int beginPosition = sqlparser.getLexer().getCurrentToken().getEndPosition() - literals.length();
        final Expression expression = getExpression(literals, sqlStatement);
        sqlparser.getLexer().nextToken();
        if (sqlparser.skipIfEqual(Symbol.DOT)) {
            String property = sqlparser.getLexer().getCurrentToken().getLiterals();
            sqlparser.getLexer().nextToken();
            return skipIfCompositeExpression(sqlStatement)
                    ? new IgnoreExpression()
                    : new PropertyExpression(new IdentifierExpression(literals), property);
        }
        if (sqlparser.equalAny(Symbol.LEFT_PAREN)) {
            sqlparser.skipParentheses();
            skipRestCompositeExpression(sqlStatement);
            return new IgnoreExpression();
        }
        return skipIfCompositeExpression(sqlStatement)
                ? new IgnoreExpression() : expression;
    }
    
    private Expression getExpression(final String literals, final SQLStatement sqlStatement) {
        if (sqlparser.equalAny(Symbol.QUESTION)) {
            sqlStatement.increaseParametersIndex();
            return new PlaceholderExpression(sqlStatement.getParametersIndex() - 1);
        }
        if (sqlparser.equalAny(Literals.CHARS)) {
            return new TextExpression(literals);
        }
        if (sqlparser.equalAny(Literals.INT)) {
            return new NumberExpression(NumberUtil.getExactlyNumber(literals, 10));
        }
        if (sqlparser.equalAny(Literals.FLOAT)) {
            return new NumberExpression(Double.parseDouble(literals));
        }
        if (sqlparser.equalAny(Literals.HEX)) {
            return new NumberExpression(NumberUtil.getExactlyNumber(literals, 16));
        }
        if (sqlparser.equalAny(Literals.IDENTIFIER)) {
            return new IdentifierExpression(SQLUtil.getExactlyValue(literals));
        }
        return new IgnoreExpression();
    }
    
    private boolean skipIfCompositeExpression(final SQLStatement sqlStatement) {
        if (sqlparser.equalAny(
                Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT, Symbol.LEFT_PAREN)) {
            sqlparser.skipParentheses();
            skipRestCompositeExpression(sqlStatement);
            return true;
        }
        return false;
    }
    
    private void skipRestCompositeExpression(final SQLStatement sqlStatement) {
        while (sqlparser.skipIfEqual(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT)) {
            if (sqlparser.equalAny(Symbol.QUESTION)) {
                sqlStatement.increaseParametersIndex();
            }
            sqlparser.getLexer().nextToken();
            sqlparser.skipParentheses();
        }
    }
    
    private void setTableToken(final SQLStatement sqlStatement, final int beginPosition, final PropertyExpression propertyExpr) {
        String owner = propertyExpr.getTable_name().getName();
        if (sqlStatement.getTables().getTableNames().contains(SQLUtil.getExactlyValue(propertyExpr.getTable_name().getName()))) {
            sqlStatement.getSqlTokens().add(new TableToken(beginPosition - owner.length(), owner));
        }
    }
}
