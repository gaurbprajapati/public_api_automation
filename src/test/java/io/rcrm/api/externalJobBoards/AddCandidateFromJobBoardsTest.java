package io.rcrm.api.externalJobBoards;

import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.externalJobBoards.CandidateFromJobBoard;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.is;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AddCandidateFromJobBoardsTest extends TestBase {
	JavaFakerCandidate javaFakerCandidate = new JavaFakerCandidate();
	String candidateFacebookURL = javaFakerCandidate.getCandidateFacebookURL().replace(" ", "");
	String candidateTwitterURL = javaFakerCandidate.getCandidateTwitterURL().replace(" ", "");
	String candidateLinkedinURL = javaFakerCandidate.getCandidateLinkedinURL().replace(" ", "");
	String candidateGithubURL = javaFakerCandidate.getCandidateGithubURL().replace(" ", "");
	String candidateXingURL = javaFakerCandidate.getCandidateXingURL().replace(" ", "");

	//specific for /candidates api
	String EXTERNAL_JOB_BOARDS_TOKEN = "7zPOWept9uSphD3bT93kDZEcHOZjJXQA9cRR27xgF8SszKQJJ1xlulgdPxDMtDf4";
	String EXTERNAL_JOB_BOARDS_TOKEN_INVALID, jobSlug = RandomStringUtils.randomAlphanumeric(10);

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void addCandidateFromJobBoardsWithInvalidAuth() {
		CandidateFromJobBoard candidateFromJobBoard = new CandidateFromJobBoard();
		CandidateFromJobBoard.Candidate candidate = candidateFromJobBoard.new Candidate();
		candidate.setFirst_name(javaFakerCandidate.getFirstName());
		candidate.setLast_name(javaFakerCandidate.getLastName());
		candidate.setEmail_id(javaFakerCandidate.getEmailID());
		candidate.setContact_number(javaFakerCandidate.getContactNumber());
		candidate.setWork_experience_year(javaFakerCandidate.getWork_ex_year());
		candidate.setResume(javaFakerCandidate.getResume());
		candidate.setCurrent_organization_name(javaFakerCandidate.getCurrentOrganization());
		candidate.setAddress(javaFakerCandidate.getCandidateAddress());
		candidate.setCity(javaFakerCandidate.getCity());
		candidate.setState(javaFakerCandidate.getState());
		candidate.setCountry(javaFakerCandidate.getCountry());
		candidate.setProfile_facebook(candidateFacebookURL);
		candidate.setProfile_twitter(candidateTwitterURL);
		candidate.setProfile_linkedin(candidateLinkedinURL);
		candidate.setProfile_github(candidateGithubURL);
		candidate.setProfile_xing(candidateXingURL);
		candidate.setNotice_period_days(javaFakerCandidate.getNotice_period());
		candidate.setCandidate_dob(javaFakerCandidate.getDOB());

		candidateFromJobBoard.setJob_reference_id(jobSlug);
		candidateFromJobBoard.setCandidate(candidate);

		String basePath = "/candidate";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, EXTERNAL_JOB_BOARDS_TOKEN_INVALID,
				null, true, candidateFromJobBoard);

		response.then().statusCode(401);
		response.then().body("success", is("false"));
		response.then().body("message", is("Unauthorized"));
		response.then().body("status_code", is(401));
	}

	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void addCandidateFromJobBoardsWithInvalidBody() {
		CandidateFromJobBoard candidateFromJobBoard = new CandidateFromJobBoard();
		CandidateFromJobBoard.Candidate candidate = candidateFromJobBoard.new Candidate();
		//invalid dob since dob field is nullable
		candidate.setCandidate_dob(javaFakerCandidate.getDOB() + 123);
		candidateFromJobBoard.setCandidate(candidate);

		String basePath = "/candidate";

		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, EXTERNAL_JOB_BOARDS_TOKEN,
				null, true, candidateFromJobBoard);

		response.then().statusCode(422);
		response.then().body("message", is("The given data was invalid."));
		response.then().body("errors.job_reference_id[0]", is("The job reference id field is required."));
		response.then().body("errors['candidate.first_name'][0]", is("The candidate.first name field is required."));
		response.then().body("errors['candidate.last_name'][0]", is("The candidate.last name field is required."));
		response.then().body("errors['candidate.email_id'][0]", is("The candidate.email id field is required."));
		response.then().body("errors['candidate.contact_number'][0]", is("The candidate.contact number field is required."));
		response.then().body("errors['candidate.work_experience_year'][0]", is("The candidate.work experience year must be an integer."));
		response.then().body("errors['candidate.current_organization_name'][0]", is("The candidate.current organization name must be a string."));
		response.then().body("errors['candidate.address'][0]", is("The candidate.address must be a string."));
		response.then().body("errors['candidate.city'][0]", is("The candidate.city must be a string."));
		response.then().body("errors['candidate.state'][0]", is("The candidate.state must be a string."));
		response.then().body("errors['candidate.country'][0]", is("The candidate.country must be a string."));
		response.then().body("errors['candidate.profile_facebook'][0]", is("The candidate.profile facebook must be a valid URL."));
		response.then().body("errors['candidate.profile_twitter'][0]", is("The candidate.profile twitter must be a valid URL."));
		response.then().body("errors['candidate.profile_linkedin'][0]", is("The candidate.profile linkedin must be a valid URL."));
		response.then().body("errors['candidate.profile_github'][0]", is("The candidate.profile github must be a valid URL."));
		response.then().body("errors['candidate.profile_xing'][0]", is("The candidate.profile xing must be a valid URL."));
		response.then().body("errors['candidate.notice_period_days'][0]", is("The candidate.notice period days must be an integer."));
	}

}