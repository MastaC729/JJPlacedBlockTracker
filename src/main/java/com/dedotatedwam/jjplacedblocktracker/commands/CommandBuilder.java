package com.dedotatedwam.jjplacedblocktracker.commands;


import com.dedotatedwam.jjplacedblocktracker.JJPlacedBlockTracker;
import com.dedotatedwam.jjplacedblocktracker.storage.SQLManager;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CommandBuilder {

	private JJPlacedBlockTracker plugin;
	private SQLManager sqlManager;
	private Logger logger;

	public CommandBuilder(JJPlacedBlockTracker plugin, Logger logger, SQLManager sqlManager) {
		this.plugin = plugin;
		this.logger = logger;
		this.sqlManager = sqlManager;
	}

	public void buildCommands () {

		// Command /getplacedblocks [player]|[block_name] [block_name]
		Sponge.getCommandManager().register(plugin, CommandSpec.builder()
				.description(Text.of("Reports the number of blocks you placed that are of a certain type on the whitelist."))
				.permission("jjplacedblocktracker.commands.getplacedblocks.self")
				.arguments(GenericArguments.firstParsing(
						GenericArguments.requiringPermission(
								GenericArguments.string(Text.of("player")),"jjplacedblocktracker.commands.getplacedblocks.other"),
						GenericArguments.string(Text.of("block_name"))),
						GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.string(Text.of("block_name")))))
				.executor(new GetPlacedBlocksCommand(logger, sqlManager))
				.build(), "getplacedblocks", "getpb");

		// Command /getallplacedblocks [player]
		Sponge.getCommandManager().register(plugin, CommandSpec.builder()
				.description(Text.of("Reports the number of all blocks you placed that are on the whitelist."))
				.permission("jjplacedblocktracker.commands.getallplacedblocks.self")
				.arguments(GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.onlyOne(
						GenericArguments.string(Text.of("player"))),"jjplacedblocktracker.commands.getallplacedblocks.other")))
				.executor(new GetAllPlacedBlocksCommand(logger, sqlManager))
				.build(), "getallplacedblocks", "getapb");
	}
}
