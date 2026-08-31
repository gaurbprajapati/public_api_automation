package io.rcrm.api.commanfunctions;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

import io.rcrm.api.pojo.albatross.*;
import io.rcrm.api.pojo.albatross.jobs.JobUpdateData;
import io.rcrm.api.pojo.albatross.jobs.UpdateJobRequest;
import io.rcrm.api.pojo.invoiceService.CreatePlacement;
import io.rcrm.api.pojo.invoiceService.Invoice;
import io.rcrm.api.pojo.invoiceService.InvoiceTemplate;
import io.rcrm.api.pojo.nyma.EmailsPage;
import io.rcrm.api.pojo.nyma.ReceiverEmailsPage;
import io.rcrm.api.pojo.nyma.SendEmailsPage;
import io.restassured.RestAssured;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.json.JSONArray;
import org.testng.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCallLog;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.javafaker.JavaFakerInvoice;
import io.rcrm.api.javafaker.JavaFakerJob;
import io.rcrm.api.javafaker.JavaFakerMeeting;
import io.rcrm.api.javafaker.JavaFakerNote;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.javafaker.JavaFakerMails;
import io.rcrm.api.javafaker.JavaFakerPlacement;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.AssignMultipleCandToMultipleJobs;
import io.rcrm.api.pojo.Call_Log;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.pojo.DeactivateUser;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.pojo.DealSplit;
import io.rcrm.api.pojo.Hotlist;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.pojo.Note;
import io.rcrm.api.pojo.Roleid;
import io.rcrm.api.pojo.Task;
import io.rcrm.api.pojo.HiringStage;

import io.rcrm.api.pojo.DealSplit.TeammatesCollaborator;
import io.rcrm.api.pojo.DealSplit.TeamsCollaborator;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.albatross.account.SignUpJson;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class commanFunction {

	// Parameters for API retry logic
	int maxRetries = 7;
	int timeout = 7500;

	public commanFunction() {
		// TODO Auto-generated constructor stub
		super();
	}

	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	JavaFakerJob jobFaker = new JavaFakerJob();
	JavaFakerDeal dealFaker = new JavaFakerDeal();

	JavaFakerCompany faker = new JavaFakerCompany();
	JavaFakerContact contactFaker = new JavaFakerContact();

	JavaFakerNote fakeNote = new JavaFakerNote();
	JavaFakerMails fakerMails = new JavaFakerMails();
	// Personal Information
	String CandidateFirstName = fakerCandidate.getFirstName();
	String CandidateLastName = fakerCandidate.getLastName();
	String CandidateEmail = "rcrmtest0@gmail.com";
	String CandidateNumber = fakerCandidate.getContactNumber();

	int qualificatioId = fakerCandidate.getQualification_id();
	String specialization = fakerCandidate.getSpecialization();

	String title = fakerCandidate.getPosition();
	int work_ex_year = fakerCandidate.getWork_ex_year();
	int RelevantExp = fakerCandidate.getRelevant_experience();
	String salarytype = fakerCandidate.getSalary_type();
	int currencyId = fakerCandidate.getCurrency_id();
	String currentStatus = fakerCandidate.getCurrentEmploymentStatus();
	int noticePeriod = fakerCandidate.getNotice_period();
	String availableForm = fakerCandidate.getAvailable_From();

	String skills = fakerCandidate.getSkills();

	// Social Links
	String fbLink = fakerCandidate.getUrl();
	String twitterLink = fakerCandidate.getUrl();
	String githubLink = fakerCandidate.getUrl();
	String linkedinLink = fakerCandidate.getUrl();
	String xingLink = fakerCandidate.getUrl();

	String dob = fakerCandidate.getDOB();
	int current_salary = fakerCandidate.getCurrent_salary();
	int salary_expectation = fakerCandidate.getSalary_expectation();
	String current_organization = fakerCandidate.getCurrentOrganization();

	String city = fakerCandidate.getCity();
	String locality = fakerCandidate.getLocality();
	String Address = fakerCandidate.getCandidateAddress();
	String candidateAvatarURL = fakerCandidate.getCandidateAvatarUrl();
	String resume = "https://files-for-testing.s3-ap-southeast-1.amazonaws.com/Sandeep_resume.pdf";

	String longText = jobFaker.getJobDescriptionText() + jobFaker.getNoteForCandidate();

	String dealName = dealFaker.getDealName();
	int dealValue = dealFaker.getDealValue();
	String dealStage = dealFaker.getNumber();
	String dealType = dealFaker.getNumber();
	String dealDate = dealFaker.getDealDate();

	String companyName = faker.getCompanyName();
	String companyWebsite = faker.getUrl();
	String contactNumber = "13456789087654";
	String companyCity = faker.getCity();
	String address = faker.getAddress();
	int industry_id = faker.getIndustry_id();

	String ContactFirstName = contactFaker.getFirstName();
	String ContactLastName = contactFaker.getLastName();
	String ContactEmail = "rcrmtest0@gmail.com";
	String contactNumbers = contactFaker.getContactNumber();
	String contactAvatarURL = fakerCandidate.getCandidateAvatarUrl();
	String contactFbLink = contactFaker.getUrl();
	String contactTwLink = contactFaker.getUrl();
	String contactLinkedinLink = contactFaker.getUrl();
	String contactXingLink = contactFaker.getUrl();

	String JobName = jobFaker.getJobName();
	int NoOfOpenings = jobFaker.getOpenings();

	String notesText = fakeNote.getNotes();

	JavaFakerTask fakerTask = new JavaFakerTask();
	String taskTitle = fakerTask.getTaskName();
	String taskDescription = fakerTask.getDescription();
	String startDate = fakerTask.getFutureDate();
	String pastDate = fakerTask.getPastDate();

	JavaFakerMeeting fakerMeeting = new JavaFakerMeeting();
	String meetingName = fakerMeeting.getMeetingName();
	String meetingDescription = fakerMeeting.getDescription();
	// String startDate = fakerMeeting.getFutureDate();
	String endDate = fakerMeeting.getEndDateWithReferenceDate(startDate);
	// String address = fakerMeeting.getAddress();

	JavaFakerCallLog fakerakerCallLog = new JavaFakerCallLog();
	String contactNo = fakerakerCallLog.getContactNumber();
	String callNotes = fakerakerCallLog.getCall_notes();

	JavaFakerHotlist fakerHotlist = new JavaFakerHotlist();
	String hotlistName = fakerHotlist.getHotlistName();

	JavaFakerPlacement placementFaker = new JavaFakerPlacement();
	JavaFakerInvoice fakerInvoice = new JavaFakerInvoice();
	JavaFakerCustomField customFieldFaker = new JavaFakerCustomField();

    private static final String cipherEmail = "AES/CBC/PKCS5Padding";
    private static final String cipherKey = "VGhlUXVpY2tCcm93bkZveEp1bXBzUmlnaHRPdmVyVGhlTGF6eURvZ01pcnphcHVyYUtlS2F0dGV5";
    private static final String cipherIv = "6768724235453264";

	public Response createNewCandidateWithMandatoryFields(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		// Personal Information
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber, 1,
				dob, 1, city, locality, Address);

		// Academic Information
		candidate.setQualification_id(qualificatioId);
		candidate.setSpecialization(specialization);

		// Employment Information
		candidate.setCurrent_organization(current_organization);
		candidate.setPosition(title);
		candidate.setWork_ex_year(work_ex_year);
		candidate.setRelevant_experience(RelevantExp);
		candidate.setSalaryType(salarytype);
		candidate.setCurrency_id(currencyId);
		candidate.setCurrent_salary(current_salary);
		candidate.setSalary_expectation(salary_expectation);
		candidate.setCurrent_status(currentStatus);
		candidate.setNotice_period(noticePeriod);
		candidate.setAvailable_from(availableForm);

		// Skills
		candidate.setSkill(skills);

		// Social Links
		candidate.setFacebook(fbLink);
		candidate.setTwitter(twitterLink);
		candidate.setLinkedin(linkedinLink);
		candidate.setGithub(githubLink);
		candidate.setXing(xingLink);
		candidate.setAvatar(candidateAvatarURL);
		candidate.setResume(resume);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", authTokenMap, null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 200);

		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(CandidateEmail, jp.get("email"), "email");
		Assert.assertEquals(CandidateNumber, jp.get("contact_number").toString(), "contact_number");
		return response;

	}

	public Response createNewCandidateWithAllFields(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
		String candidateFirstName = fakerCandidate.getFirstName();
		String candidateLastName = fakerCandidate.getLastName();
		String candidateEmail = "rcrmtest" + System.currentTimeMillis() % 10000 + "@gmail.com"; // Ensure unique email
		String candidateNumber = fakerCandidate.getContactNumber();
		String candidateGender = "1";
		String dob = fakerCandidate.getDOB();
		String willing_to_relocate = "1";
		String city = fakerCandidate.getCity();
		String locality = fakerCandidate.getLocality();
		String address = fakerCandidate.getCandidateAddress();
		String state = fakerCandidate.getState();
		String country = fakerCandidate.getCountry();
		String candidate_summary = fakerCandidate.getCandidateSummary();

		// Ensure we have a non-empty summary to use for testing
		if (candidate_summary == null || candidate_summary.trim().isEmpty()) {
			candidate_summary = "Professional with " + " years of experience in " +
					". Skilled in " + skills + ". Based in " + city + ", " + country + ".";
		}

		int qualificationId = fakerCandidate.getQualification_id();
		String specialization = fakerCandidate.getSpecialization();
		String currentOrganization = fakerCandidate.getCurrentOrganization();
		String position = fakerCandidate.getPosition();
		int workExYear = fakerCandidate.getWork_ex_year();
		int relevantExp = fakerCandidate.getRelevant_experience();
		String salaryType = fakerCandidate.getSalary_type();
		int currencyId = fakerCandidate.getCurrency_id();
		int currentSalary = fakerCandidate.getCurrent_salary();
		int salaryExpectation = fakerCandidate.getSalary_expectation();
		String currentStatus = fakerCandidate.getCurrentEmploymentStatus();
		int noticePeriod = fakerCandidate.getNotice_period();
		String availableFrom = fakerCandidate.getAvailable_From();
		String source = "LinkedIn";

		// Generate dynamic skills with multi-word values
		String skills = fakerCandidate.getSkills();

		String socialUrl = fakerCandidate.getUrl();
		String avatar = fakerCandidate.getCandidateAvatarUrl();
		String resumeUrl = "https://files-for-testing.s3-ap-southeast-1.amazonaws.com/Sandeep_resume.pdf";

		Candidate candidate = new Candidate(candidateFirstName, candidateLastName, candidateEmail, candidateNumber,
				Integer.parseInt(candidateGender), dob, Integer.parseInt(willing_to_relocate),
				city, locality, address, state, country, candidate_summary);

		// Academic Information
		candidate.setQualification_id(qualificationId);
		candidate.setSpecialization(specialization);

		// Employment Information
		candidate.setCurrent_organization(currentOrganization);
		candidate.setPosition(position);
		candidate.setWork_ex_year(workExYear);
		candidate.setRelevant_experience(relevantExp);
		candidate.setSalaryType(salaryType);
		candidate.setCurrency_id(currencyId);
		candidate.setCurrent_salary(currentSalary);
		candidate.setSalary_expectation(salaryExpectation);
		candidate.setCurrent_status(currentStatus);
		candidate.setNotice_period(noticePeriod);
		candidate.setAvailable_from(availableFrom);
		candidate.setSource(source);
		candidate.setState(state);
		candidate.setCountry(country);

		// Skills
		candidate.setSkill(skills);

		// Social Links
		candidate.setFacebook(socialUrl);
		candidate.setTwitter(socialUrl);
		candidate.setLinkedin(socialUrl);
		candidate.setGithub(socialUrl);
		candidate.setXing(socialUrl);
		candidate.setAvatar(avatar);
		candidate.setResume(resumeUrl);
		candidate.setCandidateSummary(candidate_summary);

		Response response = RestClient.doPost("JSON", baseURL, "candidates", authTokenMap, null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify the candidate was created with the provided values
		Assert.assertEquals(candidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(candidateLastName, jp.get("last_name"), "last_name");

		return response;
	}

	public Response createNewCandidateWithEmptyFields(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName);
		Response response = RestClient.doPost("JSON", baseURL, "candidates", authTokenMap, null, true, candidate);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 200);

		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(CandidateLastName, jp.get("last_name"), "last_name");
		return response;

	}

	public Response createNewCompanyWithMandatoryFields(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Company company = new Company(companyName, companyWebsite, contactNumber, faker.getLogoURL());
		company.setIndustry_id(industry_id);
		company.setLinkedin("https://www.linkedin.com/in/" + companyName.split(" ")[0]);
		company.setFacebook("https://www.facebook.com/" + companyName.split(" ")[0]);
		company.setTwitter("https://www.twitter.com/" + companyName.split(" ")[0]);
		company.setCity(city);
		company.setAddress(address);
		company.setAbout_company("This is about company " + companyName);
		Response response = RestClient.doPost("JSON", baseURL, "companies", authTokenMap, null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();
		response.then().statusCode(200);
		response.then().body("company_name", Matchers.is(companyName));
		return response;
	}

	public Response createNewContact_POST(String baseURL, Object authToken, String Company_slug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, Company_slug);
		contact.setLinkedin("https://www.linkedin.com/in/" + ContactFirstName.split(" ")[0]);
		Response response = RestClient.doPost("JSON", baseURL, "contacts", authTokenMap, null, true, contact);

		Assert.assertEquals(response.getStatusCode(), 200);

		// 4. get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		return response;
	}

	public Response createNewContactWithAllFields(String baseURL, Object authToken, String Company_slug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumber, Company_slug);
		contact.setAddress(Address);
		contact.setCity(city);
		contact.setAvatar(contactAvatarURL);
		contact.setDesignation(title);
		contact.setFacebook(contactFbLink);
		contact.setTwitter(contactTwLink);
		contact.setLinkedin(contactLinkedinLink);
		contact.setXing(contactXingLink);

		Response response = RestClient.doPost("JSON", baseURL, "contacts", authTokenMap, null, true, contact);


		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200
		Assert.assertEquals(response.getStatusCode(), 200);

		Assert.assertEquals(ContactFirstName, jp.get("first_name"), "first_name");
		Assert.assertEquals(ContactLastName, jp.get("last_name"), "last_name");
		Assert.assertEquals(ContactEmail, jp.get("email"), "email");
		Assert.assertEquals(contactNumber, jp.get("contact_number").toString(), "contact_number");
		return response;

	}

	public Response createNewJob(String baseURL, Object authToken, String Company_slug, String Contact_slug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Job job = new Job();
		job.setName(JobName);
		job.setCompany_slug(Company_slug);
		job.setContact_slug(Contact_slug);
		job.setNumber_of_openings(NoOfOpenings);
		job.setJob_type(4);

		// Below fields can't be blank
		job.setJob_description_text("Sample JD");
		job.setEnable_job_application_form(1);

		Response response = RestClient.doPost("JSON", baseURL, "jobs", authTokenMap, null, true, job);

		// get the response body:
		// String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// Verify response status code: 200

		Assert.assertEquals(response.getStatusCode(), 200);
		return response;

	}

    public Response createNewJobForMerging(String baseURL, Object authToken, String Company_slug, String Contact_slug) {
        Map<String, String> authTokenMap = getAuthTokenMap(authToken);

        JobName = jobFaker.getJobName();
        Job job = new Job();
        job.setName(JobName);
        job.setCompany_slug(Company_slug);
        job.setContact_slug(Contact_slug);
        job.setNumber_of_openings(NoOfOpenings);
        job.setJob_description_text("Sample JD");
        job.setEnable_job_application_form(1);

        Response response = RestClient.doPost("JSON", baseURL, "jobs", authTokenMap, null, true, job);

        JsonPath jp = response.jsonPath();

        Assert.assertEquals(response.getStatusCode(), 200);
        return response;
    }

    public Response updateJobStatus(String albatrossURL, String albatrossToken, String jobSlug, int jobStatus) {
        try {
            // Get job details first
            String getJobPath = "jobs/" + jobSlug + "/get";
            Response getJobResponse = RestClient.doPost("JSON", albatrossURL, getJobPath, albatrossToken, null, true, null);
            
            if (getJobResponse.getStatusCode() == 200) {
                JsonPath getJobJsonPath = getJobResponse.jsonPath();
                
                int companyId = getJobJsonPath.getInt("data.job.companyid");
                int contactId = getJobJsonPath.getInt("data.job.contactid");
                int ownerId = getJobJsonPath.getInt("data.job.ownerid");
                int jobId = getJobJsonPath.getInt("data.job.id");
                
                // Create job update data with new status
                JobUpdateData jobData = JobUpdateData.builder()
                        .slug(jobSlug)
                        .name(getJobJsonPath.getString("data.job.name"))
                        .description(getJobJsonPath.getString("data.job.description"))
                        .noofopenings(getJobJsonPath.getInt("data.job.noofopenings"))
                        .qualificationid(getJobJsonPath.getInt("data.job.qualificationid"))
                        .specialization(getJobJsonPath.getString("data.job.specialization"))
                        .minexperienceinyears(getJobJsonPath.getInt("data.job.minexperienceinyears"))
                        .maxexperienceinyears(getJobJsonPath.getInt("data.job.maxexperienceinyears"))
                        .annualsalarymin(getJobJsonPath.getInt("data.job.annualsalarymin"))
                        .annualsalarymax(getJobJsonPath.getInt("data.job.annualsalarymax"))
                        .salarytype(getJobJsonPath.getString("data.job.salarytype"))
                        .job_type(getJobJsonPath.getString("data.job.job_type"))
                        .locality(getJobJsonPath.getString("data.job.locality"))
                        .city(getJobJsonPath.getString("data.job.city"))
                        .country(getJobJsonPath.getString("data.job.country"))
                        .postalcode(getJobJsonPath.getString("data.job.postalcode"))
                        .state(getJobJsonPath.getString("data.job.state"))
                        .address(getJobJsonPath.getString("data.job.address"))
                        .currencyid(getJobJsonPath.getInt("data.job.currencyid"))
                        .companyid(companyId)
                        .contactid(contactId)
                        .ownerid(ownerId)
                        .id(jobId)
                        .jobstatus(jobStatus) // Set the new job status
                        .build();

                UpdateJobRequest updateJobRequest = UpdateJobRequest.builder()
                        .job(jobData)
                        .address_changed(false)
                        .filesInfo(new Object[]{})
                        .deleteJobKey("")
                        .secondaryContacts(new Object[]{})
                        .xml_feeds(new Object[]{})
                        .jobParserData(new Object[]{})
                        .collaborator(null)
                        .build();

                String basePath = "jobs/" + jobSlug;
                Response updateResponse = RestClient.doPost("JSON", albatrossURL, basePath, albatrossToken, null, true, updateJobRequest);
                
                Assert.assertEquals(updateResponse.getStatusCode(), 200, "Job status update should succeed");
                return updateResponse;
            } else {
                throw new RuntimeException("Failed to get job details for " + jobSlug + ": " + getJobResponse.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update job status for " + jobSlug + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Integer> getJobStatusValues(String albatrossURL, String albatrossToken) {
        Map<String, Integer> statusMap = new HashMap<>();
        try {
            Response response = RestClient.doPost("JSON", albatrossURL, "jobs/job-status-by-account/get", albatrossToken, null, true, null);
            if (response.getStatusCode() == 200) {
                JsonPath jp = response.jsonPath();
                List<Map<String, Object>> statusList = jp.getList("data");
                for (Map<String, Object> status : statusList) {
                    String label = (String) status.get("label");
                    Integer id = (Integer) status.get("id");
                    statusMap.put(label, id);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get job status values: " + e.getMessage(), e);
        }
        return statusMap;
    }

	public Response createNewDealWithMandatoryFields(String baseURL, Object authToken,
			String Company_slug, String Contact_slug, String job_slug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		Deal deal = new Deal();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type(dealType);
		deal.setCompany_slug(Company_slug);
		deal.setJob_slug(job_slug);
		deal.setContact_slugs(Contact_slug);

		Response response = RestClient.doPost("JSON", baseURL, "deals", authTokenMap, null, true, deal);

		// Get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response Code and body
		response.then().statusCode(200);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;

	}

	public Response createNewDealWithSpecifiedFields(String baseURL, Object authToken,
			HashMap<Integer, String> fieldsMap) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		int retries = 0;
		Response response = null;
		while (retries < maxRetries) {
			try {
				String[] dealFields = new String[] { dealName, String.valueOf(dealValue), dealDate, "1", dealType, "",
						"", "",
						"" };
				if (fieldsMap != null) {
					for (Map.Entry<Integer, String> e : fieldsMap.entrySet()) {
						dealFields[e.getKey()] = e.getValue();
					}
				}
				Deal deal = new Deal();
				deal.setName(dealFields[0]);
				deal.setDeal_value(Integer.valueOf(dealFields[1]));
				deal.setClose_date(dealFields[2]);
				deal.setDeal_stage(dealFields[3]);
				deal.setDeal_type(dealFields[4]);
				deal.setCompany_slug(dealFields[5]);
				deal.setJob_slug(dealFields[6]);
				deal.setContact_slugs(dealFields[7]);
				deal.setCandidate_slug(dealFields[8]);
				response = RestClient.doPost("JSON", baseURL, "deals", authTokenMap, null, true, deal);
				// Get the response body:
				String responseBody = response.getBody().asString();
				// Verify Response Code and body
				if (response.statusCode() != 200) {
					throw new Exception();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return response;
			} catch (Exception e) {
				retries++;
				if (retries == maxRetries) {
					throw new RuntimeException(
							"API call failed after maximum retries.\nResponse: " + response.prettyPrint());
				}
				// Sleep for a short duration before retrying
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}

		return null;
	}

	public Response createNewDealWithMandatoryFields(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		Deal deal = new Deal();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_stage("1");
		deal.setDeal_type("1");

		Response response = RestClient.doPost("JSON", baseURL, "deals", authTokenMap, null, true, deal);

		// Get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response Code and body
		response.then().statusCode(200);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;

	}

	public Response createNewTask(String baseURL, Object authToken, String realtedToType) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		JsonPath json;
		String entitySlug = "";
		int taskId;

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(30);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type(realtedToType);
		task.setStart_date(startDate);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", authTokenMap, null, true, task);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		taskId = jp.get("id");
		// 2295174

		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));

		return response;
	}

	public Response createTaskWithCreatedByUserId(String baseURL, Object authToken, String realtedToType, int userId) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		JsonPath json;
		String entitySlug = "";
		int taskId;

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(30);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type(realtedToType);
		task.setStart_date(startDate);
		task.setCreated_by(userId);
		task.setOwner_id(userId);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", authTokenMap, null, true, task);
		// Verify Response body
		response.then().statusCode(200);
		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));

		return response;
	}



	public Response createTaskWithCreatedByUserIdTypeAndTime(String baseURL, Object authToken, String realtedToType, int userId, int taskTypeId, String startTimeType) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		JsonPath json;
		String entitySlug = "";

		// Determine start date based on startTimeType
		String actualStartDate;
		if (startTimeType.equals("past")) {
			actualStartDate = fakerTask.getPastDate();
		} else {
			actualStartDate = fakerTask.getFutureDate();
		}

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();

			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(30);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type(realtedToType);
		task.setStart_date(actualStartDate);
		task.setCreated_by(userId);
		task.setOwner_id(userId);
		task.setTask_type_id(taskTypeId);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", authTokenMap, null, true, task);
		// Verify Response body
		response.then().statusCode(200);
		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));

		return response;
	}

	public Response createNewTaskWithCollaborators(String baseURL, Object authToken, String realtedToType,
			String userIds, String teamIds) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JsonPath json;
		String entitySlug = "";
		int taskId;

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(30);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type(realtedToType);
		task.setStart_date(startDate);
		task.setCollaborators(userIds);
		task.setCollaborator_team_ids(teamIds);

		Response response = RestClient.doPost("JSON", baseURL, "tasks", authTokenMap, null, true, task);

		response.then().body("title", Matchers.containsString(taskTitle));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));

		return response;
	}

	public Response createNewCallLog(String baseURL, Object authToken, String relatedToType) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JsonPath json;
		String entitySlug = "";
		int callLogId;

		if (relatedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (relatedToType == "contact") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}
		if (relatedToType == "company") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = jsonCompany.get("slug");
		}

		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setContact_number(contactNo);
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type(relatedToType);
		callLog.setCall_started_on(pastDate);
		callLog.setDuration("1hr");

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", authTokenMap, null, true, callLog);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath JsonPath
		JsonPath jp = response.jsonPath();

		callLogId = jp.get("id");

		response.then().body("call_notes", Matchers.containsString(callNotes));
		response.then().body("contact_number", Matchers.is(contactNo));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(relatedToType));

		return response;
	}

	public Response createNewCallLogWithCollaborators(String baseURL, Object authToken, String relatedToType,
			String userId, String teamId) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JsonPath json;
		String entitySlug = "";
		int callLogId;

		if (relatedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (relatedToType == "contact") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}
		if (relatedToType == "company") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = jsonCompany.get("slug");
		}

		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setContact_number(contactNo);
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type(relatedToType);
		callLog.setCall_started_on(pastDate);
		callLog.setCollaborator_user_ids(userId);
		callLog.setCollaborator_team_ids(teamId);

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", authTokenMap, null, true, callLog);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath JsonPath
		JsonPath jp = response.jsonPath();

		callLogId = jp.get("id");

		response.then().body("call_notes", Matchers.containsString(callNotes));
		response.then().body("contact_number", Matchers.is(contactNo));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(relatedToType));

		return response;
	}

	public Response createNewCallLogWithSpecificFields(String baseURL, Object authToken,
			HashMap<Integer, String> fieldsMap) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JsonPath json;
		String entitySlug = "";
		int callLogId;

		String[] fields = { "candidate", pastDate };
		for (int i = 0; i < fields.length; i++) {
			fields[i] = fieldsMap.get(i);
		}

		String relatedToType = fields[0];

		if (relatedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (relatedToType == "contact") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}
		if (relatedToType == "company") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = jsonCompany.get("slug");
		}

		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setContact_number(contactNo);
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type(relatedToType);
		callLog.setCall_started_on(fields[1]);

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", authTokenMap, null, true, callLog);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath JsonPath
		JsonPath jp = response.jsonPath();

		callLogId = jp.get("id");

		response.then().body("call_notes", Matchers.containsString(callNotes));
		response.then().body("contact_number", Matchers.is(contactNo));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(relatedToType));

		return response;
	}

	public Response createNewNoteAndGetResponse(String baseURL, Object authToken,
			String realtedToType) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JsonPath json;
		String entitySlug = "";

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type(realtedToType);
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()

		Response response = RestClient.doPost("JSON", baseURL, "notes", authTokenMap, null, true, note);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		// noteId = jp.get("id");
		// // 2295174

		response.then().body("description", Matchers.containsString(notesText));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		return response;

	}

	public Response createNewNoteWithCollaborators(String baseURL, Object authToken,
			String realtedToType, String userIds, String teamIds) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		JsonPath json;
		String entitySlug = "";

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type(realtedToType);
		note.setDescription(notesText + "<br><br>" + notesText); // System.lineSeparator()
		note.setCollaborator_user_ids(userIds);
		note.setCollaborator_team_ids(teamIds);

		Response response = RestClient.doPost("JSON", baseURL, "notes", authTokenMap, null, true, note);

		response.then().body("description", Matchers.containsString(notesText));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		return response;

	}

	public Response createNewMeetings(String baseURL, Object authToken, String realtedToType) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JsonPath json;
		String entitySlug = "";
		int meetingId;

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(pastDate);
		meeting.setEnd_date(endDate);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", authTokenMap, null, true, meeting);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		meetingId = jp.get("id");
		// 2295174

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("title", Matchers.containsString(meetingName));
		response.then().body("address", Matchers.containsString(address));
		response.then().body("description", Matchers.containsString(meetingDescription));

		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		return response;
	}

	public Response createNewMeetingsWithCollaborators(String baseURL, Object authToken, String realtedToType,
			String userIds, String teamIds) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JsonPath json;
		String entitySlug = "";
		int meetingId;

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(pastDate);
		meeting.setEnd_date(endDate);
		meeting.setAttendee_users(userIds);
		meeting.setCollaborator_user_ids(userIds);
		meeting.setCollaborator_team_ids(teamIds);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", authTokenMap, null, true, meeting);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		meetingId = jp.get("id");
		// 2295174

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("title", Matchers.containsString(meetingName));
		response.then().body("address", Matchers.containsString(address));
		response.then().body("description", Matchers.containsString(meetingDescription));

		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		return response;
	}

	public Response createMeetingWithCreatedByUserId(String baseURL, Object authToken, String realtedToType,
			int userId) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		JsonPath json;
		String entitySlug = "";

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(pastDate);
		meeting.setEnd_date(endDate);
		meeting.setCreated_by(userId);
		meeting.setOwner_id(userId);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", authTokenMap, null, true, meeting);

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());

		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		return response;
	}

	public Response createMeetingWithCreatedByUserIdTypeAndTime(String baseURL, Object authToken, String realtedToType,
			int userId, int meetingTypeId, String startTimeType) {
		meetingName = fakerMeeting.getMeetingName();
		meetingDescription = fakerMeeting.getDescription();
		address = faker.getAddress();
		
		// Determine start date based on startTimeType
		String actualStartDate;
		String actualEndDate;
		if (startTimeType.equals("past")) {
			actualStartDate = fakerTask.getPastDate();
			actualEndDate = fakerMeeting.getEndDateWithReferenceDate(actualStartDate);
		} else {
			actualStartDate = fakerTask.getFutureDate();
			actualEndDate = fakerMeeting.getEndDateWithReferenceDate(actualStartDate);
		}
		
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		JsonPath json;
		String entitySlug = "";

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();

			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(actualStartDate);
		meeting.setEnd_date(actualEndDate);
		meeting.setCreated_by(userId);
		meeting.setOwner_id(userId);
		meeting.setMeeting_type_id(meetingTypeId);
		Response response = RestClient.doPost("JSON", baseURL, "meetings", authTokenMap, null, true, meeting);

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());

		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		return response;
	}

	public Response createNewMeetingsWithCustomDate(String baseURL, Object authToken, String realtedToType,
			String startDate, String endDate, String randomString) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		JsonPath json;
		String entitySlug = "";
		int meetingId;

		String entityName = realtedToType.trim();

		try {
			switch (entityName) {
				case "candidate":
					json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
					entitySlug = json.get("slug");
					break;

				case "company":
					json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
					entitySlug = json.get("slug");
					break;

				case "contact":
					JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
					;
					String companySlug = jsonCompany.get("slug");
					json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
					entitySlug = json.get("slug");
					break;

				case "job":
					JsonPath jsonCompany1 = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
					String companySlug1 = jsonCompany1.get("slug");
					JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug1).jsonPath();
					String contactSlug = jsonContact.get("slug");
					json = createNewJob(baseURL, authTokenMap, companySlug1, contactSlug).jsonPath();
					entitySlug = json.get("slug");
					break;

				case "deal":
					JsonPath jsonCompany2 = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
					String companySlug2 = jsonCompany2.get("slug");

					JsonPath jsonContact2 = createNewContact_POST(baseURL, authTokenMap, companySlug2).jsonPath();
					String contactSlug2 = jsonContact2.get("slug");

					JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug2, contactSlug2).jsonPath();
					String jobSlug = jsonJob.getString("slug");

					JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug2,
							contactSlug2, jobSlug).jsonPath();

					entitySlug = jsonDeal.get("slug");
					break;

				default:
			}
		} catch (Throwable t) {
			entitySlug = getEntityResponse(baseURL, authTokenMap, realtedToType);
		}

		Response userId = getUsers(baseURL, authTokenMap);
		JsonPath jpUser = userId.jsonPath();

		int id = jpUser.get("[0].id");

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName + " " + randomString);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);

		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(startDate);
		meeting.setEnd_date(endDate);
		meeting.setOwner_id(id);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", authTokenMap, null, true, meeting);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		meetingId = jp.get("id");
		// 2295174

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("title", Matchers.containsString(meetingName));
		response.then().body("address", Matchers.containsString(address));
		response.then().body("description", Matchers.containsString(meetingDescription));

		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		return response;
	}

	public Response createNewMeetingsWithSpecifiedFields(String baseURL, Object authToken, String realtedToType,
			HashMap<Integer, String> fieldsMap) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JsonPath json;
		String entitySlug = "";
		int meetingId;

		if (realtedToType == "candidate") {
			json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "company") {
			json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType == "contact") {

			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			;
			String companySlug = jsonCompany.get("slug");
			json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "job") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			json = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}

		if (realtedToType == "deal") {
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
			String companySlug = jsonCompany.get("slug");

			JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");

			JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.getString("slug");

			JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug, contactSlug,
					jobSlug).jsonPath();

			entitySlug = jsonDeal.get("slug");
		}

		String[] meetingFields = new String[] { meetingName, meetingDescription, address, "15", pastDate, endDate };
		if (fieldsMap != null) {
			for (Map.Entry<Integer, String> e : fieldsMap.entrySet()) {
				meetingFields[e.getKey()] = e.getValue();
			}
		}

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingFields[0]);
		meeting.setDescription(meetingFields[1]);
		meeting.setAddress(meetingFields[2]);

		meeting.setReminder(Integer.parseInt(meetingFields[3]));
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(realtedToType);
		meeting.setStart_date(meetingFields[4]);
		meeting.setEnd_date(meetingFields[5]);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", authTokenMap, null, true, meeting);

		// get the response body:
		String responseBody = response.getBody().asString();

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		meetingId = jp.get("id");
		// 2295174

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("title", Matchers.containsString(meetingName));
		response.then().body("address", Matchers.containsString(address));
		response.then().body("description", Matchers.containsString(meetingDescription));

		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(realtedToType));
		return response;
	}

	public Response createNewHotlist(String baseURL, Object authToken, String realtedToType) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String generatedString = RandomStringUtils.randomAlphabetic(10);

		Hotlist hotlist = new Hotlist();
		hotlist.setFirst_name(hotlistName + " " + NoOfOpenings + " " + generatedString);
		hotlist.setRelated_to_type(realtedToType);
		hotlist.setShared(1);

		Response response = RestClient.doPost("JSON", baseURL, "hotlists", authTokenMap, null, true, hotlist);

		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("name", Matchers.containsString(hotlistName));

		return response;
	}

	public Response assignCandidateToJob(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		String candidateSlug;
		String jobSlug;
		try {
			candidateSlug = getEntityResponse(baseURL, authTokenMap, "candidate");
			jobSlug = getEntityResponse(baseURL, authTokenMap, "job");
		} catch (Exception e) {
			candidateSlug = getEntityResponse(baseURL, authTokenMap, "candidate");
			jobSlug = getEntityResponse(baseURL, authTokenMap, "job");
		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, authTokenMap, queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("candidate_slug", Matchers.is(candidateSlug));
		response.then().body("job_slug", Matchers.is(jobSlug));

		// response.then().body("status.status_id", Matchers.is(1));
		response.then().body("status.label", Matchers.is("Assigned"));
		// response.then().body("remark", Matchers.is("Updated"));
		response.then().body("visibility", Matchers.is(1));
		response.then().body("stage_date", Matchers.notNullValue());

		response.then().body("updated_on", Matchers.notNullValue());
		response.then().body("updated_by", Matchers.notNullValue());

		return response;
	}

	public Response assignCandidateByJobSlug(String baseURL, Object authToken, String jobSlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		String candidateSlug;
		try {
			candidateSlug = getEntityResponse(baseURL, authTokenMap, "candidate");

		} catch (Exception e) {
			candidateSlug = getEntityResponse(baseURL, authTokenMap, "candidate");

		}
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, authTokenMap, queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("candidate_slug", Matchers.is(candidateSlug));
		response.then().body("job_slug", Matchers.is(jobSlug));

		// response.then().body("status.status_id", Matchers.is(1));
		response.then().body("status.label", Matchers.is("Assigned"));
		// response.then().body("remark", Matchers.is("Updated"));
		response.then().body("visibility", Matchers.is(1));
		response.then().body("stage_date", Matchers.notNullValue());

		response.then().body("updated_on", Matchers.notNullValue());
		response.then().body("updated_by", Matchers.notNullValue());

		return response;
	}

	public Response assignCandidateByJobSlugAndCandidateSlug(String baseURL, Object authToken, String jobSlug, String candidateSlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, authTokenMap, queryParameters, pathParamters,
				true, null);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("candidate_slug", Matchers.is(candidateSlug));
		response.then().body("job_slug", Matchers.is(jobSlug));

		response.then().body("status.label", Matchers.is("Assigned"));
		response.then().body("visibility", Matchers.is(1));
		response.then().body("stage_date", Matchers.notNullValue());

		response.then().body("updated_on", Matchers.notNullValue());
		response.then().body("updated_by", Matchers.notNullValue());

		return response;
	}

	public String getEntityResponse(String baseURL, Object authToken, String realtedToType) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JsonPath json = null;
		String entitySlug = "";
		String entityName = realtedToType.trim();

		switch (entityName) {
			case "candidate":
				json = createNewCandidateWithMandatoryFields(baseURL, authTokenMap).jsonPath();
				entitySlug = json.get("slug");
				break;

			case "company":
				json = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
				entitySlug = json.get("slug");
				break;

			case "contact":
				JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();

				String companySlug = jsonCompany.get("slug");
				ThreadSleep(1);
				json = createNewContact_POST(baseURL, authTokenMap, companySlug).jsonPath();
				entitySlug = json.get("slug");
				break;

			case "job":
				JsonPath jsonCompany1 = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
				String companySlug1 = jsonCompany1.get("slug");
				ThreadSleep(1);
				JsonPath jsonContact = createNewContact_POST(baseURL, authTokenMap, companySlug1).jsonPath();
				String contactSlug = jsonContact.get("slug");
				ThreadSleep(1);
				json = createNewJob(baseURL, authTokenMap, companySlug1, contactSlug).jsonPath();
				entitySlug = json.get("slug");
				break;

			case "deal":
				JsonPath jsonCompany2 = createNewCompanyWithMandatoryFields(baseURL, authTokenMap).jsonPath();
				String companySlug2 = jsonCompany2.get("slug");
				ThreadSleep(1);
				JsonPath jsonContact2 = createNewContact_POST(baseURL, authTokenMap, companySlug2).jsonPath();
				String contactSlug2 = jsonContact2.get("slug");
				ThreadSleep(1);
				JsonPath jsonJob = createNewJob(baseURL, authTokenMap, companySlug2, contactSlug2).jsonPath();
				String jobSlug = jsonJob.getString("slug");
				ThreadSleep(1);
				JsonPath jsonDeal = createNewDealWithMandatoryFields(baseURL, authTokenMap, companySlug2, contactSlug2,
						jobSlug).jsonPath();

				entitySlug = jsonDeal.get("slug");
				break;

			default:
		}

		return entitySlug; // json.get("resource_url");
	}

	public Response getAllCompanies_GET(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "5");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "companies", authTokenMap, queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// Verify Response Code and body
		// response.then().statusCode(200);
		// response.then().body("data.company_name[0]", Matchers.is(companyName));
		// response.then().body("data.city[0]", Matchers.is(companyCity));
		// response.then().body("data.address[0]", Matchers.is(address));
		// response.then().body("data.facebook[0]", Matchers.is(companyWebsite));
		// response.then().body("data.twitter[0]", Matchers.is(companyWebsite));
		// response.then().body("data.linkedin[0]", Matchers.is(companyWebsite));
		// response.then().body("data.website[0]", Matchers.is(companyWebsite));
		// response.then().body("data.logo[0]",
		// Matchers.containsString("recruitcrm.io"));
		// response.then().body("data.industry_id[0]", Matchers.is(industry_id));

		return response;
	}

	public Response getAllCandidates(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "5");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "candidates", authTokenMap, queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		// response.then().body("first_name", Matchers.is(CandidateFirstName));
		// response.then().body("created_by.id", Matchers.notNullValue());
		// response.then().body("updated_by.id", Matchers.notNullValue());
		// response.then().body("owner.id", Matchers.notNullValue());
		// response.then().body("qualification.qualification_id",
		// Matchers.notNullValue());
		// response.then().body("currency.currency_id", Matchers.notNullValue());
		// response.then().body("salary_type.id", Matchers.notNullValue());

		return response;

	}

	public Response getAllCandidates_GET(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Response response = RestClient.doGet("JSON", baseURL, "candidates", authTokenMap, null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200, "Response Status Code must be 200!");

		return response;
	}

	public Response getAllContacts_GET(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "5");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "contacts", authTokenMap, queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		return response;

	}

	public Response getAllJobs_GET(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "5");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "jobs", authTokenMap, queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		return response;

	}

	public Response getAllDealsGET(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "5");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "deals", authTokenMap, queryParameters, null, true);

		// response.then().statusCode(200);
		// response.then().body("data[0].id", Matchers.notNullValue());
		// response.then().body("current_page", Matchers.comparesEqualTo(1));
		return response;
	}

	public Response getEntityResponseBasedOnSlug(String baseURL, Object authToken, String entityName, String slug) {
		String basePath;
		String pathParamKey;
		switch (entityName == null ? "" : entityName.toLowerCase()) {
		case "candidate":
			basePath = "candidates/{candidate}";
			pathParamKey = "candidate";
			break;
		case "company":
			basePath = "companies/{company}";
			pathParamKey = "company";
			break;
		case "contact":
			basePath = "contacts/{contact}";
			pathParamKey = "contact";
			break;
		case "job":
			basePath = "jobs/{job}";
			pathParamKey = "job";
			break;
		case "deal":
			basePath = "deals/{deal}";
			pathParamKey = "deal";
			break;
		default:
			throw new IllegalArgumentException("Unsupported entityName: " + entityName + ". Use: candidate, company, contact, job, deal.");
		}
		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put(pathParamKey, slug);
		Response response = RestClient.doGet("JSON", baseURL, basePath, authToken, null, pathParameters, true);
		Assert.assertEquals(response.getStatusCode(), 200);
		return response;
	}

	private void ThreadSleep(int i) {
		// TODO Auto-generated method stub

	}

	public Response getAllHiringStages(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("limit", "25");
		queryParameters.put("page", "1");

		Response response = RestClient.doGet("JSON", baseURL, "hiring-pipeline", authTokenMap, queryParameters, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 200);

		return response;

	}

	public void deleteAllSubscriptions(String baseURL, String apiKey) {
		Map<String, String> authTokenMap = new HashMap<String, String>();
		authTokenMap.put("Authorization", "Bearer " + apiKey);
		Response response = RestClient.doGet("JSON", baseURL, "subscriptions", authTokenMap, null, null, true);
		if (response.jsonPath().getList("data.id") == null) {
			return;
		} else {
			List<Long> idList = response.jsonPath().getList("data.id");
			for (int i = 0; i < idList.size(); i++) {
				RestClient.doDelete("JSON", baseURL, "subscriptions/" + idList.get(i), authTokenMap, null, null, false);
			}
		}
		if (response.jsonPath().get("next_page_url") != null) {
			deleteAllSubscriptions(baseURL, apiKey);
		}

	}

	public ArrayList<Integer> getAllSubscriptionIDs(String baseURL, String apiKey) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		Map<String, String> authTokenMap = new HashMap<String, String>();
		authTokenMap.put("Authorization", "Bearer " + apiKey);
		Response response = RestClient.doGet("JSON", baseURL, "subscriptions", authTokenMap, null, null, true);
		int i = 1;
		while (response.jsonPath().get("data[0].id") != null) {
			response = RestClient.doGet("JSON", baseURL, "subscriptions?page=" + i, authTokenMap, null, null, true);
			result.addAll(response.jsonPath().getList("data.id"));
			i++;
		}
		return result;

	}

	public ArrayList<String> getAllSubscriptionEvents(String baseURL, String apiKey) {
		ArrayList<String> result = new ArrayList<String>();
		Map<String, String> authTokenMap = new HashMap<String, String>();
		authTokenMap.put("Authorization", "Bearer " + apiKey);
		Response response = RestClient.doGet("JSON", baseURL, "subscriptions", authTokenMap, null, null, true);
		int i = 1;
		while (response.jsonPath().get("data[0].id") != null) {
			response = RestClient.doGet("JSON", baseURL, "subscriptions?page=" + i, authTokenMap, null, null, true);
			result.addAll(response.jsonPath().getList("data.event"));
			i++;
		}
		return result;

	}

	public ArrayList<String> getAllSubscriptionURLs(String baseURL, String apiKey) {
		ArrayList<String> result = new ArrayList<String>();
		Map<String, String> authTokenMap = new HashMap<String, String>();
		authTokenMap.put("Authorization", "Bearer " + apiKey);
		Response response = RestClient.doGet("JSON", baseURL, "subscriptions", authTokenMap, null, null, true);
		int i = 1;
		while (response.jsonPath().get("data[0].id") != null) {
			response = RestClient.doGet("JSON", baseURL, "subscriptions?page=" + i, authTokenMap, null, null, true);
			result.addAll(response.jsonPath().getList("data.target_url"));
			i++;
		}
		return result;

	}

	public void deleteEntities(String baseURL, String apiKey, List<String> slugs) {
		Map<String, String> authTokenMap = new HashMap<String, String>();
		authTokenMap.put("Authorization", "Bearer " + apiKey);
		for (int i = 0; i < slugs.size(); i++) {
			if (RestClient.doDelete("JSON", baseURL, "candidates/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}
			if (RestClient.doDelete("JSON", baseURL, "contacts/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}
			if (RestClient.doDelete("JSON", baseURL, "companies/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}
			if (RestClient.doDelete("JSON", baseURL, "jobs/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}
			if (RestClient.doDelete("JSON", baseURL, "deals/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}
			if (RestClient.doDelete("JSON", baseURL, "notes/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}
			if (RestClient.doDelete("JSON", baseURL, "tasks/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}
			if (RestClient.doDelete("JSON", baseURL, "meetings/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}
			if (RestClient.doDelete("JSON", baseURL, "call-logs/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}
			if (RestClient.doDelete("JSON", baseURL, "hotlists/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}
			if (RestClient.doDelete("JSON", baseURL, "subscriptions/" + slugs.get(i), authTokenMap, null, null, true)
					.statusCode() == 200) {
				continue;
			}

		}

	}

	public Response getUsers(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		Response result = RestClient.doGet("JSON", baseURL, "users", authTokenMap, null, null, true);
		return result;
	}

	public Response getTeams(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		Response result = RestClient.doGet("JSON", baseURL, "teams", authTokenMap, null, null, true);
		return result;
	}

    public Integer getOwnerUserId(String baseURL, Object authToken) {
        try {
            Response usersResp = getUsers(baseURL, authToken);
            if (usersResp == null || usersResp.getStatusCode() != 200) return null;
            return usersResp.jsonPath().getInt("[0].id");
        } catch (Exception e) {
            return null;
        }
    }

	public Response deactivateUser(String userAccountId, String firstName, String lastName, String albatrossURL,
			Object albatrossAuthToken) {
		Map<String, List<String>> roleMap = new HashMap<>();
		roleMap.put("Recruiter", Arrays.asList("5", "Restricted Team Member"));
		roleMap.put("TeamMember", Arrays.asList("3", "Team Member"));
		roleMap.put("Admin", Arrays.asList("2", "Admin"));
		roleMap.put("Owner", Arrays.asList("4", "Account Owner"));

		DeactivateUser deactivateUser = new DeactivateUser();
		deactivateUser.setKeepLicense(true);

		DeactivateUser.User user = new DeactivateUser.User();
		user.setId(Integer.parseInt(userAccountId));
		user.setFirstname(firstName);
		user.setLastname(lastName);

		Roleid roleId = new Roleid();

		for (Map.Entry<String, List<String>> entry : roleMap.entrySet()) {
			String key = entry.getKey();
			List<String> value = entry.getValue();
			if (firstName.contains(key)) {
				roleId.setId(Integer.parseInt(value.get(0)));
				roleId.setRole(value.get(1));
				break;
			}
		}

		user.setRoleid(roleId);
		deactivateUser.setUser(user);

		Response response = RestClient.doPost("JSON", albatrossURL, "users/deactivate/" + userAccountId,
				albatrossAuthToken, null, true, deactivateUser);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().assertThat().body("message", Matchers.containsString("User Deactivated Successful"));

		return response;
	}

	public Response getAllHiringPipeline(String hiringPipelineServiceURL, Object albatrossAuthToken) {
		Response response = RestClient.doGet("JSON", hiringPipelineServiceURL, "pipelines/list", albatrossAuthToken,
				null, null, true);
		response.then().statusCode(200);
		return response;
	}

	public Map<String, String> getAuthTokenMap(Object authToken) {
		Map<String, String> authTokenMap = new HashMap<String, String>();
		if (authToken instanceof Map) {
			authTokenMap = (Map<String, String>) authToken;
		} else {
			String apiKey = (String) authToken;
			authTokenMap = new HashMap<String, String>();
			authTokenMap.put("Authorization", "Bearer " + apiKey);
		}
		return authTokenMap;
	}

	public Object[][] createCandidateAndJobByUserId(String baseURL, Object authToken, int userId) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		// Personal Information
		Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, CandidateEmail, CandidateNumber, 1,
				dob, 1, city, locality, Address);
		candidate.setOwner_id(userId);
		Response response = RestClient.doPost("JSON", baseURL, "candidates", authTokenMap, null, true, candidate);
		Assert.assertEquals(response.statusCode(), 200, "Candidate Endpoint Failure");

		JsonPath jp = response.jsonPath();

		Assert.assertEquals(CandidateFirstName, jp.get("first_name"), "first_name");
		String candidateSlug = jp.get("slug");
		JsonPath jp1 = (createNewCompanyWithMandatoryFields(baseURL, authToken)).jsonPath();
		String companySlug = jp1.get("slug");
		JsonPath jp2 = (createNewContact_POST(baseURL, authToken, companySlug)).jsonPath();
		String contactSlug = jp2.get("slug");

		Job job = new Job();
		job.setName(JobName);
		job.setCompany_slug(companySlug);
		job.setContact_slug(contactSlug);
		job.setNumber_of_openings(NoOfOpenings);

		// Below fields can't be blank
		job.setJob_description_text("Sample JD");
		job.setEnable_job_application_form(1);

		job.setOwner_id(userId);

		Response response1 = RestClient.doPost("JSON", baseURL, "jobs", authTokenMap, null, true, job);
		Assert.assertEquals(response1.statusCode(), 200, "Candidate Endpoint Failure");

		JsonPath jp3 = response1.jsonPath();
		Assert.assertEquals(JobName, jp3.get("name"), "name");
		String jobSlug = jp3.get("slug");

		Object[][] data = { { candidateSlug, jobSlug } };
		return data;
	}

	public Response assignMultipleCandsToMultipleJobs(String baseURL, String albatrossURL, Object authToken,
			Object albatrossAuthToken) {

		ArrayList<Integer> candidates = new ArrayList<>();
		ArrayList<Integer> candidateIds = new ArrayList<>();
		ArrayList<Integer> jobIds = new ArrayList<>();

		ArrayList<Job> jobs = new ArrayList<>();
		for (int i = 0; i < 3; i++) {

			JsonPath jsonCandidate = createNewCandidateWithMandatoryFields(baseURL, authToken).jsonPath();
			String candidateSlug = jsonCandidate.get("slug");
			Map<String, String> pathParameters = new HashMap<String, String>();
			pathParameters.put("candidate_Slug", candidateSlug);
			Response getCandResponse = RestClient.doPost1("JSON", albatrossURL, "candidates/{candidate_Slug}/get",
					albatrossAuthToken,
					null, pathParameters, true, null);
			JsonPath jsonPath1 = getCandResponse.jsonPath();
			int candidateId = jsonPath1.get("data.candidate.id");
			candidateIds.add(candidateId);
			JsonPath jsonCompany = createNewCompanyWithMandatoryFields(baseURL, authToken).jsonPath();
			String companySlug = jsonCompany.get("slug");
			String companyName = jsonCompany.get("company.name");
			JsonPath jsonContact = createNewContact_POST(baseURL, authToken, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			JsonPath jsonJob = createNewJob(baseURL, authToken, companySlug, contactSlug).jsonPath();
			String jobSlug = jsonJob.get("slug");
			String jobName = jsonJob.get("name");
			String jobSrNo = jsonJob.get("srno");
			String jobCity = jsonJob.get("city");
			pathParameters = new HashMap<String, String>();
			pathParameters.put("job_Slug", jobSlug);
			Response response = RestClient.doPost1("JSON", albatrossURL, "jobs/{job_Slug}/get", albatrossAuthToken,
					null, pathParameters, true, null);
			Assert.assertEquals(response.getStatusCode(), 200, "Job data not found");
			jsonPath1 = response.jsonPath();
			String jobId = jsonPath1.get("data.job.id").toString();
			jobIds.add(Integer.parseInt(jobId));
			Job job = new Job(jobName, companyName, jobSrNo, jobCity, jobSlug, jobId, true);
			jobs.add(job);
			candidates.add(candidateId);
		}

		AssignMultipleCandToMultipleJobs assignMultipleCandToMultipleJobs = new AssignMultipleCandToMultipleJobs(jobs,
				candidates, false, false);

		String basePath = "candidates/assign";

		Response response1 = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true,
				assignMultipleCandToMultipleJobs);

		Assert.assertEquals(response1.getStatusCode(), 200, "Endpoint failure");

		AssignedCandInJob assignedCandInJob = new AssignedCandInJob(jobIds, candidateIds);

		String basePath1 = "jobs/get-assigned-candidates-latex/get";

		Response response2 = RestClient.doPost("JSON", albatrossURL, basePath1, albatrossAuthToken, null, true,
				assignedCandInJob);
		Assert.assertEquals(response2.getStatusCode(), 200, "Endpoint failure");
		return response2;

	}

	public Response getAllContactStages(String baseURL, Object authToken) {
		Response response = RestClient.doGet("JSON", baseURL, "sales-pipeline", authToken, null, null, true);
		assert response != null;
		response.then().statusCode(200);
		return response;
	}

	public Response getAllDealStages(String baseURL, Object authToken) {
		Response response = RestClient.doGet("JSON", baseURL, "deals-pipeline", authToken, null, null, true);
		assert response != null;
		response.then().statusCode(200);
		return response;
	}

	public Response createNewMeetingWithEntitySlug(String baseURL, Object authToken, String relatedToType,
			String entitySlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);
		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(relatedToType);
		meeting.setStart_date(pastDate);
		meeting.setEnd_date(endDate);

		Response response = RestClient.doPost("JSON", baseURL, "meetings", authTokenMap, null, true, meeting);

		assert response != null;
		response.then().statusCode(200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("title", Matchers.containsString(meetingName));
		response.then().body("address", Matchers.containsString(address));
		response.then().body("description", Matchers.containsString(meetingDescription));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(relatedToType));

		return response;
	}

	public Response createNewNoteLogWithEntitySlug(String baseURL, Object authToken, String relatedToType, String entitySlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type(relatedToType);
		note.setDescription(notesText); 

		Response response = RestClient.doPost("JSON", baseURL, "notes", authTokenMap, null, true, note);
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(relatedToType));
		return response;
	}

	public Response createNewCallLogWithEntitySlug(String baseURL, Object authToken, String relatedToType,
			String entitySlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		Call_Log callLog = new Call_Log();
		callLog.setCall_notes(callNotes);
		callLog.setCall_type("CALL_INCOMING");
		callLog.setContact_number(contactNo);
		callLog.setRelated_to(entitySlug);
		callLog.setRelated_to_type(relatedToType);
		callLog.setCall_started_on(pastDate);
		callLog.setDuration("1hr");

		Response response = RestClient.doPost("JSON", baseURL, "call-logs", authTokenMap, null, true, callLog);

		// Verify response using assertion
		assert response != null;
		response.then().statusCode(200);
		response.then().body("call_notes", Matchers.containsString(callNotes));
		response.then().body("contact_number", Matchers.is(contactNo));
		response.then().body("related_to", Matchers.is(entitySlug));
		response.then().body("related_to_type", Matchers.is(relatedToType));

		return response;
	}

	public void sendEmailToCandCont(int linkedEmailType, String entity, String nymaURL, String Email, String FullName,
			String slug, String albatrossAuthTkn) {
		ArrayList<Object> receiverList = new ArrayList<Object>();
		ArrayList<Object> emailList = new ArrayList<Object>();
		ReceiverEmailsPage receiverEmailsPage = new ReceiverEmailsPage();
		receiverEmailsPage.setEmail(Email);
		receiverEmailsPage.setName(FullName);
		receiverEmailsPage.setEntity_slug(slug);
		receiverEmailsPage.setEntity_type(entity.equalsIgnoreCase("Candidate") ? 5 : 2);
		receiverList.add(receiverEmailsPage);
		EmailsPage emailsPage = new EmailsPage();
		emailsPage.setRecivers(receiverList);
		emailsPage.setCC(emailList);
		emailsPage.setBCC(emailList);
		emailsPage.setSubject(fakerMails.getFakeEmailSubject());
		emailsPage.setBody(fakerMails.getFakeEmailBody(5));
		emailsPage.setVersion(0);
		SendEmailsPage sendEmailsPage = new SendEmailsPage();
		sendEmailsPage.setEmail(emailsPage);
		sendEmailsPage.setis_send(true);
		sendEmailsPage.setLinked_email_type(linkedEmailType);

		Response response = RestClient.doPost("JSON", nymaURL, "emails", albatrossAuthTkn, null, true, sendEmailsPage);

		// Verify response using assertion
		assert response != null;
		response.then().statusCode(200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message",
				Matchers.containsString("1 Email(s) Sent Successfully, 0 Email(s) Skipped And 0 Email(s) Failed."));
		response.then().body("action_name", Matchers.containsString("Email Sent"));
	}

	public void createEmailTemplate(String relatedToTypeId, String albatrossURL, String albatrossTkn, boolean setShare,
			String randomString) {
		New_email_templatePage new_email_templatePage = new New_email_templatePage();
		new_email_templatePage.setEmailcontext("Email Template " + randomString);
		new_email_templatePage.setRelatedtotypeid(relatedToTypeId);
		new_email_templatePage.setEmailsubject(fakerMails.getFakeEmailSubject());
		new_email_templatePage.setTemplate(fakerMails.getFakeEmailBody(5));
		new_email_templatePage.setShare(setShare);

		EmailTemplatePage emailTemplatePage = new EmailTemplatePage();
		emailTemplatePage.setNew_email_template(new_email_templatePage);
		Response response = RestClient.doPost("JSON", albatrossURL, "email-templates", albatrossTkn,
				null, false, emailTemplatePage);
		response.then().statusCode(200);
	}

	public Response createActivityTemplate(int relatedToTypeId, int activityTypeId, String albatrossURL,
			String albatrossTkn, boolean isShared, String nameSuffix) {
		New_activity_templatePage newTemplate = new New_activity_templatePage();
		newTemplate.setName("Auto Template " + nameSuffix);
		newTemplate.setTemplateBody("Auto-body " + nameSuffix);
		newTemplate.setActivityType(activityTypeId);
		newTemplate.setRelatedToTypeId(relatedToTypeId);
		newTemplate.setIsShared(isShared ? "1" : "0");

		ActivityTemplatePage payload = new ActivityTemplatePage();
		payload.setActivityTemplate(newTemplate);

		Response response = RestClient.doPost("JSON", albatrossURL, "activity-templates", albatrossTkn, null, true,
				payload);
		response.then().statusCode(200);
		return response;
	}

	public void enableMergeDuplicate(int ownerAccountID, String albatrossURL, String albatrossAuthToken,
			String entityName) {
		DuplicateMergeSetting duplicateMergeSetting = new DuplicateMergeSetting();
		duplicateMergeSetting.setId(ownerAccountID);

		switch (entityName) {
			case "contacts":
				duplicateMergeSetting.setKey("allowduplicatecontacts");
				break;
			case "companies":
				duplicateMergeSetting.setKey("allowduplicatecompanies");
				break;
			case "candidates":
				duplicateMergeSetting.setKey("allowduplicatecandidates");
				break;
			default:
				throw new IllegalArgumentException("Invalid entity name: " + entityName);
		}
		duplicateMergeSetting.setTableFlag("account");
		duplicateMergeSetting.setValue("0");

		Response response = RestClient.doPost("JSON", albatrossURL, "/global/update-fields", albatrossAuthToken, null,
				true, duplicateMergeSetting);
		response.then().statusCode(200);
	}

	public void uploadCallLogRecording(int updatedByUserId,int callLogId, String baseURL, String accountAPIKey) {
		RestAssured.baseURI = baseURL;
		File wavFile = new File(System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/sampleWav.wav");

		Response response = RestAssured.given()
				.header("Authorization", "Bearer " + accountAPIKey)
				.multiPart("call_recording", wavFile)
				.multiPart("call_log_id", callLogId)
				.multiPart("generate_transcript", 0)
				.multiPart("updated_by", updatedByUserId)
				.post("call-logs/upload-call-recording");

		response.then().statusCode(200);

		response.then().body("message",
				Matchers.containsString(
						"The recording upload is in progress. You can check the status by using the following endpoint:"));
	}

	public Response albatrossSignupResponse(String albatrossURL, String email) {
		Map<String, String> emptyTokenMap = new HashMap<String, String>();
		SignUp user = new SignUp();
		user.setFirstname(faker.getCompanyName());
		user.setEmail(email);
		user.setPassword("123456");
		user.setLocale("en");
		SignUpJson signupJson = new SignUpJson();
		signupJson.setUser(user);

		Response response = RestClient.doPost("JSON", albatrossURL, "sign-up", emptyTokenMap, null, true, signupJson);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message"), "Signup Successful ");

		return response;
	}

	public Response albatrossLoginResponse(String baseURL, String emailId, String password) {
		Map<String, String> emptyTokenMap = new HashMap<String, String>();

		Login login = new Login();
		login.setEmail(emailId);
		login.setPassword(password);

		Response response = RestClient.doPost("JSON", baseURL, "login", emptyTokenMap, null, true, login);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message"), "Login Successful");

		return response;
	}

	public List<String> createTeams(List<String> userRoles, String albatrossURL, String albatrossAuthToken,
			String baseURL, String accountAPIKey) {
		Response response = getUsers(baseURL, accountAPIKey);
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
		HashMap<String, Integer> userIdMap = new HashMap<>();
		userIdMap.put("accountOwner", user.get("[0].id"));
		userIdMap.put("admin", user.get("[1].id"));
		userIdMap.put("resTeamMember", user.get("[2].id"));
		userIdMap.put("teamMember", user.get("[3].id"));

		String teamName = "team" + RandomStringUtils.randomAlphabetic(5);

		ArrayList<String> userIds = new ArrayList<>();
		for (String role : userRoles) {
			Integer userId = userIdMap.get(role);
			if (userId != null) {
				userIds.add(String.valueOf(userId));
			} else {
				throw new IllegalArgumentException("Invalid user role: " + role);
			}
		}

		AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
		Response createTeamResponse = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, teamName, userIds);
		createTeamResponse.then().statusCode(200);

		Response teams = getTeams(baseURL, accountAPIKey);
		teams.then().statusCode(200);
		JsonPath teamPath = teams.jsonPath();

		int noOfTeams = teamPath.getInt("$.size()");
		int teamId = 0;
		for (int i = 0; i < noOfTeams; i++) {
			String team = teamPath.get("[" + i + "].team_name");
			if (team.equals(teamName)) {
				teamId = teamPath.get("[" + i + "].team_id");
				break;
			}
		}
		return Arrays.asList(teamName, String.valueOf(teamId));
	}

	public List<String> getAccountDetail(String albatrossURL, String albatrossAuthToken) {
		Response response = RestClient.doGet("JSON", albatrossURL, "get-intercom-settings", albatrossAuthToken, null,
				null, true);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);

		String externalPage = jsonPath.getString("user.accountpage");
		String talentPoolPageUrl = jsonPath.getString("user.talentpoolpageurl");
		String accountName = jsonPath.getString("user.account");
		return Arrays.asList(externalPage, talentPoolPageUrl, accountName);
	}

	public int generateUniqueColumnId() {
		synchronized (commanFunction.class) {
			Set<Integer> uniqueNums = ConcurrentHashMap.newKeySet();
			Random rand = new Random();
			int y;
			do {
				int lowerBound = 1;
				int upperBound = 100;
				y = rand.nextInt(upperBound - lowerBound) + lowerBound;
			} while (uniqueNums.contains(y));
			uniqueNums.add(y);
			return y;
		}
	}

	public Response createCustomFieldsResponse(String albatross_url, String authTokenMap, String entityName,
			String customFieldName, String customFieldType, String defaultOptions) {
		HashMap<String, String> tokn = new HashMap<>();
		tokn.put("Authorization", "Bearer " + authTokenMap);

		int x = 0;
		if (entityName.equalsIgnoreCase("candidate")) {
			x = 5;
		} else if (entityName.equalsIgnoreCase("job")) {
			x = 4;
		} else if (entityName.equalsIgnoreCase("company")) {
			x = 3;
		} else if (entityName.equalsIgnoreCase("contact")) {
			x = 2;
		} else if (entityName.equalsIgnoreCase("deal")) {
			x = 11;
		} else if (entityName.equalsIgnoreCase("placement")) {
			x = 15;
		} else if (entityName.equalsIgnoreCase("invoice")) {
            x = 16;
        }
		ExtraField ef = new ExtraField();
		ef.setEntitytypeid(x);
		ef.setExtrafieldname(customFieldName);
		ef.setExtrafieldtype(customFieldType);
		ef.setColumnid(generateUniqueColumnId());
		ef.setDefaultvalue(null);

		if (customFieldType.equalsIgnoreCase("dropdown") || customFieldType.equalsIgnoreCase("multiselect")) {
			List<DefaultOptionsValue> optionsList = new ArrayList<>();
			String[] options = defaultOptions.split(",");
			for (int i = 0; i < options.length; i++) {
				DefaultOptionsValue option = new DefaultOptionsValue();
				option.setLabel(options[i].trim());
				option.setSequence_no(i + 1);
				option.setTempId(UUID.randomUUID().toString());
				optionsList.add(option);
			}
			ef.setDefaultoptionsvalue(optionsList);
		}

		CustomFieldAlbatross cf = new CustomFieldAlbatross();
		cf.setCustumField(ef);
		Response response = null;
		for (int attempt = 0; attempt < 3; attempt++) {
			response = RestClient.doPost("JSON", albatross_url, "custom-fields", tokn, null, false, cf);
			if (response.getStatusCode() == 200) {
				break;
			}
			if (attempt < 2) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		Assert.assertEquals(response.getStatusCode(), 200);

		return response;
	}

	public Response createCustomFieldsResponse(String albatross_url, String authTokenMap, String entityName,
		String customFieldName, String customFieldType, String defaultOptions, int columnId) {
	HashMap<String, String> tokn = new HashMap<>();
		tokn.put("Authorization", "Bearer " + authTokenMap);

		int x = 0;
		if (entityName.equalsIgnoreCase("candidate")) {
			x = 5;
		} else if (entityName.equalsIgnoreCase("job")) {
			x = 4;
		} else if (entityName.equalsIgnoreCase("company")) {
			x = 3;
		} else if (entityName.equalsIgnoreCase("contact")) {
			x = 2;
		} else if (entityName.equalsIgnoreCase("deal")) {
			x = 11;
		} else if (entityName.equalsIgnoreCase("invoice")) {
            x = 16;
        }
		ExtraField ef = new ExtraField();
		ef.setEntitytypeid(x);
		ef.setExtrafieldname(customFieldName);
		ef.setExtrafieldtype(customFieldType);
		ef.setColumnid(columnId);
		ef.setDefaultvalue(null);

		if (customFieldType.equalsIgnoreCase("dropdown") || customFieldType.equalsIgnoreCase("multiselect")) {
			List<DefaultOptionsValue> optionsList = new ArrayList<>();
			String[] options = defaultOptions.split(",");
			for (int i = 0; i < options.length; i++) {
				DefaultOptionsValue option = new DefaultOptionsValue();
				option.setLabel(options[i].trim());
				option.setSequence_no(i + 1);
				option.setTempId(UUID.randomUUID().toString());
				optionsList.add(option);
			}
			ef.setDefaultoptionsvalue(optionsList);
		}

		CustomFieldAlbatross cf = new CustomFieldAlbatross();
		cf.setCustumField(ef);
		Response response = null;
		for (int attempt = 0; attempt < 3; attempt++) {
			response = RestClient.doPost("JSON", albatross_url, "custom-fields", tokn, null, false, cf);
			if (response.getStatusCode() == 200) {
				break;
			}
			if (attempt < 2) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		Assert.assertEquals(response.getStatusCode(), 200);

		return response;
	}

	public Response createNestedDependency(String albatrossURL, String authTokenMap, String entityTypeId,
			int parentCustomFieldId, int childCustomFieldId, int parentOptionId, int childOptionId1,
			int childOptionId2) {
		HashMap<String, String> tokn = new HashMap<>();
		tokn.put("Authorization", "Bearer " + authTokenMap);

		List<NestedCustomFieldPojo.Mapping> mappingsList = new ArrayList<>();

		NestedCustomFieldPojo.Mapping mapping1 = new NestedCustomFieldPojo.Mapping();
		mapping1.setParent_value_id(parentOptionId);
		mapping1.setChild_value_id(childOptionId1);
		mappingsList.add(mapping1);

		NestedCustomFieldPojo.Mapping mapping2 = new NestedCustomFieldPojo.Mapping();

		mapping2.setParent_value_id(parentOptionId);
		mapping2.setChild_value_id(childOptionId2);
		mappingsList.add(mapping2);

		NestedCustomFieldPojo nestedCustomField = new NestedCustomFieldPojo();
		nestedCustomField.setEntity(entityTypeId);
		nestedCustomField.setLevel(1);
		nestedCustomField.setDependency_id(String.valueOf(parentCustomFieldId));
		nestedCustomField.setParent_id(parentCustomFieldId);
		nestedCustomField.setChild_id(childCustomFieldId);
		nestedCustomField.setMappings(mappingsList);

		Response response = RestClient.doPost("JSON", albatrossURL, "nested-custom-fields/store", tokn, null, false,
				nestedCustomField);
		Assert.assertEquals(response.getStatusCode(), 200);

		return response;
	}

	public Response createDealWithAllFields(String baseURL, String apiAuthToken, String albatrossURL,
			String albatrossAuthToken) {
		String companySlug = createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath().get("slug");
		String contactSlug = createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath().get("slug");
		String jobSlug = createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath().get("slug");
		String candidateSlug = createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath().get("slug");
		JsonPath usersResponse = getUsers(baseURL, apiAuthToken).jsonPath();
		int accountOwnerid = usersResponse.get("[0].id");
		int adminId = usersResponse.get("[1].id");
		int teamMember = usersResponse.get("[3].id");

		ArrayList<String> userId1 = new ArrayList<String>();
		userId1.add(String.valueOf(accountOwnerid));
		userId1.add(String.valueOf(teamMember));

		AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
		Response response1 = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", userId1);
		response1.then().statusCode(200);
		Response team1 = getTeams(baseURL, apiAuthToken);
		team1.then().statusCode(200);
		int teamId = team1.jsonPath().get("[0].team_id");

		TeammatesCollaborator teammatesCollaborator = new TeammatesCollaborator();
		teammatesCollaborator.setTeammate_id(adminId);
		teammatesCollaborator.setSplit_percentage(50);

		TeamsCollaborator teamsCollaborator = new TeamsCollaborator();
		teamsCollaborator.setTeam_id(teamId);
		teamsCollaborator.setSplit_percentage(100);

		DealSplit dealSplit = new DealSplit();
		dealSplit.setTeammates_collaborator(new TeammatesCollaborator[] { teammatesCollaborator });
		dealSplit.setTeams_collaborator(new TeamsCollaborator[] { teamsCollaborator });
		dealSplit.setSplit_type("equal");

		JsonPath jsonDealStages = getAllDealStages(baseURL, apiAuthToken).jsonPath();
		dealStage = jsonDealStages.getString("id[0]");

		Deal deal = new Deal();
		deal.setName(dealName);
		deal.setDeal_value(dealValue);
		deal.setClose_date(dealDate);
		deal.setDeal_type(dealType);
		deal.setCompany_slug(companySlug);
		deal.setJob_slug(jobSlug);
		deal.setContact_slugs(contactSlug);
		deal.setCandidate_slug(candidateSlug);
		deal.setDeal_stage(dealStage);
		deal.setDeal_split(dealSplit);

		Response response = RestClient.doPost("JSON", baseURL, "deals", apiAuthToken, null, true, deal);

		response.then().statusCode(200);
		response.then().body("name", Matchers.containsString(dealName));
		response.then().body("deal_value", Matchers.is(dealValue));
		response.then().body("deal_stage.id", Matchers.is(Integer.parseInt(dealStage)));
		response.then().body("company_slug", Matchers.containsString(companySlug));
		Assert.assertEquals(response.jsonPath().getInt("archived"), 0, "Archive Not Matching!");

		return response;
	}

	public Response getEntityDetail(String albatrossURL, String albatrossAuthToken, String Slug, String entityType) {
		HashMap<String, String> tokn = new HashMap<>();
		tokn.put("Authorization", "Bearer " + albatrossAuthToken);
	
		String basePath = null;
		switch (entityType.toLowerCase()) {
			case "candidate":
				basePath = "candidates/" + Slug + "/get";
				break;
			case "job":
				basePath = "jobs/" + Slug + "/get";
				break;
			case "company":
				basePath = "companies/" + Slug + "/get";
				break;
			case "contact":
				basePath = "contacts/" + Slug + "/get";
				break;
			case "deal":
				basePath = "deals/" + Slug + "/get";
				break;
			default:
				throw new IllegalArgumentException("Unsupported entityType: " + entityType);
		}
	
		EntityDetail entityDetail = new EntityDetail();
		entityDetail.setSlug(Slug);
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, tokn, null, true, entityDetail);
		Assert.assertEquals(response.getStatusCode(), 200);
		return response;
	}

	public JsonPath assignJobToCandidate(String baseURL, Object authToken, String candidateSlug, String jobSlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		Map<String, String> pathParameters = new HashMap<>();
		pathParameters.put("candidate", candidateSlug);

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("job_slug", jobSlug);
		String basePath = "candidates/{candidate}/assign";
		Response response = RestClient.doPost1("JSON", baseURL, basePath, authTokenMap, queryParameters, pathParameters, true, null);
		System.out.println(response.prettyPrint());
		Assert.assertEquals(response.getStatusCode(), 200);
		return response.jsonPath();
	}
	

	public Response uploadCallLogRecording(String baseURL, String apiAuthToken,int generateTranscript,int callLogId) {

        // Get user ID for updated_by parameter
        Response usersResponse = getUsers(baseURL, apiAuthToken);
        usersResponse.then().statusCode(200);
        int updatedByUserId = usersResponse.jsonPath().get("[0].id");

        File wavFile = new File(System.getProperty("user.dir") + "/src/main/java/io/rcrm/api/testdata/sampleWav.wav");

        if (!wavFile.exists()) {
            throw new RuntimeException("Test file not found: " + wavFile.getAbsolutePath());
        }

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + apiAuthToken)
                .multiPart("call_recording", wavFile)
                .multiPart("call_log_id", callLogId)
                .multiPart("generate_transcript", generateTranscript)
                .multiPart("updated_by", updatedByUserId)
                .post("call-logs/upload-call-recording");

        response.then().statusCode(200);

		return response;
    }

    public String encryptAccountId(String accountId) {
        byte[] keyBytes = cipherKey.substring(0, 16).getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = cipherIv.getBytes();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        try {
            Cipher cipher = Cipher.getInstance(cipherEmail);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            byte[] encryptedData = cipher.doFinal(accountId.getBytes(StandardCharsets.UTF_8));

            String base64EncryptedData = Base64.getEncoder().encodeToString(encryptedData);
            base64EncryptedData = Base64.getEncoder()
                    .encodeToString(base64EncryptedData.getBytes(StandardCharsets.UTF_8));
            return base64EncryptedData;
        }
        catch (Exception ex) {
            return null;
        }
    }

	public Response assignCandidateToJobBySlug(String baseURL, String apiAuthToken, String candidateSlug, String jobSlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(apiAuthToken);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", candidateSlug);

		String basePath = "candidates/{candidate}/assign";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_slug", jobSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, authTokenMap, queryParameters, pathParamters,
				true, null);

		response.then().statusCode(200);
		response.then().body("candidate_slug", Matchers.is(candidateSlug));
		response.then().body("job_slug", Matchers.is(jobSlug));

		return response;
	}

	public Response createPlacement(String baseURL, String apiAuthToken, String albatrossURL, String albatrossTknA, String invoiceServiceURL) {
		AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

		Response resp = createNewCandidateWithMandatoryFields(baseURL, apiAuthToken);
        String candidateSlug = resp.jsonPath().getString("slug");
        int candidateId = allCrudFunctions.getCandidateResponse(albatrossURL, albatrossTknA,candidateSlug).jsonPath().get("data.candidate.id");
        Response response1 = createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        String companySlug = response1.jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA,companySlug).jsonPath().get("data.company.id");
        Response contactResponse = createNewContact_POST(baseURL, apiAuthToken, companySlug);
        String contactSlug = contactResponse.jsonPath().getString("slug");
        int contactId = Integer.parseInt(allCrudFunctions.getContactResponse(albatrossURL, albatrossTknA,contactSlug).jsonPath().get("data.contact.id"));
        Response response2 = createNewJob(baseURL, apiAuthToken, companySlug, contactSlug);
        String jobSlug = response2.jsonPath().getString("slug");
        int jobId = allCrudFunctions.getJobResponse(albatrossURL, albatrossTknA,jobSlug).jsonPath().get("data.job.id");
        Response dealResponse = createNewDealWithMandatoryFields(baseURL, apiAuthToken, companySlug, contactSlug, jobSlug);
        String dealSlug = dealResponse.jsonPath().getString("slug");
        int dealId = allCrudFunctions.getDealResponse(albatrossURL, albatrossTknA,dealSlug).jsonPath().get("data.deal.id");

        assignCandidateToJobBySlug(baseURL, apiAuthToken, candidateSlug, jobSlug);
        Map<String, List<Integer>> associationIds = new HashMap<>();
        associationIds.put("2", Arrays.asList(contactId));
        associationIds.put("11", Arrays.asList(dealId));

        CreatePlacement placementRequest = new CreatePlacement(companyId, jobId, candidateId, placementFaker.getCurrencyId(), associationIds);

        Response response = RestClient.doPost1( "JSON", invoiceServiceURL, "placements" , albatrossTknA,  null, null,
            true, placementRequest );

        response.then().statusCode(200);

		return response;
	}

	public Response assignMultipleCandidatesToJob(String baseURL, String apiAuthToken, ArrayList<Integer> candidateIds, int jobId) {
		Map<String, String> authTokenMap = getAuthTokenMap(apiAuthToken);
		String basePath = "candidates/assign";
		JSONObject job = new JSONObject();
		job.put("id", jobId);
		job.put("checked", true);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("candidates", candidateIds);
		jsonObject.put("jobs", Arrays.asList(job));
		Response response = RestClient.doPost1("JSON", baseURL, basePath, authTokenMap, null, null, true, jsonObject);
		response.then().statusCode(200);
		response.then().body("message", Matchers.containsString("Candidate(s) Assigned To Job"));
		return response;
	}

	public Response createNotesByPayload(String baseURL, String apiKey, JSONObject payload) {
		String basePath = "notes";
		Response response = RestClient.doPost("JSON",baseURL,basePath,apiKey,null,true,payload);
		Assert.assertEquals(response.getStatusCode(),200);
		return response;
	}

	public Response createCallLogByPayload(String baseURL, String apiKey, JSONObject payload) {
		String basePath = "call-logs";
		Response response = RestClient.doPost("JSON",baseURL,basePath,apiKey,null,true,payload);
		Assert.assertEquals(response.getStatusCode(),200);
		return response;
	}

	public Response createCustomNoteType(String albatrossURL, String authToken, String noteTypeLabel, boolean isDefault) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JSONObject payload = new JSONObject();
		JSONArray customizedNoteTypes = new JSONArray();

		JSONObject noteType = new JSONObject();
		noteType.put("label", noteTypeLabel);
		noteType.put("default", isDefault ? 1 : 0);

		customizedNoteTypes.put(noteType);
		payload.put("customizedNoteTypes", customizedNoteTypes);

		String basePath = "notes/customize-note-types";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, payload);

		Assert.assertEquals(response.getStatusCode(), 200, "Failed to create custom note type");
		return response;
	}

	public Response createCustomCallType(String albatrossURL, String authToken, String callTypeLabel, boolean isDefault) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String basePath = "call-logs/customize-call-types";

		JSONObject payload = new JSONObject();
		JSONArray customizedCallTypes = new JSONArray();
		JSONObject callType = new JSONObject();
		callType.put("label", callTypeLabel);
		callType.put("default", isDefault ? 1 : 0);
		customizedCallTypes.put(callType);
		payload.put("customizedCallTypes", customizedCallTypes);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, payload);
		Assert.assertEquals(response.getStatusCode(), 200, "Failed to create custom call type");
		return response;
	}

	public Response getNoteTypes(String albatrossURL, String authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		String basePath = "notes/get-note-types";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, authTokenMap, null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch note types");
		return response;
	}

	public Response getCallTypes(String albatrossURL, String authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String basePath = "call-logs/get-call-types";
		Response response = RestClient.doGet("JSON", albatrossURL, basePath, authTokenMap, null, null, true);
		Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch call types");
		return response;
	}

	public Response getActivityBySlug(String albatrossURL, String authToken, String entitySlug, String activityType) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String basePath = "expand-activity/get-activity-data";

		switch (activityType) {
			case "notes":
			case "call_log":
				activityType = "0";
				break;
			case "tasks":
				activityType = "1";
				break;
			case "appointments":
				activityType = "2";
				break;
			default:
				throw new IllegalArgumentException("Invalid activity type: " + activityType);
		}

		JSONObject payload = new JSONObject();
		payload.put("type", activityType);
		payload.put("page", "detailspage");
		payload.put("relatedToSlug", entitySlug);
		payload.put("relatedtotypeid", 5);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, payload);
		Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch activity data");
		return response;
	}

	public Response getCandidateNotesBySlug(String albatrossURL, String authToken, String entitySlug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String basePath = "expand-activity/get-activity-data";

		JSONObject payload = new JSONObject();
		payload.put("type", "0");
		payload.put("page", "detailspage");
		payload.put("relatedToSlug", entitySlug);
		payload.put("relatedtotypeid", 5);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, payload);
		Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch activity data");
		return response;
	}

	public int getCandidateIdBySlug(String albatross_url, String authToken, String slug) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("candidate", slug);
		String basePath = "candidates/{candidate}/get";

		Response response = RestClient.doPost1("JSON", albatross_url, basePath, authTokenMap, null, pathParamters, true,
				null);
		JsonPath jp = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		String candidateId = jp.getString("data.candidate.id");
		return Integer.parseInt(candidateId);
	}

	public int getContactIdBySlug(String albatrossUrl, String authToken, String slug) {
        Map<String, String> authTokenMap = getAuthTokenMap(authToken);
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("contact", slug);
        String basePath = "contacts/{contact}";

        Response response = RestClient.doGet("JSON", albatrossUrl, basePath, authTokenMap, null, pathParamters, true);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200);
        String contactId = jp.getString("data.contact.id");
        return Integer.parseInt(contactId);
    }

    public int getCompanyIdBySlug(String albatross_url, String authToken, String slug) {
        Map<String, String> authTokenMap = getAuthTokenMap(authToken);
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("company", slug);
        String basePath = "companies/{company}";
        Response response = RestClient.doGet("JSON", albatross_url, basePath, authTokenMap, null, pathParamters, true);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200);
        String companyId = jp.getString("data.company.id");
        return Integer.parseInt(companyId);
    }

	public int getDealIdBySlug(String albatross_url, String authToken, String slug) {
        Map<String, String> authTokenMap = getAuthTokenMap(authToken);
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("deal", slug);
        String basePath = "deals/{deal}";
        Response response = RestClient.doGet("JSON", albatross_url, basePath, authTokenMap, null, pathParamters, true);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200);
        String dealId = jp.getString("data.deal.id");
        return Integer.parseInt(dealId);
    }

	public int getJobIdBySlug(String albatross_url, String authToken, String slug) {
        Map<String, String> authTokenMap = getAuthTokenMap(authToken);
        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("job", slug);
        String basePath = "jobs/{job}/get";
        Response response = RestClient.doPost1("JSON", albatross_url, basePath, authTokenMap, null, pathParamters, true, null);
        JsonPath jp = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 200);
        String jobId = jp.getString("data.job.id");
        return Integer.parseInt(jobId);
    }

	public Response addBusinessDetails(String invoiceServiceURL, String albatrossTknA) {
		String invoicePrefix = fakerInvoice.invoicePrefix();
		JSONObject invoiceObject = new JSONObject();
		invoiceObject.put("companyName", companyName);
		invoiceObject.put("prefix", invoicePrefix);
        invoiceObject.put("number", fakerInvoice.invoiceNumber());
		Response response = RestClient.doPost1("JSON", invoiceServiceURL, "invoice-settings", albatrossTknA, null, null, true, invoiceObject);
		
		response.then().statusCode(200);
		response.then().body("data.invoiceIdPrefix", Matchers.containsString(invoicePrefix));
		response.then().body("meta.message", Matchers.containsString("Invoice Setting Saved Sucessfully"));
		return response;	
	}

	public Response createInvoice(String invoiceServiceURL, String albatrossTkn, int companyId) {
        Response response1 = addBusinessDetails(invoiceServiceURL, albatrossTkn);
        String invoicePrefix = response1.jsonPath().getString("data.invoiceIdPrefix");
        String invoiceNumber = response1.jsonPath().get("data.invoiceIdNumber");
        int accountId = response1.jsonPath().get("data.accountId");
        String fileName = invoicePrefix + "-" + String.valueOf(invoiceNumber);
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("fileName", fileName + ".pdf");
        pathParams.put("acl", "private");
        pathParams.put("key", String.valueOf(accountId) + "/invoices/" + fileName + "/" + fileName + ".pdf");
        Response response2 = RestClient.doGet("JSON", invoiceServiceURL, "invoice/files/generate-upload-url", albatrossTkn, pathParams, null, true);
        response2.then().statusCode(200);
        String s3Key = response2.jsonPath().getString("data.key");
		int templateId = getInvoiceTemplateId(invoiceServiceURL, albatrossTkn, "Full-Time Job");
		Invoice invoice = createInvoicePayload(companyId, invoicePrefix, s3Key, templateId);
		Response response = RestClient.doPost1("JSON", invoiceServiceURL, "invoices", albatrossTkn, null, null, true, invoice);
		response.then().statusCode(200);
		response.then().body("meta.message", Matchers.containsString("Invoice Created Successfully"));
		return response;
	}

	public Invoice createInvoicePayload(int companyId, String invoicePrefix, String s3Key, int templateId) {
		Invoice invoice = new Invoice();
		invoice.setInvoicePrefix(invoicePrefix);
		invoice.setInvoiceNumber(fakerInvoice.invoiceNumber());
		invoice.setTemplateId(templateId);
		invoice.setCompanyId(companyId);
		invoice.setStatusId(fakerInvoice.getInvoiceStatusId());
		invoice.setCurrencyId(fakerInvoice.getCurrencyId());
		invoice.setPaidOn(String.valueOf(fakerInvoice.getPaidOn()));
		invoice.setDueDate(String.valueOf(fakerInvoice.getDueDate()));
		invoice.setIssueDate(String.valueOf(fakerInvoice.getIssueDate()));
		invoice.setTotalAmount(fakerInvoice.getTotalAmount());
		invoice.setInvoicePdf(s3Key);
		Map<String, List<String>> associations = new HashMap<>();
        associations.put("2", new ArrayList<>());
        associations.put("3", new ArrayList<>());
        associations.put("4", new ArrayList<>());
        associations.put("5", new ArrayList<>());
        associations.put("11", new ArrayList<>());
        invoice.setAssociations(associations);

		List<Map<String, Object>> invoiceItemsList = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("fieldId", 1);
        field.put("fieldValue", String.valueOf(fakerInvoice.getTotalAmount()));
        invoiceItemsList.add(field);
        List<List<Map<String, Object>>> outerArray = new ArrayList<>();
        outerArray.add(invoiceItemsList);
        JSONArray invoiceItemsArray = new JSONArray(outerArray);
        invoice.setInvoiceItems(invoiceItemsArray.toString());

        Map<String, Object> company = new HashMap<>();
        company.put("companyId", String.valueOf(companyId));
        company.put("name", "Test Company");
        invoice.setCompany(company);
		return invoice;
	}

	public Response createJobWithJson(String baseURL, Object authToken, JSONObject jobJson) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		Response response = RestClient.doPost("JSON", baseURL, "jobs", authTokenMap, null, true, jobJson);
		response.then().statusCode(200);
		return response;
	}

	public Response generateInvoiceTable(String baseURL, Object authToken, String sfdtContent,
			List<JSONObject> invoiceItems, List<JSONObject> fieldNames, int isPayAndBill) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JSONObject content = new JSONObject();
		content.put("sfdt", sfdtContent);

		JSONObject payload = new JSONObject();
		payload.put("content", content);
		payload.put("invoiceItems", invoiceItems);
		payload.put("fieldNames", fieldNames);
		payload.put("isPayAndBill", isPayAndBill);

		Response response = RestClient.doPost("JSON", baseURL, "Document/genarateInvoiceTable", authTokenMap, null, true, payload);
		response.then().statusCode(200);

		return response;
	}


	public Response generateInvoiceTableForPayBillRow(String baseURL, Object authToken, String sfdtContent,
			List<JSONObject> invoiceItemRows, List<JSONObject> fieldNames, int isPayAndBill) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JSONObject content = new JSONObject();
		content.put("sfdt", sfdtContent);

		JSONObject payload = new JSONObject();
		payload.put("content", sfdtContent);
		payload.put("invoiceItems", invoiceItemRows);
		payload.put("fieldNames", fieldNames);
		payload.put("isPayAndBill", isPayAndBill);

		return RestClient.doPost("JSON", baseURL, "Document/genarateInvoiceTable", authTokenMap, null, true, payload);
	}

	public Response generateInvoiceTableWithDefaultValues(String baseURL, Object authToken) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		String sfdtContent = "UEsDBAoAAAAIAKQCKlt5HPObVwcAAFR6AAAEAAAAc2ZkdO1dW3OjNhT+Kx711eNBXI3f0mTcdqbt7Gz7ts1kuAjDLAYXcLxZj//7Sjr4CnhJfEFOlIccQAI+znd0pHMkzBKlsyKaRt/JP4FfoFGRzUkf5cRDoy9LROUsQ6Mlmi3QyLCNgWrb9tCybNU2h8M+moVoNNTxwFYURdV1rCumavRRPEUjpY8yEAUIlwpL7aPQ57sBFZrZR34wQyNMZUpmUC8CQRGgv8nikzMhqI9IEqARPTtgkl07WkvCZRQk/DIRATmbJDm9wF3muJFHz0+8NM55Cfl/wWXsFh4/FUq+PK7oTflTZ/w/aACOzAKmBNfPciYLCnRJa8cFyGwC0i33QxDPTLBqEYVk4IGlq4aiaIqBLcOiygvYcXr/ElecF3w3LxL25Gk2dWLEzg7K63gcRJB/p4qg9QO6i+6dOHKziNYLmL5+waqm6w/jMQI9Bk6cUzrpOWwPM/0FbGvnvMgPqAaxYjBEgbN/TaptcnBk7wBFFYGyttjgLkJgK2LKFPpvTq3TpfsSpoQpYZ4FprvuKURzR4D3j+Q5jTyCVo8MsmjeG5sCe29TMCqXEXD5FPkrUfls0VwvTaJUjFSMVMz5FHOJGEA5TTW4e9XUd/9Y6xZZ2efn+Zz0HpyCVIZT2m2M+jThul6m0iefqpR3vY+0oMwH7DUDFLJYe8eqF2DUIQgW1q9bSruqvDG1q+q2v6rvt686b101bF3zuWVNWjXnUN0Jp388HN+N76EV8SNkOite6F4B12W5GWwaA0UdWrplGEPD1iyDHS94ysRrKGd5D8i9ZIty49mBS3oRlwK4vgNPowo5chYWJLTjh41fPFOfr9x8Hy92uL30pdv9qG4Xc74z4DvkxY6bfeUbITSy0M8gRS6t4ZLWMHH5/oSTw1iZZBT8lxpOq4cYh24sG61ojbYpZC2c3akqNnFlDHSF/9mGZlq6psK0Vs1hF07i7X47f+XMg90u8V/HjUnvtyzymZv3Sc7AMVwFSOoV6BFVxuRNMbnsBYXtBTVbHZisTZhY0QxsmPudYE3xTYQe7y1TJxUhFVGvCE2r9BdSMdJCpCKkIiqK0PBAt+vGlgMW3Mp00CXSQW4Ux8R/CrJ0+uSl05mTvDzF6SSV6SGxB8aqotJWoVs2Vg3bMg1T3R8Z15W/Mj2kGrTvZqsvLd00hqqiyXSRWOmiavRTZV1miwTOFinvKTv0YYcuH3uNjj1gy/LPMgNr3sIMrGiLGO+m6TwpemwidrOe0ZtnGUm8l1VvWaSFEz85vNIZFzmewrpcjXXGJQnirkO4vOn/SmOX3phGLgKsPviZMg41cLM5h2s96FIkb3EkWE6cKVl9IAO8FV4c389InktqxKFmnpNMthfBSCFTJ4pXcv3ezRA2C9NErt7rMrVT/h1Nz2rmANv0z7T1oaqrh9nZavE7XTL94eOTIpXRiezVr9hJFKmMTYRmRUYmghEj4xLhKJFRyU3RJWOSjxiTyPeJBF8gUqW0ckSuDxGzwe6uD8Hv5+2h9zNvLJUhlXGVFQWlB3DpuSZtvSDeqpBrjVIPbnuOoMX9CrbAHrxc6MJ91+f7z381/nzcT4xCweh6a3Tk8PjqvW058jXU6s/L7ox8a4obRr54k40/w8hXNdvz3Fy3SnRzXfcV1xWO6iaoleFv//zOV3R3K46rW95tlxpKZyednXR2l3V2m1i/wmtzZM+9AgSLfFRce+OB0cDUW88G7t56tnvSvS/I7zFISjOi57c/zk57P5ol2OQFTsgEJPM4hlwAbEE2AJ8c871yFuTskx6Xik9wU3zCe8MQnvJxZzlF+Z7A3hsDtT/IOS+ZAgLhraEQvjKw7tVxzZM43vbKb3ios0y9ZAdTMOU7Ty4Yu5PXRfxpvLHUEv5Xdtf19iLZbC+8dcS3taIgLxEVQQwbfrGAj0KUX2Yg5ccewmdIc+WlDKZw/xkIPyymgCQIACCbxS2peKH+FVBuXdsmr8eN/IXb3J6pl5c/Ta97rYXK5Nv2Fqxtsjv+Thw/SiY9fK6b/pRMNqzcZXOgWDY2TdNQLEtVbN3apxe3/A3z3p/RhI4z1sNZdWzotllOhjp11fbteud4zWH4MseWnqOa7N2HTgbqxJ1DfiCBM4+L3icncyaZMwt745QlGUrQDcV74FeHT6hezVZeZypqk6logpqK2mwqmnCmsgatCcq+1jLiPlAlHmuWqXXBvtbMfgeQW7KvC8q+XmU/2uR4RWz7eh37nUFuyb4hKPvG8eBAGNKN2iYvJtemoFybr+K6O/duvpLrLr26JSjX1tu8enekW2/16l2yPxSU/WHTiI5nu44oU+N/XfA/bB7TdQK6pQXYglqAfaz9C24K9nFXILRNkOw8BlGftatmpqpKLVGsQY3TtOgeVIliDQoKezx/C9DUPZMoS9bVdzLn68pu9Sr8/N0k+yPDxlYNUeQxSG8KMit3v4GMppMcoLNv0S5RfvrM9vEM/H6+fP/rQlqZzfY6RaGvUSRXQvHIXkZAROq/Q/2vfgBQSwECFAAKAAAACACkAipbeRzzm1cHAABUegAABAAAAAAAAAAAAAAAAAAAAAAAc2ZkdFBLBQYAAAAAAQABADIAAAB5BwAAAAA=";

		List<JSONObject> invoiceItems = new ArrayList<>();
		JSONObject invoiceItem = new JSONObject();
		JSONObject fields = new JSONObject();
		fields.put("1", "{Amount}");
		invoiceItem.put("fields", fields);
		invoiceItems.add(invoiceItem);

		List<JSONObject> fieldNames = new ArrayList<>();
		JSONObject fieldName = new JSONObject();
		fieldName.put("field", "1");
		fieldName.put("label", "Amount");
		fieldName.put("type", "text");
		fieldNames.add(fieldName);

		int isPayAndBill = 0;

		JSONObject content = new JSONObject();
		content.put("sfdt", sfdtContent);

		JSONObject payload = new JSONObject();
		payload.put("content", content.toString());
		payload.put("invoiceItems", invoiceItems);
		payload.put("fieldNames", fieldNames);
		payload.put("isPayAndBill", isPayAndBill);

		Response response = RestClient.doPost("JSON", baseURL, "Document/genarateInvoiceTable", authTokenMap, null, true, payload);
		response.then().statusCode(200);

		return response;
	}

	public InvoiceTemplate createInvoiceTemplatePayload(String templateName, List<Integer> userIds, List<Integer> teamIds, String dueIn, String sfdtContent, List<JSONObject> templateItems) {
		InvoiceTemplate invoiceTemplate = new InvoiceTemplate();
		invoiceTemplate.setTemplateName(templateName);

		JSONObject sharedWith = new JSONObject();
		sharedWith.put("users", userIds);
		sharedWith.put("teams", teamIds);
		invoiceTemplate.setSharedWith(sharedWith.toString());
		JSONObject dueInObject = new JSONObject();
		dueInObject.put("dueIn", dueIn);
		invoiceTemplate.setDueIn(dueInObject.toString());

		JSONObject templateTheme = new JSONObject();
		templateTheme.put("sfdt", sfdtContent);
		invoiceTemplate.setTemplateTheme(templateTheme.toString());

		invoiceTemplate.setTemplateItems(templateItems.toString());

		return invoiceTemplate;
	}

	public Response createInvoiceTemplate(String invoiceServiceURL, String albatrossTkn, String baseURL, String apiKeyA, String syncFunctionURL) {
		Response usersResponse = getUsers(baseURL, apiKeyA);
        usersResponse.then().statusCode(200);
        JsonPath usersJsonPath = usersResponse.jsonPath();
        int userId = usersJsonPath.get("[0].id");

        Response response = generateInvoiceTableWithDefaultValues(syncFunctionURL, albatrossTkn);
        response.then().statusCode(200);
        String sfdtContent = response.jsonPath().get("sfdt");

        List<JSONObject> templateItems = new ArrayList<>();
        JSONObject templateItem = new JSONObject();
        templateItem.put("formula", "");
        templateItem.put("field_id", 1);
        templateItem.put("field_type", 4);
        templateItem.put("field_label", "Amount");
        templateItem.put("default_field_label", "Amount");
        templateItem.put("sequence_number", 1);
        templateItems.add(templateItem);

        InvoiceTemplate invoiceTemplate = createInvoiceTemplatePayload(fakerInvoice.getInvoiceTemplateName(), Arrays.asList(userId), new ArrayList<>(), "7 Days", sfdtContent, templateItems);
        Response createResponse = RestClient.doPost("JSON", invoiceServiceURL, "invoices/templates", albatrossTkn, null, true, invoiceTemplate);
        createResponse.then().statusCode(201);
		return createResponse;
	}

	public Map<String, String> createUserMap(String baseURL, String apiKey) {
		Map<String, String> userMap = new HashMap<>();
		Response response = getUsers(baseURL, apiKey);
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
		userMap.put("owner", user.get("[0].id").toString());
		userMap.put("admin", user.get("[1].id").toString());
		userMap.put("restricted", user.get("[2].id").toString());
		userMap.put("teamMember", user.get("[3].id").toString());
		userMap.put("restrictedTeamMember", user.get("[2].id").toString());
		return userMap;
	}

	public String processFilterValue(String filterValue, Map<String, String> userMap) {
		if (filterValue == null || filterValue.isEmpty()) {
			return filterValue;
		}

		String processedValue = filterValue;
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^}]+)\\}\\}");
		java.util.regex.Matcher matcher = pattern.matcher(filterValue);

		while (matcher.find()) {
			String placeholder = matcher.group(0);
			String fieldKey = matcher.group(1);

			String actualValue = userMap.get(fieldKey);
			if (actualValue != null) {
				processedValue = processedValue.replace(placeholder, actualValue);
			} else {
				throw new IllegalArgumentException("Unable to process the payload, No value found for placeholder: " + placeholder);
			}
		}

		return processedValue;
	}

	public void validateCreatedByFilteredData(JSONArray filteredData, String filterType, String filterValue,
			String fieldName, String dbField, String expectedResult, String entityName, Map<String, String> userMap) {
		String processedFilterValue = processFilterValue(filterValue, userMap);
		List<String> expectedOwnerIds = Arrays.asList(processedFilterValue.split(","));

		expectedOwnerIds = expectedOwnerIds.stream()
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.collect(Collectors.toList());

		if (expectedOwnerIds.isEmpty()) {
			Assert.fail("No expected owner IDs found. Filter value: " + filterValue + ", processed: " + processedFilterValue);
		}

		for (int i = 0; i < filteredData.length(); i++) {
			JSONObject item = filteredData.getJSONObject(i);
			String ownerId = null;
			if (item.has(dbField)) {
				ownerId = String.valueOf(item.get(dbField)).trim();
			} else {
				Assert.fail(entityName + " at index " + i + " missing field: " + dbField + ". Available fields: " + item.keySet());
			}

			if (ownerId == null || ownerId.isEmpty() || ownerId.equals("null")) {
				Assert.fail(entityName + " at index " + i + " has invalid/null userId. " + entityName + ": " + item.toString());
			}

			if (!expectedOwnerIds.contains(ownerId)) {
				Assert.fail(entityName + " '" + item.optString("name", "N/A") + "' at index " + i +
					" has userId '" + ownerId + "' which is not in expected list: " + expectedOwnerIds +
					". Filter: " + filterType + " with value: " + filterValue + " (processed: " + processedFilterValue + ")");
			}
		}
	}

	public void validateCreatedOnUpdatedOnFilteredData(JSONArray filteredData, String filterType, String filterValue,
			String fieldName, String dbField, String expectedResult) {
		for (int i = 0; i < filteredData.length(); i++) {
			JSONObject item = filteredData.getJSONObject(i);
			String itemDate = item.get(dbField).toString();
			LocalDate itemParsedDate = parseDate(itemDate);
			validateDateInPeriod(itemParsedDate, filterValue, fieldName, filterType);
		}
	}

	public void validateDateInPeriod(LocalDate candidateDate, String period, String fieldName, String filterType) {
		LocalDate startDate;
		LocalDate endDate;
		LocalDate now = LocalDate.now();

		switch (period) {
			case "today":
				startDate = endDate = now;
				break;
			case "yesterday":
				startDate = endDate = now.minusDays(1);
				break;
			case "this_week":
				startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
				endDate = startDate.plusDays(6);
				break;
			case "last_week":
				LocalDate lastWeekStart = now.minusDays(now.getDayOfWeek().getValue() + 6);
				startDate = lastWeekStart;
				endDate = lastWeekStart.plusDays(6);
				break;
			case "this_month":
				startDate = now.withDayOfMonth(1);
				endDate = now.withDayOfMonth(now.lengthOfMonth());
				break;
			case "last_month":
				LocalDate lastMonth = now.minusMonths(1);
				startDate = lastMonth.withDayOfMonth(1);
				endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
				break;
			case "this_quarter":
				int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
				int quarterStartMonth = (currentQuarter - 1) * 3 + 1;
				startDate = now.withMonth(quarterStartMonth).withDayOfMonth(1);
				endDate = now.withMonth(quarterStartMonth + 2).withDayOfMonth(now.withMonth(quarterStartMonth + 2).lengthOfMonth());
				break;
			case "last_quarter":
				int lastQuarter = (now.getMonthValue() - 1) / 3;
				if (lastQuarter == 0) {
					lastQuarter = 4;
					now = now.minusYears(1);
				}
				int lastQuarterStartMonth = (lastQuarter - 1) * 3 + 1;
				startDate = now.withMonth(lastQuarterStartMonth).withDayOfMonth(1);
				endDate = now.withMonth(lastQuarterStartMonth + 2).withDayOfMonth(now.withMonth(lastQuarterStartMonth + 2).lengthOfMonth());
				break;
			case "this_year":
				startDate = now.withDayOfYear(1);
				endDate = now.withDayOfYear(now.lengthOfYear());
				break;
			case "last_year":
				LocalDate lastYear = now.minusYears(1);
				startDate = lastYear.withDayOfYear(1);
				endDate = lastYear.withDayOfYear(lastYear.lengthOfYear());
				break;
			case "last_30":
				endDate = now;
				startDate = endDate.minusDays(30);
				break;
			case "last_60":
				endDate = now;
				startDate = endDate.minusDays(60);
				break;
			case "last_90":
				endDate = now;
				startDate = endDate.minusDays(90);
				break;
			case "last_365":
				endDate = now;
				startDate = endDate.minusDays(365);
				break;
			default:
				Assert.fail("Unsupported relative date period: " + period);
				return;
		}
		boolean isInPeriod = !candidateDate.isBefore(startDate) && !candidateDate.isAfter(endDate);
		Assert.assertTrue(isInPeriod,
				"Wrong candidate data for field: " + fieldName + " - candidate date " + candidateDate +
						" should be within period '" + period + "' (between " + startDate + " and " + endDate + ")");
	}

	public LocalDate parseDate(String dateStr) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			throw new IllegalArgumentException("Date string cannot be null or empty");
		}

		String trimmedDateStr = dateStr.trim();
		try {
			long epochSeconds = Long.parseLong(trimmedDateStr);
			return Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()).toLocalDate();
		} catch (NumberFormatException e) {
			// Not an epoch value, continue with date string parsing
		}

		DateTimeFormatter[] dateFormatters = {
			DateTimeFormatter.ofPattern("yyyy-MM-dd"),
			DateTimeFormatter.ofPattern("yy-MM-dd"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
			DateTimeFormatter.ofPattern("MM/dd/yyyy"),
			DateTimeFormatter.ofPattern("dd/MM/yyyy"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
			DateTimeFormatter.ISO_LOCAL_DATE,
			DateTimeFormatter.ISO_LOCAL_DATE_TIME
		};

		for (DateTimeFormatter formatter : dateFormatters) {
			try {
				return LocalDate.parse(trimmedDateStr, formatter);
			} catch (DateTimeParseException e) {
				try {
					LocalDateTime dateTime = LocalDateTime.parse(trimmedDateStr, formatter);
					return dateTime.toLocalDate();
				} catch (DateTimeParseException innerE) {
					continue;
				}
			}
		}

		throw new IllegalArgumentException("Unable to parse date: " + dateStr +
				". Supported formats: yyyy-MM-dd, yy-MM-dd, yyyy-MM-dd HH:mm:ss, MM/dd/yyyy, dd/MM/yyyy, ISO formats, or epoch seconds");
	}

	public Map<String, Map<String, String>> createTimestampScenarios() {
		Map<String, Map<String, String>> scenarios = new HashMap<>();

		// Today scenario
		String todayEpoch = String.valueOf(dateToEpochSeconds(com.qa.api.util.DateUtil.getTodayDateString("yyyy-MM-dd")));
		Map<String, String> todayTimestamps = new HashMap<>();
		todayTimestamps.put("createdOn", todayEpoch);
		todayTimestamps.put("updatedOn", todayEpoch);
		scenarios.put("today", todayTimestamps);

		// Yesterday scenario
		String yesterdayEpoch = String.valueOf(dateToEpochSeconds(com.qa.api.util.DateUtil.getYesterdayDateString("yyyy-MM-dd")));
		Map<String, String> yesterdayTimestamps = new HashMap<>();
		yesterdayTimestamps.put("createdOn", yesterdayEpoch);
		yesterdayTimestamps.put("updatedOn", yesterdayEpoch);
		scenarios.put("yesterday", yesterdayTimestamps);

		// Last week scenario
		String lastWeekEpoch = String.valueOf(dateToEpochSeconds(com.qa.api.util.DateUtil.getLastWeekDateString()));
		Map<String, String> lastWeekTimestamps = new HashMap<>();
		lastWeekTimestamps.put("createdOn", lastWeekEpoch);
		lastWeekTimestamps.put("updatedOn", lastWeekEpoch);
		scenarios.put("lastWeek", lastWeekTimestamps);

		// Last month scenario
		String lastMonthEpoch = String.valueOf(dateToEpochSeconds(com.qa.api.util.DateUtil.getLastMonthDateString()));
		Map<String, String> lastMonthTimestamps = new HashMap<>();
		lastMonthTimestamps.put("createdOn", lastMonthEpoch);
		lastMonthTimestamps.put("updatedOn", lastMonthEpoch);
		scenarios.put("lastMonth", lastMonthTimestamps);

		// Last quarter scenario
		String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(com.qa.api.util.DateUtil.getLastQuarterDateString()));
		Map<String, String> lastQuarterTimestamps = new HashMap<>();
		lastQuarterTimestamps.put("createdOn", lastQuarterEpoch);
		lastQuarterTimestamps.put("updatedOn", lastQuarterEpoch);
		scenarios.put("lastQuarter", lastQuarterTimestamps);

		// Last year scenario
		String lastYearEpoch = String.valueOf(dateToEpochSeconds(com.qa.api.util.DateUtil.getLastYearDateString()));
		Map<String, String> lastYearTimestamps = new HashMap<>();
		lastYearTimestamps.put("createdOn", lastYearEpoch);
		lastYearTimestamps.put("updatedOn", lastYearEpoch);
		scenarios.put("lastYear", lastYearTimestamps);

		return scenarios;
	}

	public long dateToEpochSeconds(String dateStr) {
		try {
			java.time.LocalDate date = java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			return date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().getEpochSecond();
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse date: " + dateStr + ". Expected format: yyyy-MM-dd", e);
		}
	}

	// ==================== CUSTOM FIELD HELPER METHODS FOR SORTING ====================

	public List<String> extractCustomFieldValues(Response response, String sortField) {
		JsonPath jp = response.jsonPath();
		List<String> values = new ArrayList<>();
		int dataSize = jp.getInt("data.size()");

		// Extract column ID from sortField (e.g., "custcolumn123" -> "123")
		String columnIdStr = sortField.replace("custcolumn", "");

		for (int i = 0; i < dataSize; i++) {
			String customFieldPath = "data[" + i + "].custom_fields";
			List<Map<String, Object>> customFields = jp.getList(customFieldPath);

			if (customFields != null && !customFields.isEmpty()) {
				for (Map<String, Object> field : customFields) {
					Object columnIdObj = field.get("columnid");
					if (columnIdObj != null && columnIdObj.toString().equals(columnIdStr)) {
						Object value = field.get("value");
						values.add(value != null ? value.toString() : "");
						break;
					}
				}
			} else {
				values.add(""); // Add empty string if no custom fields found
			}
		}
		return values;
	}

	public List<Number> extractCustomFieldNumericValues(Response response, String sortField) {
		JsonPath jp = response.jsonPath();
		List<Number> values = new ArrayList<>();
		int dataSize = jp.getInt("data.size()");

		// Extract column ID from sortField (e.g., "custcolumn123" -> "123")
		String columnIdStr = sortField.replace("custcolumn", "");

		for (int i = 0; i < dataSize; i++) {
			String customFieldPath = "data[" + i + "].custom_fields";
			List<Map<String, Object>> customFields = jp.getList(customFieldPath);

			if (customFields != null && !customFields.isEmpty()) {
				boolean found = false;
				for (Map<String, Object> field : customFields) {
					Object columnIdObj = field.get("columnid");
					if (columnIdObj != null && columnIdObj.toString().equals(columnIdStr)) {
						Object value = field.get("value");
						if (value != null) {
							try {
								values.add(Double.parseDouble(value.toString()));
							} catch (NumberFormatException e) {
								values.add(0);
							}
						} else {
							values.add(0);
						}
						found = true;
						break;
					}
				}
				if (!found) {
					values.add(0);
				}
			} else {
				values.add(0); // Add 0 if no custom fields found
			}
		}
		return values;
	}

	public List<Object> extractCustomFieldDateValues(Response response, String sortField) {
		JsonPath jp = response.jsonPath();
		List<Object> values = new ArrayList<>();
		int dataSize = jp.getInt("data.size()");

		// Extract column ID from sortField (e.g., "custcolumn123" -> "123")
		String columnIdStr = sortField.replace("custcolumn", "");

		for (int i = 0; i < dataSize; i++) {
			String customFieldPath = "data[" + i + "].custom_fields";
			List<Map<String, Object>> customFields = jp.getList(customFieldPath);

			if (customFields != null && !customFields.isEmpty()) {
				boolean found = false;
				for (Map<String, Object> field : customFields) {
					Object columnIdObj = field.get("columnid");
					if (columnIdObj != null && columnIdObj.toString().equals(columnIdStr)) {
						values.add(field.get("value"));
						found = true;
						break;
					}
				}
				if (!found) {
					values.add(null);
				}
			} else {
				values.add(null); // Add null if no custom fields found
			}
		}
		return values;
	}

	public int createCustomFieldAndGetColumnId(String albatrossURL, String authToken, String fieldType,
			String fieldName, String entityName) {
		Response response = createCustomFieldsResponse(albatrossURL, authToken, entityName, fieldName, fieldType, "");
		return response.jsonPath().getInt("data.custumField.columnid");
	}

	public int createCustomFieldWithOptionsAndGetColumnId(String albatrossURL, String authToken,
			String fieldType, String fieldName, String options, String entityName) {
		Response response = createCustomFieldsResponse(albatrossURL, authToken, entityName, fieldName, fieldType, options);
		return response.jsonPath().getInt("data.custumField.columnid");
	}

	public boolean isSortedAscendingText(List<String> list) {
		if (list == null || list.size() <= 1) return true;

		List<String> sortedList = new ArrayList<>(list);
		Collections.sort(sortedList, String.CASE_INSENSITIVE_ORDER);
		return list.equals(sortedList);
	}

	public boolean isSortedDescendingText(List<String> list) {
		if (list == null || list.size() <= 1) return true;

		List<String> sortedList = new ArrayList<>(list);
		Collections.sort(sortedList, String.CASE_INSENSITIVE_ORDER.reversed());
		return list.equals(sortedList);
	}

	public boolean isSortedAscendingNumeric(List<Number> list) {
		if (list == null || list.size() <= 1) return true;

		for (int i = 1; i < list.size(); i++) {
			if (list.get(i - 1) != null && list.get(i) != null) {
				if (list.get(i - 1).doubleValue() > list.get(i).doubleValue()) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isSortedDescendingNumeric(List<Number> list) {
		if (list == null || list.size() <= 1) return true;

		for (int i = 1; i < list.size(); i++) {
			if (list.get(i - 1) != null && list.get(i) != null) {
				if (list.get(i - 1).doubleValue() < list.get(i).doubleValue()) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isSortedAscendingDate(List<Object> list) {
		if (list == null || list.size() <= 1) return true;

		try {
			for (int i = 1; i < list.size(); i++) {
				if (list.get(i - 1) != null && list.get(i) != null) {
					long date1 = getTimestamp(list.get(i - 1));
					long date2 = getTimestamp(list.get(i));
					if (date1 > date2) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean isSortedDescendingDate(List<Object> list) {
		if (list == null || list.size() <= 1) return true;

		try {
			for (int i = 1; i < list.size(); i++) {
				if (list.get(i - 1) != null && list.get(i) != null) {
					long date1 = getTimestamp(list.get(i - 1));
					long date2 = getTimestamp(list.get(i));
					if (date1 < date2) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public long getTimestamp(Object value) throws ParseException {
		if (value instanceof Number) {
			return ((Number) value).longValue();
		} else if (value instanceof String) {
			String dateStr = (String) value;
			try {
				// Try parsing as Unix timestamp
				return Long.parseLong(dateStr);
			} catch (NumberFormatException e) {
				// Try parsing as date_time format first (yyyy-MM-dd'T'HH:mm:ss)
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					return sdf.parse(dateStr).getTime() / 1000; // Convert to seconds
				} catch (ParseException pe) {
					// Try parsing as date format (yyyy-MM-dd)
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						return sdf.parse(dateStr).getTime() / 1000; // Convert to seconds
					} catch (ParseException pe2) {
						return 0;
					}
				}
			}
		}
		return 0;
	}

	public Map<String, Integer> getIndustryIdMap(String baseURL, String apiKey) {
		String basePath = "industries";
		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKey, null, true, null);
		response.then().statusCode(200);
		JsonPath responseJson = response.jsonPath();
		Map<String, Integer> industryIdMap = new HashMap<>();

		// Parse defaultIndustries array
		List<Map<String, Object>> defaultIndustries = responseJson.getList("data.defaultIndustries");
		for (Map<String, Object> industry : defaultIndustries) {
			String label = (String) industry.get("label");
			Integer id = ((Number) industry.get("id")).intValue();
			industryIdMap.put(label, id);
		}

		// Parse customIndustries array
		List<Map<String, Object>> customIndustries = responseJson.getList("data.customIndustries");
		for (Map<String, Object> industry : customIndustries) {
			String label = (String) industry.get("label");
			Integer id = ((Number) industry.get("id")).intValue();
			industryIdMap.put(label, id);
		}
		industryIdMap.put("None",0);

		return industryIdMap;
	}

	public int getInvoiceTemplateId(String invoiceServiceURL, String token, String template) {
		Map<String, String> authTokenMap = getAuthTokenMap(token);
		JSONObject body = new JSONObject();
        body.put("sortPriorityList", new ArrayList<>());
        body.put("isPayBill", 1);

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("searchText", "");
        queryParams.put("orderByColumn", "");
        queryParams.put("sortDirection", "");

		Response response = RestClient.doPost("JSON", invoiceServiceURL, "invoices/templates/search", authTokenMap, queryParams, true, body);
        response.then().statusCode(200);

		for (int i = 0; i < response.jsonPath().getList("data.templates").size(); i++) {
			if (response.jsonPath().getString("data.templates["+i+"].templateName").equals(template)) {
				return response.jsonPath().getInt("data.templates["+i+"].id");
			}
		}
		return -1;
	}

	public Response createCustomJobStatus(String albatrossURL, String albatrossToken, List<JSONObject> customizedJobStatusList) {
		JSONObject requestBody = new JSONObject();
		requestBody.put("customizedJobStatus", customizedJobStatusList);
	
		Response response = RestClient.doPost("JSON", albatrossURL, "jobs-pipeline", albatrossToken, null, true, requestBody);
		response.then().statusCode(200);
		response.then().body("message", Matchers.containsString("Customize Job Status Save Successful"));
		return response;
	}

	public String createActivityDataAndGetEntitySlug(String baseURL, Object authToken, String entityType) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);
		String entitySlug = getEntityResponse(baseURL, authToken, entityType);

		Note note = new Note();
		note.setRelated_to(entitySlug);
		note.setRelated_to_type(entityType);
		note.setDescription(notesText + "<br><br>" + notesText);
		Response response = RestClient.doPost("JSON", baseURL, "notes", authTokenMap, null, true, note);
		response.then().body("related_to", Matchers.is(entitySlug));

		Task task = new Task();
		task.setTitle(taskTitle);
		task.setDescription(taskDescription);
		task.setReminder(30);
		task.setRelated_to(entitySlug);
		task.setRelated_to_type(entityType);
		task.setStart_date(startDate);
		Response taskResponse = RestClient.doPost("JSON", baseURL, "tasks", authTokenMap, null, true, task);
		taskResponse.then().body("related_to", Matchers.is(entitySlug));

		Meeting meeting = new Meeting();
		meeting.setTitle(meetingName);
		meeting.setDescription(meetingDescription);
		meeting.setAddress(address);
		meeting.setReminder(15);
		meeting.setRelated_to(entitySlug);
		meeting.setRelated_to_type(entityType);
		meeting.setStart_date(pastDate);
		meeting.setEnd_date(endDate);

		Response meetingResponse = RestClient.doPost("JSON", baseURL, "meetings", authTokenMap, null, true, meeting);
		meetingResponse.then().body("related_to", Matchers.is(entitySlug));

		return entitySlug;
	}

	public Response createEntityByRole(String baseURL, Object authToken, String entityType, int ownerUserId) {
		Response response = null;
		String companySlug = "";
		String contactSlug = "";
		String jobSlug = "";

		switch (entityType) {
			case "candidate":
				Candidate candidate = new Candidate(CandidateFirstName, CandidateLastName, ownerUserId, ownerUserId);
				response = RestClient.doPost("JSON", baseURL, "candidates", authToken, null,true, candidate);
				break;

			case "company":
				Company company = new Company(companyName, companyWebsite, contactNumber, ownerUserId, ownerUserId);
				response = RestClient.doPost("JSON", baseURL, "companies", authToken, null,true, company);
				break;

			case "contact":
				Company company1 = new Company(companyName, companyWebsite, contactNumber, ownerUserId, ownerUserId);
				response = RestClient.doPost("JSON", baseURL, "companies", authToken, null,true, company1);
				companySlug = response.jsonPath().get("slug");
				Contact contact = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumbers, companySlug, ownerUserId, ownerUserId);
				response = RestClient.doPost("JSON", baseURL, "contacts", authToken, null,true, contact);
				break;

			case "job":
				Company company2 = new Company(companyName, companyWebsite, contactNumber, ownerUserId, ownerUserId);
				response = RestClient.doPost("JSON", baseURL, "companies", authToken, null,true, company2);
				companySlug = response.jsonPath().get("slug");
				Contact contact2 = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumbers, companySlug, ownerUserId, ownerUserId);
				response = RestClient.doPost("JSON", baseURL, "contacts", authToken, null,true, contact2);
				contactSlug = response.jsonPath().get("slug");
				Job job = new Job(JobName, companySlug, contactSlug, ownerUserId, ownerUserId, city, "RBAC Job Description");
				response = RestClient.doPost("JSON", baseURL, "jobs", authToken, null,true, job);
				break;

			case "deal":
				Company company3 = new Company(companyName, companyWebsite, contactNumber, ownerUserId, ownerUserId);
				response = RestClient.doPost("JSON", baseURL, "companies", authToken, null,true, company3);
				companySlug = response.jsonPath().get("slug");
				Contact contact3 = new Contact(ContactFirstName, ContactLastName, ContactEmail, contactNumbers, companySlug, ownerUserId, ownerUserId);
				response = RestClient.doPost("JSON", baseURL, "contacts", authToken, null,true, contact3);
				contactSlug = response.jsonPath().get("slug");
				Job job2 = new Job(JobName, companySlug, contactSlug, ownerUserId, ownerUserId, city, "RBAC Job Description");
				response = RestClient.doPost("JSON", baseURL, "jobs", authToken, null,true, job2);
				jobSlug = response.jsonPath().get("slug");
				Deal deal = new Deal(dealName, dealStage, dealValue, dealDate, dealType, companySlug, jobSlug, contactSlug, ownerUserId, ownerUserId);
				response = RestClient.doPost("JSON", baseURL, "deals", authToken, null,true, deal);
			default:
		}
		return response;
	}

    public void transferContactOwnership(String baseURL, Object authToken, String contactSlug, Integer newOwnerId) {
        // Get contact details from albatross API
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("contact", contactSlug);
        String basePath = "contacts/{contact}";
        Response contactResponse = RestClient.doGet("JSON", baseURL, basePath, authToken, null, pathParameters, true);
        assertThat("Failed to get contact details from albatross API", contactResponse.getStatusCode(), equalTo(200));

        JsonPath contactJsonPath = contactResponse.jsonPath();
        Map<String, Object> contactMap = contactJsonPath.get("data.contact");
        JSONObject contactData = new JSONObject(contactMap);

        // Create transfer ownership payload
        JSONObject transferPayload = new JSONObject();
        transferPayload.put("relatedtotypeid", 2);
        transferPayload.put("selectedowner", newOwnerId);
        JSONArray selectedRows = new JSONArray();
        selectedRows.put(contactData);
        transferPayload.put("selectedrows", selectedRows);

        // Transfer ownership
        String transferEndpoint = "users/transfer-ownership/" + newOwnerId;
        Response transferResponse = RestClient.doPost("JSON", baseURL, transferEndpoint, authToken, null, true, transferPayload.toString());
        assertThat("Failed to transfer contact ownership to owner", transferResponse.getStatusCode(), equalTo(200));
    }

	public Response createInvoiceCustomFieldsResponse(String invoiceServiceUrl, String authToken, String customFieldName, String fieldType, String defaultOptions) {
        JSONObject requestBody = new JSONObject();
        String fieldTypeLower = fieldType.toLowerCase();

        if (fieldTypeLower.equals("dropdown") || fieldTypeLower.equals("multiselect")) {
            List<JSONObject> optionsList = new ArrayList<>();
            String[] options = defaultOptions.split(",");
            for (int i = 0; i < options.length; i++) {
                JSONObject option = new JSONObject();
                option.put("label", options[i].trim());
                option.put("sequence_no", i + 1);
                optionsList.add(option);
            }
            requestBody.put("defaultOptionsValue", optionsList);
        } else {
            requestBody.put("defaultOptionsValue", new ArrayList<>());
        }

        requestBody.put("type", customFieldFaker.getCustomFieldId(fieldTypeLower));
        requestBody.put("isDefault", 0);
        requestBody.put("label", customFieldName);

        Response response = RestClient.doPost1("JSON", invoiceServiceUrl, "invoices/custom-fields", authToken, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().body("meta.message", Matchers.is("Custom Field Created successfully"));

        return response;
    }

	public Response updateCandidateHiringStageRemark(String baseURL, Object authToken, String candidateSlug, String jobSlug) {
		
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		JsonPath json = getAllHiringStages(baseURL, authToken).jsonPath();
		int getStatusIdString = json.get("status_id[0]");
		
		HiringStage hiringStage = new HiringStage();
		hiringStage.setRemark(taskDescription);
		hiringStage.setStage_date(startDate);
		hiringStage.setStatus_id(getStatusIdString);

		Map<String, String> pathParameters = new HashMap<String, String>();
		pathParameters.put("candidate", candidateSlug);
		pathParameters.put("job", jobSlug);

		String basePath = "candidates/{candidate}/hiring-stages/{job}";

		Response response = RestClient.doPost1("JSON", baseURL, basePath, authTokenMap, null, pathParameters, true, hiringStage);
		
		response.then().statusCode(200);
		response.then().body("candidate_slug", Matchers.is(candidateSlug));
		response.then().body("job_slug", Matchers.is(jobSlug));
		return response;
	}

}
