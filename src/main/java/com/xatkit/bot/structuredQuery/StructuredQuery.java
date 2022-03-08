package com.xatkit.bot.structuredQuery;

import com.xatkit.bot.getResult.GetResult;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import static com.xatkit.bot.Bot.coreLibraryI18n;
import static com.xatkit.bot.Bot.getResult;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;

/**
 * The Structured Query workflow of a chatbot.
 * <p>
 * It is the entry point or lobby of a set of workflows considered as structured. The workflows available from here are:
 * <ul>
 *     <li>{@link StructuredFilter}</li>
 *     <li>{@link SelectViewField}</li>
 *     <li>{@link GetResult}</li>
 * </ul>
 */
public class StructuredQuery {

    /**
     * The entry point for the Structured Query workflow.
     */
    @Getter
    private final State awaitingStructuredQueryState;

    /**
     * The Structured Filter workflow.
     */
    public static StructuredFilter structuredFilter;

    /**
     * The Select View Field workflow.
     */
    public static SelectViewField selectViewField;


    /**
     * Instantiates a new Structured Query workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public StructuredQuery(ReactPlatform reactPlatform, State returnState) {
        structuredFilter = new StructuredFilter(reactPlatform, returnState);
        selectViewField = new SelectViewField(reactPlatform, returnState);

        val awaitingStructuredQueryState = state("AwaitingStructuredQuery");

        awaitingStructuredQueryState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("SelectAction"),
                            Utils.getFirstTrainingSentences(
                                    Intents.addFilterIntent,
                                    Intents.removeFilterIntent,
                                    Intents.addFieldToViewIntent,
                                    Intents.showDataIntent,
                                    coreLibraryI18n.Quit));
                })
                .next()
                .when(intentIs(Intents.addFilterIntent)).moveTo(structuredFilter.getSelectFieldState())
                .when(intentIs(Intents.removeFilterIntent)).moveTo(structuredFilter.getSelectFilterToRemoveState())
                .when(intentIs(Intents.addFieldToViewIntent)).moveTo(selectViewField.getSelectViewFieldState())
                .when(intentIs(Intents.showDataIntent)).moveTo(getResult.getGenerateResultSetState())
                .when(intentIs(coreLibraryI18n.Quit)).moveTo(returnState);


        this.awaitingStructuredQueryState = awaitingStructuredQueryState.getState();
    }

}
