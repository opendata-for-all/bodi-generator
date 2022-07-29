package com.xatkit.bot;

import com.xatkit.bot.customQuery.CustomQuery;
import com.xatkit.bot.getResult.CheckCorrectAnswer;
import com.xatkit.bot.getResult.GetResult;
import com.xatkit.bot.library.BotProperties;
import com.xatkit.bot.library.ContextKeys;
import com.xatkit.bot.library.Entities;
import com.xatkit.bot.library.Intents;
import com.xatkit.bot.library.Utils;
import com.xatkit.bot.sql.SqlQueries;
import com.xatkit.bot.structuredQuery.StructuredQuery;
import com.xatkit.core.XatkitBot;
import com.xatkit.plugins.core.library.CoreLibraryI18n;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class Bot {

    /**
     * The chatbot language
     * <p>
     * It must be set within the main method, after the bot properties file is loaded.
     */
    public final String language;

    /**
     * The chatbot locale, which is used to define the chatbot language.
     * <p>
     * It must be set within the main method, after the bot properties file is loaded.
     * <p>
     * The locale language is specified by {@link #language}
     */
    public final Locale locale;

    /**
     * The container of the chatbot messages.
     * <p>
     * It must be set within the main method, after the bot properties file is loaded.
     * <p>
     * The messages' language is specified by the {@link #locale}
     */
    public final ResourceBundle messages;

    /**
     * The name of the chatbot input document, which must be a tabular data file (a {@code .csv}).
     * <p>
     * It must be set within the main method, after the bot properties file is loaded.
     */
    public final String inputDoc;

    /**
     * The maximum number of entries of a table that are displayed at once in the chatbot chat box (i.e. the page size).
     */
    public final int pageLimit;

    /**
     * If the number of entries of a result set generated after a chatbot query is less or equal than this number,
     * the result set is displayed immediately afterwards.
     */
    public final int maxEntriesToDisplay;

    /**
     * This library contains useful objects to use within chatbots.
     *
     * For instance, it contains a collection of basic intents to use in a wide range oof chatbots (e.g. "Quit
     * intent", "Greetings intent", etc).
     *
     * It supports different languages, such as English, Spanish and Catalan.
     */
    public final CoreLibraryI18n coreLibraryI18n;

    /**
     * The Check Correct Answer workflow.
     */
    public final CheckCorrectAnswer checkCorrectAnswer;

    /**
     * The Get Result workflow.
     */
    public final GetResult getResult;

    /**
     * The Structured Query workflow.
     */
    public final StructuredQuery structuredQuery;

    /**
     * The Custom Query workflow.
     */
    public final CustomQuery customQuery;

    /**
     * The system that generates the SQL queries.
     */
    public final SqlQueries sqlQueries;

    /**
     * The set of intents the chatbot can recognize.
     */
    public final Intents intents;

    /**
     * The set of entities the chatbot can recognize.
     */
    public final Entities entities;

    /**
     * The {@link XatkitBot}.
     */
    public final XatkitBot xatkitBot;

    /**
     * The {@link ReactPlatform} of the chatbot.
     */
    public final ReactPlatform reactPlatform;

    public Bot(Configuration botConfiguration) {

        /*
         * Add configuration properties (e.g. authentication tokens, platform tuning, intent provider to use).
         * Check the corresponding platform's wiki page for further information on optional/mandatory parameters and
         * their values.
         */
        language = botConfiguration.getString(BotProperties.BOT_LANGUAGE, "en");
        locale = new Locale(language);
        entities = new Entities(language);
        intents = new Intents(entities, locale);
        messages = ResourceBundle.getBundle("messages", locale);
        inputDoc = botConfiguration.getString(BotProperties.DATA_NAME, "data") + ".csv";
        pageLimit = botConfiguration.getInt(BotProperties.BOT_PAGE_LIMIT, 10);
        maxEntriesToDisplay = botConfiguration.getInt(BotProperties.BOT_MAX_ENTRIES_TO_DISPLAY, 7);
        coreLibraryI18n = new CoreLibraryI18n(locale);
        char delimiter = botConfiguration.getString(BotProperties.CSV_DELIMITER, ",").charAt(0);
        boolean enableCheckCorrectAnswer = botConfiguration.getBoolean(BotProperties.BOT_ENABLE_CHECK_CORRECT_ANSWER, false);
        String odataTitle = botConfiguration.getString(BotProperties.BOT_ODATA_TITLE, null);
        String odataUrl = botConfiguration.getString(BotProperties.BOT_ODATA_URL, null);

        sqlQueries = new SqlQueries(inputDoc, delimiter);
        List<String> fields = new ArrayList<>(Utils.getEntityValues(entities.fieldEntity));
        sqlQueries.getAllFields().addAll(fields);

        /*
         * Instantiate the platform and providers we will use in the bot definition.
         */
        reactPlatform = new ReactPlatform();
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
        if (enableCheckCorrectAnswer) {
            checkCorrectAnswer = new CheckCorrectAnswer(this, startState.getState());
            getResult = new GetResult(this, checkCorrectAnswer.getProcessCheckCorrectAnswerState());
            structuredQuery = new StructuredQuery(this, startState.getState());
            customQuery = new CustomQuery(this, checkCorrectAnswer.getProcessCheckCorrectAnswerState());
        } else {
            checkCorrectAnswer = null;
            getResult = new GetResult(this, startState.getState());
            structuredQuery = new StructuredQuery(this, startState.getState());
            customQuery = new CustomQuery(this, startState.getState());
        }

        /*
         * Specify the content of the bot states (i.e. the behavior of the bot).
         */
        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(awaitingInput);
        awaitingInput
                .body(context -> {
                    List<String> filterFieldOptions = new ArrayList<>(fields);
                    context.getSession().put(ContextKeys.FILTER_FIELD_OPTIONS, filterFieldOptions);
                    List<String> viewFieldOptions = new ArrayList<>(fields);
                    context.getSession().put(ContextKeys.VIEW_FIELD_OPTIONS, viewFieldOptions);
                    reactPlatform.reply(context, messages.getString("Greetings"));
                    if (!isEmpty(odataTitle) && !isEmpty(odataUrl)) {
                        reactPlatform.reply(context, "[" + odataTitle + "](" + odataUrl + ")");
                    } else if (!isEmpty(odataTitle)) {
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
                                    intents.structuredQueryIntent,
                                    intents.customQueryIntent));
                })
                .next()
                .when(intentIs(intents.structuredQueryIntent)).moveTo(structuredQuery.getAwaitingStructuredQueryState())
                .when(intentIs(intents.customQueryIntent)).moveTo(customQuery.getAwaitingCustomQueryState());

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

        xatkitBot = new XatkitBot(botModel, botConfiguration);
    }

    /**
     * Run the {@link #xatkitBot}.
     */
    public void run() {
        this.xatkitBot.run();
    }
}
