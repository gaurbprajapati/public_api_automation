package io.recruitcrm.JobService.Filters;

import com.qa.api.util.reaper.ThreadManager;
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
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class NumberFieldJobFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String apiKey;
    String email;
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>();
    Map<String, String> contactKeyToSlugMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();
    Map<String, String> jobKeyToIdMap = new HashMap<>();
    Map<String, String> jobIdToKeyMap = new HashMap<>();
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
    @Test(groups = {"aries_service"}, dataProvider = "numberFieldFilterJobSearchTestData", description = "Filter Search Test for Number Fields")
    public void numberFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Object requestBody = "Double".equals(filterValue_TYPE) ? ensureDoubleValueInPayload(payload.toString()) : payload;
        Response response = (requestBody instanceof JSONObject) ? executeFilterSearch((JSONObject) requestBody, albatrossAuthToken, "jobs") : RestClient.doPost("JSON", ariesServiceURL, ADVANCED_SEARCH_JOBS_GET_PATH, albatrossAuthToken, null, true, requestBody);
        JSONArray data = getFilteredData(response);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        
        validateNumberFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, "Job");
    }

    @DataProvider(name = "numberFieldFilterJobSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobNumberTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
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
        
        JSONObject filterValueObj = new JSONObject();
        
        if (filterValue_TYPE.equals("NUMERIC_STRING")) {
            filterValueObj = numericStringFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("INTEGER")) {
            filterValueObj = integerFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("INTEGER_START_END")) {
            filterValueObj = integerStartEndFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("NUMERIC_STRING_START_END")) {
            filterValueObj = numericStringStartEndFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("STRING")) {
            filterValueObj = stringFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("STRING_START_END")) {
            filterValueObj = stringStartEndFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("DOUBLE")) {
            filterValueObj = doubleFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("DOUBLE_START_END")) {
            filterValueObj = doubleStartEndFilterValue(filterValue);
        } else {
            filterValueObj = emptyFilterValue(filterValue_TYPE);
        }
        
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


    private static String ensureDoubleValueInPayload(String payloadStr) {
        // "type":"Double","value":12 -> "type":"Double","value":12.0
        payloadStr = payloadStr.replaceAll("\"type\":\"Double\",\"value\":(\\d+)([,\\}])", "\"type\":\"Double\",\"value\":$1.0$2");
        // "value":12,"type":"Double" -> "value":12.0,"type":"Double"
        payloadStr = payloadStr.replaceAll("\"value\":(\\d+),\"type\":\"Double\"", "\"value\":$1.0,\"type\":\"Double\"");
        return payloadStr;
    }

    public void createTestData() {
        JSONObject jobJson = readJsonFileFromPath("src/test/resources/testData/job_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        try {
            // Creating all jobs according to payload and storing their IDs in a map
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
                    
                    if (jobIdStr != null) {
                        Integer jobId = Integer.parseInt(jobIdStr);

                        synchronized (jobKeyToIdMap) {
                            jobKeyToIdMap.put(jobKey.toLowerCase(), String.valueOf(jobId));
                        }
                        synchronized (jobIdToKeyMap) {
                            jobIdToKeyMap.put(String.valueOf(jobId), jobKey.toLowerCase());
                        }
                    }
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private String getAlbatrossAuthToken(String createdBy) {
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

    private void replaceJobPlaceholders(JSONObject job, JSONObject payload) {
        // Replace company placeholders
        if (job.has("companyid")) {
            Object companyIdValue = job.get("companyid");
            if (companyIdValue instanceof String) {
                String companyIdPlaceholder = (String) companyIdValue;
                if (companyIdPlaceholder.startsWith("{") && companyIdPlaceholder.endsWith("}")) {
                    String companyKey = companyIdPlaceholder.substring(1, companyIdPlaceholder.length() - 1);
                    if (companyKey.endsWith("_id")) {
                        companyKey = companyKey.substring(0, companyKey.length() - 3);
                    }
                    String companyId = companyKeyToIdMap.get(companyKey.toLowerCase());
                    if (companyId != null) {
                        job.put("companyid", Integer.parseInt(companyId));
                    }
                }
            }
        }

        // Replace contact placeholders
        if (job.has("contactid")) {
            Object contactIdValue = job.get("contactid");
            if (contactIdValue instanceof String) {
                String contactIdPlaceholder = (String) contactIdValue;
                if (contactIdPlaceholder.startsWith("{") && contactIdPlaceholder.endsWith("}")) {
                    String contactKey = contactIdPlaceholder.substring(1, contactIdPlaceholder.length() - 1);
                    if (contactKey.endsWith("_id")) {
                        contactKey = contactKey.substring(0, contactKey.length() - 3);
                    }
                    String contactId = contactKeyToIdMap.get(contactKey.toLowerCase());
                    if (contactId != null) {
                        job.put("contactid", Integer.parseInt(contactId));
                    }
                }
            }
        }

        // Replace owner placeholders
        if (job.has("ownerid") && job.getString("ownerid").startsWith("{")) {
            String ownerKey = job.getString("ownerid").substring(1, job.getString("ownerid").length() - 1);
            String ownerId = userMap.get(ownerKey.toLowerCase());
            if (ownerId != null) {
                job.put("ownerid", Integer.parseInt(ownerId));
            }
        }

        // Replace qualification placeholders
        if (job.has("qualificationid") && job.getString("qualificationid").startsWith("{")) {
            String qualificationKey = job.getString("qualificationid").substring(1, job.getString("qualificationid").length() - 1);
            Integer qualificationId = qualificationIdMap.get(qualificationKey);
            if (qualificationId == null) {
                for (Map.Entry<String, Integer> entry : qualificationIdMap.entrySet()) {
                    if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(qualificationKey)) {
                        qualificationId = entry.getValue();
                        break;
                    }
                }
            }
            if (qualificationId != null) {
                job.put("qualificationid", qualificationId);
            }
        }

        // Replace job status placeholders
        if (job.has("jobstatus") && job.getString("jobstatus").startsWith("{")) {
            String jobStatusKey = job.getString("jobstatus").substring(1, job.getString("jobstatus").length() - 1);
            Integer jobStatusId = jobStatusIdMap.get(jobStatusKey);
            if (jobStatusId == null) {
                for (Map.Entry<String, Integer> entry : jobStatusIdMap.entrySet()) {
                    if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(jobStatusKey)) {
                        jobStatusId = entry.getValue();
                        break;
                    }
                }
            }
            if (jobStatusId != null) {
                job.put("jobstatus", jobStatusId);
            }
        }

        // Replace hiring pipeline placeholders
        if (job.has("hiring_pipeline_id") && job.getString("hiring_pipeline_id").startsWith("{")) {
            String hiringPipelinePlaceholder = job.getString("hiring_pipeline_id");
            if (hiringPipelinePlaceholder.equals("{default_hiring_pipeline_id}")) {
                job.put("hiring_pipeline_id", 0);
            } else {
                String pipelineKey = hiringPipelinePlaceholder.substring(1, hiringPipelinePlaceholder.length() - 1);
                if (pipelineKey.endsWith("_id")) {
                    pipelineKey = pipelineKey.substring(0, pipelineKey.length() - 3);
                }
                Integer pipelineId = hiringPipelineIdMap.get(pipelineKey.toLowerCase());
                if (pipelineId != null) {
                    job.put("hiring_pipeline_id", pipelineId);
                }
            }
        }

        // Replace collaborator placeholders
        if (payload.has("collaborator")) {
            JSONObject collaborator = payload.getJSONObject("collaborator");

            if (collaborator.has("user_ids")) {
                JSONArray userIds = collaborator.getJSONArray("user_ids");
                JSONArray actualUserIds = new JSONArray();
                for (int i = 0; i < userIds.length(); i++) {
                    String userIdPlaceholder = userIds.getString(i);
                    if (userIdPlaceholder != null && userIdPlaceholder.startsWith("{") && userIdPlaceholder.endsWith("}")) {
                        String userIdKey = userIdPlaceholder.substring(1, userIdPlaceholder.length() - 1);
                        String actualUserId = userMap.get(userIdKey.toLowerCase());
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
                        String actualTeamId = teamMap.get(teamKey.toLowerCase());
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

        // Replace targetcompanies placeholders
        if (payload.has("targetcompanies")) {
            JSONArray targetCompanies = payload.getJSONArray("targetcompanies");
            for (int i = 0; i < targetCompanies.length(); i++) {
                JSONObject targetCompany = targetCompanies.getJSONObject(i);
                if (targetCompany.has("slug") && targetCompany.getString("slug").startsWith("{")) {
                    String slugKey = targetCompany.getString("slug").substring(1, targetCompany.getString("slug").length() - 1);
                    if (slugKey.endsWith("_slug")) {
                        slugKey = slugKey.substring(0, slugKey.length() - 5);
                    }
                    String actualSlug = companyKeyToSlugMap.get(slugKey.toLowerCase());
                    if (actualSlug != null) {
                        targetCompany.put("slug", actualSlug);
                    }
                }
                if (targetCompany.has("id") && targetCompany.getString("id").startsWith("{")) {
                    String idKey = targetCompany.getString("id").substring(1, targetCompany.getString("id").length() - 1);
                    if (idKey.endsWith("_id")) {
                        idKey = idKey.substring(0, idKey.length() - 3);
                    }
                    String actualId = companyKeyToIdMap.get(idKey.toLowerCase());
                    if (actualId != null) {
                        targetCompany.put("id", Integer.parseInt(actualId));
                    }
                }

                // Optional: owner placeholder inside targetcompanies
                if (targetCompany.has("owner")) {
                    String ownerPlaceholder = targetCompany.optString("owner", "");
                    if (ownerPlaceholder != null && ownerPlaceholder.startsWith("{") && ownerPlaceholder.endsWith("}")) {
                        String ownerKey = ownerPlaceholder.substring(1, ownerPlaceholder.length() - 1);
                        String actualOwnerId = userMap.get(ownerKey.toLowerCase());
                        if (actualOwnerId != null) {
                            targetCompany.put("owner", Integer.parseInt(actualOwnerId));
                        }
                    }
                }
            }
        }
    }

    public void createCompanies() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        try {
            List<CompletableFuture<Void>> createFutures = companyJson.keySet().stream()
                .filter(key -> key.startsWith("company"))
                .map(companyKey -> CompletableFuture.runAsync(() -> {
                    JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                    JSONObject payload = companyEntry.getJSONObject("payload");
                    Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                    response.then().statusCode(200);
                    JsonPath jsonPath = response.jsonPath();
                    String slug = jsonPath.getString("data.company.slug");
                    String id = jsonPath.getString("data.company.id");
                    
                    synchronized (companyKeyToSlugMap) {
                        companyKeyToSlugMap.put(companyKey.toLowerCase(), slug);
                    }
                    synchronized (companyKeyToIdMap) {
                        companyKeyToIdMap.put(companyKey.toLowerCase(), id);
                    }
                }, executor))
                .collect(Collectors.toList());
            
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
                                    String normalizedCompanyKey = companyKey.toLowerCase();
                                    String actualSlug = companyKeyToSlugMap.get(normalizedCompanyKey);
                                    String actualId = companyKeyToIdMap.get(normalizedCompanyKey);
                                    
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
                        
                        if (contactIdStr != null) {
                            synchronized (contactKeyToSlugMap) {
                                contactKeyToSlugMap.put(contactKey.toLowerCase(), slug);
                            }
                            synchronized (contactKeyToIdMap) {
                                contactKeyToIdMap.put(contactKey.toLowerCase(), contactIdStr);
                            }
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
        Response response = function.getUsers(baseURL, apiKey);
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        userMap.put("owner", user.get("[0].id").toString());
        userMap.put("admin", user.get("[1].id").toString());
        userMap.put("restrictedteammember", user.get("[2].id").toString());
        userMap.put("teammember", user.get("[3].id").toString());
        return userMap;
    }

    public Map<String, String> createTeamMap() {
        Map<String, String> teamMap = new HashMap<>();
        ArrayList<String> userId = new ArrayList<String>();
        userId.add(String.valueOf(userMap.get("owner")));
        userId.add(String.valueOf(userMap.get("teammember")));

        Response response = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1", userId);
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


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "numberFieldFilterJobSearchSmokeTestData", description = "[Smoke] Filter Search Test for Number Fields")
    public void numberFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        numberFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "numberFieldFilterJobSearchSmokeTestData", parallel = true)
    public Object[][] numberFieldFilterJobSearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
