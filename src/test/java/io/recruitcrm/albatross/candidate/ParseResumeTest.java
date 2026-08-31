package io.recruitcrm.albatross.candidate;

import com.qa.api.util.S3Uploader;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;
import org.hamcrest.Matchers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|aiTestParser")
public class ParseResumeTest extends TestBase {

    String albatrossAuthToken;
    String resumeFilePath = System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/ArjunReddyResume.pdf";
    File resumeFile = new File(resumeFilePath);
    String resumeFileName = resumeFile.getName();

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @DataProvider
    public Object[][] parseResumeData() throws IOException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("fileName", resumeFileName);
        queryParams.put("requestType", "put");

        Response presignedResponse = RestClient.doGet("JSON", albatrossURL, "get-presigned-url",
                albatrossAuthToken, queryParams, null, true);

        Assert.assertEquals(presignedResponse.getStatusCode(), 200, "Expected status code 200 for presigned URL fetch");
        JsonPath presignedJsonPath = presignedResponse.jsonPath();
        String encryptedKey = presignedJsonPath.get("data.key");
        String preSignedUrl = presignedJsonPath.getString("data.preSignedUrl");
        Assert.assertNotNull(preSignedUrl, "data.preSignedUrl required to upload resume before parse-resume");
        S3Uploader.uploadFileToS3(preSignedUrl, resumeFile.getAbsolutePath(), "application/pdf");

        JSONObject filesInfo = new JSONObject();
        filesInfo.put("key", encryptedKey);
        filesInfo.put("name", resumeFileName);
        filesInfo.put("type", "application/pdf");
        filesInfo.put("size", resumeFile.length());
        filesInfo.put("index", 0);

        JSONObject resumeParserData = new JSONObject();
        resumeParserData.put("resumesParsed", 0);
        resumeParserData.put("resumesFailed", 0);
        resumeParserData.put("resumesTotal", 1);
        resumeParserData.put("filesInfo", filesInfo);

        return new Object[][]{{resumeParserData, 0}};
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "parseResumeData", groups = "nightly-build")
    public void verifyParseResumeEndpoint(JSONObject resumeParserData, int actionId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("actionsteps", "1");

        JSONObject parseResumeRequest = new JSONObject();
        parseResumeRequest.put("resumeParserData", resumeParserData);
        parseResumeRequest.put("actionid", actionId);

        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                albatrossAuthToken, queryParams, null, true, parseResumeRequest);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK status when parsing valid resume");
        response.then().body("message_type", Matchers.equalTo("is-success"));
        response.then().body("silent_progress", Matchers.equalTo(true));
        response.then().body("message", Matchers.containsString("parsed successfully"));
        
        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.get("data.candidate"), "Candidate data should not be null after successful resume parse");
        
        // Verify sovren_document_id is generated (confirms file was processed by parser)
        Object sovrenDocIdObj = jsonPath.get("data.candidate.sovren_document_id");
        Assert.assertNotNull(sovrenDocIdObj, "Sovren document ID should be generated");
        String sovrenDocId = sovrenDocIdObj.toString();
        Assert.assertFalse(sovrenDocId.isEmpty(), "Sovren document ID should not be empty");
        Assert.assertTrue(sovrenDocId.matches("\\d+_\\d+"), 
                "Sovren document ID should follow pattern: number_number (e.g., '6_3925'), got: '" + sovrenDocId + "'");
    }

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void verifyParseResumeWithInvalidToken() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("actionsteps", "1");

        JSONObject filesInfo = new JSONObject();
        filesInfo.put("key", "invalidKey");
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

        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                "InvalidToken", queryParams, null, true, parseResumeRequest);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 Unauthorized for invalid token");
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.get("error"), "Unauthorized", "Expected error message to be 'Unauthorized'");
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void verifyParseResumeWithGetMethod() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("actionsteps", "1");

        Response response = RestClient.doGet("JSON", albatrossURL, "candidates/parse-resume",
                albatrossAuthToken, queryParams, null, true);

        Assert.assertEquals(response.getStatusCode(), 405, "Expected 405 Method Not Allowed for GET request to POST endpoint");
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void verifyParseResumeWithMissingActionSteps() {
        Map<String, String> queryParams = new HashMap<>();

        JSONObject filesInfo = new JSONObject();
        filesInfo.put("key", 1234);
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

        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                albatrossAuthToken, queryParams, null, true, parseResumeRequest);

        Assert.assertEquals(response.getStatusCode(), 422, "Expected 422 Unprocessable Entity due to missing 'actionsteps' query param");
        Assert.assertEquals(response.jsonPath().get("message_type"), "is-danger", "Expected message_type to be 'is-danger' for invalid request");
    }

    @Test
    public void verifyParseResumeWithEmptyBody() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("actionsteps", "1");

        JSONObject emptyRequest = new JSONObject();

        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                albatrossAuthToken, queryParams, null, true, emptyRequest);

        Assert.assertTrue(response.getStatusCode() >= 400,
                "Expected error status code for empty body, got: " + response.getStatusCode());
    }

    @Test
    public void verifyParseResumeWithMissingFilesInfo() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("actionsteps", "1");

        JSONObject resumeParserData = new JSONObject();
        resumeParserData.put("resumesParsed", 0);
        resumeParserData.put("resumesFailed", 0);
        resumeParserData.put("resumesTotal", 1);

        JSONObject parseResumeRequest = new JSONObject();
        parseResumeRequest.put("resumeParserData", resumeParserData);
        parseResumeRequest.put("actionid", 0);

        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                albatrossAuthToken, queryParams, null, true, parseResumeRequest);

        Assert.assertTrue(response.getStatusCode() >= 400,
                "Expected error status code when filesInfo is missing, got: " + response.getStatusCode());
    }

    @Test
    public void verifyParseResumeWithInvalidKeyFormat() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("actionsteps", "1");

        JSONObject filesInfo = new JSONObject();
        filesInfo.put("key", "!@#$%^&*()_INVALID");
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

        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                albatrossAuthToken, queryParams, null, true, parseResumeRequest);

        Assert.assertTrue(response.getStatusCode() >= 400,
                "Expected error status code for invalid key format, got: " + response.getStatusCode());
    }

    @Test(dataProvider = "parseResumeData")
    public void verifyParseResumeResponseStructure(JSONObject resumeParserData, int actionId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("actionsteps", "1");

        JSONObject parseResumeRequest = new JSONObject();
        parseResumeRequest.put("resumeParserData", resumeParserData);
        parseResumeRequest.put("actionid", actionId);

        Response response = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                albatrossAuthToken, queryParams, null, true, parseResumeRequest);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK status for response structure validation");

        JsonPath jsonPath = response.jsonPath();

        // Verify top-level response fields
        Assert.assertNotNull(jsonPath.get("message"), "Response should have message field");
        Assert.assertNotNull(jsonPath.get("message_type"), "Response should have message_type field");
        Assert.assertNotNull(jsonPath.get("silent_progress"), "Response should have silent_progress field");
        Assert.assertNotNull(jsonPath.get("data"), "Response should have data field");

        // Verify candidate object structure
        Map<String, Object> candidate = jsonPath.getMap("data.candidate");
        Assert.assertNotNull(candidate, "Candidate object should exist in data");

        // Verify key candidate fields exist
        Assert.assertTrue(candidate.containsKey("sovren_document_id"), "Should contain sovren_document_id");
        Assert.assertTrue(candidate.containsKey("firstname"), "Should contain firstname");
        Assert.assertTrue(candidate.containsKey("lastname"), "Should contain lastname");
        Assert.assertTrue(candidate.containsKey("emailid"), "Should contain emailid");
        Assert.assertTrue(candidate.containsKey("contactnumber"), "Should contain contactnumber");
        Assert.assertTrue(candidate.containsKey("city"), "Should contain city");
        Assert.assertTrue(candidate.containsKey("country"), "Should contain country");
        Assert.assertTrue(candidate.containsKey("skill"), "Should contain skill");
        Assert.assertTrue(candidate.containsKey("position"), "Should contain position");
        Assert.assertTrue(candidate.containsKey("lastorganisation"), "Should contain lastorganisation");
    }
}
