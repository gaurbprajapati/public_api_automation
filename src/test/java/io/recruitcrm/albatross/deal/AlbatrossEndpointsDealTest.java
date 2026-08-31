package io.recruitcrm.albatross.deal;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.pojo.albatross.StageHistory;
import io.rcrm.api.pojo.albatross.deal.DealRelatedEntityData;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;
import java.util.*;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AlbatrossEndpointsDealTest extends TestBase {

	commanFunction function = new commanFunction();
	AllCrudFunctions allCurdFunction = new AllCrudFunctions();
	JavaFakerDeal javaFakerDeal = new JavaFakerDeal();
	String baseApiKey;
	String albatrossToken;

	@BeforeClass(alwaysRun = true)	
	public void setUp() {
		baseApiKey = ThreadManager.getAccountApiKey();
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dealEntityDataProvider", groups = "nightly-build")
	public void getDealRelatedEntityData(int dealId, String[] candidateDetails, String[] companyDetails, String[] contactDetails, String[] jobDetails) {
		DealRelatedEntityData dealRelatedEntityData = new DealRelatedEntityData();
		dealRelatedEntityData.setDealIds(Collections.singletonList(dealId));

		Response response = RestClient.doPost("JSON", albatrossURL, "deals/get-related-entity-data", albatrossToken, null, true, dealRelatedEntityData);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Related Entities Fetched Successfully"));
		response.then().body("message_type", Matchers.is("is-success"));

		JsonPath jsonResponse = response.jsonPath();
		String[] parameters = { "data.candidates." + dealId + ".candidatenames",
				"data.candidates." + dealId + ".candidateslugs", "data.companies." + dealId + ".companynames",
				"data.companies." + dealId + ".companyslugs", "data.contacts." + dealId + ".contactnames",
				"data.contacts." + dealId + ".contactslugs", "data.jobs." + dealId + ".jobnames",
				"data.jobs." + dealId + ".jobslugs" };
		String[] values = { candidateDetails[0], candidateDetails[1], companyDetails[0], companyDetails[1], contactDetails[0], contactDetails[1], jobDetails[0], jobDetails[1] };

		for (int i = 0; i < parameters.length; i++)
			Assert.assertTrue(jsonResponse.getString(parameters[i]).contains(values[i]), values[i] + " Not Found in Related Data!");

		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//deals//getDealRelatedEntityData.json"));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dealStageHistoryDataProvider", groups = "nightly-build")
	public void getDealStageHistory(int dealId, String reason) {

		StageHistory stageHistory = new StageHistory();
		stageHistory.setEntity_type(11);
		stageHistory.setEntity_id(dealId);

		Response response = RestClient.doPost("JSON", albatrossURL, "global/get-stage-history", albatrossToken, null, true, stageHistory);
		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Stage history"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data[0].stage_name", Matchers.is("Lost"));
		response.then().body("data[0].reason", Matchers.is(reason));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//global//getStageHistory.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] dealEntityDataProvider() {

		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, baseApiKey).jsonPath();
		String candidateSlug = jsonCandidate.getString("slug");
		String candidateName = jsonCandidate.getString("first_name") + " " + jsonCandidate.getString("last_name");

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, baseApiKey).jsonPath();
		String companySlug = jsonCompany.getString("slug");
		String companyName = jsonCompany.getString("company_name");

		JsonPath jsonContact = function.createNewContact_POST(baseURL, baseApiKey, companySlug).jsonPath();
		String contactSlug = jsonContact.getString("slug");
		String contactName = jsonContact.getString("first_name") + " " + jsonContact.getString("last_name");

		JsonPath jsonJob = function.createNewJob(baseURL, baseApiKey, companySlug, contactSlug).jsonPath();
		String jobSlug = jsonJob.getString("slug");
		String jobName = jsonJob.getString("name");

		HashMap<Integer, String> fieldsMap = new HashMap<>();
		fieldsMap.put(5, companySlug);
		fieldsMap.put(6, jobSlug);
		fieldsMap.put(7, contactSlug);
		fieldsMap.put(8, candidateSlug);

		JsonPath jsonDeal = function.createNewDealWithSpecifiedFields(baseURL, baseApiKey, fieldsMap).jsonPath();
		String dealSlug = jsonDeal.getString("slug");
		int dealId = allCurdFunction.getDealResponse(albatrossURL, albatrossToken, dealSlug).jsonPath().getInt("data.deal.id");

		String[] candidateDetails = new String[] { candidateName, candidateSlug };
		String[] companyDetails = new String[] { companyName, companySlug };
		String[] contactDetails = new String[] { contactName, contactSlug };
		String[] jobDetails = new String[] { jobName, jobSlug };
		return new Object[][] { { dealId, candidateDetails, companyDetails, contactDetails, jobDetails } };
	}

	@DataProvider(parallel = true)
	public Object[][] dealStageHistoryDataProvider() {
		String dealSlug = function.createNewDealWithMandatoryFields(baseURL, baseApiKey).jsonPath().get("slug");
		int dealId = allCurdFunction.getDealResponse(albatrossURL, albatrossToken, dealSlug).jsonPath().getInt("data.deal.id");

		String reason = javaFakerDeal.getStageUpdateReason();
		
		Deal deal = new Deal();
		deal.setName(javaFakerDeal.getDealName());
		deal.setDeal_value(javaFakerDeal.getDealValue());
		deal.setClose_date(javaFakerDeal.getDealDate());
		deal.setDeal_type(javaFakerDeal.getNumber());
		deal.setDeal_stage("2");
		deal.setReason(reason);

		Response dealEditResponse = RestClient.doPost("JSON", baseURL, "deals/" + dealSlug, baseApiKey, null, true, deal);
		dealEditResponse.then().statusCode(200);
		return new Object[][] { { dealId, reason } };
	}
}
