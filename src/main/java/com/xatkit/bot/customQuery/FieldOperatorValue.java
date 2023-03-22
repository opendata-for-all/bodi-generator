package com.xatkit.bot.customQuery;

import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Utils;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.xatkit.bot.library.Utils.isDatetime;
import static com.xatkit.bot.library.Utils.isNumeric;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Field Operator Value workflow of a chatbot.
 * <p>
 * Given a condition composed by a field, an operator and a value (all the same data type) returns a resultset that
 * matches the condition (e.g. salary > 100000).
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class FieldOperatorValue extends AbstractCustomQuery {

    public FieldOperatorValue(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String value = (String) context.getSession().get(ContextKeys.VALUE);

        if (isEmpty(field) || isEmpty(operator) || isEmpty(value)) {
            return false;
        }

        if (Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field)
                && Utils.getEntityValues(bot.entities.numericOperatorEntity).contains(operator)
                && isNumeric(value)) {
            // Numeric filter
            return true;
        }

        if (Utils.getEntityValues(bot.entities.textualFieldEntity).contains(field)
                && Utils.getEntityValues(bot.entities.textualOperatorEntity).contains(operator)) {
            // Textual filter
            return true;
        }

        if (Utils.getEntityValues(bot.entities.datetimeFieldEntity).contains(field)
                && Utils.getEntityValues(bot.entities.datetimeOperatorEntity).contains(operator)
                && isDatetime(value)) {
            // Datetime filter
            return true;
        }

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
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String value = (String) context.getSession().get(ContextKeys.VALUE);
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        List<String> selectFields = new ArrayList<>(bot.entities.keyFields);
        return sqlQueries.fieldOperatorValue(selectFields, field, operator, value);
    }

    @Override
    protected boolean checkResultSetOk(StateContext context) {
        return true;
    }

    @Override
    protected boolean continueWhenResultSetNotOk(StateContext context) {
        // When result set is not ok, we stop the execution
        return false;
    }

    @Override
    protected String generateMessage(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String value = (String) context.getSession().get(ContextKeys.VALUE);
        String fieldRN = bot.entities.readableNames.get(field);
        return MessageFormat.format(bot.messages.getString("FieldOperatorValue"), fieldRN, operator, value);
    }

    @Override
    protected boolean showResultSet(StateContext context) {
        return true;
    }

    @Override
    protected State getNextStateWhenResultSetNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }
}
