package nl.wisdelft.instagram.crawler.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import nl.wisdelft.instagram.crawler.api.InstagramConnectionManager;
import nl.wisdelft.instagram.crawler.entity.Caption;
import nl.wisdelft.instagram.crawler.entity.Location;
import nl.wisdelft.instagram.crawler.entity.Media;
import nl.wisdelft.instagram.crawler.entity.User;

import org.apache.commons.io.IOUtils;

public class InstagramInserter {

	private Connection connection;
	private nl.wisdelft.instagram.crawler.api.InstagramConnectionManager apiManager;
	private PreparedStatement instpost, instuser, instloc;
	private boolean isBatch = false;
	private nl.wisdelft.instagram.crawler.api.InstagramAPI api;

	public InstagramInserter(Connection conn, InstagramConnectionManager apiManager) throws SQLException, IOException {

		connection = conn;
		instpost = connection.prepareStatement("INSERT INTO inst_posts VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		instuser = connection.prepareStatement("INSERT INTO inst_users VALUES (?, ?, ?)");
		instloc = connection.prepareStatement("INSERT INTO inst_locations VALUES (?, ?, ?, ?)");
		this.apiManager = apiManager;
		api = apiManager.getAPIInstance();

		this.initTables();
	}

	private void initTables() throws IOException, SQLException {

		String createScript = IOUtils.toString(ClassLoader.getSystemResourceAsStream("create_tables"));
		connection.createStatement().execute(createScript);
	}

	/**
	 * Insert the full time line of the specified user in the db. This function calls the Instagram API
	 * 
	 * @param userId
	 * @throws SQLException
	 */
	public int insertUserFeed(long userId, long minTime, long maxTime) throws SQLException {
		List<Media> timeline = new ArrayList<Media>();
		int total = 0;
		boolean success = false;
		int added = 0;

		while (!success) {
			try {
				timeline = api.getUserFeed(userId, minTime, maxTime);
				success = true;
			} catch (Exception e) {
				success = apiManager.handleException(e, api);
			}
		}

		if (timeline != null) {
			added = insertPosts(timeline);
			total = timeline.size();
		}
		System.out.println("Inserted " + added + " of " + total + " posts");
		return added;
	}

	/**
	 * Insert the Instagram post and its entities (user, location, hashtag) into the database
	 * 
	 * @param posts
	 * @return The number of inserted records
	 * @throws SQLException
	 */
	public int insertPosts(List<Media> posts) throws SQLException {
		int added = 0;
		for (Media post : posts) {
			added += insertPost(post);
		}
		return added;
	}

	public int insertPost(Media post) throws SQLException {

		String postId = post.getId();
		instpost.setString(1, postId);

		long userId = insertUser(post.getUser());
		instpost.setLong(2, userId);

		Location location = post.getLocation();

		if (location != null) {
			instpost.setFloat(3, location.getLatitude());
			instpost.setFloat(4, location.getLongitude());
			Long locationId = insertLocation(location);

			if (locationId == null) {
				instpost.setNull(8, Types.NULL);
			} else {
				instpost.setLong(8, locationId);
			}
		} else {
			instpost.setNull(3, Types.NULL);
			instpost.setNull(4, Types.NULL);
			instpost.setNull(8, Types.NULL);
		}

		long createdAt = post.getCreated_time() * 1000;
		instpost.setTimestamp(5, new java.sql.Timestamp(createdAt));
		Caption caption = post.getCaption();
		if (caption == null) {
			instpost.setNull(6, Types.NULL);
		} else {
			instpost.setString(6, caption.getText());
		}
		instpost.setString(7, post.getType());

		instpost.setString(9, post.getLink());

		try {
			if (isBatch)
				instpost.addBatch();
			else
				instpost.execute();
			return instpost.getUpdateCount();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return 0;
		}

	}

	public long insertUser(User user) throws SQLException {

		long userId = user.getId();
		instuser.setLong(1, userId);
		instuser.setString(2, user.getUsername());
		instuser.setString(3, user.getFull_name());

		try {
			instuser.execute();
		} catch (SQLException e) {
			if (!e.getMessage().contains("Duplicate"))
				System.out.println(e.getMessage());
		}

		return userId;
	}

	public Long insertLocation(Location location) throws SQLException {

		Long locationId = location.getId();
		if (locationId != null) {
			instloc.setLong(1, locationId);
			instloc.setString(2, location.getName());
			instloc.setFloat(3, location.getLatitude());
			instloc.setFloat(4, location.getLongitude());

			try {
				/*
				 * if (isBatch) instloc.addBatch(); else
				 */instloc.execute();
			} catch (SQLException e) {
				if (!e.getMessage().contains("Duplicate"))
					System.out.println(e.getMessage());
			}
		}

		return locationId;
	}

}
