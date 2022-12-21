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
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * The Custom Field Of Field Function workflow of a chatbot.
 * <p>
 * Given a number (n), a field name (field1), another field name (field2) and an operator (max/min), this workflow
 * shows the n field1 values whose row's field2 are between the n max/min values.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomFieldOfFieldFunction extends AbstractCustomQuery {

    public CustomFieldOfFieldFunction(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String field1 = (String) context.getSession().get(ContextKeys.FIELD + "1");
        String field2 = (String) context.getSession().get(ContextKeys.FIELD + "2");
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String number = (String) context.getSession().get(ContextKeys.NUMBER);
        if (isEmpty(number)) {
            // if no number is specified, get the top 1
            number = "1";
            context.getSession().put(ContextKeys.NUMBER, number);
        }
        if (isEmpty(field1) || isEmpty(field2) || isEmpty(operator)) {
            return false;
        }
        if (!(Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field2))
                && !(Utils.getEntityValues(bot.entities.dateFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.dateFieldEntity).contains(field2))) {
            // Check that operator type matches field type
            return false;
        }
        if (Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity).contains(operator) && !operator.equals("min") && !operator.equals("max")) {
            // Allowed numeric function operators: min, max
            return false;
        }
        if (Utils.getEntityValues(bot.entities.dateFunctionOperatorEntity).contains(operator) && !operator.equals("oldest") && !operator.equals("newest")) {
            // Allowed date function operators: oldest, newest
            return false;
        }
        if (!isNumeric(number)) {
            // Only positive integers are allowed
            return false;
        }
        return true;
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
        String field1 = (String) context.getSession().get(ContextKeys.FIELD + "1");
        String field2 = (String) context.getSession().get(ContextKeys.FIELD + "2");
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String number = (String) context.getSession().get(ContextKeys.NUMBER);

        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        List<String> keyFields = new ArrayList<>(bot.entities.keyFields);
        // TODO: Support date-time data type
        return sqlQueries.fieldOfNumericFieldFunction(keyFields, field1, field2, operator, number);
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
        String field1 = (String) context.getSession().get(ContextKeys.FIELD + "1");
        String field2 = (String) context.getSession().get(ContextKeys.FIELD + "2");
        String field1RN = bot.entities.readableNames.get(field1);
        String field2RN = bot.entities.readableNames.get(field2);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String number = (String) context.getSession().get(ContextKeys.NUMBER);
        if (number.equals("1")) {
            return MessageFormat.format(bot.messages.getString("CustomFieldOfFieldFunction1"), field1RN, operator, field2RN);
        } else {
            return MessageFormat.format(bot.messages.getString("CustomFieldOfFieldFunction"), number, field1RN, operator, field2RN);
        }
    }

    @Override
    protected State getNextStateWhenResultSetNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }
}
