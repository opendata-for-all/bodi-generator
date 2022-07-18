package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Intents;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.Bot.getResult;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.bot.Bot.sql;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Frequent Value In Field workflow of a chatbot.
 * <p>
 * Given a field name, this workflow gets the most or least frequent values of that field and shows them to the user.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomFrequentValueInField {

    /**
     * The entry point for the Custom Most Frequent Value In Field workflow.
     */
    @Getter
    private final State processCustomFrequentValueInFieldState;

    /**
     * This variable stores the error condition of the workflow (i.e. if some parameter was not recognized properly)
     */
    private boolean error;

    /**
     * Instantiates a new Custom Frequent Value In Field workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFrequentValueInField(ReactPlatform reactPlatform, State returnState) {
        val processCustomFrequentValueInFieldState = state("ProcessCustomFrequentValueInField");

        processCustomFrequentValueInFieldState
                .body(context -> {
                    error = false;
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    if (!isEmpty(field)) {
                        boolean mostFrequent = false;
                        String messageName = "CustomLeastFrequentValueInField";
                        if (context.getIntent().getDefinition().getName().equals(Intents.customMostFrequentValueInFieldIntent.getName())) {
                            mostFrequent = true;
                            messageName = "CustomMostFrequentValueInField";
                        }
                        String sqlQuery = sql.queries.frequentValueInField(field, mostFrequent);
                        ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                        int frequency = Integer.parseInt(resultSet.getRow(0).getColumnValue(1));
                        sqlQuery = sql.queries.frequentValueInFieldMatch(field, frequency);
                        resultSet = sql.runSqlQuery(sqlQuery);
                        getResult.setResultSet(resultSet);
                        reactPlatform.reply(context, MessageFormat.format(messages.getString(messageName),
                                field, frequency));
                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(getResult.getGenerateResultSetFromQueryState())
                .when(context -> !error).moveTo(getResult.getShowDataState());

        this.processCustomFrequentValueInFieldState = processCustomFrequentValueInFieldState.getState();
    }
}
