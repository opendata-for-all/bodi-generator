package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.Operation;
import bodi.generator.dataSource.ResultSet;
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
import static com.xatkit.bot.Bot.getResult;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Field of Value workflow of a chatbot.
 * <p>
 * Given a field A and a value of a field B, this workflow gets the field A of all entries with field B equal to
 * the value, and shows them to the user. If there are more than 1 entries matched, the user can choose how to see
 * them (e.g. only the maximum value, for numeric fields, only unique values, etc.)
 * <p>
 * An example user input could be: {@code What is the salary of the city Barcelona?}
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomFieldOfValue {

    /**
     * The entry point for the Custom Field Of Value workflow.
     */
    @Getter
    private final State processCustomFieldOfValueState;

    /**
     * This variable stores the error condition of the workflow (i.e. if some parameter was not recognized properly)
     */
    private boolean error;

    /**
     * This variable stores the stop condition of the workflow (i.e. if we do not want to navigate to the next state)
     */
    private boolean stop;

    /**
     * This variable stores the {@code field} parameter recognized from the matched intent.
     */
    private String field;

    /**
     * This variable stores the {@code value} parameter recognized from the matched intent.
     */
    private String value;

    /**
     * This variable stores the field of the {@code value} parameter.
     */
    private String valueField;

    /**
     * Instantiates a new Custom Value Of Field workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFieldOfValue(ReactPlatform reactPlatform, State returnState) {
        val processCustomFieldOfValueState = state("ProcessCustomValueOfField");
        val customFieldOfValueShowAllState = state("CustomFieldOfValueShowAll");
        val customFieldOfValueOperatorState = state("CustomFieldOfValueOperator");

        processCustomFieldOfValueState
                .body(context -> {
                    error = false;
                    stop = false;
                    field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(field) && !isEmpty(value)) {
                        valueField = Entities.fieldValueMap.get(value);
                        Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                        String[] operationArgs = {field, value, valueField, ""};
                        ResultSet resultSet = (ResultSet) statement.executeQuery(Operation.FIELD_OF_VALUE, operationArgs);
                        operationArgs[3] = "distinct";
                        ResultSet resultSetDistinct = (ResultSet) statement.executeQuery(Operation.FIELD_OF_VALUE, operationArgs);
                        if (resultSet.getNumRows() == 0) {
                            reactPlatform.reply(context, MessageFormat.format(messages.getString(
                                            "FieldOfValue0"), field, valueField, value));
                            stop = true;
                        } else if (resultSet.getNumRows() == 1) {
                            String result = resultSet.getRow(0).getColumnValue(0);
                            reactPlatform.reply(context, MessageFormat.format(messages.getString(
                                    "FieldOfValue1"), field, valueField, value, result));
                            stop = true;
                        } else if (resultSet.getNumRows() > 1) {
                            List<String> buttons = new ArrayList<>();
                            buttons.add(Utils.getFirstTrainingSentences(Intents.showAllIntent).get(0));
                            buttons.add(Utils.getFirstTrainingSentences(Intents.showAllDistinctIntent).get(0));
                            if (Utils.getEntityValues(Entities.numericFieldEntity).contains(field)) {
                                buttons.addAll(Utils.getEntityValues(Entities.numericFunctionOperatorEntity));
                            } else if (Utils.getEntityValues(Entities.dateFieldEntity).contains(field)) {
                                buttons.addAll(Utils.getEntityValues(Entities.dateFunctionOperatorEntity));
                            } else if (Utils.getEntityValues(Entities.textualFieldEntity).contains(field)) {
                                // textual operators here
                            }
                            buttons.add(Utils.getFirstTrainingSentences(coreLibraryI18n.Quit).get(0));
                            reactPlatform.reply(context, MessageFormat.format(messages.getString(
                                    "AskFieldOfValueOperation"), resultSet.getNumRows(), field,
                                    resultSetDistinct.getNumRows(), valueField, value), buttons);
                        }
                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(getResult.getGenerateResultSetFromQueryState())
                .when(context -> stop).moveTo(returnState)
                .when(intentIs(Intents.showAllIntent)).moveTo(customFieldOfValueShowAllState)
                .when(intentIs(Intents.showAllDistinctIntent)).moveTo(customFieldOfValueShowAllState)
                .when(intentIs(Intents.numericFunctionOperatorIntent)).moveTo(customFieldOfValueOperatorState)
                .when(intentIs(Intents.dateFunctionOperatorIntent)).moveTo(customFieldOfValueOperatorState)
                .when(intentIs(coreLibraryI18n.Quit)).moveTo(returnState);

        customFieldOfValueShowAllState
                .body(context -> {
                    context.getSession().put(ContextKeys.OPERATION, Operation.FIELD_OF_VALUE);
                    String[] operationArgs = {field, value, valueField, ""};
                    if (context.getIntent().getDefinition().getName().equals(Intents.showAllDistinctIntent.getName())) {
                        operationArgs[3] = "distinct";
                    }
                    context.getSession().put(ContextKeys.OPERATION_ARGS, operationArgs);

                })
                .next()
                .moveTo(getResult.getGenerateResultSetWithOperationState());

        customFieldOfValueOperatorState
                .body(context -> {
                    String operator = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                    String[] operationArgs = {field, value, valueField, operator};
                    // Result is a Date or a Float
                    Object result = statement.executeQuery(Operation.FIELD_OF_VALUE, operationArgs);
                    reactPlatform.reply(context, MessageFormat.format(messages.getString(
                                    "FieldOfValueWithOperation"), operator, field, valueField, value, result));
                })
                .next()
                .moveTo(returnState);


        this.processCustomFieldOfValueState = processCustomFieldOfValueState.getState();

    }
}
