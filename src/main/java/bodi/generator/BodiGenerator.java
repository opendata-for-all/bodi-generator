package bodi.generator;

import bodi.generator.dataSchema.BodiGeneratorProperties;
import bodi.generator.dataSchema.CodeGenerator;
import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSchema.DataType;
import bodi.generator.dataSchema.SchemaField;
import bodi.generator.dataSchema.SchemaType;
import bodi.generator.dataSource.Row;
import bodi.generator.dataSource.TabularDataSource;
import bodi.generator.ui.model.DownloadZipService;
import bodi.generator.ui.model.Properties;
import com.xatkit.bot.library.BotProperties;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static bodi.generator.dataSchema.BotToGraph.generateGraph;
import static bodi.generator.dataSchema.DataType.DATE;
import static bodi.generator.dataSchema.DataType.EMPTY;
import static bodi.generator.dataSchema.DataType.NUMBER;
import static bodi.generator.dataSchema.DataType.TEXT;
import static com.xatkit.bot.library.Utils.isDate;
import static com.xatkit.bot.library.Utils.isNumeric;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * It generates a complete chatbot application.
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
     * Loads the bodi-generator configuration properties from a given properties file.
     *
     * @param fileName the properties file name
     * @return the configuration
     */
    static Configuration loadBodiConfigurationProperties(String fileName) {
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
    static TabularDataSource createTabularDataSource(String inputDocName, char delimiter) {
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
     * @param tds                   the Tabular Data Source
     * @param fieldsFile            the json file containing fields information, stored in the resources folder
     * @param maxNumDifferentValues if the number of different values of a field is <= than this number, then the
     *                              field will contain information about its values in the Data Schema. Otherwise, not.
     * @return the Data Schema
     */
    public static DataSchema tabularDataSourceToDataSchema(TabularDataSource tds, String fieldsFile,
                                                    int maxNumDifferentValues) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fieldsFile);
        JSONObject fieldsJson = null;
        if (is == null) {
            System.out.println("Cannot find the fields file \"" + fieldsFile + "\"");
        } else {
            fieldsJson = new JSONObject(new JSONTokener(is));
        }
        DataSchema ds = new DataSchema();
        SchemaType schemaType = new SchemaType("mainSchemaType");
        // TODO: Test dataType inference
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
            if (schemaField.getType().equals(TEXT) && schemaField.getNumDifferentValues() <= maxNumDifferentValues) {
                schemaField.setCategorical(true);
                schemaField.addMainValues(fieldValuesSet);
            } else {
                schemaField.setCategorical(false);
            }
            if (fieldsJson != null) {
                for (String language : SchemaField.languages) {
                    JSONObject fieldJson = fieldsJson.getJSONObject(fieldName).getJSONObject(language);
                    schemaField.setReadableName(language, fieldJson.getString("readable_name"));
                    Set<String> synonyms = fieldJson.getJSONArray("synonyms").toList().stream()
                            .map(object -> Objects.toString(object, null)).collect(Collectors.toSet());
                    schemaField.addSynonyms(language, synonyms);
                    schemaField.addSynonyms(language, Collections.singleton(schemaField.getReadableName(language)));
                }
            } else {
                for (String language : SchemaField.languages) {
                    schemaField.setReadableName(language, fieldName);
                }
            }
            schemaType.addSchemaField(schemaField);
        }
        ds.addSchemaType(schemaType);
        return ds;
    }

    /**
     * Creates a Data Schema from a given Tabular Data Source.
     *
     * @return the Data Schema
     */
    public static DataSchema tabularDataSourceToDataSchema(TabularDataSource tds) {
        int maxNumDifferentValues = 10;
        DataSchema ds = new DataSchema();
        SchemaType schemaType = new SchemaType("mainSchemaType");
        // TODO: Test dataType inference
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
            if (schemaField.getType().equals(TEXT) && schemaField.getNumDifferentValues() <= maxNumDifferentValues) {
                schemaField.setCategorical(true);
                schemaField.addMainValues(fieldValuesSet);
            } else {
                schemaField.setCategorical(false);
            }
            for (String language : SchemaField.languages) {
                schemaField.setReadableName(language, fieldName);
            }
            schemaType.addSchemaField(schemaField);
        }
        ds.addSchemaType(schemaType);
        return ds;
    }

    /**
     * Creates the bot files.
     *
     * @param conf the bodi-generator configuration properties
     * @param ds   the Data Schema
     */
    public static void createBot(Configuration conf, DataSchema ds) {
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

            System.out.println("Creating the pom file");
            Path pomFile = Files.createFile(Paths.get(outputFolder + "/pom.xml"));
            Files.write(pomFile, CodeGenerator.generatePomFile(botName, enableTesting).getBytes());

            System.out.println("Copying the bodi.generator.dataSource package");
            File dsSource = new File("src/main/java/bodi/generator/dataSource/");
            File dsDest = new File(outputFolder + "/src/main/java/bodi/generator/dataSource/");
            FileUtils.copyDirectory(dsSource, dsDest);

            System.out.println("Copying the com.xatkit.bot package");
            File botSource = new File("src/main/java/com/xatkit/bot/");
            File botDest = new File(outputFolder + "/src/main/java/com/xatkit/bot/");
            FileUtils.copyDirectory(botSource, botDest);

            System.out.println("Creating csv");
            File csvSource = new File("src/main/resources/" + dataName + "/" + inputDocName);
            File csvDest = new File(outputFolder + "/src/main/resources/" + inputDocName);
            FileUtils.copyFile(csvSource, csvDest);

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

            if (enableTesting) {
                System.out.println("Copying the com.xatkit.bot test package");
                File testBotSource = new File("src/test/java/com/xatkit/bot/");
                File testBotDest = new File(outputFolder + "/src/test/java/com/xatkit/bot/");
                FileUtils.copyDirectory(testBotSource, testBotDest);
            }

            System.out.println("Creating resource bot.properties");
            createBotPropertiesFile(conf);

            List<String> botFiles = new ArrayList<>();
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/Bot.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/getResult/GetResult.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomQuery.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFilter.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomShowFieldDistinct.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFrequentValueInField.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomValueFrequency.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomValue1vsValue2.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/StructuredQuery.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/SelectViewField.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/StructuredFilter.java");

            System.out.println("Creating transitionGraph.dot test file");
            Path dotFile = Paths.get(outputFolder + "/src/test/resources/transitionGraph.dot");
            Files.write(dotFile, generateGraph(botFiles).getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the default {@code bot.properties} configuration file of the generated bot.
     *
     * @param conf the bodi-generator configuration properties
     */
    private static void createBotPropertiesFile(Configuration conf) {
        String outputFolder = conf.getString("xls.generator.output");
        try {
            FileWriter fw = new FileWriter(outputFolder + "/src/main/resources/bot.properties");

            fw.write("# Bot\n\n");
            fw.write(BotProperties.DATA_NAME + " = " + conf.getString(BodiGeneratorProperties.DATA_NAME) + "\n");
            fw.write(BotProperties.CSV_DELIMITER + " = " + conf.getString(BodiGeneratorProperties.CSV_DELIMITER) + "\n");
            fw.write(BotProperties.XATKIT_SERVER_PORT + " = " + "5000" + "\n");
            fw.write(BotProperties.XATKIT_REACT_PORT + " = " + "5001" + "\n");
            fw.write(BotProperties.BOT_LANGUAGE + " = " + "en" + "\n");
            fw.write(BotProperties.BOT_PAGE_LIMIT + " = " + "10" + "\n");
            fw.write(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY + " = " + "5" + "\n");
            fw.write(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER + " = " + "true" + "\n");

            fw.write("\n# Intent provider\n\n");
            if (conf.getString(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER)
                    .equals("com.xatkit.core.recognition.dialogflow.DialogFlowIntentRecognitionProvider")) {
                fw.write(BotProperties.XATKIT_INTENT_PROVIDER + " = " + conf.getString(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER) + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID + " = " + "your-project-id" + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH + " = " + "path-to-your-credentials-file" + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE + " = " + "en" + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP + " = " + "true" + "\n");
            } else if (conf.getString(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER)
                    .equals("com.xatkit.core.recognition.nlpjs.NlpjsIntentRecognitionProvider")) {
                fw.write(BotProperties.XATKIT_INTENT_PROVIDER + " = " + conf.getString(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER) + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_AGENTID + " = " + "default" + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_LANGUAGE + " = " + "en" + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_SERVER + " = " + "http://localhost:8080" + "\n");

            }

            if (conf.getString(BodiGeneratorProperties.XATKIT_LOGS_DATABASE)
                    .equals("com.xatkit.core.recognition.RecognitionMonitorPostgreSQL")) {
                fw.write("\n# PostgreSQL\n\n");
                fw.write(BotProperties.XATKIT_LOGS_DATABASE + " = " + conf.getString(BodiGeneratorProperties.XATKIT_LOGS_DATABASE) + "\n");
                fw.write(BotProperties.XATKIT_DATABASE_MODEL + " = " + "postgresql" + "\n");
                fw.write(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING + " = " + "true" + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_URL + " = " + "your-url" + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_USER + " = " + "your-user" + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_PASSWORD + " = " + "your-password" + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_BOT_ID + " = " + "your-bot-id" + "\n");
            }

            fw.write("\n# NLP Server properties\n\n");
            fw.write(BotProperties.SERVER_URL + " = " + "127.0.0.1:5001" + "\n");
            fw.write(BotProperties.TEXT_TO_TABLE_ENDPOINT + " = " + "text-to-table" + "\n");

            fw.write("\n# Open data resource information\n\n");
            fw.write(BotProperties.BOT_ODATA_TITLE_EN + " = " + "title-in-english" + "\n");
            fw.write(BotProperties.BOT_ODATA_TITLE_CA + " = " + "title-in-catalan" + "\n");
            fw.write(BotProperties.BOT_ODATA_TITLE_ES + " = " + "title-in-spanish" + "\n");
            fw.write(BotProperties.BOT_ODATA_URL_EN + " = " + "url-english-source" + "\n");
            fw.write(BotProperties.BOT_ODATA_URL_CA + " = " + "url-catalan-source" + "\n");
            fw.write(BotProperties.BOT_ODATA_URL_ES + " = " + "url-spanish-source" + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the bot files.
     *
     * @param properties the bodi-generator and bot properties
     * @param ds         the Data Schema
     * @param csv        the csv
     */
    public static void createBot(Properties properties, DataSchema ds, InputStream csv,
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

            System.out.println("Creating the pom file");
            Path pomFile = Files.createFile(Paths.get(outputFolder + "/pom.xml"));
            Files.write(pomFile, CodeGenerator.generatePomFile(botName, enableTesting).getBytes());

            System.out.println("Copying the bodi.generator.dataSource package");
            File dsSource = new File("src/main/java/bodi/generator/dataSource/");
            File dsDest = new File(outputFolder + "/src/main/java/bodi/generator/dataSource/");
            FileUtils.copyDirectory(dsSource, dsDest);

            System.out.println("Copying the com.xatkit.bot package");
            File botSource = new File("src/main/java/com/xatkit/bot/");
            File botDest = new File(outputFolder + "/src/main/java/com/xatkit/bot/");
            FileUtils.copyDirectory(botSource, botDest);

            System.out.println("Creating csv");
            csv.transferTo(Files.newOutputStream(Path.of(outputFolder + "/src/main/resources/" + inputDocName)));

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

            if (enableTesting) {
                System.out.println("Copying the com.xatkit.bot test package");
                File testBotSource = new File("src/test/java/com/xatkit/bot/");
                File testBotDest = new File(outputFolder + "/src/test/java/com/xatkit/bot/");
                FileUtils.copyDirectory(testBotSource, testBotDest);
            }

            System.out.println("Creating resource bot.properties");
            createBotPropertiesFile(properties);

            List<String> botFiles = new ArrayList<>();
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/Bot.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/getResult/GetResult.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomQuery.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFilter.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomShowFieldDistinct.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFrequentValueInField.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomValueFrequency.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomValue1vsValue2.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/StructuredQuery.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/SelectViewField.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/StructuredFilter.java");

            System.out.println("Creating transitionGraph.dot test file");
            Path dotFile = Paths.get(outputFolder + "/src/test/resources/transitionGraph.dot");
            Files.write(dotFile, generateGraph(botFiles).getBytes());

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

    /**
     * Creates the {@code bot.properties} configuration file of the generated bot.
     *
     * @param properties the bodi-generator and bot properties
     */
    private static void createBotPropertiesFile(Properties properties) {
        String outputFolder = (String) properties.getBodiGeneratorProperties().get(BodiGeneratorProperties.OUTPUT_DIRECTORY);
        try {
            FileWriter fw = new FileWriter(outputFolder + "/src/main/resources/bot.properties");

            fw.write("# Bot\n\n");
            fw.write(BotProperties.DATA_NAME + " = " + properties.getBotProperties().get(BotProperties.DATA_NAME) + "\n");
            fw.write(BotProperties.CSV_DELIMITER + " = " + properties.getBotProperties().get(BotProperties.CSV_DELIMITER) + "\n");
            fw.write(BotProperties.XATKIT_SERVER_PORT + " = " + properties.getBotProperties().get(BotProperties.XATKIT_SERVER_PORT) + "\n");
            fw.write(BotProperties.XATKIT_REACT_PORT + " = " + properties.getBotProperties().get(BotProperties.XATKIT_REACT_PORT) + "\n");
            fw.write(BotProperties.BOT_LANGUAGE + " = " + properties.getBotProperties().get(BotProperties.BOT_LANGUAGE) + "\n");
            fw.write(BotProperties.BOT_PAGE_LIMIT + " = " + properties.getBotProperties().get(BotProperties.BOT_PAGE_LIMIT) + "\n");
            fw.write(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY + " = " + properties.getBotProperties().get(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY) + "\n");
            fw.write(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER + " = " + properties.getBotProperties().get(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER) + "\n");

            fw.write("\n# Intent provider\n\n");
            if (properties.getBotProperties().get(BotProperties.XATKIT_INTENT_PROVIDER).equals("com.xatkit.core.recognition.dialogflow.DialogFlowIntentRecognitionProvider")) {
                fw.write(BotProperties.XATKIT_INTENT_PROVIDER + " = " + properties.getBotProperties().get(BotProperties.XATKIT_INTENT_PROVIDER) + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID + " = " + properties.getBotProperties().get(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID) + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH + " = " + properties.getBotProperties().get(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH) + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE + " = " + properties.getBotProperties().get(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE) + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP + " = " + properties.getBotProperties().get(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP) + "\n");
            } else if (properties.getBotProperties().get(BotProperties.XATKIT_INTENT_PROVIDER).equals("com.xatkit.core.recognition.nlpjs.NlpjsIntentRecognitionProvider")) {
                fw.write(BotProperties.XATKIT_INTENT_PROVIDER + " = " + properties.getBotProperties().get(BotProperties.XATKIT_INTENT_PROVIDER) + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_AGENTID + " = " + properties.getBotProperties().get(BotProperties.XATKIT_NLPJS_AGENTID) + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_LANGUAGE + " = " + properties.getBotProperties().get(BotProperties.XATKIT_NLPJS_LANGUAGE) + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_SERVER + " = " + properties.getBotProperties().get(BotProperties.XATKIT_NLPJS_SERVER) + "\n");
            }

            if (properties.getBotProperties().get(BotProperties.XATKIT_LOGS_DATABASE).equals("com.xatkit.core.recognition.RecognitionMonitorPostgreSQL")) {
                fw.write("\n# PostgreSQL\n\n");
                fw.write(BotProperties.XATKIT_LOGS_DATABASE + " = " + properties.getBotProperties().get(BotProperties.XATKIT_LOGS_DATABASE) + "\n");
                fw.write(BotProperties.XATKIT_DATABASE_MODEL + " = " + properties.getBotProperties().get(BotProperties.XATKIT_DATABASE_MODEL) + "\n");
                fw.write(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING + " = " + properties.getBotProperties().get(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING) + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_URL + " = " + properties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_URL) + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_USER + " = " + properties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_USER) + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_PASSWORD + " = " + properties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_PASSWORD) + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_BOT_ID + " = " + properties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_BOT_ID) + "\n");
            }

            fw.write("\n# NLP Server properties\n\n");
            fw.write(BotProperties.SERVER_URL + " = " + properties.getBotProperties().get(BotProperties.SERVER_URL) + "\n");
            fw.write(BotProperties.TEXT_TO_TABLE_ENDPOINT + " = " + properties.getBotProperties().get(BotProperties.TEXT_TO_TABLE_ENDPOINT) + "\n");

            fw.write("\n# Open data resource information\n\n");
            fw.write(BotProperties.BOT_ODATA_TITLE_EN + " = " + properties.getBotProperties().get(BotProperties.BOT_ODATA_TITLE_EN) + "\n");
            fw.write(BotProperties.BOT_ODATA_TITLE_CA + " = " + properties.getBotProperties().get(BotProperties.BOT_ODATA_TITLE_CA) + "\n");
            fw.write(BotProperties.BOT_ODATA_TITLE_ES + " = " + properties.getBotProperties().get(BotProperties.BOT_ODATA_TITLE_ES) + "\n");
            fw.write(BotProperties.BOT_ODATA_URL_EN + " = " + properties.getBotProperties().get(BotProperties.BOT_ODATA_URL_EN) + "\n");
            fw.write(BotProperties.BOT_ODATA_URL_CA + " = " + properties.getBotProperties().get(BotProperties.BOT_ODATA_URL_CA) + "\n");
            fw.write(BotProperties.BOT_ODATA_URL_ES + " = " + properties.getBotProperties().get(BotProperties.BOT_ODATA_URL_ES) + "\n");

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
