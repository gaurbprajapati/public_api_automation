package io.recruitcrm.albatross;

import io.rcrm.api.commanfunctions.commanFunction;
import io.restassured.path.json.JsonPath;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.albatross.GetAssociateCount;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;


@AccountType("CrossAccount")
public class GetAssociateCountTest extends TestBase {

	commanFunction function = new commanFunction();

	private String tokenA;
	private String publicAPIKeyA;
	private String publicAPIKeyB;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		tokenA =  getTokenForAccount("AccountA", "valid");
		publicAPIKeyA = getAccountApiKey("AccountA");
		publicAPIKeyB = getAccountApiKey("AccountB");

	}


	@Owner("Harika")
	@Test(dataProvider = "getValidActivityData", groups = "nightly-build")
	public void getAssociateCountTest(String activityType) {
		int activityId = getActivityId(activityType, publicAPIKeyA);
		GetAssociateCount getAssociateCount = new GetAssociateCount();
		getAssociateCount.setActivity_id(activityId);
		getAssociateCount.setActivity_type(activityType);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "associate-events/associate-count/get", tokenA, null, true, getAssociateCount);

		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}
	
	@Owner("Harika")
	@Test(dataProvider = "getInvalidActivityData", groups = "nightly-build")
	public void getAssociateCountWithInvalidDataTest(int activityId, String activityType) {
		GetAssociateCount getAssociateCount = new GetAssociateCount();
		getAssociateCount.setActivity_id(activityId);
		getAssociateCount.setActivity_type(activityType);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "associate-events/associate-count/get", tokenA, null, true, getAssociateCount);

		response.then().statusCode(200);
		response.then().body("message", Matchers.containsString("Something went wrong"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}

	
	@Owner("Harika")
	@Test(dataProvider = "getEmptyTestData", groups = "nightly-build")
	public void getAssociateCountWithEmptyDataTest(int activityId, String activityType) {
		GetAssociateCount getAssociateCount = new GetAssociateCount();
		getAssociateCount.setActivity_id(activityId);
		getAssociateCount.setActivity_type(activityType);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "associate-events/associate-count/get", tokenA, null, true, getAssociateCount);

		response.then().statusCode(200);
		response.then().body("message", Matchers.containsString("Something went wrong"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getAssociateCountEmptyResponseTest() {

		Response response = RestClient.doPost("JSON", albatrossURL, "associate-events/associate-count/get", tokenA, null, true, null);

		response.then().statusCode(200);
		response.then().body("message", Matchers.containsString("Something went wrong"));
		response.then().body("message_type", Matchers.containsString("is-danger"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void getAssociateCountInvalidTokenTest() {

		GetAssociateCount getAssociateCount = new GetAssociateCount();
		getAssociateCount.setActivity_id(0);
		getAssociateCount.setActivity_type("");

		Response response = RestClient.doPost("JSON", albatrossURL, "associate-events/associate-count/get", tokenA + "inValid", null, true, getAssociateCount);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));

	}

	@Owner("Harika")
	@Test(dataProvider = "getValidActivityData", groups = "nightly-build")
	public void crossAccountGetAssociateCountTest(String activityType) {
		int activityId = getActivityId(activityType, publicAPIKeyB);
		GetAssociateCount getAssociateCount = new GetAssociateCount();
		getAssociateCount.setActivity_id(activityId);
		getAssociateCount.setActivity_type(activityType);

		Response response = RestClient.doPost("JSON", albatrossURL, "associate-events/associate-count/get", tokenA, null, true, getAssociateCount);

		response.then().statusCode(404);
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}

	public int getActivityId(String activityType,String publicAuth){
		JsonPath json;
		int activityId = 0;

		switch (activityType) {
			case "note":
				json = function.createNewNoteAndGetResponse(baseURL, publicAuth, "candidate").jsonPath();
				activityId = json.get("id");
				break;

			case "calllog":
				json = function.createNewCallLog(baseURL, publicAuth, "candidate").jsonPath();
				activityId = json.get("id");
				break;

			case "meeting":
				json = function.createNewMeetings(baseURL, publicAuth, "candidate").jsonPath();
				activityId = json.get("id");
				break;

			case "task":
				json = function.createNewTask(baseURL, publicAuth, "candidate").jsonPath();
				activityId = json.get("id");
				break;

			default:
		}
        return activityId;
    }

	
	@DataProvider(parallel = true)
	public Object[][] getValidActivityData() {
        return new Object[][]{
            { "calllog" },
            { "note" },
            { "task" },
            { "meeting" }
        };
	}

	@DataProvider(parallel = true)
	public Object[][] getEmptyTestData() {
		return new Object[][]{
				{ 0 ,null },
				{ 0 ,"" }
		};
	}
	
	@DataProvider(parallel = true)
	public Object[][] getInvalidActivityData() {
		int callLogId = getActivityId("calllog", publicAPIKeyA);
        return new Object[][]{
            { 99999, "calllog"},
            { callLogId, "invalid_type"},
            { callLogId, "CALLLOG"}
        };
	}

}
