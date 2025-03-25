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
import ch.zhaw.hsb.aurora.enricher.Model.Item.WorkflowItem;
import ch.zhaw.hsb.aurora.enricher.Organisation.Service.Item.OrganisationItemServiceAbstract;
import ch.zhaw.hsb.aurora.enricher.Service.Request.HTTPService;

/**
 * This class is the service for an item with the state = workflow.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class WorkflowItemService extends OrganisationItemServiceAbstract implements ItemStateServiceInterface {

        Configuration configuration;
        ObjectMapper mapper = new ObjectMapper();


        public WorkflowItemService() {

                this.configuration = Configuration.getInstance();

        }

        @Override
        public List<ItemAbstract> getItems(String query) {

                List<ItemAbstract> itemModelList = new ArrayList<>();

                int lastPage = 0;
                int currentPage = 0;

                while (currentPage <= lastPage) {

                        HttpResponse<String> response = HTTPService.sendRequestWithAuthentication(null, null,
                                        this.getBaseURL()
                                                        + "&configuration=workflow&page=" + currentPage + "&" +query
                                                        +"+dc.type:"+ URLEncoder.encode(this.getTypeFilter(), StandardCharsets.UTF_8),
                                        "GET");

                        if (response != null) {
                                String jsonData = response.body();


                                JsonNode jobject;
                                try {
                                        jobject = mapper.readTree(jsonData);
                               

                                        if (lastPage == 0) {
                                                lastPage = jobject.get("_embedded").get("searchResult")
                                                                .get("page").get("totalPages").asInt() - 1;
                                        }

                                        ArrayNode worklflowItemsJson = (ArrayNode) jobject.get("_embedded")
                                                        .get("searchResult")
                                                        .get("_embedded").get("objects");

                                        for (int i = 0; i < worklflowItemsJson.size(); i++) {

                                                JsonNode workflowItemJson = worklflowItemsJson.get(i)
                                                                .get("_embedded")
                                                                .get("indexableObject").get("_embedded")
                                                                .get("workflowitem");
                                                String workflowItemID = String.valueOf(workflowItemJson.get("id").asInt());
                                                System.out.println(workflowItemID);

                                                JsonNode metadata = workflowItemJson.get("_embedded")
                                                                .get("item")
                                                                .get("metadata");
                                                String workflowItemUUID = workflowItemJson.get("_embedded")
                                                                .get("item")
                                                                .get("uuid").asText();
                                                System.out.println(workflowItemUUID);

                                                WorkflowItem workflowItem = new WorkflowItem();
                                                workflowItem.setId(workflowItemID);
                                                workflowItem.setUuid(workflowItemUUID);

                                                itemModelList.add(this.getMetadataFromItem(workflowItem, metadata));

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
        public boolean fillField(ItemAbstract item, EnrichmentModel input, String fieldName,
                        String section) {

                WorkflowItem workflowItem = (WorkflowItem) item;

                System.out.println(item.getId());

                // PUT /api/core/items/<:uuid>
                // https://github.com/DSpace/RestContract/blob/dspace-7_x/items.md

                if (input == null) {
                        return false;
                }

                String preparedInput = this.prepareFieldInput(input, "/sections/" + section + "/" + fieldName, "add");

                Integer claimedID = -1;

                // check if item already claimed

                if (HTTPService.sendRequestWithAuthentication(null, null,
                                this.configuration.getOrganisationField("repositoryAPI")
                                                + "/workflow/claimedtasks/search/findByItem?uuid="
                                                + workflowItem.getUuid(),
                                "GET") == null) {

                        HttpResponse<String> response = HTTPService
                                        .sendRequestWithAuthentication(null, null,
                                                                this.configuration
                                                                        .getOrganisationField("repositoryAPI")
                                                                        + "/workflow/pooltasks/search/findByItem?uuid="
                                                                        + workflowItem.getUuid(),
                                                        "GET");

                        if (response != null) {

                                String responseBodyPoolTask = response.body();
                                Integer poolTaskID;
                                try {
                                        poolTaskID = mapper.readTree(responseBodyPoolTask).get("id").asInt();
                               

                                        HttpResponse<String> response2 = HTTPService.sendRequestWithAuthentication(
                                                        this.configuration.getOrganisationField("repositoryAPI")
                                                                        + "/workflow/pooltasks/"
                                                                        + poolTaskID,
                                                        "text/uri-list",
                                                        this.configuration.getOrganisationField("repositoryAPI")
                                                                        + "/workflow/claimedtasks",
                                                        "POST");

                                        if (response2 != null) {
                                                String responseBodyClaimedTask = response2.body();
                                                claimedID = mapper.readTree(responseBodyClaimedTask).get("id").asInt();
                                        }

                                } catch (JsonProcessingException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                }

                        }

                }

                HttpResponse<String> response = HTTPService.sendRequestWithAuthentication(preparedInput,
                                "application/json",
                                this.configuration.getOrganisationField("repositoryAPI")
                                                + "/workflow/workflowitems/"
                                                + workflowItem.getId(),
                                "PATCH");
                if (response != null) {

                        String responseBody = response.body();

                        // return item back to pool only if we added it to claimedtasks before
                        if (claimedID != -1) {

                                HttpResponse<String> response2 = HTTPService.sendRequestWithAuthentication(
                                                null, null,
                                                this.configuration.getOrganisationField("repositoryAPI")
                                                                + "/workflow/claimedtasks/"
                                                                + claimedID,
                                                "DELETE");
                                String responseBodyDeleteFromClaimedTask = response2.body();

                                if (responseBodyDeleteFromClaimedTask == null) {

                                        return false;

                                }
                        }

                        if (responseBody != null) {

                                return true;

                        }

                }

                return false;

        }

        @Override
        public ItemAbstract getItemById(String uuid) {

                HttpResponse<String> response = HTTPService.sendRequestWithAuthentication(null, null,
                this.configuration.getOrganisationField("repositoryAPI")
                                + "/workflow/workflowitems/search/item?uuid=" + uuid,
                "GET");
                if(response != null){

                        JsonNode jsonNode;
                        try {
                                jsonNode = mapper.readTree(response.body());
                        
                                Integer workflowItemId = jsonNode.get("id").asInt();
                                JsonNode metadata = jsonNode.get("_embedded").get("item")
                                                .get("metadata");
                
                                WorkflowItem workflowItem = new WorkflowItem();
                                workflowItem.setId(workflowItemId.toString());
                                workflowItem.setUuid(uuid);
                
                                return this.getMetadataFromItem(workflowItem, metadata);
                                
                        } catch (JsonProcessingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }

                }
                return null;
                

        }

}