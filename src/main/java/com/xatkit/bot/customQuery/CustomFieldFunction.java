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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Field Function workflow of a chatbot.
 * <p>
 * Given a field name and an operator, this workflow applies the operator in the field and shows the result.
 * <p>
 * Some example operators are: {@code max}, {@code min}, {@code avg}, {@code sum}
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomFieldFunction extends AbstractCustomQuery {

    public CustomFieldFunction(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        if (!isEmpty(operator)
                && !(Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field))
                && !(Utils.getEntityValues(bot.entities.dateFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.dateFieldEntity).contains(field))) {
            // Check that operator type matches field type
            return false;
        }
        return !isEmpty(field) && !isEmpty(operator);
    }

    @Override
    protected boolean continueWhenParamsNotOk(StateContext context) {
        // When params are not ok, we stop the execution
        return false;
    }

    @Override
    protected State getNextStateWhenParamsNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }

    @Override
    protected String generateSqlStatement(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
        String value1Field = Entities.fieldValueMap.get(value1);
        String value2Field = Entities.fieldValueMap.get(value2);
        Map<String, String> valueFieldMap = new HashMap<>();
        if (!isEmpty(value1) && !isEmpty(value1Field)) {
            valueFieldMap.put(value1, value1Field);
        }
        if (!isEmpty(value2) && !isEmpty(value2Field)) {
            valueFieldMap.put(value2, value2Field);
        }
        context.getSession().put(ContextKeys.VALUE_FIELD_MAP, valueFieldMap);
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        // TODO: Support date-time data type
        return sqlQueries.numericFieldFunction(field, operator, valueFieldMap);
    }

    @Override
    protected boolean checkResultSetOk(StateContext context) {
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        return resultSet.getNumRows() > 0;
    }

    @Override
    protected boolean continueWhenResultSetNotOk(StateContext context) {
        // When result set is not ok, we stop the execution
        return false;
    }

    @Override
    protected String generateMessage(StateContext context) {
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        Map<String, String> valueFieldMap = (Map<String, String>) context.getSession().get(ContextKeys.VALUE_FIELD_MAP);
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        float result = Float.parseFloat(resultSet.getRow(0).getColumnValue(0));
        String fieldRN = bot.entities.readableNames.get(field);
        String conditions = String.join(", ", Streams.zip(valueFieldMap.keySet().stream(),
                valueFieldMap.values().stream(), (v, f) -> bot.entities.readableNames.get(f) + " " + "= " + v).collect(Collectors.toList()));
        if (conditions.isEmpty()) {
            return MessageFormat.format(bot.messages.getString("CustomFieldFunction"), operator, fieldRN, result);
        } else {
            return MessageFormat.format(bot.messages.getString("CustomFieldFunctionConditions"), operator, fieldRN, conditions, result);
        }
    }

    @Override
    protected State getNextStateWhenResultSetNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }
}
