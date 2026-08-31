package io.rcrm.api.calllogs;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCallLog;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business")
public class UploadCallRecordingTest extends TestBase {

    commanFunction function = new commanFunction();
    String accountAPIKey;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        accountAPIKey = ThreadManager.getAccountApiKey();
        RestAssured.baseURI = baseURL;
    }

    @Owner("Harika")
    @Test(dataProvider = "getGenerateTranscriptValue", groups = "nightly-build")
    public void uploadCallLogRecording(int generateTranscript) {
        JsonPath callLog = function.createNewCallLog(baseURL, accountAPIKey, "candidate").jsonPath();
        int callLogId = callLog.get("id");

        // Get user ID for updated_by parameter
        Response usersResponse = function.getUsers(baseURL, accountAPIKey);
        usersResponse.then().statusCode(200);
        int updatedByUserId = usersResponse.jsonPath().get("[1].id");

        File wavFile = new File(System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/sampleWav.wav");

        if (!wavFile.exists()) {
            throw new RuntimeException("Test file not found: " + wavFile.getAbsolutePath());
        }

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + accountAPIKey)
                .multiPart("call_recording", wavFile)
                .multiPart("call_log_id", callLogId)
                .multiPart("generate_transcript", generateTranscript)
                .multiPart("updated_by", updatedByUserId)
                .post("call-logs/upload-call-recording");

        response.then().statusCode(200);
        response.then().body("message",
                Matchers.containsString("The recording upload is in progress. You can check the status by using the following endpoint:"));
    }

    @Owner("Harika")
    @Test(dataProvider = "getTestDataToUploadRecording", groups = "nightly-build")
    public void uploadCallLogRecording_422(String callRecording, String callLogId, String generateTranscript) {

        Response response = RestAssured.given()
                .header("Authorization", "Bearer "+ accountAPIKey)
                .multiPart("call_recording", callRecording)
                .multiPart("call_log_id", callLogId)
                .multiPart("generate_transcript",generateTranscript)
                .post("call-logs/upload-call-recording");

        response.then().statusCode(422);

        if(callRecording.equals("file")) {
            response.then().body("call_recording[0]", Matchers.containsString("The call recording must be a file."));
            response.then().body("generate_transcript[0]", Matchers.containsString("The selected generate transcript is invalid."));
            response.then().body("call_log_id[0]", Matchers.containsString("The call log id must be an integer."));
        } else {
            response.then().body("call_recording[0]", Matchers.containsString("The call recording field is required."));
            response.then().body("generate_transcript[0]", Matchers.containsString("The generate transcript field is required."));
            response.then().body("call_log_id[0]", Matchers.containsString("The call log id field is required."));
        }
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void uploadCallLogRecording_401() {

        Response response = RestAssured.given()
                .header("Authorization", "Bearer "+ accountAPIKey+"123")
                .multiPart("call_recording", "")
                .multiPart("call_log_id", "")
                .multiPart("generate_transcript","")
                .post("call-logs/upload-call-recording");

        response.then().statusCode(401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void fetchCallRecordingUploadStatus() {
        JsonPath callLog = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        int callLogId = callLog.get("id");

        // Get user ID for updated_by parameter
        Response usersResponse = function.getUsers(baseURL, accountAPIKey);
        usersResponse.then().statusCode(200);
        int updatedByUserId = usersResponse.jsonPath().get("[1].id");

        function.uploadCallLogRecording(updatedByUserId, callLogId, baseURL, ThreadManager.getAccountApiKey());
        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("call_Log_id", String.valueOf(callLogId));

        Response response = RestClient.doGet("JSON", baseURL, "call-logs/get-recording-status/{call_Log_id}", ThreadManager.getAccountApiKey(), null, pathParameters, true);

        response.then().statusCode(200);
        response.then().body("message", Matchers.containsString("In Progress"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void fetchStatusForCallRecordingNotUploaded() {
        JsonPath callLog = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        int callLogId = callLog.get("id");

        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("call_Log_id", String.valueOf(callLogId));

        Response response = RestClient.doGet("JSON", baseURL, "call-logs/get-recording-status/{call_Log_id}", ThreadManager.getAccountApiKey(), null, pathParameters, true);

        response.then().statusCode(200);
        response.then().body("message", Matchers.containsString("Recording Status not available for this Call Log"));
    }

    @Owner("Harika")
    @Test(dataProvider = "getInvalidCallLogId", groups = "nightly-build")
    public void fetchStatusForCallRecording_404(String callLogId) {
        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("call_Log_id", callLogId);

        Response response = RestClient.doGet("JSON", baseURL, "call-logs/get-recording-status/{call_Log_id}", accountAPIKey, null, pathParameters, true);

        response.then().statusCode(404);
        response.then().body("errorMessage", Matchers.containsString("Call Log doesn't exist"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void fetchStatusForCallRecording_401() {
        Map<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("call_Log_id", "123");

        Response response = RestClient.doGet("JSON", baseURL, "call-logs/get-recording-status/{call_Log_id}", accountAPIKey+"123", null, pathParameters, true);

        response.then().statusCode(401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }


    @DataProvider(parallel = true)
    public Object[][] getGenerateTranscriptValue() {
        Object data[][] = { { 0 }, { 1 } };
        return data;
    }

    @DataProvider(parallel = true)
    public Object[][] getInvalidCallLogId() {
        Object data[][] = { { "123" } , { " " } };
        return data;
    }

    @DataProvider(parallel = true)
    public Object[][] getTestDataToUploadRecording() {
        Object data[][] = { {" "," "," "},{ "file","abc","2" } };
        return data;
    }
}
