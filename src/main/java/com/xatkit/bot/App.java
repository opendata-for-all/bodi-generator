package com.xatkit.bot;

import com.xatkit.bot.library.BotProperties;
import com.xatkit.bot.nlp.NLPServerClient;
import com.xatkit.bot.sql.SqlEngine;
import fr.inria.atlanmod.commons.log.Log;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The entry point of the application.
 * <p>
 * It executes a chatbot application, which is actually a set of chatbots, all the same but in different languages
 * (i.e. they speak and understand different languages)
 */
public final class App {

    private App() {
    }

    /**
     * The name of the bot properties file containing the general properties.
     */
    private static String BOT_PROPERTIES_FILE = "config.properties";

    /**
     * The name pattern of the bot properties files containing language-dependant properties.
     * <p>
     * It must be formatted to insert the corresponding suffix.
     *
     * @see MessageFormat#format(String, Object...)
     */
    private static String BOT_LANGUAGE_PROPERTIES_FILE = "config_{0}.properties";

    /**
     * The client that interacts with the server that deploys the NLP models to answer the questions.
     */
    public static NLPServerClient nlpServerClient;

    /**
     * The engine that performs the SQL related part of the bot.
     */
    public static SqlEngine sql;

    /**
     * The collection of bots, which are all the same but in different languages.
     */
    public static List<Bot> bots;

    /**
     * Initialize the application bots.
     */
    public static void initialize() {
        Configurations configurations = new Configurations();
        PropertiesConfiguration botConfiguration;
        try {
            botConfiguration = configurations.properties(Thread.currentThread().getContextClassLoader().getResource(BOT_PROPERTIES_FILE));
            nlpServerClient = new NLPServerClient("http://" + botConfiguration.getString("SERVER_URL") + "/", botConfiguration.getString(BotProperties.TEXT_TO_TABLE_ENDPOINT));
            sql = new SqlEngine();

            String[] languages = botConfiguration.getString(BotProperties.BOT_LANGUAGES).split(",");
            bots = new ArrayList<>();
            for (String language : languages) {
                String configLangFile = MessageFormat.format(BOT_LANGUAGE_PROPERTIES_FILE, language.replaceAll(" ", ""));
                try {
                    PropertiesConfiguration botLangConfiguration = configurations.properties(Thread.currentThread().getContextClassLoader().getResource(configLangFile));
                    botLangConfiguration.copy(botConfiguration);
                    bots.add(new Bot(botLangConfiguration));
                } catch (ConfigurationException e) {
                    Log.error("Configuration file {0} not found", configLangFile);
                    e.printStackTrace();
                }
            }
        } catch (ConfigurationException e) {
            Log.error("Configuration file {0} not found", BOT_PROPERTIES_FILE);
            e.printStackTrace();
        }
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        initialize();
        for (Bot bot : bots) {
            bot.run();
        }
    }
}
