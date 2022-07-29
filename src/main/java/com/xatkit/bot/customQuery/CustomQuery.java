package com.xatkit.bot.customQuery;

import com.xatkit.bot.Bot;
import com.xatkit.bot.getResult.GetResult;
import com.xatkit.execution.State;
import lombok.Getter;
import lombok.val;

import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;

/**
 * The Custom Query workflow of a chatbot.
 * <p>
 * It allows the chatbot to recognize a "somehow free" query. The currently available kinds of query allowed through
 * this workflow are:
 * <ul>
 *     <li>{@link CustomFilter}</li>
 *     <li>{@link CustomShowFieldDistinct}</li>
 *     <li>{@link CustomFrequentValueInField}</li>
 *     <li>{@link CustomValueFrequency}</li>
 *     <li>{@link CustomValue1vsValue2}</li>
 *     <li>{@link CustomNumericFieldFunction}</li>
 *     <li>{@link CustomRowOfNumericFieldFunction}</li>
 *     <li>{@link CustomRowCount}</li>
 *     <li>{@link CustomFieldOfValue}</li>
 *     <li>{@link CustomRowOfValues}</li>
 * </ul>
 * When no pre-defined query is matched, it is executed {@link GetResult#getGenerateResultSetFromQueryState()}
 */
public class CustomQuery {

    /**
     * The entry point for the Custom Query workflow.
     */
    @Getter
    private final State awaitingCustomQueryState;

    /**
     * The Custom Filter workflow.
     */
    public CustomFilter customFilter;

    /**
     * The Custom Show Field Distinct workflow.
     */
    public CustomShowFieldDistinct customShowFieldDistinct;

    /**
     * The Custom Frequent Value In Field workflow.
     */
    public CustomFrequentValueInField customFrequentValueInField;

    /**
     * The Custom Value Frequency workflow.
     */
    public CustomValueFrequency customValueFrequency;

    /**
     * The Custom Value1 vs Value2 workflow.
     */
    public CustomValue1vsValue2 customValue1vsValue2;

    /**
     * The Custom Numeric Field Function workflow.
     */
    public CustomNumericFieldFunction customNumericFieldFunction;

    /**
     * The Custom Row Of Numeric Field Function workflow.
     */
    public CustomRowOfNumericFieldFunction customRowOfNumericFieldFunction;

    /**
     * The Custom Row Count workflow.
     */
    public CustomRowCount customRowCount;

    /**
     * The Custom Field Of Value workflow.
     */
    public CustomFieldOfValue customFieldOfValue;

    /**
     * The Custom Row Of Values workflow.
     */
    public CustomRowOfValues customRowOfValues;

    /**
     * Instantiates a new Custom Query workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomQuery(Bot bot, State returnState) {
        customFilter = new CustomFilter(bot, returnState);
        customShowFieldDistinct = new CustomShowFieldDistinct(bot, returnState);
        customFrequentValueInField = new CustomFrequentValueInField(bot, returnState);
        customValueFrequency = new CustomValueFrequency(bot, returnState);
        customValue1vsValue2 = new CustomValue1vsValue2(bot, returnState);
        customNumericFieldFunction = new CustomNumericFieldFunction(bot, returnState);
        customRowOfNumericFieldFunction = new CustomRowOfNumericFieldFunction(bot, returnState);
        customRowCount = new CustomRowCount(bot, returnState);
        customFieldOfValue = new CustomFieldOfValue(bot, returnState);
        customRowOfValues = new CustomRowOfValues(bot, returnState);

        val awaitingCustomQueryState = state("AwaitingCustomQuery");

        awaitingCustomQueryState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("WriteYourQuery"));
                })
                .next()
                .when(intentIs(bot.intents.customShowFieldDistinctIntent)).moveTo(customShowFieldDistinct.getProcessCustomShowFieldDistinctState())
                .when(intentIs(bot.intents.customMostFrequentValueInFieldIntent)).moveTo(customFrequentValueInField.getProcessCustomFrequentValueInFieldState())
                .when(intentIs(bot.intents.customLeastFrequentValueInFieldIntent)).moveTo(customFrequentValueInField.getProcessCustomFrequentValueInFieldState())
                .when(intentIs(bot.intents.customValueFrequencyIntent)).moveTo(customValueFrequency.getProcessCustomValueFrequencyState())
                .when(intentIs(bot.intents.customValue1MoreThanValue2Intent)).moveTo(customValue1vsValue2.getProcessCustomValue1vsValue2State())
                .when(intentIs(bot.intents.customValue1LessThanValue2Intent)).moveTo(customValue1vsValue2.getProcessCustomValue1vsValue2State())
                .when(intentIs(bot.intents.customNumericFieldFunctionIntent)).moveTo(customNumericFieldFunction.getProcessCustomNumericFieldFunctionState())
                .when(intentIs(bot.intents.customRowOfNumericFieldFunctionIntent)).moveTo(customRowOfNumericFieldFunction.getProcessCustomRowOfNumericFieldFunctionState())
                .when(intentIs(bot.intents.customRowCountIntent)).moveTo(customRowCount.getProcessCustomRowCountState())
                .when(intentIs(bot.intents.customFieldOfValueIntent)).moveTo(customFieldOfValue.getProcessCustomFieldOfValueState())
                .when(intentIs(bot.intents.customFieldOfValueOperatorIntent)).moveTo(customFieldOfValue.getProcessCustomFieldOfValueState())
                .when(intentIs(bot.intents.customRowOfValuesIntent)).moveTo(customRowOfValues.getProcessCustomRowOfValuesState())

                .when(intentIs(bot.intents.customNumericFilterIntent)).moveTo(customFilter.getSaveCustomFilterState())
                .when(intentIs(bot.intents.customDateFilterIntent)).moveTo(customFilter.getSaveCustomFilterState())
                .when(intentIs(bot.intents.customTextualFilterIntent)).moveTo(customFilter.getSaveCustomFilterState())
                .when(intentIs(bot.intents.showDataIntent)).moveTo(bot.getResult.getGenerateResultSetState())
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState)
                .when(intentIs(bot.coreLibraryI18n.AnyValue)).moveTo(bot.getResult.getGenerateResultSetFromQueryState());

        this.awaitingCustomQueryState = awaitingCustomQueryState.getState();
    }
}
