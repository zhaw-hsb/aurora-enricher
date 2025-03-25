/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Controller;

import java.util.List;

import ch.zhaw.hsb.aurora.enricher.Organisation.Controller.OrganisationControllerAbstract;
import ch.zhaw.hsb.aurora.enricher.Organisation.Service.Provider.OrganisationsSherpaRomeoService;

/**
 * This class controls the actions needed to enrich a field through Sherpa Romeo API.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class SherpaRomeoController extends OrganisationControllerAbstract {

    public SherpaRomeoController(String uuid, List<Class<?>> itemModelClassList) {

        this.organisationProviderService = new OrganisationsSherpaRomeoService(this.getName());

        this.uuid = uuid;
        this.itemModelClassList = itemModelClassList;

    }

    @Override
    public String getName() {
        return "sherpa";
    }

}
