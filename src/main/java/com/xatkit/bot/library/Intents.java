package com.xatkit.bot.library;

import com.xatkit.bot.Bot;
import com.xatkit.i18n.XatkitI18nHelper;
import com.xatkit.intent.IntentDefinition;

import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.date;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.number;

/**
 * A set of intents the chatbot can recognize.
 */
public final class Intents {

    private Intents() {
    }

    /**
     * A container of the training sentences for each intent in a specific language given by the {@link Bot#locale}.
     */
    private static final XatkitI18nHelper BUNDLE = new XatkitI18nHelper("intents", Bot.locale);


    /**
     * The intent restartIntent.
     */
    public static final IntentDefinition restartIntent = intent("Restart")
            .trainingSentences(BUNDLE.getStringArray("Restart"))
            .getIntentDefinition();
    /**
     * The intent showDataIntent.
     */
    public static final IntentDefinition showDataIntent = intent("ShowData")
            .trainingSentences(BUNDLE.getStringArray("ShowData"))
            .getIntentDefinition();
    /**
     * The intent showAllIntent.
     */
    public static final IntentDefinition showAllIntent = intent("ShowAll")
            .trainingSentences(BUNDLE.getStringArray("ShowAll"))
            .getIntentDefinition();
    /**
     * The intent showAllDistinctIntent.
     */
    public static final IntentDefinition showAllDistinctIntent = intent("ShowAllDistinct")
            .trainingSentences(BUNDLE.getStringArray("ShowAllDistinct"))
            .getIntentDefinition();
    /**
     * The intent showNextPageIntent.
     */
    public static final IntentDefinition showNextPageIntent = intent("ShowNextPage")
            .trainingSentences(BUNDLE.getStringArray("ShowNextPage"))
            .getIntentDefinition();
    /**
     * The intent addFilterIntent.
     */
    public static final IntentDefinition addFilterIntent = intent("AddFilter")
            .trainingSentences(BUNDLE.getStringArray("AddFilter"))
            .getIntentDefinition();
    /**
     * The intent removeFilterIntent.
     */
    public static final IntentDefinition removeFilterIntent = intent("RemoveFilter")
            .trainingSentences(BUNDLE.getStringArray("RemoveFilter"))
            .getIntentDefinition();
    /**
     * The intent addFieldToViewIntent.
     */
    public static final IntentDefinition addFieldToViewIntent = intent("AddFieldToView")
            .trainingSentences(BUNDLE.getStringArray("AddFieldToView"))
            .getIntentDefinition();
    /**
     * The intent structuredQueryIntent.
     */
    public static final IntentDefinition structuredQueryIntent = intent("StructuredQuery")
            .trainingSentences(BUNDLE.getStringArray("StructuredQuery"))
            .getIntentDefinition();
    /**
     * The intent customQueryIntent.
     */
    public static final IntentDefinition customQueryIntent = intent("CustomQuery")
            .trainingSentences(BUNDLE.getStringArray("CustomQuery"))
            .getIntentDefinition();
    /**
     * The intent anotherQueryIntent.
     */
    public static final IntentDefinition anotherQueryIntent = intent("AnotherQuery")
            .trainingSentences(BUNDLE.getStringArray("AnotherQuery"))
            .getIntentDefinition();
    /**
     * The intent iDontKnowIntent.
     */
    public static final IntentDefinition iDontKnowIntent = intent("IDontKnow")
            .trainingSentences(BUNDLE.getStringArray("IDontKnow"))
            .getIntentDefinition();


    /**
     * The intent numericFieldIntent.
     */
    public static final IntentDefinition numericFieldIntent = intent("NumericField")
            .trainingSentences(BUNDLE.getStringArray("Value"))
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.numericFieldEntity)
            .getIntentDefinition();
    /**
     * The intent textualFieldIntent.
     */
    public static final IntentDefinition textualFieldIntent = intent("TextualField")
            .trainingSentences(BUNDLE.getStringArray("Value"))
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.textualFieldEntity)
            .getIntentDefinition();
    /**
     * The intent dateFieldIntent.
     */
    public static final IntentDefinition dateFieldIntent = intent("DateField")
            .trainingSentences(BUNDLE.getStringArray("Value"))
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.dateFieldEntity)
            .getIntentDefinition();


    /**
     * The intent numericOperatorIntent.
     */
    public static final IntentDefinition numericOperatorIntent = intent("NumericOperator")
            .trainingSentences(BUNDLE.getStringArray("Value"))
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.numericOperatorEntity)
            .getIntentDefinition();
    /**
     * The intent textualOperatorIntent.
     */
    public static final IntentDefinition textualOperatorIntent = intent("TextualOperator")
            .trainingSentences(BUNDLE.getStringArray("Value"))
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.textualOperatorEntity)
            .getIntentDefinition();
    /**
     * The intent dateOperatorIntent.
     */
    public static final IntentDefinition dateOperatorIntent = intent("DateOperator")
            .trainingSentences(BUNDLE.getStringArray("Value"))
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.dateOperatorEntity)
            .getIntentDefinition();

    /**
     * The intent numericFunctionOperatorIntent.
     */
    public static final IntentDefinition numericFunctionOperatorIntent = intent("NumericFunctionOperator")
            .trainingSentences(BUNDLE.getStringArray("Value"))
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.numericFunctionOperatorEntity)
            .getIntentDefinition();
    /**
     * The intent dateFunctionOperatorIntent.
     */
    public static final IntentDefinition dateFunctionOperatorIntent = intent("DateFunctionOperator")
            .trainingSentences(BUNDLE.getStringArray("Value"))
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.dateFunctionOperatorEntity)
            .getIntentDefinition();



    /**
     * The intent customNumericFilterIntent.
     */
    public static final IntentDefinition customNumericFilterIntent = intent("CustomNumericFilter")
            .trainingSentences(BUNDLE.getStringArray("CustomFilter"))
            .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(Entities.numericFieldEntity)
            .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(Entities.numericOperatorEntity)
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(number())
            .getIntentDefinition();
    /**
     * The intent customDateFilterIntent.
     */
    public static final IntentDefinition customDateFilterIntent = intent("CustomDateFilter")
            .trainingSentences(BUNDLE.getStringArray("CustomFilter"))
            .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(Entities.dateFieldEntity)
            .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(Entities.dateOperatorEntity)
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(date())
            .getIntentDefinition();
    /**
     * The intent customTextualFilterIntent.
     */
    public static final IntentDefinition customTextualFilterIntent = intent("CustomTextualFilter")
            .trainingSentences(BUNDLE.getStringArray("CustomFilter"))
            .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(Entities.textualFieldEntity)
            .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(Entities.textualOperatorEntity)
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(any())
            .getIntentDefinition();


    /**
     * The intent customShowFieldDistinctIntent.
     */
    public static final IntentDefinition customShowFieldDistinctIntent = intent("CustomShowFieldDistinct")
            .trainingSentences(BUNDLE.getStringArray("CustomShowFieldDistinct"))
            .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(Entities.fieldEntity)
            .getIntentDefinition();


    /**
     * The intent customMostFrequentValueInFieldIntent.
     */
    public static final IntentDefinition customMostFrequentValueInFieldIntent = intent("CustomMostFrequentValueInField")
            .trainingSentences(BUNDLE.getStringArray("CustomMostFrequentValueInField"))
            .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(Entities.fieldEntity)
            .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(Entities.rowNameEntity)
            .getIntentDefinition();
    /**
     * The intent customLeastFrequentValueInFieldIntent.
     */
    public static final IntentDefinition customLeastFrequentValueInFieldIntent = intent("CustomLeastFrequentValueInField")
            .trainingSentences(BUNDLE.getStringArray("CustomLeastFrequentValueInField"))
            .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(Entities.fieldEntity)
            .getIntentDefinition();


    /**
     * The intent customValueFrequencyIntent.
     */
    public static final IntentDefinition customValueFrequencyIntent = intent("CustomValueFrequency")
            .trainingSentences(BUNDLE.getStringArray("CustomValueFrequency"))
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.fieldValueEntity)
            .getIntentDefinition();


    /**
     * The intent customValue1MoreThanValue2Intent.
     */
    public static final IntentDefinition customValue1MoreThanValue2Intent = intent("CustomValue1MoreThanValue2")
            .trainingSentences(BUNDLE.getStringArray("CustomValue1MoreThanValue2"))
            .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(Entities.fieldValueEntity)
            .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(Entities.fieldValueEntity)
            .getIntentDefinition();
    /**
     * The intent customValue1LessThanValue2Intent.
     */
    public static final IntentDefinition customValue1LessThanValue2Intent = intent("CustomValue1LessThanValue2")
            .trainingSentences(BUNDLE.getStringArray("CustomValue1LessThanValue2"))
            .parameter(ContextKeys.VALUE + "1").fromFragment("VALUE1").entity(Entities.fieldValueEntity)
            .parameter(ContextKeys.VALUE + "2").fromFragment("VALUE2").entity(Entities.fieldValueEntity)
            .getIntentDefinition();


    /**
     * The intent customNumericFieldFunctionIntent.
     */
    public static final IntentDefinition customNumericFieldFunctionIntent = intent("CustomNumericFieldFunction")
            .trainingSentences(BUNDLE.getStringArray("CustomNumericFieldFunction"))
            .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(Entities.numericFieldEntity)
            .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(Entities.numericFunctionOperatorEntity)
            .getIntentDefinition();


    /**
     * The intent customRowCountIntent.
     */
    public static final IntentDefinition customRowCountIntent = intent("CustomRowCount")
            .trainingSentences(BUNDLE.getStringArray("CustomRowCount"))
            .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(Entities.rowNameEntity)
            .getIntentDefinition();


    /**
     * The intent customFieldOfValueIntent.
     */
    public static final IntentDefinition customFieldOfValueIntent = intent("CustomFieldOfValue")
            .trainingSentences(BUNDLE.getStringArray("CustomFieldOfValue"))
            .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(Entities.fieldEntity)
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.fieldValueEntity)
            .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(Entities.rowNameEntity)
            .getIntentDefinition();
    /**
     * The intent customFieldOfValueOperatorIntent.
     */
    public static final IntentDefinition customFieldOfValueOperatorIntent = intent("CustomFieldOfValueOperator")
            .trainingSentences(BUNDLE.getStringArray("CustomFieldOfValueOperator"))
            .parameter(ContextKeys.FIELD).fromFragment("FIELD").entity(Entities.fieldEntity)
            .parameter(ContextKeys.OPERATOR).fromFragment("OPERATOR").entity(Entities.functionOperatorEntity)
            .parameter(ContextKeys.VALUE).fromFragment("VALUE").entity(Entities.fieldValueEntity)
            .parameter(ContextKeys.ROW_NAME).fromFragment("ROW_NAME").entity(Entities.rowNameEntity)
            .getIntentDefinition();
}
