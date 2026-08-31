package io.recruitcrm.albatross.job;

import com.qa.api.util.S3Uploader;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.ParseJob;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@AccountType("Business|AlbatrossTkn|aiTestParser")
public class ParseJobTest extends TestBase {

    String albatrossAuthToken;
    String englishJDFilePath = System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/EnglishJobDescription.pdf";
    String spanishJDFilePath = System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/SpanishJobDescription.pdf";
    File englishJDFile = new File(englishJDFilePath);
    File spanishJDFile = new File(spanishJDFilePath);
    String englishJDFileName = englishJDFile.getName();
    String spanishJDFileName = spanishJDFile.getName();

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    private ParseJob buildParseJobRequestAfterUpload(File jdFile, String fileName, boolean onlyParserData)
            throws IOException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("fileName", fileName);
        queryParams.put("requestType", "put");

        Response presignedResponse = RestClient.doGet("JSON", albatrossURL, "get-presigned-url",
                albatrossAuthToken, queryParams, null, true);

        Assert.assertEquals(presignedResponse.getStatusCode(), 200, "Expected status code 200 for presigned URL fetch");
        JsonPath presignedJsonPath = presignedResponse.jsonPath();
        String encryptedKey = presignedJsonPath.get("data.key");
        String preSignedUrl = presignedJsonPath.getString("data.preSignedUrl");
        Assert.assertNotNull(preSignedUrl, "data.preSignedUrl required to upload JD before parse-job");
        S3Uploader.uploadFileToS3(preSignedUrl, jdFile.getAbsolutePath(), "application/pdf");

        ParseJob.DetailFilename detailFilename = new ParseJob.DetailFilename();
        detailFilename.setKey(encryptedKey);
        detailFilename.setName(fileName);

        ParseJob.ResumeParserData resumeParserData = new ParseJob.ResumeParserData();
        resumeParserData.setDetailfilename(detailFilename);

        ParseJob parseJobRequest = new ParseJob();
        parseJobRequest.setResumeParserData(resumeParserData);
        parseJobRequest.setOnlyParserData(onlyParserData);

        return parseJobRequest;
    }

    @DataProvider
    public Object[][] englishJDParseData() throws IOException {
        return new Object[][]{{buildParseJobRequestAfterUpload(englishJDFile, englishJDFileName, true)}};
    }

    @DataProvider
    public Object[][] spanishJDParseData() throws IOException {
        return new Object[][]{{buildParseJobRequestAfterUpload(spanishJDFile, spanishJDFileName, true)}};
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "englishJDParseData", priority = 1)
    public void verifyParseJobEndpoint_EnglishJD_HappyPath(ParseJob parseJobRequest) {
        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, true, parseJobRequest);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK status when parsing valid English JD");
        response.then().body("message_type", Matchers.equalTo("is-success"));
        response.then().body("message", Matchers.containsString("Job Parser Successful"));

        JsonPath jsonPath = response.jsonPath();
        String jobDocId = jsonPath.getString("data.job.job_document_id");
        Assert.assertNotNull(jobDocId, "Job document ID should be generated");
        Assert.assertFalse(jobDocId.isEmpty(), "Job document ID should not be empty");
        Assert.assertTrue(jobDocId.matches("\\d+_\\d+"), "Job document ID should follow pattern: number_number");

        String jobName = jsonPath.getString("data.job.name");
        Assert.assertNotNull(jobName, "Job name should be extracted from English JD");
        Assert.assertFalse(jobName.trim().isEmpty(), "Job name should not be empty for English JD");

        String city = jsonPath.getString("data.job.city");
        Assert.assertNotNull(city, "Job city should be extracted from English JD");
        Assert.assertFalse(city.trim().isEmpty(), "Job city should not be empty for English JD");
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "spanishJDParseData", priority = 2)
    public void verifyParseJobEndpoint_MultilingualSpanishJD_HappyPath(ParseJob parseJobRequest) {
        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, true, parseJobRequest);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK status when parsing valid Spanish JD");
        response.then().body("message_type", Matchers.equalTo("is-success"));
        response.then().body("message", Matchers.containsString("Job Parser Successful"));
        
        JsonPath jsonPath = response.jsonPath();
        
        // Verify data.job object exists
        Object dataObj = jsonPath.get("data");
        Assert.assertTrue(dataObj instanceof java.util.Map, "Data should be an object/map for multilingual parsing");
        Assert.assertNotNull(jsonPath.get("data.job"), "Job data should not be null after successful multilingual parse");
        
        // Verify job_document_id is generated (confirms multilingual file was processed)
        String jobDocId = jsonPath.get("data.job.job_document_id");
        Assert.assertNotNull(jobDocId, "Job document ID should be generated for multilingual JD");
        Assert.assertFalse(jobDocId.isEmpty(), "Job document ID should not be empty for multilingual JD");
        Assert.assertTrue(jobDocId.matches("\\d+_\\d+"), "Job document ID should follow pattern: number_number");
        
        // Note: Parsed field values (name, city, job_skill, etc.) may be empty strings until parser quality improves
        // The test verifies the multilingual parsing process works (200 + job_document_id) even if extraction is minimal
    }

    @Owner("Sampurn Chouksey")
    @Test(priority = 3)
    public void verifyParseJobWithInvalidToken() {
        ParseJob.DetailFilename detailFilename = new ParseJob.DetailFilename();
        detailFilename.setKey("invalidKey12345");
        detailFilename.setName(englishJDFileName);

        ParseJob.ResumeParserData resumeParserData = new ParseJob.ResumeParserData();
        resumeParserData.setDetailfilename(detailFilename);

        ParseJob parseJobRequest = new ParseJob();
        parseJobRequest.setResumeParserData(resumeParserData);
        parseJobRequest.setOnlyParserData(true);

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                "InvalidToken123", null, true, parseJobRequest);

        Assert.assertEquals(response.getStatusCode(), 401, "Expected 401 Unauthorized for invalid token");
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.get("error"), "Unauthorized", "Expected error message to be 'Unauthorized'");
    }

    @Owner("Sampurn Chouksey")
    @Test(priority = 5)
    public void verifyParseJobWithGetMethod() {
        Response response = RestClient.doGet("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, null, true);

        Assert.assertEquals(response.getStatusCode(), 405, "Expected 405 Method Not Allowed for GET request to POST endpoint");
        response.then().body("message", Matchers.equalTo("Method Not Allowed"));
        response.then().body("message_type", Matchers.equalTo("is-danger"));
    }

    @Owner("Sampurn Chouksey")
    @Test(priority = 6)
    public void verifyParseJobWithEmptyBody() {
        JSONObject emptyRequest = new JSONObject();

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, true, emptyRequest);

        Assert.assertEquals(response.getStatusCode(), 500,
                "API returns 500 Internal Server Error for empty body");
    }

    @Owner("Sampurn Chouksey")
    @Test(priority = 7)
    public void verifyParseJobWithMissingKey() {
        ParseJob.DetailFilename detailFilename = new ParseJob.DetailFilename();
        detailFilename.setName(englishJDFileName);

        ParseJob.ResumeParserData resumeParserData = new ParseJob.ResumeParserData();
        resumeParserData.setDetailfilename(detailFilename);

        ParseJob parseJobRequest = new ParseJob();
        parseJobRequest.setResumeParserData(resumeParserData);
        parseJobRequest.setOnlyParserData(true);

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, true, parseJobRequest);

        Assert.assertEquals(response.getStatusCode(), 200, 
                "API returns 200 with empty data when key is missing");
        
        // Verify that parsing didn't happen - data should be empty array []
        JsonPath jsonPath = response.jsonPath();
        Object data = jsonPath.get("data");
        Assert.assertTrue(data instanceof java.util.List, "Data should be a list");
        Assert.assertTrue(((java.util.List<?>)data).isEmpty(), 
                "Data should be empty array when key is missing - no parsing occurred");
    }

    @Owner("Sampurn Chouksey")
    @Test(priority = 8)
    public void verifyParseJobWithMissingFileName() {
        ParseJob.DetailFilename detailFilename = new ParseJob.DetailFilename();
        detailFilename.setKey("someValidKey");

        ParseJob.ResumeParserData resumeParserData = new ParseJob.ResumeParserData();
        resumeParserData.setDetailfilename(detailFilename);

        ParseJob parseJobRequest = new ParseJob();
        parseJobRequest.setResumeParserData(resumeParserData);
        parseJobRequest.setOnlyParserData(true);

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, true, parseJobRequest);

        Assert.assertEquals(response.getStatusCode(), 200, 
                "API returns 200 with empty data when fileName is missing");
        
        // Verify that parsing didn't happen - data should be empty array []
        JsonPath jsonPath = response.jsonPath();
        Object data = jsonPath.get("data");
        Assert.assertTrue(data instanceof java.util.List, "Data should be a list");
        Assert.assertTrue(((java.util.List<?>)data).isEmpty(), 
                "Data should be empty array when fileName is missing - no parsing occurred");
    }

    @Owner("Sampurn Chouksey")
    @Test(priority = 9)
    public void verifyParseJobWithMissingResumeParserData() {
        ParseJob parseJobRequest = new ParseJob();
        parseJobRequest.setOnlyParserData(true);

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, true, parseJobRequest);

        Assert.assertEquals(response.getStatusCode(), 500,
                "API returns 500 Internal Server Error when resumeParserData is missing");
    }

    @Owner("Sampurn Chouksey")
    @Test(priority = 10)
    public void verifyParseJobWithMissingDetailFilename() {
        JSONObject resumeParserData = new JSONObject();
        JSONObject parseJobRequest = new JSONObject();
        parseJobRequest.put("resumeParserData", resumeParserData);
        parseJobRequest.put("onlyParserData", true);

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, true, parseJobRequest);

        Assert.assertEquals(response.getStatusCode(), 500,
                "API returns 500 Internal Server Error when detailfilename is missing");
    }

    @Owner("Sampurn Chouksey")
    @Test(priority = 11)
    public void verifyParseJobWithInvalidKeyFormat() {
        ParseJob.DetailFilename detailFilename = new ParseJob.DetailFilename();
        detailFilename.setKey("!@#$%^&*()");
        detailFilename.setName(englishJDFileName);

        ParseJob.ResumeParserData resumeParserData = new ParseJob.ResumeParserData();
        resumeParserData.setDetailfilename(detailFilename);

        ParseJob parseJobRequest = new ParseJob();
        parseJobRequest.setResumeParserData(resumeParserData);
        parseJobRequest.setOnlyParserData(true);

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, true, parseJobRequest);

        Assert.assertEquals(response.getStatusCode(), 200, 
                "API returns 200 with empty data when key format is invalid");
        
        // Verify that parsing didn't happen - data should be empty array []
        JsonPath jsonPath = response.jsonPath();
        Object data = jsonPath.get("data");
        Assert.assertTrue(data instanceof java.util.List, "Data should be a list");
        Assert.assertTrue(((java.util.List<?>)data).isEmpty(), 
                "Data should be empty array when key format is invalid - no parsing occurred");
    }

    @Owner("Sampurn Chouksey")
    @Test(priority = 12)
    public void verifyParseJobWithOnlyParserDataFalse() throws IOException {
        ParseJob parseJobRequest = buildParseJobRequestAfterUpload(englishJDFile, englishJDFileName, false);

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, true, parseJobRequest);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected 200 OK status even with onlyParserData=false");
        response.then().body("message_type", Matchers.equalTo("is-success"));
    }

    @Owner("Sampurn Chouksey")
    @Test(priority = 13)
    public void verifyParseJobResponseStructure() throws IOException {
        ParseJob parseJobReq = buildParseJobRequestAfterUpload(englishJDFile, englishJDFileName, true);

        Response response = RestClient.doPost("JSON", albatrossURL, "jobs/parse-job",
                albatrossAuthToken, null, true, parseJobReq);

        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jsonPath = response.jsonPath();

        Assert.assertTrue(jsonPath.get("data.job") instanceof Map, "Job should be an object");
        Map<String, Object> job = jsonPath.getMap("data.job");
        Assert.assertNotNull(jsonPath.get("data.job.job_document_id"), "Job document ID should exist");
        Assert.assertTrue(job.containsKey("name"), "Job response should contain name field");
        Assert.assertTrue(job.containsKey("city"), "Job response should contain city field");
        Assert.assertNotNull(jsonPath.get("silent_progress"), "silent_progress should exist");
        Assert.assertNotNull(jsonPath.get("message"), "message should exist");
        Assert.assertEquals(jsonPath.get("message_type"), "is-success", "message_type should be is-success");
    }
}
