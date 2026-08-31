package io.rcrm.api.candidate;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.CandQuesAnsWithoutJob;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllCandidatesTest extends TestBase {

	public GetAllCandidatesTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	String slug = "";

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerJob jobFaker = new JavaFakerJob();
	commanFunction function = new commanFunction();
	AllCrudFunctions privateFunction = new AllCrudFunctions();
	
	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	//String CandidateEmail = "rcrmtest0@gmail.com";
	String CandidateEmail = fakerCandidate.getEmailID();
	String CandidateNumber = fakerCandidate.getContactNumber();
	String linkedinLink = fakerCandidate.getUrl();
	
	String apiAuthToken, albatrossTkn;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}
	
	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void createNewCandidateWithMandatoryFields() {

		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setLinkedin(linkedinLink);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 200);

		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");

		slug = jp.get("slug");
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void showAllCandidates() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "1");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("data[0].first_name", Matchers.is(CandidateFirstName));
		response.then().body("data.created_by", Matchers.notNullValue());
		response.then().body("data.updated_by", Matchers.notNullValue());
		response.then().body("data.owner", Matchers.notNullValue());
		response.then().body("data.qualification_id", Matchers.notNullValue());
		response.then().body("data.currency_id", Matchers.notNullValue());
		response.then().body("data.salary_type.id", Matchers.notNullValue());

	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCandidateWithCustomFields", groups = "nightly-build")
	public void verifyCustomFieldValueInShowAllCandidates_Test(String value, String value2) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "10");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "candidates", apiAuthToken, queryParameters, null, true);
		
		response.then().statusCode(200);
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		response.then().body("data.size()", Matchers.equalTo(2));
		response.then().body("data[0].custom_fields[0].value", Matchers.containsString(value2));
		response.then().body("data[1].custom_fields[0].value", Matchers.containsString(value));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//getAllCandidates.json"));
	}
	
	@DataProvider
	public Object[][] createCandidateWithCustomFields() {
		int entityId1, entityId2, columnId;
		String candidateSlug = function.getEntityResponse(baseURL, apiAuthToken, "candidate");
		entityId1 =  privateFunction.getCandidateResponse(albatrossURL, albatrossTkn, candidateSlug).jsonPath().get("data.candidate.id");
		candidateSlug = function.getEntityResponse(baseURL, apiAuthToken, "candidate");
		entityId2 =  privateFunction.getCandidateResponse(albatrossURL, albatrossTkn, candidateSlug).jsonPath().get("data.candidate.id");
		Response response = function.createCustomFieldsResponse(albatrossURL, albatrossTkn, "candidate", "candidateField", "email", "");
		columnId = response.jsonPath().get("data.custumField.columnid");
		String value1 = fakerCandidate.getEmailID();
		String value2 = fakerCandidate.getEmailID();
		privateFunction.updateCustomField("candidate", albatrossURL, entityId1, albatrossTkn, "custcolumn" + columnId, value1);
		privateFunction.updateCustomField("candidate", albatrossURL, entityId2, albatrossTkn, "custcolumn" + columnId, value2);
		return new Object[][] { { value1, value2 } };
	}
}
