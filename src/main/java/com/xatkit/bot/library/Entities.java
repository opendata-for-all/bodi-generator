package com.xatkit.bot.library;

import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;
import com.xatkit.dsl.entity.MappingEntryStep;
import com.xatkit.dsl.entity.MappingSynonymStep;
import com.xatkit.intent.EntityDefinition;
import com.xatkit.intent.MappingEntityDefinition;
import com.xatkit.intent.MappingEntityDefinitionEntry;
import lombok.NonNull;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xatkit.dsl.DSL.mapping;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * A set of entities the chatbot can recognize.
 */
public class Entities {

    /**
     * The language of the entities.
     */
    private final String language;

    /**
     * The name of the file containing the chatbot field entities.
     */
    private static String fieldsJsonFile = "fields.json";

    /**
     * The name of the file containing the chatbot operator entities.
     */
    private static String fieldOperatorsJsonFile = "fieldOperators.json";

    /**
     * The name of the file containing the chatbot associated row names.
     */
    private static String rowNamesJsonFile = "rowNames.json";

    /**
     * Contains the chatbot entities in a JSON format.
     */
    private static final JSONObject entitiesJson;

    /**
     * Contains the chatbot associated row names in a JSON format.
     */
    private static final JSONObject rowNamesJson;

    /**
     * The keys of this map are the entries of the entity {@link #fieldValueEntity} (i.e. the values of the fields),
     * and the values of the map are the field they belong to.
     * <p>
     * This data structure is necessary since {@link #fieldValueEntity} contains the values of all fields together, so
     * we need this to know which field they belong to.
     */
    public static Map<String, String> fieldValueMap;

    /**
     * The readable names associated to each field entity entry.
     * <p>
     * The field entities entries contain the original names of the fields (i.e. the names extracted from a csv
     * header). Additionally, a field entity entry can have a readable name which, as its name suggests, it is a more
     * friendly name of the field name that can be used to display it to the user.
     */
    public Map<String, String> readableNames;

    /**
     * The list of {@link #fieldEntity} considered as key fields.
     * <p>
     * It is used to display these fields in some bot answers, instead of an entire row.
     */
    public List<String> keyFields;

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
        InputStream is3 = Thread.currentThread().getContextClassLoader().getResourceAsStream(rowNamesJsonFile);
        if (is3 == null) {
            throw new NullPointerException("Cannot find the json file \"" + rowNamesJsonFile + "\"");
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
        rowNamesJson = new JSONObject(new JSONTokener(is3));
    }

    /**
     * The entity numericFieldEntity.
     */
    public final EntityDefinitionReferenceProvider numericFieldEntity;
    /**
     * The entity textualFieldEntity.
     */
    public final EntityDefinitionReferenceProvider textualFieldEntity;
    /**
     * The entity dateFieldEntity.
     */
    public final EntityDefinitionReferenceProvider dateFieldEntity;
    /**
     * The entity fieldEntity (combines {@link #numericFieldEntity}, {@link #textualFieldEntity} and
     * {@link #dateFieldEntity}).
     */
    public final EntityDefinitionReferenceProvider fieldEntity;

    /**
     * The entity numericOperatorEntity.
     */
    public final EntityDefinitionReferenceProvider numericOperatorEntity;
    /**
     * The entity textualOperatorEntity.
     */
    public final EntityDefinitionReferenceProvider textualOperatorEntity;
    /**
     * The entity dateOperatorEntity.
     */
    public final EntityDefinitionReferenceProvider dateOperatorEntity;

    /**
     * The entity numericFunctionOperatorEntity.
     */
    public final EntityDefinitionReferenceProvider numericFunctionOperatorEntity;
    /**
     * The entity dateFunctionOperatorEntity.
     */
    public final EntityDefinitionReferenceProvider dateFunctionOperatorEntity;
    /**
     * The entity functionOperatorEntity (combines {@link #numericFunctionOperatorEntity} and
     * {@link #dateFunctionOperatorEntity}).
     */
    public final EntityDefinitionReferenceProvider functionOperatorEntity;

    /**
     * The entity fieldValueEntity.
     */
    public final EntityDefinitionReferenceProvider fieldValueEntity;

    public final EntityDefinitionReferenceProvider rowNameEntity;

    /**
     * Instantiates a new {@link Entities} object.
     *
     * @param language the language of the entities
     */
    public Entities(String language) {
        this.language = language;
        this.keyFields = new ArrayList<>();
        this.readableNames = new HashMap<>();

        numericFieldEntity = generateEntity("numericFieldEntity");
        textualFieldEntity = generateEntity("textualFieldEntity");
        dateFieldEntity = generateEntity("dateFieldEntity");
        fieldEntity = mergeEntities("fieldEntity", numericFieldEntity, textualFieldEntity, dateFieldEntity);

        numericOperatorEntity = generateEntity("numericOperatorEntity");
        textualOperatorEntity = generateEntity("textualOperatorEntity");
        dateOperatorEntity = generateEntity("dateOperatorEntity");

        numericFunctionOperatorEntity = generateEntity("numericFunctionOperatorEntity");
        dateFunctionOperatorEntity = generateEntity("dateFunctionOperatorEntity");
        functionOperatorEntity = mergeEntities("functionOperator", numericFunctionOperatorEntity, dateFunctionOperatorEntity);

        fieldValueEntity = generateFieldValueEntity();

        rowNameEntity = generateRowNameEntity();
    }

    /**
     * Generates a chatbot entity.
     * <p>
     * It extracts the values and synonyms of the values of the entity from {@link #entitiesJson}
     *
     * @param entityName the name of the entity
     * @return the entity object
     */
    private EntityDefinitionReferenceProvider generateEntity(String entityName) {
        JSONObject entityJson = entitiesJson.getJSONObject(entityName);
        MappingEntryStep entity = mapping(entityName);
        for (String entry : entityJson.keySet()) {
            MappingSynonymStep synonymStep = entity.entry().value(entry);
            try {
                String readableName = entityJson.getJSONObject(entry).getJSONObject(language).getString("readableName");
                if (!isEmpty(readableName)) {
                    // Add the readable name as an entity synonym
                    synonymStep.synonym(readableName);
                    this.readableNames.put(entry, readableName);
                }
            } catch (Exception ignored) { }
            for (Object synonym : entityJson.getJSONObject(entry).getJSONObject(language).getJSONArray("synonyms")) {
                synonymStep.synonym((String) synonym);
            }
            try {
                boolean key = entityJson.getJSONObject(entry).getBoolean("key");
                if (key) {
                    this.keyFields.add(entry);
                }
            } catch (Exception ignored) { }
        }
        return (EntityDefinitionReferenceProvider) entity;
    }

    /**
     * Generates a chatbot entity, combining entries of different entities.
     *
     * @param entityName the name of the entity
     * @param entities   the entities that the new entity will be based on
     * @return the entity object
     */
    private static EntityDefinitionReferenceProvider mergeEntities(String entityName, @NonNull EntityDefinitionReferenceProvider... entities) {
        MappingEntryStep newEntity = mapping(entityName);
        for (EntityDefinitionReferenceProvider entity : entities) {
            EntityDefinition referredEntity = entity.getEntityReference().getReferredEntity();
            if (referredEntity instanceof MappingEntityDefinition) {
                MappingEntityDefinition mapping = (MappingEntityDefinition) referredEntity;
                for (MappingEntityDefinitionEntry entry : mapping.getEntries()) {
                    MappingSynonymStep synonymStep = newEntity.entry().value(entry.getReferenceValue());
                    for (String synonym : entry.getSynonyms()) {
                        synonymStep.synonym(synonym);
                    }
                }
            } else {
                throw new IllegalArgumentException(MessageFormat.format("Expected a {0}, found a {1}",
                        MappingEntityDefinition.class.getSimpleName(), referredEntity.getClass().getSimpleName()));
            }
        }
        return (EntityDefinitionReferenceProvider) newEntity;
    }

    /**
     * Generates the fieldValueEntity, which contains an entry for each value of all fields (and their optional
     * synonyms). The values of the fields are stored in {@link #entitiesJson}. Note that not all values are
     * necessarily stored. Only those that are considered important for the chatbot should be stored.
     *
     * @return the fieldValueEntity
     */
    private EntityDefinitionReferenceProvider generateFieldValueEntity() {
        fieldValueMap = new HashMap<>();
        MappingEntryStep entity = mapping("fieldValueEntity");
        for (String typeFieldEntityName : entitiesJson.keySet()) {
            for (String fieldName : entitiesJson.getJSONObject(typeFieldEntityName).keySet()) {
                JSONObject field = entitiesJson.getJSONObject(typeFieldEntityName).getJSONObject(fieldName);
                if (field.has("values") && !field.getJSONObject("values").isEmpty()) {
                    JSONObject fieldValues = field.getJSONObject("values");
                    for (String entryName : fieldValues.keySet()) {
                        JSONObject valueObject = fieldValues.getJSONObject(entryName);
                        MappingSynonymStep synonymStep = entity.entry().value(entryName);
                        for (Object synonym : valueObject.getJSONArray(language)) {
                            synonymStep.synonym((String) synonym);
                        }
                        fieldValueMap.put(entryName, fieldName);
                    }
                }
            }
        }
        return (EntityDefinitionReferenceProvider) entity;
    }

    private EntityDefinitionReferenceProvider generateRowNameEntity() {
        String entityName = "rowNameEntity";
        JSONObject entityJson = rowNamesJson.getJSONObject(entityName);
        MappingEntryStep entity = mapping(entityName);
        for (Object entry : entityJson.getJSONArray(language)) {
            entity.entry().value((String) entry);
        }
        return (EntityDefinitionReferenceProvider) entity;
    }
}
