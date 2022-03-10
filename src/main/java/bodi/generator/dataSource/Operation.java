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
    SHOW_FIELD_DISTINCT
}
