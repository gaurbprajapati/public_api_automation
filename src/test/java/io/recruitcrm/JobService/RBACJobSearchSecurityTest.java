package io.recruitcrm.JobService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACJobSearchSecurityTest extends TestBase {

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private Map<String, String> companyKeyToIdMap = new HashMap<>();
    private Map<String, String> contactKeyToIdMap = new HashMap<>();
    private Map<String, String> userMap = new HashMap<>();
    private Map<String, String> teamMap = new HashMap<>();
    private Map<String, Integer> jobStatusIdMap = new HashMap<>();
    private Map<String, Integer> qualificationIdMap = new HashMap<>();
    private Map<String, Integer> hiringPipelineIdMap = new HashMap<>();

    private String commonCompanySlug;
    private String commonContactSlug;
    private String apiAuthToken;
    private commanFunction commanFunction;
    private AllCrudFunctions allCrudFunctions;
    private String albatrossAuthToken;
    private String adminAlbatrossAuthToken;
    private String teamMemberAlbatrossAuthToken;
    private String restrictedTeamMemberAlbatrossAuthToken;

    private Integer jobStatusOpenId;
    private Integer jobStatusClosedId;
    private Integer jobStatusOnHoldId;
    private Integer jobStatusCancelledId;

    private List<JobInfo> jobInfoList = new ArrayList<>();

    private static final int RBAC_JSON_TEAM_JOBS = 12;
    private static final int RBAC_JSON_OPEN = 3;
    private static final int RBAC_JSON_CLOSED = 3;
    private static final int RBAC_JSON_ON_HOLD = 3;
    private static final int RBAC_JSON_CANCELLED = 3;
    private static final int RBAC_JSON_OWNER_ONLY_JOBS = 1;

    private static final String ADVANCED_JOB_SEARCH_GET_PATH = "advanced-search/jobs/search/get";
    private static final int SEARCH_PAGE_SIZE = 20;

    private static class JobInfo {
        final String name;
        final String ownerKey;
        final String statusLabel;

        JobInfo(String name, String ownerKey, String statusLabel) {
            this.name = name;
            this.ownerKey = ownerKey == null ? "" : ownerKey.toLowerCase();
            this.statusLabel = statusLabel == null ? "" : statusLabel.trim();
        }
    }

    @BeforeClass
    public void setUp() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        apiAuthToken = ThreadManager.getAccountApiKey();
        commanFunction = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        albatrossAuthToken = ThreadManager.getAlbatrossToken("Owner");
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");

        userMap = createUserMap();
        teamMap = createTeamMap();
        jobStatusIdMap = createJobStatusMap();
        qualificationIdMap = createQualificationMap();

        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);

        createCommonCompanyAndContactForRBAC();
        populateCompanyAndContactIdMaps();

        createTestData();

        resolveJobStatusIdsForViews();
        loadJobInfoListFromJson();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearch_RBAC_ViewOwnerOnly() {
        if (albatrossTknMap == null || albatrossTknMap.isEmpty()) {
            throw new AssertionError("RBAC tokens not available. Skipping RBAC test.");
        }
        String executorToken = albatrossTknMap.get("RestrictedTeamMember");
        if (executorToken == null) {
            throw new AssertionError("Executor token not available for role: RestrictedTeamMember");
        }
        Integer executorUserId = userIdsMap.get("RestrictedTeamMember");
        if (executorUserId == null) executorUserId = userIdsMap.get("AccountOwner");

        assertSearchForView("RestrictedTeamMember", "all", createDefaultSearchRequestBody(), executorToken, 200);
        assertSearchForView("RestrictedTeamMember", "myJobs", createOwnerFilterRequestBody(executorUserId), executorToken, 200);
        if (jobStatusOpenId != null) {
            assertSearchForView("RestrictedTeamMember", "open", createJobStatusFilterRequestBody(jobStatusOpenId), executorToken, 200);
        }
        if (jobStatusClosedId != null) {
            assertSearchForView("RestrictedTeamMember", "closed", createJobStatusFilterRequestBody(jobStatusClosedId), executorToken, 200);
        }
        if (jobStatusOnHoldId != null) {
            assertSearchForView("RestrictedTeamMember", "onHold", createJobStatusFilterRequestBody(jobStatusOnHoldId), executorToken, 200);
        }
        if (jobStatusCancelledId != null) {
            assertSearchForView("RestrictedTeamMember", "cancelled", createJobStatusFilterRequestBody(jobStatusCancelledId), executorToken, 200);
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearch_RBAC_Nothing() {
        if (albatrossTknMap == null || albatrossTknMap.isEmpty()) {
            throw new AssertionError("RBAC tokens not available. Skipping RBAC test.");
        }
        String executorToken = albatrossTknMap.get("CustomRoleNothing");
        if (executorToken == null) {
            throw new AssertionError("Executor token not available for role: CustomRoleNothing");
        }
        Integer executorUserId = userIdsMap.get("CustomRoleNothing");
        if (executorUserId == null) executorUserId = userIdsMap.get("AccountOwner");

        assertSearchForView("CustomRoleNothing", "all", createDefaultSearchRequestBody(), executorToken, 200);
        assertSearchForView("CustomRoleNothing", "myJobs", createOwnerFilterRequestBody(executorUserId), executorToken, 200);
        if (jobStatusOpenId != null) {
            assertSearchForView("CustomRoleNothing", "open", createJobStatusFilterRequestBody(jobStatusOpenId), executorToken, 200);
        }
        if (jobStatusClosedId != null) {
            assertSearchForView("CustomRoleNothing", "closed", createJobStatusFilterRequestBody(jobStatusClosedId), executorToken, 200);
        }
        if (jobStatusOnHoldId != null) {
            assertSearchForView("CustomRoleNothing", "onHold", createJobStatusFilterRequestBody(jobStatusOnHoldId), executorToken, 200);
        }
        if (jobStatusCancelledId != null) {
            assertSearchForView("CustomRoleNothing", "cancelled", createJobStatusFilterRequestBody(jobStatusCancelledId), executorToken, 200);
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearch_RBAC_TeamAccess() {
        if (albatrossTknMap == null || albatrossTknMap.isEmpty()) {
            throw new AssertionError("RBAC tokens not available. Skipping RBAC test.");
        }
        String executorToken = albatrossTknMap.get("CustomRoleTeamOnly");
        if (executorToken == null) {
            throw new AssertionError("Executor token not available for role: CustomRoleTeamOnly");
        }
        Integer executorUserId = userIdsMap.get("CustomRoleTeamOnly");
        if (executorUserId == null) executorUserId = userIdsMap.get("AccountOwner");

        assertSearchForView("CustomRoleTeamOnly", "all", createDefaultSearchRequestBody(), executorToken, 200);
        assertSearchForView("CustomRoleTeamOnly", "myJobs", createOwnerFilterRequestBody(executorUserId), executorToken, 200);
        if (jobStatusOpenId != null) {
            assertSearchForView("CustomRoleTeamOnly", "open", createJobStatusFilterRequestBody(jobStatusOpenId), executorToken, 200);
        }
        if (jobStatusClosedId != null) {
            assertSearchForView("CustomRoleTeamOnly", "closed", createJobStatusFilterRequestBody(jobStatusClosedId), executorToken, 200);
        }
        if (jobStatusOnHoldId != null) {
            assertSearchForView("CustomRoleTeamOnly", "onHold", createJobStatusFilterRequestBody(jobStatusOnHoldId), executorToken, 200);
        }
        if (jobStatusCancelledId != null) {
            assertSearchForView("CustomRoleTeamOnly", "cancelled", createJobStatusFilterRequestBody(jobStatusCancelledId), executorToken, 200);
        }
    }

    private void createCommonCompanyAndContactForRBAC() {
        Response companyResponse = commanFunction.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        commonCompanySlug = companyResponse.jsonPath().getString("slug");
        Response contactResponse = commanFunction.createNewContact_POST(baseURL, apiAuthToken, commonCompanySlug);
        commonContactSlug = contactResponse.jsonPath().getString("slug");
    }

    private void populateCompanyAndContactIdMaps() {
        Response companyResponse = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossAuthToken, commonCompanySlug);
        assertThat("Failed to get company for ID", companyResponse.getStatusCode(), equalTo(200));
        String companyIdStr = companyResponse.jsonPath().getString("data.company.id");
        assertThat("Company ID should not be null", companyIdStr, notNullValue());
        companyKeyToIdMap.put("company1", companyIdStr);

        Response contactResponse = allCrudFunctions.getContactResponse(albatrossURL, albatrossAuthToken, commonContactSlug);
        assertThat("Failed to get contact for ID", contactResponse.getStatusCode(), equalTo(200));
        String contactIdStr = contactResponse.jsonPath().getString("data.contact.id");
        assertThat("Contact ID should not be null", contactIdStr, notNullValue());
        contactKeyToIdMap.put("contact1", contactIdStr);
    }

    private void resolveJobStatusIdsForViews() {
        jobStatusOpenId = getJobStatusIdByLabel("Open");
        jobStatusClosedId = getJobStatusIdByLabel("Closed");
        jobStatusOnHoldId = getJobStatusIdByLabel("On Hold");
        jobStatusCancelledId = getJobStatusIdByLabel("Canceled");
        if (jobStatusCancelledId == null) {
            jobStatusCancelledId = getJobStatusIdByLabel("Cancelled");
        }
    }

    private Integer getJobStatusIdByLabel(String label) {
        Integer id = jobStatusIdMap.get(label);
        if (id != null) return id;
        for (Map.Entry<String, Integer> e : jobStatusIdMap.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(label)) return e.getValue();
        }
        return null;
    }

    private void loadJobInfoListFromJson() {
        JSONObject jobJson = readJsonFileFromPath("src/test/resources/job_dataRBAC.json");
        jobInfoList.clear();
        for (String key : jobJson.keySet()) {
            if (!key.startsWith("job")) continue;
            JSONObject jobEntry = jobJson.getJSONObject(key);
            JSONObject payload = jobEntry.optJSONObject("payload");
            if (payload == null) continue;
            JSONObject job = payload.optJSONObject("job");
            if (job == null) continue;
            String name = job.optString("name", "");
            String ownerKey = null;
            if (job.has("ownerid")) {
                Object o = job.get("ownerid");
                if (o instanceof String) {
                    String s = (String) o;
                    if (s.startsWith("{") && s.endsWith("}")) {
                        ownerKey = s.substring(1, s.length() - 1);
                    }
                }
            }
            String statusLabel = null;
            if (job.has("jobstatus")) {
                Object o = job.get("jobstatus");
                if (o instanceof String) {
                    String s = (String) o;
                    if (s.startsWith("{") && s.endsWith("}")) {
                        statusLabel = s.substring(1, s.length() - 1);
                    }
                }
            }
            jobInfoList.add(new JobInfo(name, ownerKey, statusLabel));
        }
    }

    public void createTestData() {
        JSONObject jobJson = readJsonFileFromPath("src/test/resources/job_dataRBAC.json");
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
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy) {
            case "owner": return albatrossAuthToken;
            case "admin": return adminAlbatrossAuthToken;
            case "teamMember": return teamMemberAlbatrossAuthToken;
            case "restrictedTeamMember": return restrictedTeamMemberAlbatrossAuthToken;
            default: return albatrossAuthToken;
        }
    }

    private void replaceJobPlaceholders(JSONObject job, JSONObject payload) {
        if (job.has("companyid")) {
            Object companyIdValue = job.get("companyid");
            if (companyIdValue instanceof String) {
                String companyIdPlaceholder = (String) companyIdValue;
                if (companyIdPlaceholder.startsWith("{") && companyIdPlaceholder.endsWith("}")) {
                    String companyKey = companyIdPlaceholder.substring(1, companyIdPlaceholder.length() - 1);
                    if (companyKey.endsWith("_id")) companyKey = companyKey.substring(0, companyKey.length() - 3);
                    String companyId = companyKeyToIdMap.get(companyKey.toLowerCase());
                    if (companyId != null) job.put("companyid", Integer.parseInt(companyId));
                }
            }
        }
        if (job.has("contactid")) {
            Object contactIdValue = job.get("contactid");
            if (contactIdValue instanceof String) {
                String contactIdPlaceholder = (String) contactIdValue;
                if (contactIdPlaceholder.startsWith("{") && contactIdPlaceholder.endsWith("}")) {
                    String contactKey = contactIdPlaceholder.substring(1, contactIdPlaceholder.length() - 1);
                    if (contactKey.endsWith("_id")) contactKey = contactKey.substring(0, contactKey.length() - 3);
                    String contactId = contactKeyToIdMap.get(contactKey.toLowerCase());
                    if (contactId != null) job.put("contactid", Integer.parseInt(contactId));
                }
            }
        }
        if (job.has("ownerid") && job.getString("ownerid").startsWith("{")) {
            String ownerKey = job.getString("ownerid").substring(1, job.getString("ownerid").length() - 1);
            String ownerId = userMap.get(ownerKey.toLowerCase());
            if (ownerId != null) job.put("ownerid", Integer.parseInt(ownerId));
        }
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
            if (qualificationId != null) job.put("qualificationid", qualificationId);
        }
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
            if (jobStatusId == null && ("Canceled".equalsIgnoreCase(jobStatusKey) || "Cancelled".equalsIgnoreCase(jobStatusKey))) {
                for (Map.Entry<String, Integer> entry : jobStatusIdMap.entrySet()) {
                    if (entry.getKey() != null && (entry.getKey().equalsIgnoreCase("Canceled") || entry.getKey().equalsIgnoreCase("Cancelled"))) {
                        jobStatusId = entry.getValue();
                        break;
                    }
                }
            }
            if (jobStatusId != null) job.put("jobstatus", jobStatusId);
        }
        if (job.has("hiring_pipeline_id") && job.getString("hiring_pipeline_id").startsWith("{")) {
            String hiringPipelinePlaceholder = job.getString("hiring_pipeline_id");
            if (hiringPipelinePlaceholder.equals("{default_hiring_pipeline_id}")) {
                job.put("hiring_pipeline_id", 0);
            } else {
                String pipelineKey = hiringPipelinePlaceholder.substring(1, hiringPipelinePlaceholder.length() - 1);
                if (pipelineKey.endsWith("_id")) pipelineKey = pipelineKey.substring(0, pipelineKey.length() - 3);
                Integer pipelineId = hiringPipelineIdMap.get(pipelineKey.toLowerCase());
                if (pipelineId != null) job.put("hiring_pipeline_id", pipelineId);
            }
        }
    }

    public Map<String, String> createUserMap() {
        Map<String, String> map = new HashMap<>();
        Response response = commanFunction.getUsers(baseURL, apiAuthToken);
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        map.put("owner", user.get("[0].id").toString());
        map.put("admin", user.get("[1].id").toString());
        map.put("restrictedteammember", user.get("[2].id").toString());
        map.put("teammember", user.get("[3].id").toString());
        map.put("customroleteamonly", user.get("[4].id").toString());
        map.put("customrolenothing", user.get("[5].id").toString());
        return map;
    }

    public Map<String, String> createTeamMap() {
        Map<String, String> map = new HashMap<>();
        ArrayList<String> userId = new ArrayList<>();
        userId.add(String.valueOf(userMap.get("owner")));
        userId.add(String.valueOf(userMap.get("admin")));
        userId.add(String.valueOf(userMap.get("teammember")));
        userId.add(String.valueOf(userMap.get("customroleteamonly")));
        Response response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", userId);
        response.then().statusCode(200);
        Response team = commanFunction.getTeams(baseURL, apiAuthToken);
        String teamId = team.jsonPath().getString("[0].team_id");
        map.put("team", teamId);
        return map;
    }

    public Map<String, Integer> createJobStatusMap() {
        try {
            return commanFunction.getJobStatusValues(albatrossURL, albatrossAuthToken);
        } catch (Exception e) {
            return new HashMap<>();
        }
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
            Assert.fail(e.getMessage());
        }
        return qualificationMap;
    }

    // --- View payloads (same as RBACJobSearchCountSecurityTest) ---

    private JSONObject createDefaultSearchRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "JOB");
        requestBody.put("defaultFilterList", JSONObject.NULL);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private JSONObject createOwnerFilterRequestBody(int ownerId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "JOB");
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "jobs");
        filter.put("searchField", "ownerid");
        filter.put("filterType", "is");
        filter.put("entityType", "job");
        filter.put("fieldType", "dropdown");
        JSONObject filterValue = new JSONObject();
        JSONArray value = new JSONArray();
        JSONObject entityObj = new JSONObject();
        entityObj.put("entityTypeId", 6);
        JSONArray entityIds = new JSONArray();
        entityIds.put(ownerId);
        entityObj.put("entityIds", entityIds);
        value.put(entityObj);
        filterValue.put("value", value);
        filterValue.put("type", "ENTITY_ASSOCIATION");
        filter.put("filterValue", filterValue);
        filters.put(filter);
        defaultFilterListInner.put("filters", filters);
        defaultFilterListInner.put("subGroupJoinOperator", "AND");
        defaultFilterList.put("defaultFilterList", defaultFilterListInner);
        requestBody.put("defaultFilterList", defaultFilterList);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private JSONObject createJobStatusFilterRequestBody(int jobStatusId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "JOB");
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "jobs");
        filter.put("searchField", "jobstatus");
        filter.put("filterType", "is");
        filter.put("entityType", "job");
        filter.put("fieldType", "NUMBER");
        JSONObject filterValue = new JSONObject();
        filterValue.put("type", "INTEGER_LIST");
        JSONArray valueArray = new JSONArray();
        valueArray.put(jobStatusId);
        filterValue.put("value", valueArray);
        filter.put("filterValue", filterValue);
        filters.put(filter);
        defaultFilterListInner.put("filters", filters);
        defaultFilterListInner.put("subGroupJoinOperator", "AND");
        defaultFilterList.put("defaultFilterList", defaultFilterListInner);
        requestBody.put("defaultFilterList", defaultFilterList);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private Map<String, String> createDefaultQueryParameters() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", String.valueOf(SEARCH_PAGE_SIZE));
        return queryParameters;
    }

    private int getExpectedSearchCount(String executorRole, String view) {
        switch (executorRole) {
            case "CustomRoleNothing":
                return 0;
            case "RestrictedTeamMember":
                if ("all".equals(view)) return RBAC_JSON_OWNER_ONLY_JOBS;
                if ("myJobs".equals(view)) return RBAC_JSON_OWNER_ONLY_JOBS;
                if ("open".equals(view)) return 1;
                if ("closed".equals(view)) return 0;
                if ("onHold".equals(view)) return 0;
                if ("cancelled".equals(view)) return 0;
                return 0;
            case "CustomRoleTeamOnly":
                if ("all".equals(view)) return RBAC_JSON_TEAM_JOBS;
                if ("myJobs".equals(view)) return 0;
                if ("open".equals(view)) return RBAC_JSON_OPEN;
                if ("closed".equals(view)) return RBAC_JSON_CLOSED;
                if ("onHold".equals(view)) return RBAC_JSON_ON_HOLD;
                if ("cancelled".equals(view)) return RBAC_JSON_CANCELLED;
                return RBAC_JSON_TEAM_JOBS;
            default:
                return 0;
        }
    }

    private boolean isVisibleToRole(JobInfo job, String executorRole) {
        if ("CustomRoleNothing".equals(executorRole)) return false;
        if ("RestrictedTeamMember".equals(executorRole)) {
            return "restrictedteammember".equals(job.ownerKey);
        }
        if ("CustomRoleTeamOnly".equals(executorRole)) {
            return "owner".equals(job.ownerKey) || "admin".equals(job.ownerKey)
                    || "teammember".equals(job.ownerKey) || "customroleteamonly".equals(job.ownerKey);
        }
        return false;
    }

    private boolean matchesView(JobInfo job, String view, String executorRole) {
        if ("all".equals(view)) return true;
        if ("myJobs".equals(view)) {
            if ("RestrictedTeamMember".equals(executorRole)) return "restrictedteammember".equals(job.ownerKey);
            if ("CustomRoleTeamOnly".equals(executorRole)) return "customroleteamonly".equals(job.ownerKey);
            if ("CustomRoleNothing".equals(executorRole)) return false;
            return false;
        }
        if ("open".equals(view)) return "Open".equalsIgnoreCase(job.statusLabel);
        if ("closed".equals(view)) return "Closed".equalsIgnoreCase(job.statusLabel);
        if ("onHold".equals(view)) return "On Hold".equalsIgnoreCase(job.statusLabel);
        if ("cancelled".equals(view)) return "Canceled".equalsIgnoreCase(job.statusLabel) || "Cancelled".equalsIgnoreCase(job.statusLabel);
        return true;
    }

    private List<String> getExpectedJobNames(String executorRole, String view) {
        List<String> names = jobInfoList.stream()
                .filter(job -> isVisibleToRole(job, executorRole) && matchesView(job, view, executorRole))
                .map(job -> job.name)
                .collect(Collectors.toList());
        Collections.sort(names);
        return names;
    }

    private void assertSearchForView(String executorRole, String view, JSONObject requestBody,
                                     String executorToken, int expectedStatusCode) {
        Map<String, String> queryParameters = createDefaultQueryParameters();
        Response searchResponse = RestClient.doPost1("JSON", ariesServiceURL, ADVANCED_JOB_SEARCH_GET_PATH,
                executorToken, queryParameters, null, true, requestBody.toString());

        assertThat(executorRole + " " + view + " - status", searchResponse.getStatusCode(), equalTo(expectedStatusCode));
        if (expectedStatusCode != 200) return;

        JsonPath searchJp = searchResponse.jsonPath();
        assertThat("meta not null", searchJp.get("meta"), notNullValue());
        assertThat("data not null", searchJp.get("data"), notNullValue());

        if ("CustomRoleNothing".equals(executorRole)) {
            // CustomRoleNothing: response data is an empty list only
            assertThat(executorRole + " " + view + " data should be empty list", searchJp.getInt("data.size()"), equalTo(0));
            return;
        }

        int count = searchJp.getInt("data.size()");
        int expectedCount = getExpectedSearchCount(executorRole, view);
        assertThat(executorRole + " " + view + " count", count, equalTo(expectedCount));

        List<String> expectedNames = getExpectedJobNames(executorRole, view);
        List<String> actualNames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = searchJp.getString("data[" + i + "].name");
            if (name != null) actualNames.add(name);
        }
        Collections.sort(actualNames);
        assertThat(executorRole + " " + view + " job names", actualNames, equalTo(expectedNames));
    }
}
