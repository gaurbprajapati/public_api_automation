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
public class UnenrollInSequenceTest extends TestBase {

	public UnenrollInSequenceTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	int candidateSeqId;
	String candidateEntitySlug, contactSlug, prospectSlug;
	int contactSeqId;
	int enrollmentStatus;
	int userId;
	String albatrossToken;

	@BeforeClass
	public void setUp() {
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
		userId = ThreadManager.getOwner().getUserId();
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getEnrollmentDetails", groups = "nightly-build")
	public void unenrollInSequenceWithMandatoryFields_POST(String entity,String prospectSlug,int sequenceId) {
		enrollInSequence enrollInSequence = new enrollInSequence();

		enrollInSequence.setSequence_id(sequenceId);
		// enrollInSequence.setEnrolled_by(EnrolledBy);

		enrollInSequence.setProspect_slug(prospectSlug);

		String basePath1 = entity + "/" + prospectSlug + "/enroll";

		Response response1 = RestClient.doPost("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, true,
				enrollInSequence);


		response1.then().statusCode(200);
		unenrollSequence unenrollInSequence = new unenrollSequence();

		//unenrollInSequence.setUnenrolled_by(userId);
		unenrollInSequence.setProspect_slug(prospectSlug);

		String basePath = entity + "/" + prospectSlug + "/un-enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true,
				unenrollInSequence);


		response.then().statusCode(200);
		response.then().body("id", notNullValue());
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getEnrollmentDetails", groups = "nightly-build")
	public void unenrollInSequenceWithInvaliUnenrolledByFields_POST(String entity,String prospectSlug,int sequenceId) {
		enrollInSequence enrollInSequence = new enrollInSequence();

		enrollInSequence.setSequence_id(sequenceId);
		// enrollInSequence.setEnrolled_by(EnrolledBy);

		enrollInSequence.setProspect_slug(prospectSlug);

		String basePath1 = entity + "/" + prospectSlug + "/enroll";

		Response response1 = RestClient.doPost("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, true,
				enrollInSequence);


		response1.then().statusCode(200);
		unenrollSequence unenrollInSequence = new unenrollSequence();
		unenrollInSequence.setUnenrolled_by(1234);
		unenrollInSequence.setProspect_slug(prospectSlug);

		String basePath = entity + "/" + prospectSlug + "/un-enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true,
				unenrollInSequence);
		Assert.assertEquals(response.getStatusCode(), 404);
		response.then().body("errorMessage", Matchers.containsString("Unenrolled By Id is not valid"));

	}
	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void unenrollInSequenceWithInvalidProspectSlugFields_POST() {
		unenrollSequence unenrollInSequence = new unenrollSequence();
		unenrollInSequence.setUnenrolled_by(userId);

		String basePath1 = "candidates" + "/" + "1234" + "/un-enroll";

		Response response1 = RestClient.doPost("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, true,
				unenrollInSequence);

		// Verify Response using Assertion and Jsonpath
		Assert.assertEquals(response1.getStatusCode(), 404);
		response1.then().body("errorMessage", Matchers.containsString("Candidate doesn't exist"));
	}
	
	@Owner("Priyanka Shinde")
	@Test(dataProvider = "getEnrollmentDetails", groups = "nightly-build")
	public void unenrollInSequenceWithAllFields_POST(String entity, String prospectSlug,int sequenceId) {
		enrollInSequence enrollInSequence = new enrollInSequence();

		enrollInSequence.setSequence_id(sequenceId);
	    enrollInSequence.setEnrolled_by(userId);

		enrollInSequence.setProspect_slug(prospectSlug);

		String basePath1 = entity + "/" + prospectSlug + "/enroll";

		Response response1 = RestClient.doPost("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, true,
				enrollInSequence);


		response1.then().statusCode(200);
		unenrollSequence unenrollInSequence = new unenrollSequence();

		unenrollInSequence.setUnenrolled_by(userId);
		unenrollInSequence.setProspect_slug(prospectSlug);

		String basePath = entity + "/" + prospectSlug + "/un-enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true,
				unenrollInSequence);


		response.then().statusCode(200);
		response.then().body("id", notNullValue());
	}

	@Owner("Priyanka Shinde")
	@Test(groups = "nightly-build")
	public void unAuthorizedUserCannotAccessUnenrollInSequence() {
		unenrollSequence unenrollInSequence = new unenrollSequence();

		unenrollInSequence.setUnenrolled_by(userId);
		unenrollInSequence.setProspect_slug(candidateEntitySlug);

		String basePath ="candidates" + "/" + prospectSlug + "/un-enroll";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"123", null, true,
				unenrollInSequence);


		response.then().statusCode(401);
		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@DataProvider
	public Object[][] getEnrollmentDetails() {
		emailSequence("candidates");
//		emailSequence("contacts");

		return new Object[][] { { "candidates", candidateEntitySlug,candidateSeqId }
//		,{ "contacts", contactSlug,contactSeqId } 
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
		createEmailStepToSequence.setTemplate_title(entity + " Email Template " + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence.setTemplate_subject("Creating email Template for " + entity + RandomStringUtils.randomAlphabetic(4));
		createEmailStepToSequence.setTemplate_content(entity + " Template body " + RandomStringUtils.randomAlphabetic(4));
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
