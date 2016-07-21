package com.github.eugene.containers;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UniqueScenario implements Scenario {

    private int buildNum;
    private Scenario scenario;
    private String runResult;
    private List<Tag> tags;

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

    public List<Tag> getTags() {
        List<Tag> tags = new ArrayList<Tag>();

        for (Tag tag : this.tags) {
            tags.add(tag);
        }

        return tags;
    }

    @Override
    public String getScenarioType() {
        return this.scenario.getScenarioType();
    }

    @Override
    public String getScreenShotPath() {
        return this.scenario.getScreenShotPath();
    }

    @Override
    public String getUri() {
        return this.scenario.getUri();
    }

    @Override
    public boolean isFailed() {
        return this.scenario.isFailed();
    }

}
