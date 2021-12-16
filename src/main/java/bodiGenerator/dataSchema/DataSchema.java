package bodiGenerator.dataSchema;

import java.util.ArrayList;
import java.util.List;

public class DataSchema {

    private List<SchemaType> schemaTypes;

    public DataSchema() {
        schemaTypes = new ArrayList<>();

    }

    public void addSchemaType(SchemaType schemaType) {
        schemaTypes.add(schemaType);
    }

    public SchemaType getSchemaType(String name) {
        for (SchemaType schemaType : schemaTypes) {
            if (schemaType.getName().equals(name)) {
                return schemaType;
            }
        }
        return null;
    }

}
