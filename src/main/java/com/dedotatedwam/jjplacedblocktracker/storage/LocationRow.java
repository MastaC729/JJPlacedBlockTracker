package com.dedotatedwam.jjplacedblocktracker.storage;

public class LocationRow {
	private int player_id;
	private long time;
	private String block_name;
	private String world;
	private int x;
	private int y;
	private int z;

	LocationRow (int player_id, long time, String block_name, String world, int x, int y, int z) {
		this.player_id = player_id;
		this.time = time;
		this.block_name = block_name;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getPlayer_id () {
		return this.player_id;
	}

	public long getTime() {
		return time;
	}

	public String getBlock_name() {
		return block_name;
	}

	public String getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}
}
