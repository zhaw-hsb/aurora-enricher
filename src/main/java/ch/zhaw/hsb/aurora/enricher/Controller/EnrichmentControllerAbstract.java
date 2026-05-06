/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;

import ch.zhaw.hsb.aurora.enricher.Configuration.Configuration;
import ch.zhaw.hsb.aurora.enricher.Factory.ServiceFactory;
import ch.zhaw.hsb.aurora.enricher.LogCollector.AdminLogCollector;
import ch.zhaw.hsb.aurora.enricher.LogCollector.HelpdeskLogCollector;
import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;
import ch.zhaw.hsb.aurora.enricher.Service.File.CSVService;
import ch.zhaw.hsb.aurora.enricher.Service.Item.State.ItemStateServiceInterface;
import ch.zhaw.hsb.aurora.enricher.Service.Provider.ProviderServiceAbstract;

/**
 * This abstract class gives default actions to control the enrichment of a field.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public abstract class EnrichmentControllerAbstract {

    protected String uuid;
    protected List<Class<?>> itemModelClassList;
    protected ProviderServiceAbstract organisationProviderService;
    protected boolean updateAll;

    abstract public String getName();

    protected String getMissingMetadataFilter() throws NameNotFoundException {

        String metadataField = Configuration.getInstance().getMetadataField(this.getName());
        if (metadataField == null) {

            throw new NameNotFoundException("Metadata field for " + this.getName() + " has not been found");

        }

        //filter to search for all publications, also those with filled metadataField
        if(updateAll){
            return "";
        }

        //filter to only get publications where the metadataField is not filled yet
        return "-" + metadataField + ":*";


        


    }

    protected boolean fillProviderField(ItemStateServiceInterface itemStateService, ItemAbstract item,
            EnrichmentModel enrichmentModel) {

        if (itemStateService.fillField(item, enrichmentModel,
                Configuration.getInstance().getMetadataField(getName()),
                getSection())) {
            return true;
        }

        return false;

    }

    protected String getSection() {

        return Configuration.getInstance().getMetadataField(getName() + ".section");

    };

    public boolean enrichAction() {

        for (Class<?> itemModelClass : this.itemModelClassList) {

            ItemAbstract itemModel;
            try {
                itemModel = (ItemAbstract) itemModelClass.getConstructor().newInstance();

                if (this.uuid != null) {

                    ItemStateServiceInterface stateServiceInstance = ServiceFactory
                            .getItemStateInstance(itemModel);
                    ItemAbstract item = stateServiceInstance.getItemById(this.uuid);

                    boolean successfullyFilled = false;
                    if (item != null) {
                        successfullyFilled = this.enrichItems(stateServiceInstance, item);

                        if (!successfullyFilled) {
                            HelpdeskLogCollector.logInfo("Enrichment model empty for UUID " + item.getUuid());
                            HelpdeskLogCollector.logInfo("Not successful for UUID  "+ item.getUuid());
                            this.writeToCSV(new ArrayList<>() {
                                {
                                    add(item);
                                }
                            });
                        }else{

                            HelpdeskLogCollector.logInfo("Successful for UUID "+item.getUuid());

                        }
                    } else {
                        HelpdeskLogCollector.logInfo("Not successful: item "+this.uuid+" not found.");
                    }

                } else {

                    this.enrichItems(ServiceFactory.getItemStateInstance(itemModel));

                }

            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {

                AdminLogCollector.logErrorAndExit("",e);

            }

        }
        return true;

    }

    private boolean enrichItems(ItemStateServiceInterface itemStateService) {

        EnrichmentModel enrichmentModel = null;

        List<ItemAbstract> items = null;

        try {
            items = itemStateService.getItems(getMissingMetadataFilter(),getName());
        } catch (NameNotFoundException e) {
            AdminLogCollector.logErrorAndExit("Name not found.", e);
        }

        List<ItemAbstract> itemModelsNotFilled = new ArrayList<>();

        HelpdeskLogCollector.logInfo(items.size()+" items found in repository.");

        int successfulCounter = 0;

        for (ItemAbstract item : items) {

            enrichmentModel = this.organisationProviderService.getFilledEnrichmentModel(item);

            boolean successfullyFilled = false;
            if (enrichmentModel != null) {
                successfullyFilled = this.fillProviderField(itemStateService, item, enrichmentModel);

            } else {
                HelpdeskLogCollector.logInfo("Enrichment model empty for UUID: " + item.getUuid());
            }

            if (!successfullyFilled) {
                itemModelsNotFilled.add(item);
                HelpdeskLogCollector.logInfo("Not successful for UUID: " + item.getUuid());
            } else {
                HelpdeskLogCollector.logInfo("Successful for UUID: " + item.getUuid());
                successfulCounter++;
            }

        }

        if (itemModelsNotFilled.size() > 0) {
            this.writeToCSV(itemModelsNotFilled);
            HelpdeskLogCollector.logInfo(itemModelsNotFilled.size()+" items not filled.");
        }

        if(successfulCounter > 0){
            HelpdeskLogCollector.logInfo(successfulCounter+" items filled.");
        }

        return true;
    }

    private boolean enrichItems(ItemStateServiceInterface itemStateService, ItemAbstract item) {

        EnrichmentModel enrichmentModel = this.organisationProviderService.getFilledEnrichmentModel(item);

        return this.fillProviderField(itemStateService, item, enrichmentModel);

    }

    private void writeToCSV(List<ItemAbstract> items) {
        HelpdeskLogCollector.logInfo("Write unsuccessful items to csv.");
        String filePath = Configuration.getInstance().getOrganisationField("unsuccessfulItemList");
        CSVService.writeItemAndEnrichmentToCSV(items, filePath);

    };

}
