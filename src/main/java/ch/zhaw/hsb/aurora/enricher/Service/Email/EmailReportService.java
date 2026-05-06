/*
* This file is part of the Aurora Publication Finder.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Email;

import ch.zhaw.hsb.aurora.enricher.Configuration.Configuration;
import ch.zhaw.hsb.aurora.enricher.LogCollector.AdminLogCollector;
import ch.zhaw.hsb.aurora.enricher.LogCollector.HelpdeskLogCollector;

/**
 * This class sends the reports to the admin and helpdesk with an email
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class EmailReportService {

    private EmailService emailService;

    public EmailReportService(EmailService emailService) {

        this.emailService = emailService;

    }

    /**
     * Method to send reports of the collected logs to the admin and helpdesk email
     */
    public void sendReports(boolean errorOccured) {
        try {
            // Admin email
            if (!AdminLogCollector.getErrors().isEmpty()) {
                String adminContent = String.join("\n---\n", AdminLogCollector.getErrors());
                emailService.sendEmail(
                    Configuration.getInstance().getOrganisationField("mail.admin"),
                    "[AURORA enricher: SYSTEM ERROR REPORT]",
                    adminContent
                );
                AdminLogCollector.clear();
            }

            // helpdesk email
            String helpdeskMailKey = "mail.helpdesk";
            String helpdeskContent = "";
            if (!HelpdeskLogCollector.getMessages().isEmpty()) {
                helpdeskContent = String.join("\n---\n", HelpdeskLogCollector.getMessages());
            }
            
            if(errorOccured){
                helpdeskContent = helpdeskContent.concat("\n---\nAn error occured. The administrator has been notified.");
            }

            emailService.sendEmail(Configuration.getInstance().getOrganisationField(helpdeskMailKey),"AURORA enricher: System Summary", helpdeskContent);
            HelpdeskLogCollector.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
