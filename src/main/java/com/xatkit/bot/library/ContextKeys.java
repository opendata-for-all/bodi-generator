package com.xatkit.bot.library;

/**
 * This class stores constant keys used to access information relative to a chatbot, like parameters or intents.
 */
public final class ContextKeys {

    private ContextKeys() {
    }

    // Session Keys

    /**
     * The constant TABULAR_DATA_SOURCE.
     */
    public static final String TABULAR_DATA_SOURCE = "TABULAR_DATA_SOURCE";
    /**
     * The constant RESULT_SET.
     */
    public static final String RESULT_SET = "RESULT_SET";
    /**
     * The constant STATEMENT.
     */
    public static final String STATEMENT = "STATEMENT";
    /**
     * The constant FILTER_FIELD_OPTIONS.
     */
    public static final String FILTER_FIELD_OPTIONS = "FILTER_FIELD_OPTIONS";
    /**
     * The constant VIEW_FIELD_OPTIONS.
     */
    public static final String VIEW_FIELD_OPTIONS = "VIEW_FIELD_OPTIONS";
    /**
     * The constant PAGE_COUNT.
     */
    public static final String PAGE_COUNT = "PAGE_COUNT";
    /**
     * The constant LAST_FIELD_NAME.
     */
    public static final String LAST_FIELD = "LAST_FIELD";
    /**
     * The constant LAST_OPERATOR_NAME.
     */
    public static final String LAST_OPERATOR = "LAST_OPERATOR";

     // Intent Parameter Keys

    /**
     * The constant field.
     */
    public static final String FIELD = "field";

    /**
     * The constant operator.
     */
    public static final String OPERATOR = "operator";

    /**
     * The constant value.
     */
    public static final String VALUE = "value";


}
