package io.recruitcrm.albatross;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;
import io.rcrm.api.commanfunctions.commanFunction;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetTeams_Test extends TestBase {

	private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	String basePath = "teams";
	commanFunction function = new commanFunction();
	String accountAPIKey;

	@BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
		accountAPIKey = getAccountApiKey("AccountA");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getTeams_Test(List<String> teamInfo) {
		String[] modes = {"dashboardcandidates", "dashboardcompanies", "dashboardjobs", "dashboard-tasks"};
		for (String mode : modes) {
			Map<String, String> queryParams = new HashMap<>();
			queryParams.put("mode", mode);
			Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, queryParams, null, true);
			validateSuccessResponse(response, teamInfo);
			assertThat("Data field should not be null", response.jsonPath().get("data"), is(notNullValue()));
		}
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getTeamsUnauthorized_Test(List<String> teamInfo) {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, null, true);
		validateUnauthorizedResponse(response);
	}

    @Owner("Smit Patel")
    @Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getTeamsEmptyToken_Test(List<String> teamInfo) {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, "", null, null, true);
		validateUnauthorizedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getTeamsForCrossAccount_Test(List<String> teamInfo) {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, null, true);
		assertThat(response.statusCode(), is(200));
		assertThat("Response message_type should be is-success", response.jsonPath().getString("message_type"), is("is-success"));
		assertThat("Team Name should not match", response.jsonPath().getString("data.records[0].name"), is(not(teamInfo.get(0))));
		assertThat("Team ID should not match", response.jsonPath().get("data.records[0].teamid"), is(not(teamInfo.get(1))));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getTeamsWithAdminToken_Test(List<String> teamInfo) {
        String adminToken = getRoleBasedToken("AccountA", "Admin");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, null, true);
		validateSuccessResponse(response, teamInfo);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getTeamsWithTeamMemberToken_Test(List<String> teamInfo) {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, null, true);
		validateSuccessResponse(response, teamInfo);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getTeamsWithRestrictedTeamMemberToken_Test(List<String> teamInfo) {
		String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, null, true);
		validateSuccessResponse(response, teamInfo);
	}

	private void validateUnauthorizedResponse(Response response) {
		assertThat(response.statusCode(), is(401));
		assertThat("Response error should be Unauthorized", response.jsonPath().getString("error"), is("Unauthorized"));
	}

	private void validateSuccessResponse(Response response, List<String> teamInfo) {
		assertThat(response.statusCode(), is(200));
		assertThat("Response message_type should be is-success", response.jsonPath().getString("message_type"), is("is-success"));
		assertThat("Response status should be success", response.jsonPath().getString("status"), is("success"));
		assertThat("Team Name should match", response.jsonPath().getString("data.records[0].name"), is(teamInfo.get(0)));
		assertThat("Team ID should match", response.jsonPath().getInt("data.records[0].teamid"), is(Integer.parseInt(teamInfo.get(1))));
	}

	@DataProvider
	public Object[][] createTeam() {
		List<String> userRoles = Arrays.asList("accountOwner", "teamMember");
		List<String> teamInfo = function.createTeams(userRoles, albatrossURL, albatrossTknA, baseURL, accountAPIKey);
		return new Object[][] { { teamInfo } };
	}
}
