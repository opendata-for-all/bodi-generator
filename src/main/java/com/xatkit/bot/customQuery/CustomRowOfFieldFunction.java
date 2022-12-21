package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Utils;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Row Of Field Function workflow of a chatbot.
 * <p>
 * Given a field name and an operator, this workflow shows all the entries that match the field value with
 * the value obtained when applying the operator.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomRowOfFieldFunction extends AbstractCustomQuery {

    public CustomRowOfFieldFunction(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String rowName = (String) context.getSession().get(ContextKeys.ROW_NAME);
        if (isEmpty(rowName)) {
            rowName = Utils.getEntityValues(bot.entities.rowNameEntity).get(0);
            context.getSession().put(ContextKeys.ROW_NAME, rowName);
        }
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        if (!isEmpty(operator)
                && !(Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field))
                && !(Utils.getEntityValues(bot.entities.dateFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.dateFieldEntity).contains(field))) {
            // Check that operator type matches field type
            return false;
        }
        return !isEmpty(field) && !isEmpty(operator) && (operator.equals("max") || operator.equals("min") || operator.equals("oldest") || operator.equals("newest"));
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
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        List<String> keyFields = new ArrayList<>(bot.entities.keyFields);
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        // TODO: Support date-time data type
        return sqlQueries.rowOfNumericFieldFunction(keyFields, field, operator);
    }

    @Override
    protected boolean checkResultSetOk(StateContext context) {
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        return resultSet.getNumRows() > 0;
    }

    @Override
    protected boolean continueWhenResultSetNotOk(StateContext context) {
        // when result set is not ok, we stop the execution
        return false;
    }

    @Override
    protected String generateMessage(StateContext context) {
        String rowName = (String) context.getSession().get(ContextKeys.ROW_NAME);
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String fieldRN = bot.entities.readableNames.get(field);
        return MessageFormat.format(bot.messages.getString("CustomRowOfFieldFunction"), rowName, operator, fieldRN);
    }

    @Override
    protected State getNextStateWhenResultSetNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }
}
