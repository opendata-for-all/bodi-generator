package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.App.sql;
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
     * Instantiates a new Custom Value1 vs Value2 workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomValue1vsValue2(Bot bot, State returnState) {
        val processCustomValue1vsValue2State = state("ProcessCustomValue1vsValue2");

        processCustomValue1vsValue2State
                .body(context -> {
                    context.getSession().put(ContextKeys.ERROR, false);
                    String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
                    String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
                    if (!isEmpty(value1) && !isEmpty(value2)) {
                        String field1 = Entities.fieldValueMap.get(value1);
                        String field2 = Entities.fieldValueMap.get(value2);
                        String field1RN = bot.entities.readableNames.get(field1);
                        String field2RN = bot.entities.readableNames.get(field2);

                        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                        String sqlQuery = sqlQueries.valueFrequency(field1, value1);
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        int value1Freq = Integer.parseInt(resultSet.getRow(0).getColumnValue(0));

                        sqlQuery = sqlQueries.valueFrequency(field2, value2);
                        resultSet = sql.runSqlQuery(bot, sqlQuery);
                        int value2Freq = Integer.parseInt(resultSet.getRow(0).getColumnValue(0));

                        if (context.getIntent().getDefinition().getName().equals(bot.intents.customValue1MoreThanValue2Intent.getName())) {
                            if (value1Freq > value2Freq) {
                                bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomValue1MoreThanValue2"),
                                        value1, value1Freq, field1RN, value2, value2Freq, field2RN));
                            } else if (value2Freq > value1Freq) {
                                bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomValue1MoreThanValue2"),
                                        value2, value2Freq, field2RN, value1, value1Freq, field1RN));
                            } else {
                                bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomValue1EqualToValue2"),
                                        value1, field1RN, value2, field2RN, value1Freq));
                            }
                        } else {
                            if (value1Freq < value2Freq) {
                                bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomValue1LessThanValue2"),
                                        value1, value1Freq, field1RN, value2, value2Freq, field2RN));
                            } else if (value2Freq < value1Freq) {
                                bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomValue1LessThanValue2"),
                                        value2, value2Freq, field2RN, value1, value1Freq, field1RN));
                            } else {
                                bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomValue1EqualToValue2"),
                                        value1, field1RN, value2, field2RN, value1Freq));
                            }
                        }
                    } else {
                        context.getSession().put(ContextKeys.ERROR, true);
                    }
                })
                .next()
                .when(context -> (boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> !(boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(returnState);

        this.processCustomValue1vsValue2State = processCustomValue1vsValue2State.getState();
    }
}
