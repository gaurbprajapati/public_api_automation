package io.rcrm.api.pojo.albatross.notification;

public class Notifications {

	private String continuationToken;
	private int limit;
	private boolean onlyUnreadNotification;
	private boolean isPolling;

	public Notifications() {
	}

	public Notifications(String continuationToken, int limit, boolean onlyUnreadNotification, boolean isPolling) {
		this.continuationToken = continuationToken;
		this.limit = limit;
		this.onlyUnreadNotification = onlyUnreadNotification;
		this.isPolling = isPolling;
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

	public boolean getOnlyUnreadNotification() {
		return onlyUnreadNotification;
	}

	public void setOnlyUnreadNotification(boolean onlyUnreadNotification) {
		this.onlyUnreadNotification = onlyUnreadNotification;
	}

	public boolean getIsPolling() {
		return isPolling;
	}

	public void setIsPolling(boolean isPolling) {
		this.isPolling = isPolling;
	}

}
