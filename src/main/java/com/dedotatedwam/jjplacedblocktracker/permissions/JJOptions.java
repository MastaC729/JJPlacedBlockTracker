package com.dedotatedwam.jjplacedblocktracker.permissions;


import com.dedotatedwam.jjplacedblocktracker.config.BlockEntry;
import com.dedotatedwam.jjplacedblocktracker.config.JJConfig;
import com.google.common.collect.Maps;

import java.util.Map;

public class JJOptions {

	public static final int DEFAULT_PLACED_BLOCK_AMOUNT = 1; // TODO Make this a config option

	// Create the default block whitelist max values for each block in the whitelist
	public static Map<String, Integer> createDefaultOptions(JJConfig config) {

		final Map<String, Integer> DEFAULT_OPTIONS = Maps.newHashMap();

		for (BlockEntry blockEntry : config.getBlockWhitelist()) {
			System.out.println("[JJPlacedBlockTracker][INFO] Block name " + blockEntry.getName() + " set to " + DEFAULT_PLACED_BLOCK_AMOUNT);
			DEFAULT_OPTIONS.put(blockEntry.getName(), DEFAULT_PLACED_BLOCK_AMOUNT);
		}

		return DEFAULT_OPTIONS;
	}
}
