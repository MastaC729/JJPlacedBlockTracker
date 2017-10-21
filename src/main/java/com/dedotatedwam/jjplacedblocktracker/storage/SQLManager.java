package com.dedotatedwam.jjplacedblocktracker.storage;


import com.dedotatedwam.jjplacedblocktracker.JJPlacedBlockTracker;
import com.dedotatedwam.jjplacedblocktracker.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SQLManager {

	private SqlService sql;
	private DataSource dataSource;

	private javax.sql.DataSource getDataSource(String jdbcUrl) throws SQLException {
		if (sql == null) {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
		}
		return sql.getDataSource(jdbcUrl);
	}

	// Gets the player id from the players database based on their UUID
	// If the player does not exist yet in players, a new row is created for the new player
	private int getPlayerID(UUID player_UUID) {

		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement("SELECT id from players where UUID =  ? ;");
			stmt.setString(1, player_UUID.toString());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			} else {
				PreparedStatement insertUser = conn.prepareStatement("INSERT INTO players (uuid) VALUES ( ? );");
				insertUser.setString(1, player_UUID.toString());
				insertUser.executeUpdate();
				rs = stmt.executeQuery();
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (Exception e) {
			JJPlacedBlockTracker.getLogger().error("Error getting player ID from database! ", e);
		}
		return -1;
	}

	// Add the recently placed block to the locations database
	public void addPlacedBlock(UUID player_UUID, String block_name, UUID world, int x, int y, int z) {
		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO locations (player_id, time, block_name, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?, ?);");
			stmt.setInt(1, getPlayerID(player_UUID));
			stmt.setLong(2, System.currentTimeMillis() / 1000);        // Gets the current Epoch time in seconds
			stmt.setString(3, block_name);
			stmt.setString(4, world.toString());
			stmt.setInt(5, x);
			stmt.setInt(6, y);
			stmt.setInt(7, z);
			stmt.executeUpdate();
		} catch (Exception e) {
			JJPlacedBlockTracker.getLogger().error("Error adding placed block entry to database! ", e);
		}
	}

	// Remove the block on the list of whitelisted placed blocks from the locations database
	public void removePlacedBlock(UUID world, int x, int y, int z) {
		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(
					"DELETE FROM locations where world = ? and x = ? and y = ? and z = ?;");
			stmt.setString(1, world.toString());
			stmt.setInt(2, x);
			stmt.setInt(3, y);
			stmt.setInt(4, z);
			stmt.executeUpdate();
		} catch (Exception e) {
			JJPlacedBlockTracker.getLogger().error("Error removing placed block from database! ", e);
		}
	}

	// Gets the amount of blocks of block_name placed by player with id player_id
	public int getAmount(String block_name, UUID player_UUID) {
		int amt = 0;
		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(
					"SELECT COUNT(*) FROM locations WHERE block_name = ? and player_id = ?;");
			stmt.setString(1, block_name);
			stmt.setInt(2, getPlayerID(player_UUID));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				amt = rs.getInt(1);
				return amt;
			}
		} catch (Exception e) {
			JJPlacedBlockTracker.getLogger().error("Error getting amount of placed blocks of name " + block_name +
					" from player " + player_UUID.toString() + "! ", e);
		}
		JJPlacedBlockTracker.getLogger().debug("" + amt);
		return 0;
	}

	/*	Gets the blocks placed within a specified timeframe
		Used in the command /getplacedblocksage <timeframe>
		NOTE: Assumes the timeframe's format has been approved already!	*/
	public Optional<List<LocationRow>> getAllPlacedBlocksTime(String timeframe) {
		if (Util.isTimeframeCorrectFormat(timeframe)) {
			long timeCheck = Util.convertTimeframe(timeframe);
			try (Connection conn = dataSource.getConnection()) {
				PreparedStatement stmt = conn.prepareStatement("SELECT * FROM locations WHERE time <= ?;");
				stmt.setLong(1, timeCheck);
				ResultSet rs = stmt.executeQuery();
				List<LocationRow> blocks = new ArrayList<>();
				while (rs.next()) {
					blocks.add(new LocationRow(rs.getInt(1), rs.getLong(2), rs.getString(3),
							rs.getString(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)));
				}
				return Optional.of(blocks);
			} catch (Exception e) {
				JJPlacedBlockTracker.getLogger().error("Error getting amount of placed blocks from time " + timeCheck + "! ", e);
			}
		}
		return null;
	}

	/* Gets the blocks placed within a specified timeframe and by a specified player
		Used in the command /getplacedblocksage <player> <timeframe>
		NOTE: Assumes the timeframe's format has been approved already!	*/
	public Optional<List<LocationRow>> getAllPlacedBlocksTimePlayer(String timeframe, UUID player_UUID) {
		if (Util.isTimeframeCorrectFormat(timeframe)) {
			long timeCheck = Util.convertTimeframe(timeframe);
			try (Connection conn = dataSource.getConnection()) {
				PreparedStatement stmt = conn.prepareStatement("SELECT player_id, time, block_name, world, x, y, z " +
						"FROM locations WHERE time <= ? and player_id = ?;");
				stmt.setLong(1, timeCheck);
				stmt.setInt(2, getPlayerID(player_UUID));
				ResultSet rs = stmt.executeQuery();
				List<LocationRow> blocks = new ArrayList<>();
				while (rs.next()) {
					blocks.add(new LocationRow(rs.getInt(1), rs.getLong(2), rs.getString(3),
							rs.getString(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)));
				}
				return Optional.of(blocks);
			} catch (Exception e) {
				JJPlacedBlockTracker.getLogger().error("Error getting amount of placed blocks from time " + timeCheck + " and player " + player_UUID.toString() + "! ", e);
			}
		}
		return null;
	}

	/* Gets the blocks placed within a specified timeframe and by a specified block name
	Used in the command /getplacedblocksage <player> <timeframe>
	NOTE: Assumes the timeframe's format has been approved already!	*/
	public Optional<List<LocationRow>> getAllPlacedBlocksTimeBlock(String timeframe, String block_name) {
		if (Util.isTimeframeCorrectFormat(timeframe)) {
			long timeCheck = Util.convertTimeframe(timeframe);
			try (Connection conn = dataSource.getConnection()) {
				PreparedStatement stmt = conn.prepareStatement("SELECT player_id, time, block_name, world, x, y, z " +
						"FROM locations WHERE time <= ? and block_name = ?;");
				stmt.setLong(1, timeCheck);
				stmt.setString(2, block_name);
				ResultSet rs = stmt.executeQuery();
				List<LocationRow> blocks = new ArrayList<>();
				while (rs.next()) {
					blocks.add(new LocationRow(rs.getInt(1), rs.getLong(2), rs.getString(3),
							rs.getString(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)));
				}
				return Optional.of(blocks);
			} catch (Exception e) {
				JJPlacedBlockTracker.getLogger().error("Error getting amount of placed blocks from time " + timeCheck + " and block name " + block_name + "! ", e);
			}
		}
		return null;
	}

	/* Gets the blocks placed within a specified timeframe and by a specified block name and player
	Used in the command /getplacedblocksage <player> <block_name> <timeframe>
	NOTE: Assumes the timeframe's format has been approved already!	*/
	public Optional<List<LocationRow>> getAllPlacedBlocksTimeBlockPlayer(String timeframe, UUID player_UUID, String block_name) {
		if (Util.isTimeframeCorrectFormat(timeframe)) {
			long timeCheck = Util.convertTimeframe(timeframe);
			try (Connection conn = dataSource.getConnection()) {
				PreparedStatement stmt = conn.prepareStatement("SELECT player_id, time, block_name, world, x, y, z " +
						"FROM locations WHERE time <= ? and player_id = ? and block_name = ?;");
				stmt.setLong(1, timeCheck);
				stmt.setInt(2, getPlayerID(player_UUID));
				stmt.setString(3, block_name);
				ResultSet rs = stmt.executeQuery();
				List<LocationRow> blocks = new ArrayList<>();
				while (rs.next()) {
					blocks.add(new LocationRow(rs.getInt(1), rs.getLong(2), rs.getString(3),
							rs.getString(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)));
				}
				return Optional.of(blocks);
			} catch (Exception e) {
				JJPlacedBlockTracker.getLogger().error("Error getting amount of placed blocks from time " + timeCheck + ", player "
						+ player_UUID.toString() + ", and block name " + block_name + "! ", e);
			}
		}
		return null;
	}

	// Initializes the following databases:
	// players: converts the player's UUID to a more lightweight int
	// locations: stores each block placed within the whitelist
	// NOTE: This should only need to be called once in the main class, then that instance should be used by everything else
	public SQLManager() {
		try {
			dataSource = getDataSource("jdbc:h2:" + JJPlacedBlockTracker.getConfigDir().toAbsolutePath()
					+ "/" + "jjdatabase");        //TODO Make this database name configurable
		} catch (SQLException e) {
			JJPlacedBlockTracker.getLogger().error("Error while getting the data source! ", e);
		}

		try (Connection conn = dataSource.getConnection()) {
			// Table for UUID --> player id conversion for Sanic speed
			PreparedStatement stmt = conn.prepareStatement("CREATE table IF NOT EXISTS players ( "
					+ "id INT AUTO_INCREMENT, "        // AUTO_INCREMENT is pretty neat
					+ "uuid VARCHAR(36) );");
			stmt.executeUpdate();

			// Table for total amount of placed blocks per player
			// player_id: obtained from players table
			// block_name: obtained from config
			// world, x, y, and z: obtained from getBlockState
			stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS locations ( "
					+ "player_id INT, "
					+ "time LONG, "
					+ "block_name VARCHAR(36), "
					+ "world VARCHAR(36), "
					+ "x INT, "
					+ "y INT, "
					+ "z INT );");
			stmt.executeUpdate();

		} catch (Exception e) {
			JJPlacedBlockTracker.getLogger().error("Error while creating new database or reading from existing one! ", e);
		}
	}

	public UUID getUUID(int player_ID) {

		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement("SELECT uuid from players where id =  ? ;");
			stmt.setInt(1, player_ID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return UUID.fromString(rs.getString(1));
			}
		} catch (SQLException e) {
			JJPlacedBlockTracker.getLogger().error("Error getting player UUID from database! ", e);
		}

		return null;
	}
}