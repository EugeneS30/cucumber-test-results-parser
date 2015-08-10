package com.github.eugene.containers;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.experimental.Builder;
import lombok.extern.slf4j.Slf4j;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Getter
@Slf4j
public class Scenario {
	private String scenarioName;
	private String uri;
	private String scenarioType;
	private List<BeforeHook> beforeHooks = new ArrayList<BeforeHook>();
	private List<AfterHook> afterHooks = new ArrayList<AfterHook>();
	private List<Step> steps = new ArrayList<Step>();
	private String runResult;
			
	// CONSTRUCTOR
	public Scenario(String scenarioName,
	                String uri,
	                String scenarioType, 
	                JSONArray scenarioSteps, 
	                JSONArray scenarioBeforeHooks, 
	                JSONArray scenarioAfterHooks) throws NullPointerException {
	    
	    log.debug("Scenario constructor start: " + scenarioName);
	    log.debug("Scenario type: " + scenarioType);

	    this.scenarioName = scenarioName;
        this.scenarioType = scenarioType;
        this.uri = uri;
        	    
	    if ("scenario".equals(scenarioType)) {
	        List<JSONObject> stepsJSON = new ArrayList<JSONObject>(scenarioSteps);
	        List<JSONObject> beforeHooksJSON = new ArrayList<JSONObject>(scenarioBeforeHooks);
	        List<JSONObject> afterHooksJSON = new ArrayList<JSONObject>(scenarioAfterHooks);
	        
	        log.debug("Adding steps");
	        for (JSONObject ob : stepsJSON) {
	            String name = (String) ob.get("name");
	            JSONObject result = (JSONObject) ob.get("result");
	                        
	            steps.add(new Step(name, result));
	        }
	        
	        log.debug("Adding beforeHooks");
	        for (JSONObject ob : beforeHooksJSON) {
	            JSONObject result = (JSONObject) ob.get("result");
	            JSONObject match = (JSONObject) ob.get("match");
	            
	            Long duration = (Long) result.get("duration");
	            String status = (String) result.get("status");
	            String location = (String) match.get("location");
	            
	            beforeHooks.add(new BeforeHook(duration, status, location));
	            
	            if ("pending".equalsIgnoreCase(status)) {
	                runResult = "pending";
	            }
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
	    
	    if (!"pending".equalsIgnoreCase(runResult)) {
	        
	        runResult = "pass";
	        for (Step step : steps) {
	            if (step.isFailed()) {
	                runResult = "fail";
	                continue;
	            }
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
	
	 public String generateUriScenarioPair() {
	        return uri + "," + scenarioName;
	    }

}
