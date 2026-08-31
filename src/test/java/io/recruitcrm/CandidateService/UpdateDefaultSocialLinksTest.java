package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class UpdateDefaultSocialLinksTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "socialLinksTestData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDefaultSocialLinks_Success(int candidateId, int entityId, String customColumnId) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("xingUrl", "https://www.xing.com");
        requestBody.put("linkedinUrl", "https://www.linkedin.com");
        requestBody.put("twitterUrl", "https://www.twitter.com");
        requestBody.put("facebookUrl", "https://www.fb.com");
        requestBody.put("githubUrl", "https://www.github.com");
        requestBody.put("candidateId", candidateId);
        requestBody.put("entityId", entityId);

        // Create socialFieldUrls array
        JSONArray socialFieldUrls = new JSONArray();
        JSONObject socialField = new JSONObject();
        socialField.put("customColumnId", customColumnId);
        socialField.put("url", "https://www.linkedinsingle.com");
        socialFieldUrls.put(socialField);
        requestBody.put("socialFieldUrls", socialFieldUrls);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/update-default-social-links",
        albatrossTkn, null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        // Validate response structure
        JsonPath jp = response.jsonPath();
        assertThat("Silent progress should be false", jp.get("silent_progress"), equalTo(false));
        assertThat("Message should match expected", jp.get("message"), equalTo("Social links updated successfully."));
        assertThat("Message type should be is-success", jp.get("message_type"), equalTo("is-success"));
        assertThat("Data should not be null", jp.get("data"), notNullValue());

        // Validate candidate data
        assertThat("Candidate ID should match", jp.get("data.id"), equalTo(candidateId));
        assertThat("First name should not be null", jp.get("data.firstname"), notNullValue());
        assertThat("Last name should not be null", jp.get("data.lastname"), notNullValue());
        assertThat("Email should not be null", jp.get("data.emailid"), notNullValue());
        assertThat("Slug should not be null", jp.get("data.slug"), notNullValue());

        // Validate social links are updated
        assertThat("Facebook URL should be updated", jp.get("data.profilefacebook"), equalTo("https://www.fb.com"));
        assertThat("GitHub URL should be updated", jp.get("data.profilegithub"), equalTo("https://www.github.com"));
        assertThat("Twitter URL should be updated", jp.get("data.profiletwitter"), equalTo("https://www.twitter.com"));
        assertThat("LinkedIn URL should be updated", jp.get("data.profilelinkedin"), equalTo("https://www.linkedin.com"));
        assertThat("Xing URL should be updated", jp.get("data.profilexing"), equalTo("https://www.xing.com"));

        // Validate timestamps
        assertThat("Created on should not be null", jp.get("data.createdon"), notNullValue());
        assertThat("Updated on should not be null", jp.get("data.updatedon"), notNullValue());
        assertThat("Updated by should not be null", jp.get("data.updatedby"), notNullValue());

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/updateDefaultSocialLinks.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "socialLinksTestData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDefaultSocialLinks_WithoutAuth(int candidateId, int entityId, String customColumnId) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("xingUrl", "https://www.xing.com");
        requestBody.put("linkedinUrl", "https://www.linkedin.com");
        requestBody.put("twitterUrl", "https://www.twitter.com");
        requestBody.put("facebookUrl", "https://www.fb.com");
        requestBody.put("githubUrl", "https://www.github.com");
        requestBody.put("candidateId", candidateId);
        requestBody.put("entityId", entityId);

        // Make API call without auth
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/update-default-social-links",
                null, null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "socialLinksTestData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDefaultSocialLinks_InvalidAuth(int candidateId, int entityId, String customColumnId) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("xingUrl", "https://www.xing.com");
        requestBody.put("linkedinUrl", "https://www.linkedin.com");
        requestBody.put("twitterUrl", "https://www.twitter.com");
        requestBody.put("facebookUrl", "https://www.fb.com");
        requestBody.put("githubUrl", "https://www.github.com");
        requestBody.put("candidateId", candidateId);
        requestBody.put("entityId", entityId);

        // Make API call with invalid auth
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/update-default-social-links",
                albatrossTkn + "invalid_token", null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "socialLinksTestData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDefaultSocialLinks_InvalidCandidateId(int candidateId, int entityId, String customColumnId) {
        // Create request body with invalid candidate ID
        JSONObject requestBody = new JSONObject();
        requestBody.put("xingUrl", "https://www.xing.com");
        requestBody.put("linkedinUrl", "https://www.linkedin.com");
        requestBody.put("twitterUrl", "https://www.twitter.com");
        requestBody.put("facebookUrl", "https://www.fb.com");
        requestBody.put("githubUrl", "https://www.github.com");
        requestBody.put("candidateId", 999999999);
        requestBody.put("entityId", entityId);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/update-default-social-links",
                albatrossTkn, null, null, true, requestBody.toString());

        // status code is 200 but data is empty array
        assertThat("Expected status code 200 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(200));
        assertThat("Should not have access to other account's data",
                (Integer) response.jsonPath().get("data.size()"), equalTo(0));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "socialLinksTestData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDefaultSocialLinks_MissingCandidateId(int candidateId, int entityId, String customColumnId) {
        // Create request body without candidate ID
        JSONObject requestBody = new JSONObject();
        requestBody.put("xingUrl", "https://www.xing.com");
        requestBody.put("linkedinUrl", "https://www.linkedin.com");
        requestBody.put("twitterUrl", "https://www.twitter.com");
        requestBody.put("facebookUrl", "https://www.fb.com");
        requestBody.put("githubUrl", "https://www.github.com");
        requestBody.put("entityId", entityId);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/update-default-social-links",
                albatrossTkn, null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 422 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(422));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "socialLinksTestData", groups = {"candidate_service", "nightly-build"})
    public void testUpdateDefaultSocialLinks_MissingEntityId(int candidateId, int entityId, String customColumnId) {
        // Create request body without entity ID
        JSONObject requestBody = new JSONObject();
        requestBody.put("xingUrl", "https://www.xing.com");
        requestBody.put("linkedinUrl", "https://www.linkedin.com");
        requestBody.put("twitterUrl", "https://www.twitter.com");
        requestBody.put("facebookUrl", "https://www.fb.com");
        requestBody.put("githubUrl", "https://www.github.com");
        requestBody.put("candidateId", candidateId);

        // Make API call
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/update-default-social-links",
                albatrossTkn, null, null, true, requestBody.toString());

        // Validate response
        assertThat("Expected status code 422 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(422));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void testUpdateDefaultSocialLinks_EmptyRequestBody() {
        // Make API call with empty request body
        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/update-default-social-links",
                albatrossTkn, null, null, true, "{}");

        // Validate response
        assertThat("Expected status code 422 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(422));
    }

    @DataProvider(name = "socialLinksTestData")
    public Object[][] getSocialLinksTestData() {
        // Step 1: Get an existing candidate by slug
        String candidateSlug = function.getEntityResponse(baseURL, apiAuthToken, "candidate");
        assertThat("Failed to get candidate slug", candidateSlug, notNullValue());
        
        Response candidateResponse = albatrossFunctions.getCandidateResponse(albatrossURL, albatrossTkn, candidateSlug);
        assertThat("Failed to get candidate by slug", candidateResponse.getStatusCode(), equalTo(200));
        
        JsonPath candidateJp = candidateResponse.jsonPath();
        int candidateId = candidateJp.get("data.candidate.id");
        assertThat("Candidate ID should not be null", candidateId, notNullValue());
        
        // Step 2: Create a social_profile custom field for candidate using existing function
        String customFieldName = "Social Profile Test Field";
        String customFieldType = "social_profile";
        String defaultOptions = ""; // No options needed for social_profile type
        
        Response customFieldResponse = function.createCustomFieldsResponse(albatrossURL, albatrossTkn, 
                "candidate", customFieldName, customFieldType, defaultOptions);
        assertThat("Failed to create custom field", customFieldResponse.getStatusCode(), equalTo(200));
        
        JsonPath customFieldJp = customFieldResponse.jsonPath();
        int actualCustomColumnId = customFieldJp.get("data.custumField.columnid");
        assertThat("Custom column ID should not be null", actualCustomColumnId, notNullValue());
        
        return new Object[][] { { candidateId, 5, String.valueOf("custcolumn" + actualCustomColumnId) } };
    }
}
