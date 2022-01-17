package com.xatkit.bot.defaultFallback;

/**
 * This class is a client to make requests to a language model running in a server.
 * <p>
 * This class is used when the language model loaded is of the type Text-to-Table. That is, the input of the language
 * model must be a natural language query + a table, and the expected output is a tabular result (returned as a {@code
 * String}).
 */
public class TextToTableClient extends DefaultFallbackNLPClient {

    /**
     * Instantiates a new {@link TextToTableClient}.
     */
    public TextToTableClient() {
        super();
    }

    /**
     * Makes a query to the language model.
     *
     * @param input the input of the language model
     * @return the output of the language model if it was successfully obtained, {@code null} otherwise
     */
    public String runTableQuery(String input) {
        return runQuery(input);
    }
}
