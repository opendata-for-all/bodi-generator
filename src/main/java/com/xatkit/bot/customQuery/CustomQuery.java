package com.xatkit.bot.customQuery;

import com.xatkit.bot.library.Intents;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;

public class CustomQuery {

    @Getter
    private State awaitingCustomQueryState;

    public CustomQuery(ReactPlatform reactPlatform, StateProvider returnState) {
        CustomFilter customFilter = new CustomFilter(reactPlatform, returnState);
        val awaitingCustomQueryState = state("AwaitingCustomQuery");

        awaitingCustomQueryState
                .body(context -> {
                            reactPlatform.reply(context, messages.getString("WriteYourQuery"));
                        }
                )
                .next()
                .when(intentIs(Intents.customFilterIntent)).moveTo(customFilter.getSaveCustomFilterState())
        ;
        this.awaitingCustomQueryState = awaitingCustomQueryState.getState();
    }
}
