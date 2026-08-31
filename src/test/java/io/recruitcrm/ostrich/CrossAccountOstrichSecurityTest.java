package io.recruitcrm.ostrich;

import java.time.Instant;
import java.util.ArrayList;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.api.util.DateUtil;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.ostrich.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountOstrichSecurityTest extends TestBase {

    private commanFunction function = new commanFunction();
    private String taskID = "";
    private String meetingID = "";
    
    @Owner("Ajendra Singh")
    @Test(dataProvider = "crossAccountOstrichTestData", groups = "nightly-build")
    public void crossAccountOstrichOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        String apiKey = getAccountApiKey(accountType);

        switch (operation.toUpperCase()) {
            case "POST_TASK_LIST":
                Tasks tasks = new Tasks();
                tasks.setPageSize(25);
                tasks.setPage(1);
                tasks.setSortBy("updatedon");
                tasks.setSortOrder("desc");
                response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/tasks", token, null, true, tasks);
                break;

            case "POST_TASK_COUNT":
                Tasks taskCount = new Tasks();
                taskCount.setCurrentTime(Long.toString(Instant.now().getEpochSecond()));
                response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/tasks/count", token, null, true, taskCount);
                break;

            case "POST_TASK_UPDATE_COLLABORATOR":
                UpdateActivityInline updateTask = new UpdateActivityInline();
                updateTask.setActivity_id(Integer.parseInt(taskID));
                updateTask.setUser_id(getAdminId(accountType));
                updateTask.setActivity_type(1);
                updateTask.setEventType("add");
                response = RestClient.doPost("JSON", ostrichURL, "/associate-events/update-collaborator", token, null, true, updateTask);
                break;

            case "POST_TASK_BULK_UPDATE":
                ArrayList<Integer> taskIDs = new ArrayList<>();
                taskIDs.add(Integer.parseInt(taskID));
                BulkUpdate bulkUpdateTask = new BulkUpdate();
                bulkUpdateTask.setKey("title");
                bulkUpdateTask.setValue("Updated Task " + RandomStringUtils.randomAlphabetic(4));
                bulkUpdateTask.setType(1);
                bulkUpdateTask.setIds(taskIDs);
                response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/bulk-update", token, null, true, bulkUpdateTask);
                break;

            case "POST_TASK_BULK_DELETE":
                ArrayList<Integer> deleteTaskIDs = new ArrayList<>();
                deleteTaskIDs.add(Integer.parseInt(taskID));
                BulkDelete bulkDeleteTask = new BulkDelete();
                bulkDeleteTask.setIdsToDelete(deleteTaskIDs);
                bulkDeleteTask.setType(1);
                response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/bulk-delete", token, null, true, bulkDeleteTask);
                break;

            case "POST_MEETING_LIST":
                Meetings meetings = new Meetings();
                meetings.setPageSize(25);
                meetings.setPage(1);
                meetings.setSortBy("updatedon");
                meetings.setSortOrder("desc");
                response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/meetings", token, null, true, meetings);
                break;

            case "POST_MEETING_COUNT":
                Meetings meetingCount = new Meetings();
                meetingCount.setCurrentTime(Long.toString(Instant.now().getEpochSecond()));
                response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/meetings/count", token, null, true, meetingCount);
                break;

            case "POST_MEETING_BY_ID":
                MeetingById getMeetingById = new MeetingById(meetingID);
                response = RestClient.doPost("JSON", ostrichURL, "calendar/meeting", token, null, true, getMeetingById);
                break;

            case "POST_MEETING_UPDATE_COLLABORATOR":
                UpdateActivityInline updateMeeting = new UpdateActivityInline();
                updateMeeting.setActivity_id(Integer.parseInt(meetingID));
                updateMeeting.setUser_id(getAdminId(accountType));
                updateMeeting.setActivity_type(2);
                updateMeeting.setEventType("add");
                response = RestClient.doPost("JSON", ostrichURL, "/associate-events/update-collaborator", token, null, true, updateMeeting);
                break;

            case "POST_MEETING_BULK_UPDATE":
                ArrayList<Integer> meetingIDs = new ArrayList<>();
                meetingIDs.add(Integer.parseInt(meetingID));
                BulkUpdate bulkUpdateMeeting = new BulkUpdate();
                bulkUpdateMeeting.setKey("title");
                bulkUpdateMeeting.setValue("Updated Meeting " + RandomStringUtils.randomAlphabetic(4));
                bulkUpdateMeeting.setType(2);
                bulkUpdateMeeting.setIds(meetingIDs);
                response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/bulk-update", token, null, true, bulkUpdateMeeting);
                break;

            case "POST_MEETING_BULK_DELETE":
                ArrayList<Integer> deleteMeetingIDs = new ArrayList<>();
                deleteMeetingIDs.add(Integer.parseInt(meetingID));
                BulkDelete bulkDeleteMeeting = new BulkDelete();
                bulkDeleteMeeting.setIdsToDelete(deleteMeetingIDs);
                bulkDeleteMeeting.setType(2);
                response = RestClient.doPost("JSON", ostrichURL, "/tasks-and-meetings/bulk-delete", token, null, true, bulkDeleteMeeting);
                break;

            case "POST_CALENDAR_MEETINGS":
                GetMeetingInCalendar getMeetingInCalendar = new GetMeetingInCalendar();
                getMeetingInCalendar.setUser_ids("1");
                getMeetingInCalendar.setStartDate(DateUtil.todayStartTime().getTime() / 1000);
                getMeetingInCalendar.setEndDate(DateUtil.todayEndTime().getTime() / 1000);
                response = RestClient.doPost("JSON", ostrichURL, "calendar/meetings", token, null, true, getMeetingInCalendar);
                break;

            case "POST_GLOBAL_SAVE":
                GlobalSaveCommonProperties commonProperties = new GlobalSaveCommonProperties();
                commonProperties.setListPageOrder(1);
                GlobalSavecolumns columns = new GlobalSavecolumns();
                columns.setTitle(commonProperties);
                String columnStateJson;
                try {
                    columnStateJson = new ObjectMapper().writeValueAsString(columns);
                } catch (Exception e) {
                    columnStateJson = "{}";
                }
                GlobalSave globalSave = new GlobalSave();
                globalSave.setColumnstate(columnStateJson);
                globalSave.setDatatablekey("tasks");
                response = RestClient.doPost("JSON", albatrossURL, "/global/save-state", token, null, true, globalSave);
                break;

            case "POST_SEQUENCING_LATEST_TASK":
                TaskSequencing latestTaskSequencing = new TaskSequencing();
                latestTaskSequencing.setUserfilter(0);
                latestTaskSequencing.setSortOrder("desc");
                latestTaskSequencing.setSearchTerm("test");
                response = RestClient.doPost("JSON", ostrichURL, "sequencing/get-latest-task", token, null, true, latestTaskSequencing);
                break;

            case "POST_SEQUENCING_TASK_LIST":
                TaskSequencing taskListSequencing = new TaskSequencing();
                taskListSequencing.setSort_by("startdate");
                taskListSequencing.setSortOrder("asc");
                taskListSequencing.setPage(1);
                taskListSequencing.setPage_size(50);
                taskListSequencing.setSearchentity(5);
                taskListSequencing.setSearchTerm("test");
                response = RestClient.doPost("JSON", ostrichURL, "sequencing/get-task-list", token, null, true, taskListSequencing);
                break;

            case "POST_MEETING_UPDATE_ATTENDEE":
                ArrayList<Attendee> attendees = new ArrayList<>();
                JsonPath candidate = function.createNewCandidateWithMandatoryFields(baseURL, apiKey).jsonPath();
                String candidateSlug = candidate.get("slug");
                String email = candidate.get("email");
                String name = candidate.get("first_name") + " " + candidate.get("last_name");
                Attendee attendee = new Attendee();
                attendee.setAttendeeid(candidateSlug);
                attendee.setAttendeetype(5);
                attendee.setEmail(email);
                attendee.setName(name);
                attendee.setAppointmentid(Integer.parseInt(meetingID));
                attendees.add(attendee);
                UpdateAttendee updateAttendee = new UpdateAttendee();
                updateAttendee.setAttendees(attendees);
                updateAttendee.setMeeting_id(Integer.parseInt(meetingID));
                updateAttendee.setEvent_type("add");
                response = RestClient.doPost("JSON", albatrossURL, "meetings/update-attendee", token, null, true, updateAttendee);
                break;

            case "CREATE_TASK":
                // Use Account A token for creating task
                JsonPath jp = function.createNewTask(baseURL, apiKey, "candidate").jsonPath();
                taskID = String.valueOf(jp.getInt("id"));
                response = RestClient.doGet("JSON", baseURL, "tasks/" + taskID, apiKey, null, null, true);
                break;

            case "CREATE_MEETING":
                // Use Account A token for creating meeting
                JsonPath jpMeeting = function.createNewMeetings(baseURL, apiKey, "candidate").jsonPath();
                meetingID = String.valueOf(jpMeeting.getInt("id"));
                response = RestClient.doGet("JSON", baseURL, "meetings/" + meetingID, apiKey, null, null, true);
                break;

            default:
                Assert.fail("Unsupported operation: " + operation);
        }

        int expectedStatus = Integer.parseInt(expectedStatusCode);
        assert response != null;
        int actualStatus = response.getStatusCode();

        try {
            response.then().statusCode(expectedStatus);
        } catch (AssertionError e) {
            throw new AssertionError("Status code mismatch in " + testScenario +
                    ": Expected " + expectedStatus + " but got " + actualStatus +
                    ". Response: " + response.getBody().asString(), e);
        }

        switch (expectedResponse) {
            case "success":
                if (operation.contains("GET") || operation.contains("LIST") || operation.contains("COUNT")) {
                    response.then().body(Matchers.notNullValue());
                }
                break;
            case "unauthorized":
                response.then().body("error", Matchers.containsString("Unauthorized"));
                break;
            case "bad_request":
                response.then().body("data.message", Matchers.notNullValue());
                break;
            case "not_found":
            case "Activity not found":
                response.then().body("message", Matchers.containsString("not found"));
                break;
            case "No records found":
                if (response.getStatusCode() == 200) {
                    response.then().body("message", Matchers.equalTo("No records found"));
                    response.then().body("message_type", Matchers.equalTo("is-danger"));
                    response.then().body("status", Matchers.equalTo("fail"));
                }
                break;
            case "Access Denied":
                response.then().body("message", Matchers.equalTo("Access Denied"));
                response.then().body("message_type", Matchers.equalTo("is-danger"));
                response.then().body("status", Matchers.equalTo("access_denied"));
                break;
            default:
                response.then().body("message", Matchers.containsString(expectedResponse));
                break;
        }
    }

    @DataProvider(name = "crossAccountOstrichTestData")
    public static Object[][] crossAccountOstrichTestData() {
        return new Object[][]{
                // ===== SCENARIO 1: SETUP - CREATE TEST DATA =====
                // Account A creates test data (should succeed)
                {"SCENARIO_1_CREATE_TASK", "AccountA", "valid", "CREATE_TASK", "200", "success", "Account A should be able to create task"},
                {"SCENARIO_1_CREATE_MEETING", "AccountA", "valid", "CREATE_MEETING", "200", "success", "Account A should be able to create meeting"},

                // ===== SCENARIO 2: VALID CROSS-ACCOUNT TASK OPERATIONS =====
                // Account B performs task operations with valid token (should succeed but not access Account A's data)
                {"SCENARIO_2_TASK_LIST", "AccountB", "valid", "POST_TASK_LIST", "200", "success", "Account B should get task list with valid token"},
                {"SCENARIO_2_TASK_COUNT", "AccountB", "valid", "POST_TASK_COUNT", "200", "success", "Account B should get task count with valid token"},
                {"SCENARIO_2_TASK_UPDATE_COLLABORATOR", "AccountB", "valid", "POST_TASK_UPDATE_COLLABORATOR", "404", "not_found", "Account B should not access Account A's task collaborators"},
                {"SCENARIO_2_TASK_BULK_UPDATE", "AccountB", "valid", "POST_TASK_BULK_UPDATE", "404", "No records found", "Account B should not bulk update Account A's tasks"},
                {"SCENARIO_2_TASK_BULK_DELETE", "AccountB", "valid", "POST_TASK_BULK_DELETE", "200", "Access Denied", "Account B should not bulk delete Account A's tasks"},

                // ===== SCENARIO 3: VALID CROSS-ACCOUNT MEETING OPERATIONS =====
                // Account B performs meeting operations with valid token (should succeed but not access Account A's data)
                {"SCENARIO_3_MEETING_LIST", "AccountB", "valid", "POST_MEETING_LIST", "200", "success", "Account B should get meeting list with valid token"},
                {"SCENARIO_3_MEETING_COUNT", "AccountB", "valid", "POST_MEETING_COUNT", "200", "success", "Account B should get meeting count with valid token"},
                {"SCENARIO_3_MEETING_BY_ID", "AccountB", "valid", "POST_MEETING_BY_ID", "404", "Activity not found", "Account B should not access Account A's meeting by ID"},
                {"SCENARIO_3_MEETING_UPDATE_COLLABORATOR", "AccountB", "valid", "POST_MEETING_UPDATE_COLLABORATOR", "404", "not_found", "Account B should not access Account A's meeting collaborators"},
                {"SCENARIO_3_MEETING_BULK_UPDATE", "AccountB", "valid", "POST_MEETING_BULK_UPDATE", "404", "No records found", "Account B should not bulk update Account A's meetings"},
                {"SCENARIO_3_MEETING_BULK_DELETE", "AccountB", "valid", "POST_MEETING_BULK_DELETE", "200", "Access Denied", "Account B should not bulk delete Account A's meetings"},

                // ===== SCENARIO 4: INVALID TOKEN TASK OPERATIONS =====
                // Account B performs task operations with invalid token (should fail)
                {"SCENARIO_4_TASK_LIST_INVALID", "AccountB", "invalid", "POST_TASK_LIST", "401", "unauthorized", "Account B should be denied task list access with invalid token"},
                {"SCENARIO_4_TASK_COUNT_INVALID", "AccountB", "invalid", "POST_TASK_COUNT", "401", "unauthorized", "Account B should be denied task count access with invalid token"},
                {"SCENARIO_4_TASK_UPDATE_COLLABORATOR_INVALID", "AccountB", "invalid", "POST_TASK_UPDATE_COLLABORATOR", "401", "unauthorized", "Account B should be denied task collaborator update with invalid token"},
                {"SCENARIO_4_TASK_BULK_UPDATE_INVALID", "AccountB", "invalid", "POST_TASK_BULK_UPDATE", "401", "unauthorized", "Account B should be denied task bulk update with invalid token"},
                {"SCENARIO_4_TASK_BULK_DELETE_INVALID", "AccountB", "invalid", "POST_TASK_BULK_DELETE", "401", "unauthorized", "Account B should be denied task bulk delete with invalid token"},

                // ===== SCENARIO 5: INVALID TOKEN MEETING OPERATIONS =====
                // Account B performs meeting operations with invalid token (should fail)
                {"SCENARIO_5_MEETING_LIST_INVALID", "AccountB", "invalid", "POST_MEETING_LIST", "401", "unauthorized", "Account B should be denied meeting list access with invalid token"},
                {"SCENARIO_5_MEETING_COUNT_INVALID", "AccountB", "invalid", "POST_MEETING_COUNT", "401", "unauthorized", "Account B should be denied meeting count access with invalid token"},
                {"SCENARIO_5_MEETING_BY_ID_INVALID", "AccountB", "invalid", "POST_MEETING_BY_ID", "401", "unauthorized", "Account B should be denied meeting by ID access with invalid token"},
                {"SCENARIO_5_MEETING_UPDATE_COLLABORATOR_INVALID", "AccountB", "invalid", "POST_MEETING_UPDATE_COLLABORATOR", "401", "unauthorized", "Account B should be denied meeting collaborator update with invalid token"},
                {"SCENARIO_5_MEETING_BULK_UPDATE_INVALID", "AccountB", "invalid", "POST_MEETING_BULK_UPDATE", "401", "unauthorized", "Account B should be denied meeting bulk update with invalid token"},
                {"SCENARIO_5_MEETING_BULK_DELETE_INVALID", "AccountB", "invalid", "POST_MEETING_BULK_DELETE", "401", "unauthorized", "Account B should be denied meeting bulk delete with invalid token"},

                // ===== SCENARIO 6: CALENDAR AND GLOBAL OPERATIONS =====
                // Test calendar and global operations
                {"SCENARIO_6_CALENDAR_MEETINGS", "AccountB", "valid", "POST_CALENDAR_MEETINGS", "200", "success", "Account B should access calendar meetings with valid token"},
                {"SCENARIO_6_CALENDAR_MEETINGS_INVALID", "AccountB", "invalid", "POST_CALENDAR_MEETINGS", "401", "unauthorized", "Account B should be denied calendar access with invalid token"},
                {"SCENARIO_6_GLOBAL_SAVE", "AccountB", "valid", "POST_GLOBAL_SAVE", "200", "success", "Account B should access global save with valid token"},
                {"SCENARIO_6_GLOBAL_SAVE_INVALID", "AccountB", "invalid", "POST_GLOBAL_SAVE", "401", "unauthorized", "Account B should be denied global save with invalid token"},

                // ===== SCENARIO 6A: SEQUENCING OPERATIONS =====
                // Test sequencing operations
                {"SCENARIO_6A_SEQUENCING_LATEST_TASK", "AccountB", "valid", "POST_SEQUENCING_LATEST_TASK", "200", "success", "Account B should access latest task sequencing with valid token"},
                {"SCENARIO_6A_SEQUENCING_LATEST_TASK_INVALID", "AccountB", "invalid", "POST_SEQUENCING_LATEST_TASK", "401", "unauthorized", "Account B should be denied latest task sequencing with invalid token"},
                {"SCENARIO_6A_SEQUENCING_TASK_LIST", "AccountB", "valid", "POST_SEQUENCING_TASK_LIST", "200", "success", "Account B should access task list sequencing with valid token"},
                {"SCENARIO_6A_SEQUENCING_TASK_LIST_INVALID", "AccountB", "invalid", "POST_SEQUENCING_TASK_LIST", "401", "unauthorized", "Account B should be denied task list sequencing with invalid token"},

                // ===== SCENARIO 6B: MEETING ATTENDEE OPERATIONS =====
                {"SCENARIO_6B_MEETING_UPDATE_ATTENDEE", "AccountB", "valid", "POST_MEETING_UPDATE_ATTENDEE", "404", "not_found", "Account B should NOT be able to update attendees for Account A's meetings - SECURITY ISSUE if this passes"},
                {"SCENARIO_6B_MEETING_UPDATE_ATTENDEE_INVALID", "AccountB", "invalid", "POST_MEETING_UPDATE_ATTENDEE", "401", "unauthorized", "Account B should be denied attendee update with invalid token"},

                // ===== SCENARIO 7: TOKEN VARIATIONS =====
                {"SCENARIO_7_EXPIRED_TOKEN", "AccountB", "expired", "POST_TASK_LIST", "401", "unauthorized", "Expired token should return 401"},
                {"SCENARIO_7_MALFORMED_TOKEN", "AccountB", "malformed", "POST_TASK_LIST", "401", "unauthorized", "Malformed token should return 401"},

                // ===== SCENARIO 8: EDGE CASES =====
                {"SCENARIO_8_NONEXISTENT_ACCOUNT", "AccountC", "valid", "POST_TASK_LIST", "401", "unauthorized", "Non-existent account should return 401"},
                {"SCENARIO_8_EMPTY_TOKEN", "AccountB", "empty", "POST_TASK_LIST", "401", "unauthorized", "Empty token should return 401"},
                {"SCENARIO_8_NULL_TOKEN", "AccountB", "null", "POST_TASK_LIST", "401", "unauthorized", "Null token should return 401"}
        };
    }

    private int getAdminId(String accountType) {
        JsonPath users = function.getUsers(baseURL, getAccountApiKey(accountType)).jsonPath();
        return users.get("[1].id");
    }

}