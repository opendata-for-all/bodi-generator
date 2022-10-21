package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.google.common.collect.Streams;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xatkit.bot.App.sql;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Numeric Field Function workflow of a chatbot.
 * <p>
 * Given a numeric field name and an operator, this workflow applies the operator in the field and shows the result.
 * <p>
 * Some example operators are: {@code max}, {@code min}, {@code avg}, {@code sum}
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomNumericFieldFunction {

    /**
     * The entry point for the Custom Numeric Field Function workflow.
     */
    @Getter
    private final State processCustomNumericFieldFunctionState;

    /**
     * Instantiates a new Custom Numeric Field Function workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomNumericFieldFunction(Bot bot, State returnState) {
        val processCustomNumericFieldFunctionState = state("ProcessCustomNumericFieldFunction");

        processCustomNumericFieldFunctionState
                .body(context -> {
                    context.getSession().put(ContextKeys.ERROR, false);
                    String field = (String) context.getSession().get(ContextKeys.FIELD);
                    String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
                    String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
                    String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
                    String value3 = (String) context.getSession().get(ContextKeys.VALUE + "3");
                    String value1Field = Entities.fieldValueMap.get(value1);
                    String value2Field = Entities.fieldValueMap.get(value2);
                    String value3Field = Entities.fieldValueMap.get(value3);
                    Map<String, String> valueFieldMap = new HashMap<>();
                    if (!isEmpty(value1) && !isEmpty(value1Field)) {
                        valueFieldMap.put(value1, value1Field);
                    }
                    if (!isEmpty(value2) && !isEmpty(value2Field)) {
                        valueFieldMap.put(value2, value2Field);
                    }
                    if (!isEmpty(value3) && !isEmpty(value3Field)) {
                        valueFieldMap.put(value3, value3Field);
                    }
                    context.getSession().put(ContextKeys.VALUE_FIELD_MAP, valueFieldMap);
                    if (!isEmpty(field) && !isEmpty(operator)) {
                        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                        String sqlQuery = sqlQueries.numericFieldFunction(field, operator, valueFieldMap);
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        float result = Float.parseFloat(resultSet.getRow(0).getColumnValue(0));
                        String fieldRN = bot.entities.readableNames.get(field);
                        String conditions = String.join(", ", Streams.zip(valueFieldMap.keySet().stream(),
                                valueFieldMap.values().stream(), (v, f) -> bot.entities.readableNames.get(f) + " " + "= " + v).collect(Collectors.toList()));
                       if (conditions.isEmpty()) {
                           bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomNumericFieldFunction"),
                                   operator, fieldRN, result));
                       } else {
                           bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomNumericFieldFunctionConditions"),
                                   operator, fieldRN, conditions, result));
                       }
                    } else {
                        context.getSession().put(ContextKeys.ERROR, true);
                    }
                })
                .next()
                .when(context -> (boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> !(boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(returnState);

        this.processCustomNumericFieldFunctionState = processCustomNumericFieldFunctionState.getState();
    }
}
