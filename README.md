#Instagram Geo-Crawler

An Instagram client that you can use to gather Instagram posts from a specific region.

You can specify multiple points where the crawler will do a radius search of 5km every 10 minutes. 

##Usage

###Instagram Authentication
You can specify your Instagram API keys by creating a `instagram.properties` file in `src/resources/` (or another location that you need to specify in `InstagramConnectionManager.getInstance()`) with content:

`name client_id, client_secret, access_token`

You should generate the access_token yourself, the application doesn't do that.

You can specify multiple API keys, but for this task, one key will suffice.

###Database
Use the `DBManager` to get a SQL connection. The necessary tables are created when you first run the app.

###Example
`Crawler.java` is a usage example where we run the crawler for London