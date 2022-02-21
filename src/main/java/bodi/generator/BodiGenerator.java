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
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import static bodi.generator.dataSchema.DataType.EMPTY;
import static bodi.generator.dataSchema.DataType.NUMBER;
import static bodi.generator.dataSchema.DataType.TEXT;
import static com.xatkit.bot.library.Utils.isDate;
import static com.xatkit.bot.library.Utils.isNumeric;

import static org.apache.commons.lang3.StringUtils.isEmpty;

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
            System.exit(1);
        }
        return botConfiguration;
    }

    /**
     * Creates a Tabular Data Source from a given tabular data file.
     *
     * @param inputDocName the file name
     * @return the Tabular Data Source
     */
    private static TabularDataSource createTabularDataSource(String inputDocName, char delimiter) {
        TabularDataSource tds = null;
        try {
            tds = new TabularDataSource(Objects.requireNonNull(BodiGenerator.class.getClassLoader()
                    .getResource(inputDocName)).getPath(), delimiter);
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
    private static DataSchema tabularDataSourceToDataSchema(TabularDataSource tds, String fieldsFile) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fieldsFile);
        if (is == null) {
            throw new NullPointerException("Cannot find the fields file \"" + fieldsFile + "\"");
        }
        JSONObject fieldsJson = new JSONObject(new JSONTokener(is));
        DataSchema ds = new DataSchema();
        SchemaType schemaType = new SchemaType("mainSchemaType");
        for (String fieldName : tds.getHeaderCopy()) {
            Set<String> fieldValuesSet = new HashSet<>();
            Map<DataType, Boolean> dataTypes = new HashMap<>();
            dataTypes.put(NUMBER, true);
            dataTypes.put(DATE, true);
            dataTypes.put(TEXT, true);
            dataTypes.put(EMPTY, true);
            int columnIndex = tds.getHeaderCopy().indexOf(fieldName);
            for (Row row : tds.getTableCopy()) {
                String value = row.getColumnValue(columnIndex);
                fieldValuesSet.add(value);
                if (dataTypes.get(NUMBER) && !isNumeric(value) && !isEmpty(value)) {
                    dataTypes.put(NUMBER, false);
                }
                if (dataTypes.get(DATE) && !isDate(value) && !isEmpty(value)) {
                    dataTypes.put(DATE, false);
                }
                if (dataTypes.get(EMPTY) && !isEmpty(value)) {
                    dataTypes.put(EMPTY, false);
                }
            }
            SchemaField schemaField = new SchemaField();
            if (dataTypes.get(EMPTY)) {
                // TODO: Consider empty (unknown) type?
                schemaField.setType(TEXT);
            } else if (dataTypes.get(DATE)) {
                schemaField.setType(DATE);
            } else if (dataTypes.get(NUMBER)) {
                schemaField.setType(NUMBER);
            } else {
                schemaField.setType(TEXT);
            }
            schemaField.setOriginalName(fieldName);
            schemaField.setNumDifferentValues(fieldValuesSet.size());
            for (String language : SchemaField.languages) {
                JSONObject fieldJson = fieldsJson.getJSONObject(fieldName).getJSONObject(language);
                schemaField.setReadableName(language, fieldJson.getString("readable_name"));
                Set<String> synonyms = fieldJson.getJSONArray("synonyms").toList().stream()
                        .map(object -> Objects.toString(object, null)).collect(Collectors.toSet());
                schemaField.addSynonyms(language, synonyms);
                schemaField.addSynonyms(language, Collections.singleton(schemaField.getReadableName(language)));
            }
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
        char delimiter = conf.getString("csv.delimiter").charAt(0);

        TabularDataSource tds = createTabularDataSource(inputDocName, delimiter);
        DataSchema ds = tabularDataSourceToDataSchema(tds, "fields.json");
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

            // TODO: Simplify: copy all resources folder??? (is there anything that must not be copied?)

            System.out.println("Creating resource fields.json");
            Path entitiesFile = Paths.get(outputFolder + "/src/main/resources/fields.json");
            Files.write(entitiesFile, ds.getSchemaType("mainSchemaType").generateFieldsJson().toString().getBytes());

            System.out.println("Creating resource fieldOperators.json");
            File fieldOperatorsSource = new File("src/main/resources/fieldOperators.json");
            File fieldOperatorsDest = new File(outputFolder + "/src/main/resources/fieldOperators.json");
            FileUtils.copyFile(fieldOperatorsSource, fieldOperatorsDest);

            System.out.println("Creating resource intents.properties");
            File intentsSource = new File("src/main/resources/intents.properties");
            File intentsDest = new File(outputFolder + "/src/main/resources/intents.properties");
            FileUtils.copyFile(intentsSource, intentsDest);

            System.out.println("Creating resource intents_ca.properties");
            File intentsCaSource = new File("src/main/resources/intents_ca.properties");
            File intentsCaDest = new File(outputFolder + "/src/main/resources/intents_ca.properties");
            FileUtils.copyFile(intentsCaSource, intentsCaDest);

            System.out.println("Creating resource intents_es.properties");
            File intentsEsSource = new File("src/main/resources/intents_es.properties");
            File intentsEsDest = new File(outputFolder + "/src/main/resources/intents_es.properties");
            FileUtils.copyFile(intentsEsSource, intentsEsDest);

            System.out.println("Creating resource messages.properties");
            File messagesSource = new File("src/main/resources/messages.properties");
            File messagesDest = new File(outputFolder + "/src/main/resources/messages.properties");
            FileUtils.copyFile(messagesSource, messagesDest);

            System.out.println("Creating resource messages_ca.properties");
            File messagesCaSource = new File("src/main/resources/messages_ca.properties");
            File messagesCaDest = new File(outputFolder + "/src/main/resources/messages_ca.properties");
            FileUtils.copyFile(messagesCaSource, messagesCaDest);

            System.out.println("Creating resource messages_es.properties");
            File messagesEsSource = new File("src/main/resources/messages_es.properties");
            File messagesEsDest = new File(outputFolder + "/src/main/resources/messages_es.properties");
            FileUtils.copyFile(messagesEsSource, messagesEsDest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create new csv with deleted columns (if any)
        System.out.println("Creating the new " + inputDocName + " with deleted columns (if any)");
        try (PrintWriter out = new PrintWriter(outputFolder + "/src/main/resources/" + inputDocName)) {
            out.println(String.join(String.valueOf(delimiter), tds.getHeaderCopy()));
            for (int i = 0; i < tds.getNumRows(); ++i) {
                out.println(String.join(String.valueOf(delimiter), tds.getRow(i).getValues()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
