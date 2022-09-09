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
 * The Custom Value Frequency workflow of a chatbot.
 * <p>
 * Given a value of a field, this workflow gets its frequency (i.e. the number of occurrences) within the field and
 * shows it to the user.
 * <p>
 * Note that only the values present in {@link Entities#fieldValueMap} can be recognized.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 * @see Entities#generateFieldValueEntity()
 */
public class CustomValueFrequency {

    /**
     * The entry point for the Custom Value Frequency workflow.
     */
    @Getter
    private final State processCustomValueFrequencyState;

    /**
     * This variable stores the error condition of the workflow (i.e. if some parameter was not recognized properly)
     */
    private boolean error;

    /**
     * Instantiates a new Custom Value Frequency workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomValueFrequency(Bot bot, State returnState) {
        val processCustomValueFrequencyState = state("ProcessCustomValueFrequency");

        processCustomValueFrequencyState
                .body(context -> {
                    error = false;
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(value)) {
                        String field = Entities.fieldValueMap.get(value);
                        String sqlQuery = bot.sqlQueries.valueFrequency(field, value);
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        int valueFrequency = Integer.parseInt(resultSet.getRow(0).getColumnValue(0));
                        String fieldRN = bot.entities.readableNames.get(field);
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("ShowValueFrequency"),
                                valueFrequency, fieldRN, value));
                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> !error).moveTo(returnState);

        this.processCustomValueFrequencyState = processCustomValueFrequencyState.getState();
    }
}
