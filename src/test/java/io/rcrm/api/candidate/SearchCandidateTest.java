package io.rcrm.api.candidate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.CustomField;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.pojo.albatross.Login;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1")
public class SearchCandidateTest extends TestBase {

	public SearchCandidateTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	commanFunction function = new commanFunction();
	String slug = "";
	String candidateSlug = "";
	Candidate candidate = null;
	int owner = 0;
	String callCreatedOn;
	String meetingCreatedOn;
	String lastCommunication;
	String candidateFullName;
	String todayDate = java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString();

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerJob jobFaker = new JavaFakerJob();
	String addRandomString = RandomStringUtils.randomAlphabetic(4);
	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	String CandidateEmail = "rcrmtest" + addRandomString + "@yopmail.com";
	//	String CandidateEmail = fakerCandidate.getEmailID();
	String CandidateNumber = fakerCandidate.getContactNumber();
	String linkedinLink = fakerCandidate.getUrl();
	String country = fakerCandidate.getCountry();
	String state = fakerCandidate.getState();
	
	String apiAuthToken;

	@BeforeClass(alwaysRun = true)		public void setUp() {
			apiAuthToken = ThreadManager.getAccountApiKey();
		}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void createNewCandidateWithMandatoryFields() {

		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setLinkedin(linkedinLink);
		candidate.setCountry(country);
		candidate.setState(state);
		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null,
				true, candidate);
		response.then().body("last_calllog_added_on", Matchers.nullValue());
		response.then().body("last_calllog_added_by", Matchers.nullValue());
		response.then().body("last_email_sent_on", Matchers.nullValue());
		response.then().body("last_email_sent_by", Matchers.nullValue());
		response.then().body("last_sms_sent_on", Matchers.nullValue());
		response.then().body("last_sms_sent_by", Matchers.nullValue());
		response.then().body("last_meeting_created_on", Matchers.nullValue());
		response.then().body("last_meeting_created_by", Matchers.nullValue());
		response.then().body("last_linkedin_message_sent_on", Matchers.nullValue());
		response.then().body("last_linkedin_message_sent_by", Matchers.nullValue());
		response.then().body("last_communication", Matchers.nullValue());

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		candidateSlug = jp.getString("slug");

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 200);

		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");
		Assert.assertEquals(linkedinLink, jp.get("linkedin").toString(), "linkedin");
		Assert.assertEquals(country, jp.get("country").toString(), "country");

		slug = jp.get("slug");
		owner = jp.get("owner");

	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByEmail() {

		//adding a call log to candidate
		callCreatedOn = function.createNewCallLogWithEntitySlug(baseURL, ThreadManager.getAccountApiKey(),"candidate",slug).jsonPath().get("created_on");
		lastCommunication = "Call on "+java.time.OffsetDateTime.parse(callCreatedOn).toLocalDateTime().toString().replace('T', ' ');

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("email", CandidateEmail);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		validateCommunicationFields(response, "data[0]", callCreatedOn, owner, null, null, lastCommunication);
		// Verify Response using Assertion and JsonPath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(CandidateEmail, jp.get("data.email[0]"), "email");
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByFirstName() {

		//sending an email to candidate
		function.sendEmailToCandCont(1, "Candidate", nymaURLv3, CandidateEmail, candidateFullName, slug, ThreadManager.getOwnerAlbatrossToken());
		lastCommunication = "Email on " + todayDate;

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("first_name", CandidateFirstName);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		validateCommunicationFields(response, "data[0]", callCreatedOn, owner, todayDate, null, lastCommunication);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(CandidateFirstName, jp.get("data.first_name[0]"), "first_name");

	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByLastName() {
		//adding a meeting to candidate
		meetingCreatedOn = function.createNewMeetingWithEntitySlug(baseURL, ThreadManager.getAccountApiKey(),"candidate",slug).jsonPath().get("created_on");
		lastCommunication = "Meeting on "+java.time.OffsetDateTime.parse(meetingCreatedOn).toLocalDateTime().toString().replace('T', ' ');

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("last_name", CandidateLastName);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		validateCommunicationFields(response, "data[0]", callCreatedOn, owner, todayDate, meetingCreatedOn, lastCommunication);
		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(CandidateLastName, jp.get("data.last_name[0]"), "last_name");
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByLinkedin() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("linkedin", linkedinLink);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(linkedinLink, jp.get("data.linkedin[0]"), "linkedin");

	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateBySlug() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("candidate_slug", slug);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(slug, jp.get("data.slug[0]"), "slug");
		Assert.assertEquals(CandidateFirstName, jp.get("data.first_name[0]"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("data.last_name[0]"), "last_name");
		Assert.assertEquals(linkedinLink, jp.get("data.linkedin[0]"), "linkedin");
		Assert.assertEquals(CandidateEmail, jp.get("data.email[0]"), "email");
		int dataSize = jp.getInt("data.size()");
		Assert.assertEquals(dataSize, 1);
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", dataProvider = "getExactSearchData", groups = "nightly-build")
	public void searchCandidateByAllFields(int exactSearch) throws ParseException {

		Map<String, String> queryParameters = new HashMap<String, String>();

		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		Date expectedDate2 = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		queryParameters.put("first_name", CandidateFirstName);
		queryParameters.put("last_name", CandidateLastName);
		queryParameters.put("email", CandidateEmail);
		queryParameters.put("linkedin", linkedinLink);
		queryParameters.put("exact_search", String.valueOf(exactSearch));
		queryParameters.put("created_from", yesterdayDateString);
		queryParameters.put("created_to", tomorrowDateString);
		queryParameters.put("updated_from", yesterdayDateString);
		queryParameters.put("updated_to", tomorrowDateString);

		// cannot include slug here as if slug is added other parameters will be
		// ignored.

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(CandidateEmail, jp.get("data.email[0]"), "email");

		String jpDate = jp.get("data.created_on[0]");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
		Assert.assertTrue(actualDate.before(expectedDate2), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);

		String jpDate2 = jp.get("data.updated_on[0]");
		Date actualDate2 = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate2);
		Assert.assertTrue(actualDate2.after(expectedDate), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate2);
		Assert.assertTrue(actualDate2.before(expectedDate2), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByContactNumber() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("contact_number", CandidateNumber);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(CandidateNumber, jp.get("data.contact_number[0]"), "contact_number");

		//Bug Automation- RCRM 7803
		String generatedString = RandomStringUtils.randomAlphabetic(10);
		queryParameters.replace("contact_number",generatedString);
		Response response2 = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		jp=response2.jsonPath();
		Assert.assertEquals(response2.getStatusCode(), 422);
		Assert.assertEquals(jp.get("contact_number[0]"),"The value should be a number","Search is not working as expected, giving result with alphabetic contact number");

	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByCountry() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("country", country);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath jp = response.jsonPath();

		Assert.assertEquals(country, jp.get("data.country[0]"), "country");

	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateBySortByAsc() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "updatedon");
		queryParameters.put("sort_order", "desc");
		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateBySortByDesc() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "createdon");
		queryParameters.put("sort_order", "asc");
		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 200);

	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByState() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("state", state);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(state, jp.get("data.state[0]"), "state");

	}

	@Owner("Gaurav Prajapati")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByCreatedFrom() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("created_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		int numberOfCandidates = jp.getInt("data.size()");
		Assert.assertTrue(numberOfCandidates > 0, "No Items Fetched Related to Current Query");
		boolean foundCandidateSlug = false;
		int candidateIndex = 0;
		for (int i = 0; i < numberOfCandidates; i++) {
			if (candidateSlug.equals(jp.getString("data[" + i + "].slug"))) {
				foundCandidateSlug = true;
				candidateIndex = i;
				break;
			}
		}
		if (!foundCandidateSlug)
			Assert.fail("Query Response Does Not Contain Required Candidate");

		Assert.assertEquals(CandidateFirstName, jp.get("data[" + candidateIndex + "].first_name"),
				"First Name is not matching, expected : " + CandidateFirstName + " but found : "
						+ jp.get("data.first_name"));
		Assert.assertEquals(CandidateLastName, jp.get("data[" + candidateIndex + "].last_name"),
				"Last Name is not matching, expected : " + CandidateLastName + " but found : "
						+ jp.get("data.last_name"));
		String jpDate = jp.get("data[" + candidateIndex + "].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Yash Rampal")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByCreatedTo() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("created_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		int numberOfCandidates = jp.getInt("data.size()");
		Assert.assertTrue(numberOfCandidates > 0, "No Items Fetched Related to Current Query");
		boolean foundCandidateSlug = false;
		int candidateIndex = 0;
		for (int i = 0; i < numberOfCandidates; i++) {
			if (candidateSlug.equals(jp.getString("data[" + i + "].slug"))) {
				foundCandidateSlug = true;
				candidateIndex = i;
				break;
			}
		}
		if (!foundCandidateSlug)
			Assert.fail("Query Response Does Not Contain Required Candidate");

		Assert.assertEquals(CandidateFirstName, jp.get("data[" + candidateIndex + "].first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("data[" + candidateIndex + "].last_name"), "last_name");
		String jpDate = jp.get("data[" + candidateIndex + "].created_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Raj Pandey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByUpdatedFrom() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String yesterdayDateString = DateUtil.getYesterdayDateString("dd-MM-yyyy");
		queryParameters.put("updated_from", yesterdayDateString);
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(yesterdayDateString);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		int numberOfCandidates = jp.getInt("data.size()");
		Assert.assertTrue(numberOfCandidates > 0, "No Items Fetched Related to Current Query");
		boolean foundCandidateSlug = false;
		int candidateIndex = 0;
		for (int i = 0; i < numberOfCandidates; i++) {
			if (candidateSlug.equals(jp.getString("data[" + i + "].slug"))) {
				foundCandidateSlug = true;
				candidateIndex = i;
				break;
			}
		}
		if (!foundCandidateSlug)
			Assert.fail("Query Response Does Not Contain Required Candidate");

		Assert.assertEquals(CandidateFirstName, jp.get("data[" + candidateIndex + "].first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("data[" + candidateIndex + "].last_name"), "last_name");
		String jpDate = jp.get("data[" + candidateIndex + "].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.after(expectedDate), "Actual date is not after the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Sampurn Chouksey")
	@Test(dependsOnMethods = "createNewCandidateWithMandatoryFields", groups = "nightly-build")
	public void searchCandidateByUpdatedTo() throws ParseException {
		Map<String, String> queryParameters = new HashMap<String, String>();
		String tomorrowDateString = DateUtil.getTomorrowDateString("dd-MM-yyyy");
		queryParameters.put("updated_to", DateUtil.getTomorrowDateString("dd-MM-yyyy"));
		Date expectedDate = new SimpleDateFormat("dd-MM-yyyy").parse(tomorrowDateString);

		Response response = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		int numberOfCandidates = jp.getInt("data.size()");
		Assert.assertTrue(numberOfCandidates > 0, "No Items Fetched Related to Current Query");
		boolean foundCandidateSlug = false;
		int candidateIndex = 0;
		for (int i = 0; i < numberOfCandidates; i++) {
			if (candidateSlug.equals(jp.getString("data[" + i + "].slug"))) {
				foundCandidateSlug = true;
				candidateIndex = i;
				break;
			}
		}
		if (!foundCandidateSlug)
			Assert.fail("Query Response Does Not Contain Required Candidate");

		Assert.assertEquals(CandidateFirstName, jp.get("data[" + candidateIndex + "].first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("data[" + candidateIndex + "].last_name"), "last_name");
		String jpDate = jp.get("data[" + candidateIndex + "].updated_on");
		Date actualDate = new SimpleDateFormat("yyyy-MM-dd").parse(jpDate);
		Assert.assertTrue(actualDate.before(expectedDate), "Actual date is not before the expected date, expected : "
				+ expectedDate + " but found : " + actualDate);
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getSearchCandidateByCustomFields_200() {
		String entityType = "candidate";
		String fieldTypeValues = "text";
		int fieldId = 1;
		createCustomFields(albatrossURL, "candidates", "custom field", fieldTypeValues, null, fieldId);

		String fieldName = fakerCandidate.getCustomFieldName(fieldTypeValues);
		String fieldValue = fakerCandidate.getCustomFieldValue(fieldTypeValues);
		List<CustomField> customFieldsList = new ArrayList<>();

		CustomField field1 = new CustomField(fieldId, fieldValue, entityType, fieldName, fieldTypeValues);
		customFieldsList.add(field1);

		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber);
		candidate.setCustom_fields(customFieldsList);
		Response response = RestClient.doPost("JSON", baseURL, "candidates", ThreadManager.getAccountApiKey(), null,
				true, candidate);
		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(CandidateFirstName, jsonPath.get("first_name"), "first_name not same");

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("first_name", CandidateFirstName);
		queryParameters.put("custom_fields[0][field_id]", String.valueOf(1));
		queryParameters.put("custom_fields[0][filter_type]", "equals");
		queryParameters.put("custom_fields[0][filter_value]", fieldValue);
		Response response2 = RestClient.doGet("JSON", baseURL, "candidates/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response2.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jsonPath2 = response2.jsonPath();
		Assert.assertEquals(fieldValue, jsonPath2.get("data[0].custom_fields[0].value"), fieldValue + "not found");
		Assert.assertEquals(fieldTypeValues, jsonPath2.get("data[0].custom_fields[0].field_type"),
				fieldTypeValues + "not found");
		Assert.assertEquals(CandidateFirstName, jsonPath2.get("data[0].first_name"), "first_name");

	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void searchCandidateByOwnerParameters_Test() {
		// create candidate using public api
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String candidateSlug = jsonCandidate.getString("slug");
		String candidateName = jsonCandidate.getString("first_name");

		// get owner data from users end point
		Response userResponse = function.getUsers(baseURL, apiAuthToken);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		int id = user.get("[0].id");
		String ownerId = String.valueOf(id);
		String ownerName = user.get("[0].first_name");
		String ownerEmail = user.get("[0].email");

		// search candidate by owner parameters
		String[] ownerParams = { "owner_id", "owner_name", "owner_email" };
		String[] ownerValues = { ownerId, ownerName, ownerEmail };

		for (int i = 0; i < ownerParams.length; i++) {
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put(ownerParams[i], ownerValues[i]);

			Response response = RestClient.doGet("JSON", baseURL, "candidates/search", apiAuthToken, queryParameters,
					null, true);

			Assert.assertEquals(response.getStatusCode(), 200);
			JsonPath jsonPath = response.jsonPath();

			Assert.assertEquals(jsonPath.getInt("data[0].owner"), Integer.parseInt(ownerId),
					"Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].first_name"), candidateName, "Failed at " + ownerParams[i]);
			Assert.assertEquals(jsonPath.get("data[0].slug"), candidateSlug, "Failed at " + ownerParams[i]);
		}
	}

	public void createCustomFields(String albatross_url, String entityName, String customFieldName,
			String customFieldType, String defaultOptions, int fielId) {

		ExtraField extraField = new ExtraField();
		extraField.setColumnid(fielId);
		extraField.setEntitytypeid(5);
		extraField.setExtrafieldname(customFieldName);
		extraField.setDefaultvalue(defaultOptions);
		extraField.setExtrafieldtype(customFieldType);
		CustomFieldAlbatross customFieldAlbatross = new CustomFieldAlbatross();
		customFieldAlbatross.setCustumField(extraField);

		Response response1 = RestClient.doPost("JSON", albatross_url, "custom-fields",
				ThreadManager.getOwnerAlbatrossToken(), null, false, customFieldAlbatross);
		Assert.assertEquals(response1.getStatusCode(), 200);
	}


	private void validateCommunicationFields(Response response, String dataPath,
											String expectedCallCreatedOn, int expectedOwner,
											String expectedEmailSentOn, String expectedMeetingCreatedOn,
											String expectedLastCommunication) {

		String pathPrefix = dataPath.isEmpty() ? "$" : dataPath;
		List<String> allFields = getAllCommunicationFields();
		for (String field : allFields) {
			response.then().body(pathPrefix, Matchers.hasKey(field));
		}
		response.then()
				.body(pathPrefix + ".last_calllog_added_on", expectedCallCreatedOn == null ? Matchers.nullValue() : Matchers.is(expectedCallCreatedOn))
				.body(pathPrefix + ".last_calllog_added_by", expectedCallCreatedOn == null ? Matchers.nullValue() : Matchers.is(expectedOwner))
				.body(pathPrefix + ".last_email_sent_on", expectedEmailSentOn == null ? Matchers.nullValue() : Matchers.containsString(expectedEmailSentOn))
				.body(pathPrefix + ".last_email_sent_by", expectedEmailSentOn == null ? Matchers.nullValue() : Matchers.is(expectedOwner))
				.body(pathPrefix + ".last_sms_sent_on", Matchers.nullValue())
				.body(pathPrefix + ".last_sms_sent_by", Matchers.nullValue())
				.body(pathPrefix + ".last_meeting_created_on", expectedMeetingCreatedOn == null ? Matchers.nullValue() : Matchers.is(expectedMeetingCreatedOn))
				.body(pathPrefix + ".last_meeting_created_by", expectedMeetingCreatedOn == null ? Matchers.nullValue() : Matchers.is(expectedOwner))
				.body(pathPrefix + ".last_linkedin_message_sent_on", Matchers.nullValue())
				.body(pathPrefix + ".last_linkedin_message_sent_by", Matchers.nullValue())
				.body(pathPrefix + ".last_communication", Matchers.containsString(expectedLastCommunication));
	}

	private List<String> getAllCommunicationFields() {
		return Arrays.asList(
				"last_calllog_added_on",
				"last_calllog_added_by",
				"last_email_sent_on",
				"last_email_sent_by",
				"last_sms_sent_on",
				"last_sms_sent_by",
				"last_meeting_created_on",
				"last_meeting_created_by",
				"last_linkedin_message_sent_on",
				"last_linkedin_message_sent_by",
				"last_communication"
		);
	}

	@DataProvider
	public Object[][] getExactSearchData() {
		Object data[][] = { { 0 }, { 1 } };
		return data;
	}

}
