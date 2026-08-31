package io.recruitcrm.CandidateService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.candidateService.PitchCandidateToContactWithoutEmail;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class PitchCandidateToContactWithoutEmailTest extends TestBase {
	commanFunction commanfunction = new commanFunction();
	AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();

	@Owner("Gaurav Prajapati")
	@Test
	public void pitchCandidateToContactWithoutEmail_200() {

		String basePath = "candidates/pitch-candidate";
		JsonPath candidateJsonPath = albatrossFunctions1
				.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		int candID = candidateJsonPath.get("data.candidate.id");
		String companySlug = commanfunction
				.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
		int companyId = albatrossFunctions1
				.getCompanyResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), companySlug).jsonPath()
				.get("data.company.id");
		String slug = commanfunction.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().get("slug");
		int contactId = Integer.parseInt(
				albatrossFunctions1.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), slug)
						.jsonPath().get("data.contact.id"));

		PitchCandidateToContactWithoutEmail pitchCandidateToContactWithoutEmail = new PitchCandidateToContactWithoutEmail();
		List<Integer> candidateIds = new ArrayList<>();
		candidateIds.add(candID);
		pitchCandidateToContactWithoutEmail.setCandidateIds(candidateIds);

		List<Integer> contactIds = new ArrayList<>();
		contactIds.add(contactId);
		pitchCandidateToContactWithoutEmail.setContactIds(contactIds);

		Map<String, Boolean> queryParameters = new HashMap<String, Boolean>();
		queryParameters.put("sendEmail", false);

		Response response = RestClient.doPostWithBooleanQueryParams("JSON", candidatesURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), queryParameters, true, pitchCandidateToContactWithoutEmail);
		Assert.assertEquals(response.getStatusCode(), 200);
		String messageInMeta = response.jsonPath().getString("meta.message");
		String successStatus = response.jsonPath().getString("meta.status");
		String responseContext = response.jsonPath().getString("meta.responseType.context");
		if (!successStatus.equals("true") && !messageInMeta.equals("Candidate pitched successfully.")
				&& !responseContext.equals("Request is successful")) {
			Assert.fail("Updating Pitch stage failed");
		}
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi//candidate//PitchCandidateToContactWithoutEmail.json"));

	}

	// TODO: This test is not working as expected. It should be fixed.
	@Owner("Yash Rampal")
	@Test
	public void pitchCandidateToContactWithoutEmail_400_InvalidQueryParameter() {
		// Testing for Bad Request by Sending wrong Query Parameter
		String basePath = "candidates/pitch-candidate";
		JsonPath candidateJsonPath = albatrossFunctions1
				.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		int candID = candidateJsonPath.get("data.candidate.id");
		String companySlug = commanfunction
				.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
		int companyId = albatrossFunctions1
				.getCompanyResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), companySlug).jsonPath()
				.get("data.company.id");
		String slug = commanfunction.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().get("slug");
		int contactId = Integer.parseInt(
				albatrossFunctions1.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), slug)
						.jsonPath().get("data.contact.id"));

		PitchCandidateToContactWithoutEmail pitchCandidateToContactWithoutEmail = new PitchCandidateToContactWithoutEmail();
		List<Integer> candidateIds = new ArrayList<>();
		candidateIds.add(candID);
		pitchCandidateToContactWithoutEmail.setCandidateIds(candidateIds);

		List<Integer> contactIds = new ArrayList<>();
		contactIds.add(contactId);
		pitchCandidateToContactWithoutEmail.setContactIds(contactIds);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sendEmail", "InvalidValueTOTest400BadRequest");

		Response response = RestClient.doPost("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				queryParameters, true, pitchCandidateToContactWithoutEmail);
		Assert.assertEquals(response.getStatusCode(), 400);
		String errorMessage = response.jsonPath().getString("error");
		String successStatus = response.jsonPath().getString("status");
		String responsePath = response.jsonPath().getString("path");
		if (!successStatus.equals(400) && !errorMessage.equals("Bad Request")
				&& !responsePath.equals("/v2/candidates/pitch-candidate")) {
			Assert.fail("Testing for 400 Bad Request Failed");
		}
	}

	// TODO: This test is not working as expected. It should be fixed.
	@Owner("Raj Pandey")
	@Test
	public void pitchCandidateToContactWithoutEmail_400_InvalidIds() {
		// Testing for Bad Request by Sending wrong Query Parameter
		String basePath = "candidates/pitch-candidate";
		JsonPath candidateJsonPath = albatrossFunctions1
				.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		int candID = candidateJsonPath.get("data.candidate.id");
		String companySlug = commanfunction
				.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
		int companyId = albatrossFunctions1
				.getCompanyResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), companySlug).jsonPath()
				.get("data.company.id");
		String slug = commanfunction.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().get("slug");
		int contactId = Integer.parseInt(
				albatrossFunctions1.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), slug)
						.jsonPath().get("data.contact.id"));

		PitchCandidateToContactWithoutEmail pitchCandidateToContactWithoutEmail = new PitchCandidateToContactWithoutEmail();
		List<Integer> candidateIds = new ArrayList<>();
		candidateIds.add(candID);
		pitchCandidateToContactWithoutEmail.setCandidateIds(candidateIds);

		List<Integer> contactIds = new ArrayList<>();
		contactIds.add(contactId);
		pitchCandidateToContactWithoutEmail.setContactIds(contactIds);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sendEmail", "InvalidValueTOTest400BadRequest");

		Response response = RestClient.doPost("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				queryParameters, true, pitchCandidateToContactWithoutEmail);
		Assert.assertEquals(response.getStatusCode(), 400);
		String errorMessage = response.jsonPath().getString("error");
		String successStatus = response.jsonPath().getString("status");
		String responsePath = response.jsonPath().getString("path");
		if (!successStatus.equals(400) && !errorMessage.equals("Bad Request")
				&& !responsePath.equals("/v2/candidates/pitch-candidate")) {
			Assert.fail("Testing for 400 Bad Request Failed");
		}
	}

	// TODO: This test is not working as expected. It should be fixed.
	@Owner("Sampurn Chouksey")
	@Test
	public void pitchCandidateToContactWithoutEmail_404_EmptyIds() {
		// Testing for Bad Request by Sending wrong Query Parameter
		String basePath = "candidates/pitch-candidate";
		JsonPath candidateJsonPath = albatrossFunctions1
				.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		int candID = candidateJsonPath.get("data.candidate.id");
		String companySlug = commanfunction
				.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");
		int companyId = albatrossFunctions1
				.getCompanyResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), companySlug).jsonPath()
				.get("data.company.id");
		String slug = commanfunction.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().get("slug");
		int contactId = Integer.parseInt(
				albatrossFunctions1.getContactResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), slug)
						.jsonPath().get("data.contact.id"));

		PitchCandidateToContactWithoutEmail pitchCandidateToContactWithoutEmail = new PitchCandidateToContactWithoutEmail();
		List<Integer> candidateIds = new ArrayList<>();
		candidateIds.add(candID);
		pitchCandidateToContactWithoutEmail.setCandidateIds(candidateIds);

		List<Integer> contactIds = new ArrayList<>();
		contactIds.add(contactId);
		pitchCandidateToContactWithoutEmail.setContactIds(contactIds);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sendEmail", "InvalidValueTOTest400BadRequest");

		Response response = RestClient.doPost("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				queryParameters, true, pitchCandidateToContactWithoutEmail);
		Assert.assertEquals(response.getStatusCode(), 400);
		String errorMessage = response.jsonPath().getString("error");
		String successStatus = response.jsonPath().getString("status");
		String responsePath = response.jsonPath().getString("path");
		if (!successStatus.equals(400) && !errorMessage.equals("Bad Request")
				&& !responsePath.equals("/v2/candidates/pitch-candidate")) {
			Assert.fail("Testing for 400 Bad Request Failed");
		}
	}

}
