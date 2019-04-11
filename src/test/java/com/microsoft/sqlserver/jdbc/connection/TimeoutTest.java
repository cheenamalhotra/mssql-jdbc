/*
 * Microsoft JDBC Driver for SQL Server Copyright(c) Microsoft Corporation All rights reserved. This program is made
 * available under the terms of the MIT License. See the LICENSE file in the project root for more information.
 */
package com.microsoft.sqlserver.jdbc.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.RandomUtil;
import com.microsoft.sqlserver.jdbc.SQLServerStatement;
import com.microsoft.sqlserver.jdbc.TestResource;
import com.microsoft.sqlserver.jdbc.TestUtils;
import com.microsoft.sqlserver.testframework.AbstractSQLGenerator;
import com.microsoft.sqlserver.testframework.AbstractTest;
import com.microsoft.sqlserver.testframework.Constants;
import com.microsoft.sqlserver.testframework.PrepUtil;


@RunWith(JUnitPlatform.class)
public class TimeoutTest extends AbstractTest {
    String randomServer = RandomUtil.getIdentifier("Server");
    static String waitForDelaySPName = RandomUtil.getIdentifier("waitForDelaySP");
    final int waitForDelaySeconds = 10;

    @Test
    @Tag("slow")
    public void testDefaultLoginTimeout() {
        long timerStart = 0;
        long timerEnd = 0;

        timerStart = System.currentTimeMillis();
        // Try a non existing server and see if the default timeout is 15 seconds
        try (Connection con = PrepUtil.getConnection("jdbc:sqlserver://" + randomServer + ";user=sa;password=pwd;")) {

        } catch (Exception e) {
            assertTrue(e.getMessage().contains(TestResource.getResource("R_tcpipConnectionToHost")));
            timerEnd = System.currentTimeMillis();
        }

        assertTrue(0 != timerEnd, TestResource.getResource("R_shouldNotConnect"));

        long timeDiff = timerEnd - timerStart;
        assertTrue(timeDiff > 14000);
    }

    @Test
    public void testFailoverInstanceResolution() throws SQLException {
        long timerStart = 0;
        long timerEnd = 0;

        timerStart = System.currentTimeMillis();
        // Try a non existing server and see if the default timeout is 15 seconds
        try (Connection con = PrepUtil
                .getConnection("jdbc:sqlserver://" + randomServer + ";databaseName=FailoverDB_abc;failoverPartner="
                        + randomServer + "\\foo;user=sa;password=pwd;")) {} catch (Exception e) {
            assertTrue(e.getMessage().contains(TestResource.getResource("R_tcpipConnectionToHost")));
            timerEnd = System.currentTimeMillis();
        }
        assertTrue(0 != timerEnd, TestResource.getResource("R_shouldNotConnect"));

        long timeDiff = timerEnd - timerStart;
        assertTrue(timeDiff > 14000);
    }

    @Test
    public void testFOInstanceResolution2() throws SQLException {
        long timerStart = 0;
        long timerEnd = 0;

        timerStart = System.currentTimeMillis();
        try (Connection con = PrepUtil
                .getConnection("jdbc:sqlserver://" + randomServer + "\\fooggg;databaseName=FailoverDB;failoverPartner="
                        + randomServer + "\\foo;user=sa;password=pwd;")) {} catch (Exception e) {
            timerEnd = System.currentTimeMillis();
        }
        assertTrue(0 != timerEnd, TestResource.getResource("R_shouldNotConnect"));

        long timeDiff = timerEnd - timerStart;
        assertTrue(timeDiff > 14000);
    }

    /**
     * When query timeout occurs, the connection is still usable.
     * 
     * @throws Exception
     */
    @Test
    @Tag(Constants.xAzureSQLDW)
    public void testQueryTimeout() throws Exception {
        try (Connection conn = getConnection()) {
            dropWaitForDelayProcedure(conn);
            createWaitForDelayPreocedure(conn);
        }

        try (Connection conn = PrepUtil.getConnection(
                connectionString + ";queryTimeout=" + (waitForDelaySeconds / 2) + Constants.SEMI_COLON)) {

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("exec " + AbstractSQLGenerator.escapeIdentifier(waitForDelaySPName));
                fail(TestResource.getResource("R_expectedExceptionNotThrown"));
            } catch (Exception e) {
                if (!(e instanceof java.sql.SQLTimeoutException)) {
                    throw e;
                }
                assertEquals(e.getMessage(), TestResource.getResource("R_queryTimedOut"),
                        TestResource.getResource("R_invalidExceptionMessage"));
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT @@version");
            } catch (Exception e) {
                fail(TestResource.getResource("R_unexpectedErrorMessage") + e.getMessage());
            }
        } catch (Exception e) {
            fail(TestResource.getResource("R_unexpectedErrorMessage") + e.getMessage());
        }
    }

    /**
     * Tests sanity of connection property.
     * 
     * @throws Exception
     */
    @Test
    @Tag(Constants.xAzureSQLDW)
    public void testCancelQueryTimeout() throws Exception {
        try (Connection conn = getConnection()) {
            dropWaitForDelayProcedure(conn);
            createWaitForDelayPreocedure(conn);
        }

        try (Connection conn = PrepUtil.getConnection(connectionString + ";queryTimeout=" + (waitForDelaySeconds / 2)
                + ";cancelQueryTimeout=" + waitForDelaySeconds + Constants.SEMI_COLON)) {

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("exec " + AbstractSQLGenerator.escapeIdentifier(waitForDelaySPName));
                fail(TestResource.getResource("R_expectedExceptionNotThrown"));
            } catch (Exception e) {
                if (!(e instanceof java.sql.SQLTimeoutException)) {
                    fail(TestResource.getResource("R_expectedExceptionNotThrown"));
                }
                assertEquals(e.getMessage(), TestResource.getResource("R_queryTimedOut"),
                        TestResource.getResource("R_invalidExceptionMessage"));
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT @@version");
            } catch (Exception e) {
                fail(TestResource.getResource("R_unexpectedErrorMessage") + e.getMessage());
            }
        } catch (Exception e) {
            fail(TestResource.getResource("R_unexpectedErrorMessage") + e.getMessage());
        }
    }

    /**
     * Tests sanity of connection property.
     * 
     * @throws Exception
     */
    @Test
    @Tag(Constants.xAzureSQLDW)
    public void testCancelQueryTimeoutOnStatement() throws Exception {
        try (Connection conn = getConnection()) {
            dropWaitForDelayProcedure(conn);
            createWaitForDelayPreocedure(conn);
        }

        try (Connection conn = PrepUtil.getConnection(connectionString + Constants.SEMI_COLON)) {

            try (SQLServerStatement stmt = (SQLServerStatement) conn.createStatement()) {
                stmt.setQueryTimeout(waitForDelaySeconds / 2);
                stmt.setCancelQueryTimeout(waitForDelaySeconds);
                stmt.execute("exec " + AbstractSQLGenerator.escapeIdentifier(waitForDelaySPName));
                fail(TestResource.getResource("R_expectedExceptionNotThrown"));
            } catch (Exception e) {
                if (!(e instanceof java.sql.SQLTimeoutException)) {
                    fail(TestResource.getResource("R_expectedExceptionNotThrown"));
                }
                assertEquals(e.getMessage(), TestResource.getResource("R_queryTimedOut"),
                        TestResource.getResource("R_invalidExceptionMessage"));
            }

            try (SQLServerStatement stmt = (SQLServerStatement) conn.createStatement()) {
                stmt.execute("SELECT @@version");
            } catch (Exception e) {
                fail(TestResource.getResource("R_unexpectedErrorMessage") + e.getMessage());
            }
        } catch (Exception e) {
            fail(TestResource.getResource("R_unexpectedErrorMessage") + e.getMessage());
        }
    }

    /**
     * When socketTimeout occurs, the connection will be marked as closed.
     * 
     * @throws Exception
     */
    @Test
    @Tag(Constants.xAzureSQLDW)
    public void testSocketTimeout() throws Exception {
        try (Connection conn = getConnection()) {
            dropWaitForDelayProcedure(conn);
            createWaitForDelayPreocedure(conn);
        }

        try (Connection conn = PrepUtil.getConnection(
                connectionString + ";socketTimeout=" + (waitForDelaySeconds * 1000 / 2) + Constants.SEMI_COLON)) {

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("exec " + AbstractSQLGenerator.escapeIdentifier(waitForDelaySPName));
                fail(TestResource.getResource("R_expectedExceptionNotThrown"));
            } catch (Exception e) {
                if (!(e instanceof SQLException)) {
                    throw e;
                }
                assertEquals(e.getMessage(), TestResource.getResource("R_readTimedOut"),
                        TestResource.getResource("R_invalidExceptionMessage"));
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT @@version");
            } catch (SQLException e) {
                assertEquals(e.getMessage(), TestResource.getResource("R_connectionIsClosed"),
                        TestResource.getResource("R_invalidExceptionMessage"));
            }
        } catch (Exception e) {
            fail(TestResource.getResource("R_unexpectedErrorMessage") + e.getMessage());
        }
    }

    private static void dropWaitForDelayProcedure(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            TestUtils.dropProcedureIfExists(AbstractSQLGenerator.escapeIdentifier(waitForDelaySPName), stmt);
        } catch (Exception e) {
            fail(TestResource.getResource("R_unexpectedErrorMessage") + e.getMessage());
        }
    }

    private void createWaitForDelayPreocedure(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE PROCEDURE " + AbstractSQLGenerator.escapeIdentifier(waitForDelaySPName) + " AS"
                    + " BEGIN" + " WAITFOR DELAY '00:00:" + waitForDelaySeconds + "';" + " END";
            stmt.execute(sql);
        } catch (Exception e) {
            fail(TestResource.getResource("R_unexpectedErrorMessage") + e.getMessage());
        }
    }

    @AfterAll
    public static void cleanup() throws SQLException {
        try (Connection conn = getConnection()) {
            dropWaitForDelayProcedure(conn);
        }
    }
}
