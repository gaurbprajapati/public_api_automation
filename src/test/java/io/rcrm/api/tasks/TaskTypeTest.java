package io.rcrm.api.tasks;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.pojo.Task;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TaskTypeTest extends TestBase {

    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
    JavaFakerTask fakerTask = new JavaFakerTask();
    String taskTitle = fakerTask.getTaskName();
    String taskDescription = fakerTask.getDescription();
    String startDate = fakerTask.getFutureDate();
    int taskTypeId;

    @Owner("Harika")
    @Test(priority = 0, groups = "nightly-build")
    public void getTaskTypes() {
        allCrudFunctions.getTaskTypeId(albatrossURL, ThreadManager.getOwnerAlbatrossToken());

        Response response = RestClient.doGet("JSON", baseURL, "task-types", ThreadManager.getAccountApiKey(), null,null, true);

        response.then().statusCode(200);

        JsonPath jp = response.jsonPath();

        taskTypeId = jp.get("[0].id");
    }

    @Owner("Harika")
    @Test(priority = 1, groups = "nightly-build")
    public void createTaskWithInvalidTaskType() {

        JsonPath json;
        String entitySlug = "";

        json = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        entitySlug = json.get("slug");

        Task task = new Task();
        task.setTitle(taskTitle);
        task.setDescription(taskDescription);
        task.setReminder(15);
        task.setRelated_to(entitySlug);
        task.setRelated_to_type("candidate");
        task.setStart_date(startDate);
        task.setTask_type_id(taskTypeId+123);

        Response response = RestClient.doPost("JSON", baseURL, "tasks", ThreadManager.getAccountApiKey(), null, true, task);

        response.then().statusCode(422);

        response.then().body("task_type_id[0]", Matchers.equalTo("Invalid task type id"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getTaskTypeWithInvalidAuth() {

        Response response = RestClient.doGet("JSON", baseURL, "task-types", ThreadManager.getAccountApiKey()+"123", null,null, true);


        response.then().statusCode(401);

    }
}
