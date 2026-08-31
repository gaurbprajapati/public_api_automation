package io.recruitcrm.albatross.callLogs;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.DeleteCallRecording;
import io.rcrm.api.pojo.reaper.Account;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class DeleteCallRecordingTest extends TestBase {
    commanFunction function = new commanFunction();
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    private String tokenA;
    private String publicAPIKeyA;
    private int callLogId;
    private String recordingData;
    private String tokenB;
    private String publicAPIKeyB;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        tokenA =  getTokenForAccount("AccountA", "valid");
        publicAPIKeyA = getAccountApiKey("AccountA");

        tokenB =  getTokenForAccount("AccountB", "valid");
        publicAPIKeyB = getAccountApiKey("AccountB");

    }
    
    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void deleteCallRecording_Success() {

        getCallLogData(publicAPIKeyA, tokenA);

        DeleteCallRecording deleteCallRecording = new DeleteCallRecording();
        DeleteCallRecording.CallLog callLog = new DeleteCallRecording.CallLog();
        callLog.setId(callLogId);
        callLog.setRecording(recordingData);
        deleteCallRecording.setCallLog(callLog);

        String basePath = "call-recording/delete";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath,
                tokenA, null, true, deleteCallRecording);

        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Delete Call Recording Successful "));
    }
    
    @Owner("Harika")
    @Test(dataProvider = "getInValidTestData", groups = "nightly-build")
    public void deleteCallRecording_InvalidData(int callLogId, String recordingData) {

        getCallLogData(publicAPIKeyA, tokenA);

        DeleteCallRecording deleteCallRecording = new DeleteCallRecording();
        DeleteCallRecording.CallLog callLog = new DeleteCallRecording.CallLog();
        callLog.setId(callLogId);
        callLog.setRecording(recordingData);
        deleteCallRecording.setCallLog(callLog);

        String basePath = "call-recording/delete";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, 
            tokenA, null, true, deleteCallRecording);

        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("callLog Not found"));
    }
    
    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void deleteCallRecording_UnauthorizedToken() {

        DeleteCallRecording deleteCallRecording = new DeleteCallRecording();
        DeleteCallRecording.CallLog callLog = new DeleteCallRecording.CallLog();
        callLog.setId(callLogId);
        callLog.setRecording(recordingData);
        deleteCallRecording.setCallLog(callLog);

        String basePath = "call-recording/delete";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, 
            tokenA + "invalid", null, true, deleteCallRecording);

        response.then().statusCode(401);
        response.then().body("error", Matchers.containsString("Unauthorized"));

    }
    
    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void deleteCallRecording_EmptyRequestBody() {
        String basePath = "call-recording/delete";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, 
            tokenA, null, true, null);

        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("callLog Not found"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void testValidCrossAccountDeleteCallRecording() {

        getCallLogData(publicAPIKeyB, tokenB);

        DeleteCallRecording deleteCallRecording = new DeleteCallRecording();
        DeleteCallRecording.CallLog callLog = new DeleteCallRecording.CallLog();
        callLog.setId(callLogId);
        callLog.setRecording(recordingData);
        deleteCallRecording.setCallLog(callLog);

        String basePath = "call-recording/delete";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath,
                tokenA , null, true, deleteCallRecording);

        response.then().statusCode(200);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("callLog Not found"));
    }

    public void getCallLogData(String publicAuth, String privateAuth){
        JsonPath callLog = function.createNewCallLog(baseURL, publicAuth, "candidate").jsonPath();
        callLogId = callLog.get("id");

        function.uploadCallLogRecording(baseURL, publicAuth, 0, callLogId);

        JsonPath callLogRecording;
        int retries = 4; // Set your retry limit
        int waitTimeMs = 20000; // Wait time between retries in milliseconds

        for (int i = 0; i < retries; i++) {
            callLogRecording = allCrudFunctions.getCallLogs(albatrossURL, privateAuth).jsonPath();
            recordingData = callLogRecording.get("data.records[0].recording");

            if (recordingData != null) {
                break; // Exit the loop if we got a non-null recording
            }

            try {
                Thread.sleep(waitTimeMs); // Wait before retrying
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                throw new RuntimeException("Thread was interrupted while waiting to retry", e);
            }
        }

        if (recordingData == null) {
            throw new RuntimeException("Failed to retrieve non-null recording after " + retries + " retries.");
        }
    }


    @DataProvider(parallel = true)
    public Object[][] getInValidTestData() {
        getCallLogData(publicAPIKeyA, tokenA);
        return new Object[][]{ {1234599, recordingData} ,{0, recordingData} };
    }

} 