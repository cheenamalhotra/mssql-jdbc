package com.microsoft.sqlserver.jdbc;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


abstract class AbstractTimeoutTask implements SqlServerTimerTask {
    private static final AtomicLong COUNTER = new AtomicLong(0);
    private final UUID connectionId;

    AbstractTimeoutTask(UUID connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * Action to interrupt the command implemented by concrete subclasses.
     */
    protected abstract void interrupt();

    @Override
    public final void run() {
        // Create a new thread to run the interrupt to ensure that blocking operations performed
        // by the interrupt do not hang the primary timer thread.
        String name = "mssql-timeout-task-" + COUNTER.incrementAndGet() + "-" + connectionId;
        Thread thread = new Thread(this::interrupt, name);
        thread.setDaemon(true);
        thread.start();
    }
}
