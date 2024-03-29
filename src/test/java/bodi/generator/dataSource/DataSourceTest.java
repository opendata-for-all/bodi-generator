package bodi.generator.dataSource;

import com.xatkit.bot.sql.SqlEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataSourceTest{

    private SqlEngine sql;

    @BeforeEach
    public void setUp() {
        //this.sql = new SqlEngine("odata_poblacio_nacionalitat_genere.csv", ',');
    }

    /**
     * Test that TabularDataSource is loaded properly.
     */
    @Test
    void testInitialization() {
        /*
        assertEquals(1113, this.tds.getNumRows());
        assertEquals(7, this.tds.getNumColumns());
        assertEquals(7, this.tds.getHeaderCopy().size());
         */
    }

    /**
     * Test:
     * <p>
     * - The table of the ResultSet is deeply copied, that is, the Rows of the table are deeply copied (i.e. they
     *    are not references to the Rows of the TabularDataSource)
     * <p>
     * - The Values List of each ResultSet Row is also a deep copy of the original TabularDataSource Row's Values List
     * <p>
     * - The structure and content of the ResultSet table is the same as the TabularDataSource's one (numColumns,
     *    numRows, header, row values for each row) given that there is no filter of field selection applied to the
     *    Statement
     */
    @Test
    void testResultSetDeepCopy() {
        /*
        String sqlQuery = sql.queries.selectAll();
        ResultSet resultSet = sql.runSqlQuery(sqlQuery);
        assertEquals(resultSet.getNumRows(), tds.getNumRows());
        assertEquals(resultSet.getNumColumns(), tds.getNumColumns());
        assertNotSame(resultSet.getHeader(), tds.getHeaderCopy());
        for (int i = 0; i < tds.getNumRows(); i++) {
            assertNotSame(tds.getRow(i), resultSet.getRow(i));
            assertEquals(tds.getRow(i).getValues(), resultSet.getRow(i).getValues());
            assertNotSame(tds.getRow(i).getValues(), resultSet.getRow(i).getValues());
        }
         */
    }


    /*
     * Test that the filters and field selections are properly added to the statement, and therefore the ResultSet
     * produced by the Statement has the correct structure and values
     */
    /*
    @Test
    void testResultSetFilteredAndFieldsSubset() {
        // 1. Apply a filter and field selections
        statement
                .addFilter("DESC_NACIONALITAT", "equals", "Xina")
                .addField("DONES")
                .addField("HOMES")
                .addField("DESC_NACIONALITAT");
        ResultSet resultSet1 = (ResultSet) statement.executeQuery(Operation.NO_OPERATION);
        assertEquals(3, resultSet1.getNumColumns());
        assertEquals(Arrays.asList("DESC_NACIONALITAT", "HOMES", "DONES"), resultSet1.getHeader());
        assertEquals(27, resultSet1.getNumRows());
        for (int i = 0; i < resultSet1.getNumRows(); i++) {
            assertEquals("Xina", resultSet1.getRow(i).getColumnValue(0));
        }
        // 2. Check that the new filter is applied but the previous filter is maintained
        statement
                .addFilter("HOMES", ">", "10");
        ResultSet resultSet2 = (ResultSet) statement.executeQuery(Operation.NO_OPERATION);
        assertEquals(3, resultSet2.getNumColumns());
        assertEquals(Arrays.asList("DESC_NACIONALITAT", "HOMES", "DONES"), resultSet2.getHeader());
        assertEquals(12, resultSet2.getNumRows());
        for (int i = 0; i < resultSet2.getNumRows(); i++) {
            assertEquals("Xina", resultSet2.getRow(i).getColumnValue(0));
            assertTrue(Integer.parseInt(resultSet2.getRow(i).getColumnValue(1)) > 10);
        }
        // 3. A field cannot be equal to two different values, so numRows should be 0
        statement
                .addFilter("DESC_NACIONALITAT", "equals", "Espanya");
        ResultSet resultSet3 = (ResultSet) statement.executeQuery(Operation.NO_OPERATION);
        assertEquals(0, resultSet3.getNumRows());
    }
    */

    /**
     * Test that duplicated filters are not added to a statement
     */
    /*
    @Test
    void testDuplicatedFiltersInStatement() {
        sql.queries.addFilter("DESC_NACIONALITAT", "equals", "Xina");
        sql.queries.addFilter("DESC_NACIONALITAT", "equals", "Xina");
        assertEquals(1, sql.queries.getFiltersAsStrings().size());
    }
     */

    /*
     * Test that duplicated fields are not added to a statement
     */
    /*
    @Test
    void testDuplicatedFieldsInStatement() {
        statement
                .addField("HOMES")
                .addField("HOMES");
        assertEquals(1, statement.getNumFields());
    }
     */

    /**
     * Test that the {@code ignoreCase} feature of a Statement works. That is, a filter can match despite the
     * upper/lower cases
     */
    /*
    @Test
    void testIgnoreCaseFiltering() {
        sql.queries.addFilter("DESC_NACIONALITAT", "equals", "XINA");
        String sqlQuery = sql.queries.selectAll();
        ResultSet resultSet1 = sql.runSqlQuery(sqlQuery);
        assertEquals(27, resultSet1.getNumRows());
        for (int i = 0; i < resultSet1.getNumRows(); i++) {
            assertEquals("Xina", resultSet1.getRow(i).getColumnValue(2));
        }
    }
     */
}
