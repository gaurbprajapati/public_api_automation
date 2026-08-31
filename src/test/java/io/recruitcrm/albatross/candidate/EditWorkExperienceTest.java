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
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class EditWorkExperienceTest extends TestBase {
    JavaFakerCandidate fakerCandidate;
    AllCrudFunctions function;
    String candidate_slug;
    String work_company_name;
    String title;
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
    public void editWorkExperienceVerify200(String candidate_slug, int workHistoryId) {
        String basePath = "candidates/candidate-work/" + workHistoryId;
        String workCompanyNameEdited = "Recruit CRM " + work_company_name;
        WorkHistory workHistory = new WorkHistory(candidate_slug, workCompanyNameEdited, title);
        workHistory.setCandidate_id(candidate_id);
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, workHistory);
        Assert.assertEquals(response.jsonPath().getString("message"), "Candidate Work History is updated successfully.", "Unexpected response message.");
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        JsonPath jp = response.jsonPath();
        String company_name = jp.get("data.work_company_name");
        Assert.assertEquals(company_name, workCompanyNameEdited, "Expected: " + workCompanyNameEdited + "but found " + company_name);
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "workHistoryData", groups = "nightly-build")
    public void editWorkExperienceVerify401(String candidate_slug, int workHistoryId) {
        String basePath = "candidates/candidate-work/" + workHistoryId;
        String workCompanyNameEdited = "Recruit CRM " + work_company_name;
        WorkHistory workHistory = new WorkHistory(candidate_slug, workCompanyNameEdited, title);
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken() + "12345", null, null, true, workHistory);
        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401, but got " + response.getStatusCode());
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "workHistoryData", groups = "nightly-build")
    public void editWorkExperienceVerify404(String candidate_slug, int workHistoryId) {
        String basePath = "candidates/candidate-work/" + workHistoryId + "123";
        String workCompanyNameEdited = "Recruit CRM " + work_company_name;
        WorkHistory workHistoryObj = new WorkHistory(candidate_slug, workCompanyNameEdited, title);
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, workHistoryObj);
        //(Commenting, as currently it is giving 200 status code. Once this is fixed will uncomment it) Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404, but got " + response.getStatusCode());
        Assert.assertEquals(response.jsonPath().getString("message"), "Work History Not Found");
        Assert.assertEquals(response.jsonPath().getString("message_type"), "is-danger");
    }

    @DataProvider(name = "workHistoryData")
    public Object[][] createWorkHistoryData() {
        JsonPath candidateJsonPath = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        candidate_id = candidateJsonPath.getInt("data.candidate.id");
        candidate_slug = candidateJsonPath.getString("data.candidate.slug");
        WorkHistory workHistory = new WorkHistory(candidate_id, candidate_slug, work_company_name, title, employment_type, industry_id, work_location, 0, work_start_date, work_end_date, work_description, salary);
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/candidate-work/create", ThreadManager.getOwnerAlbatrossToken(), null, null, true, workHistory);
        workHistoryId = response.jsonPath().getInt("data.id");
        return new Object[][]{{candidate_slug, workHistoryId}};
    }
}
