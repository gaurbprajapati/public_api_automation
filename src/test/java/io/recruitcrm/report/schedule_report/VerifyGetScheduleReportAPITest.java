package io.recruitcrm.report.schedule_report;

import java.util.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.albatross.report.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.commanfunctions.reportService.ReportServiceFunctions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class VerifyGetScheduleReportAPITest extends TestBase {

    String reportID = "";
    JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
    ArrayList<String> scheduleReportIDs = new ArrayList<>();
    String reportName = reportFaker.getReportName();
    String basePath = "schedule-report/get-all-schedules";
    private String token = "";
    ReportServiceFunctions reportServiceFunctions = new ReportServiceFunctions();
    
    @BeforeClass(alwaysRun = true)    
    public void setupPrerequisites() {
        token = ThreadManager.getOwnerAlbatrossToken();
        reportID = reportServiceFunctions.createReportPreference(baseURL, reportServiceURL, token, reportName);
        scheduleReportIDs = reportServiceFunctions.createMultipleScheduleReports(2, reportID, reportServiceURL, token);
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyGetAllScheduleReportsAPI() {
        String getBasePath = basePath + "/" + reportID;
        Response response = RestClient.doGet("JSON", reportServiceURL, getBasePath, token, null, null, true);
        assertThat(response.getStatusCode(), is(200));
        JsonPath jp = response.jsonPath();
        Map<String, Object> responseMap = jp.getMap("$");
        assertThat(responseMap, is(notNullValue()));
        assertThat(responseMap.isEmpty(), is(false));
        
        Set<String> scheduleKeys = responseMap.keySet();
        for (String scheduleKey : scheduleKeys) {
            String scheduleKeyPath = scheduleKey + ".";
            assertThat(jp.get(scheduleKeyPath + "scheduleData"), is(notNullValue()));
            assertThat(jp.get(scheduleKeyPath + "scheduleData.id"), is(notNullValue()));
        }
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyGetAllScheduleReportsAPI_Unauthorized() {
        String getBasePath = basePath + "/" + reportID;
        Response response = RestClient.doGet("JSON", reportServiceURL, getBasePath, "", null, null, true);
        assertThat(response.getStatusCode(), is(401));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyGetAllScheduleReportsAPI_InvalidReportId() {
        String getBasePath = basePath + "/" + "99999";
        Response response = RestClient.doGet("JSON", reportServiceURL, getBasePath, token, null, null, true);
        assertThat(response.getStatusCode(), is(200));
        List<?> dataList = response.jsonPath().getList("data");
        assertThat(dataList == null || dataList.isEmpty(), is(true));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyGetAllScheduleReportsAPI_EmptyReportId() {
        String getBasePath = basePath + "/";
        Response response = RestClient.doGet("JSON", reportServiceURL, getBasePath, token, null, null, true);
        assertThat(response.getStatusCode(), is(405));
    }
}
