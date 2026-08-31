package io.rcrm.api.hotlists;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Hotlist;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class CreateNewHotlistTest extends TestBase {

	public CreateNewHotlistTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();

	JavaFakerHotlist fakerHotlist = new JavaFakerHotlist();
	JavaFakerJob jobFaker = new JavaFakerJob();
	String hotlistName = fakerHotlist.getHotlistName();
	String longText = jobFaker.getJobDescriptionText() + jobFaker.getNoteForCandidate();
	
	@BeforeMethod
	public void beforeMethod() {
		waitBetweenTheEveryScript(2000);
	}
	

	@Owner("Raj Pandey")
	@Test(dataProvider = "getHotlistValidData", groups = "nightly-build")
	public void createNewhotlists(String realtedToType, int statusCode) {

		Hotlist hotlist = new Hotlist();
		hotlist.setFirst_name(hotlistName);
		hotlist.setRelated_to_type(realtedToType);
		hotlist.setShared(1);

		Response response = RestClient.doPost("JSON", baseURL, "hotlists", ThreadManager.getAccountApiKey(), null, true, hotlist);

		response.then().statusCode(statusCode);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("name", Matchers.containsString(hotlistName));
		response.then().body("related", Matchers.nullValue());

		response.then().body("created_by", Matchers.notNullValue());
		response.then().body("shared", Matchers.is(1));
	}

	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "getHotlistInvalidData", groups = "nightly-build")
	public void userShouldNotAbleToCreateHotlistWithInvalidSharedTeammmatesData(String realtedToType, int statusCode) {

		Hotlist hotlist = new Hotlist();
		hotlist.setFirst_name(hotlistName);
		hotlist.setRelated_to_type(realtedToType);
		hotlist.setShared(22);

		Response response = RestClient.doPost("JSON", baseURL, "hotlists", ThreadManager.getAccountApiKey(), null, true, hotlist);

		response.then().statusCode(statusCode);
		response.then().body("shared[0]", Matchers.containsString("The selected shared is invalid."));

	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getHotlistInvalidRelatedToData", groups = "nightly-build")
	public void userShouldNotAbleToCreateHotlistWithInvalidRelateToType(String realtedToType, int statusCode) {

		Hotlist hotlist = new Hotlist();
		hotlist.setFirst_name(hotlistName);
		hotlist.setRelated_to_type(realtedToType);
		hotlist.setShared(22);

		Response response = RestClient.doPost("JSON", baseURL, "hotlists", ThreadManager.getAccountApiKey(), null, true, hotlist);

		response.then().statusCode(statusCode);
		response.then().body("shared[0]", Matchers.containsString("The selected shared is invalid."));
		response.then().body("related_to_type[0]", Matchers.containsString("The selected related to type is invalid."));

	}

	@Owner("Yash Rampal")
	@Test(dataProvider = "getHotlistInvalidData", groups = "nightly-build")
	public void userShouldNotAbleToCreateHotlistWithNameLengthMoreThan191Characters(String realtedToType,
			int statusCode) {

		Hotlist hotlist = new Hotlist();
		hotlist.setFirst_name(longText);
		hotlist.setRelated_to_type(realtedToType);
		hotlist.setShared(1);

		Response response = RestClient.doPost("JSON", baseURL, "hotlists", ThreadManager.getAccountApiKey(), null, true, hotlist);

		response.then().statusCode(statusCode);
		response.then().body("name[0]", Matchers.containsString("The name may not be greater than 191 characters."));
	}

	@DataProvider
	public Object[][] getHotlistValidData() {
		Object data[][] = { { "candidate", 200 }, { "contact", 200 }, { "company", 200 }, { "job", 200 } };
		return data;
	}

	@DataProvider
	public Object[][] getHotlistInvalidData() {
		Object data[][] = { { "candidate", 422 }, { "contact", 422 }, { "company", 422 }, { "job", 422 } };
		return data;
	}

	@DataProvider
	public Object[][] getHotlistInvalidRelatedToData() {
		Object data[][] = { { "xcandidate", 422 }, { "xcontact", 422 }, { "xcompany", 422 }, { "xjob", 422 } };
		return data;
	}
}
