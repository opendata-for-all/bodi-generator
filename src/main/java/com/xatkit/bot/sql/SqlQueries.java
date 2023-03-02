package com.xatkit.bot.sql;

import com.google.common.collect.Streams;
import com.xatkit.bot.customQuery.FrequentValueInField;
import com.xatkit.bot.customQuery.RowCount;
import com.xatkit.bot.customQuery.SelectFieldsWithConditions;
import com.xatkit.bot.customQuery.ShowFieldDistinct;
import com.xatkit.bot.customQuery.Value1vsValue2;
import com.xatkit.bot.customQuery.ValueFrequency;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.xatkit.bot.customQuery.AbstractCustomQuery.DATETIME;
import static com.xatkit.bot.customQuery.AbstractCustomQuery.DECIMAL;
import static com.xatkit.bot.customQuery.SelectFieldsWithConditions.MIN;

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

    private static final String isoDatetimeFormat = "yyyy-MM-dd''T''HH:mm:ssZ";

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

    private static String toDateTime(String field) {
        // TODO: The format depends on the dataset. We need to know it
        return "TO_TIMESTAMP(" + field + ", '" + isoDatetimeFormat + "')";
    }

    private static String cast(String field, String dataType) {
        switch (dataType) {
            case DECIMAL:
                return toDecimal(field);
            case DATETIME:
                return toDateTime(field);
            default:
                return null;
        }
    }

    /**
     * Replaces all special characters of a text by the underscore character (i.e. {@code _}).
     * <p>
     * Special characters are all characters but alphanumeric ones.
     * <p>
     * This is necessary to preprocess field names since some databases do not support special characters in column
     * names.
     * @param text the text
     * @return the processed text
     */
    private static String replaceSpecialChars(String text) {
        String result = text.replaceAll("[^a-zA-Z0-9]", "_");
        if (result.charAt(0) == '_') {
            result = "col" + result;
        }
        return result;
    }

    /**
     * Escapes all quotes in a text by duplicating them.
     * <p>
     * This is necessary since values of sql statement conditions must escape them.
     * @param text text
     * @return the processed text
     */
    private static String escapeQuotes(String text) {
        return text.replaceAll("'", "''");
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
            sqlFilters.add(toSqlCondition(f.left, f.middle, f.right));
        }
        return sqlFilters;
    }

    /**
     * Given a field, an operator and a value, returns the string that represents the SQL condition with that
     * parameters.
     *
     * @param field    the field
     * @param operator the operator
     * @param value    the value
     * @return the string
     */
    public String toSqlCondition(String field, String operator, String value) {
        field = replaceSpecialChars(field);
        value = escapeQuotes(value);
        switch (operator) {
            // Numeric Filters
            case "=":
            case "<":
            case "<=":
            case ">":
            case ">=":
                return field + " <> ''" + " AND " + toDecimal(field) + " " + operator + " " + value ;
            case "!=":
                return field + " <> ''" + " AND " + toDecimal(field) + " <> " + value;
            // Textual Filters
            case "equals":
                return "UPPER(" + field + ") = UPPER('" + value + "')";
            case "different":
                return "UPPER(" + field + ") <> UPPER('" + value + "')";
            case "contains":
                return "UPPER(" + field + ") LIKE UPPER('%" + value + "%')";
            case "starts with":
                return "UPPER(" + field + ") LIKE UPPER('" + value + "%')";
            case "ends with":
                return "UPPER(" + field + ") LIKE UPPER('%" + value + "')";
            // Datetime Filters
            case "date_equals":
                return field + " <> ''" + " AND " + toDateTime(field) + " = " + toDateTime("'" + value + "'");
            case "date_different":
                return field + " <> ''" + " AND " + toDateTime(field) + " <> " + toDateTime("'" + value + "'");
            case "before":
                return field + " <> ''" + " AND " + toDateTime(field) + " < " + toDateTime("'" + value + "'");
            case "after":
                return field + " <> ''" + " AND " + toDateTime(field) + " > " + toDateTime("'" + value + "'");
            default:
                return null;
        }
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
     * Generates a SQL query for the {@link ShowFieldDistinct} workflow.
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
     * Generates a SQL query for the {@link FrequentValueInField} workflow.
     *
     * @param field        the field
     * @param mostFrequent indicates weather to get the highest (if true) or the lowest (if false) frequency
     * @return the sql query
     */
    public String frequentValueInField(String field, boolean mostFrequent) {
        String fieldClean = replaceSpecialChars(field);
        String operator = (mostFrequent ? "max" : "min");
        String sqlQuery =
            "SELECT " + fieldClean + " AS `" + field + "`, COUNT(" + fieldClean + ") AS freq FROM " + table;
        if (!filters.isEmpty()) {
            sqlQuery += " WHERE " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        sqlQuery += " GROUP BY `" + field + "` HAVING freq = ("
            + "SELECT " + operator + "(freq2) FROM ("
            + "SELECT " + fieldClean + " AS `" + field + "`, COUNT(" + fieldClean + ") AS freq2 FROM " + table
            + " GROUP BY `" + field + "`))";
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link Value1vsValue2} workflow.
     * <p>
     *
     * @param field1 the first field
     * @param value1 the value of the first field
     * @param field2 the second field
     * @param value2 the value of the second field
     * @return the sql query
     */
    public String value1VSValue2(String field1, String value1, String field2, String value2) {
        String field1Clean = replaceSpecialChars(field1);
        String field2Clean = replaceSpecialChars(field2);

        String sqlQuery1 = "SELECT 1 AS id, " + field1Clean + " AS `" + field1 + "`, COUNT(" + field1Clean + ") AS freq"
                + " FROM " + table + " WHERE " + field1Clean + " = '" + escapeQuotes(value1) + "'";

        String sqlQuery2 = "SELECT 1 AS id, " + field2Clean + " AS `" + field2 + "`, COUNT(" + field2Clean + ") AS freq"
                + " FROM " + table + " WHERE " + field2Clean + " = '" + escapeQuotes(value2) + "'";
        if (!filters.isEmpty()) {
            sqlQuery1 += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
            sqlQuery2 += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        sqlQuery1 += " GROUP BY `" + field1 + "`";
        sqlQuery2 += " GROUP BY `" + field2 + "`";

        return  "SELECT * FROM (" + sqlQuery1 + ") a FULL OUTER JOIN (" + sqlQuery2 + ") b ON a.id = b.id";
    }

    /**
     * Generates a SQL query for the {@link ValueFrequency} workflow.
     *
     * @param field the field of the 'where' condition
     * @param value the value of the 'where' condition
     * @return the sql query
     */
    public String valueFrequency(String field, String value) {
        String fieldClean = replaceSpecialChars(field);
        String sqlQuery = "SELECT COUNT(" + fieldClean + ") AS freq FROM " + table + " WHERE " + fieldClean + " = '" + escapeQuotes(value) + "'";
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link RowCount} workflow.
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
     * Generates a SQL query for the {@link SelectFieldsWithConditions} workflow
     * (max/min operators).
     *
     * @param selectFields  the select fields
     * @param opField       the field to which the operator is applied
     * @param operator      the operator to apply to opField
     * @param dataType      the data type of opField
     * @param valueFieldMap the value field map storing the WHERE conditions (e.g. FIELD = 'value')
     * @param number        the number of rows to select
     * @return the sql query
     */
    public String selectFieldsWithConditionsMaxMinOperator(List<String> selectFields, String opField, String operator, String dataType, Map<String, String> valueFieldMap, String number) {
        List<String> selectFieldsClean =
                selectFields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String selectFieldsString = String.join(", ", Streams.zip(selectFieldsClean.stream(), selectFields.stream(),
                (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));
        if (!selectFieldsString.isEmpty()) {
            selectFieldsString += ", ";
        }
        String opFieldClean = replaceSpecialChars(opField);
        Map<String, String> fieldValueMapClean = new HashMap<>();
        for (Map.Entry<String, String> entry : valueFieldMap.entrySet()) {
            fieldValueMapClean.put(escapeQuotes(entry.getKey()), replaceSpecialChars(entry.getValue()));
        }
        String fieldsValuesString = String.join(" AND ", Streams.zip(fieldValueMapClean.keySet().stream(), fieldValueMapClean.values().stream(),
                (v, f) -> f + " = '" + v + "'").collect(Collectors.toList()));
        if (!fieldsValuesString.isEmpty()) {
            fieldsValuesString = " AND " + fieldsValuesString;
        }
        String order = (operator.equals(MIN) ? "ASC" : "DESC");

        String sqlQuery = "SELECT " + selectFieldsString + cast(opFieldClean, dataType) + " AS `"
                + opField + "` FROM " + table + " WHERE " + opFieldClean + " <> '' " + fieldsValuesString;
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        sqlQuery += "  ORDER BY `" + opField + "` " + order + " LIMIT " + number;
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link SelectFieldsWithConditions} workflow
     * (avg/sum operators).
     *
     * @param opField       the field to which the operator is applied
     * @param operator      the operator to apply to opField
     * @param valueFieldMap the value field map storing the WHERE conditions (e.g. FIELD = 'value')
     * @return the sql query
     */
    public String selectFieldsWithConditionsAvgSumOperator(String opField, String operator, Map<String, String> valueFieldMap) {
        String opFieldClean = replaceSpecialChars(opField);
        Map<String, String> fieldValueMapClean = new HashMap<>();
        for (Map.Entry<String, String> entry : valueFieldMap.entrySet()) {
            fieldValueMapClean.put(escapeQuotes(entry.getKey()), replaceSpecialChars(entry.getValue()));
        }
        String fieldsValuesString = String.join(" AND ", Streams.zip(fieldValueMapClean.keySet().stream(), fieldValueMapClean.values().stream(),
                (v, f) -> f + " = '" + v + "'").collect(Collectors.toList()));
        if (!fieldsValuesString.isEmpty()) {
            fieldsValuesString = " AND " + fieldsValuesString;
        }

        String sqlQuery = "SELECT " + operator + "(" + toDecimal(opFieldClean) + ") AS `"
                + opField + "` FROM " + table + " WHERE " + opFieldClean + " <> '' " + fieldsValuesString;
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }


    /**
     * Generates a SQL query for the {@link SelectFieldsWithConditions} workflow
     * (no operator).
     *
     * @param selectFields  the select fields
     * @param valueFieldMap the value field map storing the WHERE conditions (e.g. FIELD = 'value')
     * @param isDistinct    the whether the selection should be distinct (true) or not (false)
     * @return the sql query
     */
    public String selectFieldsWithConditionsNoOperator(List<String> selectFields, Map<String, String> valueFieldMap, boolean isDistinct) {
        List<String> selectFieldsClean =
                selectFields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String selectFieldsString = String.join(", ", Streams.zip(selectFieldsClean.stream(), selectFields.stream(),
                (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));
        Map<String, String> fieldValueMapClean = new HashMap<>();
        for (Map.Entry<String, String> entry : valueFieldMap.entrySet()) {
            fieldValueMapClean.put(escapeQuotes(entry.getKey()), replaceSpecialChars(entry.getValue()));
        }
        String fieldsValuesString = String.join(" AND ", Streams.zip(fieldValueMapClean.keySet().stream(), fieldValueMapClean.values().stream(),
                (v, f) -> f + " = '" + v + "'").collect(Collectors.toList()));
        if (fieldsValuesString.isEmpty()) {
            fieldsValuesString = "TRUE";
        }
        String distinct = (isDistinct ? "DISTINCT " : "");
        String sqlQuery = "SELECT " + distinct + selectFieldsString + " FROM " + table
                + " WHERE " + fieldsValuesString;
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.FieldOperatorValue} workflow.
     *
     * @param selectFields the select fields
     * @param field        the field
     * @param operator     the operator
     * @param value        the value
     * @return the string
     */
    public String fieldOperatorValue(List<String> selectFields, String field, String operator, String value) {
        if (!selectFields.contains(field)) {
            selectFields.add(field);
        }
        List<String> selectFieldsClean =
                selectFields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String selectFieldsString = String.join(", ", Streams.zip(selectFieldsClean.stream(), selectFields.stream(),
                (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));

        String sqlQuery = "SELECT " + selectFieldsString + " FROM " + table
                + " WHERE " + toSqlCondition(field, operator, escapeQuotes(value));
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }

    /**
     * Generates a SQL query for the {@link com.xatkit.bot.customQuery.FieldBetweenValues} workflow.
     *
     * @param selectFields the select fields
     * @param field        the field to apply the between operator
     * @param value1       the first value of the interval
     * @param value2       the second value of the interval
     * @param dataType     the data type of the field
     * @return the string
     */
    public String fieldBetweenValues(List<String> selectFields, String field, String value1, String value2, String dataType) {
        if (!selectFields.contains(field)) {
            selectFields.add(field);
        }
        List<String> selectFieldsClean =
                selectFields.stream().map(SqlQueries::replaceSpecialChars).collect(Collectors.toList());
        String selectFieldsString = String.join(", ", Streams.zip(selectFieldsClean.stream(), selectFields.stream(),
                (fClean, f) -> fClean + " AS `" + f + "`").collect(Collectors.toList()));

        String fieldClean = replaceSpecialChars(field);
        String castedField = null;
        String castedValue1 = null;
        String castedValue2 = null;
        if (dataType.equals(DATETIME)) {
            castedField = toDateTime(fieldClean);
            castedValue1 = toDateTime("'" + escapeQuotes(value1) + "'");
            castedValue2 = toDateTime("'" + escapeQuotes(value2) + "'");
        } else if (dataType.equals(DECIMAL)) {
            castedField = toDecimal(fieldClean);
            castedValue1 = toDecimal(escapeQuotes(value1));
            castedValue2 = toDecimal(escapeQuotes(value2));
        }

        String sqlQuery = "SELECT " + selectFieldsString + " FROM " + table
                + " WHERE " + fieldClean + " <> '' AND "
                + castedField + " BETWEEN " + castedValue1 + " AND " + castedValue2;
        if (!filters.isEmpty()) {
            sqlQuery += " AND " + String.join(" AND ", getFiltersAsSqlConditions());
        }
        return sqlQuery;
    }
}
