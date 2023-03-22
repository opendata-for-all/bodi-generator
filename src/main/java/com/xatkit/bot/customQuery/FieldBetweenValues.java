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
 * The Field Between Values workflow of a chatbot.
 * <p>
 * Given a field and 2 values, it returns the rows that match the condition that the field is between those values
 * (e.g. age between 20 and 30).
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class FieldBetweenValues extends AbstractCustomQuery {

    public FieldBetweenValues(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");

        if (isEmpty(field) || isEmpty(value1) || isEmpty(value2)) {
            return false;
        }

        if (Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field)
                && isNumeric(value1) && isNumeric(value2)) {
            // Numeric interval
            return true;
        }

        if (Utils.getEntityValues(bot.entities.datetimeFieldEntity).contains(field)
                && isDatetime(value1) && isDatetime(value2)) {
            // Datetime interval
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
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        List<String> selectFields = new ArrayList<>(bot.entities.keyFields);
        String dataType = null;
        if (isNumeric(value1) && isNumeric(value2)) {
            dataType = DECIMAL;
        } else if (isDatetime(value1) && isDatetime(value2)) {
            dataType = DATETIME;
        }
        return sqlQueries.fieldBetweenValues(selectFields, field, value1, value2, dataType);
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
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
        String fieldRN = bot.entities.readableNames.get(field);
        return MessageFormat.format(bot.messages.getString("FieldBetweenValues"), fieldRN, value1, value2);
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
