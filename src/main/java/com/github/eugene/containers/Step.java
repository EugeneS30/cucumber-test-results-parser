package com.github.eugene.containers;

import lombok.extern.slf4j.Slf4j;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

@Slf4j
public class Step {

	private String name;
	private String result;
	private long duration;
	private String output;

	public Step(String name, JSONObject result, String output) {

		this.name = name;
		this.result = (String) result.get("status");
		if ("skipped".equals(this.result)) {
			log.debug("SKIPPED test. Setting duration to -1");
			this.duration = 0;
		} else if ("undefined".equals(this.result)) {
			log.debug("UNDEFINED test.  Setting duration to -2");
			this.duration = 0;
		} else {
			this.duration = (Long) result.get("duration");
		}
		this.output = output;

	}

	public boolean isFailed() {
		if ("failed".equalsIgnoreCase(result)) {
			return true;
		}

		return false;
	}

	public boolean isUndefined() {
		if ("undefined".equalsIgnoreCase(result)) {
			return true;
		}

		return false;
	}

	public boolean isSkipped() {
		if ("skipped".equalsIgnoreCase(result)) {
			return true;
		}

		return false;
	}

	public String getName() {
		return name;
	}

	public String getOutput() {
		if (!"empty".equals(output)) {
			return output;
		}

		return "empty";
	}
}
