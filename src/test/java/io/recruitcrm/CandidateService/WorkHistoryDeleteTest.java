package io.recruitcrm.CandidateService;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.candidateService.WorkHistoryRequest;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.reaper.ThreadManager;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")

@Test
public class WorkHistoryDeleteTest extends TestBase {
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	String workCompanyName = fakerCandidate.getWorkCompanyName();
	String title = fakerCandidate.getJobTitle();
	int employmentType = fakerCandidate.getEmploymentType();
	int industryId = fakerCandidate.getIndustryId();
	String workLocation = fakerCandidate.getWorkLocation();
	Boolean isCurrentlyWorking = fakerCandidate.getRandomToggleState();
	int workStartDate = fakerCandidate.getStartDate();
	int workEndDate = fakerCandidate.getEndDateWithReferenceDate(workStartDate);
	String workDescription = fakerCandidate.getDescription();
	int salary = fakerCandidate.getSalary();
	boolean isManuallyAdded = fakerCandidate.getRandomToggleState();
	WorkHistoryRequest workHistoryRequest;
	AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();

	@Owner("Yash Rampal")
	public void deleteWorkHistoryVerify_200() {
		HashMap<String, String> requiredData = addWorkHistory();
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("candidateId", requiredData.get("candidateId"));
		String basePath = "candidates/{candidateId}/work-history";

		Map<String, Object> workHistorypayload = new HashMap<String, Object>();
		workHistorypayload.put("idsToDelete", Collections.singletonList(requiredData.get("workHistoryId")));
		workHistorypayload.put("candidateSlug", requiredData.get("candidateSlug"));

		Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true, workHistorypayload);
		// Logging and verifying the response
		Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200");
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/WorkHistoryDelete.json"));
	}

	@Owner("Raj Pandey")
	public void deleteWorkHistoryVerify_401() {
		HashMap<String, String> requiredData = addWorkHistory();
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("candidateId", requiredData.get("candidateId"));
		String basePath = "candidates/{candidateId}/work-history";

		Map<String, Object> workHistorypayload = new HashMap<String, Object>();
		workHistorypayload.put("idsToDelete", Collections.singletonList(requiredData.get("workHistoryId")));
		workHistorypayload.put("candidateSlug", requiredData.get("candidateSlug"));

		Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath,
				ThreadManager.getOwnerAlbatrossToken() + fakerCandidate.getDescription(), null, pathParameters, true,
				workHistorypayload);
		// Logging and verifying the response
		Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401");
		Assert.assertEquals(response.jsonPath().get("data"), "Internal Server Error");
	}

	@Owner("Sampurn Chouksey")
	public void deleteWorkHistoryVerify_404() {
		HashMap<String, String> requiredData = addWorkHistory();
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("candidateId", requiredData.get("candidateId"));
		String basePath = "candidates/{candidateId}/work-history" + fakerCandidate.getDescription();

		Map<String, Object> workHistorypayload = new HashMap<String, Object>();
		workHistorypayload.put("idsToDelete", Collections.singletonList(requiredData.get("workHistoryId")));
		workHistorypayload.put("candidateSlug", requiredData.get("candidateSlug"));

		Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true, workHistorypayload);
		// Logging and verifying the response
		Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404");
		Assert.assertEquals(response.jsonPath().get("error"), "Not Found");
	}

	@Owner("Gaurav Prajapati")
	public void deleteWorkHistoryVerify_400() {
		HashMap<String, String> requiredData = addWorkHistory();
		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("candidateId", requiredData.get("candidateId"));
		String basePath = "candidates/{candidateId}/work-history";

		Map<String, Object> workHistorypayload = new HashMap<String, Object>();
		workHistorypayload.put("idsToDelete", Collections.singletonList(requiredData.get("workHistoryId")));
		workHistorypayload.put("candidateSlug", requiredData.get("candidateSlug") + fakerCandidate.getDescription());

		Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true, workHistorypayload);
		// Logging and verifying the response
		Assert.assertEquals(response.jsonPath().get("errors[0].errorType.context"), "Validation Error");
		Assert.assertEquals(response.jsonPath().get("meta.responseType.context"), "Error while processing request");
	}

	public HashMap<String, String> addWorkHistory() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		JsonPath candidateJsonPath = albatrossFunctions1
				.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		String candidateId = candidateJsonPath.getString("data.candidate.id");
		String candidateSlug = candidateJsonPath.getString("data.candidate.slug");
		workHistoryRequest = new WorkHistoryRequest(title, workCompanyName, employmentType, industryId,
		workLocation, salary, isCurrentlyWorking, workStartDate, workEndDate, workDescription,
		isManuallyAdded, candidateSlug);
		String basePath = "candidates/{candidateId}/work-history";
		pathParamters.put("candidateId", candidateJsonPath.getString("data.candidate.id"));
		Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				null, pathParamters, true,
				workHistoryRequest);
		JsonPath postJsonPath = response1.jsonPath();
		String workHistoryId = postJsonPath.getString("data.id");

		HashMap<String, String> candidateDetails = new HashMap<>();
		candidateDetails.put("candidateSlug", candidateSlug);
		candidateDetails.put("workHistoryId", workHistoryId); // Stored as String to parse later
		candidateDetails.put("candidateId", candidateId);

		return candidateDetails;
	}
}