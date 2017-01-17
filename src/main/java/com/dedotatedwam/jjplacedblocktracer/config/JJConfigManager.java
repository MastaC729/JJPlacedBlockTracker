package com.dedotatedwam.jjplacedblocktracer.config;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.io.File;

// Initializes default config or loads modified config

public class JJConfigManager {
	private Logger logger;
	private File mainConfig;
	private ConfigurationLoader<CommentedConfigurationNode> loader;
	private File maxPB;
}
