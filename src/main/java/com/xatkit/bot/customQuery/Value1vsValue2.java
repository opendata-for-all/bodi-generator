package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;

import java.text.MessageFormat;

import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * The Value1 vs Value2 workflow of a chatbot.
 * <p>
 * Given two field values, this workflow tells to the user which value is more/less frequent than the other.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class Value1vsValue2 extends AbstractCustomQuery {

    public Value1vsValue2(Bot bot, State returnState) {
        super(bot, returnState);
    }

    @Override
    protected boolean checkParamsOk(StateContext context) {
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
        return !isEmpty(value1) && !isEmpty(value2);
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
        String field1 = Entities.fieldValueMap.get(value1);
        String field2 = Entities.fieldValueMap.get(value2);
        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
        return sqlQueries.value1VSValue2(field1, value1, field2, value2);
    }

    @Override
    protected boolean checkResultSetOk(StateContext context) {
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        return resultSet.getNumRows() == 1 && resultSet.getHeader().size() == 6;
    }

    @Override
    protected boolean continueWhenResultSetNotOk(StateContext context) {
        // when result set is not ok, we stop the execution
        return false;
    }

    @Override
    protected String generateMessage(StateContext context) {
        ResultSet resultSet = (ResultSet) context.getSession().get(ContextKeys.RESULTSET);
        String value1 = (String) context.getSession().get(ContextKeys.VALUE + "1");
        String value2 = (String) context.getSession().get(ContextKeys.VALUE + "2");
        String field1 = Entities.fieldValueMap.get(value1);
        String field2 = Entities.fieldValueMap.get(value2);
        String field1RN = bot.entities.readableNames.get(field1);
        String field2RN = bot.entities.readableNames.get(field2);

        int value1Freq = Integer.parseInt(resultSet.getRow(0).getColumnValue(2));
        int value2Freq = Integer.parseInt(resultSet.getRow(0).getColumnValue(5));

        if (context.getIntent().getDefinition().getName().equals(bot.intents.value1MoreThanValue2Intent.getName())) {
            if (value1Freq > value2Freq) {
                return MessageFormat.format(bot.messages.getString("Value1MoreThanValue2"), value1, value1Freq, field1RN, value2, value2Freq, field2RN);
            } else if (value2Freq > value1Freq) {
                return MessageFormat.format(bot.messages.getString("Value1MoreThanValue2"), value2, value2Freq, field2RN, value1, value1Freq, field1RN);
            } else {
                return MessageFormat.format(bot.messages.getString("Value1EqualToValue2"), value1, field1RN, value2, field2RN, value1Freq);
            }
        } else {
            if (value1Freq < value2Freq) {
                return MessageFormat.format(bot.messages.getString("Value1LessThanValue2"), value1, value1Freq, field1RN, value2, value2Freq, field2RN);
            } else if (value2Freq < value1Freq) {
                return MessageFormat.format(bot.messages.getString("Value1LessThanValue2"), value2, value2Freq, field2RN, value1, value1Freq, field1RN);
            } else {
                return MessageFormat.format(bot.messages.getString("Value1EqualToValue2"), value1, field1RN, value2, field2RN, value1Freq);
            }
        }
    }

    @Override
    protected State getNextStateWhenResultSetNotOk() {
        return bot.getResult.getGenerateResultSetFromQueryState();
    }
}
