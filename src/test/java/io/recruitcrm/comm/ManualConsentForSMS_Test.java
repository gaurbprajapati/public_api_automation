package io.recruitcrm.comm;

import com.qa.api.util.Owner;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.comm.RecordManualConsent;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.*;

@AccountType("Business|AlbatrossTkn")
public class ManualConsentForSMS_Test extends TestBase {

    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    String albatrossAuthToken;
    String basePath = "conversation/record-manual-consent";

    @BeforeClass(alwaysRun = true)
    public void Setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getCandidateEntityData", groups = "nightly-build")
    public void recordManualConsentForCandidateTest(int entityId, int entityTypeId) {
        RecordManualConsent payload = new RecordManualConsent(entityId, entityTypeId);

        Response response = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken, null, true, payload);

        response.then().statusCode(200);
        response.then().body("success", Matchers.is(true));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getContactEntityData", groups = "nightly-build")
    public void recordManualConsentForContactTest(int entityId, int entityTypeId) {
        RecordManualConsent payload = new RecordManualConsent(entityId, entityTypeId);

        Response response = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken, null, true, payload);

        response.then().statusCode(200);
        response.then().body("success", Matchers.is(true));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getCandidateEntityData", groups = "nightly-build")
    public void recordManualConsentAlreadyReceivedTest(int entityId, int entityTypeId) {
        RecordManualConsent payload = new RecordManualConsent(entityId, entityTypeId);

        Response firstResponse = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(firstResponse.getStatusCode(), 200, "Prerequisite: first manual consent call should succeed");

        Response response = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken, null, true, payload);

        response.then().statusCode(409);
        response.then().body("error", Matchers.is(true));
        response.then().body("errorCode", Matchers.is(409));
        response.then().body("errorMessage", Matchers.is("consent_already_received"));
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void recordManualConsentWithEmptyPayloadTest() {
        Response response = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken, null, true, new JSONObject());

        response.then().statusCode(422);
        response.then().body("error", Matchers.is(true));
        response.then().body("errorCode", Matchers.is(422));
        response.then().body("errorMessage", Matchers.is("The entity id field is required."));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getCandidateEntityData", groups = "nightly-build")
    public void recordManualConsentUnauthorizedTest(int entityId, int entityTypeId) {
        RecordManualConsent payload = new RecordManualConsent(entityId, entityTypeId);

        Response response = RestClient.doPost("JSON", commURL, basePath, albatrossAuthToken + "12345", null, true, payload);

        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

    @DataProvider
    public Object[][] getCandidateEntityData() {
        int candidateId = albatrossFunctions.createCandidate(albatrossURL, albatrossAuthToken).jsonPath().getInt("data.candidate.id");
        return new Object[][]{
            {candidateId, 5}
        };
    }

    @DataProvider
    public Object[][] getContactEntityData() {
        int contactId = albatrossFunctions.createContact(albatrossURL, albatrossAuthToken).jsonPath().getInt("data.contact.id");
        return new Object[][]{
            {contactId, 2}
        };
    }
}