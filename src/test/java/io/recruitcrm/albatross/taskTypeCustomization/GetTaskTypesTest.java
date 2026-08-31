package io.recruitcrm.albatross.taskTypeCustomization;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetTaskTypesTest extends TestBase {

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getTaskTypes_Test() {

        Response response = RestClient.doGet("JSON", albatrossURL, "task-types", ThreadManager.getOwnerAlbatrossToken(), null, null, true);

        response.then().statusCode(200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message_type", Matchers.containsString("is-success"));

    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getTaskTypesInvalidAuth_Test() {

        Response response = RestClient.doGet("JSON", albatrossURL, "task-types", ThreadManager.getOwnerAlbatrossToken()+"123", null, null,
                true);
        response.then().statusCode(401);

    }
}
