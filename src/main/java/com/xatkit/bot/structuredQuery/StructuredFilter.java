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

import java.util.List;

import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;
import static java.util.Objects.isNull;

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
                            reactPlatform.reply(context, "Select a field", (List<String>) context.getSession().get(ContextKeys.filterFieldOptions));
                        }
                )
                .next()
                .when(intentIs(Intents.fieldNameIntent)).moveTo(selectOperatorNameState)
        ;
        selectOperatorNameState
                .body(context -> {
                            String textualFieldName = (String) context.getIntent().getValue(ContextKeys.textualFieldName);
                            String numericFieldName = (String) context.getIntent().getValue(ContextKeys.numericFieldName);
                            if (isNull(textualFieldName) || textualFieldName.isEmpty()) {
                                context.getSession().put(ContextKeys.lastFieldName, numericFieldName);
                                reactPlatform.reply(context, "Select an operator", Utils.getEntityValues(Entities.numericOperatorEntity));
                            } else if (isNull(numericFieldName) || numericFieldName.isEmpty()) {
                                context.getSession().put(ContextKeys.lastFieldName, textualFieldName);
                                reactPlatform.reply(context, "Select an operator", Utils.getEntityValues(Entities.textualOperatorEntity));
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
                            if (isNull(textualOperatorName) || textualOperatorName.isEmpty()) {
                                context.getSession().put(ContextKeys.lastOperatorName, numericOperatorName);
                            } else if (isNull(numericOperatorName) || numericOperatorName.isEmpty()) {
                                context.getSession().put(ContextKeys.lastOperatorName, textualOperatorName);
                            }
                            reactPlatform.reply(context, "Write a value");
                        }
                )
                .next()
                .when(intentIs(Intents.operatorValueIntent)).moveTo(saveFilterState)
        ;
        saveFilterState
                .body(context -> {
                            String fieldName = (String) context.getSession().get(ContextKeys.lastFieldName);
                            String operatorName = (String) context.getSession().get(ContextKeys.lastOperatorName);
                            String operatorValue = (String) context.getIntent().getValue(ContextKeys.operatorValue);
                            Statement statement = (Statement) context.getSession().get(ContextKeys.statement);
                            statement.addFilter(fieldName, operatorName, operatorValue);
                            reactPlatform.reply(context,
                                    "'" + fieldName + " " + operatorName + " " + operatorValue + "' added");
                        }
                )
                .next()
                .moveTo(returnState)
        ;

        this.selectFilterFieldState = selectFilterFieldState.getState();
    }
}
