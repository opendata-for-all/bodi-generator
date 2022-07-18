package com.xatkit.bot.nlp;

import bodi.generator.dataSource.ResultSet;
import bodi.generator.dataSource.Row;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.xatkit.bot.Bot;
import fr.inria.atlanmod.commons.log.Log;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xatkit.bot.Bot.checkCorrectAnswer;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * This class is a client to make requests to a server that runs NLP models.
 * <p>
 * The purpose of this class is to be used when the bot is not able to find an answer to a user input (i.e. when no
 * intent is matched) and therefore, to empower the chatbot with the ability of answering a wider range of questions
 * related to its tabular data source.
 */
public class NLPServerClient {

    /**
     * The URL of the server.
     */
    protected String serverUrl;

    /**
     * The {@code textToTable} endpoint of the server.
     * <p>
     * This endpoint takes care of running a query in the language model that takes as input a natural language query
     * and outputs the result from the appropriate tabular data source.
     */
    protected String textToTableEndpoint;

    /**
     * The HTTP status code that indicates a success in the request.
     */
    protected static final int HTTP_STATUS_OK = 200;

    /**
     * Instantiates a new {@link NLPServerClient}.
     * <p>
     * The attributes of the instance are loaded from a resources file {@code bot.properties}
     */
    public NLPServerClient() {
        Configuration configuration;
        Configurations configurations = new Configurations();
        try {
            configuration = configurations.properties(Thread.currentThread().getContextClassLoader()
                    .getResource(Bot.botPropertiesFile));
            this.serverUrl = "http://" + configuration.getString("SERVER_URL") + "/";
            this.textToTableEndpoint = configuration.getString("TEXT_TO_TABLE_ENDPOINT");
        } catch (ConfigurationException e) {
            e.printStackTrace();
            System.out.println("Configuration file not found");
        }
    }

    /**
     * Gets the response from the server endpoint.
     *
     * @param input the input of the server
     * @return the server response
     * @throws UnirestException the unirest exception
     */
    private JSONObject getResponse(String input) throws UnirestException {
        JSONObject request = new JSONObject();
        request.put("input", input);
        request.put("language", Bot.language);
        request.put("fields", (Collection<?>) null);
        request.put("filters", (Collection<?>) null);
        request.put("ignoreCase", true);
        return Unirest.post(serverUrl + textToTableEndpoint)
                .header("Content-Type", "application/json")
                .body(request).asJson().getBody().getObject();
    }

    /**
     * Makes a query to the server and obtains the translations of the server input.
     *
     * @param input the input of the server
     * @return if successful, a map containing the language-translation entries, otherwise null
     */
    public Map<String, String> getTranslations(String input) {
        try {
            JSONObject response = getResponse(input);
            String sqlQuery = response.getString("sql");
            String inputInEnglish = response.getString("input_en");
            Map<String, String> translations = new HashMap<>();
            translations.put("sql", sqlQuery);
            translations.put("english", inputInEnglish);
            return translations;
        } catch (Exception e) {
            Log.error(e, "An error occurred while getting the SQL result, see the attached exception");
            return null;
        }
    }

    /**
     * Makes a query to the server and obtains a {@link bodi.generator.dataSource.ResultSet} containing the response.
     *
     * @param input the input of the server
     * @return if successful, the {@link bodi.generator.dataSource.ResultSet} containing the result of the server,
     * otherwise an empty {@link bodi.generator.dataSource.ResultSet}.
     */
    public ResultSet runQuery(String input) {
        try {
            JSONObject response = getResponse(input);
            String sqlQuery = response.getString("sql");
            JSONArray headerJson = response.getJSONArray("header");
            JSONArray tableJson = response.getJSONArray("table");
            if (isEmpty(sqlQuery)) {
                Log.info("Sorry, query text could not be translated to SQL statement");
                return new ResultSet();
            }
            Log.info("Query text translated to SQL statement: {0}", sqlQuery);
            List<String> header = new ArrayList<>();
            for (int i = 0; i < headerJson.length(); i++) {
                header.add(headerJson.getString(i));
            }
            List<Row> table = new ArrayList<>();
            for (int i = 0; i < tableJson.length(); i++) {
                JSONArray rowJson = tableJson.getJSONArray(i);
                List<String> values = new ArrayList<>();
                for (int j = 0; j < rowJson.length(); j++) {
                    if (rowJson.isNull(j)) {
                        values.add("");
                    } else {
                        values.add(rowJson.get(j).toString());
                    }
                }
                table.add(new Row(values));
            }
            checkCorrectAnswer.setLastSqlQuery(sqlQuery);
            return new ResultSet(header, table);
        } catch (Exception e) {
            Log.error(e, "An error occurred while getting the SQL result, see the attached exception");
        }
        return new ResultSet();
    }
}
