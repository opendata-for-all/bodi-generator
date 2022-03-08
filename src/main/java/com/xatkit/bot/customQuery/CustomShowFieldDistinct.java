package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.Operation;
import bodi.generator.dataSource.ResultSet;
import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import static com.xatkit.bot.Bot.getResult;
import static com.xatkit.dsl.DSL.state;

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
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomShowFieldDistinct(ReactPlatform reactPlatform, State returnState) {
        val processCustomShowFieldDistinctState = state("ProcessCustomShowFieldDistinct");

        processCustomShowFieldDistinctState
                .body(context -> {
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                    ResultSet resultSet = statement.executeQuery(Operation.SHOW_FIELD_DISTINCT, field);
                    context.getSession().put(ContextKeys.RESULT_SET, resultSet);
                })
                .next()
                .moveTo(getResult.getGenerateResultSetState());

        this.processCustomShowFieldDistinctState = processCustomShowFieldDistinctState.getState();
    }
}
