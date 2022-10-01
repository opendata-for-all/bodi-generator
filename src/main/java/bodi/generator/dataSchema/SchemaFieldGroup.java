package bodi.generator.dataSchema;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A group/category of {@link SchemaField}.
 */
public class SchemaFieldGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The id or root name of the {@link SchemaFieldGroup}.
     */
    private String name;

    /**
     * Collection of names of the {@link SchemaFieldGroup}.
     * <p>
     * They are defined in all the languages stored in {@link DataSchema#languages}. The keys are the languages and the
     * values are the actual name collections in that language.
     */
    private Map<String, Set<String>> languageNames;

    /**
     * The data type of the field group.
     * <p>
     * The data type is inferred from the type of the schema fields in {@link #schemaFields}.
     * @see DataType
     */
    private DataType type;

    /**
     * The collection of {@link SchemaField} associated to the {@link SchemaFieldGroup}.
     */
    private List<SchemaField> schemaFields;

    /**
     * Instantiates a new empty {@link SchemaFieldGroup}.
     */
    public SchemaFieldGroup() {
        languageNames = new HashMap<>();
        schemaFields = new ArrayList<>();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets all the language names.
     *
     * @return the language names
     */
    public Map<String, Set<String>> getLanguageNames() {
        return languageNames;
    }

    /**
     * Gets the language names in a specific language.
     *
     * @param language the language
     * @return the names associated to the given language
     */
    public Set<String> getLanguageNames(String language) {
        return languageNames.get(language);
    }

    /**
     * Sets the language names in a specific language.
     *
     * @param language the language
     * @param names    the names
     */
    public void setLanguageNames(String language, Set<String> names) {
        this.languageNames.put(language, names);
    }

    /**
     * Sets all the language names.
     *
     * @param languageNames the language names
     */
    public void setLanguageNames(Map<String, Set<String>> languageNames) {
        this.languageNames = languageNames;
    }

    /**
     * Gets the type of the field group.
     *
     * @return the type
     * @see DataType
     */
    public DataType getType() {
        return type;
    }

    /**
     * Sets the type of the field group.
     *
     * @param type the type
     * @see DataType
     */
    public void setType(DataType type) {
        this.type = type;
    }

    /**
     * Gets the collection of schema fields of the field group.
     *
     * @return the schema fields
     */
    public List<SchemaField> getSchemaFields() {
        return schemaFields;
    }

    /**
     * Gets a schema field of the collection of schema fields of the field group.
     *
     * @param originalName the original name of the schema field
     * @return the schema field
     */
    public SchemaField getSchemaField(String originalName) {
        for (SchemaField schemaField : schemaFields) {
            if (schemaField.getOriginalName().equals(originalName)) {
                return schemaField;
            }
        }
        return null;
    }

    /**
     * Add schema field to the collection of schema fields of the field group.
     *
     * @param schemaField the schema field
     */
    public void addSchemaField(SchemaField schemaField) {
        schemaFields.add(schemaField);
        this.recomputeDataType();
    }

    /**
     * Delete schema field from the collection of schema fields of the field group.
     *
     * @param schemaField the schema field to delete
     */
    public void deleteSchemaField(SchemaField schemaField) {
        schemaFields.remove(schemaField);
        this.recomputeDataType();
    }

    /**
     * Generates a JSON object containing all the information of the {@link SchemaFieldGroup}.
     *
     * @return the json object containing the SchemaField information
     */
    public JSONObject generateFieldGroupJson() {
        JSONObject entity = new JSONObject();
        entity.put("type", type);
        for (var entry : languageNames.entrySet()) {
            entity.put(entry.getKey(), entry.getValue());
        }
        entity.put("fields", schemaFields.stream().map(SchemaField::getOriginalName).collect(Collectors.toSet()));
        return entity;
    }

    /**
     * Recompute data type of the field group.
     * <p>
     * It is necessary to recompute the data type when a schema field is removed from the field group or when a new
     * schema field is added to the group.
     */
    private void recomputeDataType() {
        Set<DataType> fieldsDataTypes = schemaFields.stream().map(SchemaField::getType).collect(Collectors.toSet());
        if (fieldsDataTypes.size() == 1) {
            this.type = fieldsDataTypes.iterator().next();
        } else if (fieldsDataTypes.size() == 0) {
            this.type = null;
        } else {
            this.type = DataType.TEXT;
        }
    }
}
