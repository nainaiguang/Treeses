/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.create;


import com.nng.exception.TreesesException;
import com.nng.lexical_analysis.analysis.mean_analyzer.SQLParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.exception.SQLParsingException;
import com.nng.lexical_analysis.analysis.mean_analyzer.exception.SQLParsingUnsupportedException;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.IdentifierExpression;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatementParser;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.ddl.DDLStatement;
import com.nng.lexical_analysis.analysis.word_analyzer.Lexer;
import com.nng.lexical_analysis.analysis.word_analyzer.token.*;
import com.nng.lexical_analysis.contact.controlType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Create语句解析器.
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractCreateParser implements SQLStatementParser {
    
    private final SQLParser sqlParser;
    
    private final createStatement createStatement;
    
    public AbstractCreateParser(final SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        createStatement = new createStatement();
        createStatement.setControlType(controlType.CREATE);
    }
    
    @Override
    public DDLStatement parse() {
        sqlParser.getLexer().nextToken();
        getSqlParser().skipAll(getSkipWordsBetweenCreateAndKeyword());
        if (!sqlParser.skipIfEqual(DefaultKeyword.TABLE)) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
        getSqlParser().skipAll(getSkipWordsBetweenKeywordAndTableName());
        sqlParser.parseSingleTable(createStatement);
        //要判断是否结束或者是不是左括号才能才是解析
        if(sqlParser.equalAny(Symbol.LEFT_PAREN))
        {
            parserItem(createStatement);
        }
        else if(!sqlParser.getLexer().isEnd())
        {
            throw new TreesesException("not right end");
        }
        //sqlParser.getLexer().nextToken();
        return createStatement;
    }




    protected abstract Keyword[] getSkipWordsBetweenCreateAndKeyword();
    
    protected abstract Keyword[] getSkipWordsBetweenKeywordAndTableName();

    /**
     * 分析括号里面的词
     * @param statement
     * @return
     */
    protected DDLStatement parserItem(DDLStatement statement)
    {
        Boolean isRight=true;//一个表示接下来是否应该还有词的变量
        String colunm_name;
        String colunm_leixing;
        if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
            while (!sqlParser.getLexer().isEnd()) {
                sqlParser.getLexer().nextToken();
                colunm_name=parsercolumn();
                sqlParser.getLexer().nextToken();
                colunm_leixing=parserLiteral();
                statement.setColumn(colunm_name,colunm_leixing);
                sqlParser.getLexer().nextToken();
                isRight=parserdouhao();
                if(isRight)
                {
                    continue;
                }
                else
                {
                    sqlParser.getLexer().nextToken();
                    if(sqlParser.getLexer().isEnd())
                    {
                        break;
                    }
                    else
                    throw new TreesesException("not right end");
                }
            }
        }
        return statement;
    }

    /**
     * 判断是否为行名
     * @return
     */
    private String parsercolumn()
    {
        if(sqlParser.equalAny(Literals.IDENTIFIER))
        {
            return sqlParser.getLexer().getCurrentToken().getLiterals();
        }
        else
        {
            throw new SQLParsingException(sqlParser.getLexer(),Literals.IDENTIFIER);
        }
    }

    /**
     * 判断是否为定义
     * @return
     */
    private String parserLiteral()
    {
        if(sqlParser.equalAny(DefaultKeyword.INT,DefaultKeyword.CHAR,DefaultKeyword.FLOAT,DefaultKeyword.VARCHAR,DefaultKeyword.DOUBLE)){
            return sqlParser.getLexer().getCurrentToken().getLiterals();
        }
        else
        {
            throw new SQLParsingException(sqlParser.getLexer(),Literals.VARIABLE);
        }
    }

    /**
     * 判断是否正确结束
     * @return
     */
    private boolean parserdouhao()
    {
        if(sqlParser.equalAny(Symbol.COMMA))
        {
            return true;
        }
        else if(sqlParser.equalAny(Symbol.RIGHT_PAREN))
        {
            return false;
        }
        else
        {
            throw new SQLParsingException(sqlParser.getLexer(), Assist.END);
        }
    }
}
