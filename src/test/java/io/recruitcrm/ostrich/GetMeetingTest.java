package io.recruitcrm.ostrich;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.api.util.DateUtil;
import io.rcrm.api.pojo.ostrich.*;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetMeetingTest extends TestBase {
    commanFunction function = new commanFunction();
    String AccOwnerName;

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getListOfMeetings() {
        JsonPath jp = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        int meetingId = jp.getInt("id");
        String meetingTitle = jp.get("title");
        String meetingDescription = jp.getString("description");
        Meetings meetings = new Meetings();
        meetings.setPageSize(25);
        meetings.setPage(1);
        meetings.setSortBy("updatedon");
        meetings.setSortOrder("desc");

        String basePath = "/tasks-and-meetings/meetings";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/activities/getMeeting.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.records.id", Matchers.contains(meetingId));
        response.then().body("data.records.title", Matchers.contains(meetingTitle));
        response.then().body("data.records.description", Matchers.contains(meetingDescription));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getListOfMeetings_401() {
        function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        Meetings meetings = new Meetings();
        meetings.setPageSize(25);
        meetings.setPage(1);
        meetings.setSortBy("updatedon");
        meetings.setSortOrder("desc");

        String basePath = "/tasks-and-meetings/meetings";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, meetings);
        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getListOfMeetings_422() {
        function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        Meetings meetings = new Meetings();
        meetings.setSortOrder("abc");

        String basePath = "/tasks-and-meetings/meetings";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);
        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("data.message", Matchers.containsString("The selected sort order is invalid."));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getCountOfMeetings() {
        for (int i = 0; i < 3; i++) {
            function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        }
        Meetings meetings = new Meetings();
        String currentUnixTimestampString = Long.toString(Instant.now().getEpochSecond());
        meetings.setCurrentTime(currentUnixTimestampString);

        String basePath = "/tasks-and-meetings/meetings/count";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("data.total_count", Matchers.is(3));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/activities/getMeetingCount.json"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getCountOfMeetings_401() {
        function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        Meetings meetings = new Meetings();
        String currentUnixTimestampString = Long.toString(Instant.now().getEpochSecond());
        meetings.setCurrentTime(currentUnixTimestampString);

        String basePath = "/tasks-and-meetings/meetings/count";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "12345", null, true, meetings);
        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getCountOfMeetings_422() {
        function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate");
        Meetings meetings = new Meetings();
        meetings.setCurrentTime("-*/+");

        String basePath = "/tasks-and-meetings/meetings/count";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);
        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("data.message", Matchers.containsString("The current time must be an integer."));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void filterMeetings() {
        int  ownerId = getAccOwnerId();
        JsonPath jp = function.createNewMeetingsWithCollaborators(baseURL, ThreadManager.getAccountApiKey(), "candidate", String.valueOf(ownerId),"").jsonPath();
        String meetingTitle = jp.get("title");
        int meetingId = jp.getInt("id");
        String candidateSlug = jp.get("related_to");
        ArrayList<Integer> userId = new ArrayList<>();
        ArrayList<String> userSlug = new ArrayList<>();
        ArrayList<Integer> meetingStatus = new ArrayList<>();
        ArrayList<Integer> relatedToType = new ArrayList<>();

        userId.add(ownerId);
        userSlug.add(AccOwnerName);
        meetingStatus.add(1);
        relatedToType.add(5);

        Map<String, List<String>> relatedTo = new HashMap<>();

        relatedTo.put("2", new ArrayList<>());
        relatedTo.put("3", new ArrayList<>());
        relatedTo.put("4", new ArrayList<>());
        relatedTo.put("5", new ArrayList<>());
        relatedTo.put("11", new ArrayList<>());

        relatedTo.get("5").add(candidateSlug);


        DateRange createdOn = new DateRange();
        createdOn.setId(2);
        createdOn.setStartdate(DateUtil.getStartDayEpochTime());
        createdOn.setEnddate(DateUtil.getEndDayEpochTime());

        Meetings meetings = new Meetings();
        meetings.setPageSize(25);
        meetings.setPage(1);
        meetings.setSortBy("updatedon");
        meetings.setSortOrder("desc");
        meetings.setCurrentTime(DateUtil.getCurrentEpochTime());
        meetings.setUserIds(userId);
        meetings.setUserSlugs(userSlug);
        meetings.setCreatedOn(createdOn);
        meetings.setAssociations(relatedTo);
        meetings.setRelatedTo(relatedTo);
        meetings.setIsFilterApplied(true);
        meetings.setCreatedBy(userId);
        meetings.setUpdatedBy(userId);
        meetings.setUpdatedOn(createdOn);
        meetings.setAttendeesId(userId);
        meetings.setAttendeeSlugs(userSlug);
        meetings.setOwnerIds(userId);
        meetings.setCreatedByIds(userId);
        meetings.setUpdatedByIds(userId);
        meetings.setMeetingStatus(meetingStatus);
        meetings.setRelatedToType(relatedToType);

        String basePath = "/tasks-and-meetings/meetings";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("data.filtered_count", Matchers.is(1));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/activities/getMeeting.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.records.id", Matchers.contains(meetingId));
        response.then().body("data.records.title", Matchers.contains(meetingTitle));
    }

    @Owner("Harika")
    @Test
    public void searchMeetings() {
        JsonPath jp = function.createNewMeetings(baseURL, ThreadManager.getAccountApiKey(), "candidate").jsonPath();
        String meetingTitle = jp.get("title");
        int meetingId = jp.getInt("id");
        Meetings meetings = new Meetings();
        meetings.setSearchTerm(meetingTitle);

        String basePath = "/tasks-and-meetings/meetings";
        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, meetings);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/activities/getMeeting.json"));
        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("data.filtered_count", Matchers.is(1));
        response.then().body("data.records.id", Matchers.contains(meetingId));
        response.then().body("data.records.title", Matchers.contains(meetingTitle));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void globalSaveMeetings() {
        GlobalSaveCommonProperties commonProperties1 = new GlobalSaveCommonProperties();
        commonProperties1.setListPageOrder(1);
        GlobalSaveCommonProperties commonProperties2 = new GlobalSaveCommonProperties();
        commonProperties2.setListPageOrder(2);
        GlobalSaveCommonProperties commonProperties3 = new GlobalSaveCommonProperties();
        commonProperties3.setListPageOrder(3);
        GlobalSaveCommonProperties commonProperties4 = new GlobalSaveCommonProperties();
        commonProperties4.setListPageOrder(4);
        GlobalSaveCommonProperties commonProperties5 = new GlobalSaveCommonProperties();
        commonProperties5.setListPageOrder(5);
        GlobalSaveCommonProperties commonProperties6 = new GlobalSaveCommonProperties();
        commonProperties6.setListPageOrder(6);
        GlobalSaveCommonProperties commonProperties7 = new GlobalSaveCommonProperties();
        commonProperties7.setListPageOrder(7);
        GlobalSavecolumns columns = new GlobalSavecolumns();
        columns.setTitle(commonProperties1);
        columns.setStatus(commonProperties2);
        columns.setAssociations(commonProperties3);
        columns.setCollaborator(commonProperties4);
        columns.setCreatedby(commonProperties5);
        columns.setCreatedon(commonProperties6);
        columns.setDescription(commonProperties7);

        String columnStateJson = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            columnStateJson = mapper.writeValueAsString(columns);
        } catch (Exception e) {
            e.printStackTrace();
        }

        GlobalSave globalSave = new GlobalSave();
        globalSave.setColumnstate(columnStateJson);
        globalSave.setDatatablekey("meetings");

        String basePath = "/global/save-state";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null, true, globalSave);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("message_type", Matchers.containsString("is-success"));
    }

    public int getAccOwnerId() {
        JsonPath users = function.getUsers(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        AccOwnerName = users.get("[0].first_name") + "_" + users.get("[0].last_name");
        return users.get("[0].id");
    }

}
