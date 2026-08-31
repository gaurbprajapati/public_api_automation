package io.recruitcrm.albatross.userManagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.albatross.GetTeam;
import io.rcrm.api.pojo.albatross.GetTeam.TeamFilters;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetTeamsTest extends TestBase {

	JavaFakerCompany faker = new JavaFakerCompany();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	commanFunction function = new commanFunction();
	int ownerIdInt, adminIdInt, resTeamMemberIdInt, teamMemberIdInt;
	String ownerId, adminId, resTeamMemberId, teamMemberId;
	String team1 = faker.getCity() + " Team " + faker.getRandomId();
	String team2 = faker.getCity() + " Team " + faker.getRandomId();

	@BeforeClass(alwaysRun = true)	public void setUp() throws IOException {

		Response resp = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		Assert.assertEquals(resp.getStatusCode(), 200);
		JsonPath user = resp.jsonPath();
		ownerIdInt = user.get("[0].id");
		adminIdInt = user.get("[1].id");
		resTeamMemberIdInt = user.get("[2].id");
		teamMemberIdInt = user.get("[3].id");
		ownerId = String.valueOf(ownerIdInt);
		adminId = String.valueOf(adminIdInt);
		resTeamMemberId = String.valueOf(resTeamMemberIdInt);
		teamMemberId = String.valueOf(teamMemberIdInt);

		List<List<String>> teamsData = Arrays.asList(Arrays.asList(team1, ownerId),
				Arrays.asList("Engineering Team", ownerId, adminId),
				Arrays.asList("QA Team", ownerId, adminId, teamMemberId),
				Arrays.asList(team2, ownerId, adminId, teamMemberId, resTeamMemberId));

		for (List<String> teamData : teamsData) {
			String teamName = teamData.get(0);
			List<String> userIds = teamData.subList(1, teamData.size());

			ArrayList<String> userIdList = new ArrayList<>(userIds);
			Response response = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), teamName, userIdList);
			Assert.assertEquals(response.getStatusCode(), 200);
		}
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllTeams_Test() {
		GetTeam team = new GetTeam();
		team.setPage_size("4");

		Response response = RestClient.doPost("JSON", albatrossURL, "teams/get", ThreadManager.getOwnerAlbatrossToken(), null, true, team);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.filtered_count", Matchers.is(4));
		response.then().body("data.total_count", Matchers.is(4));
		response.then().body("data.records[1].id", Matchers.notNullValue());
		response.then().body("data.records[1].label", Matchers.containsString("Engineering Team"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getTeamsWithTeamNameFilter_Test() {
		TeamFilters filters = new TeamFilters();
		filters.setTeamName(team1);
		GetTeam team = new GetTeam();
		team.setPage_size("4");
		team.setTeamFilters(filters);

		Response response = RestClient.doPost("JSON", albatrossURL, "teams/get", ThreadManager.getOwnerAlbatrossToken(), null, true, team);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.filtered_count", Matchers.is(1));
		response.then().body("data.total_count", Matchers.is(4));
		response.then().body("data.records[0].id", Matchers.notNullValue());
		response.then().body("data.records[0].label", Matchers.is(team1));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getTeamsWithTeamUsersFilter_Test() {
		TeamFilters filters = new TeamFilters();
		filters.setTeamUsers(adminId);
		GetTeam team = new GetTeam();
		team.setPage_size("4");
		team.setTeamFilters(filters);

		Response response = RestClient.doPost("JSON", albatrossURL, "teams/get", ThreadManager.getOwnerAlbatrossToken(), null, true, team);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.filtered_count", Matchers.is(3));
		response.then().body("data.total_count", Matchers.is(4));
		response.then().body("data.records[0].id", Matchers.notNullValue());
		response.then().body("data.records[0].label", Matchers.is("Engineering Team"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getTeamsWithTeamOwnersFilter_Test() {
		TeamFilters filters = new TeamFilters();
		filters.setTeamCreatedBy(ownerId);
		GetTeam team = new GetTeam();
		team.setPage_size("4");
		team.setTeamFilters(filters);

		Response response = RestClient.doPost("JSON", albatrossURL, "teams/get", ThreadManager.getOwnerAlbatrossToken(), null, true, team);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.filtered_count", Matchers.is(4));
		response.then().body("data.total_count", Matchers.is(4));
		response.then().body("data.records[0].id", Matchers.notNullValue());
		response.then().body("data.records[0].label", Matchers.containsString("Team"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getTeamsWithTeamUsersAndOwnersFilter_Test() {
		TeamFilters filters = new TeamFilters();
		filters.setTeamUsers(ownerId + "," + adminId + "," + resTeamMemberId);
		filters.setTeamCreatedBy(ownerId + "," + teamMemberId);
		GetTeam team = new GetTeam();
		team.setPage_size("4");
		team.setTeamFilters(filters);

		Response response = RestClient.doPost("JSON", albatrossURL, "teams/get", ThreadManager.getOwnerAlbatrossToken(), null, true, team);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.filtered_count", Matchers.is(1));
		response.then().body("data.total_count", Matchers.is(4));
		response.then().body("data.records[0].id", Matchers.notNullValue());
		response.then().body("data.records[0].label", Matchers.is(team2));
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllTeamsWithEmptyPageSize_Test() {
		GetTeam team = new GetTeam();

		Response response = RestClient.doPost("JSON", albatrossURL, "teams/get", ThreadManager.getOwnerAlbatrossToken(), null, true, team);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("status", Matchers.is("fail"));
		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("message", Matchers.containsString("The page size field is required."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserGetAllTeams_Test() {
		GetTeam team = new GetTeam();
		team.setPage_size("1");
		team.setTeamFilters(null);

		Response response = RestClient.doPost("JSON", albatrossURL, "teams/get", ThreadManager.getOwnerAlbatrossToken()+"1234", null, true, team);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

}
