package io.recruitcrm.albatross.taskTypeCustomization;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
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
public class EditDeleteTaskTypeTest extends TestBase {

    String generatedString = RandomStringUtils.randomAlphabetic(4);
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void editTaskType_Test() {

        int id = allCrudFunctions.getTaskTypeId(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("data.customizeTaskType[0].id");

        TaskTypePage taskTypePage = new TaskTypePage();
        taskTypePage.setId(id);
        taskTypePage.setLabel("Task Type updated " + generatedString);
        taskTypePage.setDefault(0);
        ArrayList<Object> taskTypes = new ArrayList<>();
        taskTypes.add(taskTypePage);

        TaskTypeCustomizationPage taskTypeCustomizationPage = new TaskTypeCustomizationPage();
        taskTypeCustomizationPage.setCustomizedTaskTypes(taskTypes);


        Response response = RestClient.doPost("JSON", albatrossURL, "task-types", ThreadManager.getOwnerAlbatrossToken(), null, true, taskTypeCustomizationPage);

        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("action_name", Matchers.containsString("Task Type Customization"));
        response.then().body("message", Matchers.containsString("Task Type Customization Successful"));
        response.then().body("data.customizeTaskType[0].id", Matchers.notNullValue());
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void editTaskTypeWithInvalidId_Test() {

        int id = allCrudFunctions.getTaskTypeId(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("data.customizeTaskType[0].id");

        TaskTypePage taskTypePage = new TaskTypePage();
        taskTypePage.setId(id + 123);
        taskTypePage.setLabel("Task Type updated " + generatedString);
        taskTypePage.setDefault(0);
        ArrayList<Object> taskTypes = new ArrayList<>();
        taskTypes.add(taskTypePage);

        TaskTypeCustomizationPage taskTypeCustomizationPage = new TaskTypeCustomizationPage();
        taskTypeCustomizationPage.setCustomizedTaskTypes(taskTypes);


        Response response = RestClient.doPost("JSON", albatrossURL, "task-types", ThreadManager.getOwnerAlbatrossToken(), null, true, taskTypeCustomizationPage);

        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("action_name", Matchers.containsString("Task Type Customization"));
        response.then().body("data.customizeTaskType[0].error",
                Matchers.containsString("Task Type does not exists"));

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void deleteTaskType_Test() {

        int id = allCrudFunctions.getTaskTypeId(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("data.customizeTaskType[0].id");

        TaskTypePage taskTypePage = new TaskTypePage();
        taskTypePage.setId(id);
        taskTypePage.setLabel("Task Type " + generatedString);
        taskTypePage.setDefault(1);
        taskTypePage.setDeleted(true);
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
        response.then().body("data.customizeTaskType[0].deleted", Matchers.comparesEqualTo(true));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void deleteTaskTypeWithInvalidId_Test() {

        int id = allCrudFunctions.getTaskTypeId(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath().get("data.customizeTaskType[0].id");

        TaskTypePage taskTypePage = new TaskTypePage();
        taskTypePage.setId(id+123);
        taskTypePage.setLabel("Task Type " + generatedString);
        taskTypePage.setDefault(1);
        taskTypePage.setDeleted(true);
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
        response.then().body("data.customizeTaskType[0].error",
                Matchers.containsString("Task Type does not exists"));
    }

}
