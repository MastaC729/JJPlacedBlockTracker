package com.dedotatedwam.jjplacedblocktracer.main;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "jjplacedblocktracker", name = "JJPlacedBlockTracker", version = "1.0.0")
public class JJPlacedBlockTracker {

	@Inject
	private Logger logger;

	@Inject @DefaultConfig(sharedRoot = false) private Path defaultConfig;
	@Inject @DefaultConfig(sharedRoot = false) private ConfigurationLoader<CommentedConfigurationNode> loader;
	@Inject @ConfigDir(sharedRoot = false) private Path privateConfigDir;

	private ConfigurationNode config;

	@Listener
	public void preInit(GamePreInitializationEvent event) {
		try {
			config = loader.load();

			if (!defaultConfig.toFile().exists()) {
				config.getNode("placeholder").setValue(true);
				loader.save(config);
			}
		} catch (IOException e) {
			logger.warn("Error loading default configuration!");
		}
	}

	@Listener
	public void init(GameInitializationEvent event) {

	}

	@Listener
	public void postInit(GamePostInitializationEvent event) {

	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		logger.info("JJPlacedBlockTracer has started.");
	}

	@Listener
	public void onServerStop(GameStoppedServerEvent event) {
		logger.info("JJPlacedBlockTracer has stopped.");
	}
}
