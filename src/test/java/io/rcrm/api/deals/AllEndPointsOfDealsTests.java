package io.rcrm.api.deals;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.pojo.DealSplit;
import io.rcrm.api.pojo.DealSplit.TeammatesCollaborator;
import io.rcrm.api.pojo.DealSplit.TeamsCollaborator;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndPointsOfDealsTests extends TestBase {

	public AllEndPointsOfDealsTests() {
		super();
	}

	String slug = "";
	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	JavaFakerHotlist fakerHotlist = new JavaFakerHotlist();
	String hotlistName = fakerHotlist.getHotlistName();

	JavaFakerDeal dealFaker = new JavaFakerDeal();
	String dealName = dealFaker.getDealName();
	int dealValue = dealFaker.getDealValue();
	String dealStage = dealFaker.getNumber();
	String dealType = dealFaker.getNumber();
	String dealDate = dealFaker.getDealDate();
	String reason = dealFaker.getStageUpdateReason();

	String candidateSlug;
	String companySlug;
	String contactSlug;
	String jobSlug;
	String dealStageName;
	
	String albatrossAuthToken;
	String apiAuthToken;
	
	@BeforeClass(alwaysRun = true)	public void Setup() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void createNewDealWithAllFields(String dealName, Deal deal, String companySlug, String dealStage,
			int dealValue) {

		Response response = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken, null, true, deal);

		response.then().statusCode(200);
		slug = response.jsonPath().getString("slug");
		response.then().body("name", Matchers.containsString(dealName));
		response.then().body("deal_value", Matchers.is(dealValue));
		response.then().body("deal_stage.id", Matchers.is(Integer.parseInt(dealStage)));
		response.then().body("company_slug", Matchers.containsString(companySlug));
		Assert.assertEquals(response.jsonPath().getInt("archived"), 0, "Archive Not Matching!");
		Assert.assertEquals(response.jsonPath().getInt("deal_stage_remarks[0].stage_id"), Integer.parseInt(dealStage), "Deal Stage Id Not Matching!");

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//deal//createDeal.json"));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dealSlug", groups = "nightly-build")
	public void showAllDealsGET(String slug, String dealName, String dealStage) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "deals", apiAuthToken, queryParameters, null, true);

		response.then().statusCode(200);
		response.then().body("data[0].slug", Matchers.equalTo(slug));
		response.then().body("data[0].name", Matchers.is(dealName));
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		response.then().body("data[0].deal_split", Matchers.notNullValue());
		response.then().body("data[0].deal_stage_remarks[0].stage_id", Matchers.is(Integer.parseInt(dealStage)));
		response.then().body("data[0].archived", Matchers.is(0));
		response.then().body("data.size()", Matchers.equalTo(1));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//deal//getAllDeals.json"));
	}
	
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dealSlug", groups = "nightly-build")
	public void searchDealByFields(String slug, String dealName, String dealStage) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("deal_name", dealName);

		Response response = RestClient.doGet("JSON", baseURL, "deals/search", apiAuthToken, queryParameters, null, true);

		response.then().statusCode(200);
		response.then().body("data[0].slug", Matchers.equalTo(slug));
		response.then().body("data[0].name", Matchers.equalToIgnoringCase(dealName));
		response.then().body("data[0].deal_split", Matchers.notNullValue());
		response.then().body("data[0].deal_stage_remarks[0].stage_id", Matchers.is(Integer.parseInt(dealStage)));
		response.then().body("data[0].archived", Matchers.is(0));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//deal//searchDealByFields.json"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dealSlug", groups = "nightly-build")
	public void editDealBySlug(String slug, String dealName, String dealStage) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", slug);
		String basePath = "deals/{deal}";

		JsonPath jsonDealStages = function.getAllDealStages(baseURL, apiAuthToken).jsonPath();
		dealStage = jsonDealStages.getString("id[1]");
		dealStageName = jsonDealStages.getString("label[1]");
		dealName = dealName + " Edited";
		Deal deal = new Deal();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_type(dealType);
		deal.setDeal_stage(dealStage);
		deal.setReason(reason);

		Response usersResponse = function.getUsers(baseURL, apiAuthToken);
		usersResponse.then().statusCode(200);
		JsonPath user = usersResponse.jsonPath();
		int accountOwnerid = user.get("[0].id");
		int resTeamMember = user.get("[2].id");
		int teamMember = user.get("[3].id");

		ArrayList<String> userId1 = new ArrayList<String>();
		userId1.add(String.valueOf(accountOwnerid));
		userId1.add(String.valueOf(teamMember));

		Response response1 = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team2", userId1);
		response1.then().statusCode(200);

		Response team1 = function.getTeams(baseURL, apiAuthToken);
		team1.then().statusCode(200);
		int teamId = team1.jsonPath().get("[0].team_id");

		TeammatesCollaborator teammatesCollaborator = new TeammatesCollaborator();
		teammatesCollaborator.setTeammate_id(resTeamMember);
		teammatesCollaborator.setSplit_percentage(25.25);

		TeamsCollaborator teamsCollaborator = new TeamsCollaborator();
		teamsCollaborator.setTeam_id(teamId);
		teamsCollaborator.setSplit_percentage(35.35);

		DealSplit dealSplit = new DealSplit();
		dealSplit.setTeammates_collaborator(new TeammatesCollaborator[] { teammatesCollaborator });
		dealSplit.setTeams_collaborator(new TeamsCollaborator[] { teamsCollaborator });
		dealSplit.setSplit_type("custom");

		deal.setDeal_split(dealSplit);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, true,
				deal);

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("name", Matchers.containsString(dealName));
		response.then().body("archived", Matchers.is(0));

		Assert.assertEquals(response.jsonPath().getInt("deal_split.teammates_collaborator[1].teammate_id"), resTeamMember, "Teammate id is not as expected");
		Assert.assertEquals(response.jsonPath().getDouble("deal_split.teammates_collaborator[1].split_percentage"), 25.25, "Teammate split percentage is not as expected");
		Assert.assertEquals(response.jsonPath().getInt("deal_split.teams_collaborator[0].team_id"), teamId, "Team id is not as expected");
		Assert.assertEquals(response.jsonPath().getDouble("deal_split.teams_collaborator[0].split_percentage"), 35.35, "Team split percentage is not as expected");
		Assert.assertEquals(response.jsonPath().getDouble("deal_split.teammates_unallocated_split_percentage"), 74.75, "teammate unallocated split percentage is not as expected");
		Assert.assertEquals(response.jsonPath().getDouble("deal_split.teams_unallocated_split_percentage"), 64.65, "team unallocated split percentage is not as expected");
		Assert.assertEquals(response.jsonPath().getString("deal_split.split_type"), "custom", "split type is not as expected");
		Assert.assertEquals(response.jsonPath().getInt("deal_stage_remarks[0].stage_id"), Integer.parseInt(dealStage), "Deal Stage Id Not Matching!");
		Assert.assertEquals(response.jsonPath().getString("deal_stage_remarks[0].stage_name"), dealStageName, "Deal Stage Name Not Matching!");
		Assert.assertEquals(response.jsonPath().getString("deal_stage_remarks[0].reason"), reason, "Reason Not Matching!");

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//deal//editDeal.json"));
	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dealSlug", groups = "nightly-build")
	public void searchDealBySlug(String slug, String dealName, String dealStage) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", slug);

		Response response = RestClient.doGet("JSON", baseURL, "deals/{deal}", apiAuthToken, null, pathParamters, true);

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("slug", Matchers.equalTo(slug));
		response.then().body("name", Matchers.equalTo(dealName));
		response.then().body("deal_stage_remarks[0].stage_id", Matchers.is(Integer.parseInt(dealStage)));
		response.then().body("archived", Matchers.is(0));
		
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//deal//searchDealBySlug.json"));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dealSlug", groups = "nightly-build")
	public void deleteDealBySlug(String slug, String dealName, String dealStage) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", slug);
		String basePath = "deals/{deal}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, false);

		Assert.assertEquals(response.getStatusCode(), 200);
		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody.contains("Deleted Successfully!"), "Deleted Successfully!");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dealSlug", groups = "nightly-build")
	public void getDealStageHistory(String slug, String dealName, String dealStage) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("deal", slug);

		Response response = RestClient.doGet("JSON", baseURL, "deal/get-stage-history/{deal}", apiAuthToken, null, pathParams, true);
		assert response != null : "Get Stage History Response is Null!";
		response.then().statusCode(200);
		response.then().body("deal_stage_remarks[0].stage_id", Matchers.is(Integer.parseInt(dealStage)));
		response.then().body("deal_stage_remarks[0].stage_name", Matchers.notNullValue());
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//deal//getDealStageHistory.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] getEntityValidData() {
		try {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath().get("slug");
			String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath().get("slug");
			String jobSlug = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath().get("slug");
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath().get("slug");

			JsonPath jsonDealStages = function.getAllDealStages(baseURL, apiAuthToken).jsonPath();
			dealStage = jsonDealStages.getString("id[0]");

			Deal deal = new Deal();
			deal.setName(dealName);
			deal.setDeal_value(dealValue);
			deal.setClose_date(dealDate);
			deal.setDeal_type(dealType);
			deal.setCompany_slug(companySlug);
			deal.setJob_slug(jobSlug);
			deal.setContact_slugs(contactSlug);
			deal.setCandidate_slug(candidateSlug);
			deal.setDeal_stage(dealStage);

			Object data[][] = { { dealName, deal, companySlug, dealStage, dealValue } };

			return data;
		} catch (Exception e) {
			throw new RuntimeException("Failed to prepare test data", e);
		}
	}

	@DataProvider
	public Object[][] dealSlug() {
		JsonPath json = function.createDealWithAllFields(baseURL, apiAuthToken, albatrossURL, albatrossAuthToken).jsonPath();
		
		String dealName = json.getString("name");
		String dealStage = json.getString("deal_stage.id");
		return new Object[][] { { json.get("slug"), dealName, dealStage } };
	}
}
