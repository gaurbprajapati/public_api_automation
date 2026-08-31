package io.recruitcrm.report;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.albatross.report.JavaFakerSavePerferences;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetTeamMode_Test extends TestBase {

	public GetTeamMode_Test() {
		// TODO Auto-generated constructor stub
		super();
	}
	JavaFakerSavePerferences fakerReportData = new JavaFakerSavePerferences();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	commanFunction function = new commanFunction();
	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getTeamMode_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String mode = "job_recruiter";
		queryParameters.put("mode", mode);

		String basePath = "teams";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
		response.then().body("data.records", Matchers.notNullValue());
		Assert.assertEquals(response.getStatusCode(), 200, "Team Mode Called Successfully");
		response.then().body("status", Matchers.containsString("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("teamModeData.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getTeamModeWithInvalidData_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String mode = fakerReportData.getModes();
		queryParameters.put("mode", mode);

		String basePath = "teams";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);

		response.then().body("data.records", Matchers.notNullValue());
		Assert.assertEquals(response.getStatusCode(), 200, "All Team Modes Called Successfully");//Response has Data for All Team Modes for null or invalid value 
		response.then().body("status", Matchers.containsString("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("teamModeData.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getTeamModeWithEmptyData_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("mode", "");

		String basePath = "teams";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);

		response.then().body("data.records", Matchers.notNullValue());
		Assert.assertEquals(response.getStatusCode(), 200, "All Team Modes Called Successfully");//Response has Data for All Team Modes for null or invalid value
		response.then().body("status", Matchers.containsString("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("teamModeData.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetTeamMode_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String mode = fakerReportData.getModes();
		queryParameters.put("mode", mode);

		String basePath = "teams";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"12345", queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 401, "Unauthorized Access Validated Successfully");
		response.then().body("error", Matchers.containsString("Unauthorized"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));

	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getUserValidTestData", groups = "nightly-build")
	public void verifyDeactivatedUserIDsGetTeamMode_Test(ArrayList<String> userId) {
		Response response = RestClient.doGet("JSON", baseURL, "teams", ThreadManager.getAccountApiKey(), null, null,
				true);
		JsonPath user = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);

		// verify that deactivated user is present in team list
		List<Integer> users = user.getList("[0].users");
		for (int i = 0; i < userId.size(); i++) {
			Assert.assertTrue(users.contains(Integer.valueOf(userId.get(i))));
		}

	}

	@DataProvider()
	public Object[][] getUserValidTestData() {
		Response userResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		JsonPath user = userResponse.jsonPath();
		ArrayList<String> userIds = new ArrayList<>();
		userIds.add(String.valueOf(user.getInt("[0].id")));
		userIds.add(String.valueOf(user.getInt("[1].id")));
		userIds.add(String.valueOf(user.getInt("[2].id")));
		userIds.add(String.valueOf(user.getInt("[3].id")));
		String teamMemberDeactivatedID = String.valueOf(userIds.get(userIds.size() - 1));
		Response response1 = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1",
				userIds);
		String teamMemberFirstName = user.getString("[3].first_name");
		String teamMemberLastNameName = user.getString("[3].last_name");
		Response deactivateUser = function.deactivateUser(teamMemberDeactivatedID, teamMemberFirstName,
				teamMemberLastNameName, albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		return new Object[][] { { userIds } };
	}

}
