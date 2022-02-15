package com.xatkit.bot.library;

import com.xatkit.bot.Bot;
import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;
import com.xatkit.dsl.entity.MappingEntryStep;
import com.xatkit.dsl.entity.MappingSynonymStep;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;

import static com.xatkit.dsl.DSL.mapping;

public final class Entities {

    /**
     * The name of the file containing the chatbot entities. It may be refined with a language code suffix and a file
     * extension.
     */
    private static String entitiesJsonFile = "entities";

    /**
     * Contains the chatbot entities in a JSON format and in a specific language.
     */
    private static final JSONObject entitiesJson;

    // Get the bot language and read the appropriate .json file.
    static {
        if (Bot.language.equals("es")) {
            entitiesJsonFile += "_es.json";
        } else if (Bot.language.equals("ca")) {
            entitiesJsonFile += "_ca.json";
        } else {
            entitiesJsonFile += ".json";
        }
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(entitiesJsonFile);
        if (is == null) {
            throw new NullPointerException("Cannot find the entities file \"" + entitiesJsonFile + "\"");
        }
        entitiesJson = new JSONObject(new JSONTokener(is));
    }

    private Entities() {
    }


    /**
     * Generates a chatbot entity.
     * <p>
     * It extracts the values and synonyms of the values of the entity from {@link #entitiesJson}
     *
     * @param entityName the name of the entity
     * @return the entity object
     */
    public static EntityDefinitionReferenceProvider generateEntity(String entityName) {
        JSONObject entityJson = entitiesJson.getJSONObject(entityName);
        MappingEntryStep entity = mapping(entityName);
        for (String entry : entityJson.keySet()) {
            MappingSynonymStep synonymStep = entity.entry().value(entry);
            for (Object synonym : entityJson.getJSONObject(entry).getJSONArray("synonyms")) {
                synonymStep.synonym((String) synonym);
            }
        }
        return (EntityDefinitionReferenceProvider) entity;
    }

    /**
     * The entity numericFieldEntity.
     */
    public static final EntityDefinitionReferenceProvider numericFieldEntity = generateEntity("numericFieldEntity");
    /**
     * The entity textualFieldEntity.
     */
    public static final EntityDefinitionReferenceProvider textualFieldEntity = generateEntity("textualFieldEntity");
    /**
     * The entity dateFieldEntity.
     */
    public static final EntityDefinitionReferenceProvider dateFieldEntity = generateEntity("dateFieldEntity");
    /**
     * The entity numericOperatorEntity.
     */
    public static final EntityDefinitionReferenceProvider numericOperatorEntity = generateEntity("numericOperatorEntity");
    /**
     * The entity textualOperatorEntity.
     */
    public static final EntityDefinitionReferenceProvider textualOperatorEntity = generateEntity("textualOperatorEntity");
    /**
     * The entity dateOperatorEntity.
     */
    public static final EntityDefinitionReferenceProvider dateOperatorEntity = generateEntity("dateOperatorEntity");

}
