package bodiGenerator.dataSchema;

import java.util.ArrayList;
import java.util.List;

public class EntityType {

    private String name;
    private List<EntityField> entityFields;

    public EntityType(String name) {
        this.name = name;
        this.entityFields = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<EntityField> getEntityFields() {
        return entityFields;
    }

    public void addEntityField(EntityField entityField) {
        entityFields.add(entityField);
    }

}
