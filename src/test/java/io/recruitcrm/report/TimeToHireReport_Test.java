package io.recruitcrm.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.KpiLists;
import io.recruitcrm.report.pojo.TimeToHireReport;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TimeToHireReport_Test extends TestBase{
    public TimeToHireReport_Test(){
        super();
    }

    String slug = "";
    commanFunction function = new commanFunction();
    ListFunctions listFunctions = new ListFunctions();

    @Owner("Smit Patel")
    @Test(dataProvider = "kpiListProvider", groups = "nightly-build")
    public void timeToHireReportPost_Test(ArrayList<Object> kpiLists) {
        ArrayList<Integer> job_ids = createMultipleJobs(3);

        TimeToHireReport timeToHireReport = new TimeToHireReport();
        timeToHireReport.setJob_ids(job_ids);
        timeToHireReport.setKpi_lists(kpiLists);

        Response response = RestClient.doPost("JSON", reportServiceURL, "reports/time-to-hire-report-new", ThreadManager.getOwnerAlbatrossToken(), null, true, timeToHireReport);

        JsonPath json = response.jsonPath();
        List<Map<String, Object>> jobs = json.getList("data");
        
        response.then().statusCode(200);
        assertThat(json.getString("chart_data.series_data[0].seriesname"), equalTo("Applied"));
        for (Map<String, Object> job : jobs) {
            assertThat("Job name should match the expected pattern", (String) job.get("job_name"), containsString("Job"));
        }        
    }
    
    @DataProvider
    public Object[][] kpiListProvider() {
        ArrayList<Object> kpiLists0 = new ArrayList<>();

        KpiLists KpiLists1 = new KpiLists();
        KpiLists1.setLabel("Applied");
        KpiLists1.setValue("10");
        KpiLists1.setChecked(true);

        KpiLists KpiLists2 = new KpiLists();
        KpiLists2.setLabel("Assigned");
        KpiLists2.setValue("1");
        KpiLists2.setChecked(true);

        kpiLists0.add(KpiLists1);
        kpiLists0.add(KpiLists2);

        return new Object[][] { { kpiLists0 } };
    }
    
    private ArrayList<Integer> createMultipleJobs(int count) {
        String accountApiKey = ThreadManager.getAccountApiKey();
        String ownerAlbatrossToken = ThreadManager.getOwnerAlbatrossToken();

        ExecutorService executor = Executors.newFixedThreadPool(6);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            futures.add(executor.submit(() -> {
            	JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, accountApiKey).jsonPath();
                JsonPath contactJson = function.createNewContactWithAllFields(baseURL, accountApiKey, companyJson.getString("slug")).jsonPath();
                JsonPath job = function.createNewJob(baseURL, accountApiKey, companyJson.getString("slug"), contactJson.getString("slug")).jsonPath();

                String slug = job.getString("slug");

                JsonPath jobJson = null;

                Response res = RestClient.doPost("JSON", albatrossURL, "jobs/" + slug + "/get", ownerAlbatrossToken, null, true, null);
                if (res.statusCode() == 200 && res.jsonPath().get("data.job.id") != null) jobJson = res.jsonPath();

                if (jobJson == null) throw new RuntimeException("Failed to fetch job by slug: " + slug);
                
                return jobJson.getInt("data.job.id");
            }));
        }

        ArrayList<Integer> jobIds = new ArrayList<>();
        for (Future<Integer> future : futures) {
            try {
                jobIds.add(future.get());
            } catch (Throwable e) {
                throw new RuntimeException("Job creation failed", e);
            }
        }

        executor.shutdown();
        return jobIds;
    }
}