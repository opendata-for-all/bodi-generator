package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Utils;
import com.xatkit.bot.sql.SqlQueries;
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
     * Instantiates a new Custom Value Of Field workflow.
     *
     * @param bot           the chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFieldOfValue(Bot bot, State returnState) {
        val processCustomFieldOfValueState = state("ProcessCustomFieldOfValue");
        val processCustomFieldOfValueShowAllState = state("ProcessCustomFieldOfValueShowAll");
        val processCustomFieldOfValueOperatorState = state("ProcessCustomFieldOfValueOperator");

        processCustomFieldOfValueState
                .body(context -> {
                    context.getSession().put(ContextKeys.ERROR, false);
                    context.getSession().put(ContextKeys.STOP, false);
                    context.getSession().put(ContextKeys.CONTINUE, false);
                    String field = (String) context.getSession().get(ContextKeys.FIELD);
                    String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
                    String value = (String) context.getSession().get(ContextKeys.VALUE);
                    String valueField = null;
                    if (!isEmpty(value)) {
                        valueField = Entities.fieldValueMap.get(value);
                        context.getSession().put(ContextKeys.VALUE_FIELD, valueField);
                    }
                    if (context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customFieldOfValueIntent.getName())) {
                        // operator may have a value from another previous intent
                        operator = null;
                        context.getSession().put(ContextKeys.OPERATOR, operator);
                    }
                    if (!isEmpty(field) && !isEmpty(value) && !isEmpty(valueField)) {
                        if (isEmpty(operator)) {
                            String fieldRN = bot.entities.readableNames.get(field);
                            String valueFieldRN = bot.entities.readableNames.get(valueField);
                            SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                            String sqlQuery = sqlQueries.fieldOfValue(field, valueField, value, false);
                            ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                            if (resultSet.getNumRows() == 0
                                    || (resultSet.getNumRows() == 1 && isEmpty(resultSet.getRow(0).getColumnValue(0)))) {
                                bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(
                                        "FieldOfValue0"), fieldRN, valueFieldRN, value));
                                context.getSession().put(ContextKeys.STOP, true);
                            } else if (resultSet.getNumRows() == 1) {
                                String result = resultSet.getRow(0).getColumnValue(0);
                                bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(
                                        "FieldOfValue1"), fieldRN, valueFieldRN, value, result));
                                context.getSession().put(ContextKeys.STOP, true);
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
                                sqlQuery = sqlQueries.fieldOfValue(field, valueField, value, true);
                                ResultSet resultSetDistinct = sql.runSqlQuery(bot, sqlQuery);
                                buttons.add(Utils.getFirstTrainingSentences(bot.coreLibraryI18n.Quit).get(0));
                                bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(
                                                "AskFieldOfValueOperation"), resultSet.getNumRows(), fieldRN,
                                        resultSetDistinct.getNumRows(), valueFieldRN, value), buttons);
                            }
                        } else if (!isEmpty(operator)
                                && !(Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field))
                                && !(Utils.getEntityValues(bot.entities.dateFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.dateFieldEntity).contains(field))) {
                            // Check that operator type matches field type
                            context.getSession().put(ContextKeys.ERROR, true);
                        } else if (!isEmpty(operator)) {
                            context.getSession().put(ContextKeys.CONTINUE, true);
                        }
                    } else {
                        context.getSession().put(ContextKeys.ERROR, true);
                    }
                })
                .next()
                .when(context -> (boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> (boolean) context.getSession().get(ContextKeys.STOP)).moveTo(returnState)
                .when(context -> (boolean) context.getSession().get(ContextKeys.CONTINUE)).moveTo(processCustomFieldOfValueOperatorState)
                .when(intentIs(bot.intents.showAllIntent)).moveTo(processCustomFieldOfValueShowAllState)
                .when(intentIs(bot.intents.showAllDistinctIntent)).moveTo(processCustomFieldOfValueShowAllState)
                .when(intentIs(bot.intents.numericFunctionOperatorIntent)).moveTo(processCustomFieldOfValueOperatorState)
                .when(intentIs(bot.intents.dateFunctionOperatorIntent)).moveTo(processCustomFieldOfValueOperatorState)
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState);

        this.processCustomFieldOfValueState = processCustomFieldOfValueState.getState();

        processCustomFieldOfValueShowAllState
                .body(context -> {
                    boolean isDistinct = (context.getIntent().getDefinition().getName().equals(bot.intents.showAllDistinctIntent.getName()));
                    String field = (String) context.getSession().get(ContextKeys.FIELD);
                    String value = (String) context.getSession().get(ContextKeys.VALUE);
                    String valueField = (String) context.getSession().get(ContextKeys.VALUE_FIELD);
                    SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                    String sqlQuery = sqlQueries.fieldOfValue(field, valueField, value, isDistinct);
                    ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                    context.getSession().put(ContextKeys.RESULTSET, resultSet);
                })
                .next()
                .moveTo(bot.getResult.getShowDataState());

        processCustomFieldOfValueOperatorState
                .body(context -> {
                    String field = (String) context.getSession().get(ContextKeys.FIELD);
                    String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
                    String value = (String) context.getSession().get(ContextKeys.VALUE);
                    String valueField = (String) context.getSession().get(ContextKeys.VALUE_FIELD);
                    if (context.getIntent().getDefinition().getName().equals(bot.intents.numericFunctionOperatorIntent.getName())
                            || context.getIntent().getDefinition().getName().equals(bot.intents.dateFunctionOperatorIntent.getName())) {
                        operator = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    }
                    SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                    String sqlQuery = sqlQueries.fieldOfValueOperator(field, valueField, value, operator);
                    ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                    String result = resultSet.getRow(0).getColumnValue(0);
                    String fieldRN = bot.entities.readableNames.get(field);
                    String valueFieldRN = bot.entities.readableNames.get(valueField);
                    bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(
                                    "FieldOfValueWithOperation"), operator, fieldRN, valueFieldRN, value, result));
                })
                .next()
                .moveTo(returnState);
    }
}
