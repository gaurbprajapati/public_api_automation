package io.recruitcrm.CandidateService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAccountCustomViewPageTest extends TestBase {

	private static final String BASE_PATH = "account-view-columns";
	JavaFakerCustomField faker = new JavaFakerCustomField();
	String albatrossAuthToken;

	public GetAccountCustomViewPageTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Yash Rampal")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getAccountViewForColumns_200() {
		String customTextField = createCustomFields("text", "text");

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidates");

		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH,
				ThreadManager.getOwnerAlbatrossToken(),
				queryParameters, null, true);

		assertThat(response.getStatusCode(), equalTo(200));
		response.then().assertThat()
				.body(matchesJsonSchemaInClasspath("privateApi/candidate/customView.json"));
		assertThat(response.jsonPath().getString("meta.message"), equalTo("Account View Columns Fetched Successfully"));
		Map<String, Object> accountViewColumns = response.jsonPath().getMap("data[0].accountViewColumns");
		boolean isCustomFieldPresent = accountViewColumns.entrySet().stream()
				.anyMatch(entry -> entry.getValue() instanceof Map &&
						customTextField.equals(((Map<?, ?>) entry.getValue()).get("label")));

		assertThat("Custom field '" + customTextField + "' not found in accountViewColumns!", isCustomFieldPresent, is(true));
	}

	@Owner("Raj Pandey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getAccountViewForColumns_405() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidates");

		Response response = RestClient.doPost("JSON", candidatesURL, BASE_PATH,
				ThreadManager.getOwnerAlbatrossToken(),
				queryParameters, false, null);
		assertThat(response.getStatusCode(), equalTo(405));
		assertThat(response.jsonPath().getString("errors[0].message"), containsString("not supported"));
		assertThat(response.jsonPath().getInt("meta.status"), equalTo(405));
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getAccountViewForColumns_401() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidates");

		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH,
				"InvalidAuthToken",
				queryParameters, null, true);
		assertThat(response.getStatusCode(), equalTo(401));
		assertThat(response.jsonPath().getString("meta.message"), equalTo("Unauthorised access"));
		assertThat(response.jsonPath().getInt("meta.responseType.code"), equalTo(104));
	}

	

	@Owner("Gaurav Prajapati")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getAccountViewForColumns_404() {
		String basePath = "ccount-view-columns"; // Incorrect endpoint

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidates");

		Response response = RestClient.doGet("JSON", candidatesURL, basePath,
				ThreadManager.getOwnerAlbatrossToken(),
				queryParameters, null, true);
		assertThat(response.getStatusCode(), equalTo(404));
		assertThat(response.jsonPath().getInt("meta.status"), equalTo(404));
		assertThat(response.jsonPath().get("errors[0].message"), equalTo("No static resource v2/ccount-view-columns."));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateAssignedJobAccountViewColumns_200() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidate_assigned_job");
		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);
		assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));

		// Validate meta information
		assertThat("Expected success message", response.jsonPath().getString("meta.message"), equalTo("Account View Columns Fetched Successfully"));
		assertThat("Expected response type code 103", response.jsonPath().getInt("meta.responseType.code"), equalTo(103));
		assertThat("Expected success context", response.jsonPath().getString("meta.responseType.context"), equalTo("Request is successful"));
		assertThat("Expected status 200 in meta", response.jsonPath().getInt("meta.status"), equalTo(200));
		assertThat("Expected timestamp not null", response.jsonPath().get("meta.timestamp"), notNullValue());
		assertThat("Expected requestUuid not null", response.jsonPath().get("meta.requestUuid"), notNullValue());

		// Validate account view columns structure
		Map<String, Object> accountViewColumns = response.jsonPath().getMap("data[0].accountViewColumns");
		assertThat("Account view columns should not be null", accountViewColumns, notNullValue());

		// Validate required columns exist based on actual API response structure
		String[] requiredColumns = {
				"id", "srno", "name", "jobstatus", "archived", "jobstatuslabel", "companyid", "companyname",
				"contactid", "secondarycontactid", "salarytype", "currencyid", "state", "country",
				"jobquestions", "allowapply", "description", "job_skill", "address", "city", "locality",
				"qualificationid", "qualification", "specialization", "detailfilename", "jdtext", "jdtextdetails",
				"minexperienceinyears", "maxexperienceinyears", "annualsalarymin", "annualsalarymax",
				"noofopenings", "contactname", "contactslug", "companyslug", "contactemail", "contactnumber",
				"ownerid", "ownername", "createdby", "creatorname", "createdon", "updatedon", "updatedby",
				"hiring_pipeline_id", "hiring_pipeline_name", "job_type", "job_category", "remote", "hotlist",
				"contactfirstname", "contactlastname", "postalcode", "candidatestatus", "share", "interview",
				"bill_rate", "pay_rate", "formatted_cv", "coverletter", "portfolio", "other_file_1", "other_file_2",
				"slug", "jobpostingstatus", "note"
		};

		for (String column : requiredColumns) {
			assertThat("Missing expected account view column: " + column, accountViewColumns.containsKey(column), is(true));
		}

		// Validate specific column properties using Hamcrest assertions with descriptive messages
		assertThat("Expected id column label", response.jsonPath().getString("data[0].accountViewColumns.id.label"), equalTo("Id"));
		assertThat("Expected id column entity", response.jsonPath().getString("data[0].accountViewColumns.id.entity"), equalTo("candidate_assigned_job"));
		assertThat("Expected id column field", response.jsonPath().getString("data[0].accountViewColumns.id.field"), equalTo("id"));

		assertThat("Expected srno column label", response.jsonPath().getString("data[0].accountViewColumns.srno.label"), equalTo("ID"));
		assertThat("Expected srno column longlabel", response.jsonPath().getString("data[0].accountViewColumns.srno.longlabel"), equalTo("Job ID"));
		assertThat("Expected srno column entity", response.jsonPath().getString("data[0].accountViewColumns.srno.entity"), equalTo("candidate_assigned_job"));
		assertThat("Expected srno column field", response.jsonPath().getString("data[0].accountViewColumns.srno.field"), equalTo("srno"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateAssignedJobAccountViewColumns_401() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidate_assigned_job");

		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken + "InvalidAuthToken", queryParameters, null, true);

		assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
		assertThat("Expected unauthorized message", response.jsonPath().getString("meta.message"), equalTo("Unauthorised access"));
		assertThat("Expected response type code 104", response.jsonPath().getInt("meta.responseType.code"), equalTo(104));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateAssignedJobAccountViewColumns_404() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidate_assigned_job");

		Response response = RestClient.doGet("JSON", candidatesURL, "ccount-view-columns", // Incorrect endpoint
				albatrossAuthToken, queryParameters, null, true);

		assertThat("Expected status code 404", response.getStatusCode(), equalTo(404));
		assertThat("Expected not found error", response.jsonPath().getString("error"), equalTo("Not Found"));
		assertThat("Expected correct path in error", response.jsonPath().getString("path"), equalTo("/v2/ccount-view-columns"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateAssignedJobAccountViewColumns_405() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidate_assigned_job");

		Response response = RestClient.doPost("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, false, null);

		assertThat("Expected status code 405", response.getStatusCode(), equalTo(405));
		assertThat("Expected method not allowed error", response.jsonPath().getString("error"), equalTo("Method Not Allowed"));
		assertThat("Expected correct path in error", response.jsonPath().getString("path"), equalTo("/v2/account-view-columns"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateAssignedJobAccountViewColumns_400() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", ""); // Empty entity parameter

		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

		assertThat("Expected status code 400", response.getStatusCode(), equalTo(400));
		assertThat("Expected error context", response.jsonPath().getString("meta.responseType.context"), equalTo("Error while processing request"));
	}

	public String createCustomFields(String extraFieldType, String defaultValue) {
		String customFieldName = faker.getCustomFieldName("candidate");
		String basePath = "custom-fields";

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(5);
		extraField.setExtrafieldname(customFieldName);
		extraField.setDefaultvalue(faker.getDefaultvalue(defaultValue));
		extraField.setExtrafieldtype(extraFieldType);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false,
				customField);
		assertThat(response.getStatusCode(), equalTo(200));
		return customFieldName;
	}
}
