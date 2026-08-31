package io.recruitcrm.ostrich;

import io.rcrm.api.testbase.TestBase;

import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.ostrich.TaskSequencing;
import io.rcrm.api.restclient.RestClient;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TaskSequencingTest extends TestBase {
    commanFunction function = new commanFunction();

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getLatestTask_POST() {
        JsonPath jp = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        String taskTitle = jp.get("title");
        int taskId = jp.getInt("id");
        TaskSequencing taskSequencing = new TaskSequencing();
        taskSequencing.setUserfilter(0);
        taskSequencing.setSortOrder("desc");
        taskSequencing.setSearchTerm(taskTitle);

        String basePath = "sequencing/get-latest-task";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, taskSequencing);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/activities/getLatestTask.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.records.id", Matchers.contains(taskId));
        response.then().body("data.records.title", Matchers.contains(taskTitle));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getLatestTask_POST_401() {
        TaskSequencing taskSequencing = new TaskSequencing();
        taskSequencing.setStatus(null);
        taskSequencing.setUserfilter(0);
        taskSequencing.setUserfilterslug("");
        taskSequencing.setStartdate(null);
        taskSequencing.setEnddate(null);
        taskSequencing.setSortOrder("desc");

        String basePath = "sequencing/get-latest-task";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, taskSequencing);
        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getLatestTask_POST_422() {
        TaskSequencing taskSequencing = new TaskSequencing();
        taskSequencing.setSortOrder("abc");

        String basePath = "sequencing/get-latest-task";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, taskSequencing);
        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("data.message", Matchers.is("The selected sort order is invalid."));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getTaskList_POST() {
        JsonPath jp = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        String taskTitle = jp.getString("title");
        TaskSequencing taskSequencing = new TaskSequencing();
        taskSequencing.setSort_by("startdate");
        taskSequencing.setSortOrder("asc");
        taskSequencing.setPage(1);
        taskSequencing.setPage_size(50);
        taskSequencing.setSearchentity(5);
        taskSequencing.setSearchTerm(taskTitle);

        String basePath = "sequencing/get-task-list";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, taskSequencing);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.containsString("is-success"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getTaskList_POST_401() {
        JsonPath jp = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        String taskTitle = jp.get("title");
        TaskSequencing taskSequencing = new TaskSequencing();
        taskSequencing.setSort_by("startdate");
        taskSequencing.setSortOrder("asc");
        taskSequencing.setPage(1);
        taskSequencing.setPage_size(50);
        taskSequencing.setSearchentity(5);
        taskSequencing.setSearchTerm(taskTitle);

        String basePath = "sequencing/get-task-list";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, taskSequencing);
        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getTaskList_POST_422() {
        TaskSequencing taskSequencing = new TaskSequencing();
        taskSequencing.setSortOrder("abc");

        String basePath = "sequencing/get-task-list";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, taskSequencing);
        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("data.message", Matchers.is("The selected sort order is invalid. (and 1 more error)"));
    }

}
