package com.dedotatedwam.jjplacedblocktracker;

import com.dedotatedwam.jjplacedblocktracker.config.JJConfig;
import com.dedotatedwam.jjplacedblocktracker.storage.SQLManager;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@Plugin(id = "jjplacedblocktracker", name = "JJPlacedBlockTracker", version = "1.0.0")
public class JJPlacedBlockTracker {

	@Inject
	private Logger logger;

	private static JJConfig config;
	@Inject @ConfigDir(sharedRoot = true) private Path configDir;
	@Inject @DefaultConfig(sharedRoot = true) private ConfigurationLoader<CommentedConfigurationNode> configLoader;

	@Listener
	public void preInit(GamePreInitializationEvent event) {
		try {
			Files.createDirectories(configDir);
			config = JJConfig.fromLoader(configLoader);
		} catch (Exception e) {
			logger.warn("Error loading default configuration!");
		}

		SQLManager sqlManager = new SQLManager();
		try {
			sqlManager.testTable();
		} catch (SQLException e) {
			logger.error("YOU DUN GOOFED!");
		}

	}

	@Listener
	public void init(GameInitializationEvent event) {
	}

	@Listener
	public void postInit(GamePostInitializationEvent event) {

	}

	@Listener
	public void onServerStart(GameStartedServerEvent event){
		logger.info("JJPlacedBlockTracer has started.");
	}

	@Listener
	public void onServerStop(GameStoppedServerEvent event) {
		logger.info("JJPlacedBlockTracer has stopped.");
	}

	public static JJConfig getConfig() {
		return config;
	}
}
