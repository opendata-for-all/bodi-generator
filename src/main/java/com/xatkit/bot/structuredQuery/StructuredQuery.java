package com.xatkit.bot.structuredQuery;

import com.xatkit.bot.Bot;
import com.xatkit.bot.getResult.GetResult;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

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
     * The Reset Bot workflow.
     */
    public static ResetBot resetBot;


    /**
     * Instantiates a new Structured Query workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public StructuredQuery(Bot bot, State returnState) {
        structuredFilter = new StructuredFilter(bot, returnState);
        selectViewField = new SelectViewField(bot, returnState);
        resetBot = new ResetBot(bot, returnState);

        val awaitingStructuredQueryState = state("AwaitingStructuredQuery");

        awaitingStructuredQueryState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("SelectAction"),
                            Utils.getFirstTrainingSentences(
                                    bot.intents.addFilterIntent,
                                    bot.intents.removeFilterIntent,
                                    //bot.intents.addFieldToViewIntent,
                                    bot.intents.showDataIntent,
                                    bot.intents.resetIntent,
                                    bot.coreLibraryI18n.Quit));
                })
                .next()
                .when(intentIs(bot.intents.addFilterIntent)).moveTo(structuredFilter.getSelectFieldState())
                .when(intentIs(bot.intents.removeFilterIntent)).moveTo(structuredFilter.getSelectFilterToRemoveState())
                //.when(intentIs(bot.intents.addFieldToViewIntent)).moveTo(selectViewField.getSelectViewFieldState())
                .when(intentIs(bot.intents.showDataIntent)).moveTo(bot.getResult.getGenerateResultSetState())
                .when(intentIs(bot.intents.resetIntent)).moveTo(resetBot.getResetBotState())
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState);


        this.awaitingStructuredQueryState = awaitingStructuredQueryState.getState();
    }

}
