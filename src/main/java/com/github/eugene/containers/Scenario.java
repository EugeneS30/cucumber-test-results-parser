package com.github.eugene.containers;

import java.util.List;


public interface Scenario {
    
    String generateUriScenarioPair();
    
    String getRunResult();
    
    String getScenarioName();
    
    String getScenarioType();
    
    String getScreenShotPath();
    
    List<Tag> getTags();
    
    String getUri();
    
    boolean isFailed();
    
}
