package bodiGenerator.dataSchema;

import java.util.ArrayList;
import java.util.List;

public class DataSchema {

    private List<EntityType> entityTypes;

    public DataSchema() {
        entityTypes = new ArrayList<>();

    }

    public void addEntityType(EntityType entityType) {
        entityTypes.add(entityType);
    }

    public EntityType getEntityType(String name) {
        for (EntityType entityType : entityTypes) {
            if (entityType.getName().equals(name)) {
                return entityType;
            }
        }
        return null;
    }

}
