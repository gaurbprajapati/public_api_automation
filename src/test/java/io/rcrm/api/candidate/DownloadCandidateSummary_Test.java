package io.rcrm.api.candidate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.albatross.CandidateSummary;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DownloadCandidateSummary_Test extends TestBase {
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerJob jobFaker = new JavaFakerJob();

	String summaryHtmlContent = "<head>" + fakerCandidate.getCandidateSummary();
	String action = "download";
	String fileName = fakerCandidate.getFileName();

	public DownloadCandidateSummary_Test() {
		super();
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void downloadCandidateSummaryWithMandatoryParameters_Test() {

		CandidateSummary candidateSummary = new CandidateSummary();
		candidateSummary.setSummary_content_html(summaryHtmlContent);
		candidateSummary.setAction(action);
		String basePath = "global/convert-to-pdf";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true,
				candidateSummary);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Html to Pdf converted successfully."));
		response.then().body("status", Matchers.is("success"));

		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//downloadCandidateSummary.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void downloadCandidateSummaryWithOptionalParameters_Test() {

		CandidateSummary candidateSummary = new CandidateSummary();
		candidateSummary.setSummary_content_html(summaryHtmlContent);
		candidateSummary.setAction(action);
		candidateSummary.setFile_title(fileName);
		String basePath = "global/convert-to-pdf";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true,
				candidateSummary);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.is("Html to Pdf converted successfully."));
		response.then().body("status", Matchers.is("success"));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//downloadCandidateSummary.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void downloadCandidateSummaryWithNullValues_Test() {

		CandidateSummary candidateSummary = new CandidateSummary();
		String basePath = "global/convert-to-pdf";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true,
				candidateSummary);

		Assert.assertEquals(response.getStatusCode(), 400);
		response.then().body("errorMessage", Matchers.is("Validation error occur"));
		response.then().body("errorCode", Matchers.is(400));
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//downloadCandidateSummaryWithNullValues.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotDownloadCandidateSummary_Test() {

		CandidateSummary candidateSummary = new CandidateSummary();
		String basePath = "global/convert-to-pdf";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, null, true,
				candidateSummary);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().assertThat().body(
				JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//unauthorizedUserAccess.json"));
	}

}
