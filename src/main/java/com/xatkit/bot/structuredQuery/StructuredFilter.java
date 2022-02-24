package com.xatkit.bot.structuredQuery;

import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.xatkit.bot.Bot.coreLibraryI18n;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Structured Filter workflow of a chatbot.
 * <p>
 * It recognizes a filter query and processes the given filter, storing it in the chatbot memory.
 * <p>
 * This process is done in steps. First, the user inputs the field that wants to be filtered. Second, he/she inputs
 * the operator of the filter. Finally, he/she inputs the value of the filter operation.
 * <p>
 * This workflow is helpful in cases where there is no knowledge about, for instance, the data type of some field,
 * the available operators for some data type or the field names.
 */
public class StructuredFilter {

    /**
     * The entry point for the Structured Filter workflow.
     */
    @Getter
    private final State selectFieldState;

    /**
     * Instantiates a new Structured Filter workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public StructuredFilter(ReactPlatform reactPlatform, State returnState) {
        val selectFieldState = state("SelectField");
        val saveFieldState = state("SaveField");

        val writeOperator = state("WriteOperator");
        val saveOperator = state("SaveOperator");

        val writeDateValue = state("WriteDateValue");
        val writeTextualValue = state("WriteTextualValue");
        val writeNumericValue = state("WriteNumericValue");

        val saveFilterState = state("saveFilter");

        // Used to store the field data type when selecting the field. Later, in SaveOperator, it is used to
        // transition to the datatype-dependant proper state.
        final String[] fieldIntentName = new String[1];

        // Input the FIELD name

        selectFieldState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("SelectField"),
                            (List<String>) context.getSession().get(ContextKeys.FILTER_FIELD_OPTIONS));
                })
                .next()
                .when(intentIs(Intents.numericFieldIntent)).moveTo(saveFieldState)
                .when(intentIs(Intents.textualFieldIntent)).moveTo(saveFieldState)
                .when(intentIs(Intents.dateFieldIntent)).moveTo(saveFieldState);

        // Save the FIELD name

        saveFieldState
                .body(context -> {
                    String fieldName = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    context.getSession().put(ContextKeys.LAST_FIELD, fieldName);
                    fieldIntentName[0] = context.getIntent().getDefinition().getName();
                })
                .next()
                .moveTo(writeOperator);

        // Input the OPERATOR name

        writeOperator
                .body(context -> {
                    List<String> operators = new ArrayList<>();
                    if (fieldIntentName[0].equals(Intents.textualFieldIntent.getName())) {
                        operators = Utils.getEntityValues(Entities.textualOperatorEntity);
                    } else if (fieldIntentName[0].equals(Intents.numericFieldIntent.getName())) {
                        operators = Utils.getEntityValues(Entities.numericOperatorEntity);
                    } else if (fieldIntentName[0].equals(Intents.dateFieldIntent.getName())) {
                        operators = Utils.getEntityValues(Entities.dateOperatorEntity);
                    }
                    reactPlatform.reply(context, messages.getString("SelectOperator"), operators);
                })
                .next()
                .when(intentIs(Intents.textualOperatorIntent)).moveTo(saveOperator)
                .when(intentIs(Intents.numericOperatorIntent)).moveTo(saveOperator)
                .when(intentIs(Intents.dateOperatorIntent)).moveTo(saveOperator);

        // Save the OPERATOR name

        saveOperator
                .body(context -> {
                    String operatorName = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    context.getSession().put(ContextKeys.LAST_OPERATOR, operatorName);
                })
                .next()
                .when(context -> fieldIntentName[0].equals(Intents.textualFieldIntent.getName())).moveTo(writeTextualValue)
                .when(context -> fieldIntentName[0].equals(Intents.numericFieldIntent.getName())).moveTo(writeNumericValue)
                .when(context -> fieldIntentName[0].equals(Intents.dateFieldIntent.getName())).moveTo(writeDateValue);

        // Input the VALUE
        // Divided by data types for safety (e.g. a date may be recognized as a text if we don't separate data types)

        writeTextualValue
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("WriteTextualValue"));
                })
                .next()
                .when(intentIs(coreLibraryI18n.AnyValue)).moveTo(saveFilterState);

        writeNumericValue
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("WriteNumericValue"));
                })
                .next()
                .when(intentIs(coreLibraryI18n.NumberValue)).moveTo(saveFilterState);

        writeDateValue
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("WriteDateValue"));
                })
                .next()
                .when(intentIs(coreLibraryI18n.DateValue)).moveTo(saveFilterState);

        // Finally, save the filter, composed by a FIELD, an OPERATOR, and a VALUE

        saveFilterState
                .body(context -> {
                    String fieldName = (String) context.getSession().get(ContextKeys.LAST_FIELD);
                    String operatorName = (String) context.getSession().get(ContextKeys.LAST_OPERATOR);
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);

                    if (!isEmpty(value)) {
                        Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                        statement.addFilter(fieldName, operatorName, value);
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("FilterAdded"),
                                fieldName, operatorName, value));
                    }
                })
                .next()
                .moveTo(returnState);

        this.selectFieldState = selectFieldState.getState();
    }
}
