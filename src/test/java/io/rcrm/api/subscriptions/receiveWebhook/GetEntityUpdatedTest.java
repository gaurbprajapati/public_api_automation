package io.rcrm.api.subscriptions.receiveWebhook;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.WebhookHelper;
import com.qa.api.util.RetryOn500OrSkippedAnalyzer;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCallLog;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.javafaker.JavaFakerMeeting;
import io.rcrm.api.javafaker.JavaFakerNote;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.pojo.Call_Log;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.pojo.HiringStage;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.pojo.Note;
import io.rcrm.api.pojo.Subscription;
import io.rcrm.api.pojo.Task;
import io.rcrm.api.pojo.albatross.AssignedCandInJob;
import io.rcrm.api.pojo.albatross.UpdateHiringStage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetEntityUpdatedTest extends TestBase {

	commanFunction function = new commanFunction();
	WebhookHelper webhookHelper;
	Map<String, String> pathParameters = new HashMap<String, String>();
	String responseFromWebhook;
	JsonPath jp;
	List<String> slugs = new ArrayList<String>();
	ArrayList<Integer> jobids = new ArrayList<>();
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerContact contactFaker = new JavaFakerContact();
	JavaFakerCompany faker = new JavaFakerCompany();
	JavaFakerDeal dealFaker = new JavaFakerDeal();
	JavaFakerJob jobFaker = new JavaFakerJob();
	JavaFakerNote fakeNote = new JavaFakerNote();
	JavaFakerTask fakerTask = new JavaFakerTask();
	JavaFakerMeeting fakerMeeting = new JavaFakerMeeting();
	JavaFakerCallLog fakerakerCallLog = new JavaFakerCallLog();
	ArrayList<String> candidatesSlug = new ArrayList<>();
	ArrayList<String> jobsSlug = new ArrayList<>();
	String entitySlug = "";

	@BeforeClass
	public void setUp() throws IOException {
		webhookHelper = new WebhookHelper();
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "updationEvents", retryAnalyzer = RetryOn500OrSkippedAnalyzer.class)
	public void getEntityUpdated(String event) {
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
		// Create new subscription
		Subscription subscription = new Subscription(event, webhookHelper.getTargetURL());
		RestClient.doPost("JSON", baseURL, "subscriptions", ThreadManager.getAccountApiKey(), null, false, subscription);

		// Trigger subscription and extract data
		Response response = null;
		switch (event) {
		case "candidate.updated": {
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");

			String CandidateFirstName = fakerCandidate.getFirstName() + " Edited";
			String CandidateLastName = fakerCandidate.getLastName();
			String CandidateEmail = "rcrmtest0@gmail.com";
			String CandidateNumber = fakerCandidate.getContactNumber();
			String dob = fakerCandidate.getDOB();
			String city = fakerCandidate.getCity();
			String locality = fakerCandidate.getLocality();
			String Address = fakerCandidate.getCandidateAddress();

			Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber,
					1, dob, 1, city, locality, Address);

			pathParameters = new HashMap<String, String>();
			pathParameters.put("candidate", candidateSlug);

			response = RestClient.doPost1("JSON", baseURL, "candidates/{candidate}", ThreadManager.getAccountApiKey(), null, pathParameters,
					false, candidate);
			break;
		}
		case "candidate.hiringstage.updated": {
			// For single candidate with single job
			response = function.assignCandidateToJob(baseURL, ThreadManager.getAccountApiKey());
			String candidateSlug = response.jsonPath().get("candidate_slug");
			String jobSlug = response.jsonPath().get("job_slug");
			slugs.add(candidateSlug);
			slugs.add(jobSlug);

			JsonPath jp = function.getAllHiringStages(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			int getStatusIdString = jp.get("status_id[0]");

			HiringStage hiringStage = new HiringStage();
			String taskTitle = fakerTask.getTaskName();
			String startDate = fakerTask.getFutureDate();
			hiringStage.setRemark(taskTitle);
			hiringStage.setStage_date(startDate);
			hiringStage.setStatus_id(getStatusIdString);

			pathParameters = new HashMap<String, String>();
			pathParameters.put("candidate", candidateSlug);
			pathParameters.put("job", jobSlug);

			response = RestClient.doPost1("JSON", baseURL, "candidates/{candidate}/hiring-stages/{job}",
					ThreadManager.getAccountApiKey(), null, pathParameters, true, hiringStage);

			// Bug automation for multiple candidates with multiple jobs
			// Commenting as webhook does not trigger from albatross on Test env.
			/*
			JsonPath jsonPath = (function.assignMultipleCandsToMultipleJobs(baseURL, albatrossURL,
					ThreadManager.getAccountApiKey(), ThreadManager.getOwnerAlbatrossToken())).jsonPath();
			ArrayList<Integer> id = new ArrayList<>();
			int candidatestatusid = getStatusIdString;
			String remark = jobFaker.getJob_status() + "Job Remarks";
			int jobid = jsonPath.get("data.data[0].jobid");
			ArrayList<Integer> candidateid = new ArrayList<>();
			boolean isMarkUnavailable = false;
			boolean updateUserObj = false;

			for (int i = 0; i < 9; i++) {
				//Required all assignment ids
				id.add(jsonPath.get("data.data[" + i + "].id"));
				if (i % 4 == 0) {
					//Required Unique Job Ids and related Slugs
					jobids.add(jsonPath.get("data.data[" + i + "].jobid"));
					candidatesSlug.add(jsonPath.get("data.data[" + i + "].candidateslug"));
					jobsSlug.add(jsonPath.get("data.data[" + i + "].jobslug"));
				}
				//Required all candidates ids (duplicates as well, one id is repeated thrice, thus 3ids = 3*3 = 9)
				candidateid.add(jsonPath.get("data.data[" + i + "].candidateid"));
			}
			
			UpdateHiringStage updateHiringStage = new UpdateHiringStage(id, candidatestatusid, remark, 0, jobid, jobids,
					candidateid, isMarkUnavailable, updateUserObj);
			
			response = RestClient.doPost("JSON", albatrossURL, "candidates/update-hiring-stage",
					ThreadManager.getOwnerAlbatrossToken(), null, true, updateHiringStage);
			Assert.assertEquals("Update Hiring Stage failed", response.getStatusCode(), 200);
			*/

			break;
		}
		case "contact.updated": {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");
			String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath()
					.get("slug");
			slugs.add(companySlug);

			String ContactFirstName = contactFaker.getFirstName() + " Edited";
			String ContactLastName = contactFaker.getLastName();
			String ContactEmail = "rcrmtest0@gmail.com";
			String contactNumbers = contactFaker.getContactNumber();

			Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumbers, companySlug);

			pathParameters = new HashMap<String, String>();
			pathParameters.put("contact", contactSlug);

			response = RestClient.doPost1("JSON", baseURL, "contacts/{contact}", ThreadManager.getAccountApiKey(), null, pathParameters,
					false, contact);

			break;
		}
		case "company.updated": {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");

			String companyName = faker.getCompanyName() + " Edited";
			String companyWebsite = faker.getUrl();
			String contactNumber = "13456789087654";

			Company company = new Company(companyName, companyWebsite, contactNumber, "");

			pathParameters = new HashMap<String, String>();
			pathParameters.put("company", companySlug);

			response = RestClient.doPost1("JSON", baseURL, "companies/{company}", ThreadManager.getAccountApiKey(), null, pathParameters,
					false, company);
			break;
		}
		case "job.updated": {
			String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug");
			String contactSlug = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug).jsonPath()
					.get("slug");
			String jobSlug = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath()
					.get("slug");
			slugs.add(companySlug);
			slugs.add(contactSlug);

			Job job = new Job();
			String JobName = jobFaker.getJobName() + " Edited";
			job.setName(JobName);
			job.setCompany_slug(companySlug);
			job.setContact_slug(contactSlug);
			job.setNumber_of_openings(3);
			job.setJob_description_text("Sample JD");
			job.setEnable_job_application_form(1);

			pathParameters = new HashMap<String, String>();
			pathParameters.put("job", jobSlug);

			response = RestClient.doPost1("JSON", baseURL, "jobs/{job}", ThreadManager.getAccountApiKey(), null, pathParameters, false,
					job);
			break;
		}
		case "deal.updated": {
			String dealSlug = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath().get("slug");

			String dealStage = dealFaker.getNumber();
			String dealName = dealFaker.getDealName() + " Edited";
			int dealValue = dealFaker.getDealValue();
			String dealType = dealFaker.getNumber();
			String dealDate = dealFaker.getDealDate();

			Deal deal = new Deal();
			deal.setName(dealName);
			deal.setDeal_value(dealValue);
			deal.setClose_date(dealDate);
			deal.setDeal_stage(dealStage);
			deal.setDeal_type(dealType);

			pathParameters = new HashMap<String, String>();
			pathParameters.put("deal", dealSlug);

			response = RestClient.doPost1("JSON", baseURL, "deals/{deal}", ThreadManager.getAccountApiKey(), null, pathParameters, true,
					deal);

			break;
		}
		case "meeting.updated": {
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug").toString();
			slugs.add(candidateSlug);

			String meetingName = fakerMeeting.getMeetingName();
			String meetingDescription = fakerMeeting.getDescription();
			String startDate = fakerMeeting.getFutureDate();
			String address = fakerMeeting.getAddress();
			String endDate = fakerMeeting.getEndDateWithReferenceDate(startDate);
			String pastDate = fakerMeeting.getPastDate();

			Meeting meeting = new Meeting();
			meeting.setTitle(meetingName);
			meeting.setDescription(meetingDescription);
			meeting.setAddress(address);
			meeting.setReminder(15);
			meeting.setRelated_to(candidateSlug);
			meeting.setRelated_to_type("candidate");
			meeting.setStart_date(pastDate);
			meeting.setEnd_date(endDate);

			response = RestClient.doPost("JSON", baseURL, "meetings", ThreadManager.getAccountApiKey(), null, true, meeting);
			String meetingID = response.jsonPath().get("id").toString();

			meeting = new Meeting();
			meeting.setTitle(meetingName + " Edited");
			meeting.setDescription(meetingDescription);
			meeting.setAddress(address);
			meeting.setReminder(15);
			meeting.setStart_date(pastDate);
			meeting.setEnd_date(startDate);

			pathParameters = new HashMap<String, String>();
			pathParameters.put("meeting", meetingID);

			response = RestClient.doPost1("JSON", baseURL, "meetings/{meeting}", ThreadManager.getAccountApiKey(), null, pathParameters,
					true, meeting);
			break;
		}
		case "task.updated": {
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug").toString();
			slugs.add(candidateSlug);

			JavaFakerTask fakerTask = new JavaFakerTask();
			String taskTitle = fakerTask.getTaskName();
			String taskDescription = fakerTask.getDescription();
			String startDate = fakerTask.getFutureDate();

			Task task = new Task();
			task.setTitle(taskTitle);
			task.setDescription(taskDescription);
			task.setReminder(30);
			task.setRelated_to(candidateSlug);
			task.setRelated_to_type("candidate");
			task.setStart_date(startDate);

			response = RestClient.doPost("JSON", baseURL, "tasks", ThreadManager.getAccountApiKey(), null, true, task);
			String taskID = response.jsonPath().get("id").toString();

			task = new Task();
			task.setTitle(taskTitle + " Edited");
			task.setDescription(taskDescription);
			task.setReminder(15);
			task.setStart_date(startDate);

			pathParameters = new HashMap<String, String>();
			pathParameters.put("task", taskID);

			response = RestClient.doPost1("JSON", baseURL, "tasks/{task}", ThreadManager.getAccountApiKey(), null, pathParameters, true,
					task);
			break;
		}
		case "note.updated": {
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug").toString();
			slugs.add(candidateSlug);

			JavaFakerNote fakeNote = new JavaFakerNote();
			String notesText = fakeNote.getNotes();

			Note note = new Note();
			note.setRelated_to(candidateSlug);
			note.setRelated_to_type("candidate");
			note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()

			response = RestClient.doPost("JSON", baseURL, "notes", ThreadManager.getAccountApiKey(), null, true, note);
			String noteID = response.jsonPath().get("id").toString();

			note = new Note();
			note.setRelated_to(candidateSlug);
			note.setRelated_to_type("candidate");
			note.setDescription(notesText + "<br>Edited<br>" + notesText);

			pathParameters = new HashMap<String, String>();
			pathParameters.put("note", noteID);

			response = RestClient.doPost1("JSON", baseURL, "notes/{note}", ThreadManager.getAccountApiKey(), null, pathParameters, true,
					note);
			break;
		}
		case "calllog.updated": {
			String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath()
					.get("slug").toString();
			slugs.add(candidateSlug);

			String contactNo = fakerakerCallLog.getContactNumber();
			String callNotes = fakerakerCallLog.getCall_notes();
			String pastDate = fakerTask.getPastDate();

			Call_Log callLog = new Call_Log();
			callLog.setCall_notes(callNotes);
			callLog.setCall_type("CALL_INCOMING");
			callLog.setContact_number(contactNo);
			callLog.setRelated_to(candidateSlug);
			callLog.setRelated_to_type("candidate");
			callLog.setCall_started_on(pastDate);

			response = RestClient.doPost("JSON", baseURL, "call-logs", ThreadManager.getAccountApiKey(), null, true, callLog);
			String calllogID = response.jsonPath().get("id").toString();

			callLog = new Call_Log();
			callLog.setCall_notes(callNotes);
			callLog.setCall_type("CALL_OUTGOING");
			callLog.setContact_number(contactNo);
			callLog.setCall_started_on(pastDate);
			callLog.setRelated_to(candidateSlug);
			callLog.setRelated_to_type("candidate");

			pathParameters = new HashMap<String, String>();
			pathParameters.put("call_log", calllogID);

			response = RestClient.doPost1("JSON", baseURL, "call-logs/{call_log}", ThreadManager.getAccountApiKey(), null, pathParameters,
					true, callLog);
			break;
		}
		}

		jp = response.jsonPath();

		if (!jobids.isEmpty() && event.equals("candidate.hiringstage.updated")){
			for (int i = 0; i < 3; i++) {
				responseFromWebhook = webhookHelper.getAllData(jobsSlug.get(i), i);// getting candidates for one job
				for (int j = 0; j < 3; j++) {
					// verifying all 3 candidates for one job
					assertThat("Web Hook Response", responseFromWebhook, containsString(candidatesSlug.get(j)));
				}
			}
		} else {
			if (jp.get("slug") != null) {
				entitySlug = jp.get("slug");
			} else if (jp.get("id") != null) {
				entitySlug = jp.get("id").toString();
			} else if (jp.get("candidate_slug") != null) {
				entitySlug = jp.get("candidate_slug");
			}
			slugs.add(entitySlug);

		// Fetch data from webhook site
		try {
			responseFromWebhook = webhookHelper.getData(entitySlug);
		} catch (Exception e) {
			Assert.fail("Failed to fetch Webhook data for Event " + event + ", " + e.getMessage());
		}
		/**
		 * Verify that 1) If webhook is sent in the first place 2) Correct contents are
		 * sent
		 */

		assertThat(responseFromWebhook, containsString(entitySlug));
		}
		function.deleteAllSubscriptions(baseURL, ThreadManager.getAccountApiKey());
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		webhookHelper.clear();
	}

	@DataProvider(name = "updationEvents")
	public Object[][] dpMethod() {
		return new Object[][] { { "candidate.updated" }, { "candidate.hiringstage.updated" }, { "contact.updated" },
				{ "company.updated" }, { "job.updated" }, { "deal.updated" }, { "meeting.updated" }, { "task.updated" },
				{ "note.updated" }, { "calllog.updated" } };
	}

}
