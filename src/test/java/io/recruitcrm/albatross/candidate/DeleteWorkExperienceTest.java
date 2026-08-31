package io.recruitcrm.albatross.candidate;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.WorkHistory;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DeleteWorkExperienceTest extends TestBase {
    JavaFakerCandidate fakerCandidate;
    AllCrudFunctions function;
    String work_company_name;
    String title;
    String candidate_slug;
    String work_location;
    String work_description;
    int candidate_id;
    int workHistoryId;
    int employment_type;
    int industry_id;
    int work_start_date;
    int work_end_date;
    int salary;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        fakerCandidate = new JavaFakerCandidate();
        function = new AllCrudFunctions();
        work_company_name = fakerCandidate.getWorkCompanyName();
        title = fakerCandidate.getJobTitle();
        employment_type = fakerCandidate.getEmploymentType();
        industry_id = fakerCandidate.getIndustryId();
        work_location = fakerCandidate.getWorkLocation();
        work_start_date = fakerCandidate.getStartDate();
        work_end_date = fakerCandidate.getEndDateWithReferenceDate(work_start_date);
        work_description = fakerCandidate.getDescription().replaceAll("<[^>]*>", "");
        salary = fakerCandidate.getSalary();
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "workHistoryData", groups = "nightly-build")
    public void deleteWorkExperienceVerify200(int candidate_id, String candidate_slug, int workHistoryId) {
        Map<String, Object> body = new HashMap<>();
        body.put("idsToDelete", Collections.singletonList(workHistoryId));
        body.put("candidateSlug", candidate_slug);
        Response response = RestClient.doDeleteOnce("application/json", albatrossURL, "candidates/candidate-work/delete-work", ThreadManager.getOwnerAlbatrossToken(), null, null, false, body);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        Assert.assertEquals(response.jsonPath().getString("message"), "Work history deleted successfully.", "Unexpected response message.");
        Response response1 = RestClient.doGet("JSON", albatrossURL, "candidates/candidate-work/" + candidate_id, ThreadManager.getOwnerAlbatrossToken(), null, null, true);
        Assert.assertEquals(response1.getStatusCode(), 200, "Expected status code 200 for GET, but got " + response1.getStatusCode());
        List<Integer> ids = response1.jsonPath().getList("data.id");
        Assert.assertFalse(ids.contains(workHistoryId), "Deleted workHistoryId " + workHistoryId + " still exists.");
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "workHistoryData", groups = "nightly-build")
    public void deleteMultipleWorkExperienceVerify200(int candidate_id, String candidate_slug, int workHistoryId) {
        List<Integer> workHistoryIds = new ArrayList<>();
        workHistoryIds.add(workHistoryId);
        WorkHistory workHistory = new WorkHistory(candidate_id, candidate_slug, fakerCandidate.getWorkCompanyName(), fakerCandidate.getJobTitle(), employment_type, industry_id, work_location, 0, work_start_date, work_end_date, work_description, salary);
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/candidate-work/create", ThreadManager.getOwnerAlbatrossToken(), null, null, true, workHistory);
        int newWorkHistoryId = response.jsonPath().getInt("data.id");
        workHistoryIds.add(newWorkHistoryId);
        Map<String, Object> body = new HashMap<>();
        body.put("idsToDelete", workHistoryIds);
        body.put("candidateSlug", candidate_slug);
        Response response1 = RestClient.doDeleteOnce("application/json", albatrossURL, "candidates/candidate-work/delete-work", ThreadManager.getOwnerAlbatrossToken(), null, null, false, body);
        Assert.assertEquals(response1.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        Assert.assertEquals(response1.jsonPath().getString("message"), "Work history deleted successfully.", "Unexpected response message.");
        Response response2 = RestClient.doGet("JSON", albatrossURL, "candidates/candidate-work/" + candidate_id, ThreadManager.getOwnerAlbatrossToken(), null, null, true);
        Assert.assertEquals(response2.getStatusCode(), 200, "Expected status code 200 for GET, but got " + response1.getStatusCode());
        List<Integer> ids = response2.jsonPath().getList("data.id");
        for (int id : workHistoryIds) {
            Assert.assertFalse(ids.contains(id), "Deleted workHistoryId " + id + " still exists.");
        }
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "workHistoryData", groups = "nightly-build")
    public void deleteWorkExperienceVerify401(int candidate_id, String candidate_slug, int workHistoryId) {
        Map<String, Object> body = new HashMap<>();
        body.put("idsToDelete", Collections.singletonList(workHistoryId));
        body.put("candidateSlug", candidate_slug);
        Response response = RestClient.doDeleteOnce("application/json", albatrossURL, "candidates/candidate-work/delete-work", ThreadManager.getOwnerAlbatrossToken() + "132465", null, null, false, body);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401, but got " + response.getStatusCode());
    }

    @DataProvider(name = "workHistoryData")
    public Object[][] createWorkHistoryData() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        candidate_id = candidateJsonPath.getInt("data.candidate.id");
        candidate_slug = candidateJsonPath.getString("data.candidate.slug");
        WorkHistory workHistory = new WorkHistory(candidate_id, candidate_slug, work_company_name, title, employment_type, industry_id, work_location, 0, work_start_date, work_end_date, work_description, salary);
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/candidate-work/create", ThreadManager.getOwnerAlbatrossToken(), null, null, true, workHistory);
        workHistoryId = response.jsonPath().getInt("data.id");
        return new Object[][]{{candidate_id, candidate_slug, workHistoryId}};
    }

}
