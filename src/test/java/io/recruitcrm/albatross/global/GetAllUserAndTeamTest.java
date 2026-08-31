package io.recruitcrm.albatross.global;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.rcrm.api.testbase.TestBase.AccountType;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllUserAndTeamTest extends TestBase {

    public GetAllUserAndTeamTest() {
        super();
    }

    String path = "global/get-all-users-and-teams";

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void getAllUsersAndTeams() {
        Response response = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken(), null, true, null);


        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//allUsersAndTeams//getAllUsersAndTeams.json"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getAllUsersAndTeamsInvalidEndpoint() {
        Response response = RestClient.doPost("JSON", albatrossURL, path + 12345, ThreadManager.getOwnerAlbatrossToken(), null, true, null);


        Assert.assertEquals(response.getStatusCode(), 404);
        response.then().body("message", Matchers.is("HTTP Error"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void getAllUsersAndTeamsUnauthorizedAccess() {
        Response response = RestClient.doPost("JSON", albatrossURL, path, ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, null);


        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }
}
