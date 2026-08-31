package io.recruitcrm.CandidateService;

import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.candidateService.WorkHistoryRequest;
import io.rcrm.api.restclient.RestClient;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateWorkHistoryTest extends TestBase {

	public CreateWorkHistoryTest() {
		// TODO Auto-generated constructor stub
		super();
	}

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
	@Test

	public void addWorkExpericencVerify_200() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		JsonPath candidateJsonPath = albatrossFunctions1
				.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		String candSlug = candidateJsonPath.get("data.candidate.slug");
		workHistoryRequest = new WorkHistoryRequest();
		workHistoryRequest = new WorkHistoryRequest(title, workCompanyName, employmentType, industryId,
		workLocation, salary, isCurrentlyWorking, workStartDate, workEndDate, workDescription,
		isManuallyAdded, candSlug);

		String basePath = "candidates/{candidateId}/work-history";
		pathParamters.put("candidateId", candidateJsonPath.getString("data.candidate.id"));
		Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				null, pathParamters, true,
				workHistoryRequest);
		Assert.assertEquals(response1.getStatusCode(), 201);
		// Schema getting checked
		response1.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/CreateWorkHistory.json"));
	}

	@Owner("Raj Pandey")
	@Test

	public void addWorkExpericencVerify_401() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		JsonPath candidateJsonPath = albatrossFunctions1
				.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		String candSlug = candidateJsonPath.get("data.candidate.slug");
		workHistoryRequest = new WorkHistoryRequest();
		workHistoryRequest = new WorkHistoryRequest(title, workCompanyName, employmentType, industryId,
		workLocation, salary, isCurrentlyWorking, workStartDate, workEndDate, workDescription,
		isManuallyAdded, candSlug);

		String basePath = "candidates/{candidateId}/work-history";
		pathParamters.put("candidateId", candidateJsonPath.getString("data.candidate.id"));
		Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath,
				ThreadManager.getOwnerAlbatrossToken() + fakerCandidate.getDescription(), null, pathParamters, true,
				workHistoryRequest);
		// Assert the response status and validate JSON schema
		Assert.assertEquals(response1.getStatusCode(), 401);
		Assert.assertEquals(response1.jsonPath().get("data"), "Internal Server Error");
	}

	@Owner("Sampurn Chouksey")
	@Test

	public void addWorkExpericencVerify_404() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		JsonPath candidateJsonPath = albatrossFunctions1
				.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		String candSlug = candidateJsonPath.get("data.candidate.slug");
		workHistoryRequest = new WorkHistoryRequest();
		workHistoryRequest = new WorkHistoryRequest(title, workCompanyName, employmentType, industryId,
		workLocation, salary, isCurrentlyWorking, workStartDate, workEndDate, workDescription,
		isManuallyAdded, candSlug);

		String basePath = "candidates/{candidateId}/work-history" + fakerCandidate.getDescription();
		pathParamters.put("candidateId", candidateJsonPath.getString("data.candidate.id"));
		Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				null, pathParamters, true,
				workHistoryRequest);
		// Assert the response status and validate JSON schema
		Assert.assertEquals(response1.getStatusCode(), 404);
		Assert.assertEquals(response1.jsonPath().get("error"), "Not Found");
	}

	@Owner("Gaurav Prajapati")
	@Test

	public void addWorkExpericencVerify_400() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		JsonPath candidateJsonPath = albatrossFunctions1
				.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		String candSlug = candidateJsonPath.get("data.candidate.slug");
		workHistoryRequest = new WorkHistoryRequest();
		workHistoryRequest = new WorkHistoryRequest(title, workCompanyName, employmentType, industryId,
		workLocation, salary, isCurrentlyWorking, workStartDate, workEndDate, workDescription,
		isManuallyAdded, candSlug);

		String basePath = "candidates/{candidateId}/work-history";
		pathParamters.put("candidateId", candidateJsonPath.getString("data.candidate.id"));
		Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				null, pathParamters, true,
				workHistoryRequest);
		Assert.assertEquals(response1.getStatusCode(), 400);
		Assert.assertEquals(response1.jsonPath().get("errors[0].errorType.context"), "Validation Error");
		Assert.assertEquals(response1.jsonPath().get("meta.responseType.context"), "Error while processing request");

	}

}