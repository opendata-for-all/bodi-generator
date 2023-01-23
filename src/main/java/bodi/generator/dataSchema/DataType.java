package bodi.generator.dataSchema;

import bodi.generator.dataSource.TabularDataSource;

/**
 * Enumeration of all data types that can be inferred from a {@link TabularDataSource}, which are defined in the
 * {@link SchemaField} objects.
 *
 * @see SchemaField
 */
public enum DataType {

    /**
     * Number data type.
     */
    NUMBER,

    /**
     * Datetime data type.
     */
    DATETIME,

    /**
     * Text data type.
     */
    TEXT,

    /**
     * No data type.
     */
    EMPTY
}
