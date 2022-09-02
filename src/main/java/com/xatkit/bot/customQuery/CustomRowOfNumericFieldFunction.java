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
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Row Of Numeric Field Function workflow of a chatbot.
 * <p>
 * Given a numeric field name and an operator, this workflow shows all the entries that match the field value with
 * the value obtained when applying the operator.
 * <p>
 * Allowed operators are: {@code max}, {@code min}
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomRowOfNumericFieldFunction {

    /**
     * The entry point for the Custom Row Of Numeric Field Function workflow.
     */
    @Getter
    private final State processCustomRowOfNumericFieldFunctionState;

    /**
     * This variable stores the error condition of the workflow (i.e. if some parameter was not recognized properly)
     */
    private boolean error;

    /**
     * Instantiates a new Custom Row Of Numeric Field Function workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomRowOfNumericFieldFunction(Bot bot, State returnState) {
        val processCustomRowOfNumericFieldFunctionState = state("ProcessCustomRowOfNumericFieldFunction");

        processCustomRowOfNumericFieldFunctionState
                .body(context -> {
                    error = false;
                    String rowName = (String) context.getIntent().getValue(ContextKeys.ROW_NAME);
                    if (isEmpty(rowName)) {
                        rowName = Utils.getEntityValues(bot.entities.rowNameEntity).get(0);
                    }
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    String operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    if (!isEmpty(field) && !isEmpty(operator) && (operator.equals("max") || operator.equals("min"))) {
                        List<String> keyFields = new ArrayList<>(bot.entities.keyFields);
                        String sqlQuery = bot.sqlQueries.rowOfNumericFieldFunction(keyFields, field, operator);
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        bot.getResult.setResultSet(resultSet);
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("CustomRowOfNumericFieldFunction"),
                                rowName, operator, field));
                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> !error).moveTo(bot.getResult.getShowDataState());

        this.processCustomRowOfNumericFieldFunctionState = processCustomRowOfNumericFieldFunctionState.getState();
    }
}
