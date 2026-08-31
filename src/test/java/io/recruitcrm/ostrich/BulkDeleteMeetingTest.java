package io.recruitcrm.ostrich;

import java.util.ArrayList;

import io.rcrm.api.pojo.ostrich.BulkDelete;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.ostrich.Meetings;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class BulkDeleteMeetingTest extends TestBase {
    commanFunction function = new commanFunction();

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void bulkDeleteMeetings() {
        ArrayList<Integer> meetingIDs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            int meetingId = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate")
                    .jsonPath().get("id");
            meetingIDs.add(meetingId);
        }
        BulkDelete meetings = new BulkDelete();
        meetings.setIdsToDelete(meetingIDs);
        meetings.setType(2);

        String basePath = "/tasks-and-meetings/bulk-delete";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/activities/bulkDeleteActivity.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Records deleted successfully"));
        response.then().body("data.count", Matchers.is(2));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void bulkDeleteMeetings_401() {
        ArrayList<Integer> meetingIDs = new ArrayList<>();
        int meetingId = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath()
                .get("id");
        meetingIDs.add(meetingId);

        BulkDelete meetings = new BulkDelete();
        meetings.setIdsToDelete(meetingIDs);
        meetings.setType(2);

        String basePath = "/tasks-and-meetings/bulk-delete";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, meetings);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void bulkDeleteMeetings_422() {
        BulkDelete meetings = new BulkDelete();
        meetings.setType(2);

        String basePath = "/tasks-and-meetings/bulk-delete";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);

        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("data.message", Matchers.containsString("The ids to delete field is required."));
    }

}

