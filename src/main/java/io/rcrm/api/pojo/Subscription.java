package io.rcrm.api.pojo;

public class Subscription {
	
	private String event;
	private String target_url;
	
	
	public Subscription(String event, String target_url) {
		super();
		this.event = event;
		this.target_url = target_url;
	}
	
	public String getEvent() {
		return this.event;
	}
	
	public String getTarget_url() {
		return this.target_url;
	}
}
