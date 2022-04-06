package com.xatkit.bot.getResult;

import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import static com.xatkit.bot.Bot.coreLibraryI18n;
import static com.xatkit.bot.Bot.messages;
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
     * Instantiates a new Check Correct Answer Function workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CheckCorrectAnswer(ReactPlatform reactPlatform, State returnState) {
        val processCheckCorrectAnswerState = state("ProcessCheckCorrectAnswer");

        processCheckCorrectAnswerState
                .body(context -> {
                    String lastSqlQuery = (String) context.getSession().get(ContextKeys.LAST_SQL_QUERY);
                    if (!isEmpty(lastSqlQuery)) {
                        reactPlatform.reply(context, messages.getString("ShowSQL"));
                        reactPlatform.reply(context, lastSqlQuery);
                    }
                    reactPlatform.reply(context, messages.getString("AskCorrectAnswer"),
                            Utils.getFirstTrainingSentences(
                                    coreLibraryI18n.Yes, coreLibraryI18n.No, Intents.iDontKnowIntent));
                    context.getSession().put(ContextKeys.LAST_SQL_QUERY, null);
                })
                .next()
                .when(intentIs(coreLibraryI18n.Yes)).moveTo(returnState)
                .when(intentIs(coreLibraryI18n.No)).moveTo(returnState)
                .when(intentIs(Intents.iDontKnowIntent)).moveTo(returnState);

        this.processCheckCorrectAnswerState = processCheckCorrectAnswerState.getState();
    }
}
