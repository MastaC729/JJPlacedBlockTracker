package com.dedotatedwam.jjplacedblocktracker.commands;

import com.dedotatedwam.jjplacedblocktracker.JJPlacedBlockTracker;
import com.dedotatedwam.jjplacedblocktracker.config.BlockEntry;
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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

//TODO rewrite this whole class, it makes me want to drink bleach
public class GetAllPlacedBlocksCommand implements CommandExecutor {

	private Logger logger;
	private SQLManager sqlManager;

	public GetAllPlacedBlocksCommand(Logger logger, SQLManager sqlManager) {
		this.logger = logger;
		this.sqlManager = sqlManager;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		UUID targetPlayer = null;
		User targetUser = null;

		// Check to see if a player was given and if the player is a valid username
		if (args.<String>getOne("player").isPresent()) {
			String playerArg = args.<String>getOne("player").get();
			if (playerArg.length() > 2 && playerArg.length() <= 16) {
				Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
				Optional<User> playerArgTest = userStorage.get().get(playerArg);
				if (!playerArgTest.isPresent()) {
					src.sendMessage(Text.of(TextColors.RED, "User is invalid or has not logged into this server yet!"));
					return CommandResult.empty();
				}
				targetPlayer = playerArgTest.get().getUniqueId();
				targetUser = playerArgTest.get();

			} else {
				src.sendMessage(Text.of(TextColors.RED, "Invalid username!"));
				return CommandResult.empty();
			}
		}

		if (src instanceof ConsoleSource || src instanceof CommandBlockSource) {
			// If they didn't specify a player in the arguments
			if (targetPlayer == null) {
				src.sendMessage(Text.of(TextColors.RED, "You must specify a player!")); // Because consoles can't place blocks ya jiving janko
				src.sendMessage(Text.of(TextColors.RED, "Usage: /getallplacedblock [player]"));
				return CommandResult.empty();
			} else {
				Map<String, Integer> amts = new HashMap<>();

				List<BlockEntry> blockEntries = JJPlacedBlockTracker.config.getBlockWhitelist();

				for (BlockEntry blockEntry : blockEntries) {
					String block_name = blockEntry.getName();
					int amt = sqlManager.getAmount(block_name, targetPlayer);
					amts.put(block_name, amt);
				}

				// If the player has not placed any whitelisted blocks
				if (amts.isEmpty()) {
					src.sendMessage(Text.of(TextColors.YELLOW, "Player " + targetPlayer + "has not placed any blocks that are on the whitelist."));
					return CommandResult.success();
				}

				// Pretty print the list of placed blocks and their amount / total allowed blocks
				List<Text> messages = new ArrayList<>();
				messages.add(Text.of(TextColors.YELLOW, "====================================================="));
				messages.add(Text.of(TextColors.YELLOW, "Placement stats for " + targetUser.getName() + ":"));
				for (Map.Entry<String, Integer> entry : amts.entrySet()) {
					messages.add(Text.of(TextColors.YELLOW, entry.getKey() + ": " + entry.getValue()
							+ "/" + JJPermissions.getPlacedBlocksPermissions(targetUser, entry.getKey()) + " blocks"));
				}
				messages.add(Text.of(TextColors.YELLOW, "====================================================="));

				src.sendMessages(messages);
				return CommandResult.success();
			}
		}
		// If the command source is a player
		else if (src instanceof Player) {
			// If they didn't specify a name - check perms and look up their amount
			if (targetPlayer == null) {
				if (src.hasPermission("jjplacedblocktracker.commands.getallplacedblocks.self")) {

					Map <String, Integer> amts = new HashMap<>();

					List<BlockEntry> blockEntries = JJPlacedBlockTracker.config.getBlockWhitelist();

					for (BlockEntry blockEntry : blockEntries) {
						String block_name = blockEntry.getName();
						int amt = sqlManager.getAmount(block_name, ((Player) src).getUniqueId());
						amts.put(block_name, amt);
					}

					// If the player has not placed any whitelisted blocks
					if (amts.isEmpty()) {
						src.sendMessage(Text.of(TextColors.YELLOW, "You have not placed any blocks that are on the whitelist."));
						return CommandResult.success();
					}

					// Pretty print the list of placed blocks and their amount / total allowed blocks
					List<Text> messages = new ArrayList<>();
					messages.add(Text.of(TextColors.YELLOW, "====================================================="));
					messages.add(Text.of(TextColors.YELLOW, "Placement stats: "));
					for (Map.Entry<String, Integer> entry : amts.entrySet()) {
						messages.add(Text.of(TextColors.YELLOW, entry.getKey() + ": " + entry.getValue()
								+ "/" + JJPermissions.getPlacedBlocksPermissions((Player) src, entry.getKey()) + " blocks"));
					}
					messages.add(Text.of(TextColors.YELLOW, "====================================================="));

					src.sendMessages(messages);
					return CommandResult.success();
				}
				else {
					src.sendMessage(Text.of(TextColors.RED,"You do not have permission to do this, ya dangus."));
					return CommandResult.empty();
				}
			}
			// If they're looking up someone else or if the person they're looking up is themself
			else {
				if (src.hasPermission("jjplacedblocktracker.commands.getplacedblocks.other")
						|| ((Player) src).getPlayer().get().getUniqueId().equals(targetPlayer)) {
					Map<String, Integer> amts = new HashMap<>();

					List<BlockEntry> blockEntries = JJPlacedBlockTracker.config.getBlockWhitelist();

					for (BlockEntry blockEntry : blockEntries) {
						String block_name = blockEntry.getName();
						int amt = sqlManager.getAmount(block_name, targetPlayer);
						amts.put(block_name, amt);
					}

					// If the player has not placed any whitelisted blocks
					if (amts.isEmpty()) {
						src.sendMessage(Text.of(TextColors.YELLOW, "Player "
								+ targetUser.getName()
								+ "has not placed any blocks that are on the whitelist."));
						return CommandResult.success();
					}

					// Pretty print the list of placed blocks and their amount / total allowed blocks
					List<Text> messages = new ArrayList<>();
					messages.add(Text.of(TextColors.YELLOW, "====================================================="));
					messages.add(Text.of(TextColors.YELLOW, "Placement stats for " + targetUser.getName() + ":"));
					for (Map.Entry<String, Integer> entry : amts.entrySet()) {
						messages.add(Text.of(TextColors.YELLOW, entry.getKey() + ": " + entry.getValue()
								+ "/" + JJPermissions.getPlacedBlocksPermissions(targetUser, entry.getKey()) + " blocks"));
					}
					messages.add(Text.of(TextColors.YELLOW, "====================================================="));

					src.sendMessages(messages);
					return CommandResult.success();
				}
				else {
					src.sendMessage(Text.of(TextColors.RED,"You do not have permission to do this, ya dangus."));
					return CommandResult.empty();
				}
			}
		}
		return CommandResult.empty();
	}
}