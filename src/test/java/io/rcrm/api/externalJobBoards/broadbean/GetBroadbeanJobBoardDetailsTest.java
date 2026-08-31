package io.rcrm.api.externalJobBoards.broadbean;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.is;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetBroadbeanJobBoardDetailsTest extends TestBase {


    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void getBroadbeanJobBoardDetailsWithInvalidAuth() {
        String basePath = "/broadbean/get-connected-account/job_detail";
        Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken() + 123,
                null, null, true);

        response.then().statusCode(401);
        response.then().body("error", is("Unauthorized"));
    }
}
