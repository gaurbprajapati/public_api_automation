package io.recruitcrm.albatross;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import io.rcrm.api.commanfunctions.commanFunction;

import java.util.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetUsersWithTeamsTest extends TestBase {

    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	commanFunction function = new commanFunction();
	String accountAPIKey;;
    String basePath = "users/users-with-teams";

	@BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
		accountAPIKey = getAccountApiKey("AccountA");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getUsersWithTeams_Test(List<String> teamInfo) {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, null, true);
		validateSuccessResponse(response, teamInfo);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getUsersWithTeamsUnauthorized_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, null, true);
		validateUnauthorizedResponse(response);
	}

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
	public void getUsersWithTeamsEmptyToken_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, "", null, null, true);
		validateUnauthorizedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getUsersWithTeamsForCrossAccount_Test(List<String> teamInfo) {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, null, true);
		assertThat(response.statusCode(), is(200));
		assertThat("Response message_type should be is-success", response.jsonPath().getString("message_type"), is("is-success"));
		assertThat("Team Name should not match", response.jsonPath().getString("data.teams[0].name"), is(not(teamInfo.get(0))));
		assertThat("Team ID should not match", response.jsonPath().get("data.teams[0].id"), is(not(teamInfo.get(1))));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getUsersWithTeamsWithAdminToken_Test(List<String> teamInfo) {
        String adminToken = getRoleBasedToken("AccountA", "Admin");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, null, true);
		validateSuccessResponse(response, teamInfo);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getUsersWithTeamsWithTeamMemberToken_Test(List<String> teamInfo) {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, null, true);
		validateSuccessResponse(response, teamInfo);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createTeam", groups = "nightly-build")
	public void getUsersWithTeamsWithRestrictedTeamMemberToken_Test(List<String> teamInfo) {
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
		assertThat("Team Name should match", response.jsonPath().getString("data.teams[0].name"), is(teamInfo.get(0)));
		assertThat("Team ID should match", response.jsonPath().getInt("data.teams[0].id"), is(Integer.parseInt(teamInfo.get(1))));
		assertThat("Team userids should contain 2 users", response.jsonPath().getString("data.teams[0].userids").split(",").length, is(2));
	}

	@DataProvider
	public Object[][] createTeam() {
		List<String> userRoles = Arrays.asList("accountOwner", "teamMember");
		List<String> teamInfo = function.createTeams(userRoles, albatrossURL, albatrossTknA, baseURL, accountAPIKey);
		return new Object[][] { { teamInfo } };
	}
}
