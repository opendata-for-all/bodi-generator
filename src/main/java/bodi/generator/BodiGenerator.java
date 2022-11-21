package bodi.generator;

import bodi.generator.dataSchema.BodiGeneratorProperties;
import bodi.generator.dataSchema.CodeGenerator;
import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSchema.DataType;
import bodi.generator.dataSchema.SchemaField;
import bodi.generator.dataSchema.SchemaType;
import bodi.generator.dataSource.Row;
import bodi.generator.dataSource.TabularDataSource;
import bodi.generator.ui.model.Properties;
import bodi.generator.ui.service.DownloadZipService;
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
                tds.replaceNumericColumnComma(fieldName);
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
                for (String language : DataSchema.languages) {
                    JSONObject fieldJson = fieldsJson.getJSONObject(fieldName).getJSONObject(language);
                    schemaField.setReadableName(language, fieldJson.getString("readable_name"));
                    Set<String> synonyms = fieldJson.getJSONArray("synonyms").toList().stream()
                            .map(object -> Objects.toString(object, null)).collect(Collectors.toSet());
                    schemaField.addSynonyms(language, synonyms);
                    schemaField.addSynonyms(language, Collections.singleton(schemaField.getReadableName(language)));
                }
            } else {
                for (String language : DataSchema.languages) {
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
                tds.replaceNumericColumnComma(fieldName);
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
            for (String language : DataSchema.languages) {
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

            System.out.println("Creating resource rowNames.json");
            File rowNamesSource = new File("src/main/resources/rowNames.json");
            File rowNamesDest = new File(outputFolder + "/src/main/resources/rowNames.json");
            FileUtils.copyFile(rowNamesSource, rowNamesDest);

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

            System.out.println("Creating resource config.properties");
            createBotPropertiesFile(conf);
            String[] languages = conf.getString(BotProperties.BOT_LANGUAGES).split(",");
            for (String language : languages) {
                createBotLanguagePropertiesFile(language, conf);
            }

            List<String> botFiles = new ArrayList<>();
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/Bot.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/getResult/CheckCorrectAnswer.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/getResult/GetResult.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFieldOfNumericFieldFunction.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFieldOfValue.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFilter.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFrequentValueInField.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomNumericFieldFunction.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomQuery.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomRowCount.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomRowOfNumericFieldFunction.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomRowOfValues.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomShowFieldDistinct.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomValue1vsValue2.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomValueFrequency.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/ResetBot.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/SelectViewField.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/StructuredFilter.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/StructuredQuery.java");

            System.out.println("Creating transitionGraph.dot test file");
            Path dotFile = Paths.get(outputFolder + "/src/test/resources/transitionGraph.dot");
            Files.write(dotFile, generateGraph(botFiles).getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the default {@code config.properties} configuration file of the generated bot.
     *
     * @param conf the bodi-generator configuration properties
     */
    private static void createBotPropertiesFile(Configuration conf) {
        String outputFolder = conf.getString(BodiGeneratorProperties.OUTPUT_DIRECTORY);
        try {
            FileWriter fw = new FileWriter(outputFolder + "/src/main/resources/config.properties");

            fw.write("# Bot\n\n");
            fw.write(BotProperties.DATA_NAME + " = " + conf.getString(BodiGeneratorProperties.DATA_NAME) + "\n");
            fw.write(BotProperties.CSV_DELIMITER + " = " + conf.getString(BodiGeneratorProperties.CSV_DELIMITER) + "\n");
            fw.write(BotProperties.BOT_PAGE_LIMIT + " = " + "10" + "\n");
            fw.write(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY + " = " + "7" + "\n");
            fw.write(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER + " = " + "true" + "\n");
            fw.write(BotProperties.BOT_LANGUAGES + " = " + conf.getString(BotProperties.BOT_LANGUAGES) + "\n");

            fw.write("\n# NLP Server properties\n\n");
            fw.write(BotProperties.SERVER_URL + " = " + "127.0.0.1:5050" + "\n");
            fw.write(BotProperties.TEXT_TO_TABLE_ENDPOINT + " = " + "text-to-table" + "\n");

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the default {@code config_{language}.properties} configuration file of the generated bot.
     *
     * @param language the language that the properties belong to
     * @param conf     the bodi-generator configuration properties
     */
    private static void createBotLanguagePropertiesFile(String language, Configuration conf) {
        String outputFolder = conf.getString(BodiGeneratorProperties.OUTPUT_DIRECTORY);
        try {
            FileWriter fw = new FileWriter(outputFolder + "/src/main/resources/config_" + language + ".properties");

            fw.write("# Bot\n\n");
            fw.write(BotProperties.XATKIT_SERVER_PORT + " = " + "5000" + "\n");
            fw.write(BotProperties.XATKIT_REACT_PORT + " = " + "5001" + "\n");
            fw.write(BotProperties.BOT_LANGUAGE + " = " + language + "\n");

            fw.write("\n# Intent provider\n\n");
            if (conf.getString(BotProperties.XATKIT_INTENT_PROVIDER)
                    .equals("com.xatkit.core.recognition.dialogflow.DialogFlowIntentRecognitionProvider")) {
                fw.write(BotProperties.XATKIT_INTENT_PROVIDER + " = " + conf.getString(BotProperties.XATKIT_INTENT_PROVIDER) + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID + " = " + "your-project-id" + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH + " = " + "path-to-your-credentials-file" + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE + " = " + language + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP + " = " + "true" + "\n");
            } else if (conf.getString(BotProperties.XATKIT_INTENT_PROVIDER)
                    .equals("com.xatkit.core.recognition.nlpjs.NlpjsIntentRecognitionProvider")) {
                fw.write(BotProperties.XATKIT_INTENT_PROVIDER + " = " + conf.getString(BotProperties.XATKIT_INTENT_PROVIDER) + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_AGENTID + " = " + "default" + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_LANGUAGE + " = " + language + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_SERVER + " = " + "http://localhost:8080" + "\n");

            }

            if (conf.getString(BotProperties.XATKIT_LOGS_DATABASE)
                    .equals("com.xatkit.core.recognition.RecognitionMonitorPostgreSQL")) {
                fw.write("\n# PostgreSQL\n\n");
                fw.write(BotProperties.XATKIT_LOGS_DATABASE + " = " + conf.getString(BotProperties.XATKIT_LOGS_DATABASE) + "\n");
                fw.write(BotProperties.XATKIT_DATABASE_MODEL + " = " + "postgresql" + "\n");
                fw.write(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING + " = " + "true" + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_URL + " = " + "your-url" + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_USER + " = " + "your-user" + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_PASSWORD + " = " + "your-password" + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_BOT_ID + " = " + "your-bot-id" + "\n");
            }

            fw.write("\n# Open data resource information\n\n");
            fw.write(BotProperties.BOT_ODATA_TITLE + " = " + "title" + "\n");
            fw.write(BotProperties.BOT_ODATA_URL + " = " + "url" + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            System.out.println("Creating " + inputDocName);
            tds.writeCsv(outputFolder + "/src/main/resources/" + inputDocName);

            System.out.println("Creating resource fields.json");
            Path entitiesFile = Paths.get(outputFolder + "/src/main/resources/fields.json");
            Files.write(entitiesFile, ds.getSchemaType("mainSchemaType").generateFieldsJson().toString().getBytes());

            System.out.println("Creating resource fieldOperators.json");
            File fieldOperatorsSource = new File("src/main/resources/fieldOperators.json");
            File fieldOperatorsDest = new File(outputFolder + "/src/main/resources/fieldOperators.json");
            FileUtils.copyFile(fieldOperatorsSource, fieldOperatorsDest);

            String defaultRowNamesFile = "defaultRowNames.json";
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(defaultRowNamesFile);
            JSONObject defaultRowNamesJson = null;
            if (is == null) {
                System.out.println("Cannot find the default row names file \"" + defaultRowNamesFile + "\"");
            } else {
                defaultRowNamesJson = new JSONObject(new JSONTokener(is));
            }
            System.out.println("Creating resource rowNames.json");
            Path rowNamesFile = Paths.get(outputFolder + "/src/main/resources/rowNames.json");
            Files.write(rowNamesFile, ds.generateRowNamesJson(defaultRowNamesJson).toString().getBytes());

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

            System.out.println("Creating resource config.properties");
            createBotPropertiesFile(properties);

            Set<String> languages = (Set<String>) properties.getBotProperties().get(BotProperties.BOT_LANGUAGES);
            for (String language : languages) {
                createBotLanguagePropertiesFile(language, properties);
            }

            List<String> botFiles = new ArrayList<>();
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/Bot.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/getResult/CheckCorrectAnswer.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/getResult/GetResult.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFieldOfNumericFieldFunction.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFieldOfValue.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFilter.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomFrequentValueInField.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomNumericFieldFunction.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomQuery.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomRowCount.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomRowOfNumericFieldFunction.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomRowOfValues.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomShowFieldDistinct.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomValue1vsValue2.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/customQuery/CustomValueFrequency.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/ResetBot.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/SelectViewField.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/StructuredFilter.java");
            botFiles.add(outputFolder + "/src/main/java/com/xatkit/bot/structuredQuery/StructuredQuery.java");

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
     * Creates the {@code config.properties} configuration file of the generated bot.
     *
     * @param properties the bodi-generator and bot properties
     */
    private static void createBotPropertiesFile(Properties properties) {
        String outputFolder = (String) properties.getBodiGeneratorProperties().get(BodiGeneratorProperties.OUTPUT_DIRECTORY);
        try {
            FileWriter fw = new FileWriter(outputFolder + "/src/main/resources/config.properties");

            fw.write("# Bot\n\n");
            fw.write(BotProperties.DATA_NAME + " = " + properties.getBotProperties().get(BodiGeneratorProperties.DATA_NAME) + "\n");
            fw.write(BotProperties.CSV_DELIMITER + " = " + properties.getBotProperties().get(BodiGeneratorProperties.CSV_DELIMITER) + "\n");
            fw.write(BotProperties.BOT_PAGE_LIMIT + " = " + properties.getBotProperties().get(BotProperties.BOT_PAGE_LIMIT) + "\n");
            fw.write(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY + " = " + properties.getBotProperties().get(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY) + "\n");
            fw.write(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER + " = " + properties.getBotProperties().get(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER) + "\n");
            String languagesString = String.join(",", (Set<String>) properties.getBotProperties().get(BotProperties.BOT_LANGUAGES));
            fw.write(BotProperties.BOT_LANGUAGES + " = " + languagesString + "\n");

            fw.write("\n# NLP Server properties\n\n");
            fw.write(BotProperties.SERVER_URL + " = " + properties.getBotProperties().get(BotProperties.SERVER_URL) + "\n");
            fw.write(BotProperties.TEXT_TO_TABLE_ENDPOINT + " = " + properties.getBotProperties().get(BotProperties.TEXT_TO_TABLE_ENDPOINT) + "\n");

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the default {@code config_{language}.properties} configuration file of the generated bot.
     *
     * @param language   the language that the properties belong to
     * @param properties the bodi-generator and bot properties
     */
    private static void createBotLanguagePropertiesFile(String language, Properties properties) {
        String outputFolder = (String) properties.getBodiGeneratorProperties().get(BodiGeneratorProperties.OUTPUT_DIRECTORY);
        Map<String, Object> botPropertiesLang = properties.getBotPropertiesLang().get(language);
        try {
            FileWriter fw = new FileWriter(outputFolder + "/src/main/resources/config_" + language + ".properties");

            fw.write("# Bot\n\n");
            fw.write(BotProperties.XATKIT_SERVER_PORT + " = " + botPropertiesLang.get(BotProperties.XATKIT_SERVER_PORT) + "\n");
            fw.write(BotProperties.XATKIT_REACT_PORT + " = " + botPropertiesLang.get(BotProperties.XATKIT_REACT_PORT) + "\n");
            fw.write(BotProperties.BOT_LANGUAGE + " = " + botPropertiesLang.get(BotProperties.BOT_LANGUAGE) + "\n");

            fw.write("\n# Intent provider\n\n");
            if (botPropertiesLang.get(BotProperties.XATKIT_INTENT_PROVIDER).equals("com.xatkit.core.recognition.dialogflow.DialogFlowIntentRecognitionProvider")) {
                fw.write(BotProperties.XATKIT_INTENT_PROVIDER + " = " + botPropertiesLang.get(BotProperties.XATKIT_INTENT_PROVIDER) + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID + " = " + botPropertiesLang.get(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID) + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH + " = " + botPropertiesLang.get(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH) + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE + " = " + botPropertiesLang.get(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE) + "\n");
                fw.write(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP + " = " + botPropertiesLang.get(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP) + "\n");
            } else if (botPropertiesLang.get(BotProperties.XATKIT_INTENT_PROVIDER).equals("com.xatkit.core.recognition.nlpjs.NlpjsIntentRecognitionProvider")) {
                fw.write(BotProperties.XATKIT_INTENT_PROVIDER + " = " + botPropertiesLang.get(BotProperties.XATKIT_INTENT_PROVIDER) + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_AGENTID + " = " + botPropertiesLang.get(BotProperties.XATKIT_NLPJS_AGENTID) + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_LANGUAGE + " = " + botPropertiesLang.get(BotProperties.XATKIT_NLPJS_LANGUAGE) + "\n");
                fw.write(BotProperties.XATKIT_NLPJS_SERVER + " = " + botPropertiesLang.get(BotProperties.XATKIT_NLPJS_SERVER) + "\n");
            }

            if (botPropertiesLang.get(BotProperties.XATKIT_LOGS_DATABASE).equals("com.xatkit.core.recognition.RecognitionMonitorPostgreSQL")) {
                fw.write("\n# PostgreSQL\n\n");
                fw.write(BotProperties.XATKIT_LOGS_DATABASE + " = " + botPropertiesLang.get(BotProperties.XATKIT_LOGS_DATABASE) + "\n");
                fw.write(BotProperties.XATKIT_DATABASE_MODEL + " = " + botPropertiesLang.get(BotProperties.XATKIT_DATABASE_MODEL) + "\n");
                fw.write(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING + " = " + botPropertiesLang.get(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING) + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_URL + " = " + botPropertiesLang.get(BotProperties.XATKIT_POSTGRESQL_URL) + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_USER + " = " + botPropertiesLang.get(BotProperties.XATKIT_POSTGRESQL_USER) + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_PASSWORD + " = " + botPropertiesLang.get(BotProperties.XATKIT_POSTGRESQL_PASSWORD) + "\n");
                fw.write(BotProperties.XATKIT_POSTGRESQL_BOT_ID + " = " + botPropertiesLang.get(BotProperties.XATKIT_POSTGRESQL_BOT_ID) + "\n");
            }

            fw.write("\n# Open data resource information\n\n");
            fw.write(BotProperties.BOT_ODATA_TITLE + " = " + botPropertiesLang.get(BotProperties.BOT_ODATA_TITLE) + "\n");
            fw.write(BotProperties.BOT_ODATA_URL + " = " + botPropertiesLang.get(BotProperties.BOT_ODATA_URL) + "\n");

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
