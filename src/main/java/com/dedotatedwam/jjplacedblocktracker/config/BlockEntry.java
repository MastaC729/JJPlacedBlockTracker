package com.dedotatedwam.jjplacedblocktracker.config;

import com.dedotatedwam.jjplacedblocktracker.JJPlacedBlockTracker;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ConfigSerializable
public class BlockEntry {

	@Setting
	private String id;
	@Setting
	private String name;
	@Setting
	private TraitList traits;

	public BlockEntry() {
	}

	public BlockEntry(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean equals(BlockState blockState) {
		String incomingId = blockState.getId();
		// MIGHT BE A BIG BUG BOY.
		if (incomingId.contains("[")) {
			incomingId = incomingId.split("\\[")[0];
		}
		// Return false immediately if the state ID doesn't match ours.
		if (!id.equals(incomingId)) {
			return false;
		}
		for (Map.Entry<BlockTrait<?>, ?> entry : blockState.getTraitMap().entrySet()) {
			String key = entry.getKey().getName();
			Object value = entry.getValue();
			// Continue to next cycle if a trait present on the state isn't present here.
			if (traits == null || (traits != null && !traits.containsKey(key))) {
				continue;
			}
			Object present = traits.get(key);
			// Lowercase string values for ease of comparision.
			if (present instanceof String) {
				present = ((String) present).toLowerCase();
			}

			// Convert enum values to strings.
			if (value instanceof Enum) {
				value = ((Enum) value).name().toLowerCase();
			}

			if (!present.equals(value)) {
				return false;
			}
		}
		return true;
	}


	public Optional<BlockEntry> isWatchedBlock(BlockState state) {
		List<BlockEntry> blockEntries = JJPlacedBlockTracker.config.getBlockWhitelist();
		for (BlockEntry blockEntry : blockEntries) {
			if (blockEntry.equals(state)) {
				return Optional.of(blockEntry);
			}
		}
		return Optional.empty();
	}
}