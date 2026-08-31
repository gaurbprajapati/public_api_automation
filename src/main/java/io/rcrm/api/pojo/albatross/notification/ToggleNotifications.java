package io.rcrm.api.pojo.albatross.notification;

import java.util.ArrayList;

public class ToggleNotifications {

	private String attribute;
	private ArrayList<String> notificationIds;
	private boolean markAllRead;
	private boolean toggleValue;

	public ToggleNotifications() {
	}

	public ToggleNotifications(String attribute, ArrayList<String> notificationIds, boolean markAllRead,
			boolean toggleValue) {

		this.toggleValue = toggleValue;
		this.markAllRead = markAllRead;
		this.notificationIds = notificationIds;
		this.attribute = attribute;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public ArrayList<String> getNotificationIds() {
		return notificationIds;
	}

	public void setNotificationIds(ArrayList<String> notificationIds) {
		this.notificationIds = notificationIds;
	}

	public boolean isMarkAllRead() {
		return markAllRead;
	}

	public void setMarkAllRead(boolean markAllRead) {
		this.markAllRead = markAllRead;
	}

	public boolean isToggleValue() {
		return toggleValue;
	}

	public void setToggleValue(boolean toggleValue) {
		this.toggleValue = toggleValue;
	}

}
