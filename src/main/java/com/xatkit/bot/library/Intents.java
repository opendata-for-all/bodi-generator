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
}
