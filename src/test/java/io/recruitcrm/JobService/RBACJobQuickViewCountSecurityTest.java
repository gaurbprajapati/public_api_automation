package io.recruitcrm.JobService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
public class RBACJobQuickViewCountSecurityTest extends TestBase {

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    Map<String, String> companyKeyToIdMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();
    Map<String, String> userMap = new HashMap<>();
    Map<String, String> teamMap = new HashMap<>();
    Map<String, Integer> jobStatusIdMap = new HashMap<>();
    Map<String, Integer> qualificationIdMap = new HashMap<>();
    Map<String, Integer> hiringPipelineIdMap = new HashMap<>();

    private String commonCompanySlug;
    private String commonContactSlug;
    private String apiAuthToken;
    private commanFunction commanFunction;
    private AllCrudFunctions allCrudFunctions;
    String albatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;

    @BeforeClass(alwaysRun = true)
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
    }

    @Owner("Raj Pandey")
    @Test(groups = {"role-based", "job-quick-view-count-access", "job_service", "nightly-build"})
    public void testJobQuickViewCount_RBAC_ViewOwnerOnly() {
        if (albatrossTknMap == null || albatrossTknMap.isEmpty()) {
            throw new AssertionError("RBAC tokens not available. Skipping RBAC test.");
        }

        String executorToken = albatrossTknMap.get("RestrictedTeamMember");
        if (executorToken == null) {
            throw new AssertionError("Executor token not available for role: " + "RestrictedTeamMember");
        }

        Response countResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                executorToken, null, null, true);

        assertThat(countResponse.getStatusCode(), equalTo(200));

        JsonPath countJp = countResponse.jsonPath();

        // RestrictedTeamMember (View Owner Only):
        int allJobs = countJp.getInt("data[0].allJobs");
        int myJobs = countJp.getInt("data[0].myJobs");
        int openJobs = countJp.getInt("data[0].openJobs");
        int closedJobs = countJp.getInt("data[0].closedJobs");
        int onHoldJobs = countJp.getInt("data[0].onHoldJobs");
        int cancelledJobs = countJp.getInt("data[0].cancelledJobs");
        int archivedJobs = countJp.getInt("data[0].archivedJobs");
        assertThat("RestrictedTeamMember allJobs should be owner-only count", allJobs, equalTo(1));
        assertThat("RestrictedTeamMember myJobs should equal allJobs (only own jobs)", myJobs, equalTo(allJobs));
        assertThat("RestrictedTeamMember openJobs (job13 is Open)", openJobs, equalTo(1));
        assertThat("RestrictedTeamMember closedJobs", closedJobs, equalTo(0));
        assertThat("RestrictedTeamMember onHoldJobs", onHoldJobs, equalTo(0));
        assertThat("RestrictedTeamMember cancelledJobs", cancelledJobs, equalTo(0));
        assertThat("RestrictedTeamMember archivedJobs", archivedJobs, equalTo(0));
        assertThat("RestrictedTeamMember myJobs should not exceed allJobs", myJobs, lessThanOrEqualTo(allJobs));

    }

    @Owner("Raj Pandey")
    @Test(groups = {"role-based", "job-quick-view-count-access", "job_service", "nightly-build"})
    public void testJobQuickViewCount_RBAC_Nothing() {
        if (albatrossTknMap == null || albatrossTknMap.isEmpty()) {
            throw new AssertionError("RBAC tokens not available. Skipping RBAC test.");
        }

        String executorToken = albatrossTknMap.get("CustomRoleNothing");
        if (executorToken == null) {
            throw new AssertionError("Executor token not available for role: " + "CustomRoleNothing");
        }

        Response countResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                executorToken, null, null, true);

        assertThat(countResponse.getStatusCode(), equalTo(200));
        JsonPath countJp = countResponse.jsonPath();

        // CustomRoleNothing (Nothing):
        int allJobs = countJp.getInt("data[0].allJobs");
        int myJobs = countJp.getInt("data[0].myJobs");
        int openJobs = countJp.getInt("data[0].openJobs");
        int closedJobs = countJp.getInt("data[0].closedJobs");
        int onHoldJobs = countJp.getInt("data[0].onHoldJobs");
        int cancelledJobs = countJp.getInt("data[0].cancelledJobs");
        int archivedJobs = countJp.getInt("data[0].archivedJobs");
        int notInAnyHotlist = countJp.getInt("data[0].notInAnyHotlist");
        assertThat("CustomRoleNothing allJobs", allJobs, equalTo(0));
        assertThat("CustomRoleNothing myJobs", myJobs, equalTo(0));
        assertThat("CustomRoleNothing openJobs", openJobs, equalTo(0));
        assertThat("CustomRoleNothing closedJobs", closedJobs, equalTo(0));
        assertThat("CustomRoleNothing onHoldJobs", onHoldJobs, equalTo(0));
        assertThat("CustomRoleNothing cancelledJobs", cancelledJobs, equalTo(0));
        assertThat("CustomRoleNothing archivedJobs", archivedJobs, equalTo(0));
        assertThat("CustomRoleNothing notInAnyHotlist", notInAnyHotlist, equalTo(0));

    }

    @Owner("Raj Pandey")
    @Test(groups = {"role-based", "job-quick-view-count-access", "job_service", "nightly-build"})
    public void testJobQuickViewCount_RBAC_TeamAccess() {
        if (albatrossTknMap == null || albatrossTknMap.isEmpty()) {
            throw new AssertionError("RBAC tokens not available. Skipping RBAC test.");
        }

        String executorToken = albatrossTknMap.get("CustomRoleTeamOnly");
        if (executorToken == null) {
            throw new AssertionError("Executor token not available for role: " + "CustomRoleTeamOnly");
        }

        Response countResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                executorToken, null, null, true);

        assertThat(countResponse.getStatusCode(), equalTo(200));
        JsonPath countJp = countResponse.jsonPath();

        // CustomRoleTeamOnly (Team Access)
        int allJobs = countJp.getInt("data[0].allJobs");
        int myJobs = countJp.getInt("data[0].myJobs");
        int openJobs = countJp.getInt("data[0].openJobs");
        int closedJobs = countJp.getInt("data[0].closedJobs");
        int onHoldJobs = countJp.getInt("data[0].onHoldJobs");
        int cancelledJobs = countJp.getInt("data[0].cancelledJobs");
        int archivedJobs = countJp.getInt("data[0].archivedJobs");
        assertThat("CustomRoleTeamOnly allJobs (team jobs only)", allJobs, equalTo(12));
        assertThat("CustomRoleTeamOnly myJobs (no job in JSON owned by CustomRoleTeamOnly)", myJobs, equalTo(0));
        assertThat("CustomRoleTeamOnly openJobs", openJobs, equalTo(3));
        assertThat("CustomRoleTeamOnly closedJobs", closedJobs, equalTo(3));
        assertThat("CustomRoleTeamOnly onHoldJobs", onHoldJobs, equalTo(3));
        assertThat("CustomRoleTeamOnly cancelledJobs", cancelledJobs, equalTo(3));
        assertThat("CustomRoleTeamOnly archivedJobs", archivedJobs, equalTo(0));
        assertThat("CustomRoleTeamOnly myJobs should not exceed allJobs", myJobs, lessThanOrEqualTo(allJobs));

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

        // Replace job status placeholders (API may return "Cancelled" vs "Canceled")
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
    }

    public Map<String, String> createUserMap() {
        Map<String, String> userMap = new HashMap<>();
        Response response = commanFunction.getUsers(baseURL, apiAuthToken);
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        userMap.put("owner", user.get("[0].id").toString());
        userMap.put("admin", user.get("[1].id").toString());
        userMap.put("restrictedteammember", user.get("[2].id").toString());
        userMap.put("teammember", user.get("[3].id").toString());
        userMap.put("customroleteamonly", user.get("[4].id").toString());
        userMap.put("customrolenothing",user.get("[5].id").toString());
        return userMap;
    }

    public Map<String, String> createTeamMap() {
        Map<String, String> teamMap = new HashMap<>();
        ArrayList<String> userId = new ArrayList<String>();
        userId.add(String.valueOf(userMap.get("owner")));
        userId.add(String.valueOf(userMap.get("admin")));
        userId.add(String.valueOf(userMap.get("teammember")));
        userId.add(String.valueOf(userMap.get("customroleteamonly")));

        Response response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", userId);
        response.then().statusCode(200);
        Response team = commanFunction.getTeams(baseURL, apiAuthToken);
        String teamId = team.jsonPath().getString("[0].team_id");
        teamMap.put("team", teamId);
        return teamMap;
    }

    public Map<String, Integer> createJobStatusMap() {
        Map<String, Integer> statusMap = new HashMap<>();
        try {
            statusMap = commanFunction.getJobStatusValues(albatrossURL, albatrossAuthToken);
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
}
