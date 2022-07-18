package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.Bot.getResult;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.bot.Bot.sql;
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
     * This variable stores the error condition of the workflow (i.e. if some parameter was not recognized properly)
     */
    private boolean error;

    /**
     * Instantiates a new Custom Numeric Field Function workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomNumericFieldFunction(ReactPlatform reactPlatform, State returnState) {
        val processCustomNumericFieldFunctionState = state("ProcessCustomNumericFieldFunction");

        processCustomNumericFieldFunctionState
                .body(context -> {
                    error = false;
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    String operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    if (!isEmpty(field) && !isEmpty(operator)) {
                        String sqlQuery = sql.queries.numericFieldFunction(field, operator);
                        ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                        float result = Float.parseFloat(resultSet.getRow(0).getColumnValue(0));
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomNumericFieldFunction"),
                                operator, field, result));
                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(getResult.getGenerateResultSetFromQueryState())
                .when(context -> !error).moveTo(returnState);

        this.processCustomNumericFieldFunctionState = processCustomNumericFieldFunctionState.getState();
    }
}
