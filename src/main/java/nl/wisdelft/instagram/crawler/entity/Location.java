package nl.wisdelft.instagram.crawler.entity;

public class Location {
	
	private Long id;
	private String name;
	private float latitude;
	private float longitude;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	@Override
	public String toString() {
		return "Location [id=" + id + ", name=" + name + ", latitude="
				+ latitude + ", longitude=" + longitude + "]";
	}
}
