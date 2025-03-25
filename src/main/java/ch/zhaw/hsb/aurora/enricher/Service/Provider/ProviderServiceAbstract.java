/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Provider;


import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;

/**
 * This abstract class provides default actions for the service of a provider.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
abstract public class ProviderServiceAbstract {

    abstract public EnrichmentModel getFilledEnrichmentModel(ItemAbstract item);

}
