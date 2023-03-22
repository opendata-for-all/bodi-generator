package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;

import java.text.MessageFormat;

import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * The Show Field Distinct workflow of a chatbot.
 * <p>
 * Given a field name, this workflow gets the unique values (i.e. a set) of that field and shows it to the user.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class ShowFieldDistinct extends AbstractCustomQuery {

    public ShowFieldDistinct(Bot bot, State returnState) {
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

    @Override
    protected String generateSqlStatement(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        return sqlQueries.showFieldDistinct(field);
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
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String fieldRN = bot.entities.readableNames.get(field);
        return MessageFormat.format(bot.messages.getString("ShowFieldDistinct"), fieldRN);
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
