package com.xatkit.bot.customQuery;

import com.xatkit.bot.library.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;

import java.text.MessageFormat;

/**
 * The Field Count workflow of a chatbot.
 * <p>
 * It is used to tell the user how many fields or columns is the chatbot's tabular data source composed by.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class FieldCount extends AbstractCustomQuery {

    public FieldCount(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
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
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        return sqlQueries.fieldCount();
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
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        int fieldCount = resultSet.getNumColumns();
        return MessageFormat.format(bot.messages.getString("ShowFieldCount"), fieldCount);
    }

    @Override
    protected boolean showResultSet(StateContext context) {
        return false;
    }

    @Override
    protected State getNextStateWhenResultSetNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }
}
