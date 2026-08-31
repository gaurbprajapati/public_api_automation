package io.rcrm.api.externalJobBoards.broadbean;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.externalJobBoards.broadbean.BroadbeanPermission;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.is;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AddBroadbeanJobBoardPermissionTest extends TestBase {


    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void addBroadbeanJobBoardPermissionWithInvalidAuth() {
        BroadbeanPermission broadbeanJobBoard = new BroadbeanPermission("1");

        String basePath = "/broadbean/permission";

        Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken() + 123, null, true,
                broadbeanJobBoard);

        response.then().statusCode(401);
        response.then().body("error", is("Unauthorized"));
    }
}
