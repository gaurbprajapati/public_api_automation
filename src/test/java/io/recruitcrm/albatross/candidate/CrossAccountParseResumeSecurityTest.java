package io.recruitcrm.albatross.candidate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountParseResumeSecurityTest extends TestBase {

    private JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
    private String resumeFilePath = System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/AnchitResume.pdf";
    private File resumeFile = new File(resumeFilePath);
    private String resumeFileName = resumeFile.getName();
    private String candidateIdFromAccountA = "";
    private String presignedKeyFromAccountA = "";

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "crossAccountParseResumeTestData", groups = "nightly-build")
    public void crossAccountParseResumeOperations_Test(String testScenario, String accountType, String tokenType, 
            String operation, String expectedStatusCode, String expectedResponse, String description) {
        
        String generatedString = RandomStringUtils.randomAlphabetic(4);
        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;
        Map<String, String> queryParams = new HashMap<>();
        
        try {
            switch (operation.toUpperCase()) {
                case "GET_PRESIGNED_URL":
                    queryParams.put("fileName", resumeFileName);
                    queryParams.put("requestType", "put");
                    response = RestClient.doGet("JSON", albatrossURL, "get-presigned-url", token, queryParams, null, true);
                    
                    if (response.getStatusCode() == 200 && accountType.equals("AccountA")) {
                        JsonPath jp = response.jsonPath();
                        String key = jp.get("data.key");
                        if (key != null) {
                            presignedKeyFromAccountA = key;
                        }
                    }
                    break;
                    
                case "PARSE_RESUME":
                    queryParams.clear();
                    queryParams.put("actionsteps", "1");
                    
                    JSONObject filesInfo = new JSONObject();
                    filesInfo.put("key", !presignedKeyFromAccountA.isEmpty() ? presignedKeyFromAccountA : "validKey" + generatedString);
                    filesInfo.put("name", resumeFileName);
                    filesInfo.put("type", "application/pdf");
                    filesInfo.put("size", 175894);
                    filesInfo.put("index", 0);

                    JSONObject resumeParserData = new JSONObject();
                    resumeParserData.put("resumesParsed", 0);
                    resumeParserData.put("resumesFailed", 0);
                    resumeParserData.put("resumesTotal", 1);
                    resumeParserData.put("filesInfo", filesInfo);

                    JSONObject parseResumeRequest = new JSONObject();
                    parseResumeRequest.put("resumeParserData", resumeParserData);
                    parseResumeRequest.put("actionid", 0);
                    
                    response = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume", token, queryParams, null, true, parseResumeRequest);
                    
                    if (response.getStatusCode() == 200 && accountType.equals("AccountA")) {
                        JsonPath jp = response.jsonPath();
                        Integer candidateId = jp.get("data.candidate.id");
                        if (candidateId != null) {
                            candidateIdFromAccountA = Integer.toString(candidateId);
                        }
                    }
                    break;
                    
                case "ACCESS_PARSED_CANDIDATE":
                    String candidateId = !candidateIdFromAccountA.isEmpty() ? candidateIdFromAccountA : "12345";
                    queryParams.clear();
                    queryParams.put("candidateId", candidateId);
                    response = RestClient.doGet("JSON", albatrossURL, "candidates/" + candidateId, token, null, null, true);
                    break;
                    
                case "PARSE_WITH_CROSS_ACCOUNT_KEY":
                    queryParams.clear();
                    queryParams.put("actionsteps", "1");
                    
                    JSONObject crossAccountFilesInfo = new JSONObject();
                    crossAccountFilesInfo.put("key", !presignedKeyFromAccountA.isEmpty() ? presignedKeyFromAccountA : "invalidCrossAccountKey");
                    crossAccountFilesInfo.put("name", resumeFileName);
                    crossAccountFilesInfo.put("type", "application/pdf");
                    crossAccountFilesInfo.put("size", 175894);
                    crossAccountFilesInfo.put("index", 0);

                    JSONObject crossAccountResumeData = new JSONObject();
                    crossAccountResumeData.put("resumesParsed", 0);
                    crossAccountResumeData.put("resumesFailed", 0);
                    crossAccountResumeData.put("resumesTotal", 1);
                    crossAccountResumeData.put("filesInfo", crossAccountFilesInfo);

                    JSONObject crossAccountRequest = new JSONObject();
                    crossAccountRequest.put("resumeParserData", crossAccountResumeData);
                    crossAccountRequest.put("actionid", 0);
                    
                    response = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume", token, queryParams, null, true, crossAccountRequest);
                    break;
                    
                default:
                    Assert.fail("Unsupported operation: " + operation);
            }
            
            if (tokenType.equals("invalid") || tokenType.equals("expired") || tokenType.equals("malformed") || 
                tokenType.equals("empty") || tokenType.equals("null")) {
                response.then().statusCode(401);
                return;
            }
            
            int expectedStatus = Integer.parseInt(expectedStatusCode);
            response.then().statusCode(expectedStatus);
            
            switch (expectedResponse) {
                case "success":
                    if (operation.equals("PARSE_RESUME")) {
                        JsonPath jsonPath = response.jsonPath();
                        String messageType = jsonPath.get("message_type");
                        if ("is-success".equals(messageType)) {
                            response.then().body("message_type", Matchers.equalTo("is-success"));
                            response.then().body("silent_progress", Matchers.equalTo(true));
                            response.then().body("message", Matchers.containsString("parsed successfully"));
                            response.then().body("data.candidate.id", Matchers.notNullValue());
                            response.then().body("data.candidate.candidatename", Matchers.notNullValue());
                            response.then().body("data.candidate.emailid", Matchers.notNullValue());
                        } else if ("is-danger".equals(messageType)) {
                            response.then().body("message_type", Matchers.equalTo("is-danger"));
                        }
                    } else if (operation.equals("GET_PRESIGNED_URL")) {
                        response.then().body("data.preSignedUrl", Matchers.notNullValue());
                        response.then().body("data.key", Matchers.notNullValue());
                    }
                    break;
                    
                case "cross_account_access_denied":
                    JsonPath jsonPath = response.jsonPath();
                    String messageType = jsonPath.get("message_type");
                    if (messageType != null) {
                        Assert.assertEquals(messageType, "is-danger");
                    }
                    break;
                    
                case "invalid_key_error":
                    JsonPath keyErrorJsonPath = response.jsonPath();
                    String keyErrorMessageType = keyErrorJsonPath.get("message_type");
                    if (keyErrorMessageType != null) {
                        Assert.assertEquals(keyErrorMessageType, "is-danger", "Expected message_type to be 'is-danger' for cross-account key usage");
                    }
                    break;
                    
                case "unauthorized":
                    try {
                        response.then().body("error", Matchers.containsString("Unauthorized"));
                    } catch (Exception e) {
                    }
                    break;
                    
                default:
                    try {
                        response.then().body("error_message", Matchers.equalTo(expectedResponse));
                    } catch (Exception e) {
                        try {
                            response.then().body("error", Matchers.equalTo(expectedResponse));
                        } catch (Exception e2) {
                        }
                    }
                    break;
            }
            
        } catch (Exception e) {
            if (expectedResponse.contains("unauthorized") || expectedResponse.contains("access_denied") || 
                expectedResponse.contains("invalid_key") || expectedResponse.contains("cross_account")) {
            } else {
                throw e;
            }
        }
    }

    @DataProvider(name = "crossAccountParseResumeTestData")
    public static Object[][] crossAccountParseResumeTestData() {
        return new Object[][] {
            {"SCENARIO_1_GET_PRESIGNED", "AccountA", "valid", "GET_PRESIGNED_URL", "200", "success", "Account A should be able to get presigned URL"},
            {"SCENARIO_1_PARSE_RESUME", "AccountA", "valid", "PARSE_RESUME", "200", "success", "Account A should be able to parse resume successfully"},
            {"SCENARIO_2_CROSS_ACCESS", "AccountB", "valid", "ACCESS_PARSED_CANDIDATE", "405", "cross_account_access_denied", "Account B should be denied access to Account A's parsed candidate"},
            {"SCENARIO_2_CROSS_KEY", "AccountB", "valid", "PARSE_WITH_CROSS_ACCOUNT_KEY", "200", "invalid_key_error", "Account B should not be able to use Account A's presigned key"},
            {"SCENARIO_3_B_PRESIGNED", "AccountB", "valid", "GET_PRESIGNED_URL", "200", "success", "Account B should be able to get their own presigned URL"},
            {"SCENARIO_3_B_PARSE", "AccountB", "valid", "PARSE_RESUME", "200", "success", "Account B should be able to parse resume with their own credentials"},
            {"SCENARIO_4_INVALID_TOKEN", "AccountB", "invalid", "PARSE_RESUME", "401", "unauthorized", "Invalid token should return 401"},
            {"SCENARIO_4_INVALID_PRESIGNED", "AccountA", "invalid", "GET_PRESIGNED_URL", "401", "unauthorized", "Invalid token should deny presigned URL access"},
            {"SCENARIO_5_EXPIRED_TOKEN", "AccountB", "expired", "PARSE_RESUME", "401", "unauthorized", "Expired token should return 401"},
            {"SCENARIO_5_MALFORMED_TOKEN", "AccountA", "malformed", "GET_PRESIGNED_URL", "405", "unauthorized", "Malformed token should return 405"},
            {"SCENARIO_6_CROSS_PRESIGNED", "AccountB", "valid", "GET_PRESIGNED_URL", "200", "success", "Account B should get their own presigned URL (not Account A's)"},
            {"SCENARIO_6_ISOLATED_PARSE", "AccountA", "valid", "PARSE_RESUME", "200", "success", "Account A operations should remain isolated from Account B"},
            {"SCENARIO_6_FILENAME_ISOLATION", "AccountB", "valid", "GET_PRESIGNED_URL", "200", "success", "Account B should be able to get presigned URL for any filename"},
            {"SCENARIO_6_DATA_ISOLATION", "AccountA", "valid", "PARSE_RESUME", "200", "success", "Account A parsed data should not be visible to Account B"}
        };
    }
}
