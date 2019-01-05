/*
 * Microsoft JDBC Driver for SQL Server Copyright(c) Microsoft Corporation All rights reserved. This program is made
 * available under the terms of the MIT License. See the LICENSE file in the project root for more information.
 */
package com.microsoft.sqlserver.jdbc;

import java.util.logging.Level;


/**
 * The tds default implementation of a timeout command
 */
class TdsCommandTimeoutTask extends AbstractTimeoutTask {
    private final TDSCommand command;
    private final SQLServerConnection sqlServerConnection;

    public TdsCommandTimeoutTask(TDSCommand command, SQLServerConnection sqlServerConnection) {
        super(sqlServerConnection == null ? null : sqlServerConnection.getClientConIdInternal());
        this.command = command;
        this.sqlServerConnection = sqlServerConnection;
    }

    @Override
    protected void interrupt() {
        try {
            // If TCP Connection to server is silently dropped, exceeding the query timeout
            // on the same connection does not throw SQLTimeoutException
            // The application stops responding instead until SocketTimeoutException is
            // thrown. In this case, we must manually terminate the connection.
            if (null == command && null != sqlServerConnection) {
                sqlServerConnection.terminate(SQLServerException.DRIVER_ERROR_IO_FAILED,
                        SQLServerException.getErrString("R_connectionIsClosed"));
            } else {
                // If the timer wasn't canceled before it ran out of
                // time then interrupt the registered command.
                command.interrupt(SQLServerException.getErrString("R_queryTimedOut"));
            }
        } catch (SQLServerException e) {
            // Unfortunately, there's nothing we can do if we fail to time out the request. There
            // is no way to report back what happened.
            assert null != command;
            command.log(Level.FINE, "Command could not be timed out. Reason: " + e.getMessage());
        }
    }
}
