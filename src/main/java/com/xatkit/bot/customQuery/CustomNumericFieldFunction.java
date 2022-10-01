package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

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
                    if (!isEmpty(field) && !isEmpty(operator)) {
                        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                        String sqlQuery = sqlQueries.numericFieldFunction(field, operator);
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        float result = Float.parseFloat(resultSet.getRow(0).getColumnValue(0));
                        String fieldRN = bot.entities.readableNames.get(field);
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomNumericFieldFunction"),
                                operator, fieldRN, result));
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
