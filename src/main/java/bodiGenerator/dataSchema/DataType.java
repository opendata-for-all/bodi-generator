package bodiGenerator.dataSchema;

import bodiGenerator.dataSource.TabularDataSource;

/**
 * Enumeration of all data types that can be inferred from a {@link TabularDataSource}, which are defined in the
 * {@link SchemaField} objects
 *
 * @see SchemaField
 */
public enum DataType {
    /**
     * Number data type.
     */
    NUMBER,
    /**
     * Date data type.
     */
    DATE,
    /**
     * Text data type.
     */
    TEXT
}