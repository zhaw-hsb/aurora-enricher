/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import ch.zhaw.hsb.aurora.enricher.Controller.EnrichmentControllerAbstract;

/**
 * This class starts the enricher application and takes arguments
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            throw new IllegalArgumentException("Missing arguments.");
        }

        Class<?> controller = null;
        List<Class<?>> itemModelClassList = new ArrayList<>();
        String id = null;

        for (int i = 0; i < args.length; i++) {

            switch (args[i]) {
                case "-controller":
                    String controllerName = args[++i];
                    try {
                        System.out.println("Controller: "+controllerName);
                        controller = Class.forName("ch.zhaw.hsb.aurora.enricher.Controller." + controllerName);
                    } catch (ClassNotFoundException e) {
                        System.out.println("Controller " + controllerName + " not found.");
                        System.exit(1);

                    }
                    break;
                case "-items":

                    String[] itemTypeNameList = args[++i].split(",");

                    for (String itemTypeName : itemTypeNameList) {

                        try {
                            itemModelClassList.add(Class
                                    .forName("ch.zhaw.hsb.aurora.enricher.Model.Item." + itemTypeName));
                        } catch (ClassNotFoundException e) {

                            System.out.println("Item " + itemTypeName + " not found.");
                            System.exit(1);
                        }

                    }

                    break;
                case "-id":

                    id = args[++i];
                    break;

                default:
                    break;
            }

        }

        try {
            Class<?>[] paramTypes = { String.class, List.class };

            EnrichmentControllerAbstract controllerInstance = (EnrichmentControllerAbstract) controller.getConstructor(paramTypes)
                    .newInstance(id, itemModelClassList);
            controllerInstance.enrichAction();

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
