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
    public static final String LAST_FIELD_NAME = "LAST_FIELD_NAME";
    /**
     * The constant LAST_OPERATOR_NAME.
     */
    public static final String LAST_OPERATOR_NAME = "LAST_OPERATOR_NAME";

     // Intent Parameter Keys

    /**
     * The constant TEXTUAL_FIELD_NAME.
     */
    public static final String TEXTUAL_FIELD_NAME = "TEXTUAL_FIELD_NAME";
    /**
     * The constant NUMERIC_FIELD_NAME.
     */
    public static final String NUMERIC_FIELD_NAME = "NUMERIC_FIELD_NAME";
    /**
     * The constant DATE_FIELD_NAME.
     */
    public static final String DATE_FIELD_NAME = "DATE_FIELD_NAME";
    /**
     * The constant TEXTUAL_OPERATOR_NAME.
     */
    public static final String TEXTUAL_OPERATOR_NAME = "TEXTUAL_OPERATOR_NAME";
    /**
     * The constant NUMERIC_OPERATOR_NAME.
     */
    public static final String NUMERIC_OPERATOR_NAME = "NUMERIC_OPERATOR_NAME";
    /**
     * The constant DATE_OPERATOR_NAME.
     */
    public static final String DATE_OPERATOR_NAME = "DATE_OPERATOR_NAME";
    /**
     * The constant value.
     */
    public static final String VALUE = "value";
    /**
     * The constant NUMERIC_VALUE.
     */
    public static final String NUMERIC_VALUE = "NUMERIC_VALUE";
    /**
     * The constant DATE_VALUE.
     */
    public static final String DATE_VALUE = "DATE_VALUE";
    /**
     * The constant TEXTUAL_VALUE.
     */
    public static final String TEXTUAL_VALUE = "TEXTUAL_VALUE";

}
