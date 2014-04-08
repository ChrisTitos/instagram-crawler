package nl.wisdelft.instagram.crawler.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The DBManager provides convenience functions for opening and closing a database connection
 * 
 */
public class DBManager {

	private String url;
	private String user;
	private String pw;

	private String DRIVER_NAME;

	private Connection connection;

	public DBManager(String url, String user, String password, String drivername) {
		this.url = url;
		this.user = user;
		this.pw = password;
		DRIVER_NAME = drivername;
	}

	public Connection connect() {
		try {
			System.out.println("searching for driver");
			Class.forName(DRIVER_NAME);
			connection = DriverManager.getConnection(url + "?allowMultiQueries=true", user, pw);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connection;

	}

	public void disconnect() {

		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
