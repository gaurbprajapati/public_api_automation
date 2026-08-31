package io.recruitcrm.ostrich;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.ostrich.Meetings;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class MeetingSortTest extends TestBase {
    commanFunction function = new commanFunction();
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @BeforeClass(alwaysRun = true)    public void setupMeetingsOnce() {
        // Create custom meeting types first
        allCrudFunctions.createCustomMeeting(albatrossURL, ThreadManager.getOwnerAlbatrossToken());
        
        // Get meeting types
        Response meetingTypesResponse = RestClient.doGet("JSON", baseURL, "meeting-types", 
                ThreadManager.getAccountApiKey(), null, null, true);
        meetingTypesResponse.then().statusCode(200);
        
        JsonPath meetingTypesJson = meetingTypesResponse.jsonPath();
        List<Integer> meetingTypeIds = meetingTypesJson.getList("id");
        
        // Fetch user list
        Response userResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
        userResponse.then().statusCode(200);

        JsonPath userJson = userResponse.jsonPath();
        int accountOwnerId = userJson.getInt("[0].id");
        int adminId = userJson.getInt("[1].id");
        int resTeamMemberId = userJson.getInt("[2].id");

        // Create 3 meetings: 2 with past dates, 1 with future date
        List<String> entityTypes = Arrays.asList("candidate", "contact", "company");
        List<Integer> userIds = Arrays.asList(accountOwnerId, adminId, resTeamMemberId);
        List<String> startTimes = Arrays.asList("past", "future", "past");
        
        // Create meetings with different types and start times for status testing
        for (int i = 0; i < entityTypes.size(); i++) {
            String entityType = entityTypes.get(i);
            int userId = userIds.get(i);
            int meetingTypeId = meetingTypeIds.get(i % meetingTypeIds.size());
            String startTimeType = startTimes.get(i);
            
            Response meetingResponse = function.createMeetingWithCreatedByUserIdTypeAndTime(baseURL, ThreadManager.getAccountApiKey(), entityType, userId, meetingTypeId, startTimeType);
            meetingResponse.then().statusCode(200); // Validate meeting creation was successful
        }
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "getTextFieldData", groups = "nightly-build")
    public void searchMeetingsSortedByTextField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Meetings meeting = new Meetings();
        meeting.setSortBy(sortField);
        meeting.setSortOrder(sortOrder);

        Response response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/meetings",
                ThreadManager.getOwnerAlbatrossToken(), null, true, meeting);
        response.then().statusCode(statusCode);

        List<String> values = response.jsonPath().getList(jsonPath);
        
        if (sortOrder.equals("asc")) {
            Assert.assertTrue(isSortedAscendingText(values), sortField + " not sorted ascending");
        } else {
            Assert.assertTrue(isSortedDescendingText(values), sortField + " not sorted descending");
        }
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "getIntegerFieldData", groups = "nightly-build")
    public void searchMeetingsSortedByIntegerField(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Meetings meeting = new Meetings();
        meeting.setSortBy(sortField);
        meeting.setSortOrder(sortOrder);

        Response response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/meetings",
                ThreadManager.getOwnerAlbatrossToken(), null, true, meeting);
        response.then().statusCode(statusCode);

        List<Integer> values = response.jsonPath().getList(jsonPath);

        if (sortOrder.equals("asc")) {
            Assert.assertTrue(isSortedAscending(values), sortField + " not sorted ascending");
        } else {
            Assert.assertTrue(isSortedDescending(values), sortField + " not sorted descending");
        }
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "getMeetingValidData")
    public void searchMeetingsSortedByStatus(String sortOrder, int statusCode) {
        Meetings meeting = new Meetings();
        meeting.setSortBy("status");
        meeting.setSortOrder(sortOrder);

        Response response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/meetings",
                ThreadManager.getOwnerAlbatrossToken(), null, true, meeting);
        response.then().statusCode(statusCode);

        // Check both status values and their underlying start dates
        List<Integer> statuses = response.jsonPath().getList("data.records.status");
        List<Integer> startDates = response.jsonPath().getList("data.records.startdate");
        
        if (sortOrder.equals("asc")) {
            Assert.assertTrue(isSortedAscending(statuses), "Status values not sorted ascending");
            Assert.assertTrue(isSortedAscending(startDates), "Start dates not sorted ascending");
        } else {
            Assert.assertTrue(isSortedDescending(statuses), "Status values not sorted descending");
            Assert.assertTrue(isSortedDescending(startDates), "Start dates not sorted descending");
        }
    }

    // ------------------- Sorting Helpers -------------------

    private boolean isSortedAscendingText(List<String> list) {
        if (list == null || list.size() <= 1) return false;
        
        List<String> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList, String.CASE_INSENSITIVE_ORDER);
        return list.equals(sortedList);
    }

    private boolean isSortedDescendingText(List<String> list) {
        if (list == null || list.size() <= 1) return false;
        
        List<String> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList, String.CASE_INSENSITIVE_ORDER.reversed());
        return list.equals(sortedList);
    }

    private boolean isSortedAscending(List<Integer> list) {
        if (list == null || list.size() <= 1) return false;
        
        List<Integer> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList);
        return list.equals(sortedList);
    }

    private boolean isSortedDescending(List<Integer> list) {
        if (list == null || list.size() <= 1) return false;
        
        List<Integer> sortedList = new ArrayList<>(list);
        Collections.sort(sortedList, Collections.reverseOrder());
        return list.equals(sortedList);
    }

    // ------------------- Data Providers -------------------

    @DataProvider
    public Object[][] getTextFieldData() {
        return new Object[][] {
                {"title", "data.records.title", "asc", 200},
                {"title", "data.records.title", "desc", 200},
                {"description", "data.records.description", "asc", 200},
                {"description", "data.records.description", "desc", 200},
                {"address", "data.records.address", "asc", 200},
                {"address", "data.records.address", "desc", 200}
//                {"relatedto", "data.records.relatedtoname", "asc", 200},
//                {"relatedto", "data.records.relatedtoname", "desc", 200},
//                {"createdby", "data.records.creatorname", "asc", 200},
//                {"createdby", "data.records.creatorname", "desc", 200},
//                {"updatedby", "data.records.creatorname", "asc", 200},
//                {"updatedby", "data.records.creatorname", "desc", 200},
//                {"ownername", "data.records.creatorname", "asc", 200},
//                {"ownername", "data.records.creatorname", "desc", 200}
        };
    }

    @DataProvider
    public Object[][] getIntegerFieldData() {
        return new Object[][] {
                {"startdate", "data.records.startdate", "asc", 200},
                {"startdate", "data.records.startdate", "desc", 200},
                {"createdon", "data.records.createdon", "asc", 200},
                {"createdon", "data.records.createdon", "desc", 200},
                {"updatedon", "data.records.updatedon", "asc", 200},
                {"updatedon", "data.records.updatedon", "desc", 200}
//                {"notetype", "data.records.notetype", "asc", 200},
//                {"notetype", "data.records.notetype", "desc", 200}
        };
    }

    @DataProvider
    public Object[][] getMeetingValidData() {
        return new Object[][] {
                {"asc", 200},
                {"desc", 200}
        };
    }
}
