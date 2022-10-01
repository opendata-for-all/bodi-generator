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
     * Instantiates a new Custom Row Of Values workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomRowOfValues(Bot bot, State returnState) {
        val processCustomRowOfValuesState = state("ProcessCustomRowOfValues");

        processCustomRowOfValuesState
                .body(context -> {
                    context.getSession().put(ContextKeys.ERROR, false);
                    String rowName = (String) context.getSession().get(ContextKeys.ROW_NAME);
                    if (isEmpty(rowName)) {
                        rowName = Utils.getEntityValues(bot.entities.rowNameEntity).get(0);
                    }
                    String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
                    String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
                    String value3 = (String) context.getSession().get(ContextKeys.VALUE + "3");
                    List<String> keyFields = new ArrayList<>(bot.entities.keyFields);
                    SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                    if (!isEmpty(value1)) {
                        String field1 = Entities.fieldValueMap.get(value1);
                        String sqlQuery = sqlQueries.rowOfValues1(keyFields, field1, value1);
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        context.getSession().put(ContextKeys.RESULTSET, resultSet);
                        String field1RN = bot.entities.readableNames.get(field1);
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("RowOfValue1"),
                                rowName, field1RN, value1));
                    } else if (!isEmpty(value2) && !isEmpty(value3)) {
                        String field2 = Entities.fieldValueMap.get(value2);
                        String field3 = Entities.fieldValueMap.get(value3);
                        String sqlQuery = sqlQueries.rowOfValues2(keyFields, field2, value2, field3, value3);
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        context.getSession().put(ContextKeys.RESULTSET, resultSet);
                        String field2RN = bot.entities.readableNames.get(field2);
                        String field3RN = bot.entities.readableNames.get(field3);
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("RowOfValue2AndValue3"),
                                rowName, field2RN, value2, field3RN, value3));
                    } else {
                        context.getSession().put(ContextKeys.ERROR, true);
                    }
                })
                .next()
                .when(context -> (boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> !(boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(bot.getResult.getShowDataState());

        this.processCustomRowOfValuesState = processCustomRowOfValuesState.getState();
    }
}
