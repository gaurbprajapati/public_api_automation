package io.recruitcrm.albatross.neptune;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.restassured.path.json.JsonPath;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.neptune.JavaFakerSummary;
import io.rcrm.api.pojo.neptune.CallTranscript;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GenerateCallTranscriptTest extends TestBase {
	JavaFakerSummary javaFakerSummary = new JavaFakerSummary();
	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

	private String tokenA;
	private String publicAPIKeyA;
	private String publicAPIKeyB;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		tokenA =  getTokenForAccount("AccountA", "valid");
		publicAPIKeyA = getAccountApiKey("AccountA");

		publicAPIKeyB = getAccountApiKey("AccountB");

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void generateCallTranscriptTest() {

		int callLogId = getCallLogData(publicAPIKeyA, tokenA);
		String basePath = "generate-call-transcript";

		CallTranscript callTranscript = new CallTranscript();
		callTranscript.setCall_log_id(callLogId);

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true,
				callTranscript);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("data", Matchers.notNullValue());
		response.then().body("data.call_log_id", Matchers.is(callLogId));
		response.then().body("meta.message", Matchers.is("Generate Transcription Queued"));
		response.then().body("meta.message_type", Matchers.is("is-success"));
		response.then().assertThat().body(JsonSchemaValidator
				.matchesJsonSchemaInClasspath("schemaValidation//rcrm//callTranscript.json"));

	}

	@Owner("Harika")
	@Test
	public void generateCallTranscriptWithoutRecordingTest() {

		int callLogId = function.createNewCallLog(baseURL, publicAPIKeyA, "candidate").jsonPath().get("id");
		String basePath = "generate-call-transcript";

		CallTranscript callTranscript = new CallTranscript();
		callTranscript.setCall_log_id(callLogId);

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true,
				callTranscript);

		Assert.assertEquals(response.getStatusCode(), 400);
		response.then().body("meta.message", Matchers.is("Call log has no recording"));
		response.then().body("meta.message_type", Matchers.is("is-fail"));

	}

	@Owner("Harika")
	@Test(groups = "nightly-build")
	public void crossAccountGenerateCallTranscriptTest() {

		int callLogId = function.createNewCallLog(baseURL, publicAPIKeyB, "candidate").jsonPath().get("id");
		String basePath = "generate-call-transcript";

		CallTranscript callTranscript = new CallTranscript();
		callTranscript.setCall_log_id(callLogId);

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true,
				callTranscript);

		Assert.assertEquals(response.getStatusCode(), 404);
		response.then().body("meta.message", Matchers.is("Call log not found"));
		response.then().body("meta.message_type", Matchers.is("is-fail"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void generateCallTranscriptWithInvalidId_Test() {

		String basePath = "generate-call-transcript";

		CallTranscript callTranscript = new CallTranscript();
		callTranscript.setCall_log_id(javaFakerSummary.getContactId());

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true,
				callTranscript);

		Assert.assertEquals(response.getStatusCode(), 404);
		response.then().body("meta.message", Matchers.is("Call log not found"));
		response.then().body("meta.message_type", Matchers.is("is-fail"));
		response.then().assertThat().body(JsonSchemaValidator
				.matchesJsonSchemaInClasspath("schemaValidation//rcrm//callTranscript.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void generateCallTranscriptWithNullId_Test() {
		String basePath = "generate-call-transcript";

		CallTranscript callTranscript = new CallTranscript();

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true,
				callTranscript);

		Assert.assertEquals(response.getStatusCode(), 400);
		response.then().body("errors.errors.call_log_id.errorMsg", Matchers.is("call_log_id must be an integer"));
		response.then().body("errors.errorMessage", Matchers.is("Validation error"));
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGenerateCallTranscript_Test() {
		String basePath = "generate-call-transcript";

		CallTranscript callTranscript = new CallTranscript();

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA+"123", null, true,
				callTranscript);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("data", Matchers.nullValue());
		response.then().body("detail", Matchers.is("Unauthorized"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//unauthorizedGPTAccess.json"));
	}

	public int getCallLogData(String publicAuth, String privateAuth){
		String recordingData = "";

		JsonPath callLog = function.createNewCallLog(baseURL, publicAuth, "candidate").jsonPath();
		int callLogId = callLog.get("id");

		function.uploadCallLogRecording(baseURL, publicAuth, 0, callLogId);

		JsonPath callLogRecording;
		int retries = 5; // Set your retry limit
		int waitTimeMs = 20000; // Wait time between retries in milliseconds

		for (int i = 0; i < retries; i++) {
			callLogRecording = allCrudFunctions.getCallLogs(albatrossURL, privateAuth).jsonPath();
			recordingData = callLogRecording.get("data.records[0].recording");

			if (recordingData != null) {
				break; // Exit the loop if we got a non-null recording
			}

			try {
				Thread.sleep(waitTimeMs); // Wait before retrying
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Restore interrupt status
				throw new RuntimeException("Thread was interrupted while waiting to retry", e);
			}
		}

		if (recordingData == null) {
			throw new RuntimeException("Failed to retrieve non-null recording after " + retries + " retries.");
		}

		return callLogId;
	}

}
