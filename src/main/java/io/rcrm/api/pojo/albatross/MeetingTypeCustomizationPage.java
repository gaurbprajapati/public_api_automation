package io.rcrm.api.pojo.albatross;

import java.util.ArrayList;

public class MeetingTypeCustomizationPage {

	ArrayList<Object> customizedMeetingTypes = new ArrayList<>();

	public MeetingTypeCustomizationPage(ArrayList<Object> customizedMeetingTypes) {
		super();
		this.customizedMeetingTypes = customizedMeetingTypes;
	}

	public MeetingTypeCustomizationPage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ArrayList<Object> getCustomizedMeetingTypes() {
		return customizedMeetingTypes;
	}

	public void setCustomizedMeetingTypes(ArrayList<Object> customizedMeetingTypes) {
		this.customizedMeetingTypes = customizedMeetingTypes;
	}

}
