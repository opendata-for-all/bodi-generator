package bodiGenerator.dataSchema;

import bodiGenerator.dataSource.TabularDataSource;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a field of a {@link SchemaType}.
 *
 * @see SchemaField
 */
public class SchemaField {

    /**
     * The name of the field as it was in the {@link TabularDataSource} from which it was inferred.
     */
    private String originalName;

    /**
     * A secondary, more human-friendly, name. It may be used instead of {@link #originalName} if is a complex or
     * difficult-to-read name.
     */
    private String readableName;

    /**
     * Collection of synonyms of the field name.
     * <p>
     * In a chatbot, they are useful for the user to be able to refer to a
     * field in different ways. For instance, {@code location} could be a synonym of {@code address}
     */
    private List<String> synonyms;

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
     * Instantiates a new empty {@link SchemaField}
     */
    public SchemaField() {
        synonyms = new ArrayList<>();
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
    public String getReadableName() {
        return readableName;
    }

    /**
     * Sets the readable name of the field.
     *
     * @param readableName the readable name
     */
    public void setReadableName(String readableName) {
        this.readableName = readableName;
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

}
