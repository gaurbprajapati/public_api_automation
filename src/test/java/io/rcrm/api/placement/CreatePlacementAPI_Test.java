package io.rcrm.api.placement;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.pojo.HiringStage;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CreatePlacementAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	String albatrossTknA;
	String albatrossTknB;
	commanFunction function;
	AllCrudFunctions allCrudFunctions;
	JavaFakerPlacement placementFaker;
	String basePath = "candidates/{candidate}/hiring-stages/{job}";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
		function = new commanFunction();
		allCrudFunctions = new AllCrudFunctions();
		placementFaker = new JavaFakerPlacement();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateAndJobSlug", groups = "nightly-build")
	public void createPlacementWithValidToken_PublicAPI(String getCandidateSlug, String getJobSlug) {

		HiringStage hiringStage = new HiringStage();
		hiringStage.setStatus_id(1);
		hiringStage.setStage_date("");
		hiringStage.setCreate_placement(true);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", getCandidateSlug);
		pathParamters.put("job", getJobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParamters, true, hiringStage);

		response.then().statusCode(200);
		response.then().assertThat().body("status.status_id", Matchers.is(1));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//updateCandidateHiringStage.json"));

		Response placementsResponse = null;
		for (int tryCount = 0; tryCount < 5; tryCount++) {
			waitBetweenTheEveryScript(10000);
			placementsResponse = RestClient.doGet("JSON", baseURL, "placements", apiKeyA, null, null, true);
			if (placementsResponse.statusCode() == 200) {
				break;
			}
		}
		placementsResponse.then().statusCode(200);
		placementsResponse.then().assertThat().body("data[0].candidate_slug", Matchers.is(getCandidateSlug));
		placementsResponse.then().assertThat().body("data[0].job_slug", Matchers.is(getJobSlug));
		placementsResponse.then().assertThat().body("data.size()", Matchers.equalTo(1));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createPlacementWithInvalidToken_PublicAPI() {

		HiringStage hiringStage = new HiringStage();
		hiringStage.setStatus_id(1);
		hiringStage.setStage_date("");
		hiringStage.setCreate_placement(true);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", placementFaker.getInvalidToken());
		pathParamters.put("job", placementFaker.getInvalidToken());

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA + "123", null, pathParamters, true, hiringStage);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCandidateAndJobSlug", groups = "nightly-build")
	public void createPlacementWithCrossAccountToken_PublicAPI(String getCandidateSlug, String getJobSlug) {

		HiringStage hiringStage = new HiringStage();
		hiringStage.setStatus_id(1);
		hiringStage.setStage_date("");
		hiringStage.setCreate_placement(true);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", getCandidateSlug);
		pathParamters.put("job", getJobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyB, null, pathParamters, true, hiringStage);

		response.then().statusCode(200);
		response.then().assertThat().body("errorMessage", Matchers.is("Candidate doesn't exist"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//candidate//candidateNotExists.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] getCandidateAndJobSlug() {
		Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
		String companySlug = companyResponse.jsonPath().getString("slug");
		Response contactResponse = function.createNewContact_POST(baseURL, apiKeyA, companySlug);
		String contactSlug = contactResponse.jsonPath().getString("slug");
		Response jobResponse = function.createNewJob(baseURL, apiKeyA, companySlug, contactSlug);
		String getJobSlug = jobResponse.jsonPath().getString("slug");
		Response candidateResponse = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA);
		String getCandidateSlug = candidateResponse.jsonPath().getString("slug");
		function.assignCandidateByJobSlugAndCandidateSlug(baseURL, apiKeyA, getJobSlug, getCandidateSlug);
		return new Object[][] { { getCandidateSlug, getJobSlug } };
	}
}