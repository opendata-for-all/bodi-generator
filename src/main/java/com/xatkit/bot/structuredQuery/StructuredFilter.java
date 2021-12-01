package com.xatkit.bot.structuredQuery;

import bodiGenerator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.List;

import static com.xatkit.bot.Bot.messages;
import static com.xatkit.bot.library.Utils.isNumeric;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class StructuredFilter {

    @Getter
    private State selectFilterFieldState;

    public StructuredFilter(ReactPlatform reactPlatform, StateProvider returnState) {
        val selectFilterFieldState = state("SelectFilterField");
        val selectOperatorNameState = state("SelectOperatorName");
        val selectOperatorValueState = state("SelectOperatorValue");
        val saveFilterState = state("SaveFilterState");

        selectFilterFieldState
                .body(context -> {
                            reactPlatform.reply(context, messages.getString("SelectField"),
                                    (List<String>) context.getSession().get(ContextKeys.filterFieldOptions));
                        }
                )
                .next()
                .when(intentIs(Intents.fieldNameIntent)).moveTo(selectOperatorNameState)
        ;
        selectOperatorNameState
                .body(context -> {
                            String textualFieldName = (String) context.getIntent().getValue(ContextKeys.textualFieldName);
                            String numericFieldName = (String) context.getIntent().getValue(ContextKeys.numericFieldName);
                            if (!isEmpty(textualFieldName)) {
                                context.getSession().put(ContextKeys.lastFieldName, textualFieldName);
                                reactPlatform.reply(context, messages.getString("SelectOperator"),
                                        Utils.getEntityValues(Entities.textualOperatorEntity));
                            } else if (!isEmpty(numericFieldName)) {
                                context.getSession().put(ContextKeys.lastFieldName, numericFieldName);
                                reactPlatform.reply(context, messages.getString("SelectOperator"),
                                        Utils.getEntityValues(Entities.numericOperatorEntity));
                            }
                        }
                )
                .next()
                .when(intentIs(Intents.operatorNameIntent)).moveTo(selectOperatorValueState)
        ;
        selectOperatorValueState
                .body(context -> {
                            String textualOperatorName = (String) context.getIntent().getValue(ContextKeys.textualOperatorName);
                            String numericOperatorName = (String) context.getIntent().getValue(ContextKeys.numericOperatorName);
                            if (!isEmpty(textualOperatorName)) {
                                context.getSession().put(ContextKeys.lastOperatorName, textualOperatorName);
                                context.getSession().put(ContextKeys.lastOperatorType, "textual");
                            } else if (!isEmpty(numericOperatorName)) {
                                context.getSession().put(ContextKeys.lastOperatorName, numericOperatorName);
                                context.getSession().put(ContextKeys.lastOperatorType, "numeric");
                            }
                            reactPlatform.reply(context, messages.getString("WriteValue"));
                        }
                )
                .next()
                .when(intentIs(Intents.valueIntent)).moveTo(saveFilterState)
                //.when(intentIs(Intents.numericValueIntent).or(intentIs(Intents.textualValueIntent))).moveTo(saveFilterState)
        ;
        saveFilterState
                .body(context -> {
                            context.getSession().put(ContextKeys.operatorValueError, false);
                            String fieldName = (String) context.getSession().get(ContextKeys.lastFieldName);
                            String operatorName = (String) context.getSession().get(ContextKeys.lastOperatorName);
                            String operatorType = (String) context.getSession().get(ContextKeys.lastOperatorType);
                            String value = context.getIntent().getMatchedInput();
                            //String value = (String) context.getIntent().getValue(ContextKeys.value);
                            if (operatorType.equals("numeric") && !isNumeric(value)) {
                                context.getSession().put(ContextKeys.operatorValueError, true);
                                reactPlatform.reply(context, messages.getString("ExpectedNumericValue"));
                            } else {
                                Statement statement = (Statement) context.getSession().get(ContextKeys.statement);
                                statement.addFilter(fieldName, operatorName, value);
                                reactPlatform.reply(context, MessageFormat.format(messages.getString("FilterAdded"),
                                        fieldName, operatorName, value));
                            }
                            /*
                            if (!isEmpty(textualValue)) {
                                if (operatorType.equals("textual")) {
                                    operatorValue = textualValue;
                                } else {
                                    // error
                                    context.getSession().put(ContextKeys.operatorValueError, true);
                                    reactPlatform.reply(context, "Not expected a textual value...");
                                }
                            } else if (!isEmpty(numericValue)) {
                                if (operatorType.equals("numeric") || operatorType.equals("textual")) {
                                    operatorValue = numericValue;
                                } else {
                                    // error
                                    context.getSession().put(ContextKeys.operatorValueError, true);
                                    reactPlatform.reply(context, "Not expected a numeric value...");
                                }
                            }
                            if (nonNull(value)) {
                                Statement statement = (Statement) context.getSession().get(ContextKeys.statement);
                                statement.addFilter(fieldName, operatorName, value);
                                reactPlatform.reply(context,
                                        "'" + fieldName + " " + operatorName + " " + value + "' added");
                            }
                             */
                        }
                )
                .next()
                .when(context -> (boolean) context.getSession().get(ContextKeys.operatorValueError)).moveTo(selectOperatorValueState)
                .when(context -> !((boolean) context.getSession().get(ContextKeys.operatorValueError))).moveTo(returnState)
        ;

        this.selectFilterFieldState = selectFilterFieldState.getState();
    }
}
