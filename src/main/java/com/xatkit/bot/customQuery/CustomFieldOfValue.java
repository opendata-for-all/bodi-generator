package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.xatkit.bot.App.sql;
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
     * This variable stores the {@code operator} parameter recognized from the matched intent.
     */
    private String operator;

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
     * @param bot           the chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFieldOfValue(Bot bot, State returnState) {
        val processCustomFieldOfValueState = state("ProcessCustomValueOfField");
        val processCustomFieldOfValueShowAllState = state("ProcessCustomFieldOfValueShowAll");
        val processCustomFieldOfValueOperatorState = state("ProcessCustomFieldOfValueOperator");

        processCustomFieldOfValueState
                .body(context -> {
                    error = false;
                    stop = false;
                    field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(value)) {
                        valueField = bot.entities.fieldValueMap.get(value);
                    }
                    if (!isEmpty(field) && !isEmpty(value) && isEmpty(operator)) {
                        String sqlQuery = bot.sqlQueries.fieldOfValue(field, valueField, value, false);
                        ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                        if (resultSet.getNumRows() == 0
                                || (resultSet.getNumRows() == 1 && isEmpty(resultSet.getRow(0).getColumnValue(0)))) {
                            bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(
                                            "FieldOfValue0"), field, valueField, value));
                            stop = true;
                        } else if (resultSet.getNumRows() == 1) {
                            String result = resultSet.getRow(0).getColumnValue(0);
                            bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(
                                    "FieldOfValue1"), field, valueField, value, result));
                            stop = true;
                        } else if (resultSet.getNumRows() > 1) {
                            List<String> buttons = new ArrayList<>();
                            buttons.add(Utils.getFirstTrainingSentences(bot.intents.showAllIntent).get(0));
                            buttons.add(Utils.getFirstTrainingSentences(bot.intents.showAllDistinctIntent).get(0));
                            if (Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field)) {
                                buttons.addAll(Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity));
                            } else if (Utils.getEntityValues(bot.entities.dateFieldEntity).contains(field)) {
                                buttons.addAll(Utils.getEntityValues(bot.entities.dateFunctionOperatorEntity));
                            } else if (Utils.getEntityValues(bot.entities.textualFieldEntity).contains(field)) {
                                // textual operators here
                            }
                            sqlQuery = bot.sqlQueries.fieldOfValue(field, valueField, value, true);
                            ResultSet resultSetDistinct = sql.runSqlQuery(sqlQuery);
                            buttons.add(Utils.getFirstTrainingSentences(bot.coreLibraryI18n.Quit).get(0));
                            bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(
                                    "AskFieldOfValueOperation"), resultSet.getNumRows(), field,
                                    resultSetDistinct.getNumRows(), valueField, value), buttons);
                        }
                    } else if (!isEmpty(operator)) {
                        // Check that operator type matches field type
                        if (!(Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field))
                                &&
                                !(Utils.getEntityValues(bot.entities.dateFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.dateFieldEntity).contains(field))) {
                            error = true;
                        }
                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> stop).moveTo(returnState)
                .when(context -> !error && !isEmpty(operator)).moveTo(processCustomFieldOfValueOperatorState)
                .when(intentIs(bot.intents.showAllIntent)).moveTo(processCustomFieldOfValueShowAllState)
                .when(intentIs(bot.intents.showAllDistinctIntent)).moveTo(processCustomFieldOfValueShowAllState)
                .when(intentIs(bot.intents.numericFunctionOperatorIntent)).moveTo(processCustomFieldOfValueOperatorState)
                .when(intentIs(bot.intents.dateFunctionOperatorIntent)).moveTo(processCustomFieldOfValueOperatorState)
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState);

        this.processCustomFieldOfValueState = processCustomFieldOfValueState.getState();

        processCustomFieldOfValueShowAllState
                .body(context -> {
                    boolean isDistinct = (context.getIntent().getDefinition().getName().equals(bot.intents.showAllDistinctIntent.getName()));
                    String sqlQuery = bot.sqlQueries.fieldOfValue(field, valueField, value, isDistinct);
                    ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                    bot.getResult.setResultSet(resultSet);

                })
                .next()
                .moveTo(bot.getResult.getShowDataState());

        processCustomFieldOfValueOperatorState
                .body(context -> {
                    if (!context.getIntent().getDefinition().getName().equals(bot.intents.customFieldOfValueOperatorIntent.getName())) {
                        operator = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    }
                    String sqlQuery = bot.sqlQueries.fieldOfValueOperator(field, valueField, value, operator);
                    ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                    String result = resultSet.getRow(0).getColumnValue(0);
                    bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(
                                    "FieldOfValueWithOperation"), operator, field, valueField, value, result));
                })
                .next()
                .moveTo(returnState);
    }
}
