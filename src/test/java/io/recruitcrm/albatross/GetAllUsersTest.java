package io.recruitcrm.albatross;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.commanfunctions.commanFunction;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.hamcrest.Matchers;

import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;


@AccountType("CrossAccount")
public class GetAllUsersTest extends TestBase {

	private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	private String accountA_APIKey;
	String basePath = "users/all";
	commanFunction function = new commanFunction();

	@BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
		accountA_APIKey = getAccountApiKey("AccountA");
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getAllUsers_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, null, true);
		validateSuccessResponse(response);
		assertThat("Data list size should be 5", response.jsonPath().getList("data").size(), is(5));	
		assertThat("Users list should not be empty", response.jsonPath().getList("data").isEmpty(), is(false));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getAllUsersUnauthorized_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, null, true);
		validateUnauthorizedResponse(response);
	}

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
	public void getAllUsersEmptyToken_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, "", null, null, true);
		validateUnauthorizedResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getAllUsersForCrossAccount_Test() {
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, null, true);
		JsonPath user = function.getUsers(baseURL, accountA_APIKey).jsonPath();
		assertThat(response.statusCode(), is(200));
		assertThat("Response message_type should be is-success", response.jsonPath().getString("message_type"), is("is-success"));
		assertThat("User ID should not match with Account A", response.jsonPath().getInt("data[1].id"), is(not(user.getInt("[0].id"))));
		assertThat("User name should not match with Account A", response.jsonPath().getString("data[1].name"), is(not(user.getString("[0].first_name") + " " + user.getString("[0].last_name"))));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getAllUsersWithAdminToken_Test() {
        String adminToken = getRoleBasedToken("AccountA", "Admin");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, null, true);
		validateSuccessResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getAllUsersWithTeamMemberToken_Test() {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, null, true);
		validateSuccessResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getAllUsersWithRestrictedTeamMemberToken_Test() {
		String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, null, true);
		validateSuccessResponse(response);
	}

	private void validateUnauthorizedResponse(Response response) {
		assertThat(response.statusCode(), is(401));
		assertThat("Response error should be Unauthorized", response.jsonPath().getString("error"), is("Unauthorized"));
	}

	private void validateSuccessResponse(Response response) {
		assertThat(response.statusCode(), is(200));
		JsonPath user = function.getUsers(baseURL, accountA_APIKey).jsonPath();
		for (int i = 0; i < 3; i++) {
			assertThat("User email should match at index " + i, response.jsonPath().getString("data[" + (i+1) + "].email"), is(user.getString("[" + i + "].email")));
			assertThat("User ID should match at index " + i, response.jsonPath().getInt("data[" + (i+1) + "].id"), is(user.getInt("[" + i + "].id")));
			assertThat("User name should match at index " + i, response.jsonPath().getString("data[" + (i+1) + "].name"), is(user.getString("[" + i + "].first_name") + " " + user.getString("[" + i + "].last_name")));
		}
		assertThat("Response message_type should be is-success", response.jsonPath().getString("message_type"), is("is-success"));
		assertThat("Response status should be success", response.jsonPath().getString("status"), is("success"));
	}
}
