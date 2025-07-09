package com.nvd.expensetracker.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    private static final Logger stackTraceLogger = LoggerFactory.getLogger("STACKTRACE_LOGGER");

    public static void logException(String context, Exception e) {
        stackTraceLogger.error("Exception at {}: {}", context, e.getMessage(), e);
    }

    public static void logError(String message) {
        stackTraceLogger.error(message);
    }

    public static void logError(String message, Throwable t) {
        stackTraceLogger.error(message, t);
    }
}
