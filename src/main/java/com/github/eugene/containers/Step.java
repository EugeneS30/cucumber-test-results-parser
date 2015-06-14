package com.github.eugene.containers;

import org.json.simple.JSONObject;

public class Step {

	private String name;
	private String result;

	public Step(String name, JSONObject result) {
		this.name = name;
		this.result = (String) result.get("status");
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
