package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;

import java.text.MessageFormat;

import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * The Value Frequency workflow of a chatbot.
 * <p>
 * Given a value of a field, this workflow gets its frequency (i.e. the number of occurrences) within the field and
 * shows it to the user.
 * <p>
 * Note that only the values present in {@link Entities#fieldValueMap} can be recognized.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class ValueFrequency extends AbstractCustomQuery {

    public ValueFrequency(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String value = (String) context.getSession().get(ContextKeys.VALUE);
        return !isEmpty(value);
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
        String value = (String) context.getSession().get(ContextKeys.VALUE);
        String field = Entities.fieldValueMap.get(value);
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        return sqlQueries.valueFrequency(field, value);
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
        String value = (String) context.getSession().get(ContextKeys.VALUE);
        String field = Entities.fieldValueMap.get(value);
        String fieldRN = bot.entities.readableNames.get(field);
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        int valueFrequency = Integer.parseInt(resultSet.getRow(0).getColumnValue(0));
        return MessageFormat.format(bot.messages.getString("ShowValueFrequency"), valueFrequency, fieldRN, value);
    }

    @Override
    protected State getNextStateWhenResultSetNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }
}
