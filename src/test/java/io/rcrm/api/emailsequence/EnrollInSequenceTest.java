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
public class EnrollInSequenceTest extends TestBase {

	public EnrollInSequenceTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	int candidateSeqId;
	int contactSeqId;
	int userId;
	String candidateEntitySlug;
	String albatrossToken;

	@BeforeClass
	public void setUp() {
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
		userId = ThreadManager.getOwner().getUserId();
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "enrollSequenceDetails", groups = "nightly-build")
	public void enrollInSequenceWithMandatoryFields_POST(String entity, int EnrolledBy, int sequenceId,
			String prospectSlug) {

		enrollInSequence enrollInSequence = new enrollInSequence();

		enrollInSequence.setSequence_id(sequenceId);
		enrollInSequence.setEnrolled_by(EnrolledBy);

		enrollInSequence.setProspect_slug(prospectSlug);

		String basePath = entity + "/" + prospectSlug + "/enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, enrollInSequence);


		response.then().statusCode(200);
		response.then().body("id", notNullValue());
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void enrollInSequenceWithInvalidEnrolledByFields_POST() {

		enrollInSequence enrollInSequence = new enrollInSequence();
		enrollInSequence.setEnrolled_by(1234);
		String basePath = "candidates" + "/" + candidateEntitySlug + "/enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true,
				enrollInSequence);

		// Verify Response using Assertion and Jsonpath
		Assert.assertEquals(response.getStatusCode(), 404);
		response.then().body("errorMessage", Matchers.containsString("enrolled By Id is not valid"));
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void enrollInSequenceWithInvalidSequenceIdFields_POST() {
		enrollInSequence enrollInSequence = new enrollInSequence();
		enrollInSequence.setSequence_id(1234);
		enrollInSequence.setEnrolled_by(userId);
		String basePath = "candidates" + "/" + candidateEntitySlug + "/enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true,
				enrollInSequence);

		// Verify Response using Assertion and Jsonpath
		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("errorMessage", Matchers.containsString("Invalid Sequence ID"));
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void enrollInSequenceWithInvalidProspectSlugFields_POST() {
		enrollInSequence enrollInSequence = new enrollInSequence();
		enrollInSequence.setSequence_id(candidateSeqId);
		enrollInSequence.setEnrolled_by(userId);
		String basePath = "candidates" + "/" + candidateEntitySlug + "/enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true,
				enrollInSequence);

		// Verify Response using Assertion and Jsonpath
		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("errorMessage", Matchers.containsString("Candidate is already enrolled"));
		String basePath1 = "candidates" + "/" + "1234" + "/enroll";

		Response response1 = RestClient.doPost("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, true,
				enrollInSequence);

		// Verify Response using Assertion and Jsonpath
		Assert.assertEquals(response1.getStatusCode(), 404);
		response1.then().body("errorMessage", Matchers.containsString("Candidate doesn't exist"));
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "enrollSequenceDetails", groups = "nightly-build")
	public void enrollInSequenceWithAllFields_POST(String entity, int EnrolledBy, int sequenceId, String prospectSlug) {

		enrollInSequence enrollInSequence = new enrollInSequence();

		enrollInSequence.setSequence_id(sequenceId);
		enrollInSequence.setEnrolled_by(EnrolledBy);

		enrollInSequence.setProspect_slug(prospectSlug);

		String basePath = entity + "/" + prospectSlug + "/enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true,
				enrollInSequence);


		response.then().statusCode(200);
		response.then().body("id", notNullValue());
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void unAuthorizedUserCannotAccessEnrollInSequence() {
		enrollInSequence enrollInSequence = new enrollInSequence();

		enrollInSequence.setSequence_id(candidateSeqId);
		// enrollInSequence.setEnrolled_by(EnrolledBy);

		enrollInSequence.setProspect_slug(candidateEntitySlug);

		String basePath = "candidates" + "/" + candidateEntitySlug + "/enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"123", null, true,
				enrollInSequence);


		response.then().statusCode(401);
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@DataProvider
	public Object[][] enrollSequenceDetails() {
		candidateEntitySlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
				.get("slug");

//		String contactSlug = function
//				.createNewContact_POST(baseURL, authTokenMapPublicAPI, function
//						.createNewCompanyWithMandatoryFields(baseURL, authTokenMapPublicAPI).jsonPath().get("slug"))
//				.jsonPath().get("slug");

		enrollEmailSequence("candidates", candidateEntitySlug);
//		enrollEmailSequence("contacts", contactSlug);

		return new Object[][] { { "candidates", userId, candidateSeqId, candidateEntitySlug }
//		,{ "contacts", userId, contactSeqId, contactSlug } 
		};
	}

	private void enrollEmailSequence(String entity, String prospectSlug) {
		CreateEmailSequencePage createEmailSequence = new CreateEmailSequencePage();
		SequenceSettingPage sequenceSetting = new SequenceSettingPage();
		sequenceSetting.setThread_emails_as_replies(1);
		sequenceSetting.setExecute_step_on_business_days(1);
		JSONObject settings = new JSONObject(sequenceSetting);

		createEmailSequence.setEntity_type(entity.equals("candidates") ? 5 : 2);
		createEmailSequence.setSeq_title(entity + " add sequence test " + RandomStringUtils.randomAlphabetic(4));
		createEmailSequence.setSeq_settings(settings.toString());
		createEmailSequence.setSilent_progress(false);
		createEmailSequence.setSave_steps(0);

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", albatrossToken, null, true,
				createEmailSequence);
		JsonPath jp = response.jsonPath();
		int seqId = jp.get("data.id");
		response.then().statusCode(200);
		if (entity.equals("candidates")) {
			candidateSeqId = seqId;
		} else {
			contactSeqId = seqId;
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
		Response responseAddEmailStep = RestClient.doPost1("JSON", nymaURL, basePath, albatrossToken, null,
				pathParameters, true, addEmailStep);

		responseAddEmailStep.then().statusCode(200);
	}

}
