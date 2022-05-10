package com.xatkit.bot.library;

/**
 * This class stores constant property names used to configure the generated bot.
 */
public final class BotProperties {

    private BotProperties() {
    }

    // Bot

    public static final String DATA_NAME = "xls.importer.xls";
    public static final String CSV_DELIMITER = "csv.delimiter";
    public static final String XATKIT_SERVER_PORT = "xatkit.server.port";
    public static final String XATKIT_REACT_PORT = "xatkit.react.port";
    public static final String BOT_LANGUAGE = "bot.language";
    public static final String BOT_PAGE_LIMIT = "bot.pageLimit";
    public static final String BOT_MAX_ENTRIES_TO_DISPLAY = "bot.maxEntriesToDisplay";
    public static final String BOT_ENABLE_CHECK_CORRECT_ANSWER = "bot.enableCheckCorrectAnswer";

    // Intent provider

    public static final String XATKIT_INTENT_PROVIDER = "xatkit.intent.provider";

    // Intent provider: DialogFlow

    public static final String XATKIT_DIALOGFLOW_PROJECT_ID = "xatkit.dialogflow.projectId";
    public static final String XATKIT_DIALOGFLOW_CREDENTIALS_PATH = "xatkit.dialogflow.credentials.path";
    public static final String XATKIT_DIALOGFLOW_LANGUAGE = "xatkit.dialogflow.language";
    public static final String XATKIT_DIALOGFLOW_CLEAN_ON_STARTUP = "xatkit.dialogflow.clean_on_startup";

    // Intent provider: NLP.js

    public static final String XATKIT_NLPJS_AGENTID = "xatkit.nlpjs.agentId";
    public static final String XATKIT_NLPJS_LANGUAGE = "xatkit.nlpjs.language";
    public static final String XATKIT_NLPJS_SERVER = "xatkit.nlpjs.server";

    // Database

    public static final String XATKIT_LOGS_DATABASE = "xatkit.logs.database";

    // Database: PostgreSQL

    public static final String XATKIT_DATABASE_MODEL = "xatkit.database.model";
    public static final String XATKIT_RECOGNITION_ENABLE_MONITORING = "xatkit.recognition.enable_monitoring";
    public static final String XATKIT_POSTGRESQL_URL = "xatkit.postgresql.url";
    public static final String XATKIT_POSTGRESQL_USER = "xatkit.postgresql.user";
    public static final String XATKIT_POSTGRESQL_PASSWORD = "xatkit.postgresql.password";
    public static final String XATKIT_POSTGRESQL_BOT_ID = "xatkit.postgresql.bot_id";

    // NLP Server properties

    public static final String SERVER_URL = "SERVER_URL";
    public static final String TEXT_TO_TABLE_ENDPOINT = "TEXT_TO_TABLE_ENDPOINT";

    // Open data resource information

    public static final String BOT_ODATA_TITLE_EN = "bot.odata.title.en";
    public static final String BOT_ODATA_TITLE_CA = "bot.odata.title.ca";
    public static final String BOT_ODATA_TITLE_ES = "bot.odata.title.es";
    public static final String BOT_ODATA_URL_EN = "bot.odata.url.en";
    public static final String BOT_ODATA_URL_CA = "bot.odata.url.ca";
    public static final String BOT_ODATA_URL_ES = "bot.odata.url.es";

}
