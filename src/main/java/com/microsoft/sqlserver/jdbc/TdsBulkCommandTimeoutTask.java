/*
 * Microsoft JDBC Driver for SQL Server Copyright(c) Microsoft Corporation All rights reserved. This program is made
 * available under the terms of the MIT License. See the LICENSE file in the project root for more information.
 */
package com.microsoft.sqlserver.jdbc;

import java.util.logging.Level;


class TdsBulkCommandTimeoutTask extends AbstractTimeoutTask {
    private final TDSCommand command;

    public TdsBulkCommandTimeoutTask(TDSCommand command, SQLServerConnection sqlServerConnection) {
        super(sqlServerConnection == null ? null : sqlServerConnection.getClientConIdInternal());
        this.command = command;
    }

    @Override
    protected void interrupt() {
        // If the timer wasn't canceled before it ran out of
        // time then interrupt the registered command.
        try {
            command.interrupt(SQLServerException.getErrString("R_queryTimedOut"));
        } catch (SQLServerException e) {
            // Unfortunately, there's nothing we can do if we
            // fail to time out the request. There is no way
            // to report back what happened.
            command.log(Level.FINE, "Command could not be timed out. Reason: " + e.getMessage());
        }
    }
}
