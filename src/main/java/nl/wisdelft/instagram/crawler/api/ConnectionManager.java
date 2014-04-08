package nl.wisdelft.instagram.crawler.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Class that deals with handling of connections to API's (Instagram or Foursquare)
 * 
 * @see <a
 *      href="https://github.com/WISDelft/WUDE/tree/master/COMMIT%20-%20WUDE/twitter.gatherer/src/main/java/nl/wisdelft/twitter/gatherer">Twitter-Gatherer</a>
 */
public abstract class ConnectionManager {

	protected List<API> instances = new ArrayList<API>();

	protected abstract void loadConfiguration(String file) throws IOException;

	/**
	 * Get the api instance and return a copy of the API object
	 * 
	 * @param index
	 * @return
	 */
	protected abstract API getAPI(int index);

	/**
	 * Gets the first api instance
	 * 
	 * @return
	 */
	public API getAPIInstance() {
		return getAPIInstance(null);
	}

	/**
	 * Gets the next api instance
	 * 
	 * @param connection
	 *            instance currently held
	 * @return
	 */
	public API getAPIInstance(API connection) {
		if (connection == null) {
			return getAPI(0);
		} else {
			// find the given twitter instance
			int curId = -1;
			for (int i = 0; i < instances.size(); i++) {
				if (instances.get(i).equals(connection)) {
					curId = i;
					break;
				}
			}
			// if it is not the last one
			if (curId + 1 < instances.size()) {
				return getAPI(curId + 1);
			} else {
				return getAPI(0);
			}
		}

	}

	/**
	 * Determines whether a exception can be seen a success and sleeps the thread or changes connection when necessary
	 * 
	 * @param ex
	 *            The exception
	 * @param connection
	 *            The current API in use.
	 * @return whether the exception can be seen as a success
	 */
	public synchronized boolean handleException(Exception ex, API connection) {
		if (ex instanceof WebApplicationException) {
			Response response = ((WebApplicationException) ex).getResponse();

			int remaining = response.getHeaderString("X-Ratelimit-Remaining") == null ? 100
					: Integer.valueOf(response.getHeaderString("X-Ratelimit-Remaining"));
			// handle rate limit exceptions
			if (response.getStatus() == 400) { // instagram
				Type type = new TypeToken<HashMap<String, Object>>() {
				}.getType();
				HashMap<String, Object> json = new HashMap<String, Object>();
				json = (new Gson()).fromJson(response.readEntity(String.class), type);
				System.out.println(json);

				if (json.containsKey("meta")) {
					Map<String, String> meta = (Map<String, String>) json.get("meta");
					if (meta.containsKey("code")) {
						if (String.valueOf(meta.get("code")).equals("420"))
							remaining = 0;
					}
				} else if (json.containsKey("code")) {
					System.out.println(json.get("code"));
					if ((Double) json.get("code") == 420)
						remaining = 0;
				}
			}
			if (remaining == 0 || response.getStatus() == 403) {

				System.out.println("Rate limit on " + connection.name);
				try {
					// else change connection and wait
					API newConnection = getAPIInstance(connection);
					connection.auth = newConnection.auth;
					connection.name = newConnection.name;
					// wait 10 seconds to not spam the connection when all
					// connections
					// are rate limitted.
					System.out.println(" new connection: " + connection.name);
					Thread.sleep(1000 * 10);

				} catch (InterruptedException ex2) {
					ex2.printStackTrace();
				}
				// request failed
				return false;
			}
			// the user does not exist or has a private profile
			else if (response.getStatus() == 400 || response.getStatus() == 504) {
				// the best we can get for this request --> request success
				return true;
			}
			// twitter servers under heavy load or network error or twitter
			// internal
			// server error
			else if (response.getStatus() == 503 || response.getStatus() == 500) {
				// request failed, but wait a bit before retrying
				try {
					Thread.sleep(1000 * 5);
				} catch (InterruptedException e) {
					Type type = new TypeToken<HashMap<String, Object>>() {
					}.getType();
					HashMap<String, String> json = new HashMap<String, String>();
					json = (new Gson()).fromJson(response.readEntity(String.class), type);
					System.err.println(json);
					e.printStackTrace();
				}
				return false;
			} else {
				// System.err.println(response.readEntity(String.class));
				ex.printStackTrace();
				return false;
			}

		}
		// unknown error
		ex.printStackTrace();
		return false;

	}

}
