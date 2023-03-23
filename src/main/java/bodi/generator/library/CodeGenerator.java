package bodi.generator.library;

import bodi.generator.ui.model.Properties;
import com.xatkit.bot.library.BotProperties;
import org.apache.commons.configuration2.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * This class isolates methods that generate code for a chatbot.
 * <p>
 * This code must be generated because it depends on the input data of the chatbot (e.g. a csv file), which means
 * that it is not generic for all chatbots.
 */
public final class CodeGenerator {

    private CodeGenerator() {
    }

    /**
     * The number of whitespaces an indent has.
     */
    private static final int INDENT_SIZE = 4;

    /**
     * Generates a {@code pom} file for a chatbot.
     *
     * @param botName the bot name
     * @param enableTesting true if testing is enabled (it will add extra dependencies to the pom file), false otherwise
     * @return the string containing the content of the pom.xml file
     */
    public static String generatePomFile(String botName, boolean enableTesting) {
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.xatkit</groupId>
                    <artifactId>""" + botName + """
                </artifactId>
                    <version>1.0.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <properties>
                        <maven.compiler.source>1.8</maven.compiler.source>
                        <maven.compiler.target>1.8</maven.compiler.target>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <maven-assembly-plugin.version>3.1.0</maven-assembly-plugin.version>
                        <maven-help-plugin.version>3.2.0</maven-help-plugin.version>
                        <junit-jupiter.version>LATEST</junit-jupiter.version>
                        <lombok.version>LATEST</lombok.version>
                        <opencsv.version>5.5.2</opencsv.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>com.opencsv</groupId>
                            <artifactId>opencsv</artifactId>
                        </dependency>
                        <!-- Xatkit Internal -->
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>core</artifactId>
                            <version>5.0.0-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>chat-platform</artifactId>
                            <version>3.0.1-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>react-platform</artifactId>
                            <version>4.0.1-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>core-library-i18n</artifactId>
                            <version>0.0.1-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>xatkit-dialogflow</artifactId>
                            <version>0.0.1-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>xatkit-nlpjs</artifactId>
                            <version>0.0.1-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>xatkit-nlu-client</artifactId>
                            <version>0.0.1-SNAPSHOT</version>
                        </dependency>
                        <dependency>
                            <groupId>com.xatkit</groupId>
                            <artifactId>xatkit-logs-postgres</artifactId>
                            <version>0.0.1-SNAPSHOT</version>
                        </dependency>
                """;
                if (enableTesting) {
                    pom += """
                            <dependency>
                                <groupId>com.xatkit</groupId>
                                <artifactId>labs-bot-testing-tools</artifactId>
                                <version>0.0.1-SNAPSHOT</version>
                            </dependency>
                    """;
                }
               pom += """
                               <!-- Utils -->
                               <dependency>
                                   <groupId>org.apache.drill.exec</groupId>
                                   <artifactId>drill-jdbc</artifactId>
                                   <version>1.20.0</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.apache.drill.exec</groupId>
                                   <artifactId>drill-java-exec</artifactId>
                                   <version>1.20.0</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.codehaus.janino</groupId>
                                   <artifactId>janino</artifactId>
                                   <version>3.0.11</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.codehaus.janino</groupId>
                                   <artifactId>commons-compiler</artifactId>
                                   <version>3.0.11</version>
                               </dependency>
                               <dependency>
                                   <groupId>org.projectlombok</groupId>
                                   <artifactId>lombok</artifactId>
                               </dependency>
                               <dependency>
                                   <groupId>org.json</groupId>
                                   <artifactId>json</artifactId>
                                   <version>20211205</version>
                               </dependency>
                               <dependency>
                                    <groupId>com.google.guava</groupId>
                                    <artifactId>guava</artifactId>
                                    <version>31.1-jre</version>
                                </dependency>
                               <!-- Tests -->
                               <dependency>
                                   <groupId>org.junit.jupiter</groupId>
                                   <artifactId>junit-jupiter</artifactId>
                               </dependency>
                           </dependencies>
                           <dependencyManagement>
                               <dependencies>
                                   <dependency>
                                       <groupId>com.opencsv</groupId>
                                       <artifactId>opencsv</artifactId>
                                       <version>${opencsv.version}</version>
                                   </dependency>
                                   <!-- Utils -->
                                   <dependency>
                                       <groupId>org.projectlombok</groupId>
                                       <artifactId>lombok</artifactId>
                                       <version>${lombok.version}</version>
                                       <scope>provided</scope>
                                   </dependency>
                                   <!-- Tests -->
                                   <dependency>
                                       <groupId>org.junit.jupiter</groupId>
                                       <artifactId>junit-jupiter</artifactId>
                                       <version>${junit-jupiter.version}</version>
                                       <scope>test</scope>
                                   </dependency>
                               </dependencies>
                           </dependencyManagement>
                           <build>
                               <pluginManagement>
                                   <plugins>
                                       <plugin>
                                           <groupId>org.apache.maven.plugins</groupId>
                                           <artifactId>maven-assembly-plugin</artifactId>
                                           <version>${maven-assembly-plugin.version}</version>
                                           <executions>
                                               <execution>
                                                   <phase>package</phase>
                                                   <goals>
                                                       <goal>single</goal>
                                                   </goals>
                                               </execution>
                                           </executions>
                                           <configuration>
                                               <descriptorRefs>
                                                   <descriptorRef>jar-with-dependencies</descriptorRef>
                                               </descriptorRefs>
                                               <archive>
                                                   <manifest>
                                                       <mainClass>com.xatkit.bot.Bot</mainClass>
                                                   </manifest>
                                               </archive>
                                               <finalName>bot</finalName>
                                           </configuration>
                                       </plugin>
                                   </plugins>
                               </pluginManagement>
                               <plugins>
                                   <plugin>
                                       <groupId>org.apache.maven.plugins</groupId>
                                       <artifactId>maven-assembly-plugin</artifactId>
                                   </plugin>
                                   <plugin>
                                       <artifactId>maven-surefire-plugin</artifactId>
                                       <version>2.22.1</version>
                                   </plugin>
                               </plugins>
                           </build>
                       </project>
                       """;
        return pom;
    }


    /**
     * Creates the {@code config.properties} configuration file of the generated bot.
     *
     * @param properties the bodi-generator and bot properties
     */
    protected static void createBotPropertiesFile(Properties properties) {
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
    protected static void createBotLanguagePropertiesFile(String language, Properties properties) {
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
            } else if (botPropertiesLang.get(BotProperties.XATKIT_INTENT_PROVIDER).equals("com.xatkit.core.recognition.nluserver.NLUServerIntentRecognitionProvider")) {
                fw.write(BotProperties.XATKIT_INTENT_PROVIDER + " = " + botPropertiesLang.get(BotProperties.XATKIT_INTENT_PROVIDER) + "\n");
                fw.write(BotProperties.XATKIT_NLUSERVER_BOTNAME + " = " + botPropertiesLang.get(BotProperties.XATKIT_NLUSERVER_BOTNAME) + "\n");
                fw.write(BotProperties.XATKIT_NLUSERVER_LANGUAGE + " = " + botPropertiesLang.get(BotProperties.XATKIT_NLUSERVER_LANGUAGE) + "\n");
                fw.write(BotProperties.XATKIT_NLUSERVER_URL + " = " + botPropertiesLang.get(BotProperties.XATKIT_NLUSERVER_URL) + "\n");
                fw.write(BotProperties.XATKIT_NLUSERVER_FORCE_OVERWRITE + " = " + botPropertiesLang.get(BotProperties.XATKIT_NLUSERVER_FORCE_OVERWRITE) + "\n");
                fw.write(BotProperties.XATKIT_NLUSERVER_CONFIDENCE_THRESHOLD + " = " + botPropertiesLang.get(BotProperties.XATKIT_NLUSERVER_CONFIDENCE_THRESHOLD) + "\n");

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

    /**
     * Creates the default {@code config.properties} configuration file of the generated bot.
     *
     * @param conf the bodi-generator configuration properties
     */
    protected static void createBotPropertiesFile(Configuration conf) {
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
    protected static void createBotLanguagePropertiesFile(String language, Configuration conf) {
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

            }  else if (conf.getString(BotProperties.XATKIT_INTENT_PROVIDER)
                    .equals("com.xatkit.core.recognition.nluserver.NLUServerIntentRecognitionProvider")) {
                fw.write(BotProperties.XATKIT_INTENT_PROVIDER + " = " + conf.getString(BotProperties.XATKIT_INTENT_PROVIDER) + "\n");
                fw.write(BotProperties.XATKIT_NLUSERVER_BOTNAME + " = " + "bot-name" + "\n");
                fw.write(BotProperties.XATKIT_NLUSERVER_LANGUAGE + " = " + language + "\n");
                fw.write(BotProperties.XATKIT_NLUSERVER_URL + " = " + "http://127.0.0.1:8000" + "\n");
                fw.write(BotProperties.XATKIT_NLUSERVER_FORCE_OVERWRITE + " = " + "true" + "\n");
                fw.write(BotProperties.XATKIT_NLUSERVER_CONFIDENCE_THRESHOLD + " = " + "0.0" + "\n");
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
}
