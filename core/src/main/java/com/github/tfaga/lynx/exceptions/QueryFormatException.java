package com.github.tfaga.lynx.exceptions;

import com.github.tfaga.lynx.enums.QueryFormatError;

/**
 * @author Tilen Faganel
 * @version 1.0.0
 * @since 1.0.0
 */
public class QueryFormatException extends RuntimeException {

    private String field;

    private QueryFormatError reason;

    public QueryFormatException(String msg, String field, QueryFormatError reason) {

        super(msg);

        this.field = field;
        this.reason = reason;
    }

    public String getField() {
        return field;
    }

    public QueryFormatError getReason() {
        return reason;
    }
}
