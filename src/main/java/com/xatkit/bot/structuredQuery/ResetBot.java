package com.xatkit.bot.structuredQuery;

import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

import static com.xatkit.dsl.DSL.state;

/**
 * The Reset Bot workflow of a chatbot.
 * <p>
 * It resets the conversation parameters that could have been set by the user, such as filters.
 */
public class ResetBot {

    /**
     * The entry point for the Reset Bot workflow.
     */
    @Getter
    private final State resetBotState;

    /**
     * Instantiates a new Reset Bot workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public ResetBot(Bot bot, State returnState) {
        val resetBotState = state("ResetBot");

        resetBotState
                .body(context -> {
                    bot.sqlQueries.clearFilters();
                    List<String> fields = Utils.getEntityValues(bot.entities.fieldEntity);
                    List<String> filterFieldOptions = new ArrayList<>(fields);
                    context.getSession().put(ContextKeys.FILTER_FIELD_OPTIONS, filterFieldOptions);
                    List<String> viewFieldOptions = new ArrayList<>(fields);
                    context.getSession().put(ContextKeys.VIEW_FIELD_OPTIONS, viewFieldOptions);
                })
                .next()
                .moveTo(returnState);

        this.resetBotState = resetBotState.getState();
    }
}
