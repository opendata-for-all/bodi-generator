package com.xatkit.bot.nlp;

import bodi.generator.dataSource.ResultSet;
import bodi.generator.dataSource.Row;
import bodi.generator.dataSource.Statement;
import com.mashape.unirest.http.Unirest;
import com.xatkit.bot.Bot;
import fr.inria.atlanmod.commons.log.Log;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
     * Makes a query to the server.
     *
     * @param input the input of the server
     * @return if successful, the {@link bodi.generator.dataSource.ResultSet} containing the result of the server,
     * otherwise an empty {@link bodi.generator.dataSource.ResultSet}.
     */
    public ResultSet runQuery(String input, Statement statement) {
        JSONObject request = new JSONObject();
        request.put("input", input);
        request.put("language", Bot.language);
        request.put("fields", statement.getFieldsAsSqlVariables());
        request.put("filters", statement.getFiltersAsSqlConditions());
        request.put("ignoreCase", statement.isIgnoreCaseFilterValue());
        try {
            JSONObject response = Unirest.post(serverUrl + textToTableEndpoint)
                    .header("Content-Type", "application/json")
                    .body(request).asJson().getBody().getObject();
            // assert response.getStatus() == HTTP_STATUS_OK; // COMPROBARRRRR
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
            return new ResultSet(header, table);
        } catch (Exception e) {
            Log.error(e, "An error occurred while getting the SQL result, see the attached exception");
            return new ResultSet();
        }
    }
}
