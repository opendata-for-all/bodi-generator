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
     * The intent datetimeFieldOperatorValue.
     */
    public final IntentDefinition datetimeFieldOperatorValue;
    /**
     * The intent textualFieldOperatorValue.
     */
    public final IntentDefinition textualFieldOperatorValue;


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
        datetimeFieldOperatorValue = intent("DatetimeFieldOperatorValue")
                .trainingSentences(BUNDLE.getStringArray("FieldOperatorValue"))
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.dateFieldEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.dateOperatorEntity)
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(dateTime())
                .getIntentDefinition();
        textualFieldOperatorValue = intent("TextualFieldOperatorValue")
                .trainingSentences(BUNDLE.getStringArray("FieldOperatorValue"))
                .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(entities.rowNameEntity)
                .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(entities.textualFieldEntity)
                .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(entities.textualOperatorEntity)
                .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(any())
                .getIntentDefinition();
    }
}
