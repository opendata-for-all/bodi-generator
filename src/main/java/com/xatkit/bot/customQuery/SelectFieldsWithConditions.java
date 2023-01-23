package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.google.common.collect.Streams;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Utils;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;
import lombok.val;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xatkit.bot.App.sql;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Select Fields With Conditions workflow of a chatbot.
 * <p>
 * It performs a SELECT SQL query with some optional value equality conditions. Additionally, it allows to apply an
 * operator to a specific field (e.g. max, min, avg, sum).
 * <p>
 * Check {@link #checkParamsOk(StateContext)} to see the allowed queries or combinations of parameters.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class SelectFieldsWithConditions extends AbstractCustomQuery {

    public SelectFieldsWithConditions(Bot bot, State returnState) {
        super(bot, returnState);
    }

    public static final String MAX = "max";
    public static final String MIN = "min";
    public static final String OLDEST = "oldest";
    public static final String NEWEST = "newest";
    public static final String ALL = "all";
    public static final String DISTINCT = "distinct";
    public static final String DECIMAL = "decimal";
    public static final String DATETIME = "datetime";
    public static final String DEFAULT_NUMBER = "1";

    /*
    Auxiliar methods
     */

    /**
     * Gets a {@link Map} where each of its entries contains a given value (the entry key) with its corresponding field
     * (the entry value)
     *
     * @param values the values
     * @return the value field map
     */
    public static Map<String, String> getValueFieldMap(String... values) {
        Map<String, String> valueFieldMap = new HashMap<>();
        for (String value : values) {
            String valueField = Entities.fieldValueMap.get(value);
            if (!isEmpty(value) && !isEmpty(valueField)) {
                valueFieldMap.put(value, valueField);
            }
        }
        return valueFieldMap;
    }

    /**
     * Returns true if the given operator is the {@link #ALL} operator, false otherwise.
     *
     * @param operator the operator
     * @return the boolean
     */
    public static boolean all(String operator) {
        return !isEmpty(operator) && operator.equals(ALL);
    }

    /**
     * Returns true if the given operator is the {@link #DISTINCT} operator, false otherwise.
     *
     * @param operator the operator
     * @return the boolean
     */
    public static boolean distinct(String operator) {
        return !isEmpty(operator) && operator.equals(DISTINCT);
    }

    /**
     * Returns true if the given operator is the {@link #ALL} or the {@link #DISTINCT} operator, false otherwise.
     *
     * @param operator the operator
     * @return the boolean
     */
    public static boolean allDistinct(String operator) {
        return !isEmpty(operator) && (all(operator) || distinct(operator));
    }

    /**
     * Returns true if the given operator is the {@link #MAX} or the {@link #MIN} operator, false otherwise.
     *
     * @param operator the operator
     * @return the boolean
     */
    public static boolean maxMin(String operator) {
        return !isEmpty(operator) && (operator.equals(MAX) || operator.equals(MIN));
    }

    /**
     * Returns true if the given operator is the {@link #OLDEST} or the {@link #NEWEST} operator, false otherwise.
     *
     * @param operator the operator
     * @return the boolean
     */
    public static boolean oldestNewest(String operator) {
        return !isEmpty(operator) && (operator.equals(OLDEST) || operator.equals(NEWEST));
    }

    /**
     * Returns true if the given operator is the {@link #MAX}, {@link #MIN}, {@link #OLDEST} or {@link #NEWEST}
     * operator, false otherwise.
     *
     * @param operator the operator
     * @return the boolean
     */
    public static boolean maxMinOldestNewest(String operator) {
        return !isEmpty(operator) && (maxMin(operator)|| oldestNewest(operator));
    }

    /**
     * Returns the equivalent numeric operator of a datetime operator. Equivalencies are:
     * <p>
     * {@link #OLDEST} => {@link #MIN}
     * <p>
     * {@link #NEWEST} => {@link #MAX}
     *
     * @param operator the operator
     * @return the equivalent operator
     */
    public static String datetimeOperatorToNumericOperator(String operator) {
        if (operator.equals(OLDEST)) {
            return MIN;
        } else if (operator.equals(NEWEST)) {
            return MAX;
        }
        return operator;
    }

    /**
     * Generates a list of fields that are intended to be selected ones in a SELECT SQL query.
     * <p>
     * keyFields, valueFieldMap's values and targetField will make up the resulting list of fields.
     * <p>
     * operatorField will be removed from the resulting list, since an operator is applied to it, and we process it
     * separately.
     * <p>
     * Null parameters are allowed, they will simply be ignored.
     *
     * @param keyFields     the key fields
     * @param valueFieldMap the value field map
     * @param targetField      the tar field
     * @param operatorField the op field
     * @return the select fields
     */
    public static List<String> getSelectFields(List<String> keyFields,Map<String, String> valueFieldMap, String targetField, String operatorField) {
        List<String> selectFields = new ArrayList<>();
        if (keyFields != null) {
            selectFields.addAll(keyFields);
        }
        if (valueFieldMap != null) {
            selectFields.addAll(valueFieldMap.values());
        }
        if (!isEmpty(targetField)) {
            selectFields.add(targetField);
        }
        // remove duplicated fields
        selectFields = new ArrayList<>(new LinkedHashSet<>(selectFields));
        if (!isEmpty(operatorField)) {
            selectFields.remove(operatorField);
        }
        return selectFields;
    }

    /**
     * Returns the given number, or {@link #DEFAULT_NUMBER} if it is empty.
     *
     * @param number the number
     * @return the string
     */
    public static String getNumberOrDefault(String number) {
        if (isEmpty(number)) {
            // if no number is specified, get the top 1
            number = DEFAULT_NUMBER;
        }
        return number;
    }

    /**
     * Gets the target field, which is basically a field that has to be selected in a SELECT SQL query.
     *
     * @param operator the operator
     * @param field1   the field 1
     * @param field2   the field 2
     * @return the tar field
     */
    public static String getTargetField(String operator, String field1, String field2) {
        if (!isEmpty(operator)) {
            if (allDistinct(operator)) {
                // Give me the FIELD1 of VALUE (ALL / DISTINCT)
                // Give me the targetField of VALUE
                return field1;
            } else if (!isEmpty(field1) && !isEmpty(field2)) {
                // Give me the FIELD1 with the highest FIELD2...
                // Give me the targetField with the highest operatorField
                return field1;
            } else {
                // Give me the highest FIELD1...
                // Give me the highest operatorField
                return null;
            }
        }
        // Give me the FIELD1 of VALUE => Give me the targetField of VALUE
        // Give me the ROW_NAME of VALUE => NULL
        return field1;
    }

    /**
     * Gets the operator field, which is basically a field to which an operator will be applied in a SELECT SQL query.
     *
     * @param operator the operator
     * @param field1   the field 1
     * @param field2   the field 2
     * @return the op field
     */
    public static String getOperatorField(String operator, String field1, String field2) {
        if (!isEmpty(operator)) {
            if (!isEmpty(field1) && !isEmpty(field2)) {
                // Give me the FIELD1 with the highest FIELD2...
                // Give me the targetField with the highest operatorField
                return field2;
            } else {
                // Give me the highest FIELD1...
                // Give me the highest operatorField
                return field1;
            }
        }
        return null;
    }

    /**
     * Gets operator data type.
     *
     * @param operator the operator
     * @return the operator data type
     */
    public static String getOperatorDataType(String operator) {
        if (maxMin(operator)) {
            return DECIMAL;
        }
        if (oldestNewest(operator)) {
            return DATETIME;
        }
        return null;
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String number = (String) context.getSession().get(ContextKeys.NUMBER);
        String field1 = (String) context.getSession().get(ContextKeys.FIELD + "1");
        String rowName = (String) context.getSession().get(ContextKeys.ROW_NAME);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String field2 = (String) context.getSession().get(ContextKeys.FIELD + "2");
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");

        if (allDistinct(operator)) {
            // This is the 2nd iteration of the workflow, and we assume parameters are OK
            return true;
        }

        String operatorField = getOperatorField(operator, field1, field2);
        if (!(isEmpty(operator) == isEmpty(operatorField))) {
            // We should have both operator and operatorField or none of them
            return false;
        }
        if (!isEmpty(operator)
                && !(Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.numericFieldEntity).contains(operatorField))
                && !(Utils.getEntityValues(bot.entities.datetimeFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.datetimeFieldEntity).contains(operatorField))) {
            // Check that operator type matches operatorField type
            return false;
        }

        Map<String, String> valueFieldMap = getValueFieldMap(value1, value2);
        context.getSession().put(ContextKeys.VALUE_FIELD_MAP, valueFieldMap);
        boolean someValue = !valueFieldMap.isEmpty();

        if (someValue
                && isEmpty(number) && isEmpty(rowName) && isEmpty(field1) && isEmpty(operator) && isEmpty(field2)) {
            // Who are the VALUE?
            // Who are the women?
            context.getSession().put(ContextKeys.ROW_NAME, Utils.getEntityValues(bot.entities.rowNameEntity).get(0));
            // We store a rowName, since this is the same as "Give me the rows that are woman"
            return true;
        }

        if (!isEmpty(rowName) && someValue
                && isEmpty(number) && isEmpty(field1) && isEmpty(operator) && isEmpty(field2)) {
            // Give me the ROW_NAME of VALUE
            // Give me the members of marketing
            return true;
        }

        if (!isEmpty(field1) && someValue
                && isEmpty(number) && isEmpty(rowName) && isEmpty(operator) && isEmpty(field2)) {
            // Give me the FIELD1 of VALUE
            // Give me the salaries of marketing
            return true;
        }

        if (!isEmpty(operator) && !isEmpty(field1)
                && isEmpty(rowName) && isEmpty(field2)) {
            // Give me the [NUMBER] OPERATOR FIELD1 [of VALUE]
            // Give me the [5] highest salary [of marketing]
            return true;
        }

        if (!isEmpty(field1) && !isEmpty(operator) && !isEmpty(field2)
                && isEmpty(rowName)) {
            // Give me the [NUMBER] FIELD1 with the OPERATOR FIELD2 [of VALUE]
            // Give me the [5] ages with the highest salary [of marketing]
            return true;
        }

        if (!isEmpty(rowName) && !isEmpty(operator) && !isEmpty(field1)
                && isEmpty(field2)) {
            // Give me the [NUMBER] ROW_NAME with the OPERATOR FIELD1 [of VALUE]
            // Give me the [5] members with the highest salary [of marketing]
            return true;
        }
        // At this point, we have not found a valid combination of parameters
        return false;
    }

    @Override
    protected boolean continueWhenParamsNotOk(StateContext context) {
        // when params are not ok, we stop the execution
        return false;
    }

    @Override
    protected State getNextStateWhenParamsNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }

    @Override
    protected String generateSqlStatement(StateContext context) {
        String number = (String) context.getSession().get(ContextKeys.NUMBER);
        String field1 = (String) context.getSession().get(ContextKeys.FIELD + "1");
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String field2 = (String) context.getSession().get(ContextKeys.FIELD + "2");
        Map<String, String> valueFieldMap = (Map<String, String>) context.getSession().get(ContextKeys.VALUE_FIELD_MAP);
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);

        String operatorField = getOperatorField(operator, field1, field2);
        String targetField = getTargetField(operator, field1, field2);
        List<String> selectFields;

        if (isEmpty(operator) || all(operator)) {
            // SELECT fields WHERE conditions
            selectFields = getSelectFields(bot.entities.keyFields, valueFieldMap, targetField, null);
            return sqlQueries.selectFieldsWithConditionsNoOperator(selectFields, valueFieldMap, false);
        } else if (distinct(operator)) {
            // SELECT DISTINCT targetField WHERE conditions
            selectFields = getSelectFields(null, null, targetField, null);
            return sqlQueries.selectFieldsWithConditionsNoOperator(selectFields, valueFieldMap, true);
        } else if (maxMinOldestNewest(operator)) {
            // SELECT fields, MAX(operatorField) WHERE conditions
            selectFields = getSelectFields(bot.entities.keyFields, valueFieldMap, targetField, operatorField);
            String dataType = getOperatorDataType(operator);
            // if we have a datetime operator (oldest/newest), convert it to numeric operator (min/max)
            operator = datetimeOperatorToNumericOperator(operator);
            number = getNumberOrDefault(number);
            return sqlQueries.selectFieldsWithConditionsMaxMinOperator(selectFields, operatorField, operator, dataType, valueFieldMap, number);
        } else {
            // Other operators (AVG, SUM)
            // SELECT AVG(operatorField) WHERE conditions
            return sqlQueries.selectFieldsWithConditionsAvgSumOperator(operatorField, operator, valueFieldMap);
        }
    }

    @Override
    protected boolean checkResultSetOk(StateContext context) {
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        String number = (String) context.getSession().get(ContextKeys.NUMBER);
        number = getNumberOrDefault(number);
        String rowName = (String) context.getSession().get(ContextKeys.ROW_NAME);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        if (isEmpty(operator) && isEmpty(rowName) && resultSet.getNumRows() > 1) {
            // We should ask the user to apply an operator
            // When there is a rowName, we allow a result set with > 1 rows
            return false;
        }
        if (!isEmpty(operator) && !allDistinct(operator) && resultSet.getNumRows() > Integer.parseInt(number)) {
            // There are more rows than expected
            return false;
        }
        return true;
    }

    @Override
    protected boolean continueWhenResultSetNotOk(StateContext context) {
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        if (isEmpty(operator) && resultSet.getNumRows() > 1) {
            // We ask the operator to the user
            return true;
        }
        return false;
    }

    @Override
    protected String generateMessage(StateContext context) {
        String rowName = (String) context.getSession().get(ContextKeys.ROW_NAME);
        String field1 = (String) context.getSession().get(ContextKeys.FIELD + "1");
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String field2 = (String) context.getSession().get(ContextKeys.FIELD + "2");
        String number = (String) context.getSession().get(ContextKeys.NUMBER);
        if (allDistinct(operator)) {
            return null;
        }
        String operatorField = getOperatorField(operator, field1, field2);
        String targetField = getTargetField(operator, field1, field2);
        String operatorFieldRN = bot.entities.readableNames.get(operatorField);
        String targetFieldRN;
        if (isEmpty(targetField)) {
            // If we don't have a targetField, we print a row name (the given one if it is not null, default one otherwise)
            targetFieldRN = (!isEmpty(rowName) ? rowName : Utils.getEntityValues(bot.entities.rowNameEntity).get(0));
        } else {
            targetFieldRN = bot.entities.readableNames.get(targetField);
        }
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        Map<String, String> valueFieldMap = (Map<String, String>) context.getSession().get(ContextKeys.VALUE_FIELD_MAP);
        String conditions = String.join(", ", Streams.zip(valueFieldMap.keySet().stream(),
                valueFieldMap.values().stream(), (v, f) -> bot.entities.readableNames.get(f) + " " + "= " + v).collect(Collectors.toList()));
        if (resultSet.getNumRows() == 0) {
            return MessageFormat.format(bot.messages.getString("SelectFieldsWithConditions0"), targetFieldRN, conditions);
        } else {
            if (isEmpty(operator)) {
                return MessageFormat.format(bot.messages.getString("SelectFieldsWithConditionsNoOperator"), targetFieldRN, conditions);
            } else {
                number = getNumberOrDefault(number);
                if (!isEmpty(conditions)) {
                    conditions = ", " + conditions;
                }
                if (maxMinOldestNewest(operator)) {
                    if (number.equals(DEFAULT_NUMBER)) {
                        return MessageFormat.format(bot.messages.getString("SelectFieldsWithConditionsMaxMin1"), targetFieldRN, operator, operatorFieldRN, conditions);
                    }
                    return MessageFormat.format(bot.messages.getString("SelectFieldsWithConditionsMaxMinNum"), number, targetFieldRN, operator, operatorFieldRN, conditions);
                } else {
                    // Other operators (AVG, SUM)
                    return MessageFormat.format(bot.messages.getString("SelectFieldsWithConditionsAvg"), operator, operatorFieldRN, conditions);
                }
            }
        }
    }

    @Override
    protected State getNextStateWhenResultSetNotOk() {
        // At this point, we ask the user an operator since we obtained 2 or more rows. Available options are:
        // Show all
        // Show all distinct
        // Numeric operator if targetField is a numeric field (max, min, avg, sum)
        // Datetime operator if targetField is a datetime field (oldest, newest)
        val askOperator = state(this.getClass().getSimpleName() + "AskOperator");
        val saveOperator = state(this.getClass().getSimpleName() + "SaveOperator");
        askOperator
                .body(context -> {
                    String field1 = (String) context.getSession().get(ContextKeys.FIELD + "1");
                    String field2 = (String) context.getSession().get(ContextKeys.FIELD + "2");
                    String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
                    String field1RN = bot.entities.readableNames.get(field1);
                    SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                    ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
                    Map<String, String> valueFieldMap = (Map<String, String>) context.getSession().get(ContextKeys.VALUE_FIELD_MAP);
                    String conditions = String.join(", ", Streams.zip(valueFieldMap.keySet().stream(),
                            valueFieldMap.values().stream(), (v, f) -> bot.entities.readableNames.get(f) + " " + "= " + v).collect(Collectors.toList()));
                    List<String> buttons = new ArrayList<>();
                    buttons.add(Utils.getFirstTrainingSentences(bot.intents.showAllIntent).get(0));
                    buttons.add(Utils.getFirstTrainingSentences(bot.intents.showAllDistinctIntent).get(0));
                    if (Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field1)) {
                        buttons.addAll(Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity));
                    } else if (Utils.getEntityValues(bot.entities.datetimeFieldEntity).contains(field1)) {
                        buttons.addAll(Utils.getEntityValues(bot.entities.datetimeFunctionOperatorEntity));
                    } else if (Utils.getEntityValues(bot.entities.textualFieldEntity).contains(field1)) {
                        // TODO: textual operators here
                    }
                    String targetField = getTargetField(operator, field1, field2);
                    List<String> selectFields = getSelectFields(null, null, targetField, null);
                    String sqlQuery = sqlQueries.selectFieldsWithConditionsNoOperator(selectFields, valueFieldMap, true);
                    ResultSet resultSetDistinct = sql.runSqlQuery(bot, sqlQuery);
                    buttons.add(Utils.getFirstTrainingSentences(bot.coreLibraryI18n.Quit).get(0));
                    bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(
                                    "SelectFieldsWithConditionsAskOperator"), resultSet.getNumRows(), field1RN,
                            resultSetDistinct.getNumRows(), conditions), buttons);
                })
                .next()
                .when(intentIs(bot.intents.showAllIntent)).moveTo(saveOperator)
                .when(intentIs(bot.intents.showAllDistinctIntent)).moveTo(saveOperator)
                .when(intentIs(bot.intents.numericFunctionOperatorIntent)).moveTo(saveOperator)
                .when(intentIs(bot.intents.datetimeFunctionOperatorIntent)).moveTo(saveOperator)
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState);
        saveOperator
                .body(context -> {
                    String input = context.getIntent().getMatchedInput();
                    String operator;
                    if (input.equals(Utils.getFirstTrainingSentences(bot.intents.showAllIntent).get(0))) {
                        operator = ALL;
                    } else if (input.equals(Utils.getFirstTrainingSentences(bot.intents.showAllDistinctIntent).get(0))) {
                        operator = DISTINCT;
                    } else {
                        // Regular operator
                        operator = input;
                    }
                    context.getSession().put(ContextKeys.OPERATOR, operator);
                })
                .next()
                .moveTo(mainState);

        return askOperator.getState();
    }
}
