package io.recruitcrm.CandidateService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
public class GetEntityColumnsTest extends TestBase {
	
	// Constants for reusability
	private static final String BASE_PATH = "entity-columns";
	private static final int CANDIDATE_ENTITY_TYPE_ID = 5;
	
	JavaFakerCustomField faker = new JavaFakerCustomField();
	String albatrossAuthToken;
	String createdFieldName; // Store the field name for verification

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		createdFieldName = createCustomFields("text", "text");
	}

	@Owner("Raj Pandey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getEntityColumnsTest_200() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidates");
		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(response.jsonPath().get("meta.message").toString(), "Entity Column Fetched Successfully");
		Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");
		List<String> requiredColumns = Arrays.asList(
				"srno", "candidatename", "firstname", "lastname", "emailid",
				"genderid", "contactnumber", "candidatedob", "age", "qualificationid",
				"specialization", "workexpyr", "summary", "relevantexperience", "email_opt_out",
				"resumetext", "currentsalary", "resume", "resumefilename", "lastorganisation",
				"skill", "willingtorelocate", "salaryexpectation", "position", "address",
				"city", "locality", "state", "country", "currencyid", "currencycountry", "symbol",
				"accountid", "ownerid", "ownerslug", "ownername", "canaccess", "profilepic",
				"deleted", "authid", "resumeupdatedon", "resumeupdaterequestedon",
				"requestresumelinkstatus", "resumeaddedon", "profilefacebook", "profiletwitter",
				"profilelinkedin", "profilegithub", "profilexing", "source", "createdbyname",
				"qualification", "currentstatus", "noticeperiod", "createdby", "createdon",
				"updatedon", "availablefrom", "salarytype", "languageskills", "proficiency_level",
				"accountlogo", "accounttitle", "candidateterms", "jobapplicationsettings",
				"eeocompliance", "gdprcompliance", "unavailable",
				"slug", "institute_name", "educational_qualification", "educational_specialization",
				"grade", "education_location", "education_start_date", "education_end_date",
				"education_description", "education_history", "work_history", "title",
				"work_company_name", "employment_type", "industry_id", "work_location", "salary",
				"is_currently_working", "work_start_date", "work_end_date", "work_description",
				"hotlist", "candidate_company_slug", "candidate_company_city", "candidate_company_logo",
				"off_limit_status", "last_calllog_created_on", "last_sms_sent_on",
				"last_email_sent_on", "last_communication_timestamp", "last_communication_method");
		for (String column : requiredColumns) {
			Assert.assertTrue(columnsMap.containsKey(column),
					"Missing expected column: " + column);
		}
		boolean isCustomFieldPresent = columnsMap.keySet().stream().anyMatch(name -> name.startsWith("custcolumn"));
		Assert.assertTrue(isCustomFieldPresent, "No custom field column found in the response!");
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getEntityColumnsUnauthorizedTest_401() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidates");

		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, "InvalidToken", queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(response.jsonPath().getString("meta.message"), "Unauthorised access");
		Assert.assertEquals(response.jsonPath().getInt("meta.status"), 401);
		Assert.assertEquals(response.jsonPath().getInt("meta.responseType.code"), 104);
		Assert.assertEquals(response.jsonPath().getString("meta.responseType.context"),
				"Warning");
		Assert.assertEquals(response.jsonPath().getString("data"), "Invalid or expired token");
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getEntityColumnsTest_404() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidates");

		Response response = RestClient.doGet("JSON", candidatesURL, "ntity-columns", albatrossAuthToken, queryParameters, null, true);
		Assert.assertEquals(response.getStatusCode(), 404);
		Assert.assertNull(response.jsonPath().get("data"));
		Assert.assertEquals(response.jsonPath().get("errors[0].message"), "No static resource v2/ntity-columns.");
	}

	@Owner("Yash Rampal")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getEntityColumnsTest_400() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", ""); // Missing entity

		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 400);
		Assert.assertEquals(response.jsonPath().get("meta.responseType.context"), "Error while processing request");
	}

	@Owner("Raj Pandey")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getEntityColumnsTest_405() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidates");

		Response response = RestClient.doPost("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters,
				true, null);
		Assert.assertEquals(response.getStatusCode(), 405);
	}

	@Owner("Sampurn Chouksey")
	@Test(dataProvider = "entityDataProvider", groups = {"candidate_service", "nightly-build"})
	public void getEntityColumnsTestInvalidEntityTypes_400(String entity, int expectedStatus) {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", entity);

		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken,
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), expectedStatus, "Unexpected status for entity: " + entity);

		if (expectedStatus == 400) {
			Assert.assertEquals(response.jsonPath().getInt("meta.status"), 400, "Incorrect status code in response");
			Assert.assertEquals(response.jsonPath().getInt("meta.responseType.code"), 101, "Incorrect response code");
			Assert.assertEquals(response.jsonPath().getString("meta.responseType.context"),
					"Error while processing request", "Incorrect response context");
			Assert.assertEquals(response.jsonPath().getString("errors[0].message"),
					"Entity must be one of the predefined enum values", "Incorrect error message");
			Assert.assertEquals(response.jsonPath().getInt("errors[0].errorType.code"), 201,
					"Incorrect error type code");
			Assert.assertEquals(response.jsonPath().getString("errors[0].errorType.context"),
					"Validation Error", "Incorrect error type context");
		}
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateAssignedJobEntityColumnsTest_200() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidate_assigned_job");
		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, null, true);

		assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
		assertThat("Expected success message", response.jsonPath().getString("meta.message"), equalTo("Entity Column Fetched Successfully"));
		assertThat("Expected response type code 103", response.jsonPath().getInt("meta.responseType.code"), equalTo(103));
		assertThat("Expected success context", response.jsonPath().getString("meta.responseType.context"), equalTo("Request is successful"));
		assertThat("Expected status 200 in meta", response.jsonPath().getInt("meta.status"), equalTo(200));
		assertThat("Expected timestamp not null", response.jsonPath().get("meta.timestamp"), notNullValue());
		assertThat("Expected requestUuid not null", response.jsonPath().get("meta.requestUuid"), notNullValue());

		// Validate columns structure
		assertThat("Expected columns data not null", response.jsonPath().get("data[0].columns"), notNullValue());

		// Validate required columns based on actual API response structure
		List<String> requiredColumns = Arrays.asList(
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
		);

		for (String column : requiredColumns) {
			assertThat("Missing expected column: " + column,
					response.jsonPath().get("data[0].columns." + column), notNullValue());
		}

		// Validate presence of custom fields
		Map<String, Object> columnsMap = response.jsonPath().getMap("data[0].columns");
		boolean isCustomFieldPresent = columnsMap.keySet().stream().anyMatch(name -> name.startsWith("custcolumn"));
		assertThat("Expected custom field columns to be present", isCustomFieldPresent, is(true));

		// Validate presence of job associated custom fields
		boolean isJobAssociatedCustomFieldPresent = columnsMap.keySet().stream()
				.anyMatch(name -> name.startsWith("job_associated_cust_column"));
		assertThat("Expected job associated custom field columns to be present", isJobAssociatedCustomFieldPresent, is(true));

		// Validate specific column properties using Hamcrest assertions
		assertThat("Expected id column label", response.jsonPath().getString("data[0].columns.id.label"), equalTo("Id"));
		assertThat("Expected id column entity", response.jsonPath().getString("data[0].columns.id.entity"), equalTo("candidate_assigned_job"));
		assertThat("Expected id column field", response.jsonPath().getString("data[0].columns.id.field"), equalTo("id"));
		assertThat("Expected id column type", response.jsonPath().getString("data[0].columns.id.type"), equalTo("number"));

		assertThat("Expected srno column label", response.jsonPath().getString("data[0].columns.srno.label"), equalTo("ID"));
		assertThat("Expected srno column longlabel", response.jsonPath().getString("data[0].columns.srno.longlabel"), equalTo("Job ID"));
		assertThat("Expected srno column entity", response.jsonPath().getString("data[0].columns.srno.entity"), equalTo("candidate_assigned_job"));
		assertThat("Expected srno column field", response.jsonPath().getString("data[0].columns.srno.field"), equalTo("srno"));
		assertThat("Expected srno column type", response.jsonPath().getString("data[0].columns.srno.type"), equalTo("number"));

		assertThat("Expected name column label", response.jsonPath().getString("data[0].columns.name.label"), equalTo("Name"));
		assertThat("Expected name column longlabel", response.jsonPath().getString("data[0].columns.name.longlabel"), equalTo("Job Name"));
		assertThat("Expected name column entity", response.jsonPath().getString("data[0].columns.name.entity"), equalTo("candidate_assigned_job"));
		assertThat("Expected name column field", response.jsonPath().getString("data[0].columns.name.field"), equalTo("name"));
		assertThat("Expected name column type", response.jsonPath().getString("data[0].columns.name.type"), equalTo("text"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateAssignedJobEntityColumnsUnauthorizedTest_401() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidate_assigned_job");

		Response response = RestClient.doGet("JSON", candidatesURL, BASE_PATH, "InvalidToken", queryParameters, null, true);
		assertThat("Expected status code 401", response.getStatusCode(), equalTo(401));
		assertThat("Expected unauthorized message", response.jsonPath().getString("meta.message"), equalTo("Unauthorised access"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateAssignedJobEntityColumnsTest_404() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidate_assigned_job");

		Response response = RestClient.doGet("JSON", candidatesURL, "ntity-columns", albatrossAuthToken, queryParameters, null, true); // Incorrect URL
		assertThat("Expected status code 404", response.getStatusCode(), equalTo(404));
		assertThat("Expected not found error", response.jsonPath().getString("error"), equalTo("Not Found"));
	}

	@Owner("Suhel Bhadane")
	@Test(groups = {"candidate_service", "nightly-build"})
	public void getCandidateAssignedJobEntityColumnsTest_405() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity", "candidate_assigned_job");

		Response response = RestClient.doPost("JSON", candidatesURL, BASE_PATH, albatrossAuthToken, queryParameters, true, null);
		assertThat("Expected status code 405", response.getStatusCode(), equalTo(405));
		assertThat("Expected method not allowed error", response.jsonPath().getString("error"), equalTo("Method Not Allowed"));
	}

	public String createCustomFields(String extraFieldType, String defaultValue) {
		String customFieldName = faker.getCustomFieldName("candidates");
		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(CANDIDATE_ENTITY_TYPE_ID);
		extraField.setExtrafieldname(customFieldName);
		extraField.setExtrafieldtype(extraFieldType);
		extraField.setDefaultvalue(null);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false,
				customField);
		Assert.assertEquals(response.getStatusCode(), 200);
		return customFieldName;
	}

	@DataProvider(name = "entityDataProvider")
	public Object[][] entityDataProvider() {
		return new Object[][] {
				{ "jobs", 400 },
				{ "companies", 400 },
				{ "contacts", 400 },
				{ "12345", 400 },
				{ "invalidEntity1121309", 400 },
				{ "@#$@", 400 },
		};
	}
}
