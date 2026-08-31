package io.rcrm.api.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.CandQuesAnsWithoutJob;
import io.rcrm.api.pojo.CustomField;
import io.rcrm.api.pojo.JobAssociatedFields;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CustomFieldsAndJobAssociatedFieldsTest extends TestBase {
	public CustomFieldsAndJobAssociatedFieldsTest() {
		// TODO Auto-generated constructor stub
		super();
	}

	commanFunction function = new commanFunction();
	String slug = "";
	CandQuesAnsWithoutJob candQuesAnsWithoutJob = new CandQuesAnsWithoutJob();
	AllCrudFunctions privateFunction = new AllCrudFunctions();
	JavaFakerJob faker = new JavaFakerJob();
	String apiAuthToken, albatrossTkn;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}
	
	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void getJobAssociatedFieldsWithValidResponse200() {
		String candidateSlug;
		String jobSlug;
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		} catch (Exception e) {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters, true, null);
		pathParamters = new HashMap<String, String>();

		pathParamters.put("job", jobSlug);
		pathParamters.put("candidate", candidateSlug);

		String basePath1 = "candidates/associated-field/{candidate}/{job}";
		Response response1 = RestClient.doGet("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamters, true);

		response1.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//job//jobAssociatedCustomFields.json"));

		Assert.assertEquals(response1.getStatusCode(), 200);

	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void getJobAssociatedFieldsWithInvalidAuthToken() {
		String candidateSlug;
		String jobSlug;
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		} catch (Exception e) {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters, true, null);
		pathParamters = new HashMap<String, String>();

		pathParamters.put("job", jobSlug);
		pathParamters.put("candidate", candidateSlug);
		String basePath1 = "candidates/associated-field/{candidate}/{job}";
		Response response1 = RestClient.doGet("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey()+"12345", null, pathParamters, true);

		Assert.assertEquals(response1.getStatusCode(), 401);

	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void getJobAssociatedFieldsWithInvalidResponse404() {
		String candidateSlug;
		String jobSlug;
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		} catch (Exception e) {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters, true, null);
		pathParamters = new HashMap<String, String>();

		pathParamters.put("job", jobSlug);
		pathParamters.put("candidate", candidateSlug + "123");

		String basePath1 = "candidates/associated-field/{candidate}/{job}";

		Response response1 = RestClient.doGet("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamters, true);
		JsonPath jp1 = response1.jsonPath();

		Assert.assertEquals(response1.getStatusCode(), 404);
		int errorCodeInt = jp1.getInt("statusCode");
		String message = jp1.getString("message");
		String errorCodeString = String.valueOf(errorCodeInt);

		Assert.assertEquals("404", errorCodeString, "statusCode");
		Assert.assertEquals("Candidate doesn't exist", message, "Error Message");

	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void updateJobAssociatedFieldsWithValidResponse200() {
		String candidateSlug;
		String jobSlug; 
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		} catch (Exception e) {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters, true, null);
		AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
		albatrossFunctions1.createJobAssociatedFields(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		pathParamters = new HashMap<String, String>();
		pathParamters.put("job", jobSlug);
		pathParamters.put("candidate", candidateSlug);
		// Create associated_custom_field array
		List<CustomField> associatedCustomFieldsList = new ArrayList<>();
		
		CustomField field1 = new CustomField();
        field1.setField_id(1);
        field1.setValue("Text1");
        associatedCustomFieldsList.add(field1);

		// Create JobAssociatedFields object and set the array
		JobAssociatedFields jobAssociatedFields = new JobAssociatedFields();
		jobAssociatedFields.setAssociated_fields(associatedCustomFieldsList);

		String basePath1 = "candidates/associated-field/{candidate}/{job}";
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				jobAssociatedFields);
		response1.then().assertThat()
				.body(matchesJsonSchemaInClasspath("publicApi//job//updateJobAssociatedCustomFields.json"));

		Assert.assertEquals(response1.getStatusCode(), 200);

	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void updateJobAssociatedFieldsWithFileAsValue() {
		String candidateSlug;
		String jobSlug;
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		} catch (Exception e) {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters, true, null);
		pathParamters = new HashMap<String, String>();
		pathParamters.put("job", jobSlug);
		pathParamters.put("candidate", candidateSlug);
		// Create associated_custom_field array
		List<CustomField> associatedCustomFieldsList = new ArrayList<>();

		CustomField field1 = new CustomField();
		field1.setField_id(6);
		field1.setValue("https://files-for-testing.s3-ap-southeast-1.amazonaws.com/Sandeep_resume.pdf");
		associatedCustomFieldsList.add(field1);

		// Create JobAssociatedFields object and set the array
		JobAssociatedFields jobAssociatedFields = new JobAssociatedFields();
		jobAssociatedFields.setAssociated_fields(associatedCustomFieldsList);

		String basePath1 = "candidates/associated-field/{candidate}/{job}";
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				jobAssociatedFields);
		JsonPath jp = response1.jsonPath();

		Assert.assertEquals( jp.getString("data[0].file_name"),"sandeep_resume.pdf");
		Assert.assertEquals(response1.getStatusCode(), 200);
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void updateJobAssociatedFieldsWithInvalidResponse404() {
		String candidateSlug;
		String jobSlug;
		try {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		} catch (Exception e) {
			candidateSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "candidate");
			jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");
		}

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters, true, null);
		AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();
		albatrossFunctions1.createJobAssociatedFields(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
		pathParamters = new HashMap<String, String>();
		pathParamters.put("job", jobSlug);
		pathParamters.put("candidate", candidateSlug + "1234");
		// Create associated_custom_field array
		List<CustomField> associatedCustomFieldsList = new ArrayList<>();

		CustomField field1 = new CustomField();
        field1.setField_id(1);
        field1.setValue("Text1");
        associatedCustomFieldsList.add(field1);

		// Create JobAssociatedFields object and set the array
		JobAssociatedFields jobAssociatedFields = new JobAssociatedFields();
		jobAssociatedFields.setAssociated_fields(associatedCustomFieldsList);

		String basePath1 = "candidates/associated-field/{candidate}/{job}";
		Response response1 = RestClient.doPost1("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamters, true,
				jobAssociatedFields);
		JsonPath jp1 = response1.jsonPath();

		Assert.assertEquals(response1.getStatusCode(), 404);
		int errorCodeInt = jp1.getInt("statusCode");
		String message = jp1.getString("message");
		String errorCodeString = String.valueOf(errorCodeInt);

		Assert.assertEquals("404", errorCodeString, "statusCode");
		Assert.assertEquals("Candidate doesn't exist", message, "Error Message");

	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void getCustomFieldsWithValidResponse200() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String basePath1 = "custom-fields";
		Response response1 = RestClient.doGet("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamters, true);
		Assert.assertEquals(response1.getStatusCode(), 200);
		response1.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//customFields.json"));


	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void getCustomFieldsWithInvalidResponse404() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String basePath1 = "custom-fieldss";
		Response response1 = RestClient.doGet("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamters, true);
		Assert.assertEquals(response1.getStatusCode(), 404);

	}

	@Owner("Raj Pandey")
	@Test(groups = "nightly-build")
	public void getJobCustomFieldsWithValidResponse200() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String basePath1 = "custom-fields/jobs";
		Response response1 = RestClient.doGet("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamters, true);
		response1.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//customFields.json"));

		Assert.assertEquals(response1.getStatusCode(), 200);

	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void getJobCustomFieldsWithInvalidResponse404() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		String basePath1 = "custom-fields/jobss";
		Response response1 = RestClient.doGet("JSON", baseURL, basePath1, ThreadManager.getAccountApiKey(), null, pathParamters, true);
		Assert.assertEquals(response1.getStatusCode(), 404);

	}
	
	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void verifyCustomFieldValueInShowAllJobs_Test(String value, String value2) {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "10");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "jobs", apiAuthToken, queryParameters, null, true);
		
		response.then().statusCode(200);
		response.then().body("data[0].id", Matchers.notNullValue());
		response.then().body("current_page", Matchers.comparesEqualTo(1));
		response.then().body("data.size()", Matchers.equalTo(2));
		response.then().body("data[0].custom_fields[0].value", Matchers.containsString(value2));
		response.then().body("data[1].custom_fields[0].value", Matchers.containsString(value));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//job//getAllJobs.json"));
	}
	
	@DataProvider
	public Object[][] createCustomFields() {
		int entityId, entityId2, columnId;
		String jobSlug = function.getEntityResponse(baseURL, apiAuthToken, "job");
		entityId =  privateFunction.getJobResponse(albatrossURL, albatrossTkn, jobSlug).jsonPath().get("data.job.id");
		jobSlug = function.getEntityResponse(baseURL, apiAuthToken, "job");
		entityId2 =  privateFunction.getJobResponse(albatrossURL, albatrossTkn, jobSlug).jsonPath().get("data.job.id");
		Response resp = function.createCustomFieldsResponse(albatrossURL, albatrossTkn, "job", "jobField", "text", "");
		columnId = resp.jsonPath().get("data.custumField.columnid");
		String value = faker.getJobCustomField("text");
		String value2 = faker.getJobCustomField("text");
		privateFunction.updateCustomField("job", albatrossURL, entityId, albatrossTkn, "custcolumn" + columnId, value);
		privateFunction.updateCustomField("job", albatrossURL, entityId2, albatrossTkn, "custcolumn" + columnId, value2);
		return new Object[][] { { value, value2 } };
	}
}