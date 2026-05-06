/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Organisation.Service.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ch.zhaw.hsb.aurora.enricher.Configuration.Configuration;
import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Service.Provider.SherpaRomeoServiceAbstract;

/**
 * This class provides functions for the SherpaRomeo service of an organisation.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class OrganisationsSherpaRomeoService extends SherpaRomeoServiceAbstract {

        public OrganisationsSherpaRomeoService(String name) {
                super(name);
        }

        public boolean firstExclusionCriteria(JsonNode permittedOA) {

                String matchLocations = Configuration.getInstance().getMatchStringField(this.name + ".location");

                JsonNode locationPhrases = permittedOA.path("location")
                                .path("location_phrases");

                if(locationPhrases.isArray()){
                        for (int l = 0; l < locationPhrases.size(); l++) {

                                String locationPhrase = locationPhrases.path(l).path("phrase").asText();
                                if (locationPhrase.matches(matchLocations)) {
                                        return false;
                                }

                        }      
                }

                

                return true;

        }

        public boolean secondExclusionCriteria(JsonNode permittedOA) {

               
                JsonNode additionalOAfee = permittedOA.path("additional_oa_fee");


                return !additionalOAfee.isMissingNode() && !additionalOAfee.asText().equals("no");

        
                
        }

        public boolean thirdExclusionCriteria(JsonNode permittedOA) {

                String matchVersions = Configuration.getInstance().getMatchStringField(this.name + ".version");

                String version = permittedOA.path("article_version_phrases").path(0)
                                .path("phrase").asText();

                if (version.matches(matchVersions)) {

                        return false;

                }

                return true;

        }

        public boolean fourthExclusionCriteria(JsonNode permittedOA) {

                JsonNode prerequisiteObject = permittedOA.get("prerequisites");

                if (prerequisiteObject != null) {
                        return true;
                }

                return false;
        }

        @Override
        public EnrichmentModel fillEnrichmentModel(String uri, JsonNode permittedOA) {

                EnrichmentModel enrichmentModel = new EnrichmentModel();
         
                enrichmentModel.setUri(uri);
        
                enrichmentModel.setEmbargoUnit(permittedOA.path("embargo").path("units").asText());

                enrichmentModel.setEmbargoAmount(permittedOA.path("embargo").path("amount").asInt(-1));

                enrichmentModel.setLicense(permittedOA.path("license").path(0)
                                .path("license_phrases").path(0).path("phrase").asText());

                enrichmentModel.setVersion(permittedOA.path("article_version_phrases").path(0)
                                .path("phrase").asText());

                return enrichmentModel;

        }

        @Override
        protected boolean checkNextOACriteria(int index, ArrayNode permittedOAs, EnrichmentModel sherpaModel) {

                String version = sherpaModel.getVersion();

                if (version.equals("Published")) {
                        return false;
                } else if (version.equals("Accepted")) {
                        // check if next is lesser than accepted or undefined
                        int nextK = index + 1;
                        while (nextK < permittedOAs.size()) {
                                String nextVersion;

                                nextVersion = permittedOAs.path(nextK)
                                                .path("article_version_phrases").path(0)
                                                .path("phrase").asText("undefined");

                                if (nextVersion.equals("Published")) {
                                        return true;
                                }
                                nextK++;

                        }

                }

                return false;
        }

        public boolean isBestVersion(String version) {
                return version.equals("Published");
        }

}
