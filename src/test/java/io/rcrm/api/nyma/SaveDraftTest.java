package io.rcrm.api.nyma;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.nyma.DraftBody;
import io.rcrm.api.pojo.nyma.Recipients;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1")
public class SaveDraftTest extends TestBase {

    commanFunction function = new commanFunction();
    int draftId;
    String albatrossAuthToken;
    String apiToken;
    String candidateEntitySlug;
    int accountOwnerid;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Ajendra Singh")
    @Test(priority = 0, dataProvider = "getTestData", groups = "nightly-build")
    public void saveDraft(int accountOwnerid, String candidateEntitySlug) throws InterruptedException {
        DraftBody draftBody = new DraftBody();
        draftBody.setFrom(accountOwnerid);
        Recipients recipients = new Recipients();
        recipients.setIdentifier(candidateEntitySlug);
        recipients.setType("candidate");
        draftBody.setTo(recipients);
        draftBody.setBody("This is a test email");
        draftBody.setSubject("Test Email");
        draftBody.setIncludeSignature(true);
        draftBody.setIncludeOptOutLink(true);

        String basePath = "drafts";

        Response response = RestClient.doPost("JSON", baseURL, basePath, apiToken, null, true, draftBody);
        assert response != null;

        response.then().statusCode(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//email//createDraft.json"));
        response.then().body("message", Matchers.is("We are currently processing your request"));
        response.then().body("draft_status_id", Matchers.notNullValue());
        draftId = response.jsonPath().get("draft_status_id");
        Thread.sleep(1000);     //1 second of sleep for the draft to be created
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "getTestData", priority = 1, groups = "nightly-build")
    public void saveDraftInvalidRequest(int accountOwnerid, String candidateEntitySlug){
        DraftBody draftBody = new DraftBody();
        draftBody.setFrom(accountOwnerid);
        Recipients recipients = new Recipients();
        recipients.setIdentifier(candidateEntitySlug);
        recipients.setType("candidate");
        draftBody.setTo(recipients);
        draftBody.setBody("This is a test email");
        draftBody.setSubject("");                       //no email subject
        draftBody.setIncludeSignature(true);
        draftBody.setIncludeOptOutLink(true);

        String basePath = "drafts";

        Response response = RestClient.doPost("JSON", baseURL, basePath, apiToken, null, true,
                draftBody);
        assert response != null;
        response.then().statusCode(422);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//email//createDraftInvalid.json"));
        response.then().body("subject[0]", Matchers.is("Subject is required"));
    }

    @Owner("Ajendra Singh")
    @Test(priority = 3, groups = "nightly-build")
    public void getDraftStatus(){
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("draftId", String.valueOf(draftId));

        String basePath = "drafts/status/{draftId}";

        Response response = RestClient.doGet("JSON", baseURL, basePath, apiToken,null,
                pathParamters, true);

        assert response != null;
        response.then().statusCode(200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//email//draftStatusSuccess.json"));
        response.then().body("message", Matchers.is("Draft saved successfully"));
    }

    @Owner("Ajendra Singh")
    @Test (priority = 2, groups = "nightly-build")
    public void getDraftStatusNotFound(){
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("draftId", String.valueOf(draftId+1234));

        String basePath = "drafts/status/{draftId}";

        Response response = RestClient.doGet("JSON", baseURL, basePath, apiToken,null,
                pathParamters, true);

        assert response != null;
        response.then().statusCode(404);
        response.then().body("message", Matchers.is("Draft Status ID is invalid"));
    }

    @DataProvider
    public Object[][] getTestData() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL,  apiToken).jsonPath();
        candidateEntitySlug = jsonCandidate.get("slug");
        Response usersResponse = function.getUsers(baseURL, apiToken);
        usersResponse.then().statusCode(200);
        JsonPath user = usersResponse.jsonPath();
        accountOwnerid = user.get("[0].id");
        return new Object[][]{{accountOwnerid, candidateEntitySlug}};
    }
}