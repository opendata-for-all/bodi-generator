package bodi.generator.dataSchema;

import bodi.generator.dataSource.TabularDataSource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.isNull;


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

    @Serial
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
     * Collection of row names or aliases of the {@link DataSchema}, in different languages.
     * <p>
     * Row names are the different ways one could refer to the rows of a dataset.
     */
    private Map<String, Set<String>> rowNames;

    /**
     * Instantiates a new empty {@link DataSchema}.
     */
    public DataSchema() {
        schemaTypes = new ArrayList<>();
        rowNames = new HashMap<>();
        for (String language : DataSchema.languages) {
            rowNames.put(language, new HashSet<>());
        }
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

    /**
     * Gets the row names of the {@link DataSchema}.
     *
     * @return the row names
     */
    public Map<String, Set<String>> getRowNames() {
        return rowNames;
    }

    /**
     * Generates a JSON object containing all the row names.
     *
     * @param defaultRowNamesJson the default row names, if any, which are generic row names for every Data Schema
     * @return the json object containing the row names and the default row names, if any
     */
    public JSONObject generateRowNamesJson(JSONObject defaultRowNamesJson) {
        JSONObject rowNamesJson = new JSONObject();
        rowNamesJson.put("rowNameEntity", new JSONObject());
        for (String language : DataSchema.languages) {
            rowNamesJson.getJSONObject("rowNameEntity").put(language, new JSONArray());
            if (!isNull(defaultRowNamesJson)) {
                JSONArray defaultRowNamesLang = defaultRowNamesJson.getJSONObject("rowNameEntity").getJSONArray(language);
                rowNamesJson.getJSONObject("rowNameEntity").getJSONArray(language).putAll(defaultRowNamesLang);
            }
            rowNamesJson.getJSONObject("rowNameEntity").getJSONArray(language).putAll(rowNames.get(language));
        }
        return rowNamesJson;
    }

}
