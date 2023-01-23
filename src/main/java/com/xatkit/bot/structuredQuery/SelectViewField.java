package com.xatkit.bot.structuredQuery;

import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.List;

import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;

/**
 * The Select View Field workflow of a chatbot.
 * <p>
 * It allows the user to select the fields (columns) of the chatbot data table that he/she wants to be displayed
 * (i.e. to hide undesired columns from a table view).
 */
public class SelectViewField {

    /**
     * The entry point for the Select View Field workflow.
     */
    @Getter
    private final State selectViewFieldState;

    /**
     * Instantiates a new Select View Field workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public SelectViewField(Bot bot, State returnState) {
        val selectViewFieldState = state("SelectViewField");
        val saveViewFieldState = state("SaveViewField");

        selectViewFieldState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("SelectField"),
                            (List<String>) context.getSession().get(ContextKeys.VIEW_FIELD_OPTIONS));
                })
                .next()
                .when(intentIs(bot.intents.textualFieldIntent)).moveTo(saveViewFieldState)
                .when(intentIs(bot.intents.numericFieldIntent)).moveTo(saveViewFieldState)
                .when(intentIs(bot.intents.datetimeFieldIntent)).moveTo(saveViewFieldState);

        saveViewFieldState
                .body(context -> {
                    String fieldName = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    // sqlQueries.addField(fieldName);
                    List<String> viewFieldOptions =
                            (List<String>) context.getSession().get(ContextKeys.VIEW_FIELD_OPTIONS);
                    viewFieldOptions.remove(fieldName);
                    bot.reactPlatform.reply(context,
                            MessageFormat.format(bot.messages.getString("FieldAddedToView"), fieldName));
                })
                .next()
                .moveTo(returnState);

        this.selectViewFieldState = selectViewFieldState.getState();
    }
}
