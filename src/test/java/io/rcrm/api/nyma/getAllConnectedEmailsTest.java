package io.rcrm.api.nyma;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn|Email1|Email2")
public class getAllConnectedEmailsTest extends TestBase {

    commanFunction function = new commanFunction();

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void getTeamConnectedEmailList(){
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("offset", 0);
        requestBody.put("page_size", 5);

        Response response = RestClient.doGet1("JSON", nymaURLv3, "/team-connected-emails", ThreadManager.getOwnerAlbatrossToken(), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().body("status", Matchers.is("success"));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//email//getTeamConnectedEmailList.json"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void getInvalidBodyTeamConnectedEmailList(){
        Map<String, Object> requestBody = new HashMap<>();
        Response response = RestClient.doGet1("JSON", nymaURLv3, "/team-connected-emails", ThreadManager.getOwnerAlbatrossToken(), null, null, true, requestBody);

        response.then().statusCode(422);
        response.then().body("status", Matchers.is("fail"));
        response.then().body("message", Matchers.is("The offset field is required.,The page size field is required."));
        response.then().body("message_type", Matchers.is("is-danger"));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void getInvalidAuthTeamConnectedEmailList() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("offset", 0);
        requestBody.put("page_size", 5);
        Response response = RestClient.doGet1("JSON", nymaURLv3, "/team-connected-emails", ThreadManager.getOwnerAlbatrossToken()+"1234", null, null, true, requestBody);
        response.then().statusCode(401);
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void getTeamConnectedEmailDetail(){
        String authApiToken = ThreadManager.getAccountApiKey();
        Response response = function.getUsers(baseURL, authApiToken);
        response.then().statusCode(200);

        JsonPath user = response.jsonPath();
        int adminId = user.get("[1].id");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("team_user_id", adminId);
        requestBody.put("team_selected_linked_email_type", 1);
        response = RestClient.doPost("JSON", nymaURLv3, "/connected-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, requestBody);
        response.then().statusCode(200);

        response.then().body("status", Matchers.is("success"));
        response.then().body(matchesJsonSchemaInClasspath("privateApi//email//getTeamConnectedEmail.json"));
    }
}