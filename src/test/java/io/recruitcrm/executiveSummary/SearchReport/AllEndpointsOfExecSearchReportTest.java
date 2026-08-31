package io.recruitcrm.executiveSummary.SearchReport;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.*;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import com.google.gson.Gson;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.executiveSummaryService.ExecutiveSearchReportFunctions;
import io.rcrm.api.commanfunctions.executiveSummaryService.ExecutiveSummaryFunctions;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.executive_summary.ExecutiveSearchReportFaker;
import io.rcrm.api.pojo.executiveSummary.Candidate_fields;
import io.rcrm.api.pojo.executiveSummary.ExecutiveSearchReport;
import io.rcrm.api.pojo.executiveSummary.GenerateReport;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointsOfExecSearchReportTest extends TestBase {

	String reportID = "";
	int jobID;

	ListFunctions listFunctions ;
	commanFunction function ;
	ExecutiveSummaryFunctions executiveSummaryFunctions;

	ExecutiveSearchReportFaker ExecutiveSearchReportFaker;
	ExecutiveSearchReportFunctions executiveSearchReportFunctions;
	String albatrossAuthTkn;

	@BeforeClass(alwaysRun = true)	public void setUp(){
		albatrossAuthTkn = ThreadManager.getOwnerAlbatrossToken();
		ExecutiveSearchReportFaker = new ExecutiveSearchReportFaker();
		executiveSearchReportFunctions = new ExecutiveSearchReportFunctions();
		executiveSummaryFunctions = new ExecutiveSummaryFunctions();
		function = new commanFunction();
		listFunctions = new ListFunctions();
	}

	@Owner("Sandeep")
	@Test(priority = 0, dataProvider = "getExecutiveSearchReportData", groups = "nightly-build")
	public void createExecutiveSearchReport_Test(int jobId_report, int titleTemplateID, int candidateProfileTemplateID,
			String selectedCollabrators, String selected_candidates_ids, String candidateFields) {

		String reportTitleName = ExecutiveSearchReportFaker.getExecutiveSearchReportName();

//		try {
		ExecutiveSearchReport executiveSearchReport = new ExecutiveSearchReport();
		executiveSearchReport.setJob_id(jobId_report);
		executiveSearchReport.setReport_title(reportTitleName);
		executiveSearchReport.setTitle_template_id(titleTemplateID);
		executiveSearchReport.setCandidate_profile_template_id(candidateProfileTemplateID);
		executiveSearchReport.setShow_candidates(1);
		executiveSearchReport.setShow_collaborators(1);
		executiveSearchReport.setSelected_candidates_ids(selected_candidates_ids);
		executiveSearchReport.setSelected_collaborators(selectedCollabrators);
		executiveSearchReport.setEsr_revamp(0);

		executiveSearchReport.setCandidate_fields(candidateFields);
		executiveSearchReport.setReport_content_json("\"{\\\"name\\\":\\\"John\\\",\\\"age\\\":30}\"");

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports",
				albatrossAuthTkn, null, true, executiveSearchReport);

		response.then().statusCode(200);
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"schemaValidation//ExecutiveSearchReportSchema//CreateExecutiveSearchReportSchema.json"));

		response.then().body("report_title", Matchers.is(reportTitleName));

		// Verify Response using Assertion and Jsonpath
		JsonPath jp = response.jsonPath();

		int ID = jp.get("id");
		reportID = Integer.toString(ID);

		jobID = jp.get("job_id");

		// reportID = Integer.toString(ID);
//		} catch (Exception e) {
//			Assert.fail();
//		} finally {
//			if (reportID != null)
//				executiveSearchReportFunctions.deleteExecutiveSearchByID(reportID, executiveSummaryServiceURL,
//						authTokenMap);
//		}

	}

	@Owner("Sandeep")
	@Test(priority = 1, dependsOnMethods = "createExecutiveSearchReport_Test", dataProvider = "getExecutiveSearchReportData", groups = "nightly-build")
	public void editExecutiveSearchReportByReportID_Test(int jobId_report, int titleTemplateID,
			int candidateProfileTemplateID, String selectedCollabrators, String selected_candidates_ids,
			String candidateFields) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("report_id", reportID);
		String basePath = "/executive-search-reports/{report_id}";

		ExecutiveSearchReport executiveSearchReport = new ExecutiveSearchReport();
		executiveSearchReport.setReport_title(ExecutiveSearchReportFaker.getExecutiveSearchReportName() + "Edited");

		executiveSearchReport.setJob_id(jobID);
		executiveSearchReport.setReport_title(ExecutiveSearchReportFaker.getExecutiveSearchReportName() + "Edited");
		executiveSearchReport.setTitle_template_id(titleTemplateID);
		executiveSearchReport.setCandidate_profile_template_id(candidateProfileTemplateID);
		executiveSearchReport.setShow_candidates(1);
		executiveSearchReport.setShow_collaborators(1);
		executiveSearchReport.setSelected_candidates_ids(selected_candidates_ids);
		executiveSearchReport.setSelected_collaborators(selectedCollabrators);
		executiveSearchReport.setEsr_revamp(0);

		executiveSearchReport.setCandidate_fields(candidateFields);
		executiveSearchReport.setReport_content_json("\"{\\\"name\\\":\\\"John\\\",\\\"age\\\":30}\"");

		Response response = RestClient.doPost1("JSON", executiveSummaryServiceURL, basePath, albatrossAuthTkn, null,
				pathParamters, true, executiveSearchReport);

		response.then().statusCode(200);

	}

	@Owner("Sandeep")
	@Test(priority = 2, dependsOnMethods = "createExecutiveSearchReport_Test", dataProvider = "getGenerateActionData", groups = "nightly-build")
	public void GenerateExecutiveSearchReportTest(String actionName, int statusCode) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("report_id", reportID);
		String basePath = "/executive-search-reports/generate/{report_id}";

		GenerateReport generateReport = new GenerateReport();
		generateReport.setReport_content_html("<html><body><h1>Hello World</h1></body></html>");
		generateReport.setAction(actionName);

		Response response = RestClient.doPost1("JSON", executiveSummaryServiceURL, basePath, albatrossAuthTkn, null,
				pathParamters, true, generateReport);

		response.then().statusCode(statusCode);

	}

	@Owner("Sandeep")
	@Test(priority = 3, dependsOnMethods = "createExecutiveSearchReport_Test", groups = "nightly-build")
	public void getAllExecutiveSearchReportsByJobID_Test() {
		String basePath = "/executive-search-reports";

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("job_id", String.valueOf(jobID));

		Response response = RestClient.doGet("JSON", executiveSummaryServiceURL, basePath, albatrossAuthTkn,
				queryParameters, null, true);

		response.then().statusCode(200);
	}

	@Owner("Sandeep")
	@Test(priority = 4, dependsOnMethods = "createExecutiveSearchReport_Test", groups = "nightly-build")
	public void deleteExecutiveSearchReportByReportID_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("report_id", reportID);
		String basePath = "/executive-search-reports/{report_id}";

		Response response = RestClient.doDelete("JSON", executiveSummaryServiceURL, basePath, albatrossAuthTkn, null,
				pathParamters, false);

		response.then().statusCode(200);
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getExecutiveSearchReportData", groups = "nightly-build")
	public void getExecutiveSearchReportWithAllPlaceHolders_Test_200(int jobID, int titleTemplateID,
																 int candidateProfileTemplateID, String selectedCollabrators, String selected_candidates_ids,
																 String candidateFields) {

		ExecutiveSearchReport executiveSearchReport = new ExecutiveSearchReport();
		executiveSearchReport.setJob_id(jobID);
		executiveSearchReport.setReport_title(ExecutiveSearchReportFaker.getExecutiveSearchReportName());
		executiveSearchReport.setTitle_template_id(titleTemplateID);
		executiveSearchReport.setCandidate_profile_template_id(candidateProfileTemplateID);
		executiveSearchReport.setShow_candidates(1);
		executiveSearchReport.setShow_collaborators(1);
		executiveSearchReport.setSelected_candidates_ids(selected_candidates_ids);
		executiveSearchReport.setSelected_collaborators(selectedCollabrators);
		executiveSearchReport.setCandidate_fields(candidateFields);
		executiveSearchReport.setEsr_revamp(0);

		String reportContentJson = createReportContentJson(selected_candidates_ids);
		executiveSearchReport.setReport_content_json(reportContentJson);

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports",
				albatrossAuthTkn, null, true, executiveSearchReport);

		Assert.assertEquals(response.getStatusCode(), 200);

		String expectedReportContentJson = response.jsonPath().get("report_content_json");
		if (!expectedReportContentJson.contains(reportContentJson)) {
			Assert.fail("Report content json is not as expected");
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getExecutiveSearchReportData", groups = "nightly-build")
	public void getExecutiveSearchReport_UnauthorizedTest_401(int jobID, int titleTemplateID,
														   int candidateProfileTemplateID, String selectedCollabrators, String selected_candidates_ids,
														   String candidateFields) {

		ExecutiveSearchReport executiveSearchReport = new ExecutiveSearchReport();
		executiveSearchReport.setJob_id(jobID);
		executiveSearchReport.setReport_title(ExecutiveSearchReportFaker.getExecutiveSearchReportName());
		executiveSearchReport.setTitle_template_id(titleTemplateID);
		executiveSearchReport.setCandidate_profile_template_id(candidateProfileTemplateID);
		executiveSearchReport.setShow_candidates(1);
		executiveSearchReport.setShow_collaborators(1);
		executiveSearchReport.setSelected_candidates_ids(selected_candidates_ids);
		executiveSearchReport.setSelected_collaborators(selectedCollabrators);
		executiveSearchReport.setCandidate_fields(candidateFields);
		executiveSearchReport.setEsr_revamp(0);

		String reportContentJson = createReportContentJson(selected_candidates_ids);

		executiveSearchReport.setReport_content_json(reportContentJson);

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports",
				"invalidToken", null, true, executiveSearchReport);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getExecutiveSearchReport_MissingFieldsTest_422() {
		ExecutiveSearchReport executiveSearchReport = new ExecutiveSearchReport();

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports",
				albatrossAuthTkn, null, true, executiveSearchReport);

		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("message", Matchers.containsString("The given data was invalid."));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "getExecutiveSearchReportData", groups = "nightly-build")
	public void getExecutiveSearchReport_InvalidJobIDTest_422(int jobID, int titleTemplateID,
														   int candidateProfileTemplateID, String selectedCollabrators, String selected_candidates_ids,
														   String candidateFields) {
		// Invalid Job ID
		jobID = -1;

		ExecutiveSearchReport executiveSearchReport = new ExecutiveSearchReport();
		executiveSearchReport.setJob_id(jobID);
		executiveSearchReport.setReport_title(ExecutiveSearchReportFaker.getExecutiveSearchReportName());
		executiveSearchReport.setTitle_template_id(titleTemplateID);
		executiveSearchReport.setCandidate_profile_template_id(candidateProfileTemplateID);
		executiveSearchReport.setShow_candidates(1);
		executiveSearchReport.setShow_collaborators(1);
		executiveSearchReport.setSelected_candidates_ids(selected_candidates_ids);
		executiveSearchReport.setSelected_collaborators(selectedCollabrators);
		executiveSearchReport.setCandidate_fields(candidateFields);
		executiveSearchReport.setEsr_revamp(0);

		String reportContentJson = createReportContentJson(selected_candidates_ids);

		executiveSearchReport.setReport_content_json(reportContentJson);

		Response response = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports",
				albatrossAuthTkn, null, true, executiveSearchReport);

		Assert.assertEquals(response.getStatusCode(), 422);
		response.then().body("message", Matchers.containsString("The given data was invalid."));
		response.then().body("errors.job_id[0]", Matchers.containsString("The selected job id is invalid."));
	}

    @Owner("Smit Patel")
    @Test(dataProvider = "getExecutiveSearchReportData", groups = "nightly-build")
    public void verifyTwoESRCannotBeTheSameName(int jobID, int titleTemplateID, int candidateProfileTemplateID, String selectedCollabrators, String selected_candidates_ids, String candidateFields) {
        String duplicateReportName = ExecutiveSearchReportFaker.getExecutiveSearchReportName();
        Response firstResponse = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports", albatrossAuthTkn, null, true, buildExecutiveSearchReport(jobID, titleTemplateID, candidateProfileTemplateID, selectedCollabrators, selected_candidates_ids, candidateFields, duplicateReportName));
        firstResponse.then().statusCode(200);
        Response secondResponse = RestClient.doPost("JSON", executiveSummaryServiceURL, "executive-search-reports", albatrossAuthTkn, null, true, buildExecutiveSearchReport(jobID, titleTemplateID, candidateProfileTemplateID, selectedCollabrators, selected_candidates_ids, candidateFields, duplicateReportName));
        Assert.assertEquals(secondResponse.getStatusCode(), 422);
        secondResponse.then().body("message", Matchers.is("The given data was invalid."));
        secondResponse.then().body("errors.report_title[0]", Matchers.is("The report title has already been taken."));
    }

    private ExecutiveSearchReport buildExecutiveSearchReport(int jobID, int titleTemplateID, int candidateProfileTemplateID, String selectedCollabrators, String selected_candidates_ids, String candidateFields, String reportTitle) {
        ExecutiveSearchReport report = new ExecutiveSearchReport();
        report.setJob_id(jobID); report.setReport_title(reportTitle); report.setTitle_template_id(titleTemplateID); report.setCandidate_profile_template_id(candidateProfileTemplateID);
        report.setShow_candidates(1); report.setShow_collaborators(1); report.setSelected_candidates_ids(selected_candidates_ids); report.setSelected_collaborators(selectedCollabrators);
        report.setEsr_revamp(0); report.setCandidate_fields(candidateFields); report.setReport_content_json("\"{\\\"name\\\":\\\"John\\\",\\\"age\\\":30}\"");
        return report;
    }

    private String createReportContentJson(String selected_candidates_ids) {
		JavaFakerCompany javaFakerCompany = new JavaFakerCompany();
		JavaFakerContact javaFakerContact = new JavaFakerContact();
		JavaFakerCandidate javaFakerCandidate = new JavaFakerCandidate();
		JavaFakerJob javaFakerJob = new JavaFakerJob();
		JavaFakerUser javaFakerUser = new JavaFakerUser();
		Response usersResponse = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		String userId = usersResponse.jsonPath().getString("data[0].id");

		return String.format(
				"{\"executive_search_title_content\":\"%s\"}",
				String.join(" ",
						javaFakerContact.getContactContent(javaFakerCompany.getCompanyName(), javaFakerCandidate.getJobTitle()),
						javaFakerJob.getJobContent(javaFakerContact.getContactNumber()),
						javaFakerCompany.getCompanyContent(),
						javaFakerCandidate.getCandidateContent(selected_candidates_ids),
						javaFakerUser.getUserContent(userId)
				)
		);
	}

	@DataProvider
	public Object[][] getExecutiveSearchReportData() {
		String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

		AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
		Response getJobResponse = albatrossFunctions.getJobResponse(albatrossURL, albatrossAuthTkn, jobSlug);

		// Verify Response using Assertion and Jsonpath
		JsonPath jpJob = getJobResponse.jsonPath();
		int jobID = jpJob.get("data.job.id");

		// Get all templates IDs

		int titleTemplateID = 0;
		int candidateProfileTemplateID = 0;

		for (int i = 1; i < 3; i++) {

			ExecutiveSummaryFunctions executiveSummaryFunctions = new ExecutiveSummaryFunctions();
			Response response = executiveSummaryFunctions.createTemplateForExecutiveSummary(i,
					executiveSummaryServiceURL, albatrossAuthTkn);

			response.then().statusCode(200);

			// Verify Response using Assertion and Jsonpath
			JsonPath jsonPathTemplate = response.jsonPath();

			if (i == 1)
				titleTemplateID = jsonPathTemplate.get("id");

			if (i == 2)
				candidateProfileTemplateID = jsonPathTemplate.get("id");
		}

		// Get collabrators

		JsonPath jsonGetCollabrators = listFunctions.getAllCollabrators(baseURL, ThreadManager.getAccountApiKeyMap()).jsonPath();
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

			Response getCandResponse = albatrossFunctions.getCandidateResponse(albatrossURL, albatrossAuthTkn,
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

	@DataProvider
	public Object[][] getGenerateActionData() {

		Object data[][] = { { "save", 200 }, { "download", 200 } };
		return data;

	}

}
