package io.rcrm.api.deals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.DealSplit;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditDealTest extends TestBase {

	public EditDealTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String deal_slug = "";
	commanFunction function = new commanFunction();
	JavaFakerHotlist fakerHotlist = new JavaFakerHotlist();
	String hotlistName = fakerHotlist.getHotlistName();
	Deal deal = new Deal();
	JavaFakerDeal dealFaker = new JavaFakerDeal();
	JavaFakerJob jobFaker = new JavaFakerJob();
	String dealName = dealFaker.getDealName();
	int dealValue = dealFaker.getDealValue();
	String dealStage = dealFaker.getNumber();
	String dealType = dealFaker.getNumber();
	String dealDate = dealFaker.getDealDate();
	String invalidDealValue = dealFaker.getMaxNumber();
	String longText = jobFaker.getJobDescriptionText() + jobFaker.getNoteForCandidate();
	Faker faker = new Faker();
	String paragraph = faker.lorem().characters(301);
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

	@Owner("Smit Patel")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void editDealBySlug422_POST(String deal_slug,String dealname,int dealvalue,String companySlug){
   // Issue in this edit Deal By Slug 422-POST
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", deal_slug);

		String basePath = "deals/{deal}";
		deal.setName(dealName);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				deal);

		// Verify Response Code and body
		response.then().statusCode(422);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("At least one value must change!"));
		
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getEntityValidData", groups = "nightly-build")
	public void editDealBySlug_POST(String deal_slug,String dealname,int dealvalue,String companySlug) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", deal_slug);
		String basePath = "deals/{deal}";

		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);
		deal.setOwner_id(String.valueOf(0));
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				deal);
		
		// Get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response Code and body
		response.then().statusCode(200);
		response.then().body("name", Matchers.containsString(dealName));
		response.then().body("deal_value", Matchers.is(dealValue));
		response.then().body("deal_split.teammates_collaborator[0].split_percentage", Matchers.is(100));

	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void editDealByInvalidSlug404_POST() {
		Deal deal = new Deal();
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", deal_slug + "123459");
		String basePath = "deals/{deal}";
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				deal);


		// Verify Response Code and body
		response.then().statusCode(404);
		response.then().body("error", Matchers.is(true));
		response.then().body("errorCode", Matchers.is(404));
		response.then().body("errorMessage", Matchers.is("Deal doesn't exist"));
	}
	
	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void editDealByInvalidFieldsValues422_POST() {
		JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String deal_slug=jsonDeal.get("slug");
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", deal_slug);

		String basePath = "deals/{deal}";

		// Here we can also use data provider.
		Deal deal = new Deal();
		deal.setName(paragraph);
		 deal.setClose_date("2022-018-14");
		  deal.setDeal_stage(invalidDealValue); 
		deal.setDeal_type("3");

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				deal);
		// Get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response Code and body
		response.then().statusCode(422);
		response.then().body("name[0]", Matchers.containsString("The name may not be greater than 300 characters."));
		  response.then().body("close_date[0]",Matchers.containsString("The close date is not a valid date."));
		  
		  response.then().body("deal_stage[0]", Matchers.is("Invalid deal stage"));
		 
		response.then().body("deal_type[0]", Matchers.containsString("The selected deal type is invalid."));
	}
	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotEditDeal() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		 String dealSlug =jsonDeal.get("slug");
		pathParamters.put("deal", dealSlug);
		String basePath = "deals/{deal}";
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);
		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"12345", null, pathParamters, true,
				deal);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getInvalidDataForDealSplit", groups = "nightly-build")
	public void editDealWithInvalidDealSplitData(int userId, double splitPercentageForTeammates, int teamId,
												   double splitPercentageForTeam, String splitType, int errorCode, String errorMessage) {

		JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String dealSlug =jsonDeal.get("slug");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("deal", dealSlug);

		String basePath = "deals/{deal}";
		deal.setName(dealName);

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

		Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				deal);

		response.then().statusCode(errorCode);
		//response.then().body("error", Matchers.containsString(errorMessage));
	}

	@DataProvider
	public Object[][] getInvalidDataForDealSplit() {
		//get Correct Team id
		Response usersResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
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
		Response response1 = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1", userId1);
		response1.then().statusCode(200);

		Response team1 = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
		team1.then().statusCode(200);
		int teamId =  team1.jsonPath().get("[0].team_id");

		return new Object[][] {
				{-1, 50, teamId, 50, "equal", 422, "Teammate id is not valid"},
				{adminId, 101, teamId, 50, "custom", 422, "Teammate split percentage is not valid"},
				{adminId, 50, -1, 50, "equal", 422, "Team id is not valid"},
				{adminId, 50, teamId, 101, "custom", 422, "Team split percentage is not valid"},
				{adminId, 50, teamId, 50, "invalid", 422, "Split type is not valid"}
		};
	}

	@DataProvider
	public Object[][] getEntityValidData() {

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = jsonCompany.get("slug");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		JsonPath jsonContact1 = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug")+ "," + jsonContact1.get("slug");

		JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, jsonContact.get("slug")).jsonPath();
		JsonPath jsonJob1 = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, jsonContact1.get("slug")).jsonPath();
		String jobSlug = jsonJob.getString("slug")+ "," + jsonJob1.getString("slug");
		JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug, jobSlug).jsonPath();
		 deal_slug=jsonDeal.get("slug");
			String dealName = jsonDeal.get("name");
			int dealValue = jsonDeal.get("deal_value");

		Object data[][] = { { deal_slug,dealName,dealValue,companySlug} };

		return data;
	}

}