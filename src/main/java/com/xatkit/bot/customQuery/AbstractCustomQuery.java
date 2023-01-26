package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;
import fr.inria.atlanmod.commons.log.Log;
import lombok.Getter;
import lombok.val;

import static com.xatkit.bot.App.sql;
import static com.xatkit.dsl.DSL.state;
import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * Abstract Custom Query.
 * <p>
 * This is the template that all custom queries must implement. It has a main state that:
 * <ul>
 *     <li>Checks that the intent parameters are OK</li>
 *     <li>Generates the appropriate SQL statement</li>
 *     <li>Executes it and saves the obtained result set</li>
 *     <li>Shows the answer to the user</li>
 * </ul>
 * It allows defining dedicated states to navigate to when something is wrong (the intent parameters or the result set)
 * to, for instance, ask about a missing parameter.
 */
public abstract class AbstractCustomQuery {

    /**
     * The Bot.
     */
    protected Bot bot;

    /**
     * The entry point for the workflow.
     */
    @Getter
    protected final State mainState;

    /**
     * The state where the chatbot ends up arriving once the workflow is finished.
     */
    protected final State returnState;

    /**
     * The constant DECIMAL (it is a data type).
     */
    public static final String DECIMAL = "decimal";

    /**
     * The constant DATETIME (it is a data type).
     */
    public static final String DATETIME = "datetime";

    /**
     * Instantiates a new custom query workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public AbstractCustomQuery(Bot bot, State returnState) {
        this.bot = bot;
        this.returnState = returnState;

        val mainState = state(this.getClass().getSimpleName());

        this.mainState = mainState.getState();

        mainState
                .body(context -> {
                    context.getSession().put(ContextKeys.BAD_PARAMS, false);
                    context.getSession().put(ContextKeys.BAD_RESULTSET, false);
                    context.getSession().put(ContextKeys.ALL_OK, false);
                    if (!checkParamsOk(context)) {
                        context.getSession().put(ContextKeys.BAD_PARAMS, true);
                        Log.error("Intent parameters are not OK");
                        return;
                    }
                    String sqlStatement = generateSqlStatement(context);
                    executeSqlAndStoreResultSet(sqlStatement, context);
                    if (!checkResultSetOk(context)) {
                        context.getSession().put(ContextKeys.BAD_RESULTSET, true);
                        Log.error("The obtained result set is not OK");
                        return;
                    }
                    String message = generateMessage(context);
                    if (!isEmpty(message)) {
                        bot.reactPlatform.reply(context, message);
                    }
                    context.getSession().put(ContextKeys.ALL_OK, true);

                })
                .next()
                .when(context -> (boolean) context.getSession().get(ContextKeys.BAD_PARAMS) && !continueWhenParamsNotOk(context)).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> (boolean) context.getSession().get(ContextKeys.BAD_PARAMS) && continueWhenParamsNotOk(context)).moveTo(getNextStateWhenParamsNotOk())
                .when(context -> (boolean) context.getSession().get(ContextKeys.BAD_RESULTSET) && !continueWhenResultSetNotOk(context)).moveTo(bot.getResult.getGenerateResultSetFromQueryState())
                .when(context -> (boolean) context.getSession().get(ContextKeys.BAD_RESULTSET) && continueWhenResultSetNotOk(context)).moveTo(getNextStateWhenResultSetNotOk())
                .when(context -> (boolean) context.getSession().get(ContextKeys.ALL_OK)).moveTo(bot.getResult.getShowDataState());

    }

    /**
     * Returns true if the intent parameters are OK, and false otherwise.
     *
     * @param context the current context
     * @return the boolean
     */
    protected abstract boolean checkParamsOk(StateContext context);

    /**
     * Returns true if the decision is to move to the next state of the workflow
     * ({@link #getNextStateWhenParamsNotOk()}) when the intent parameters are not OK, and false otherwise.
     *
     * @param context the current context
     * @return the boolean
     */
    protected abstract boolean continueWhenParamsNotOk(StateContext context);

    /**
     * Returns the state to go to when the intent parameters are not OK.
     *
     * @return the next state when params not ok
     */
    protected abstract State getNextStateWhenParamsNotOk();

    /**
     * Generates the appropriate SQL statement to obtain the data requested by the user query.
     *
     * @param context the current context
     * @return the string
     */
    protected abstract String generateSqlStatement(StateContext context);

    /**
     * Executes the SQL statement and stores the obtained result set in the current context.
     *
     * @param sqlStatement the sql statement
     * @param context      the context
     */
    protected void executeSqlAndStoreResultSet(String sqlStatement, StateContext context) {
        ResultSet resultSet = sql.runSqlQuery(bot, sqlStatement);
        context.getSession().put(ContextKeys.RESULTSET, resultSet);
    }

    /**
     * Returns true if the result set is OK, and false otherwise.
     *
     * @param context the current context
     * @return the boolean
     */
    protected abstract boolean checkResultSetOk(StateContext context);

    /**
     * Returns true if the decision is to move to the next state of the workflow
     * ({@link #getNextStateWhenResultSetNotOk()}) when the result set is not OK, and false otherwise.
     *
     * @param context the current context
     * @return the boolean
     */
    protected abstract boolean continueWhenResultSetNotOk(StateContext context);

    /**
     * Returns the state to go to when the result set is not OK.
     *
     * @return the next state when result set not ok
     */
    protected abstract State getNextStateWhenResultSetNotOk();

    /**
     * Generates the message that accompanies the result set.
     *
     * @param context the current context
     * @return the string
     */
    protected abstract String generateMessage(StateContext context);
}
