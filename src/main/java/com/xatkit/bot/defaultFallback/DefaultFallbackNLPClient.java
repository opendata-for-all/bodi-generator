package com.xatkit.bot.defaultFallback;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import fr.inria.atlanmod.commons.log.Log;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.JSONObject;

/**
 * This class is a client to make requests to a language model running in a server.
 * <p>
 * The purpose of this class is to be used within the default fallback state of a Xatkit bot, when the bot is not
 * able to find an answer to a user input (i.e. when no intent is matched) and therefore, to empower the chatbot with
 * the ability of answering a wider range of questions related to its tabular data source.
 */
class DefaultFallbackNLPClient {

    /**
     * The name of the language model.
     * <p>
     * It must match with a Huggingface endpoint of a language model.
     */
    private String modelName;

    /**
     * The URL of the server running the language model.
     */
    private String modelServerUrl;

    /**
     * The {@code SetModel} endpoint of the server.
     * <p>
     * This endpoint takes care of setting (loading) the language model.
     */
    private String setModelEndpoint;

    /**
     * The {@code RunModel} endpoint of the server.
     * <p>
     * This endpoint takes care of running a query in the language model.
     */
    private String runModelEndpoint;

    /**
     * The HTTP status code that indicates a success in the request.
     */
    private static final int HTTP_STATUS_OK = 200;

    /**
     * Instantiates a new {@link DefaultFallbackNLPClient}.
     * <p>
     * The attributes of the instance are loaded from a resources file {@code defaultFallback.properties}
     */
    DefaultFallbackNLPClient() {
        Configuration configuration;
        Configurations configurations = new Configurations();
        try {
            configuration = configurations.properties(Thread.currentThread().getContextClassLoader()
                    .getResource("defaultFallback.properties"));
            this.modelName = configuration.getString("MODEL_NAME");
            this.modelServerUrl = configuration.getString("SERVER_URL");
            if (this instanceof TextToSQLClient) {
                this.setModelEndpoint = configuration.getString("SET_MODEL_ENDPOINT_SQL");
                this.runModelEndpoint = configuration.getString("RUN_MODEL_ENDPOINT_SQL");
            } else if (this instanceof TextToTableClient) {
                this.setModelEndpoint = configuration.getString("SET_MODEL_ENDPOINT_TABLE");
                this.runModelEndpoint = configuration.getString("RUN_MODEL_ENDPOINT_TABLE");
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.out.println("Configuration file not found");
        }
    }

    /**
     * Sets the language model.
     *
     * @return true if the language model was successfully set, false otherwise
     */
    public boolean setModel() {
        JSONObject request = new JSONObject();
        request.put("modelName", modelName);
        try {
            HttpResponse<JsonNode> response;
            response = Unirest
                    .post(modelServerUrl + setModelEndpoint)
                    .header("Content-Type", "application/json")
                    .body(request).asJson();
            if (response.getStatus() == HTTP_STATUS_OK) {
                Log.info("Language model {0} loaded in {1}", modelName, modelServerUrl);
                return true;
            }
        } catch (Exception e) {
            Log.error(e, "An error occurred while setting the model, see the attached exception");
        }
        return false;
    }

    /**
     * Makes a query to the language model.
     *
     * @param input the input of the language model
     * @return the output of the language model if it was successfully obtained, {@code null} otherwise
     */
    protected String runQuery(String input) {
        JSONObject request = new JSONObject();
        request.put("input", input);
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.post(modelServerUrl + runModelEndpoint)
                    .header("Content-Type", "application/json")
                    .body(request).asJson();
            if (response.getStatus() == HTTP_STATUS_OK) {
                return response.getBody().getObject().getString("output");
            }
        } catch (Exception e) {
            Log.error(e, "An error occurred while computing the answer, see the attached exception");
        }
        return null;
    }
}
