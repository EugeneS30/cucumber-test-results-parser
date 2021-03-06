package com.github.eugene.containers;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class BackgroundScenario {
    final static Logger log = Logger.getLogger(BackgroundScenario.class);
    
	private String scenarioName;
	private String scenarioType;
	private List<BeforeHook> beforeHooks = new ArrayList<BeforeHook>();
	private List<AfterHook> afterHooks = new ArrayList<AfterHook>();
	private List<Step> steps = new ArrayList<Step>();
	int iter = 0;
	
	public BackgroundScenario(@NonNull String backgroundScenarioName, 
	        String backgroundScenarioType, 
	        JSONArray backgroundScenarioSteps, 
	        JSONArray backgroundScenarioBeforeHooks, 
	        JSONArray backgroundScenarioAfterHooks) throws NullPointerException {
	    
	    log.debug("Scenario constructor start: " + backgroundScenarioName);
	    log.debug("Scenario type: " + backgroundScenarioType);

	    this.scenarioName = backgroundScenarioName;
        this.scenarioType = backgroundScenarioType;
        	    
	    if ("scenario".equals(backgroundScenarioType)) {
	        List<JSONObject> stepsJSON = new ArrayList<JSONObject>(backgroundScenarioSteps);
	        List<JSONObject> beforeHooksJSON = new ArrayList<JSONObject>(backgroundScenarioBeforeHooks);
	        List<JSONObject> afterHooksJSON = new ArrayList<JSONObject>(backgroundScenarioAfterHooks);
	        
	        log.debug("Adding steps");
	        for (JSONObject ob : stepsJSON) {
	            String name = (String) ob.get("name");
	            JSONObject result = (JSONObject) ob.get("result");
	            String output = "empty";
	            try {
	                output = (String) ob.get("output");
	            } catch (Throwable e) {
	                
	            }
	                        
	            steps.add(new Step(name, result, output));
	        }
	        
	        log.debug("Adding beforeHooks");
	        for (JSONObject ob : beforeHooksJSON) {
	            JSONObject result = (JSONObject) ob.get("result");
	            JSONObject match = (JSONObject) ob.get("match");
	            
	            Long duration = (Long) result.get("duration");
	            String status = (String) result.get("status");
	            String location = (String) match.get("location");
	            
	            beforeHooks.add(new BeforeHook(duration, status, location));
	        }
	        
	        log.debug("Adding afterHooks");
	        for (JSONObject ob : afterHooksJSON) {
	            JSONObject result = (JSONObject) ob.get("result");
	            JSONObject match = (JSONObject) ob.get("match");
	            
	            Long duration = (Long) result.get("duration");
	            String status = (String) result.get("status");
	            String location = (String) match.get("location");
	            
	            afterHooks.add(new AfterHook(duration, status, location));
	        }
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
