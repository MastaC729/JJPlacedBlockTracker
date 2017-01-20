package com.dedotatedwam.jjplacedblocktracker.config;

import java.util.HashMap;
import java.util.Map;

public class TraitList {

	private Map<String, Object> traits = new HashMap<>();

	public boolean containsKey(String key) {
		return traits.containsKey(key);
	}

	public void put(String key, Object value) {
		traits.put(key, value);
	}

	public Object get(String key) {
		return traits.get(key);
	}

	public Map<String, Object> getTraits() {
		return traits;
	}

}