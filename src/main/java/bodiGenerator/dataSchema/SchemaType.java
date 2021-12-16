package bodiGenerator.dataSchema;
import java.util.ArrayList;
import java.util.List;

public class SchemaType {

    private String name;
    private List<SchemaField> schemaFields;

    public SchemaType(String name) {
        this.name = name;
        this.schemaFields = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<SchemaField> getSchemaFields() {
        return schemaFields;
    }

    public void addEntityField(EntityField entityField) {
        entityFields.add(entityField);
    }

}
