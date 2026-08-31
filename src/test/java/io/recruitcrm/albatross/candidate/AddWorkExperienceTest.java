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
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AddWorkExperienceTest extends TestBase {
    JavaFakerCandidate fakerCandidate;
    AllCrudFunctions function;
    String work_location;
    String work_company_name;
    String work_description;
    String title;
    int employment_type;
    int industry_id;
    int work_start_date;
    int work_end_date;
    int salary;


    @BeforeClass(alwaysRun = true)    public void setUp() {
        fakerCandidate = new JavaFakerCandidate();
        work_location = fakerCandidate.getWorkLocation();
        work_company_name = fakerCandidate.getWorkCompanyName();
        work_description = fakerCandidate.getDescription().replaceAll("<[^>]*>", "");
        title = fakerCandidate.getJobTitle();
        employment_type = fakerCandidate.getEmploymentType();
        industry_id = fakerCandidate.getIndustryId();
        work_start_date = fakerCandidate.getStartDate();
        work_end_date = fakerCandidate.getEndDateWithReferenceDate(work_start_date);
        salary = fakerCandidate.getSalary();
        function = new AllCrudFunctions();
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "candidateData", groups = "nightly-build")
    public void addWorkExperienceVerify200(int candidate_id, String candidate_slug) {
        WorkHistory workHistory = new WorkHistory(candidate_id, candidate_slug, work_company_name, title, employment_type, industry_id, work_location, 0, work_start_date, work_end_date, work_description, salary);
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/candidate-work/create", ThreadManager.getOwnerAlbatrossToken(), null, null, true, workHistory);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        Assert.assertEquals(response.jsonPath().getString("message"), "Candidate Work History is created successfully.", "Unexpected response message.");
        int workHistoryId = response.jsonPath().getInt("data.id");
        Response response1 = RestClient.doGet("JSON", albatrossURL, "candidates/candidate-work/" + candidate_id, ThreadManager.getOwnerAlbatrossToken(), null, null, true);
        Assert.assertEquals(response1.getStatusCode(), 200, "Expected status code 200 for GET, but got " + response1.getStatusCode());
        List<Integer> ids = response1.jsonPath().getList("data.id");
        Assert.assertTrue(ids.contains(workHistoryId), "Created workHistoryId " + workHistoryId + " doesn't exists.");
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "candidateData", groups = "nightly-build")
    public void addWorkExperienceVerify401(int candidate_id, String candidate_slug) {
        WorkHistory workHistory = new WorkHistory();
        workHistory.setCandidate_slug(candidate_slug);
        workHistory.setTitle(title);
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/candidate-work/create", ThreadManager.getOwnerAlbatrossToken() + "12345", null, null, true, workHistory);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401, but got " + response.getStatusCode());
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "candidateData", groups = "nightly-build")
    public void addWorkExperienceVerify422(int candidate_id, String candidate_slug) {
        String basePath = "candidates/candidate-work/create";
        JSONObject workHistory = new JSONObject();
        workHistory.put("candidate_slug", candidate_slug);
        workHistory.put("title", title);
        workHistory.put("work_company_name", work_company_name);
        workHistory.put("employment_type", employment_type);
        workHistory.put("industry_id", industry_id);
        workHistory.put("work_location", work_location);
        workHistory.put("salary", salary);
        workHistory.put("work_start_date", work_start_date);
        workHistory.put("work_end_date", work_end_date);
        workHistory.put("work_description", work_description);
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, workHistory.toString());
        Assert.assertEquals(response.getStatusCode(), 422, "Expected 422 for missing candidate_id but got " + response.getStatusCode());
    }

    @DataProvider(name = "candidateData")
    public Object[][] createCandidate() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        int candidate_id = candidateJsonPath.getInt("data.candidate.id");
        String candidate_slug = candidateJsonPath.getString("data.candidate.slug");
        return new Object[][]{{candidate_id, candidate_slug}};
    }
}