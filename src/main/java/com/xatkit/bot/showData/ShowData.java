package com.xatkit.bot.showData;

import bodi.generator.dataSource.ResultSet;
import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;

/**
 * The Show Data workflow of a chatbot.
 * <p>
 * It prints the processed table on which the chatbot serves. That is, it applies the necessary filters or operations
 * in the table (previously specified by the user) and shows it to the user.
 * <p>
 * If the resulting table is too long to display it in the chatbot chat box, it is divided by pages of a maximum
 * length so the user can navigate through the pages in a more friendly way.
 */
public class ShowData {

    /**
     * The entry point for the Show Data workflow.
     */
    @Getter
    private final State showDataState;

    /**
     * The maximum number of entries of a table that are displayed at once in the chatbot chat box (i.e. the page size).
     */
    private final int pageLimit = 10;

    /**
     * Instantiates a new Show Data workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public ShowData(ReactPlatform reactPlatform, StateProvider returnState) {
        val showDataState = state("ShowData");

        showDataState
                .body(context -> {
                    Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                    ResultSet resultSet = statement.executeQuery();
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
                        reactPlatform.reply(context, MessageFormat.format(
                                messages.getString("PageCount"), pageCount, totalPages));
                        reactPlatform.reply(context, resultSetString, Utils.getFirstTrainingSentences(
                                Intents.showNextPageIntent,
                                Intents.stopViewIntent));
                    } else {
                        reactPlatform.reply(context, messages.getString("NothingFound"),
                                Utils.getFirstTrainingSentences(Intents.stopViewIntent));
                    }
                })
                .next()
                .when(intentIs(Intents.showNextPageIntent)).moveTo(showDataState)
                .when(intentIs(Intents.stopViewIntent)).moveTo(returnState);

        this.showDataState = showDataState.getState();
    }
}
