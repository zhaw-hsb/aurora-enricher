/*
* This file is part of the Aurora Publication Finder.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Email;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

import ch.zhaw.hsb.aurora.enricher.Configuration.PropertyCredentialsConfiguration;


/**
 * This class provides the service to send emails
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class EmailService {
    
    
    private static final String SMTP_HOST = "smtps.zhaw.ch";
    private static final String SMTP_PORT = "587";
    private final String SMTP_USER;
    private final String SMTP_PASSWORD;


    public EmailService(PropertyCredentialsConfiguration propertyCredentialsConfiguration){
        this.SMTP_USER = propertyCredentialsConfiguration.getMail();
        this.SMTP_PASSWORD = propertyCredentialsConfiguration.getMailPassword();

    }

    /**
     * Method to send an email
     * @param to email address of the receiver
     * @param subject subject of the email
     * @param body body of the email
     */
    public void sendEmail(String to, String subject, String body) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Create session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });

        try {
            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            // Send email
            Transport.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
