package com.dedotatedwam.jjplacedblocktracker.storage;


import com.dedotatedwam.jjplacedblocktracker.JJPlacedBlockTracker;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

public class SQLManager {

	@Inject
	private Logger logger;

	private static SqlService sql;
	private static DataSource dataSource;

	private static javax.sql.DataSource getDataSource(String jdbcUrl) throws SQLException {
		if (sql == null) {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
		}
		return sql.getDataSource(jdbcUrl);
	}

	// Initializes the following databases:
	// players: converts the player's UUID to a more lightweight int
	// locations: stores each block placed within the whitelist
	public static void init() throws SQLException {
		Connection conn = dataSource.getConnection();
		Statement stmt = conn.createStatement();

		try {
			// Table for UUID --> player id conversion for Sanic speed
			stmt.executeUpdate("CREATE table IF NOT EXISTS players ( "
					+ "id INT AUTO_INCREMENT, "		// AUTO_INCREMENT is pretty neat
					+ "uuid VARCHAR(36) );");
			// Table for total amount of placed blocks per player
			// player_id: obtained from players table
			// block_name: obtained from config
			// world, x, y, and z: obtained from getBlockState
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS locations ( "
					+ "player_id INT, "
					+ "block_name VARCHAR(36), "
					+ "world VARCHAR(36), "
					+ "x INT, "
					+ "y INT, "
					+ "z INT );");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	// Gets the player id from the players database based on their UUID
	// If the player does not exist yet in players, a new row is created for the new player
	private int getPlayerID (UUID player_UUID) {

		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement("SELECT id from players where UUID =  ? ;");
			stmt.setString(1, player_UUID.toString());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			else {
				PreparedStatement insertUser = conn.prepareStatement("INSERT INTO players (uuid) VALUES ( ? );");
				insertUser.setString(1, player_UUID.toString());
				insertUser.executeUpdate();
				rs = stmt.executeQuery();
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (Exception e) {
			logger.error("Error getting player ID from database! ", e);
		}
		return -1;
	}

	// Add the recently placed block to the locations database
	public void addPlacedBlock (UUID player_UUID, String block_name, UUID world, int x, int y, int z) {
		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO locations (player_id, block_name, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?);");
			stmt.setInt(1, getPlayerID(player_UUID));
			stmt.setString(2, block_name);
			stmt.setString(3, world.toString());
			stmt.setInt(4, x);
			stmt.setInt(5, y);
			stmt.setInt(6, z);
			stmt.executeUpdate();
		} catch (Exception e) {
			logger.error("Error adding placed block entry to database! ", e);
		}
	}

	// Remove the block on the list of whitelisted placed blocks from the locations database
	public void removePlacedBlock (UUID world, int x, int y, int z) {
		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(
					"DELETE FROM locations where world = ? and x = ? and y = ? and z = ?;");
			stmt.setString(1, world.toString());
			stmt.setInt(2, x);
			stmt.setInt(3, y);
			stmt.setInt(4, z);
			stmt.executeUpdate();
		} catch (Exception e) {
			logger.error("Error removing placed block from database! ", e);
		}
	}

	// Gets the amount of blocks of block_name placed by player with id player_id
	public int getAmount (String block_name, UUID player_UUID) {
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
			logger.error("Error getting amount of placed blocks of name " + block_name +
					" from player " + player_UUID.toString() + "! ", e);
		}
		logger.debug("" + amt);
		return 0;
	}

	public SQLManager(Logger logger) {
		this.logger = logger;
		try {
			dataSource = getDataSource("jdbc:h2:" + JJPlacedBlockTracker.getParentDirectory().getAbsolutePath()
					+ "/" + "jjdatabase");        //TODO Make this database name configurable
		} catch (SQLException e) {
			logger.error("Error while getting the data source! ", e);
		}
	}
}
