package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
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
 * The Custom Value1 vs Value2 workflow of a chatbot.
 * <p>
 * Given two field values, this workflow tells to the user which value is more/less frequent than the other.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomValue1vsValue2 {

    /**
     * The entry point for the Custom Value1 more than Value2 workflow.
     */
    @Getter
    private final State processCustomValue1vsValue2State;

    /**
     * This variable stores the error condition of the workflow (i.e. if some parameter was not recognized properly)
     */
    private boolean error;

    /**
     * Instantiates a new Custom Value1 vs Value2 workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomValue1vsValue2(ReactPlatform reactPlatform, State returnState) {
        val processCustomValue1vsValue2State = state("ProcessCustomValue1vsValue2");

        processCustomValue1vsValue2State
                .body(context -> {
                    error = false;
                    String value1 = (String) context.getIntent().getValue(ContextKeys.VALUE + "1");
                    String value2 = (String) context.getIntent().getValue(ContextKeys.VALUE + "2");
                    if (!isEmpty(value1) && !isEmpty(value2)) {
                        String field1 = Entities.fieldValueMap.get(value1);
                        String field2 = Entities.fieldValueMap.get(value2);

                        String sqlQuery = sql.queries.valueFrequency(field1, value1);
                        ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                        int value1Freq = Integer.parseInt(resultSet.getRow(0).getColumnValue(0));

                        sqlQuery = sql.queries.valueFrequency(field1, value1);
                        resultSet = sql.runSqlQuery(sqlQuery);
                        int value2Freq = Integer.parseInt(resultSet.getRow(0).getColumnValue(0));

                        if (context.getIntent().getDefinition().getName().equals(Intents.customValue1MoreThanValue2Intent.getName())) {
                            if (value1Freq > value2Freq) {
                                reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomValue1MoreThanValue2"),
                                        value1, value1Freq, field1, value2, value2Freq, field2));
                            } else if (value2Freq > value1Freq) {
                                reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomValue1MoreThanValue2"),
                                        value2, value2Freq, field2, value1, value1Freq, field1));
                            } else {
                                reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomValue1EqualToValue2"),
                                        value1, field1, value2, field2, value1Freq));
                            }
                        } else {
                            if (value1Freq < value2Freq) {
                                reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomValue1LessThanValue2"),
                                        value1, value1Freq, field1, value2, value2Freq, field2));
                            } else if (value2Freq < value1Freq) {
                                reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomValue1LessThanValue2"),
                                        value2, value2Freq, field2, value1, value1Freq, field1));
                            } else {
                                reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomValue1EqualToValue2"),
                                        value1, field1, value2, field2, value1Freq));
                            }
                        }
                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(getResult.getGenerateResultSetFromQueryState())
                .when(context -> !error).moveTo(returnState);

        this.processCustomValue1vsValue2State = processCustomValue1vsValue2State.getState();
    }
}
