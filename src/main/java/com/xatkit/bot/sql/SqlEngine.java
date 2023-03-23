package com.xatkit.bot.sql;

import com.xatkit.bot.library.Row;
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

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The SQL Engine of a chatbot.
 * <p>
 * It is the responsible for executing SQL queries in a database.
 */
public class SqlEngine {

    /**
     * The url of the database.
     */
    private final String url = "jdbc:drill:drillbit=localhost";

    /**
     * The driver connection.
     */
    private Connection conn;

    /**
     * The Statement used to execute SQL queries in a given database.
     */
    private Statement statement;

    /**
     * Instantiates a new {@link SqlEngine}
     * <p>
     * It connects to a local database made up of the {@code .csv} file(s) stored in the resources folder.
     */
    public SqlEngine() {
        try {
            Class.forName("org.apache.drill.jdbc.Driver");
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            Log.error("An error occurred while connecting to {0}, see the attached exception", url);
            Log.error(e.getMessage());
        }
    }

    /**
     * Executes an SQL query.
     *
     * @param sqlQuery the sql query
     * @param bot      the chatbot
     * @return if successful, the {@link com.xatkit.bot.library.ResultSet} containing the result of the SQL query,
     * otherwise {@code null}.
     */
    public com.xatkit.bot.library.ResultSet runSqlQuery(Bot bot, String sqlQuery) {
        if (isEmpty(sqlQuery)) {
            return new com.xatkit.bot.library.ResultSet();
        }
        Log.info("Trying to run the SQL query: {0}", sqlQuery);
        try {
            if (isNull(conn) || conn.isClosed()) {
                Log.warn("The drillbit connection was lost. Trying to establish connection again.");
                conn = DriverManager.getConnection(url);
                statement = conn.createStatement();
            }
            if (!conn.isClosed()) {
                ResultSet resultSet = statement.executeQuery(sqlQuery);
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int numColumns = resultSetMetaData.getColumnCount();
                List<String> header = new ArrayList<>();
                List<Row> table = new ArrayList<>();
                for (int i = 1; i <= numColumns; i++) {
                    String originalName = resultSetMetaData.getColumnLabel(i);
                    String readableName = bot.entities.readableNames.get(originalName);
                    if (!isEmpty(readableName)) {
                        header.add(readableName);
                    } else {
                        header.add(originalName);
                    }
                }
                while (resultSet.next()) {
                    List<String> values = new ArrayList<>();
                    for (int i = 1; i <= numColumns; i++) {
                        values.add(resultSet.getString(i));
                    }
                    table.add(new Row(values));
                }
                return new com.xatkit.bot.library.ResultSet(header, table);
            } else {
                Log.error("An error occurred while reconnecting to {0}, see the attached exception", url);
            }
        } catch (SQLException e) {
            Log.error("An error occurred while running the SQL query {0}, see the attached exception", sqlQuery);
            Log.error(e.getMessage());
        }
        return new com.xatkit.bot.library.ResultSet();
    }
}
