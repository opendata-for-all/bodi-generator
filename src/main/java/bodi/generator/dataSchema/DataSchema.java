package bodi.generator.dataSchema;

import bodi.generator.dataSource.TabularDataSource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link DataSchema} is a higher-level definition of a {@link TabularDataSource}. It supplements the information
 * extracted from the raw data with more semantic knowledge, such as synonyms for a specific field or relations between
 * sub-tables.
 * <p>
 * Its main purpose is to use its information to generate more sophisticated chatbot components.
 * <p>
 * This is not a data container. It is only an outline of a data container (i.e. a {@link TabularDataSource})
 */
public class DataSchema implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * A set containing all the available languages of the {@link DataSchema} content.
     */
    public static final Set<String> languages = new HashSet<>(Set.of("en", "ca", "es"));

    /**
     * Collection of {@link SchemaType} generated from a {@link TabularDataSource}.
     */
    private List<SchemaType> schemaTypes;

    /**
     * Instantiates a new empty {@link DataSchema}.
     */
    public DataSchema() {
        schemaTypes = new ArrayList<>();
    }

    /**
     * Add a {@link SchemaType}.
     *
     * @param schemaType the {@link SchemaType} to add
     */
    public void addSchemaType(SchemaType schemaType) {
        schemaTypes.add(schemaType);
    }

    /**
     * Gets a {@link SchemaType}.
     *
     * @param name the name of the {@link SchemaType}
     * @return the {@link SchemaType}, or {@code null} if it does not exist
     */
    public SchemaType getSchemaType(String name) {
        for (SchemaType schemaType : schemaTypes) {
            if (schemaType.getName().equals(name)) {
                return schemaType;
            }
        }
        return null;
    }
}
