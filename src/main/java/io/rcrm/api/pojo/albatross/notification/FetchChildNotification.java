package io.rcrm.api.pojo.albatross.notification;

public class FetchChildNotification {

	private String continuationToken;
	private int limit;

	public FetchChildNotification() {
	}

	public FetchChildNotification(String continuationToken, int limit) {
		this.continuationToken = continuationToken;
		this.limit = limit;
	}

	public String getContinuationToken() {
		return continuationToken;
	}

	public void setContinuationToken(String continuationToken) {
		this.continuationToken = continuationToken;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}
