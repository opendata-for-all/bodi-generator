package com.xatkit.bot.customQuery;

import bodiGenerator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.execution.StateContext;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class CustomFilter {

    @Getter
    private State saveNumericFilterState;
    @Getter
    private State saveTextualFilterState;
    @Getter
    private State saveDateFilterState;

    private static void saveFilterAndReply(StateContext context, ReactPlatform reactPlatform, String fieldKey,
                                           String operatorKey, String valueKey) {
        String field = (String) context.getIntent().getValue(fieldKey);
        String operator = (String) context.getIntent().getValue(operatorKey);
        String value = (String) context.getIntent().getValue(valueKey);
        if (!isEmpty(field) && !isEmpty(operator) && !isEmpty(value)) {
            Statement statement = (Statement) context.getSession().get(ContextKeys.statement);
            statement.addFilter(field, operator, value);
            reactPlatform.reply(context, MessageFormat.format(messages.getString("FilterAdded"),
                    field, operator, value));
        }
        else {
            reactPlatform.reply(context, messages.getString("SomethingWentWrong"));
        }
    }

    public CustomFilter(ReactPlatform reactPlatform, StateProvider returnState) {
        /*
         These states could be one, but for safety they are separated and the parameters are type-dependant
         */
        val saveNumericFilterState = state("SaveCustomFilter");
        val saveTextualFilterState = state("SaveTextualFilter");
        val saveDateFilterState = state("SaveDateFilter");

        saveNumericFilterState
                .body(context -> {
                            saveFilterAndReply(context, reactPlatform, ContextKeys.numericFieldName,
                                    ContextKeys.numericOperatorName, ContextKeys.numericValue);
                        }
                )
                .next()
                .moveTo(returnState)
        ;
        this.saveNumericFilterState = saveNumericFilterState.getState();

        saveTextualFilterState
                .body(context -> {
                            saveFilterAndReply(context, reactPlatform, ContextKeys.textualFieldName,
                                    ContextKeys.textualOperatorName, ContextKeys.textualValue);
                        }
                )
                .next()
                .moveTo(returnState)
        ;
        this.saveTextualFilterState = saveTextualFilterState.getState();

        saveDateFilterState
                .body(context -> {
                            saveFilterAndReply(context, reactPlatform, ContextKeys.dateFieldName,
                                    ContextKeys.dateOperatorName, ContextKeys.dateValue);
                        }
                )
                .next()
                .moveTo(returnState)
        ;
        this.saveDateFilterState = saveDateFilterState.getState();
    }
}
