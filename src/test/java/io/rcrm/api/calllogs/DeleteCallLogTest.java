package io.rcrm.api.calllogs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class DeleteCallLogTest extends TestBase {

	public DeleteCallLogTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String callLogId = "";
	commanFunction function = new commanFunction();


	@Owner("Harika")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void deleteCallLogById_GET(String relatedToType, int statusCode) {
		JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), relatedToType).jsonPath();
		int callLogID = json.get("id");
		callLogId = String.valueOf(callLogID);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLogId);

		String basePath = "call-logs/{callLog}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		// Verify Response Code and body
		response.then().statusCode(statusCode);
		response.then().body(Matchers.is("\"Deleted Successfully!\""));

	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void deleteCallLogByInvalidID_GET() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLogId + "123");

		String basePath = "call-logs/{callLog}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		// Verify Response Code and body
		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Call Log doesn't exist"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotDeleteCallLog() {
		JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int callLogId = json.get("id");
		String callLogID = String.valueOf(callLogId);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLogID);
		String basePath = "call-logs/{callLog}";
		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey() +"12345", null, pathParamters,
				true);
		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createAndGetCallLogId", groups = "nightly-build")
	public void userCannotDeleteAutomatedCallLog_PublicAPI_Test(int callLogId) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLogId", String.valueOf(callLogId));

		String basePath = "call-logs/{callLogId}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		response.then().statusCode(400);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(400));
		response.then().body("errorMessage", Matchers.is("Deleting an automated call log is not allowed"));
	}

	@DataProvider
	public Object[][] getEntityValidData() {
		Object data[][] = { { "candidate", 200 }, { "contact", 200 }, { "company", 200 } };
		return data;
	}

	@DataProvider
	public Object[][] createAndGetCallLogId() {
		JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int newCallLogId = json.get("id");

		ReaperIntegration.updateAutomatedCallLog(ThreadManager.getAccount().getAccountId());

		return new Object[][]{
			{newCallLogId}
		};
	}
}