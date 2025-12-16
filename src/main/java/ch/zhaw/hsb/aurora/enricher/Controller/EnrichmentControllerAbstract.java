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

    abstract public String getName();

    protected String getQuery() throws NameNotFoundException {

        String value = Configuration.getInstance().getMetadataField(this.getName());
        if (value != null) {
            return "query=-" + value + ":*";

        }
        throw new NameNotFoundException("Metadata field for " + this.getName() + " has not been found");

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
                            System.out.println("Enrichment model empty for UUID " + item.getUuid());
                            System.out.println("Not successful for UUID  "+ item.getUuid());
                            this.writeToCSV(new ArrayList<>() {
                                {
                                    add(item);
                                }
                            });
                        }else{

                            System.out.println("Successful for UUID "+item.getUuid());

                        }
                    } else {
                        System.out.println("Not successful: item "+this.uuid+" not found.");
                    }

                } else {

                    this.enrichItems(ServiceFactory.getItemStateInstance(itemModel));

                }

            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {

                System.out.println(e);
                System.exit(1);

            }

        }
        return true;

    }

    private boolean enrichItems(ItemStateServiceInterface itemStateService) {

        EnrichmentModel enrichmentModel = null;

        List<ItemAbstract> items = null;

        try {
            items = itemStateService.getItems(getQuery(),getName());
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<ItemAbstract> itemModelsNotFilled = new ArrayList<>();

        for (ItemAbstract item : items) {

            enrichmentModel = this.organisationProviderService.getFilledEnrichmentModel(item);

            boolean successfullyFilled = false;
            if (enrichmentModel != null) {
                successfullyFilled = this.fillProviderField(itemStateService, item, enrichmentModel);

            } else {
                System.out.println("Enrichment model empty for UUID: " + item.getUuid());
            }

            if (!successfullyFilled) {
                itemModelsNotFilled.add(item);
                System.out.println("Not successful for UUID: " + item.getUuid());
            } else {
                System.out.println("Successful for UUID: " + item.getUuid());
            }

        }

        if (itemModelsNotFilled.size() > 0) {
            this.writeToCSV(itemModelsNotFilled);

        }

        return true;
    }

    private boolean enrichItems(ItemStateServiceInterface itemStateService, ItemAbstract item) {

        EnrichmentModel enrichmentModel = this.organisationProviderService.getFilledEnrichmentModel(item);

        return this.fillProviderField(itemStateService, item, enrichmentModel);

    }

    private void writeToCSV(List<ItemAbstract> items) {
        System.out.println("Write unsuccessful items to csv.");
        String filePath = Configuration.getInstance().getOrganisationField("unsuccessfulItemList");
        CSVService.writeItemAndEnrichmentToCSV(items, filePath);

    };

}
