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
     * The intent addFieldToViewIntent.
     */
    public static final IntentDefinition addFieldToViewIntent = intent("AddFieldToView")
            .trainingSentences(BUNDLE.getStringArray("AddFieldToView"))
            .getIntentDefinition();
    /**
     * The intent customQueryIntent.
     */
    public static final IntentDefinition customQueryIntent = intent("CustomQuery")
            .trainingSentences(BUNDLE.getStringArray("CustomQuery"))
            .getIntentDefinition();
    /**
     * The intent customNumericFilterIntent.
     */
    public static final IntentDefinition customNumericFilterIntent = intent("CustomNumericFilter")
            .trainingSentences(BUNDLE.getStringArray("CustomFilter"))
            .parameter(ContextKeys.NUMERIC_FIELD_NAME).fromFragment("FIELD").entity(Entities.numericFieldEntity)
            .parameter(ContextKeys.NUMERIC_OPERATOR_NAME).fromFragment("OPERATOR").entity(Entities.numericOperatorEntity)
            .parameter(ContextKeys.NUMERIC_VALUE).fromFragment("VALUE").entity(number())
            .getIntentDefinition();
    /**
     * The intent customDateFilterIntent.
     */
    public static final IntentDefinition customDateFilterIntent = intent("CustomDateFilter")
            .trainingSentences(BUNDLE.getStringArray("CustomFilter"))
            .parameter(ContextKeys.DATE_FIELD_NAME).fromFragment("FIELD").entity(Entities.dateFieldEntity)
            .parameter(ContextKeys.DATE_OPERATOR_NAME).fromFragment("OPERATOR").entity(Entities.dateOperatorEntity)
            .parameter(ContextKeys.DATE_VALUE).fromFragment("VALUE").entity(date())
            .getIntentDefinition();
    /**
     * The intent customTextualFilterIntent.
     */
    public static final IntentDefinition customTextualFilterIntent = intent("CustomTextualFilter")
            .trainingSentences(BUNDLE.getStringArray("CustomFilter"))
            .parameter(ContextKeys.TEXTUAL_FIELD_NAME).fromFragment("FIELD").entity(Entities.textualFieldEntity)
            .parameter(ContextKeys.TEXTUAL_OPERATOR_NAME).fromFragment("OPERATOR").entity(Entities.textualOperatorEntity)
            .parameter(ContextKeys.TEXTUAL_VALUE).fromFragment("VALUE").entity(any())
            .getIntentDefinition();
    /**
     * The intent fieldNameIntent.
     */
    public static final IntentDefinition fieldNameIntent = intent("FieldName")
            .trainingSentences(BUNDLE.getStringArray("FieldName"))
            .parameter(ContextKeys.NUMERIC_FIELD_NAME).fromFragment("NUMERIC_FIELD").entity(Entities.numericFieldEntity)
            .parameter(ContextKeys.TEXTUAL_FIELD_NAME).fromFragment("TEXTUAL_FIELD").entity(Entities.textualFieldEntity)
            .parameter(ContextKeys.DATE_FIELD_NAME).fromFragment("DATE_FIELD").entity(Entities.dateFieldEntity)
            .getIntentDefinition();
    /**
     * The intent operatorNameIntent.
     */
    public static final IntentDefinition operatorNameIntent = intent("OperatorName")
            .trainingSentences(BUNDLE.getStringArray("OperatorName"))
            .parameter(ContextKeys.NUMERIC_OPERATOR_NAME).fromFragment("NUMERIC_OPERATOR").entity(Entities.numericOperatorEntity)
            .parameter(ContextKeys.TEXTUAL_OPERATOR_NAME).fromFragment("TEXTUAL_OPERATOR").entity(Entities.textualOperatorEntity)
            .parameter(ContextKeys.DATE_OPERATOR_NAME).fromFragment("DATE_OPERATOR").entity(Entities.dateOperatorEntity)
            .getIntentDefinition();
}
