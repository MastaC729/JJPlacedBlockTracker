package com.dedotatedwam.jjplacedblocktracker.storage;


import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLManager {

	@Inject
	private Logger logger;

	private SqlService sql;
	private String dataSource = "jdbc:h2:~/jjplacedblocktrackerdatabase.db";		//TODO Make this database name configurable

	public javax.sql.DataSource getDataSource(String jdbcUrl) throws SQLException {
		if (sql == null) {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
		}
		return sql.getDataSource(jdbcUrl);
	}

	public void testQuery() throws SQLException {
		Connection conn = getDataSource(dataSource).getConnection();
		try {
			//conn.prepareStatement("SELECT * FROM test_tbl").execute();
			conn.prepareStatement("SELECT * FROM test_tbl").execute();
		} finally {
			conn.close();
		}

	}

	// Test method to make sure SQL is actually working :(
	public void testTable() throws SQLException {

		Statement stmt = null;
		ResultSet rs;

		Connection conn = getDataSource(dataSource).getConnection();
		try {
			// Create a table
			conn.prepareStatement("CREATE table test_table ( "
					+ "test_column1 INT( 4 ), "
					+ "test_column2 TEXT, "
					+ "test_column3 TEXT ) IF NOT EXISTS;");
			//Insert a row into the table with some values
			conn.prepareStatement("INSERT into test_table ("
					+ "test_column1, test_column2, test_column3) "
					+ "values ('1234', 'HEYO', 'WAZZUP');");
			//See if those values saved to the table
			String query = "SELECT test_column1, test_column2, test_column3 FROM test_table;";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				int test_column1_value = rs.getInt("test_column1");
				String test_column2_value = rs.getString("test_column2");
				String test_column3_value = rs.getString("test_column3");
				System.out.println(test_column1_value + "\t" + test_column2_value + "\t" + test_column3_value);
			}
		} catch (Exception e) {
			logger.error(e.getCause().getMessage());
		} finally {
			if (stmt != null) { stmt.close(); }
			conn.close();
		}
	}
}
