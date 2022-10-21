package com.xatkit.bot.sql;

import com.google.common.collect.Streams;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        return "CAST(" + field + " AS DECIMAL(38," + SCALE + "))";
    }

    /**
     * Replaces all special characters of a text by the underscore character (i.e. {@code _}).
     * <p>
     * Special characters are all characters but alphanumeric ones.
     * <p>
     * This is necessary to preprocess field names since some databases do not support special characters in column
     * names.
     * @param text
     * @return
     */
    private static String replaceSpecialChars(String text) {
        String result = text.replaceAll("[^a-zA-Z0-9]", "_");
        if (result.charAt(0) == '_') {
            result = "col" + result;
        }
        return result;
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
     * Gets the collection of filters as Strings.
     * <p>
     * The field names are replaced by readable names stored in a {@link Map}.
     *
     * @param readableNames the readable names of the fields
     *
     * @return the fields as strings
     * @see com.xatkit.bot.library.Entities#readableNames
     */
    public List<String> getFiltersAsStrings(Map<String, String> readableNames) {
        List<String> stringFilters = new ArrayList<>();
        for (ImmutableTriple<String, String, String> f : filters) {
            stringFilters.add(readableNames.get(f.left) + " " + f.middle + " " + f.right);
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
    public Set<String> getFiltersAsSqlConditions() {
        Set<String> sqlFilters = new HashSet<>();
        for (ImmutableTriple<String, String, String> f : filters) {
            String field = replaceSpecialChars(f.left);
            switch (f.middle) {
                // Numeric Filters
                case "=":
                case "<":
                case "<=":
                case ">":
                case ">=":
                    sqlFilters.add(toDecimal(field) + " " + f.middle + " " + f.right);
                    sqlFilters.add(field + " <> ''");
                    break;
                case "!=":
                    sqlFilters.add(toDecimal(field) + " <> " + f.right);
                    sqlFilters.add(field + " <> ''");
                    break;
                    // Textual Filters
                case "equals": // Also a date filter
                    sqlFilters.add("UPPER(" + field + ") = UPPER('" + f.right + "')");
                    break;
                case "different": // Also a date filter
                    sqlFilters.add("UPPER(" + field + ") <> UPPER('" + f.right + "')");
                    break;
                case "contains":
                    sqlFilters.add("UPPER(" + field + ") LIKE UPPER('%" + f.right + "%')");
                    break;
                case "starts with":
                    sqlFilters.add("UPPER(" + field + ") LIKE UPPER('" + f.right + "%')");
                    break;
                case "ends with":
                    sqlFilters.add("UPPER(" + field + ") LIKE UPPER('%" + f.right + "')");
                    break;
                // Date Filters
                case "before":
                    sqlFilters.add("date(" + field + ") < date('" + f.right + "')");
                    break;
                case "after":
                    sqlFilters.add("date(" + field + ") > date('" + f.right + "')");
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
        List<String> fieldsClean = allFields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String fieldsString = String.join(", ", Streams.zip(fieldsClean.stream(), allFields.stream(), (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));
        String sqlQuery = "SELECT " + fieldsString + " FROM " + table;
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
        String fieldClean = replaceSpecialChars(field);
        String sqlQuery = "SELECT DISTINCT " + fieldClean + " AS `" + field + "` FROM " + table;
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
        String fieldClean = replaceSpecialChars(field);
        String order = (mostFrequent ? "DESC" : "ASC");
        String sqlQuery = "SELECT " + fieldClean + " AS `" + field + "`, COUNT(" + fieldClean + ") AS freq FROM " + table;
        if (!filters.isEmpty()) {
            sqlQuery += " WHERE " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        sqlQuery += " GROUP BY `" + field + "` ORDER BY freq " + order + " LIMIT 1";
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
        String fieldClean = replaceSpecialChars(field);
        String sqlQuery = "SELECT " + fieldClean  + " AS `" + field + "`, COUNT(" + fieldClean + ") AS freq FROM " + table;
        if (!filters.isEmpty()) {
            sqlQuery += " WHERE " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        sqlQuery += " GROUP BY `" + field + "` HAVING freq = " + frequency;
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomValueFrequency} workflow.
     * <p>
     * It is also used in {@link com.xatkit.bot.customQuery.CustomValue1vsValue2}
     *
     * @param field the field of the 'where' condition
     * @param value the value of the 'where' condition
     * @return the sql query
     */
    public String valueFrequency(String field, String value) {
        String fieldClean = replaceSpecialChars(field);
        String sqlQuery = "SELECT COUNT(" + fieldClean + ") AS freq FROM " + table + " WHERE " + fieldClean + " = '" + value + "'";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    private String numericFieldFunction(String field, String operator) {
        return numericFieldFunction(field, operator, new HashMap<>());
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomNumericFieldFunction} workflow.
     *
     * @param field         the field
     * @param operator      the operator
     * @param valueFieldMap the field-value conditions
     * @return the sql query
     */
    public String numericFieldFunction(String field, String operator, Map<String, String> valueFieldMap) {
        String fieldClean = replaceSpecialChars(field);
        Map<String, String> fieldValueMapClean = new HashMap<>();
        for (Map.Entry<String, String> entry : valueFieldMap.entrySet()) {
            fieldValueMapClean.put(entry.getKey(), replaceSpecialChars(entry.getValue()));
        }
        String fieldsValuesString = String.join(" AND ", Streams.zip(fieldValueMapClean.keySet().stream(), fieldValueMapClean.values().stream(),
                (v, f) -> f + " = '" + v + "'").collect(Collectors.toList()));
        String sqlQuery = "SELECT " + operator + "(" + toDecimal(fieldClean) + ") AS `" + field + "` FROM "
                + table + " WHERE " + fieldClean + " <> ''";
        if (!fieldsValuesString.isEmpty()) {
            sqlQuery += " AND " + fieldsValuesString;
        }
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
        List<String> fieldsClean = fields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String fieldsString = String.join(", ", Streams.zip(fieldsClean.stream(), fields.stream(), (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));
        String fieldClean = replaceSpecialChars(field);
        String sqlQuery = "SELECT DISTINCT " + fieldsString + " FROM " + table + " WHERE " + fieldClean + " <> '' AND "
                + toDecimal(fieldClean) + " = (" + numericFieldFunction(field, operator) + ")";
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
     * @param keyFields     the key fields to be selected
     * @param targetField   the target field to be selected
     * @param valueFieldMap the field-value conditions
     * @param isDistinct    indicates weather to select distinct (if true) or not (if false)
     * @return the sql query
     */
    public String fieldOfValue(List<String> keyFields, String targetField, Map<String, String> valueFieldMap,
                               boolean isDistinct) {
        String distinct = (isDistinct ? "DISTINCT " : "");
        List<String> fields = (isDistinct ? new ArrayList<>() : new ArrayList<>(keyFields));
        if (!fields.contains(targetField)) {
            fields.add(targetField);
        }
        List<String> fieldsClean = fields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String fieldsString = String.join(", ", Streams.zip(fieldsClean.stream(), fields.stream(), (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));

        Map<String, String> fieldValueMapClean = new HashMap<>();
        for (Map.Entry<String, String> entry : valueFieldMap.entrySet()) {
            fieldValueMapClean.put(entry.getKey(), replaceSpecialChars(entry.getValue()));
        }
        String fieldsValuesString = String.join(" AND ", Streams.zip(fieldValueMapClean.keySet().stream(), fieldValueMapClean.values().stream(),
                (v, f) -> f + " = '" + v + "'").collect(Collectors.toList()));
        String sqlQuery = "SELECT " + distinct + fieldsString + " FROM " + table + " WHERE " + fieldsValuesString;
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return  sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomFieldOfValue} workflow (with operator).
     *
     * @param keyFields     the key fields to be selected
     * @param targetField   the target field to be selected
     * @param valueFieldMap the field-value conditions
     * @param operator      the operator
     * @return the sql query
     */
    public String fieldOfValueOperator(List<String> keyFields, String targetField, Map<String, String> valueFieldMap,
                                       String operator) {
        List<String> fields = (operator.equals("min") || operator.equals("max") ? new ArrayList<>(keyFields) : new ArrayList<>());
        fields.remove(targetField);
        List<String> fieldsClean = fields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String fieldsString = String.join(", ", Streams.zip(fieldsClean.stream(), fields.stream(), (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));
        if (!fieldsString.isEmpty()) {
            fieldsString += ", ";
        }
        String targetFieldClean = replaceSpecialChars(targetField);
        Map<String, String> fieldValueMapClean = new HashMap<>();
        for (Map.Entry<String, String> entry : valueFieldMap.entrySet()) {
            fieldValueMapClean.put(entry.getKey(), replaceSpecialChars(entry.getValue()));
        }
        String fieldsValuesString = String.join(" AND ", Streams.zip(fieldValueMapClean.keySet().stream(), fieldValueMapClean.values().stream(),
                (v, f) -> f + " = '" + v + "'").collect(Collectors.toList()));
        String sqlQuery = "SELECT " + fieldsString + operator + "(" + toDecimal(targetFieldClean) + ") AS `"
                + targetField + "` FROM " + table + " WHERE " + targetFieldClean + " <> '' AND " + fieldsValuesString;
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        if (!fieldsString.isEmpty()) {
            String order = (operator.equals("min") ? "ASC" : "DESC");
            String fieldsStringGroupBy = "`" + String.join("`, `", fields) + "`";
            sqlQuery += " GROUP BY " + fieldsStringGroupBy + " ORDER BY `" + targetField + "` " + order + " LIMIT 1";
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
        List<String> fieldsClean = fields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String fieldsString = String.join(", ", Streams.zip(fieldsClean.stream(), fields.stream(), (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));
        String fieldClean = replaceSpecialChars(field);
        String sqlQuery = "SELECT DISTINCT " + fieldsString + " FROM " + table + " WHERE " + fieldClean + " = '" + value + "'";
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
        List<String> fieldsClean = fields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String fieldsString = String.join(", ", Streams.zip(fieldsClean.stream(), fields.stream(), (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));
        String field1Clean = replaceSpecialChars(field1);
        String field2Clean = replaceSpecialChars(field2);
        String sqlQuery = "SELECT DISTINCT " + fieldsString + " FROM " + table + " WHERE " + field1Clean + " = '" + value1
            + "' AND " + field2Clean + " = '" + value2 + "'";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.CustomFieldOfNumericFieldFunction} workflow.
     *
     * @param keyFields    the key fields to be selected
     * @param field1       the target field to be selected
     * @param numericField the numeric field where the operator is applied
     * @param operator     the operator
     * @param number       the number of rows to get
     * @return the sql query
     */
    public String fieldOfNumericFieldFunction(List<String> keyFields, String field1, String numericField,
                                              String operator, String number) {
        List<String> fields = new ArrayList<>(keyFields);
        if (!fields.contains(field1)) {
            fields.add(field1);
        }
        fields.remove(numericField);
        List<String> fieldsClean = fields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String fieldsString = String.join(", ", Streams.zip(fieldsClean.stream(), fields.stream(), (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));
        String fieldsStringGroupBy = "`" + String.join("`, `", fields) + "`";
        String order = (operator.equals("min") ? "ASC" : "DESC");
        String numericFieldClean = replaceSpecialChars(numericField);
        String sqlQuery = "SELECT " + fieldsString + ", "
                + operator + "(" + toDecimal(numericFieldClean) + ") AS `" + numericField + "` FROM " + table
                + " WHERE " + numericFieldClean + " <> ''";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        sqlQuery += " GROUP BY " + fieldsStringGroupBy + " ORDER BY `" + numericField + "` " + order + " LIMIT " + number;
        return sqlQuery;
    }
}
