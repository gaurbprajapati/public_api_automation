package io.recruitcrm.albatross.callLogs;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
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
public class AllEndpointsOfCallLogTest extends TestBase {
    commanFunction function = new commanFunction();
    String entitySlug,callTo= null;
    int callLogId;

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void addCallLog_POST() {
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
    @Test(dependsOnMethods = {"addCallLog_POST"}, groups = "nightly-build")
    public void editCallLog_POST() {
        HashMap<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("id", String.valueOf(callLogId));

        String basePath = "call-logs/{id}";

        CallLog callLog = new CallLog();
        callLog.setCalltype("Outgoing call");
        callLog.setContactnumber("1234567890");
        callLog.setCallfrom("9090909090");
        callLog.setCallto(callTo);
        callLog.setCallnotes("This is a test call update");
        callLog.setSubject("Test Call");
        callLog.setAccountid(ThreadManager.getAccount().getAccountId());
        callLog.setStartedon(System.currentTimeMillis());
        callLog.setRelatedcandidate(entitySlug);
        callLog.setPin(0);
        callLog.setCustomcalltypeid(0);
        callLog.setType("3");
        callLog.setDuration("600");

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

        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,pathParameters, true, addCallLog);

        response.then().statusCode(200);

        JsonPath jp = response.jsonPath();

        response.then().body("status", Matchers.containsString("success"));
        response.then().body("data.callLog.id", Matchers.notNullValue());
        response.then().body("message", Matchers.containsString("Update Call log Successful "));
        assertThat(jp.getInt("data.callLog.duration"), Matchers.is(600));
    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"addCallLog_POST"}, groups = "nightly-build")
    public void getCallLogActivityData_GET() {

        String basePath = "expand-activity/get-activity-data";

        GetActivityData getActivityData = new GetActivityData();
        getActivityData.setType("0");
        getActivityData.setPage("detailspage");
        getActivityData.setOffset(0);
        getActivityData.setPagesize(15);
        getActivityData.setRelatedToSlug(entitySlug);
        getActivityData.setRelatedtotypeid(5);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, getActivityData);

        response.then().statusCode(200);

        JsonPath jp = response.jsonPath();

        response.then().body("status", Matchers.containsString("success"));
        assertThat(jp.getInt("data.events.notes[0].duration"), Matchers.is(600));
    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"addCallLog_POST"}, groups = "nightly-build")
    public void getCallLogList_GET() {
        GetCallLogsList getCallLogsList = new GetCallLogsList();
        getCallLogsList.setPage(1);
        getCallLogsList.setSortBy("updatedon");
        getCallLogsList.setSortOrder("desc");
        getCallLogsList.setPage_size(25);
        getCallLogsList.setFilter("0");

        String basePath = "call-logs/get";

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, getCallLogsList);

        response.then().statusCode(200);

        JsonPath jp = response.jsonPath();

        response.then().body("message_type", Matchers.containsString("is-success"));
        assertThat(jp.getInt("data.records[0].duration"), Matchers.is(600));
    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"addCallLog_POST"}, groups = "nightly-build")
    public void deleteCallLog_DELETE() {
        GlobalDelete globalDelete = new GlobalDelete();
        globalDelete.setIdsToDelete(callLogId);
        globalDelete.setTableFlag("call_log");
        globalDelete.setFieldKey("id");

        String basePath = "global/delete-record";

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, globalDelete);

        response.then().statusCode(200);

        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Delete Call Log Successful "));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "createAndGetCallLogId", groups = "nightly-build")
    public void userCannotDeleteAutomatedCallLog_Albatross_Test(int callLogId) {
        GlobalDelete globalDelete = new GlobalDelete();
        globalDelete.setIdsToDelete(callLogId);
        globalDelete.setTableFlag("call_log");
        globalDelete.setFieldKey("id");

        String basePath = "global/delete-record";

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true, globalDelete);

        response.then().statusCode(200);
        response.then().body("message_type", Matchers.is("is-danger"));
        response.then().body("message", Matchers.is("Deleting an automated call log is not allowed"));
        response.then().body("status", Matchers.is("fail"));
        response.then().body("data", Matchers.empty());
    }

    @DataProvider
	public Object[][] createAndGetCallLogId() {
		JsonPath json = function.createNewCallLog(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
		int newCallLogId = json.get("id");

		ReaperIntegration.updateAutomatedCallLog(ThreadManager.getAccount().getAccountId());

		return new Object[][]{
            {newCallLogId}
        };
	}

}
