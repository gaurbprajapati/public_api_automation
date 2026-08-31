package io.recruitcrm.albatross.job;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.pojo.albatross.jobs.CheckMergeDuplicate;
import io.rcrm.api.pojo.albatross.jobs.JobUpdateData;
import io.rcrm.api.pojo.albatross.jobs.MergeDuplicates;
import io.rcrm.api.pojo.albatross.jobs.SearchEntity;
import io.rcrm.api.pojo.albatross.jobs.UpdateJobRequest;
import io.rcrm.api.pojo.albatross.xmlfeed.BulkUpdateXmlFeedField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointJobAlbatrossTest extends TestBase {

	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	Object accountAPIKey;
	Object albatrossAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		accountAPIKey = ThreadManager.getAccountApiKey();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void bulkUpdateJobsXmlFeedField() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath().getString("slug");
		String jobSlug1 = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath().getString("slug");
		String jobSlug2 = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath().getString("slug");
		int jobId1 = allCrudFunctions.getJobResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), jobSlug1).jsonPath().getInt("data.job.id");
		int jobId2 = allCrudFunctions.getJobResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), jobSlug2).jsonPath().getInt("data.job.id");
		allCrudFunctions.createCustomXmlFeed(jobBoardServiceURL, ThreadManager.getOwnerAlbatrossToken());

		JsonPath jsonFeeds = allCrudFunctions.getXmlFeedsList(jobBoardServiceURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();

		String xmlFeeds = jsonFeeds.getString("data.default_job_boards[1].id") + "-"
				+ jsonFeeds.getString("data.default_job_boards[1].jobboard_type") + ","
				+ jsonFeeds.getString("data.records.data[1].id") + "-"
				+ jsonFeeds.getString("data.records.data[1].jobboard_type");

		BulkUpdateXmlFeedField bulkUpdateXmlFeedField = new BulkUpdateXmlFeedField("xml_feeds", xmlFeeds, "jobboard_job_association_t", new ArrayList<>(Arrays.asList(jobId1, jobId2)), true);

		String basePath = "global/update-fields";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, bulkUpdateXmlFeedField);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.is("Field Updated Successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.xml_feeds", Matchers.is(xmlFeeds));
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider= "searchJobTestData", groups = "nightly-build")
	public void searchJob_Test(String jobSlug, String jobName) {
		SearchEntity searchJob = new SearchEntity(jobName,  false, false, true, false, false, false);
		Response response = RestClient.doPost("JSON", albatrossURL, "global/search-entity", albatrossAuthToken, null, true,
				searchJob);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getList("data").size(), 1);
		Assert.assertEquals(jsonPath.getString("data[0].title"), jobName);
		Assert.assertEquals(jsonPath.getString("data[0].slug"), jobSlug);
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void searchJobWithInvalidData_Test( ) {
		SearchEntity searchJob = new SearchEntity("abc", false, false, true, false, false, false);
		Response response = RestClient.doPost("JSON", albatrossURL, "global/search-entity", albatrossAuthToken, null, true,
				searchJob);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getList("data").size(), 0);
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotSearchJob_Test( ) {
		SearchEntity searchJob = new SearchEntity("abc",  false, false, true, false, false, false);
		Response response = RestClient.doPost("JSON", albatrossURL, "global/search-entity", ThreadManager.getOwnerAlbatrossToken()+"ab", null, true,
				searchJob);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.get("error"), "Unauthorized");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "mergeJobTestData", groups = "nightly-build")
	public void checkJobMergeDuplicate_Test(Integer primaryJobId, Integer secondaryJobId){
		CheckMergeDuplicate checkMergeDuplicate = new CheckMergeDuplicate();
		checkMergeDuplicate.setPrimaryJobId(primaryJobId);
		checkMergeDuplicate.setSecondaryJobId(secondaryJobId);
		Response response = RestClient.doPost("JSON", albatrossURL, "jobs/check-merge-duplicates", ThreadManager.getOwnerAlbatrossToken(), null, true,
				checkMergeDuplicate);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message"), "Both jobs are ready to be merged");
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void checkJobMergeDuplicateWithInvalidJobId_Test(){
		CheckMergeDuplicate checkMergeDuplicate = new CheckMergeDuplicate();
		checkMergeDuplicate.setPrimaryJobId(11010);
		checkMergeDuplicate.setSecondaryJobId(231313);
		Response response = RestClient.doPost("JSON", albatrossURL, "jobs/check-merge-duplicates", ThreadManager.getOwnerAlbatrossToken(), null, true,
				checkMergeDuplicate);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message"), "Job(s) not found");
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotCheckJobMergeDuplicate_Test(){
		CheckMergeDuplicate checkMergeDuplicate = new CheckMergeDuplicate();
		checkMergeDuplicate.setPrimaryJobId(1);
		checkMergeDuplicate.setSecondaryJobId(2);
		Response response = RestClient.doPost("JSON", albatrossURL, "jobs/check-merge-duplicates", ThreadManager.getOwnerAlbatrossToken()+"ab", null, true,
				checkMergeDuplicate);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.get("error"), "Unauthorized");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "mergeJobTestData", groups = "nightly-build")
	public void mergeJob_Test(Integer primaryJobId, Integer secondaryJobId) {
		MergeDuplicates mergeDuplicates = new MergeDuplicates();
		mergeDuplicates.setSelectedEntities(new ArrayList<>(Arrays.asList(secondaryJobId)));
		mergeDuplicates.setEntityTypeId(4);
		mergeDuplicates.setMergeTo(primaryJobId);
		Response response = RestClient.doPost("JSON", albatrossURL, "merge-duplicates", ThreadManager.getOwnerAlbatrossToken(), null, true,
				mergeDuplicates);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message"), "You will receive a notification once the merging process is complete.");
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void mergeJobWithEmptyRequestBody_Test(){
		MergeDuplicates mergeDuplicates = new MergeDuplicates();
		mergeDuplicates.setSelectedEntities(new ArrayList<>(Arrays.asList()));
		mergeDuplicates.setEntityTypeId(4);
		mergeDuplicates.setMergeTo(0);
		Response response = RestClient.doPost("JSON", albatrossURL, "merge-duplicates", ThreadManager.getOwnerAlbatrossToken(), null, true,
				mergeDuplicates);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.get("message"), "The selected entities field is required.");
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void  unautorizedUserCannotMergeJob_Test(){
		MergeDuplicates mergeDuplicates = new MergeDuplicates();
		mergeDuplicates.setSelectedEntities(new ArrayList<>(Arrays.asList()));
		mergeDuplicates.setEntityTypeId(4);
		mergeDuplicates.setMergeTo(0);
		Response response = RestClient.doPost("JSON", albatrossURL, "merge-duplicates", ThreadManager.getOwnerAlbatrossToken()+"ab", null, true,
				mergeDuplicates);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.get("error"), "Unauthorized");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void editClosedJobTest() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().getString("slug");
		
		Job job = new Job();
		JavaFakerJob jobFaker = new JavaFakerJob();
		job.setName(jobFaker.getJobName());
		job.setCompany_slug(companySlug);
		job.setContact_slug(contactSlug);
		job.setNumber_of_openings(jobFaker.getOpenings());
		job.setJob_status("0");
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		
		Response jobCreationResponse = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);
		JsonPath jobCreationJsonPath = jobCreationResponse.jsonPath();
		String jobSlug = jobCreationJsonPath.getString("slug");
		String originalJobName = jobCreationJsonPath.getString("name");

		String getJobPath = "jobs/" + jobSlug + "/get";
		Response getJobResponse = RestClient.doPost("JSON", albatrossURL, getJobPath, ThreadManager.getOwnerAlbatrossToken(), null, true, null);
		
		JsonPath getJobJsonPath = getJobResponse.jsonPath();
		
		int companyId = getJobJsonPath.getInt("data.job.companyid");
		int contactId = getJobJsonPath.getInt("data.job.contactid");
		int ownerId = getJobJsonPath.getInt("data.job.ownerid");
		int jobId = getJobJsonPath.getInt("data.job.id");
		
		JobUpdateData jobData = JobUpdateData.builder()
				.slug(jobSlug)
				.name("Updated " + originalJobName)
				.description("")
				.noofopenings(5)
				.qualificationid(0)
				.specialization("")
				.minexperienceinyears(0)
				.maxexperienceinyears(0)
				.annualsalarymin(0)
				.annualsalarymax(0)
				.salarytype(jobFaker.getSalaryTypeAsString())
				.job_type("")
				.locality("")
				.city("")
				.country("")
				.postalcode(null)
				.state("")
				.address("")
				.currencyid(0)
				.companyid(companyId)
				.contactid(contactId)
				.ownerid(ownerId)
				.id(jobId)
				.build();

		UpdateJobRequest updateJobRequest = UpdateJobRequest.builder()
				.job(jobData)
				.address_changed(false)
				.filesInfo(new Object[]{})
				.deleteJobKey("")
				.secondaryContacts(new Object[]{})
				.xml_feeds(new Object[]{})
				.jobParserData(new Object[]{})
				.collaborator(null)
				.build();

		String basePath = "jobs/" + jobSlug;
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, updateJobRequest);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message_type", Matchers.is("is-success"));

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("data.job.name"), "Updated " + originalJobName);
		Assert.assertEquals(jsonPath.getInt("data.job.noofopenings"), 5);
		Assert.assertEquals(jsonPath.getInt("data.job.jobstatus"), 0);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void editArchivedJobTest() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
				.jsonPath().getString("slug");
		
		Job job = new Job();
		JavaFakerJob jobFaker = new JavaFakerJob();
		job.setName(jobFaker.getJobName());
		job.setCompany_slug(companySlug);
		job.setContact_slug(contactSlug);
		job.setNumber_of_openings(jobFaker.getOpenings());
		job.setJob_status("0");
		job.setJob_description_text(jobFaker.getJobDescriptionText());
		
		Response jobCreationResponse = RestClient.doPost("JSON", baseURL, "jobs", ThreadManager.getAccountApiKey(), null, true, job);
		JsonPath jobCreationJsonPath = jobCreationResponse.jsonPath();
		String jobSlug = jobCreationJsonPath.getString("slug");
		String originalJobName = jobCreationJsonPath.getString("name");

		String getJobPath = "jobs/" + jobSlug + "/get";
		Response getJobResponse = RestClient.doPost("JSON", albatrossURL, getJobPath, ThreadManager.getOwnerAlbatrossToken(), null, true, null);
		
		JsonPath getJobJsonPath = getJobResponse.jsonPath();
		
		int companyId = getJobJsonPath.getInt("data.job.companyid");
		int contactId = getJobJsonPath.getInt("data.job.contactid");
		int ownerId = getJobJsonPath.getInt("data.job.ownerid");
		int jobId = getJobJsonPath.getInt("data.job.id");
		
		Map<String, Object> archiveRequest = new HashMap<>();
		archiveRequest.put("key", "archived");
		archiveRequest.put("value", 1);
		archiveRequest.put("tableFlag", "job");
		archiveRequest.put("id", Arrays.asList(jobId));
		
		String archivePath = "global/update-fields";
		RestClient.doPost("JSON", albatrossURL, archivePath, ThreadManager.getOwnerAlbatrossToken(), null, true, archiveRequest);
	
		JobUpdateData jobData = JobUpdateData.builder()
				.slug(jobSlug)
				.name("Updated " + originalJobName)
				.description("")
				.noofopenings(5)
				.qualificationid(0)
				.specialization("")
				.minexperienceinyears(0)
				.maxexperienceinyears(0)
				.annualsalarymin(0)
				.annualsalarymax(0)
				.salarytype(jobFaker.getSalaryTypeAsString())
				.job_type("")
				.locality("")
				.city("")
				.country("")
				.postalcode(null)
				.state("")
				.address("")
				.currencyid(0)
				.companyid(companyId)
				.contactid(contactId)
				.ownerid(ownerId)
				.id(jobId)
				.build();

		UpdateJobRequest updateJobRequest = UpdateJobRequest.builder()
				.job(jobData)
				.address_changed(false)
				.filesInfo(new Object[]{})
				.deleteJobKey("")
				.secondaryContacts(new Object[]{})
				.xml_feeds(new Object[]{})
				.jobParserData(new Object[]{})
				.collaborator(null)
				.build();

		String basePath = "jobs/" + jobSlug;
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, updateJobRequest);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message_type", Matchers.is("is-success"));

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("data.job.name"), "Updated " + originalJobName);
		Assert.assertEquals(jsonPath.getInt("data.job.noofopenings"), 5);
		Assert.assertEquals(jsonPath.getInt("data.job.jobstatus"), 0);
	}

	@DataProvider
	public Object[][] searchJobTestData() {
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String companySlug = jsonCompany.get("slug");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");
		JsonPath primaryJob = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug).jsonPath();
		String primaryJobSlug = primaryJob.getString("slug");
		String primaryJobName = primaryJob.getString("name");
		return new Object[][] { { primaryJobSlug,primaryJobName} };
	}

	@DataProvider
	public Object[][] mergeJobTestData(){
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
		String companySlug = jsonCompany.get("slug");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");
		JsonPath primaryJob = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug).jsonPath();
		JsonPath secondaryJob = function.createNewJob(baseURL, accountAPIKey, companySlug, contactSlug).jsonPath();


		String primaryJobSlug = primaryJob.getString("slug");
		String secondaryJobSlug = secondaryJob.getString("slug");
		int primaryJobId = function.getEntityDetail(albatrossURL, albatrossAuthToken.toString(), primaryJobSlug, "job").jsonPath()
				.getInt("data.job.id");

		int secondaryJobId = function.getEntityDetail(albatrossURL, albatrossAuthToken.toString(), secondaryJobSlug, "job").jsonPath()
				.getInt("data.job.id");
		return new Object[][] { { primaryJobId,secondaryJobId } };
	}

	public Integer getJobId(String jobName){
		SearchEntity searchSecondaryJob = new SearchEntity(jobName, false, false, true, false, false, false);
		JsonPath secondaryJobJson = RestClient.doPost("JSON", albatrossURL, "global/search-entity", albatrossAuthToken, null, true,
				searchSecondaryJob).jsonPath();
		return secondaryJobJson.getInt("data[0].id");
	}

}