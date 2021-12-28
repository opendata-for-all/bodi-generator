package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.Bot.messages;
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
     * The entry point for the Custom Filter workflow (with numeric filters).
     */
    @Getter
    private final State saveNumericFilterState;

    /**
     * The entry point for the Custom Filter workflow (with textual filters).
     */
    @Getter
    private final State saveTextualFilterState;

    /**
     * The entry point for the Custom Filter workflow (with date filters).
     */
    @Getter
    private final State saveDateFilterState;

    /**
     * Extracts a filter from a chatbot context (i.e. the parts of a filter recognized from a user utterance) and
     * stores it in the proper format within the chatbot. Then, a message is written to the user saying if the filter
     * was properly added or an error happened.
     *
     * @param context       the context of a chatbot
     * @param reactPlatform the react platform of a chatbot
     * @param fieldKey      the key of a {@code field} parameter stored in a chatbot
     * @param operatorKey   the key of an {@code operator} parameter stored in a chatbot
     * @param valueKey      the key of a {@code value} parameter stored in a chatbot
     */
    private static void saveFilterAndReply(StateContext context, ReactPlatform reactPlatform, String fieldKey,
                                           String operatorKey, String valueKey) {
        String field = (String) context.getIntent().getValue(fieldKey);
        String operator = (String) context.getIntent().getValue(operatorKey);
        String value = (String) context.getIntent().getValue(valueKey);
        if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
            Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
            statement.addFilter(field, operator, value);
            reactPlatform.reply(context, MessageFormat.format(messages.getString("FilterAdded"),
                    field, operator, value));
        } else {
            reactPlatform.reply(context, messages.getString("SomethingWentWrong"));
        }
    }

    /**
     * Instantiates a new Custom Filter workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFilter(ReactPlatform reactPlatform, StateProvider returnState) {
        //These states could be one, but for safety they are separated and the parameters are type-dependant
        val saveNumericFilterState = state("SaveCustomFilter");
        val saveTextualFilterState = state("SaveTextualFilter");
        val saveDateFilterState = state("SaveDateFilter");

        saveNumericFilterState
                .body(context -> {
                    saveFilterAndReply(context, reactPlatform, ContextKeys.NUMERIC_FIELD_NAME,
                            ContextKeys.NUMERIC_OPERATOR_NAME, ContextKeys.NUMERIC_VALUE);
                })
                .next()
                .moveTo(returnState);

        this.saveNumericFilterState = saveNumericFilterState.getState();

        saveTextualFilterState
                .body(context -> {
                    saveFilterAndReply(context, reactPlatform, ContextKeys.TEXTUAL_FIELD_NAME,
                            ContextKeys.TEXTUAL_OPERATOR_NAME, ContextKeys.TEXTUAL_VALUE);
                })
                .next()
                .moveTo(returnState);

        this.saveTextualFilterState = saveTextualFilterState.getState();

        saveDateFilterState
                .body(context -> {
                    saveFilterAndReply(context, reactPlatform, ContextKeys.DATE_FIELD_NAME,
                            ContextKeys.DATE_OPERATOR_NAME, ContextKeys.DATE_VALUE);
                })
                .next()
                .moveTo(returnState);

        this.saveDateFilterState = saveDateFilterState.getState();
    }
}
