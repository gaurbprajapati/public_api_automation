package io.recruitcrm.albatross.candidate;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import java.util.*;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.WorkHistory;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetCandidateWorkHistory_Test extends TestBase {

	private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	commanFunction function = new commanFunction();
	AllCrudFunctions crudFunction = new AllCrudFunctions();
	JavaFakerCandidate faker = new JavaFakerCandidate();
	String basePath = "candidates/candidate-work/{id}";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateWorkHistoryDetails", groups = "nightly-build")
	public void getCandidateWorkHistoryDetails_Test(int candidateId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candiate Work History Fetched Successfully."));
		List<Map<String, Object>> dataList = response.jsonPath().getList("data");
		assertThat(dataList, is(not(empty())));
		assertThat(dataList.get(0).get("id"), is(notNullValue()));
		assertThat(dataList.get(0).get("accountid"), is(getAccountId("AccountA")));
		assertThat(dataList.get(0).get("candidate_id"), is(candidateId));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/getCandidateWorkHistoryAlbatross.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateDetails", groups = "nightly-build")
	public void getCandidateWorkHistoryEmptyDetails_Test(int candidateId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candiate Work History Fetched Successfully."));
		assertThat(response.jsonPath().getList("data"), is(empty()));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getCandidateWorkHistoryWithInvalidId_Test() {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", faker.getRandomId());

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candidate Not Found"));
		assertThat(response.jsonPath().getList("data"), is(empty()));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getCandidateWorkHistoryUnauthorized_Test() {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", faker.getRandomId());

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(401));
		assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateWorkHistoryDetails", groups = "nightly-build")
	public void getCandidateWorkHistoryAdminToken_Test(int candidateId) {
		String adminToken = getRoleBasedToken("AccountA", "Admin");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candiate Work History Fetched Successfully."));
		List<Map<String, Object>> dataList = response.jsonPath().getList("data");
		assertThat(dataList, is(not(empty())));
		assertThat(dataList.get(0).get("id"), is(notNullValue()));
		assertThat(dataList.get(0).get("accountid"), is(getAccountId("AccountA")));
		assertThat(dataList.get(0).get("candidate_id"), is(candidateId));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateWorkHistoryDetails", groups = "nightly-build")
	public void getCandidateWorkHistoryTeamMemberToken_Test(int candidateId) {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candiate Work History Fetched Successfully."));
		List<Map<String, Object>> dataList = response.jsonPath().getList("data");
		assertThat(dataList, is(not(empty())));
		assertThat(dataList.get(0).get("id"), is(notNullValue()));
		assertThat(dataList.get(0).get("accountid"), is(getAccountId("AccountA")));
		assertThat(dataList.get(0).get("candidate_id"), is(candidateId));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateWorkHistoryDetails", groups = "nightly-build")
	public void getCandidateWorkHistoryRestrictedTeamMemberToken_Test(int candidateId) {
		String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("You don't have access to read the candidate data"));
		assertThat(response.jsonPath().getList("data"), is(empty()));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateWorkHistoryDetails", groups = "nightly-build")
	public void getCandidateWorkHistoryCrossAccount_Test(int candidateId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candidate Not Found"));
		assertThat(response.jsonPath().getList("data"), is(empty()));
	}

	@DataProvider
	public Object[][] getCandidateWorkHistoryDetails() {
		JsonPath candidateJsonPath = crudFunction.createCandidate(albatrossURL, albatrossTknA).jsonPath();
		int candidateId = candidateJsonPath.getInt("data.candidate.id");
		String candidateSlug = candidateJsonPath.getString("data.candidate.slug");

		String workCompanyName = faker.getWorkCompanyName();
		String title = faker.getJobTitle();
		int employmentType = faker.getEmploymentType();
		int industryId = faker.getIndustryId();
		String workLocation = faker.getWorkLocation();
		int isCurrentlyWorking = faker.currentlyWorking();
		int workStartDate = faker.getStartDate();
		int workEndDate = faker.getEndDateWithReferenceDate(workStartDate);
		String workDescription = faker.getCandidateSummary();
		int salary = faker.getSalary();

		WorkHistory workHistory = new WorkHistory(candidateSlug, workCompanyName, title, employmentType, industryId, workLocation, isCurrentlyWorking, workStartDate, workEndDate, workDescription, salary);
		workHistory.setCandidate_id(candidateId);

		Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/candidate-work/create", albatrossTknA, null, null, true, workHistory);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candidate Work History is created successfully."));

		return new Object[][] { { candidateId } };
	}

	@DataProvider
	public Object[][] getCandidateDetails() {
		JsonPath candidateJsonPath = crudFunction.createCandidate(albatrossURL, albatrossTknA).jsonPath();

		int candidateId = candidateJsonPath.getInt("data.candidate.id");

		return new Object[][] { { candidateId } };
	}
}