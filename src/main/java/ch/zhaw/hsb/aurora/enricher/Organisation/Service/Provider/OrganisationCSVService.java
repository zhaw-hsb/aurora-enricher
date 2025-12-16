/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Organisation.Service.Provider;

import java.util.List;
import java.util.Map;

import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;
import ch.zhaw.hsb.aurora.enricher.Service.Provider.CSVServiceAbstract;

/**
 * This class provides functions for the CSV service of an organisation.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class OrganisationCSVService extends CSVServiceAbstract {

    public OrganisationCSVService(String name) {
        super(name);
    }

    @Override
    public EnrichmentModel fillEnrichmentModel(ItemAbstract item, List<Map<String, String>> records) {

        EnrichmentModel enrichmentModel = new EnrichmentModel();

        for (Map<String, String> record : records) {

            if (record.get("Publisher").equalsIgnoreCase(item.getPublisher())
                    && record.get("PublicationType").equals(item.getType())) {

                enrichmentModel.setVersion(record.get("Version"));
                enrichmentModel.setEmbargoAmount(record.get("EmbargoAmount").equals("")?-1:Integer.parseInt(record.get("EmbargoAmount")));
                enrichmentModel.setEmbargoUnit(record.get("EmbargoUnit"));
                enrichmentModel.setLicense(record.get("License"));
                enrichmentModel.setUri(record.get("URL"));
                return enrichmentModel;
            }

        }

        return null;

    }

}
