package com.dedotatedwam.jjplacedblocktracer.data;

// Manages the data containers for the number of current placed blocks and the list of placed blocks

import org.spongepowered.api.block.BlockState;

import java.util.List;

public class JJDataManager {
	List currPB = new List(new List (new String, new List(BlockState)));
}
