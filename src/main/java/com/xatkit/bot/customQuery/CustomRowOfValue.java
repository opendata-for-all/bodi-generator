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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Row Of Value workflow of a chatbot.
 * <p>
 * Given 1 or 2 field values, the bot shows all the entries that match these values in their respective fields.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomRowOfValue extends AbstractCustomQuery {

    public CustomRowOfValue(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String rowName = (String) context.getSession().get(ContextKeys.ROW_NAME);
        if (isEmpty(rowName)) {
            rowName = Utils.getEntityValues(bot.entities.rowNameEntity).get(0);
            context.getSession().put(ContextKeys.ROW_NAME, rowName);
        }
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
        return !isEmpty(value1) || !isEmpty(value2);
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

    @Override
    protected String generateSqlStatement(StateContext context) {
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
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
        return sqlQueries.rowOfValues(keyFields, valueFieldMap);
    }

    @Override
    protected boolean checkResultSetOk(StateContext context) {
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        return resultSet.getNumRows() > 0;
    }

    @Override
    protected boolean continueWhenResultSetNotOk(StateContext context) {
        // When result set is not ok, we stop the execution
        return false;
    }

    @Override
    protected String generateMessage(StateContext context) {
        String rowName = (String) context.getSession().get(ContextKeys.ROW_NAME);
        Map<String, String> valueFieldMap = (Map<String, String>) context.getSession().get(ContextKeys.VALUE_FIELD_MAP);
        String conditions = String.join(", ", Streams.zip(valueFieldMap.keySet().stream(),
                valueFieldMap.values().stream(), (v, f) -> bot.entities.readableNames.get(f) + " " + "= " + v).collect(Collectors.toList()));

        return MessageFormat.format(bot.messages.getString("RowOfValue"), rowName, conditions);
    }

    @Override
    protected State getNextStateWhenResultSetNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }
}
