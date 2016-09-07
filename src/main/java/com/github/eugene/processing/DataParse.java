package com.github.eugene.processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
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

    @Setter
    @Getter
    private static int lastBuild;

    public static Map<Integer, List<FeatureFileElement>> extractBuildsData() throws FileNotFoundException, IOException {

        List<Integer> ignoredBuildsList = new ArrayList<Integer>();
        File ignoredBuildsFile = new File(System.getProperty("user.dir") + ConfigurationClass.ignoreFileRelativePath);

        log.info("Attempting to read the ignored builds file");
        try (BufferedReader br = new BufferedReader(new FileReader(ignoredBuildsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                ignoredBuildsList.add(Integer.parseInt(line));
            }
        }

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

            log.debug("Checking if build should be ignored...");
            if (ignoredBuildsList.contains(buildNumber)) {
                log.info("This build is set to be IGNORED. Continuing to the next build...");
                continue;
            }

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

    private static int getLastBuildNumber(Map<Integer, List<FeatureFileElement>> buildsResults) {

        int lastBuild = 0;

        for (Integer buildNum : buildsResults.keySet()) {
            if (buildNum > lastBuild) {
                lastBuild = buildNum;
            }
        }

        return lastBuild;

    }

    public static List<UniqueScenario> extractUniqueScenarios(Map<Integer, List<FeatureFileElement>> allBuildsResults) {

        setLastBuild(getLastBuildNumber(allBuildsResults));

        final List<UniqueScenario> allBuildResults = new ArrayList<UniqueScenario>();

        for (Entry<Integer, List<FeatureFileElement>> currentBuild : allBuildsResults.entrySet()) {
            for (FeatureFileElement featureFileElement : currentBuild.getValue()) {
                for (Scenario scenario : featureFileElement.getScenarios()) {
                    if (scenario.getTags().toString().contains("Manual")) {
                        log.debug("skipping scenario tagged as @Manual");
                        continue;
                    }
                    allBuildResults.add(new UniqueScenario(currentBuild.getKey(), scenario, scenario.getRunResult(), scenario.getTags()));
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

    private static List<Scenario> getLastBuildScenarios(List<UniqueScenario> uniqueScenariosList) {

        int lastBuildNumber = getLastBuild();
        List<Scenario> lastBuildScenariosList = new ArrayList<Scenario>();

        for (UniqueScenario scenario : uniqueScenariosList) {
            if (scenario.getBuildNum() == lastBuildNumber) {
                log.info("Scenario: {} was identified in last build", scenario.getScenarioName());
                lastBuildScenariosList.add(scenario);
            }
        }

        return lastBuildScenariosList;

    }

    public static List<UniqueScenario> removeLegacyScenarios(List<UniqueScenario> uniqueScenariosList) {

        // We will need to have a full list of scenarios in the last build to know which ones are
        // relevant at the moment.
        List<Scenario> lastBuildScenariosList = getLastBuildScenarios(uniqueScenariosList);
        List<UniqueScenario> uniqueScenarioNamesSetClean = new ArrayList<UniqueScenario>();

        log.debug("Looking for legacy scenarios");

        // Iterate over all unique scenarios
        for (UniqueScenario uniqueScenario : uniqueScenariosList) {
            // for each unique scenario check if it is in the last build
            for (Scenario scenario : lastBuildScenariosList) {
                if (uniqueScenario.getScenarioName().equals(scenario.getScenarioName())
                        && uniqueScenario.getUri().equals(scenario.getUri())) {
                    uniqueScenarioNamesSetClean.add(uniqueScenario);
                    break;
                }
            }

        }

        // return uniqueScenariosList;
        return uniqueScenarioNamesSetClean;

    }

}
