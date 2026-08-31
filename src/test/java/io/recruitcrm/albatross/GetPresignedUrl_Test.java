package io.recruitcrm.albatross;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetPresignedUrl_Test extends TestBase {

    private String albatrossTknA;
    private String albatrossTknInvalidA;
    private String albatrossTknB;
    String basePath = "get-presigned-url";
    commanFunction function = new commanFunction();
    String accountAPIKey;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        accountAPIKey = getAccountApiKey("AccountA");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPresignedUrlDataProvider", groups = "nightly-build")
    public void getPresignedUrl_Test(String requestType, String key, String acl, String nextKey, String fileName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", requestType);
        queryParams.put("key", key);
        queryParams.put("acl", acl);
        queryParams.put("next_key", nextKey);
        queryParams.put("fileName", fileName);

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.preSignedUrl", Matchers.notNullValue());

    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void getPresignedUrlWithInvalidToken_Test() {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", "put");
        queryParams.put("key", "Candidates/test-slug/profilepic");
        queryParams.put("acl", "public-read");
        queryParams.put("next_key", "no");
        queryParams.put("fileName", "test-image.jpg");

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, queryParams, null, true);
        response.then().statusCode(401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getPresignedUrlWithMissingParameters_Test() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", "put");

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.preSignedUrl", Matchers.notNullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPresignedUrlDataProvider", groups = "nightly-build")
    public void getPresignedUrlWithCrossAccount_Test(String requestType, String key, String acl, String nextKey, String fileName) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", requestType);
        queryParams.put("key", key);
        queryParams.put("acl", acl);
        queryParams.put("next_key", nextKey);
        queryParams.put("fileName", fileName);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, queryParams, null, true);
        response.then().statusCode(404); // ERB: https://rcrm.atlassian.net/browse/SS-25858
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getPresignedUrlDataProvider", groups = "nightly-build")
    public void getPresignedUrlWithAdminToken_Test(String requestType, String key, String acl, String nextKey, String fileName) {
        String adminToken = getRoleBasedToken("AccountA", "Admin");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", requestType);
        queryParams.put("key", key);
        queryParams.put("acl", acl);
        queryParams.put("next_key", nextKey);
        queryParams.put("fileName", fileName);

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, queryParams, null, true);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.preSignedUrl", Matchers.notNullValue());
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getPresignedUrlDataProvider", groups = "nightly-build")
    public void getPresignedUrlWithTeamMemberToken_Test(String requestType, String key, String acl, String nextKey, String fileName) {
        String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", requestType);
        queryParams.put("key", key);
        queryParams.put("acl", acl);
        queryParams.put("next_key", nextKey);
        queryParams.put("fileName", fileName);

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, queryParams, null, true);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.preSignedUrl", Matchers.notNullValue());
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getPresignedUrlDataProvider", groups = "nightly-build")
    public void getPresignedUrlWithRestrictedTeamMemberToken_Test(String requestType, String key, String acl, String nextKey, String fileName) {
        String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", requestType);
        queryParams.put("key", key);
        queryParams.put("acl", acl);
        queryParams.put("next_key", nextKey);
        queryParams.put("fileName", fileName);

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedTeamMemberToken, queryParams, null, true);
        response.then().statusCode(404); //ERB: https://rcrm.atlassian.net/browse/SS-25862
    }

    @DataProvider
    public Object[][] getPresignedUrlDataProvider() {
        String slug = function.createNewCandidateWithMandatoryFields(baseURL, accountAPIKey).jsonPath().get("slug");
        return new Object[][] { { "put", "Candidates/" + slug + "/profilepic", "public-read", "no", "profile.jpg" } };
    }
}