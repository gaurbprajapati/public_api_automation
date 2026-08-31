package io.recruitcrm.ostrich;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.ArrayList;

import io.rcrm.api.pojo.ostrich.UpdateActivityInline;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.ostrich.Tasks;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class UpdateTaskTest extends TestBase {
    commanFunction function = new commanFunction();
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void updateTaskCollaborator() {
        int taskId = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath()
                .get("id");
        UpdateActivityInline tasks = new UpdateActivityInline();
        tasks.setActivity_id(taskId);
        tasks.setUser_id(getAdminId());
        tasks.setTeam_id(createTeams());
        tasks.setActivity_type(1);
        tasks.setEventType("add");

        String basePath = "/associate-events/update-collaborator";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/activities/updateActivtyCollaborator.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.is("Collaborator updated successfully"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void updateTaskCollaborator_422() {
        int taskId = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath()
                .get("id");
        UpdateActivityInline tasks = new UpdateActivityInline();
        tasks.setActivity_id(taskId);
        tasks.setUser_id(getAdminId());
        tasks.setTeam_id(createTeams());
        tasks.setActivity_type(1);
        tasks.setEventType("adds");

        String basePath = "/associate-events/update-collaborator";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("data.message", Matchers.is("The selected event type is invalid."));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void updateTaskCollaborator_401() {
        int taskId = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath()
                .get("id");
        UpdateActivityInline tasks = new UpdateActivityInline();
        tasks.setActivity_id(taskId);
        tasks.setUser_id(getAdminId());
        tasks.setTeam_id(createTeams());
        tasks.setActivity_type(1);
        tasks.setEventType("add");

        String basePath = "/associate-events/update-collaborator";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
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
