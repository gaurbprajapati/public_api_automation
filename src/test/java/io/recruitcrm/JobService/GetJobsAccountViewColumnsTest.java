package io.recruitcrm.JobService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetJobsAccountViewColumnsTest extends TestBase {

	private static final String BASE_PATH = "account-view-columns";
	String albatrossAuthToken;

	public GetJobsAccountViewColumnsTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"job_service", "nightly-build"})
	public void getJobsAccountViewColumns_200() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "jobs");
		Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

		assert response != null;
		assertThat(response.getStatusCode(), equalTo(200));
		assertThat("Expected success message", response.jsonPath().getString("meta.message"), equalTo("Account View Columns Fetched Successfully"));
		assertThat("Expected response type code 103", response.jsonPath().getInt("meta.responseType.code"), equalTo(103));
		assertThat("Expected success context", response.jsonPath().getString("meta.responseType.context"), equalTo("Request is successful"));
		assertThat("Expected status 200 in meta", response.jsonPath().getInt("meta.status"), equalTo(200));
		assertThat("Expected timestamp not null", response.jsonPath().get("meta.timestamp"), notNullValue());
		assertThat("Expected requestUuid not null", response.jsonPath().get("meta.requestUuid"), notNullValue());

		assertThat("Data array should not be empty", response.jsonPath().getList("data").size(), greaterThan(0));
		assertThat("First data element should not be null", response.jsonPath().get("data[0]"), notNullValue());

		Map<String, Object> accountViewColumnsMap = response.jsonPath().getMap("data[0].accountViewColumns");
		assertThat("Account view columns should not be null", accountViewColumnsMap, notNullValue());
		assertThat("Account view columns should contain id", accountViewColumnsMap.containsKey("id"), is(true));

		// Expected job account-view columns from API response
		String[] requiredColumns = {
				"id", "srno", "name", "jobstatus", "jobstatuslabel", "archived",
				"companyid", "companyname", "companyslug", "contactid", "secondarycontactid",
				"contactname", "contactslug", "contactemail", "contactnumber",
				"salarytype", "currencyid", "state", "country", "collaborator",
				"jobquestions", "allowapply", "description", "job_skill",
				"address", "city", "locality", "qualificationid", "qualification", "specialization",
				"detailfilename", "jdtext", "jdtextdetails",
				"minexperienceinyears", "maxexperienceinyears", "annualsalarymin", "annualsalarymax",
				"noofopenings", "ownerid", "ownername", "createdby", "creatorname",
				"createdon", "updatedon", "updatedby", "slug", "jobpostingstatus", "note",
				"hiring_pipeline_id", "hiring_pipeline_name", "job_type", "job_category",
				"remote", "hotlist", "xml_feeds", "targetcompanies",
				"contactfirstname", "contactlastname", "postalcode", "pay_rate", "bill_rate",
				"job_function", "job_industry"
		};
		for (String column : requiredColumns) {
			assertThat("Missing expected column: " + column, accountViewColumnsMap.containsKey(column), is(true));
		}
		boolean hasCustColumn = accountViewColumnsMap.keySet().stream().anyMatch(name -> name.startsWith("custcolumn"));
		assertThat("At least one custcolumn should be present", hasCustColumn, is(true));

		// Validate id column structure
		Map<String, Object> idColumn = response.jsonPath().getMap("data[0].accountViewColumns.id");
		assertThat("Id column should not be null", idColumn, notNullValue());
		String[] idColumnRequiredFields = {
				"label", "longlabel", "entity", "field",
				"visible", "visible_locked",
				"detailPageOrder", "detailPageOrderV2", "listPageOrder", "detailPageVisible"
		};
		for (String field : idColumnRequiredFields) {
			assertThat("Id column missing required field: " + field, idColumn.containsKey(field), is(true));
		}
		assertThat("Id column entity should be jobs", idColumn.get("entity"), equalTo("jobs"));

		// Validate name column (Job Title)
		Map<String, Object> nameColumn = response.jsonPath().getMap("data[0].accountViewColumns.name");
		assertThat("Name column should not be null", nameColumn, notNullValue());
		assertThat("Name column entity should be jobs", nameColumn.get("entity"), equalTo("jobs"));
		assertThat("Name column field should be name", nameColumn.get("field"), equalTo("name"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"job_service", "nightly-build"})
	public void getJobsAccountViewColumns_401() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "jobs");

		Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, "InvalidToken", queryParameters, null, true);

		assert response != null;
		assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
	}

	@Owner("Suhel Bhadane")
	@Test(dataProvider = "unauthorizedTokenProvider", groups = {"job_service", "nightly-build"})
	public void getJobsAccountViewColumns_401_WithNonOwnerTokens(String role, String token) {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "jobs");

		Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, token, queryParameters, null, true);
		assert response != null;
		int statusCode = response.getStatusCode();
		assertThat("Expected status code 403 for " + role + " token", statusCode, equalTo(403));
		assertThat("Expected meta.status 403", response.jsonPath().getInt("meta.status"), equalTo(403));
		assertThat("Expected error context", response.jsonPath().getString("meta.responseType.context"), equalTo("Error while processing request"));
		assertThat("Expected data null", response.jsonPath().get("data"), nullValue());
		assertThat("Expected error message", response.jsonPath().getString("errors[0].message"), equalTo("Only account owner can access account view columns."));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"job_service", "nightly-build"})
	public void getJobsAccountViewColumns_404() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "jobs");

		Response response = RestClient.doGet("JSON", jobServiceURL, "ccount-view-columns", albatrossAuthToken, queryParameters, null, true);

		assert response != null;
		assertThat("Expected status code 404", response.getStatusCode(), equalTo(404));
		assertThat("Expected not found error", response.jsonPath().getString("error"), equalTo("Not Found"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"job_service", "nightly-build"})
	public void getJobsAccountViewColumns_405() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "jobs");

		Response response = RestClient.doPost("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, false, null);

		assertThat("Expected status code 405", response.getStatusCode(), equalTo(405));
		assertThat("Expected method not allowed error", response.jsonPath().getString("error"), equalTo("Method Not Allowed"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"job_service", "nightly-build"})
	public void getJobsAccountViewColumns_400() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "");

		Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

		assert response != null;
		assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
		assertThat("Expected error context", response.jsonPath().getString("meta.responseType.context"), equalTo("Error while processing request"));
	}

	@DataProvider(name = "unauthorizedTokenProvider")
	public Object[][] unauthorizedTokenProvider() {
		return new Object[][] {
				{ "Admin", ThreadManager.getAlbatrossToken("Admin") },
				{ "TeamMember", ThreadManager.getAlbatrossToken("TeamMember") },
				{ "RestrictedTeamMember", ThreadManager.getAlbatrossToken("RestrictedTeamMember") }
		};
	}
}
