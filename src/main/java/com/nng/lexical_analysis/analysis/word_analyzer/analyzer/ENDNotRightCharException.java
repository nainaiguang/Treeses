package com.nng.lexical_analysis.analysis.word_analyzer.analyzer;

import com.nng.exception.TreesesException;

public final class ENDNotRightCharException extends TreesesException {
    private static final long serialVersionUID = 8575890835166900925L;

    private static final String MESSAGE = "Illegal input, unterminated '%s'.";

    public ENDNotRightCharException(final char cause) {

        super(String.format(MESSAGE,cause));
    }
    public ENDNotRightCharException(final String cause) {

        super(String.format(MESSAGE,cause));
    }
}
