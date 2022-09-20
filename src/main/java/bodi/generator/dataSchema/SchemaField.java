package bodi.generator.dataSchema;

import bodi.generator.dataSource.TabularDataSource;
import org.json.JSONObject;

import java.io.Serializable;
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
public class SchemaField implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The name of the field as it was in the {@link TabularDataSource} from which it was inferred.
     */
    private String originalName;

    /**
     * A secondary, more human-friendly, name. It may be used instead of {@link #originalName} if is a complex or
     * difficult-to-read name.
     * <p>
     * It is defined in all the languages stored in {@link DataSchema#languages}. The keys are the languages and the values are
     * the actual names in that language.
     */
    private Map<String, String> readableName;

    /**
     * Collection of synonyms of the field name.
     * <p>
     * In a chatbot, they are useful for the user to be able to refer to a field in different ways. For instance,
     * {@code location} could be a synonym of {@code address}.
     * <p>
     * They are defined in all the languages stored in {@link DataSchema#languages}. The keys are the languages and the
     * values are the actual synonym collections in that language.
     */
    private Map<String, Set<String>> synonyms;

    /**
     * It contains a subset of the different values of the field, and this values can contain synonyms in different
     * languages. Note that being a subset is just a possibility. All different values of the field can be stored
     * here. Actually, the most common cases are to store all the values (when there are just a few of values) or none
     * of them (when there are a lot of different values)
     * <p>
     * The keys are the values of the field. The values are maps that contain an entry for each language in
     * {@link DataSchema#languages} and a set of synonyms in that language (if any).
     * <p>
     * Example entry: {"Male", {"en", ["Man"]}, {"es", ["Hombre", "Macho"]}, {"ca", ["Home", "Mascle"]} >
     */
    private Map<String, Map<String, Set<String>>> mainValues;

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
     * Indicates weather this field is categorical or not.
     * <p>
     * This can be useful to consider storing the main values of the field only if it is categorical.
     */
    private boolean categorical;

    /**
     * Indicates weather this field is a key field or not.
     * <p>
     * Key fields can be used, for instance, to show only key fields instead of an entire entry of a data source.
     */
    private boolean key;

    /**
     * Instantiates a new empty {@link SchemaField}.
     */
    public SchemaField() {
        readableName = new HashMap<>();
        synonyms = new HashMap<>();
        for (String language : DataSchema.languages) {
            readableName.put(language, null);
            synonyms.put(language, new HashSet<>());
        }
        this.resetMainValues();
        key = false;
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
     * Gets the readable name of the field.
     *
     * @return the readable name
     */
    public Map<String, String> getReadableName() {
        return readableName;
    }

    /**
     * Gets the readable name of the field in a specific language.
     *
     * @param language the language of the readable name
     * @return the readable name in a specific language
     */
    public String getReadableName(String language) {
        return readableName.get(language);
    }

    /**
     * Sets the readable name of the field in a specific language.
     *
     * @param readableName the readable name
     * @param language     the language
     */
    public void setReadableName(String language, String readableName) {
        this.readableName.put(language, readableName);
    }

    /**
     * Sets the readable name of the field.
     *
     * @param readableName the readable name
     */
    public void setReadableName(Map<String, String> readableName) {
        this.readableName = readableName;
    }

    /**
     * Adds a set of synonyms of the field, in a specific language.
     *
     * @param newSynonyms the synonyms to add
     * @param language    the language
     */
    public void addSynonyms(String language, Collection<String> newSynonyms) {
        synonyms.get(language).addAll(newSynonyms);
    }

    /**
     * Gets the set of synonyms of the field, in a specific language.
     *
     * @param language the language
     * @return the synonyms
     */
    public Set<String> getSynonyms(String language) {
        return synonyms.get(language);
    }

    /**
     * Gets the synonyms of the field.
     *
     * @return the synonyms
     */
    public Map<String, Set<String>> getSynonyms() {
        return synonyms;
    }

    /**
     * Sets the synonyms of the field.
     *
     * @param synonyms the synonyms
     */
    public void setSynonyms(Map<String, Set<String>> synonyms) {
        this.synonyms = synonyms;
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
     * Adds a set of values to {@link #mainValues}, with no synonyms in any value.
     * @param values the main values to add
     */
    public void addMainValues(Set<String> values) {
        for (String value : values) {
            if (!value.isEmpty()) {
                Map<String, Set<String>> valueSynonyms = new HashMap<>();
                for (String language: DataSchema.languages) {
                    valueSynonyms.put(language, new HashSet<>());
                }
                mainValues.put(value, valueSynonyms);
            }
        }
    }

    /**
     * Gets the main values of the field.
     *
     * @return the main values
     */
    public Map<String, Map<String, Set<String>>> getMainValues() {
        return mainValues;
    }

    /**
     * Sets the main values of the field.
     *
     * @param mainValues the main values to add
     */
    public void setMainValues(Map<String, Map<String, Set<String>>> mainValues) {
        this.mainValues = mainValues;
    }

    /**
     * Resets the main values, deleting all values that were stored.
     */
    public void resetMainValues() {
        mainValues = new HashMap<>();
    }

    /**
     * Gets the value of the categorical attribute.
     *
     * @return true if the field is categorical, false otherwise
     */
    public boolean isCategorical() {
        return categorical;
    }

    /**
     * Sets the value of the categorical attribute.
     *
     * @param categorical the new value of the categorical attribute
     */
    public void setCategorical(boolean categorical) {
        this.categorical = categorical;
    }

    /**
     * Gets the value of the key attribute.
     *
     * @return true if the field is a key field, false otherwise
     */
    public boolean isKey() {
        return key;
    }

    /**
     * Sets the value of the key attribute.
     *
     * @param key the new value of the key attribute
     */
    public void setKey(boolean key) {
        this.key = key;
    }

    /**
     * Generates a JSON object containing all the information of the SchemaField.
     *
     * @return the json object containing the SchemaField information
     */
    public JSONObject generateFieldJson() {
        JSONObject entity = new JSONObject();
        for (String language : DataSchema.languages) {
            JSONObject languageEntity = new JSONObject();
            languageEntity.put("readableName", this.getReadableName(language));
            languageEntity.put("synonyms", this.getSynonyms(language));
            entity.put(language, languageEntity);
        }
        entity.put("numDifferentValues", this.numDifferentValues);
        entity.put("key", this.key);
        JSONObject valuesEntity = new JSONObject();
        for (var entry : mainValues.entrySet()) {
            JSONObject languagesEntity = new JSONObject();
            String value = entry.getKey();
            for (var entry2 : entry.getValue().entrySet()) {
                languagesEntity.put(entry2.getKey(), entry2.getValue());
            }
            valuesEntity.put(value, languagesEntity);
        }
        entity.put("values", valuesEntity);
        return entity;
    }
}
