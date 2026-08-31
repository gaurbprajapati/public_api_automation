package io.recruitcrm.albatross.meetingTypeCustomization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetMeetingTypesTest extends TestBase {

	@Owner("Harika")
	@Test(priority = 0, groups = "nightly-build")
	public void getMeetingTypes_Test() {

		String basePath = "meetings/meeting-types";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true);
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));

	}

	@Owner("Harika")
	@Test(priority = 1, groups = "nightly-build")
	public void getMeetingTypesInvalidAuth_Test() {

		String basePath = "meetings/meeting-types";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, null,
				true);
		response.then().statusCode(401);

	}

}
