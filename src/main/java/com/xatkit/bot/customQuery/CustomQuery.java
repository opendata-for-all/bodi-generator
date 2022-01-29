package com.xatkit.bot.customQuery;

import com.xatkit.bot.library.Intents;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import static com.xatkit.bot.Bot.coreLibraryI18n;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;

/**
 * The Custom Query workflow of a chatbot.
 * <p>
 * It allows the chatbot to recognize a "somehow free" query. The currently available kinds of query allowed through
 * this workflow are:
 * <ul>
 *     <li>{@link CustomFilter}</li>
 * </ul>
 */
public class CustomQuery {

    /**
     * The entry point for the Custom Query workflow.
     */
    @Getter
    private final State awaitingCustomQueryState;

    /**
     * Instantiates a new Custom Query workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomQuery(ReactPlatform reactPlatform, StateProvider returnState) {
        CustomFilter customFilter = new CustomFilter(reactPlatform, returnState);
        SQLQuery sqlQuery = new SQLQuery(reactPlatform, returnState);

        val awaitingCustomQueryState = state("AwaitingCustomQuery");

        awaitingCustomQueryState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("WriteYourQuery"));
                })
                .next()
                .when(intentIs(Intents.customNumericFilterIntent)).moveTo(customFilter.getSaveNumericFilterState())
                .when(intentIs(Intents.customDateFilterIntent)).moveTo(customFilter.getSaveDateFilterState())
                .when(intentIs(Intents.customTextualFilterIntent)).moveTo(customFilter.getSaveTextualFilterState())
                .when(intentIs(coreLibraryI18n.Quit)).moveTo(returnState)
                .when(intentIs(coreLibraryI18n.AnyValue)).moveTo(sqlQuery.getProcessQuery());

        this.awaitingCustomQueryState = awaitingCustomQueryState.getState();
    }
}
