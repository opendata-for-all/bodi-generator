package com.xatkit.bot.structuredQuery;

import bodiGenerator.dataSource.Statement;
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

public class SelectViewField {

    @Getter
    private State selectViewFieldState;

    public SelectViewField(ReactPlatform reactPlatform, StateProvider returnState) {
        val selectViewFieldState = state("SelectViewField");
        val saveViewFieldState = state("SaveViewField");

        selectViewFieldState
                .body(context -> {
                            reactPlatform.reply(context, messages.getString("SelectField"),
                                    (List<String>) context.getSession().get(ContextKeys.viewFieldOptions));
                        }
                )
                .next()
                .when(intentIs(Intents.fieldNameIntent)).moveTo(saveViewFieldState)
        ;
        saveViewFieldState
                .body(context -> {
                            String textualFieldName = (String) context.getIntent().getValue(ContextKeys.textualFieldName);
                            String numericFieldName = (String) context.getIntent().getValue(ContextKeys.numericFieldName);
                            String fieldName = (!isEmpty(textualFieldName) ? textualFieldName : numericFieldName);
                            Statement statement = (Statement) context.getSession().get(ContextKeys.statement);
                            statement.addField(fieldName);
                            List<String> viewFieldOptions = (List<String>) context.getSession().get(ContextKeys.viewFieldOptions);
                            viewFieldOptions.remove(fieldName);
                            reactPlatform.reply(context,
                                    MessageFormat.format(messages.getString("FieldAddedToView"), fieldName));
                        }
                )
                .next()
                .moveTo(returnState)
                ;
        this.selectViewFieldState = selectViewFieldState.getState();
    }
}
