package io.rcrm.api.emailsequence;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.DateUtil;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.enrollInSequence;
import io.rcrm.api.pojo.nyma.AddEmailStepsToSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailStepToSequencePage;
import io.rcrm.api.pojo.nyma.SequenceSettingPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class SearchEnrollmentByFieldsTest extends TestBase {

	commanFunction function = new commanFunction();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	int candidateSeqId;
	String candidateEntitySlug, contactSlug, prospectSlug;
	int contactSeqId;
	int enrollmentStatus;
	int userId;
	String seqName;


	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getEnrollmentDetails", groups = "nightly-build")
	public void searchEnrollmentById(String entity, String prospectSlug, int sequenceId) {
		enrollInSequence enrollInSequence = new enrollInSequence();

		enrollInSequence.setSequence_id(sequenceId);
		// enrollInSequence.setEnrolled_by(EnrolledBy);

		enrollInSequence.setProspect_slug(prospectSlug);

		String basePath1 = entity + "/" + prospectSlug + "/enroll";

		Response response1 = RestClient.doPost("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, true,
				enrollInSequence);


		response1.then().statusCode(200);
		JsonPath jp2 = response1.jsonPath();
		int EnrollmentId = jp2.get("id");
		String enrollmentId = String.valueOf(EnrollmentId);

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("enrollment_id", enrollmentId);

		Response response = RestClient.doGet("JSON", baseURL, "enrollments/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		Integer id = jp.get("data.id[0]");
		Assert.assertTrue(id.equals(EnrollmentId));
		enrollmentStatus = jp.get("data.status.status_id[0]");

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchEnrollmentBySequenceId() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("sequence_id", String.valueOf(candidateSeqId));

		Response response = RestClient.doGet("JSON", baseURL, "enrollments/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		Integer id = jp.get("data.sequence_id[0]");
		Assert.assertTrue(id.equals(candidateSeqId));
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchEnrollmentByProspectSlug() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("prospect_slug", candidateEntitySlug);

		queryParameters.put("prospect_type", "candidate");

		Response response1 = RestClient.doGet("JSON", baseURL, "enrollments/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response1.getStatusCode(), 200);
		JsonPath jp1 = response1.jsonPath();

		Assert.assertEquals(candidateEntitySlug, jp1.get("data.prospect_slug[0]"), "ProspectSlug");
		Map<String, String> queryParameters1 = new HashMap<>();
		queryParameters1.put("prospect_slug", candidateEntitySlug);

		Response response = RestClient.doGet("JSON", baseURL, "enrollments/search", ThreadManager.getAccountApiKey(),
				queryParameters1, null, true);

		// Verify Response using Assertion and Jsonpath
		Assert.assertEquals(response.getStatusCode(), 422);
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getSequenceTypeData", groups = "nightly-build")
	public void searchEnrollmentByProspectType(String entity, int statusCode) {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("prospect_type", entity);
		Response response = RestClient.doGet("JSON", baseURL, "enrollments/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchEnrollmentsByEnrollmentStatus() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("enrollment_status", String.valueOf(enrollmentStatus));

		Response response = RestClient.doGet("JSON", baseURL, "enrollments/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		Integer enrollmentsStatus = jp.get("data.status.status_id[0]");
		Assert.assertTrue(enrollmentsStatus.equals(enrollmentStatus));
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchEnrollmentsByAllFields() throws ParseException {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("expand", "*");
		queryParameters.put("page", "1");
		queryParameters.put("limit", "1");

		Response response = RestClient.doGet("JSON", baseURL, "enrollments/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchEnrollmentBySortByAsc() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sort_by", "updated_on");
		queryParameters.put("sort_order", "desc");
		Response response = RestClient.doGet("JSON", baseURL, "enrollments/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchEnrollmentBySortByDesc() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("sort_by", "enrolled_on");
		queryParameters.put("sort_order", "asc");
		Response response = RestClient.doGet("JSON", baseURL, "enrollments/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
	}

	@DataProvider
	public Object[][] getEnrollmentDetails() {
		emailSequence("candidates");
//		emailSequence("contacts");

		return new Object[][] { { "candidates", candidateEntitySlug, candidateSeqId }
//		,{ "contacts", contactSlug, contactSeqId }
		};

	}

	private void emailSequence(String entity) {

		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();
		candidateEntitySlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
				.get("slug");
//		contactSlug = function
//				.createNewContact_POST(baseURL, authTokenMapPublicAPI, function
//						.createNewCompanyWithMandatoryFields(baseURL, authTokenMapPublicAPI).jsonPath().get("slug"))
//				.jsonPath().get("slug");
		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);
		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setEntity_type(entity.equals("candidates") ? 5 : 2);
		createEmailSequence.setSeq_title(entity + " add sequence test " + generatedString);
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequence);
		JsonPath jp = response.jsonPath();
		int seqId = jp.get("data.id");
		response.then().statusCode(200);
		if (entity.equals("candidates")) {
			candidateSeqId = seqId;
			prospectSlug = candidateEntitySlug;
		} else {
			contactSeqId = seqId;
			prospectSlug = contactSlug;
		}

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(seqId));

		CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
		createEmailStepToSequence.setStep_no(1);
		createEmailStepToSequence.setNo_of_days(2);
		createEmailStepToSequence
				.setTemplate_title(entity + " Email Template " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence
				.setTemplate_subject("Creating email Template for " + entity + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence
				.setTemplate_content(entity + " Template body " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence.setTime(3600);
		createEmailStepToSequence.setType(1);
		createEmailStepToSequence.setUpdate_type("all");
		createEmailStepToSequence.setInclude_opt_out_link(1);

		ArrayList<Object> emailStep = new ArrayList<>();
		emailStep.add(createEmailStepToSequence);
		AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
		addEmailStep.setSteps(emailStep);

		String basePath = "email-sequences/{id}/steps";
		Response responseAddEmailStep = RestClient.doPost1("JSON", nymaURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				pathParameters, true, addEmailStep);

		responseAddEmailStep.then().statusCode(200);
		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getUsers = albatrossFunctions.getUsers(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		JsonPath jp1 = getUsers.jsonPath();
		userId = jp1.get("data.records[0].id");

	}

	@DataProvider
	public Object[][] getSequenceTypeData() {
		Object data[][] = { { "candidate", 200 }, { "contact", 200 }
		};
		return data;
	}

}
