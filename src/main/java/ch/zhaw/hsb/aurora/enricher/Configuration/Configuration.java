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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.zhaw.hsb.aurora.enricher.Main;

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

        try {
            InputStream input =  Main.class.getClassLoader().getResourceAsStream(
                    "assets/config/organisation.properties");
            prop = new Properties();
            prop.load(input);
            configuration = new Configuration();
            return configuration;

        } catch (IOException e) {
            System.out.println("Configuration file not found.");
            e.printStackTrace();
            System.exit(1);

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
            // TODO Auto-generated catch block
            e.printStackTrace();
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
