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
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import com.microsoft.sqlserver.jdbc.SQLServerStatement;
import com.microsoft.sqlserver.testframework.AbstractTest;


@RunWith(JUnitPlatform.class)
public class TimeoutTest extends AbstractTest {
    private String SQL_SERVER_TIMEOUT_THREAD = "com.microsoft.sqlserver.jdbc.TimeoutPoller";
    
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
     * @throws SQLException
     * @throws InterruptedException
     */
    public void testTimeoutDaemonThread() throws SQLException, InterruptedException {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            for (int i = 0; i < 100; i++) {
                try (SQLServerStatement statement = (SQLServerStatement) connection.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.execute("SELECT 1");
                    // Daemon thread is activated
                    assertTrue(isTimeoutThreadRunning());
                }
            }

            try (Connection connection1 = DriverManager.getConnection(connectionString)) {
                for (int i = 0; i < 100; i++) {
                    try (SQLServerStatement statement = (SQLServerStatement) connection1.createStatement()) {
                        statement.execute("SELECT 1");
                        // No timeout used, still daemon thread is active
                        assertTrue(isTimeoutThreadRunning());
                    }
                }
            }

            // Closed 1 connection - still daemon thread is active in background
            assertTrue(isTimeoutThreadRunning());
            for (int i = 0; i < 100; i++) {
                try (SQLServerStatement statement = (SQLServerStatement) connection.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.execute("SELECT 1");
                    assertTrue(isTimeoutThreadRunning());
                }
            }
        }
        Thread.sleep(500); // wait some time
        // Daemon thread killed
        assertFalse(isTimeoutThreadRunning());

        // Connection created again
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            try (SQLServerStatement statement = (SQLServerStatement) connection.createStatement()) {
                statement.execute("SELECT 1");
                // Daemon thread is not yet activated
                assertFalse(isTimeoutThreadRunning());
            }

            for (int i = 0; i < 100; i++) {
                try (SQLServerStatement statement = (SQLServerStatement) connection.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.execute("SELECT 1");
                    // Daemon thread is activated
                    assertTrue(isTimeoutThreadRunning());
                }
            }
        }

        Thread.sleep(500); // wait some time
        // Daemon thread killed
        assertFalse(isTimeoutThreadRunning());

        // Try with DataSource
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setURL(connectionString);
        try (Connection con = ds.getConnection()) {
            assertFalse(isTimeoutThreadRunning());
            for (int i = 0; i < 100; i++) {
                try (SQLServerStatement statement = (SQLServerStatement) con.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.execute("SELECT 1");
                    // Daemon thread is activated
                    assertTrue(isTimeoutThreadRunning());
                }
            }
        }

        Thread.sleep(500); // wait some time
        // Daemon thread killed
        assertFalse(isTimeoutThreadRunning());

        // Try with DataSource
        SQLServerConnectionPoolDataSource dsPool = new SQLServerConnectionPoolDataSource();
        dsPool.setURL(connectionString);
        try (Connection con = dsPool.getConnection()) {
            assertFalse(isTimeoutThreadRunning());
            for (int i = 0; i < 100; i++) {
                try (SQLServerStatement statement = (SQLServerStatement) con.createStatement()) {
                    statement.setQueryTimeout(10);
                    statement.execute("SELECT 1");
                    // Daemon thread is activated
                    assertTrue(isTimeoutThreadRunning());
                }
            }
        }

        Thread.sleep(500); // wait some time
        // Daemon thread killed
        assertFalse(isTimeoutThreadRunning());
    }

    private boolean runQuery(String query, int timeout) throws SQLException {
        try (Connection con = DriverManager.getConnection(connectionString);
                PreparedStatement preparedStatement = con.prepareStatement(query)) {
            // set provided timeout
            preparedStatement.setQueryTimeout(timeout);
            return preparedStatement.execute();
        }
    }

    private boolean isTimeoutThreadRunning() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread thread : threadSet) {
            if (thread.getName().equalsIgnoreCase(SQL_SERVER_TIMEOUT_THREAD)) {
                return true;
            }
        }
        return false;
    }
}
