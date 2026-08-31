package io.recruitcrm.report;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

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
import io.rcrm.api.pojo.albatross.targetReports.*;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TargetReportsTest extends TestBase {

	commanFunction function = new commanFunction();
	JavaFakerTargetReports faker = new JavaFakerTargetReports();
	Object albatrossTkn;
	Object accountAPIKey;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
		accountAPIKey = ThreadManager.getAccountApiKey();
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void fetchTargetTitle_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		String basePath = "reports/fetch-target-title";

		Response response = RestClient.doGet("JSON", reportServiceURL, basePath, albatrossTkn, null, null, true);

		response.then().statusCode(200);

		response.then().body("[0].id", Matchers.is(targetReportId));
		response.then().body("[0].title", Matchers.is(targetReportName));
		response.then().body("[0].assignee_id", Matchers.is(accountOwnerId));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//targetReports//fetchTarget.json"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void fetchTargetTitleWithEmptyData_Test() {

		String basePath = "reports/fetch-target-title";

		Response response = RestClient.doGet("JSON", reportServiceURL, basePath, albatrossTkn, null, null, true);

		response.then().statusCode(200);

		response.then().body("$", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void fetchTargetTitleWithInvalidRequestType_Test() {

		String basePath = "reports/fetch-target-title";

		Response response = RestClient.doPost1("JSON", reportServiceURL, basePath, albatrossTkn, null, null, true, null);

		response.then().statusCode(405);

		response.then().body("message", Matchers.is(""));
		response.then().body("exception", Matchers.is("Symfony\\Component\\HttpKernel\\Exception\\MethodNotAllowedHttpException"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void fetchTargetTitleWithUnauthorizedAccess_Test() {

		String basePath = "reports/fetch-target-title";

		Response response = RestClient.doGet("JSON", reportServiceURL, basePath, albatrossTkn + "123", null, null, true);

		response.then().statusCode(500);
		response.then().body("message", Matchers.containsString("App\\Helpers\\AccessControl"));
		response.then().body("exception", Matchers.is("TypeError"));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void getTargetReportData_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		TargetReportData targetReport = new TargetReportData();
		targetReport.setTargetId(targetReportId);

		String basePath = "reports/get-report-data";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true,	targetReport);

		response.then().statusCode(200);

		JsonPath jsonPath = response.jsonPath();
		String frequency = jsonPath.getString("[0].frequency");
		List<String> validFrequencies = Arrays.asList("Daily", "Weekly", "Monthly", "Quarterly", "Yearly");

		response.then().body("[0].created_by", Matchers.notNullValue());
		Assert.assertTrue(validFrequencies.contains(frequency), frequency + " not found");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getTargetReportDataWithInvalidId_Test() {

		TargetReportData targetReport = new TargetReportData();

		targetReport.setTargetId(faker.getRandomTargetId());

		String basePath = "reports/get-report-data";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true,	targetReport);

		response.then().statusCode(200);
		response.then().body("$", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getTargetReportDataWithEmptyData_Test() {

		TargetReportData targetReport = new TargetReportData();

		String basePath = "reports/get-report-data";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true,	targetReport);

		response.then().statusCode(200);
		response.then().body("$", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getTargetReportDataWithUnauthorizedAccess_Test() {

		TargetReportData targetReport = new TargetReportData();
		targetReport.setTargetId(faker.getRandomTargetId());

		String basePath = "reports/get-report-data";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn + "123", null, true, targetReport);

		response.then().statusCode(200);
		response.then().body("$", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void targetReport_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		String kpiLabel = faker.getKPILabel();
		String kpiValue = faker.getKPIValue(kpiLabel);
		String targetValue = faker.getKPICount();
		String interval = faker.getFrequency();
		long fromDate = faker.getStartDate(3);
		long toDate = faker.getEndDate(3);

		List<RecruiterTargetReport.Kpi> kpiLists = Collections.singletonList(new RecruiterTargetReport.Kpi(kpiLabel, kpiValue, targetValue, true));

		RecruiterTargetReport kpiRequest = new RecruiterTargetReport();
		kpiRequest.setRecruiter_ids(Collections.singletonList(Integer.parseInt(accountOwnerId)));
		kpiRequest.setKpi_lists(kpiLists);
		kpiRequest.setFrom_date(fromDate);
		kpiRequest.setTo_date(toDate);
		kpiRequest.setTeam_ids(Collections.emptyList());
		kpiRequest.setRole_ids(Collections.emptyList());
		kpiRequest.setCompany_wide(false);
		kpiRequest.setInterval(interval);
		kpiRequest.setTargetId(targetReportId);
		kpiRequest.setTeam_selected(false);
		kpiRequest.setTeammate_selected(true);
		kpiRequest.setKpis_included(kpiLists);
		kpiRequest.setStarted_on(fromDate);
		kpiRequest.setEnded_on(toDate);

		String basePath = "reports/target-report";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true, kpiRequest);

		response.then().statusCode(200);
		response.then().body("chart_data.series_data[0].seriesname", Matchers.is("Below Target"));
		response.then().body("chart_data.series_data[1].seriesname", Matchers.is("Above Target"));
		response.then().body("data." + accountOwnerId, Matchers.notNullValue());
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void targetReportWithInvalidId_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		String kpiLabel = faker.getKPILabel();
		String kpiValue = faker.getKPIValue(kpiLabel);
		String targetValue = faker.getKPICount();
		String interval = faker.getFrequency();
		long fromDate = faker.getStartDate(3);
		long toDate = faker.getEndDate(3);

		List<RecruiterTargetReport.Kpi> kpiLists = Collections.singletonList(new RecruiterTargetReport.Kpi(kpiLabel, kpiValue, targetValue, true));

		RecruiterTargetReport kpiRequest = new RecruiterTargetReport();
		kpiRequest.setRecruiter_ids(Collections.singletonList(Integer.parseInt(faker.getAssigneeId())));
		kpiRequest.setKpi_lists(kpiLists);
		kpiRequest.setFrom_date(fromDate);
		kpiRequest.setTo_date(toDate);
		kpiRequest.setTeam_ids(Collections.emptyList());
		kpiRequest.setRole_ids(Collections.emptyList());
		kpiRequest.setInterval(interval);
		kpiRequest.setTargetId(targetReportId);
		kpiRequest.setKpis_included(kpiLists);
		kpiRequest.setStarted_on(fromDate);
		kpiRequest.setEnded_on(toDate);

		String basePath = "reports/target-report";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true, kpiRequest);

		response.then().statusCode(200);
		response.then().body("data", Matchers.empty());
		response.then().body("chart_data", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void targetReportWithEmptyData_Test() {

		RecruiterTargetReport targetReport = new RecruiterTargetReport();

		String basePath = "reports/target-report";

		Response response = RestClient.doPostOnce("JSON", reportServiceURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(422);

		response.then().body("recruiter_ids[0]", Matchers.is("The recruiter ids must be an array."));
		response.then().body("team_ids[0]", Matchers.is("The team ids must be an array."));
		response.then().body("role_ids[0]", Matchers.is("The role ids must be an array."));
		response.then().body("kpi_lists[0]", Matchers.is("The kpi lists must be an array."));
		response.then().body("kpis_included[0]", Matchers.is("The kpis included must be an array."));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void targetReportWithUnauthorizedAccess_Test() {

		RecruiterTargetReport targetReport = new RecruiterTargetReport();

		String basePath = "reports/target-report";

		Response response = RestClient.doPostOnce("JSON", reportServiceURL, basePath, albatrossTkn + "123", null, true, targetReport);

		response.then().body("recruiter_ids[0]", Matchers.is("The recruiter ids must be an array."));
		response.then().body("team_ids[0]", Matchers.is("The team ids must be an array."));
		response.then().body("role_ids[0]", Matchers.is("The role ids must be an array."));
		response.then().body("kpi_lists[0]", Matchers.is("The kpi lists must be an array."));
		response.then().body("kpis_included[0]", Matchers.is("The kpis included must be an array."));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void shareTargetReport_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		ShareTargetReport targetReport = new ShareTargetReport();
		targetReport.setReport_shared(true);
		targetReport.setTarget_user_preference_id(targetReportId);
		targetReport.setAuto_refresh(true);
		targetReport.setRefresh_time(faker.getRefreshTime());

		String basePath = "reports/targets-share";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		response.then().body("message", Matchers.is("Report sharing updated successfully."));
		response.then().body("status", Matchers.is(200));
		response.then().body("share_url", Matchers.containsString("https://" + System.getProperty("envname") + ".recruitcrm.net/v1/external-target-reports"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void shareTargetReportWithInvalidId_Test() {

		ShareTargetReport targetReport = new ShareTargetReport();
		targetReport.setReport_shared(true);
		targetReport.setTarget_user_preference_id(faker.getRandomTargetId());
		targetReport.setAuto_refresh(true);
		targetReport.setRefresh_time(faker.getRefreshTime());

		String basePath = "reports/targets-share";

		Response response = RestClient.doPostOnce("JSON", reportServiceURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(500);
		response.then().body("message", Matchers.is("Cannot use object of type Illuminate\\Http\\JsonResponse as array"));
		response.then().body("exception", Matchers.is("Error"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void shareTargetReportWithEmptyData_Test() {

		ShareTargetReport targetReport = new ShareTargetReport();

		String basePath = "reports/targets-share";

		Response response = RestClient.doPostOnce("JSON", reportServiceURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(500);

		response.then().body("message", Matchers.is("Cannot use object of type Illuminate\\Http\\JsonResponse as array"));
		response.then().body("exception", Matchers.is("Error"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void shareTargetReportWithUnauthorizedAccess_Test() {

		ShareTargetReport targetReport = new ShareTargetReport();

		String basePath = "reports/targets-share";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn + "123", null, true, targetReport);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Invalid authcode or unauthorized user"));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void targetReportTable_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		String kpiLabel = faker.getKPILabel();
		String kpiValue = faker.getKPIValue(kpiLabel);
		String targetValue = faker.getKPICount();
		String interval = faker.getFrequency();
		long fromDate = faker.getStartDate(3);
		long toDate = faker.getEndDate(3);

		List<TargetReportTable.KPI> kpiLists = Collections.singletonList(new TargetReportTable.KPI(kpiValue, kpiLabel, true, true, targetValue));

		TargetReportTable targetReport = new TargetReportTable();
		targetReport.setRecruiter_ids(Collections.singletonList(Integer.parseInt(accountOwnerId)));
		targetReport.setStarted_on(fromDate);
		targetReport.setEnded_on(toDate);
		targetReport.setKpi_lists(kpiLists);
		targetReport.setInterval(interval);
		targetReport.setTargetId(targetReportId);
		targetReport.setTeam_ids(Collections.emptyList());
		targetReport.setRole_ids(Collections.emptyList());
		targetReport.setFrom_date(fromDate);
		targetReport.setTo_date(toDate);

		String basePath = "reports/target-report-table";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);
		response.then().body("chart_data.series_data[0].seriesname", Matchers.is("Below Target"));
		response.then().body("chart_data.series_data[1].seriesname", Matchers.is("Above Target"));
		response.then().body("data." + accountOwnerId, Matchers.notNullValue());
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void targetReportTableWithInvalidInterval_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		String kpiLabel = faker.getKPILabel();
		String kpiValue = faker.getKPIValue(kpiLabel);
		String targetValue = faker.getKPICount();
		String interval = faker.getFrequency();
		long fromDate = faker.getStartDate(3);
		long toDate = faker.getEndDate(3);

		List<RecruiterTargetReport.Kpi> kpiLists = Collections.singletonList(new RecruiterTargetReport.Kpi(kpiLabel, kpiValue, targetValue, true));

		RecruiterTargetReport kpiRequest = new RecruiterTargetReport();
		kpiRequest.setRecruiter_ids(Collections.singletonList(Integer.parseInt(faker.getAssigneeId())));
		kpiRequest.setKpi_lists(kpiLists);
		kpiRequest.setTeam_ids(Collections.emptyList());
		kpiRequest.setRole_ids(Collections.emptyList());
		kpiRequest.setInterval(interval);
		kpiRequest.setTargetId(targetReportId);
		kpiRequest.setKpis_included(kpiLists);
		kpiRequest.setStarted_on(fromDate);
		kpiRequest.setEnded_on(toDate);

		String basePath = "reports/target-report-table";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true, kpiRequest);

		response.then().statusCode(200);
		response.then().body("data", Matchers.empty());
		response.then().body("chart_data", Matchers.empty());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void targetReportTableWithEmptyData_Test() {

		RecruiterTargetReport targetReport = new RecruiterTargetReport();

		String basePath = "reports/target-report-table";

		Response response = RestClient.doPostOnce("JSON", reportServiceURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(422);

		response.then().body("kpi_lists[0]", Matchers.is("The kpi lists must be an array."));
		response.then().body("team_ids[0]", Matchers.is("The team ids must be an array."));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void targetReportTableWithUnauthorizedAccess_Test() {

		RecruiterTargetReport targetReport = new RecruiterTargetReport();

		String basePath = "reports/target-report-table";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn + "123", null, true, targetReport);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Invalid authcode or unauthorized user"));
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void updateTargetReportEmailNotification_Test(int targetReportId, String targetReportName, String accountOwnerId, TargetReport.KpiList kpiList) throws JsonProcessingException {

		TargetReportEmailNotification targetReport = new TargetReportEmailNotification();
		targetReport.setTarget_id(targetReportId);
		targetReport.setEmail_notification_status("1");

		String basePath = "reports/update-email-notfication";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		response.then().body("message", Matchers.is("Report Email Notification Status updated successfully."));
		response.then().body("status", Matchers.is(200));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void updateTargetReportEmailNotificationWithInvalidId_Test() {

		TargetReportEmailNotification targetReport = new TargetReportEmailNotification();
		targetReport.setTarget_id(faker.getRandomTargetId());
		targetReport.setEmail_notification_status(faker.getNotificationStatus());

		String basePath = "reports/update-email-notfication";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true, targetReport);

		response.then().statusCode(200);

		response.then().body("message", Matchers.is("Report Email Notification Status updated successfully."));
		response.then().body("status", Matchers.is(200));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void updateTargetReportEmailNotificationWithEmptyData_Test() {

		TargetReportEmailNotification targetReport = new TargetReportEmailNotification();

		String basePath = "reports/update-email-notfication";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn, null, true,	targetReport);

		response.then().statusCode(200);

		response.then().body("message", Matchers.is("Report Email Notification Status updated successfully."));
		response.then().body("status", Matchers.is(200));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void updateTargetReportEmailNotificationWithUnauthorizedAccess_Test() {

		TargetReportEmailNotification targetReport = new TargetReportEmailNotification();

		String basePath = "reports/update-email-notfication";

		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, albatrossTkn + "123", null, true,	targetReport);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Invalid authcode or unauthorized user"));
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
