package com.xatkit.bot.defaultFallback;

import bodi.generator.dataSource.ResultSet;
import bodi.generator.dataSource.Row;
import fr.inria.atlanmod.commons.log.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    public ResultSet runTableQuery(String input) {
        JSONObject response = runQuery(input);
        Log.info("Query text translated to SQL statement: {0}", response.getString("sql"));
        JSONArray header_json = response.getJSONArray("header");
        JSONArray table_json = response.getJSONArray("table");
        List<String> header = new ArrayList<>();
        for (int i = 0; i < header_json.length(); i++) {
            header.add(header_json.getString(i));
        }
        List<Row> table = new ArrayList<>();
        for (int i = 0; i < table_json.length(); i++) {
            JSONArray row_json = table_json.getJSONArray(i);
            List<String> values = new ArrayList<>();
            for (int j = 0; j < row_json.length(); j++) {
                values.add(row_json.get(j).toString());
            }
            table.add(new Row(values));
        }
        return new ResultSet(header, table);
    }
}
