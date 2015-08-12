package com.github.eugene.parse;

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

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.eugene.containers.FeatureFileElement;
import com.github.eugene.containers.Scenario;
import com.github.eugene.containers.UniqueScenario;

public class Parser {

    final static Logger log = Logger.getLogger(ParseResultsTest.class);

    private static final String buildsDirPath = "C:\\.jenkins\\jobs\\Connect Automated Functional Tests - Firefox 32\\builds";
    private static final String jsonRelativePath = "\\htmlreports\\Functional_Test_Report\\cucumber.json";

    public static void parse() {
        
        File dir = new File(buildsDirPath);
        File[] buildsDir = dir.listFiles();

        Map<Integer, List<FeatureFileElement>> buildsResults = new TreeMap<Integer, List<FeatureFileElement>>(Collections.reverseOrder());

        for (File buildPath : buildsDir) {

            int buildNumber = -1; // setting dummy value

            try {
                buildNumber = Integer.parseInt(buildPath.getName().toString());
            } catch (NumberFormatException e) {
                log.error("Invalid folder name: " + buildPath.getName().toString());
                continue;
            }

            log.info("Parsing buildResults: " + buildPath);

            try {
                FileReader reader = new FileReader(String.join("\\", buildPath.toString(), jsonRelativePath));

                JSONParser jsonParser = new JSONParser();

                JSONArray jsonFileData = (JSONArray) jsonParser.parse(reader);
                List<JSONObject> allJSONFileElementsList = new ArrayList<JSONObject>();
                allJSONFileElementsList.addAll(jsonFileData);

                List<FeatureFileElement> featureFileObjectsList = new ArrayList<FeatureFileElement>();

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

        // A list of UniqueScenario objects (buildNum, scenario, isFailed)
        List<UniqueScenario> allBuildResults = new ArrayList<UniqueScenario>();

        for (Entry<Integer, List<FeatureFileElement>> build : buildsResults.entrySet()) {
            for (FeatureFileElement ffe : build.getValue()) {
                for (Scenario scenario : ffe.getScenarios()) {
                    allBuildResults.add(new UniqueScenario(build.getKey(), scenario, scenario.getRunResult()));
                }
            }
        }

        // Generates a list of unique scenario names from all the builds.
        SortedSet<String> uniqueScenarioNamesSet = new TreeSet<String>();

        for (UniqueScenario scenario : allBuildResults) {
            uniqueScenarioNamesSet.add(scenario.generateUriScenarioPair());
        }

        // A list of all build numbers
        List<Integer> buildsList = new ArrayList<Integer>();
        for (Integer buildNum : buildsResults.keySet()) {
            buildsList.add(buildNum);
        }


    }

}
