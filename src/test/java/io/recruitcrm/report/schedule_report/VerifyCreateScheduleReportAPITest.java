package io.recruitcrm.report.schedule_report;

import java.util.ArrayList;
import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.albatross.report.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.ScheduleReport.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.commanfunctions.reportService.ReportServiceFunctions;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class VerifyCreateScheduleReportAPITest extends TestBase {

    String reportID = "";
    ListFunctions listFunctions = new ListFunctions();
    ReportServiceFunctions reportServiceFunctions = new ReportServiceFunctions();
    JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
    JavaFakerScheduleReport scheduleReportFaker = new JavaFakerScheduleReport();
    String reportName = reportFaker.getReportName();
    String basePath = "schedule-report";
    private String token;
    
    @BeforeClass(alwaysRun = true)    public void setupPrerequisites() {
        token = ThreadManager.getOwnerAlbatrossToken();
        reportID = reportServiceFunctions.createReportPreference(baseURL, reportServiceURL, token, reportName);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getScheduleReportValidTestData", groups = "nightly-build")
    public void verifyCreateScheduleReportAPI(ArrayList<Integer> collaboratorTeamIds) {
        String body = scheduleReportFaker.getEmailBody();
        ScheduleReportRequest scheduleReportRequest = reportServiceFunctions.scheduleReportRequest(body, collaboratorTeamIds, true, reportID);
        Response response = RestClient.doPost("JSON", reportServiceURL, basePath, token, null, true, scheduleReportRequest);

        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("data.body"), is(body));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getScheduleReportValidTestData", groups = "nightly-build")
    public void verifyCreateScheduleReportAPI_Unauthorized(ArrayList<Integer> collaboratorTeamIds) {
        String body = scheduleReportFaker.getEmailBody();
        ScheduleReportRequest scheduleReportRequest = reportServiceFunctions.scheduleReportRequest(body, collaboratorTeamIds, true, reportID);
        Response response = RestClient.doPost("JSON", reportServiceURL, basePath, token + "Unauthorized", null, true, scheduleReportRequest);
        assertThat(response.getStatusCode(), is(401));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getScheduleReportValidTestData", groups = "nightly-build")
    public void verifyCreateScheduleReportAPI_InvalidReportId(ArrayList<Integer> collaboratorTeamIds) {
        String body = scheduleReportFaker.getEmailBody();
        ScheduleReportRequest scheduleReportRequest = reportServiceFunctions.scheduleReportRequest(body, collaboratorTeamIds, false, reportID);
        Response response = RestClient.doPost("JSON", reportServiceURL, basePath, token, null, true, scheduleReportRequest);

        if (response.getStatusCode() == 200)
            assertThat(response.jsonPath().get("status"), is("User Limit Reached"));
        else
            assertThat(response.getStatusCode(), is(500));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyCreateScheduleReportAPI_EmptyPayload() {
        Response response = RestClient.doPost("JSON", reportServiceURL, basePath, token, null, true, new ScheduleReportRequest());
        assertThat(response.getStatusCode(), is(500));
    }

    @DataProvider
    public Object[][] getScheduleReportValidTestData() {
        JsonPath jsonGetCollabrators = listFunctions.getAllCollabrators(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        ArrayList<Integer> collaboratorTeamIds = jsonGetCollabrators.get("id");
        return new Object[][] { { collaboratorTeamIds } };
    }
}
