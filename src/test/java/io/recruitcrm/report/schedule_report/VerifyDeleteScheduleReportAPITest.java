package io.recruitcrm.report.schedule_report;

import java.util.*;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.rcrm.api.javafaker.albatross.report.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.commanfunctions.reportService.ReportServiceFunctions;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class VerifyDeleteScheduleReportAPITest extends TestBase {

    String reportID = "";
    String scheduleReportIDForDeletion = "";
    ReportServiceFunctions reportServiceFunctions = new ReportServiceFunctions();
    JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
    ArrayList<String> scheduleReportIDs = new ArrayList<>();
    String reportName = reportFaker.getReportName();
    String basePath = "schedule-report";
    private String token;

    @BeforeClass(alwaysRun = true)    public void setupPrerequisites() {
        token = ThreadManager.getOwnerAlbatrossToken();
        reportID = reportServiceFunctions.createReportPreference(baseURL, reportServiceURL, token, reportName);
        scheduleReportIDForDeletion = reportServiceFunctions.createMultipleScheduleReports(1, reportID, reportServiceURL, token).get(0);
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyDeleteScheduleReportAPI() {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", scheduleReportIDForDeletion);
        String deleteBasePath = basePath + "/{id}";
        Response response = RestClient.doDelete("JSON", reportServiceURL, deleteBasePath, token, null, pathParameters, true);
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().get("status_message"), is("success"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyDeleteScheduleReportAPI_Unauthorized() {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", scheduleReportIDForDeletion);
        String deleteBasePath = basePath + "/{id}";
        Response response = RestClient.doDelete("JSON", reportServiceURL, deleteBasePath, token + "Unauthorized", null, pathParameters, true);
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().get("error"), is("Invalid authcode or unauthorized user"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyDeleteScheduleReportAPI_InvalidScheduleId() {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "99999");
        String deleteBasePath = basePath + "/{id}";
        Response response = RestClient.doDelete("JSON", reportServiceURL, deleteBasePath, token, null, pathParameters, true);
        assertThat(response.getStatusCode(), is(500));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyDeleteScheduleReportAPI_EmptyScheduleId() {
        String deleteBasePath = basePath;
        Response response = RestClient.doDelete("JSON", reportServiceURL, deleteBasePath, token, null, null, true);
        assertThat(response.getStatusCode(), is(405));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyDeleteScheduleReportAPI_AlreadyDeleted() {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", scheduleReportIDForDeletion);
        String deleteBasePath = basePath + "/{id}";
        Response response = RestClient.doDelete("JSON", reportServiceURL, deleteBasePath, token, null, pathParameters, true);
        assertThat(response.getStatusCode(), is(200));

        Response secondDeleteResponse = RestClient.doDelete("JSON", reportServiceURL, deleteBasePath, token, null, pathParameters, true);
        assertThat(secondDeleteResponse.getStatusCode(), is(500));
    }
}
