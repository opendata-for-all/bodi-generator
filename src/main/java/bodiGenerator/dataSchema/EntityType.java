package bodiGenerator.dataSchema;

import bodiGenerator.dataSource.Row;
import bodiGenerator.dataSource.TabularDataSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static bodiGenerator.dataSchema.DataType.NUMBER;
import static bodiGenerator.dataSchema.DataType.DATE;
import static bodiGenerator.dataSchema.DataType.TEXT;
import static com.xatkit.bot.library.Utils.isDate;
import static com.xatkit.bot.library.Utils.isNumeric;

public class EntityType {

    private String name;
    private List<EntityField> entityFields;

    public EntityType(String name) {
        this.name = name;
        this.entityFields = new ArrayList<>();
    }

    public void fill(TabularDataSource tds) {
        for (String fieldName : tds.getHeader()) {
            Set<String> fieldValuesSet = new HashSet<>();
            Map<DataType, Boolean> dataTypes = new HashMap<>();
            dataTypes.put(NUMBER, true);
            dataTypes.put(DATE, true);
            dataTypes.put(TEXT, true);
            int columnIndex = tds.getHeader().indexOf(fieldName);
            for (Row row : tds.getTableCopy()) {
                String value = row.getColumnValue(columnIndex);
                fieldValuesSet.add(value);
                if (dataTypes.get(NUMBER) && isNumeric(value)) {
                    dataTypes.put(NUMBER, false);
                }
                if (dataTypes.get(DATE) && isDate(value)) {
                    dataTypes.put(DATE, false);
                }
            }
            EntityField entityField = new EntityField();
            entityField.setOriginalName(fieldName);
            entityField.setReadableName(fieldName);
            if (dataTypes.get(DATE)) {
                entityField.setType(DATE);
            } else if (dataTypes.get(NUMBER)) {
                entityField.setType(NUMBER);
            } else {
                entityField.setType(TEXT);
            }
            entityField.setNumDifferentValues(fieldValuesSet.size());
            this.addEntityField(entityField);
        }
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
