/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Item;

import java.lang.reflect.Method;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.zhaw.hsb.aurora.enricher.Configuration.Configuration;
import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;

/**
 * This abstract class provides default actions for the service of an item.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
abstract public class ItemServiceAbstract implements ItemServiceInterface {

    String typeFilter;
    String itemPath;

    public ItemServiceAbstract() {

    }

    public String getTypeFilter() {
        String typeFilter = null;
        List<String> types = Configuration.getInstance().getAllByKeyStart("type.");

        if (types != null && types.size() > 0) {

            typeFilter = "(\"";

            for (String type : types) {
                if (!typeFilter.equals("(\"")) {
                    typeFilter = typeFilter + "\" OR \"";
                }

                typeFilter = typeFilter + type;

            }

            typeFilter = typeFilter + "\")";

        }

        return typeFilter;

    }

    public String getBaseURL() {

        return Configuration.getInstance().getOrganisationField("repositoryAPI") + "/discover/search/objects?size="
                + Configuration.getInstance().getOrganisationField("itemsPerPage");
    }

    public ItemAbstract getMetadataFromItem(ItemAbstract item, JsonNode metadata) {

        if (metadata.isEmpty()) {
            return item;
        }
        try {
            item.setIssn(
                    metadata.get("dc.identifier.issn").get(0).get("value").asText());
        } catch (NullPointerException e) {

        }
        try {
            item.setPublisher(
                    metadata.get("dc.publisher").get(0).get("value").asText());

        } catch (NullPointerException e) {

        }
        try {
            item.setType(
                    metadata.get("dc.type").get(0).get("value").asText());

        } catch (NullPointerException e) {

        }

        return item;

    }

    public String prepareFieldInput(EnrichmentModel inputModel, String itemPath, String op) {

        String delimiter = " *** ";

        Method[] methods = inputModel.getClass().getDeclaredMethods();

        String inputValue = null;

        for (Method method : methods) {

            if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                try {

                    String fieldName = method.getName().substring(4);

                    Object value = method.invoke(inputModel);

                    if (inputValue != null) {
                        inputValue = inputValue + delimiter;
                    }

                    inputValue = inputValue + fieldName + ": " + value;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode wholeOperation = mapper.createObjectNode();
        wholeOperation.put("op", "add");
        wholeOperation.put("path", itemPath);

        ArrayNode values = mapper.createArrayNode();
        ObjectNode value = mapper.createObjectNode();
        value.put("value", inputValue);
        values.add(value);

        wholeOperation.set("value", values);

        return mapper.createArrayNode().add(wholeOperation).toString();

    };

}
