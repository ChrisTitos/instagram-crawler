package nl.wisdelft.instagram.crawler;

import java.sql.Connection;
import java.util.ArrayList;

import nl.wisdelft.instagram.crawler.api.InstagramConnectionManager;
import nl.wisdelft.instagram.crawler.db.DBManager;
import nl.wisdelft.instagram.crawler.db.InstagramInserter;
import ch.hsr.geohash.WGS84Point;

import com.google.common.collect.Lists;

public class Crawler {

	static String dbURL = "jdbc:mysql://localhost:3306/amsterdam_tweets";
	static String username = "root";
	static String password = "wachtwoord";
	static String driver = "com.mysql.jdbc.Driver";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		DBManager db = new DBManager(dbURL, username, password, driver);
		Connection con = db.connect();

		InstagramConnectionManager manager = InstagramConnectionManager.getInstance();

		InstagramCrawler inst = new InstagramCrawler(manager, new InstagramInserter(con, manager));

		// coordinates for london
		WGS84Point london1 = new WGS84Point(51.49495785672717, -0.25165557861328125);
		// http://www.freemaptools.com/radius-around-point.htm?clat=51.51205441622754&clng=0.01201629638671875&r=5.000&lc=FFFFFF&lw=1&fc=00FF00
		WGS84Point london2 = new WGS84Point(51.51205441622754, 0.01201629638671875);
		// http://www.freemaptools.com/radius-around-point.htm?clat=51.508515&clng=-0.12548719999995228&r=5.000&lc=FFFFFF&lw=1&fc=00FF00
		WGS84Point london3 = new WGS84Point(51.51205441622754, -0.12548719999995228);
		ArrayList<WGS84Point> londonPoints = Lists.newArrayList(london1, london2, london3);

		inst.setCoordinates(londonPoints);

		(new Thread(inst)).start();

	}
}