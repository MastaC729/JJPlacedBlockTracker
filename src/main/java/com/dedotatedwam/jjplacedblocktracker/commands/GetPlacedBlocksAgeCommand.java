package com.dedotatedwam.jjplacedblocktracker.commands;

import com.dedotatedwam.jjplacedblocktracker.JJPlacedBlockTracker;
import com.dedotatedwam.jjplacedblocktracker.Util;
import com.dedotatedwam.jjplacedblocktracker.storage.LocationRow;
import com.dedotatedwam.jjplacedblocktracker.storage.SQLManager;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GetPlacedBlocksAgeCommand implements CommandExecutor {

	private Logger logger;
	private SQLManager sqlManager;

	public GetPlacedBlocksAgeCommand(Logger logger, SQLManager sqlManager) {
		this.logger = logger;
		this.sqlManager = sqlManager;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		String firstArg = "";		// MUST be player, block_name, or timeframe, but CANNOT be empty
		String secondArg = "";		// Can be a block_name, timeframe, or empty
		String thirdArg = "";		// Can be a timeframe or empty

		Optional<String> optArg = args.getOne("player");
		if (optArg.isPresent())
			firstArg = optArg.get();
		optArg = args.getOne("block_name");
		if (optArg.isPresent())
			secondArg = optArg.get();
		optArg = args.getOne("timeframe");
		if (optArg.isPresent())
			thirdArg = optArg.get();

		Optional<User> userOptional;

		if (firstArg.length() > 2 && firstArg.length() <= 16)
			userOptional = Sponge.getServiceManager().provide(UserStorageService.class).get().get(firstArg);
		else userOptional = Optional.empty();

		// Let's find out which of the three possible arguments player is
		// If they're not a valid player name and they're not a block on the block whitelist and they're not a valid timeframe
		if (!userOptional.isPresent()
				&& !JJPlacedBlockTracker.config.isBlockOnWhitelist(firstArg)
				&& !Util.isTimeframeCorrectFormat(thirdArg)) {
			src.sendMessage(Text.of(TextColors.RED, "Invalid first argument!"));
			return CommandResult.empty();
		} else if (userOptional.isPresent()) {		// A valid username for the first argument
			// Let's find out which of the two possible arguments block_name is
			if (JJPlacedBlockTracker.config.isBlockOnWhitelist(secondArg)) {		// A block on the whitelist for the second argument
				if (Util.isTimeframeCorrectFormat(thirdArg)) {		// A valid timeframe for the third argument
					printPlacedBlocksAgePlayerBlock(userOptional.get().getUniqueId(), secondArg, thirdArg, src);
					return CommandResult.success();
				}
			}
			else if (Util.isTimeframeCorrectFormat(secondArg)) {		// A valid timeframe for the second argument
				printPlacedBlocksAgePlayer(userOptional.get().getUniqueId(), secondArg, src);
				return CommandResult.success();
			}
		} else if (JJPlacedBlockTracker.config.isBlockOnWhitelist(firstArg)) {		// A block on the whitelist for the first argument
			if (Util.isTimeframeCorrectFormat(secondArg)) {		// A valid timeframe for the second argument
				printPlacedBlocksAgeBlock(firstArg, secondArg, src);
				return CommandResult.success();
			}
		} else if (Util.isTimeframeCorrectFormat(firstArg)) {		// A valid timeframe for the first argument
			printAllPlacedBlocksAge(firstArg, src);
			return CommandResult.success();
		}

		src.sendMessage(Text.of(TextColors.RED, "Invalid format!"));
		return CommandResult.empty();
	}

	// Pretty prints to CommandSource src all blocks placed past a certain timeframe
	private void printAllPlacedBlocksAge(String timeframe, CommandSource src) {
		Optional<List<LocationRow>> allPlacedBlocksTime = sqlManager.getAllPlacedBlocksTime(timeframe);
		if (allPlacedBlocksTime.get().toString().equals("[]")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "No blocks placed before that timeframe."));
			CommandResult.success();
		}
		else {
			LocalDateTime expiry = LocalDateTime.ofInstant(Instant.ofEpochSecond(Util.convertTimeframe(timeframe)), ZoneId.systemDefault());
			UserStorageService uSS = Sponge.getServiceManager().provide(UserStorageService.class).get();	// Used to get username from LocationRow.playerid
			List<LocationRow> blocks = allPlacedBlocksTime.get();
			List<Text> messages = new ArrayList<>();
			messages.add(Text.of(TextColors.YELLOW, "====================================================="));
			messages.add(Text.of(TextColors.YELLOW, "Blocks placed before " + expiry.format(DateTimeFormatter.ISO_DATE_TIME) + ":"));
			for (LocationRow block : blocks) {
				messages.add(Text.of(TextColors.YELLOW, block.getBlock_name()
						+ " placed by "
						+ uSS.get(sqlManager.getUUID(block.getPlayer_id())).get().getName()
						+ " " + Util.timeFormatter(System.currentTimeMillis()/1000 - block.getTime())
						+ " ago in world " + Util.getWorldNameFromUUID(UUID.fromString(block.getWorld()))
						+ " at X: " + block.getX() + " Y: " + block.getY() + " Z: " + block.getZ()));
			}
			messages.add(Text.of(TextColors.YELLOW, "====================================================="));
			src.sendMessages(messages);
		}
	}

	// Pretty prints to CommandSource src all blocks placed by player of block type block_name past a certain timeframe
	private void printPlacedBlocksAgePlayerBlock(UUID player, String block_name, String timeframe, CommandSource src) {
		Optional<List<LocationRow>> placedBlocksTime = sqlManager.getAllPlacedBlocksTimeBlockPlayer(timeframe, player, block_name);
		String playerName = Sponge.getServiceManager().provide(UserStorageService.class).get().get(player).get().getName();
		if (placedBlocksTime.get().toString().equals("[]")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "No blocks of type " + block_name + " placed by "
					+ playerName + " before that timeframe."));
			CommandResult.success();
		}
		else {
			LocalDateTime expiry = LocalDateTime.ofInstant(Instant.ofEpochSecond(Util.convertTimeframe(timeframe)), ZoneId.systemDefault());
			List<LocationRow> blocks = placedBlocksTime.get();
			List<Text> messages = new ArrayList<>();
			messages.add(Text.of(TextColors.YELLOW, "====================================================="));
			messages.add(Text.of(TextColors.YELLOW, "Blocks of type " + block_name + " placed by " + playerName
					+  " before " + expiry.format(DateTimeFormatter.ISO_DATE_TIME) + ":"));
			for (LocationRow block : blocks) {
				messages.add(Text.of(TextColors.YELLOW, "In world " + Util.getWorldNameFromUUID(UUID.fromString(block.getWorld()))
						+ " at X: " + block.getX() + " Y: " + block.getY() + " Z: " + block.getZ() + " placed "
						+ Util.timeFormatter(System.currentTimeMillis()/1000 - block.getTime())
						+ " ago"));
			}
			messages.add(Text.of(TextColors.YELLOW, "====================================================="));
			src.sendMessages(messages);
		}
	}

	// Pretty prints to CommandSource src all blocks placed by player past a certain timeframe
	private void printPlacedBlocksAgePlayer(UUID player, String timeframe, CommandSource src) {
		Optional<List<LocationRow>> placedBlocksTimePlayer = sqlManager.getAllPlacedBlocksTimePlayer(timeframe, player);
		String playerName = Sponge.getServiceManager().provide(UserStorageService.class).get().get(player).get().getName();
		if (placedBlocksTimePlayer.get().toString().equals("[]")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "No blocks placed by " + playerName + " before that timeframe."));
			CommandResult.success();
		}
		else {
			LocalDateTime expiry = LocalDateTime.ofInstant(Instant.ofEpochSecond(Util.convertTimeframe(timeframe)), ZoneId.systemDefault());
			List<LocationRow> blocks = placedBlocksTimePlayer.get();
			List<Text> messages = new ArrayList<>();
			messages.add(Text.of(TextColors.YELLOW, "====================================================="));
			messages.add(Text.of(TextColors.YELLOW, "Blocks placed by " + playerName
					+  " before " + expiry.format(DateTimeFormatter.ISO_DATE_TIME) + ":"));
			for (LocationRow block : blocks) {
				messages.add(Text.of(TextColors.YELLOW, "Block type " + block.getBlock_name() + " placed "
						+ Util.timeFormatter(System.currentTimeMillis()/1000 - block.getTime())
						+ " ago in world " + Util.getWorldNameFromUUID(UUID.fromString(block.getWorld()))
						+ " at X: " + block.getX() + " Y: " + block.getY() + " Z: " + block.getZ()));
			}
			messages.add(Text.of(TextColors.YELLOW, "====================================================="));
			src.sendMessages(messages);
		}
	}

	// Pretty prints to CommandSource src all blocks of type block_name past a certain timeframe
	private void printPlacedBlocksAgeBlock(String block_name, String timeframe, CommandSource src) {
		Optional<List<LocationRow>> placedBlocksTimeBlock = sqlManager.getAllPlacedBlocksTimeBlock(timeframe, block_name);
		if (placedBlocksTimeBlock.get().toString().equals("[]")) {
			src.sendMessage(Text.of(TextColors.YELLOW, "No blocks placed of block type " + block_name + " before that timeframe."));
			CommandResult.success();
		}
		else {
			LocalDateTime expiry = LocalDateTime.ofInstant(Instant.ofEpochSecond(Util.convertTimeframe(timeframe)), ZoneId.systemDefault());
			UserStorageService uSS = Sponge.getServiceManager().provide(UserStorageService.class).get();	// Used to get username from LocationRow.playerid
			List<LocationRow> blocks = placedBlocksTimeBlock.get();
			List<Text> messages = new ArrayList<>();
			messages.add(Text.of(TextColors.YELLOW, "====================================================="));
			messages.add(Text.of(TextColors.YELLOW, "Blocks of type "
					+  " before " + expiry.format(DateTimeFormatter.ISO_DATE_TIME) + ":"));
			for (LocationRow block : blocks) {
				messages.add(Text.of(TextColors.YELLOW, uSS.get(sqlManager.getUUID(block.getPlayer_id())).get().getName() + " placed "
						+ Util.timeFormatter(System.currentTimeMillis()/1000 - block.getTime())
						+ " ago in world " + Util.getWorldNameFromUUID(UUID.fromString(block.getWorld()))
						+ " at X: " + block.getX() + " Y: " + block.getY() + " Z: " + block.getZ()));
			}
			messages.add(Text.of(TextColors.YELLOW, "====================================================="));
			src.sendMessages(messages);
		}
	}
}