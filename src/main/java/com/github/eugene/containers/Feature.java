package com.github.eugene.containers;

import java.util.List;

public interface Feature {
    
    List<Scenario> getScenarios();
    
    int getScenariosNumber();
    
    String getName();
    
    String getId();
    
    String getUri();    

}
