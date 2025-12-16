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
        public List<ItemAbstract> getItems(String query, String controllerName) {

                List<ItemAbstract> itemModelList = new ArrayList<>();

                int lastPage = 0;
                int currentPage = 0;

                while (currentPage <= lastPage) {

                        HttpResponse<String> response = HTTPService.sendRequestWithAuthentication(null, null,
                                        this.getBaseURL()
                                                        + "&configuration=workflow&page=" + currentPage + "&" +query
                                                        +"+dc.type:"+ URLEncoder.encode(this.getTypeFilter(controllerName), StandardCharsets.UTF_8),
                                        "GET");

                        if (response != null) {
                              
                                try {
                                        JsonNode jobject = mapper.readTree(response.body());
                               
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
                                                JsonNode metadata = workflowItemJson.get("sections");

                                               HttpResponse<String> itemResponse = HTTPService.sendRequestWithAuthentication(null, null,
                                                        this.configuration.getOrganisationField("repositoryAPI")
                                                                        + "/workflow/workflowitems/"
                                                                        + workflowItemID
                                                                        +"/item",
                                                        "GET");


                                                if(itemResponse != null){
                                                        WorkflowItem workflowItem = new WorkflowItem();
                                                        workflowItem.setId(workflowItemID);
                                                        workflowItem.setUuid(mapper.readTree(itemResponse.body()).get("uuid").asText());

                                                        itemModelList.add(this.getMetadataFromItem(workflowItem, metadata));
                                                }
                                                

                                        }

                                } catch (JsonProcessingException e) {
                                        
                                }

                        }

                        currentPage++;

                }

                return itemModelList;


        }

        @Override
        public boolean fillField(ItemAbstract item, EnrichmentModel input, String fieldName,
                        String section) {

                if (input == null) {
                        return false;
                }

                WorkflowItem workflowItem = (WorkflowItem) item;
                String preparedInput = this.prepareFieldInput(input, "/sections/" + section + "/" + fieldName, "add");

                // check if item already claimed
                HttpResponse<String> responseIsClaimedTask = HTTPService.sendRequestWithAuthentication(null, null,
                                this.configuration.getOrganisationField("repositoryAPI")
                                                + "/workflow/claimedtasks/search/findByItem?uuid="
                                                + workflowItem.getUuid(),
                                "GET");



                if ( responseIsClaimedTask == null) {
                        //search in pool for item
                        HttpResponse<String> responseTaskInPool = HTTPService
                                        .sendRequestWithAuthentication(null, null,
                                                                this.configuration
                                                                        .getOrganisationField("repositoryAPI")
                                                                        + "/workflow/pooltasks/search/findByItem?uuid="
                                                                        + workflowItem.getUuid(),
                                                        "GET");

                        if (responseTaskInPool != null) {

                                
                                try {
                                        int poolTaskID = mapper.readTree(responseTaskInPool.body()).get("id").asInt();
                               
                                        //claim pooltask
                                        HttpResponse<String> responseTaskClaimed = HTTPService.sendRequestWithAuthentication(
                                                        this.configuration.getOrganisationField("repositoryAPI")
                                                                        + "/workflow/pooltasks/"
                                                                        + poolTaskID,
                                                        "text/uri-list",
                                                        this.configuration.getOrganisationField("repositoryAPI")
                                                                        + "/workflow/claimedtasks",
                                                        "POST");

                                        if (responseTaskClaimed != null) {
                                                workflowItem.setClaimedTaskID(mapper.readTree(responseTaskClaimed.body()).get("id").asInt());
                                        }

                                } catch (JsonProcessingException e) {
                                        
                                }

                        }

                }else{
                        try{
                                //set ID for already claimed tasks
                                workflowItem.setClaimedTaskID(mapper.readTree(responseIsClaimedTask.body()).get("id").asInt());

                        } catch (JsonProcessingException e) {
                               
                        }


                }

                if(workflowItem.getClaimedTaskID() != -1){

                        //fill field
                        HttpResponse<String> responseFillField = HTTPService.sendRequestWithAuthentication(preparedInput,
                                        "application/json",
                                        this.configuration.getOrganisationField("repositoryAPI")
                                                        + "/workflow/workflowitems/"
                                                        + workflowItem.getId(),
                                        "PATCH");
                        if (responseFillField != null) {

                                // return all items back to pool
                                HttpResponse<String> responseReturnToPool = HTTPService.sendRequestWithAuthentication(
                                                null, null,
                                                this.configuration.getOrganisationField("repositoryAPI")
                                                                + "/workflow/claimedtasks/"
                                                                + workflowItem.getClaimedTaskID(),
                                                "DELETE");

                                if (responseReturnToPool.body() == null) {

                                        return false;

                                }
                                

                                if (responseFillField.body() != null) {

                                        return true;

                                }

                        }
                }

                return false;

        }

        @Override
        public ItemAbstract getItemById(String uuid) {

                HttpResponse<String> responseWorkflowItem = HTTPService.sendRequestWithAuthentication(null, null,
                this.configuration.getOrganisationField("repositoryAPI")
                                + "/workflow/workflowitems/search/item?uuid=" + uuid,
                "GET");
                if(responseWorkflowItem != null){

                        JsonNode jsonNode;
                        try {
                                jsonNode = mapper.readTree(responseWorkflowItem.body());
                        
                                Integer workflowItemId = jsonNode.get("id").asInt();
                                JsonNode metadata = jsonNode.get("sections");
                
                                WorkflowItem workflowItem = new WorkflowItem();
                                workflowItem.setId(workflowItemId.toString());
                                workflowItem.setUuid(uuid);
                
                                return this.getMetadataFromItem(workflowItem, metadata);
                                
                        } catch (JsonProcessingException e) {

                        }

                }
                return null;
                

        }
        
        @Override
        public ItemAbstract getMetadataFromItem(ItemAbstract item, JsonNode sectionsNode) {

                if (sectionsNode.isEmpty()) {
                        return item;
                }

                if (sectionsNode != null) {
                        for (JsonNode sectionNode : sectionsNode) {
                                try {
                                item.setIssn(
                                        sectionNode.get("dc.identifier.issn").get(0).get("value").asText());
                                } catch (NullPointerException e) {

                                }

                                try {
                                item.setPublisher(
                                        sectionNode.get("dc.publisher").get(0).get("value").asText());

                                } catch (NullPointerException e) {

                                }
                                try {
                                item.setType(
                                        sectionNode.get("dc.type").get(0).get("value").asText());

                                } catch (NullPointerException e) {

                                }
                        }
                }

                return item;

        }

}