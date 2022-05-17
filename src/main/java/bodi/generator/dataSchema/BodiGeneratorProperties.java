package bodi.generator.dataSchema;

/**
 * This class stores constant property names used to configure the Bodi Generator.
 */
public final class BodiGeneratorProperties {

    private BodiGeneratorProperties() {
    }

    public static final String BOT_NAME = "xls.generator.bot.name";
    public static final String OUTPUT_DIRECTORY = "xls.generator.output";
    public static final String DATA_NAME = "xls.importer.xls"; // REPETIDA
    public static final String CSV_DELIMITER = "csv.delimiter"; // REPETIDA
    public static final String ENABLE_TESTING = "enable_testing";
    public static final String MAX_NUM_DIFFERENT_VALUES = "maxNumDifferentValues";
    public static final String XATKIT_INTENT_PROVIDER = "xatkit.intent.provider"; // REPETIDA
    public static final String XATKIT_LOGS_DATABASE = "xatkit.logs.database"; // REPETIDA
}