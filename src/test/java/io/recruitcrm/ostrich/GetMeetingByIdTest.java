package io.recruitcrm.ostrich;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.ostrich.MeetingById;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class GetMeetingByIdTest extends TestBase {
    commanFunction function = new commanFunction();


    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getMeetingById() {
        JsonPath jp = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        int meetingId = jp.getInt("id");
        String meetingTitle = jp.get("title");
        String meetingDescription = jp.getString("description");

        String basePath = "calendar/meeting";
        MeetingById getMeetingById = new MeetingById(String.valueOf(meetingId));

        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null,true, getMeetingById);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/activities/getMeetingById.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Meeting fetched successfully."));
        response.then().body("data[0].id", equalTo(meetingId));
        response.then().body("data[0].title", equalTo(meetingTitle));
        response.then().body("data[0].description", equalTo(meetingDescription));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getMeetingByInvalidId() {

        String basePath = "calendar/meeting";
        MeetingById getMeetingById = new MeetingById("123");

        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null,true, getMeetingById);

        response.then().statusCode(404);
        response.then().body("message_type", Matchers.containsString("is-danger"));
        response.then().body("message", Matchers.containsString("Activity not found"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getMeetingById_401() {
        JsonPath jp = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        int meetingId = jp.getInt("id");

        String basePath = "calendar/meeting";
        MeetingById getMeetingById = new MeetingById(String.valueOf(meetingId));

        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123",
                null,true, getMeetingById);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getMeetingById_422() {
        String basePath = "calendar/meeting";
        MeetingById getMeetingById = new MeetingById("abc");

        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null,true, getMeetingById);

        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("data.message", Matchers.containsString("The id must be an integer."));
    }

}
