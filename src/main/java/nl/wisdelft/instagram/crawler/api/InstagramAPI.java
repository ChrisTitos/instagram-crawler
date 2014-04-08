package nl.wisdelft.instagram.crawler.api;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import nl.wisdelft.instagram.crawler.entity.Media;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.ClientProperties;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class InstagramAPI extends API {

	private static String MEDIA_ENDPOINT = "https://api.instagram.com/v1/media/%s";
	private static String MEDIA_SEARCH_ENDPOINT = "https://api.instagram.com/v1/media/search/";
	private static String USER_FEED_ENDPOINT = "https://api.instagram.com/v1/users/%s/media/recent/";

	private Gson gson;
	private Client rest;

	public InstagramAPI(String name, InstagramAuthentication auth) {
		super(name, auth);
		gson = new Gson();
		rest = ClientBuilder.newClient();
		// rest.register(new LoggingFilter());
		rest.property(ClientProperties.CONNECT_TIMEOUT, 0);
		rest.property(ClientProperties.READ_TIMEOUT, 0);

	}

	/**
	 * Given the short url of this instagram media, get the corresponding Media object from the Instagram API
	 * 
	 * @param shortUrl
	 *            A valid (short) url of an Instagram post
	 * @return The full instagram Media object
	 * @throws Exception
	 */
	public Media getInstagramPostByURL(String shortUrl) throws Exception {

		String response = IOUtils.toString(new URL("http://api.instagram.com/oembed?url=" + shortUrl));

		Type type = new TypeToken<HashMap<String, Object>>() {
		}.getType();
		HashMap<String, String> json = new HashMap<String, String>();
		json = gson.fromJson(response, type);
		String media_id = json.get("media_id");

		return getInstagramPostById(media_id);
	}

	/**
	 * Get the Media object from the Instagram API by its id
	 * 
	 * @param mediaId
	 *            A valid, existing instagram media id
	 * @return The Media object
	 * @throws Exception
	 */
	public Media getInstagramPostById(String mediaId) throws WebApplicationException {

		Response apiresponse = rest.target(String.format(MEDIA_ENDPOINT, mediaId)).queryParam("access_token", getAuth().access_token).request().get();
		String response = apiresponse.readEntity(String.class);

		InstagramResponse<Media> json = new InstagramResponse<Media>();

		Type type = new TypeToken<InstagramResponse<Media>>() {
		}.getType();
		json = gson.fromJson(response, type);

		return json.getData();
	}

	/**
	 * Get all the instagram posts between two dates in a <b>5 km</b> radius around a point.
	 * 
	 * The difference between the date cannot be more than 7 days. In fact, to make sure you get all the posts, it is
	 * recommended to search in intervals of no more than 15 minutes.
	 * 
	 * @param beginDate
	 *            Timestamp (seconds)
	 * @param endDate
	 *            Timestamp (seconds)
	 * @return A list of found Media objects
	 * @throws Exception
	 */
	public List<Media> getInstagramPosts(long beginDate, long endDate, float latitude, float longitude) throws WebApplicationException {

		Response apiresponse = rest.target(MEDIA_SEARCH_ENDPOINT)
				.queryParam("lat", latitude)
				.queryParam("lng", longitude)
				.queryParam("distance", 5000)
				.queryParam("min_timestamp", beginDate)
				.queryParam("max_timestamp", endDate)
				.queryParam("count", -1)
				.queryParam("access_token", getAuth().access_token)
				.request()
				.get();

		String response = apiresponse.readEntity(String.class);

		InstagramResponse<ArrayList<Media>> json = new InstagramResponse<ArrayList<Media>>();
		Type type = new TypeToken<InstagramResponse<ArrayList<Media>>>() {
		}.getType();

		json = gson.fromJson(response, type);

		return json.getData();
	}

	public List<Media> getUserFeed(long userId) throws WebApplicationException {
		return getUserFeed(userId, -1, -1);
	}

	public List<Media> getUserFeed(long userId, long beginTime, long endTime) throws WebApplicationException {
		WebTarget target = rest.target(String.format(USER_FEED_ENDPOINT, userId))
				.queryParam("count", "-1")
				.queryParam("access_token", getAuth().access_token);
		if (beginTime != -1 && endTime != -1) {
			target = target.queryParam("min_timestamp", beginTime).queryParam("max_timestamp", endTime);
			System.out.println(target.getUri());
		}
		Response apiresponse = target.request().get();

		if (apiresponse.getStatus() != 200) {
			throw new WebApplicationException(apiresponse);
		}

		String response = apiresponse.readEntity(String.class);

		InstagramResponse<ArrayList<Media>> json = new InstagramResponse<ArrayList<Media>>();
		Type type = new TypeToken<InstagramResponse<ArrayList<Media>>>() {
		}.getType();

		json = gson.fromJson(response, type);
		return json.getData();

	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof InstagramAPI))
			return false;
		InstagramAPI o = (InstagramAPI) other;
		if (this.name == null || o.name == null)
			return false;
		return this.name.equals(o.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public InstagramAuthentication getAuth() {
		return (InstagramAuthentication) auth;
	}
}
