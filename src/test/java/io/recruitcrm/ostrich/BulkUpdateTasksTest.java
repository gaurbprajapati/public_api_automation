package io.recruitcrm.ostrich;

import java.util.ArrayList;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.ostrich.BulkUpdate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class BulkUpdateTasksTest extends TestBase {
    commanFunction function = new commanFunction();
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();


    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void bulkUpdateTasks() {
        ArrayList<Integer> taskIDs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            int taskId = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath()
                    .get("id");
            taskIDs.add(taskId);
        }
        int taskTypeId = allCrudFunctions.getTaskTypeId(albatrossURL,ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("data.customizeTaskType[0].id");

        String[] updateKeys = { "title", "dueDate", "collaborator", "owner", "notetype" };
        Object[] updateValues = { "title " + RandomStringUtils.randomAlphabetic(4),
                Long.toString(System.currentTimeMillis()), createTeams() + "-1" + "," + getAdminId() + "-2",
                getAdminId(), String.valueOf(taskTypeId) };

        for (int i = 0; i < updateKeys.length; i++) {
            BulkUpdate bulkUpdate = new BulkUpdate();
            bulkUpdate.setKey(updateKeys[i]);
            bulkUpdate.setValue(updateValues[i].toString());
            bulkUpdate.setType(1);
            bulkUpdate.setIds(taskIDs);

            String basePath = "/tasks-and-meetings/bulk-update";
            Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                    null, true, bulkUpdate);

            Assert.assertEquals(response.getStatusCode(), 200);
            response.then().body("message", Matchers.is("2 records updated successfully"));
        }
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void bulkUpdateTasks_401() {
        ArrayList<Integer> taskIDs = new ArrayList<>();
        String[] updateKeys = { "title", "dueDate", "collaborator", "owner" };
        Object[] updateValues = { "title " + RandomStringUtils.randomAlphabetic(4),
                Long.toString(System.currentTimeMillis()), createTeams() + "-1" + "," + getAdminId() + "-2",
                getAdminId() };

        for (int i = 0; i < updateKeys.length; i++) {
            BulkUpdate bulkUpdate = new BulkUpdate();
            bulkUpdate.setKey(updateKeys[i]);
            bulkUpdate.setValue(updateValues[i].toString());
            bulkUpdate.setType(1);
            bulkUpdate.setIds(taskIDs);

            String basePath = "/tasks-and-meetings/bulk-update";
            Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                    ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, bulkUpdate);

            response.then().statusCode(401);
            response.then().body("error", Matchers.containsString("Unauthorized"));
        }
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void bulkUpdateTasks_422() {
        ArrayList<Integer> taskIDs = new ArrayList<>();
        String[] updateKeys = { "title", "dueDate", "collaborator", "owner" };
        Object[] updateValues = { "title " + RandomStringUtils.randomAlphabetic(4),
                Long.toString(System.currentTimeMillis()), createTeams() + "-1" + "," + getAdminId() + "-2",
                getAdminId() };

        for (int i = 0; i < updateKeys.length; i++) {
            BulkUpdate bulkUpdate = new BulkUpdate();
            bulkUpdate.setKey(updateKeys[i]);
            bulkUpdate.setValue(updateValues[i].toString());
            bulkUpdate.setType(99);
            bulkUpdate.setIds(taskIDs);

            String basePath = "/tasks-and-meetings/bulk-update";
            Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                    null, true, bulkUpdate);

            Assert.assertEquals(response.getStatusCode(), 422);

            response.then().body("data.message", Matchers.is("The ids field is required. (and 1 more error)"));
        }
    }

    public int getAdminId() {
        JsonPath users = function.getUsers(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        int adminId = users.get("[1].id");
        return adminId;
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
}
