package com.github.eugene.containers;

import java.util.ArrayList;
import java.util.List;


public class UniqueScenario { 
    private int buildNum;
    private Scenario scenario;
    private String runResult;
    private List<Tag> tags;
    
    public UniqueScenario(int buildNum, Scenario scenario, String runResult, List<Tag> tags) {
        this.buildNum = buildNum;
        this.scenario = scenario;
        this.runResult = runResult;
        this.tags = tags;
    }
    
    public String generateUriScenarioPair() {
        return scenario.getUri() + "," + scenario.getScenarioName();
    }
    
    public String getScenarioName() {
        return scenario.getScenarioName();
    }
    
    public String getScenarioUri() {
        return scenario.getUri();
    }
    
    public int getBuildNum() {
        return buildNum;
    }
    
    public Scenario getScenario() {
        return scenario;
    }
    
    public String getRunResult() {
        return runResult;
    }
    
    public List<String> getTags() {
        List<String> tags = new ArrayList<String>();
        
        for (Tag tag : this.tags) {
            tags.add(tag.toString());
        }
        
        return tags;
    }
    
}
