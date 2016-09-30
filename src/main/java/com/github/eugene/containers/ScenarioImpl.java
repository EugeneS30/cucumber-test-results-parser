package com.github.eugene.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ScenarioImpl implements Scenario {
    final static Logger log = Logger.getLogger(Scenario.class);

    private String scenarioName;
    private String uri;
    private String scenarioType;
    private List<BeforeHook> beforeHooks = new ArrayList<BeforeHook>();
    private List<AfterHook> afterHooks = new ArrayList<AfterHook>();
    private List<Step> steps = new ArrayList<Step>();
    private List<Tag> tags = new ArrayList<Tag>();
    private String runResult;
    private String outputPath;

    // CONSTRUCTOR
    public ScenarioImpl(String scenarioName, 
                    String uri, 
                    String scenarioType, 
                    JSONArray scenarioSteps, 
                    JSONArray scenarioBeforeHooks,
                    JSONArray scenarioAfterHooks, 
                    JSONArray tags
                   ) throws NullPointerException {

        log.debug("Scenario constructor start: " + scenarioName);
        log.debug("Scenario type: " + scenarioType);
        
        if (scenarioSteps == null) {
            throw new NullPointerException("This scenario doesn't seem to contain any steps! Skipping...");
        }

        this.scenarioName = scenarioName;
        this.scenarioType = scenarioType;
        this.uri = uri;

        List<JSONObject> stepsJSON = new ArrayList<JSONObject>(scenarioSteps);
        List<JSONObject> beforeHooksJSON = new ArrayList<JSONObject>(scenarioBeforeHooks);
        List<JSONObject> afterHooksJSON = new ArrayList<JSONObject>(scenarioAfterHooks);
        List<JSONObject> tagsJSON = new ArrayList<JSONObject>();
        if (tags != null) {
            tagsJSON = new ArrayList<JSONObject>(tags);
        }
        
        log.debug("Adding steps");
        for (JSONObject ob : stepsJSON) {
            String name = (String) ob.get("name");
            JSONObject result = (JSONObject) ob.get("result");
                      
            try {
                outputPath = ob.get("output").toString();
                
            } catch (Throwable e) {
                
            }
            
            steps.add(new Step(name, result, outputPath));
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
                runResult = "Pending";
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
        
        log.debug("Adding tags");
        for (JSONObject ob : tagsJSON) {
            String name = (String) ob.get("name").toString().substring(1);
            
            if ("Classes".equals(name)) {
                this.tags.add(Tag.Classes);
            }
            else if ("Communities".equals(name)) {
                this.tags.add(Tag.Communities);
            }
            else if ("Core".equals(name)) {
                this.tags.add(Tag.Core);
            }
            else if ("Discussions".equals(name)) {
                this.tags.add(Tag.Discussions);
            }
            else if ("Feedback".equals(name)) {
                this.tags.add(Tag.Feedback);
            }
            else if ("KnownIssue".equals(name)) {
                this.tags.add(Tag.KnownIssue);
            }
            else if ("LearningContent".equals(name)) {
                this.tags.add(Tag.LearningContent);
            }
            else if ("Manual".equals(name)) {
                this.tags.add(Tag.Manual);
            }
            else if ("Marksbook".equals(name)) {
                this.tags.add(Tag.Marksbook);
            }
            else if ("Notices".equals(name)) {
                this.tags.add(Tag.Notices);
            }
            else if ("RequiresHover".equals(name)) {
                this.tags.add(Tag.RequiresHover);
            }
            else if ("Submissions".equals(name)) {
                this.tags.add(Tag.Submissions);
            }
            else if ("smoke".equals(name)) {
                this.tags.add(Tag.smoke);
            }
            
            
        }

        if (!"pending".equalsIgnoreCase(runResult)) {

            runResult = "Pass";
            for (Step step : steps) {
                if (step.isFailed() || step.isSkipped() || step.isUndefined()) {
                    runResult = "Fail";
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
    
    private Step getLastStep() {
        return steps.get(steps.size() - 1);
    }
    
    @Override
    public String getScreenShotPath() {
        Step step = getLastStep();
        
        return step.getOutput();
    }

    @Override
    public String generateUriScenarioPair() {
        return uri + "," + scenarioName;
    }

    @Override
    public String getScenarioType() {
        return scenarioType;
    }

    @Override
    public String getScenarioName() {
        return scenarioName;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getRunResult() {
        return runResult;
    }
    
    @Override
    public List<Tag> getTags() {
        return tags;
    }

}
