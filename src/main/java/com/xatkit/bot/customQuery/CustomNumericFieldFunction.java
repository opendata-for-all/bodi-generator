package com.xatkit.bot.customQuery;

import bodi.generator.dataSource.Operation;
import bodi.generator.dataSource.Statement;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.execution.State;
import com.xatkit.plugins.react.platform.ReactPlatform;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;

import static com.xatkit.bot.Bot.messages;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The Custom Numeric Field Function workflow of a chatbot.
 * <p>
 * Given a numeric field name and an operator, this workflow applies the operator in the field and shows the result.
 * <p>
 * Some example operators are: {@code max}, {@code min}, {@code avg}, {@code sum}
 * <p>
 * This workflow is run within a {@link CustomQuery} workflow.
 *
 * @see CustomQuery
 */
public class CustomNumericFieldFunction {

    /**
     * The entry point for the Custom Numeric Field Function workflow.
     */
    @Getter
    private final State processCustomNumericFieldFunctionState;

    /**
     * Instantiates a new Custom Numeric Field Function workflow.
     *
     * @param reactPlatform the react platform of a chatbot
     * @param returnState   the state where the chatbot ends up arriving once the workflow is finished
     */
    public CustomNumericFieldFunction(ReactPlatform reactPlatform, State returnState) {
        val processCustomNumericFieldFunctionState = state("ProcessCustomNumericFieldFunction");

        processCustomNumericFieldFunctionState
                .body(context -> {
                    String field = (String) context.getIntent().getValue(ContextKeys.FIELD);
                    String operator = (String) context.getIntent().getValue(ContextKeys.OPERATOR);
                    if (!isEmpty(field) && !isEmpty(operator)) {
                        Statement statement = (Statement) context.getSession().get(ContextKeys.STATEMENT);
                        Float result = (Float) statement.executeQuery(Operation.NUMERIC_FIELD_FUNCTION, field, operator);
                        reactPlatform.reply(context, MessageFormat.format(messages.getString("CustomNumericFieldFunction"),
                                operator, field, result));
                    } else {
                        reactPlatform.reply(context, messages.getString("FieldNotRecognized"));
                    }
                })
                .next()
                .moveTo(returnState);

        this.processCustomNumericFieldFunctionState = processCustomNumericFieldFunctionState.getState();
    }
}
