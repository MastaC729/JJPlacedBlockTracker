package com.dedotatedwam.jjplacedblocktracker;


import com.dedotatedwam.jjplacedblocktracker.config.BlockEntry;
import com.dedotatedwam.jjplacedblocktracker.permissions.JJPermissions;
import com.dedotatedwam.jjplacedblocktracker.storage.SQLManager;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

final public class BlockListeners {

	private Logger logger;

	// Listens to see if a broken block is on the whitelist --> if it is, then it gets the necessary data
	// and removes it from the locations database
	@Listener
	public void onBreakBlock(ChangeBlockEvent.Break event, Logger logger, SQLManager sqlManager) {

		// If the break block event was caused by the server
		if (!event.getCause().first(Player.class).isPresent())
			return;

		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			BlockState state = transaction.getOriginal().getState();
			BlockEntry blockEntry = new BlockEntry();
			Optional<BlockEntry> blockEntryOptional = blockEntry.isWatchedBlock(state);
			// If the block is on the whitelist
			if (blockEntryOptional.isPresent()) {
				UUID world = event.getTargetWorld().getUniqueId();					// Get the world UUID
				int x = transaction.getFinal().getLocation().get().getBlockX();		// Get the block's x coord
				int y = transaction.getFinal().getLocation().get().getBlockY();		// etc
				int z = transaction.getFinal().getLocation().get().getBlockZ();		// etc

				sqlManager.removePlacedBlock(world, x, y, z);
			}
		}
	}

	@Listener
	public void onPlaceBlock(ChangeBlockEvent.Place event, Logger logger, SQLManager sqlManager) {
		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			BlockState state = transaction.getFinal().getState();
			BlockEntry blockEntry = new BlockEntry();
			Optional<BlockEntry> blockEntryOptional = blockEntry.isWatchedBlock(state);

			JJPermissions jjPerms = new JJPermissions();

			Player playerPlaced;
			if (event.getCause().first(Player.class).isPresent())
				playerPlaced = event.getCause().first(Player.class).get();
			else return;		// Don't do anything else because the server placed a block, not the player
			World world = event.getTargetWorld();

			// If the block isn't on the whitelist
			if (!blockEntryOptional.isPresent()) {
				// logger.debug("Block " + transaction.getFinal().getState().getName() + " is not on the whitelist.");
				return;		// Do nothing to the event
			}
			// If the player has the permission to place it
			else if (sqlManager.getAmount(getBlockName(state),
					playerPlaced.getUniqueId()) < jjPerms.getPlacedBlocksPermissions(playerPlaced, getBlockName(state))) {
				UUID player_UUID = playerPlaced.getUniqueId();
				UUID worldID = world.getUniqueId();					// Get the world UUID
				String block_name = getBlockName(state);			// The name this plugin considers the block as
				int x = transaction.getFinal().getLocation().get().getBlockX();		// Get the block's x coord
				int y = transaction.getFinal().getLocation().get().getBlockY();		// etc
				int z = transaction.getFinal().getLocation().get().getBlockZ();		// etc

				sqlManager.addPlacedBlock(player_UUID, block_name, worldID, x, y, z);
			}
			else {
				event.setCancelled(true);			// Prevent the player from placing the block
				playerPlaced.sendMessage(Text.of(TextColors.RED, "You cannot place any more of this."));
			}
		}
	}

	// Assumes the block is in the whitelist
	// If not, this will return NULL
	private String getBlockName (BlockState state) {
		List<BlockEntry> blockEntries = JJPlacedBlockTracker.config.getBlockWhitelist();
		for (BlockEntry blockEntry : blockEntries) {
			if (blockEntry.equals(state)) {
				return blockEntry.getName();
			}
		}
		return null;		// TODO this could cause some issues for dumdums that don't follow directions...
	}
}



