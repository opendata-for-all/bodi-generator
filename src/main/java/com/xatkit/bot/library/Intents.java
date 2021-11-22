package com.xatkit.bot.library;

import com.xatkit.intent.IntentDefinition;

import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.intent;


public class Intents {

    public static final IntentDefinition restartIntent = intent("Restart")
            .trainingSentence("restart")
            .getIntentDefinition()
            ;
    public static final IntentDefinition showDataIntent = intent("ShowData")
            .trainingSentence("show data")
            .getIntentDefinition()
            ;
    public static final IntentDefinition showNextPageIntent = intent("ShowNextPage")
            .trainingSentence("next page")
            .getIntentDefinition()
            ;
    public static final IntentDefinition stopViewIntent = intent("StopView")
            .trainingSentence("stop view")
            .getIntentDefinition()
            ;
    public static final IntentDefinition addFilterIntent = intent("AddFilter")
            .trainingSentence("add filter")
            .getIntentDefinition()
            ;
    public static final IntentDefinition addFieldToViewIntent = intent("AddFieldToView")
            .trainingSentence("add field to view")
            .getIntentDefinition()
            ;
    public static final IntentDefinition customQueryIntent = intent("CustomQuery")
            .trainingSentence("custom query")
            .getIntentDefinition()
            ;
    public static final IntentDefinition customFilterIntent = intent("CustomFilter")
            .trainingSentence("I want to filter NUMERICFIELD NUMERICOPERATOR VALUE")
            .trainingSentence("I want to filter TEXTUALFIELD TEXTUALOPERATOR VALUE")
            .trainingSentence("filter NUMERICFIELD NUMERICOPERATOR VALUE")
            .trainingSentence("filter TEXTUALFIELD TEXTUALOPERATOR VALUE")
            .trainingSentence("NUMERICFIELD NUMERICOPERATOR VALUE")
            .trainingSentence("TEXTUALFIELD TEXTUALOPERATOR VALUE")
            .parameter(ContextKeys.numericFieldName)
            .fromFragment("NUMERICFIELD")
            .entity(Entities.numericFieldEntity)
            .parameter(ContextKeys.numericOperatorName)
            .fromFragment("NUMERICOPERATOR")
            .entity(Entities.numericOperatorEntity)
            .parameter(ContextKeys.textualFieldName)
            .fromFragment("TEXTUALFIELD")
            .entity(Entities.textualFieldEntity)
            .parameter(ContextKeys.textualOperatorName)
            .fromFragment("TEXTUALOPERATOR")
            .entity(Entities.textualOperatorEntity)
            .parameter(ContextKeys.operatorValue)
            .fromFragment("VALUE")
            .entity(any())
            .getIntentDefinition()
            ;
    public static final IntentDefinition fieldNameIntent = intent("FieldName")
            .trainingSentence("NUMERICFIELD")
            .trainingSentence("TEXTUALFIELD")
            .parameter(ContextKeys.numericFieldName)
            .fromFragment("NUMERICFIELD")
            .entity(Entities.numericFieldEntity)
            .parameter(ContextKeys.textualFieldName)
            .fromFragment("TEXTUALFIELD")
            .entity(Entities.textualFieldEntity)
            .getIntentDefinition()
            ;
    public static final IntentDefinition operatorNameIntent = intent("OperatorName")
            .trainingSentence("NUMERICOPERATOR")
            .trainingSentence("TEXTUALOPERATOR")
            .parameter(ContextKeys.numericOperatorName)
            .fromFragment("NUMERICOPERATOR")
            .entity(Entities.numericOperatorEntity)
            .parameter(ContextKeys.textualOperatorName)
            .fromFragment("TEXTUALOPERATOR")
            .entity(Entities.textualOperatorEntity)
            .getIntentDefinition()
            ;
    public static final IntentDefinition operatorValueIntent = intent("OperatorValue")
            .trainingSentence("VALUE")
            .parameter(ContextKeys.operatorValue)
            .fromFragment("VALUE")
            .entity(any())
            .getIntentDefinition()
            ;
}
