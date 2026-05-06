/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import ch.zhaw.hsb.aurora.enricher.Main;
import ch.zhaw.hsb.aurora.enricher.LogCollector.AdminLogCollector;

/**
 * This class retrieves configuration properties from the credentials.properties file. 
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class PropertyCredentialsConfiguration {


    private Properties prop;

    public PropertyCredentialsConfiguration() {

        try (InputStream input = Main.class.getClassLoader().getResourceAsStream(
                "assets/config/credentials.properties"); InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            Properties prop = new Properties();
            prop.load(reader);
            this.prop = prop;

        } catch (IOException e) {
            AdminLogCollector.logErrorAndExit("Could not read credentials.properties.", e);
        }
    }

    public String getUsername(){

        return this.prop.getProperty("username");

    }

    public String getPassword(){

        return this.prop.getProperty("password");

    }

    public String getProviderAPIKey(String provider){

        return this.prop.getProperty("apikey."+provider);

    }

        /**
     * Method to get mail address
     * @return String
     */
    public String getMail() {

        try {
            return this.prop.getProperty("mail");
        } catch (Exception e) {
            AdminLogCollector.logErrorAndExit("Error retrieving mail.", e);
        }
        return null;

    }

    /**
     * Method to get mail password
     * @return String
     */
    public String getMailPassword() {

        try {
            return this.prop.getProperty("mail.password");
        } catch (Exception e) {
            AdminLogCollector.logErrorAndExit("Error retrieving mail password.", e);
        }

        return null;

    }
    
}
