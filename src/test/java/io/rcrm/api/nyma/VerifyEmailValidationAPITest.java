package io.rcrm.api.nyma;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.nyma.EmailValidationRequest;
import io.rcrm.api.pojo.reaper.UpdateEntityRequest;
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

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email")
public class VerifyEmailValidationAPITest extends TestBase {

    commanFunction function = new commanFunction();
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    private static final String basePath = "emails/validate";
    String accountAPIKey;
    String privateAPIKey;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        accountAPIKey = ThreadManager.getAccountApiKey();
        privateAPIKey = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Harika")
    @Test(dataProvider = "getValidEntityData", groups = "nightly-build")
    public void verifyEmailValidationWithValidEntities(int entityId, int entityTypeId) {

        List<Integer> entityIds = Collections.singletonList(entityId);
        EmailValidationRequest request = new EmailValidationRequest(entityIds, entityTypeId);

        Response response = RestClient.doPost("JSON", nymaURL, basePath, privateAPIKey, null, true, request);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for entity ID: " + entityId + " and entity type: " + entityTypeId);
        if(entityTypeId==5) {
            response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/nyma/validateEmailCandidate.json"));
            response.then().body("[0].emailid", notNullValue());
        } else if(entityTypeId==2) {
            response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/nyma/validateEmailContact.json"));
            response.then().body("[0].email", notNullValue());
        }
        response.then().body("[0].id.toString()", is(String.valueOf(entityId)));
        response.then().body("[0].slug", notNullValue());
        response.then().body("[0].email_opt_out", equalTo(0));
        response.then().body("[0].valid", equalTo(true));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void verifyEmailValidationWithEmptyRequest() {

        Response response = RestClient.doPost("JSON", nymaURL, basePath, privateAPIKey, null, true, null);

        response.then().statusCode(422);
        response.then().body("message", equalTo("The entity ids field is required.,The entity type id field is required."));
        response.then().body("message_type", equalTo("is-danger"));
        response.then().body("status", equalTo("fail"));

    }

    @Owner("Harika")
    @Test(dataProvider = "getInvalidEntityData", groups = "nightly-build")
    public void verifyEmailValidationWithInValidData(int entityId, int entityTypeId) {

        List<Integer> entityIds = Collections.singletonList(entityId);
        EmailValidationRequest request = new EmailValidationRequest(entityIds, entityTypeId);

        Response response = RestClient.doPost("JSON", nymaURL, basePath, privateAPIKey, null, true, request);

        if (entityId == 99999999) {
            response.then().statusCode(200);
            response.then().body("message", equalTo("No entities found"));
        } else {
            response.then().statusCode(422);
            response.then().body("message", equalTo("The selected entity type id is invalid."));
        }
        response.then().body("message_type", equalTo("is-danger"));

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void verifyEmailValidationWithInvalidCandidateData() {

        List<Integer> entityIds = getInvalidCandidateData();
        EmailValidationRequest request = new EmailValidationRequest(entityIds, 5);

        Response response = RestClient.doPost("JSON", nymaURL, basePath, privateAPIKey, null, true, request);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for invalid candidate data");

        response.then().body("[0].id.toString()", is(String.valueOf(entityIds.get(0)))).body("[0].error", is("incorrect_email")).body("[0].valid", equalTo(false));
        response.then().body("[1].id.toString()", is(String.valueOf(entityIds.get(1)))).body("[1].error", is("no_email")).body("[1].valid", equalTo(false));
        response.then().body("[2].id.toString()", is(String.valueOf(entityIds.get(2)))).body("[2].error", is("opted_out_of_email")).body("[2].valid", equalTo(false));

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void verifyEmailValidationWithInvalidAuth() {

        List<Integer> entityIds = Collections.singletonList(9999);
        EmailValidationRequest request = new EmailValidationRequest(entityIds, 5);

        Response response = RestClient.doPost("JSON", nymaURL, basePath, privateAPIKey + "invalid", null, true, request);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401 for invalid token");
    }

    @DataProvider(parallel = true)
    public Object[][] getValidEntityData() {

        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
        String candidateEntitySlug = jsonCandidate.get("slug");
        Response getCandResponse = albatrossFunctions.getCandidateResponse(albatrossURL, privateAPIKey, candidateEntitySlug);
        JsonPath jpCandidate = getCandResponse.jsonPath();
        int candidateId = jpCandidate.get("data.candidate.id");

        JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
        String companyEntitySlug = jsonCompany.get("slug");
        JsonPath jsonContact = function.createNewContact_POST(baseURL, accountAPIKey, companyEntitySlug)
                .jsonPath();

        String contactEntitySlug = jsonContact.get("slug");
        Response getContactResponse = albatrossFunctions.getContactResponse(albatrossURL, privateAPIKey,
                contactEntitySlug);
        JsonPath jpContact = getContactResponse.jsonPath();
        int contactId = Integer.parseInt(jpContact.get("data.contact.id"));

        return new Object[][] {
            {candidateId, 5},  // Valid candidate
            {contactId, 2},  // Valid contact
        };
    }

    @DataProvider(parallel = true)
    public Object[][] getInvalidEntityData() {
        return new Object[][] {
            {99999999, 5},  // Invalid candidate ID
            {57312012, 999}  // Invalid entity type ID
        };
    }


    public List<Integer> getInvalidCandidateData() {
        List<String> candidateSlugs = new ArrayList<>();
        List<Integer> candidateIds = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath();
            candidateSlugs.add(jsonCandidate.get("slug"));
        }

        // Apply specific invalid updates to each candidate
        updateEntityColumn(candidateSlugs.get(0), "emailid", "test@yopmail.com."); // Invalid email
        updateEntityColumn(candidateSlugs.get(1), "emailid", ""); // Empty email
        updateEntityColumn(candidateSlugs.get(2), "email_opt_out", "1"); // Opted out
        updateEntityColumn(candidateSlugs.get(0), "email_opt_out", "0");
        updateEntityColumn(candidateSlugs.get(1), "email_opt_out", "0");

        for (String slug : candidateSlugs) {
            Response response = albatrossFunctions.getCandidateResponse(albatrossURL, privateAPIKey, slug);
            JsonPath json = response.jsonPath();
            int id = json.getInt("data.candidate.id");
            candidateIds.add(id);
        }

        return candidateIds;
    }

    private void updateEntityColumn(String slug, String column, String value) {
        Map<String, String> columnsAndValue = new HashMap<>();
        columnsAndValue.put(column, value);
        UpdateEntityRequest updateEntityRequest = new UpdateEntityRequest();
        updateEntityRequest.setEntityType("candidate");
        updateEntityRequest.setColumnsAndValue(columnsAndValue);
        ReaperIntegration.updateEntityColumns(slug, updateEntityRequest);
    }
} 