package io.recruitcrm.CandidateService;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.candidateService.WorkHistoryRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class EditWorkHistoryTest extends TestBase {

    public EditWorkHistoryTest() {
        super();
    }

    JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
    String workCompanyName = fakerCandidate.getWorkCompanyName();
    String title = fakerCandidate.getJobTitle();
    int employmentType = fakerCandidate.getEmploymentType();
    int industryId = fakerCandidate.getIndustryId();
    String workLocation = fakerCandidate.getWorkLocation();
    Boolean isCurrentlyWorking = fakerCandidate.getRandomToggleState();
    int workStartDate = fakerCandidate.getStartDate();
    int workEndDate = fakerCandidate.getEndDateWithReferenceDate(workStartDate);
    String workDescription = fakerCandidate.getDescription();
    int salary = fakerCandidate.getSalary();
    boolean isManuallyAdded = fakerCandidate.getRandomToggleState();
    WorkHistoryRequest workHistoryRequest;
    AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();

    @Owner("Raj Pandey")
    @Test
    public void editWorkHistoryVerify_200() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        String candSlug = candidateJsonPath.get("data.candidate.slug");
        workHistoryRequest = new WorkHistoryRequest(title, workCompanyName, employmentType, industryId,
                workLocation, salary, isCurrentlyWorking, workStartDate, workEndDate, workDescription,
                isManuallyAdded, candSlug);

        String basePath = "candidates/{candidateId}/work-history";
        pathParamters.put("candidateId", candidateJsonPath.getString("data.candidate.id"));
        Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParamters, true,
                workHistoryRequest);

        Assert.assertEquals(response1.getStatusCode(), 201);

        String workHistoryId = response1.jsonPath().getString("data.id");

        basePath = "candidates/work-history/" + workHistoryId;

        // Edit the work history
        workHistoryRequest.setTitle(fakerCandidate.getJobTitle());
        workHistoryRequest.setWorkCompanyName(fakerCandidate.getWorkCompanyName());
        workHistoryRequest.setEmploymentType(fakerCandidate.getEmploymentType());
        workHistoryRequest.setIndustryId(fakerCandidate.getIndustryId());
        workHistoryRequest.setWorkLocation(fakerCandidate.getWorkLocation());
        workHistoryRequest.setSalary(fakerCandidate.getSalary());
        workHistoryRequest.setWorkDescription(fakerCandidate.getDescription());
        workHistoryRequest.setIsManuallyAdded(fakerCandidate.getRandomToggleState());

        Response response2 = RestClient.doPatchOnce("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken(), null, true, workHistoryRequest);

        Assert.assertEquals(response2.getStatusCode(), 200);
        response2.then().body("meta.message", Matchers.is("Work history updated successfully"));
        response2.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/candidate/EditWorkHistory.json"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void editWorkHistoryVerify_401() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        String candSlug = candidateJsonPath.get("data.candidate.slug");
        workHistoryRequest = new WorkHistoryRequest(title, workCompanyName, employmentType, industryId,
                workLocation, salary, isCurrentlyWorking, workStartDate, workEndDate, workDescription,
                isManuallyAdded, candSlug);

        String basePath = "candidates/{candidateId}/work-history";
        pathParamters.put("candidateId", candidateJsonPath.getString("data.candidate.id"));
        Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParamters, true,
                workHistoryRequest);

        Assert.assertEquals(response1.getStatusCode(), 201);

        String workHistoryId = response1.jsonPath().getString("data.id");

        basePath = "candidates/work-history/" + workHistoryId;

        workHistoryRequest.setTitle(fakerCandidate.getJobTitle());
        workHistoryRequest.setWorkCompanyName(fakerCandidate.getWorkCompanyName());
        workHistoryRequest.setEmploymentType(fakerCandidate.getEmploymentType());
        workHistoryRequest.setIndustryId(fakerCandidate.getIndustryId());
        workHistoryRequest.setWorkLocation(fakerCandidate.getWorkLocation());
        workHistoryRequest.setSalary(fakerCandidate.getSalary());
        workHistoryRequest.setWorkDescription(fakerCandidate.getDescription());
        workHistoryRequest.setIsManuallyAdded(fakerCandidate.getRandomToggleState());

        Response response2 = RestClient.doPatchOnce("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "1234", null, true, workHistoryRequest);

        Assert.assertEquals(response2.getStatusCode(), 401);
        response2.then().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Gaurav Prajapati")
    @Test
    public void editWorkHistoryVerify_404() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        String candSlug = candidateJsonPath.get("data.candidate.slug");
        workHistoryRequest = new WorkHistoryRequest(title, workCompanyName, employmentType, industryId,
                workLocation, salary, isCurrentlyWorking, workStartDate, workEndDate, workDescription,
                isManuallyAdded, candSlug);

        String basePath = "candidates/{candidateId}/work-history";
        pathParamters.put("candidateId", candidateJsonPath.getString("data.candidate.id"));
        Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParamters, true,
                workHistoryRequest);

        Assert.assertEquals(response1.getStatusCode(), 201);

        String workHistoryId = response1.jsonPath().getString("data.id");

        basePath = "candidates/work-history/-" + workHistoryId;

        // Edit the work history
        workHistoryRequest.setTitle(fakerCandidate.getJobTitle());
        workHistoryRequest.setWorkCompanyName(fakerCandidate.getWorkCompanyName());
        workHistoryRequest.setEmploymentType(fakerCandidate.getEmploymentType());
        workHistoryRequest.setIndustryId(fakerCandidate.getIndustryId());
        workHistoryRequest.setWorkLocation(fakerCandidate.getWorkLocation());
        workHistoryRequest.setSalary(fakerCandidate.getSalary());
        workHistoryRequest.setWorkDescription(fakerCandidate.getDescription());
        workHistoryRequest.setIsManuallyAdded(fakerCandidate.getRandomToggleState());

        Response response2 = RestClient.doPatchOnce("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken(), null, true, workHistoryRequest);

        Assert.assertEquals(response2.getStatusCode(), 404);
        response2.then().body("errors[0].message", Matchers.is("Candidate Work History id -" + workHistoryId + " not found."));
    }

    @Owner("Yash Rampal")
    @Test
    public void editWorkHistoryVerify_400() {
        Map<String, String> pathParamters = new HashMap<String, String>();
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
        String candSlug = candidateJsonPath.get("data.candidate.slug");
        workHistoryRequest = new WorkHistoryRequest(title, workCompanyName, employmentType, industryId,
                workLocation, salary, isCurrentlyWorking, workStartDate, workEndDate, workDescription,
                isManuallyAdded, candSlug);

        String basePath = "candidates/{candidateId}/work-history";
        pathParamters.put("candidateId", candidateJsonPath.getString("data.candidate.id"));
        Response response1 = RestClient.doPost1("JSON", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, pathParamters, true,
                workHistoryRequest);

        Assert.assertEquals(response1.getStatusCode(), 201);

        String workHistoryId = response1.jsonPath().getString("data.id");

        basePath = "candidates/work-history/" + workHistoryId + fakerCandidate.getDescription();

        // Edit the work history
        workHistoryRequest.setTitle(fakerCandidate.getJobTitle());
        workHistoryRequest.setWorkCompanyName(fakerCandidate.getWorkCompanyName());
        workHistoryRequest.setEmploymentType(fakerCandidate.getEmploymentType());
        workHistoryRequest.setIndustryId(fakerCandidate.getIndustryId());
        workHistoryRequest.setWorkLocation(fakerCandidate.getWorkLocation());
        workHistoryRequest.setSalary(fakerCandidate.getSalary());
        workHistoryRequest.setWorkDescription(fakerCandidate.getDescription());
        workHistoryRequest.setIsManuallyAdded(fakerCandidate.getRandomToggleState());

        Response response2 = RestClient.doPatchOnce("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken(), null, true, workHistoryRequest);

        Assert.assertEquals(response2.getStatusCode(), 400);
        response2.then().body("error", Matchers.is("Bad Request"));
    }
}
