package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.App.sql;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Field Of Numeric Field Function workflow of a chatbot.
 * <p>
 * Given a number (n), a field name (field1), a numeric field name (field2) and an operator (max/min), this workflow
 * shows the n field1 values whose row's field2 are between the n max/min values.
 * <p>
 * Allowed operators are: {@code max}, {@code min}
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomFieldOfNumericFieldFunction {

    /**
     * The entry point for the Custom Field Of Numeric Field Function workflow.
     */
    @Getter
    private final State processCustomFieldOfNumericFieldFunctionState;

    /**
     * This variable stores the error condition of the workflow (i.e. if some parameter was not recognized properly)
     */
    private boolean error;

    /**
     * Instantiates a new Custom Field Of Numeric Field Function workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomFieldOfNumericFieldFunction(Bot bot, State returnState) {
        val processCustomFieldOfNumericFieldFunctionState = state("ProcessCustomFieldOfNumericFieldFunction");

        processCustomFieldOfNumericFieldFunctionState
                .body(context -> {
                    error = false;
                    String field1 = (String) context.getIntent().getValue(ContextKeys.FIELD + "1");
                    String field2 = (String) context.getIntent().getValue(ContextKeys.FIELD + "2");
                    String operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    String number = (String) context.getIntent().getValue(ContextKeys.NUMBER);
                    if (!isEmpty(field1) && !isEmpty(field2) && !isEmpty(operator) && (operator.equals("max") || operator.equals("min"))) {
                        if (isEmpty(number)) {
                            // if no number is specified, get the top 1
                            number = "1";
                        }
                        String sqlQuery = bot.sqlQueries.fieldOfNumericFieldFunction(field1, field2, operator, number);
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        bot.getResult.setResultSet(resultSet);
                        String field1RN = bot.entities.readableNames.get(field1);
                        String field2RN = bot.entities.readableNames.get(field2);
                        if (number.equals("1")) {
                            bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomFieldOfNumericFieldFunction1"),
                                    field1RN, operator, field2RN));
                        } else {
                            bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomFieldOfNumericFieldFunction"),
                                    number, field1RN, operator, field2RN));
                        }

                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> !error).moveTo(bot.getResult.getShowDataState());

        this.processCustomFieldOfNumericFieldFunctionState = processCustomFieldOfNumericFieldFunctionState.getState();
    }
}
