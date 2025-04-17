/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.File;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import ch.zhaw.hsb.aurora.enricher.Main;
import ch.zhaw.hsb.aurora.enricher.Model.Item.ItemAbstract;

/**
 * This class is a service for CSV files.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class CSVService {

    public static List<Map<String, String>> readCSVIntoMap(String filePath) {
        List<Map<String, String>> records = new ArrayList<>();

        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(filePath);  // Pfad zu deiner Datei im Ressourcenordner
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            if (inputStream == null) {
                throw new IOException("File not found.");
            }

            for (CSVRecord csvRecord : csvParser) {
                records.add(csvRecord.toMap());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }

    public static void writeItemAndEnrichmentToCSV(List<ItemAbstract> items, String path) {

        File file = new File(path);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            List<Method> itemGetters = getGetters(items.get(0).getClass());

            List<String> headers = new ArrayList<>();
            for (Method itemGetter : itemGetters) {
                headers.add(itemGetter.getName().substring(3)); // Remove 'get' from method name
            }

            try (CSVPrinter csvPrinter = new CSVPrinter(writer,
                    CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])))) {
                for (ItemAbstract item : items) {
                    List<Object> values = new ArrayList<>();
                    for (Method itemGetter : itemGetters) {
                        values.add(itemGetter.invoke(item));
                    }

                    csvPrinter.printRecord(values);
                }
            }

        } catch (IOException | ReflectiveOperationException e) {
            e.printStackTrace();
        }

    }

    private static List<Method> getGetters(Class<?> reflectionClass) {
        List<Method> getters = new ArrayList<>();
        Set<String> methodNames = new HashSet<>();

        while (reflectionClass != null) {
            for (Method method : reflectionClass.getDeclaredMethods()) {
                if (isGetter(method) && !methodNames.contains(method.getName())) {
                    getters.add(method);
                    methodNames.add(method.getName());
                }
            }
            reflectionClass = reflectionClass.getSuperclass();
        }

        return getters;
    }

    private static boolean isGetter(Method method) {
        if (!method.getName().startsWith("get"))
            return false;
        if (method.getParameterCount() != 0)
            return false;
        if (void.class.equals(method.getReturnType()))
            return false;
        return true;
    }

}
