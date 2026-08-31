package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters;

import com.qa.api.util.TestUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.Filters.common.ContractStaffingFilterBase;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class DealFilterTestSupport extends ContractStaffingFilterBase {

    protected static DealFilterTestContext dealFilterContext;

    protected synchronized void ensureDealFilterTestData() {
        if (dealFilterContext != null) {
            return;
        }
        initializeAuthAndFunction();
        dealFilterContext = buildDealFilterTestContext();
    }

    private DealFilterTestContext buildDealFilterTestContext() {
        try {
            long ts = System.currentTimeMillis();
            Integer templateId = createDealFilterRuleTemplate();

            JsonPath sharedCompany = createDealFilterSharedCompany("FilterDealCo_" + ts, "Mumbai");
            String companySlug = sharedCompany.getString("slug");
            String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, companySlug)
                    .jsonPath().getString("slug");

            FilterDealData dealA = createDealFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "FilterDeal_Alpha_" + ts);
            Thread.sleep(2000);
            FilterDealData dealB = createDealFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "FilterDeal_Beta_" + ts);
            Thread.sleep(2000);
            FilterDealData dealC = createDealFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "FilterDeal_Gamma_" + ts);
            Thread.sleep(2000);

            FilterDealData dealD = createDealFilterEntityWithTimesheet(templateId, companySlug, contactSlug,
                    "FilterDeal_Deleted_" + ts);
            int orphanedTimesheetId = dealD.timesheetId;
            int orphanedCandidateId = dealD.candidateId;
            deleteDealFilterEntityBySlug(dealD.slug);
            Thread.sleep(1000);

            return new DealFilterTestContext(dealA, dealB, dealC, orphanedTimesheetId, orphanedCandidateId);
        } catch (Exception e) {
            throw new AssertionError("Error creating deal filter test data: " + e.getMessage(), e);
        }
    }

    private Integer createDealFilterRuleTemplate() {
        Map<String, Object> config = getFilterTestTimesheetConfig();
        String templateName = ruleEngineenFake.getTestTemplateName("DealFilterTest");
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

    private FilterDealData createDealFilterEntityWithTimesheet(Integer templateId, String companySlug,
                                                               String contactSlug, String dealName) throws Exception {
        JsonPath jsonJob = createDealFilterJob("FilterJob_" + System.nanoTime(), "Mumbai", companySlug, contactSlug);
        String jobSlug = jsonJob.getString("slug");

        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String candidateSlug = jsonCandidate.getString("slug");
        Integer realCandidateId = getRealCandidateId(albatrossAuthToken, candidateSlug);
        assertThat("Real candidate ID should be fetched", realCandidateId, notNullValue());

        assignCandidateToJob(apiAuthToken, candidateSlug, jobSlug);
        Thread.sleep(1500);

        int timesheetId = createDealFilterTimesheet(templateId, jobSlug, realCandidateId);

        JsonPath jsonDeal = createDealFilterEntity(dealName, companySlug, jobSlug, contactSlug, candidateSlug);
        String dealSlug = jsonDeal.getString("slug");
        int dealSrno = getDealFilterPublicApiId(jsonDeal, dealSlug);

        JsonPath dealJsonPath = allCrudFunctions.getDealResponse(albatrossURL, albatrossAuthToken, dealSlug).jsonPath();
        int dealId = dealJsonPath.getInt("data.deal.id");
        String resolvedDealName = dealJsonPath.getString("data.deal.name");
        String stageName = dealJsonPath.getString("data.deal.dealstagelabel");
        String ownerName = dealJsonPath.getString("data.deal.ownername");
        int ownerUserId = dealJsonPath.getInt("data.deal.ownerid");

        return new FilterDealData(dealId, dealSrno, resolvedDealName, stageName, ownerName, ownerUserId, dealSlug,
                realCandidateId, timesheetId);
    }

    private int createDealFilterTimesheet(Integer templateId, String jobSlug, Integer realCandidateId) throws Exception {
        Map<String, Object> config = getFilterTestTimesheetConfig();
        int userId = getDealFilterTimesheetUserId();

        JsonPath jobJsonPath = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job").jsonPath();
        int jobId = jobJsonPath.getInt("data.job.id");

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
        return ((Number) timesheets.get(0).get("id")).intValue();
    }

    private int getDealFilterTimesheetUserId() {
        return ThreadManager.getAccount().getAccountId();
    }

    private JsonPath createDealFilterSharedCompany(String companyName, String city) {
        JavaFakerCompany faker = new JavaFakerCompany();
        Company company = new Company(companyName, faker.getCompanyWebsite(), faker.getContactNumber(), faker.getLogoURL());
        company.setCity(city);
        company.setAddress("Deal filter automation address");
        company.setAbout_company("Shared company for deal filter automation");

        Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);
        assertThat("Create company should return 200", response.getStatusCode(), equalTo(200));
        return response.jsonPath();
    }

    private JsonPath createDealFilterJob(String jobName, String city, String companySlug, String contactSlug) {
        Job job = new Job();
        job.setName(jobName);
        job.setCompany_slug(companySlug);
        job.setContact_slug(contactSlug);
        job.setNumber_of_openings(1);
        job.setJob_type(4);
        job.setCity(city);
        job.setJob_description_text("Job created for deal filter automation");
        job.setEnable_job_application_form(1);

        Response response = RestClient.doPost("JSON", baseURL, "jobs", apiAuthToken, null, true, job);
        assertThat("Create job should return 200", response.getStatusCode(), equalTo(200));
        return response.jsonPath();
    }

    private JsonPath createDealFilterEntity(String dealName, String companySlug, String jobSlug,
                                            String contactSlug, String candidateSlug) {
        HashMap<Integer, String> fieldsMap = new HashMap<>();
        fieldsMap.put(0, dealName);
        fieldsMap.put(5, companySlug);
        fieldsMap.put(6, jobSlug);
        fieldsMap.put(7, contactSlug);
        fieldsMap.put(8, candidateSlug);
        return function.createNewDealWithSpecifiedFields(baseURL, apiAuthToken, fieldsMap).jsonPath();
    }

    private int getDealFilterPublicApiId(JsonPath createResponse, String slug) {
        Object idValue = createResponse.get("id");
        if (idValue != null) {
            return ((Number) idValue).intValue();
        }

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("deal", slug);
        Response response = RestClient.doGet("JSON", baseURL, "deals/{deal}",
                apiAuthToken, null, pathParameters, true);
        assertThat("Fetch deal by slug should return 200", response.getStatusCode(), equalTo(200));
        return response.jsonPath().getInt("id");
    }

    private Response deleteDealFilterEntityBySlug(String dealSlug) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("deal", dealSlug);
        Response response = RestClient.doDelete("JSON", baseURL, "deals/{deal}",
                apiAuthToken, null, pathParameters, true);
        assertThat("Delete deal should return 200", response.getStatusCode(), equalTo(200));
        return response;
    }

    protected String buildDealFilterValue(Integer... dealIds) {
        return buildBracketedIdFilterValue(dealIds);
    }

    protected String buildDealFilterBarLabel(String... dealNames) {
        return buildDropdownFilterBarLabel(dealNames);
    }

    protected Response searchDealFilterEntities(String searchTerm) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("search", searchTerm);
        requestBody.put("companies", false);
        requestBody.put("jobs", false);
        requestBody.put("deals", true);
        requestBody.put("ids_of_selected_records", new HashMap<>());

        Object payload = TestUtil.getSerializedJSON(requestBody);
        return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/search-entity",
                albatrossAuthToken, null, true, payload);
    }

    protected List<Map<String, Object>> getDealsFromSearchEntity(Response response) {
        List<Map<String, Object>> deals = response.jsonPath().getList("data.data.11");
        return deals == null ? Collections.emptyList() : deals;
    }

    protected Map<Integer, String> buildDealIdToNameMap() {
        Map<Integer, String> idToName = new HashMap<>();
        idToName.put(dealFilterContext.dealA.id, dealFilterContext.dealA.name);
        idToName.put(dealFilterContext.dealB.id, dealFilterContext.dealB.name);
        idToName.put(dealFilterContext.dealC.id, dealFilterContext.dealC.name);
        return idToName;
    }

    protected Set<String> dealNamesForIds(List<Integer> dealIds, Map<Integer, String> dealIdToName) {
        Set<String> names = new HashSet<>();
        for (Integer id : dealIds) {
            String name = dealIdToName.get(id);
            if (name != null) {
                names.add(name);
            }
        }
        return names;
    }

    protected String resolveDealName(JSONObject timesheet) {
        if (!timesheet.isNull("dealName") && !timesheet.optString("dealName", "").isEmpty()) {
            return timesheet.optString("dealName");
        }
        JSONArray deals = timesheet.optJSONArray("deals");
        if (deals != null && deals.length() > 0) {
            JSONObject deal = deals.getJSONObject(0);
            if (!deal.isNull("name")) {
                return deal.optString("name", "");
            }
        }
        JSONObject deal = timesheet.optJSONObject("deal");
        if (deal != null && !deal.isNull("name")) {
            return deal.optString("name", "");
        }
        return "";
    }

    protected boolean isDealNameEmpty(JSONObject timesheet) {
        String name = resolveDealName(timesheet);
        return name == null || name.trim().isEmpty();
    }

}
