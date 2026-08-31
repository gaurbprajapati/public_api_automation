package io.recruitcrm.albatross.meetingTypeCustomization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerNote;
import io.rcrm.api.pojo.albatross.MeetingTypeCustomizationPage;
import io.rcrm.api.pojo.albatross.MeetingTypePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AddMeetingTypeTest extends TestBase {

	String generatedString = RandomStringUtils.randomAlphabetic(4);

	static int id;

	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void createMeetingType_Test() {

		MeetingTypePage meetingTypePage = new MeetingTypePage();
		meetingTypePage.setLabel("Meeting Type " + generatedString);
		meetingTypePage.setDefault(0);
		ArrayList<Object> meetingTypes = new ArrayList<>();
		meetingTypes.add(meetingTypePage);

		MeetingTypeCustomizationPage MeetingTypeCustomizationPage = new MeetingTypeCustomizationPage();
		MeetingTypeCustomizationPage.setCustomizedMeetingTypes(meetingTypes);

		String basePath = "meetings/customize-meeting-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				MeetingTypeCustomizationPage);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Meeting Type Customization"));
		response.then().body("message", Matchers.containsString("Customized Meeting Type Saved Successfully"));
		response.then().body("data.customizeMeetingType[0].id", Matchers.notNullValue());
		response.then().body("data.customizeMeetingType[0].is_custom", Matchers.comparesEqualTo(1));
		JsonPath jp = response.jsonPath();
		id = jp.get("data.customizeMeetingType[0].id");

	}

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void createMeetingTypeWithExistingLabel_Test() {

		MeetingTypePage meetingTypePage = new MeetingTypePage();
		meetingTypePage.setLabel("Meeting Type " + generatedString);
		meetingTypePage.setDefault(0);
		ArrayList<Object> meetingTypes = new ArrayList<>();
		meetingTypes.add(meetingTypePage);

		MeetingTypeCustomizationPage MeetingTypeCustomizationPage = new MeetingTypeCustomizationPage();
		MeetingTypeCustomizationPage.setCustomizedMeetingTypes(meetingTypes);

		String basePath = "meetings/customize-meeting-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				MeetingTypeCustomizationPage);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("action_name", Matchers.containsString("Meeting Type Customization"));
		response.then().body("data.customizeMeetingType[0].error", Matchers.containsString("Title already Exists"));

	}

	@Owner("Harika")
	@Test(priority = 2, groups = "nightly-build")
	public void createMeetingType422_Test() {

		MeetingTypePage meetingTypePage = new MeetingTypePage();
		meetingTypePage.setLabel(" ");
		ArrayList<Object> meetingTypes = new ArrayList<>();
		meetingTypes.add(meetingTypePage);

		MeetingTypeCustomizationPage MeetingTypeCustomizationPage = new MeetingTypeCustomizationPage();
		MeetingTypeCustomizationPage.setCustomizedMeetingTypes(meetingTypes);

		String basePath = "meetings/customize-meeting-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				MeetingTypeCustomizationPage);
		response.then().statusCode(422);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("label field is required"));
	}
	
	@Owner("Harika")
	@Test(priority = 3, groups = "nightly-build")
	public void createMeetingTypeCharLimitExceed_Test() {

		MeetingTypePage meetingTypePage = new MeetingTypePage();
		JavaFakerNote fakerNote = new JavaFakerNote();
		meetingTypePage.setLabel(fakerNote.getNoteDescriptionText());
		meetingTypePage.setDefault(0);
		ArrayList<Object> meetingTypes = new ArrayList<>();
		meetingTypes.add(meetingTypePage);

		MeetingTypeCustomizationPage MeetingTypeCustomizationPage = new MeetingTypeCustomizationPage();
		MeetingTypeCustomizationPage.setCustomizedMeetingTypes(meetingTypes);

		String basePath = "meetings/customize-meeting-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				MeetingTypeCustomizationPage);
		response.then().statusCode(422);
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("label must not be greater than 50 characters"));

	}

	@Owner("Harika")
	@Test(priority = 4, groups = "nightly-build")
	public void createMeetingTypeInvalidAuth_Test() {

		MeetingTypePage meetingTypePage = new MeetingTypePage();
		meetingTypePage.setLabel("Meeting Type " + generatedString);
		meetingTypePage.setDefault(0);
		ArrayList<Object> meetingTypes = new ArrayList<>();
		meetingTypes.add(meetingTypePage);

		MeetingTypeCustomizationPage MeetingTypeCustomizationPage = new MeetingTypeCustomizationPage();
		MeetingTypeCustomizationPage.setCustomizedMeetingTypes(meetingTypes);

		String basePath = "meetings/customize-meeting-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				MeetingTypeCustomizationPage);
		response.then().statusCode(401);

	}

	

}
