/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Organisation.Service.Item;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.zhaw.hsb.aurora.enricher.Model.Enrichment.EnrichmentModel;
import ch.zhaw.hsb.aurora.enricher.Service.Item.ItemServiceAbstract;

/**
 * This abstract class provides default functions for a item service of an organisation.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
abstract public class  OrganisationItemServiceAbstract extends ItemServiceAbstract{


    @Override
    public String prepareFieldInput(EnrichmentModel input, String itemPath, String op) {

        String delimiter = " *** ";
        String inputValue = "Version: " + input.getVersion() + delimiter;
        if(input.getEmbargoAmount()<0 && input.getEmbargoUnit()==null){
            inputValue = inputValue + "Embargo: None" + delimiter;
        }else{
            inputValue = inputValue +"Embargo: " + input.getEmbargoAmount() + " " + input.getEmbargoUnit() + delimiter;
        }
        if(input.getLicense() == null || input.getLicense().equals("")){
            inputValue = inputValue + "Licence: None"+ delimiter;

        }else{
            inputValue = inputValue + "Licence: " + input.getLicense() + delimiter;
        }
                 
        inputValue = inputValue +  "URL: " + input.getUri();

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode wholeOperation = mapper.createObjectNode();
        wholeOperation.put("op", op);
        wholeOperation.put("path", itemPath);

        ArrayNode values = mapper.createArrayNode();
        ObjectNode value = mapper.createObjectNode();
        value.put("value", inputValue);
        values.add(value);

        wholeOperation.set("value", values);

        return mapper.createArrayNode().add(wholeOperation).toString();
    }

}