package bodi.generator.ui.controller.user;

import bodi.generator.dataSchema.BodiGeneratorProperties;
import bodi.generator.ui.model.BodiGeneratorObjects;
import bodi.generator.ui.model.Properties;
import bodi.generator.ui.service.DashboardService;
import com.xatkit.bot.library.BotProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

/**
 * The controller for the {@code properties} functionalities of the bodi-generator UI.
 */
@Controller
@RequestMapping("/bodi-generator/properties")
public class PropertiesController {

    /**
     * The shared objects between the controllers.
     */
    private final BodiGeneratorObjects objects;

    /**
     * The dashboard service of the controller.
     */
    private final DashboardService dashboard = new DashboardService();

    /**
     * Creates a new {@link PropertiesController}.
     *
     * @param objects the objects
     */
    public PropertiesController(BodiGeneratorObjects objects) {
        this.objects = objects;
    }

    /**
     * Show the {@code properties} page.
     *
     * @param model the model
     * @return the name of the page
     */
    @GetMapping("")
    public String showProperties(Model model) {
        if (objects.isDataImported()) {
            model.addAttribute("properties", objects.getProperties());
        }
        return dashboard.view(DashboardView.PROPERTIES, model);
    }

    /**
     * Store the general properties.
     *
     * @param updatedProperties the updated properties
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/store_properties_general")
    public String storePropertiesGeneral(@Valid @ModelAttribute("properties") Properties updatedProperties) {
        Properties properties = objects.getProperties();

        // bodi-generator.properties
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.BOT_NAME, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.BOT_NAME));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.ENABLE_TESTING, Boolean.valueOf(updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.ENABLE_TESTING).toString()));
        // bot.properties
        properties.getBotProperties().put(BotProperties.XATKIT_SERVER_PORT, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.XATKIT_SERVER_PORT).toString()));
        properties.getBotProperties().put(BotProperties.XATKIT_REACT_PORT, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.XATKIT_REACT_PORT).toString()));
        properties.getBotProperties().put(BotProperties.BOT_LANGUAGE, updatedProperties.getBotProperties().get(BotProperties.BOT_LANGUAGE));
        properties.getBotProperties().put(BotProperties.BOT_PAGE_LIMIT, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.BOT_PAGE_LIMIT).toString()));
        properties.getBotProperties().put(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY).toString()));
        properties.getBotProperties().put(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER, Boolean.valueOf(updatedProperties.getBotProperties().get(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER).toString()));

        return dashboard.redirect(DashboardView.PROPERTIES);
    }

    /**
     * Sets the intent provider.
     *
     * @param updatedProperties the updated properties
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/set_intent_provider")
    public String setIntentProvider(@Valid @ModelAttribute("properties") Properties updatedProperties) {
        Properties properties = objects.getProperties();

        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER));
        properties.getBotProperties().put(BotProperties.XATKIT_INTENT_PROVIDER, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.XATKIT_INTENT_PROVIDER));

        return dashboard.redirect(DashboardView.PROPERTIES);
    }

    /**
     * Store the intent provider properties.
     *
     * @param updatedProperties the updated properties
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/store_properties_intent_provider")
    public String storePropertiesIntentProvider(@Valid @ModelAttribute("properties") Properties updatedProperties) {
        Properties properties = objects.getProperties();

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
        return dashboard.redirect(DashboardView.PROPERTIES);
    }

    /**
     * Sets the database.
     *
     * @param updatedProperties the updated properties
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/set_database")
    public String setDatabase(@Valid @ModelAttribute("properties") Properties updatedProperties) {
        Properties properties = objects.getProperties();

        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.XATKIT_LOGS_DATABASE, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.XATKIT_LOGS_DATABASE));
        properties.getBotProperties().put(BotProperties.XATKIT_LOGS_DATABASE, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.XATKIT_LOGS_DATABASE));

        return dashboard.redirect(DashboardView.PROPERTIES);
    }

    /**
     * Store the database properties.
     *
     * @param updatedProperties the updated properties
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/store_properties_database")
    public String storePropertiesDatabase(@Valid @ModelAttribute("properties") Properties updatedProperties) {
        Properties properties = objects.getProperties();

        if (properties.getBotProperties().get(BotProperties.XATKIT_LOGS_DATABASE).equals("com.xatkit.core.recognition.RecognitionMonitorPostgreSQL")) {
            // properties.getBotProperties().put(BotProperties.XATKIT_DATABASE_MODEL, updatedProperties.getBotProperties().get(BotProperties.XATKIT_DATABASE_MODEL));
            properties.getBotProperties().put(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING, Boolean.valueOf(updatedProperties.getBotProperties().get(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING).toString()));
            properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_URL, updatedProperties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_URL));
            properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_USER, updatedProperties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_USER));
            properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_PASSWORD, updatedProperties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_PASSWORD));
            properties.getBotProperties().put(BotProperties.XATKIT_POSTGRESQL_BOT_ID, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.XATKIT_POSTGRESQL_BOT_ID).toString()));
        }
        return dashboard.redirect(DashboardView.PROPERTIES);
    }

    /**
     * Store the nlp server properties.
     *
     * @param updatedProperties the updated properties
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/store_properties_nlp_server")
    public String storePropertiesNlpServer(@Valid @ModelAttribute("properties") Properties updatedProperties) {
        Properties properties = objects.getProperties();

        properties.getBotProperties().put(BotProperties.SERVER_URL, updatedProperties.getBotProperties().get(BotProperties.SERVER_URL));
        properties.getBotProperties().put(BotProperties.TEXT_TO_TABLE_ENDPOINT, updatedProperties.getBotProperties().get(BotProperties.TEXT_TO_TABLE_ENDPOINT));

        return dashboard.redirect(DashboardView.PROPERTIES);
    }

    /**
     * Store the open data properties.
     *
     * @param updatedProperties the updated properties
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/store_properties_open_data")
    public String storePropertiesOpenData(@Valid @ModelAttribute("properties") Properties updatedProperties) {
        Properties properties = objects.getProperties();

        properties.getBotProperties().put(BotProperties.BOT_ODATA_TITLE_EN, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_TITLE_EN));
        properties.getBotProperties().put(BotProperties.BOT_ODATA_TITLE_CA, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_TITLE_CA));
        properties.getBotProperties().put(BotProperties.BOT_ODATA_TITLE_ES, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_TITLE_ES));
        properties.getBotProperties().put(BotProperties.BOT_ODATA_URL_EN, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_URL_EN));
        properties.getBotProperties().put(BotProperties.BOT_ODATA_URL_CA, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_URL_CA));
        properties.getBotProperties().put(BotProperties.BOT_ODATA_URL_ES, updatedProperties.getBotProperties().get(BotProperties.BOT_ODATA_URL_ES));

        return dashboard.redirect(DashboardView.PROPERTIES);
    }
}
