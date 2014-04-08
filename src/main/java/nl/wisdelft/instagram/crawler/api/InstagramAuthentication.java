package nl.wisdelft.instagram.crawler.api;

public class InstagramAuthentication extends Authentication {

	public String access_token;
	public String client_id;
	public String client_secret;

	public InstagramAuthentication(String name, String client_id, String client_secret, String access_token) {
		super(name);
		this.access_token = access_token;
		this.client_id = client_id;
		this.client_secret = client_secret;
	}
}
