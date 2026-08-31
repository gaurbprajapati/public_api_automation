package io.rcrm.api.deals;

import java.util.ArrayList;
import java.util.List;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.DealSplit;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.restassured.path.json.JsonPath;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.pojo.DealCustomField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateNewDealTest extends TestBase {

	public CreateNewDealTest() {
		super();
	}

	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

	Faker faker = new Faker();
	JavaFakerDeal dealFaker = new JavaFakerDeal();
	JavaFakerJob jobFaker = new JavaFakerJob();
	JavaFakerCompany companyFaker = new JavaFakerCompany();

	String dealName = dealFaker.getDealName();
	int dealValue = dealFaker.getDealValue();
	String dealStage = dealFaker.getNumber();
	String dealType = dealFaker.getNumber();
	String dealDate = dealFaker.getDealDate();
	String invalidDealValue = dealFaker.getMaxNumber();
	String longText = jobFaker.getJobDescriptionText() + jobFaker.getNoteForCandidate();
	String paragraph = faker.lorem().characters(301);

	String albatrossAuthToken;
	String apiAuthToken;

	@BeforeClass(alwaysRun = true)
	public void Setup() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void createNewDealWithMandatoryFields() {
		Deal deal = new Deal();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);

		Response response = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken, null, true, deal);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("name", Matchers.containsString(dealName));
		response.then().body("deal_value", Matchers.is(dealValue));
		response.then().body("deal_stage.id", Matchers.is(1));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void createNewDealWithAllFields(String company_slug, String contact_slug, String job_slug,
			String candidate_slug) {
		Deal deal = new Deal();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);
		deal.setCompany_slug(company_slug);
		deal.setJob_slug(job_slug);
		deal.setContact_slugs(contact_slug);
		deal.setCandidate_slug(candidate_slug);

		Response usersResponse = function.getUsers(baseURL, apiAuthToken);
		Assert.assertEquals(usersResponse.getStatusCode(), 200);
		JsonPath user = usersResponse.jsonPath();
		int accountOwnerid = user.get("[0].id");
		int adminId = user.get("[1].id");
		int resTeamMember = user.get("[2].id");
		int teamMember = user.get("[3].id");

		ArrayList<String> userId1 = new ArrayList<String>();
		userId1.add(String.valueOf(accountOwnerid));
		userId1.add(String.valueOf(adminId));
		userId1.add(String.valueOf(resTeamMember));
		userId1.add(String.valueOf(teamMember));

		Response response1 = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", userId1);
		Assert.assertEquals(response1.getStatusCode(), 200);

		Response team1 = function.getTeams(baseURL, apiAuthToken);
		team1.then().statusCode(200);
		int teamId = team1.jsonPath().get("[0].team_id");

		DealSplit.TeammatesCollaborator teammatesCollaborator = new DealSplit.TeammatesCollaborator();
		teammatesCollaborator.setTeammate_id(adminId);
		teammatesCollaborator.setSplit_percentage(50);

		DealSplit.TeammatesCollaborator teammatesCollaborator1 = new DealSplit.TeammatesCollaborator();
		teammatesCollaborator1.setTeammate_id(resTeamMember);
		teammatesCollaborator1.setSplit_percentage(50);

		DealSplit.TeammatesCollaborator teammatesCollaborator2 = new DealSplit.TeammatesCollaborator();
		teammatesCollaborator2.setTeammate_id(teamMember);
		teammatesCollaborator2.setSplit_percentage(50);

		DealSplit.TeamsCollaborator teamsCollaborator = new DealSplit.TeamsCollaborator();
		teamsCollaborator.setTeam_id(teamId);
		teamsCollaborator.setSplit_percentage(100);

		DealSplit dealSplit = new DealSplit();
		dealSplit.setTeammates_collaborator(new DealSplit.TeammatesCollaborator[] { teammatesCollaborator,
				teammatesCollaborator1, teammatesCollaborator2 });
		dealSplit.setTeams_collaborator(new DealSplit.TeamsCollaborator[] { teamsCollaborator });
		// split percentage will be equally divided among all team mates and teams when split type is equal
		dealSplit.setSplit_type("equal");

		deal.setDeal_split(dealSplit);

		Response response = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken, null, true, deal);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("name", Matchers.containsString(dealName));
		response.then().body("deal_value", Matchers.is(dealValue));
		response.then().body("deal_stage.id", Matchers.is(1));
		response.then().body("company_slug", Matchers.containsString(company_slug));
		Assert.assertEquals(response.jsonPath().getInt("deal_split.teammates_collaborator[1].teammate_id"), adminId,
				"Teammate id is not as expected");
		Assert.assertEquals(response.jsonPath().getDouble("deal_split.teammates_collaborator[1].split_percentage"),
				25.0, "Teammate split percentage is not as expected");
		Assert.assertEquals(response.jsonPath().getInt("deal_split.teammates_collaborator[2].teammate_id"),
				resTeamMember, "Teammate id is not as expected");
		Assert.assertEquals(response.jsonPath().getDouble("deal_split.teammates_collaborator[2].split_percentage"),
				25.0, "Teammate split percentage is not as expected");
		Assert.assertEquals(response.jsonPath().getInt("deal_split.teammates_collaborator[3].teammate_id"), teamMember,
				"Teammate id is not as expected");
		Assert.assertEquals(response.jsonPath().getDouble("deal_split.teammates_collaborator[3].split_percentage"),
				25.0, "Teammate split percentage is not as expected");

		Assert.assertEquals(response.jsonPath().getInt("deal_split.teams_collaborator[0].team_id"), teamId,
				"Team id is not as expected");
		Assert.assertEquals(response.jsonPath().getDouble("deal_split.teams_collaborator[0].split_percentage"), 100.0,
				"Team split percentage is not as expected");
		Assert.assertEquals(response.jsonPath().getDouble("deal_split.teammates_unallocated_split_percentage"), 0.0,
				"teammate unallocated split percentage is not as expected");
		Assert.assertEquals(response.jsonPath().getDouble("deal_split.teams_unallocated_split_percentage"), 0.0,
				"team unallocated split percentage is not as expected");
		Assert.assertEquals(response.jsonPath().getString("deal_split.split_type"), "equal",
				"split type is not as expected");
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void userShouldNotAbleToCreateDealWithEmptyData() {
		Deal deal = new Deal();
		deal.setDeal_value(-1);
		Response response = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken, null, true, deal);

		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("name[0]", Matchers.containsString("The name field is required."));
		response.then().body("deal_value[0]", Matchers.containsString("The deal value must be at least 0."));
		response.then().body("close_date[0]", Matchers.containsString("The close date field is required."));
		response.then().body("deal_stage[0]", Matchers.containsString("The deal stage field is required."));
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void userShouldNotAbleToCreateDealWithMoreThanLimit() {
		Deal deal = new Deal();
		deal.setName(paragraph);
		deal.setClose_date("2022-018-14");
		deal.setDeal_stage(invalidDealValue);
		deal.setDeal_type("3");

		Response response = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken, null, true, deal);

		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("name[0]", Matchers.containsString("The name may not be greater than 300 characters."));
		response.then().body("close_date[0]", Matchers.containsString("The close date is not a valid date."));
		response.then().body("deal_stage[0]", Matchers.is("Invalid deal stage"));
		response.then().body("deal_type[0]", Matchers.containsString("The selected deal type is invalid."));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCreateNewDeal() {
		Deal deal = new Deal();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);

		Response response = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken + "12345", null, true, deal);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "getInvalidDataForDealSplit", groups = "nightly-build")
	public void createDealWithInvalidDealSplitData(int userId, double splitPercentageForTeammates, int teamId,
			double splitPercentageForTeam, String splitType, int errorCode, String errorMessage) {
		Deal deal = new Deal();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);

		DealSplit.TeammatesCollaborator teammatesCollaborator = new DealSplit.TeammatesCollaborator();
		teammatesCollaborator.setTeammate_id(userId);
		teammatesCollaborator.setSplit_percentage(splitPercentageForTeammates);

		DealSplit.TeamsCollaborator teamsCollaborator = new DealSplit.TeamsCollaborator();
		teamsCollaborator.setTeam_id(teamId);
		teamsCollaborator.setSplit_percentage(splitPercentageForTeam);

		DealSplit dealSplit = new DealSplit();
		dealSplit.setTeammates_collaborator(new DealSplit.TeammatesCollaborator[] { teammatesCollaborator });
		dealSplit.setTeams_collaborator(new DealSplit.TeamsCollaborator[] { teamsCollaborator });
		dealSplit.setSplit_type(splitType);
		deal.setDeal_split(dealSplit);

		Response response = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken, null, true, deal);
		Assert.assertEquals(response.getStatusCode(), errorCode);
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createDealWithNumberTypeCustomField() {

		JavaFakerCandidate faker = new JavaFakerCandidate();

		ExtraField extraField = new ExtraField();
		extraField.setColumnid(1);
		extraField.setEntitytypeid(11);
		extraField.setExtrafieldname(faker.getCustomFieldName("Deal"));
		extraField.setDefaultvalue(null);
		extraField.setExtrafieldtype("number");
		CustomFieldAlbatross customFieldAlbatross = new CustomFieldAlbatross();
		customFieldAlbatross.setCustumField(extraField);

		Response response1 = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false,
				customFieldAlbatross);
		Assert.assertEquals(response1.getStatusCode(), 200);

		String randomDecimalValue = dealFaker.getRandomDecimalValue(2);
		DealCustomField deal = new DealCustomField();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);

		List<DealCustomField.CustomField> customFields = new ArrayList<>();
		DealCustomField.CustomField customField = new DealCustomField.CustomField();
		customField.setField_id(1);
		customField.setValue(randomDecimalValue);
		customFields.add(customField);

		deal.setCustom_fields(customFields);

		Response response = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken, null, true, deal);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("name", Matchers.containsString(dealName));
		response.then().body("custom_fields[0].entity_type", Matchers.is("deal"));
		response.then().body("custom_fields[0].field_type", Matchers.is("number"));
		response.then().body("custom_fields[0].value", Matchers.is(randomDecimalValue));
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createDealWithDateTimeCustomField() {
		
		Response customFieldResponse = allCrudFunctions.createCustomFields(albatrossURL, albatrossAuthToken, "date_time", 11);
		Assert.assertEquals(customFieldResponse.getStatusCode(), 200);

		String randomDate = companyFaker.getDateTimeCustomFieldValue();
		DealCustomField deal = new DealCustomField();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);

		List<DealCustomField.CustomField> customFields = new ArrayList<>();
		DealCustomField.CustomField customField = new DealCustomField.CustomField();
		customField.setField_id(1);
		customField.setValue(randomDate);
		customFields.add(customField);

		deal.setCustom_fields(customFields);

		Response response = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken, null, true, deal);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("name", Matchers.containsString(dealName));
		response.then().body("custom_fields[0].entity_type", Matchers.is("deal"));
		response.then().body("custom_fields[0].field_type", Matchers.is("date_time"));
		response.then().body("custom_fields[0].value", Matchers.startsWith(randomDate.substring(0, 19)));
	}
	
	@DataProvider(parallel = true)
	public Object[][] getEntityValidData() {

		String candidateSlug1 = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath()
				.get("slug");
		String candidateSlug2 = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath()
				.get("slug");
		String candidateSlug = candidateSlug1 + "," + candidateSlug2;

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath().get("slug");
		String contactSlug1 = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath().get("slug");
		String contactSlug2 = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath().get("slug");
		String contactSlug = contactSlug1 + "," + contactSlug2;
		String jobSlug1 = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug1).jsonPath()
				.get("slug");
		String jobSlug2 = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug2).jsonPath()
				.get("slug");
		String jobSlug = jobSlug1 + "," + jobSlug2;

		Object data[][] = { { companySlug, contactSlug, jobSlug, candidateSlug } };

		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getInvalidDataForDealSplit() {

		Response usersResponse = function.getUsers(baseURL, apiAuthToken);
		usersResponse.then().statusCode(200);
		JsonPath user = usersResponse.jsonPath();
		int accountOwnerid = user.get("[0].id");
		int adminId = user.get("[1].id");
		int resTeamMember = user.get("[2].id");
		int teamMember = user.get("[3].id");

		ArrayList<String> userId1 = new ArrayList<String>();
		userId1.add(String.valueOf(accountOwnerid));
		userId1.add(String.valueOf(adminId));
		userId1.add(String.valueOf(resTeamMember));
		userId1.add(String.valueOf(teamMember));
		Response response1 = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", userId1);
		response1.then().statusCode(200);

		Response team1 = function.getTeams(baseURL, apiAuthToken);
		team1.then().statusCode(200);
		int teamId = team1.jsonPath().get("[0].team_id");

		return new Object[][] { { -1, 50, teamId, 50, "equal", 422, "Teammate id is not valid" },
				{ adminId, 101, teamId, 50, "custom", 422,
						"Sum of Split Percentage for Teammates should not exceed 100%" },
				{ adminId, 50, -1, 50, "equal", 422, "Team id is not valid" },
				{ adminId, 50, teamId, 101, "custom", 422,
						"Sum of Split Percentage for Teams should not exceed 100%." },
				{ adminId, 50, teamId, 50, "invalid", 422, "Split_type must be custom/equal(case insensitive)." } };
	}

}
