package io.rcrm.api.hotlists;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class DeleteHotlistByIDTest extends TestBase{

	public DeleteHotlistByIDTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	String slug = "";
	commanFunction function = new commanFunction();

	JavaFakerHotlist fakerHotlist = new JavaFakerHotlist();
	String hotlistName = fakerHotlist.getHotlistName();
	
	@Owner("Raj Pandey")
	@Test(dataProvider = "getHotlistValidData", groups = "nightly-build")
	public void deleteHotlistByID_HotlistTest(String realtedToType, int statusCode) {
		JsonPath json;
		String hotlistID = "";

		json = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), realtedToType).jsonPath();
		int hotlistID_int = json.get("id");

		hotlistID = String.valueOf(hotlistID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), statusCode);

		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains("Deleted Successfully!"), "Deleted Successfully!");
	}
	
	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "getHotlistInvalidData", groups = "nightly-build")
	public void userShouldNotBeAbleToDeleteHotlistByInvalidID_HotlistTest(String realtedToType, int statusCode) {
		JsonPath json;
		String hotlistID = "";

		json = function.createNewHotlist(baseURL, ThreadManager.getAccountApiKey(), realtedToType).jsonPath();
		int hotlistID_int = json.get("id");

		hotlistID = String.valueOf(hotlistID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", "x001"+hotlistID);
		String basePath = "hotlists/{hotlist}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, false);

		verify422ForHotlistEndpoint(response,statusCode,"Hotlist doesn't exist",true);
		
		
	}
	
	
	
	
	public void verify422ForHotlistEndpoint(Response response, int httpStatus, String errorMessage, boolean isTrue) {
		response.then().body("errorMessage", Matchers.is(errorMessage));
		response.then().body("errorCode", Matchers.is(httpStatus));
		response.then().body("error", Matchers.is(isTrue));
	}
	
	@DataProvider
	public Object[][] getHotlistValidData() {
		Object data[][] = { { "candidate", 200 }, { "contact", 200 }, { "company", 200 }, { "job", 200 } };
		return data;
	}
	
	@DataProvider
	public Object[][] getHotlistInvalidData() {
		Object data[][] = { { "candidate", 404 }, { "contact", 404 }, { "company", 404 }, { "job", 404 } };
		return data;
	}


}
