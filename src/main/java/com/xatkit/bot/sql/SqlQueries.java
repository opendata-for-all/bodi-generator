package com.xatkit.bot.sql;

import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.ArrayList;
import java.util.List;

/**
 * The SQL queries generator.
 */
public class SqlQueries {

    /**
     * The table name.
     */
    private final String table;

    /**
     * The scale of decimal values (i.e. the number of decimal digits).
     */
    private static final int SCALE = 4;

    /**
     * A set of filters that are intended to be added to the generated SQL queries.
     */
    private final List<ImmutableTriple<String, String, String>> filters;

    /**
     * A list containing all the fields or column names of the {@link #table}. Must be initialized.
     */
    @Getter
    private final List<String> allFields;

    /**
     * Instantiates a new {@link SqlQueries}.
     * @param tableName the name of the table
     * @param delimiter the csv delimiter
     */
    public SqlQueries(String tableName, char delimiter) {
        table = "table(cp.`" + tableName + "`(type => 'text', fieldDelimiter => '" + delimiter
                + "', extractHeader =>" + " true))";
        filters = new ArrayList<>();
        allFields = new ArrayList<>();
    }

    /**
     * Returns the appropriate string to cast a field to the decimal data type.
     * @param field the field to be cast
     * @return the cast statement
     */
    private static String toDecimal(String field) {
        return "CAST(" + field + " as DECIMAL(38," + SCALE + "))";
    }

    /**
     * Add a new filter to {@link #filters}.
     *
     * @param field    the filter's field
     * @param operator the filter's operator
     * @param value    the filter's value
     */
    public void addFilter(String field, String operator, String value) {
        if (!filters.contains(new ImmutableTriple<>(field, operator, value))) {
            filters.add(new ImmutableTriple<>(field, operator, value));
        }
    }

    /**
     * Remove a filter in {@link #filters}.
     *
     * @param field    the filter's field
     * @param operator the filter's operator
     * @param value    the filter's value
     */
    public void removeFilter(String field, String operator, String value) {
        filters.remove(new ImmutableTriple<>(field, operator, value));
    }

    /**
     * Remove all stored filters in {@link #filters}.
     */
    public void clearFilters() {
        filters.clear();
    }

    /**
     * Gets the collection of filters as Strings.
     *
     * @return the fields as strings
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
     */
    public List<String> getFiltersAsSqlConditions() {
        List<String> sqlFilters = new ArrayList<>();
        for (ImmutableTriple<String, String, String> f : filters) {
            switch (f.middle) {
                // Numeric Filters
                case "=":
                case "<":
                case "<=":
                case ">":
                case ">=":
                    sqlFilters.add(toDecimal(f.left) + " " + f.middle + " " + f.right);
                    break;
                case "!=":
                    sqlFilters.add(toDecimal(f.left) + " <> " + f.right);
                    break;
                    // Textual Filters
                case "equals": // Also a date filter
                    sqlFilters.add("UPPER(" + f.left + ") = UPPER('" + f.right + "')");
                    break;
                case "different": // Also a date filter
                    sqlFilters.add("UPPER(" + f.left + ") <> UPPER('" + f.right + "')");
                    break;
                case "contains":
                    sqlFilters.add("UPPER(" + f.left + ") LIKE UPPER('%" + f.right + "%')");
                    break;
                case "starts with":
                    sqlFilters.add("UPPER(" + f.left + ") LIKE UPPER('" + f.right + "%')");
                    break;
                case "ends with":
                    sqlFilters.add("UPPER(" + f.left + ") LIKE UPPER('%" + f.right + "')");
                    break;
                // Date Filters
                case "before":
                    sqlFilters.add("date(" + f.left + ") < date('" + f.right + "')");
                    break;
                case "after":
                    sqlFilters.add("date(" + f.left + ") > date('" + f.right + "')");
                    break;
                default:
                    break;
            }
        }
        return sqlFilters;
    }


    /**
     * Generates a SQL query to select all data.
     *
     * @return the sql query
     */
    public String selectAll() {
        String sqlQuery = "SELECT * FROM " + table;
        if (!filters.isEmpty()) {
            sqlQuery += " WHERE " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomShowFieldDistinct} workflow.
     *
     * @param field the field
     * @return the sql query
     */
    public String showFieldDistinct(String field) {
        String sqlQuery = "SELECT DISTINCT " + field + " FROM " + table;
        if (!filters.isEmpty()) {
            sqlQuery += " WHERE " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomFrequentValueInField} workflow.
     * <p>
     * This is the first step of the workflow. This SQL query only gets the most frequent value (it does not take
     * into account if there are more values with the maximum frequency).
     *
     * @param field        the field
     * @param mostFrequent indicates weather to get the highest (if true) or the lowest (if false) frequency
     * @return the sql query
     */
    public String frequentValueInField(String field, boolean mostFrequent) {
        String order = (mostFrequent ? "DESC" : "ASC");
        String sqlQuery = "SELECT " + field + ", COUNT(" + field + ") AS freq FROM " + table;
        if (!filters.isEmpty()) {
            sqlQuery += " WHERE " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        sqlQuery += " GROUP BY " + field + " ORDER BY freq " + order + " LIMIT 1";
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomFrequentValueInField} workflow.
     * <p>
     * This is the second and final step of the workflow. It gets all the values of a field with a given frequency
     * (which is expected to be the maximum frequency of any value in the field)
     *
     * @param field     the field
     * @param frequency the target frequency
     * @return the sql query
     */
    public String frequentValueInFieldMatch(String field, int frequency) {
        String sqlQuery = "SELECT " + field + ", COUNT(" + field + ") AS freq FROM " + table;
        if (!filters.isEmpty()) {
            sqlQuery += " WHERE " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        sqlQuery += " GROUP BY " + field + " HAVING freq = " + frequency;
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomValueFrequency} workflow.
     *
     * @param field the field of the 'where' condition
     * @param value the value of the 'where' condition
     * @return the sql query
     */
    public String valueFrequency(String field, String value) {
        String sqlQuery = "SELECT COUNT(" + field + ") AS freq FROM " + table + " WHERE " + field + " = '" + value + "'";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomNumericFieldFunction} workflow.
     *
     * @param field    the field
     * @param operator the operator
     * @return the sql query
     */
    public String numericFieldFunction(String field, String operator) {
        String sqlQuery = "SELECT " + operator + "(" + toDecimal(field) + ") as " + operator + "_" + field + " FROM "
                + table + " WHERE " + field + " <> ''";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomRowOfNumericFieldFunction} workflow.
     *
     * @param keyFields the fields to be selected. If empty, select {@link #allFields}
     * @param field     the field
     * @param operator  the operator
     * @return the sql query
     */
    public String rowOfNumericFieldFunction(List<String> keyFields, String field, String operator) {
        List<String> fields = (keyFields.isEmpty() ? new ArrayList<>(allFields) : new ArrayList<>(keyFields));
        if (!fields.contains(field)) {
            fields.add(field);
        }
        String fieldsString = String.join(", ", fields);
        String sqlQuery = "SELECT DISTINCT " + fieldsString + " FROM " + table + " WHERE " + toDecimal(field) + " = ("
            + numericFieldFunction(field, operator) + ")";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomRowCount} workflow.
     *
     * @return the sql query
     */
    public String rowCount() {
        String sqlQuery = "SELECT COUNT(*) AS count FROM " + table;
        if (!filters.isEmpty()) {
            sqlQuery += " WHERE " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return  sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomFieldOfValue} workflow (without operator).
     *
     * @param targetField the field to be selected
     * @param field       the field of the 'where' condition
     * @param value       the value of the 'where' condition
     * @param isDistinct  indicates weather to select distinct (if true) or not (if false)
     * @return the sql query
     */
    public String fieldOfValue(String targetField, String field, String value, boolean isDistinct) {
        String distinct = (isDistinct ? "DISTINCT" : "");
        String sqlQuery = "SELECT " + distinct + " " + targetField + " FROM " + table + " WHERE "
            + field + " = '" + value + "'";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return  sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomFieldOfValue} workflow (with operator).
     *
     * @param targetField the field to be selected
     * @param field1      the field of the 'where' condition
     * @param value1      the value of the 'where' condition
     * @param operator    the operator
     * @return the sql query
     */
    public String fieldOfValueOperator(String targetField, String field1, String value1, String operator) {
        String sqlQuery = "SELECT " + operator + "(" + toDecimal(targetField) + ") as " + operator + "_" + targetField
                + " FROM " + table + " WHERE " + field1 + " = '" + value1 + "'";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomRowOfValues} workflow (with 1 condition).
     *
     * @param keyFields the fields to be selected. If empty, select {@link #allFields}
     * @param field     the field of the 'where' condition
     * @param value     the value of the 'where' condition
     * @return the sql query
     */
    public String rowOfValues1(List<String> keyFields, String field, String value) {
        List<String> fields = (keyFields.isEmpty() ? new ArrayList<>(allFields) : new ArrayList<>(keyFields));
        if (!fields.contains(field)) {
            fields.add(field);
        }
        String fieldsString = String.join(", ", fields);
        String sqlQuery = "SELECT DISTINCT " + fieldsString + " FROM " + table + " WHERE " + field + " = '" + value + "'";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomRowOfValues} workflow (with 2 conditions).
     *
     * @param keyFields the fields to be selected. If empty, select {@link #allFields}
     * @param field1    the first field of the 'where' condition
     * @param value1    the first value of the 'where' condition
     * @param field2    the second field of the 'where' condition
     * @param value2    the second value of the 'where' condition
     * @return the sql query
     */
    public String rowOfValues2(List<String> keyFields, String field1, String value1, String field2, String value2) {
        List<String> fields = (keyFields.isEmpty() ? new ArrayList<>(allFields) : new ArrayList<>(keyFields));
        if (!fields.contains(field1)) {
            fields.add(field1);
        }
        if (!fields.contains(field2)) {
            fields.add(field2);
        }
        String fieldsString = String.join(", ", fields);
        String sqlQuery = "SELECT DISTINCT " + fieldsString + " FROM " + table + " WHERE " + field1 + " = '" + value1
            + "' AND " + field2 + " = '" + value2 + "'";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }
}
