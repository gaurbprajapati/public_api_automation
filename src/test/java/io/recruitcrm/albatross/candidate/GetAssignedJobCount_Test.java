package io.recruitcrm.albatross.candidate;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.*;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import com.qa.api.util.Owner;


@AccountType("CrossAccount")
public class GetAssignedJobCount_Test extends TestBase {

    String apiKeyA;
    String apiKeyB;
    String albatrossTknA;
    String albatrossTknB;
    commanFunction function;
    AllCrudFunctions allCrudFunctions;
    JavaFakerPlacement fakerPlacement;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        apiKeyA = getAccountApiKey("AccountA");
        apiKeyB = getAccountApiKey("AccountB");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        fakerPlacement = new JavaFakerPlacement();
    }


    @Owner("Sai Teja SG")
    @Test(dataProvider = "getCandidateId", groups = "nightly-build")
    public void getAssignedJobCountWithValidToken_Test(int candidateId) {
        Response response = RestClient.doPost("JSON", albatrossURL, "candidates/" + candidateId + "/jobs-assigned-count/get", albatrossTknA, null, true, null);
        response.then().statusCode(200);
        response.then().body("data.count", Matchers.equalTo(1));
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAssignedJobCountWithInvalidToken_Test() {
        Response response = RestClient.doPost("JSON", albatrossURL, "candidates/" + fakerPlacement.getRandomID() + "/jobs-assigned-count/get", fakerPlacement.getInvalidToken(), null, true, null);
        response.then().statusCode(401);
        response.then().assertThat().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCandidateId", groups = "nightly-build")
    public void getAssignedJobCountEmptyBody_Test(int candidateId) {
        Response response = RestClient.doPost("JSON", albatrossURL, "candidates/" + candidateId + "/jobs-assigned-count/get", albatrossTknA, null, true, null);
        response.then().statusCode(200);
        response.then().body("data.count", Matchers.equalTo(1));
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));

    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void getAssignedJobCountInvalidId_Test() {
        JSONObject invalidId = new JSONObject();
        invalidId.put("candidateId", fakerPlacement.getRandomID());
        Response response = RestClient.doPost("JSON", albatrossURL, "candidates/" + fakerPlacement.getRandomID() + "/jobs-assigned-count/get", albatrossTknA, null, true, invalidId);
        response.then().statusCode(200);
        response.then().assertThat().body("data", Matchers.empty());
        response.then().assertThat().body("message", Matchers.is("Access Denied"));
        response.then().assertThat().body("message_type", Matchers.is("is-danger"));

    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getCandidateId", groups = "nightly-build")
    public void getAssignedJobCountWithCrossAccountToken_Test(int candidateId) {
        Response response = RestClient.doPost("JSON", albatrossURL, "candidates/" + candidateId + "/jobs-assigned-count/get", albatrossTknB, null, true, null);
        response.then().statusCode(200);
        response.then().assertThat().body("data", Matchers.empty());
        response.then().assertThat().body("message", Matchers.is("Access Denied"));
        response.then().assertThat().body("message_type", Matchers.is("is-danger"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getCandidateId", groups = "nightly-build")
    public void getAssignedJobCountWithAdminToken_Test(int candidateId) {
        Response response = RestClient.doPost("JSON", albatrossURL, "candidates/" + candidateId + "/jobs-assigned-count/get", getRoleBasedToken("AccountA", "Admin"), null, true, null);
        response.then().statusCode(200);
        response.then().body("data.count", Matchers.equalTo(1));
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getCandidateId", groups = "nightly-build")
    public void getAssignedJobCountWithTeamMemberToken_Test(int candidateId) {
        Response response = RestClient.doPost("JSON", albatrossURL, "candidates/" + candidateId + "/jobs-assigned-count/get", getRoleBasedToken("AccountA", "Team Member"), null, true, null);
        response.then().statusCode(200);
        response.then().body("data.count", Matchers.equalTo(1));
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getCandidateId", groups = "nightly-build")
    public void getAssignedJobCountWithRestrictedToken_Test(int candidateId) {
        Response response = RestClient.doPost("JSON", albatrossURL, "candidates/" + candidateId + "/jobs-assigned-count/get", getRoleBasedToken("AccountA", "Restricted"), null, true, null);
        response.then().statusCode(200);
        response.then().assertThat().body("data", Matchers.empty());
        response.then().assertThat().body("message", Matchers.is("Access Denied"));
        response.then().assertThat().body("message_type", Matchers.containsString("is-danger"));
    }

    @DataProvider(parallel = true)
    public Object[][] getCandidateId(){
        Response response = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA);
        String candidateSlug = response.jsonPath().getString("slug");
        int candidateId = allCrudFunctions.getCandidateResponse(albatrossURL, albatrossTknA,candidateSlug).jsonPath().get("data.candidate.id");
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
        String companySlug = companyResponse.jsonPath().getString("slug");
        Response contactResponse = function.createNewContact_POST(baseURL, apiKeyA, companySlug);
        String contactSlug = contactResponse.jsonPath().getString("slug");
        Response jobResponse = function.createNewJob(baseURL, apiKeyA, companySlug, contactSlug);
        String jobSlug = jobResponse.jsonPath().getString("slug");
        function.assignCandidateToJobBySlug(baseURL, apiKeyA, candidateSlug, jobSlug);
        return new Object[][] {
                { candidateId }
        };
    }
}