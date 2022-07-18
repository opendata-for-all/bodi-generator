package com.xatkit.bot.sql;

import bodi.generator.dataSource.Row;
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
 * The SQL Engine of a chatbot.
 * <p>
 * It is the responsible for generating and executing SQL queries in a database made up of csv files.
 */
public class SqlEngine {

    /**
     * The url of the local database.
     */
    private final String url = "jdbc:drill:zk=local";

    /**
     * The Statement used to execute SQL queries in a given database.
     */
    private Statement statement;

    /**
     * The system that generates the SQL queries.
     */
    public SqlQueries queries;

    /**
     * Instantiates a new {@link SqlEngine}
     * <p>
     * It connects to a local database made up of the {@code .csv} file(s) stored in the resources folder.
     */
    public SqlEngine(String tableName, char delimiter) {
        queries = new SqlQueries(tableName, delimiter);
        try {
            Connection conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            String sqlQuery = queries.selectAll();
            bodi.generator.dataSource.ResultSet resultSet = this.runSqlQuery(sqlQuery);
            queries.getAllFields().addAll(resultSet.getHeader());
        } catch (SQLException e) {
            Log.error(e, "An error occurred while connecting to {0}, see the attached exception", url);
        }
    }

    /**
     * Executes an SQL query.
     *
     * @param sqlQuery the sql query
     * @return if successful, the {@link bodi.generator.dataSource.ResultSet} containing the result of the SQL query,
     * otherwise {@code null}.
     */
    public bodi.generator.dataSource.ResultSet runSqlQuery(String sqlQuery) {
        if (isEmpty(sqlQuery)) {
            return new bodi.generator.dataSource.ResultSet();
        }
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
        return new bodi.generator.dataSource.ResultSet();
    }
}
