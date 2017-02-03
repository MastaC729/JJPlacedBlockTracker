package com.dedotatedwam.jjplacedblocktracker;

import com.dedotatedwam.jjplacedblocktracker.commands.CommandBuilder;
import com.dedotatedwam.jjplacedblocktracker.config.JJConfig;
import com.dedotatedwam.jjplacedblocktracker.permissions.JJPermissions;
import com.dedotatedwam.jjplacedblocktracker.storage.SQLManager;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@Plugin(id = "jjplacedblocktracker", name = "JJPlacedBlockTracker", version = "1.0.0")
public class JJPlacedBlockTracker {

	@Inject
	private Logger logger;
	public static JJConfig config;
	public SQLManager sqlManager;
	@Inject @ConfigDir(sharedRoot = false) private Path configDir;
	@Inject @DefaultConfig(sharedRoot = false) private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	@Inject private Game game;

	public static Subject GLOBAL_SUBJECT;
	public PermissionService permissionService;

	@Listener
	public void preInit(GamePreInitializationEvent event) throws SQLException {
		try {
			Files.createDirectories(configDir);
			config = JJConfig.fromLoader(configLoader);
		} catch (Exception e) {
			logger.warn("Error loading default configuration!", e);
		}

		sqlManager = new SQLManager(logger, configDir);

		GLOBAL_SUBJECT = this.permissionService.getDefaults();

		// Set custom options for block whitelist - handles max placed blocks
		JJPermissions.setOptionPermissions();

		// Registration of permission descriptions - is skipped if no permissions plugin is installed
		if (game.getServiceManager().provide(PermissionService.class).isPresent()) {
			permissionService = game.getServiceManager().provideUnchecked(PermissionService.class);
			JJPermissions.registerPD(PermissionDescription.ROLE_ADMIN, "jjplacedblocktracker.whitelist.unlimited",
					"Allows the user to place an unlimited amount of anything on the block whitelist for this plugin.", permissionService);
			JJPermissions.registerPD(PermissionDescription.ROLE_USER, "jjplacedblocktracker.commands.getplacedblocks.self",
					"Allows the user to check how many blocks they placed of a certain type on the whitelist.", permissionService);
			JJPermissions.registerPD(PermissionDescription.ROLE_USER, "jjplacedblocktracker.commands.getallplacedblocks.self",
					"Allows the user to check how many blocks they placed of every whitelisted block.", permissionService);
			JJPermissions.registerPD(PermissionDescription.ROLE_STAFF, "jjplacedblocktracker.commands.getplacedblocks.other",
					"Allows the user to check how many blocks someone else placed of a certain type on the whitelist.", permissionService);
			JJPermissions.registerPD(PermissionDescription.ROLE_STAFF, "jjplacedblocktracker.commands.getallplacedblocks.other",
					"Allows the user to check how many blocks someone else placed of every whitelisted block.", permissionService);
		}
		else {
			logger.info("Skipping registration of permission descriptions, no permissions plugin installed!");
		}
	}

	@Listener
	public void init(GameInitializationEvent event) {
		game.getEventManager().registerListeners(this, new BlockListeners(logger, sqlManager));
	}

	@Listener
	public void postInit(GamePostInitializationEvent event) {

	}

	@Listener
	public void onServerStart(GameStartedServerEvent event){
		CommandBuilder cBuilder = new CommandBuilder(this, logger, sqlManager);
		cBuilder.buildCommands();

		logger.info("JJPlacedBlockTracer has finished loading and has started.");
	}

	@Listener
	public void onServerStop(GameStoppedServerEvent event) {

	}

	public static JJConfig getConfig() {
		return config;
	}

	public Path getConfigDir() {
		return configDir;
	}

	public Logger getLogger() {
		return logger;
	}
}
