package bodi.generator.dataSource;

import com.xatkit.bot.Bot;
import com.xatkit.bot.library.Utils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Constant value for the empty cells of a {@link TabularDataSource}.
     */
    private final String NULL_CELL = "<NULL>";

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

    /**
     * Gets {@link #ignoreCaseFilterValue}.
     *
     * @return the {@link #ignoreCaseFilterValue} value
     */
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

    /**
     * Remove a filter in {@link #filters}.
     *
     * @param field    the filter's field
     * @param operator the filter's operator
     * @param value    the filter's value
     * @return the statement
     */
    public Statement removeFilter(String field, String operator, String value) {
        filters.remove(new ImmutableTriple<>(field, operator, value));
        return this;
    }

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
     * Remove a field in {@link #fields}.
     *
     * @param field the field
     * @return the statement
     */
    public Statement removeField(String field) {
        fields.remove(field);
        return this;
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
     * Applies all the filters in {@link #filters} to {@code header} and {@code table}.
     *
     * @param header the header of a result set
     * @param table  the table of a result set
     */
    private void applyFilters(List<String> header, List<Row> table) {
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
                case "after":
                    table.removeIf(row -> {
                        String rowDate = row.getColumnValue(header.indexOf(f.left));
                        if (isEmpty(rowDate)) {
                            return true;
                        }
                        for (String dateFormat : Utils.dateFormats) {
                            try {
                                SimpleDateFormat format1 = new SimpleDateFormat(dateFormat);
                                // DialogFlow always returns dates with this format
                                SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                                Date date1 = format1.parse(rowDate);
                                Date date2 = format2.parse(f.right);
                                if (f.middle.equals("before")) {
                                    return !date1.before(date2);
                                } else {
                                    return !date1.after(date2);
                                }
                            } catch (Exception ignored) { }
                        }
                        return true;
                    });
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Deletes all fields not present in {@link #fields}, in {@code header} and {@code table}.
     *
     * @param header the header of a result set
     * @param table  the table of a result set
     */
    private void deleteFields(List<String> header, List<Row> table) {
        if (!fields.isEmpty()) {
            List<String> fieldsToDelete = tds.getHeaderCopy();
            fieldsToDelete.removeAll(fields);
            for (String field : fieldsToDelete) {
                table.forEach(row -> row.removeValue(header.indexOf(field)));
                header.remove(field);
            }
        }
    }

    /**
     * Executes the query with the current {@link #filters}, {@link #fields} and the {@link #ignoreCaseFilterValue}
     * condition.
     * <p>
     * This method allows to run another operation to the generated result
     * set, and return the appropriate object.
     *
     * @param operation the operation
     * @param args      the arguments of the operation (if any)
     * @return          the object containing the result of the query
     */
    public Object executeQuery(Operation operation, String... args) {
        List<String> header = tds.getHeaderCopy();
        List<Row> table = tds.getTableCopy();
        // Filtering
        applyFilters(header, table);

        switch (operation) {
            case NO_OPERATION:
                deleteFields(header, table);
                return new ResultSet(header, table);
            case SHOW_FIELD_DISTINCT:
                showFieldDistinct(header, table, args[0]);
                return new ResultSet(header, table);
            case FREQUENT_VALUE_IN_FIELD:
                return frequentValueInField(header, table, args[0], args[1]);
            default:
                break;
        }
        return null;
    }

    /**
     * Updates a header and a table, removing all the fields (columns) except the one passed as an argument.
     *
     * @param header the header
     * @param table  the table
     * @param field  the field to preserve in the header and the table
     */
    private void selectField(List<String> header, List<Row> table, String field) {
        List<String> fieldsToDelete = tds.getHeaderCopy();
        fieldsToDelete.remove(field);
        for (String fieldToDelete : fieldsToDelete) {
            table.forEach(row -> row.removeValue(header.indexOf(fieldToDelete)));
            header.remove(fieldToDelete);
        }
    }

    /**
     * Given a field name, gets the frequency of each value of that field.
     *
     * @param header the header
     * @param table  the table
     * @param field  the target field to get the frequencies of its values
     * @return       a {@link Map} containing the value - frequency pairs
     */
    private Map<String, Integer> getFieldFrequencies(List<String> header, List<Row> table, String field) {
        selectField(header, table, field);
        Map<String, Integer> frequenciesTable = new HashMap<>();
        for (Row row : table) {
            String value = row.getColumnValue(0);
            value = (value.equals("") ? NULL_CELL : value);
            int count = frequenciesTable.getOrDefault(value, 0);
            frequenciesTable.put(value, count + 1);
        }
        return frequenciesTable;
    }

    /**
     * Executes the {@link Operation#SHOW_FIELD_DISTINCT} operation.
     *
     * @param header the header of a result set
     * @param table  the table of a result set
     * @param field  the field from which unique values are to be extracted
     *
     * @see Operation#SHOW_FIELD_DISTINCT
     */
    private void showFieldDistinct(List<String> header, List<Row> table, String field) {
        selectField(header, table, field);
        Set<String> fieldValues = new HashSet<>();
        table.removeIf(row -> !fieldValues.add(row.getColumnValue(0)));
    }

    /**
     * Executes the {@link Operation#FREQUENT_VALUE_IN_FIELD} operation.
     *
     * @param header            the header of a result set
     * @param table             the table of a result set
     * @param field             the field from which the most or least frequent values are to be gotten
     * @param frequencyOperator the kind of frequency that wants to be obtained: {@code most} for the most frequent
     *                          values, and {@code least} for the least frequent values
     *
     * @see Operation#FREQUENT_VALUE_IN_FIELD
     */
    private Pair<Set<String>, Integer> frequentValueInField(List<String> header, List<Row> table, String field,
                                                            String frequencyOperator) {
        if (!table.isEmpty()) {
            Map<String, Integer> frequenciesTable = getFieldFrequencies(header, table, field);
            Map.Entry<String, Integer> firstEntry = frequenciesTable.entrySet().iterator().next();
            Set<String> frequentValues = new HashSet<>(Collections.singleton(firstEntry.getKey()));
            int frequency = firstEntry.getValue();
            for (Map.Entry<String, Integer> entry : frequenciesTable.entrySet()) {
                if (frequencyOperator.equals("most") && entry.getValue() > frequency
                        || frequencyOperator.equals("least") && entry.getValue() < frequency) {
                    frequentValues = new HashSet<>(Collections.singleton(entry.getKey()));
                    frequency = entry.getValue();
                } else if (entry.getValue() == frequency) {
                    frequentValues.add(entry.getKey());
                }
            }
            return new MutablePair<>(frequentValues, frequency);
        } else {
            return null;
        }
    }

    /**
     * Gets the collection of filters as Strings.
     *
     * @return the fields as strings
     * @see #fields
     */
    public List<String> getFiltersAsStrings() {
        List<String> stringFilters = new ArrayList<>();
        for (ImmutableTriple<String, String, String> f : filters) {
            stringFilters.add(f.left + " " + f.middle + " " + f.right);
        }
        return stringFilters;
    }

    /**
     * Gets the collection of filters as SQL conditions.
     * <p>
     * This is useful to add the statement filters as conditions in the WHERE clause of a SQL statement.
     *
     * @return the filters as sql conditions
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
