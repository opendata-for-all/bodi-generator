package bodiGenerator.dataSchema;
import bodiGenerator.dataSource.TabularDataSource;

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
     * The name of the {@link SchemaType}
     */
    private String name;

    /**
     * The collection of fields that form a {@link SchemaType}
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
     * Gets the name of the {@link SchemaType}
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the collection of {@link SchemaField}
     *
     * @return the schema fields
     */
    public List<SchemaField> getSchemaFields() {
        return schemaFields;
    }

    /**
     * Adds a {@link SchemaField} to the {@link SchemaType}
     *
     * @param schemaField the {@link SchemaField}
     */
    public void addSchemaField(SchemaField schemaField) {
        schemaFields.add(schemaField);
    }

}
