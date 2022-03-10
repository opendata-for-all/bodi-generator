package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.Operation;
import bodi.generator.dataSource.ResultSet;
import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.Bot.coreLibraryI18n;
import static com.xatkit.bot.Bot.getResult;
import static com.xatkit.bot.Bot.maxEntriesToDisplay;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Filter workflow of a chatbot.
 * <p>
 * It recognizes a filter query and processes the given filter, storing it in the chatbot memory.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomFilter {

    /**
     * The entry point for the Custom Filter workflow.
     */
    @Getter
    private final State saveCustomFilterState;

    /**
     * This variable stores the number of rows of the generated result set.
     */
    private int resultSetNumRows;
    /**
     * Instantiates a new Custom Filter workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFilter(ReactPlatform reactPlatform, State returnState) {
        val saveCustomFilterState = state("SaveCustomFilter");
        val selectNextActionState = state("SelectNextAction");

        saveCustomFilterState
                .body(context -> {
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    String operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
                        Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                        statement.addFilter(field, operator, value);
                        ResultSet resultSet = (ResultSet) statement.executeQuery(Operation.NO_OPERATION);
                        resultSetNumRows = resultSet.getNumRows();
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("FilterAdded"),
                                field, operator, value, resultSet.getNumRows()));
                    } else {
                        reactPlatform.reply(context, messages.getString("SomethingWentWrong"));
                    }
                })
                .next()
                .when(context -> resultSetNumRows <= maxEntriesToDisplay).moveTo(getResult.getGenerateResultSetState())
                .when(context -> resultSetNumRows > maxEntriesToDisplay).moveTo(selectNextActionState);

        selectNextActionState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("SelectNextAction"),
                            Utils.getFirstTrainingSentences(
                                    Intents.anotherQueryIntent,
                                    Intents.showDataIntent,
                                    coreLibraryI18n.Quit));
                })
                .next()
                .when(intentIs(Intents.anotherQueryIntent)).moveTo(returnState)
                .when(intentIs(Intents.showDataIntent)).moveTo(getResult.getGenerateResultSetState())
                .when(intentIs(coreLibraryI18n.Quit)).moveTo(returnState);

        this.saveCustomFilterState = saveCustomFilterState.getState();
    }
}
