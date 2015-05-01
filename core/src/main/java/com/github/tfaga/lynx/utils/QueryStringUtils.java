package com.github.tfaga.lynx.utils;

import com.github.tfaga.lynx.beans.QueryOrder;
import com.github.tfaga.lynx.beans.QueryParameters;
import com.github.tfaga.lynx.enums.OrderDirection;
import com.github.tfaga.lynx.enums.QueryFormatError;
import com.github.tfaga.lynx.exceptions.QueryFormatException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * @author Tilen Faganel
 * @version 1.0.0
 * @since 1.0.0
 */
public class QueryStringUtils {

    private static final Logger log = Logger.getLogger(QueryStringUtils.class.getSimpleName());

    public static final String LIMIT_DELIMITER = "$limit";

    public static final String LIMIT_DELIMITER_ALT = "$max";

    public static final String OFFSET_DELIMITER = "$offset";

    public static final String OFFSET_DELIMITER_ALT = "$skip";

    public static final String ORDER_DELIMITER = "$order";

    public static final String ORDER_DELIMITER_ALT = "$sort";

    public static final String FIELDS_DELIMITER = "$fields";

    public static final String FIELDS_DELIMITER_ALT = "$select";

    public static QueryParameters parseUri(URI uri) {

        log.finest("Parsing uri object: " + uri);

        if (uri == null) return new QueryParameters();

        return parse(uri.getRawQuery());
    }

    public static QueryParameters parseUri(String uri) {

        log.finest("Parsing uri string: " + uri);

        if (uri == null || uri.isEmpty()) return new QueryParameters();

        int idxQuery = uri.indexOf("?");
        int idxFragment = uri.indexOf("#");

        if (idxQuery == -1) return new QueryParameters();

        if (idxFragment == -1) return parse(uri.substring(idxQuery + 1));

        if (idxFragment < idxQuery) return new QueryParameters();

        return parse(uri.substring(idxQuery + 1, idxFragment));
    }

    public static QueryParameters parse(String queryString) {

        log.finest("Parsing query string: " + queryString);

        QueryParameters params = new QueryParameters();

        if (queryString == null || queryString.isEmpty()) return params;

        for (String pair : queryString.split("&")) {

            int idxOfPair = pair.indexOf("=");

            if (idxOfPair == -1) {

                parsePair(params, pair, "");
                continue;
            }

            String key, value;

            try {

                key = URLDecoder.decode(pair.substring(0, idxOfPair), StandardCharsets.UTF_8.displayName());
                value = URLDecoder.decode(pair.substring(idxOfPair + 1), StandardCharsets.UTF_8.displayName());
            } catch (UnsupportedEncodingException e) {

                log.severe("UTF-8 encoding is not supported on this system");

                throw new AssertionError();
            }

            parsePair(params, key, value);
        }

        return params;
    }

    private static void parsePair(QueryParameters params, String key, String value) {

        log.finest("Parsing query string pair: " + key + " " + value);

        if (params == null) return;

        if (key == null || key.isEmpty()) return;

        if (value == null || value.isEmpty()) return;

        switch (key) {

            case LIMIT_DELIMITER:
            case LIMIT_DELIMITER_ALT:

                try {

                    params.setLimit(Long.parseLong(value));
                } catch (NumberFormatException e) {

                    log.finest("Value for '" + key + "' was incorrect: '" + value + "'");

                    throw new QueryFormatException(key, QueryFormatError.NOT_A_NUMBER);
                }

                if (params.getLimit() < 0) {

                    log.finest("Value for '" + key + "' was negative: '" + value + "'");

                    throw new QueryFormatException(key, QueryFormatError.NEGATIVE);
                }

                break;

            case OFFSET_DELIMITER:
            case OFFSET_DELIMITER_ALT:

                try {

                    params.setOffset(Long.parseLong(value));
                } catch (NumberFormatException e) {

                    log.finest("Value for '" + key + "' was incorrect: '" + value + "'");

                    throw new QueryFormatException(key, QueryFormatError.NOT_A_NUMBER);
                }

                if (params.getOffset() < 0) {

                    log.finest("Value for '" + key + "' was negative: '" + value + "'");

                    throw new QueryFormatException(key, QueryFormatError.NEGATIVE);
                }

                break;

            case ORDER_DELIMITER:
            case ORDER_DELIMITER_ALT:

                params.getOrder().clear();

                String[] ordersString = value.split(",");

                for (String o : ordersString) {

                    QueryOrder order = parseOrder(key, o);

                    if (order != null && !params.getOrder().stream()
                            .anyMatch(co -> co.getField()
                                    .equals(order.getField()))) {

                        params.getOrder().add(order);
                    }
                }

                break;

            case FIELDS_DELIMITER:
            case FIELDS_DELIMITER_ALT:

                String[] fields = value.split(",");

                Arrays.stream(fields).filter(f -> !f.isEmpty()).distinct()
                        .forEach(f -> params.getFields().add(f));

                break;
        }
    }

    private static QueryOrder parseOrder(String key, String value) {

        log.finest("Parsing order string: " + value);

        if (value == null || value.isEmpty()) return null;

        QueryOrder o = new QueryOrder();

        String[] pair = value.split(" ");

        if (pair[0].isEmpty()) {

            log.finest("Order string '" + value + "' is malformed");

            throw new QueryFormatException(key, QueryFormatError.MALFORMED);
        }

        o.setField(pair[0]);

        if (pair.length > 1) {

            try {

                o.setOrder(OrderDirection.valueOf(pair[1]));
            } catch (IllegalArgumentException e) {

                throw new QueryFormatException(key, QueryFormatError.MALFORMED);
            }
        } else {

            o.setOrder(OrderDirection.ASC);
        }

        return o;
    }
}
