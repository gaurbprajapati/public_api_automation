package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters;

import com.qa.api.util.TestUtil;
import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.pojo.Company;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.Filters.common.ContractStaffingFilterBase;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.FilterPeriodTimesheetData;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.PeriodFilterTestContext;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.*;

import static io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.TimesheetFilterTestSupport.*;
import static io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.TimesheetPeriodFilterDateUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class PeriodFilterTestSupport extends ContractStaffingFilterBase {

    protected static PeriodFilterTestContext periodFilterContext;

    protected synchronized void ensurePeriodFilterTestData() {
        if (periodFilterContext != null) {
            return;
        }
        initializeAuthAndFunction();
        periodFilterContext = buildPeriodFilterTestContext();
    }

    private PeriodFilterTestContext buildPeriodFilterTestContext() {
        try {
            long ts = System.currentTimeMillis();
            LocalDate today = LocalDate.now();
            long daySeconds = 86400L;
            long todayEpoch = System.currentTimeMillis() / 1000L;

            Map<String, Object> config = copyTimesheetConfigWithJobDates(
                    todayEpoch - (450L * daySeconds), todayEpoch + (60L * daySeconds));

            Integer templateId = createPeriodFilterRuleTemplate();
            JsonPath sharedCompany = createPeriodFilterSharedCompany("FilterPeriodCo_" + ts, "Mumbai");
            String companySlug = sharedCompany.getString("slug");
            String contactSlug = function.createNewContact_POST(baseURL, apiAuthToken, companySlug)
                    .jsonPath().getString("slug");

            JsonPath jsonJob = createPeriodFilterJob("FilterPeriodJob_" + ts, companySlug, contactSlug);
            String jobSlug = jsonJob.getString("slug");
            int jobId = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job")
                    .jsonPath().getInt("data.job.id");

            JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String candidateSlug = jsonCandidate.getString("slug");
            Integer candidateId = getRealCandidateId(albatrossAuthToken, candidateSlug);
            assertThat("Real candidate ID should be fetched", candidateId, notNullValue());

            assignCandidateToJob(apiAuthToken, candidateSlug, jobSlug);
            Thread.sleep(1500);

            int userId = function.getUsers(baseURL, apiAuthToken).jsonPath().getInt("[0].id");
            Response timesheetSettingsResponse = enableWeeklyTimesheetWithDynamicValues(albatrossAuthToken, jobId,
                    candidateId, userId, templateId, (String) config.get("dayPattern"),
                    (String) config.get("regularHours"), (String) config.get("rulesApplied"),
                    (Double) config.get("payRate"), (Double) config.get("billRate"),
                    (String) config.get("breakBillable"), (Long) config.get("jobStartDate"),
                    (Long) config.get("jobEndDate"), (Integer) config.get("timesheetFrequency"),
                    (Integer) config.get("timesheetStartDay"), (Integer) config.get("payCurrencyId"),
                    (Integer) config.get("billCurrencyId"), (Integer) config.get("breakTimeThreshold"));
            assertThat("Timesheet settings should succeed", timesheetSettingsResponse.getStatusCode(), equalTo(200));
            Thread.sleep(1500);

            Response freeSlotsResponse = getFreeSlotsForTimesheet(albatrossAuthToken, candidateId,
                    (Long) config.get("jobStartDate"), (Long) config.get("jobEndDate"),
                    (Integer) config.get("timesheetFrequency"), (Integer) config.get("timesheetStartDay"));
            assertThat("Free slots should return 200", freeSlotsResponse.getStatusCode(), equalTo(200));

            List<Map<String, Object>> slots = freeSlotsResponse.jsonPath().getList("data");
            assertThat("Free slots should not be empty", slots == null || slots.isEmpty(), is(false));

            long currentInstant = startOfDayEpoch(today);
            long lastWeekInstant = startOfDayEpoch(today.minusWeeks(1));
            long lastMonthInstant = startOfDayEpoch(today.minusMonths(1));
            long lastQuarterInstant = startOfDayEpoch(today.minusMonths(3));
            long lastYearInstant = startOfDayEpoch(today.minusYears(1));
            long futureInstant = startOfDayEpoch(today.plusWeeks(2));
            long distantPastInstant = startOfDayEpoch(today.minusDays(200));

            LinkedHashMap<Integer, Map<String, Object>> uniqueSlotsByIndex = new LinkedHashMap<>();
            LinkedHashMap<Integer, String> labelBySlotIndex = new LinkedHashMap<>();
            addUniqueSlot(slots, uniqueSlotsByIndex, labelBySlotIndex, currentInstant, "CURRENT");
            addUniqueSlot(slots, uniqueSlotsByIndex, labelBySlotIndex, lastWeekInstant, "LAST_WEEK");
            addUniqueSlot(slots, uniqueSlotsByIndex, labelBySlotIndex, lastMonthInstant, "LAST_MONTH");
            addUniqueSlot(slots, uniqueSlotsByIndex, labelBySlotIndex, lastQuarterInstant, "LAST_QUARTER");
            addUniqueSlot(slots, uniqueSlotsByIndex, labelBySlotIndex, lastYearInstant, "LAST_YEAR");
            addUniqueSlot(slots, uniqueSlotsByIndex, labelBySlotIndex, futureInstant, "FUTURE");
            addUniqueSlot(slots, uniqueSlotsByIndex, labelBySlotIndex, distantPastInstant, "DISTANT_PAST");

            List<Map<String, Object>> selectedSlots = new ArrayList<>(uniqueSlotsByIndex.values());
            createTimesheetsFromSlots(albatrossAuthToken, jobId, candidateId, selectedSlots);
            Thread.sleep(2000);

            Response timesheetsResponse = getTimesheetsForContractor(albatrossAuthToken, jobId, candidateId);
            assertThat("Get timesheets should return 200", timesheetsResponse.getStatusCode(), equalTo(200));
            List<Map<String, Object>> timesheets = timesheetsResponse.jsonPath().getList("data");
            assertThat("Created timesheets should exist", timesheets.size(), greaterThanOrEqualTo(selectedSlots.size()));

            Map<Long, FilterPeriodTimesheetData> timesheetByStart = mapTimesheetsByStartDate(timesheets);
            Map<String, FilterPeriodTimesheetData> timesheetsByLabel = new LinkedHashMap<>();
            for (Map.Entry<Integer, String> entry : labelBySlotIndex.entrySet()) {
                Map<String, Object> slot = uniqueSlotsByIndex.get(entry.getKey());
                timesheetsByLabel.put(entry.getValue(), resolveTimesheetData(timesheetByStart, slot, entry.getValue()));
            }

            FilterPeriodTimesheetData currentPeriod = timesheetsByLabel.get("CURRENT");
            FilterPeriodTimesheetData lastWeekPeriod = timesheetsByLabel.get("LAST_WEEK");
            FilterPeriodTimesheetData lastMonthPeriod = timesheetsByLabel.get("LAST_MONTH");
            FilterPeriodTimesheetData lastQuarterPeriod = timesheetsByLabel.get("LAST_QUARTER");
            FilterPeriodTimesheetData lastYearPeriod = timesheetsByLabel.get("LAST_YEAR");
            FilterPeriodTimesheetData futurePeriod = timesheetsByLabel.get("FUTURE");
            FilterPeriodTimesheetData distantPastPeriod = timesheetsByLabel.get("DISTANT_PAST");

            List<Integer> seededIds = new ArrayList<>();
            for (FilterPeriodTimesheetData timesheetData : timesheetsByLabel.values()) {
                seededIds.add(timesheetData.timesheetId);
            }
            List<Integer> searchableTimesheetIds = resolveSearchableTimesheetIds(seededIds, timesheets);

            long equalToPeriodStartEpoch = currentPeriod.startDate;
            long beforeFilterDateEpoch = startOfDayEpoch(today.minusDays(30));
            long beforePeriodStartBoundaryEpoch = startOfDayEpoch(
                    toLocalDate(currentPeriod.startDate).minusDays(1));
            long afterFilterDateEpoch = startOfDayEpoch(today.minusDays(14));

            long betweenStart = distantPastPeriod.startDate;
            long betweenEnd = currentPeriod.endDate;
            String betweenFilterValue = buildBetweenFilterValue(betweenStart, betweenEnd);
            String betweenFilterBarLabel = formatDateRangeLabel(betweenStart, betweenEnd);
            String exactCurrentPeriodBetweenValue = buildBetweenFilterValue(
                    currentPeriod.startDate, currentPeriod.endDate);
            String exactCurrentPeriodBetweenBarLabel = formatDateRangeLabel(
                    currentPeriod.startDate, currentPeriod.endDate);

            return new PeriodFilterTestContext(today, currentPeriod, lastWeekPeriod,
                    lastMonthPeriod, lastQuarterPeriod, lastYearPeriod, futurePeriod, distantPastPeriod,
                    searchableTimesheetIds, equalToPeriodStartEpoch,
                    beforeFilterDateEpoch, beforePeriodStartBoundaryEpoch, afterFilterDateEpoch,
                    betweenFilterValue, betweenFilterBarLabel,
                    exactCurrentPeriodBetweenValue, exactCurrentPeriodBetweenBarLabel);
        } catch (Exception e) {
            throw new AssertionError("Error creating period filter test data: " + e.getMessage(), e);
        }
    }

    protected boolean timesheetPeriodMatchesFilter(long periodStart, long periodEnd,
                                                   String filterType, Object filterValue,
                                                   LocalDate referenceDate) {
        switch (filterType) {
            case "is":
                return matchesIsPreset(periodStart, periodEnd, String.valueOf(filterValue), referenceDate);
            case "is_equal_to":
                return matchesIsEqualTo(periodStart, periodEnd, toLong(filterValue));
            case "has_any_value":
                return periodStart > 0 && periodEnd >= periodStart;
            case "is_before":
                return matchesIsBefore(periodStart, periodEnd, toLong(filterValue));
            case "is_after":
                return matchesIsAfter(periodStart, periodEnd, toLong(filterValue));
            case "is_between":
                return matchesIsBetween(periodStart, periodEnd, String.valueOf(filterValue));
            case "is_not_between":
                return matchesIsNotBetween(periodStart, periodEnd, String.valueOf(filterValue));
            case "is_mt":
                return matchesIsMoreThan(periodStart, periodEnd, Integer.parseInt(String.valueOf(filterValue)),
                        referenceDate);
            case "is_lt":
                return matchesIsLessThan(periodStart, periodEnd, Integer.parseInt(String.valueOf(filterValue)),
                        referenceDate);
            default:
                throw new IllegalArgumentException("Unsupported filter type: " + filterType);
        }
    }

    protected long readPeriodStart(JSONObject timesheet) {
        JSONObject period = timesheet.getJSONObject("timesheetPeriod");
        return period.getLong("timesheetStartDate");
    }

    protected long readPeriodEnd(JSONObject timesheet) {
        JSONObject period = timesheet.getJSONObject("timesheetPeriod");
        return period.getLong("timesheetEndDate");
    }

    protected List<Integer> getAllSeededTimesheetIds() {
        return new ArrayList<>(periodFilterContext.searchableTimesheetIds);
    }

    protected void validateStrictPeriodFilteredData(JSONArray data, String filterType, Object filterValue,
                                                    String validationMode, String testId) {
        List<Integer> seededIds = getAllSeededTimesheetIds();

        if ("SEEDED_EMPTY".equals(validationMode)) {
            assertNoSeededTimesheetsPresent(data, seededIds, testId);
            return;
        }

        assertThat(testId + ": Should return timesheets", data.length(), greaterThan(0));
        assertAtLeastOneSeededTimesheetPresent(data, seededIds, testId);
        assertSeededResultsMatchFilter(data, filterType, filterValue, seededIds, testId);
    }

    protected void assertAtLeastOneSeededTimesheetPresent(JSONArray data, List<Integer> seededIds, String testId) {
        Set<Integer> seededIdSet = new HashSet<>(seededIds);
        boolean found = false;
        for (int i = 0; i < data.length(); i++) {
            if (seededIdSet.contains(data.getJSONObject(i).getInt("id"))) {
                found = true;
                break;
            }
        }
        assertThat(testId + ": At least one seeded timesheet should be present in filter results", found, is(true));
    }

    protected void assertSeededResultsMatchFilter(JSONArray data, String filterType, Object filterValue,
                                                  List<Integer> seededIds, String testId) {
        Set<Integer> seededIdSet = new HashSet<>(seededIds);
        for (int i = 0; i < data.length(); i++) {
            JSONObject timesheet = data.getJSONObject(i);
            int timesheetId = timesheet.getInt("id");
            if (!seededIdSet.contains(timesheetId)) {
                continue;
            }
            long periodStart = readPeriodStart(timesheet);
            long periodEnd = readPeriodEnd(timesheet);
            assertThat(testId + ": Seeded timesheet " + timesheetId + " period should match filter",
                    timesheetPeriodMatchesFilter(periodStart, periodEnd, filterType, filterValue,
                            periodFilterContext.referenceDate), is(true));
        }
    }

    protected Object[] periodFilterRow(String testId, String filterType, Object filterValue,
                                       String filterBarLabel, String validationMode) {
        return new Object[]{testId, filterType, filterValue, filterBarLabel, validationMode};
    }

    private Integer createPeriodFilterRuleTemplate() {
        Map<String, Object> config = getFilterTestTimesheetConfig();
        String templateName = ruleEngineenFake.getTestTemplateName("PeriodFilterTest");
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

    private JsonPath createPeriodFilterSharedCompany(String companyName, String city) {
        JavaFakerCompany faker = new JavaFakerCompany();
        Company company = new Company(companyName, faker.getCompanyWebsite(), faker.getContactNumber(), faker.getLogoURL());
        company.setCity(city);
        company.setAddress("Period filter automation address");
        company.setAbout_company("Shared company for period filter automation");

        Response response = RestClient.doPost("JSON", baseURL, "companies", apiAuthToken, null, true, company);
        assertThat("Create company should return 200", response.getStatusCode(), equalTo(200));
        return response.jsonPath();
    }

    private JsonPath createPeriodFilterJob(String jobName, String companySlug, String contactSlug) {
        Job job = new Job();
        job.setName(jobName);
        job.setCompany_slug(companySlug);
        job.setContact_slug(contactSlug);
        job.setNumber_of_openings(1);
        job.setJob_type(4);
        job.setCity("Mumbai");
        job.setJob_description_text("Job created for period filter automation");
        job.setEnable_job_application_form(1);

        Response response = RestClient.doPost("JSON", baseURL, "jobs", apiAuthToken, null, true, job);
        assertThat("Create job should return 200", response.getStatusCode(), equalTo(200));
        return response.jsonPath();
    }

    private void addUniqueSlot(List<Map<String, Object>> slots,
                               LinkedHashMap<Integer, Map<String, Object>> uniqueSlotsByIndex,
                               LinkedHashMap<Integer, String> labelBySlotIndex,
                               long instant, String label) {
        int index = findSlotIndexContaining(slots, instant);
        uniqueSlotsByIndex.putIfAbsent(index, slots.get(index));
        labelBySlotIndex.putIfAbsent(index, label);
    }

    private List<Integer> resolveSearchableTimesheetIds(List<Integer> seededIds,
                                                        List<Map<String, Object>> timesheets) {
        Set<Integer> seededIdSet = new HashSet<>(seededIds);
        List<Integer> searchableIds = new ArrayList<>();
        for (Map<String, Object> row : timesheets) {
            int timesheetId = ((Number) row.get("id")).intValue();
            if (seededIdSet.contains(timesheetId)) {
                searchableIds.add(timesheetId);
            }
        }
        assertThat("All seeded timesheets should be available on contractor list", searchableIds.size(),
                equalTo(seededIds.size()));
        return searchableIds;
    }

    private int findSlotIndexContaining(List<Map<String, Object>> slots, long instant) {
        for (int i = 0; i < slots.size(); i++) {
            Map<String, Object> slot = slots.get(i);
            long start = ((Number) slot.get("startDate")).longValue();
            long end = ((Number) slot.get("endDate")).longValue();
            if (instant >= start && instant <= end) {
                return i;
            }
        }
        throw new AssertionError("No slot found containing epoch " + instant);
    }

    private Response createTimesheetsFromSlots(String authToken, Integer jobId, Integer candidateId,
                                               List<Map<String, Object>> slots) {
        List<Map<String, Object>> timesheetDates = new ArrayList<>();
        for (Map<String, Object> slot : slots) {
            Map<String, Object> dateRange = new HashMap<>();
            dateRange.put("startDate", ((Number) slot.get("startDate")).longValue());
            dateRange.put("endDate", ((Number) slot.get("endDate")).longValue());
            timesheetDates.add(dateRange);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("contractorIds", Arrays.asList(candidateId));
        payload.put("timesheetDates", timesheetDates);

        String jsonPayload = TestUtil.getSerializedJSON(payload);
        Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheets/jobs/" + jobId + "/contractors",
                authToken, null, true, jsonPayload);
        assertThat("Create timesheets should return 200", response.getStatusCode(), equalTo(200));
        return response;
    }

    private Map<Long, FilterPeriodTimesheetData> mapTimesheetsByStartDate(List<Map<String, Object>> timesheets) {
        Map<Long, FilterPeriodTimesheetData> byStart = new HashMap<>();
        for (Map<String, Object> row : timesheets) {
            Map<String, Object> period = (Map<String, Object>) row.get("timesheetPeriod");
            long startDate = ((Number) period.get("timesheetStartDate")).longValue();
            long endDate = ((Number) period.get("timesheetEndDate")).longValue();
            int timesheetId = ((Number) row.get("id")).intValue();
            byStart.put(startDate, new FilterPeriodTimesheetData("UNLABELED", timesheetId, startDate, endDate));
        }
        return byStart;
    }

    private FilterPeriodTimesheetData resolveTimesheetData(Map<Long, FilterPeriodTimesheetData> byStart,
                                                           Map<String, Object> slot, String label) {
        long startDate = ((Number) slot.get("startDate")).longValue();
        long endDate = ((Number) slot.get("endDate")).longValue();
        FilterPeriodTimesheetData data = byStart.get(startDate);
        assertThat("Timesheet should exist for slot " + label, data, notNullValue());
        return new FilterPeriodTimesheetData(label, data.timesheetId, startDate, endDate);
    }

    private long toLong(Object filterValue) {
        if (filterValue instanceof Number) {
            return ((Number) filterValue).longValue();
        }
        return Long.parseLong(String.valueOf(filterValue));
    }

}
