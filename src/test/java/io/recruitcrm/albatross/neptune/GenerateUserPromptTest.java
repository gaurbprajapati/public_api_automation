package io.recruitcrm.albatross.neptune;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.neptune.JavaFakerSummary;
import io.rcrm.api.pojo.neptune.Summary;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GenerateUserPromptTest extends TestBase {
	JavaFakerSummary javaFakerSummary = new JavaFakerSummary();

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void generateUserPromptForNoteSummary_Test() {

		Summary summary = new Summary(javaFakerSummary.getNoteText(), "manual_prompt",
				javaFakerSummary.getPromptText());

		String basePath = "generate-note-summary";

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, summary);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.generated_text", Matchers.notNullValue());
		response.then().body("meta.message", Matchers.is("Note generated successfully"));
	}

	@Owner("Divya")
	@Test(dependsOnMethods = "generateUserPromptForNoteSummary_Test", groups = "nightly-build")
	public void generateUserPromptForCallLogSummary_Test() {

		Summary summary = new Summary(javaFakerSummary.getNoteText(), "manual_prompt",
				javaFakerSummary.getPromptText());

		String basePath = "generate-call-log-summary";

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, summary);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.generated_text", Matchers.notNullValue());
		response.then().body("meta.message", Matchers.is("Call log generated successfully"));
	}

	@Owner("Divya")
	@Test(dependsOnMethods = "generateUserPromptForCallLogSummary_Test", groups = "nightly-build")
	public void generateUserPromptWillNullPrompt_Test() {

		Summary summary = new Summary(javaFakerSummary.getNoteText(), "manual_prompt", null);

		String basePath = "generate-note-summary";

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, summary);
		Assert.assertEquals(response.getStatusCode(), 400);

		response.then().body("data", Matchers.nullValue());
		response.then().body("errors.errors.key.errorMsg", Matchers.is("Prompt is required when key is manual_prompt"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValuesValidation.json"));
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGenerateUserPrompt_Test() {

		Summary summary = new Summary(javaFakerSummary.getNoteText(), "manual_prompt",
				javaFakerSummary.getPromptText());

		String basePath = "generate-note-summary";

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				summary);
		Assert.assertEquals(response.getStatusCode(), 401);

		response.then().body("detail", Matchers.is("Unauthorized"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//unauthorizedGPTAccess.json"));

	}

}
