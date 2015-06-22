package com.github.eugene.containers;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.experimental.Builder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Getter
public class Scenario {
	
	private String name;
	private String type;
	private List<JSONObject> stepsJSON = new ArrayList<JSONObject>();
	private List<Step> steps = new ArrayList<Step>();
	
	public Scenario(String scenarioName, String scenarioType, JSONArray scenarioSteps) {
		this.name = scenarioName;
		this.type = scenarioType;
		stepsJSON = scenarioSteps;
		
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
