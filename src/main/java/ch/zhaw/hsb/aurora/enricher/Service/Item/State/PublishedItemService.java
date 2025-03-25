/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Item.State;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ch.zhaw.hsb.aurora.enricher.Configuration.Configuration;
import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;
import ch.zhaw.hsb.aurora.enricher.Model.Item.PublishedItem;
import ch.zhaw.hsb.aurora.enricher.Organisation.Service.Item.OrganisationItemServiceAbstract;
import ch.zhaw.hsb.aurora.enricher.Service.Request.HTTPService;

/**
 * This class is the service for an item with the state = published (archived).
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class PublishedItemService extends OrganisationItemServiceAbstract implements ItemStateServiceInterface {

    ObjectMapper mapper = new ObjectMapper();


    public PublishedItemService() {

    }

    @Override
    public List<ItemAbstract> getItems(String query) {

        List<ItemAbstract> itemModelList = new ArrayList<>();

        int lastPage = 0;
        int currentPage = 0;

        while (currentPage <= lastPage) {

            //f.has_content_in_original_bundle=false,equals filter to only receive the ones without fulltext
            HttpResponse<String> response = HTTPService.sendRequest(null, null, this.getBaseURL() + "&page="
                    + currentPage + "&"+"f.has_content_in_original_bundle=false,equals"+ "&"+ query + "+dc.type:"+ URLEncoder.encode(this.getTypeFilter(), StandardCharsets.UTF_8), "GET", null);

            String jsonData;
            if (response != null) {
                jsonData = response.body();
                JsonNode jobject;
                try {
                    jobject = mapper.readTree(jsonData);
                    
                    if (lastPage == 0) {

                        lastPage = jobject.get("_embedded").get("searchResult")
                                .get("page").get("totalPages").asInt() - 1;
                    }
    
                    ArrayNode publishedItemsJson = (ArrayNode) jobject.get("_embedded").get("searchResult")
                            .get("_embedded").get("objects");
    
                    for (int i = 0; i < publishedItemsJson.size(); i++) {
                        JsonNode publishedItemJson = publishedItemsJson.get(i).get("_embedded")
                                .get("indexableObject");
                        String publishedItemUUID = publishedItemJson.get("uuid").asText();
    
                        JsonNode metadata = publishedItemJson.get("metadata");
    
                        PublishedItem publishedItem = new PublishedItem();
                        publishedItem.setUuid(publishedItemUUID);
    
                        itemModelList.add(this.getMetadataFromItem(publishedItem, metadata));
                    }



                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                

            }

            currentPage++;

        }

        return itemModelList;

    }

    @Override
    public boolean fillField(ItemAbstract item, EnrichmentModel input, String fieldName, String section) {

        if (input == null) {
            return false;
        }

        String preparedInput = this.prepareFieldInput(input, "/metadata/" + fieldName, "replace");

        HttpResponse<String> response = HTTPService.sendRequestWithAuthentication(preparedInput, "application/json",
                Configuration.getInstance().getOrganisationField("repositoryAPI") + "/core/items/" + item.getUuid(),
                "PATCH");

        if (response != null) {

            return true;

        }

        return false;

    }

    @Override
    public ItemAbstract getItemById(String uuid) {
        HttpResponse<String> response = HTTPService.sendRequestWithAuthentication(null, null,
                Configuration.getInstance().getOrganisationField("repositoryAPI") + "/core/items/" + uuid, "GET");
        if (response != null) {

            JsonNode jsonNode;
            try {
                jsonNode = mapper.readTree(response.body());     
                JsonNode metadata = jsonNode.get("metadata");

                PublishedItem publishedItem = new PublishedItem();
                publishedItem.setUuid(uuid);
        
                return this.getMetadataFromItem(publishedItem, metadata);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
       
        }

        return null;

       
    }

}
