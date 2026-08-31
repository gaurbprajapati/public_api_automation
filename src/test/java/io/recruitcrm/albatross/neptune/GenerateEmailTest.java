package io.recruitcrm.albatross.neptune;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.neptune.JavaFakerSummary;
import io.rcrm.api.pojo.neptune.GenerateEmail;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GenerateEmailTest extends TestBase {

    private JavaFakerSummary javaFakerSummary = new JavaFakerSummary();

    // Cross account test fields
    private String tokenA;
    private String publicAPIKeyA;
    private String tokenB;
    private String publicAPIKeyB;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        tokenA = getTokenForAccount("AccountA", "valid");
        publicAPIKeyA = getAccountApiKey("AccountA");
        tokenB = getTokenForAccount("AccountB", "valid");
        publicAPIKeyB = getAccountApiKey("AccountB");
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "getData", groups = "nightly-build")
    public void generateEmailWithMandatoryParameters_Test(String key) {
        String prompt;
        String basePath = "generate-email";
        GenerateEmail generateEmail = new GenerateEmail();

        generateEmail.setKey(key);
        if (key.equals("manual_prompt")) {
            prompt = javaFakerSummary.getPromptText();
            generateEmail.setPrompt(prompt);
        }

        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true, generateEmail);
        Assert.assertEquals(response.getStatusCode(), 200);

        response.then().body("data.key", Matchers.is(key));
        response.then().body("meta.message", Matchers.is("Email generated successfully"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void generateEmailWithOptionalParametersForCandidate_Test() {
        commanFunction function = new commanFunction();

        Response response = function.createNewCandidateWithMandatoryFields(baseURL, publicAPIKeyA);
        JsonPath jp = response.jsonPath();
        String slug = jp.get("slug");

        AllCrudFunctions crudFunctions = new AllCrudFunctions();
        Response response1 = crudFunctions.getCandidateResponse(albatrossURL, tokenA, slug);
        jp = response1.jsonPath();

        int record_id = Integer.parseInt(jp.get("data.candidate.id").toString());
        String tone = javaFakerSummary.getRandomString(5);
        String prompt = javaFakerSummary.getPromptText();
        String last_response = javaFakerSummary.getRandomString(20);
        String entity = "candidates";
        String basePath = "generate-email";

        GenerateEmail generateEmail = new GenerateEmail(tone, prompt, "schedule_interview", last_response, entity, record_id);
        Response response2 = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true, generateEmail);

        Assert.assertEquals(response2.getStatusCode(), 200);
        response2.then().body("data.key", Matchers.is("schedule_interview"));
        response2.then().body("meta.message", Matchers.is("Email generated successfully"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void generateEmailWithOptionalParametersForContacts_Test() {
        commanFunction function = new commanFunction();

        Response response = function.createNewCompanyWithMandatoryFields(baseURL, publicAPIKeyA);
        JsonPath jp = response.jsonPath();
        String companySlug = jp.get("slug");

        Response response1 = function.createNewContact_POST(baseURL, publicAPIKeyA, companySlug);
        jp = response1.jsonPath();
        String slug = jp.get("slug");

        AllCrudFunctions crudFunctions = new AllCrudFunctions();
        Response response2 = crudFunctions.getContactResponse(albatrossURL, tokenA, slug);
        jp = response2.jsonPath();

        int record_id = Integer.parseInt(jp.get("data.contact.id").toString());
        String tone = javaFakerSummary.getRandomString(5);
        String prompt = javaFakerSummary.getPromptText();
        String last_response = javaFakerSummary.getRandomString(20);
        String entity = "contacts";
        String basePath = "generate-email";

        GenerateEmail generateEmail = new GenerateEmail(tone, prompt, "schedule_interview", last_response, entity, record_id);
        Response response3 = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true, generateEmail);

        Assert.assertEquals(response3.getStatusCode(), 200);
        response3.then().body("data.key", Matchers.is("schedule_interview"));
        response3.then().body("meta.message", Matchers.is("Email generated successfully"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void generateEmailWithInvalidEntity_Test() {
        int record_id = javaFakerSummary.getCandidateId();
        String key = "schedule_interview";
        String tone = javaFakerSummary.getRandomString(5);
        String prompt = javaFakerSummary.getPromptText();
        String last_response = javaFakerSummary.getRandomString(20);
        String entity = javaFakerSummary.getRandomString(7);
        String basePath = "generate-email";

        GenerateEmail generateEmail = new GenerateEmail(tone, prompt, key, last_response, entity, record_id);
        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true, generateEmail);

        Assert.assertEquals(response.getStatusCode(), 400);
        response.then().body("data", Matchers.nullValue());
        response.then().body("errors.errors.body.errorMsg", Matchers.is("entity is invalid"));
        response.then().assertThat().body(
                JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//validationErrorResponse.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void generateEmailWithInvalidRecordId_Test() {
        int record_id = javaFakerSummary.getCandidateId();
        String key = "schedule_interview";
        String tone = javaFakerSummary.getRandomString(5);
        String prompt = javaFakerSummary.getPromptText();
        String last_response = javaFakerSummary.getRandomString(20);
        String entity = "candidates";
        String basePath = "generate-email";

        GenerateEmail generateEmail = new GenerateEmail(tone, prompt, key, last_response, entity, record_id);
        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true, generateEmail);

        Assert.assertEquals(response.getStatusCode(), 400);
        response.then().body("data", Matchers.nullValue());
        response.then().body("errors.errors.body.errorMsg", Matchers.is("candidates not found"));
        response.then().assertThat().body(
                JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//validationErrorResponse.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void generateEmailWithInvalidKey_Test() {
        int record_id = javaFakerSummary.getCandidateId();
        String key = javaFakerSummary.getRandomKey();
        String tone = javaFakerSummary.getRandomString(5);
        String prompt = javaFakerSummary.getPromptText();
        String last_response = javaFakerSummary.getRandomString(20);
        String entity = "candidates";
        String basePath = "generate-email";

        GenerateEmail generateEmail = new GenerateEmail(tone, prompt, key, last_response, entity, record_id);
        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true, generateEmail);

        Assert.assertEquals(response.getStatusCode(), 400);
        response.then().body("data", Matchers.nullValue());
        response.then().body("errors.errors.key.errorMsg", Matchers.is("key is invalid"));
        response.then().assertThat().body(
                JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//nullValuesValidation.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void generateEmailWithNullValues_Test() {
        String basePath = "generate-email";
        GenerateEmail generateEmail = new GenerateEmail(null, null, null, null, null, 0);

        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA, null, true, generateEmail);

        Assert.assertEquals(response.getStatusCode(), 400);
        response.then().body("data", Matchers.nullValue());
        response.then().body("errors.errors.key.errorMsg", Matchers.is("Input should be a valid string"));
        response.then().assertThat().body(
                JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//validationErrorResponse.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void unauthorizedUserCannotGenerateEmail_Test() {
        String basePath = "generate-email";
        GenerateEmail generateEmail = new GenerateEmail();

        Response response = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenA + "123", null, true, generateEmail);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("detail", Matchers.is("Unauthorized"));
        response.then().assertThat().body(
                JsonSchemaValidator.matchesJsonSchemaInClasspath("schemaValidation//rcrm//unauthorizedGPTAccess.json"));
    }

    @Owner("Suhel Bhadane")
    @Test(groups = "nightly-build")
    public void testValidCrossAccountGenerateEmail() {
        commanFunction function = new commanFunction();
        Response response = function.createNewCandidateWithMandatoryFields(baseURL, publicAPIKeyA);
        JsonPath jp = response.jsonPath();
        String slug = jp.get("slug");

        AllCrudFunctions crudFunctions = new AllCrudFunctions();
        Response response1 = crudFunctions.getCandidateResponse(albatrossURL, tokenA, slug);
        jp = response1.jsonPath();
        int record_id = Integer.parseInt(jp.get("data.candidate.id").toString());

        String tone = javaFakerSummary.getRandomString(5);
        String prompt = javaFakerSummary.getPromptText();
        String last_response = javaFakerSummary.getRandomString(20);
        String entity = "candidates";
        String basePath = "generate-email";

        GenerateEmail generateEmail = new GenerateEmail(tone, prompt, "schedule_interview", last_response, entity, record_id);
        Response response2 = RestClient.doPost("JSON", neptuneServiceURL, basePath, tokenB, null, true, generateEmail);

        Assert.assertEquals(response2.getStatusCode(), 400);
        response2.then().body("data", Matchers.nullValue());
        response2.then().body("errors.errors.body.errorMsg", Matchers.is("candidates not found"));
    }

    @DataProvider(parallel = true)
    public Object[] getData() {
        return new Object[]{
                "schedule_interview",
                "candidate_outreach",
                "application_status",
                "interview_feedback",
                "rejection_email",
                "job_offer_email",
                "client_outreach",
                "suggestive_prompt",
                "manual_prompt"
        };
    }
}