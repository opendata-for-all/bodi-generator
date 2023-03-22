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
 * The Row Count workflow of a chatbot.
 * <p>
 * It is used to tell the user how many rows or entries are stored in the chatbot's tabular data source.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 * @see Entities#generateRowNameEntity()
 */
public class RowCount extends AbstractCustomQuery {

    public RowCount(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String rowName = (String) context.getSession().get(ContextKeys.ROW_NAME);
        return !isEmpty(rowName);
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
        return sqlQueries.rowCount();
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
        String rowName = (String) context.getSession().get(ContextKeys.ROW_NAME);
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        int rowCount = Integer.parseInt(resultSet.getRow(0).getColumnValue(0));
        return MessageFormat.format(bot.messages.getString("ShowRowCount"), rowCount, rowName);
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
