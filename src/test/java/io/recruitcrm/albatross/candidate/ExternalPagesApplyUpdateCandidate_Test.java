package io.recruitcrm.albatross.candidate;

import com.qa.api.util.reaper.ThreadManager;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import org.testng.annotations.DataProvider;
import io.rcrm.api.pojo.Candidate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ExternalPagesApplyUpdateCandidate_Test extends TestBase {
	
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	commanFunction function = new commanFunction();
	String accountApiKey;
	
	String encryptedAccountId;
	String candidateFirstName;
	String candidateLastName;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountApiKey = ThreadManager.getAccountApiKey();
		int accountId = ThreadManager.getAccount().getAccountId();
		encryptedAccountId = function.encryptAccountId(String.valueOf(accountId));
		candidateFirstName = fakerCandidate.getFirstName();
		candidateLastName = fakerCandidate.getLastName();
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "createCandidateData", groups = "nightly-build")
	public void externalPagesApplyUpdateCandidateWithValidData_Test(String candidateEmail, String candidateSlug) {
		JSONObject candidateData = new JSONObject();
		candidateData.put("firstname", candidateFirstName);
		candidateData.put("lastname", candidateLastName);
		candidateData.put("emailid", candidateEmail);
		candidateData.put("slug", candidateSlug);
		candidateData.put("resumefilename", "");
		candidateData.put("workexpmonth", "");
		candidateData.put("resume", "");
		candidateData.put("accountid", encryptedAccountId);

		JSONObject requestBody = new JSONObject();
		requestBody.put("candidate", candidateData);
		requestBody.put("accountid", encryptedAccountId);

		String basePath = "external-pages/apply-update-candidate";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, null, null, null, true, requestBody);
		
		JsonPath jp = response.jsonPath();
		assertThat("Response status code should be 200", response.getStatusCode(), is(equalTo(200)));
		assertThat("Response status should be success", jp.get("status"), is(equalTo("success")));
		assertThat("Response message should contain Add Candidate From Talent Pool Successful", jp.get("message"), is(equalTo("Add Candidate From Talent Pool Successful")));
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "createCandidateData", groups = "nightly-build")
	public void externalPagesApplyUpdateCandidateWithEmptyAccountId_Test(String candidateEmail, String candidateSlug) {
		JSONObject candidateData = new JSONObject();
		candidateData.put("firstname", candidateFirstName + "_invalid");
		candidateData.put("lastname", candidateLastName + "_invalid");
		candidateData.put("emailid", candidateEmail);
		candidateData.put("slug", candidateSlug);
		candidateData.put("resumefilename", "");
		candidateData.put("workexpmonth", "");
		candidateData.put("resume",  "");
		candidateData.put("accountid", "");

		JSONObject requestBody = new JSONObject();
		requestBody.put("candidate", candidateData);
		requestBody.put("accountid", "");

		String basePath = "external-pages/apply-update-candidate";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, null, null, null, true, requestBody);

        JsonPath jp = response.jsonPath();
		assertThat("Response status code must be 200 but failed to add candidate", response.getStatusCode(), is(equalTo(200)));
        assertThat("Failed to add candidate", jp.get("message"), is(containsString("Failed To Add Candidate From Talent Pool")));
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "createCandidateData", groups = "nightly-build")
	public void externalPagesApplyUpdateCandidateWithMissingAccountId_Test(String candidateEmail, String candidateSlug) {
		JSONObject candidateData = new JSONObject();
		candidateData.put("firstname", candidateFirstName + "_missing");
		candidateData.put("lastname", candidateLastName + "_missing");
		candidateData.put("emailid", candidateEmail);
		candidateData.put("slug", candidateSlug);
		candidateData.put("resumefilename", "");
		candidateData.put("workexpmonth", "");
		candidateData.put("resume", "");

		JSONObject requestBody = new JSONObject();
		requestBody.put("candidate", candidateData);

		String basePath = "external-pages/apply-update-candidate";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, null, null, null, true, requestBody);

		assertThat("Response status code must not be 200", response.getStatusCode(), is(not(equalTo(200))));
	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void externalPagesApplyUpdateCandidateWithInvalidEmail_Test() {
		JSONObject candidateData = new JSONObject();
		candidateData.put("firstname", candidateFirstName + "_invalid_email");
		candidateData.put("lastname", candidateLastName + "_invalid_email");
		candidateData.put("emailid", "invalid_email_format");
		candidateData.put("resumefilename", "");
		candidateData.put("workexpmonth", "");
		candidateData.put("resume", "");
		candidateData.put("accountid", encryptedAccountId);

		JSONObject requestBody = new JSONObject();
		requestBody.put("candidate", candidateData);
		requestBody.put("accountid", encryptedAccountId);

		String basePath = "external-pages/apply-update-candidate";
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, null, null, null, true, requestBody);

		assertThat("Response status code must not be 200", response.getStatusCode(), is(not(equalTo(200))));
	}

	@DataProvider
	public Object[][] createCandidateData() {
		Candidate candidate = new Candidate();
		String candidateEmail = fakerCandidate.getEmailID();
		candidate.setFirst_name(fakerCandidate.getFirstName());
		candidate.setLast_name(fakerCandidate.getLastName());
		candidate.setEmail(candidateEmail);
		candidate.setContact_number(fakerCandidate.getContactNumber());

		Response response = RestClient.doPost("JSON", baseURL, "candidates", accountApiKey, null, true, candidate);
		assertThat("Response status code should be 200", response.getStatusCode(), is(equalTo(200)));

		String candidateSlug = response.jsonPath().getString("slug");
		return new Object[][] { {candidateEmail, candidateSlug} };
	}
}
