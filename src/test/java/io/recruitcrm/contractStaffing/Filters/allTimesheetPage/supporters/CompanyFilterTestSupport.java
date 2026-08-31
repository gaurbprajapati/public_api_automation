package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters;

import com.qa.api.util.TestUtil;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.Filters.common.ContractStaffingFilterBase;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.CompanyFilterTestContext;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.FilterCompanyData;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class CompanyFilterTestSupport extends ContractStaffingFilterBase {

    protected static CompanyFilterTestContext companyFilterContext;

    protected synchronized void ensureCompanyFilterTestData() {
        if (companyFilterContext != null) {
            return;
        }
        initializeAuthAndFunction();
        companyFilterContext = buildCompanyFilterTestContext();
    }

    private CompanyFilterTestContext buildCompanyFilterTestContext() {
        try {
            long ts = System.currentTimeMillis();
            Integer templateId = createCompanyFilterRuleTemplate();

            FilterCompanyData companyA = createCompanyFilterEntityWithTimesheet(templateId,
                    "FilterCo_Alpha_" + ts, "Mumbai");
            Thread.sleep(1500);
            FilterCompanyData companyB = createCompanyFilterEntityWithTimesheet(templateId,
                    "Filter Co Beta " + ts, "Delhi");
            Thread.sleep(1500);
            FilterCompanyData companyC = createCompanyFilterEntityWithTimesheet(templateId,
                    "FilterCo_Gamma_" + ts, "Bangalore");
            Thread.sleep(1500);

            FilterCompanyData companyD = createCompanyFilterEntityWithTimesheet(templateId,
                    "FilterCo_Deleted_" + ts, "Pune");
            int orphanedTimesheetId = companyD.timesheetId;
            deleteCompanyFilterEntityBySlug(companyD.slug);
            Thread.sleep(1500);

            return new CompanyFilterTestContext(companyA, companyB, companyC, orphanedTimesheetId);
        } catch (Exception e) {
            throw new AssertionError("Error creating company filter test data: " + e.getMessage(), e);
        }
    }

    private Integer createCompanyFilterRuleTemplate() {
        Map<String, Object> config = getFilterTestTimesheetConfig();
        String templateName = ruleEngineenFake.getTestTemplateName("CompanyFilterTest");
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

    private FilterCompanyData createCompanyFilterEntityWithTimesheet(Integer templateId,
                                                                     String companyName, String city) throws Exception {
        Map<String, Object> config = getFilterTestTimesheetConfig();

        JsonPath jsonCompany = createCompanyFilterEntity(companyName, city);
        String companySlug = jsonCompany.getString("slug");
        String resolvedCompanyName = jsonCompany.getString("company_name");
        int companySrno = getCompanyFilterPublicApiId(jsonCompany, companySlug);

        JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
        String contactSlug = jsonContact.getString("slug");

        JsonPath jsonJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
        String jobSlug = jsonJob.getString("slug");

        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        String candidateSlug = jsonCandidate.getString("slug");
        Integer realCandidateId = getRealCandidateId(albatrossAuthToken, candidateSlug);
        assertThat("Real candidate ID should be fetched", realCandidateId, notNullValue());

        int userId = getCompanyFilterTimesheetUserId();

        JsonPath jobJsonPath = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job").jsonPath();
        int jobId = jobJsonPath.getInt("data.job.id");
        int companyId = jobJsonPath.getInt("data.job.companyid");
        assertThat("Company ID should be fetched", companyId, greaterThan(0));

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
        return new FilterCompanyData(companyId, companySrno, resolvedCompanyName, city, companySlug, timesheetId);
    }

    private int getCompanyFilterTimesheetUserId() {
        Response usersResponse = function.getUsers(baseURL, apiAuthToken);
        return usersResponse.jsonPath().getInt("[0].id");
    }

    private JsonPath createCompanyFilterEntity(String companyName, String city) {
        JavaFakerCompany faker = new JavaFakerCompany();
        Company company = new Company(companyName, faker.getCompanyWebsite(), faker.getContactNumber(), faker.getLogoURL());
        company.setCity(city);
        company.setAddress("Filter automation address");
        company.setAbout_company("Company created for timesheet filter automation");

        Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);
        assertThat("Create company should return 200", response.getStatusCode(), equalTo(200));
        return response.jsonPath();
    }

    private Response deleteCompanyFilterEntityBySlug(String companySlug) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("company", companySlug);
        Response response = RestClient.doDelete("JSON", baseURL, "companies/{company}",
                apiAuthToken, null, pathParameters, true);
        assertThat("Delete company should return 200", response.getStatusCode(), equalTo(200));
        return response;
    }

    private int getCompanyFilterPublicApiId(JsonPath createResponse, String slug) {
        Object idValue = createResponse.get("id");
        if (idValue != null) {
            return ((Number) idValue).intValue();
        }

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("company", slug);
        Response response = RestClient.doGet("JSON", baseURL, "companies/{company}",
                apiAuthToken, null, pathParameters, true);
        assertThat("Fetch company by slug should return 200", response.getStatusCode(), equalTo(200));
        return response.jsonPath().getInt("id");
    }

    protected String buildCompanyFilterValue(Integer... companyIds) {
        return buildBracketedIdFilterValue(companyIds);
    }

    protected String buildCompanyFilterBarLabel(String... companyNames) {
        return buildDropdownFilterBarLabel(companyNames);
    }

    protected Response searchCompanyFilterEntities(String searchTerm) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("search", searchTerm);
        requestBody.put("companies", true);
        requestBody.put("jobs", false);
        requestBody.put("deals", false);
        requestBody.put("fromContractorsListPage", true);
        requestBody.put("ids_of_selected_records", new HashMap<>());

        Object payload = TestUtil.getSerializedJSON(requestBody);
        return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/search-entity",
                albatrossAuthToken, null, true, payload);
    }

    protected List<Map<String, Object>> getCompaniesFromSearchEntity(Response response) {
        List<Map<String, Object>> companies = response.jsonPath().getList("data.data.3");
        return companies == null ? Collections.emptyList() : companies;
    }

    protected String resolveCompanyName(JSONObject timesheet) {
        if (!timesheet.isNull("companyName") && !timesheet.optString("companyName", "").isEmpty()) {
            return timesheet.optString("companyName");
        }
        JSONObject company = timesheet.optJSONObject("company");
        if (company != null && !company.isNull("name")) {
            return company.optString("name", "");
        }
        JSONObject job = timesheet.optJSONObject("job");
        if (job != null && !job.isNull("companyName")) {
            return job.optString("companyName", "");
        }
        return "";
    }

    protected boolean isCompanyNameEmpty(JSONObject timesheet) {
        String name = resolveCompanyName(timesheet);
        return name == null || name.trim().isEmpty();
    }

    protected List<Integer> parseCompanyIds(String filterValue) {
        return TimesheetFilterTestSupport.parseBracketedIntList(filterValue);
    }

    protected Map<Integer, String> buildCompanyIdToNameMap() {
        Map<Integer, String> idToName = new HashMap<>();
        idToName.put(companyFilterContext.companyA.id, companyFilterContext.companyA.name);
        idToName.put(companyFilterContext.companyB.id, companyFilterContext.companyB.name);
        idToName.put(companyFilterContext.companyC.id, companyFilterContext.companyC.name);
        return idToName;
    }

    protected Set<String> companyNamesForIds(List<Integer> companyIds, Map<Integer, String> companyIdToName) {
        Set<String> names = new HashSet<>();
        for (Integer id : companyIds) {
            String name = companyIdToName.get(id);
            if (name != null) {
                names.add(name);
            }
        }
        return names;
    }
}
