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

import ch.zhaw.hsb.aurora.enricher.Configuration.PropertyCredentialsConfiguration;
import ch.zhaw.hsb.aurora.enricher.Controller.EnrichmentControllerAbstract;
import ch.zhaw.hsb.aurora.enricher.LogCollector.AdminLogCollector;
import ch.zhaw.hsb.aurora.enricher.Service.Email.EmailReportService;
import ch.zhaw.hsb.aurora.enricher.Service.Email.EmailService;

/**
 * This class starts the enricher application and takes arguments
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class Main {

    public static void main(String[] args) {
        
        EmailReportService emailReportService = new EmailReportService(new EmailService(new PropertyCredentialsConfiguration()));

        //on errors send email and exit program
        AdminLogCollector.setOnErrorHandler(errors -> {
            emailReportService.sendReports(true);
            System.exit(1);
        });

        if (args.length == 0) {
            AdminLogCollector.logErrorAndExit("Missing arguments.",null);
        }

        Class<?> controller = null;
        List<Class<?>> itemModelClassList = new ArrayList<>();
        String id = null;
        Boolean updateAll = false;

        for (int i = 0; i < args.length; i++) {

            switch (args[i]) {
                case "-controller":
                    String controllerName = args[++i];
                    try {
                        AdminLogCollector.logInfo("Controller: "+controllerName);
                        controller = Class.forName("ch.zhaw.hsb.aurora.enricher.Controller." + controllerName);
                    } catch (ClassNotFoundException e) {
                        AdminLogCollector.logErrorAndExit("Controller " + controllerName + " not found.",e);
                    }
                    break;
                case "-items":

                    String[] itemTypeNameList = args[++i].split(",");

                    for (String itemTypeName : itemTypeNameList) {

                        try {
                            itemModelClassList.add(Class
                                    .forName("ch.zhaw.hsb.aurora.enricher.Model.Item." + itemTypeName));
                        } catch (ClassNotFoundException e) {

                            AdminLogCollector.logErrorAndExit("Item " + itemTypeName + " not found.",e);
                        }

                    }

                    break;
                case "-id":

                    id = args[++i];
                    break;

                case "-updateAll":
                    updateAll = true;
                    break;

                default:
                    break;
            }

        }

        try {
            Class<?>[] paramTypes = { String.class, List.class, Boolean.class };

            EnrichmentControllerAbstract controllerInstance = (EnrichmentControllerAbstract) controller.getConstructor(paramTypes)
                    .newInstance(id, itemModelClassList, updateAll);
            controllerInstance.enrichAction();


            // Send email reports
            emailReportService.sendReports(false);

   
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
