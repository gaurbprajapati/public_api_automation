package io.recruitcrm.albatross.offlimit;

import org.testng.annotations.*;

import com.qa.api.util.DateUtil;

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
import io.rcrm.api.pojo.offlimit.MarkCandidateOffLimit;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.javafaker.JavaFakerCompany;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetOffLimitStatus_Test extends TestBase {

	private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	private Object apiAuthToken;
	commanFunction function = new commanFunction();
	AllCrudFunctions crudFunction = new AllCrudFunctions();
	JavaFakerCompany faker = new JavaFakerCompany();
	String basePath = "off-limit/get-status/5/{id}";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = getAccountApiKey("AccountA");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateOffLimitDetails", groups = "nightly-build")
	public void getOffLimitStatus_Test(int candidateId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().get("data.id"), is(notNullValue()));
		assertThat(response.jsonPath().get("data.account_id"), is(getAccountId("AccountA")));
		assertThat(response.jsonPath().get("data.entity_id"), is(candidateId));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/offlimit/getOffLimitStatusSchema.json"));
	}
	
	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateDetails", groups = "nightly-build")
	public void getOffLimitStatusEmpty_Test(int candidateId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().get("data"), nullValue());
	}
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getOffLimitStatusWithInvalidId_Test() {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", faker.getRandomId());

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));

		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().get("data"), nullValue());
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getOffLimitStatusUnauthorized_Test() {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", faker.getRandomId());

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, pathParams, true);

		assertThat(response.getStatusCode(), is(401));
		assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
	}
	
	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateOffLimitDetails", groups = "nightly-build")
	public void getOffLimitStatusWithAdminToken_Test(int candidateId) {
		String adminToken = getRoleBasedToken("AccountA", "Admin");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, null, pathParams, true);
		
		assertThat(response.getStatusCode(), is(200));

		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().get("data.id"), is(notNullValue()));
		assertThat(response.jsonPath().get("data.account_id"), is(getAccountId("AccountA")));
		assertThat(response.jsonPath().get("data.entity_id"), is(candidateId));
	}
	
	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateOffLimitDetails", groups = "nightly-build")
	public void getOffLimitStatusWithTeamMemberToken_Test(int candidateId) {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));

		// This is a bug, team member should not be able to see off limit status of owner record
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().get("data.id"), is(notNullValue()));
		assertThat(response.jsonPath().get("data.account_id"), is(getAccountId("AccountA")));
		assertThat(response.jsonPath().get("data.entity_id"), is(candidateId));
	}
	
	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateOffLimitDetails", groups = "nightly-build")
	public void getOffLimitStatusWithRestrictedTeamMemberToken_Test(int candidateId) {
		String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));

		// This is a bug, restricted team member should not be able to see off limit status of owner record
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().get("data.id"), is(notNullValue()));
		assertThat(response.jsonPath().get("data.account_id"), is(getAccountId("AccountA")));
		assertThat(response.jsonPath().get("data.entity_id"), is(candidateId));
	}
	
	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateOffLimitDetails", groups = "nightly-build")
	public void getOffLimitStatusForCrossAccount_Test(int candidateId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(candidateId));

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, null, pathParams, true);

		assertThat(response.getStatusCode(), is(200));
		
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().get("data"), nullValue());
	}


	@DataProvider
	public Object[][] getCandidateOffLimitDetails() {
		JsonPath candidateJsonPath = crudFunction.createCandidate(albatrossURL, albatrossTknA).jsonPath();
		int candidateId = candidateJsonPath.getInt("data.candidate.id");
		String candidateSlug = candidateJsonPath.getString("data.candidate.slug");

		Response offlimitStatusResponse = RestClient.doGet("JSON", baseURL, "off-limit-status", apiAuthToken, null, null, false);
		assertThat(offlimitStatusResponse.getStatusCode(), is(200));
		JsonPath offlimitJsonPath = offlimitStatusResponse.jsonPath();
		int offlimitStatusId = offlimitJsonPath.get("[0].id");

		MarkCandidateOffLimit markOffLimit = new MarkCandidateOffLimit();
		markOffLimit.setCandidate_slugs(candidateSlug);
		markOffLimit.setStatus_id(String.valueOf(offlimitStatusId));
		markOffLimit.setEnd_date(DateUtil.getTomorrowDateString());
		markOffLimit.setReason(faker.getRandomReason());

		Response response = RestClient.doPost1("JSON", baseURL, "candidates/mark-off-limit", apiAuthToken, null, null, false, markOffLimit);
		
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("remark"), is("Records Were Updated"));

		return new Object[][] { { candidateId } };
	}

	@DataProvider
	public Object[][] getCandidateDetails() {
		JsonPath candidateJsonPath = crudFunction.createCandidate(albatrossURL, albatrossTknA).jsonPath();

		int candidateId = candidateJsonPath.getInt("data.candidate.id");

		return new Object[][] { { candidateId } };
	}

}