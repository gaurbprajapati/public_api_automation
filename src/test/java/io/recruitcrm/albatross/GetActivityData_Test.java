package io.recruitcrm.albatross;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.GetActivityData;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetActivityData_Test extends TestBase {

    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	String basePath = "expand-activity/get-activity-data";
	commanFunction function = new commanFunction();
	String accountAPIKey;

	@BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
		accountAPIKey = getAccountApiKey("AccountA");
	}

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getAllActivitiesData", groups = "nightly-build")
    public void getAllActivitiesData_Test(String relatedToSlug, int relatedtotypeid) {
        GetActivityData getActivityData = getActivityDataObject(relatedToSlug, relatedtotypeid, "-1");

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getActivityData);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.filtered_count", Matchers.equalTo("3"));
        response.then().body("data.events.allrecords.size()", Matchers.equalTo(3));
    }

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getCallLogData", groups = "nightly-build")
	public void getCallLogData_Test(String relatedToSlug, int relatedtotypeid) {
		GetActivityData getActivityData = getActivityDataObject(relatedToSlug, relatedtotypeid, "0");

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getActivityData);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.filtered_count", Matchers.equalTo("1"));
        response.then().body("data.events.notes.size()", Matchers.equalTo(1));
	}

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getMeetingsData", groups = "nightly-build")
    public void getMeetingsData_Test(String relatedToSlug, int relatedtotypeid) {
        GetActivityData getActivityData = getActivityDataObject(relatedToSlug, relatedtotypeid, "2");

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getActivityData);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.filtered_count", Matchers.equalTo(1));
        response.then().body("data.events.appointments.size()", Matchers.equalTo(1));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getNotesData", groups = "nightly-build")
    public void getNotesData_Test(String relatedToSlug, int relatedtotypeid) {
        GetActivityData getActivityData = getActivityDataObject(relatedToSlug, relatedtotypeid, "0");

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getActivityData);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.filtered_count", Matchers.equalTo("1"));
        response.then().body("data.events.notes.size()", Matchers.equalTo(1));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getTasksData", groups = "nightly-build")
    public void getTasksData_Test(String relatedToSlug, int relatedtotypeid) {
        GetActivityData getActivityData = getActivityDataObject(relatedToSlug, relatedtotypeid, "1");

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getActivityData);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.filtered_count", Matchers.equalTo(1));
        response.then().body("data.events.tasks.size()", Matchers.equalTo(1));
    }

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getActivityDataWithInvalidToken_Test() {
		GetActivityData getActivityData = new GetActivityData();

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, true, getActivityData);
        response.then().statusCode(401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getActivityData", groups = "nightly-build")
	public void getActivityDataWithCrossAccount_Test(String relatedToSlug) {
        GetActivityData getActivityData =  getActivityDataObject(relatedToSlug, 5, "0");

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknB, null, true, getActivityData);
        response.then().statusCode(422);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("The Activity Related To Slug value is invalid."));
        response.then().body("data", Matchers.empty());
    }

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getActivityData", groups = "nightly-build")
	public void getActivityDataWithAdminToken_Test(String relatedToSlug) {
		String adminToken = getRoleBasedToken("AccountA", "Admin");
        GetActivityData getActivityData = getActivityDataObject(relatedToSlug, 5, "0");

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, adminToken, null, true, getActivityData);
		response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.filtered_count", Matchers.equalTo("1"));
        response.then().body("data.events.notes.size()", Matchers.equalTo(1));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getActivityData", groups = "nightly-build")
	public void getActivityDataWithTeamMemberToken_Test(String relatedToSlug) {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
        GetActivityData getActivityData = getActivityDataObject(relatedToSlug, 5, "0");

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, teamMemberToken, null, true, getActivityData);
		response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.filtered_count", Matchers.equalTo("1"));
        response.then().body("data.events.notes.size()", Matchers.equalTo(1));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getActivityData")
	public void getActivityDataWithRestrictedTeamMemberToken_Test(String relatedToSlug) {
		String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
        GetActivityData getActivityData = getActivityDataObject(relatedToSlug, 5, "0");

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, true, getActivityData);
		response.then().statusCode(422);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("The Activity Related To Slug value is invalid."));
        response.then().body("data", Matchers.empty());
	}

	@DataProvider(parallel = true)
	public Object[][] getCallLogData() {
		Response callLogResponse = function.createNewCallLog(baseURL, accountAPIKey, "candidate");
		JsonPath jsonPath = callLogResponse.jsonPath();
		String candidateSlug = jsonPath.get("related_to");
        callLogResponse = function.createNewCallLog(baseURL, accountAPIKey, "contact");
        jsonPath = callLogResponse.jsonPath();
        String contactSlug = jsonPath.get("related_to");
        callLogResponse = function.createNewCallLog(baseURL, accountAPIKey, "company");
        jsonPath = callLogResponse.jsonPath();
        String companySlug = jsonPath.get("related_to");
        return new Object[][] {  { candidateSlug, 5 }, { contactSlug, 2 }, { companySlug, 3 } };
    }

    @DataProvider(parallel = true)
    public Object[][] getMeetingsData() {
        Response meetingResponse = function.createNewMeetings(baseURL, accountAPIKey, "candidate");
		JsonPath jsonPath = meetingResponse.jsonPath();
		String candidateSlug = jsonPath.get("related_to");
        meetingResponse = function.createNewMeetings(baseURL, accountAPIKey, "contact");
        jsonPath = meetingResponse.jsonPath();
        String contactSlug = jsonPath.get("related_to");
        meetingResponse = function.createNewMeetings(baseURL, accountAPIKey, "company");
        jsonPath = meetingResponse.jsonPath();
        String companySlug = jsonPath.get("related_to");
        meetingResponse = function.createNewMeetings(baseURL, accountAPIKey, "job");
        jsonPath = meetingResponse.jsonPath();
        String jobSlug = jsonPath.get("related_to");
        meetingResponse = function.createNewMeetings(baseURL, accountAPIKey, "deal");
        jsonPath = meetingResponse.jsonPath();
        String dealSlug = jsonPath.get("related_to");
        return new Object[][] {  { candidateSlug, 5 }, { contactSlug, 2 }, { companySlug, 3 }, { jobSlug, 4 }, { dealSlug, 11 } };
    }

    @DataProvider(parallel = true)
    public Object[][] getNotesData() {
        Response noteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "candidate");
		JsonPath jsonPath = noteResponse.jsonPath();
		String candidateSlug = jsonPath.get("related_to");
        noteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "contact");
        jsonPath = noteResponse.jsonPath();
        String contactSlug = jsonPath.get("related_to");
        noteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "company");
        jsonPath = noteResponse.jsonPath();
        String companySlug = jsonPath.get("related_to");
        noteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "job");
        jsonPath = noteResponse.jsonPath();
        String jobSlug = jsonPath.get("related_to");
        noteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "deal");
        jsonPath = noteResponse.jsonPath();
        String dealSlug = jsonPath.get("related_to");
        return new Object[][] { { candidateSlug, 5 }, { contactSlug, 2 }, { companySlug, 3 }, { jobSlug, 4 }, { dealSlug, 11 } };
    }

    @DataProvider(parallel = true)
    public Object[][] getTasksData() {
        Response taskResponse = function.createNewTask(baseURL, accountAPIKey, "candidate");
        JsonPath jsonPath = taskResponse.jsonPath();
        String candidateSlug = jsonPath.get("related_to");
        taskResponse = function.createNewTask(baseURL, accountAPIKey, "contact");
        jsonPath = taskResponse.jsonPath();
        String contactSlug = jsonPath.get("related_to");
        taskResponse = function.createNewTask(baseURL, accountAPIKey, "company");
        jsonPath = taskResponse.jsonPath();
        String companySlug = jsonPath.get("related_to");
        taskResponse = function.createNewTask(baseURL, accountAPIKey, "job");
        jsonPath = taskResponse.jsonPath();
        String jobSlug = jsonPath.get("related_to");
        taskResponse = function.createNewTask(baseURL, accountAPIKey, "deal");
        jsonPath = taskResponse.jsonPath();
        String dealSlug = jsonPath.get("related_to");
        return new Object[][] { { candidateSlug, 5 }, { contactSlug, 2 }, { companySlug, 3 }, { jobSlug, 4 }, { dealSlug, 11 } };
    }

    @DataProvider
    public Object[][] getActivityData() {
        Response noteResponse = function.createNewNoteAndGetResponse(baseURL, accountAPIKey, "candidate");
		JsonPath jsonPath = noteResponse.jsonPath();
		String candidateSlug = jsonPath.get("related_to");
        return new Object[][] { { candidateSlug } };
    }

    @DataProvider(parallel = true)
    public Object[][] getAllActivitiesData() {
        String candidateSlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "candidate");
        String contactSlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "contact");
        String companySlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "company");
        String jobSlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "job");
        String dealSlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "deal");
        return new Object[][] { { candidateSlug, 5 }, { contactSlug, 2 }, { companySlug, 3 }, { jobSlug, 4 }, { dealSlug, 11 } };
    }

    public GetActivityData getActivityDataObject(String relatedToSlug, int relatedtotypeid, String type) {
        GetActivityData getActivityData = new GetActivityData();
        getActivityData.setType(type);
        getActivityData.setPagesize(15);
        getActivityData.setPage("detailspage");
        getActivityData.setRelatedToSlug(relatedToSlug);
        getActivityData.setRelatedtotypeid(relatedtotypeid);
        getActivityData.setRelatedtocompany(null);
        getActivityData.setOffset(0);
        return getActivityData;
    }
}
