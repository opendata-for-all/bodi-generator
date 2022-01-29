package bodi.generator.dataSource;

import com.xatkit.bot.Bot;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The object used for querying a {@link TabularDataSource} and returning the result. It contains information about
 * what to filter and query.
 * <p>
 * A {@link Statement} is related to a single {@link TabularDataSource} object.
 */
public class Statement {

    /**
     * The {@link TabularDataSource} object bound to this {@link Statement}.
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
     * Instantiates a new Statement bound to a given {@link TabularDataSource}.
     *
     * @param tds the {@link TabularDataSource}
     */
    public Statement(TabularDataSource tds) {
        this.tds = tds;
        this.filters = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.ignoreCaseFilterValue = false;
    }

    public boolean isIgnoreCaseFilterValue() {
        return ignoreCaseFilterValue;
    }

    /**
     * Sets {@code #ignoreCaseFilterValue}.
     *
     * @param ignoreCaseFilterValue the value
     * @return the statement
     */
    public Statement setIgnoreCaseFilterValue(boolean ignoreCaseFilterValue) {
        this.ignoreCaseFilterValue = ignoreCaseFilterValue;
        return this;
    }

    /**
     * Gets the {@link TabularDataSource} bound to the {@link Statement}.
     *
     * @return the tabular data source
     */
    public TabularDataSource getTabularDataSource() {
        return tds;
    }

    /**
     * Add a new filter to {@link #filters}.
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
        List<String> header = tds.getHeaderCopy();
        List<Row> table = tds.getTableCopy();
        // Filtering
        for (ImmutableTriple<String, String, String> f : filters) {
            String value;
            if (ignoreCaseFilterValue) {
                value = f.right.toLowerCase();
            } else {
                value = f.right;
            }
            switch (f.middle) {
                // Numeric Filters
                case "=":
                    table.removeIf(row ->
                        isEmpty(row.getColumnValue(header.indexOf(f.left))) ||
                        !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) == Float.parseFloat(f.right))
                    );
                    break;
                case "<":
                    table.removeIf(row ->
                        isEmpty(row.getColumnValue(header.indexOf(f.left))) ||
                        !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) < Float.parseFloat(f.right))
                    );
                    break;
                case "<=":
                    table.removeIf(row ->
                        isEmpty(row.getColumnValue(header.indexOf(f.left))) ||
                        !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) <= Float.parseFloat(f.right))
                    );
                    break;
                case ">":
                    table.removeIf(row ->
                        isEmpty(row.getColumnValue(header.indexOf(f.left))) ||
                        !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) > Float.parseFloat(f.right))
                    );
                    break;
                case ">=":
                    table.removeIf(row ->
                        isEmpty(row.getColumnValue(header.indexOf(f.left))) ||
                        !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) >= Float.parseFloat(f.right))
                    );
                    break;
                case "!=":
                    table.removeIf(row ->
                        isEmpty(row.getColumnValue(header.indexOf(f.left))) ||
                        !(Float.parseFloat(row.getColumnValue(header.indexOf(f.left))) != Float.parseFloat(f.right))
                    );
                    break;
                // Textual Filters
                case "equals": // Also a date filter
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue)
                            .equals(value)));
                    break;
                case "different": // Also a date filter
                    table.removeIf(row -> row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue)
                            .equals(value));
                    break;
                case "contains":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue)
                            .contains(value)));
                    break;
                case "starts with":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue)
                            .startsWith(value)));
                    break;
                case "ends with":
                    table.removeIf(row -> !(row.getColumnValue(header.indexOf(f.left), ignoreCaseFilterValue)
                            .endsWith(value)));
                    break;
                // Date Filters
                case "before":
                    LocalDateTime filterDate = LocalDateTime.parse(f.right, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    table.removeIf(row -> {
                        String rowDateString = row.getColumnValue(header.indexOf(f.left));
                        if (isEmpty(rowDateString)) {
                            return true;
                        }
                        try {
                            LocalDateTime rowDate = LocalDateTime.parse(rowDateString,
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            return rowDate.isAfter(filterDate);
                        } catch (DateTimeParseException ignored) { }
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.ROOT);
                            Date date = format.parse(rowDateString);
                            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                            String newDate = format.format(date);
                            LocalDateTime rowDate = LocalDateTime.parse(newDate,
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            return rowDate.isAfter(filterDate);
                        } catch (Exception ignored) { }
                        return true;
                    });
                    break;
                case "after":
                    filterDate = LocalDateTime.parse(f.right, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    table.removeIf(row -> {
                        String rowDateString = row.getColumnValue(header.indexOf(f.left));
                        if (isEmpty(rowDateString)) {
                            return true;
                        }
                        try {
                            LocalDateTime rowDate = LocalDateTime.parse(rowDateString,
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            return rowDate.isBefore(filterDate);
                        } catch (DateTimeParseException ignored) { }
                        try { // 31/01/2021 12:00:00 AM
                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.ROOT);
                            Date date = format.parse(rowDateString);
                            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                            String newDate = format.format(date);
                            LocalDateTime rowDate = LocalDateTime.parse(newDate,
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            return rowDate.isBefore(filterDate);
                        } catch (Exception ignored) { }
                        return true;
                    });
                    break;
                default:
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

    /**
     * Gets the collection of filters as SQL conditions.
     * <p>
     * This is useful to add the statement filters as conditions in the WHERE clause of a SQL statement.
     *
     * @return the fields as sql variables
     * @see #fields
     */
    public List<String> getFiltersAsSqlConditions() {
        // The table name is used to refer to a table column in SQL queries (-4 subtracts the file extension)
        String tableName = Bot.inputDoc.substring(0, Bot.inputDoc.length() - 4);
        List<String> sqlFilters = new ArrayList<>();
        for (ImmutableTriple<String, String, String> f : filters) {
            switch (f.middle) {
                // Numeric Filters
                case "=":
                case "<":
                case "<=":
                case ">":
                case ">=":
                case "!=":
                    sqlFilters.add(tableName + "." + f.left + " " + f.middle + " " + f.right);
                    break;
                // Textual Filters
                case "equals": // Also a date filter
                    sqlFilters.add(tableName + "." + f.left + " = \"" + f.right + "\"");
                    break;
                case "different": // Also a date filter
                    sqlFilters.add(tableName + "." + f.left + " != \"" + f.right + "\"");
                    break;
                case "contains":
                    sqlFilters.add(tableName + "." + f.left + " LIKE \"%" + f.right + "%\"");
                    break;
                case "starts with":
                    sqlFilters.add(tableName + "." + f.left + " LIKE \"" + f.right + "%\"");
                    break;
                case "ends with":
                    sqlFilters.add(tableName + "." + f.left + " LIKE \"%" + f.right + "\"");
                    break;
                // Date Filters
                case "before":
                    sqlFilters.add("date(" + tableName + "." + f.left + ") < date(\"" + f.right + "\")");
                    break;
                case "after":
                    sqlFilters.add("date(" + tableName + "." + f.left + ") > date(\"" + f.right + "\")");
                    break;
                default:
                    break;
            }
        }
        return sqlFilters;
    }

    /**
     * Gets the collection of fields as SQL variables.
     * <p>
     * This is useful to add the collection of fields in the SELECT clause of a SQL query.
     *
     * @return the fields as sql variables
     * @see #fields
     */
    public List<String> getFieldsAsSqlVariables() {
        // The table name is used to refer to a table column in SQL queries (-4 subtracts the file extension)
        String tableName = Bot.inputDoc.substring(0, Bot.inputDoc.length() - 4);
        List<String> sqlFields = new ArrayList<>();
        for (String field : fields) {
            sqlFields.add(tableName + "." + field);
        }
        return sqlFields;
    }

}
