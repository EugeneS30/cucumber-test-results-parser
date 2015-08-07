package com.github.eugene.parse;

import lombok.Getter;

import com.github.eugene.containers.Scenario;

@Getter
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
    
}
