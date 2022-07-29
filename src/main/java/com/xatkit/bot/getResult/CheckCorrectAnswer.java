package com.xatkit.bot.getResult;

import com.xatkit.bot.Bot;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Check Correct Answer workflow of a chatbot.
 * <p>
 * This is used to ask the user if the answer provided by the bot (in some previously executed workflow) was correct
 * or not. So an example usage of this workflow is to set its entry point state as the return state of another workflow.
 */
public class CheckCorrectAnswer {

    /**
     * The entry point for the Check Correct Answer workflow.
     */
    @Getter
    private final State processCheckCorrectAnswerState;

    /**
     * This variable stores the last SQL query used by the bot to obtain an answer.
     */
    @Setter
    private String lastSqlQuery;

    /**
     * Instantiates a new Check Correct Answer Function workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CheckCorrectAnswer(Bot bot, State returnState) {
        val processCheckCorrectAnswerState = state("ProcessCheckCorrectAnswer");

        processCheckCorrectAnswerState
                .body(context -> {
                    if (!isEmpty(lastSqlQuery)) {
                        bot.reactPlatform.reply(context, bot.messages.getString("ShowSQL"));
                        bot.reactPlatform.reply(context, lastSqlQuery);
                    }
                    bot.reactPlatform.reply(context, bot.messages.getString("AskCorrectAnswer"),
                            Utils.getFirstTrainingSentences(
                                    bot.coreLibraryI18n.Yes, bot.coreLibraryI18n.No, bot.intents.iDontKnowIntent));
                    lastSqlQuery = null;
                })
                .next()
                .when(intentIs(bot.coreLibraryI18n.Yes)).moveTo(returnState)
                .when(intentIs(bot.coreLibraryI18n.No)).moveTo(returnState)
                .when(intentIs(bot.intents.iDontKnowIntent)).moveTo(returnState);

        this.processCheckCorrectAnswerState = processCheckCorrectAnswerState.getState();
    }
}
