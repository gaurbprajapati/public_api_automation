package io.recruitcrm.executiveSummary.SearchReport;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.executiveSummaryService.ExecutiveSearchReportFunctions;
import io.rcrm.api.commanfunctions.executiveSummaryService.ExecutiveSummaryFunctions;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.executive_summary.ExecutiveSearchReportFaker;
import io.rcrm.api.pojo.executiveSummary.Candidate_fields;
import io.rcrm.api.pojo.executiveSummary.ExecutiveSearchReport;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateExecutiveSearchReportTest extends TestBase {

	String reportID = "";
	int jobID;

	ListFunctions listFunctions = new ListFunctions();
	commanFunction function = new commanFunction();
	ExecutiveSummaryFunctions executiveSummaryFunctions = new ExecutiveSummaryFunctions();

	ExecutiveSearchReportFaker ExecutiveSearchReportFaker = new ExecutiveSearchReportFaker();
	ExecutiveSearchReportFunctions executiveSearchReportFunctions = new ExecutiveSearchReportFunctions();

	@Owner("Sandeep")
	@Test(dataProvider = "getExecutiveSearchReportData", groups = "nightly-build")
	public void createExecutiveSearchReport_executiveSummaryTest(int jobId_report, int titleTemplateID,
			int candidateProfileTemplateID, String selectedCollabrators, String selected_candidates_ids,
			String candidateFields) {

		String reportTitleName = ExecutiveSearchReportFaker.getExecutiveSearchReportName();

		try {
			ExecutiveSearchReport executiveSearchReport = new ExecutiveSearchReport();
			executiveSearchReport.setJob_id(jobId_report);
			executiveSearchReport.setReport_title(reportTitleName);
			executiveSearchReport.setTitle_template_id(titleTemplateID);
			executiveSearchReport.setCandidate_profile_template_id(candidateProfileTemplateID);
			executiveSearchReport.setShow_candidates(1);
			executiveSearchReport.setShow_collaborators(1);
			executiveSearchReport.setSelected_candidates_ids(selected_candidates_ids);
			executiveSearchReport.setSelected_collaborators(selectedCollabrators);

			executiveSearchReport.setCandidate_fields(candidateFields);
			executiveSearchReport.setReport_content_json("\"{\\\"name\\\":\\\"John\\\",\\\"age\\\":30}\"");

			Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports",
					ThreadManager.getOwnerAlbatrossToken(), null, true, executiveSearchReport);

			response.then().statusCode(200);
			response.then().assertThat().body(matchesJsonSchemaInClasspath(
					"schemaValidation//ExecutiveSearchReportSchema//CreateExecutiveSearchReportSchema.json"));

			response.then().body("report_title", Matchers.is(reportTitleName));

			// Verify Response using Assertion and Jsonpath
			JsonPath jp = response.jsonPath();

			int ID = jp.get("id");
			reportID = Integer.toString(ID);

			jobID = jp.get("job_id");

			reportID = Integer.toString(ID);
		} catch (Exception e) {
			Assert.fail();
		} finally {
//			Delete all Test Data 

			if (reportID != null)
				executiveSearchReportFunctions.deleteExecutiveSearchByID(reportID, executiveSummaryServiceURL,
						ThreadManager.getOwnerAlbatrossToken());
		}
	}

	@Owner("Sandeep")
	@Test(dataProvider = "getExecutiveSearchReportData", groups = "nightly-build")
	public void userCannotCreateNewSearchReportWithInvalidData_executiveSummaryTest(int jobId_report,
			int titleTemplateID, int candidateProfileTemplateID, String selectedCollabrators,
			String selected_candidates_ids, String candidateFields) {

		String reportTitleName = ExecutiveSearchReportFaker.getExecutiveSearchReportName();

		try {
			ExecutiveSearchReport executiveSearchReport = new ExecutiveSearchReport();
			executiveSearchReport.setJob_id(jobId_report);
			executiveSearchReport.setReport_title(reportTitleName + ",0x0001");
			executiveSearchReport.setTitle_template_id(titleTemplateID);
			executiveSearchReport.setCandidate_profile_template_id(candidateProfileTemplateID);
			executiveSearchReport.setShow_candidates(1111000);
			executiveSearchReport.setShow_collaborators(1111000);
			executiveSearchReport.setSelected_candidates_ids(selected_candidates_ids + ",0x0001");
			executiveSearchReport.setSelected_collaborators(selectedCollabrators + ",0x0001");

			executiveSearchReport.setCandidate_fields(candidateFields);
			executiveSearchReport.setReport_content_json("\"{\\\"name\\\":\\\"John\\\",\\\"age\\\":30}\"");

			Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports",
					ThreadManager.getOwnerAlbatrossToken(), null, true, executiveSearchReport);

			response.then().statusCode(422);

		} catch (Exception e) {
			Assert.fail();
		} finally {
		}
	}

	@Owner("Sandeep")
	@Test(dataProvider = "getExecutiveSearchReportData", groups = "nightly-build")
	public void unAuthorizedCannotCreateNewTemplateValidData_executiveSummaryTest(int jobId_report, int titleTemplateID,
			int candidateProfileTemplateID, String selectedCollabrators, String selected_candidates_ids,
			String candidateFields) {

		String reportTitleName = ExecutiveSearchReportFaker.getExecutiveSearchReportName();

		try {
			ExecutiveSearchReport executiveSearchReport = new ExecutiveSearchReport();
			executiveSearchReport.setJob_id(jobId_report);
			executiveSearchReport.setReport_title(reportTitleName);
			executiveSearchReport.setTitle_template_id(titleTemplateID);
			executiveSearchReport.setCandidate_profile_template_id(candidateProfileTemplateID);
			executiveSearchReport.setShow_candidates(1);
			executiveSearchReport.setShow_collaborators(1);
			executiveSearchReport.setSelected_candidates_ids(selected_candidates_ids);
			executiveSearchReport.setSelected_collaborators(selectedCollabrators);

			executiveSearchReport.setCandidate_fields(candidateFields);
			executiveSearchReport.setReport_content_json("\"{\\\"name\\\":\\\"John\\\",\\\"age\\\":30}\"");

			Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports",
					ThreadManager.getOwnerAlbatrossToken()+"x001", null, true, executiveSearchReport);

			response.then().statusCode(401);

		} catch (Exception e) {
			Assert.fail();
		} finally {

		}
	}

	@DataProvider
	public Object[][] getExecutiveSearchReportData() {

		String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getJobResponse = albatrossFunctions.getJobResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), jobSlug);

		// Verify Response using Assertion and Jsonpath
		JsonPath jpJob = getJobResponse.jsonPath();
		int jobID = jpJob.get("data.job.id");

		// Get all templates IDs

		int titleTemplateID = 0;
		int candidateProfileTemplateID = 0;

		for (int i = 1; i < 3; i++) {

			ExecutiveSummaryFunctions executiveSummaryFunctions = new ExecutiveSummaryFunctions();
			Response response = executiveSummaryFunctions.createTemplateForExecutiveSummary(i,
					executiveSummaryServiceURL, ThreadManager.getOwnerAlbatrossToken());

			response.then().statusCode(200);

			// Verify Response using Assertion and Jsonpath
			JsonPath jsonPathTemplate = response.jsonPath();

			if (i == 1)
				titleTemplateID = jsonPathTemplate.get("id");

			if (i == 2)
				candidateProfileTemplateID = jsonPathTemplate.get("id");
		}

		// Get collabrators

		JsonPath jsonGetCollabrators = listFunctions.getAllCollabrators(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		ArrayList<Integer> recruiterIds = jsonGetCollabrators.get("id");

		String selectedCollabrators = recruiterIds.toString();

		// Assign Candidates to job and get candidate ID for report

		String selected_candidates_ids = "";
		StringBuilder strcat = new StringBuilder();

		for (int i = 0; i < 3; i++) {
			JsonPath jsonAssigneCandidate;

			jsonAssigneCandidate = function.assignCandidateByJobSlug(baseURL, ThreadManager.getAccountApiKey(), jobSlug)
					.jsonPath();
			String candidateSlug = jsonAssigneCandidate.get("candidate_slug");

			Response getCandResponse = albatrossFunctions.getCandidateResponse(albatrossURL, ThreadManager.getOwnerAlbatrossToken(),
					candidateSlug);

			// Verify Response using Assertion and Jsonpath
			JsonPath jpCand = getCandResponse.jsonPath();

			int candidateID = jpCand.get("data.candidate.id");

			String candidatesIds = String.valueOf(candidateID);

			strcat.append(selected_candidates_ids);
			strcat.append(candidatesIds);
			selected_candidates_ids = candidatesIds;

		}

		// get list of candidates column names with max 5 columns

		ArrayList<Object> candidate_fields = new ArrayList<Object>();

		Candidate_fields candidate_fields1 = new Candidate_fields();
		candidate_fields1.setColumn_name("first_name");

		Candidate_fields candidate_fields2 = new Candidate_fields();
		candidate_fields2.setColumn_name("last_name");

		candidate_fields.add(candidate_fields1);
		candidate_fields.add(candidate_fields2);

		Gson gson = new Gson();
		String candidateFieldsObjectInString = gson.toJson(candidate_fields);

		// create report content json

		Object data[][] = { { jobID, titleTemplateID, candidateProfileTemplateID, selectedCollabrators,
				selected_candidates_ids, candidateFieldsObjectInString } };
		return data;
	}
}
