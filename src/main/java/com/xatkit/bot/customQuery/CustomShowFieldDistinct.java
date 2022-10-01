package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import static com.xatkit.bot.App.sql;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Show Field Distinct workflow of a chatbot.
 * <p>
 * Given a field name, this workflow gets the unique values (i.e. a set) of that field and shows it to the user.
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomShowFieldDistinct {

    /**
     * The entry point for the Custom Show Field Distinct workflow.
     */
    @Getter
    private final State processCustomShowFieldDistinctState;

    /**
     * Instantiates a new Custom Show Field Distinct workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomShowFieldDistinct(Bot bot, State returnState) {
        val processCustomShowFieldDistinctState = state("ProcessCustomShowFieldDistinct");

        processCustomShowFieldDistinctState
                .body(context -> {
                    context.getSession().put(ContextKeys.ERROR, false);
                    String field = (String) context.getSession().get(ContextKeys.FIELD);
                    if (!isEmpty(field)) {
                        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                        String sqlQuery = sqlQueries.showFieldDistinct(field);
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        context.getSession().put(ContextKeys.RESULTSET, resultSet);
                    } else {
                        context.getSession().put(ContextKeys.ERROR, true);
                    }
                })
                .next()
                .when(context -> (boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> !(boolean) context.getSession().get(ContextKeys.ERROR)).moveTo(bot.getResult.getShowDataState());

        this.processCustomShowFieldDistinctState = processCustomShowFieldDistinctState.getState();
    }
}
