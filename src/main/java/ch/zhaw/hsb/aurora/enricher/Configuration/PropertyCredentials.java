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

/**
 * This class retrieves configuration properties from the credentials.properties file. 
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class PropertyCredentials {


    private Properties prop;

    public PropertyCredentials() {

        try (InputStream input = Main.class.getClassLoader().getResourceAsStream(
                "assets/config/credentials.properties"); InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            Properties prop = new Properties();
            prop.load(reader);
            this.prop = prop;

        } catch (IOException e) {

            this.prop = null;
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
    
}
