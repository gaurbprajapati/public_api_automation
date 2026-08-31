package io.recruitcrm.albatross;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetActivityCount_Test extends TestBase {

    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	String basePath = "expand-activity/get-activity-count";
	commanFunction function = new commanFunction();
	String accountAPIKey;

	@BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
		accountAPIKey = getAccountApiKey("AccountA");
	}

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getActivityCountData", groups = "nightly-build")
    public void getActivityCount_Test(String relatedToSlug, int relatedtotypeid) {
        JSONObject activityCount = getActivityCountObject(relatedToSlug, relatedtotypeid);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, activityCount);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("data.totalNotesCount", Matchers.equalTo("1"));
		response.then().body("data.totalAppointmentsCount", Matchers.equalTo(1));
		response.then().body("data.totalTasksCount", Matchers.equalTo(1));
    }

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getActivityCountWithInvalidToken_Test() {
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, true, null);
        response.then().statusCode(401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getActivityCountData")
	public void getActivityCountWithCrossAccount_Test(String relatedToSlug, int relatedtotypeid) {
        JSONObject activityCount = getActivityCountObject(relatedToSlug, 5);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknB, null, true, activityCount);
        response.then().statusCode(422);
    }

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getActivityCountDataForCandidate", groups = "nightly-build")
	public void getActivityCountWithAdminToken_Test(String relatedToSlug, int relatedtotypeid) {
		String adminToken = getRoleBasedToken("AccountA", "Admin");
        JSONObject activityCount = getActivityCountObject(relatedToSlug, 5);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, adminToken, null, true, activityCount);
		response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("data.totalNotesCount", Matchers.equalTo("1"));
		response.then().body("data.totalAppointmentsCount", Matchers.equalTo(1));
		response.then().body("data.totalTasksCount", Matchers.equalTo(1));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getActivityCountDataForCandidate", groups = "nightly-build")
	public void getActivityCountWithTeamMemberToken_Test(String relatedToSlug, int relatedtotypeid) {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
        JSONObject activityCount = getActivityCountObject(relatedToSlug, 5);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, teamMemberToken, null, true, activityCount);
		response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("data.totalNotesCount", Matchers.equalTo("1"));
		response.then().body("data.totalAppointmentsCount", Matchers.equalTo(1));
		response.then().body("data.totalTasksCount", Matchers.equalTo(1));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getActivityCountDataForCandidate")
	public void getActivityCountWithRestrictedTeamMemberToken_Test(String relatedToSlug, int relatedtotypeid) {
		String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
        JSONObject activityCount = getActivityCountObject(relatedToSlug, relatedtotypeid);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, true, activityCount);
		response.then().statusCode(422);
	}

	@DataProvider(parallel = true)
    public Object[][] getActivityCountData() {
        String candidateSlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "candidate");
        String contactSlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "contact");
        String companySlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "company");
        String jobSlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "job");
        String dealSlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "deal");
        return new Object[][] { { candidateSlug, 5 }, { contactSlug, 2 }, { companySlug, 3 }, { jobSlug, 4 }, { dealSlug, 11 } };
    }

	@DataProvider(parallel = true)
    public Object[][] getActivityCountDataForCandidate() {
        String candidateSlug = function.createActivityDataAndGetEntitySlug(baseURL, accountAPIKey, "candidate");
        return new Object[][] { { candidateSlug, 5 }};
    }

    public JSONObject getActivityCountObject(String relatedToSlug, int relatedtotypeid) {
        JSONObject activityCount = new JSONObject();
        activityCount.put("relatedToSlug", relatedToSlug);
        activityCount.put("relatedtotypeid", relatedtotypeid);
        activityCount.put("page", "detailspage");
        activityCount.put("skipCountForType", -1);
        return activityCount;
    }
}
