package io.recruitcrm.albatross.account;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qa.api.util.reaper.ThreadManager;

import org.testng.Assert;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.albatross.JavaFakerTargetReports;
import io.rcrm.api.pojo.albatross.GetEntityColumns;
import io.rcrm.api.pojo.albatross.targetReports.*;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TargetReportsCURDTest extends TestBase {

	commanFunction function = new commanFunction();
	JavaFakerTargetReports faker = new JavaFakerTargetReports();
	Object albatrossTkn;
	Object accountAPIKey;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
		accountAPIKey = ThreadManager.getAccountApiKey();
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createTargetReport_Test() throws JsonProcessingException {
		
		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		JsonPath user = userResponse.jsonPath();
		String accountOwnerid = user.getString("[0].id");
		String accountOwnerName = user.get("[0].first_name") + " " + user.get("[0].last_name");
		String kpiLabel = faker.getKPILabel();
		String targetReportName = faker.getTargetReportName();

		List<TargetReport.Recruiter> recruiters = Collections
				.singletonList(new TargetReport.Recruiter(accountOwnerid, accountOwnerName, true, true));
		List<String> recruiterTeams = Collections.emptyList();
		List<String> roles = Collections.emptyList();
		List<TargetReport.Kpi> kpis = Collections.singletonList(new TargetReport.Kpi(faker.getKPIValue(kpiLabel), kpiLabel, true, true, faker.getKPICount()));
		TargetReport.KpiList kpiList = new TargetReport.KpiList(recruiters, recruiterTeams, roles, kpis);

		TargetReport targetReport = new TargetReport();
		targetReport.setTitle(targetReportName);
		targetReport.setAssignee_type(faker.getAssigneeType());
		targetReport.setAssignee_id(accountOwnerid);
		targetReport.setFrequency(faker.getFrequency());
		targetReport.setStart_date(faker.getStartDate(3));
		targetReport.setEnd_date(faker.getEndDate(3));
		targetReport.setKpiListObject(kpiList);

		String basePath = "target-reports/create";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);
		response.then().body("message", Matchers.is(targetReportName + " was created"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.title", Matchers.is(targetReportName));
		response.then().body("data.assignee_id", Matchers.is(accountOwnerid));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createTargetReportWithEmptyData_Test() {

		TargetReport targetReport = new TargetReport();

		String basePath = "target-reports/create";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);
		JsonPath jsonPath = response.jsonPath();

		response.then().statusCode(422);

		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("data", Matchers.empty());

		String message = jsonPath.getString("message");

		List<String> expectedErrors = Arrays.asList(
				"The title must be a string.", 
				"The title field is required.",
				"The assignee type field is required.", 
				"The assignee id must be a string.",
				"The frequency field is required.", 
				"The kpi list must be a string.", 
				"The kpi list field is required.",
				"The start date must be greater than 0.", 
				"The end date must be greater than 0.");
		List<String> actualErrors = Arrays.asList(message.split(","));
		for (String expectedError : expectedErrors) {
			Assert.assertTrue(actualErrors.contains(expectedError), "Missing error: " + expectedError);
		}
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createTargetReportWithRandomData_Test() {

		TargetReport targetReport = new TargetReport();
		targetReport.setTitle(faker.getTargetReportName());
		targetReport.setAssignee_type(faker.getAssigneeType());
		targetReport.setAssignee_id(faker.getAssigneeId());
		targetReport.setFrequency(faker.getFrequency());
		targetReport.setStart_date(faker.getStartDate(3));
		targetReport.setEnd_date(faker.getEndDate(3));

		String basePath = "target-reports/create";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(422);

		response.then().body("message", Matchers.is("The kpi list must be a string.,The kpi list field is required."));
		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("data", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createTargetReportWithInvalidStartDateAndEndDate_Test() throws JsonProcessingException {

		TargetReport targetReport = new TargetReport();
		long startDate = faker.getEndDate(3);
		TargetReport.KpiList kpiList = new TargetReport.KpiList();

		targetReport.setTitle(faker.getTargetReportName());
		targetReport.setAssignee_type(faker.getAssigneeType());
		targetReport.setAssignee_id(faker.getAssigneeId());
		targetReport.setFrequency(faker.getFrequency());
		targetReport.setStart_date(startDate);
		targetReport.setEnd_date(faker.getStartDate(3));
		targetReport.setKpiListObject(kpiList);

		String basePath = "target-reports/create";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(422);

		response.then().body("message", Matchers.is("The end date must be greater than or equal to " + startDate + "."));
		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("data", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createTargetReportWithInvalidAssigneeAndFrequency_Test() throws JsonProcessingException {

		TargetReport targetReport = new TargetReport();

		TargetReport.KpiList kpiList = new TargetReport.KpiList();

		targetReport.setTitle(faker.getTargetReportName());
		targetReport.setAssignee_type(faker.getFrequency());
		targetReport.setAssignee_id(faker.getAssigneeId());
		targetReport.setFrequency(faker.getAssigneeType());
		targetReport.setStart_date(faker.getStartDate(3));
		targetReport.setEnd_date(faker.getEndDate(3));
		targetReport.setKpiListObject(kpiList);

		String basePath = "target-reports/create";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(422);

		response.then().body("message", Matchers.is("The selected assignee type is invalid.,The selected frequency is invalid."));
		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("data", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createTargetReportWithUnauthorizedAccess_Test() {

		TargetReport targetReport = new TargetReport();

		String basePath = "target-reports/create";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn + "123", null, true, targetReport);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void updateTargetReport_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		String updatedTargetReportName = targetReportName + " " + faker.getFrequency();

		TargetReport targetReport = new TargetReport();
		targetReport.setId(targetReportId);
		targetReport.setTitle(updatedTargetReportName);
		targetReport.setAssignee_type(faker.getAssigneeType());
		targetReport.setAssignee_id(accountOwnerId);
		targetReport.setFrequency(faker.getFrequency());
		targetReport.setStart_date(faker.getStartDate(3));
		targetReport.setEnd_date(faker.getEndDate(3));
		targetReport.setKpiListObject(kpiList);

		String basePath = "target-reports/update";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);
		response.then().body("message", Matchers.is(updatedTargetReportName + " was updated"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.title", Matchers.is(updatedTargetReportName));
		response.then().body("data.assignee_id", Matchers.is(accountOwnerId));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void updateTargetReportWithInvalidId_Test(int targetReportId, String targetReportName, String accountOwnerId,
			TargetReport.KpiList kpiList) throws JsonProcessingException {

		TargetReport targetReport = new TargetReport();
		targetReport.setId(targetReportId);
		targetReport.setTitle(faker.getTargetReportName());
		targetReport.setAssignee_type(faker.getAssigneeType());
		targetReport.setAssignee_id(accountOwnerId);
		targetReport.setFrequency(faker.getFrequency());
		targetReport.setStart_date(faker.getStartDate(3));
		targetReport.setEnd_date(faker.getEndDate(3));
		targetReport.setKpiListObject(kpiList);
		targetReport.setId(faker.getRandomTargetId());

		String basePath = "target-reports/update";

		Response response = RestClient.doPostOnce("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		response.then().body("message", Matchers.is("Target report not found"));
		response.then().body("message_type", Matchers.is("is-danger"));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void updateTargetReportWithEmptyData_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		TargetReport targetReport = new TargetReport();
		targetReport.setId(targetReportId);
		targetReport.setAssignee_type(faker.getAssigneeType());
		targetReport.setAssignee_id(accountOwnerId);
		targetReport.setFrequency(faker.getFrequency());
		targetReport.setStart_date(faker.getStartDate(3));
		targetReport.setEnd_date(faker.getEndDate(3));
		targetReport.setKpiListObject(kpiList);
		targetReport.setId(faker.getRandomTargetId());

		String basePath = "target-reports/update";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(422);

		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("data", Matchers.empty());
		response.then().body("message", Matchers.is("The title must be a string.,The title field is required."));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void updateTargetReportWithUnauthorizedAccess_Test() {

		TargetReport targetReport = new TargetReport();

		String basePath = "target-reports/update";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn + "123", null, true,	targetReport);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void deleteTargetReport_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		DeleteTargetReport targetReport = new DeleteTargetReport();
		targetReport.setIdsToDelete(Collections.singletonList(targetReportId));
		targetReport.setSlugsToDelete(Collections.singletonList(null));
		targetReport.setTableFlag("target_reports");

		String basePath = "global/delete-record";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		response.then().body("message", Matchers.is("Delete Target Successful"));
		response.then().body("data.id", Matchers.is(1));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void deleteTargetReportWithInvalidId_Test() {

		DeleteTargetReport targetReport = new DeleteTargetReport();
		targetReport.setIdsToDelete(Collections.singletonList(faker.getRandomTargetId()));
		targetReport.setSlugsToDelete(Collections.singletonList(null));
		targetReport.setTableFlag("target_reports");

		String basePath = "global/delete-record";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		response.then().body("message", Matchers.is("Failed To Delete Target : You are not authorized to delete selected records."));
		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("data", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void deleteTargetReportWithEmptyData_Test() {

		DeleteTargetReport targetReport = new DeleteTargetReport();

		String basePath = "global/delete-record";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		response.then().body("message", Matchers.is("Record Deleted Successfully"));
		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("data", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void deleteTargetReportWithUnauthorizedAccess_Test() {

		DeleteTargetReport targetReport = new DeleteTargetReport();

		String basePath = "global/delete-record";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn + "123", null, true, targetReport);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void searchTargetReport_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		SearchTargetReport targetReport = new SearchTargetReport();
		targetReport.setPage_size(faker.getPageSize());
		targetReport.setPage("1");
		targetReport.setSort_by(faker.getSortBy());
		targetReport.setSortOrder(faker.getSortOrder());

		String basePath = "target-reports/search/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.total_count", Matchers.is(1));
		response.then().body("data.records[0].id", Matchers.is(targetReportId));
		response.then().body("data.records[0].title", Matchers.is(targetReportName));
		response.then().body("data.records[0].assignee_id", Matchers.is(accountOwnerId));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void searchTargetReportWithInvalidPage_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		SearchTargetReport targetReport = new SearchTargetReport();
		targetReport.setPage_size(faker.getPageSize());
		targetReport.setPage(faker.getPageNumber());
		targetReport.setSort_by(faker.getSortBy());
		targetReport.setSortOrder(faker.getSortOrder());

		String basePath = "target-reports/search/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.total_count", Matchers.is(1));
		response.then().body("data.records", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void searchTargetReportWithEmptyData_Test() {

		SearchTargetReport targetReport = new SearchTargetReport();

		String basePath = "target-reports/search/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.total_count", Matchers.is(0));
		response.then().body("data.records", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void searchTargetReportWithInvalidSort_Test() {

		SearchTargetReport targetReport = new SearchTargetReport();
		targetReport.setPage_size(faker.getPageSize());
		targetReport.setPage(faker.getPageNumber());
		targetReport.setSort_by(faker.getSortBy());
		targetReport.setSortOrder(faker.getTargetReportName());

		String basePath = "target-reports/search/get";

		Response response = RestClient.doPostOnce("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(500);

		response.then().body("message", Matchers.is("Order direction must be \"asc\" or \"desc\"."));
		response.then().body("exception", Matchers.is("InvalidArgumentException"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void searchTargetReportWithUnauthorizedAccess_Test() {

		SearchTargetReport targetReport = new SearchTargetReport();

		String basePath = "target-reports/search/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn + "123", null, true, targetReport);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getTargetReportEntityColumns_Test() throws JsonProcessingException {

		GetEntityColumns getEntityColumns = new GetEntityColumns();
		getEntityColumns.setEntity("target_reports");

		String basePath = "global/get-entity-columns";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, getEntityColumns);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.is("is_success"));
		response.then().body("data.columns.id.entity", Matchers.is("target_reports"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getTargetReportEntityColumnsWithEmptyData_Test() {

		GetEntityColumns getEntityColumns = new GetEntityColumns();

		String basePath = "global/get-entity-columns";

		Response response = RestClient.doPostOnce("JSON", albatrossURL, basePath, albatrossTkn, null, true, getEntityColumns);

		response.then().statusCode(422);

		response.then().body("message", Matchers.containsString("The entity must be a string."));
		response.then().body("data", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getTargetReportEntityColumnsWithRandomData_Test() {

		GetEntityColumns getEntityColumns = new GetEntityColumns();
		getEntityColumns.setEntity(faker.getTargetReportName());

		String basePath = "global/get-entity-columns";

		Response response = RestClient.doPostOnce("JSON", albatrossURL, basePath, albatrossTkn, null, true, getEntityColumns);

		response.then().statusCode(500);

		response.then().body("message", Matchers.containsString("Failed to open stream: No such file or directory"));
		response.then().body("exception", Matchers.is("ErrorException"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getTargetReportEntityColumnsWithUnauthorizedAccess_Test() {

		GetEntityColumns getEntityColumns = new GetEntityColumns();
		String basePath = "global/get-entity-columns";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn + "123", null, true,	getEntityColumns);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void archiveTargetReport_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		ArchiveTargetReport targetReport = new ArchiveTargetReport();
		targetReport.setKey("archived");
		targetReport.setValue(1);
		targetReport.setTableFlag("target_reports");
		targetReport.setId(Collections.singletonList(targetReportId));

		String basePath = "global/update-fields";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Targets Archived Successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void archiveTargetReportWithInvalidId_Test() {

		ArchiveTargetReport targetReport = new ArchiveTargetReport();
		targetReport.setKey("archived");
		targetReport.setValue(1);
		targetReport.setTableFlag("target_reports");
		targetReport.setId(Collections.singletonList(faker.getRandomTargetId()));

		String basePath = "global/update-fields";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Targets Archived Successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void archiveTargetReportWithEmptyData_Test() {

		ArchiveTargetReport targetReport = new ArchiveTargetReport();

		String basePath = "global/update-fields";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(422);

		response.then().body("message", Matchers.is("The key field is required.,The table flag field is required."));
		response.then().body("message_type", Matchers.is("is-danger"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void archiveTargetReportWithUnauthorizedAccess_Test() {

		ArchiveTargetReport targetReport = new ArchiveTargetReport();

		String basePath = "global/update-fields";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn + "123", null, true,	targetReport);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@DataProvider(parallel = true)
	public Object[][] getTargetReportData() throws JsonProcessingException {

		Response userResponse = function.getUsers(baseURL, accountAPIKey);
		JsonPath user = userResponse.jsonPath();
		String accountOwnerId = user.getString("[0].id");
		String accountOwnerName = user.get("[0].first_name") + " " + user.get("[0].last_name");
		String kpiLabel = faker.getKPILabel();
		String targetReportName = faker.getTargetReportName();

		List<TargetReport.Recruiter> recruiters = Collections.singletonList(new TargetReport.Recruiter(accountOwnerId, accountOwnerName, true, true));
		List<String> recruiterTeams = Collections.emptyList();
		List<String> roles = Collections.emptyList();
		List<TargetReport.Kpi> kpis = Collections.singletonList(new TargetReport.Kpi(faker.getKPIValue(kpiLabel), kpiLabel, true, true, faker.getKPICount()));

		TargetReport.KpiList kpiList = new TargetReport.KpiList(recruiters, recruiterTeams, roles, kpis);

		TargetReport targetReport = new TargetReport();
		targetReport.setTitle(targetReportName);
		targetReport.setAssignee_type(faker.getAssigneeType());
		targetReport.setAssignee_id(accountOwnerId);
		targetReport.setFrequency(faker.getFrequency());
		targetReport.setStart_date(faker.getStartDate(3));
		targetReport.setEnd_date(faker.getEndDate(3));
		targetReport.setKpiListObject(kpiList);

		String basePath = "target-reports/create";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		JsonPath json = response.jsonPath();
		int targetReportId = json.get("data.id");

		Object data[][] = { { targetReportId, targetReportName, accountOwnerId, kpiList } };

		return data;
	}

}
