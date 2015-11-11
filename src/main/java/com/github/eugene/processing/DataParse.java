package com.github.eugene.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.eugene.config.ConfigurationClass;
import com.github.eugene.containers.FeatureFileElement;
import com.github.eugene.containers.Scenario;
import com.github.eugene.containers.UniqueScenario;

@Slf4j
public class DataParse {
    private final static String buildsDirPath = ConfigurationClass.buildsDirPath;
    private final static String jsonRelativePath = ConfigurationClass.jsonRelativePath;

    public static Map<Integer, List<FeatureFileElement>> extractBuildsData() {

        File dir = new File(buildsDirPath);
        File[] buildsDir = dir.listFiles();

        Map<Integer, List<FeatureFileElement>> buildsResults = new TreeMap<Integer, List<FeatureFileElement>>(Collections.reverseOrder());

        JSONParser jsonParser = new JSONParser();

        for (File buildPath : buildsDir) {
            
            List<FeatureFileElement> featureFileObjectsList = new ArrayList<FeatureFileElement>();
            int buildNumber = -1; // setting dummy value

            try {
                buildNumber = Integer.parseInt(buildPath.getName().toString());
            } catch (NumberFormatException e) {
                log.warn("Not a valid folder name. Skipping...: " + buildPath.getName().toString());
                continue;
            }

            log.info("Parsing buildData: " + buildPath);

            try {
                FileReader reader = new FileReader(String.join("\\", buildPath.toString(), jsonRelativePath));

                JSONArray jsonFileData = (JSONArray) jsonParser.parse(reader);
                List<JSONObject> allJSONFileElementsList = new ArrayList<JSONObject>();
                allJSONFileElementsList.addAll(jsonFileData);

                int iter = 0;
                for (JSONObject featureFileElement : allJSONFileElementsList) {
                    
                    log.debug("Processing new featureFileElement: " + iter);
                    iter++ ;
                    
                    String name = (String) featureFileElement.get("name");
                    String uri = (String) featureFileElement.get("uri");
                    JSONArray elements = (JSONArray) featureFileElement.get("elements");

                    featureFileObjectsList.add(new FeatureFileElement(name, uri, elements));
                }

                buildsResults.put(buildNumber, featureFileObjectsList);

            }

            catch (FileNotFoundException e) {
                log.error("File not found: " + buildPath.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return buildsResults;

    }

    public static List<UniqueScenario> extractUniqueScenarios(Map<Integer, List<FeatureFileElement>> buildsResults) {

        final List<UniqueScenario> allBuildResults = new ArrayList<UniqueScenario>();

        for (Entry<Integer, List<FeatureFileElement>> build : buildsResults.entrySet()) {
            for (FeatureFileElement featureFileElement : build.getValue()) {
                for (Scenario scenario : featureFileElement.getScenarios()) {
                    allBuildResults.add(new UniqueScenario(build.getKey(), scenario, scenario.getRunResult(), scenario.getTags()));
                }
            }
        }
        return allBuildResults;
    }

    public static SortedSet<String> createUniqueScenriosNames(List<UniqueScenario> uniqueScenariosList) {
        
        log.debug("Extracting unique scenarios names");

        SortedSet<String> uniqueScenarioNamesSet = new TreeSet<String>();

        for (UniqueScenario scenario : uniqueScenariosList) {
            uniqueScenarioNamesSet.add(scenario.generateUriScenarioPair());
        }

        return uniqueScenarioNamesSet;

    }

    public static List<Integer> getAllBuildsNumbers(Map<Integer, List<FeatureFileElement>> buildsData) {
        
        log.debug("Getting builds numbers list");

        List<Integer> buildsList = new ArrayList<Integer>();
        for (Integer buildNum : buildsData.keySet()) {
            buildsList.add(buildNum);
        }

        return buildsList;

    }

}
