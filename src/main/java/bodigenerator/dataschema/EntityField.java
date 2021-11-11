package bodigenerator.dataschema;

import java.util.ArrayList;
import java.util.List;

public class EntityField {

    private String originalName;
    private String readableName;
    private List<String> synonyms;
    private String type;
    private int numDifferentValues;

    public EntityField() {
        synonyms = new ArrayList<>();
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getReadableName() {
        return readableName;
    }

    public void setReadableName(String readableName) {
        this.readableName = readableName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNumDifferentValues() {
        return numDifferentValues;
    }

    public void setNumDifferentValues(int numDifferentValues) {
        this.numDifferentValues = numDifferentValues;
    }

}
