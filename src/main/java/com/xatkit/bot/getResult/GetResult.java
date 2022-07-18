package com.xatkit.bot.getResult;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.Bot.coreLibraryI18n;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.bot.Bot.nlpServerClient;
import static com.xatkit.bot.Bot.pageLimit;
import static com.xatkit.bot.Bot.sql;
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
     * that question, using {@link com.xatkit.bot.Bot#nlpServerClient}.
     * <p>
     * The filters previously applied by the user are also added to
     * the query (see {@link com.xatkit.bot.nlp.NLPServerClient#runQuery(String)}
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
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public GetResult(ReactPlatform reactPlatform, State returnState) {
        val generateResultSetState = state("GenerateResultSet");
        val generateResultSetFromQueryState = state("GenerateResultSetFromQuery");
        val showDataState = state("ShowData");

        generateResultSetState
                .body(context -> {
                    String sqlQuery = sql.queries.selectAll();
                    resultSet = sql.runSqlQuery(sqlQuery);
                })
                .next()
                .moveTo(showDataState);

        this.generateResultSetState = generateResultSetState.getState();

        generateResultSetFromQueryState
                .body(context -> {
                    String query = context.getIntent().getMatchedInput();
                    resultSet = nlpServerClient.runQuery(query);
                })
                .next()
                .moveTo(showDataState);

        this.generateResultSetFromQueryState = generateResultSetFromQueryState.getState();

        showDataState
                .body(context -> {
                    int pageCount = 1;
                    if (context.getIntent().getMatchedInput()
                            .equals(Intents.showNextPageIntent.getTrainingSentences().get(0))) {
                        pageCount = (int) context.getSession().get(ContextKeys.PAGE_COUNT) + 1;
                    }
                    int totalEntries = resultSet.getNumRows();
                    int totalPages = totalEntries / pageLimit;
                    if (totalEntries % pageLimit != 0) {
                        totalPages += 1;
                    }
                    if (pageCount > totalPages) {
                        // Page overflow
                        pageCount = 1;
                    }
                    int offset = (pageCount - 1) * pageLimit;
                    context.getSession().put(ContextKeys.PAGE_COUNT, pageCount);

                    if (totalEntries > 0) {
                        // Print table
                        String resultSetString = resultSet.printTable(offset, pageLimit);
                        int selectedEntries = (offset + pageLimit > totalEntries ? totalEntries - offset : pageLimit);
                        reactPlatform.reply(context, MessageFormat.format(
                                messages.getString("ShowingRecords"), selectedEntries, totalEntries));
                        if (totalPages > 1) {
                            reactPlatform.reply(context, MessageFormat.format(
                                    messages.getString("PageCount"), pageCount, totalPages));
                            reactPlatform.reply(context, resultSetString, Utils.getFirstTrainingSentences(
                                    Intents.showNextPageIntent,
                                    coreLibraryI18n.Quit));
                        } else {
                            reactPlatform.reply(context, resultSetString);
                        }
                    } else {
                        reactPlatform.reply(context, messages.getString("NothingFound"));
                    }
                })
                .next()
                .when(context -> (resultSet.getNumRows() <= pageLimit)).moveTo(returnState)
                .when(intentIs(Intents.showNextPageIntent)).moveTo(showDataState)
                .when(intentIs(coreLibraryI18n.Quit)).moveTo(returnState);

        this.showDataState = showDataState.getState();
    }
}
