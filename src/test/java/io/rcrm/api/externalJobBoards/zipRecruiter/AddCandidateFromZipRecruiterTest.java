package io.rcrm.api.externalJobBoards.zipRecruiter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.json.JSONObject;
import org.junit.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerZipRecruiter;
import io.rcrm.api.pojo.externalJobBoards.ZipRecruiterJobBoard;
import io.rcrm.api.pojo.externalJobBoards.ZipRecruiterJobRecord;
import io.rcrm.api.pojo.externalJobBoards.ZipRecruiterProfile;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AddCandidateFromZipRecruiterTest extends TestBase {

	JavaFakerZipRecruiter faker = new JavaFakerZipRecruiter();

	int jobID;
	String responseId = faker.getResponseId();
	String firstName = faker.getFirstName();
	String lastName = faker.getLastName();
	String name = faker.getName();
	String email = faker.getEmail();
	String phone = faker.getPhoneNumber();
	String executiveSummary = faker.getExecutiveSummary();
	String mobile = faker.getPhoneNumber();
	String position = faker.getPosition();
	String description = faker.getDescription();
	String employer = faker.getEmployer();
	String textResume = faker.getTextResume();

	commanFunction function = new commanFunction();

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void addCandidateFromZipRecruiterWithValidJobId() {
		String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getJobResponse = albatrossFunctions.getJobResponse(albatrossURL,
				ThreadManager.getOwnerAlbatrossToken(), jobSlug);
		JsonPath jpJob = getJobResponse.jsonPath();
		jobID = jpJob.get("data.job.id");

		ZipRecruiterJobRecord zipRecruicterRecord = new ZipRecruiterJobRecord();
		zipRecruicterRecord.setPosition(position);
		zipRecruicterRecord.setDescription(description);
		zipRecruicterRecord.setEmployer(employer);
		ZipRecruiterJobRecord zipRecruiterRecordArray[] = new ZipRecruiterJobRecord[1];
		zipRecruiterRecordArray[0] = zipRecruicterRecord;

		ZipRecruiterProfile zipRecruicterProfile = new ZipRecruiterProfile();
		zipRecruicterProfile.setExecutive_summary(executiveSummary);
		zipRecruicterProfile.setMobile(mobile);
		zipRecruicterProfile.setJob_records(zipRecruiterRecordArray);
		zipRecruicterProfile.setText_resume(textResume);

		ZipRecruiterJobBoard zipRecruiterJobBoard = new ZipRecruiterJobBoard();
		zipRecruiterJobBoard.setResponse_id(responseId);
		zipRecruiterJobBoard.setJob_id(jobID);
		zipRecruiterJobBoard.setFirst_name(firstName);
		zipRecruiterJobBoard.setLast_name(lastName);
		zipRecruiterJobBoard.setName(name);
		zipRecruiterJobBoard.setEmail(email);
		zipRecruiterJobBoard.setPhone(phone);
		zipRecruiterJobBoard.setProfile(zipRecruicterProfile);
		zipRecruiterJobBoard.setGreat_match(true);

		String baseURL = "https://" + System.getProperty("envname") + ".recruitcrm.net/";
		String basePath = "/actions/jobboard/ziprecruiter.php";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				true, zipRecruiterJobBoard);

		response.then().statusCode(200);
		String json = response.asString().replaceAll("<[^>]+>", "");

		// JsonPath jsonPath = json.json();
		JSONObject jsonObject = new JSONObject(json);
		Assert.assertTrue(jsonObject.getString("message").equals("Add Candidate From Ziprecruiter Successful "));
		Assert.assertTrue(jsonObject.getString("message_type").equals("is-success"));
		Assert.assertTrue(jsonObject.getString("status").equals("success"));
		Assert.assertTrue(jsonObject.getString("action_name").equals("Add Candidate From ZipRecruiter"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void addCandidateFromZipRecruiterWithInValidJobId() {
		int inValdidJobID = 123;

		ZipRecruiterJobRecord zipRecruicterRecord = new ZipRecruiterJobRecord();
		zipRecruicterRecord.setPosition(position);
		zipRecruicterRecord.setDescription(description);
		zipRecruicterRecord.setEmployer(employer);
		ZipRecruiterJobRecord zipRecruiterRecordArray[] = new ZipRecruiterJobRecord[1];
		zipRecruiterRecordArray[0] = zipRecruicterRecord;

		ZipRecruiterProfile zipRecruicterProfile = new ZipRecruiterProfile();
		zipRecruicterProfile.setExecutive_summary(executiveSummary);
		zipRecruicterProfile.setMobile(mobile);
		zipRecruicterProfile.setJob_records(zipRecruiterRecordArray);
		zipRecruicterProfile.setText_resume(textResume);

		ZipRecruiterJobBoard zipRecruiterJobBoard = new ZipRecruiterJobBoard();
		zipRecruiterJobBoard.setResponse_id(responseId);
		zipRecruiterJobBoard.setJob_id(inValdidJobID);
		zipRecruiterJobBoard.setFirst_name(firstName);
		zipRecruiterJobBoard.setLast_name(lastName);
		zipRecruiterJobBoard.setName(name);
		zipRecruiterJobBoard.setEmail(email);
		zipRecruiterJobBoard.setPhone(phone);
		zipRecruiterJobBoard.setProfile(zipRecruicterProfile);
		zipRecruiterJobBoard.setGreat_match(true);

		String baseURL = "https://" + System.getProperty("envname") + ".recruitcrm.net/";
		String basePath = "/actions/jobboard/ziprecruiter.php";

		Response response = RestClient.doPost("JSON", baseURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				true, zipRecruiterJobBoard);

		response.then().statusCode(200);

		String json = response.asString().replaceAll("<[^>]+>", "");
		JSONObject jsonObject = new JSONObject(json);
		Assert.assertTrue(
				jsonObject.getString("message").equals("Failed To Add Candidate From Ziprecruiter : Data not saved"));
		Assert.assertTrue(jsonObject.getString("message_type").equals("is-danger"));
		Assert.assertTrue(jsonObject.getString("status").equals("fail"));
		Assert.assertTrue(jsonObject.getString("action_name").equals("Add Candidate From ZipRecruiter"));
	}

}
