package com.github.eugene.parse;

import lombok.Getter;

import com.github.eugene.containers.Scenario;

@Getter
public class UniqueScenario {
    private int buildNum;
    private Scenario scenario;
    private boolean isFailed;
    
    public UniqueScenario(int buildNum, Scenario scenario, boolean isFailed) {
        this.buildNum = buildNum;
        this.scenario = scenario;
        this.isFailed = isFailed;
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
