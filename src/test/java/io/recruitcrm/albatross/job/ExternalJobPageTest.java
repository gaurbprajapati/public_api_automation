package io.recruitcrm.albatross.job;

import com.qa.api.util.reaper.ThreadManager;

import java.util.*;
import io.rcrm.api.company.NestedCustomFieldsTest;
import io.rcrm.api.pojo.albatross.jobs.JobCustomFields;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.pojo.albatross.jobs.JobExternalPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ExternalJobPageTest extends TestBase {

	commanFunction function = new commanFunction();

	String accountAPIKey;
	String albatrossAuthToken;
	JavaFakerJob javaFakerJob;
	private String originForExternalJobPage;
	NestedCustomFieldsTest nestedCustomFieldsTest;
	private String originForNestedCustomFields;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountAPIKey = ThreadManager.getAccountApiKey();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		javaFakerJob = new JavaFakerJob();
		originForExternalJobPage = "https://" + System.getProperty("envname") + "web.recruitcrm.net";
		originForNestedCustomFields = "https://" + System.getProperty("envname") + ".recruitcrm.net";
		nestedCustomFieldsTest = new NestedCustomFieldsTest();
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getTeamTestData", groups = "nightly-build")
	public void getExternalJobTeamCollaboratorDetailTest(String teamId, String teamName) {

		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-team-name/" + teamId, null, null,
				null, true);
		response.then().statusCode(200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.get("data.team_name"), teamName);
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void getExternalJobTeamCollaboratorDetailWithInvalidIdTest() {

		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-team-name/" + "1234",
				null, null, null, true);
		response.then().statusCode(200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.get("message"), "Team not found");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getCompanyTestData", groups = "nightly-build")
	public void getExternalJobCompanyDetailTest(String companySlug, String companyName) {

		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-company-name/" + companySlug,
				null, null, null, true);
		response.then().statusCode(200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getString("data.company_name"), companyName);
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void getExternalJobCompanyDetailWithInvalidSlugTest() {

		Response response = RestClient.doGet("JSON", albatrossURL, "external-pages/get-company-name/" + "1234",
				null, null, null, true);
		response.then().statusCode(200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.get("message"), "Company not found");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getExternalJobPageData", groups = "nightly-build")
	public void getExternalJobPageTest(List<String> teamInfo, List<String> companyInfo, List<String> jobInfo,
			String externalPageUrl) {

		Map<String, String> queryParams = new HashMap<String, String>() {
			{
				put("batch", "true");
				put("account", externalPageUrl);
			}
		};

		JobExternalPage jobExternalPage = JobExternalPage.builder()
				.limit(10)
				.offset(0)
				.search_data("")
				.onlyJobs(true)
				.build();

		Response response = getJobsByAccount(albatrossURL, "external-pages/jobs-by-account/get", queryParams,
				jobExternalPage, originForExternalJobPage);

		JsonPath jsonPath = response.jsonPath();

		List<String> jobDetail = jsonPath.getList("data.jobs");
		List<String> slugs = jsonPath.getList("data.jobs.slug");
		List<String> names = jsonPath.getList("data.jobs.name");

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jobDetail.size(), 1);
		Assert.assertEquals(slugs.get(0), jobInfo.get(0));
		Assert.assertEquals(names.get(0), jobInfo.get(1));
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void getExternalJobPageWithInvalidAccountTest() {

		Map<String, String> queryParams = new HashMap<String, String>() {
			{
				put("account", "1234");
				put("batch", "true");
			}
		};
		JobExternalPage jobExternalPage = JobExternalPage.builder().limit(10).offset(0).search_data("").onlyJobs(true)
				.build();

		Response response = RestClient.doPost("JSON", albatrossURL, "external-pages/jobs-by-account/get",
				albatrossAuthToken, queryParams, true, jobExternalPage);

		response.then().statusCode(200);
		JsonPath jsonPath = response.jsonPath();
		List<String> jobDetail = jsonPath.getList("data.jobs");

		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.get("message"), "You are not allowed to access this URL.");
		Assert.assertEquals(jobDetail.size(), 0);
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getExternalJobPageData", groups = "nightly-build")
	public void getExternalJobPageWithCompanySlugFilterTest(List<String> teamInfo, List<String> companyInfo,
			List<String> jobInfo,
			String externalPageUrl) {

		Map<String, String> queryParams = new HashMap<String, String>() {
			{
				put("batch", "true");
				put("account", externalPageUrl);
				put("companySlug", companyInfo.get(0));
			}
		};

		JobExternalPage jobExternalPage = JobExternalPage.builder()
				.limit(10)
				.offset(0)
				.search_data("")
				.onlyJobs(true)
				.build();

		Response response = getJobsByAccount(albatrossURL, "external-pages/jobs-by-account/get", queryParams,
				jobExternalPage, originForExternalJobPage);

		JsonPath jsonPath = response.jsonPath();

		List<String> jobDetail = jsonPath.getList("data.jobs");
		List<String> slugs = jsonPath.getList("data.jobs.slug");
		List<String> names = jsonPath.getList("data.jobs.name");

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jobDetail.size(), 1);
		Assert.assertEquals(slugs.get(0), jobInfo.get(0));
		Assert.assertEquals(names.get(0), jobInfo.get(1));
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getExternalJobPageData", groups = "nightly-build")
	public void getExternalJobPageWithCompanyInValidSlugFilterTest(List<String> teamInfo, List<String> companyInfo,
			List<String> jobInfo,
			String externalPageUrl) {

		Map<String, String> queryParams = new HashMap<String, String>() {
			{
				put("batch", "true");
				put("account", externalPageUrl);
				put("companySlug", companyInfo.get(0) + "abc");
			}
		};

		JobExternalPage jobExternalPage = JobExternalPage.builder()
				.limit(10)
				.offset(0)
				.search_data("")
				.onlyJobs(true)
				.build();

		Response response = getJobsByAccount(albatrossURL, "external-pages/jobs-by-account/get", queryParams,
				jobExternalPage, originForExternalJobPage);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.get("message"), "Company Slug doesn't exist");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getExternalJobPageData", groups = "nightly-build")
	public void getExternalJobPageWithTeamIDFilterTest(List<String> teamInfo, List<String> companyInfo,
			List<String> jobInfo,
			String externalPageUrl) {

		Map<String, String> queryParams = new HashMap<String, String>() {
			{
				put("batch", "true");
				put("account", externalPageUrl);
				put("teamId", teamInfo.get(0));
			}
		};

		JobExternalPage jobExternalPage = JobExternalPage.builder()
				.limit(10)
				.offset(0)
				.search_data("")
				.onlyJobs(true)
				.build();

		Response response = getJobsByAccount(albatrossURL, "external-pages/jobs-by-account/get", queryParams,
				jobExternalPage, originForExternalJobPage);

		JsonPath jsonPath = response.jsonPath();

		List<String> jobDetail = jsonPath.getList("data.jobs");
		List<String> slugs = jsonPath.getList("data.jobs.slug");
		List<String> names = jsonPath.getList("data.jobs.name");

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jobDetail.size(), 1);
		Assert.assertEquals(slugs.get(0), jobInfo.get(0));
		Assert.assertEquals(names.get(0), jobInfo.get(1));
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getExternalJobPageData", groups = "nightly-build")
	public void getExternalJobPageWithTeamInValidIDFilterTest(List<String> teamInfo, List<String> companyInfo,
			List<String> jobInfo,
			String externalPageUrl) {

		Map<String, String> queryParams = new HashMap<String, String>() {
			{
				put("batch", "true");
				put("account", externalPageUrl);
				put("teamId", teamInfo.get(0) + "1");
			}
		};

		JobExternalPage jobExternalPage = JobExternalPage.builder()
				.limit(10)
				.offset(0)
				.search_data("")
				.onlyJobs(true)
				.build();

		Response response = getJobsByAccount(albatrossURL, "external-pages/jobs-by-account/get", queryParams,
				jobExternalPage, originForExternalJobPage);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.get("message"), "Team ID doesn't exist");
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getExternalJobCustomFieldsData", groups = "nightly-build")
	public void getExternalJobWithDefaultOptionsCustomFieldsTest(String jobSlug) {
		Map<String, Object> fieldData = nestedCustomFieldsTest.createNestedCustomDependency("candidate", "5");
		JobCustomFields jobCustomFields = JobCustomFields.builder().source("jobapply").jobslug(jobSlug).build();
        Response response  = getJobsByAccount(albatrossURL, "external-pages/custom-fields/get-default-options", null, jobCustomFields, originForNestedCustomFields);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.get("message"),"Default options for entity custom fields");

		Map<String, Object> dataMap = jsonPath.getMap("data");
		Set<String> keys = dataMap.keySet();
		String parentKey = keys.iterator().next();
		String childKey = keys.stream().skip(1).findFirst().orElse(null);

		Assert.assertNotNull(parentKey);
		Assert.assertNotNull(childKey);

		List<Map<String, Object>> parentOptions = jsonPath.getList("data." + parentKey);
		Assert.assertEquals(fieldData.get("option1"), parentOptions.get(0).get("value"));
		Assert.assertEquals(fieldData.get("option2"), parentOptions.get(1).get("value"));

		List<Map<String, Object>> childOptions = jsonPath.getList("data." + childKey);
		Assert.assertEquals(fieldData.get("option1"), childOptions.get(0).get("value"));
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getExternalJobCustomFieldsData", groups = "nightly-build")
	public void getExternalJobWithoutDefaultOptionsCustomFieldsTest(String jobSlug) {
		JobCustomFields jobCustomFields = JobCustomFields.builder().source("jobapply").jobslug(jobSlug).build();
		Response response  = getJobsByAccount(albatrossURL, "external-pages/custom-fields/get-default-options", null, jobCustomFields, originForNestedCustomFields);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.get("message"), "Default options for entity custom fields");

		List<Map<String, Object>> dataList = jsonPath.getList("data");
		Assert.assertTrue(dataList.isEmpty());
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getExternalJobCustomFieldsData", groups = "nightly-build")
	public void getExternalJobWithDefaultOptionsCustomFieldsWithInvalidJobslugTest(String jobSlug) {
		JobCustomFields jobCustomFields = JobCustomFields.builder().source("jobapply").jobslug(jobSlug + "12345").build();
		Response response  = getJobsByAccount(albatrossURL, "external-pages/custom-fields/get-default-options", null, jobCustomFields, originForNestedCustomFields);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.get("message"), "Invalid source or identifier provided");
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getExternalJobCustomFieldsData", groups = "nightly-build")
	public void getExternalJobWithDefaultOptionsCustomFieldsWithInvalidEndpointTest(String jobSlug) {
		JobCustomFields jobCustomFields = JobCustomFields.builder().source("jobapply").jobslug(jobSlug).build();
		Response response  = getJobsByAccount(albatrossURL, "external-pages/custom-fields/get-default-options12345", null, jobCustomFields, originForNestedCustomFields);
		Assert.assertEquals(response.getStatusCode(), 404);
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getExternalJobCustomFieldsData", groups = "nightly-build")
	public void getExternalJobWithNestedCustomFieldsTest(String jobSlug) {
		Map<String, Object> fieldData = nestedCustomFieldsTest.createNestedCustomDependency("candidate", "5");
		JobCustomFields jobCustomFields = JobCustomFields.builder().source("jobapply").jobslug(jobSlug).build();
		Response response  = getJobsByAccount(albatrossURL, "external-pages/nested-custom-fields/get", null, jobCustomFields, originForNestedCustomFields);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.get("message"), "NestedCustomFields Dependency Data");

		List<Map<String, Object>> dataList = jsonPath.getList("data");
		Map<String, Object> firstEntry = dataList.get(0);
		String parentKey = firstEntry.keySet().iterator().next();
		Map<String, Object> parentData = (Map<String, Object>) firstEntry.get(parentKey);
		Assert.assertNotNull(parentData);

		Map<String, Object> childrenMap = (Map<String, Object>) parentData.get("children");
		String childKey = childrenMap.keySet().iterator().next();
		Map<String, Object> childData = (Map<String, Object>) childrenMap.get(childKey);
		Assert.assertNotNull(childData);

		Map<String, List<String>> dependencyData = (Map<String, List<String>>) childData.get("dependency");
		String dependencyKey = dependencyData.keySet().iterator().next();
		Assert.assertFalse(dependencyData.get(dependencyKey).isEmpty());
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getExternalJobCustomFieldsData", groups = "nightly-build")
	public void getExternalJobWithoutNestedCustomFieldsTest(String jobSlug) {
		JobCustomFields jobCustomFields = JobCustomFields.builder().source("jobapply").jobslug(jobSlug).build();
		Response response  = getJobsByAccount(albatrossURL, "external-pages/nested-custom-fields/get", null, jobCustomFields, originForNestedCustomFields);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.get("message"), "NestedCustomFields Dependency Data");

		List<Map<String, Object>> dataList = jsonPath.getList("data");
		Assert.assertTrue(dataList.isEmpty());
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getExternalJobCustomFieldsData", groups = "nightly-build")
	public void getExternalJobWithNestedCustomFieldsWithInvalidSlugTest(String jobSlug) {
		JobCustomFields jobCustomFields = JobCustomFields.builder().source("jobapply").jobslug(jobSlug + "12345").build();
		Response response  = getJobsByAccount(albatrossURL, "external-pages/nested-custom-fields/get", null, jobCustomFields, originForNestedCustomFields);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.get("message"), "Invalid source or identifier provided");
	}

	@Owner("Raj Pandey")
	@Test(dataProvider = "getExternalJobCustomFieldsData", groups = "nightly-build")
	public void getExternalJobWithNestedCustomFieldsWithInvalidEndpointTest(String jobSlug) {
		JobCustomFields jobCustomFields = JobCustomFields.builder().source("jobapply").jobslug(jobSlug).build();
		Response response  = getJobsByAccount(albatrossURL, "external-pages/nested-custom-fields/get12345", null, jobCustomFields, originForNestedCustomFields);
		Assert.assertEquals(response.getStatusCode(), 404);
	}


	@DataProvider
	public Object[][] getExternalJobCustomFieldsData(){
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String companySlug = jsonCompany.get("slug");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");
		JsonPath jsonJob = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug).jsonPath();
		String jobSlug = jsonJob.get("slug");

		return new Object[][]{{ jobSlug }};
	}



	@DataProvider
	public Object[][] getExternalJobPageData() {

		List<String> userRoles = Arrays.asList("accountOwner", "teamMember");
		List<String> teamInfo = function.createTeams(userRoles, albatrossURL, albatrossAuthToken, baseURL,
				accountAPIKey);
		String teamId = teamInfo.get(1);
		String teamName = teamInfo.get(0);
		String externalPageUrl = function.getAccountDetail(albatrossURL, albatrossAuthToken).get(0);

		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String companySlug = jsonCompany.get("slug");
		String companyName = jsonCompany.get("name");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");

		Job job = new Job();
		job.setName(javaFakerJob.getJobName());
		job.setCompany_slug(companySlug);
		job.setContact_slug(contactSlug);
		job.setNumber_of_openings(javaFakerJob.getOpenings());
		job.setJob_description_text("Sample JD");
		job.setEnable_job_application_form(1);
		job.setCollaborator_team_ids(String.valueOf(teamId));

		JsonPath response = RestClient.doPost("JSON", baseURL, "jobs", accountAPIKey, null, true, job).jsonPath();

		String jobSlug = response.get("slug");
		String jobName = response.get("name");

		return new Object[][] {
				{ Arrays.asList(teamId, teamName), Arrays.asList(companySlug, companyName),
						Arrays.asList(jobSlug, jobName), externalPageUrl }
		};
	}

	@DataProvider
	public Object[][] getTeamTestData() {
		List<String> userRoles = Arrays.asList("accountOwner", "teamMember");
		List<String> teamInfo = function.createTeams(userRoles, albatrossURL, albatrossAuthToken, baseURL,
				accountAPIKey);
		String teamId = teamInfo.get(1);
		String teamName = teamInfo.get(0);
		return new Object[][] {
				{ teamId, teamName } };
	};

	@DataProvider
	public Object[][] getCompanyTestData() {
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String companySlug = jsonCompany.get("slug");
		String companyName = jsonCompany.get("company_name");
		return new Object[][] {
				{ companySlug, companyName } };
	}

	public static Response getJobsByAccount(String albatrossURL, String basePath, Map<String, String> queryParams,
			Object jobExternalPage, String origin) {
		Map<String, String> headers = new HashMap<>();
		headers.put("accept", "*/*");
		headers.put("content-type", "application/json");
		headers.put("origin", origin);

		String fullURL = albatrossURL + "/" + basePath;
		RequestSpecification request = RestAssured.given().headers(headers)
				.body(jobExternalPage);

		// Add queryParams only if it's not null
		if (queryParams != null) {
			request.queryParams(queryParams);
		}
		Response response = request.post(fullURL.toString());
		return response;
	}

}
