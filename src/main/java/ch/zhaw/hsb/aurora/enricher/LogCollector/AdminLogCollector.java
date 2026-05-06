/*
* This file is part of the Aurora Publication Finder.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.LogCollector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class collects all logs for admins.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class AdminLogCollector {

    private static List<String> adminErrors = new ArrayList<>();
     // Handler for ERROR logs
    private static Consumer<List<String>> onErrorHandler;

    // Set the error handler from outside
    public static void setOnErrorHandler(Consumer<List<String>> handler) {
        onErrorHandler = handler;
    }


    private AdminLogCollector(){}


     /**
     * Method to log an error message with the exception stack trace.
     * @param message The error message to log.
     * @param e The exception to log (can be null if no exception).
     */
    public static void logErrorAndExit(String message, Exception e) {
        log("ERROR", message, e);
    }

    /**
     * Method to log a warning message.     *
     * @param message The warning message to log.
     */
    public static void logWarning(String message, Exception e) {
        log("WARNING", message, e);
    }

    /**
     * Method to log an informational message.
     * @param message The informational message to log.
     */
    public static void logInfo(String message) {
        log("INFO", message, null);
    }


        /**
     * A private helper method to log messages with different levels (ERROR, WARNING, INFO).
     * It formats the log entry with the log level, message, and exception stack trace (if any).
     * Calls handler on error.
     * @param level The log level (ERROR, WARNING, INFO).
     * @param message The message to log.
     * @param e The exception to log (optional).
     */
    private static void log(String level, String message, Exception e) {
        StringBuilder logEntry = new StringBuilder();

        logEntry.append("[").append(level).append("] ").append(message);
        System.out.println(logEntry);

        if (e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logEntry.append("\n").append(sw.toString());
        }

        adminErrors.add(logEntry.toString());

        if (level.equals("ERROR")  && onErrorHandler != null) {
             onErrorHandler.accept(adminErrors); // Call the handler
        }
    }

    /**
     * Method to get all collected logged errors
     * @return list of errors
     */
    public static List<String> getErrors() {
        return adminErrors;
    }

    /**
     * Method to remove all collected logged errors
     */
    public static void clear() {
        adminErrors.clear();
    }
    
}
