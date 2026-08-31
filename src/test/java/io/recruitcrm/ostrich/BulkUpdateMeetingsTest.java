package io.recruitcrm.ostrich;

import java.util.ArrayList;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.albatross.MeetingTypeCustomizationPage;
import io.rcrm.api.pojo.albatross.MeetingTypePage;
import io.rcrm.api.pojo.ostrich.BulkUpdate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class BulkUpdateMeetingsTest extends TestBase {
    commanFunction function = new commanFunction();
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void bulkUpdateMeetings() {
        ArrayList<Integer> meetingIDs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            int meetingId = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate")
                    .jsonPath().get("id");
            meetingIDs.add(meetingId);
        }

        String[] updateKeys = { "notetype", "collaborator", "ownerid" };
        Object[] updateValues = { String.valueOf(createMeetingType()), createTeams() + "-1," + getAdminId() + "-2",
                String.valueOf(getAdminId()) };

        for (int i = 0; i < updateKeys.length; i++) {
            BulkUpdate bulkUpdate = new BulkUpdate();
            bulkUpdate.setKey(updateKeys[i]);
            bulkUpdate.setValue(updateValues[i].toString());
            bulkUpdate.setType(2);
            bulkUpdate.setIds(meetingIDs);

            String basePath = "/tasks-and-meetings/bulk-update";
            Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                    null, true, bulkUpdate);

            Assert.assertEquals(response.getStatusCode(), 200);
            response.then().assertThat()
                    .body(matchesJsonSchemaInClasspath("privateApi/activities/bulkUpateActivity.json"));
            response.then().body("message_type", Matchers.containsString("is-success"));
            response.then().body("message", Matchers.is("2 records updated successfully"));
        }
    }


    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void bulkUpdateMeetings_401() {
        ArrayList<Integer> meetingIDs = new ArrayList<>();
        int meetingId = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath()
                .get("id");
        meetingIDs.add(meetingId);

        String[] updateKeys = { "notetype", "collaborator", "ownerid" };
        Object[] updateValues = { String.valueOf(createMeetingType()), createTeams() + "-1," + getAdminId() + "-2",
                String.valueOf(getAdminId()) };

        for (int i = 0; i < updateKeys.length; i++) {
            BulkUpdate bulkUpdate = new BulkUpdate();
            bulkUpdate.setKey(updateKeys[i]);
            bulkUpdate.setValue(updateValues[i].toString());
            bulkUpdate.setType(2);
            bulkUpdate.setIds(meetingIDs);

            String basePath = "/tasks-and-meetings/bulk-update";
            Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                    ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, bulkUpdate);

            Assert.assertEquals(response.getStatusCode(), 401);
            response.then().body("error", Matchers.containsString("Unauthorized"));
        }
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void bulkUpdateMeetings_422() {
        String[] updateKeys = { "notetype", "collaborator", "ownerid" };
        Object[] updateValues = { String.valueOf(createMeetingType()), createTeams() + "-1," + getAdminId() + "-2",
                String.valueOf(getAdminId()) };

        for (int i = 0; i < updateKeys.length; i++) {
            BulkUpdate bulkUpdate = new BulkUpdate();
            bulkUpdate.setKey(updateKeys[i]);
            bulkUpdate.setValue(updateValues[i].toString());
            bulkUpdate.setType(2);

            String basePath = "/tasks-and-meetings/bulk-update";
            Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                    null, true, bulkUpdate);

            Assert.assertEquals(response.getStatusCode(), 422);
            response.then().body("data.message", Matchers.containsString("The ids field is required."));
        }
    }

    public int createTeams() {
        int team1Id = 0;
        Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        int accountOwnerid = user.get("[0].id");
        int teamMember = user.get("[3].id");

        ArrayList<String> userId1 = new ArrayList<String>();
        userId1.add(String.valueOf(accountOwnerid));
        userId1.add(String.valueOf(teamMember));

        allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(),"team1",userId1);
        team1Id  = function.getTeams(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("[0].team_id");

        return team1Id;
    }

    public int createMeetingType() {
        JsonPath jp = allCrudFunctions.createCustomMeeting(albatrossURL, ThreadManager.getOwnerAlbatrossToken())
                .jsonPath();
        return jp.get("data.customizeMeetingType[0].id");
    }

    public int getAdminId() {
        JsonPath users = function.getUsers(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        return users.get("[1].id");
    }
}

