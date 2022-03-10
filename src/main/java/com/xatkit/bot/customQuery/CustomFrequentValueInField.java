package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.Operation;
import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Set;

import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.state;
import static java.util.Objects.isNull;

/**
 * The Custom Frequent Value In Field workflow of a chatbot.
 * <p>
 * Given a field name, this workflow gets the most or least frequent values of that field.
 * <p>
 * These are the different entry points in this workflow:
 * <ul>
 *     <li>
 *         {@link #processCustomMostFrequentValueInFieldState} It gets the most frequent values of the specified field.
 *     </li>
 *
 *     <li>
 *         {@link #processCustomLeastFrequentValueInFieldState} It gets the least frequent values of the specified
 *         field.
 *     </li>
 * </ul>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomFrequentValueInField {

    /**
     * The entry point for the Custom Most Frequent Value In Field workflow.
     */
    @Getter
    private final State processCustomMostFrequentValueInFieldState;

    /**
     * The entry point for the Custom Less Frequent Value In Field workflow.
     */
    @Getter
    private final State processCustomLeastFrequentValueInFieldState;

    /**
     * Instantiates a new Custom Show Field Distinct workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFrequentValueInField(ReactPlatform reactPlatform, State returnState) {
        val processCustomMostFrequentValueInFieldState = state("ProcessCustomMostFrequentValueInField");
        val processCustomLeastFrequentValueInFieldState = state("ProcessCustomLeastFrequentValueInField");

        processCustomMostFrequentValueInFieldState
                .body(context -> {
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                    Pair<Set<String>, Integer> result =
                            (Pair<Set<String>, Integer>) statement.executeQuery(Operation.FREQUENT_VALUE_IN_FIELD, field, "most");
                    if (isNull(result)) {
                        reactPlatform.reply(context, messages.getString("NothingFound"));
                    } else {
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomMostFrequentValueInField"),
                                field, result.getValue()));
                        reactPlatform.reply(context, new ArrayList<>(result.getKey()).toString());
                    }
                })
                .next()
                .moveTo(returnState);

        this.processCustomMostFrequentValueInFieldState = processCustomMostFrequentValueInFieldState.getState();

        processCustomLeastFrequentValueInFieldState
                .body(context -> {
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                    Pair<Set<String>, Integer> result =
                            (Pair<Set<String>, Integer>) statement.executeQuery(Operation.FREQUENT_VALUE_IN_FIELD, field, "least");
                    if (isNull(result)) {
                        reactPlatform.reply(context, messages.getString("NothingFound"));
                    } else {
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomLeastFrequentValueInField"),
                                field, result.getValue()));
                        reactPlatform.reply(context, new ArrayList<>(result.getKey()).toString());
                    }
                })
                .next()
                .moveTo(returnState);

        this.processCustomLeastFrequentValueInFieldState = processCustomLeastFrequentValueInFieldState.getState();
    }
}
