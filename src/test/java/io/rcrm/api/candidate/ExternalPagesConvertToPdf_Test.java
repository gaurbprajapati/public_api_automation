package io.rcrm.api.candidate;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ExternalPagesConvertToPdf_Test extends TestBase {
	
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	commanFunction function = new commanFunction();
	
	String summaryHtmlContent = "<head>" + fakerCandidate.getCandidateSummary();
	String action = "download";
	String fileName = fakerCandidate.getFileName();
	String encryptedAccountId;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		int accountId = ThreadManager.getAccount().getAccountId();
		encryptedAccountId = function.encryptAccountId(String.valueOf(accountId));
	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void externalPagesConvertToPdfWithMandatoryParameters_Test() {
		JSONObject requestBody = new JSONObject();
		requestBody.put("summary_content_html", summaryHtmlContent);
		requestBody.put("action", action);
		requestBody.put("accountid", encryptedAccountId);
		String basePath = "external-pages/convert-to-pdf";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, null, null, null, true, requestBody);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.containsString("successfully"));
		response.then().body("status", Matchers.is("success"));
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//downloadCandidateSummary.json"));
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void externalPagesConvertToPdfWithOptionalParameters_Test() {
		JSONObject requestBody = new JSONObject();
		requestBody.put("summary_content_html", summaryHtmlContent);
		requestBody.put("action", action);
		requestBody.put("file_title", fileName);
		requestBody.put("accountid", encryptedAccountId);
		String basePath = "external-pages/convert-to-pdf";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, null, null, null, true, requestBody);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.containsString("successfully"));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//downloadCandidateSummary.json"));
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void externalPagesConvertToPdfWithNullValues_Test() {
		JSONObject requestBody = new JSONObject();
		requestBody.put("accountid", encryptedAccountId);
		String basePath = "external-pages/convert-to-pdf";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, null, null, null, true, requestBody);
		Assert.assertEquals(response.getStatusCode(), 400);
		response.then().body("errorMessage", Matchers.is("Validation error occur"));
		response.then().body("errorCode", Matchers.is(400));
		response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//downloadCandidateSummaryWithNullValues.json"));
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void externalPagesConvertToPdfWithInvalidAccountId_Test() {
		JSONObject requestBody = new JSONObject();
		requestBody.put("summary_content_html", summaryHtmlContent);
		requestBody.put("action", action);
		requestBody.put("file_title", fileName);
		requestBody.put("accountid", "invalid_encrypted_account_id");
		String basePath = "external-pages/convert-to-pdf";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, null, null, null, true, requestBody);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.is("Invalid Account ID"));
		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("status", Matchers.is("fail"));
	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void externalPagesConvertToPdfWithMissingAccountId_Test() {
		JSONObject requestBody = new JSONObject();
		requestBody.put("summary_content_html", summaryHtmlContent);
		requestBody.put("action", action);
		requestBody.put("file_title", fileName);
		String basePath = "external-pages/convert-to-pdf";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, null, null, null, true, requestBody);
		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.is("User not found"));
		response.then().body("message_type", Matchers.is("is-fail"));
		response.then().body("status", Matchers.is("fail"));
	}
}
