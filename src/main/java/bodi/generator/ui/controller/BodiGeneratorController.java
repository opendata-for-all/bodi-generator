package bodi.generator.ui.controller;

import bodi.generator.dataSchema.BodiGeneratorProperties;
import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSchema.SchemaField;
import bodi.generator.dataSchema.SchemaType;
import bodi.generator.dataSource.TabularDataSource;
import bodi.generator.ui.model.Properties;
import com.xatkit.bot.library.BotProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import static bodi.generator.BodiGenerator.createBot;
import static bodi.generator.BodiGenerator.tabularDataSourceToDataSchema;
import static java.util.Objects.isNull;


/**
 * The bodi-generator UI main controller. It handles requests from the user interface.
 */
@Controller
@RequestMapping("/bodi-generator")
public class BodiGeneratorController {

    private byte[] csv;
    private String dataName;
    private TabularDataSource tds;
    private DataSchema ds;
    private SchemaType schemaType;
    private SchemaField schemaField;
    private Properties properties;

    @GetMapping
    public String showMainPage(@RequestParam(value = "field", required = false) String field,
                               Model model) {
        model.addAttribute("tds", tds);
        model.addAttribute("schemaType", schemaType);
        if (!isNull(csv)) {
            model.addAttribute("fileName", dataName);
            if (!isNull(field)) {
                schemaField = schemaType.getSchemaField(field);
            } else {
                schemaField = null;
            }
            model.addAttribute("schemaField", schemaField);
            model.addAttribute("properties", properties);
            //model.addAttribute("botProperties", botProperties);
        }
        return "bodi-generator/main";
    }

    @PostMapping("/storeField")
    public String saveSchemaField(@Valid @ModelAttribute("schemaField") SchemaField updatedSchemaField,
                                  BindingResult bindingResult,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            // Handle errors
            // return "bodi-generator/main";
        }
        // this.schemaField.setType(updatedSchemaField.getType());
        this.schemaField.setReadableName(updatedSchemaField.getReadableName());
        this.schemaField.setSynonyms(updatedSchemaField.getSynonyms());
        this.schemaField.setMainValues(updatedSchemaField.getMainValues());
        if (!this.schemaField.isCategorical() && updatedSchemaField.isCategorical()) {
            this.schemaField.addMainValues(tds.getColumnUniqueValues(this.schemaField.getOriginalName()));
        } else if (this.schemaField.isCategorical() && !updatedSchemaField.isCategorical()) {
            this.schemaField.resetMainValues();
        }
        this.schemaField.setCategorical(updatedSchemaField.isCategorical());
        return "redirect:/bodi-generator";
    }

    @PostMapping("/storeFile")
    public String storeFile(@RequestParam("file") MultipartFile file,
                            Model model) throws IOException {
        csv = file.getInputStream().readAllBytes();
        // TODO: check csv.getContentType().equals("text/csv")
        dataName = file.getOriginalFilename();
        tds = new TabularDataSource(new ByteArrayInputStream(csv), ','); // TODO: DELIMITER VARIABLE
        ds = tabularDataSourceToDataSchema(tds);
        schemaType = ds.getSchemaType("mainSchemaType");

        properties = new Properties();

        properties.setBodiGeneratorProperties(new HashMap<>());

        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.BOT_NAME, "bot-" + dataName.replace(".csv", ""));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.OUTPUT_DIRECTORY, "./ui-bot");
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.DATA_NAME, dataName.replace(".csv", ""));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.CSV_DELIMITER, ',');
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.ENABLE_TESTING, true);
        // properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.MAX_NUM_DIFFERENT_VALUES, 15);
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER, "com.xatkit.core.recognition.dialogflow.DialogFlowIntentRecognitionProvider");
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.XATKIT_LOGS_DATABASE, "com.xatkit.core.recognition.RecognitionMonitorPostgreSQL");

        properties.setBotProperties(new HashMap<>());

        //properties.getBotProperties().put(BotProperties.DATA_NAME, "carrecs_govern_bcn");
        properties.getBotProperties().put(BotProperties.CSV_DELIMITER, ',');
        properties.getBotProperties().put(BotProperties.XATKIT_SERVER_PORT, 5000);
        properties.getBotProperties().put(BotProperties.XATKIT_REACT_PORT, 5001);
        properties.getBotProperties().put(BotProperties.BOT_LANGUAGE, "es");
        properties.getBotProperties().put(BotProperties.BOT_PAGE_LIMIT, 10);
        properties.getBotProperties().put(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY, 7);
        properties.getBotProperties().put(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER, true);
        properties.getBotProperties().put(BotProperties.XATKIT_INTENT_PROVIDER, "com.xatkit.core.recognition.dialogflow.DialogFlowIntentRecognitionProvider");
        properties.getBotProperties().put(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID, "<your project id>");
        properties.getBotProperties().put(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH, "<path to your credentials file>");
        properties.getBotProperties().put(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE, "es");
        properties.getBotProperties().put(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP, false);
        properties.getBotProperties().put(BotProperties.XATKIT_NLPJS_AGENTID, "default");
        properties.getBotProperties().put(BotProperties.XATKIT_NLPJS_LANGUAGE, "es");
        properties.getBotProperties().put(BotProperties.XATKIT_NLPJS_SERVER, "http://localhost:8080");
        properties.getBotProperties().put(BotProperties.XATKIT_LOGS_DATABASE, "com.xatkit.core.recognition.RecognitionMonitorPostgreSQL");
        properties.getBotProperties().put(BotProperties.XATKIT_DATABASE_MODEL, "postgresql");
        properties.getBotProperties().put(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING, true);
        properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_URL, "jdbc:postgresql://your-url");
        properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_USER, "username");
        properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_PASSWORD, "password");
        properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_BOT_ID, 1);
        properties.getBotProperties().put(BotProperties.SERVER_URL, "127.0.0.1:5002");
        properties.getBotProperties().put(BotProperties.TEXT_TO_TABLE_ENDPOINT, "text-to-table");
        properties.getBotProperties().put(BotProperties.BOT_ODATA_TITLE_EN, "title-in-english");
        properties.getBotProperties().put(BotProperties.BOT_ODATA_TITLE_CA, "title-in-catalan");
        properties.getBotProperties().put(BotProperties.BOT_ODATA_TITLE_ES, "title-in-spanish");
        properties.getBotProperties().put(BotProperties.BOT_ODATA_URL_EN, "url-english-source");
        properties.getBotProperties().put(BotProperties.BOT_ODATA_URL_CA, "url-catalan-source");
        properties.getBotProperties().put(BotProperties.BOT_ODATA_URL_ES, "url-spanish-source");

        return "redirect:/bodi-generator";
    }

    @PostMapping("/storeBodiGeneratorProperties")
    public String storeBodiGeneratorProperties(@Valid @ModelAttribute("bodiGeneratorProperties") Properties updatedProperties,
                                               Model model) {
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.BOT_NAME, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.BOT_NAME));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.OUTPUT_DIRECTORY, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.OUTPUT_DIRECTORY));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.DATA_NAME, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.DATA_NAME));
        properties.getBotProperties().put(BotProperties.DATA_NAME,updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.DATA_NAME));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.CSV_DELIMITER, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.CSV_DELIMITER));
        properties.getBotProperties().put(BotProperties.CSV_DELIMITER,updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.CSV_DELIMITER));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.ENABLE_TESTING, Boolean.valueOf(updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.ENABLE_TESTING).toString()));
        // properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.MAX_NUM_DIFFERENT_VALUES, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.MAX_NUM_DIFFERENT_VALUES));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER));
        properties.getBotProperties().put(BotProperties.XATKIT_INTENT_PROVIDER,updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.XATKIT_LOGS_DATABASE, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.XATKIT_LOGS_DATABASE));
        properties.getBotProperties().put(BotProperties.XATKIT_LOGS_DATABASE,updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.XATKIT_LOGS_DATABASE));

        return "redirect:/bodi-generator";
    }

    @PostMapping("/storeBotProperties")
    public String storeBotProperties(@Valid @ModelAttribute("botProperties") Properties updatedProperties,
                                     Model model) {
        properties.getBotProperties().put(BotProperties.DATA_NAME, updatedProperties.getBotProperties().get(BotProperties.DATA_NAME));
        // properties.getBotProperties().put(BotProperties.CSV_DELIMITER, updatedProperties.getBotProperties().get(BotProperties.CSV_DELIMITER));
        properties.getBotProperties().put(BotProperties.XATKIT_SERVER_PORT, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.XATKIT_SERVER_PORT).toString()));
        properties.getBotProperties().put(BotProperties.XATKIT_REACT_PORT, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.XATKIT_REACT_PORT).toString()));
        properties.getBotProperties().put(BotProperties.BOT_LANGUAGE, updatedProperties.getBotProperties().get(BotProperties.BOT_LANGUAGE));
        properties.getBotProperties().put(BotProperties.BOT_PAGE_LIMIT, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.BOT_PAGE_LIMIT).toString()));
        properties.getBotProperties().put(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY).toString()));
        properties.getBotProperties().put(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER, Boolean.valueOf(updatedProperties.getBotProperties().get(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER).toString()));
        // properties.getBotProperties().put(BotProperties.XATKIT_INTENT_PROVIDER, updatedProperties.getBotProperties().get(BotProperties.XATKIT_INTENT_PROVIDER));
        if (properties.getBotProperties().get(BotProperties.XATKIT_INTENT_PROVIDER).equals("com.xatkit.core.recognition.dialogflow.DialogFlowIntentRecognitionProvider")) {
            properties.getBotProperties().put(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID, updatedProperties.getBotProperties().get(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID));
            properties.getBotProperties().put(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH, updatedProperties.getBotProperties().get(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH));
            properties.getBotProperties().put(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE, updatedProperties.getBotProperties().get(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE));
            properties.getBotProperties().put(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP, Boolean.valueOf(updatedProperties.getBotProperties().get(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP).toString()));
        } else if (properties.getBotProperties().get(BotProperties.XATKIT_INTENT_PROVIDER).equals("com.xatkit.core.recognition.nlpjs.NlpjsIntentRecognitionProvider")) {
            properties.getBotProperties().put(BotProperties.XATKIT_NLPJS_AGENTID, updatedProperties.getBotProperties().get(BotProperties.XATKIT_NLPJS_AGENTID));
            properties.getBotProperties().put(BotProperties.XATKIT_NLPJS_LANGUAGE, updatedProperties.getBotProperties().get(BotProperties.XATKIT_NLPJS_LANGUAGE));
            properties.getBotProperties().put(BotProperties.XATKIT_NLPJS_SERVER, updatedProperties.getBotProperties().get(BotProperties.XATKIT_NLPJS_SERVER));
        }
        // properties.getBotProperties().put(BotProperties.XATKIT_LOGS_DATABASE, updatedProperties.getBotProperties().get(BotProperties.XATKIT_LOGS_DATABASE));
        if (properties.getBotProperties().get(BotProperties.XATKIT_LOGS_DATABASE).equals("com.xatkit.core.recognition.RecognitionMonitorPostgreSQL")) {
            // properties.getBotProperties().put(BotProperties.XATKIT_DATABASE_MODEL, updatedProperties.getBotProperties().get(BotProperties.XATKIT_DATABASE_MODEL));
            properties.getBotProperties().put(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING, Boolean.valueOf(updatedProperties.getBotProperties().get(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING).toString()));
            properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_URL, updatedProperties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_URL));
            properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_USER, updatedProperties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_USER));
            properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_PASSWORD, updatedProperties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_PASSWORD));
            properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_BOT_ID, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_BOT_ID).toString()));
        }
        properties.getBotProperties().put(BotProperties.SERVER_URL, updatedProperties.getBotProperties().get(BotProperties.SERVER_URL));
        properties.getBotProperties().put(BotProperties.TEXT_TO_TABLE_ENDPOINT, updatedProperties.getBotProperties().get(BotProperties.TEXT_TO_TABLE_ENDPOINT));

        properties.getBotProperties().put(BotProperties.BOT_ODATA_TITLE_EN, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_TITLE_EN));
        properties.getBotProperties().put(BotProperties.BOT_ODATA_TITLE_CA, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_TITLE_CA));
        properties.getBotProperties().put(BotProperties.BOT_ODATA_TITLE_ES, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_TITLE_ES));
        properties.getBotProperties().put(BotProperties.BOT_ODATA_URL_EN, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_URL_EN));
        properties.getBotProperties().put(BotProperties.BOT_ODATA_URL_CA, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_URL_CA));
        properties.getBotProperties().put(BotProperties.BOT_ODATA_URL_ES, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_URL_ES));
        return "redirect:/bodi-generator";
    }

    @PostMapping("/createBot")
    public String createBotEndpoint(Model model) {
        createBot(properties, ds, new ByteArrayInputStream(csv));
        return "redirect:/bodi-generator";
    }

}
