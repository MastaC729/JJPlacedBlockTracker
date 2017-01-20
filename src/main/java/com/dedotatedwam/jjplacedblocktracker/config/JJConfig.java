package com.dedotatedwam.jjplacedblocktracker.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

// Initializes default config or loads modified config

@ConfigSerializable
public class JJConfig {

	private static final TypeToken<JJConfig> TYPE = TypeToken.of(JJConfig.class);

	static {
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(TraitList.class), new TraitListSerializer());
	}

	private final ConfigurationLoader<?> loader;
	private final ConfigurationNode node;
	@Setting("Block Whitelist") private List<BlockEntry> blockWhitelist;

	private JJConfig(ConfigurationLoader<?> loader, ConfigurationNode node) {
		this.loader = loader;
		this.node = node;
	}

	public static JJConfig fromLoader(ConfigurationLoader<?> loader) throws IOException {
		ConfigurationNode node = loader.load();
		ConfigurationNode fallbackConfig;
		try {
			fallbackConfig = loadDefaultConfiguration();
		}
		catch (IOException e) {
			throw new Error("Default configuration could not be loaded!");
		}
		node.mergeValuesFrom(fallbackConfig);

		JJConfig config = new JJConfig(loader, node);
		config.load();
		return config;
	}

	private void load() throws IOException {
		try {
			ObjectMapper.forObject(this).populate(node);
		} catch (ObjectMappingException e) {
			throw new IOException(e);
		}
		loader.save(node);
	}

	public void save() throws IOException {
		try {
			ObjectMapper.forObject(this).serialize(node);
		} catch (ObjectMappingException e) {
			throw new IOException(e);
		}
		loader.save(node);
	}

	public List<BlockEntry> getBlockWhitelist () {
		return blockWhitelist;
	}



	public static ConfigurationNode loadDefaultConfiguration() throws IOException {
		URL defaultConfig = JJConfig.class.getResource("default.conf");
		if (defaultConfig == null) {
			throw new Error("Default config is not present in jar.");
		}
		HoconConfigurationLoader fallbackLoader = HoconConfigurationLoader.builder().setURL(defaultConfig).build();
		return fallbackLoader.load();
	}
}