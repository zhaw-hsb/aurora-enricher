/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Provider;

import java.util.List;
import java.util.Map;

import ch.zhaw.hsb.aurora.enricher.Configuration.Configuration;
import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;

/**
 * This abstract class provides default actions for the service of a CSV file provider.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
abstract public class CSVServiceAbstract extends ProviderServiceAbstract {

    String name;

    public CSVServiceAbstract(String name) {
        this.name = name;
    }

    @Override
    public EnrichmentModel getFilledEnrichmentModel(ItemAbstract item) {

        String file = Configuration.getInstance().getOrganisationField("policyList");

        List<Map<String, String>> records = ch.zhaw.hsb.aurora.enricher.Service.File.CSVService
                .readCSVIntoMap(file);

        return this.fillEnrichmentModel(item, records);


    }

    abstract public EnrichmentModel fillEnrichmentModel(ItemAbstract item, List<Map<String, String>> records);

}
