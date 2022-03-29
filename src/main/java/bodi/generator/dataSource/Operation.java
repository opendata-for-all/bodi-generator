package bodi.generator.dataSource;

/**
 * A set of operations that a chatbot can do on a {@link TabularDataSource}.
 */
public enum Operation {

    /**
     * No operation or empty operation.
     */
    NO_OPERATION,
    /**
     * SHOW_FIELD_DISTINCT Operation.
     * <p>
     * Given a field name, this operation gets the unique values (i.e. a set) of that field
     */
    SHOW_FIELD_DISTINCT,
    /**
     * FREQUENT_VALUE_IN_FIELD Operation.
     * <p>
     * Given a field name, this operation gets the most or the least frequent values of that field
     */
    FREQUENT_VALUE_IN_FIELD,

    /**
     * VALUE_FREQUENCY Operation.
     * <p>
     * Given a value of a field, this operation gets its frequency (i.e. the number of occurrences) within the field.
     */
    VALUE_FREQUENCY
}
