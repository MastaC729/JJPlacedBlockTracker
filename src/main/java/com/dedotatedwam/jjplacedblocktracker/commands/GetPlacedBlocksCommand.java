package com.dedotatedwam.jjplacedblocktracker.commands;


import com.dedotatedwam.jjplacedblocktracker.JJPlacedBlockTracker;
import com.dedotatedwam.jjplacedblocktracker.permissions.JJPermissions;
import com.dedotatedwam.jjplacedblocktracker.storage.SQLManager;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class GetPlacedBlocksCommand implements CommandExecutor {

	private Logger logger;
	private SQLManager sqlManager;

	public GetPlacedBlocksCommand(Logger logger, SQLManager sqlManager) {
		this.logger = logger;
		this.sqlManager = sqlManager;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		Player targetPlayer = null;
		String block_name;

		// Check to see if a block name was given and if the block name is in the whitelist
		if (args.<String>getOne("block_name").isPresent()) {
			block_name = args.<String>getOne("block_name").get();

			if (!JJPlacedBlockTracker.config.isBlockOnWhitelist(block_name)) {
				src.sendMessage(Text.of(TextColors.RED, "This block is not in the whitelist!"));
				return CommandResult.empty();
			}
		}
		else {
			block_name = null;
		}

		// If the player argument is actually a block_name argument in disguise
		if (args.<String>getOne("player").isPresent() && block_name == null) {
			String playerArg = args.<String>getOne("player").get();
			if (playerArg.length() > 2 && playerArg.length() < 15) {		// A valid Minecraft username
				Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
				Optional<User> playerArgTest = userStorage.get().get(playerArg);
				if (playerArgTest.isPresent()) {
					src.sendMessage(Text.of(TextColors.RED, "A player cannot be a block, ya dangus!"));
					return CommandResult.empty();
				}
			}
			block_name = args.<String>getOne("player").get();
			if (!JJPlacedBlockTracker.config.isBlockOnWhitelist(block_name)) {
				src.sendMessage(Text.of(TextColors.RED, "This block is not in the whitelist!"));
				return CommandResult.empty();
			}
		}
		// Check to see if a player was given and if the player is a valid username
		else  {
			String playerArg = args.<String>getOne("player").get();
			Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
			Optional<User> playerArgTest = userStorage.get().get(playerArg);
			if (!playerArgTest.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, "User is invalid or has not logged into this server yet!"));
				return CommandResult.empty();
			}
			targetPlayer = userStorage.get().get(playerArg).get().getPlayer().get();
		}

		// User is likely checking for usage of the command - no arguments or only player given w/ no block name
		//if ((targetPlayer == null && block_name == null) || block_name == null) {
			//src.sendMessage(Text.of(TextColors.RED, "Usage: /getplacedblocks [player] [block_name]"));
			//src.sendMessage(Text.of(TextColors.RED, "        /getplacedblocks [block_name]"));
		//	return CommandResult.success();
		//}

		JJPermissions jjPerms = new JJPermissions();

		// If the command source is a console or a command block
		if (src instanceof ConsoleSource || src instanceof CommandBlockSource) {
			// If they didn't specify a player in the arguments
			if (targetPlayer == null) {
				src.sendMessage(Text.of(TextColors.RED, "You must specify a player!")); // Because consoles can't place blocks ya jiving janko
				return CommandResult.empty();
			}

			// Assuming they gave both a player and a block_name
			int amt = sqlManager.getAmount(block_name, targetPlayer.getUniqueId());
			src.sendMessage(Text.of(TextColors.YELLOW, "Player " + targetPlayer.get(Keys.DISPLAY_NAME).get().toPlain() +
					" has placed the following amount of block type " + block_name + ": " + amt + "/"
					+ jjPerms.getPlacedBlocksPermissions(targetPlayer, block_name) + " blocks"));
			return CommandResult.success();
		}
		// If the command source is a player
		else if (src instanceof Player) {
			// If they didn't specify a name - check perms and look up their amount
			if (targetPlayer == null) {
				if (src.hasPermission("jjplacedblocktracker.commands.getplacedblocks.self")) {
					int amt = sqlManager.getAmount(block_name, ((Player) src).getUniqueId());
					src.sendMessage(Text.of(TextColors.YELLOW, "You have placed the following amount of block type " + block_name + ": " + amt + "/"
							+ jjPerms.getPlacedBlocksPermissions(((Player) src).getPlayer().get(), block_name) + " blocks"));
					return CommandResult.success();
				} else {
					src.sendMessage(Text.of(TextColors.RED,"You do not have permission to do this, ya dangus."));
					return CommandResult.empty();
				}
			}
			// If they're looking up someone else
			else {
				if (src.hasPermission("jjplacedblocktracker.commands.getplacedblocks.other")) {
					int amt = sqlManager.getAmount(block_name, targetPlayer.getUniqueId());
					src.sendMessage(Text.of(TextColors.YELLOW, "Player " + targetPlayer.get(Keys.DISPLAY_NAME).get().toPlain() +
							" has have placed the following amount of block type " + block_name + ": " + amt + "/"
							+ jjPerms.getPlacedBlocksPermissions(targetPlayer, block_name) + " blocks"));
					return CommandResult.success();
				} else {
					src.sendMessage(Text.of(TextColors.RED,"You do not have permission to do this, ya dangus."));
					return CommandResult.empty();
				}
			}
		}
		return CommandResult.empty();
	}
}

// TODO Move all messages from this class and all other classes to a dedicated class for messaging the player or the console, as well as reporting things to the logger.