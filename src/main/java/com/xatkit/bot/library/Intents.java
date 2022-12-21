package com.xatkit.bot.library;

import com.xatkit.i18n.XatkitI18nHelper;
import com.xatkit.intent.IntentDefinition;

import java.util.Locale;

import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.date;
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
     * The intent dateFieldIntent.
     */
    public final IntentDefinition dateFieldIntent;
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
     * The intent dateOperatorIntent.
     */
    public final IntentDefinition dateOperatorIntent;

    /**
     * The intent numericFunctionOperatorIntent.
     */
    public final IntentDefinition numericFunctionOperatorIntent;
    /**
     * The intent dateFunctionOperatorIntent.
     */
    public final IntentDefinition dateFunctionOperatorIntent;


    /**
     * The intent customNumericFilterIntent.
     */
    public final IntentDefinition customNumericFilterIntent;
    /**
     * The intent customDateFilterIntent.
     */
    public final IntentDefinition customDateFilterIntent;
    /**
     * The intent customTextualFilterIntent.
     */
    public final IntentDefinition customTextualFilterIntent;


    /**
     * The intent customShowFieldDistinctIntent.
     */
    public final IntentDefinition customShowFieldDistinctIntent;


    /**
     * The intent customMostFrequentValueInFieldIntent.
     */
    public final IntentDefinition customMostFrequentValueInFieldIntent;
    /**
     * The intent customLeastFrequentValueInFieldIntent.
     */
    public final IntentDefinition customLeastFrequentValueInFieldIntent;


    /**
     * The intent customValueFrequencyIntent.
     */
    public final IntentDefinition customValueFrequencyIntent;


    /**
     * The intent customValue1MoreThanValue2Intent.
     */
    public final IntentDefinition customValue1MoreThanValue2Intent;
    /**
     * The intent customValue1LessThanValue2Intent.
     */
    public final IntentDefinition customValue1LessThanValue2Intent;


    /**
     * The intent customFieldFunctionIntent.
     */
    public final IntentDefinition customFieldFunctionIntent;


    /**
     * The intent customRowOfFieldFunctionIntent.
     */
    public final IntentDefinition customRowOfFieldFunctionIntent;


    /**
     * The intent customRowCountIntent.
     */
    public final IntentDefinition customRowCountIntent;


    /**
     * The intent customFieldOfValueIntent.
     */
    public final IntentDefinition customFieldOfValueIntent;

    /**
     * The intent customRowOfValuesIntent.
     */
    public final IntentDefinition customRowOfValuesIntent;

    /**
     * The intent customFieldOfFieldFunctionIntent.
     */
    public final IntentDefinition customFieldOfFieldFunctionIntent;

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
        dateFieldIntent = intent("DateField")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.dateFieldEntity)
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
        dateOperatorIntent = intent("DateOperator")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.dateOperatorEntity)
                .getIntentDefinition();

        numericFunctionOperatorIntent = intent("NumericFunctionOperator")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.numericFunctionOperatorEntity)
                .getIntentDefinition();
        dateFunctionOperatorIntent = intent("DateFunctionOperator")
                .trainingSentences(BUNDLE.getStringArray("Value"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.dateFunctionOperatorEntity)
                .getIntentDefinition();


        customNumericFilterIntent = intent("CustomNumericFilter")
                .trainingSentences(BUNDLE.getStringArray("CustomFilter"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.numericFieldEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.numericOperatorEntity)
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(number())
                .getIntentDefinition();
        customDateFilterIntent = intent("CustomDateFilter")
                .trainingSentences(BUNDLE.getStringArray("CustomFilter"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.dateFieldEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.dateOperatorEntity)
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(date())
                .getIntentDefinition();
        customTextualFilterIntent = intent("CustomTextualFilter")
                .trainingSentences(BUNDLE.getStringArray("CustomFilter"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.textualFieldEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.textualOperatorEntity)
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(any())
                .getIntentDefinition();


        customShowFieldDistinctIntent = intent("CustomShowFieldDistinct")
                .trainingSentences(BUNDLE.getStringArray("CustomShowFieldDistinct"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.fieldEntity)
                .getIntentDefinition();


        customMostFrequentValueInFieldIntent = intent("CustomMostFrequentValueInField")
                .trainingSentences(BUNDLE.getStringArray("CustomMostFrequentValueInField"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.fieldEntity)
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .getIntentDefinition();
        customLeastFrequentValueInFieldIntent = intent("CustomLeastFrequentValueInField")
                .trainingSentences(BUNDLE.getStringArray("CustomLeastFrequentValueInField"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.fieldEntity)
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .getIntentDefinition();


        customValueFrequencyIntent = intent("CustomValueFrequency")
                .trainingSentences(BUNDLE.getStringArray("CustomValueFrequency"))
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(entities.fieldValueEntity)
                .getIntentDefinition();


        customValue1MoreThanValue2Intent = intent("CustomValue1MoreThanValue2")
                .trainingSentences(BUNDLE.getStringArray("CustomValue1MoreThanValue2"))
                .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(entities.fieldValueEntity)
                .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(entities.fieldValueEntity)
                .getIntentDefinition();
        customValue1LessThanValue2Intent = intent("CustomValue1LessThanValue2")
                .trainingSentences(BUNDLE.getStringArray("CustomValue1LessThanValue2"))
                .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(entities.fieldValueEntity)
                .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(entities.fieldValueEntity)
                .getIntentDefinition();


        customFieldFunctionIntent = intent("CustomFieldFunction")
                .trainingSentences(BUNDLE.getStringArray("CustomFieldFunction"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.numericFieldEntity)
                // TODO: Support date-time type operators
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.numericFunctionOperatorEntity)
                .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(entities.fieldValueEntity)
                .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(entities.fieldValueEntity)
                .getIntentDefinition();


        customRowOfFieldFunctionIntent = intent("CustomRowOfFieldFunction")
                .trainingSentences(BUNDLE.getStringArray("CustomRowOfFieldFunction"))
                // TODO: Support date-time type operators
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.numericFieldEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.numericFunctionOperatorEntity)
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .getIntentDefinition();


        customRowCountIntent = intent("CustomRowCount")
                .trainingSentences(BUNDLE.getStringArray("CustomRowCount"))
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .getIntentDefinition();


        customFieldOfValueIntent = intent("CustomFieldOfValue")
                .trainingSentences(BUNDLE.getStringArray("CustomFieldOfValue"))
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.fieldEntity)
                .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(entities.fieldValueEntity)
                .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(entities.fieldValueEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.functionOperatorEntity)
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .getIntentDefinition();

        customRowOfValuesIntent = intent("CustomRowOfValues")
                .trainingSentences(BUNDLE.getStringArray("CustomRowOfValues"))
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(entities.fieldValueEntity)
                .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(entities.fieldValueEntity)
                .parameter(ContextKeys.VALUE + "3").fromFragment("VALUE3").entity(entities.fieldValueEntity)
                .getIntentDefinition();

        customFieldOfFieldFunctionIntent = intent("CustomFieldOfFieldFunction")
                .trainingSentences(BUNDLE.getStringArray("CustomFieldOfFieldFunction"))
                .parameter(ContextKeys.NUMBER).fromFragment("NUMBER").entity(number())
                .parameter(ContextKeys.FIELD + "1").fromFragment("FIELD1").entity(entities.fieldEntity)
                // TODO: Support date-time type operators
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.numericFunctionOperatorEntity)
                .parameter(ContextKeys.FIELD + "2").fromFragment("FIELD2").entity(entities.numericFieldEntity)
                .getIntentDefinition();
    }
}
