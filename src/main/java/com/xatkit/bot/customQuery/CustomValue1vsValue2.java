package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.Operation;
import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.Bot.getResult;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Value1 vs Value2 workflow of a chatbot.
 * <p>
 * Given two field values, this workflow tells to the user which value is more/less frequent than the other.
 * <p>
 * These are the different entry points in this workflow:
 * <ul>
 *     <li>
 *         {@link #processCustomValue1MoreThanValue2State} It tells which is the most frequent value of the two.
 *     </li>
 *
 *     <li>
 *         {@link #processCustomValue1LessThanValue2State} It tells which is the least frequent value of the two.
 *     </li>
 * </ul>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomValue1vsValue2 {

    /**
     * The entry point for the Custom Value1 more than Value2 workflow.
     */
    @Getter
    private final State processCustomValue1MoreThanValue2State;

    /**
     * The entry point for the Custom Value1 less than Value2 workflow.
     */
    @Getter
    private final State processCustomValue1LessThanValue2State;

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
        val processCustomValue1MoreThanValue2State = state("ProcessCustomValue1MoreThanValue2");
        val processCustomValue1LessThanValue2State = state("ProcessCustomValue1LessThanValue2");

        processCustomValue1MoreThanValue2State
                .body(context -> {
                    error = false;
                    String value1 = (String) context.getIntent().getValue(ContextKeys.VALUE + "1");
                    String value2 = (String) context.getIntent().getValue(ContextKeys.VALUE + "2");
                    if (!isEmpty(value1) && !isEmpty(value2)) {
                        String field1 = Entities.fieldValueMap.get(value1);
                        String field2 = Entities.fieldValueMap.get(value2);
                        Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                        int value1Freq = (int) statement.executeQuery(Operation.VALUE_FREQUENCY, field1, value1);
                        int value2Freq = (int) statement.executeQuery(Operation.VALUE_FREQUENCY, field2, value2);
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
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(getResult.getGenerateResultSetFromQueryState())
                .when(context -> !error).moveTo(returnState);

        this.processCustomValue1MoreThanValue2State = processCustomValue1MoreThanValue2State.getState();

        processCustomValue1LessThanValue2State
                .body(context -> {
                    error = false;
                    String value1 = (String) context.getIntent().getValue(ContextKeys.VALUE + "1");
                    String value2 = (String) context.getIntent().getValue(ContextKeys.VALUE + "2");
                    if (!isEmpty(value1) && !isEmpty(value2)) {
                        String field1 = Entities.fieldValueMap.get(value1);
                        String field2 = Entities.fieldValueMap.get(value2);
                        Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                        int value1Freq = (int) statement.executeQuery(Operation.VALUE_FREQUENCY, field1, value1);
                        int value2Freq = (int) statement.executeQuery(Operation.VALUE_FREQUENCY, field2, value2);
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
                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(getResult.getGenerateResultSetFromQueryState())
                .when(context -> !error).moveTo(returnState);

        this.processCustomValue1LessThanValue2State = processCustomValue1LessThanValue2State.getState();
    }
}
