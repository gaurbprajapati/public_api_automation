package io.recruitcrm.ostrich;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.api.util.DateUtil;
import io.rcrm.api.pojo.ostrich.*;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetTaskTest extends TestBase {
    commanFunction function = new commanFunction();
    String AccOwnerName;


    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getListOfTasks() {
        JsonPath jp = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        int taskId = jp.getInt("id");
        String taskTitle = jp.getString("title");
        Tasks tasks = new Tasks();
        tasks.setPageSize(25);
        tasks.setPage(1);
        tasks.setSortBy("updatedon");
        tasks.setSortOrder("desc");

        String basePath = "/tasks-and-meetings/tasks";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/activities/getTask.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.records.id", Matchers.contains(taskId));
        response.then().body("data.records.title", Matchers.contains(taskTitle));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getListOfTasks_401() {
        function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        Tasks tasks = new Tasks();
        tasks.setPageSize(25);
        tasks.setPage(1);
        tasks.setSortBy("updatedon");
        tasks.setSortOrder("desc");

        String basePath = "/tasks-and-meetings/tasks";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getListOfTasks_422() {
        function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        Tasks tasks = new Tasks();
        tasks.setSortOrder("abc");

        String basePath = "/tasks-and-meetings/tasks";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("data.message", Matchers.is("The selected sort order is invalid."));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getCountOfTasks() {
        for (int i = 0; i < 2; i++) {
            function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        }
        Tasks tasks = new Tasks();
        String currentUnixTimestampString = Long.toString(Instant.now().getEpochSecond());
        tasks.setCurrentTime(currentUnixTimestampString);

        String basePath = "/tasks-and-meetings/tasks/count";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/activities/getTaskCount.json"));
        response.then().body("data.totalTaskCount", Matchers.is(2));
        response.then().body("message_type", Matchers.containsString("is-success"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getCountOfTasks_401() {
        function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate");

        Tasks tasks = new Tasks();
        String currentUnixTimestampString = Long.toString(Instant.now().getEpochSecond());
        tasks.setCurrentTime(currentUnixTimestampString);

        String basePath = "/tasks-and-meetings/tasks/count";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getCountOfTasks_422() {
        function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        Tasks tasks = new Tasks();
        tasks.setCurrentTime("/*-+");

        String basePath = "/tasks-and-meetings/tasks/count";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("data.message", Matchers.containsString("The current time must be an integer."));
    }

    @Owner("Harika")
    @Test
    public void searchTasks() {
        JsonPath jp = function.createNewTask(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        int taskId = jp.getInt("id");
        String taskTitle = jp.getString("title");
        Tasks tasks = new Tasks();
        tasks.setSearchTerm(taskTitle);

        String basePath = "/tasks-and-meetings/tasks";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("data.filtered_count", Matchers.is(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/activities/getTask.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.filtered_count", Matchers.is(1));
        response.then().body("data.records.id", Matchers.contains(taskId));
        response.then().body("data.records.title", Matchers.contains(taskTitle));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void filterTasks() {
        int ownerId = getAccOwnerId();
        JsonPath jp = function.createNewTaskWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "candidate", String.valueOf(ownerId),"").jsonPath();
        int taskId = jp.getInt("id");
        String taskTitle = jp.getString("title");
        String candidateSlug = jp.get("related_to");
        ArrayList<Integer> userId = new ArrayList<>();
        ArrayList<String> userSlug = new ArrayList<>();
        ArrayList<Integer> taskStatus = new ArrayList<>();
        ArrayList<Integer> relatedToType = new ArrayList<>();

        userId.add(ownerId);
        userSlug.add(AccOwnerName);
        taskStatus.add(2);
        relatedToType.add(5);

        Map<String, List<String>> relatedTo = new HashMap<>();

        relatedTo.put("2", new ArrayList<>());
        relatedTo.put("3", new ArrayList<>());
        relatedTo.put("4", new ArrayList<>());
        relatedTo.put("5", new ArrayList<>());
        relatedTo.put("11", new ArrayList<>());

        relatedTo.get("5").add(candidateSlug);

        DateRange createdOn = new DateRange();
        createdOn.setId(2);
        createdOn.setStartdate(DateUtil.getStartDayEpochTime());
        createdOn.setEnddate(DateUtil.getEndDayEpochTime());

        Tasks tasks = new Tasks();
        tasks.setPageSize(25);
        tasks.setPage(1);
        tasks.setSortBy("updatedon");
        tasks.setSortOrder("desc");
        tasks.setCurrentTime(DateUtil.getCurrentEpochTime());
        tasks.setUserIds(userId);
        tasks.setUserSlugs(userSlug);
        tasks.setCreatedOn(createdOn);
        tasks.setAssociations(relatedTo);
        tasks.setRelatedTo(relatedTo);
        tasks.setIsFilterApplied(true);
        tasks.setCreatedBy(userId);
        tasks.setUpdatedBy(userId);
        tasks.setUpdatedOn(createdOn);
        tasks.setOwnerIds(userId);
        tasks.setCreatedByIds(userId);
        tasks.setUpdatedByIds(userId);
        tasks.setTaskStatus(taskStatus);
        tasks.setRelatedToType(relatedToType);

        String basePath = "/tasks-and-meetings/tasks";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, tasks);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("data.filtered_count", Matchers.is(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/activities/getTask.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.records.id", Matchers.contains(taskId));
        response.then().body("data.records.title", Matchers.contains(taskTitle));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void globalSaveTasks() {
        GlobalSaveCommonProperties commonProperties1 = new GlobalSaveCommonProperties();
        commonProperties1.setListPageOrder(1);
        GlobalSaveCommonProperties commonProperties2 = new GlobalSaveCommonProperties();
        commonProperties2.setListPageOrder(2);
        GlobalSaveCommonProperties commonProperties3 = new GlobalSaveCommonProperties();
        commonProperties3.setListPageOrder(3);
        GlobalSaveCommonProperties commonProperties4 = new GlobalSaveCommonProperties();
        commonProperties4.setListPageOrder(4);
        GlobalSaveCommonProperties commonProperties5 = new GlobalSaveCommonProperties();
        commonProperties5.setListPageOrder(5);
        GlobalSaveCommonProperties commonProperties6 = new GlobalSaveCommonProperties();
        commonProperties6.setListPageOrder(6);
        GlobalSaveCommonProperties commonProperties7 = new GlobalSaveCommonProperties();
        commonProperties7.setListPageOrder(7);
        GlobalSavecolumns columns = new GlobalSavecolumns();
        columns.setTitle(commonProperties1);
        columns.setStatus(commonProperties2);
        columns.setAssociations(commonProperties3);
        columns.setCollaborator(commonProperties4);
        columns.setCreatedby(commonProperties5);
        columns.setCreatedon(commonProperties6);
        columns.setDescription(commonProperties7);

        String columnStateJson = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            columnStateJson = mapper.writeValueAsString(columns);
        } catch (Exception e) {
            e.printStackTrace();
        }

        GlobalSave globalSave = new GlobalSave();
        globalSave.setColumnstate(columnStateJson);
        globalSave.setDatatablekey("tasks");

        String basePath = "/global/save-state";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, globalSave);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.containsString("is-success"));
    }

    public int getAccOwnerId() {
        JsonPath users = function.getUsers(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        AccOwnerName = users.get("[0].first_name") + "_" + users.get("[0].last_name");
        return users.get("[0].id");
    }
}