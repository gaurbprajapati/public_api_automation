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
public class GetExternalPagesPresignedUrl_Test extends TestBase {

    String basePath = "external-pages/get-presigned-url";
    commanFunction function = new commanFunction();
    String accountAPIKey;
    private String albatrossTknA;
    private String albatrossTknInvalidA;
    private String albatrossTknB;
    @BeforeClass(alwaysRun = true)    public void setUp() {
        accountAPIKey = getAccountApiKey("AccountA");
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getExternalPagesPresignedUrlDataProvider", groups = "nightly-build")
    public void getExternalPagesPresignedUrl_Test(String requestType, String direct, String accountid) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", requestType);
        queryParams.put("direct", direct);
        queryParams.put("accountid", accountid);

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("status", Matchers.containsString("fail"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void getExternalPagesPresignedUrlWithInvalidToken_Test() {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", "get");
        queryParams.put("direct", "true");
        queryParams.put("accountid", "ankzbExPT0xXTENzS3NKV09uNHVYZz09");

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknInvalidA, queryParams, null, true);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("status", Matchers.containsString("fail"));
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void getExternalPagesPresignedUrlWithMissingParameters_Test() {
        Map<String, String> queryParams = new HashMap<>();

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknA, queryParams, null, true);
        response.then().statusCode(422);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("The request type field is required."));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getExternalPagesPresignedUrlDataProvider", groups = "nightly-build")
    public void getExternalPagesPresignedUrlWithCrossAccount_Test(String requestType, String direct, String accountid) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", requestType);
        queryParams.put("direct", direct);
        queryParams.put("accountid", accountid);

        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTknB, queryParams, null, true);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("status", Matchers.containsString("fail"));
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getExternalPagesPresignedUrlDataProvider", groups = "nightly-build")
    public void getExternalPagesPresignedUrlWithAdminToken_Test(String requestType, String direct, String accountid) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", requestType);
        queryParams.put("direct", direct);
        queryParams.put("accountid", accountid);

        String adminToken = getRoleBasedToken("AccountA", "Admin");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, adminToken, queryParams, null, true);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("status", Matchers.containsString("fail"));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getExternalPagesPresignedUrlDataProvider", groups = "nightly-build")
    public void getExternalPagesPresignedUrlWithTeamMemberToken_Test(String requestType, String direct, String accountid) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", requestType);
        queryParams.put("direct", direct);
        queryParams.put("accountid", accountid);

        String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, teamMemberToken, queryParams, null, true);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("status", Matchers.containsString("fail"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getExternalPagesPresignedUrlDataProvider", groups = "nightly-build")
    public void getExternalPagesPresignedUrlWithRestrictedTeamMemberToken_Test(String requestType, String direct, String accountid) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requestType", requestType);
        queryParams.put("direct", direct);
        queryParams.put("accountid", accountid);
        String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, restrictedTeamMemberToken, queryParams, null, true);
        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("status", Matchers.containsString("fail"));
    }

    @DataProvider
    public Object[][] getExternalPagesPresignedUrlDataProvider() {
        String accountid = function.encryptAccountId(String.valueOf(getAccountId("AccountA")));
        return new Object[][] { { "get", "true", accountid } };
    }
}
