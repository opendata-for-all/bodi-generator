package bodi.generator.dataSchema;

import bodi.generator.BodiGenerator;

/**
 * Generates and stores bot information and components, which are used to generate the source code of a bot.
 */
public class BotProperties {

    /**
     * Contains higher-level information about a tabular data source. It is used to generate bot components.
     */
    private final DataSchema ds;

    /**
     * The name of the bot this {@link BotProperties} refers to.
     */
    private final String botName;

    /**
     * The name of the document that the bot must read at runtime to satisfy the user queries.
     * <p>
     * This file should be the same as the one used to define {@link #ds} (consider any usage of
     * {@link BodiGenerator#createTabularDataSource(String, char)} before setting this attribute)
     */
    private final String inputDocName;

    /**
     * Instantiates a new {@link BotProperties}.
     *
     * @param botName      the bot name
     * @param inputDocName the name of the input document
     * @param ds           the data schema
     */
    public BotProperties(String botName, String inputDocName, DataSchema ds) {
        this.botName = botName;
        this.inputDocName = inputDocName;
        this.ds = ds;
    }


    /**
     * Creates the elements that will be part of the bot.
     */
    public void createBotStructure() {
    }

    /**
     * Gets the bot name.
     *
     * @return the bot name
     */
    public String getBotName() {
        return botName;
    }

    /**
     * Gets input document name.
     *
     * @return the input document name
     */
    public String getInputDocName() {
        return inputDocName;
    }

}
