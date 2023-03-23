package bodi.generator.dataSchema;

import bodi.generator.dataSource.TabularDataSource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * A data type definition inferred from a {@link TabularDataSource}. It can be formed from a whole table or
 * from a set of columns of a table.
 * <p>
 * This type consists of a set of {@link SchemaField} and is a part of a {@link DataSchema}
 *
 * @see DataSchema
 * @see SchemaField
 * @see TabularDataSource
 */
public class SchemaType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The name of the {@link SchemaType}.
     */
    private String name;

    /**
     * The collection of fields that form a {@link SchemaType}.
     */
    private List<SchemaField> schemaFields;

    /**
     * The collection of field groups of the {@link SchemaType}.
     */
    private List<SchemaFieldGroup> schemaFieldGroups;

    /**
     * The collection of fields that have been deleted from the {@link SchemaType}. Used to recover deleted fields.
     */
    private List<SchemaField> deletedSchemaFields;

    /**
     * Collection of row names or aliases of the {@link SchemaType}, in different languages.
     * <p>
     * Row names are the different ways one could refer to the rows of a dataset.
     */
    private Map<String, Set<String>> rowNames;

    /**
     * Instantiates a new {@link SchemaType} with a given name.
     *
     * @param name the name
     */
    public SchemaType(String name) {
        this.name = name;
        this.schemaFields = new ArrayList<>();
        this.schemaFieldGroups = new ArrayList<>();
        this.deletedSchemaFields = new ArrayList<>();
        this.rowNames = new HashMap<>();
        String defaultRowNamesFile = "defaultRowNames.json";
        InputStream rowNamesIS = Thread.currentThread().getContextClassLoader().getResourceAsStream(defaultRowNamesFile);
        if (rowNamesIS == null) {
            System.out.println("Cannot find the default row names file \"" + defaultRowNamesFile + "\"");
        } else {
            JSONObject defaultRowNamesJson = new JSONObject(new JSONTokener(rowNamesIS));
            for (String language : DataSchema.languages) {
                JSONArray defaultRowNamesLang = defaultRowNamesJson.getJSONObject("rowNameEntity").getJSONArray(language);
                rowNames.put(language, new HashSet<>(defaultRowNamesLang.toList().stream()
                        .map(object -> Objects.toString(object, null))
                        .toList()));
            }
        }
    }

    /**
     * Gets the name of the {@link SchemaType}.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the collection of {@link SchemaField}.
     *
     * @return the schema fields
     */
    public List<SchemaField> getSchemaFields() {
        return schemaFields;
    }

    /**
     * Gets the collection of {@link SchemaFieldGroup}.
     *
     * @return the schema field groups
     */
    public List<SchemaFieldGroup> getSchemaFieldGroups() {
        return schemaFieldGroups;
    }

    /**
     * Gets the collection of deleted {@link SchemaField}.
     *
     * @return the deleted schema fields
     */
    public List<SchemaField> getDeletedSchemaFields() {
        return deletedSchemaFields;
    }

    /**
     * Gets a {@link SchemaField}.
     *
     * @param originalName the original name of the schema field
     * @return if it exists, the schema field. Otherwise, {@code null}
     */
    public SchemaField getSchemaField(String originalName) {
        for (SchemaField schemaField : schemaFields) {
            if (schemaField.getOriginalName().equals(originalName)) {
                return schemaField;
            }
        }
        return null;
    }

    public SchemaFieldGroup getSchemaFieldGroup(String name) {
        for (SchemaFieldGroup schemaFieldGroup : schemaFieldGroups) {
            if (schemaFieldGroup.getName().equals(name)) {
                return schemaFieldGroup;
            }
        }
        return null;
    }

    /**
     * Gets a deleted {@link SchemaField}.
     *
     * @param originalName the original name of the schema field
     * @return if it exists, the schema field. Otherwise, {@code null}
     */
    public SchemaField getDeletedSchemaField(String originalName) {
        for (SchemaField schemaField : deletedSchemaFields) {
            if (schemaField.getOriginalName().equals(originalName)) {
                return schemaField;
            }
        }
        return null;
    }

    /**
     * Adds a {@link SchemaField} to the {@link SchemaType}.
     *
     * @param schemaField the {@link SchemaField}
     */
    public void addSchemaField(SchemaField schemaField) {
        schemaFields.add(schemaField);
    }

    /**
     * Adds a {@link SchemaFieldGroup} to the {@link SchemaType}.
     *
     * @param schemaFieldGroup the {@link SchemaFieldGroup}
     */
    public void addSchemaFieldGroup(SchemaFieldGroup schemaFieldGroup) {
        schemaFieldGroups.add(schemaFieldGroup);
    }

    /**
     * Add a new schema field to the schema type, which is a merger of other schema fields.
     * <p>
     * The new schema field is a {@link DataType#TEXT} schema field.
     *
     * @param name                 the name of the new schema field
     * @param fieldsToMerge        the names of the schema fields to merge
     * @param categorical          the categorical condition of the new schema field
     * @param removeOriginalFields if true, remove the original schema fields, otherwise not
     * @param tds                  the tabular data source containing the merged field
     */
    public void mergeTextualSchemaFields(String name, List<String> fieldsToMerge, boolean categorical,
                                         boolean removeOriginalFields, TabularDataSource tds) {
        SchemaField newSchemaField = new SchemaField();
        newSchemaField.setType(DataType.TEXT);
        newSchemaField.setOriginalName(name);
        for (String language : DataSchema.languages) {
            newSchemaField.setReadableName(language, name);
        }
        newSchemaField.setCategorical(categorical);
        Set<String> mainValues = tds.getColumnUniqueValues(newSchemaField.getOriginalName());
        newSchemaField.setNumDifferentValues(mainValues.size());
        if (newSchemaField.isCategorical()) {
            newSchemaField.addMainValues(mainValues);
        }
        if (removeOriginalFields) {
            for (String originalField : fieldsToMerge) {
                this.deleteSchemaField(originalField);
            }
        }
        this.addSchemaField(newSchemaField);
    }

    /**
     * Deletes a {@link SchemaField}.
     *
     * @param originalName the original name of the schema field to delete
     */
    public void deleteSchemaField(String originalName) {
        for (SchemaField schemaField : schemaFields) {
            if (schemaField.getOriginalName().equals(originalName)) {
                this.deleteSchemaField(schemaField);
                break;
            }
        }
    }

    /**
     * Deletes a {@link SchemaField}.
     *
     * @param schemaField the schema field to delete
     */
    public void deleteSchemaField(SchemaField schemaField) {
        deletedSchemaFields.add(schemaField);
        schemaFields.remove(schemaField);
    }

    /**
     * Recovers a deleted {@link SchemaField}.
     *
     * @param originalName the name of the schema field to be recovered
     */
    public void recoverSchemaField(String originalName) {
        SchemaField schemaField = this.getDeletedSchemaField(originalName);
        this.addSchemaField(schemaField);
        deletedSchemaFields.remove(schemaField);
    }

    /**
     * Deletes a {@link SchemaFieldGroup}.
     *
     * @param schemaFieldGroup the schema field group to delete
     */
    public void deleteSchemaFieldGroup(SchemaFieldGroup schemaFieldGroup) {
        schemaFieldGroups.remove(schemaFieldGroup);
    }

    /**
     * Deletes a {@link SchemaFieldGroup}.
     *
     * @param name the name of the schema field group to delete
     */
    public void deleteSchemaFieldGroup(String name) {
        for (SchemaFieldGroup schemaFieldGroup : schemaFieldGroups) {
            if (schemaFieldGroup.getName().equals(name)) {
                this.deleteSchemaFieldGroup(schemaFieldGroup);
                break;
            }
        }
    }

    /**
     * Gets the row names of the {@link SchemaType}.
     *
     * @return the row names
     */
    public Map<String, Set<String>> getRowNames() {
        return rowNames;
    }

    /**
     * Generates a JSON object containing all the fields of the SchemaType, classified by types.
     *
     * @return the json object containing all the fields of the SchemaType
     */
    public JSONObject generateFieldsJson() {
        JSONObject entities = new JSONObject();
        entities.put("numericFieldEntity", new JSONObject());
        entities.put("datetimeFieldEntity", new JSONObject());
        entities.put("textualFieldEntity", new JSONObject());
        entities.put("fieldGroups", new JSONObject());
        for (SchemaField schemaField : schemaFields) {
            JSONObject entity = schemaField.generateFieldJson();
            switch (schemaField.getType()) {
                case NUMBER:
                    entities.getJSONObject("numericFieldEntity").put(schemaField.getOriginalName(), entity);
                    break;
                case DATETIME:
                    entities.getJSONObject("datetimeFieldEntity").put(schemaField.getOriginalName(), entity);
                    break;
                case TEXT:
                case EMPTY:
                    entities.getJSONObject("textualFieldEntity").put(schemaField.getOriginalName(), entity);
                    break;
            }
        }
        for (SchemaFieldGroup schemaFieldGroup : schemaFieldGroups) {
            if (schemaFieldGroup.getSchemaFields().size() >= 2) {
                JSONObject entity = schemaFieldGroup.generateFieldGroupJson();
                entities.getJSONObject("fieldGroups").put(schemaFieldGroup.getName(), entity);
            }
        }
        return entities;
    }

    /**
     * Generates a JSON object containing all the row names.
     *
     * @return the json object containing the row names and the default row names, if any
     */
    public JSONObject generateRowNamesJson() {
        JSONObject rowNamesJson = new JSONObject();
        rowNamesJson.put("rowNameEntity", new JSONObject());
        for (String language : DataSchema.languages) {
            rowNamesJson.getJSONObject("rowNameEntity").put(language, new JSONArray());
            rowNamesJson.getJSONObject("rowNameEntity").getJSONArray(language).putAll(rowNames.get(language));
        }
        return rowNamesJson;
    }
}
