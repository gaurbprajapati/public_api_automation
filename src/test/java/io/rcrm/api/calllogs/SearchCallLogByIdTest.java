package io.rcrm.api.calllogs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;

import static org.hamcrest.Matchers.equalTo;
import com.qa.api.util.Owner;

@AccountType("Business")
public class SearchCallLogByIdTest extends TestBase {

	public SearchCallLogByIdTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String callLog_Id = "";
	commanFunction function = new commanFunction();
	String apiAuthTkn;

	@BeforeClass(alwaysRun = true)	public void setup() {
		apiAuthTkn = ThreadManager.getAccountApiKey();
	}

	@Owner("Harika")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void searchCallLogById_GET(String realtedToType, int statusCode) {
		JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), realtedToType).jsonPath();
		int callLogID = json.get("id");
		String entitySlug = json.get("related_to");
		String callNote = json.get("call_notes");
		callLog_Id = String.valueOf(callLogID);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLog_Id);

		String basePath = "call-logs/{callLog}";
		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);


		String responseBody = response.getBody().asString();

		response.then().statusCode(statusCode);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("call_notes", Matchers.is(callNote));
		response.then().body("call_type", Matchers.is("CALL_INCOMING"));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		response.then().body("duration", Matchers.comparesEqualTo(3600));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_users"));
		MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("collaborator_teams"));
	}

	@Owner("Harika")
	@Test(dataProvider = "getRecordingParameter", groups = "nightly-build")
	public void searchCallLogWithRecordingById_GET(String recording,int statusCode) {
		JsonPath json = function.createNewCallLog(baseURL, apiAuthTkn, "candidate").jsonPath();
		int callLogID = json.get("id");
		String entitySlug = json.get("related_to");
		String callNote = json.get("call_notes");
		callLog_Id = String.valueOf(callLogID);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLog_Id);

		Map<String, String> queryParamters = new HashMap<String, String>();
		queryParamters.put("recording_required", recording);

		String basePath = "call-logs/{callLog}";
		Response response = RestClient.doGet("JSON", baseURL, basePath, apiAuthTkn, queryParamters, pathParamters, true);

		String responseBody = response.getBody().asString();

		if(statusCode==200){
			if(recording.equals("true")){
				MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("recording"));
			}else{
				MatcherAssert.assertThat(responseBody, CoreMatchers.not(CoreMatchers.containsString("recording")));
			}
			response.then().body("id", Matchers.notNullValue());
			response.then().body("call_notes", Matchers.is(callNote));
			response.then().body("related_to", Matchers.is(entitySlug));
		}else{
			MatcherAssert.assertThat(responseBody, CoreMatchers.containsString("The recording_required field must be one of the following values: true, false, 1, 0."));
		}
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void searchCallLogByInvalidId_GET() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("callLog", callLog_Id + "12345");

		String basePath = "call-logs/{callLog}";

		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true);


		// Verify Response Code and body
		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Call Log doesn't exist"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSearchCallLogById() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("CallLog", callLog_Id);

		String basePath = "call-logs/{CallLog}";
		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters, true);


		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] getEntityValidData() {

		Object data[][] = { { "candidate", 200 }, { "contact", 200 }, { "company", 200 } };
		return data;
	}


	@DataProvider(parallel = true)
	public Object[][] getRecordingParameter() {
		Object data[][] = { { "true", 200 }, { "false", 200 } ,{"abc",422}};
		return data;
	}

}
