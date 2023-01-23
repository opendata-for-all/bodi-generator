package com.xatkit.bot.library;

import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;
import com.xatkit.dsl.entity.MappingEntryStep;
import com.xatkit.dsl.entity.MappingSynonymStep;
import com.xatkit.intent.EntityDefinition;
import com.xatkit.intent.MappingEntityDefinition;
import com.xatkit.intent.MappingEntityDefinitionEntry;
import fr.inria.atlanmod.commons.log.Log;
import lombok.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * The collection of field groups.
     * <p>
     * The keys are the field group names and the values are the collections of fields each field group includes.
     *
     * @see com.xatkit.bot.customQuery.SpecifyEntities
     */
    public Map<String, Set<String>> fieldGroups;

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
    public EntityDefinitionReferenceProvider numericFieldEntity;
    /**
     * The entity textualFieldEntity.
     */
    public EntityDefinitionReferenceProvider textualFieldEntity;
    /**
     * The entity datetimeFieldEntity.
     */
    public EntityDefinitionReferenceProvider datetimeFieldEntity;
    /**
     * The entity fieldEntity (combines {@link #numericFieldEntity}, {@link #textualFieldEntity} and
     * {@link #datetimeFieldEntity}).
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
     * The entity datetimeOperatorEntity.
     */
    public final EntityDefinitionReferenceProvider datetimeOperatorEntity;

    /**
     * The entity numericFunctionOperatorEntity.
     */
    public final EntityDefinitionReferenceProvider numericFunctionOperatorEntity;
    /**
     * The entity datetimeFunctionOperatorEntity.
     */
    public final EntityDefinitionReferenceProvider datetimeFunctionOperatorEntity;
    /**
     * The entity functionOperatorEntity (combines {@link #numericFunctionOperatorEntity} and
     * {@link #datetimeFunctionOperatorEntity}).
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
        this.fieldGroups = new HashMap<>();

        numericFieldEntity = generateEntity("numericFieldEntity");
        textualFieldEntity = generateEntity("textualFieldEntity");
        datetimeFieldEntity = generateEntity("datetimeFieldEntity");
        readFieldGroups();
        fieldEntity = mergeEntities("fieldEntity", numericFieldEntity, textualFieldEntity, datetimeFieldEntity);

        numericOperatorEntity = generateEntity("numericOperatorEntity");
        textualOperatorEntity = generateEntity("textualOperatorEntity");
        datetimeOperatorEntity = generateEntity("datetimeOperatorEntity");

        numericFunctionOperatorEntity = generateEntity("numericFunctionOperatorEntity");
        datetimeFunctionOperatorEntity = generateEntity("datetimeFunctionOperatorEntity");
        functionOperatorEntity = mergeEntities("functionOperator", numericFunctionOperatorEntity, datetimeFunctionOperatorEntity);

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
                    this.readableNames.put(entry, readableName);
                    if (!entry.equals(readableName)) {
                        // Add the readable name as an entity synonym
                        synonymStep.synonym(readableName);
                    }
                } else {
                    this.readableNames.put(entry, entry);
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
        // Check there are no duplicated readable names
        Set<String> readableNamesSet = new HashSet<>(this.readableNames.values());
        List<String> readableNamesList = new ArrayList<>(this.readableNames.values());
        if (readableNamesSet.size() < readableNamesList.size()) {
            throw new IllegalArgumentException("duplicated readable names were found in field entities");
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

    /**
     * Reads the field groups in {@link #entitiesJson} and fills {@link #fieldGroups}. Also adds the field groups in
     * their corresponding field entity (depending on the field group type), so they can be matched with the entities.
     */
    private void readFieldGroups() {
        String entityName = "fieldGroups";
        try {
            JSONObject fieldGroupsJson = entitiesJson.getJSONObject(entityName);
            MappingEntryStep numericFieldGroupsEntity = mapping("numericFieldGroupsEntity");
            MappingEntryStep textualFieldGroupsEntity = mapping("textualFieldGroupsEntity");
            MappingEntryStep datetimeFieldGroupsEntity = mapping("datetimeFieldGroupsEntity");
            for (String entry : fieldGroupsJson.keySet()) {
                JSONObject fieldGroupJson = fieldGroupsJson.getJSONObject(entry);
                String type = fieldGroupJson.getString("type");
                JSONArray fieldGroupNames = fieldGroupJson.getJSONArray(language);
                List<String> fieldList = fieldGroupJson.getJSONArray("fields").toList().stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.toList());
                // check fields in the list actually exist, if not, remove them
                List<String> realFields = null;
                switch (type) {
                    case "NUMBER":
                        realFields = Utils.getEntityValues(numericFieldEntity);
                        break;
                    case "TEXT":
                        realFields = Utils.getEntityValues(textualFieldEntity);
                        break;
                    case "DATETIME":
                        realFields = Utils.getEntityValues(datetimeFieldEntity);
                        break;
                    default:
                        realFields = new ArrayList<>();
                }
                for (String field : fieldList) {
                    if (!realFields.contains(field)) {
                        fieldList.remove(field);
                    }
                }
                if (!fieldList.isEmpty()) {
                    for (Object fieldGroupName : fieldGroupNames) {
                        String fieldGroupNameString = (String) fieldGroupName;
                        fieldGroups.put(fieldGroupNameString, new HashSet<>(fieldList));
                        switch (type) {
                            case "NUMBER":
                                numericFieldGroupsEntity.entry().value(fieldGroupNameString);
                                break;
                            case "TEXT":
                                textualFieldGroupsEntity.entry().value(fieldGroupNameString);
                                break;
                            case "DATETIME":
                                datetimeFieldGroupsEntity.entry().value(fieldGroupNameString);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            numericFieldEntity = mergeEntities("numericFieldEntity", numericFieldEntity, (EntityDefinitionReferenceProvider) numericFieldGroupsEntity);
            textualFieldEntity = mergeEntities("textualFieldEntity", textualFieldEntity, (EntityDefinitionReferenceProvider) textualFieldGroupsEntity);
            datetimeFieldEntity = mergeEntities("datetimeFieldEntity", datetimeFieldEntity, (EntityDefinitionReferenceProvider) datetimeFieldGroupsEntity);
        } catch (JSONException e) {
            Log.warn(e.getMessage());
        }
    }
}
