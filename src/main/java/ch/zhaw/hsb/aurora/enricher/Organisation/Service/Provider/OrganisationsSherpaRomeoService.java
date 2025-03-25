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
                // TODO Auto-generated constructor stub
        }

        public boolean firstExclusionCriteria(JsonNode permittedOA) {

                String matchLocations = Configuration.getInstance().getMatchStringField(this.name + ".location");

                ArrayNode locationPhrases = (ArrayNode) permittedOA.get("location")
                                .get("location_phrases");

                for (int l = 0; l < locationPhrases.size(); l++) {

                        String locationPhrase = locationPhrases.get(l).get("phrase").asText();
                        if (locationPhrase.matches(matchLocations)) {
                                return false;
                        }

                }

                return true;

        }

        public boolean secondExclusionCriteria(JsonNode permittedOA) {

                String additionalOAfee = permittedOA.get("additional_oa_fee").asText();

                if (additionalOAfee.equals("no")) {
                        return false;
                }

                return true;
        }

        public boolean thirdExclusionCriteria(JsonNode permittedOA) {

                String matchVersions = Configuration.getInstance().getMatchStringField(this.name + ".version");

                String version = permittedOA.get("article_version_phrases").get(0)
                                .get("phrase").asText();

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

                try {
                        enrichmentModel.setUri(uri);

                } catch (NullPointerException e) {

                }
                try {
                        enrichmentModel.setEmbargoUnit(permittedOA.get("embargo").get("units").asText());

                } catch (NullPointerException e) {

                }
                try {
                        enrichmentModel.setEmbargoAmount(permittedOA.get("embargo").get("amount").asInt());

                } catch (NullPointerException e) {

                }
                try {
                        enrichmentModel.setLicense(permittedOA.get("license").get(0)
                                        .get("license_phrases").get(0).get("phrase").asText());

                } catch (NullPointerException e) {

                }
                try {

                        enrichmentModel.setVersion(permittedOA.get("article_version_phrases").get(0)
                                        .get("phrase").asText());

                } catch (NullPointerException e) {

                }

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

                                nextVersion = permittedOAs.get(nextK)
                                                .get("article_version_phrases").get(0)
                                                .get("phrase").asText();

                                nextVersion = nextVersion == null ? "undefined" : nextVersion;

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
