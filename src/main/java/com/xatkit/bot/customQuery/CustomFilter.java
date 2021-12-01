package com.xatkit.bot.customQuery;

import bodiGenerator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import static com.xatkit.bot.library.Utils.isNumeric;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class CustomFilter {

    @Getter
    private State saveCustomFilterState;

    public CustomFilter(ReactPlatform reactPlatform, StateProvider returnState) {
        val saveCustomFilterState = state("SaveCustomFilter");

        saveCustomFilterState
                .body(context -> {
                            String textualFieldName = (String) context.getIntent().getValue(ContextKeys.textualFieldName);
                            String numericFieldName = (String) context.getIntent().getValue(ContextKeys.numericFieldName);
                            String fieldName = (!isEmpty(textualFieldName) ? textualFieldName : numericFieldName);

                            String textualOperatorName = (String) context.getIntent().getValue(ContextKeys.textualOperatorName);
                            String numericOperatorName = (String) context.getIntent().getValue(ContextKeys.numericOperatorName);
                            String operatorName = (!isEmpty(textualOperatorName) ? textualOperatorName : numericOperatorName);

                            String value = (String) context.getIntent().getValue(ContextKeys.value);

                            if (!isEmpty(numericFieldName) &&
                                    !isEmpty(numericOperatorName) &&
                                    !isNumeric(value)) {
                                reactPlatform.reply(context, "Expected a numeric value");
                            } else if (!isEmpty(numericFieldName) &&
                                    !isEmpty(textualOperatorName)) {
                                reactPlatform.reply(context, "Expected a numeric operator (and value)");
                            } else if (!isEmpty(textualFieldName) &&
                                    !isEmpty(numericOperatorName)){
                                reactPlatform.reply(context, "Expected a textual operator");
                            } else {
                                Statement statement = (Statement) context.getSession().get(ContextKeys.statement);
                                statement.addFilter(fieldName, operatorName, value);
                                reactPlatform.reply(context,
                                        "'" + fieldName + " " + operatorName + " " + value + "' added");
                            }
                        }
                )
                .next()
                .moveTo(returnState)
        ;
        this.saveCustomFilterState = saveCustomFilterState.getState();
    }
}
