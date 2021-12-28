package com.xatkit.bot.structuredQuery;

import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
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
    private final State selectFilterFieldState;

    /**
     * Instantiates a new Structured Filter workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public StructuredFilter(ReactPlatform reactPlatform, StateProvider returnState) {
        val selectFilterFieldState = state("SelectFilterField");
        val selectOperatorNameState = state("SelectOperatorName");
        val saveOperatorType = state("SaveOperatorType");
        val writeDateValue = state("WriteDateValue");
        val writeTextualValue = state("WriteTextualValue");
        val writeNumericValue = state("WriteNumericValue");
        val saveFilterState = state("saveFilterState");
        final String[] expectedValueIntent = new String[1];

        selectFilterFieldState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("SelectField"),
                            (List<String>) context.getSession().get(ContextKeys.FILTER_FIELD_OPTIONS));
                })
                .next()
                .when(intentIs(Intents.fieldNameIntent)).moveTo(selectOperatorNameState);

        selectOperatorNameState
                .body(context -> {
                    String textualFieldName = (String) context.getIntent().getValue(ContextKeys.TEXTUAL_FIELD_NAME);
                    String numericFieldName = (String) context.getIntent().getValue(ContextKeys.NUMERIC_FIELD_NAME);
                    String dateFieldName = (String) context.getIntent().getValue(ContextKeys.DATE_FIELD_NAME);
                    if (!isEmpty(textualFieldName)) {
                        context.getSession().put(ContextKeys.LAST_FIELD_NAME, textualFieldName);
                        reactPlatform.reply(context, messages.getString("SelectOperator"),
                                Utils.getEntityValues(Entities.textualOperatorEntity));
                    } else if (!isEmpty(numericFieldName)) {
                        context.getSession().put(ContextKeys.LAST_FIELD_NAME, numericFieldName);
                        reactPlatform.reply(context, messages.getString("SelectOperator"),
                                Utils.getEntityValues(Entities.numericOperatorEntity));
                    } else if (!isEmpty(dateFieldName)) {
                        context.getSession().put(ContextKeys.LAST_FIELD_NAME, dateFieldName);
                        reactPlatform.reply(context, messages.getString("SelectOperator"),
                                Utils.getEntityValues(Entities.dateOperatorEntity));
                    }
                })
                .next()
                .when(intentIs(Intents.operatorNameIntent)).moveTo(saveOperatorType);

        saveOperatorType
                .body(context -> {
                    String textualOperatorName =
                            (String) context.getIntent().getValue(ContextKeys.TEXTUAL_OPERATOR_NAME);
                    String numericOperatorName =
                            (String) context.getIntent().getValue(ContextKeys.NUMERIC_OPERATOR_NAME);
                    String dateOperatorName =
                            (String) context.getIntent().getValue(ContextKeys.DATE_OPERATOR_NAME);
                    if (!isEmpty(textualOperatorName)) {
                        context.getSession().put(ContextKeys.LAST_OPERATOR_NAME, textualOperatorName);
                        expectedValueIntent[0] = "textual";
                    } else if (!isEmpty(numericOperatorName)) {
                        context.getSession().put(ContextKeys.LAST_OPERATOR_NAME, numericOperatorName);
                        expectedValueIntent[0] = "numeric";
                    } else if (!isEmpty(dateOperatorName)) {
                        context.getSession().put(ContextKeys.LAST_OPERATOR_NAME, dateOperatorName);
                        expectedValueIntent[0] = "date";
                    }
                })
                .next()
                .when(context -> expectedValueIntent[0].equals("textual")).moveTo(writeTextualValue)
                .when(context -> expectedValueIntent[0].equals("numeric")).moveTo(writeNumericValue)
                .when(context -> expectedValueIntent[0].equals("date")).moveTo(writeDateValue);

        writeTextualValue
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("WriteTextualValue"));
                })
                .next()
                .when(intentIs(Intents.textualValueIntent)).moveTo(saveFilterState);

        writeNumericValue
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("WriteNumericValue"));
                })
                .next()
                .when(intentIs(Intents.numericValueIntent)).moveTo(saveFilterState);

        writeDateValue
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("WriteDateValue"));
                })
                .next()
                .when(intentIs(Intents.dateValueIntent)).moveTo(saveFilterState);

        saveFilterState
                .body(context -> {
                    String fieldName = (String) context.getSession().get(ContextKeys.LAST_FIELD_NAME);
                    String operatorName = (String) context.getSession().get(ContextKeys.LAST_OPERATOR_NAME);
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

        this.selectFilterFieldState = selectFilterFieldState.getState();
    }
}
