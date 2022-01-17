package com.xatkit.bot.defaultFallback;

import bodi.generator.dataSource.Row;
import com.xatkit.bot.Bot;
import fr.inria.atlanmod.commons.log.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * This class is a client to make requests to a language model running in a server.
 * <p>
 * This class is used when the language model loaded is of the type Text-to-SQL. That is, the input of the language
 * model must be a natural language query and the expected output is a SQL statement equivalent to the input.
 */
public class TextToSQLClient extends DefaultFallbackNLPClient {

    /**
     * The Statement used to execute SQL queries in a given database.
     */
    private Statement statement;

    /**
     * Instantiates a new {@link TextToSQLClient}
     * <p>
     * It connects to a local database made up of the {@code .csv} file(s) stored in the resources folder.
     */
    public TextToSQLClient() {
        super();
        String url = "jdbc:relique:csv:" + "src/main/resources" + "?" + "fileExtension=.csv";
        try {
            Connection conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
        } catch (SQLException e) {
            Log.error(e, "An error occurred while connecting to {0}, see the attached exception", url);
        }
    }

    /**
     * Makes a query to the language model.
     *
     * @param input the input of the language model
     * @return if successful, the {@link bodi.generator.dataSource.ResultSet} containing the result of the SQL query,
     * otherwise {@code null}.
     */
    public bodi.generator.dataSource.ResultSet runSqlQuery(String input) {
        String sqlQuery = runQuery(input);
        if (isEmpty(sqlQuery)) {
            return null;
        }
        sqlQuery = setTableName(sqlQuery);
        Log.info("Trying to run the SQL query: {0}", sqlQuery);
        try {
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int numColumns = resultSetMetaData.getColumnCount();
            List<String> header = new ArrayList<>();
            List<Row> table = new ArrayList<>();
            for (int i = 1; i <= numColumns; i++) {
                header.add(resultSetMetaData.getColumnLabel(i));
            }
            while (resultSet.next()) {
                List<String> values = new ArrayList<>();
                for (int i = 1; i <= numColumns; i++) {
                    values.add(resultSet.getString(i));
                }
                table.add(new Row(values));
            }
           return new bodi.generator.dataSource.ResultSet(header, table);
        } catch (SQLException e) {
            Log.error(e, "An error occurred while running the SQL query {0}, see the attached exception", sqlQuery);
        }
        return null;
    }

    /**
     * Sets the table name of an SQL query, according to the {@link Bot#inputDoc} available resource.
     *
     * @param sqlQuery the SQL query
     * @return the SQL query with the table name updated
     */
    private String setTableName(String sqlQuery) {
        String tableName = Bot.inputDoc.substring(0, Bot.inputDoc.length() - 4);
        return sqlQuery.replaceFirst("FROM table", "FROM " + tableName);
    }
}































