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

import static com.xatkit.bot.Bot.messages;
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
    private final State entryPoint;

    /**
     * Instantiates a new Custom Value Frequency workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomValueFrequency(ReactPlatform reactPlatform, State returnState) {
        val entryPoint = state("EntryPoint");

        entryPoint
                .body(context -> {
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(value)) {
                        String field = Entities.fieldValueMap.get(value);
                        Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                        int valueFrequency = (int) statement.executeQuery(Operation.VALUE_FREQUENCY, field, value);
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomShowValueFrequency"),
                                valueFrequency, field, value));
                    } else {
                        reactPlatform.reply(context, messages.getString("FieldNotRecognized"));
                    }
                })
                .next()
                .moveTo(returnState);

        this.entryPoint = entryPoint.getState();
    }
}