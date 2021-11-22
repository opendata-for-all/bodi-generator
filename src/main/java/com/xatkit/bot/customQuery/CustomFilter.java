package com.xatkit.bot.customQuery;

import bodiGenerator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.dsl.state.StateProvider;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import static com.xatkit.dsl.DSL.state;
import static java.util.Objects.isNull;

public class CustomFilter {

    @Getter
    private State saveCustomFilterState;

    public CustomFilter(ReactPlatform reactPlatform, StateProvider returnState) {
        val saveCustomFilterState = state("SaveCustomFilter");

        saveCustomFilterState
                .body(context -> {
                            String textualFieldName = (String) context.getIntent().getValue(ContextKeys.textualFieldName);
                            String numericFieldName = (String) context.getIntent().getValue(ContextKeys.numericFieldName);
                            String fieldName = (isNull(textualFieldName) || textualFieldName.isEmpty() ? numericFieldName : textualFieldName);

                            String textualOperatorName = (String) context.getIntent().getValue(ContextKeys.textualOperatorName);
                            String numericOperatorName = (String) context.getIntent().getValue(ContextKeys.numericOperatorName);
                            String operatorName = (isNull(textualOperatorName) || textualOperatorName.isEmpty() ? numericOperatorName : textualOperatorName);

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
        this.saveCustomFilterState = saveCustomFilterState.getState();
    }
}
