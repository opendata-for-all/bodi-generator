package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.App.sql;
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
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFilter(Bot bot, State returnState) {
        val saveCustomFilterState = state("SaveCustomFilter");
        val selectNextActionState = state("SelectNextAction");

        saveCustomFilterState
                .body(context -> {
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    String operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
                        bot.sqlQueries.addFilter(field, operator, value);
                        String sqlQuery =  bot.sqlQueries.selectAll();
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        bot.getResult.setResultSet(resultSet);
                        resultSetNumRows = resultSet.getNumRows();
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("FilterAdded"),
                                field, operator, value, resultSet.getNumRows()));
                    } else {
                        bot.reactPlatform.reply(context, bot.messages.getString("SomethingWentWrong"));
                    }
                })
                .next()
                .when(context -> resultSetNumRows <= bot.maxEntriesToDisplay).moveTo(bot.getResult.getShowDataState())
                .when(context -> resultSetNumRows > bot.maxEntriesToDisplay).moveTo(selectNextActionState);

        selectNextActionState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("SelectNextAction"),
                            Utils.getFirstTrainingSentences(
                                    bot.intents.anotherQueryIntent,
                                    bot.intents.showDataIntent,
                                    bot.coreLibraryI18n.Quit));
                })
                .next()
                .when(intentIs(bot.intents.anotherQueryIntent)).moveTo(returnState)
                .when(intentIs(bot.intents.showDataIntent)).moveTo(bot.getResult.getShowDataState())
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState);

        this.saveCustomFilterState = saveCustomFilterState.getState();
    }
}
