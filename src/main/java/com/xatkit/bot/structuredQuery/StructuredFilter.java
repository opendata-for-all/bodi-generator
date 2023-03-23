package com.xatkit.bot.structuredQuery;

import com.xatkit.bot.library.ResultSet;
import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Utils;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xatkit.bot.App.sql;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Structured Filter workflow of a chatbot.
 * <p>
 * These are the different entry points in this workflow:
 * <ul>
 *     <li>
 *         {@link #selectFieldState} It recognizes a filter query and processes the given filter, storing it in the
 *         chatbot memory.
 *         <p>
 *         This process is done in steps. First, the user inputs the field that wants to be filtered. Second, he/she
 *         inputs the operator of the filter. Finally, he/she inputs the value of the filter operation.
 *     </li>
 *
 *     <li>
 *         {@link #selectFilterToRemoveState} It allows the user to select a currently applied filter to remove it.
 *     </li>
 * </ul>
 * <p>
 * This workflow is helpful in cases where there is no knowledge about, for instance, the data type of some field,
 * the available operators for some data type or the field names.
 */
public class StructuredFilter {

    /**
     * The entry point to apply a filter.
     */
    @Getter
    private final State selectFieldState;

    /**
     * The entry point to remove a (previously applied) filter.
     */
    @Getter
    private final State selectFilterToRemoveState;

    /**
     * Instantiates a new Structured Filter workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public StructuredFilter(Bot bot, State returnState) {
        val selectFieldState = state("SelectField");
        val saveFieldState = state("SaveField");

        val selectOperatorState = state("SelectOperator");
        val saveOperatorState = state("SaveOperator");

        val writeDatetimeValueState = state("WriteDatetimeValue");
        val writeTextualValueState = state("WriteTextualValue");
        val writeNumericValueState = state("WriteNumericValue");

        val saveStructuredFilterState = state("SaveStructuredFilter");

        // Input the FIELD name

        selectFieldState
                .body(context -> {
                    SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                    List<String> fields = sqlQueries.getAllFields();
                    List<String> fieldsRN = fields.stream().map(field -> bot.entities.readableNames.get(field)).collect(Collectors.toList());
                    bot.reactPlatform.reply(context, bot.messages.getString("SelectField"), fieldsRN);
                })
                .next()
                .when(intentIs(bot.intents.numericFieldIntent)).moveTo(saveFieldState)
                .when(intentIs(bot.intents.textualFieldIntent)).moveTo(saveFieldState)
                .when(intentIs(bot.intents.datetimeFieldIntent)).moveTo(saveFieldState);

        // Save the FIELD name

        saveFieldState
                .body(context -> {
                    context.getSession().put(ContextKeys.FIELD, context.getIntent().getValue(ContextKeys.VALUE));
                    context.getSession().put(ContextKeys.INTENT_NAME, context.getIntent().getDefinition().getName());
                })
                .next()
                .moveTo(selectOperatorState);

        // Input the OPERATOR name

        selectOperatorState
                .body(context -> {
                    List<String> operators = new ArrayList<>();
                    String fieldIntentName = (String) context.getSession().get(ContextKeys.INTENT_NAME);
                    if (fieldIntentName.equals(bot.intents.textualFieldIntent.getName())) {
                        operators = Utils.getEntityValues(bot.entities.textualOperatorEntity);
                    } else if (fieldIntentName.equals(bot.intents.numericFieldIntent.getName())) {
                        operators = Utils.getEntityValues(bot.entities.numericOperatorEntity);
                    } else if (fieldIntentName.equals(bot.intents.datetimeFieldIntent.getName())) {
                        operators = Utils.getEntityValues(bot.entities.datetimeOperatorEntity);
                    }
                    bot.reactPlatform.reply(context, bot.messages.getString("SelectOperator"), operators);
                })
                .next()
                .when(intentIs(bot.intents.textualOperatorIntent)).moveTo(saveOperatorState)
                .when(intentIs(bot.intents.numericOperatorIntent)).moveTo(saveOperatorState)
                .when(intentIs(bot.intents.datetimeOperatorIntent)).moveTo(saveOperatorState);

        // Save the OPERATOR name

        saveOperatorState
                .body(context -> {
                    context.getSession().put(ContextKeys.OPERATOR, context.getIntent().getValue(ContextKeys.VALUE));
                })
                .next()
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.textualFieldIntent.getName())).moveTo(writeTextualValueState)
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.numericFieldIntent.getName())).moveTo(writeNumericValueState)
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.datetimeFieldIntent.getName())).moveTo(writeDatetimeValueState);

        // Input the VALUE
        // Divided by data types for safety (e.g. a date may be recognized as a text if we don't separate data types)

        writeTextualValueState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("WriteTextualValue"));
                })
                .next()
                .when(intentIs(bot.coreLibraryI18n.AnyValue)).moveTo(saveStructuredFilterState);

        writeNumericValueState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("WriteNumericValue"));
                })
                .next()
                .when(intentIs(bot.coreLibraryI18n.NumberValue)).moveTo(saveStructuredFilterState);

        writeDatetimeValueState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("WriteDatetimeValue"));
                })
                .next()
                .when(intentIs(bot.coreLibraryI18n.DateTimeValue)).moveTo(saveStructuredFilterState);

        // Finally, save the filter, composed by a FIELD, an OPERATOR, and a VALUE

        saveStructuredFilterState
                .body(context -> {
                    String field = (String) context.getSession().get(ContextKeys.FIELD);
                    String operator = (String) context.getSession().get(ContextKeys.OPERATOR);
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
                        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                        sqlQueries.addFilter(field, operator, value);
                        String sqlQuery =  sqlQueries.selectAll();
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        context.getSession().put(ContextKeys.RESULTSET, resultSet);
                        int resultSetNumRows = resultSet.getNumRows();
                        context.getSession().put(ContextKeys.RESULTSET_NUM_ROWS, resultSet.getNumRows());
                        String fieldRN = bot.entities.readableNames.get(field);
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("FilterAdded"),
                                fieldRN, operator, value, resultSetNumRows));
                    } else {
                        bot.reactPlatform.reply(context, bot.messages.getString("SomethingWentWrong"));
                    }
                })
                .next()
                .when(context -> (int) context.getSession().get(ContextKeys.RESULTSET_NUM_ROWS) <= bot.maxEntriesToDisplay).moveTo(bot.getResult.getShowDataState())
                .when(context -> (int) context.getSession().get(ContextKeys.RESULTSET_NUM_ROWS) > bot.maxEntriesToDisplay).moveTo(returnState);

        this.selectFieldState = selectFieldState.getState();

        // The Remove Filter workflow

        val selectFilterToRemoveState = state("SelectFilterToRemove");
        val removeFilterState = state("RemoveFilter");

        selectFilterToRemoveState
                .body(context -> {
                    SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                    List<String> currentFilters = sqlQueries.getFiltersAsStrings(bot.entities.readableNames);
                    if (currentFilters.isEmpty()) {
                        bot.reactPlatform.reply(context, bot.messages.getString("NoFilters"),
                                Utils.getFirstTrainingSentences(bot.coreLibraryI18n.Quit));
                    } else {
                        currentFilters.add(Utils.getFirstTrainingSentences(bot.coreLibraryI18n.Quit).get(0));
                        bot.reactPlatform.reply(context, bot.messages.getString("SelectFilter"), currentFilters);
                    }
                })
                .next()
                .when(intentIs(bot.intents.numericFieldOperatorValueIntent)).moveTo(removeFilterState)
                .when(intentIs(bot.intents.datetimeFieldOperatorValueIntent)).moveTo(removeFilterState)
                .when(intentIs(bot.intents.textualFieldOperatorValueIntent)).moveTo(removeFilterState)
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState);

        removeFilterState
                .body(context -> {
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    String operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
                        SqlQueries sqlQueries = (SqlQueries) context.getSession().get(ContextKeys.SQL_QUERIES);
                        sqlQueries.removeFilter(field, operator, value);
                        String sqlQuery =  sqlQueries.selectAll();
                        ResultSet resultSet = sql.runSqlQuery(bot, sqlQuery);
                        String fieldRN = bot.entities.readableNames.get(field);
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("FilterRemoved"),
                                fieldRN, operator, value, resultSet.getNumRows()));
                    } else {
                        bot.reactPlatform.reply(context, bot.messages.getString("SomethingWentWrong"));
                    }
                })
                .next()
                .moveTo(returnState);

        this.selectFilterToRemoveState = selectFilterToRemoveState.getState();
    }
}
