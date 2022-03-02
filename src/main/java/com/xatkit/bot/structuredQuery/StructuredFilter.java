package com.xatkit.bot.structuredQuery;

import bodi.generator.dataSource.ResultSet;
import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.xatkit.bot.Bot.coreLibraryI18n;
import static com.xatkit.bot.Bot.messages;
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
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public StructuredFilter(ReactPlatform reactPlatform, State returnState) {
        val selectFieldState = state("SelectField");
        val saveFieldState = state("SaveField");

        val writeOperatorState = state("WriteOperator");
        val saveOperatorState = state("SaveOperator");

        val writeDateValueState = state("WriteDateValue");
        val writeTextualValueState = state("WriteTextualValue");
        val writeNumericValueState = state("WriteNumericValue");

        val saveStructuredFilterState = state("SaveStructuredFilter");

        // Used to store the field data type when selecting the field. Later, in SaveOperator, it is used to
        // transition to the datatype-dependant proper state.
        final String[] fieldIntentName = new String[1];

        // Input the FIELD name

        selectFieldState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("SelectField"),
                            (List<String>) context.getSession().get(ContextKeys.FILTER_FIELD_OPTIONS));
                })
                .next()
                .when(intentIs(Intents.numericFieldIntent)).moveTo(saveFieldState)
                .when(intentIs(Intents.textualFieldIntent)).moveTo(saveFieldState)
                .when(intentIs(Intents.dateFieldIntent)).moveTo(saveFieldState);

        // Save the FIELD name

        saveFieldState
                .body(context -> {
                    String fieldName = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    context.getSession().put(ContextKeys.LAST_FIELD, fieldName);
                    fieldIntentName[0] = context.getIntent().getDefinition().getName();
                })
                .next()
                .moveTo(writeOperatorState);

        // Input the OPERATOR name

        writeOperatorState
                .body(context -> {
                    List<String> operators = new ArrayList<>();
                    if (fieldIntentName[0].equals(Intents.textualFieldIntent.getName())) {
                        operators = Utils.getEntityValues(Entities.textualOperatorEntity);
                    } else if (fieldIntentName[0].equals(Intents.numericFieldIntent.getName())) {
                        operators = Utils.getEntityValues(Entities.numericOperatorEntity);
                    } else if (fieldIntentName[0].equals(Intents.dateFieldIntent.getName())) {
                        operators = Utils.getEntityValues(Entities.dateOperatorEntity);
                    }
                    reactPlatform.reply(context, messages.getString("SelectOperator"), operators);
                })
                .next()
                .when(intentIs(Intents.textualOperatorIntent)).moveTo(saveOperatorState)
                .when(intentIs(Intents.numericOperatorIntent)).moveTo(saveOperatorState)
                .when(intentIs(Intents.dateOperatorIntent)).moveTo(saveOperatorState);

        // Save the OPERATOR name

        saveOperatorState
                .body(context -> {
                    String operatorName = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    context.getSession().put(ContextKeys.LAST_OPERATOR, operatorName);
                })
                .next()
                .when(context -> fieldIntentName[0].equals(Intents.textualFieldIntent.getName())).moveTo(writeTextualValueState)
                .when(context -> fieldIntentName[0].equals(Intents.numericFieldIntent.getName())).moveTo(writeNumericValueState)
                .when(context -> fieldIntentName[0].equals(Intents.dateFieldIntent.getName())).moveTo(writeDateValueState);

        // Input the VALUE
        // Divided by data types for safety (e.g. a date may be recognized as a text if we don't separate data types)

        writeTextualValueState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("WriteTextualValue"));
                })
                .next()
                .when(intentIs(coreLibraryI18n.AnyValue)).moveTo(saveStructuredFilterState);

        writeNumericValueState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("WriteNumericValue"));
                })
                .next()
                .when(intentIs(coreLibraryI18n.NumberValue)).moveTo(saveStructuredFilterState);

        writeDateValueState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("WriteDateValue"));
                })
                .next()
                .when(intentIs(coreLibraryI18n.DateValue)).moveTo(saveStructuredFilterState);

        // Finally, save the filter, composed by a FIELD, an OPERATOR, and a VALUE

        saveStructuredFilterState
                .body(context -> {
                    String field = (String) context.getSession().get(ContextKeys.LAST_FIELD);
                    String operator = (String) context.getSession().get(ContextKeys.LAST_OPERATOR);
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
                        Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                        statement.addFilter(field, operator, value);
                        ResultSet resultSet = statement.executeQuery();
                        context.getSession().put(ContextKeys.RESULT_SET, resultSet);
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("FilterAdded"),
                                field, operator, value, resultSet.getNumRows()));
                    } else {
                        reactPlatform.reply(context, messages.getString("SomethingWentWrong"));
                    }
                })
                .next()
                .moveTo(returnState);

        this.selectFieldState = selectFieldState.getState();

        // The Remove Filter workflow

        val selectFilterToRemoveState = state("SelectFilterToRemove");
        val removeFilterState = state("RemoveFilter");

        selectFilterToRemoveState
                .body(context -> {
                    Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                    List<String> currentFilters = statement.getFiltersAsStrings();
                    if (currentFilters.isEmpty()) {
                        reactPlatform.reply(context, messages.getString("NoFilters"),
                                Utils.getFirstTrainingSentences(coreLibraryI18n.Quit));
                    } else {
                        currentFilters.add(Utils.getFirstTrainingSentences(coreLibraryI18n.Quit).get(0));
                        reactPlatform.reply(context, messages.getString("SelectFilter"), currentFilters);
                    }
                })
                .next()
                .when(intentIs(Intents.customNumericFilterIntent)).moveTo(removeFilterState)
                .when(intentIs(Intents.customDateFilterIntent)).moveTo(removeFilterState)
                .when(intentIs(Intents.customTextualFilterIntent)).moveTo(removeFilterState)
                .when(intentIs(coreLibraryI18n.Quit)).moveTo(returnState);

        removeFilterState
                .body(context -> {
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    String operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    String value = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
                        Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                        statement.removeFilter(field, operator, value);
                        ResultSet resultSet = statement.executeQuery();
                        context.getSession().put(ContextKeys.RESULT_SET, resultSet);
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("FilterRemoved"),
                                field, operator, value, resultSet.getNumRows()));
                    } else {
                        reactPlatform.reply(context, messages.getString("SomethingWentWrong"));
                    }
                })
                .next()
                .moveTo(returnState);

        this.selectFilterToRemoveState = selectFilterToRemoveState.getState();
    }
}
