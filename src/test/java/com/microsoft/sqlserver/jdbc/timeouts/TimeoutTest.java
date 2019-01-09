/*
 * Microsoft JDBC Driver for SQL Server Copyright(c) Microsoft Corporation All rights reserved. This program is made
 * available under the terms of the MIT License. See the LICENSE file in the project root for more information.
 */

package com.microsoft.sqlserver.jdbc.timeouts;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerStatement;
import com.microsoft.sqlserver.testframework.AbstractTest;


@RunWith(JUnitPlatform.class)
public class TimeoutTest extends AbstractTest {
    private String SHARED_TIMER_THREAD = "mssql-jdbc-shared-timer-core-";

    @Test
    public void testBasicQueryTimeout() {
        boolean exceptionThrown = false;
        try {
            // wait 1 minute and timeout after 10 seconds
            Assert.assertTrue("Select succeeded", runQuery("WAITFOR DELAY '00:01'", 10));
        } catch (SQLException e) {
            exceptionThrown = true;
            Assert.assertTrue("Timeout exception not thrown", e.getClass().equals(SQLTimeoutException.class));
        }
        Assert.assertTrue("A SQLTimeoutException was expected", exceptionThrown);
    }

    @Test
    public void testQueryTimeoutValid() {
        boolean exceptionThrown = false;
        int timeoutInSeconds = 10;
        long start = System.currentTimeMillis();
        try {
            // wait 1 minute and timeout after 10 seconds
            Assert.assertTrue("Select succeeded", runQuery("WAITFOR DELAY '00:01'", timeoutInSeconds));
        } catch (SQLException e) {
            int secondsElapsed = (int) ((System.currentTimeMillis() - start) / 1000);
            Assert.assertTrue("Query did not timeout expected, elapsedTime=" + secondsElapsed,
                    secondsElapsed >= timeoutInSeconds);
            exceptionThrown = true;
            Assert.assertTrue("Timeout exception not thrown", e.getClass().equals(SQLTimeoutException.class));
        }
        Assert.assertTrue("A SQLTimeoutException was expected", exceptionThrown);
    }

    @Test
    /**
     * This test will fail if any other JUnit test leaks any connection
     * 
     * @throws SQLException
     * @throws InterruptedException
     */
    public void testSharedTimerThread() throws SQLException, InterruptedException {
        if (null != connection) {
            connection.close();
        }

        Thread.sleep(500); // wait some time
        // Shared Timer Shutdown
        assertFalse(isSingleSharedTimerRunning());

        try (Connection connection = DriverManager.getConnection(connectionString)) {
            for (int i = 0; i < 100; i++) {
                try (SQLServerStatement statement = (SQLServerStatement) connection.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.execute("SELECT 1");
                    // Shared Timer instance started
                    assertTrue(isSingleSharedTimerRunning());
                }
            }

            try (Connection connection1 = DriverManager.getConnection(connectionString)) {
                for (int i = 0; i < 100; i++) {
                    try (SQLServerStatement statement = (SQLServerStatement) connection1.createStatement()) {
                        statement.execute("SELECT 1");
                        // No timeout used, still daemon thread is active
                        assertTrue(isSingleSharedTimerRunning());
                    }
                }
            }

            // Closed 1 connection - still daemon thread is active in background
            assertTrue(isSingleSharedTimerRunning());
            for (int i = 0; i < 100; i++) {
                try (SQLServerStatement statement = (SQLServerStatement) connection.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.execute("SELECT 1");
                    assertTrue(isSingleSharedTimerRunning());
                }
            }
        }
        Thread.sleep(500); // wait some time
        // Shared Timer Shutdown
        assertFalse(isSingleSharedTimerRunning());

        // Connection created again
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            try (SQLServerStatement statement = (SQLServerStatement) connection.createStatement()) {
                statement.execute("SELECT 1");
                // Daemon thread is not yet activated
                assertFalse(isSingleSharedTimerRunning());
            }

            for (int i = 0; i < 100; i++) {
                try (SQLServerStatement statement = (SQLServerStatement) connection.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.execute("SELECT 1");
                    // Shared Timer instance started
                    assertTrue(isSingleSharedTimerRunning());
                }
            }
        }

        Thread.sleep(500); // wait some time
        // Shared Timer Shutdown
        assertFalse(isSingleSharedTimerRunning());

        // Try with DataSource
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setURL(connectionString);
        try (Connection con = ds.getConnection()) {
            assertFalse(isSingleSharedTimerRunning());
            for (int i = 0; i < 100; i++) {
                try (SQLServerStatement statement = (SQLServerStatement) con.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.execute("SELECT 1");
                    // Shared Timer instance started
                    assertTrue(isSingleSharedTimerRunning());
                }
            }
        }

        Thread.sleep(500); // wait some time
        // Shared Timer Shutdown
        assertFalse(isSingleSharedTimerRunning());

        // Try with DataSource
        SQLServerConnectionPoolDataSource dsPool = new SQLServerConnectionPoolDataSource();
        dsPool.setURL(connectionString);
        try (Connection con = dsPool.getConnection()) {
            assertFalse(isSingleSharedTimerRunning());
            for (int i = 0; i < 100; i++) {
                try (SQLServerStatement statement = (SQLServerStatement) con.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.execute("SELECT 1");
                    // Shared Timer instance started
                    assertTrue(isSingleSharedTimerRunning());
                }
            }
        }

        Thread.sleep(500); // wait some time
        // Shared Timer Shutdown
        assertFalse(isSingleSharedTimerRunning());
    }

    private boolean runQuery(String query, int timeout) throws SQLException {
        try (Connection con = DriverManager.getConnection(connectionString);
                PreparedStatement preparedStatement = con.prepareStatement(query)) {
            // set provided timeout
            preparedStatement.setQueryTimeout(timeout);
            return preparedStatement.execute();
        }
    }

    private boolean isSingleSharedTimerRunning() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        int count = 0;
        for (Thread thread : threadSet) {
            if (thread.getName().startsWith(SHARED_TIMER_THREAD)) {
                count++;
            }
        }
        return count == 1;
    }
}
