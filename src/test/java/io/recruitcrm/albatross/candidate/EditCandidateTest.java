package io.recruitcrm.albatross.candidate;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditCandidateTest extends TestBase {

    JavaFakerCandidate fakerCandidate;
    AllCrudFunctions function;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        fakerCandidate = new JavaFakerCandidate();
        function = new AllCrudFunctions();
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "candidateEditData", groups = "nightly-build")
    public void editCandidateVerify200(String candidateSlug) {
        String updatedFirstName = fakerCandidate.getFirstName();
        String updatedLastName = fakerCandidate.getLastName();
        String updatedEmail = fakerCandidate.getEmailID();
        String updatedContactNumber = fakerCandidate.getContactNumber();
        String updatedCity = fakerCandidate.getCity();
        String updatedAddress = fakerCandidate.getCandidateAddress();
        JSONObject candidate = new JSONObject();
        candidate.put("slug", candidateSlug);
        candidate.put("firstname", updatedFirstName);
        candidate.put("lastname", updatedLastName);
        candidate.put("emailid", updatedEmail);
        candidate.put("contactnumber", updatedContactNumber);
        candidate.put("city", updatedCity);
        candidate.put("address", updatedAddress);

        JSONObject requestBody = new JSONObject();
        requestBody.put("candidate", candidate);
        requestBody.put("address_changed", false);
        requestBody.put("filesInfo", new JSONObject());
        requestBody.put("deleteResumeKey", "");
        requestBody.put("deleteEducation", new JSONArray());
        requestBody.put("deleteWork", new JSONArray());
        requestBody.put("sovrenData", new JSONArray());

        String basePath = "candidates/" + candidateSlug;
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("message"), "Candidate Updated");
        Assert.assertEquals(response.jsonPath().getString("message_type"), "is-success");
        Assert.assertNotNull(response.jsonPath().get("data.candidate"));
        Assert.assertEquals(response.jsonPath().getString("data.candidate.firstname"), updatedFirstName);
        Assert.assertEquals(response.jsonPath().getString("data.candidate.lastname"), updatedLastName);
        Assert.assertEquals(response.jsonPath().getString("data.candidate.emailid"), updatedEmail);
        Assert.assertEquals(response.jsonPath().getString("data.candidate.contactnumber"), updatedContactNumber);
        Assert.assertEquals(response.jsonPath().getString("data.candidate.city"), updatedCity);
        Assert.assertEquals(response.jsonPath().getString("data.candidate.address"), updatedAddress);
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "candidateEditData", groups = "nightly-build")
    public void editCandidateVerify401(String candidateSlug) {
        String updatedFirstName = fakerCandidate.getFirstName();
        String updatedEmail = fakerCandidate.getEmailID();

        JSONObject candidate = new JSONObject();
        candidate.put("slug", candidateSlug);
        candidate.put("firstname", updatedFirstName);
        candidate.put("emailid", updatedEmail);

        JSONObject requestBody = new JSONObject();
        requestBody.put("candidate", candidate);
        requestBody.put("address_changed", false);
        requestBody.put("filesInfo", new JSONObject());
        requestBody.put("deleteResumeKey", "");
        requestBody.put("deleteEducation", new JSONArray());
        requestBody.put("deleteWork", new JSONArray());
        requestBody.put("sovrenData", new JSONArray());

        String basePath = "candidates/" + candidateSlug;
        String invalidToken = ThreadManager.getOwnerAlbatrossToken() + "invalid";
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, invalidToken, null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 401);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "candidateEditData", groups = "nightly-build")
    public void editCandidateVerifyAccessDenied(String candidateSlug) {
        String updatedFirstName = fakerCandidate.getFirstName();
        String updatedEmail = fakerCandidate.getEmailID();

        JSONObject candidate = new JSONObject();
        candidate.put("slug", candidateSlug + "invalid");
        candidate.put("firstname", updatedFirstName);
        candidate.put("emailid", updatedEmail);

        JSONObject requestBody = new JSONObject();
        requestBody.put("candidate", candidate);
        requestBody.put("address_changed", false);
        requestBody.put("filesInfo", new JSONObject());
        requestBody.put("deleteResumeKey", "");
        requestBody.put("deleteEducation", new JSONArray());
        requestBody.put("deleteWork", new JSONArray());
        requestBody.put("sovrenData", new JSONArray());

        String basePath = "candidates/" + candidateSlug + "invalid";
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 401);
        Assert.assertEquals(response.jsonPath().getString("message"), "Update Candidate : Access Denied");
        Assert.assertEquals(response.jsonPath().getString("message_type"), "is-danger");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "candidateEditData", groups = "nightly-build")
    public void editCandidateVerify422(String candidateSlug) {
        JSONObject candidate = new JSONObject();
        candidate.put("slug", candidateSlug);
        candidate.put("firstname", "");
        candidate.put("emailid", "invalid-email");

        JSONObject requestBody = new JSONObject();
        requestBody.put("candidate", candidate);
        requestBody.put("address_changed", false);
        requestBody.put("filesInfo", new JSONObject());
        requestBody.put("deleteResumeKey", "");
        requestBody.put("deleteEducation", new JSONArray());
        requestBody.put("deleteWork", new JSONArray());
        requestBody.put("sovrenData", new JSONArray());

        String basePath = "candidates/" + candidateSlug;
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, requestBody);

        Assert.assertEquals(response.getStatusCode(), 422);
        Assert.assertEquals(response.jsonPath().getString("message_type"), "is-danger");
    }

    @DataProvider(name = "candidateEditData")
    public Object[][] createCandidateEditData() {
        Response candidateResponse = function.createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
        JsonPath candidateJsonPath = candidateResponse.jsonPath();
        String candidateSlug = candidateJsonPath.getString("data.candidate.slug");
        return new Object[][]{{candidateSlug}};
    }
}
