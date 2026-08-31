package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters;

import com.qa.api.util.TestUtil;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.Filters.common.ContractStaffingFilterBase;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.FilterJobData;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.JobFilterTestContext;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class JobFilterTestSupport extends ContractStaffingFilterBase {

    protected static JobFilterTestContext jobFilterContext;

    protected synchronized void ensureJobFilterTestData() {
        if (jobFilterContext != null) {
            return;
        }
        initializeAuthAndFunction();
        jobFilterContext = buildJobFilterTestContext(getFilterTestTimesheetConfig());
    }

    protected JobFilterTestContext buildJobFilterTestContext(Map<String, Object> timesheetConfig) {
        try {
            long ts = System.currentTimeMillis();
            Integer templateId = createJobFilterRuleTemplate();

            JsonPath sharedCompany = createJobFilterSharedCompany("FilterJobCo_" + ts, "Mumbai");
            String companySlug = sharedCompany.getString("slug");
            String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, companySlug)
                    .jsonPath().getString("slug");

            FilterJobData jobA = createJobFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "FilterJob_Alpha_" + ts, "Mumbai", timesheetConfig);
            Thread.sleep(1500);
            FilterJobData jobB = createJobFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "Filter Job Beta " + ts, "Delhi", timesheetConfig);
            Thread.sleep(1500);
            FilterJobData jobC = createJobFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "FilterJob_Gamma_" + ts, "Bangalore", timesheetConfig);
            Thread.sleep(1500);

            FilterJobData jobD = createJobFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "FilterJob_Deleted_" + ts, "Pune", timesheetConfig);
            int orphanedTimesheetId = jobD.timesheetId;
            int orphanedCandidateId = jobD.candidateId;
            deleteJobFilterEntityBySlug(jobD.slug);
            Thread.sleep(1500);

            return new JobFilterTestContext(jobA, jobB, jobC, orphanedTimesheetId, orphanedCandidateId);
        } catch (Exception e) {
            throw new AssertionError("Error creating job filter test data: " + e.getMessage(), e);
        }
    }

    protected JobFilterTestContext activeJobFilterContext() {
        return jobFilterContext;
    }

    protected Integer createJobFilterRuleTemplate() {
        Map<String, Object> config = getFilterTestTimesheetConfig();
        String templateName = ruleEngineenFake.getTestTemplateName("JobFilterTest");
        List<Integer> workDayIds = Arrays.asList(1, 2, 3, 4, 5);
        List<Map<String, Object>> customRules = buildCustomRulesFromDescription(
                (String) config.get("rulesApplied"), workDayIds,
                (Double) config.get("payRate"), (Double) config.get("billRate"), "Shift");
        Integer templateId = createRuleTemplate(albatrossAuthToken, templateName, workDayIds,
                (String) config.get("regularHours"), customRules, (String) config.get("breakBillable"),
                SHIFTS_LOGGING, (Integer) config.get("breakTimeThreshold"));
        assertThat("Template should be created", templateId, notNullValue());
        return templateId;
    }

    protected FilterJobData createJobFilterEntityWithTimesheet(Integer templateId, String companySlug,
                                                             String contactSlug, String jobName,
                                                             String city, Map<String, Object> config) throws Exception {

        JsonPath jsonJob = createJobFilterEntity(jobName, city, companySlug, contactSlug);
        String jobSlug = jsonJob.getString("slug");
        String resolvedJobName = jsonJob.getString("name");
        int jobSrno = getJobFilterPublicApiId(jsonJob, jobSlug);

        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String candidateSlug = jsonCandidate.getString("slug");
        Integer realCandidateId = getRealCandidateId(albatrossAuthToken, candidateSlug);
        assertThat("Real candidate ID should be fetched", realCandidateId, notNullValue());

        int userId = getJobFilterTimesheetUserId();

        JsonPath jobJsonPath = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job").jsonPath();
        int jobId = jobJsonPath.getInt("data.job.id");
        assertThat("Job ID should be fetched", jobId, greaterThan(0));

        assignCandidateToJob(apiAuthToken, candidateSlug, jobSlug);
        Thread.sleep(1500);

        Response timesheetResponse = enableWeeklyTimesheetWithDynamicValues(albatrossAuthToken, jobId,
                realCandidateId, userId, templateId, (String) config.get("dayPattern"),
                (String) config.get("regularHours"), (String) config.get("rulesApplied"),
                (Double) config.get("payRate"), (Double) config.get("billRate"),
                (String) config.get("breakBillable"), (Long) config.get("jobStartDate"),
                (Long) config.get("jobEndDate"), (Integer) config.get("timesheetFrequency"),
                (Integer) config.get("timesheetStartDay"), (Integer) config.get("payCurrencyId"),
                (Integer) config.get("billCurrencyId"), (Integer) config.get("breakTimeThreshold"));
        assertThat("Timesheet settings should succeed", timesheetResponse.getStatusCode(), equalTo(200));
        Thread.sleep(1500);

        Response freeSlotsResponse = getFreeSlotsForTimesheet(albatrossAuthToken, realCandidateId,
                (Long) config.get("jobStartDate"), (Long) config.get("jobEndDate"),
                (Integer) config.get("timesheetFrequency"), (Integer) config.get("timesheetStartDay"));
        assertThat("Free slots should return 200", freeSlotsResponse.getStatusCode(), equalTo(200));

        Response createTimesheetResponse = createTimesheetFromSlots(albatrossAuthToken, jobId,
                realCandidateId, freeSlotsResponse);
        assertThat("Create timesheet should return 200", createTimesheetResponse.getStatusCode(), equalTo(200));

        Response timesheetsResponse = getTimesheetsForContractor(albatrossAuthToken, jobId, realCandidateId);
        assertThat("Get timesheets should return 200", timesheetsResponse.getStatusCode(), equalTo(200));

        List<Map<String, Object>> timesheets = timesheetsResponse.jsonPath().getList("data");
        assertThat("Timesheets should not be empty", timesheets.isEmpty(), is(false));

        int timesheetId = ((Number) timesheets.get(0).get("id")).intValue();
        return new FilterJobData(jobId, jobSrno, resolvedJobName, city, jobSlug, realCandidateId, timesheetId);
    }

    private int getJobFilterTimesheetUserId() {
        Response usersResponse = function.getUsers(baseURL, apiAuthToken);
        return usersResponse.jsonPath().getInt("[0].id");
    }

    protected JsonPath createJobFilterSharedCompany(String companyName, String city) {
        JavaFakerCompany faker = new JavaFakerCompany();
        Company company = new Company(companyName, faker.getCompanyWebsite(), faker.getContactNumber(), faker.getLogoURL());
        company.setCity(city);
        company.setAddress("Job filter automation address");
        company.setAbout_company("Shared company for job filter automation");

        Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);
        assertThat("Create company should return 200", response.getStatusCode(), equalTo(200));
        return response.jsonPath();
    }

    private JsonPath createJobFilterEntity(String jobName, String city, String companySlug, String contactSlug) {
        Job job = new Job();
        job.setName(jobName);
        job.setCompany_slug(companySlug);
        job.setContact_slug(contactSlug);
        job.setNumber_of_openings(1);
        job.setJob_type(4);
        job.setCity(city);
        job.setJob_description_text("Job created for timesheet filter automation");
        job.setEnable_job_application_form(1);

        Response response = RestClient.doPost("JSON", baseURL, "jobs", apiAuthToken, null, true, job);
        assertThat("Create job should return 200", response.getStatusCode(), equalTo(200));
        return response.jsonPath();
    }

    private Response deleteJobFilterEntityBySlug(String jobSlug) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("job", jobSlug);
        Response response = RestClient.doDelete("JSON", baseURL, "jobs/{job}",
                apiAuthToken, null, pathParameters, true);
        assertThat("Delete job should return 200", response.getStatusCode(), equalTo(200));
        return response;
    }

    private int getJobFilterPublicApiId(JsonPath createResponse, String slug) {
        Object idValue = createResponse.get("id");
        if (idValue != null) {
            return ((Number) idValue).intValue();
        }

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("job", slug);
        Response response = RestClient.doGet("JSON", baseURL, "jobs/{job}",
                apiAuthToken, null, pathParameters, true);
        assertThat("Fetch job by slug should return 200", response.getStatusCode(), equalTo(200));
        return response.jsonPath().getInt("id");
    }

    protected String buildJobFilterValue(Integer... jobIds) {
        return buildBracketedIdFilterValue(jobIds);
    }

    protected String buildJobFilterBarLabel(String... jobNames) {
        return buildDropdownFilterBarLabel(jobNames);
    }

    protected Response searchJobFilterEntities(String searchTerm) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("search", searchTerm);
        requestBody.put("companies", false);
        requestBody.put("jobs", true);
        requestBody.put("deals", false);
        requestBody.put("fromContractorsListPage", true);
        requestBody.put("ids_of_selected_records", new HashMap<>());

        Object payload = TestUtil.getSerializedJSON(requestBody);
        return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/search-entity",
                albatrossAuthToken, null, true, payload);
    }

    protected List<Map<String, Object>> getJobsFromSearchEntity(Response response) {
        List<Map<String, Object>> jobs = response.jsonPath().getList("data.data.4");
        return jobs == null ? Collections.emptyList() : jobs;
    }

    protected String resolveJobName(JSONObject timesheet) {
        if (!timesheet.isNull("jobName") && !timesheet.optString("jobName", "").isEmpty()) {
            return timesheet.optString("jobName");
        }
        JSONObject job = timesheet.optJSONObject("job");
        if (job != null) {
            if (!job.isNull("name") && !job.optString("name", "").isEmpty()) {
                return job.optString("name");
            }
            if (!job.isNull("title") && !job.optString("title", "").isEmpty()) {
                return job.optString("title");
            }
        }
        return "";
    }

    protected boolean isJobNameEmpty(JSONObject timesheet) {
        String name = resolveJobName(timesheet);
        return name == null || name.trim().isEmpty();
    }

    protected Map<Integer, String> buildJobIdToNameMap() {
        JobFilterTestContext ctx = activeJobFilterContext();
        Map<Integer, String> idToName = new HashMap<>();
        idToName.put(ctx.jobA.id, ctx.jobA.name);
        idToName.put(ctx.jobB.id, ctx.jobB.name);
        idToName.put(ctx.jobC.id, ctx.jobC.name);
        return idToName;
    }

    protected Set<String> jobNamesForIds(List<Integer> jobIds, Map<Integer, String> jobIdToName) {
        Set<String> names = new HashSet<>();
        for (Integer id : jobIds) {
            String name = jobIdToName.get(id);
            if (name != null) {
                names.add(name);
            }
        }
        return names;
    }
}
