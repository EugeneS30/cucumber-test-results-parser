package com.github.eugene.containers;

public class BeforeHook implements Hook{
        
    private Long duration;
    private String status;
    private String location;
    
    public BeforeHook(Long duration, String status, String location) {
        this.duration = duration;
        this.status = status;
        this.location = location;
    }

}
