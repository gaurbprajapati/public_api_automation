package io.rcrm.api.externalJobBoards.broadbean;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.pojo.externalJobBoards.broadbean.BroadbeanAdcUserName;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.is;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class MapAdcUsernameBroadbeanJobBoardTest extends TestBase {

    JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();
    String broadbeanAdcUsername = javaFakerJobBoards.getAdcUsername();

    int generatedNums = Integer.parseInt(RandomStringUtils.randomNumeric(4));

    @Owner("Gaurav Prajapati")
    @Test(groups = "nightly-build")
    public void mapAdcUsernameFromBroadbeanAccountWithInvalidId() {
        BroadbeanAdcUserName broadbeanAdcUserName = new BroadbeanAdcUserName(generatedNums, broadbeanAdcUsername + " mapped");

        String basePath = "/broadbean/map-adcusername/map";

        Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, broadbeanAdcUserName);

        response.then().statusCode(404);
        response.then().body("error", is(true));
        response.then().body("error_code", is(404));
        response.then().body("error_message", is("Account connection not found"));
        response.then().body("silent_progress", is(false));
    }

    @Owner("Yash Rampal")
    @Test(groups = "nightly-build")
    public void mapAdcUsernameFromBroadbeanAccountWithInvalidAuth() {
        BroadbeanAdcUserName broadbeanAdcUserName = new BroadbeanAdcUserName(generatedNums, broadbeanAdcUsername + " mapped");

        String basePath = "/broadbean/map-adcusername/map";

        Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken() + 123,
                null, true, broadbeanAdcUserName);

        response.then().statusCode(401);
        response.then().body("error", is("Unauthorized"));
    }
}
