package bodiGenerator.dataSchema;

import com.xatkit.bot.metamodel.Intent;
import com.xatkit.bot.metamodel.IntentParameterType;
import com.xatkit.bot.metamodel.Mapping;
import com.xatkit.bot.metamodel.MappingEntry;
import com.xatkit.bot.metamodel.State;

import java.util.ArrayList;
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
    private Map<String, Intent> intents = new HashMap<>();
    private List<State> states = new ArrayList<>();
    private String entitiesFile;

    public BotProperties(String botName, String inputDocName, DataSchema ds) {
        this.botName = botName;
        this.inputDocName = inputDocName;
        this.ds = ds;
    }

    public void createTypes(EntityType mainEntityType) {
        /*
         Create NumericField and TextualField entities
         */
        Mapping numericFieldEntity = new Mapping("NumericField", "numericFieldEntity");
        Mapping dateFieldEntity = new Mapping("DateField", "dateFieldEntity");
        Mapping textualFieldEntity = new Mapping("TextualField", "textualFieldEntity");
        for (EntityField entityField : mainEntityType.getEntityFields()) {
            MappingEntry entry = new MappingEntry(entityField.getOriginalName()); // Here you can add synonyms
            if (entityField.getType().equals(NUMBER)) {
                numericFieldEntity.addMappingEntry(entry);
            } else if (entityField.getType().equals(DATE)) {
                dateFieldEntity.addMappingEntry(entry);
            } else if (entityField.getType().equals(TEXT)){
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

    public void createEntitiesFile() {
        entitiesFile = CodeGenerator.generateEntitiesFile(this.getTypes());
    }

    public void createBotStructure() {
        EntityType mainEntityType = ds.getEntityType("mainEntityType");
        createTypes(mainEntityType);
        createEntitiesFile();
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

    public List<Intent> getIntents() {
        return intents.values().stream().toList();
    }

    public List<State> getStates() {
        return states;
    }

    public String getEntitiesFile() {
        return entitiesFile;
    }
}
