package com.xatkit.bot.library;

import com.xatkit.i18n.XatkitI18nHelper;
import com.xatkit.intent.IntentDefinition;

import java.util.Locale;

import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.dateTime;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.number;

/**
 * A set of intents the chatbot can recognize.
 */
public class Intents {

    /**
     * A container of the training sentences for each intent in a specific language.
     */
    private XatkitI18nHelper BUNDLE;

    /**
     * The intent resetIntent.
     */
    public final IntentDefinition resetIntent;
    /**
     * The intent showDataIntent.
     */
    public final IntentDefinition showDataIntent;
    /**
     * The intent showAllIntent.
     */
    public final IntentDefinition showAllIntent;
    /**
     * The intent showAllDistinctIntent.
     */
    public final IntentDefinition showAllDistinctIntent;
    /**
     * The intent showNextPageIntent.
     */
    public final IntentDefinition showNextPageIntent;
    /**
     * The intent showPreviousPageIntent.
     */
    public final IntentDefinition showPreviousPageIntent;
    /**
     * The intent addFilterIntent.
     */
    public final IntentDefinition addFilterIntent;
    /**
     * The intent removeFilterIntent.
     */
    public final IntentDefinition removeFilterIntent;
    /**
     * The intent addFieldToViewIntent.
     */
    public final IntentDefinition addFieldToViewIntent;
    /**
     * The intent structuredQueryIntent.
     */
    public final IntentDefinition structuredQueryIntent;
    /**
     * The intent customQueryIntent.
     */
    public final IntentDefinition customQueryIntent;
    /**
     * The intent anotherQueryIntent.
     */
    public final IntentDefinition anotherQueryIntent;
    /**
     * The intent iDontKnowIntent.
     */
    public final IntentDefinition iDontKnowIntent;


    /**
     * The intent numericFieldIntent.
     */
    public final IntentDefinition numericFieldIntent;
    /**
     * The intent textualFieldIntent.
     */
    public final IntentDefinition textualFieldIntent;
    /**
     * The intent datetimeFieldIntent.
     */
    public final IntentDefinition datetimeFieldIntent;
    /**
     * The intent fieldIntent.
     */
    public final IntentDefinition fieldIntent;


    /**
     * The intent numericOperatorIntent.
     */
    public final IntentDefinition numericOperatorIntent;
    /**
     * The intent textualOperatorIntent.
     */
    public final IntentDefinition textualOperatorIntent;
    /**
     * The intent datetimeOperatorIntent.
     */
    public final IntentDefinition datetimeOperatorIntent;

    /**
     * The intent numericFunctionOperatorIntent.
     */
    public final IntentDefinition numericFunctionOperatorIntent;
    /**
     * The intent datetimeFunctionOperatorIntent.
     */
    public final IntentDefinition datetimeFunctionOperatorIntent;


    /**
     * The intent showFieldDistinctIntent.
     */
    public final IntentDefinition showFieldDistinctIntent;


    /**
     * The intent mostFrequentValueInFieldIntent.
     */
    public final IntentDefinition mostFrequentValueInFieldIntent;
    /**
     * The intent leastFrequentValueInFieldIntent.
     */
    public final IntentDefinition leastFrequentValueInFieldIntent;


    /**
     * The intent valueFrequencyIntent.
     */
    public final IntentDefinition valueFrequencyIntent;


    /**
     * The intent value1MoreThanValue2Intent.
     */
    public final IntentDefinition value1MoreThanValue2Intent;
    /**
     * The intent value1LessThanValue2Intent.
     */
    public final IntentDefinition value1LessThanValue2Intent;


    /**
     * The intent rowCountIntent.
     */
    public final IntentDefinition rowCountIntent;


    /**
     * The intent selectFieldsWithConditionsIntent.
     */
    public final IntentDefinition selectFieldsWithConditionsIntent;


    /**
     * The intent numericFieldOperatorValueIntent.
     */
    public final IntentDefinition numericFieldOperatorValueIntent;
    /**
     * The intent datetimeFieldOperatorValueIntent.
     */
    public final IntentDefinition datetimeFieldOperatorValueIntent;
    /**
     * The intent textualFieldOperatorValueIntent.
     */
    public final IntentDefinition textualFieldOperatorValueIntent;


    public final IntentDefinition numericFieldBetweenValuesIntent;
    public final IntentDefinition datetimeFieldBetweenValuesIntent;


    /**
     * Instantiates a new {@link Intents} object.
     *
     * @param entities the entities to use in the intents
     * @param locale   the locale to get the intents' training sentences in a specific language
     */
    public Intents(Entities entities, Locale locale) {
        BUNDLE = new XatkitI18nHelper("intents", locale);

        resetIntent = intent("Reset")
                .trainingSentences(BUNDLE.getStringArray("Reset"))
                .getIntentDefinition();
        showDataIntent = intent("ShowData")
                .trainingSentences(BUNDLE.getStringArray("ShowData"))
                .getIntentDefinition();
        showAllIntent = intent("ShowAll")
                .trainingSentences(BUNDLE.getStringArray("ShowAll"))
                .getIntentDefinition();
        showAllDistinctIntent = intent("ShowAllDistinct")
                .trainingSentences(BUNDLE.getStringArray("ShowAllDistinct"))
                .getIntentDefinition();
        showNextPageIntent = intent("ShowNextPage")
                .trainingSentences(BUNDLE.getStringArray("ShowNextPage"))
                .getIntentDefinition();
        showPreviousPageIntent = intent("ShowPreviousPage")
                .trainingSentences(BUNDLE.getStringArray("ShowPreviousPage"))
                .getIntentDefinition();
        addFilterIntent = intent("AddFilter")
                .trainingSentences(BUNDLE.getStringArray("AddFilter"))
                .getIntentDefinition();
        removeFilterIntent = intent("RemoveFilter")
                .trainingSentences(BUNDLE.getStringArray("RemoveFilter"))
                .getIntentDefinition();
        addFieldToViewIntent = intent("AddFieldToView")
                .trainingSentences(BUNDLE.getStringArray("AddFieldToView"))
                .getIntentDefinition();
        structuredQueryIntent = intent("StructuredQuery")
                .trainingSentences(BUNDLE.getStringArray("StructuredQuery"))
                .getIntentDefinition();
        customQueryIntent = intent("CustomQuery")
                .trainingSentences(BUNDLE.getStringArray("CustomQuery"))
                .getIntentDefinition();
        anotherQueryIntent = intent("AnotherQuery")
                .trainingSentences(BUNDLE.getStringArray("AnotherQuery"))
                .getIntentDefinition();
        iDontKnowIntent = intent("IDontKnow")
                .trainingSentences(BUNDLE.getStringArray("IDontKnow"))
                .getIntentDefinition();


        numericFieldIntent = intent("NumericField")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.numericFieldEntity)
                .getIntentDefinition();
        textualFieldIntent = intent("TextualField")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.textualFieldEntity)
                .getIntentDefinition();
        datetimeFieldIntent = intent("DatetimeField")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.datetimeFieldEntity)
                .getIntentDefinition();
        fieldIntent = intent("Field")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.fieldEntity)
                .getIntentDefinition();


        numericOperatorIntent = intent("NumericOperator")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.numericOperatorEntity)
                .getIntentDefinition();
        textualOperatorIntent = intent("TextualOperator")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.textualOperatorEntity)
                .getIntentDefinition();
        datetimeOperatorIntent = intent("DatetimeOperator")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.datetimeOperatorEntity)
                .getIntentDefinition();

        numericFunctionOperatorIntent = intent("NumericFunctionOperator")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.numericFunctionOperatorEntity)
                .getIntentDefinition();
        datetimeFunctionOperatorIntent = intent("DatetimeFunctionOperator")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.datetimeFunctionOperatorEntity)
                .getIntentDefinition();


        showFieldDistinctIntent = intent("ShowFieldDistinct")
                .trainingSentences(BUNDLE.getStringArray("ShowFieldDistinct"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.fieldEntity)
                .getIntentDefinition();


        mostFrequentValueInFieldIntent = intent("MostFrequentValueInField")
                .trainingSentences(BUNDLE.getStringArray("MostFrequentValueInField"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.fieldEntity)
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .getIntentDefinition();
        leastFrequentValueInFieldIntent = intent("LeastFrequentValueInField")
                .trainingSentences(BUNDLE.getStringArray("LeastFrequentValueInField"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.fieldEntity)
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .getIntentDefinition();


        valueFrequencyIntent = intent("ValueFrequency")
                .trainingSentences(BUNDLE.getStringArray("ValueFrequency"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.fieldValueEntity)
                .getIntentDefinition();


        value1MoreThanValue2Intent = intent("Value1MoreThanValue2")
                .trainingSentences(BUNDLE.getStringArray("Value1MoreThanValue2"))
                .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(entities.fieldValueEntity)
                .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(entities.fieldValueEntity)
                .getIntentDefinition();
        value1LessThanValue2Intent = intent("Value1LessThanValue2")
                .trainingSentences(BUNDLE.getStringArray("Value1LessThanValue2"))
                .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(entities.fieldValueEntity)
                .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(entities.fieldValueEntity)
                .getIntentDefinition();


        rowCountIntent = intent("RowCount")
                .trainingSentences(BUNDLE.getStringArray("RowCount"))
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .getIntentDefinition();


        selectFieldsWithConditionsIntent = intent("SelectFieldsWithConditions")
                .trainingSentences(BUNDLE.getStringArray("SelectFieldsWithConditions"))
                .parameter(ContextKeys.NUMBER).fromFragment("NUMBER").entity(number())
                .parameter(ContextKeys.FIELD + "1").fromFragment("FIELD1").entity(entities.fieldEntity)
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.functionOperatorEntity)
                .parameter(ContextKeys.FIELD + "2").fromFragment("FIELD2").entity(entities.fieldEntity)
                .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(entities.fieldValueEntity)
                .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(entities.fieldValueEntity)
                .getIntentDefinition();


        numericFieldOperatorValueIntent = intent("NumericFieldOperatorValue")
                .trainingSentences(BUNDLE.getStringArray("FieldOperatorValue"))
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.numericFieldEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.numericOperatorEntity)
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(number())
                .getIntentDefinition();
        datetimeFieldOperatorValueIntent = intent("DatetimeFieldOperatorValue")
                .trainingSentences(BUNDLE.getStringArray("FieldOperatorValue"))
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.datetimeFieldEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.datetimeOperatorEntity)
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(dateTime())
                .getIntentDefinition();
        textualFieldOperatorValueIntent = intent("TextualFieldOperatorValue")
                .trainingSentences(BUNDLE.getStringArray("FieldOperatorValue"))
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.textualFieldEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.textualOperatorEntity)
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(any())
                .getIntentDefinition();


        numericFieldBetweenValuesIntent = intent("NumericFieldBetweenValues")
                .trainingSentences(BUNDLE.getStringArray("FieldBetweenValues"))
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.numericFieldEntity)
                .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(number())
                .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(number())
                .getIntentDefinition();
        datetimeFieldBetweenValuesIntent = intent("DatetimeFieldBetweenValues")
                .trainingSentences(BUNDLE.getStringArray("FieldBetweenValues"))
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.datetimeFieldEntity)
                .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(dateTime())
                .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(dateTime())
                .getIntentDefinition();
    }
}
