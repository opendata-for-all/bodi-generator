package bodi.generator.library;

import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSource.TabularDataSource;
import bodi.generator.ui.model.Properties;
import bodi.generator.ui.service.DownloadZipService;
import com.xatkit.bot.library.BotProperties;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static bodi.generator.library.BodiGeneratorProperties.MAIN_SCHEMA_TYPE;
import static bodi.generator.library.BotGeneratorUtils.copyDirectory;
import static bodi.generator.library.BotGeneratorUtils.copyFile;
import static bodi.generator.library.BotGeneratorUtils.deleteFolder;
import static bodi.generator.library.BotGeneratorUtils.writeFile;
import static bodi.generator.library.CodeGenerator.createBotLanguagePropertiesFile;
import static bodi.generator.library.CodeGenerator.createBotPropertiesFile;

/**
 * The necessary methods to generate a chatbot application (UI version).
 */
public final class BotGeneratorUI {

    private BotGeneratorUI() {
    }

    /**
     * Creates the bot files and pack them as a zip file.
     *
     * @param properties the bodi-generator and bot properties
     * @param ds         the Data Schema
     * @param tds        the Tabular Data Source
     * @param response   the response where to send the zip file
     */
    public static void createBot(Properties properties, DataSchema ds, TabularDataSource tds,
                                   HttpServletResponse response) {
        String botName = (String) properties.getBodiGeneratorProperties().get(BodiGeneratorProperties.BOT_NAME);
        String dataName = (String) properties.getBodiGeneratorProperties().get(BodiGeneratorProperties.DATA_NAME);
        String inputDocName = dataName + ".csv";
        String outputFolder = (String) properties.getBodiGeneratorProperties().get(BodiGeneratorProperties.OUTPUT_DIRECTORY);
        boolean enableTesting = (boolean) properties.getBodiGeneratorProperties().get(BodiGeneratorProperties.ENABLE_TESTING);
        System.out.println("Attempting to create the bot " + botName + " in " + outputFolder);
        try {
            File outputFolderFile = new File(outputFolder);
            deleteFolder(outputFolderFile);
        } catch (NullPointerException ignored) {
        }
        System.out.println("Creating the project structure");
        try {
            Files.createDirectories(Paths.get(outputFolder));
            Files.createDirectories(Paths.get(outputFolder + "/src/main/java/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/main/resources/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/test/java/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/test/resources/"));

            // .csv
            tds.writeCsv(outputFolder + "/src/main/resources/" + inputDocName);

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

            // configuration.properties
            createBotPropertiesFile(properties);
            Set<String> languages = (Set<String>) properties.getBotProperties().get(BotProperties.BOT_LANGUAGES);
            for (String language : languages) {
                createBotLanguagePropertiesFile(language, properties);
            }

            // fieldOperators.json
            copyFile("src/main/resources/fieldOperators.json", outputFolder + "/src/main/resources/fieldOperators.json");

            // entities.json
            writeFile(outputFolder + "/src/main/resources/entities.json", ds.getSchemaType(MAIN_SCHEMA_TYPE).generateEntitiesJson().toString().getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        DownloadZipService.generateZipFile(response, outputFolder, botName);

        try {
            File outputFolderFile = new File(outputFolder);
            deleteFolder(outputFolderFile);
        } catch (NullPointerException e) {
            System.out.println("Error deleting the existing content of the " + outputFolder + " folder. Maybe it does"
                    + " not exist?");
        }
    }
}
