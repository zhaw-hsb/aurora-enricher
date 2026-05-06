/*
* This file is part of the Aurora Publication Finder.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.LogCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * This class collects all logs for the helpdesk.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class HelpdeskLogCollector {
    private static List<String> messages = new ArrayList<>();

    private HelpdeskLogCollector(){}


    /**
     * Method to collect a message
     * @param message the logged message
     */
    public static void logInfo(String message) {
        messages.add(message);
    }

    /**
     * Method to get all collected messages
     * @return list of messages
     */
    public static List<String> getMessages() {
        return messages;
    }

    /**
     * Method to remove all collected messages
     */
    public static void clear() {
        messages.clear();
    }
}
