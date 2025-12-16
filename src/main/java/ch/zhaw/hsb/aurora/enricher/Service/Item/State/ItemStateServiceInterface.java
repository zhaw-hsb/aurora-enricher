/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Item.State;

import java.util.List;

import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;

/**
 * This interface defines the characteristics of a item state service.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public interface ItemStateServiceInterface {

        public boolean fillField(ItemAbstract item,  EnrichmentModel input, String fieldName, String section);
        public List<ItemAbstract> getItems(String query, String controllerName);
        public ItemAbstract getItemById(String uuid);

    
}
