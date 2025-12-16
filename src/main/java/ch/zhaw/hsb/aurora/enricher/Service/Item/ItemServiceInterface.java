/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Item;


import com.fasterxml.jackson.databind.JsonNode;

import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;

/**
 * This interface defines the characteristics of a item service.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public interface ItemServiceInterface {

        public String prepareFieldInput(EnrichmentModel input, String itemPath, String op);
        public ItemAbstract getMetadataFromItem(ItemAbstract item, JsonNode metadata);
        public String getBaseURL();
        public String getTypeFilter(String controllerName);
    
}
