package io.rcrm.api.targetReports;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.albatross.JavaFakerTargetReports;
import io.rcrm.api.pojo.albatross.targetReports.TargetReport;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetTargetReportsTest extends TestBase {

	commanFunction function = new commanFunction();
	JavaFakerTargetReports faker = new JavaFakerTargetReports();
	String basePath = "target-report/get";
	Object albatrossTkn;
	Object accountAPIKey;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
		accountAPIKey = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getTargetReportData", groups = "nightly-build")
	public void getTargetReport_Test(int targetReportId, String targetReportName, String accountOwnerId) {

		Response response = RestClient.doGet("JSON", baseURL, basePath, accountAPIKey, null, null, true);

		response.then().statusCode(200);

		response.then().body("total", Matchers.is(1));
		response.then().body("data[0].TargetId", Matchers.is(targetReportId));
		response.then().body("data[0].TargetName", Matchers.is(targetReportName));
		response.then().body("data[0].Assignees", Matchers.is(accountOwnerId));
		response.then().body("data[0].CreatedBy", Matchers.is(Integer.parseInt(accountOwnerId)));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//targetReports//getTarget.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getTargetReportWithEmptyData_Test() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, accountAPIKey, null, null, true);

		response.then().statusCode(200);

		response.then().body("data", Matchers.empty());
		response.then().body("total", Matchers.is(0));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getTargetReportWithInvalidRequestType_Test() {

		Response response = RestClient.doPost1("JSON", baseURL, basePath, accountAPIKey, null, null, true, null);

		response.then().statusCode(405);

		response.then().body("message", Matchers.is(""));
		response.then().body("exception", Matchers.is("Symfony\\Component\\HttpKernel\\Exception\\MethodNotAllowedHttpException"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getTargetReportWithUnauthorizedAccess_Test() {

		Response response = RestClient.doGet("JSON", baseURL, basePath, accountAPIKey + "123", null, null, true);

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

		Response response = RestClient.doPost("JSON", albatrossURL, "target-reports/create", albatrossTkn, null, true, targetReport);
		response.then().statusCode(200);

		JsonPath json = response.jsonPath();
		int targetReportId = json.get("data.id");

		Object data[][] = { { targetReportId, targetReportName, accountOwnerId } };

		return data;
	}

}