package io.recruitcrm.ConversationService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetCandidateContactNumbersTest extends TestBase {

	private static final String BASE_PATH = "conversation/contact-numbers";
	private static final String ENTITY_TYPE_CANDIDATE = "5";
	JavaFakerCandidate faker = new JavaFakerCandidate();
	
	// Cross account test fields
	String albatrossAuthTokenA;
	String publicAPIKeyA;
	String albatrossAuthTokenB;
	String publicAPIKeyB;
	String candidateSlug; // Store candidate slug for tests
	String primaryNumber;
	int candidateId; // Store candidate ID for reference
	AllCrudFunctions function = new AllCrudFunctions();
	private static final int CANDIDATE_ENTITY_TYPE_ID = 5;
	private static final String PHONE_CUSTOM_FIELD_TYPE = "phonenumber";
	private int customFieldColumnId = 1; // Will be set after creating custom field
	private String customPhoneNumber; // Custom phone number to add
	
	@BeforeClass(alwaysRun = true)	public void setUp() {
		// Setup for AccountA
		albatrossAuthTokenA = getTokenForAccount("AccountA","valid");
		publicAPIKeyA = getAccountApiKey("AccountA");
		// Setup for AccountB
		albatrossAuthTokenB = getTokenForAccount("AccountB","valid");
		publicAPIKeyB = getAccountApiKey("AccountB");
		createCandidateAndGetSlug();
		createPhoneCustomFieldAndSetValue();
	}


	@Owner("Rahul Shibu")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateContactNumbers_200() {
		Map<String, String> queryParameters = createQueryParams(ENTITY_TYPE_CANDIDATE, candidateSlug);
		Response response = RestClient.doGet("JSON", commURL, BASE_PATH, albatrossAuthTokenA, queryParameters, null, true);

		// Validate response status
		assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
		assertThat("Expected primary contact number not null", response.jsonPath().get("primary"), notNullValue());
		assertThat("Expected primary contact number not empty", response.jsonPath().getString("primary"), not(emptyString()));
		assertThat("Expected custom array", response.jsonPath().getList("custom"), notNullValue());
		assertThat("Expected historical array", response.jsonPath().getList("historical"), notNullValue());

		List<String> customNumbers = response.jsonPath().getList("custom");
		assertThat("Custom array should contain our custom phone number", customNumbers, hasItem(customPhoneNumber));
		String actualPrimaryNumber = response.jsonPath().getString("primary");
		assertThat("Primary number should match the candidate's contact number", actualPrimaryNumber, equalTo(primaryNumber));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/conversation/getCandidateContactNumber.json"));
	}
	
	@Owner("Rahul Shibu")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateContactNumbers_404() {
		// Use non-existent candidate slug
		Map<String, String> queryParameters = createQueryParams(ENTITY_TYPE_CANDIDATE, "non-existent-slug");
		Response response = RestClient.doGet("JSON", commURL, BASE_PATH, albatrossAuthTokenA, queryParameters, null, true);
		
		assertThat("Expected status code 404", response.getStatusCode(), equalTo(404));
		assertThat("Expected error flag", response.jsonPath().getBoolean("error"), equalTo(true));
		assertThat("Expected error code", response.jsonPath().getInt("errorCode"), equalTo(404));
		assertThat("Expected error message", response.jsonPath().getString("errorMessage"), equalTo("Candidate not found."));
	}
	
	@Owner("Rahul Shibu")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateContactNumbers_422_InvalidEntityType() {
		// Use invalid entity type
		Map<String, String> queryParameters = createQueryParams("2", candidateSlug);
		Response response = RestClient.doGet("JSON", commURL, BASE_PATH, albatrossAuthTokenA, queryParameters, null, true);
		
		assertThat("Expected status code 422", response.getStatusCode(), equalTo(422));
		assertThat("Expected error flag", response.jsonPath().getBoolean("error"), equalTo(true));
		assertThat("Expected error code", response.jsonPath().getInt("errorCode"), equalTo(422));
		assertThat("Expected error message", response.jsonPath().getString("errorMessage"), equalTo("Unsupported entity type."));
	}
	
	@Owner("Rahul Shibu")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateContactNumbers_422_MissingEntityType() {
		// Missing entityType parameter
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entitySlug", candidateSlug);
		
		Response response = RestClient.doGet("JSON", commURL, BASE_PATH, albatrossAuthTokenA, queryParameters, null, true);
		
		assertThat("Expected status code 422", response.getStatusCode(), equalTo(422));
		assertThat("Expected error flag", response.jsonPath().getBoolean("error"), equalTo(true));
		assertThat("Expected error code", response.jsonPath().getInt("errorCode"), equalTo(422));
		assertThat("Expected error message", response.jsonPath().getString("errorMessage"), equalTo("The entity type id field is required."));
	}
	
	@Owner("Rahul Shibu")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateContactNumbers_422_MissingEntitySlug() {
		// Missing entitySlug parameter
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityTypeId", ENTITY_TYPE_CANDIDATE);
		
		Response response = RestClient.doGet("JSON", commURL, BASE_PATH, albatrossAuthTokenA, queryParameters, null, true);
		
		assertThat("Expected status code 422", response.getStatusCode(), equalTo(422));
		assertThat("Expected error flag", response.jsonPath().getBoolean("error"), equalTo(true));
		assertThat("Expected error code", response.jsonPath().getInt("errorCode"), equalTo(422));
		assertThat("Expected error message", response.jsonPath().getString("errorMessage"), equalTo("The entity slug field is required."));
	}
	
	@Owner("Rahul Shibu")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateContactNumbers_401() {
		Map<String, String> queryParameters = createQueryParams(ENTITY_TYPE_CANDIDATE, candidateSlug);
		Response response = RestClient.doGet("JSON", commURL, BASE_PATH, "InvalidToken", queryParameters, null, true);
		
		assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
		assertThat("Expected error message", response.jsonPath().getString("error"), equalTo("Unauthorized"));
	}
	
	@Owner("Rahul Shibu")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateContactNumbers_CrossAccount() {
		// Try to access AccountA's candidate using AccountB's token
		Map<String, String> queryParameters = createQueryParams(ENTITY_TYPE_CANDIDATE, candidateSlug);
		Response response = RestClient.doGet("JSON", commURL, BASE_PATH, albatrossAuthTokenB, queryParameters, null, true);

		assertThat("Expected status code 401 for cross-account access", response.getStatusCode(), equalTo(401));
		assertThat("Expected error message for cross-account access", response.jsonPath().getString("error"), equalTo("Unauthorized"));
	}

	private void createCandidateAndGetSlug() {
		Response createResponse = function.createCandidate(albatrossURL, albatrossAuthTokenA);
		assertThat("Candidate creation should be successful", createResponse.getStatusCode(), equalTo(200));

		candidateId = createResponse.jsonPath().getInt("data.candidate.id");
		candidateSlug = createResponse.jsonPath().getString("data.candidate.slug");
		primaryNumber = createResponse.jsonPath().getString("data.candidate.contactnumber");
		assertThat("Candidate ID should not be null", candidateId, notNullValue());
		assertThat("Candidate slug should not be null", candidateSlug, notNullValue());
		assertThat("Candidate slug should not be empty", candidateSlug, not(emptyString()));
	}
	private void createPhoneCustomFieldAndSetValue() {
		createPhoneNumberCustomField();
		setCustomPhoneNumberValue();
	}

	private void createPhoneNumberCustomField() {
		String customFieldName = "phone custom";
		customFieldColumnId = 1;

		// Create request body matching exact curl format
		JSONObject custumField = new JSONObject();
		custumField.put("columnid", customFieldColumnId);
		custumField.put("extrafieldname", customFieldName);
		custumField.put("extrafieldtype", PHONE_CUSTOM_FIELD_TYPE);
		custumField.put("entitytypeid", CANDIDATE_ENTITY_TYPE_ID);
		custumField.put("defaultvalue", JSONObject.NULL);
		custumField.put("defaultoptionsvalue", new ArrayList<>());

		JSONObject requestBody = new JSONObject();
		requestBody.put("custumField", custumField);
		requestBody.put("deleteSocialFile", false);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthTokenA, null, false, requestBody);
		assertThat("Custom field creation should be successful", response.getStatusCode(), equalTo(200));
		assertThat("Custom field should be saved successfully", response.jsonPath().getString("message"), equalTo("Custom Field Saved Successfully"));
	}


	private void setCustomPhoneNumberValue() {
		customPhoneNumber = faker.getContactNumber();
		List<Integer> entityIds = Arrays.asList(candidateId);
		String customFieldKey = "custcolumn" + customFieldColumnId;

		UpdateFields updateFields = new UpdateFields();
		updateFields.setKey(customFieldKey);
		updateFields.setValue(customPhoneNumber);
		updateFields.setTableFlag("candidate");
		updateFields.setId(entityIds);

		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthTokenA, null, true, updateFields);
		assertThat("Custom field update should be successful", response.getStatusCode(), equalTo(200));
		assertThat("Update should be successful", response.jsonPath().getString("message_type"), equalTo("is-success"));
		assertThat("Update message should confirm success", response.jsonPath().getString("message"), equalTo("Field Updated Successfully"));
	}

	private Map<String, String> createQueryParams(String entityType, String entitySlug) {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entityTypeId", entityType);
		queryParameters.put("entitySlug", entitySlug);
		return queryParameters;
	}

}
