package bodi.generator.ui.controller.user;

import bodi.generator.dataSchema.BodiGeneratorProperties;
import bodi.generator.dataSchema.DataSchema;
import bodi.generator.ui.model.BodiGeneratorObjects;
import bodi.generator.ui.model.Properties;
import bodi.generator.ui.service.DashboardService;
import com.xatkit.bot.library.BotProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

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
     * The list of errors to display in the {@code properties} page.
     */
    private List<String> errors = new ArrayList<>();

    /**
     * The current selected language to edit the language-dependant properties.
     */
    private String selectedLanguage = "en";

    /**
     * The current selected tab to display in the {@code properties} page.
     */
    private PropertiesTab tab = PropertiesTab.GENERAL;

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
            Map<String, Boolean> enabledLanguages = new HashMap<>();
            Set<String> enabledLanguagesSet = (Set<String>) objects.getProperties().getBotProperties().get(BotProperties.BOT_LANGUAGES);
            for (String lang : DataSchema.languages) {
                enabledLanguages.put(lang, enabledLanguagesSet.contains(lang));
            }
            model.addAttribute("enabledLanguages", enabledLanguages);
            model.addAttribute("selectedLanguage", selectedLanguage);
            model.addAttribute("errors", errors);
        }
        return dashboard.viewProperties(tab, model);
    }

    /**
     * Change the selected language in the {@code properties} page.
     *
     * @param currentTab the current tab in the properties page
     * @param language the selected language
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/change_selected_language/{currentTab}")
    public String changeSelectedLanguage(@PathVariable String currentTab,
                                         @RequestParam(value = "language") String language) {
        selectedLanguage = language;
        tab = PropertiesTab.valueOf(currentTab);
        return dashboard.redirect(DashboardView.PROPERTIES);
    }

    /**
     * Store the general properties.
     *
     * @param updatedProperties the updated properties
     * @param enabledLanguages  the enabled languages of the bot
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/store_properties_general")
    public String storePropertiesGeneral(@Valid @ModelAttribute("properties") Properties updatedProperties,
                                         @Valid @RequestParam(value = "enabledLanguages", required = false) Set<String> enabledLanguages) {
        Properties properties = objects.getProperties();

        // bodi-generator.properties
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.BOT_NAME, updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.BOT_NAME));
        properties.getBodiGeneratorProperties().put(BodiGeneratorProperties.ENABLE_TESTING, Boolean.valueOf(updatedProperties.getBodiGeneratorProperties().get(BodiGeneratorProperties.ENABLE_TESTING).toString()));
        // config.properties
        properties.getBotProperties().put(BotProperties.BOT_PAGE_LIMIT, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.BOT_PAGE_LIMIT).toString()));
        properties.getBotProperties().put(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY, Integer.valueOf(updatedProperties.getBotProperties().get(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY).toString()));
        properties.getBotProperties().put(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER, Boolean.valueOf(updatedProperties.getBotProperties().get(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER).toString()));

        errors = new ArrayList<>();

        if (!isEmpty(enabledLanguages)) {
            properties.getBotProperties().put(BotProperties.BOT_LANGUAGES, enabledLanguages);
            if (!enabledLanguages.contains(selectedLanguage)) {
                selectedLanguage = enabledLanguages.iterator().next();
            }
        } else {
            errors.add("You need to select at least 1 language");
        }
        tab = PropertiesTab.GENERAL;
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
        Map<String, Object> botPropertiesLang = properties.getBotPropertiesLang().get(selectedLanguage);
        Map<String, Object> updatedBotPropertiesLang = updatedProperties.getBotPropertiesLang().get(selectedLanguage);

        botPropertiesLang.put(BotProperties.XATKIT_INTENT_PROVIDER, updatedBotPropertiesLang.get(BotProperties.XATKIT_INTENT_PROVIDER));

        tab = PropertiesTab.INTENT_PROVIDER;
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
        Map<String, Object> botPropertiesLang = properties.getBotPropertiesLang().get(selectedLanguage);
        Map<String, Object> updatedBotPropertiesLang = updatedProperties.getBotPropertiesLang().get(selectedLanguage);
        if (botPropertiesLang.get(BotProperties.XATKIT_INTENT_PROVIDER).equals("com.xatkit.core.recognition.dialogflow.DialogFlowIntentRecognitionProvider")) {
            botPropertiesLang.put(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID, updatedBotPropertiesLang.get(BotProperties.XATKIT_DIALOGFLOW_PROJECT_ID));
            botPropertiesLang.put(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH, updatedBotPropertiesLang.get(BotProperties.XATKIT_DIALOGFLOW_CREDENTIALS_PATH));
            botPropertiesLang.put(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE, updatedBotPropertiesLang.get(BotProperties.XATKIT_DIALOGFLOW_LANGUAGE));
            botPropertiesLang.put(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP, Boolean.valueOf(updatedBotPropertiesLang.get(BotProperties.XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP).toString()));
        } else if (botPropertiesLang.get(BotProperties.XATKIT_INTENT_PROVIDER).equals("com.xatkit.core.recognition.nlpjs.NlpjsIntentRecognitionProvider")) {
            botPropertiesLang.put(BotProperties.XATKIT_NLPJS_AGENTID, updatedBotPropertiesLang.get(BotProperties.XATKIT_NLPJS_AGENTID));
            botPropertiesLang.put(BotProperties.XATKIT_NLPJS_LANGUAGE, updatedBotPropertiesLang.get(BotProperties.XATKIT_NLPJS_LANGUAGE));
            botPropertiesLang.put(BotProperties.XATKIT_NLPJS_SERVER, updatedBotPropertiesLang.get(BotProperties.XATKIT_NLPJS_SERVER));
        }
        tab = PropertiesTab.INTENT_PROVIDER;
        return dashboard.redirect(DashboardView.PROPERTIES);
    }

    /**
     * Store the bot properties.
     *
     * @param updatedProperties the updated properties
     * @return the name of the endpoint to redirect
     */
    @PostMapping("/store_properties_bot")
    public String storePropertiesBot(@Valid @ModelAttribute("properties") Properties updatedProperties) {
        Properties properties = objects.getProperties();
        Map<String, Object> botPropertiesLang = properties.getBotPropertiesLang().get(selectedLanguage);
        Map<String, Object> updatedBotPropertiesLang = updatedProperties.getBotPropertiesLang().get(selectedLanguage);
        botPropertiesLang.put(BotProperties.XATKIT_SERVER_PORT, updatedBotPropertiesLang.get(BotProperties.XATKIT_SERVER_PORT));
        botPropertiesLang.put(BotProperties.XATKIT_REACT_PORT, updatedBotPropertiesLang.get(BotProperties.XATKIT_REACT_PORT));
        tab = PropertiesTab.BOT;
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
        Map<String, Object> botPropertiesLang = properties.getBotPropertiesLang().get(selectedLanguage);
        Map<String, Object> updatedBotPropertiesLang = updatedProperties.getBotPropertiesLang().get(selectedLanguage);

        botPropertiesLang.put(BotProperties.XATKIT_LOGS_DATABASE, updatedBotPropertiesLang.get(BotProperties.XATKIT_LOGS_DATABASE));
        tab = PropertiesTab.DATABASE;
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
        Map<String, Object> botPropertiesLang = properties.getBotPropertiesLang().get(selectedLanguage);
        Map<String, Object> updatedBotPropertiesLang = updatedProperties.getBotPropertiesLang().get(selectedLanguage);
        if (botPropertiesLang.get(BotProperties.XATKIT_LOGS_DATABASE).equals("com.xatkit.core.recognition.RecognitionMonitorPostgreSQL")) {
            // properties.getBotProperties().put(BotProperties.XATKIT_DATABASE_MODEL, updatedBotPropertiesLang.get(BotProperties.XATKIT_DATABASE_MODEL));
            botPropertiesLang.put(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING, Boolean.valueOf(updatedBotPropertiesLang.get(BotProperties.XATKIT_RECOGNITION_ENABLE_MONITORING).toString()));
            botPropertiesLang.put(BotProperties.XATKIT_POSTGRESQL_URL, updatedBotPropertiesLang.get(BotProperties.XATKIT_POSTGRESQL_URL));
            botPropertiesLang.put(BotProperties.XATKIT_POSTGRESQL_USER, updatedBotPropertiesLang.get(BotProperties.XATKIT_POSTGRESQL_USER));
            botPropertiesLang.put(BotProperties.XATKIT_POSTGRESQL_PASSWORD, updatedBotPropertiesLang.get(BotProperties.XATKIT_POSTGRESQL_PASSWORD));
            botPropertiesLang.put(BotProperties.XATKIT_POSTGRESQL_BOT_ID, Integer.valueOf(updatedBotPropertiesLang.get(BotProperties.XATKIT_POSTGRESQL_BOT_ID).toString()));
        }
        tab = PropertiesTab.DATABASE;
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

        tab = PropertiesTab.NLP_SERVER;
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
        Map<String, Object> botPropertiesLang = properties.getBotPropertiesLang().get(selectedLanguage);
        Map<String, Object> updatedBotPropertiesLang = updatedProperties.getBotPropertiesLang().get(selectedLanguage);

        botPropertiesLang.put(BotProperties.BOT_ODATA_TITLE, updatedBotPropertiesLang.get(BotProperties.BOT_ODATA_TITLE));
        botPropertiesLang.put(BotProperties.BOT_ODATA_URL, updatedBotPropertiesLang.get(BotProperties.BOT_ODATA_URL));

        tab = PropertiesTab.OPEN_DATA;
        return dashboard.redirect(DashboardView.PROPERTIES);
    }
}
