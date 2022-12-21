package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.google.common.collect.Streams;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Utils;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;
import lombok.val;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class CustomFieldOfValue extends AbstractCustomQuery {

    public CustomFieldOfValue(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        if (!isEmpty(operator) && !operator.equals("all") && !operator.equals("distinct")
                && !(Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field))
                && !(Utils.getEntityValues(bot.entities.dateFunctionOperatorEntity).contains(operator) && Utils.getEntityValues(bot.entities.dateFieldEntity).contains(field))) {
            // Check that operator type matches field type
            return false;
        }
        return !isEmpty(field) && (!isEmpty(value1) || !isEmpty(value2));
    }

    @Override
    protected boolean continueWhenParamsNotOk(StateContext context) {
        // when params are not ok, we stop the execution
        return false;
    }

    @Override
    protected State getNextStateWhenParamsNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }

    protected String generateSqlStatement(StateContext context) {
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        String value1Field = Entities.fieldValueMap.get(value1);
        String value2Field = Entities.fieldValueMap.get(value2);
        Map<String, String> valueFieldMap = new HashMap<>();
        if (!isEmpty(value1) && !isEmpty(value1Field)) {
            valueFieldMap.put(value1, value1Field);
        }
        if (!isEmpty(value2) && !isEmpty(value2Field)) {
            valueFieldMap.put(value2, value2Field);
        }
        context.getSession().put(ContextKeys.VALUE_FIELD_MAP, valueFieldMap);
        List<String> keyFields = new ArrayList<>(bot.entities.keyFields);
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        if (isEmpty(operator) || operator.equals("all")) {
            return sqlQueries.fieldOfValue(keyFields, field, valueFieldMap, false);
        } else if (operator.equals("distinct")) {
            return sqlQueries.fieldOfValue(keyFields, field, valueFieldMap, true);
        } else {
            return sqlQueries.fieldOfValueOperator(keyFields, field, valueFieldMap, operator);
        }
    }

    @Override
    protected boolean checkResultSetOk(StateContext context) {
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        if (isEmpty(operator) && resultSet.getNumRows() > 1) {
            // We should ask the user to apply an operator
            return false;
        }
        if (!isEmpty(operator) && !operator.equals("all") && !operator.equals("distinct") && resultSet.getNumRows() > 1) {
            // There should only be 1 row, but there are more
            return false;
        }
        return true;
    }

    @Override
    protected boolean continueWhenResultSetNotOk(StateContext context) {
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        if (isEmpty(operator) && resultSet.getNumRows() > 1) {
            // We ask the operator to the user
            return true;
        }
        return false;
    }

    @Override
    protected String generateMessage(StateContext context) {
        String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
        if (operator.equals("all") || operator.equals("distinct")) {
            return null;
        }
        String field = (String) context.getSession().get(ContextKeys.FIELD);
        String fieldRN = bot.entities.readableNames.get(field);
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        Map<String, String> valueFieldMap = (Map<String, String>) context.getSession().get(ContextKeys.VALUE_FIELD_MAP);
        String conditions = String.join(", ", Streams.zip(valueFieldMap.keySet().stream(),
                valueFieldMap.values().stream(), (v, f) -> bot.entities.readableNames.get(f) + " " + "= " + v).collect(Collectors.toList()));
        if (resultSet.getNumRows() == 0 || (resultSet.getNumRows() == 1 && isEmpty(resultSet.getRow(0).getColumnValue(0)))) {
            return MessageFormat.format(bot.messages.getString("FieldOfValue0"), fieldRN, conditions);
        } else {
            String result = resultSet.getRow(0).getColumnValue(resultSet.getHeader().indexOf(fieldRN));
            if (!isEmpty(operator)) {
                return MessageFormat.format(bot.messages.getString("FieldOfValueWithOperation"), operator, fieldRN, conditions, result);
            } else if (resultSet.getNumRows() == 1) {
                return MessageFormat.format(bot.messages.getString("FieldOfValue1"), fieldRN, conditions, result);
            }
        }
        // When resultSet.getNumRows() > 1, there is no message
        return null;
    }

    @Override
    protected State getNextStateWhenResultSetNotOk() {
        val askOperator = state(this.getClass().getSimpleName() + "AskOperator");
        val saveOperator = state(this.getClass().getSimpleName() + "SaveOperator");
        askOperator
                .body(context -> {
                    String field = (String) context.getSession().get(ContextKeys.FIELD);
                    String fieldRN = bot.entities.readableNames.get(field);
                    SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                    ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
                    Map<String, String> valueFieldMap = (Map<String, String>) context.getSession().get(ContextKeys.VALUE_FIELD_MAP);
                    List<String> keyFields = new ArrayList<>(bot.entities.keyFields);
                    String conditions = String.join(", ", Streams.zip(valueFieldMap.keySet().stream(),
                            valueFieldMap.values().stream(), (v, f) -> bot.entities.readableNames.get(f) + " " + "= " + v).collect(Collectors.toList()));

                    List<String> buttons = new ArrayList<>();
                    buttons.add(Utils.getFirstTrainingSentences(bot.intents.showAllIntent).get(0));
                    buttons.add(Utils.getFirstTrainingSentences(bot.intents.showAllDistinctIntent).get(0));
                    if (Utils.getEntityValues(bot.entities.numericFieldEntity).contains(field)) {
                        buttons.addAll(Utils.getEntityValues(bot.entities.numericFunctionOperatorEntity));
                    } else if (Utils.getEntityValues(bot.entities.dateFieldEntity).contains(field)) {
                        buttons.addAll(Utils.getEntityValues(bot.entities.dateFunctionOperatorEntity));
                    } else if (Utils.getEntityValues(bot.entities.textualFieldEntity).contains(field)) {
                        // TODO: textual operators here
                    }
                    String sqlQuery = sqlQueries.fieldOfValue(keyFields, field, valueFieldMap, true);
                    ResultSet resultSetDistinct = sql.runSqlQuery(bot, sqlQuery);
                    buttons.add(Utils.getFirstTrainingSentences(bot.coreLibraryI18n.Quit).get(0));
                    bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString(
                                    "AskFieldOfValueOperation"), resultSet.getNumRows(), fieldRN,
                            resultSetDistinct.getNumRows(), conditions), buttons);
                })
                .next()
                .when(intentIs(bot.intents.showAllIntent)).moveTo(saveOperator)
                .when(intentIs(bot.intents.showAllDistinctIntent)).moveTo(saveOperator)
                .when(intentIs(bot.intents.numericFunctionOperatorIntent)).moveTo(saveOperator)
                .when(intentIs(bot.intents.dateFunctionOperatorIntent)).moveTo(saveOperator)
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState);
        saveOperator
                .body(context -> {
                    String operator = context.getIntent().getMatchedInput();
                    if (operator.equals(Utils.getFirstTrainingSentences(bot.intents.showAllIntent).get(0))) {
                        operator = "all";
                    } else if (operator.equals(Utils.getFirstTrainingSentences(bot.intents.showAllDistinctIntent).get(0))) {
                        operator = "distinct";
                    }
                    context.getSession().put(ContextKeys.OPERATOR, operator);
                })
                .next()
                .moveTo(mainState);

        return askOperator.getState();
    }
}
