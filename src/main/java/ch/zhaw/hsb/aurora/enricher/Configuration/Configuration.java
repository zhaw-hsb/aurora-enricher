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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.zhaw.hsb.aurora.enricher.Main;
import ch.zhaw.hsb.aurora.enricher.LogCollector.AdminLogCollector;

/**
 * This class retrieves configuration properties from the organisation.properties file. 
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class Configuration {

    private static Configuration configuration = null;

    private static Properties prop = null;

    private Configuration() {

    }

    public static Configuration getInstance() {

        if (prop != null) {
            return configuration;
        }

        String path = "assets/config/organisation.properties";
        String localPath = "assets/config/organisation-local.properties";

         if(Main.class.getClassLoader().getResource(localPath) != null){
                path = localPath.toString();
        }

        try {
            InputStream input =  Main.class.getClassLoader().getResourceAsStream(
                    path);
            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            prop = new Properties();
            prop.load(reader);
            configuration = new Configuration();
            return configuration;

        } catch (IOException e) {
           AdminLogCollector.logErrorAndExit("Configuration file not found.",e);
        }

        return null;

    }

    public String getTypeField(String type) {

        return prop.getProperty("type." + type);

    }

    public String getMetadataField(String provider) {
        return prop.getProperty("metadata." + provider);
    }

    public String getMatchStringField(String matchType) {
        return prop.getProperty("matchString." + matchType);
    }

    public String getOrganisationField(String info) {
        return prop.getProperty("organisation." + info);

    }

    public String getProviderUrl(String provider) {
        return prop.getProperty("url." + provider);
    }


    public String getCSRFTokenEndpoint() {

        Object value;
        try {
            value = prop.getProperty("dspace.version");
            
            if (value != null) {

                String version = ((String)value).split("\\.")[0];

                if(version.equals("7")){

                    return "/authn/status";

                }else{
                    
                    return "/security/csrf";

                }

            }
        } catch (Exception e) {
            AdminLogCollector.logWarning("Exception in getting CSRFTokenEndpoint", e);
        }

        // default
        return null;

    }


    public List<String> getAllByKeyStart(String keyStart){

        List<String> filteredItems = new ArrayList<String>();
            
        for (Map.Entry<Object, Object> entry : prop.entrySet()) {
            String key = (String) entry.getKey();
            
            // Check if the key starts with "type."
            if (key.startsWith(keyStart)) {
                // Print or store the key-value pair
                filteredItems.add((String) entry.getValue());
            }
        }

        return filteredItems;

    }
     

}
