package com.github.eugene.containers;

public class AfterHook {
    
    private Long duration;
    private String status;
    private String location;
    
    public AfterHook(Long duration, String status, String location) {
        this.duration = duration;
        this.status = status;
        this.location = location;
    }

}
