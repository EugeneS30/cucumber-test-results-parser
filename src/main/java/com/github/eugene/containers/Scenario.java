package com.github.eugene.containers;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Scenario {
	
	private String name;
	private String type;
	private List<JSONObject> stepsJSON = new ArrayList<JSONObject>();
	private List<Step> steps = new ArrayList<Step>();
	
	public Scenario(String name, String type, JSONArray steps) {
		this.name = name;
		this.type = type;
		stepsJSON = steps;
		
		initiateSteps();
	}
	
	private void initiateSteps() {
		for (JSONObject ob : stepsJSON) {
			String name = (String) ob.get("name");
			JSONObject result = (JSONObject) ob.get("result");
						
			steps.add(new Step(name, result));
		}
	}
	
	public boolean isFailed() {
		for (Step step : steps) {
			if (step.isFailed()) {
				return true;
			}
			
		}
		return false;
	}

}
