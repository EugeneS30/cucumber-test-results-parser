package com.github.eugene.containers;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

public class Step {
    final static Logger log = Logger.getLogger(Step.class);

	private String name;
	private String result;
	private long duration;

	public Step(String name, JSONObject result) {
	    log.debug("Step constructor start...");
	    
		this.name = name;
		this.result = (String) result.get("status");
		if ("skipped".equals(this.result)) {
		    log.debug("SKIPPED test. Setting duration to -1");
		    this.duration = -1;
		}
		else if ("undefined".equals(this.result)) {
		    log.debug("UNDEFINED test.  Setting duration to -2");
		    this.duration = -2;
		}
		else {
		    this.duration = (Long) result.get("duration");
		}

	}

	public boolean isFailed() {
		if ("failed".equals(result)) {
			return true;
		}

		return false;
	}
	
	public String getName() {
		return name;
	}

}
