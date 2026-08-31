package io.rcrm.api.hotlists;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.pojo.Hotlist;
import io.rcrm.api.pojo.Note;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class AllEndpointsOfHotlistTest extends TestBase {

	public AllEndpointsOfHotlistTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();

	JavaFakerHotlist fakerHotlist = new JavaFakerHotlist();
	String hotlistName = fakerHotlist.getHotlistName();
	


	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getHotlistValidData",priority=0, groups = "nightly-build")
	public void createNewhotlists(String realtedToType, String statusCode) {

		Hotlist hotlist = new Hotlist();
		hotlist.setFirst_name(hotlistName);
		hotlist.setRelated_to_type(realtedToType);
		hotlist.setShared(1);
		
		

		Response response = RestClient.doPost("JSON", baseURL, "hotlists", ThreadManager.getAccountApiKey(), null, true, hotlist);

		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//hotlist//createHotlist.json"));

		response.then().body("id", Matchers.notNullValue());
		response.then().body("name", Matchers.containsString(hotlistName));

	}


	@Owner("Yash Rampal")
	@Test(priority=1, groups = "nightly-build")
	public void showAllHotlistByID() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "4");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "hotlists", ThreadManager.getAccountApiKey(), queryParameters, null, true);


		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//hotlist//getAllHotlists.json"));

		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void searchHotlistByID() {
		JsonPath json;
		String hotlistID = "";

		json = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int hotlistID_int = json.get("id");

		hotlistID = String.valueOf(hotlistID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);


		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("related_to_type", Matchers.is("candidate"));
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void searchHotlistByFields() {
		JsonPath json;
		String hotlistID = "";

		json = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int hotlistID_int = json.get("id");
		String hotlistName = json.get("name");

		hotlistID = String.valueOf(hotlistID_int);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("name", hotlistName);
		queryParameters.put("related_to_type", "candidate");
		queryParameters.put("shared", "1");
		String basePath = "hotlists/search";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, null, true);


		response.then().statusCode(200);
		response.then().body("data.related_to_type[0]", Matchers.is("candidate"));
		response.then().body("data.name[0]", Matchers.is(hotlistName));

	}
	
	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void editHotlistByFields() {
		String generatedString = RandomStringUtils.randomAlphabetic(4);

		JsonPath json;
		String hotlistID = "";

		json = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int hotlistID_int = json.get("id");

		hotlistID = String.valueOf(hotlistID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}";

		Hotlist hotlist = new Hotlist();
		hotlist.setFirst_name(hotlistName + " " + generatedString + " Edited");
		hotlist.setRelated_to_type("candidate");
		hotlist.setShared(1);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null,
				pathParamters, true, hotlist);

		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//hotlist//editHotlist.json"));
		response.then().body("id", Matchers.notNullValue());
		response.then().body("name", Matchers.containsString(hotlistName));
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void deleteHotlistByID() {
		JsonPath json;
		String hotlistID = "";

		json = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int hotlistID_int = json.get("id");

		hotlistID = String.valueOf(hotlistID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);

		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains("Deleted Successfully!"), "Deleted Successfully!");
	}

	@DataProvider
	public Object[][] getHotlistValidData() {
		Object data[][] = { { "candidate", "200" }, { "contact", "200" }, { "company", "200" }, { "job", "200" } };
		return data;
	}

}
