package com.xatkit.bot.structuredQuery;

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
     * This variable stores the number of rows of the generated result set.
     */
    private int resultSetNumRows;

    /**
     * Used to store the field data type when selecting the field. Later, in SaveOperator, it is used to transition
     * to the datatype-dependant proper state.
     */
    private String fieldIntentName;

    /**
     * This variable stores the {@code field} parameter recognized from the matched intent.
     */
    private String field;

    /**
     * This variable stores the {@code operator} parameter recognized from the matched intent.
     */
    private String operator;

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

        val writeDateValueState = state("WriteDateValue");
        val writeTextualValueState = state("WriteTextualValue");
        val writeNumericValueState = state("WriteNumericValue");

        val saveStructuredFilterState = state("SaveStructuredFilter");

        // Input the FIELD name

        selectFieldState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("SelectField"),
                            (List<String>) context.getSession().get(ContextKeys.FILTER_FIELD_OPTIONS));
                })
                .next()
                .when(intentIs(bot.intents.numericFieldIntent)).moveTo(saveFieldState)
                .when(intentIs(bot.intents.textualFieldIntent)).moveTo(saveFieldState)
                .when(intentIs(bot.intents.dateFieldIntent)).moveTo(saveFieldState);

        // Save the FIELD name

        saveFieldState
                .body(context -> {
                    field = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    fieldIntentName = context.getIntent().getDefinition().getName();
                })
                .next()
                .moveTo(selectOperatorState);

        // Input the OPERATOR name

        selectOperatorState
                .body(context -> {
                    List<String> operators = new ArrayList<>();
                    if (fieldIntentName.equals(bot.intents.textualFieldIntent.getName())) {
                        operators = Utils.getEntityValues(bot.entities.textualOperatorEntity);
                    } else if (fieldIntentName.equals(bot.intents.numericFieldIntent.getName())) {
                        operators = Utils.getEntityValues(bot.entities.numericOperatorEntity);
                    } else if (fieldIntentName.equals(bot.intents.dateFieldIntent.getName())) {
                        operators = Utils.getEntityValues(bot.entities.dateOperatorEntity);
                    }
                    bot.reactPlatform.reply(context, bot.messages.getString("SelectOperator"), operators);
                })
                .next()
                .when(intentIs(bot.intents.textualOperatorIntent)).moveTo(saveOperatorState)
                .when(intentIs(bot.intents.numericOperatorIntent)).moveTo(saveOperatorState)
                .when(intentIs(bot.intents.dateOperatorIntent)).moveTo(saveOperatorState);

        // Save the OPERATOR name

        saveOperatorState
                .body(context -> {
                    operator = (String) context.getIntent().getValue(ContextKeys.VALUE);
                })
                .next()
                .when(context -> fieldIntentName.equals(bot.intents.textualFieldIntent.getName())).moveTo(writeTextualValueState)
                .when(context -> fieldIntentName.equals(bot.intents.numericFieldIntent.getName())).moveTo(writeNumericValueState)
                .when(context -> fieldIntentName.equals(bot.intents.dateFieldIntent.getName())).moveTo(writeDateValueState);

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

        writeDateValueState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("WriteDateValue"));
                })
                .next()
                .when(intentIs(bot.coreLibraryI18n.DateValue)).moveTo(saveStructuredFilterState);

        // Finally, save the filter, composed by a FIELD, an OPERATOR, and a VALUE

        saveStructuredFilterState
                .body(context -> {
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
                        bot.sqlQueries.addFilter(field, operator, value);
                        String sqlQuery =  bot.sqlQueries.selectAll();
                        ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                        bot.getResult.setResultSet(resultSet);
                        resultSetNumRows = resultSet.getNumRows();
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("FilterAdded"),
                                field, operator, value, resultSetNumRows));
                    } else {
                        bot.reactPlatform.reply(context, bot.messages.getString("SomethingWentWrong"));
                    }
                })
                .next()
                .when(context -> resultSetNumRows <= bot.maxEntriesToDisplay).moveTo(bot.getResult.getShowDataState())
                .when(context -> resultSetNumRows > bot.maxEntriesToDisplay).moveTo(returnState);

        this.selectFieldState = selectFieldState.getState();

        // The Remove Filter workflow

        val selectFilterToRemoveState = state("SelectFilterToRemove");
        val removeFilterState = state("RemoveFilter");

        selectFilterToRemoveState
                .body(context -> {
                    List<String> currentFilters = bot.sqlQueries.getFiltersAsStrings();
                    if (currentFilters.isEmpty()) {
                        bot.reactPlatform.reply(context, bot.messages.getString("NoFilters"),
                                Utils.getFirstTrainingSentences(bot.coreLibraryI18n.Quit));
                    } else {
                        currentFilters.add(Utils.getFirstTrainingSentences(bot.coreLibraryI18n.Quit).get(0));
                        bot.reactPlatform.reply(context, bot.messages.getString("SelectFilter"), currentFilters);
                    }
                })
                .next()
                .when(intentIs(bot.intents.customNumericFilterIntent)).moveTo(removeFilterState)
                .when(intentIs(bot.intents.customDateFilterIntent)).moveTo(removeFilterState)
                .when(intentIs(bot.intents.customTextualFilterIntent)).moveTo(removeFilterState)
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState);

        removeFilterState
                .body(context -> {
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    String operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
                        bot.sqlQueries.removeFilter(field, operator, value);
                        String sqlQuery =  bot.sqlQueries.selectAll();
                        ResultSet resultSet = sql.runSqlQuery(sqlQuery);
                        bot.reactPlatform.reply(context, MessageFormat.format(bot.messages.getString("FilterRemoved"),
                                field, operator, value, resultSet.getNumRows()));
                    } else {
                        bot.reactPlatform.reply(context, bot.messages.getString("SomethingWentWrong"));
                    }
                })
                .next()
                .moveTo(returnState);

        this.selectFilterToRemoveState = selectFilterToRemoveState.getState();
    }
}
