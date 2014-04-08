package nl.wisdelft.instagram.crawler.api;


public abstract class API {

	public String name;
	public Authentication auth;

	public API(String name, Authentication auth) {
		this.name = name;
		this.auth = auth;
	}

	public abstract Authentication getAuth();

}
