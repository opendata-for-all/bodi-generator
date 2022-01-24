package bodi.generator;

import bodi.generator.dataSchema.BotProperties;
import bodi.generator.dataSchema.CodeGenerator;
import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSchema.DataType;
import bodi.generator.dataSchema.SchemaField;
import bodi.generator.dataSchema.SchemaType;
import bodi.generator.dataSource.Row;
import bodi.generator.dataSource.TabularDataSource;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static bodi.generator.dataSchema.DataType.DATE;
import static bodi.generator.dataSchema.DataType.NUMBER;
import static bodi.generator.dataSchema.DataType.TEXT;
import static com.xatkit.bot.library.Utils.isDate;
import static com.xatkit.bot.library.Utils.isNumeric;

/**
 * The entry point of the bodi-generator application. It generates a complete chatbot application.
 */
public final class BodiGenerator {

    private BodiGenerator() {
    }

    /**
     * Deletes a folder.
     * <p>
     * This is used to delete the generated bot folder (if any)
     *
     * @param file the file
     */
    private static void deleteFolder(File file) {
        File[] listFiles = file.listFiles();
        int length = Objects.requireNonNull(listFiles).length;
        for (int i = 0; i < length; ++i) {
            File subFile = listFiles[i];
            boolean isDirectory = subFile.isDirectory();
            if (isDirectory) {
                deleteFolder(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }

    /**
     * Loads the bot configuration properties from a given properties file.
     *
     * @param fileName the properties file name
     * @return the configuration
     */
    private static Configuration loadBotConfProperties(String fileName) {
        Configurations configurations = new Configurations();
        Configuration botConfiguration = new BaseConfiguration();
        try {
            botConfiguration = configurations.properties(BodiGenerator.class.getClassLoader().getResource(fileName));
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.out.println("'" + fileName + "' file not found");
        }
        return botConfiguration;
    }

    /**
     * Creates a Tabular Data Source from a given tabular data file.
     *
     * @param inputDocName the file name
     * @return the Tabular Data Source
     */
    private static TabularDataSource createTabularDataSource(String inputDocName) {
        TabularDataSource tds = null;
        try {
            tds = new TabularDataSource(Objects.requireNonNull(BodiGenerator.class.getClassLoader()
                    .getResource(inputDocName)).getPath());
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("'" + inputDocName + "' file not found");
            System.exit(1);
        }
        return tds;
    }

    /**
     * Creates a Data Schema from a given Tabular Data Source.
     *
     * @param tds the Tabular Data Source
     * @return the Data Schema
     */
    private static DataSchema tabularDataSourceToDataSchema(TabularDataSource tds) {
        DataSchema ds = new DataSchema();
        SchemaType schemaType = new SchemaType("mainSchemaType");
        for (String fieldName : tds.getHeaderCopy()) {
            Set<String> fieldValuesSet = new HashSet<>();
            Map<DataType, Boolean> dataTypes = new HashMap<>();
            dataTypes.put(NUMBER, true);
            dataTypes.put(DATE, true);
            dataTypes.put(TEXT, true);
            int columnIndex = tds.getHeaderCopy().indexOf(fieldName);
            for (Row row : tds.getTableCopy()) {
                String value = row.getColumnValue(columnIndex);
                fieldValuesSet.add(value);
                if (dataTypes.get(NUMBER) && !isNumeric(value)) {
                    dataTypes.put(NUMBER, false);
                }
                if (dataTypes.get(DATE) && !isDate(value)) {
                    dataTypes.put(DATE, false);
                }
            }
            SchemaField schemaField = new SchemaField();
            schemaField.setOriginalName(fieldName);
            schemaField.setReadableName(fieldName);
            if (dataTypes.get(DATE)) {
                schemaField.setType(DATE);
            } else if (dataTypes.get(NUMBER)) {
                schemaField.setType(NUMBER);
            } else {
                schemaField.setType(TEXT);
            }
            schemaField.setNumDifferentValues(fieldValuesSet.size());
            schemaType.addSchemaField(schemaField);
        }
        ds.addSchemaType(schemaType);
        return ds;
    }

    /**
     * Creates a Bot Properties from a given Data Schema.
     *
     * @param ds           the Data Schema
     * @param botName      the bot name
     * @param inputDocName the input doc name
     * @return the Bot Properties
     */
    private static BotProperties dataSchemaToBotProperties(DataSchema ds, String botName, String inputDocName) {
        BotProperties bp = new BotProperties(botName, inputDocName, ds);
        bp.createBotStructure();
        return bp;
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        // Load bot properties
        Configuration conf = loadBotConfProperties("bot.properties");
        String inputDocName = conf.getString("xls.importer.xls");
        String botName = conf.getString("xls.generator.bot.name");
        String outputFolder = conf.getString("xls.generator.output");

        TabularDataSource tds = createTabularDataSource(inputDocName);
        DataSchema ds = tabularDataSourceToDataSchema(tds);
        BotProperties bp = dataSchemaToBotProperties(ds, botName, inputDocName);

        System.out.println("Attempting to create the bot " + botName + " in " + outputFolder);
        try {
            File outputFolderFile = new File(outputFolder);
            deleteFolder(outputFolderFile);
        } catch (NullPointerException e) {
            System.out.println("Error deleting the existing content of the " + outputFolder
                    + " folder. Maybe it does not exist?");
        }
        System.out.println("Creating the project structure");
        try {
            Files.createDirectories(Paths.get(outputFolder));
            Files.createDirectories(Paths.get(outputFolder + "/src/main/java/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/main/resources/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/test/java/"));
            Files.createDirectories(Paths.get(outputFolder + "/src/test/resources/"));

            System.out.println("Creating the pom file");
            Path pomFile = Files.createFile(Paths.get(outputFolder + "/pom.xml"));
            Files.write(pomFile, CodeGenerator.generatePomFile(botName).getBytes());

            System.out.println("Creating the bot configuration file");
            File botConfSource = new File("src/main/resources/bot.properties");
            File botConfDest = new File(outputFolder + "/src/main/resources/bot.properties");
            FileUtils.copyFile(botConfSource, botConfDest);

            System.out.println("Copying the bodi.generator.dataSource package");
            File dsSource = new File("src/main/java/bodi/generator/dataSource/");
            File dsDest = new File(outputFolder + "/src/main/java/bodi/generator/dataSource/");
            FileUtils.copyDirectory(dsSource, dsDest);

            System.out.println("Copying the com.xatkit.bot package");
            File botSource = new File("src/main/java/com/xatkit/bot/");
            File botDest = new File(outputFolder + "/src/main/java/com/xatkit/bot/");
            FileUtils.copyDirectory(botSource, botDest);

            System.out.println("Overwriting Entities.java");
            Path entitiesFile = Paths.get(outputFolder + "/src/main/java/com/xatkit/bot/library/Entities.java");
            Files.write(entitiesFile, bp.getEntitiesFile().getBytes());

            System.out.println("Creating resource intents.properties");
            File intentsSource = new File("src/main/resources/intents.properties");
            File intentsDest = new File(outputFolder + "/src/main/resources/intents.properties");
            FileUtils.copyFile(intentsSource, intentsDest);

            System.out.println("Creating resource intents_cat.properties");
            File intentsCatSource = new File("src/main/resources/intents_cat.properties");
            File intentsCatDest = new File(outputFolder + "/src/main/resources/intents_cat.properties");
            FileUtils.copyFile(intentsCatSource, intentsCatDest);

            System.out.println("Creating resource messages.properties");
            File messagesSource = new File("src/main/resources/messages.properties");
            File messagesDest = new File(outputFolder + "/src/main/resources/messages.properties");
            FileUtils.copyFile(messagesSource, messagesDest);

            System.out.println("Creating resource messages_cat.properties");
            File messagesCatSource = new File("src/main/resources/messages_cat.properties");
            File messagesCatDest = new File(outputFolder + "/src/main/resources/messages_cat.properties");
            FileUtils.copyFile(messagesCatSource, messagesCatDest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create new csv with deleted columns (if any)
        System.out.println("Creating the new " + inputDocName + " with deleted columns (if any)");
        try (PrintWriter out = new PrintWriter(outputFolder + "/src/main/resources/" + inputDocName)) {
            out.println(tds.getHeaderCopy().stream().map(field -> "\"" + field + "\"")
                    .collect(Collectors.joining(",")));
            for (int i = 0; i < tds.getNumRows(); ++i) {
                out.println(tds.getRow(i).getValues().stream().map(field -> "\"" + field + "\"")
                        .collect(Collectors.joining(",")));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
