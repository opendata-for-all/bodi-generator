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
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class StructuredFilter {

    @Getter
    private State selectFilterFieldState;

    public StructuredFilter(ReactPlatform reactPlatform, StateProvider returnState) {
        val selectFilterFieldState = state("SelectFilterField");
        val selectOperatorNameState = state("SelectOperatorName");
        val saveOperatorType = state("SaveOperatorType");
        val writeDateValue = state("WriteDateValue");
        val writeTextualValue = state("WriteTextualValue");
        val writeNumericValue = state("WriteNumericValue");
        val saveFilterState = state("saveFilterState");
        final String[] expectedValueIntent = new String[1];


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
                            String dateFieldName = (String) context.getIntent().getValue(ContextKeys.dateFieldName);
                            if (!isEmpty(textualFieldName)) {
                                context.getSession().put(ContextKeys.lastFieldName, textualFieldName);
                                reactPlatform.reply(context, messages.getString("SelectOperator"),
                                        Utils.getEntityValues(Entities.textualOperatorEntity));
                            } else if (!isEmpty(numericFieldName)) {
                                context.getSession().put(ContextKeys.lastFieldName, numericFieldName);
                                reactPlatform.reply(context, messages.getString("SelectOperator"),
                                        Utils.getEntityValues(Entities.numericOperatorEntity));
                            } else if (!isEmpty(dateFieldName)) {
                                context.getSession().put(ContextKeys.lastFieldName, dateFieldName);
                                reactPlatform.reply(context, messages.getString("SelectOperator"),
                                        Utils.getEntityValues(Entities.dateOperatorEntity));
                            }
                        }
                )
                .next()
                .when(intentIs(Intents.operatorNameIntent)).moveTo(saveOperatorType)
        ;
        saveOperatorType
                .body(context -> {
                            String textualOperatorName = (String) context.getIntent().getValue(ContextKeys.textualOperatorName);
                            String numericOperatorName = (String) context.getIntent().getValue(ContextKeys.numericOperatorName);
                            String dateOperatorName = (String) context.getIntent().getValue(ContextKeys.dateOperatorName);
                            if (!isEmpty(textualOperatorName)) {
                                context.getSession().put(ContextKeys.lastOperatorName, textualOperatorName);
                                expectedValueIntent[0] = "textual";
                            } else if (!isEmpty(numericOperatorName)) {
                                context.getSession().put(ContextKeys.lastOperatorName, numericOperatorName);
                                expectedValueIntent[0] = "numeric";
                            } else if (!isEmpty(dateOperatorName)) {
                                context.getSession().put(ContextKeys.lastOperatorName, dateOperatorName);
                                expectedValueIntent[0] = "date";
                            }
                        }
                )
                .next()
                .when(context -> expectedValueIntent[0].equals("textual")).moveTo(writeTextualValue)
                .when(context -> expectedValueIntent[0].equals("numeric")).moveTo(writeNumericValue)
                .when(context -> expectedValueIntent[0].equals("date")).moveTo(writeDateValue)
        ;
        writeTextualValue
                .body(context -> {
                            reactPlatform.reply(context, messages.getString("WriteTextualValue"));
                        }
                )
                .next()
                .when(intentIs(Intents.textualValueIntent)).moveTo(saveFilterState)
        ;
        writeNumericValue
                .body(context -> {
                            reactPlatform.reply(context, messages.getString("WriteNumericValue"));
                        }
                )
                .next()
                .when(intentIs(Intents.numericValueIntent)).moveTo(saveFilterState)
        ;
        writeDateValue
                .body(context -> {
                            reactPlatform.reply(context, messages.getString("WriteDateValue"));
                        }
                )
                .next()
                .when(intentIs(Intents.dateValueIntent)).moveTo(saveFilterState)
        ;
        saveFilterState
                .body(context -> {
                            String fieldName = (String) context.getSession().get(ContextKeys.lastFieldName);
                            String operatorName = (String) context.getSession().get(ContextKeys.lastOperatorName);
                            String value = (String) context.getIntent().getValue(ContextKeys.value);

                            if (!isEmpty(value)) {
                                Statement statement = (Statement) context.getSession().get(ContextKeys.statement);
                                statement.addFilter(fieldName, operatorName, value);
                                reactPlatform.reply(context, MessageFormat.format(messages.getString("FilterAdded"),
                                        fieldName, operatorName, value));
                            }
                        }
                )
                .next()
                .moveTo(returnState)
        ;

        this.selectFilterFieldState = selectFilterFieldState.getState();
    }
}
