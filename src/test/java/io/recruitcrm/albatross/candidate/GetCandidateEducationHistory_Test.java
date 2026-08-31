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
import io.rcrm.api.pojo.EducationHistoryRequestInCandidateDetailPage;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetCandidateEducationHistory_Test extends TestBase {

	private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	commanFunction function = new commanFunction();
	AllCrudFunctions crudFunction = new AllCrudFunctions();
	JavaFakerCandidate faker = new JavaFakerCandidate();
	String basePath = "candidates/candidate-education/{id}";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateEducationHistoryDetails", groups = "nightly-build")
	public void getCandidateEducationHistoryDetails_Test(int candidateId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candiate Education History Fetched Successfully."));
		List<Map<String, Object>> dataList = response.jsonPath().getList("data");
		assertThat(dataList, is(not(empty())));
		assertThat(dataList.get(0).get("id"), is(notNullValue()));
		assertThat(dataList.get(0).get("accountid"), is(getAccountId("AccountA")));
		assertThat(dataList.get(0).get("candidate_id"), is(candidateId));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/getCandidateEducationHistoryAlbatross.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateDetails", groups = "nightly-build")
	public void getCandidateEducationHistoryEmptyDetails_Test(int candidateId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candiate Education History Fetched Successfully."));
		assertThat(response.jsonPath().getList("data"), is(empty()));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getCandidateEducationHistoryWithInvalidId_Test() {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", faker.getRandomId());

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candidate Not Found"));
		assertThat(response.jsonPath().getList("data"), is(empty()));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getCandidateEducationHistoryUnauthorized_Test() {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", faker.getRandomId());

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(401));
		assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateEducationHistoryDetails", groups = "nightly-build")
	public void getCandidateEducationHistoryAdminToken_Test(int candidateId) {
		String adminToken = getRoleBasedToken("AccountA", "Admin");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candiate Education History Fetched Successfully."));
		List<Map<String, Object>> dataList = response.jsonPath().getList("data");
		assertThat(dataList, is(not(empty())));
		assertThat(dataList.get(0).get("id"), is(notNullValue()));
		assertThat(dataList.get(0).get("accountid"), is(getAccountId("AccountA")));
		assertThat(dataList.get(0).get("candidate_id"), is(candidateId));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateEducationHistoryDetails", groups = "nightly-build")
	public void getCandidateEducationHistoryTeamMemberToken_Test(int candidateId) {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candiate Education History Fetched Successfully."));
		List<Map<String, Object>> dataList = response.jsonPath().getList("data");
		assertThat(dataList, is(not(empty())));
		assertThat(dataList.get(0).get("id"), is(notNullValue()));
		assertThat(dataList.get(0).get("accountid"), is(getAccountId("AccountA")));
		assertThat(dataList.get(0).get("candidate_id"), is(candidateId));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateEducationHistoryDetails", groups = "nightly-build")
	public void getCandidateEducationHistoryRestrictedTeamMemberToken_Test(int educationHistoryId) {
		String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(educationHistoryId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("You don't have access to read the candidate data"));
		assertThat(response.jsonPath().getList("data"), is(empty()));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateEducationHistoryDetails", groups = "nightly-build")
	public void getCandidateEducationHistoryCrossAccount_Test(int candidateId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candidate Not Found"));
		assertThat(response.jsonPath().getList("data"), is(empty()));
	}

	@DataProvider
	public Object[][] getCandidateEducationHistoryDetails() {
		JsonPath candidateJsonPath = crudFunction.createCandidate(albatrossURL, albatrossTknA).jsonPath();
		int candidateId = candidateJsonPath.getInt("data.candidate.id");
		String candidateSlug = candidateJsonPath.getString("data.candidate.slug");

		String instituteName = faker.getInstituteName();
		String educationalQualification = faker.getEducationalQualification();
		String educationalSpecialization = faker.getSpecialization();
		String grade = faker.getGrade();
		String educationLocation = faker.getEducationLocation();
		int educationStartDate = faker.getStartDate();
		int educationEndDate = faker.getEndDateWithReferenceDate(educationStartDate);
		String educationDescription = faker.getDescription().replaceAll("<[^>]*>", "");

		EducationHistoryRequestInCandidateDetailPage educationHistoryRequest = new EducationHistoryRequestInCandidateDetailPage(instituteName, educationalQualification, educationalSpecialization, grade, educationLocation, educationStartDate, educationEndDate, educationDescription, 0, candidateId, candidateSlug);

		Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/candidate-education/create", albatrossTknA, null, null, true, educationHistoryRequest);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Candidate Education is created successfully."));

		return new Object[][] { { candidateId } };
	}

	@DataProvider
	public Object[][] getCandidateDetails() {
		JsonPath candidateJsonPath = crudFunction.createCandidate(albatrossURL, albatrossTknA).jsonPath();

		int candidateId = candidateJsonPath.getInt("data.candidate.id");

		return new Object[][] { { candidateId } };
	}
}