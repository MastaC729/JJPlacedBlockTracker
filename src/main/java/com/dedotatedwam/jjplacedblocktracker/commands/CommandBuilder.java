package com.dedotatedwam.jjplacedblocktracker.commands;


import com.dedotatedwam.jjplacedblocktracker.JJPlacedBlockTracker;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CommandBuilder {

	private final JJPlacedBlockTracker plugin;
	private Logger logger;

	public CommandBuilder(JJPlacedBlockTracker plugin, Logger logger) {
		this.plugin = plugin;
		this.logger = logger;
	}

	public void buildCommands () {
		// Command /getplacedblocks [player] [block_name]
		Sponge.getCommandManager().register(plugin, CommandSpec.builder()
				.description(Text.of("Reports the number of blocks you placed that are of a certain type on the whitelist."))
				.permission("jjplacedblocktracker.commands.getplacedblocks")
				.arguments(GenericArguments.firstParsing(
						GenericArguments.string(Text.of("player")),
						GenericArguments.string(Text.of("block_name"))),
						GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("block_name")))))
				.executor(new GetPlacedBlocksCommand())
				.build(), "getplacedblocks", "getpb");

		// Command /getallplacedblocks [player]
		Sponge.getCommandManager().register(plugin, CommandSpec.builder()
				.description(Text.of("Reports the number of all blocks you placed that are on the whitelist."))
				.permission("jjplacedblocktracker.commands.getallplacedblocks")
				.arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("player")))))
				.executor(new GetAllPlacedBlocksCommand())
				.build(), "getallplacedblocks", "getapb");
	}
}
