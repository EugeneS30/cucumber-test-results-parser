package com.github.eugene.containers;


public class UniqueScenario { 
    private int buildNum;
    private Scenario scenario;
    private String runResult;
    
    public UniqueScenario(int buildNum, Scenario scenario, String runResult) {
        this.buildNum = buildNum;
        this.scenario = scenario;
        this.runResult = runResult;
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
    
}
