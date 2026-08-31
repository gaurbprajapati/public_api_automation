package io.recruitcrm.albatross.neptune;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.neptune.JavaFakerSummary;
import io.rcrm.api.pojo.neptune.EmailTemplate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GenerateEmailTemplateTest extends TestBase {
	JavaFakerSummary javaFakerSummary = new JavaFakerSummary();
	commanFunction function = new commanFunction();
	String key = "manual_prompt";
	private int candidateId;

	@BeforeClass
	public void setUp() {
		candidateId = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath().getInt("id");
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void generateEmailTemplateWithMandatoryFields_Test() {

		EmailTemplate emailTemplate = new EmailTemplate();
		emailTemplate.setKey(key);
		emailTemplate.setPrompt(javaFakerSummary.getPromptText());
		emailTemplate.setRelated_to(candidateId);

		String basePath = "generate-email-template";

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				emailTemplate);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.generated_text", Matchers.notNullValue());
		response.then().body("meta.message", Matchers.is("Email template generated successfully"));
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void generateEmailTemplateWithOptionalFields_Test() {

		EmailTemplate emailTemplate = new EmailTemplate();
		emailTemplate.setKey(key);
		emailTemplate.setPrompt(javaFakerSummary.getPromptText());
		emailTemplate.setLast_response(javaFakerSummary.getNoteText());
		emailTemplate.setRelated_to(candidateId);

		String basePath = "generate-email-template";

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				emailTemplate);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("data.generated_text", Matchers.notNullValue());

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void generateEmailTemplateWithInvalidKey_Test() {

		EmailTemplate emailTemplate = new EmailTemplate();
		emailTemplate.setKey(javaFakerSummary.getRandomKey());
		emailTemplate.setPrompt(javaFakerSummary.getPromptText());
		emailTemplate.setLast_response(javaFakerSummary.getNoteText());

		String basePath = "generate-email-template";

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				emailTemplate);
		Assert.assertEquals(response.getStatusCode(), 400);

		response.then().body("data", Matchers.nullValue());
		response.then().body("errors.errors.key.errorMsg", Matchers.is("key is invalid"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValuesValidation.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void generateEmailTemplateWithNullValues_Test() {

		EmailTemplate emailTemplate = new EmailTemplate();
		emailTemplate.setKey(null);
		emailTemplate.setPrompt(null);

		String basePath = "generate-email-template";

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				emailTemplate);
		Assert.assertEquals(response.getStatusCode(), 400);

		response.then().body("data", Matchers.nullValue());
		response.then().body("errors.errors.key.errorMsg", Matchers.is("Input should be a valid string"));
		response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValuesValidation.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void generateEmailTemplateWithNullFields_Test() {

		String basePath = "generate-email-template";

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, null);
		Assert.assertEquals(response.getStatusCode(), 400);

		response.then().body("data", Matchers.nullValue());
		response.then().body("errors.errors.body.errorMsg", Matchers.is("Field required"));
		response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullFieldsValidation.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGenerateEmailTemplate_Test() {

		EmailTemplate emailTemplate = new EmailTemplate();

		String basePath = "generate-email-template";

		Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
				emailTemplate);
		Assert.assertEquals(response.getStatusCode(), 401);

		response.then().body("detail", Matchers.is("Unauthorized"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//unauthorizedGPTAccess.json"));

	}

}
