package com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition;

import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import lombok.Getter;

@Getter
public class columnCondition {
    private Column A;
    private Column B;
    private Symbol symbol;
    public columnCondition(Column A,Column B,Symbol symbol)
    {
        this.A=A;
        this.B=B;
        this.symbol=symbol;
    }

}
