package io.recruitcrm.albatross.callLogs;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.AddCallLog;
import io.rcrm.api.pojo.albatross.CallLog;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AddCallLogTest extends TestBase{
    commanFunction function = new commanFunction();
    String entitySlug,callTo= null;
    int callLogId;

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void addCallLogPOST_200() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        entitySlug = jsonCandidate.get("slug");
        callTo = jsonCandidate.get("first_name")+" "+jsonCandidate.get("last_name");

        CallLog callLog = new CallLog();
        callLog.setCalltype("Outgoing call");
        callLog.setContactnumber("1234567890");
        callLog.setCallfrom("9090909090");
        callLog.setCallto(callTo);
        callLog.setCallnotes("This is a test call");
        callLog.setSubject("Test Call");
        callLog.setAccountid(ThreadManager.getAccount().getAccountId());
        callLog.setStartedon(System.currentTimeMillis());
        callLog.setRelatedcandidate(entitySlug);
        callLog.setPin(0);
        callLog.setCustomcalltypeid(0);
        callLog.setType("3");
        callLog.setDuration("300");

        Map<String, List<Object>> associationsMap = new HashMap<>();

        // Initializing the map with empty lists
        associationsMap.put("2", new ArrayList<>());
        associationsMap.put("3", new ArrayList<>());
        associationsMap.put("4", new ArrayList<>());
        associationsMap.put("5", new ArrayList<>());
        associationsMap.put("11", new ArrayList<>());

        AddCallLog addCallLog = new AddCallLog();
        addCallLog.setCallLog(callLog);
        addCallLog.setAssociatedData(associationsMap);

        Response response = RestClient.doPost("JSON", albatrossURL, "call-logs", ThreadManager.getOwnerAlbatrossToken(), null, true, addCallLog);

        response.then().statusCode(200);

        JsonPath jp = response.jsonPath();
        callLogId = jp.get("data.callLog.id");

        response.then().body("status", Matchers.containsString("success"));
        response.then().body("data.callLog.id", Matchers.notNullValue());
        assertThat(jp.getInt("data.callLog.duration"), Matchers.is(300));
    }

    @Owner("Harika")
    @Test(dataProvider = "callDurationFieldTestData", groups = "nightly-build")
    public void callDurationFieldValidationPOST_422(String duration,String message) {
        CallLog callLog = new CallLog();
        callLog.setCalltype("Outgoing call");
        callLog.setContactnumber("1234567890");
        callLog.setCallfrom("9090909090");
        callLog.setCallto(callTo);
        callLog.setCallnotes("This is a test call");
        callLog.setSubject("Test Call");
        callLog.setAccountid(ThreadManager.getAccount().getAccountId());
        callLog.setStartedon(System.currentTimeMillis());
        callLog.setRelatedcandidate(entitySlug);
        callLog.setPin(0);
        callLog.setCustomcalltypeid(0);
        callLog.setType("3");
        callLog.setDuration(duration);

        Map<String, List<Object>> associationsMap = new HashMap<>();

        // Initializing the map with empty lists
        associationsMap.put("2", new ArrayList<>());
        associationsMap.put("3", new ArrayList<>());
        associationsMap.put("4", new ArrayList<>());
        associationsMap.put("5", new ArrayList<>());
        associationsMap.put("11", new ArrayList<>());

        AddCallLog addCallLog = new AddCallLog();
        addCallLog.setCallLog(callLog);
        addCallLog.setAssociatedData(associationsMap);

        Response response = RestClient.doPost("JSON", albatrossURL, "call-logs", ThreadManager.getOwnerAlbatrossToken(), null, true, addCallLog);

        response.then().statusCode(422);


        response.then().body("message_type", Matchers.equalTo("is-danger"));
        response.then().body("message", Matchers.containsString(message));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void addCallLogInvalidAuthPOST_401() {
        CallLog callLog = new CallLog();
        callLog.setCalltype("Outgoing call");
        callLog.setContactnumber("1234567890");
        callLog.setCallfrom("9090909090");
        callLog.setCallto(callTo);
        callLog.setCallnotes("This is a test call");
        callLog.setSubject("Test Call");
        callLog.setAccountid(ThreadManager.getAccount().getAccountId());
        callLog.setStartedon(System.currentTimeMillis());
        callLog.setRelatedcandidate(entitySlug);
        callLog.setPin(0);
        callLog.setCustomcalltypeid(0);
        callLog.setType("3");
        callLog.setDuration("300");

        Map<String, List<Object>> associationsMap = new HashMap<>();

        // Initializing the map with empty lists
        associationsMap.put("2", new ArrayList<>());
        associationsMap.put("3", new ArrayList<>());
        associationsMap.put("4", new ArrayList<>());
        associationsMap.put("5", new ArrayList<>());
        associationsMap.put("11", new ArrayList<>());

        AddCallLog addCallLog = new AddCallLog();
        addCallLog.setCallLog(callLog);
        addCallLog.setAssociatedData(associationsMap);

        Response response = RestClient.doPost("JSON", albatrossURL, "call-logs", ThreadManager.getOwnerAlbatrossToken()+"x001", null, true, addCallLog);

        response.then().statusCode(401);
    }

    @DataProvider
    public Object[][] callDurationFieldTestData() {
        Object data[][] = { { "86400" ,"The call log.duration must not be greater than 86399." } ,{ "-1" ,"The call log.duration must be at least 0" },{ "abcd","The call log.duration must be an integer." }};
        return data;
    }

}
