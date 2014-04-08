package nl.wisdelft.instagram.crawler.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class InstagramConnectionManager extends ConnectionManager {

	private static InstagramConnectionManager connectionManager = null;
	private static String configFile = "instagram.properties";

	public static InstagramConnectionManager getInstance() throws IOException {
		return getInstance(configFile);
	}

	public static InstagramConnectionManager getInstance(String configFile) throws IOException {
		if (connectionManager == null || !configFile.equals(InstagramConnectionManager.configFile)) {
			connectionManager = new InstagramConnectionManager(configFile);
		}
		return connectionManager;
	}

	private InstagramConnectionManager(String configFile) throws IOException {

		loadConfiguration(configFile);
	}

	protected void loadConfiguration(String file) throws IOException {
		// load the configuration from the properties files
		Properties prop = new Properties();
		// resource stream
		InputStream stream = ClassLoader.getSystemResourceAsStream(file);
		if (stream == null) {
			throw new FileNotFoundException("No resource with name '" + file + "' found.");
		}
		prop.load(stream);
		// for each property that starts with "usem" create a new configuration.
		for (String p : prop.stringPropertyNames()) {
			if (p.startsWith("usem")) {
				String val = prop.getProperty(p);
				String[] keys = val.split(",");
				// extra objects for reading clarity and debugging
				// Object containing the authentication info read from the
				// properties
				// file
				InstagramAuthentication auth = new InstagramAuthentication(p, keys[0].trim(), keys[1].trim(), keys[2].trim());

				// Container object for the connection and its name
				InstagramAPI instagramConnection = new InstagramAPI(p, auth);
				// Twitter stream connection, based on the configuration

				instances.add(instagramConnection);

			}
		}
		System.out.println("Loaded " + instances.size() + " instagram authentications.");
	}

	/**
	 * Gets the first twitter instance
	 * 
	 * @return
	 */
	@Override
	public InstagramAPI getAPIInstance() {
		return (InstagramAPI) super.getAPIInstance(null);
	}

	/**
	 * Gets the next twitter instance
	 * 
	 * @param connection
	 *            instance currently held
	 * @return
	 */
	@Override
	public InstagramAPI getAPIInstance(API connection) {
		return (InstagramAPI) super.getAPIInstance(connection);

	}

	/**
	 * Get the instagram api instance and return a copy of the InstagramAPI object
	 * 
	 * @param index
	 * @return
	 */
	protected InstagramAPI getAPI(int index) {
		// check if the instance with this index exists
		if (index >= 0 && index < instances.size()) {
			InstagramAPI api = new InstagramAPI(instances.get(index).name, (InstagramAuthentication) instances.get(index).auth);
			return api;
		} else {
			return null;
		}
	}
}
