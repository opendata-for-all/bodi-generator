package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;

import java.text.MessageFormat;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Frequent Value In Field workflow of a chatbot.
 * <p>
 * Given a field name, this workflow gets the most or least frequent values of that field and shows them to the user.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class FrequentValueInField extends AbstractCustomQuery {

    public FrequentValueInField(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        return !isEmpty(field);
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

    protected String generateSqlStatement(StateContext context) {
        boolean mostFrequent = false;
        if (context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.mostFrequentValueInFieldIntent.getName())) {
            mostFrequent = true;
        }
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        return sqlQueries.frequentValueInField(field, mostFrequent);
    }

    @Override
    protected boolean checkResultSetOk(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String fieldRN = bot.entities.readableNames.get(field);
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        List<String> header = resultSet.getHeader();
        return header.get(0).equals(fieldRN) && header.get(1).equals("freq") && resultSet.getNumRows() > 0;
    }

    @Override
    protected boolean continueWhenResultSetNotOk(StateContext context) {
        // When result set is not ok, we stop the execution
        return false;
    }

    @Override
    protected String generateMessage(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String fieldRN = bot.entities.readableNames.get(field);
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        String frequency = resultSet.getRow(0).getColumnValue(1);
        String messageName = "LeastFrequentValueInField";
        if (context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.mostFrequentValueInFieldIntent.getName())) {
            messageName = "MostFrequentValueInField";
        }
        return MessageFormat.format(bot.messages.getString(messageName), fieldRN, frequency);
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
