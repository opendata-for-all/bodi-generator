package bodi.generator.library;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static bodi.generator.library.BotGeneratorUtils.copyDirectory;
import static bodi.generator.library.BotGeneratorUtils.copyFile;
import static bodi.generator.library.BotGeneratorUtils.deleteFolder;
import static bodi.generator.library.BotGeneratorUtils.writeFile;

/**
 * The necessary methods to generate a chatbot application (CLI version).
 */
public final class BotGeneratorCLI {

    private BotGeneratorCLI() {
    }

    /**
     * Creates the bot files.
     *
     * Required files:
     *
     *  - the csv file
     *  - fields.json to be copied
     *  - rowNames.json to be copeid
     *  - config.properties, config_en.properties...
     *
     * @param conf the bodi-generator configuration properties
     */
    public static void createBot(Configuration conf) {
        String botName = conf.getString(BodiGeneratorProperties.BOT_NAME);
        String dataName = conf.getString(BodiGeneratorProperties.DATA_NAME);
        String inputDocName = dataName + ".csv";
        String outputFolder = conf.getString(BodiGeneratorProperties.OUTPUT_DIRECTORY);
        boolean enableTesting = conf.getBoolean(BodiGeneratorProperties.ENABLE_TESTING);
        System.out.println("Attempting to create the bot " + botName + " in " + outputFolder);
        try {
            File outputFolderFile = new File(outputFolder);
            deleteFolder(outputFolderFile);
        } catch (NullPointerException e) {
            System.out.println("Error deleting the existing content of the " + outputFolder + " folder. Maybe it does"
                    + " not exist?");
        }
        System.out.println("Creating the project structure");
        try {
            Files.createDirectories(Paths.get(outputFolder));
            Files.createDirectories(Paths.get(outputFolder + "/src/main/java/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/main/resources/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/test/java/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/test/resources/"));

            // .csv
            copyFile("src/main/resources/" + inputDocName, outputFolder + "/src/main/resources/" + inputDocName);

            // pom.xml
            writeFile(outputFolder + "/pom.xml", CodeGenerator.generatePomFile(botName, enableTesting).getBytes());

            // com.xatkit.bot.*
            copyDirectory("src/main/java/com/xatkit/bot/", outputFolder + "/src/main/java/com/xatkit/bot/");
            if (enableTesting) {
                System.out.println("Copying the com.xatkit.bot test package");
                copyDirectory("src/test/java/com/xatkit/bot/", outputFolder + "/src/test/java/com/xatkit/bot/");
            }

            // intents.properties
            copyFile("src/main/resources/intents.properties", outputFolder + "/src/main/resources/intents.properties");
            copyFile("src/main/resources/intents_ca.properties", outputFolder + "/src/main/resources/intents_ca.properties");
            copyFile("src/main/resources/intents_es.properties", outputFolder + "/src/main/resources/intents_es.properties");

            // messages.properties
            copyFile("src/main/resources/messages.properties", outputFolder + "/src/main/resources/messages.properties");
            copyFile("src/main/resources/messages_ca.properties", outputFolder + "/src/main/resources/messages_ca.properties");
            copyFile("src/main/resources/messages_es.properties", outputFolder + "/src/main/resources/messages_es.properties");

            // config.properties
            copyFile("src/main/resources/config.properties", outputFolder + "/src/main/resources/config.properties");
            copyFile("src/main/resources/config_ca.properties", outputFolder + "/src/main/resources/config_ca.properties");
            copyFile("src/main/resources/config_es.properties", outputFolder + "/src/main/resources/config_es.properties");
            copyFile("src/main/resources/config_en.properties", outputFolder + "/src/main/resources/config_en.properties");

            // fieldOperators.json
            copyFile("src/main/resources/fieldOperators.json", outputFolder + "/src/main/resources/fieldOperators.json");

            // fields.json
            copyFile("src/main/resources/fields.json", outputFolder + "/src/main/resources/fields.json");

            // rowNames.json
            copyFile("src/main/resources/rowNames.json", outputFolder + "/src/main/resources/rowNames.json");

            if (enableTesting) {
                copyFile("src/test/resources/customQueryUtterances_en.csv", outputFolder + "/src/test/resources/customQueryUtterances_en.csv");
                copyFile("src/test/resources/customQueryUtterances_es.csv", outputFolder + "/src/test/resources/customQueryUtterances_es.csv");
                copyFile("src/test/resources/customQueryUtterances_ca.csv", outputFolder + "/src/test/resources/customQueryUtterances_ca.csv");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
