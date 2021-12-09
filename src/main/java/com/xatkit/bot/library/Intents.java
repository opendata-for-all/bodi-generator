package com.xatkit.bot.library;

import com.xatkit.bot.Bot;
import com.xatkit.i18n.XatkitI18nHelper;
import com.xatkit.intent.IntentDefinition;

import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.intent;

public class Intents {

    public final static XatkitI18nHelper bundle = new XatkitI18nHelper("intents", Bot.LOCALE);

    public static final IntentDefinition restartIntent = intent("Restart")
            .trainingSentences(bundle.getStringArray("Restart"))
            .getIntentDefinition()
            ;
    public static final IntentDefinition showDataIntent = intent("ShowData")
            .trainingSentences(bundle.getStringArray("ShowData"))
            .getIntentDefinition()
            ;
    public static final IntentDefinition showNextPageIntent = intent("ShowNextPage")
            .trainingSentences(bundle.getStringArray("ShowNextPage"))
            .getIntentDefinition()
            ;
    public static final IntentDefinition stopViewIntent = intent("StopView")
            .trainingSentences(bundle.getStringArray("StopView"))
            .getIntentDefinition()
            ;
    public static final IntentDefinition addFilterIntent = intent("AddFilter")
            .trainingSentences(bundle.getStringArray("AddFilter"))
            .getIntentDefinition()
            ;
    public static final IntentDefinition addFieldToViewIntent = intent("AddFieldToView")
            .trainingSentences(bundle.getStringArray("AddFieldToView"))
            .getIntentDefinition()
            ;
    public static final IntentDefinition customQueryIntent = intent("CustomQuery")
            .trainingSentences(bundle.getStringArray("CustomQuery"))
            .getIntentDefinition()
            ;
    public static final IntentDefinition customFilterIntent = intent("CustomFilter")
            .trainingSentences(bundle.getStringArray("CustomFilter"))

            .parameter(ContextKeys.numericFieldName).fromFragment("NUMERIC_FIELD").entity(Entities.numericFieldEntity)
            .parameter(ContextKeys.numericOperatorName).fromFragment("NUMERIC_OPERATOR").entity(Entities.numericOperatorEntity)

            .parameter(ContextKeys.textualFieldName).fromFragment("TEXTUAL_FIELD").entity(Entities.textualFieldEntity)
            .parameter(ContextKeys.textualOperatorName).fromFragment("TEXTUAL_OPERATOR").entity(Entities.textualOperatorEntity)

            .parameter(ContextKeys.value).fromFragment("VALUE").entity(any())
            .getIntentDefinition()
            ;
    public static final IntentDefinition fieldNameIntent = intent("FieldName")
            .trainingSentences(bundle.getStringArray("FieldName"))
            .parameter(ContextKeys.numericFieldName).fromFragment("NUMERIC_FIELD").entity(Entities.numericFieldEntity)
            .parameter(ContextKeys.textualFieldName).fromFragment("TEXTUAL_FIELD").entity(Entities.textualFieldEntity)
            .parameter(ContextKeys.dateFieldName).fromFragment("DATE_FIELD").entity(Entities.dateFieldEntity)
            .getIntentDefinition()
            ;
    public static final IntentDefinition operatorNameIntent = intent("OperatorName")
            .trainingSentences(bundle.getStringArray("OperatorName"))
            .parameter(ContextKeys.numericOperatorName).fromFragment("NUMERIC_OPERATOR").entity(Entities.numericOperatorEntity)
            .parameter(ContextKeys.textualOperatorName).fromFragment("TEXTUAL_OPERATOR").entity(Entities.textualOperatorEntity)
            .parameter(ContextKeys.dateOperatorName).fromFragment("DATE_OPERATOR").entity(Entities.dateOperatorEntity)
            .getIntentDefinition()
            ;
    public static final IntentDefinition valueIntent = intent("Value")
            .trainingSentences(bundle.getStringArray("Value"))
            .parameter(ContextKeys.value).fromFragment("VALUE").entity(any())
            .getIntentDefinition()
        ;
    /*
    public static final IntentDefinition numericValueIntent = intent("NumericValue")
            .trainingSentences(bundle.getStringArray("NumericValue"))
            .parameter(ContextKeys.numericValue).fromFragment("NUMERIC_VALUE").entity(number())
            .getIntentDefinition()
            ;
    public static final IntentDefinition textualValueIntent = intent("TextualValue")
            .trainingSentences(bundle.getStringArray("TextualValue"))
            .parameter(ContextKeys.textualValue).fromFragment("TEXTUAL_VALUE").entity(any())
            .getIntentDefinition()
            ;
     */
}
