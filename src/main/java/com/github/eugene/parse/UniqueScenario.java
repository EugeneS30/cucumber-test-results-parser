package com.github.eugene.parse;

import lombok.Getter;

import com.github.eugene.containers.Scenario;

@Getter
public class UniqueScenario {
    private int buildNum;
    private Scenario scenario;
    private boolean scenarioResult;
    
    public UniqueScenario(int buildNum, Scenario scenario, boolean scenarioResult) {
        this.buildNum = buildNum;
        this.scenario = scenario;
        this.scenarioResult = scenarioResult;
    }
    
    
}
