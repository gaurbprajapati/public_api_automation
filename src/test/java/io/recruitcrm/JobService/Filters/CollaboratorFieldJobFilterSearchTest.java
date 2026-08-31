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
import io.recruitcrm.Filters.FilterValueFactory;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CollaboratorFieldJobFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String accountOwnerAPIKey;
    String email;
    Map<String, String> jobKeyToIdMap = new HashMap<>();
    Map<String, String> jobIdToKeyMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>();
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();
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
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
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
    @Test(groups = {"aries_service"}, dataProvider = "collaboratorFieldFilterJobSearchTestData", description = "Filter Search Test for Collaborator Field")
    public void collaboratorFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, jobKeyToIdMap, jobIdToKeyMap, 15, "Job");
    }

    @DataProvider(name = "collaboratorFieldFilterJobSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobCollaboratorFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "")});
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        String processedFilterValue = processFilterValue(filterValue, fieldName);
        
        // Create filterValue object with type and value array
        JSONObject filterValueObj = new JSONObject();
        if (filterType.equals("is_empty") || filterType.equals("has_any_value")) {
            filterValueObj = FilterValueFactory.emptyFilterValue(filterValue_TYPE);
        } else if (filterValue_TYPE.equals("ENTITY_ASSOCIATION")) {
            filterValueObj = FilterValueFactory.entityAssociationFilterValue(processedFilterValue);
        } else {
            filterValueObj = FilterValueFactory.emptyFilterValue(filterValue_TYPE);
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
        filter.put("entityType", "job");
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

    private String processFilterValue(String filterValue, String fieldName) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }

        String processedValue = filterValue;

        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(filterValue);

        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String fieldKey = matcher.group(1);

            String actualValue = null;
            if (fieldKey.startsWith("team") && !fieldKey.equals("teamMember")) {
                actualValue = teamMap.get(fieldKey);
            } else if (fieldKey.equals("owner") || fieldKey.equals("admin") || fieldKey.equals("restrictedTeamMember") || fieldKey.equals("teamMember")) {
                actualValue = userMap.get(fieldKey);
            }
            
            if (actualValue != null) {
                processedValue = processedValue.replace(placeholder, actualValue);
            } else {
                throw new IllegalArgumentException("Unable to process the payload, No value found for placeholder: " + placeholder + " in field: " + fieldName);
            }
        }

        return processedValue;
    }

    public void createTestData() {
        JSONObject jobJson = readJsonFileFromPath("src/test/resources/testData/job_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            List<CompletableFuture<Void>> createFutures = jobJson.keySet().stream()
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
                }, executor))
                .collect(Collectors.toList());

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
                        Integer companyId = jsonPath.getInt("data.company.id");
                        
                        synchronized (companyKeyToSlugMap) {
                            companyKeyToSlugMap.put(companyKey, slug);
                        }
                        synchronized (companyKeyToIdMap) {
                            companyKeyToIdMap.put(companyKey, String.valueOf(companyId));
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
                        String contactIdStr = jsonPath.getString("data.contact.id");
                        
                        if (contactIdStr == null) {
                            return;
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

    public Map<String, String> createUserMap() {
        Map<String, String> userMap = new HashMap<>();
        Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
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

        Response response = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1", userId);
        response.then().statusCode(200);
        Response team = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
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
                    .getAllCandidateHiringStages(baseURL, accountOwnerAPIKey).jsonPath();
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



    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "collaboratorFieldFilterJobSearchSmokeTestData", description = "[Smoke] Filter Search Test for Collaborator Field")
    public void collaboratorFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        collaboratorFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE, testCaseId);
    }

    @DataProvider(name = "collaboratorFieldFilterJobSearchSmokeTestData", parallel = true)
    public Object[][] collaboratorFieldFilterJobSearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
