package bodiGenerator.dataSchema;

import com.xatkit.bot.metamodel.CoreIntentParameterType;
import com.xatkit.bot.metamodel.Intent;
import com.xatkit.bot.metamodel.IntentParameter;
import com.xatkit.bot.metamodel.IntentParameterType;
import com.xatkit.bot.metamodel.Mapping;
import com.xatkit.bot.metamodel.MappingEntry;
import com.xatkit.bot.metamodel.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotProperties {

    private DataSchema ds;
    private String botName;
    private String inputDocName;
    private Map<String, IntentParameterType> types = new HashMap<>();
    private Map<String, Intent> intents = new HashMap<>();
    private List<State> states = new ArrayList<>();
    private String entitiesFile;
    private String botInfoPropertiesFile;

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
        Mapping textualFieldEntity = new Mapping("TextualField", "textualFieldEntity");
        for (EntityField entityField : mainEntityType.getEntityFields()) {
            MappingEntry entry = new MappingEntry(entityField.getOriginalName()); // Here you can add synonyms
            if (entityField.getType().equals("numeric")) {
                numericFieldEntity.addMappingEntry(entry);
            } else {
                textualFieldEntity.addMappingEntry(entry);
            }

        }
        types.put("numericFieldEntity", numericFieldEntity);
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
    }

    // Actually not necessary
    public void createIntents() {
        Intent restartIntent = new Intent("Restart", "restartIntent");
        restartIntent.addTrainingSentence("restart");
        intents.put("restartIntent", restartIntent);

        Intent showDataIntent = new Intent("ShowData", "showDataIntent");
        showDataIntent.addTrainingSentence("show data");
        intents.put("showDataIntent", showDataIntent);

        Intent showNextPageIntent = new Intent("ShowNextPage", "showNextPageIntent");
        showNextPageIntent.addTrainingSentence("next page");
        intents.put("showNextPageIntent", showNextPageIntent);

        Intent stopViewIntent = new Intent("StopView", "stopViewIntent");
        stopViewIntent.addTrainingSentence("stop view");
        intents.put("stopViewIntent", stopViewIntent);

        Intent addFilterIntent = new Intent("AddFilter", "addFilterIntent");
        addFilterIntent.addTrainingSentence("add filter");
        intents.put("addFilterIntent", addFilterIntent);

        Intent addFieldToViewIntent = new Intent("AddFieldToView", "addFieldToViewIntent");
        addFieldToViewIntent.addTrainingSentence("add field to view");
        intents.put("addFieldToViewIntent", addFieldToViewIntent);

        Intent customQueryIntent = new Intent("CustomQuery", "customQueryIntent");
        customQueryIntent.addTrainingSentence("custom query");
        intents.put("customQueryIntent", customQueryIntent);

        Intent customFilterIntent = new Intent("CustomFilter", "customFilterIntent");
        // TODO: Check that NUMERICFIELD && NUMERICOPERATOR OR TEXTUALFIELD && TEXTUALOPERATOR (and also check values)
        customFilterIntent.addTrainingSentence("I want to filter NUMERICFIELD NUMERICOPERATOR VALUE");
        customFilterIntent.addTrainingSentence("I want to filter TEXTUALFIELD TEXTUALOPERATOR VALUE");
        customFilterIntent.addTrainingSentence("filter NUMERICFIELD NUMERICOPERATOR VALUE");
        customFilterIntent.addTrainingSentence("filter TEXTUALFIELD TEXTUALOPERATOR VALUE");
        customFilterIntent.addTrainingSentence("NUMERICFIELD NUMERICOPERATOR VALUE");
        customFilterIntent.addTrainingSentence("TEXTUALFIELD TEXTUALOPERATOR VALUE");
        customFilterIntent.addParameter(new IntentParameter("numericFieldName", "NUMERICFIELD", types.get("numericFieldEntity")));
        customFilterIntent.addParameter(new IntentParameter("numericOperatorName", "NUMERICOPERATOR", types.get("numericOperatorEntity")));
        customFilterIntent.addParameter(new IntentParameter("textualFieldName", "TEXTUALFIELD", types.get("textualFieldEntity")));
        customFilterIntent.addParameter(new IntentParameter("textualOperatorName", "TEXTUALOPERATOR", types.get("textualOperatorEntity")));
        customFilterIntent.addParameter(new IntentParameter("operatorValue", "VALUE", new CoreIntentParameterType("any")));
        intents.put("customFilterIntent", customFilterIntent);

        Intent fieldNameIntent = new Intent("FieldName", "fieldNameIntent");
        fieldNameIntent.addTrainingSentence("NUMERICFIELD");
        fieldNameIntent.addTrainingSentence("TEXTUALFIELD");
        fieldNameIntent.addParameter(new IntentParameter("numericFieldName", "NUMERICFIELD", types.get("numericFieldEntity")));
        fieldNameIntent.addParameter(new IntentParameter("textualFieldName", "TEXTUALFIELD", types.get("textualFieldEntity")));
        intents.put("fieldNameIntent", fieldNameIntent);

        Intent operatorNameIntent = new Intent("OperatorName", "operatorNameIntent");
        operatorNameIntent.addTrainingSentence("NUMERICOPERATOR");
        operatorNameIntent.addTrainingSentence("TEXTUALOPERATOR");
        operatorNameIntent.addParameter(new IntentParameter("numericOperatorName", "NUMERICOPERATOR", types.get("numericOperatorEntity")));
        operatorNameIntent.addParameter(new IntentParameter("textualOperatorName", "TEXTUALOPERATOR", types.get("textualOperatorEntity")));
        intents.put("operatorNameIntent", operatorNameIntent);

        Intent operatorValueIntent = new Intent("OperatorValue", "operatorValueIntent");
        operatorValueIntent.addTrainingSentence("VALUE");
        operatorValueIntent.addParameter(new IntentParameter("operatorValue", "VALUE", new CoreIntentParameterType("any")));
        intents.put("operatorValueIntent", operatorValueIntent); // TODO: SEPARATE NUMERIC AND TEXTUAL ("any") VALUES
    }

    public void createEntitiesFile() {
        entitiesFile = CodeGenerator.generateEntitiesFile(this.getTypes());
    }

    public void createBotInfoPropertiesFile() {
        botInfoPropertiesFile = CodeGenerator.generateBotInfoPropertiesFile(inputDocName);
    }

    public void createBotStructure() {
        EntityType mainEntityType = ds.getEntityType("mainEntityType");
        createTypes(mainEntityType);
        createEntitiesFile();
        createBotInfoPropertiesFile();
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

    public String getBotInfoPropertiesFile() {
        return botInfoPropertiesFile;
    }
}
