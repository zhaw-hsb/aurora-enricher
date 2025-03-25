/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Provider;

import java.lang.reflect.Method;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ch.zhaw.hsb.aurora.enricher.Configuration.Configuration;
import ch.zhaw.hsb.aurora.enricher.Configuration.PropertyCredentials;
import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;
import ch.zhaw.hsb.aurora.enricher.Service.Request.HTTPService;

/**
 * This abstract class provides default actions for the service of a SherpaRomeo provider.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public abstract class SherpaRomeoServiceAbstract extends ProviderServiceAbstract {

    // this service has an api to be able to fetch info from sherparomeo with issn
    // or with publisher

    protected String name;

    public SherpaRomeoServiceAbstract(String name) {

        this.name = name;

    }

    public EnrichmentModel getFilledEnrichmentModel(ItemAbstract item) {

        EnrichmentModel enrichmentModel = null;

        enrichmentModel = this.fetchInfoWithISSN(item.getIssn());

        return enrichmentModel;

    }

    private EnrichmentModel fetchInfoWithISSN(String ISSN) {

        EnrichmentModel sherpaRomeoModel = null;

        String jsonData;

        PropertyCredentials propertyCredentials = new PropertyCredentials();

        String sherpaBaseURL = Configuration.getInstance().getProviderUrl(this.name) + "&api-key="
                + propertyCredentials.getProviderAPIKey(this.name);
        HttpResponse<String> response = HTTPService.sendRequest(null, null,
                sherpaBaseURL + "&identifier=" + ISSN,
                "GET", null);

        if (response != null) {
            jsonData = response.body();

            ObjectMapper mapper = new ObjectMapper();

            JsonNode jobject;
            try {
                jobject = mapper.readTree(jsonData);
            

                ArrayNode items = (ArrayNode) jobject.get("items");

                if (items.size() < 1) {
                    return null;
                }

                for (int i = 0; i < items.size(); i++) {

                    ArrayNode publisherPolicies = (ArrayNode) items.get(i).get("publisher_policy");
                    String uri = items.get(i).get("system_metadata").get("uri").asText();

                    for (int j = 0; j < publisherPolicies.size(); j++) {

                        JsonNode publisherPolicy = publisherPolicies.get(j);

                        ArrayNode permittedOAs = (ArrayNode) publisherPolicy.get("permitted_oa");

                        for (int k = 0; k < permittedOAs.size(); k++) {

                            JsonNode permittedOA = permittedOAs.get(k);

                            if (this.checkExclusionCriterias(permittedOA)) {
                                continue;
                            }

                            sherpaRomeoModel = this.fillEnrichmentModel(uri, permittedOA);

                            if (!this.checkNextOACriteria(k, permittedOAs, sherpaRomeoModel)) {
                                if (this.isBestVersion(sherpaRomeoModel.getVersion())) {
                                    return sherpaRomeoModel;
                                } else {
                                    break;
                                }

                            }

                        }

                    }

                }
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return sherpaRomeoModel;

    }

    protected abstract EnrichmentModel fillEnrichmentModel(String uri, JsonNode permittedOA);

    protected abstract boolean checkNextOACriteria(int index, ArrayNode permittedOA, EnrichmentModel enrichmentModel);

    public abstract boolean isBestVersion(String version);

    private boolean checkExclusionCriterias(JsonNode permittedOA) {

        Class<?> organisationClass = this.getClass();

        Method[] methods = organisationClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().endsWith("ExclusionCriteria")) {

                try {
                    boolean isExcluded = (Boolean) method.invoke(this, permittedOA);

                    if (isExcluded) {
                        return true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return false;

    }

}
