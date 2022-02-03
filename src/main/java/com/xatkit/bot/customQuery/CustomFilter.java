package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
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
     * The entry point for the Custom Filter workflow.
     */
    @Getter
    private final State saveFilterState;

    /**
     * Instantiates a new Custom Filter workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFilter(ReactPlatform reactPlatform, StateProvider returnState) {
        val saveFilterState = state("SaveFilter");

        saveFilterState
                .body(context -> {
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    String operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
                        Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                        statement.addFilter(field, operator, value);
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("FilterAdded"),
                                field, operator, value));
                    } else {
                        reactPlatform.reply(context, messages.getString("SomethingWentWrong"));
                    }
                })
                .next()
                .moveTo(returnState);

        this.saveFilterState = saveFilterState.getState();
    }
}
