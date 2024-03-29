package bodi.generator.library;

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

    // SchemaType name. Currently, each DataSchema has only 1 SchemaType, but more could be added
    public static final String MAIN_SCHEMA_TYPE = "mainSchemaType";
}
