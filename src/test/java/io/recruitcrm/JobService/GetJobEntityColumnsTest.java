package io.recruitcrm.JobService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
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
public class GetJobEntityColumnsTest extends TestBase {

	private static final String BASE_PATH = "entity-columns";
	String albatrossAuthToken;

	public GetJobEntityColumnsTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"job_service", "nightly-build"})
	public void getJobEntityColumnsTest_200() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "jobs");
		Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

		assert response != null;
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Entity Column Fetched Successfully");
		Assert.assertEquals(response.jsonPath().getInt("meta.status"), 200);
		Assert.assertEquals(response.jsonPath().getInt("meta.responseType.code"), 103);
		Assert.assertEquals(response.jsonPath().getString("meta.responseType.context"), "Request is successful");

		Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");
		assertThat("Columns should not be null", columnsMap, notNullValue());
		assertThat("Columns should contain id", columnsMap.containsKey("id"), is(true));

		// Expected job columns from entity-columns response
		List<String> requiredColumns = Arrays.asList(
				"id", "srno", "name", "jobstatus", "jobstatuslabel", "archived",
				"companyid", "companyname", "companyslug", "contactid", "contactname",
				"salarytype", "currencyid", "state", "country", "collaborator",
				"noofopenings", "ownerid", "ownername", "createdby", "creatorname",
				"createdon", "updatedon", "updatedby", "slug",
				"description", "jdtextdetails", "minexperienceinyears", "maxexperienceinyears",
				"annualsalarymin", "annualsalarymax", "address", "city", "locality",
				"job_skill", "qualificationid", "qualification", "job_type", "job_function",
				"job_industry", "remote", "postalcode", "pay_rate", "bill_rate");
		for (String column : requiredColumns) {
			assertThat("Missing expected column: " + column, columnsMap.containsKey(column), is(true));
		}
		boolean hasCustColumn = columnsMap.keySet().stream().anyMatch(name -> name.startsWith("custcolumn"));
		assertThat("At least one custom column (custcolumn*) should be present", hasCustColumn, is(true));

		// Validate id column structure
		Map<String, Object> idColumn = response.jsonPath().getMap("data[0].columns.id");
		assertThat("Id column should not be null", idColumn, notNullValue());
		String[] idColumnImportantFields = {
				"entity", "field", "label", "longlabel",
				"visible", "visible_locked", "detailPageVisible", "detailPageOrder",
				"detailPageOrderV2", "listPageOrder", "primaryOrder"
		};
		for (String field : idColumnImportantFields) {
			assertThat("Id column missing important field: " + field, idColumn.containsKey(field), is(true));
		}

		// Validate name column (Job Title)
		Map<String, Object> nameColumn = response.jsonPath().getMap("data[0].columns.name");
		assertThat("Name column should not be null", nameColumn, notNullValue());
		assertThat("Name column entity should be jobs", nameColumn.get("entity"), equalTo("jobs"));
		assertThat("Name column field should be name", nameColumn.get("field"), equalTo("name"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"job_service", "nightly-build"})
	public void getJobEntityColumnsUnauthorizedTest_401() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "jobs");

		Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, "InvalidToken", queryParameters, null, true);
		assert response != null;
		Assert.assertEquals(response.getStatusCode(), 401);
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"job_service", "nightly-build"})
	public void getJobEntityColumnsTest_404() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "jobs");

		Response response = RestClient.doGet("JSON", jobServiceURL, "ntity-columns", albatrossAuthToken, queryParameters, null, true);
		assert response != null;
		Assert.assertEquals(response.getStatusCode(), 404);
		Assert.assertEquals(response.jsonPath().get("error"), "Not Found");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"job_service", "nightly-build"})
	public void getJobEntityColumnsTest_400() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "");

		Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);
		assert response != null;
		Assert.assertEquals(response.getStatusCode(), 400);
		Assert.assertEquals(response.jsonPath().get("meta.responseType.context"), "Error while processing request");
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"job_service", "nightly-build"})
	public void getJobEntityColumnsTest_405() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "jobs");

		Response response = RestClient.doPost("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken,
				queryParameters, true, null);
		Assert.assertEquals(response.getStatusCode(), 405);
		Assert.assertEquals(response.jsonPath().get("error"), "Method Not Allowed");
	}

	@Owner("Suhel Bhadane")
	@Test(dataProvider = "entityDataProvider", groups = {"job_service", "nightly-build"})
	public void getJobEntityColumnsTestInvalidEntityTypes(String entity, int expectedStatus) {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", entity);

		Response response = RestClient.doGet("JSON", jobServiceURL, BASE_PATH, albatrossAuthToken,
				queryParameters, null, true);

		assert response != null;
		Assert.assertEquals(response.getStatusCode(), expectedStatus, "Unexpected status for entity: " + entity);

		if (expectedStatus == 400) {
			Assert.assertEquals(response.jsonPath().getInt("meta.status"), 400, "Incorrect status code in response");
			Assert.assertEquals(response.jsonPath().getInt("meta.responseType.code"), 101, "Incorrect response code");
			Assert.assertEquals(response.jsonPath().getString("meta.responseType.context"), "Error while processing request", "Incorrect response context");
			Assert.assertEquals(response.jsonPath().getString("errors[0].message"), "Entity must be one of the predefined enum values", "Incorrect error message");
			Assert.assertEquals(response.jsonPath().getInt("errors[0].errorType.code"), 201, "Incorrect error type code");
			Assert.assertEquals(response.jsonPath().getString("errors[0].errorType.context"), "Validation Error", "Incorrect error type context");
		}
	}

	@DataProvider(name = "entityDataProvider")
	public Object[][] entityDataProvider() {
		return new Object[][] {
				{ "12345", 400 },
				{ "invalidEntity1121309", 400 },
				{ "@#$@", 400 }
		};
	}
}
