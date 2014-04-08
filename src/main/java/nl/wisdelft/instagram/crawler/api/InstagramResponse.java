package nl.wisdelft.instagram.crawler.api;

import java.util.Map;

public class InstagramResponse<T> {
	
	private Map<String, Object> meta;
	private T data;
	private Map<String, Object> pagination;
	public Map<String, Object> getMeta() {
		return meta;
	}
	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	public Map<String, Object> getPagination() {
		return pagination;
	}
	public void setPagination(Map<String, Object> pagination) {
		this.pagination = pagination;
	}
}
