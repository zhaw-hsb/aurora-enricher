/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Factory;

import java.lang.reflect.InvocationTargetException;

import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;
import ch.zhaw.hsb.aurora.enricher.Service.Item.State.ItemStateServiceInterface;

/**
 * This class generates new ItemStateServices.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class ServiceFactory {

    private static final String SERVICE_PACKAGE = "ch.zhaw.hsb.aurora.enricher.Service.Item.State.";

    public static ItemStateServiceInterface getItemStateInstance(ItemAbstract itemModel) {

        String itemModelClassName = itemModel.getClass().getSimpleName();

        Class<?> itemStateService;

        try {
            itemStateService = Class
                    .forName(SERVICE_PACKAGE + itemModelClassName
                            + "Service");

            return (ItemStateServiceInterface) itemStateService.getConstructor().newInstance();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            if (e instanceof InvocationTargetException) {
                e.printStackTrace();
                Throwable cause = e.getCause();
                System.out.println("Cause of the InvocationTargetException: " + cause);
            }
            System.out.println(e);
            System.out.println("ItemStateServiceClass not found.");
            System.exit(1);
        }
        return null;

    }

}
