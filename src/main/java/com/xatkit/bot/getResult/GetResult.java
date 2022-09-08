package com.xatkit.bot.getResult;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.App;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.App.nlpServerClient;
import static com.xatkit.bot.App.sql;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;

/**
 * The Get Result workflow of a chatbot.
 * <p>
 * It prints the processed table on which the chatbot serves. It can be done through 2 different ways:
 * <ul>
 *     <li>{@link #generateResultSetState}</li>
 *
 *     <li>{@link #generateResultSetFromQueryState}</li>
 * </ul>
 * After one of these states is executed, the generated result is printed in the chatbot interface.
 * <p>
 * If the resulting table is too long to display it in the chatbot chat box, it is divided by pages of a maximum
 * length so the user can navigate through the pages in a more friendly way.
 */
public class GetResult {

    /**
     * One of the entry points for the Get Result workflow.
     * <p>
     * This state applies the necessary filters in the table (previously specified by the user) and
     * gets the resulting table.
     */
    @Getter
    private final State generateResultSetState;

    /**
     * One of the entry points for the Get Result workflow.
     * <p>
     * When no intent is recognized from a user question, this state is executed to try to obtain a tabular answer to
     * that question, using {@link App#nlpServerClient}.
     * <p>
     * <del>The filters previously applied by the user are also added to the query (see
     * {@link com.xatkit.bot.nlp.NLPServerClient#runQuery(String, String)}</del>
     */
    @Getter
    private final State generateResultSetFromQueryState;

    /**
     * One of the entry points for the Get Result workflow.
     * <p>
     * This state shows the result set stored in {@link #resultSet} (previously set by another workflow)
     */
    @Getter
    private final State showDataState;

    /**
     * This {@link ResultSet} stores the result to be printed.
     */
    @Setter
    private ResultSet resultSet;

    /**
     * Instantiates a new Get Result workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public GetResult(Bot bot, State returnState) {
        val generateResultSetState = state("GenerateResultSet");
        val generateResultSetFromQueryState = state("GenerateResultSetFromQuery");
        val showDataState = state("ShowData");

        generateResultSetState
                .body(context -> {
                    String sqlQuery = bot.sqlQueries.selectAll();
                    resultSet = sql.runSqlQuery(sqlQuery);
                })
                .next()
                .moveTo(showDataState);

        this.generateResultSetState = generateResultSetState.getState();

        generateResultSetFromQueryState
                .body(context -> {
                    String query = context.getIntent().getMatchedInput();
                    resultSet = nlpServerClient.runQuery(query, bot.language);
                })
                .next()
                .moveTo(showDataState);

        this.generateResultSetFromQueryState = generateResultSetFromQueryState.getState();

        showDataState
                .body(context -> {
                    int pageCount = 1;
                    if (context.getIntent().getMatchedInput()
                            .equals(bot.intents.showNextPageIntent.getTrainingSentences().get(0))) {
                        pageCount = (int) context.getSession().get(ContextKeys.PAGE_COUNT) + 1;
                    } else if (context.getIntent().getMatchedInput()
                            .equals(bot.intents.showPreviousPageIntent.getTrainingSentences().get(0))) {
                        pageCount = (int) context.getSession().get(ContextKeys.PAGE_COUNT) - 1;
                    }
                    int totalEntries = resultSet.getNumRows();
                    int totalPages = totalEntries / bot.pageLimit;
                    if (totalEntries % bot.pageLimit != 0) {
                        totalPages += 1;
                    }
                    if (pageCount > totalPages) {
                        // Page overflow
                        pageCount = 1;
                    } else if (pageCount < 1) {
                        // Page overflow
                        pageCount = totalPages;
                    }
                    int offset = (pageCount - 1) * bot.pageLimit;
                    context.getSession().put(ContextKeys.PAGE_COUNT, pageCount);

                    if (totalEntries > 0) {
                        // Print table
                        String resultSetString = resultSet.printTable(offset, bot.pageLimit);
                        int selectedEntries = (offset + bot.pageLimit > totalEntries ? totalEntries - offset : bot.pageLimit);
                        bot.reactPlatform.reply(context, MessageFormat.format(
                                bot.messages.getString("ShowingRecords"), selectedEntries, totalEntries));
                        if (totalPages > 1) {
                            bot.reactPlatform.reply(context, MessageFormat.format(
                                    bot.messages.getString("PageCount"), pageCount, totalPages));
                            bot.reactPlatform.reply(context, resultSetString, Utils.getFirstTrainingSentences(
                                    bot.intents.showPreviousPageIntent,
                                    bot.intents.showNextPageIntent,
                                    bot.coreLibraryI18n.Quit));
                        } else {
                            bot.reactPlatform.reply(context, resultSetString);
                        }
                    } else {
                        bot.reactPlatform.reply(context, bot.messages.getString("NothingFound"));
                    }
                })
                .next()
                .when(context -> (resultSet.getNumRows() <= bot.pageLimit)).moveTo(returnState)
                .when(intentIs(bot.intents.showPreviousPageIntent)).moveTo(showDataState)
                .when(intentIs(bot.intents.showNextPageIntent)).moveTo(showDataState)
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState);

        this.showDataState = showDataState.getState();
    }
}
