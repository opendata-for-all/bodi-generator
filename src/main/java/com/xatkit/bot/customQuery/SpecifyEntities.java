package com.xatkit.bot.customQuery;

import com.xatkit.bot.Bot;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.execution.State;
import com.xatkit.intent.ContextParameter;
import com.xatkit.intent.ContextParameterValue;
import lombok.Getter;
import lombok.val;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.state;


/**
 * The Specify Entities workflow of a chatbot.
 * <p>
 * This workflow is executed before the Custom Query workflows. It is used to read the Custom Query's intent
 * parameters and, if some parameter is not an actual field entity entry but a field group, the chatbot then asks the
 * user to specify those fields.
 */
public class SpecifyEntities {

    /**
     * The entry point for the Specify Entities workflow.
     */
    @Getter
    private final State checkEntitiesState;

    /**
     * Instantiates a new Specify Entities workflow.
     *
     * @param bot         the chatbot that uses this workflow
     * @param returnState the state where the chatbot ends up arriving once the workflow is finished
     */
    public SpecifyEntities(Bot bot, State returnState) {
        val checkEntitiesState = state("CheckEntities");
        val specifyEntityState = state("SpecifyEntity");
        val saveEntityState = state("SaveEntity");

        checkEntitiesState
                .body(context -> {
                    context.getSession().put(ContextKeys.INTENT_NAME, context.getIntent().getDefinition().getName());
                    Map<String, String> entitiesToSpecify = new HashMap<>();
                    for (ContextParameterValue parameterValue : context.getIntent().getValues()) {
                        ContextParameter parameter = parameterValue.getContextParameter();
                        context.getSession().put(parameter.getName(), parameterValue.getValue());
                        if ((parameter.getEntity().getReferredEntity().equals(bot.entities.fieldEntity.getEntityReference().getReferredEntity())
                                || parameter.getEntity().getReferredEntity().equals(bot.entities.numericFieldEntity.getEntityReference().getReferredEntity())
                                || parameter.getEntity().getReferredEntity().equals(bot.entities.textualFieldEntity.getEntityReference().getReferredEntity())
                                || parameter.getEntity().getReferredEntity().equals(bot.entities.datetimeFieldEntity.getEntityReference().getReferredEntity()))
                                && bot.entities.fieldGroups.containsKey((String) parameterValue.getValue())) {
                            entitiesToSpecify.put((String) parameterValue.getValue(), parameter.getName());
                        }
                    }
                    context.getSession().put(ContextKeys.ENTITIES_TO_SPECIFY, entitiesToSpecify);
                })
                .next()
                .when(context -> ((Map<String, String>) context.getSession().get(ContextKeys.ENTITIES_TO_SPECIFY)).isEmpty()).moveTo(returnState)
                .when(context -> !((Map<String, String>) context.getSession().get(ContextKeys.ENTITIES_TO_SPECIFY)).isEmpty()).moveTo(specifyEntityState);

        this.checkEntitiesState = checkEntitiesState.getState();

        specifyEntityState
                .body(context -> {
                    Map<String, String> entitiesToSpecify = (Map<String, String>) context.getSession().get(ContextKeys.ENTITIES_TO_SPECIFY);
                    if (entitiesToSpecify.size() > 0) {
                        String entity = entitiesToSpecify.keySet().iterator().next();
                        context.getSession().put(ContextKeys.ENTITY_TO_SPECIFY, entity);
                        List<String> fieldGroup = new ArrayList<>(bot.entities.fieldGroups.get(entity));
                        List<String> fieldGroupRN = fieldGroup.stream().map(field -> bot.entities.readableNames.get(field)).collect(Collectors.toList());
                        bot.reactPlatform.reply(context,
                                MessageFormat.format(bot.messages.getString("SpecifyEntity"), entity), fieldGroupRN);
                    }
                })
                .next()
                .when(context -> ((Map<String, String>) context.getSession().get(ContextKeys.ENTITIES_TO_SPECIFY)).isEmpty()).moveTo(returnState)
                .when(intentIs(bot.intents.fieldIntent)).moveTo(saveEntityState);

        saveEntityState
                .body(context -> {
                    String entityToSpecify = (String) context.getSession().get(ContextKeys.ENTITY_TO_SPECIFY);
                    String entity = (String) context.getIntent().getValue(ContextKeys.VALUE);
                    Map<String, String> entitiesToSpecify = (Map<String, String>) context.getSession().get(ContextKeys.ENTITIES_TO_SPECIFY);
                    String contextKey = entitiesToSpecify.get(entityToSpecify);
                    context.getSession().put(contextKey, entity);
                    entitiesToSpecify.remove(entityToSpecify);
                })
                .next()
                .when(context -> ((Map<String, String>) context.getSession().get(ContextKeys.ENTITIES_TO_SPECIFY)).isEmpty()).moveTo(returnState)
                .when(context -> !((Map<String, String>) context.getSession().get(ContextKeys.ENTITIES_TO_SPECIFY)).isEmpty()).moveTo(specifyEntityState);
    }
}
