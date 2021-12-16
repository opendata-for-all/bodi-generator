package bodiGenerator.dataSchema;

import com.xatkit.bot.metamodel.Intent;
import com.xatkit.bot.metamodel.IntentParameterType;
import com.xatkit.bot.metamodel.Mapping;
import com.xatkit.bot.metamodel.MappingEntry;
import com.xatkit.bot.metamodel.State;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bodiGenerator.dataSchema.DataType.DATE;
import static bodiGenerator.dataSchema.DataType.NUMBER;
import static bodiGenerator.dataSchema.DataType.TEXT;

public class BotProperties {

    private DataSchema ds;
    private String botName;
    private String inputDocName;
    private Map<String, IntentParameterType> types = new HashMap<>();

    public BotProperties(String botName, String inputDocName, DataSchema ds) {
        this.botName = botName;
        this.inputDocName = inputDocName;
        this.ds = ds;
    }

    public void createTypes(SchemaType mainSchemaType) {
        /*
         Create NumericField and TextualField entities
         */
        Mapping numericFieldEntity = new Mapping("NumericField", "numericFieldEntity");
        Mapping dateFieldEntity = new Mapping("DateField", "dateFieldEntity");
        Mapping textualFieldEntity = new Mapping("TextualField", "textualFieldEntity");
        for (SchemaField schemaField : mainSchemaType.getSchemaFields()) {
            MappingEntry entry = new MappingEntry(schemaField.getOriginalName()); // Here you can add synonyms
            if (schemaField.getType().equals(NUMBER)) {
                numericFieldEntity.addMappingEntry(entry);
            } else if (schemaField.getType().equals(DATE)) {
                dateFieldEntity.addMappingEntry(entry);
            } else if (schemaField.getType().equals(TEXT)){
                textualFieldEntity.addMappingEntry(entry);
            }

        }
        types.put("numericFieldEntity", numericFieldEntity);
        types.put("dateFieldEntity", dateFieldEntity);
        types.put("textualFieldEntity", textualFieldEntity);

        /*
         Create NumericOperator and TextualOperator entities
         */
        Mapping numericOperatorEntity = new Mapping("NumericOperator", "numericOperatorEntity");
        numericOperatorEntity.addMappingEntry(new MappingEntry("="));
        numericOperatorEntity.addMappingEntry(new MappingEntry("<", Arrays.asList("less than", "lower than")));
        numericOperatorEntity.addMappingEntry(new MappingEntry("<=", Arrays.asList("less than or equals", "lower than or equals")));
        numericOperatorEntity.addMappingEntry(new MappingEntry(">", Arrays.asList("greater than", "higher than")));
        numericOperatorEntity.addMappingEntry(new MappingEntry(">=", Arrays.asList("greater than or equals", "higher than or equals")));
        numericOperatorEntity.addMappingEntry(new MappingEntry("!=", Arrays.asList("not equals", "different")));
        types.put("numericOperatorEntity", numericOperatorEntity);

        Mapping textualOperatorEntity = new Mapping("TextualOperator", "textualOperatorEntity");
        textualOperatorEntity.addMappingEntry(new MappingEntry("equals"));
        textualOperatorEntity.addMappingEntry(new MappingEntry("different"));
        textualOperatorEntity.addMappingEntry(new MappingEntry("contains"));
        textualOperatorEntity.addMappingEntry(new MappingEntry("starts with"));
        textualOperatorEntity.addMappingEntry(new MappingEntry("ends with"));
        types.put("textualOperatorEntity", textualOperatorEntity);

        Mapping dateOperatorEntity = new Mapping("DateOperator", "dateOperatorEntity");
        dateOperatorEntity.addMappingEntry(new MappingEntry("equals"));
        dateOperatorEntity.addMappingEntry(new MappingEntry("different"));
        dateOperatorEntity.addMappingEntry(new MappingEntry("before"));
        dateOperatorEntity.addMappingEntry(new MappingEntry("after"));
        types.put("dateOperatorEntity", dateOperatorEntity);
    }

    public String getEntitiesFile() {
        return CodeGenerator.generateEntitiesFile(this.getTypes());
    }

    public void createBotStructure() {
        SchemaType mainSchemaType = ds.getSchemaType("mainSchemaType");
        createTypes(mainSchemaType);
    }

    public String getBotName() {
        return botName;
    }

    public String getInputDocName() {
        return inputDocName;
    }

    public List<IntentParameterType> getTypes() {
        return types.values().stream().toList();
    }

}
