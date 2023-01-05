package com.xatkit.bot.customQuery;

import com.xatkit.bot.Bot;
import com.xatkit.bot.getResult.GetResult;
import com.xatkit.bot.library.ContextKeys;
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
 *     <li>{@link CustomRowCount}</li>
 *     <li>{@link CustomSelectFieldsWithConditions}</li>
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
     * The Custom Row Count workflow.
     */
    public CustomRowCount customRowCount;

    /**
     * The Custom Select Fields With Conditions workflow.
     */
    public CustomSelectFieldsWithConditions customSelectFieldsWithConditions;

    /**
     * The Specify Entities workflow.
     */
    public SpecifyEntities specifyEntities;

    /**
     * Instantiates a new Custom Query workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomQuery(Bot bot, State returnState) {
        val awaitingCustomQueryState = state("AwaitingCustomQuery");
        val redirectCustomQueryState = state("RedirectCustomQuery");

        specifyEntities = new SpecifyEntities(bot, redirectCustomQueryState.getState());

        customFilter = new CustomFilter(bot, returnState);
        customShowFieldDistinct = new CustomShowFieldDistinct(bot, returnState);
        customFrequentValueInField = new CustomFrequentValueInField(bot, returnState);
        customValueFrequency = new CustomValueFrequency(bot, returnState);
        customValue1vsValue2 = new CustomValue1vsValue2(bot, returnState);
        customRowCount = new CustomRowCount(bot, returnState);
        customSelectFieldsWithConditions = new CustomSelectFieldsWithConditions(bot, returnState);

        awaitingCustomQueryState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("WriteYourQuery"));
                })
                .next()
                .when(intentIs(bot.intents.customShowFieldDistinctIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.customMostFrequentValueInFieldIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.customLeastFrequentValueInFieldIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.customValueFrequencyIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.customValue1MoreThanValue2Intent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.customValue1LessThanValue2Intent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.customRowCountIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.customSelectFieldsWithConditionsIntent)).moveTo(specifyEntities.getCheckEntitiesState())

                .when(intentIs(bot.intents.customNumericFilterIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.customDateFilterIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.customTextualFilterIntent)).moveTo(specifyEntities.getCheckEntitiesState())

                .when(intentIs(bot.intents.showDataIntent)).moveTo(bot.getResult.getGenerateResultSetState())
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState)
                .when(intentIs(bot.coreLibraryI18n.AnyValue)).moveTo(bot.getResult.getGenerateResultSetFromQueryState());

        redirectCustomQueryState
                .body(context -> { })
                .next()
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customShowFieldDistinctIntent.getName())).moveTo(customShowFieldDistinct.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customMostFrequentValueInFieldIntent.getName())).moveTo(customFrequentValueInField.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customLeastFrequentValueInFieldIntent.getName())).moveTo(customFrequentValueInField.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customValueFrequencyIntent.getName())).moveTo(customValueFrequency.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customValue1MoreThanValue2Intent.getName())).moveTo(customValue1vsValue2.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customValue1LessThanValue2Intent.getName())).moveTo(customValue1vsValue2.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customRowCountIntent.getName())).moveTo(customRowCount.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customSelectFieldsWithConditionsIntent.getName())).moveTo(customSelectFieldsWithConditions.getMainState())

                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customNumericFilterIntent.getName())).moveTo(customFilter.getSaveCustomFilterState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customDateFilterIntent.getName())).moveTo(customFilter.getSaveCustomFilterState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.customTextualFilterIntent.getName())).moveTo(customFilter.getSaveCustomFilterState());

        this.awaitingCustomQueryState = awaitingCustomQueryState.getState();
    }
}
