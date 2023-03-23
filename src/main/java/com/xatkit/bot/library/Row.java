package com.xatkit.bot.library;


import java.io.Serializable;
import java.util.List;

/**
 * Represents a row within a table. That is, an entry of a table.
 * <p>
 * It does not contain information about the name of the columns (i.e. the labels of its cells), so it should not be
 * used alone.
 * <p>
 * bodi.generator.dataSchemaTabularDataSource and {@link ResultSet} are examples of classes that use {@link Row}
 * to create a tabular data structure.
 */
public class Row implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The content of the row. Each element of this collection represents a cell.
     */
    private List<String> values;

    /**
     * Instantiates a new Row.
     *
     * @param values the content of the row
     */
    public Row(List<String> values) {
        this.values = values;
    }

    /**
     * Remove a cell from the row.
     * <p>
     * It should be used when removing a whole column from a table (i.e. a collection of {@link Row})
     *
     * @param i the
     * @return the string
     */
    public String removeValue(int i) {
        return this.values.remove(i);
    }

    /**
     * Gets the value for a specific position of the row (i.e. its value for a given column index).
     *
     * @param index the index of the column
     * @return the column value
     */
    public String getColumnValue(int index) {
        return this.values.get(index);
    }

    /**
     * Gets the value for a specific position of the row (i.e. its value for a given column index) and lower-cases
     * the result if the parameter {@code lowerCase} is true.
     *
     * @param index     the index of the column
     * @param lowerCase the lower-case condition
     * @return the column value
     */
    public String getColumnValue(int index, boolean lowerCase) {
        String value = this.getColumnValue(index);
        if (lowerCase) {
            return value.toLowerCase();
        }
        return value;
    }

    /**
     * Gets the content of the {@link Row}.
     *
     * @return the content of the row
     */
    public List<String> getValues() {
        return values;
    }

}
