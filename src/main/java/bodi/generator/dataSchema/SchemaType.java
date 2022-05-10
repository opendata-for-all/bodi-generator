package bodi.generator.dataSchema;
import bodi.generator.dataSource.TabularDataSource;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


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
public class SchemaType {

    /**
     * The name of the {@link SchemaType}.
     */
    private String name;

    /**
     * The collection of fields that form a {@link SchemaType}.
     */
    private List<SchemaField> schemaFields;

    /**
     * Instantiates a new {@link SchemaType} with a given name.
     *
     * @param name the name
     */
    public SchemaType(String name) {
        this.name = name;
        this.schemaFields = new ArrayList<>();
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
     * Gets a schema field.
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

    /**
     * Adds a {@link SchemaField} to the {@link SchemaType}.
     *
     * @param schemaField the {@link SchemaField}
     */
    public void addSchemaField(SchemaField schemaField) {
        schemaFields.add(schemaField);
    }

    /**
     * Generates a JSON object containing all the fields of the SchemaType, classified by types.
     *
     * @return the json object containing all the fields of the SchemaType
     */
    public JSONObject generateFieldsJson() {
        JSONObject entities = new JSONObject();
        entities.put("numericFieldEntity", new JSONObject());
        entities.put("dateFieldEntity", new JSONObject());
        entities.put("textualFieldEntity", new JSONObject());
        for (SchemaField schemaField : schemaFields) {
            JSONObject entity = schemaField.generateFieldJson();
            switch (schemaField.getType()) {
                case NUMBER:
                    entities.getJSONObject("numericFieldEntity").put(schemaField.getOriginalName(), entity);
                    break;
                case DATE:
                    entities.getJSONObject("dateFieldEntity").put(schemaField.getOriginalName(), entity);
                    break;
                case TEXT:
                case EMPTY:
                    entities.getJSONObject("textualFieldEntity").put(schemaField.getOriginalName(), entity);
                    break;
            }
        }
        return entities;
    }

}
