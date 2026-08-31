package io.rcrm.api.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.albatross.CreateTeam;
import io.rcrm.api.pojo.albatross.Team;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetUsersTest extends TestBase {

	String apiAuthToken;
	String albatrossTkn;
	AllCrudFunctions function = new AllCrudFunctions();

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllUsers_Test() {

		Response response = RestClient.doGet("JSON", baseURL, "users", apiAuthToken, null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("[0].id", Matchers.notNullValue());
		response.then().body("[0].last_name", Matchers.is("1"));
		response.then().body("[0].email", Matchers.is(ThreadManager.getAccount().getOwner().getEmail()));
		response.then().body("[0].role", Matchers.is("Account Owner"));
		response.then().body("[0].two_factor_authentication_enabled", Matchers.is(0));
		response.then().body("[0].status", Matchers.is("Active"));
		response.then().body("[0].email_signature_added", Matchers.is("No"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllUsersWithTeamParemeter_Test() {

		Response userResponse = RestClient.doGet("JSON", baseURL, "users", apiAuthToken, null, null, true);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		int userId = user.get("[0].id");
		List<String> userIds = new ArrayList<>();
		userIds.add(String.valueOf(userId));

		Team team = new Team();
		team.setLabel("Team");
		team.setUserids(userIds);

		CreateTeam createTeam = new CreateTeam();
		createTeam.setTeam(team);

		Response teamResponse = RestClient.doPost("JSON", albatrossURL, "teams", albatrossTkn, null, true, createTeam);
		Assert.assertEquals(teamResponse.getStatusCode(), 200);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("expand", "team");

		Response response = RestClient.doGet("JSON", baseURL, "users", apiAuthToken, queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("[0].id", Matchers.notNullValue());
		response.then().body("[0].last_name", Matchers.is("1"));
		response.then().body("[0].teams[0].team_id", Matchers.notNullValue());
		response.then().body("[0].teams[0].team_name", Matchers.is("Team"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllUsers_Test() {

		Response response = RestClient.doGet("JSON", baseURL, "users", apiAuthToken + "123", null, null, true);

		Assert.assertEquals(response.getStatusCode(), 401);

		response.then().body("error", Matchers.containsString("Unauthorized"));
	}
}