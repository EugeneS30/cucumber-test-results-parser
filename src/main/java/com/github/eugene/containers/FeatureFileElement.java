package com.github.eugene.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.eugene.config.PropertiesContainer;

/**
 * FeatureFileElement container
 * <p>
 * Contains all data from the results of a single feature file
 * 
 * @author eugene.shragovich
 *
 */

public class FeatureFileElement {

	final static Logger log = Logger.getLogger(FeatureFileElement.class);
	
	private final static String featuresFolder = PropertiesContainer.getProperty("featuresFolder");

	private String featureFileElementName;
	private String featureFileElementUri;
	private List<JSONObject> scenariosJSON = new ArrayList<JSONObject>();
	private List<Scenario> scenarios = new ArrayList<Scenario>();
	private List<BackgroundScenario> backgroundScenarios = new ArrayList<BackgroundScenario>();
	private String featureFileElementPath;

	public FeatureFileElement(String name, String uri, JSONArray scenarios) {
		log.debug("==============================");
		log.debug("FeatureFileElement constructor start" + name);

		featureFileElementName = name;
		featureFileElementUri = uri.substring(uri.indexOf(featuresFolder));
		scenariosJSON = scenarios;
		featureFileElementPath = name + "," + uri;

		initiateScenarios();
	}

	public List<Scenario> getScenarios() {
		return scenarios;
	}

	private void initiateScenarios() {
		if (scenariosJSON == null) {
			log.warn("This FeatureFileElement does not contain any scenarios! Skipping...");
			return;
		}
		for (JSONObject ob : scenariosJSON) {
			String scenarioName = (String) ob.get("name");
			String scenarioType = (String) ob.get("type");
			JSONArray scenarioSteps = (JSONArray) ob.get("steps");
			JSONArray scenarioBeforeHooks = (JSONArray) ob.get("before");
			JSONArray scenarioAfterHooks = (JSONArray) ob.get("after");
			JSONArray tags = (JSONArray) ob.get("tags");

			if ("scenario".equals(scenarioType)) {
				try {
					scenarios.add(new ScenarioImpl(scenarioName, featureFileElementUri, scenarioType, scenarioSteps,
							scenarioBeforeHooks, scenarioAfterHooks, tags));
				}

				catch (NullPointerException e) {
					e.getMessage();

				}

			} else if ("background".equals(scenarioType)) {
				backgroundScenarios.add(new BackgroundScenario(scenarioName, scenarioType, scenarioSteps,
						scenarioBeforeHooks, scenarioAfterHooks));
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder returnString = new StringBuilder();

		returnString.append("Name:" + featureFileElementName + "\n");
		returnString.append("uri:" + featureFileElementUri + "\n");
		returnString.append("Steps:" + scenarios + "\n");

		return returnString.toString();
	}

	public String getPath() {
		return featureFileElementName + "," + featureFileElementUri;
	}

	public void getScenariosResults() {
		for (Scenario scenario : scenarios) {
			System.out.println(scenario.isFailed());
		}
	}

	public Map<String, Boolean> generateScenarioResultPairs() {
		Map<String, Boolean> scenarioResultPairs = new HashMap<String, Boolean>();

		for (Scenario scenario : scenarios) {
			if (!"background".equals(scenario.getScenarioType())) {

				String key = featureFileElementPath + "," + scenario.getScenarioName();
				Boolean value = scenario.isFailed();

				log.debug("Entry :" + key + ":" + value);

				scenarioResultPairs.put(key, value);
			}

		}

		return scenarioResultPairs;
	}

}
