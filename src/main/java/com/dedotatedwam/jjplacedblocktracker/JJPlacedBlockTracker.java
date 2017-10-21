package com.dedotatedwam.jjplacedblocktracker;

import com.dedotatedwam.jjplacedblocktracker.commands.CommandBuilder;
import com.dedotatedwam.jjplacedblocktracker.config.JJConfig;
import com.dedotatedwam.jjplacedblocktracker.storage.SQLManager;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@Plugin(id = "jjplacedblocktracker", name = "JJPlacedBlockTracker", version = "1.1.1")
public class JJPlacedBlockTracker {

	@Inject
	private Logger logger;
	private static JJPlacedBlockTracker instance;
	public static JJConfig config;
	private static PluginContainer plugin;
	private SQLManager sqlManager;
	@Inject @ConfigDir(sharedRoot = false) private Path configDir;
	@Inject @DefaultConfig(sharedRoot = false) private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	@Inject Game game;

	public static Subject GLOBAL_SUBJECT;
	public PermissionService permissionService;

	@Listener
	public void preInit(GamePreInitializationEvent event) throws SQLException {
		JJPlacedBlockTracker.instance = this;
		plugin = Sponge.getPluginManager().getPlugin("jjplacedblocktracker").get();
		logger.info("UltimateChat Phynix version " + plugin.getVersion() + " is loading...");
		try {
			Files.createDirectories(configDir);
			config = JJConfig.fromLoader(configLoader);
		} catch (Exception e) {
			logger.warn("Error loading configuration!", e);
		}

		sqlManager = new SQLManager();

		// Set custom options for block whitelist - handles max placed blocks
		// JJPermissions.setOptionPermissions();

		// Registration of permission descriptions - is skipped if no permissions plugin is installed
		// I don't know how to do this yet, so screw it
		/* Optional<PermissionDescription.Builder> optBuilder = permissionService.newDescriptionBuilder(this);
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
		}*/
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

		logger.info("JJPlacedBlockTracer version such and such has finished loading and has started.");	//TODO Change this to use PluginContainer, see UltimateChat
	}

	@Listener
	public void onServerStop(GameStoppedServerEvent event) {

	}

	public static JJConfig getConfig() {
		return JJPlacedBlockTracker.instance.config;
	}

	public static Path getConfigDir() {
		return JJPlacedBlockTracker.instance.configDir;
	}

	public static Logger getLogger() { return JJPlacedBlockTracker.instance.logger; }
}
