package io.rcrm.api.hotlists;

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
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class unAuthorizedCannotDoActionsOnHotlistendpoints_hotlistTest extends TestBase {

	public unAuthorizedCannotDoActionsOnHotlistendpoints_hotlistTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();

	JavaFakerHotlist fakerHotlist = new JavaFakerHotlist();
	String hotlistName = fakerHotlist.getHotlistName();

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void unathorizedUserCannotCreateNewhotlists_HotlistTest(String realtedToType, int statusCode,
			Object authTokenMapValid) {

		Hotlist hotlist = new Hotlist();
		hotlist.setFirst_name(hotlistName);
		hotlist.setRelated_to_type(realtedToType);
		hotlist.setShared(1);

		Response response = RestClient.doPost("JSON", baseURL, "hotlists", ThreadManager.getAccountApiKey()+"x001", null, true, hotlist);

		response.then().statusCode(statusCode);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Yash Rampal")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void unathorizedUserCannotGetAllHotlistByID_HotlistTest(String realtedToType, int statusCode,
			Object authTokenMapValid) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "10");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "hotlists", ThreadManager.getAccountApiKey()+"x001", queryParameters, null, true);


		response.then().statusCode(statusCode);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void unathorizedUserCannotSearchHotlistByID_HotlistTest(String realtedToType, int statusCode,
			Object authTokenMapValid) {
		JsonPath json;
		String hotlistID = "";

		json = function.createNewHotlist(baseURL, authTokenMapValid, realtedToType).jsonPath();
		int hotlistID_int = json.get("id");

		hotlistID = String.valueOf(hotlistID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"x001", null, pathParamters, true);


		response.then().statusCode(statusCode);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void unathorizedUserCannotSearchHotlistByFields_hotlistTest(String realtedToType, int statusCode,
			Object authTokenMapValid) {
		JsonPath json;
		String hotlistID = "";

		json = function.createNewHotlist(baseURL, authTokenMapValid, "candidate").jsonPath();
		int hotlistID_int = json.get("id");
		String hotlistName = json.get("name");

		hotlistID = String.valueOf(hotlistID_int);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("name", hotlistName);
		queryParameters.put("related_to_type", "candidate");
		queryParameters.put("shared", "1");
		String basePath = "hotlists/search";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"x001", queryParameters, null, true);


		response.then().statusCode(statusCode);
		response.then().body("error", Matchers.containsString("Unauthorized"));

	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void unathorizedUserCannotEditHotlistByFields_HotlistTest(String realtedToType, int statusCode,
			Object authTokenMapValid) {
		String generatedString = RandomStringUtils.randomAlphabetic(4);

		JsonPath json;
		String hotlistID = "";

		json = function.createNewHotlist(baseURL, authTokenMapValid, "candidate").jsonPath();
		int hotlistID_int = json.get("id");

		hotlistID = String.valueOf(hotlistID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}";

		Hotlist hotlist = new Hotlist();
		hotlist.setFirst_name(hotlistName + " " + generatedString + " Edited");
		hotlist.setRelated_to_type("candidate");
		hotlist.setShared(1);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"x001", null, pathParamters, true,
				hotlist);

		response.then().statusCode(statusCode);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Yash Rampal")
	@Test(dataProvider = "getMeetingValidData", groups = "nightly-build")
	public void unathorizedUserCannotDeleteHotlistByID_HotlistTest(String realtedToType, int statusCode,
			Object authTokenMapValid) {
		JsonPath json;
		String hotlistID = "";

		json = function.createNewHotlist(baseURL, authTokenMapValid, "candidate").jsonPath();
		int hotlistID_int = json.get("id");

		hotlistID = String.valueOf(hotlistID_int);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("hotlist", hotlistID);
		String basePath = "hotlists/{hotlist}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"x001", null, pathParamters, false);

		response.then().statusCode(statusCode);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] getMeetingValidData() {

		Object data[][] = { { "candidate", 401, ThreadManager.getAccountApiKey() }, { "contact", 401, ThreadManager.getAccountApiKey() },
				{ "company", 401, ThreadManager.getAccountApiKey() }, { "job", 401, ThreadManager.getAccountApiKey() } };
		return data;
	}

}
