package com.github.eugene.containers;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * FeatureFileElement container
 * <p>
 * Contains all data from the results of a single feature file
 * 
 * @author eugene.shragovich
 *
 */

@Slf4j
public class FeatureFileElement {

    private String name;
    private String uri;
    private List<JSONObject> scenariosJSON = new ArrayList<JSONObject>();
    private List<Scenario> scenarios = new ArrayList<Scenario>();

    public FeatureFileElement(String name, String uri, JSONArray scenarios) {
        log.info("==============================");
        log.info("FeatureFileElement constructor start" + name);
        
        this.name = name;
        this.uri = uri;
        scenariosJSON = scenarios;

        initiateScenarios();
    }

    private void initiateScenarios() {
        for (JSONObject ob : scenariosJSON) {
            String scenarioName = (String) ob.get("name");
            String scenarioType = (String) ob.get("type");
            JSONArray scenarioSteps = (JSONArray) ob.get("steps");
            JSONArray scenarioBeforeHooks = (JSONArray) ob.get("before");
            JSONArray scenarioAfterHooks = (JSONArray) ob.get("after");
            
            scenarios.add(new Scenario(scenarioName, scenarioType, scenarioSteps, scenarioBeforeHooks, scenarioAfterHooks));
        }
    }

    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();

        returnString.append("Name:" + name + "\n");
        returnString.append(" uri:" + uri + "\n");
        returnString.append(" Steps:" + scenarios + "\n");

        return returnString.toString();
    }

    public String getPath() {
        return name + "," + uri;
    }

    public void getScenariosResults() {
        for (Scenario scenario : scenarios) {
            System.out.println(scenario.isFailed());
        }
    }

}
