package io.rcrm.api.subscriptions.receiveWebhook;

import com.qa.api.util.WebhookHelper;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.RetryOn500OrSkippedAnalyzer;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.enrollInSequence;
import io.rcrm.api.pojo.unenrollSequence;
import io.rcrm.api.pojo.nyma.AddEmailStepsToSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailSequencePage;
import io.rcrm.api.pojo.nyma.CreateEmailStepToSequencePage;
import io.rcrm.api.pojo.nyma.SequenceSettingPage;
import io.rcrm.api.pojo.Subscription;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class GetEntityEnrollUnenrollTest extends TestBase {

	WebhookHelper webhookHelper;
	JsonPath responseFromWebhook;
	String entitySlug;
	String basePath;

	commanFunction function = new commanFunction();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	int candidateSeqId;
	int contactSeqId;
	int userId;
	String candidateEntitySlug;
	enrollInSequence enrollInSequence = new enrollInSequence();
	unenrollSequence unenrollInSequence = new unenrollSequence();

	@BeforeClass
	public void setUp() throws IOException {
		webhookHelper = new WebhookHelper();
		userId = ThreadManager.getOwner().getUserId();
	}

	@Owner("Priyanka Shinde")
	@Test(dataProvider = "enrollEvents", retryAnalyzer = RetryOn500OrSkippedAnalyzer.class)
	public void getEntityEnrollUnenroll(String event) {
		webhookHelper.clearRequests();
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());

		Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
		RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

		switch (event) {
		case "candidate.enrolled":
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
					.jsonPath().get("slug");
			entitySlug = candidateSlug;

			enrollEmailSequence("candidates", candidateSlug);

			enrollInSequence.setSequence_id(candidateSeqId);
			enrollInSequence.setEnrolled_by(userId);
			enrollInSequence.setProspect_slug(entitySlug);

			basePath = "candidates/" + entitySlug + "/enroll";

			RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, enrollInSequence);
			break;

		case "candidate.unenrolled":
			unenrollInSequence.setUnenrolled_by(userId);
			unenrollInSequence.setProspect_slug(entitySlug);

			basePath = "candidates/" + entitySlug + "/un-enroll";

			RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, unenrollInSequence);
			break;

		case "contact.enrolled":
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");
			String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath()
					.get("slug");

			entitySlug = contactSlug;

			enrollEmailSequence("contacts", contactSlug);

			enrollInSequence.setSequence_id(contactSeqId);
			enrollInSequence.setEnrolled_by(userId);
			enrollInSequence.setProspect_slug(entitySlug);

			basePath = "contacts/" + entitySlug + "/enroll";

			RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, enrollInSequence);
			break;

		case "contact.unenrolled":
			unenrollInSequence.setUnenrolled_by(userId);
			unenrollInSequence.setProspect_slug(entitySlug);

			basePath = "contacts/" + entitySlug + "/un-enroll";

			RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, true, unenrollInSequence);
			break;

		default:
			break;
		}
		try {
			responseFromWebhook = webhookHelper.getJsonData();
		} catch (Exception e) {
			Assert.fail("Failed to fetch Webhook data for Event "+event+", "+e.getMessage());
		}
		Assert.assertNotNull(responseFromWebhook.get("id"));
		Assert.assertNotNull(responseFromWebhook.get("prospect_slug"));
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		webhookHelper.clear();
	}

	@DataProvider(name = "enrollEvents")
	public Object[][] dpMethod() {
		return new Object[][] { { "candidate.enrolled" }, { "candidate.unenrolled" }, { "contact.enrolled" },
				{ "contact.unenrolled" } };
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

		Response response = RestClient.doPost("JSON", nymaURL, "email-sequences", ThreadManager.getOwnerAlbatrossToken(), null, true,
				createEmailSequence);
		JsonPath jp = response.jsonPath();
		int seqId = jp.get("data.id");
		response.then().statusCode(200);
		if (entity.equals("candidates")) {
			candidateSeqId = seqId;
		} else {
			contactSeqId = seqId;
		}

		Map<String, String> pathParameters = new HashMap<>();
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

	}
}