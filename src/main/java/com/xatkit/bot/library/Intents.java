package com.xatkit.bot.library;

import com.xatkit.bot.Bot;
import com.xatkit.i18n.XatkitI18nHelper;
import com.xatkit.intent.IntentDefinition;

import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.date;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.number;

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
    public static final IntentDefinition customNumericFilterIntent = intent("CustomNumericFilter")
            .trainingSentences(bundle.getStringArray("CustomFilter"))
            .parameter(ContextKeys.numericFieldName).fromFragment("FIELD").entity(Entities.numericFieldEntity)
            .parameter(ContextKeys.numericOperatorName).fromFragment("OPERATOR").entity(Entities.numericOperatorEntity)
            .parameter(ContextKeys.numericValue).fromFragment("VALUE").entity(number())
            .getIntentDefinition()
            ;
    public static final IntentDefinition customDateFilterIntent = intent("CustomDateFilter")
            .trainingSentences(bundle.getStringArray("CustomFilter"))
            .parameter(ContextKeys.dateFieldName).fromFragment("FIELD").entity(Entities.dateFieldEntity)
            .parameter(ContextKeys.dateOperatorName).fromFragment("OPERATOR").entity(Entities.dateOperatorEntity)
            .parameter(ContextKeys.dateValue).fromFragment("VALUE").entity(date())
            .getIntentDefinition()
            ;
    public static final IntentDefinition customTextualFilterIntent = intent("CustomTextualFilter")
            .trainingSentences(bundle.getStringArray("CustomFilter"))
            .parameter(ContextKeys.textualFieldName).fromFragment("FIELD").entity(Entities.textualFieldEntity)
            .parameter(ContextKeys.textualOperatorName).fromFragment("OPERATOR").entity(Entities.textualOperatorEntity)
            .parameter(ContextKeys.textualValue).fromFragment("VALUE").entity(any())
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
    public static final IntentDefinition numericValueIntent = intent("NumericValue")
            .trainingSentences(bundle.getStringArray("NumericValue"))
            .parameter(ContextKeys.value).fromFragment("VALUE").entity(number())
            .getIntentDefinition()
            ;
    public static final IntentDefinition dateValueIntent = intent("DateValue")
            .trainingSentences(bundle.getStringArray("DateValue"))
            .parameter(ContextKeys.value).fromFragment("VALUE").entity(date())
            .getIntentDefinition()
            ;
    public static final IntentDefinition textualValueIntent = intent("TextualValue")
            .trainingSentences(bundle.getStringArray("TextualValue"))
            .parameter(ContextKeys.value).fromFragment("VALUE").entity(any())
            .getIntentDefinition()
            ;

}
