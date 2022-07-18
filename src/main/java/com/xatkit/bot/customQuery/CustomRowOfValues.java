package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.xatkit.bot.Bot.getResult;
import static com.xatkit.bot.Bot.messages;
import static com.xatkit.bot.Bot.sql;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Row Of Values workflow of a chatbot.
 * <p>
 * Given 1 or 2 field values, the bot shows all the entries that match these values in their respective fields.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomRowOfValues {

    /**
     * The entry point for the Custom Row Of Values workflow.
     */
    @Getter
    private final State processCustomRowOfValuesState;

    /**
     * This variable stores the error condition of the workflow (i.e. if some parameter was not recognized properly)
     */
    private boolean error;

    /**
     * Instantiates a new Custom Row Of Values workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomRowOfValues(ReactPlatform reactPlatform, State returnState) {
        val processCustomRowOfValuesState = state("ProcessCustomRowOfValues");

        processCustomRowOfValuesState
                .body(context -> {
                    error = false;
                    String rowName = (String) context.getIntent().getValue(ContextKeys.ROW_NAME);
                    if (isEmpty(rowName)) {
                        rowName = Utils.getEntityValues(Entities.rowNameEntity).get(0);
                    }
                    String value1 = (String) context.getIntent().getValue(ContextKeys.VALUE + "1");
                    String value2 = (String) context.getIntent().getValue(ContextKeys.VALUE + "2");
                    String value3 = (String) context.getIntent().getValue(ContextKeys.VALUE + "3");
                    List<String> keyFields = new ArrayList<>(Entities.keyFields);
                    if (!isEmpty(value1)) {
                        String field1 = Entities.fieldValueMap.get(value1);
                        String sqlQuery = sql.queries.rowOfValues1(keyFields, field1, value1);
                        ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                        getResult.setResultSet(resultSet);
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("RowOfValue1"),
                                rowName, field1, value1));
                    } else if (!isEmpty(value2) && !isEmpty(value3)) {
                        String field2 = Entities.fieldValueMap.get(value2);
                        String field3 = Entities.fieldValueMap.get(value3);
                        String sqlQuery = sql.queries.rowOfValues2(keyFields, field2, value2, field3, value3);
                        ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                        getResult.setResultSet(resultSet);
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("RowOfValue2AndValue3"),
                                rowName, field2, value2, field3, value3));
                    } else {
                        error = true;
                    }
                })
                .next()
                .when(context -> error).moveTo(getResult.getGenerateResultSetFromQueryState())
                .when(context -> !error).moveTo(getResult.getShowDataState());

        this.processCustomRowOfValuesState = processCustomRowOfValuesState.getState();
    }
}
