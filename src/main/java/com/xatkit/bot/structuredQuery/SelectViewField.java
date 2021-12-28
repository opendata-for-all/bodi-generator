package com.xatkit.bot.structuredQuery;

import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Intents;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.List;

import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

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
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public SelectViewField(ReactPlatform reactPlatform, StateProvider returnState) {
        val selectViewFieldState = state("SelectViewField");
        val saveViewFieldState = state("SaveViewField");

        selectViewFieldState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("SelectField"),
                            (List<String>) context.getSession().get(ContextKeys.VIEW_FIELD_OPTIONS));
                })
                .next()
                .when(intentIs(Intents.fieldNameIntent)).moveTo(saveViewFieldState);

        saveViewFieldState
                .body(context -> {
                    String textualFieldName = (String) context.getIntent().getValue(ContextKeys.TEXTUAL_FIELD_NAME);
                    String numericFieldName = (String) context.getIntent().getValue(ContextKeys.NUMERIC_FIELD_NAME);
                    String dateFieldName = (String) context.getIntent().getValue(ContextKeys.DATE_FIELD_NAME);
                    String fieldName = null;
                    fieldName = (!isEmpty(textualFieldName) ? textualFieldName : fieldName);
                    fieldName = (!isEmpty(numericFieldName) ? numericFieldName : fieldName);
                    fieldName = (!isEmpty(dateFieldName) ? dateFieldName : fieldName);
                    Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                    statement.addField(fieldName);
                    List<String> viewFieldOptions =
                            (List<String>) context.getSession().get(ContextKeys.VIEW_FIELD_OPTIONS);
                    viewFieldOptions.remove(fieldName);
                    reactPlatform.reply(context,
                            MessageFormat.format(messages.getString("FieldAddedToView"), fieldName));
                })
                .next()
                .moveTo(returnState);

        this.selectViewFieldState = selectViewFieldState.getState();
    }
}
