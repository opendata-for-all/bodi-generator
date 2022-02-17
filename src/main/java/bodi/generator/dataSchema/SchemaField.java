package bodi.generator.dataSchema;

import bodi.generator.dataSource.TabularDataSource;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Represents a field of a {@link SchemaType}.
 *
 * @see SchemaField
 */
public class SchemaField {

    /**
     * A set containing all the possible languages of the fields.
     */
    public static final Set<String> languages = new HashSet<>(Set.of("en", "ca", "es"));

    /**
     * The name of the field as it was in the {@link TabularDataSource} from which it was inferred.
     */
    private String originalName;

    /**
     * A secondary, more human-friendly, name. It may be used instead of {@link #originalName} if is a complex or
     * difficult-to-read name.
     * <p>
     * It is defined in all the languages stored in {@link #languages}. The keys are the languages and the values are
     * the actual names in that language.
     */
    private Map<String, String> readableName;

    /**
     * Collection of synonyms of the field name.
     * <p>
     * In a chatbot, they are useful for the user to be able to refer to a field in different ways. For instance,
     * {@code location} could be a synonym of {@code address}.
     * <p>
     * They are defined in all the languages stored in {@link #languages}. The keys are the languages and the
     * values are the actual synonym collections in that language.
     */
    private Map<String, Set<String>> synonyms;

    /**
     * The data type of the field.
     *
     * @see DataType
     */
    private DataType type;

    /**
     * The number of different values that were seen within this field when reading the {@link TabularDataSource}
     * <p>
     * This can be useful in a chatbot, for instance, to show the user all possible options of a field (e.g. when
     * filtering a table) iff {@code numDifferentValues < n}, for some {@code n}
     */
    private int numDifferentValues;

    /**
     * Instantiates a new empty {@link SchemaField}.
     */
    public SchemaField() {
        readableName = new HashMap<>();
        synonyms = new HashMap<>();
        for (String language : languages) {
            readableName.put(language, null);
            synonyms.put(language, new HashSet<>());
        }
    }

    /**
     * Gets the original name of the field.
     *
     * @return the original name
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * Sets the original name of the field.
     *
     * @param originalName the original name
     */
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    /**
     * Gets the readable name of the field in a specific language.
     *
     * @return the readable name
     */
    public String getReadableName(String language) {
        return readableName.get(language);
    }

    /**
     * Sets the readable name of the field in a specific language.
     *
     * @param readableName the readable name
     */
    public void setReadableName(String language, String readableName) {
        this.readableName.put(language, readableName);
    }

    /**
     * Adds a set of synonyms of the field, in a specific language.
     *
     * @param newSynonyms the synonyms to add
     */
    public void addSynonyms(String language, Collection<String> newSynonyms) {
        synonyms.get(language).addAll(newSynonyms);
    }

    /**
     * Gets the list of synonyms of the field, in a specific language.
     *
     * @return the synonyms
     */
    public Set<String> getSynonyms(String language) {
        return synonyms.get(language);
    }

    /**
     * Gets the type of the field.
     *
     * @return the type
     */
    public DataType getType() {
        return type;
    }

    /**
     * Sets the type of the field.
     *
     * @param type the type
     * @see DataType
     */
    public void setType(DataType type) {
        this.type = type;
    }

    /**
     * Gets the number of different values this field has.
     *
     * @return the number of different values
     */
    public int getNumDifferentValues() {
        return numDifferentValues;
    }

    /**
     * Sets the number of different values this field has.
     *
     * @param numDifferentValues the number of different values
     */
    public void setNumDifferentValues(int numDifferentValues) {
        this.numDifferentValues = numDifferentValues;
    }

    /**
     * Generates a JSON object containing all the information of the SchemaField.
     *
     * @return the json object containing the SchemaField information
     */
    public JSONObject generateFieldJson() {
        JSONObject entity = new JSONObject();
        for (String language : languages) {
            JSONObject languageEntity = new JSONObject();
            languageEntity.put("readableName", this.getReadableName(language));
            languageEntity.put("synonyms", this.getSynonyms(language));
            entity.put(language, languageEntity);
        }
        entity.put("numDifferentValues", this.numDifferentValues);
        return entity;
    }
}
