package io.rcrm.api.emailsequence;

import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.enrollInSequence;
import io.rcrm.api.pojo.unenrollSequence;
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
public class AllEndpointsOfEmailSequencesTest extends TestBase {
	public AllEndpointsOfEmailSequencesTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	String candidateEntitySlug = null;
	int userId, candidateSequenceId;
	String albatrossToken;

	@BeforeClass
	public void setUp() {
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
		userId = ThreadManager.getOwner().getUserId();
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "enrollSequenceDetails", groups = "nightly-build")
	public void enrollInSequence_POST(int EnrolledBy, int sequenceId, String prospectSlug) {
		enrollInSequence enrollInSequences = new enrollInSequence();
		enrollInSequences.setEnrolled_by(EnrolledBy);
		enrollInSequences.setSequence_id(sequenceId);
		enrollInSequences.setProspect_slug(prospectSlug);
		String basePath = "candidates/" + prospectSlug + "/enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true,
				enrollInSequences);


		response.then().statusCode(200);
		response.then().body("id", notNullValue());
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void getAllEnrollmentStatuses_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "enrollment-statuses", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		response.then().statusCode(200);

		response.then().body("stage_id", Matchers.notNullValue());
		response.then().body("label", Matchers.notNullValue());

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchEnrollmentByProspectSlug() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("prospect_slug", candidateEntitySlug);
		queryParameters.put("prospect_type", "candidate");

		Response response = RestClient.doGet("JSON", baseURL, "enrollments/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		Assert.assertEquals(candidateEntitySlug, jp.get("data.prospect_slug[0]"), "ProspectSlug");

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void searchSequenceBySequenceId() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		String candidateSeqId = String.valueOf(candidateSequenceId);

		queryParameters.put("sequence_id", candidateSeqId);

		Response response = RestClient.doGet("JSON", baseURL, "email-sequences/search", ThreadManager.getAccountApiKey(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response using Assertion and JsonPath
		JsonPath jp = response.jsonPath();
		Assert.assertTrue(jp.get("data.id[0]").equals(candidateSequenceId));

	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void unenrollInSequence_POST() {

		unenrollSequence unenrollInSequence = new unenrollSequence();

		unenrollInSequence.setUnenrolled_by(userId);

		unenrollInSequence.setProspect_slug(candidateEntitySlug);

		String basePath = "candidates/" + candidateEntitySlug + "/un-enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true,
				unenrollInSequence);


		response.then().statusCode(200);
		response.then().body("id", notNullValue());
	}

	@DataProvider
	public Object[][] enrollSequenceDetails() {
		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();
		JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		candidateEntitySlug = jsonCandidate.get("slug");

		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);
		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setEntity_type(5);
		createEmailSequence.setSeq_title("Candidate" + " add sequence test " + generatedString);
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", albatrossToken, null, true,
				createEmailSequence);
		response.then().statusCode(200);
		JsonPath jp = response.jsonPath();
		candidateSequenceId = jp.get("data.id");

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("id", String.valueOf(candidateSequenceId));

		CreateEmailStepToSequencePage createEmailStepToSequence = new CreateEmailStepToSequencePage();
		createEmailStepToSequence.setStep_no(1);
		createEmailStepToSequence.setNo_of_days(2);
		createEmailStepToSequence
				.setTemplate_title("Candidate Email Template " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence
				.setTemplate_subject("Creating email Template for Candidate " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence
				.setTemplate_content("Candidate Template body " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence.setTime(3600);
		createEmailStepToSequence.setType(1);
		createEmailStepToSequence.setUpdate_type("all");
		createEmailStepToSequence.setInclude_opt_out_link(1);

		ArrayList<Object> emailStep = new ArrayList<>();
		emailStep.add(createEmailStepToSequence);
		AddEmailStepsToSequencePage addEmailStep = new AddEmailStepsToSequencePage();
		addEmailStep.setSteps(emailStep);

		String basePath = "email-sequences/{id}/steps";
		Response responseAddEmailStep = RestClient.doPost1("JSON", nymaURL, basePath, albatrossToken, null,
				pathParameters, true, addEmailStep);

		responseAddEmailStep.then().statusCode(200);
		response.then().statusCode(200);

		Object data[][] = { { userId, candidateSequenceId, candidateEntitySlug } };
		return data;
	}

}
