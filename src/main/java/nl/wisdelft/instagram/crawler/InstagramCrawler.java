package nl.wisdelft.instagram.crawler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nl.wisdelft.instagram.crawler.api.InstagramAPI;
import nl.wisdelft.instagram.crawler.api.InstagramConnectionManager;
import nl.wisdelft.instagram.crawler.db.InstagramInserter;
import nl.wisdelft.instagram.crawler.entity.Media;
import ch.hsr.geohash.WGS84Point;

public class InstagramCrawler implements Runnable {

	private ArrayList<WGS84Point> coords;
	private InstagramConnectionManager manager;
	private InstagramAPI api;
	private InstagramInserter inserter;

	/**
	 * Initialize the crawler
	 * 
	 * @param manager
	 * @param inserter
	 */
	public InstagramCrawler(InstagramConnectionManager manager, InstagramInserter inserter) {
		coords = new ArrayList<WGS84Point>();
		this.inserter = inserter;
		this.manager = manager;
		api = this.manager.getAPIInstance();
	}

	/**
	 * Set points that will be crawled.
	 * 
	 * The crawling area will be 5 km around each point, so make sure that there is as little overlap as possible
	 * 
	 * @param coordinates
	 * @return The InstagramCrawler instance (fluent interface)
	 */
	public InstagramCrawler setCoordinates(ArrayList<WGS84Point> coordinates) {
		this.coords = coordinates;

		return this;
	}

	/**
	 * Add points to be crawled.
	 * 
	 * The crawling area will be 5 km around each point, so make sure that there is as little overlap as possible
	 * 
	 * @param coordinates
	 * @return The InstagramCrawler instance (fluent interface)
	 */
	public InstagramCrawler addCoordinates(ArrayList<WGS84Point> coordinates) {
		for (WGS84Point coord : coordinates) {
			addCoordinate(coord);
		}
		return this;
	}

	/**
	 * Add a single point to be crawled to the set of points.
	 * 
	 * @param coordinate
	 * @return The InstagramCrawler instance (fluent interface)
	 */
	public InstagramCrawler addCoordinate(WGS84Point coordinate) {
		coords.add(coordinate);

		return this;
	}

	/**
	 * Start the crawling. Every ten minutes we do a search query to the Instagram API, and subsequently save them in
	 * the database
	 */
	public void run() {

		System.out.println("Starting crawling");
		Calendar start = Calendar.getInstance();

		while (true) {
			try {
				Thread.sleep(1000 * 60 * 2);

				Calendar end = Calendar.getInstance();

				crawlInstagram(start.getTimeInMillis() / 1000, end.getTimeInMillis() / 1000);

				start = end;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Call the Instagram API to search the provided areas, within a timespan
	 * 
	 * @param beginTime
	 * @param endTime
	 */
	private void crawlInstagram(long beginTime, long endTime) {

		for (WGS84Point coord : coords) {
			boolean success = false;
			List<Media> media = null;
			while (!success) {
				try {
					media = api.getInstagramPosts(beginTime, endTime, (float) coord.getLatitude(), (float) coord.getLongitude());

					success = true;
				} catch (Exception e) {
					success = manager.handleException(e, api);
				}
			}
			// System.out.println(media);

			if (media != null) {
				try {
					int added = inserter.insertPosts(media);
					System.out.println("Inserted " + added + " posts around " + coord);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
