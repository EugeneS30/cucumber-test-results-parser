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
			
	public Scenario(String scenarioName,
	                String uri,
	                String scenarioType, 
	                JSONArray scenarioSteps, 
	                JSONArray scenarioBeforeHooks, 
	                JSONArray scenarioAfterHooks) throws NullPointerException {
	    
	    log.info("Scenario constructor start: " + scenarioName);
	    log.info("Scenario type: " + scenarioType);

	    this.scenarioName = scenarioName;
        this.scenarioType = scenarioType;
        this.uri = uri;
        	    
	    if ("scenario".equals(scenarioType)) {
	        List<JSONObject> stepsJSON = new ArrayList<JSONObject>(scenarioSteps);
	        List<JSONObject> beforeHooksJSON = new ArrayList<JSONObject>(scenarioBeforeHooks);
	        List<JSONObject> afterHooksJSON = new ArrayList<JSONObject>(scenarioAfterHooks);
	        
	        log.info("Adding steps");
	        for (JSONObject ob : stepsJSON) {
	            String name = (String) ob.get("name");
	            JSONObject result = (JSONObject) ob.get("result");
	                        
	            steps.add(new Step(name, result));
	        }
	        
	        log.info("Adding beforeHooks");
	        for (JSONObject ob : beforeHooksJSON) {
	            JSONObject result = (JSONObject) ob.get("result");
	            JSONObject match = (JSONObject) ob.get("match");
	            
	            Long duration = (Long) result.get("duration");
	            String status = (String) result.get("status");
	            String location = (String) match.get("location");
	            
	            beforeHooks.add(new BeforeHook(duration, status, location));
	        }
	        
	        log.info("Adding afterHooks");
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
