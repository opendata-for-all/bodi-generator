package bodi.generator.dataSchema;

import bodi.generator.BodiGenerator;
import com.xatkit.bot.metamodel.IntentParameterType;
import com.xatkit.bot.metamodel.Mapping;
import com.xatkit.bot.metamodel.MappingEntry;
import com.xatkit.dsl.entity.EntityDefinitionReferenceProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bodi.generator.dataSchema.DataType.DATE;
import static bodi.generator.dataSchema.DataType.NUMBER;
import static bodi.generator.dataSchema.DataType.TEXT;

/**
 * Generates and stores bot information and components, which are used to generate the source code of a bot.
 */
public class BotProperties {

    /**
     * Contains higher-level information about a tabular data source. It is used to generate bot components.
     */
    private final DataSchema ds;

    /**
     * The name of the bot this {@link BotProperties} refers to.
     */
    private final String botName;

    /**
     * The name of the document that the bot must read at runtime to satisfy the user queries.
     * <p>
     * This file should be the same as the one used to define {@link #ds} (consider any usage of
     * {@link BodiGenerator#createTabularDataSource(String)} before setting this attribute)
     */
    private final String inputDocName;

    /**
     * Collection of metamodels of the bot entities.
     * <p>
     * It is implemented as a {@link Map} since some other meta-concepts of a chatbot may need references to the types,
     * thus with a map it is possible to get a type knowing its name (the key)
     *
     * @see IntentParameterType
     * @see EntityDefinitionReferenceProvider
     */
    private final Map<String, IntentParameterType> types = new HashMap<>();

    /**
     * Instantiates a new {@link BotProperties}.
     *
     * @param botName      the bot name
     * @param inputDocName the name of the input document
     * @param ds           the data schema
     */
    public BotProperties(String botName, String inputDocName, DataSchema ds) {
        this.botName = botName;
        this.inputDocName = inputDocName;
        this.ds = ds;
    }

    /**
     * Create the types of a chatbot from a given {@link SchemaType}.
     *
     * @param mainSchemaType the schema type
     *
     * @see #types
     */
    public void createTypes(SchemaType mainSchemaType) {
        /*
         Create NumericField, DateField and TextualField entities
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
            } else if (schemaField.getType().equals(TEXT)) {
                textualFieldEntity.addMappingEntry(entry);
            }

        }
        types.put("numericFieldEntity", numericFieldEntity);
        types.put("dateFieldEntity", dateFieldEntity);
        types.put("textualFieldEntity", textualFieldEntity);

        /*
         Create NumericOperator, DateOperator and TextualOperator entities
         */
        Mapping numericOperatorEntity = new Mapping("NumericOperator", "numericOperatorEntity");
        numericOperatorEntity.addMappingEntry(new MappingEntry("="));
        numericOperatorEntity.addMappingEntry(new MappingEntry("<", Arrays.asList("less than", "lower than")));
        numericOperatorEntity.addMappingEntry(new MappingEntry("<=",
                Arrays.asList("less than or equals", "lower than or equals")));
        numericOperatorEntity.addMappingEntry(new MappingEntry(">", Arrays.asList("greater than", "higher than")));
        numericOperatorEntity.addMappingEntry(new MappingEntry(">=",
                Arrays.asList("greater than or equals", "higher than or equals")));
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

    /**
     * Generates and gets the entities file corresponding to the {@link BotProperties}.
     *
     * @return the entities file
     */
    public String getEntitiesFile() {
        return CodeGenerator.generateEntitiesFile(this.getTypes());
    }

    /**
     * Creates the elements that will be part of the bot.
     */
    public void createBotStructure() {
        SchemaType mainSchemaType = ds.getSchemaType("mainSchemaType");
        createTypes(mainSchemaType);
    }

    /**
     * Gets the bot name.
     *
     * @return the bot name
     */
    public String getBotName() {
        return botName;
    }

    /**
     * Gets input document name.
     *
     * @return the input document name
     */
    public String getInputDocName() {
        return inputDocName;
    }

    /**
     * Gets types of the bot.
     *
     * @return the types
     */
    public List<IntentParameterType> getTypes() {
        return types.values().stream().toList();
    }

}
