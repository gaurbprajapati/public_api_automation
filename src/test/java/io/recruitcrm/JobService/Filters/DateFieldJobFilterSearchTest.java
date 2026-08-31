package io.recruitcrm.JobService.Filters;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.hiringPipeline.HiringPipeline;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.pojo.albatross.hiringpipeline.CreateHiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.HiringStages;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.response.Response;
import com.qa.api.util.reaper.ReaperIntegration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.DateUtil;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DateFieldJobFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String apiKey;
    String albatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String email;
    Map<String, Map<String, String>> timestampScenarios;
    ConcurrentHashMap<String, String> jobKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> jobIdToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyKeyToSlugMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> contactKeyToSlugMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> contactKeyToIdMap = new ConcurrentHashMap<>();
    Map<String, String> userMap = new HashMap<>();
    Map<String, String> teamMap = new HashMap<>();
    Map<String, Integer> jobStatusIdMap = new HashMap<>();
    Map<String, Integer> qualificationIdMap = new HashMap<>();
    Map<String, Integer> hiringPipelineIdMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        apiKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        userMap = createUserMap();
        teamMap = createTeamMap();
        jobStatusIdMap = createJobStatusMap();
        qualificationIdMap = createQualificationMap();
        hiringPipelineIdMap = createHiringPipelineMap();
        createCompanies();
        createContacts();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dateFieldFilterJobSearchTestData", description = "Filter Search Test for Date Fields")
    public void dateFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);

        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        validateEntityDateField(data, filterType, filterValue, fieldName, dbField, expectedResult, "Job");
    }

    @DataProvider(name = "dateFieldFilterJobSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobDateTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String filterType = test.getString("filterType");
                String filterValue = test.getString("filterValue");

                testData.add(new Object[]{
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void createTestData() {
        JSONObject jobJson = readJsonFileFromPath("src/test/resources/testData/job_data.json");
        ConcurrentHashMap<String, String> jobSlugMap = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(jobJson.keySet().stream()
                    .filter(key -> key.startsWith("job"))
                    .map(jobKey -> CompletableFuture.runAsync(() -> {
                        JSONObject jobEntry = jobJson.getJSONObject(jobKey);
                        JSONObject payload = jobEntry.getJSONObject("payload");
                        JSONObject job = payload.getJSONObject("job");
                        String createdBy = jobEntry.has("createdBy") ? jobEntry.getString("createdBy") : "admin";
                        String authToken = getAlbatrossAuthToken(createdBy);
                        
                        replaceJobPlaceholders(job, payload);
                        
                        Response response = RestClient.doPost("JSON", albatrossURL, "/jobs", authToken, null, true, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String jobIdStr = jsonPath.getString("data.job.id");
                        String jobSlug = jsonPath.getString("data.job.slug");
                        jobSlugMap.put(jobKey, jobSlug);
                        
                        if (jobIdStr == null) {
                            return;
                        }
                        
                        Integer jobId = Integer.parseInt(jobIdStr);

                        synchronized (jobKeyToIdMap) {
                            jobKeyToIdMap.put(jobKey.toLowerCase(), String.valueOf(jobId));
                        }
                        synchronized (jobIdToKeyMap) {
                            jobIdToKeyMap.put(String.valueOf(jobId), jobKey.toLowerCase());
                        }
                    }, executor)).toArray(CompletableFuture[]::new)).join();

            List<Integer> jobIds = new ArrayList<>();
            for (String jobKey : jobKeyToIdMap.keySet()) {
                jobIds.add(Integer.parseInt(jobKeyToIdMap.get(jobKey)));
            }

            List<String> jobSlugs = new ArrayList<>(jobSlugMap.values());

            createActivityForJob(jobSlugs);
            updateJobsWithTimestampScenarios(jobIds);
            updateJobsWithLastActivityTimestamps(jobIds);

        } finally {
            executor.shutdown();
        }
    }

    private void updateJobsWithTimestampScenarios(List<Integer> jobIds) {
        timestampScenarios = createTimestampScenarios();

        int jobIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : timestampScenarios.entrySet()) {
            if (jobIndex >= timestampScenarios.size() || jobIndex >= jobIds.size()) {
                break;
            }

            Map<String, String> timestamps = scenario.getValue();
            Integer jobId = jobIds.get(jobIndex);

            JSONObject fieldsAndValues = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndValues.put(timestamp.getKey(), timestamp.getValue());
            }

            Response updateResponse = ReaperIntegration.updateJobFields(jobId, fieldsAndValues);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update the job fields timestamps");
            }
            jobIndex++;
        }
    }

    private Map<String, Map<String, String>> createTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        // Today scenario
        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("createdon", todayEpoch);
        todayTimestamps.put("updatedon", todayEpoch);
        scenarios.put("today_scenario", todayTimestamps);

        // Yesterday scenario
        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("createdon", yesterdayEpoch);
        yesterdayTimestamps.put("updatedon", yesterdayEpoch);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);

        // This week scenario
        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("createdon", thisWeekEpoch);
        thisWeekTimestamps.put("updatedon", thisWeekEpoch);
        scenarios.put("this_week_scenario", thisWeekTimestamps);

        // Last week scenario
        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("createdon", lastWeekEpoch);
        lastWeekTimestamps.put("updatedon", lastWeekEpoch);
        scenarios.put("last_week_scenario", lastWeekTimestamps);

        // This month scenario
        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("createdon", thisMonthEpoch);
        thisMonthTimestamps.put("updatedon", thisMonthEpoch);
        scenarios.put("this_month_scenario", thisMonthTimestamps);

        // Last month scenario
        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("createdon", lastMonthEpoch);
        lastMonthTimestamps.put("updatedon", lastMonthEpoch);
        scenarios.put("last_month_scenario", lastMonthTimestamps);

        // This quarter scenario
        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("createdon", thisQuarterEpoch);
        thisQuarterTimestamps.put("updatedon", thisQuarterEpoch);
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);

        // Last quarter scenario
        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("createdon", lastQuarterEpoch);
        lastQuarterTimestamps.put("updatedon", lastQuarterEpoch);
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);

        // This year scenario
        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("createdon", thisYearEpoch);
        thisYearTimestamps.put("updatedon", thisYearEpoch);
        scenarios.put("this_year_scenario", thisYearTimestamps);

        // Last year scenario
        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("createdon", lastYearEpoch);
        lastYearTimestamps.put("updatedon", lastYearEpoch);
        scenarios.put("last_year_scenario", lastYearTimestamps);

        // Static date scenario 1 - Historical data (June 15, 2022)
        Map<String, String> staticTimestamps1 = new HashMap<>();
        staticTimestamps1.put("createdon", "1655251200");  // 2022-06-15 00:00:00 UTC
        staticTimestamps1.put("updatedon", "1655251200");
        scenarios.put("static_date_scenario1", staticTimestamps1);

        // Static date scenario 2 - Previous year data (March 10, 2023)
        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("createdon", "1678406400");  // 2023-03-10 00:00:00 UTC
        staticTimestamps2.put("updatedon", "1678406400");
        scenarios.put("static_date_scenario2", staticTimestamps2);

        // Static date scenario 3 - Current year data (June 6, 2024)
        Map<String, String> staticTimestamps3 = new HashMap<>();
        staticTimestamps3.put("createdon", "1717689600");  // 2024-06-06 00:00:00 UTC
        staticTimestamps3.put("updatedon", "1717689600");
        scenarios.put("static_date_scenario3", staticTimestamps3);

        // Static date scenario 4 - Future planning data (February 14, 2025)
        Map<String, String> staticTimestamps4 = new HashMap<>();
        staticTimestamps4.put("createdon", "1739491200");  // 2025-02-14 00:00:00 UTC
        staticTimestamps4.put("updatedon", "1739491200");
        scenarios.put("static_date_scenario4", staticTimestamps4);

        return scenarios;
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        // Create filterValue object with type and value
        JSONObject filterValueObj = new JSONObject();
        
        if (filterType.equals("is_between")) {
            filterValueObj.put("type", "LONG_START_END");
            JSONObject rangeValue = new JSONObject();
            String startValue = filterValue.split(",")[0].trim();
            String endValue = filterValue.split(",")[1].trim();
            long startEpoch = dateToEpochSeconds(startValue);
            long endEpoch = dateToEpochSeconds(endValue);
            rangeValue.put("start", startEpoch);
            rangeValue.put("end", endEpoch);
            filterValueObj.put("value", rangeValue);
        } else {
            filterValueObj.put("type", filterValue_TYPE);
            if (filterType.equals("is_mt") || filterType.equals("is_lt")) {
                filterValueObj.put("value", Integer.parseInt(filterValue));
            } else if (filterType.equals("has_any_value") || filterType.equals("is_empty")) {
                // For date fields, has_any_value and is_empty use 0 as value with LONG type
                filterValueObj.put("value", filterValue.isEmpty() ? 0 : Integer.parseInt(filterValue));
            } else if (filterType.equals("is_equal_to") || filterType.equals("is_before") || filterType.equals("is_after")) {
                long epochValue = dateToEpochSeconds(filterValue);
                filterValueObj.put("value", epochValue);
            } else {
                filterValueObj.put("value", filterValue);
            }
        }   
        
        // Create filterSearchList structure
        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();
        
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");
        
        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "jobs");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        if (fieldName.equals("Last Note Added On")) {
            filter.put("entityType", "job_last_activities_t");
        } else {
            filter.put("entityType", "job");
        }
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);
        
        filtersArray.put(filter);
        groupFilterList.put("filters", filtersArray);
        
        groupFilterListArray.put(groupFilterList);
        filterSearchList.put("groupFilterList", groupFilterListArray);
        filterSearchList.put("groupJoinOperator", "AND");
        
        payload.put("filterSearchList", filterSearchList);

        return payload;
    }

    public void createCompanies() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            List<CompletableFuture<Void>> createFutures = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                String companyKey = "company" + i;
                if (companyJson.has(companyKey)) {
                    createFutures.add(CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        String createdBy = companyEntry.has("createdBy") ? companyEntry.getString("createdBy") : "admin";
                        String authToken = getAlbatrossAuthToken(createdBy);
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, authToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String slug = jsonPath.getString("data.company.slug");
                        String companyIdStr = jsonPath.getString("data.company.id");
                        
                        if (companyIdStr == null) {
                            return;
                        }
                        
                        synchronized (companyKeyToSlugMap) {
                            companyKeyToSlugMap.put(companyKey, slug);
                        }
                        synchronized (companyKeyToIdMap) {
                            companyKeyToIdMap.put(companyKey, companyIdStr);
                        }
                    }, executor));
                }
            }

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    public void createContacts() {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/contact_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            List<CompletableFuture<Void>> createFutures = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                String contactKey = "contact" + i;
                if (contactJson.has(contactKey)) {
                    createFutures.add(CompletableFuture.runAsync(() -> {
                        JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                        JSONObject payload = contactEntry.getJSONObject("payload");
                        String createdBy = contactEntry.getString("createdBy");
                        String authToken = getAlbatrossAuthToken(createdBy);
                        
                        if (payload.has("selectedcompanies")) {
                            JSONArray selectedCompanies = payload.getJSONArray("selectedcompanies");
                            for (int j = 0; j < selectedCompanies.length(); j++) {
                                JSONObject companyInfo = selectedCompanies.getJSONObject(j);
                                String slugPlaceholder = companyInfo.optString("slug", "");
                                String idPlaceholder = companyInfo.optString("id", "");
                                
                                String companyKey = null;
                                if (slugPlaceholder.startsWith("{") && slugPlaceholder.endsWith("_slug}")) {
                                    companyKey = slugPlaceholder.substring(1, slugPlaceholder.length() - 6);
                                } else if (idPlaceholder.startsWith("{") && idPlaceholder.endsWith("_id}")) {
                                    companyKey = idPlaceholder.substring(1, idPlaceholder.length() - 4);
                                }
                                
                                if (companyKey != null) {
                                    String actualSlug = companyKeyToSlugMap.get(companyKey);
                                    String actualId = companyKeyToIdMap.get(companyKey);
                                    
                                    if (actualSlug != null && actualId != null) {
                                        companyInfo.put("slug", actualSlug);
                                        companyInfo.put("id", actualId);
                                    }
                                }
                            }
                        }
                        
                        Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", authToken, null, true, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String slug = jsonPath.getString("data.contact.slug");
                        String contactIdStr = jsonPath.getString("data.contact.id");
                        
                        if (contactIdStr == null) {
                            return;
                        }
                        
                        synchronized (contactKeyToSlugMap) {
                            contactKeyToSlugMap.put(contactKey, slug);
                        }
                        synchronized (contactKeyToIdMap) {
                            contactKeyToIdMap.put(contactKey, contactIdStr);
                        }
                    }, executor));
                }
            }

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void replaceJobPlaceholders(JSONObject job, JSONObject payload) {
        if (job.has("companyid")) {
            String companyIdPlaceholder = job.getString("companyid");
            if (companyIdPlaceholder != null && companyIdPlaceholder.startsWith("{") && companyIdPlaceholder.endsWith("_id}")) {
                String companyKey = companyIdPlaceholder.substring(1, companyIdPlaceholder.length() - 4);
                String actualCompanyId = companyKeyToIdMap.get(companyKey);
                if (actualCompanyId != null) {
                    job.put("companyid", actualCompanyId);
                }
            }
        }
        
        if (job.has("contactid")) {
            String contactIdPlaceholder = job.getString("contactid");
            if (contactIdPlaceholder != null && contactIdPlaceholder.startsWith("{") && contactIdPlaceholder.endsWith("_id}")) {
                String contactKey = contactIdPlaceholder.substring(1, contactIdPlaceholder.length() - 4);
                String actualContactId = contactKeyToIdMap.get(contactKey);
                if (actualContactId != null) {
                    job.put("contactid", actualContactId);
                }
            }
        }
        
        if (job.has("ownerid")) {
            String ownerPlaceholder = job.getString("ownerid");
            if (ownerPlaceholder != null && ownerPlaceholder.startsWith("{") && ownerPlaceholder.endsWith("}")) {
                String ownerKey = ownerPlaceholder.substring(1, ownerPlaceholder.length() - 1);
                String actualOwnerId = userMap.get(ownerKey);
                if (actualOwnerId != null) {
                    job.put("ownerid", Integer.parseInt(actualOwnerId));
                }
            }
        }
        
        if (job.has("qualificationid")) {
            String qualificationPlaceholder = job.getString("qualificationid");
            if (qualificationPlaceholder != null && qualificationPlaceholder.startsWith("{") && qualificationPlaceholder.endsWith("}")) {
                String qualificationLabel = qualificationPlaceholder.substring(1, qualificationPlaceholder.length() - 1);
                Integer qualificationId = qualificationIdMap.get(qualificationLabel);
                if (qualificationId == null) {
                    for (Map.Entry<String, Integer> entry : qualificationIdMap.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(qualificationLabel)) {
                            qualificationId = entry.getValue();
                            break;
                        }
                    }
                }
                if (qualificationId != null) {
                    job.put("qualificationid", qualificationId);
                }
            }
        }
        
        if (job.has("jobstatus")) {
            String jobStatusPlaceholder = job.getString("jobstatus");
            if (jobStatusPlaceholder != null && jobStatusPlaceholder.startsWith("{") && jobStatusPlaceholder.endsWith("}")) {
                String jobStatusLabel = jobStatusPlaceholder.substring(1, jobStatusPlaceholder.length() - 1);
                Integer jobStatusId = jobStatusIdMap.get(jobStatusLabel);
                if (jobStatusId == null) {
                    for (Map.Entry<String, Integer> entry : jobStatusIdMap.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(jobStatusLabel)) {
                            jobStatusId = entry.getValue();
                            break;
                        }
                    }
                }
                if (jobStatusId != null) {
                    job.put("jobstatus", jobStatusId);
                }
            }
        }
        
        if (job.has("hiring_pipeline_id")) {
            Object hiringPipelineValue = job.get("hiring_pipeline_id");
            String hiringPipelinePlaceholder = null;
            
            if (hiringPipelineValue instanceof String) {
                hiringPipelinePlaceholder = (String) hiringPipelineValue;
            } else if (hiringPipelineValue != null) {
                String valueAsString = String.valueOf(hiringPipelineValue);
                if (valueAsString.startsWith("{")) {
                    hiringPipelinePlaceholder = valueAsString;
                }
            }
            
            if (hiringPipelinePlaceholder != null && hiringPipelinePlaceholder.startsWith("{") && hiringPipelinePlaceholder.endsWith("}")) {
                if (hiringPipelinePlaceholder.equals("{default_hiring_pipeline_id}")) {
                    job.put("hiring_pipeline_id", 0);
                } else {
                    String pipelineKey = hiringPipelinePlaceholder.substring(1, hiringPipelinePlaceholder.length() - 1);
                    if (pipelineKey.endsWith("_id")) {
                        pipelineKey = pipelineKey.substring(0, pipelineKey.length() - 3);
                    }
                    
                    Integer actualPipelineId = hiringPipelineIdMap.get(pipelineKey);
                    if (actualPipelineId != null) {
                        job.put("hiring_pipeline_id", actualPipelineId);
                    }
                }
            }
        }
        
        if (payload.has("collaborator")) {
            JSONObject collaborator = payload.getJSONObject("collaborator");
            
            if (collaborator.has("user_ids")) {
                JSONArray userIds = collaborator.getJSONArray("user_ids");
                JSONArray actualUserIds = new JSONArray();
                for (int i = 0; i < userIds.length(); i++) {
                    String userIdPlaceholder = userIds.getString(i);
                    if (userIdPlaceholder != null && userIdPlaceholder.startsWith("{") && userIdPlaceholder.endsWith("}")) {
                        String userIdKey = userIdPlaceholder.substring(1, userIdPlaceholder.length() - 1);
                        String actualUserId = userMap.get(userIdKey);
                        if (actualUserId != null) {
                            actualUserIds.put(Integer.parseInt(actualUserId));
                        }
                    } else {
                        actualUserIds.put(userIdPlaceholder);
                    }
                }
                collaborator.put("user_ids", actualUserIds);
            }
            
            if (collaborator.has("team_ids")) {
                JSONArray teamIds = collaborator.getJSONArray("team_ids");
                JSONArray actualTeamIds = new JSONArray();
                for (int i = 0; i < teamIds.length(); i++) {
                    String teamIdPlaceholder = teamIds.getString(i);
                    if (teamIdPlaceholder != null && teamIdPlaceholder.startsWith("{") && teamIdPlaceholder.endsWith("}")) {
                        String teamKey = teamIdPlaceholder.substring(1, teamIdPlaceholder.length() - 1);
                        String actualTeamId = teamMap.get(teamKey);
                        if (actualTeamId != null) {
                            actualTeamIds.put(Integer.parseInt(actualTeamId));
                        }
                    } else {
                        actualTeamIds.put(teamIdPlaceholder);
                    }
                }
                collaborator.put("team_ids", actualTeamIds);
            }
        }
        
        if (payload.has("targetcompanies")) {
            JSONArray targetCompanies = payload.getJSONArray("targetcompanies");
            for (int i = 0; i < targetCompanies.length(); i++) {
                JSONObject targetCompany = targetCompanies.getJSONObject(i);
                
                if (targetCompany.has("slug")) {
                    String slugPlaceholder = targetCompany.getString("slug");
                    if (slugPlaceholder != null && slugPlaceholder.startsWith("{") && slugPlaceholder.endsWith("_slug}")) {
                        String companyKey = slugPlaceholder.substring(1, slugPlaceholder.length() - 6);
                        String actualSlug = companyKeyToSlugMap.get(companyKey);
                        if (actualSlug != null) {
                            targetCompany.put("slug", actualSlug);
                        }
                    }
                }
                
                if (targetCompany.has("id")) {
                    String idPlaceholder = targetCompany.getString("id");
                    if (idPlaceholder != null && idPlaceholder.startsWith("{") && idPlaceholder.endsWith("_id}")) {
                        String companyKey = idPlaceholder.substring(1, idPlaceholder.length() - 4);
                        String actualId = companyKeyToIdMap.get(companyKey);
                        if (actualId != null) {
                            targetCompany.put("id", actualId);
                        }
                    }
                }
                
                if (targetCompany.has("owner")) {
                    String ownerPlaceholder = targetCompany.getString("owner");
                    if (ownerPlaceholder != null && ownerPlaceholder.startsWith("{") && ownerPlaceholder.endsWith("}")) {
                        String ownerKey = ownerPlaceholder.substring(1, ownerPlaceholder.length() - 1);
                        String actualOwnerId = userMap.get(ownerKey);
                        if (actualOwnerId != null) {
                            targetCompany.put("owner", Integer.parseInt(actualOwnerId));
                        }
                    }
                }
            }
        }
    }

    public String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy) {
            case "owner":
                return albatrossAuthToken;
            case "admin":
                return adminAlbatrossAuthToken;
            case "teamMember":
                return teamMemberAlbatrossAuthToken;
            case "restrictedTeamMember":
                return restrictedTeamMemberAlbatrossAuthToken;
            default:
                return albatrossAuthToken;
        }
    }

    public Map<String, String> createUserMap() {
        Map<String, String> userMap = new HashMap<>();
        Response response = function.getUsers(baseURL, apiKey);
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        userMap.put("owner", user.get("[0].id").toString());
        userMap.put("admin", user.get("[1].id").toString());
        userMap.put("restrictedTeamMember", user.get("[2].id").toString());
        userMap.put("teamMember", user.get("[3].id").toString());
        return userMap;
    }

    public Map<String, String> createTeamMap() {
        Map<String, String> teamMap = new HashMap<>();
        ArrayList<String> userId = new ArrayList<String>();
        userId.add(String.valueOf(userMap.get("owner")));
        userId.add(String.valueOf(userMap.get("teamMember")));

        Response response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", userId);
        response.then().statusCode(200);
        Response team = function.getTeams(baseURL, apiKey);
        String teamId = team.jsonPath().getString("[0].team_id");
        teamMap.put("team", teamId);
        return teamMap;
    }

    public Map<String, Integer> createJobStatusMap() {
        Map<String, Integer> statusMap = new HashMap<>();
        try {
            statusMap = function.getJobStatusValues(albatrossURL, albatrossAuthToken);
        } catch (Exception e) {
        }
        return statusMap;
    }

    public Map<String, Integer> createQualificationMap() {
        Map<String, Integer> qualificationMap = new HashMap<>();
        try {
            Map<String, String> authTokenMap = new HashMap<>();
            authTokenMap.put("Authorization", "Bearer " + albatrossAuthToken);
            Response response = RestClient.doPost("JSON", albatrossURL, "qualifications", authTokenMap, null, true, null);
            if (response.getStatusCode() == 200) {
                JSONObject responseJson = new JSONObject(response.getBody().asString());
                JSONArray dataArray = responseJson.getJSONArray("data");
                
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject qualificationObj = dataArray.getJSONObject(i);
                    String qualificationLabel = qualificationObj.getString("label");
                    Integer qualificationId = qualificationObj.getInt("id");
                    qualificationMap.put(qualificationLabel, qualificationId);
                }
            }
        } catch (Exception e) {
        }
        return qualificationMap;
    }

    public Map<String, Integer> createHiringPipelineMap() {
        Map<String, Integer> pipelineMap = new HashMap<>();
        ListFunctions listFunctions = new ListFunctions();
        HiringPipeline hiringFaker = new HiringPipeline();
        
        try {
            JsonPath jsonGetAllCandidateHiringStages = listFunctions
                    .getAllCandidateHiringStages(baseURL, apiKey).jsonPath();
            ArrayList<Integer> hiringStagesID = jsonGetAllCandidateHiringStages.get("status_id");
            
            if (hiringStagesID == null || hiringStagesID.isEmpty()) {
                return pipelineMap;
            }
            
            for (int pipelineNum = 1; pipelineNum <= 3; pipelineNum++) {
                ArrayList<Object> hiringStagesList = new ArrayList<Object>();
                
                HiringStages stage1 = new HiringStages();
                stage1.setId(10);
                stage1.setSequenceno(0);
                hiringStagesList.add(stage1);
                
                HiringStages stage2 = new HiringStages();
                stage2.setId(1);
                stage2.setSequenceno(1);
                hiringStagesList.add(stage2);
                
                HiringStages stage3 = new HiringStages();
                stage3.setId(8);
                stage3.setSequenceno(55);
                hiringStagesList.add(stage3);
                
                CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
                createHiringPipeline.setName(hiringFaker.getHiringPipelineName() + "_Pipeline" + pipelineNum);
                createHiringPipeline.setIs_primary("0");
                createHiringPipeline.setHiring_stages(hiringStagesList);

                Response response = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/add",
                        albatrossAuthToken, null, true, createHiringPipeline);

                if (response.getStatusCode() == 200) {
                    JsonPath jsonPath = response.jsonPath();
                    Integer pipelineId = jsonPath.getInt("id");
                    String pipelineKey = "hiring_pipeline_" + pipelineNum;
                    pipelineMap.put(pipelineKey, pipelineId);
                }
            }
        } catch (Exception e) {
        }
        
        return pipelineMap;
    }

    private void createActivityForJob(List<String> slugs) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            
            CompletableFuture.allOf(slugs.stream()
                    .limit(slugs.size() - 1) 
                    .map(slug -> CompletableFuture.runAsync(() -> {
                        function.createNewNoteLogWithEntitySlug(baseURL, apiKey, "job", slug);
                    }, executor)).toArray(CompletableFuture[]::new)).join();
        } finally {
            executor.shutdown();
        }
    }

    private void updateJobsWithLastActivityTimestamps(List<Integer> jobIds) {
        Map<String, Map<String, String>> lastActivityScenarios = createLastActivityTimestampScenarios();
        int jobIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : lastActivityScenarios.entrySet()) {
            if (jobIndex >= lastActivityScenarios.size() || jobIndex >= jobIds.size()) {
                break;
            }

            Map<String, String> timestamps = scenario.getValue();
            Integer jobId = jobIds.get(jobIndex);

            JSONObject fieldsAndTimestamps = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
            }

            Response updateResponse = ReaperIntegration.updateLastActivityTimestamp("job", jobId, fieldsAndTimestamps);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update the job last activity timestamps");
            }
            jobIndex++;
        }
    }

    private Map<String, Map<String, String>> createLastActivityTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        // Today scenario
        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("note_created_on", todayEpoch);
        scenarios.put("today_last_activity", todayTimestamps);

        // Yesterday scenario
        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("note_created_on", yesterdayEpoch);
        scenarios.put("yesterday_last_activity", yesterdayTimestamps);

        // This week scenario
        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("note_created_on", thisWeekEpoch);
        scenarios.put("this_week_last_activity", thisWeekTimestamps);

        // Last week scenario
        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("note_created_on", lastWeekEpoch);
        scenarios.put("last_week_last_activity", lastWeekTimestamps);

        // This month scenario
        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("note_created_on", thisMonthEpoch);
        scenarios.put("this_month_last_activity", thisMonthTimestamps);

        // Last month scenario
        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("note_created_on", lastMonthEpoch);
        scenarios.put("last_month_last_activity", lastMonthTimestamps);

        // This quarter scenario
        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("note_created_on", thisQuarterEpoch);
        scenarios.put("this_quarter_last_activity", thisQuarterTimestamps);

        // Last quarter scenario
        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("note_created_on", lastQuarterEpoch);
        scenarios.put("last_quarter_last_activity", lastQuarterTimestamps);

        // This year scenario
        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("note_created_on", thisYearEpoch);
        scenarios.put("this_year_last_activity", thisYearTimestamps);

        // Last year scenario
        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("note_created_on", lastYearEpoch);
        scenarios.put("last_year_last_activity", lastYearTimestamps);

        // Static date scenario 1 - Historical data (June 15, 2022)
        Map<String, String> staticLastActivityTimestamps1 = new HashMap<>();
        staticLastActivityTimestamps1.put("note_created_on", "1655251200");
        scenarios.put("static_last_activity_scenario1", staticLastActivityTimestamps1);

        // Static date scenario 2 - Previous year data (March 10, 2023)
        Map<String, String> staticLastActivityTimestamps2 = new HashMap<>();
        staticLastActivityTimestamps2.put("note_created_on", "1678406400");
        scenarios.put("static_last_activity_scenario2", staticLastActivityTimestamps2);

        // Static date scenario 3 - Current year data (June 6, 2024)
        Map<String, String> staticLastActivityTimestamps3 = new HashMap<>();
        staticLastActivityTimestamps3.put("note_created_on", "1717689600");
        scenarios.put("static_last_activity_scenario3", staticLastActivityTimestamps3);

        // Static date scenario 4 - Future planning data (February 14, 2025)
        Map<String, String> staticLastActivityTimestamps4 = new HashMap<>();
        staticLastActivityTimestamps4.put("note_created_on", "1739491200");
        scenarios.put("static_last_activity_scenario4", staticLastActivityTimestamps4);

        return scenarios;
    }


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "dateFieldFilterJobSearchSmokeTestData", description = "[Smoke] Filter Search Test for Date Fields")
    public void dateFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        dateFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "dateFieldFilterJobSearchSmokeTestData", parallel = true)
    public Object[][] dateFieldFilterJobSearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
