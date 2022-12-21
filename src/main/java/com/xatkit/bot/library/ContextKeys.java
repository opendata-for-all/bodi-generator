package com.xatkit.bot.library;

/**
 * This class stores constant keys used to access information relative to a chatbot, like parameters or intents.
 */
public final class ContextKeys {

    private ContextKeys() {
    }

    // Session Keys

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
     * The constant ERROR.
     */
    public static final String ERROR = "error";
    /**
     * The constant STOP.
     */
    public static final String STOP = "stop";
    /**
     * The constant RESULTSET_NUM_ROWS.
     */
    public static final String RESULTSET_NUM_ROWS = "resultset_num_rows";
    /**
     * The constant INTENT_NAME.
     */
    public static final String INTENT_NAME = "intent_name";
    /**
     * The constant RESULTSET.
     */
    public static final String RESULTSET = "resultset";
    /**
     * The constant SQL_QUERIES.
     */
    public static final String SQL_QUERIES = "sql_queries";
    /**
     * The constant CONTINUE.
     */
    public static final String CONTINUE = "continue";
    /**
     * The constant ENTITIES_TO_SPECIFY.
     */
    public static final String ENTITIES_TO_SPECIFY = "entities_to_specify";
    /**
     * The constant ENTITY_TO_SPECIFY.
     */
    public static final String ENTITY_TO_SPECIFY = "entity_to_specify";
    /**
     * The constant VALUE_FIELD_MAP.
     */
    public static final String VALUE_FIELD_MAP = "value_field_map";
    /**
     * The constant BAD_PARAMS.
     */
    public static final String BAD_PARAMS = "bad_params";
    /**
     * The constant BAD_RESULTSET.
     */
    public static final String BAD_RESULTSET = "bad_resultset";
    /**
     * The constant ALL_OK.
     */
    public static final String ALL_OK = "all_ok";

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
    /**
     * The constant row_name.
     */
    public static final String ROW_NAME = "row_name";
    /**
     * The constant number.
     */
    public static final String NUMBER = "number";
}
