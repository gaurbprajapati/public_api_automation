package io.recruitcrm.albatross.chromeExtension;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.*;
import io.rcrm.api.testbase.TestBase;
import org.testng.annotations.*;
import org.testng.Assert;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.rcrm.api.pojo.chromeExtension.*;
import io.rcrm.api.restclient.RestClient;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEntityActivityTest_ExtensionTest extends TestBase {

	commanFunction function;
	String albatrossAuthToken;
	String apiAuthToken;
	JavaFakerMeeting meeting;
	String meetingName, description, descriptionWithSize, address, reminderTime;
	long startDate, endDate;
	JavaFakerTask javaFakerTask;
	String taskName;

	@BeforeClass(alwaysRun = true)	public void Setup() {
		function = new commanFunction();
		meeting = new JavaFakerMeeting();
		javaFakerTask = new JavaFakerTask();
		meetingName = meeting.getMeetingName();
		description = meeting.getDescription();
		descriptionWithSize = meeting.getDescription();
		address = meeting.getAddress();
		startDate = meeting.getMeetingStartDate();
		endDate = meeting.getMeetingEndDate();
		reminderTime = meeting.getReminderData();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
		taskName = javaFakerTask.getTaskName();
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTestData", groups = "nightly-build")
	public void createMeeting_Extension(List<Integer> userIds, String entitySlug, String entityType) {
		Meeting meeting = new Meeting();
		Meeting.Appointment appointment = new Meeting.Appointment(meetingName, entitySlug, startDate, reminderTime,
				endDate, userIds.get(0), descriptionWithSize, entityType, userIds.get(0));

		meeting.setAppointment(appointment);
		meeting.setTask(false);
		meeting.setCollaborator_user_ids(userIds);
		List<Integer> teamIds = new ArrayList<>();
		meeting.setCollaborator_team_ids(teamIds);
		meeting.setCollaborator(teamIds);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/meetings",
				albatrossAuthToken, null, true, meeting);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.get("message"), "Meeting Added");
		List<Integer> collaboratorIds = jsonPath.getList("data.appointment.collaborator_user_ids");
		for (int i = 0; i < collaboratorIds.size(); i++) {
			Assert.assertTrue(collaboratorIds.contains(userIds.get(i)));
		}
		Assert.assertEquals(jsonPath.get("data.appointment.title"), meetingName);
		Assert.assertEquals(jsonPath.get("data.appointment.relatedto"), entitySlug);
		Assert.assertEquals(jsonPath.get("data.appointment.description"), descriptionWithSize);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createMeetingWithEmptyRequestBody_Extension() {
		Meeting meeting = new Meeting();
		Meeting.Appointment appointment = new Meeting.Appointment();
		meeting.setAppointment(appointment);
		meeting.setTask(false);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/meetings",
				albatrossAuthToken, null, true, meeting);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.get("message"), "user id is not valid");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotCreateMeeting_Extension() {
		Meeting meeting = new Meeting();
		Meeting.Appointment appointment = new Meeting.Appointment();
		meeting.setAppointment(appointment);
		meeting.setTask(false);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/meetings",
				albatrossAuthToken + "abc", null, true, meeting);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getTestData", groups = "nightly-build")
	public void createTask_Extension(List<Integer> userIds, String entitySlug, String entityType) {
		TaskPojo taskPojo = new TaskPojo();
		TaskPojo.Task task = new TaskPojo.Task(taskName, userIds.get(0), entitySlug, reminderTime, startDate,
				userIds.get(0), descriptionWithSize, entityType);

		taskPojo.setTask(task);
		taskPojo.setCollaborator_user_ids(userIds);
		List<Integer> teamIds = new ArrayList<>();
		taskPojo.setCollaborator_team_ids(teamIds);
		taskPojo.setCollaborator(teamIds);
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/tasks",
				albatrossAuthToken, null, true, taskPojo);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.get("message"), "Task Added");
		List<Integer> collaboratorIds = jsonPath.getList("data.task.collaborator_user_ids");
		for (int i = 0; i < collaboratorIds.size(); i++) {
			Assert.assertTrue(collaboratorIds.contains(userIds.get(i)));
		}
		Assert.assertEquals(jsonPath.get("data.task.title"), taskName);
		Assert.assertEquals(jsonPath.get("data.task.relatedto"), entitySlug);
		Assert.assertEquals(jsonPath.get("data.task.description"), descriptionWithSize);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void createTaskWithEmptyRequestBody_Extension() {
		TaskPojo taskPojo = new TaskPojo();
		TaskPojo.Task task = new TaskPojo.Task();
		taskPojo.setTask(task);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/tasks",
				albatrossAuthToken, null, true, taskPojo);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
		Assert.assertEquals(jsonPath.get("message"), "The Title field is required.,Reminder field is required,The collaborator user ids must be an array.,The collaborator team ids must be an array.");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unautorizedUserCannotCreateTask_Extension() {
		TaskPojo taskPojo = new TaskPojo();
		TaskPojo.Task task = new TaskPojo.Task();
		taskPojo.setTask(task);

		Response response = RestClient.doPostExtension("JSON", albatrossURL, "extensions/chrome/tasks",
				albatrossAuthToken + "abc", null, true, taskPojo);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getAllUsersTeams_Extensions() {
		Response response = RestClient.doGetExtension("JSON", albatrossURL, "/extensions/chrome/users-with-teams",
				albatrossAuthToken, null, true);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		List<Map<String, Object>> users = jsonPath.getList("data.users");
		String[] userName = { "Owner", "Admin", "Recruiter", "TeamMember" };
		for (int i = 0; i < users.size(); i++) {
			Assert.assertTrue(users.get(i).get("name").toString().contains(userName[i]));
		}
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllUsersTeams_Extensions() {
		Response response = RestClient.doGetExtension("JSON", albatrossURL, "/extensions/chrome/users-with-teams",
				albatrossAuthToken + "abc", null, true);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void verifySeachEntity_Extensions() {
		JsonPath companyJsonPath = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String companyName = companyJsonPath.getString("company_name");
		SearchEntity searchEntity = new SearchEntity(companyName, false, "test", companyName, false, false, "3.1.51");
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "/extensions/chrome/search-entity",
				albatrossAuthToken, null, true, searchEntity);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "success");
		List<Map<String, Object>> searchData = jsonPath.getList("data");
		Assert.assertEquals(searchData.get(0).get("title"), companyName);
		Assert.assertEquals(searchData.get(0).get("slug"), companyJsonPath.getString("slug"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void verifySeachEntityWithEmptyRequestBody_Extensions() {
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "/extensions/chrome/search-entity",
				albatrossAuthToken, null, true, null);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getList("data").size(), 0);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotSearchEntity_Extensions() {
		JsonPath companyJsonPath = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String companyName = companyJsonPath.getString("company_name");
		SearchEntity searchEntity = new SearchEntity(companyName, false, "test", companyName, false, false, "3.1.51");
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "/extensions/chrome/search-entity",
				albatrossAuthToken + "abc", null, true, searchEntity);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getAuthUsetDetails_Extensions() {
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "/extensions/chrome/getAuthUser",
				albatrossAuthToken, null, true, null);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("message_type"), "is-success");
		Assert.assertEquals((String) jsonPath.get("user.role"), "Account Owner");
		Assert.assertEquals((Integer) jsonPath.get("user.roleid"), Integer.valueOf(4));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAuthUserDetails_Extensions() {
		Response response = RestClient.doPostExtension("JSON", albatrossURL, "/extensions/chrome/getAuthUser",
				albatrossAuthToken + "abc", null, true, null);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.get("status"), "fail");
		Assert.assertEquals(jsonPath.get("message"), "Unauthorized access");
	}

	@DataProvider(parallel = true)
	public Object[][] getTestData() {
		JsonPath companyJsonPath = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String companySlug = companyJsonPath.getString("slug");
		JsonPath contactJsonPath = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
		String contactSlug = contactJsonPath.getString("slug");

		Response userResponse = function.getUsers(baseURL, apiAuthToken);
		Assert.assertEquals(userResponse.getStatusCode(), 200);
		JsonPath user = userResponse.jsonPath();
		List<Integer> userIds = user.getList("id");
		String teamMemberDeactivatedID = String.valueOf(user.getInt("[3].id"));
		String teamMemberFirstName = user.getString("[3].first_name");
		String teamMemberLastNameName = user.getString("[3].last_name");
		function.deactivateUser(teamMemberDeactivatedID, teamMemberFirstName, teamMemberLastNameName, albatrossURL,
				albatrossAuthToken);

		return new Object[][] { { userIds, companySlug, "3" }, { userIds, contactSlug, "2" } };
	}

}
