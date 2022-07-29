package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.App.sql;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Row Count workflow of a chatbot.
 * <p>
 * It is used to tell the user how many rows or entries are stored in the chatbot's tabular data source.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 * @see Entities#generateRowNameEntity()
 */
public class CustomRowCount {

    /**
     * The entry point for the Custom Row Count workflow.
     */
    @Getter
    private final State processCustomRowCountState;

    /**
     * This variable stores the error condition of the workflow (i.e. if some parameter was not recognized properly)
     */
    private boolean error;

    /**
     * Instantiates a new Custom Row Count workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomRowCount(Bot bot, State returnState) {
        val processCustomRowCountState = state("ProcessCustomRowCount");

        processCustomRowCountState
                .body(context -> {
                    error = false;
                    String rowName = (String) context.getIntent().getValue(ContextKeys.ROW_NAME);
                    if (!isEmpty(rowName)) {
                        String sqlQuery = bot.sqlQueries.rowCount();
                        ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                        int rowCount = Integer.parseInt(resultSet.getRow(0).getColumnValue(0));
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("ShowRowCount"),
                                rowCount, rowName));
                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> !error).moveTo(returnState);

        this.processCustomRowCountState = processCustomRowCountState.getState();
    }
}
