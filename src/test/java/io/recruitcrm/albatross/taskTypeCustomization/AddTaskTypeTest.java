package io.recruitcrm.albatross.taskTypeCustomization;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerNote;
import io.rcrm.api.pojo.albatross.TaskTypeCustomizationPage;
import io.rcrm.api.pojo.albatross.TaskTypePage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.ArrayList;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AddTaskTypeTest extends TestBase {

    String generatedString = RandomStringUtils.randomAlphabetic(4);


    @Owner("Harika")
    @Test(priority = 0, groups = "nightly-build")
    public void createTaskType_Test() {

        TaskTypePage taskTypePage = new TaskTypePage();
        taskTypePage.setLabel("Task Type " + generatedString);
        taskTypePage.setDefault(0);
        ArrayList<Object> taskTypes = new ArrayList<>();
        taskTypes.add(taskTypePage);

        TaskTypeCustomizationPage taskTypeCustomizationPage = new TaskTypeCustomizationPage();
        taskTypeCustomizationPage.setCustomizedTaskTypes(taskTypes);

        Response response = RestClient.doPost("JSON", albatrossURL, "task-types", ThreadManager.getOwnerAlbatrossToken(), null, true,
                taskTypeCustomizationPage);

        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("action_name", Matchers.containsString("Task Type Customization"));
        response.then().body("message", Matchers.containsString("Task Type Customization Successful"));
        response.then().body("data.customizeTaskType[0].id", Matchers.notNullValue());
        response.then().body("data.customizeTaskType[0].is_custom", Matchers.comparesEqualTo(1));
    }

    @Owner("Harika")
    @Test(priority = 1, groups = "nightly-build")
    public void createTaskTypeWithExistingLabel_Test() {

        TaskTypePage taskTypePage = new TaskTypePage();
        taskTypePage.setLabel("Task Type " + generatedString);
        taskTypePage.setDefault(0);
        ArrayList<Object> taskTypes = new ArrayList<>();
        taskTypes.add(taskTypePage);

        TaskTypeCustomizationPage taskTypeCustomizationPage = new TaskTypeCustomizationPage();
        taskTypeCustomizationPage.setCustomizedTaskTypes(taskTypes);

        Response response = RestClient.doPost("JSON", albatrossURL, "task-types", ThreadManager.getOwnerAlbatrossToken(), null, true,
                taskTypeCustomizationPage);

        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("action_name", Matchers.containsString("Task Type Customization"));
        response.then().body("data.customizeTaskType[0].error", Matchers.containsString("Title already Exists"));

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void createTaskType422_Test() {

        TaskTypePage taskTypePage = new TaskTypePage();
        taskTypePage.setLabel(" ");
        taskTypePage.setDefault(0);
        ArrayList<Object> taskTypes = new ArrayList<>();
        taskTypes.add(taskTypePage);

        TaskTypeCustomizationPage taskTypeCustomizationPage = new TaskTypeCustomizationPage();
        taskTypeCustomizationPage.setCustomizedTaskTypes(taskTypes);

        Response response = RestClient.doPost("JSON", albatrossURL, "task-types", ThreadManager.getOwnerAlbatrossToken(), null, true,
                taskTypeCustomizationPage);

        response.then().statusCode(422);
        response.then().body("status", Matchers.containsString("fail"));
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("label field is required"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void createTaskTypeCharLimitExceed_Test() {

        JavaFakerNote fakerNote = new JavaFakerNote();

        TaskTypePage taskTypePage = new TaskTypePage();
        taskTypePage.setLabel(fakerNote.getNoteDescriptionText());
        taskTypePage.setDefault(0);
        ArrayList<Object> taskTypes = new ArrayList<>();
        taskTypes.add(taskTypePage);

        TaskTypeCustomizationPage taskTypeCustomizationPage = new TaskTypeCustomizationPage();
        taskTypeCustomizationPage.setCustomizedTaskTypes(taskTypes);

        Response response = RestClient.doPost("JSON", albatrossURL, "task-types", ThreadManager.getOwnerAlbatrossToken(), null, true,
                taskTypeCustomizationPage);

        response.then().statusCode(422);
        response.then().body("status", Matchers.containsString("fail"));
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("label must not be greater than 50 characters"));

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void createTaskTypeInvalidAuth_Test() {

        TaskTypePage taskTypePage = new TaskTypePage();
        taskTypePage.setLabel("Task Type " + generatedString);
        taskTypePage.setDefault(0);
        ArrayList<Object> taskTypes = new ArrayList<>();
        taskTypes.add(taskTypePage);

        TaskTypeCustomizationPage taskTypeCustomizationPage = new TaskTypeCustomizationPage();
        taskTypeCustomizationPage.setCustomizedTaskTypes(taskTypes);

        Response response = RestClient.doPost("JSON", albatrossURL, "task-types", ThreadManager.getOwnerAlbatrossToken()+"123", null, true,
                taskTypeCustomizationPage);

        response.then().statusCode(401);

    }

}
