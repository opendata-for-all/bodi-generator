package bodiGenerator.dataSource;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The object used for querying a {@link TabularDataSource} and returning the result. It contains information about
 * what to filter and query.
 * <p>
 * A {@link Statement} is related to a single {@link TabularDataSource} object.
 */
public class Statement {

    /**
     * The {@link TabularDataSource} object bound to this {@link Statement}
     * @see TabularDataSource
     */
    private TabularDataSource tds;

    /**
     * The collection of filters of a {@link Statement}. Each entry is an individual filter, and consists of three
     * arguments:
     * <ul>
     *     <li>{@code left} - the field (column) where the filter is going to be applied</li>
     *     <li>{@code middle} - the operator of the filter (e.g. {@code =}, {@code >=}, {@code before}, etc.)</li>
     *     <li>{@code right} - the value of the filter. Depending on the field and the operator, it can be a numeric,
     *     textual or date value</li>
     * </ul>
     * Some examples of valid filters are the following ones:
     * <p>
     * {@code <"age", ">=", "21">} (if {@code age} contains only numbers)
     * <p>
     * {@code <"name", "equals", "John">}
     */
    private List<ImmutableTriple<String, String, String>> filters;

    /**
     * The collection of fields that, when querying the {@link TabularDataSource} {@link #tds}, must be present in the
     * result.
     * <p>
     * When this collection is empty, it is assumed that all fields of {@link #tds} will be in the result,
     * since it is not possible to show a table with no columns
     */
    private List<String> fields;

    /**
     * If true, all filters will not be case-sensitive. For instance {@code "John"} would be considered equal to
     * {@code "john"}.
     * <p>
     * Otherwise, all filters will be case-sensitive ({@code "John"} would be different from {@code "john"}).
     */
    private boolean ignoreCaseFilterValue;

    /**
     * Instantiates a new Statement bound to a given {@link TabularDataSource}
     *
     * @param tds the {@link TabularDataSource}
     */
    public Statement(TabularDataSource tds) {
        this.tds = tds;
        this.filters = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.ignoreCaseFilterValue = false;
    }

    /**
     * Sets {@code #ignoreCaseFilterValue}
     *
     * @param ignoreCaseFilterValue the value
     * @return the statement
     */
    public Statement ignoreCaseFilterValue(boolean ignoreCaseFilterValue) {
        this.ignoreCaseFilterValue = ignoreCaseFilterValue;
        return this;
    }

    /**
     * Gets the {@link TabularDataSource} bound to the {@link Statement}
     *
     * @return the tabular data source
     */
    public TabularDataSource getTabularDataSource() {
        return tds;
    }

    /**
     * Add a new filter to {@link #filters}
     *
     * @param field    the filter's field
     * @param operator the filter's operator
     * @param value    the filter's value
     * @return the statement
     */
    public Statement addFilter(String field, String operator, String value) {
        if (!filters.contains(new ImmutableTriple<>(field, operator, value))) {
            filters.add(new ImmutableTriple<>(field, operator, value));
        }
        return this;
    }

    // TODO: removeFilter Method
    // TODO: removeField Method

    /**
     * Add a new field to {@link #fields}.
     *
     * @param field the field
     * @return the statement
     */
    public Statement addField(String field) {
        if (!fields.contains(field)) {
            fields.add(field);
        }
        return this;
    }

    /**
     * Execute the query with the current {@link #filters}, {@link #fields} and the {@link #ignoreCaseFilterValue}
     * condition.
     *
     * @return the result set
     * @see ResultSet
     */
    public ResultSet executeQuery() {
        List<String> header =tds.getHeaderCopy();
        List<Row> table = tds.getTableCopy();
        // Filtering
        for (ImmutableTriple<String, String, String> f : filters) {
            String value;
            if (ignoreCaseFilterValue) {
                value = f.right.toLowerCase();
            } else {
                value = f.right;
            }
            switch(f.middle) {
                // Numeric Filters
                case "=":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) == Float.parseFloat(f.right)));
                    break;
                case "<":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) < Float.parseFloat(f.right)));
                    break;
                case "<=":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) <= Float.parseFloat(f.right)));
                    break;
                case ">":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) > Float.parseFloat(f.right)));
                    break;
                case ">=":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) >= Float.parseFloat(f.right)));
                    break;
                case "!=":
                    table.removeIf(row -> !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) != Float.parseFloat(f.right)));
                    break;
                // Textual Filters
                case "equals": // Also a date filter
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue).equals(value)));
                    break;
                case "different": // Also a date filter
                    table.removeIf(row -> row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue).equals(value));
                    break;
                case "contains":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue).contains(value)));
                    break;
                case "starts with":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue).startsWith(value)));
                    break;
                case "ends with":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue).endsWith(value)));
                    break;
                // Date Filters
                case "before":
                    LocalDateTime filterDate = LocalDateTime.parse(f.right, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    table.removeIf(row -> {
                        String rowDateString = row.getColumnValue(header.indexOf(f.left));
                        LocalDateTime rowDate = LocalDateTime.parse(rowDateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        return rowDate.isAfter(filterDate);
                    });
                    break;
                case "after":
                    filterDate = LocalDateTime.parse(f.right, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    table.removeIf(row -> {
                        String rowDateString = row.getColumnValue(header.indexOf(f.left));
                        LocalDateTime rowDate = LocalDateTime.parse(rowDateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        return rowDate.isBefore(filterDate);
                    });
                    break;
            }
        }
        // Deleting fields not present in fields
        if (!fields.isEmpty()) {
            List<String> fieldsToDelete = tds.getHeaderCopy();
            fieldsToDelete.removeAll(fields);
            for (String field : fieldsToDelete) {
                table.forEach(row -> row.removeValue(header.indexOf(field)));
                header.remove(field);
            }
        }
        return new ResultSet(header, table);
    }

    /**
     * Gets the number of filters.
     * <p>
     * For testing purposes.
     *
     * @return the number of filters
     */
    public int getNumFilters() {
        return filters.size();
    }

    /**
     * Gets the number of fields.
     * <p>
     * For testing purposes.
     *
     * @return the number of fields
     */
    public int getNumFields() {
        return fields.size();
    }

}
