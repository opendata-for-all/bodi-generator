package bodi.generator.ui.controller.user;

import bodi.generator.dataSchema.BodiGeneratorProperties;
import bodi.generator.dataSchema.DataSchema;
import bodi.generator.dataSource.TabularDataSource;
import bodi.generator.ui.model.BodiGeneratorObjects;
import bodi.generator.ui.model.Properties;
import bodi.generator.ui.service.DashboardService;
import com.xatkit.bot.library.BotProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static bodi.generator.BodiGenerator.tabularDataSourceToDataSchema;

/**
 * The controller for the {@code import_data} functionalities of the bodi-generator UI.
 */
@Controller
@RequestMapping("/bodi-generator/import_data")
public class ImportDataController {

    /**
     * The shared objects between the controllers.
     */
    private final BodiGeneratorObjects objects;

    /**
     * The dashboard service of the controller.
     */
    private final DashboardService dashboard = new DashboardService();

    /**
     * Creates a new {@link ImportDataController}.
     *
     * @param objects the objects
     */
    public ImportDataController(BodiGeneratorObjects objects) {
        this.objects = objects;
    }

    /**
     * Show the {@code import_data} page.
     *
     * @param model the model
     * @return the name of the page
     */
    @GetMapping("")
    public String showImportData(Model model) {
        if (objects.isDataImported()) {
            model.addAttribute("tds", objects.getTds());
            model.addAttribute("fileName", objects.getDataName());
        }
        return dashboard.view(DashboardView.IMPORT_DATA, model);
    }

    /**
     * Store the data file.
     *
     * Also sets the default values of the {@link Properties}.
     *
     * @param file         the file
     * @param csvDelimiter the csv delimiter
     * @return the name of the endpoint to redirect
     * @throws IOException the io exception
     */
    @PostMapping("/store_file")
    public String storeFile(@RequestParam("file") MultipartFile file,
                            @RequestParam("csvDelimiter") char csvDelimiter) throws IOException {
        objects.setCsvDelimiter(csvDelimiter);
        // TODO: check csv.getContentType().equals("text/csv")
        objects.setDataName(file.getOriginalFilename());
        objects.setTds(new TabularDataSource(new ByteArrayInputStream(file.getInputStream().readAllBytes()), csvDelimiter)); // TODO: HANDLE DELIMITER ERROR
        objects.setDs(tabularDataSourceToDataSchema(objects.getTds()));
        objects.setSchemaType(objects.getDs().getSchemaType("mainSchemaType"));

        objects.setProperties(new Properties());

        Properties properties = objects.getProperties();

        properties.setBodiGeneratorProperties(new HashMap<>());

        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.BOT_NAME, "bot-" + objects.getDataName().replace(".csv", ""));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.OUTPUT_DIRECTORY, "bot-" + objects.getDataName().replace(".csv", ""));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.DATA_NAME, objects.getDataName().replace(".csv", ""));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.CSV_DELIMITER, objects.getCsvDelimiter());
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.ENABLE_TESTING, true);

        properties.setBotProperties(new HashMap<>());

        properties.getBotProperties().put(BotProperties.DATA_NAME, objects.getDataName().replace(".csv", ""));
        properties.getBotProperties().put(BotProperties.CSV_DELIMITER, objects.getCsvDelimiter());
        properties.getBotProperties().put(BotProperties.BOT_PAGE_LIMIT, 10);
        properties.getBotProperties().put(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY, 7);
        properties.getBotProperties().put(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER, true);
        properties.getBotProperties().put(BotProperties.BOT_LANGUAGES, (new HashSet<>(DataSchema.languages)));
        properties.getBotProperties().put(BotProperties.SERVER_URL, "127.0.0.1:5002");
        properties.getBotProperties().put(BotProperties.TEXT_TO_TABLE_ENDPOINT, "text-to-table");

        properties.setBotPropertiesLang(new HashMap<>());

        int portCount = 5000;
        for (String language : DataSchema.languages) {
            properties.getBotPropertiesLang().put(language, new HashMap<>());

            Map<String, Object> botPropertiesLang = properties.getBotPropertiesLang().get(language);

            botPropertiesLang.put(BotProperties.XATKIT_SERVER_PORT, portCount++);
            botPropertiesLang.put(BotProperties.XATKIT_REACT_PORT, portCount++);
            botPropertiesLang.put(BotProperties.BOT_LANGUAGE, language);
            botPropertiesLang.put(BotProperties.XATKIT_INTENT_PROVIDER, "com.xatkit.core.recognition.dialogflow.DialogFlowIntentRecognitionProvider");
            botPropertiesLang.put(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID, "<your project id>");
            botPropertiesLang.put(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH, "<path to your credentials file>");
            botPropertiesLang.put(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE, language);
            botPropertiesLang.put(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP, false);
            botPropertiesLang.put(BotProperties.XATKIT_NLPJS_AGENTID, "default");
            botPropertiesLang.put(BotProperties.XATKIT_NLPJS_LANGUAGE, language);
            botPropertiesLang.put(BotProperties.XATKIT_NLPJS_SERVER, "http://localhost:8080");
            botPropertiesLang.put(BotProperties.XATKIT_LOGS_DATABASE, "com.xatkit.core.recognition.RecognitionMonitorPostgreSQL");
            botPropertiesLang.put(BotProperties.XATKIT_DATABASE_MODEL, "postgresql");
            botPropertiesLang.put(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING, true);
            botPropertiesLang.put(BotProperties.XATKIT_POSTGRESQL_URL, "jdbc:postgresql://your-url");
            botPropertiesLang.put(BotProperties.XATKIT_POSTGRESQL_USER, "username");
            botPropertiesLang.put(BotProperties.XATKIT_POSTGRESQL_PASSWORD, "password");
            botPropertiesLang.put(BotProperties.XATKIT_POSTGRESQL_BOT_ID, 1);
            botPropertiesLang.put(BotProperties.BOT_ODATA_TITLE, "title");
            botPropertiesLang.put(BotProperties.BOT_ODATA_URL, "url");
        }

        objects.setDataImported(true);

        return dashboard.redirect(DashboardView.IMPORT_DATA);
    }
}
