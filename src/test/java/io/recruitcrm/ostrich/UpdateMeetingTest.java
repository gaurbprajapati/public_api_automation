package io.recruitcrm.ostrich;
import java.util.*;

import io.rcrm.api.pojo.ostrich.Attendee;
import io.rcrm.api.pojo.ostrich.UpdateActivityInline;
import io.rcrm.api.pojo.ostrich.UpdateAttendee;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.ostrich.Meetings;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class UpdateMeetingTest extends TestBase {
    commanFunction function = new commanFunction();
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void updateMeetingCollaborator() {
        int meetingId = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath()
                .get("id");
        UpdateActivityInline meetings = new UpdateActivityInline();
        meetings.setActivity_id(meetingId);
        meetings.setUser_id(getAdminId());
        meetings.setTeam_id(createTeams());
        meetings.setActivity_type(2);
        meetings.setEventType("add");

        String basePath = "/associate-events/update-collaborator";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/activities/updateActivtyCollaborator.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.is("Collaborator updated successfully"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void updateMeetingCollaborator_401() {
        UpdateActivityInline meetings = new UpdateActivityInline();
        meetings.setActivity_id(123);
        meetings.setUser_id(getAdminId());
        meetings.setTeam_id(createTeams());
        meetings.setActivity_type(2);
        meetings.setEventType("1");

        String basePath = "/associate-events/update-collaborator";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, meetings);

        response.then().statusCode(401);
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void updateMeetingCollaborator_404() {
        int meetingId = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath()
                .get("id");
        UpdateActivityInline meetings = new UpdateActivityInline();
        meetings.setActivity_id(meetingId);
        meetings.setUser_id(getAdminId());
        meetings.setTeam_id(createTeams());
        meetings.setActivity_type(3);
        meetings.setEventType("add");

        String basePath = "/associate-events/update-collaborator";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);

        response.then().statusCode(404);
        response.then().body("message", Matchers.is("Activity not found"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void updateMeetingAttendee() {
        int meetingId = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath()
                .get("id");
        JsonPath candidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
                .jsonPath();
        String candidateSlug = candidate.get("slug");
        String email = candidate.get("email");
        String name = candidate.get("first_name") + " " + candidate.get("last_name");

        Attendee attendee = new Attendee();
        attendee.setAttendeeid(candidateSlug);
        attendee.setAttendeetype(5);
        attendee.setEmail(email);
        attendee.setName(name);
        attendee.setAppointmentid(meetingId);

        List<Attendee> attendees = new ArrayList<>();
        attendees.add(attendee);

        UpdateAttendee meetings = new UpdateAttendee();
        meetings.setAttendees(attendees);
        meetings.setMeeting_id(meetingId);
        meetings.setEvent_type("add");

        String basePath = "meetings/update-attendee";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);

        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/activities/updateMeetingAttendee.json"));
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.containsString("success"));
        response.then().body("message", Matchers.is("Attendees updated"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void updateMeetingAttendee_401() {
        int meetingId = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath()
                .get("id");
        JsonPath candidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
                .jsonPath();
        String candidateSlug = candidate.get("slug");
        String email = candidate.get("email");
        String name = candidate.get("first_name") + " " + candidate.get("last_name");

        Attendee attendee = new Attendee();
        attendee.setAttendeeid(candidateSlug);
        attendee.setAttendeetype(5);
        attendee.setEmail(email);
        attendee.setName(name);
        attendee.setAppointmentid(meetingId);

        List<Attendee> attendees = new ArrayList<>();
        attendees.add(attendee);

        UpdateAttendee meetings = new UpdateAttendee();
        meetings.setAttendees(attendees);
        meetings.setMeeting_id(meetingId);
        meetings.setEvent_type("add");

        String basePath = "meetings/update-attendee";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"abc",
                null, true, meetings);

        Assert.assertEquals(response.getStatusCode(), 401);
    }

    public int getAdminId() {
        JsonPath users = function.getUsers(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        return users.get("[1].id");
    }

    public int createTeams() {
        int team1Id = 0;
        Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        int accountOwnerid = user.get("[0].id");
        int teamMember = user.get("[3].id");

        ArrayList<String> userId1 = new ArrayList<String>();
        userId1.add(String.valueOf(accountOwnerid));
        userId1.add(String.valueOf(teamMember));

        allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(),"team1",userId1);
        team1Id  = function.getTeams(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("[0].team_id");

        return team1Id;
    }


}