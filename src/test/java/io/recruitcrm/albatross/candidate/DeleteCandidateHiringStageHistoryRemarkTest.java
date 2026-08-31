package io.recruitcrm.albatross.candidate;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.albatross.DeleteHiringStageHistoryRemarkRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.*;
import org.hamcrest.Matchers;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class DeleteCandidateHiringStageHistoryRemarkTest extends TestBase {

    String basePath = "candidates/delete-hiring-stage-history-remark";

    commanFunction commanFunction = new commanFunction();
    JavaFakerCandidate faker = new JavaFakerCandidate();

    String apiTokenAValid;
    String albatrossTokenAValid;
    String albatrossTokenBValid;
    String albatrossTokenAInvalid;
    String restrictedToken;


    @BeforeClass
    public void setUp() {
        apiTokenAValid = getAccountApiKey("AccountA");
        albatrossTokenAValid = getTokenForAccount("AccountA", "valid");
        albatrossTokenBValid = getTokenForAccount("AccountB", "valid");
        albatrossTokenAInvalid = getTokenForAccount("AccountA", "invalid");
        restrictedToken = getRoleBasedToken("AccountA", "Restricted");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getHiringStageHistoryData")
    public void deleteCandidateHiringStageHistoryRemark_Test(String candidateSlug, int jobId, int id, int candidatestatusid, int updatedon) {

        DeleteHiringStageHistoryRemarkRequest body = new DeleteHiringStageHistoryRemarkRequest();
        body.setId(id);
        body.setUpdatedon(String.valueOf(updatedon));
        body.setCandidatestatusid(candidatestatusid);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTokenAValid, null, true, body);
        response.then().statusCode(200);
        
        response.then().body("message", Matchers.is("History entry deleted successfully."));
        response.then().body("data.records[0].candidatestatusid", Matchers.is(1));
        response.then().body("data.records[0].updatedon", Matchers.not(updatedon));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getInitialHiringStageHistoryData")
    public void verifyUserCannotDeleteIntialCandidateHiringStageHistoryRemark_Test(String candidateSlug, int jobId, int id, int candidatestatusid, int updatedon) {

        DeleteHiringStageHistoryRemarkRequest body = new DeleteHiringStageHistoryRemarkRequest();
        body.setId(id);
        body.setUpdatedon(String.valueOf(updatedon));
        body.setCandidatestatusid(candidatestatusid);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTokenAValid, null, true, body);
        response.then().statusCode(200);
        
        response.then().body("message", Matchers.is("The first Assigned or Applied entry for this assignment cannot be modified."));
        response.then().body("data", Matchers.empty());
    }

    @Owner("Sai Teja SG")
    @Test
    public void deleteCandidateHiringStageHistoryRemarkWithInvalidData_Test() {

        DeleteHiringStageHistoryRemarkRequest body = new DeleteHiringStageHistoryRemarkRequest();
        body.setId(faker.getSalary());
        body.setUpdatedon(String.valueOf(faker.getSalary()));
        body.setCandidatestatusid(faker.getSalary());

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTokenAValid, null, true, body);
        response.then().statusCode(200);
        
        response.then().body("message", Matchers.is("History entry not found."));
        response.then().body("data", Matchers.empty());
    }

    @Owner("Sai Teja SG")
    @Test
    public void deleteCandidateHiringStageHistoryRemarkWithDefaultData_Test() {

        DeleteHiringStageHistoryRemarkRequest body = new DeleteHiringStageHistoryRemarkRequest();

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTokenAValid, null, true, body);
        response.then().statusCode(422);
        
        response.then().body("message", Matchers.is("The updatedon field is required.,The candidatestatusid must be at least 1."));
        response.then().body("data", Matchers.empty());
    }

    @Owner("Sai Teja SG")
    @Test
    public void deleteCandidateHiringStageHistoryRemarkWithEmptyData_Test() {

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTokenAValid, null, true, null);
        response.then().statusCode(422);
        
        response.then().body("message", Matchers.is("The id field is required.,The updatedon field is required.,The candidatestatusid field is required."));
        response.then().body("data", Matchers.empty());
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getHiringStageHistoryData")
    public void restrictedTeamMemberCannotDeleteCandidateHiringStageHistoryRemark_Test(String candidateSlug, int jobId, int id, int candidatestatusid, int updatedon) {

        DeleteHiringStageHistoryRemarkRequest body = new DeleteHiringStageHistoryRemarkRequest();
        body.setId(id);
        body.setUpdatedon(String.valueOf(updatedon));
        body.setCandidatestatusid(candidatestatusid);
        
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, restrictedToken, null, true, body);
        response.then().statusCode(200);
        
        response.then().body("message", Matchers.is("Delete action not allowed."));
        response.then().body("data", Matchers.empty());
    }

    @Owner("Sai Teja SG")
    @Test
    public void unauthorisedUserCannotDeleteCandidateHiringStageHistoryRemark_Test() {

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTokenAInvalid, null, true, null);
        response.then().statusCode(401);
        
        response.then().body("error", Matchers.is("Unauthorized"));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getHiringStageHistoryData")
    public void crossAccountUserCannotDeleteCandidateHiringStageHistoryRemark_Test(String candidateSlug, int jobId, int id, int candidatestatusid, int updatedon) {

        DeleteHiringStageHistoryRemarkRequest body = new DeleteHiringStageHistoryRemarkRequest();
        body.setId(id);
        body.setUpdatedon(String.valueOf(updatedon));
        body.setCandidatestatusid(candidatestatusid);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTokenBValid, null, true, body);
        response.then().statusCode(200);
        
        response.then().body("message", Matchers.is("History entry not found."));
        response.then().body("data", Matchers.empty());
    }

    @DataProvider
    private Object[][] getHiringStageHistoryData() {
        
        JsonPath jsonAssigneCandidate = commanFunction.assignCandidateToJob(baseURL, apiTokenAValid).jsonPath();
		String candidateSlug = jsonAssigneCandidate.get("candidate_slug");
		String jobSlug = jsonAssigneCandidate.get("job_slug");

        commanFunction.updateCandidateHiringStageRemark(baseURL, apiTokenAValid, candidateSlug, jobSlug);

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/"+jobSlug+"/get", albatrossTokenAValid, null, false, null);
        response.then().statusCode(200);
        JsonPath jp = response.jsonPath();
        int jobId = jp.getInt("data.job.id");

        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateslug", candidateSlug);
        requestBody.put("jobid", jobId);
        Response getResponse = RestClient.doPost("JSON", albatrossURL, "candidates/get-hiring-stage-history-remark", albatrossTokenAValid, null, true, requestBody.toString());
        getResponse.then().statusCode(200);
        JsonPath jsonPath = getResponse.jsonPath();
        int id = jsonPath.getInt("data.records[0].id");
        int candidatestatusid = jsonPath.getInt("data.records[0].candidatestatusid");
        int updatedon = jsonPath.getInt("data.records[0].updatedon");

        return new Object[][] { { candidateSlug, jobId, id, candidatestatusid, updatedon } };
    }

    @DataProvider
    private Object[][] getInitialHiringStageHistoryData() {
        
        JsonPath jsonAssignCandidate = commanFunction.assignCandidateToJob(baseURL, apiTokenAValid).jsonPath();
		String candidateSlug = jsonAssignCandidate.get("candidate_slug");
		String jobSlug = jsonAssignCandidate.get("job_slug");

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/"+jobSlug+"/get", albatrossTokenAValid, null, false, null);
        response.then().statusCode(200);
        JsonPath jp = response.jsonPath();
        int jobId = jp.getInt("data.job.id");

        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateslug", candidateSlug);
        requestBody.put("jobid", jobId);
        Response getResponse = RestClient.doPost("JSON", albatrossURL, "candidates/get-hiring-stage-history-remark", albatrossTokenAValid, null, true, requestBody.toString());
        getResponse.then().statusCode(200);
        JsonPath jsonPath = getResponse.jsonPath();
        int id = jsonPath.getInt("data.records[0].id");
        int candidatestatusid = jsonPath.getInt("data.records[0].candidatestatusid");
        int updatedon = jsonPath.getInt("data.records[0].updatedon");

        return new Object[][] { { candidateSlug, jobId, id, candidatestatusid, updatedon } };
    }
}
