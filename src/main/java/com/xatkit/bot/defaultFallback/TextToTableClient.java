package com.xatkit.bot.defaultFallback;

import bodi.generator.dataSource.ResultSet;
import bodi.generator.dataSource.Row;
import bodi.generator.dataSource.Statement;
import fr.inria.atlanmod.commons.log.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * This class is a client to make requests to a language model running in a server.
 * <p>
 * This class is used when the language model loaded is of the type Text-to-Table. That is, the input of the language
 * model must be a natural language query, and the expected output is a tabular result.
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
     * @return if successful, the {@link bodi.generator.dataSource.ResultSet} containing the result of the language
     * model, otherwise {@code null}.
     */
    public ResultSet runTableQuery(String input, Statement statement) {
        JSONObject response = runQuery(input, statement);
        try {
            String sqlQuery = response.getString("sql");
            JSONArray headerJson = response.getJSONArray("header");
            JSONArray tableJson = response.getJSONArray("table");
            if (isEmpty(sqlQuery)) {
                Log.info("Sorry, query text could not be translated to SQL statement");
                return null;
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
            e.printStackTrace();
            return null;
        }
    }
}
