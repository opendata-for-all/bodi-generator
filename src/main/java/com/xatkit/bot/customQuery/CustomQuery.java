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
 *     <li>{@link ShowFieldDistinct}</li>
 *     <li>{@link FrequentValueInField}</li>
 *     <li>{@link ValueFrequency}</li>
 *     <li>{@link Value1vsValue2}</li>
 *     <li>{@link RowCount}</li>
 *     <li>{@link SelectFieldsWithConditions}</li>
 *     <li>{@link FieldOperatorValue}</li>
 *     <li>{@link FieldBetweenValues}</li>
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
     * The Show Field Distinct workflow.
     */
    public ShowFieldDistinct showFieldDistinct;

    /**
     * The Frequent Value In Field workflow.
     */
    public FrequentValueInField frequentValueInField;

    /**
     * The Value Frequency workflow.
     */
    public ValueFrequency valueFrequency;

    /**
     * The Value1 vs Value2 workflow.
     */
    public Value1vsValue2 value1VsValue2;

    /**
     * The Row Count workflow.
     */
    public RowCount rowCount;

    /**
     * The Select Fields With Conditions workflow.
     */
    public SelectFieldsWithConditions selectFieldsWithConditions;

    /**
     * The Field Operator Value workflow.
     */
    public FieldOperatorValue fieldOperatorValue;

    /**
     * The Field Between Values workflow.
     */
    public FieldBetweenValues fieldBetweenValues;

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

        showFieldDistinct = new ShowFieldDistinct(bot, returnState);
        frequentValueInField = new FrequentValueInField(bot, returnState);
        valueFrequency = new ValueFrequency(bot, returnState);
        value1VsValue2 = new Value1vsValue2(bot, returnState);
        rowCount = new RowCount(bot, returnState);
        selectFieldsWithConditions = new SelectFieldsWithConditions(bot, returnState);
        fieldOperatorValue = new FieldOperatorValue(bot, returnState);
        fieldBetweenValues = new FieldBetweenValues(bot, returnState);

        awaitingCustomQueryState
                .body(context -> {
                    bot.reactPlatform.reply(context, bot.messages.getString("WriteYourQuery"));
                })
                .next()
                .when(intentIs(bot.intents.showFieldDistinctIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.mostFrequentValueInFieldIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.leastFrequentValueInFieldIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.valueFrequencyIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.value1MoreThanValue2Intent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.value1LessThanValue2Intent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.rowCountIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.selectFieldsWithConditionsIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.numericFieldOperatorValueIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.datetimeFieldOperatorValueIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                // TODO: FOR TEXTUAL VALUES WE NEED TO IMPLEMENT ANY SYSTEM ENTITY
                //.when(intentIs(bot.intents.textualFieldOperatorValueIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.numericFieldBetweenValuesIntent)).moveTo(specifyEntities.getCheckEntitiesState())
                .when(intentIs(bot.intents.datetimeFieldBetweenValuesIntent)).moveTo(specifyEntities.getCheckEntitiesState())

                .when(intentIs(bot.intents.showDataIntent)).moveTo(bot.getResult.getGenerateResultSetState())
                .when(intentIs(bot.coreLibraryI18n.Quit)).moveTo(returnState)
                .when(intentIs(bot.coreLibraryI18n.AnyValue)).moveTo(bot.getResult.getGenerateResultSetFromQueryState());

        redirectCustomQueryState
                .body(context -> { })
                .next()
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.showFieldDistinctIntent.getName())).moveTo(showFieldDistinct.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.mostFrequentValueInFieldIntent.getName())).moveTo(frequentValueInField.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.leastFrequentValueInFieldIntent.getName())).moveTo(frequentValueInField.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.valueFrequencyIntent.getName())).moveTo(valueFrequency.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.value1MoreThanValue2Intent.getName())).moveTo(value1VsValue2.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.value1LessThanValue2Intent.getName())).moveTo(value1VsValue2.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.rowCountIntent.getName())).moveTo(rowCount.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.selectFieldsWithConditionsIntent.getName())).moveTo(selectFieldsWithConditions.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.numericFieldOperatorValueIntent.getName())).moveTo(fieldOperatorValue.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.datetimeFieldOperatorValueIntent.getName())).moveTo(fieldOperatorValue.getMainState())
                // TODO: FOR TEXTUAL VALUES WE NEED TO IMPLEMENT ANY SYSTEM ENTITY
                //.when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.textualFieldOperatorValueIntent.getName())).moveTo(fieldOperatorValue.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.numericFieldBetweenValuesIntent.getName())).moveTo(fieldBetweenValues.getMainState())
                .when(context -> context.getSession().get(ContextKeys.INTENT_NAME).equals(bot.intents.datetimeFieldBetweenValuesIntent.getName())).moveTo(fieldBetweenValues.getMainState());

        this.awaitingCustomQueryState = awaitingCustomQueryState.getState();
    }
}
