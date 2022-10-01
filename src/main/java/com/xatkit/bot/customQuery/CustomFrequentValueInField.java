package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.App.sql;
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
     * Instantiates a new Custom Frequent Value In Field workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFrequentValueInField(Bot bot, State returnState) {
        val processCustomFrequentValueInFieldState = state("ProcessCustomFrequentValueInField");

        processCustomFrequentValueInFieldState
                .body(context -> {
                    context.getSession().put(ContextKeys.ERROR, false);
                    String field = (String) context.getSession().get(ContextKeys.FIELD);
                    if (!isEmpty(field)) {
                        boolean mostFrequent = false;
                        String messageName = "CustomLeastFrequentValueInField";
                        if (context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customMostFrequentValueInFieldIntent.getName())) {
                            mostFrequent = true;
                            messageName = "CustomMostFrequentValueInField";
                        }
                        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                        String sqlQuery = sqlQueries.frequentValueInField(field, mostFrequent);
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        int frequency = Integer.parseInt(resultSet.getRow(0).getColumnValue(1));
                        sqlQuery = sqlQueries.frequentValueInFieldMatch(field, frequency);
                        resultSet = sql.runSqlQuery(bot, sqlQuery);
                        context.getSession().put(ContextKeys.RESULTSET, resultSet);
                        String fieldRN = bot.entities.readableNames.get(field);
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(messageName),
                                fieldRN, frequency));
                    } else {
                        context.getSession().put(ContextKeys.ERROR, true);
                    }
                })
                .next()
                .when(context -> (boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> !(boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(bot.getResult.getShowDataState());

        this.processCustomFrequentValueInFieldState = processCustomFrequentValueInFieldState.getState();
    }
}
