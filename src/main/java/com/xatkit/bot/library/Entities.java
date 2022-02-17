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
     * The name of the file containing the chatbot field entities.
     */
    private static String fieldsJsonFile = "fields.json";

    /**
     * The name of the file containing the chatbot operator entities.
     */
    private static String fieldOperatorsJsonFile = "fieldOperators.json";

    /**
     * Contains the chatbot entities in a JSON format and in the bot language ({@link Bot#language}).
     */
    private static final JSONObject entitiesJson;

    // Merge the json files containing bot entities
    static {
        InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(fieldsJsonFile);
        if (is1 == null) {
            throw new NullPointerException("Cannot find the json file \"" + fieldsJsonFile + "\"");
        }
        InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(fieldOperatorsJsonFile);
        if (is2 == null) {
            throw new NullPointerException("Cannot find the json file \"" + fieldOperatorsJsonFile + "\"");
        }
        JSONObject fields = new JSONObject(new JSONTokener(is1));
        JSONObject fieldOperators = new JSONObject(new JSONTokener(is2));
        entitiesJson = new JSONObject();
        for (String key : fields.keySet()) {
            entitiesJson.put(key, fields.getJSONObject(key));
        }
        for (String key : fieldOperators.keySet()) {
            entitiesJson.put(key, fieldOperators.getJSONObject(key));
        }
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
            for (Object synonym : entityJson.getJSONObject(entry).getJSONObject(Bot.language).getJSONArray("synonyms")) {
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
