package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import bodi.generator.dataSource.Statement;
import com.xatkit.bot.languageModel.TextToTableClient;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.Bot.coreLibraryI18n;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;

/**
 * The SQL Query workflow of a chatbot.
 * <p>
 * When no intent is recognized from a user question, this workflow is executed to try to obtain a tabular answer to
 * that question. This is done by making a request to a server running a language model that translates the natural
 * language question to a SQL statement and then executes it in the chatbot tabular data source to obtain the answer.
 * <p>
 * The filters applied previously by the user are also added to the SQL query (see
 * {@link com.xatkit.bot.languageModel.LanguageModelClient#runQuery(String, Statement)}
 * <p>
 * The obtained answer is printed to the user by pages.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class SQLQuery {

    /**
     * The entry point for the SQL Query workflow.
     */
    @Getter
    private final State processQuery;

    /**
     * The client that interacts with the server that deploys the language model to answer the questions.
     */
    TextToTableClient defaultFallbackNLPClient = new TextToTableClient();

    /**
     * The maximum number of entries of a table that are displayed at once in the chatbot chat box (i.e. the page size).
     */
    private final int pageLimit = 10;

    /**
     * This {@link ResultSet} stores the answer of the last user query that was processed by this workflow.
     */
    private ResultSet resultSet;

    /**
     * Instantiates a new SQL Query workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public SQLQuery(ReactPlatform reactPlatform, StateProvider returnState) {
        val processQuery = state("ProcessQuery");
        val showDataState = state("ShowData");

        processQuery
                .body(context -> {
                    Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                    String question = context.getIntent().getMatchedInput();
                    resultSet = defaultFallbackNLPClient.runTableQuery(question, statement);
                })
                .next()
                .moveTo(showDataState);

        showDataState
                .body(context -> {
                    int pageCount = 1;
                    if (context.getIntent().getMatchedInput()
                            .equals(Intents.showNextPageIntent.getTrainingSentences().get(0))) {
                        pageCount = (int) context.getSession().get(ContextKeys.PAGE_COUNT) + 1;
                    }
                    // TODO: Handle exception when resultSet is NULL
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
                        reactPlatform.reply(context, MessageFormat.format(
                                messages.getString("PageCount"), pageCount, totalPages));
                        reactPlatform.reply(context, resultSetString, Utils.getFirstTrainingSentences(
                                Intents.showNextPageIntent,
                                coreLibraryI18n.Quit));
                    } else {
                        reactPlatform.reply(context, messages.getString("NothingFound"),
                                Utils.getFirstTrainingSentences(coreLibraryI18n.Quit));
                    }
                })
                .next()
                .when(intentIs(Intents.showNextPageIntent)).moveTo(showDataState)
                .when(intentIs(coreLibraryI18n.Quit)).moveTo(returnState);

        this.processQuery = processQuery.getState();
    }

}
