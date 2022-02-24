package com.xatkit.bot;

import bodi.generator.dataSource.TabularDataSource;
import com.xatkit.bot.customQuery.CustomFilter;
import com.xatkit.bot.customQuery.CustomQuery;
import com.xatkit.bot.getResult.GetResult;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.bot.structuredQuery.SelectViewField;
import com.xatkit.bot.structuredQuery.StructuredFilter;
import com.xatkit.bot.structuredQuery.StructuredQuery;
import com.xatkit.core.XatkitBot;
import com.xatkit.plugins.core.library.CoreLibraryI18n;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The entry point of the application.
 */
public final class Bot {

    private Bot() {
    }

    /**
     * The chatbot language
     * <p>
     * It must be set within the main method, after the bot properties file is loaded.
     */
    public static String language = null;

    /**
     * The name of the bot properties file.
     */
    public static String botPropertiesFile = "bot.properties";

    /**
     * The container of the chatbot messages.
     * <p>
     * It must be set within the main method, after the bot properties file is loaded.
     * <p>
     * The messages' language is specified by the {@link #locale}
     */
    public static ResourceBundle messages = null;

    /**
     * The chatbot locale, which is used to define the chatbot language.
     * <p>
     * It must be set within the main method, after the bot properties file is loaded.
     * <p>
     * The locale language is specified by {@link #language}
     */
    public static Locale locale = null;

    /**
     * The name of the chatbot input document, which must be a tabular data file (a {@code .csv}).
     * <p>
     * It must be set within the main method, after the bot properties file is loaded.
     */
    public static String inputDoc = null;

    /**
     * This library contains useful objects to use within chatbots.
     *
     * For instance, it contains a collection of basic intents to use in a wide range oof chatbots (e.g. "Quit
     * intent", "Greetings intent", etc).
     *
     * It supports different languages, such as English, Spanish and Catalan.
     */
    public static CoreLibraryI18n coreLibraryI18n;

    /**
     * The Get Result workflow.
     */
    public static GetResult getResult;

    /**
     * The Structured Filter workflow.
     */
    public static StructuredFilter structuredFilter;

    /**
     * The Select View Field workflow.
     */
    public static SelectViewField selectViewField;

    /**
     * The Custom Filter workflow.
     */
    public static CustomFilter customFilter;

    /**
     * The Structured Query workflow.
     */
    public static StructuredQuery structuredQuery;

    /**
     * The Custom Query workflow.
     */
    public static CustomQuery customQuery;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {

        /*
         * Add configuration properties (e.g. authentication tokens, platform tuning, intent provider to use).
         * Check the corresponding platform's wiki page for further information on optional/mandatory parameters and
         * their values.
         */
        Configuration botConfiguration = new BaseConfiguration();
        Configurations configurations = new Configurations();
        try {
            botConfiguration = configurations.properties(Thread.currentThread().getContextClassLoader().getResource(
                    botPropertiesFile));
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.out.println("Configuration file not found");
        }
        language = botConfiguration.getString("bot.language", "en");
        locale = new Locale(language);
        messages = ResourceBundle.getBundle("messages", locale);
        inputDoc = botConfiguration.getString("xls.importer.xls");
        coreLibraryI18n = new CoreLibraryI18n(locale);
        char delimiter = botConfiguration.getString("csv.delimiter").charAt(0);
        String odataTitle = botConfiguration.getString("bot.odata.title." + language, null);
        String odataUrl = botConfiguration.getString("bot.odata.url." + language, null);

        /*
         * Instantiate the platform and providers we will use in the bot definition.
         */
        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = reactPlatform.getReactEventProvider();
        ReactIntentProvider reactIntentProvider = reactPlatform.getReactIntentProvider();

        /*
         * Create the states we want to use in our bot.
         */
        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val startState = state("Start");

        /*
         * Initialize the chatbot workflows.
         */
        getResult = new GetResult(reactPlatform, startState.getState());
        structuredFilter = new StructuredFilter(reactPlatform, startState.getState());
        selectViewField = new SelectViewField(reactPlatform, startState.getState());
        customFilter = new CustomFilter(reactPlatform, startState.getState());
        structuredQuery = new StructuredQuery(reactPlatform, startState.getState());
        customQuery = new CustomQuery(reactPlatform, startState.getState());

        /*
         * Specify the content of the bot states (i.e. the behavior of the bot).
         */
        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(awaitingInput);
        awaitingInput
                .body(context -> {
                    if (!context.getSession().containsKey(ContextKeys.TABULAR_DATA_SOURCE)) {
                        context.getSession().put(ContextKeys.TABULAR_DATA_SOURCE,
                                new TabularDataSource(Objects.requireNonNull(Thread.currentThread()
                                .getContextClassLoader().getResource(inputDoc)).getPath(), delimiter));
                    }
                    List<String> fields = new ArrayList<>();
                    fields.addAll(Utils.getEntityValues(Entities.numericFieldEntity));
                    fields.addAll(Utils.getEntityValues(Entities.textualFieldEntity));
                    fields.addAll(Utils.getEntityValues(Entities.dateFieldEntity));
                    TabularDataSource tds =
                            (TabularDataSource) context.getSession().get(ContextKeys.TABULAR_DATA_SOURCE);
                    context.getSession().put(ContextKeys.STATEMENT, tds.createStatement());
                    // Uncomment to disable case-sensitivity in filter values
                        //.setIgnoreCaseFilterValue(true));
                    List<String> filterFieldOptions = new ArrayList<>(fields);
                    context.getSession().put(ContextKeys.FILTER_FIELD_OPTIONS, filterFieldOptions);
                    List<String> viewFieldOptions = new ArrayList<>(fields);
                    context.getSession().put(ContextKeys.VIEW_FIELD_OPTIONS, viewFieldOptions);
                    reactPlatform.reply(context, messages.getString("Greetings"));
                    if (!isEmpty(odataTitle) && !isEmpty(odataUrl)) {
                        reactPlatform.reply(context, "[" + odataTitle + "](" + odataUrl + ")");
                    }  else if (!isEmpty(odataTitle)) {
                        reactPlatform.reply(context, odataTitle);
                    } else if (!isEmpty(odataUrl)) {
                        reactPlatform.reply(context, "[" + odataUrl + "](" + odataUrl + ")");
                    }
                })
                .next()
                .moveTo(startState);
        startState
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("SelectAction"),
                            Utils.getFirstTrainingSentences(
                                    Intents.structuredQueryIntent,
                                    Intents.customQueryIntent));
                })
                .next()
                .when(intentIs(Intents.structuredQueryIntent)).moveTo(structuredQuery.getAwaitingStructuredQueryState())
                .when(intentIs(Intents.customQueryIntent)).moveTo(customQuery.getAwaitingCustomQueryState());

        /*
         * The state that is executed if the engine doesn't find any navigable transition in a state and the state
         * doesn't contain a fallback.
         */
        val defaultFallback = fallbackState()
                .body(context -> {
                    reactPlatform.reply(context, messages.getString("DefaultFallbackMessage"));
                });

        /*
         * Creates the bot model that will be executed by the Xatkit engine.
         */
        val botModel = model()
                .usePlatform(reactPlatform)
                .listenTo(reactEventProvider)
                .listenTo(reactIntentProvider)
                .initState(init)
                .defaultFallbackState(defaultFallback);


        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
        /*
         * The bot is now started, you can check http://localhost:5000/admin to test it.
         * The logs of the bot are stored in the logs folder at the root of this project.
         */
    }
}
