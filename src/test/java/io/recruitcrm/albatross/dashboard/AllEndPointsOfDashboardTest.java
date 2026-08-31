package io.recruitcrm.albatross.dashboard;

import java.util.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import org.hamcrest.Matchers;
import org.json.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.pojo.Job;
import io.rcrm.api.pojo.albatross.dashboard.*;
import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndPointsOfDashboardTest extends TestBase {

    Object accountOwnerAPIKey;
    Object albatrossTkn;
    commanFunction function = new commanFunction();
    String unautorizedAccountOwnerAPIKey;
    String basePath = "dashboard/widget/get";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        unautorizedAccountOwnerAPIKey = ThreadManager.getAccountApiKey() + "abc";
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getUserValidTestData", groups = "nightly-build")
    public void getTotalDealStats_Test(String accountId) {

        AccountId account = new AccountId();
        account.setOwnerid(accountId + "-2");
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "total_deal_stats");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null,
                true, account);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by total_deal_stats Successful "));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//dashboard//TotalDealStats.json"));
    }

    @Owner("Akshaya Uppala")
    @Test(groups = "nightly-build")
    public void getTotalDealStatsWithEmptyRequestBody_Test() {
        AccountId account = new AccountId();
        account.setOwnerid("");
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "total_deal_stats");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null,
                true, account);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("fail"));
        response.then().body("message",
                Matchers.containsString("Failed To Candidate pipeline by total_deal_stats : Undefined array key 1"));
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getUserValidTestData", groups = "nightly-build")
    public void getTotalJobStats_Test(String accountId) {
        AccountId account = new AccountId();
        account.setOwnerid(accountId);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "total_job_stats");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null,
                true, account);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by total_job_stats Successful "));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//dashboard//TotalJobStats.json"));

    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getUserValidTestData", groups = "nightly-build")
    public void getTotalCandidateStats_Test(String accountId) {

        AccountId account = new AccountId();
        account.setOwnerid(accountId);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "total_candidate_stats");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null,
                true, account);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message",
                Matchers.containsString("Candidate pipeline by total_candidate_stats Successful "));
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi//dashboard//TotalCandidateStats.json"));

    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "candidateAssignmentTestData", groups = "nightly-build")
    public void verifyTotalCandidateStatistics_Test(String accountOwnerId) {
        AccountId account = new AccountId();
        account.setOwnerid(accountOwnerId);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "total_candidate_stats");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true, account);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by total_candidate_stats Successful"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//dashboard//TotalCandidateStats.json"));
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> candidateStatsData = jsonPath.getList("data.records.candidate_stats_data");
        Assert.assertNotNull(candidateStatsData, "Candidate stats data should not be null");
        Map<String, Integer> hiringStageMap = new HashMap<>();
        for (Map<String, Object> stage : candidateStatsData) {
            String label = (String) stage.get("label");
            Integer count = (Integer) stage.get("total_count");
            hiringStageMap.put(label, count);
        }
        Assert.assertEquals(hiringStageMap.get("Assigned"), Integer.valueOf(1), "Assigned stage should have exactly 1 candidate");
        Assert.assertEquals(hiringStageMap.get("Applied"), Integer.valueOf(0), "Applied stage should have 0 candidates");
        Assert.assertEquals(hiringStageMap.get("Interview Not Attended"), Integer.valueOf(0), "Interview Not Attended stage should have 0 candidates");
        Assert.assertEquals(hiringStageMap.get("Interview Rescheduled"), Integer.valueOf(0), "Interview Rescheduled stage should have 0 candidates");
        Assert.assertEquals(hiringStageMap.get("Rejected"), Integer.valueOf(0), "Rejected stage should have 0 candidates");
        Assert.assertEquals(hiringStageMap.get("Placed"), Integer.valueOf(0), "Placed stage should have 0 candidates");
        int totalCount = hiringStageMap.values().stream().mapToInt(Integer::intValue).sum();
        Assert.assertEquals(totalCount, 1, "Total candidate count should be exactly 1");
        Integer totalCandidateFromResponse = jsonPath.get("data.records.total_data.total_candidate");
        Integer totalAssignedCandidate = jsonPath.get("data.records.total_data.total_assigned_candidate");
        Integer totalNotAssignedCandidate = jsonPath.get("data.records.total_data.total_not_assigned_candidate");
        Assert.assertEquals(totalCandidateFromResponse, Integer.valueOf(1), "Total candidates should be exactly 1");
        Assert.assertEquals(totalAssignedCandidate, Integer.valueOf(1), "Total assigned candidates should be exactly 1");
        Assert.assertEquals(totalNotAssignedCandidate, Integer.valueOf(0), "Total not assigned candidates should be exactly 0");
        Assert.assertEquals(totalAssignedCandidate, Integer.valueOf(totalCount), "Total assigned candidates should match sum of hiring stage counts");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "jobStatisticsTestData", groups = "nightly-build")
    public void verifyTotalJobStatistics_Test(String accountOwnerId) {
        AccountId account = new AccountId();
        account.setOwnerid(accountOwnerId);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "total_job_stats");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true, account);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by total_job_stats Successful"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//dashboard//TotalJobStats.json"));
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> jobStatsData = jsonPath.getList("data.records.job_stats_data");
        Assert.assertNotNull(jobStatsData, "Job stats data should not be null");
        Map<String, Integer> jobStatusMap = new HashMap<>();
        for (Map<String, Object> status : jobStatsData) {
            String label = (String) status.get("label");
            Integer count = (Integer) status.get("total_count");
            jobStatusMap.put(label, count);
        }
        Assert.assertEquals(jobStatusMap.get("Open"), Integer.valueOf(1), "Open status should have exactly 1 job");
        Assert.assertEquals(jobStatusMap.get("On Hold"), Integer.valueOf(1), "On Hold status should have exactly 1 job");
        Assert.assertEquals(jobStatusMap.get("Canceled"), Integer.valueOf(1), "Canceled status should have exactly 1 job");
        Assert.assertEquals(jobStatusMap.get("Closed"), Integer.valueOf(1), "Closed status should have exactly 1 job");
        Integer totalCountFromResponse = jsonPath.get("data.records.total_data.total_jobs");
        Assert.assertNotNull(totalCountFromResponse, "Total count should not be null");
        Assert.assertEquals((int) totalCountFromResponse, 4, "Total count should be at least 4 after creating test jobs");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "dealStatisticsTestData", groups = "nightly-build")
    public void verifyTotalDealStatistics_Test(String accountOwnerId) {
        AccountId account = new AccountId();
        account.setOwnerid(accountOwnerId + "-2");
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "total_deal_stats");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true, account);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by total_deal_stats Successful"));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//dashboard//TotalDealStats.json"));
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> dealStatsData = jsonPath.getList("data.records.deal_stats_data");
        Assert.assertNotNull(dealStatsData, "Deal stats data should not be null");
        Map<String, Integer> dealStatusMap = new HashMap<>();
        for (Map<String, Object> status : dealStatsData) {
            String label = (String) status.get("label");
            Integer count = (Integer) status.get("total_count");
            dealStatusMap.put(label, count);
        }
        Assert.assertEquals((int) dealStatusMap.get("Won"), 1, "Won status should have exactly 1 deal");
        Assert.assertEquals((int) dealStatusMap.get("Lost"), 1, "Lost status should have exactly 1 deal");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "candidatePipelineByCompanyTestData", groups = "nightly-build")
    public void verifyCandidatePipelineByCompany_Test(String companyName, String companySlug, List<Integer> hiringStageIds, String teamId) {
        JSONObject hiringData = new JSONObject();
        hiringData.put("ownerid", teamId);
        hiringData.put("hiring_stage_one", hiringStageIds.get(0));
        hiringData.put("hiring_stage_two", hiringStageIds.get(1));
        hiringData.put("hiring_stage_three", hiringStageIds.get(2));
        hiringData.put("hiring_stage_four", hiringStageIds.get(3));
        hiringData.put("hiring_stage_five", hiringStageIds.get(10));
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "company");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true, hiringData);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by company Successful"));
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> companyRecords = jsonPath.getList("data.records");
        Assert.assertNotNull(companyRecords, "Company records should not be null");
        boolean companyFound = false;
        int assignedCandidateCount = 0;
        for (Map<String, Object> record : companyRecords) {
            String recordCompanyName = (String) record.get("companyname");
            if (companyName.equals(recordCompanyName)) {
                companyFound = true;
                Object assignedCount = record.get("1");
                assignedCandidateCount = assignedCount != null ? (Integer) assignedCount : 0;
                break;
            }
        }
        Assert.assertTrue(companyFound, "Created company '" + companyName + "' should be found in pipeline by company response");
        Assert.assertEquals(assignedCandidateCount, 1, "Company should have exactly 1 assigned candidate");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "candidatePipelineByCompanyWithSingleOwnerTestData", groups = "nightly-build")
    public void verifyCandidatePipelineByCompanyWithSingleOwner_Test(String companyName, String companySlug, List<Integer> hiringStageIds, Integer ownerId) {
        HiringData hiringData = createHiringDataPayload(ownerId, hiringStageIds);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "company");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true, hiringData);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by company Successful"));
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> companyRecords = jsonPath.getList("data.records");
        Assert.assertNotNull(companyRecords, "Company records should not be null");
        boolean companyFound = false;
        int assignedCandidateCount = 0;
        for (Map<String, Object> record : companyRecords) {
            String recordCompanyName = (String) record.get("companyname");
            if (companyName.equals(recordCompanyName)) {
                companyFound = true;
                Object assignedCount = record.get("1");
                assignedCandidateCount = assignedCount != null ? (Integer) assignedCount : 0;
                break;
            }
        }
        Assert.assertTrue(companyFound, "Created company '" + companyName + "' should be found in pipeline by company response");
        Assert.assertEquals(assignedCandidateCount, 1, "Company should have exactly 1 assigned candidate");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "jobStatusFilterTestData", groups = "nightly-build")
    public void verifyJobStatusFilterOnCandidatePipelineByCompanyForTeam_Test(String companyNameWithOpenJob, String companyNameWithClosedJob, List<Integer> hiringStageIds, Integer ownerId) {
        HiringData hiringData = createHiringDataPayload(ownerId, hiringStageIds, 0, "", "1");
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "company");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true, hiringData);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by company Successful"));
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> companyRecords = jsonPath.getList("data.records");
        Assert.assertNotNull(companyRecords, "Company records should not be null");
        boolean companyWithOpenJobFound = false;
        boolean companyWithClosedJobFound = false;
        for (Map<String, Object> record : companyRecords) {
            String recordCompanyName = (String) record.get("companyname");
            if (companyNameWithOpenJob.equals(recordCompanyName)) {
                companyWithOpenJobFound = true;
                Object assignedCount = record.get("1");
                Assert.assertNotNull(assignedCount, "Company with open job should have assigned candidates");
                Assert.assertTrue(((Integer) assignedCount) > 0, "Company with open job should have at least 1 assigned candidate");
            } else if (companyNameWithClosedJob.equals(recordCompanyName)) {
                companyWithClosedJobFound = true;
            }
        }
        Assert.assertTrue(companyWithOpenJobFound, "Company with open job '" + companyNameWithOpenJob + "' should appear in filtered results");
        Assert.assertFalse(companyWithClosedJobFound, "Company with closed job '" + companyNameWithClosedJob + "' should NOT appear in filtered results when filtering by open jobs");
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "searchTextFilterTestData", groups = "nightly-build")
    public void verifySearchTextFilterOnCandidatePipelineByCompany_Test(String targetCompanyName, String otherCompanyName, List<Integer> hiringStageIds, Integer ownerId) {
        HiringData hiringData = createHiringDataPayload(ownerId, hiringStageIds, 0, targetCompanyName, "");
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "company");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true, hiringData);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by company Successful"));
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> companyRecords = jsonPath.getList("data.records");
        Assert.assertNotNull(companyRecords, "Company records should not be null");
        boolean targetCompanyFound = false;
        boolean otherCompanyFound = false;
        for (Map<String, Object> record : companyRecords) {
            String recordCompanyName = (String) record.get("companyname");
            if (targetCompanyName.equals(recordCompanyName)) {
                targetCompanyFound = true;
                Object assignedCount = record.get("1");
                Assert.assertNotNull(assignedCount, "Target company should have assigned candidates");
                Assert.assertTrue(((Integer) assignedCount) > 0, "Target company should have at least 1 assigned candidate");
            } else if (otherCompanyName.equals(recordCompanyName)) {
                otherCompanyFound = true;
            }
        }
        Assert.assertTrue(targetCompanyFound, "Target company '" + targetCompanyName + "' should appear in search results");
        Assert.assertFalse(otherCompanyFound, "Other company '" + otherCompanyName + "' should NOT appear when searching for specific company name");
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "teamMemberOwnershipFilterTestData", groups = "nightly-build")
    public void verifyDashboardShowsCompaniesRelatedToSpecificTeamMember_Test(String companyName, String companySlug, List<Integer> hiringStageIds, Integer teamMemberId) {
        HiringData hiringData = createHiringDataPayload(teamMemberId, hiringStageIds, 0, "", "");
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "company");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null, true, hiringData);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by company Successful"));
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> companyRecords = jsonPath.getList("data.records");
        Assert.assertNotNull(companyRecords, "Company records should not be null");
        boolean teamMemberCompanyFound = false;
        for (Map<String, Object> record : companyRecords) {
            String recordCompanyName = (String) record.get("companyname");
            if (companyName.equals(recordCompanyName)) {
                teamMemberCompanyFound = true;
                Object assignedCount = record.get("1");
                Assert.assertNotNull(assignedCount, "Team member's company should have candidates");
                Assert.assertTrue(((Integer) assignedCount) > 0, "Team member's company should have at least 1 assigned candidate");
                break;
            }
        }
        Assert.assertTrue(teamMemberCompanyFound, "Company '" + companyName + "' should appear in dashboard when filtering by the team member who owns it");
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getUserAndHiringPipelineTestData", groups = "nightly-build")
    public void getRecruiterAssigned_Test(ArrayList<String> userData, List<Integer> hiringStageIds) {

        HiringData hiringData = new HiringData();
        hiringData.setOwnerid(Integer.valueOf(userData.get(0)));
        hiringData.setHiring_stage_one(hiringStageIds.get(0));
        hiringData.setHiring_stage_two(hiringStageIds.get(1));
        hiringData.setHiring_stage_three(hiringStageIds.get(2));
        hiringData.setHiring_stage_four(hiringStageIds.get(3));
        hiringData.setHiring_stage_five(hiringStageIds.get(10));
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "recruiter_assigned");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null,
                true, hiringData);
        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message",
                Matchers.containsString("Candidate pipeline by recruiter_assigned Successful "));

        JsonPath jsonPath = response.jsonPath();
        String userName[] = {"Owner", "Admin", "Recruiter", "TeamMember"};
        int recordsSize = jsonPath.getList("data.records").size();
        List<String> fullNameList = jsonPath.getList("data.records.Fullname");
        List<String> emailList = jsonPath.getList("data.records.Email");

        if (userData.size() > 1 && userData.get(1).contains(userName[0])) {
            Assert.assertEquals(recordsSize, 1);
            Assert.assertTrue(fullNameList.get(0).contains(userName[0]));
            Assert.assertTrue(emailList.get(0).contains("ReaperTestAcc"));
        } else if (userData.size() > 1 && userData.get(1).contains(userName[2])) {
            Assert.assertEquals(recordsSize, 1);
            Assert.assertTrue(fullNameList.get(0).contains(userName[2]));
        } else {
            Assert.assertEquals(recordsSize, 4);
            for (int i = 0; i < 4; i++) {
                Assert.assertTrue(fullNameList.get(i).contains(userName[i]));
            }
        }

    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getUserAndHiringPipelineTestData", groups = "nightly-build")
    public synchronized void getCandidatePipelineByOwner_Test(ArrayList<String> userData, List<Integer> hiringStageIds) {

        HiringData hiringData = new HiringData();
        hiringData.setOwnerid(Integer.valueOf(userData.get(0)));
        hiringData.setHiring_stage_one(hiringStageIds.get(0));
        hiringData.setHiring_stage_two(hiringStageIds.get(1));
        hiringData.setHiring_stage_three(hiringStageIds.get(2));
        hiringData.setHiring_stage_four(hiringStageIds.get(3));
        hiringData.setHiring_stage_five(hiringStageIds.get(10));
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "owner");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null,
                true, hiringData);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by owner Successful "));

        JsonPath jsonPath = response.jsonPath();
        String userName[] = {"Owner", "Admin", "Recruiter", "TeamMember"};
        int recordsSize = jsonPath.getList("data.records").size();
        List<String> fullNameList = jsonPath.getList("data.records.Fullname");
        List<String> emailList = jsonPath.getList("data.records.Email");

        if (userData.size() > 1 && userData.get(1).contains(userName[0])) {
            Assert.assertEquals(recordsSize, 1);
            Assert.assertTrue(fullNameList.get(0).contains(userName[0]));
            Assert.assertTrue(emailList.get(0).contains("ReaperTestAcc"));
        } else if (userData.size() > 1 && userData.get(1).contains(userName[2])) {
            Assert.assertEquals(recordsSize, 1);
            Assert.assertTrue(fullNameList.get(0).contains(userName[2]));
        } else {
            Assert.assertEquals(recordsSize, 4);
            for (int i = 0; i < 4; i++) {
                Assert.assertTrue(fullNameList.get(i).contains(userName[i]));
            }
        }
    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getUserAndHiringPipelineTestData", groups = "nightly-build")
    public void getCandidatePipelineByCompany_Test(ArrayList<String> userData, List<Integer> hiringStageIds) {

        HiringData hiringData = new HiringData();
        hiringData.setOwnerid(Integer.valueOf(userData.get(0)));
        hiringData.setHiring_stage_one(hiringStageIds.get(0));
        hiringData.setHiring_stage_two(hiringStageIds.get(1));
        hiringData.setHiring_stage_three(hiringStageIds.get(2));
        hiringData.setHiring_stage_four(hiringStageIds.get(3));
        hiringData.setHiring_stage_five(hiringStageIds.get(10));
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "company");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null,
                true, hiringData);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by company Successful "));
        JsonPath jsonPath = response.jsonPath();
        int recordsSize = jsonPath.getList("data.records").size();
        Assert.assertEquals(recordsSize, 0);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getUserAndHiringPipelineTestData", groups = "nightly-build")
    public void exportOwnerData_Test(ArrayList<String> userData, List<Integer> hiringStageIds) {
        HiringStage hiringStage = new HiringStage();
        hiringStage.setOwnerid(Integer.valueOf(userData.get(0)));
        List<HiringStage.HiringStageCandidate> hiringStageCandidate = new ArrayList<>();
        hiringStage.setHiring_stage_list(hiringStageCandidate);

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "export_owner");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null,
                true, hiringStage);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by export_owner Successful "));

        JsonPath jsonPath = response.jsonPath();
        String userName[] = {"Owner", "Admin", "Recruiter", "TeamMember"};
        int recordsSize = jsonPath.getList("data.records").size();
        List<String> fullNameList = jsonPath.getList("data.records.Fullname");
        List<String> emailList = jsonPath.getList("data.records.Email");

        if (userData.size() > 1 && userData.get(1).contains(userName[0])) {
            Assert.assertEquals(recordsSize, 1);
            Assert.assertTrue(fullNameList.get(0).contains(userName[0]));
            Assert.assertTrue(emailList.get(0).contains("ReaperTestAcc"));
        } else if (userData.size() > 1 && userData.get(1).contains(userName[2])) {
            Assert.assertEquals(recordsSize, 1);
            Assert.assertTrue(fullNameList.get(0).contains(userName[2]));
        } else {
            Assert.assertEquals(recordsSize, 4);
            for (int i = 0; i < 4; i++) {
                Assert.assertTrue(fullNameList.get(i).contains(userName[i]));
            }
        }
    }

    @Owner("Akshaya Uppala")
    @Test(dataProvider = "getUserAndHiringPipelineTestData", groups = "nightly-build")
    public void getExportCompanyData_Test(ArrayList<String> userData, List<Integer> hiringStageIds) {

        HiringStage hiringStage = new HiringStage();
        hiringStage.setOwnerid(Integer.valueOf(userData.get(0)));
        List<HiringStage.HiringStageCandidate> hiringStageCandidate = new ArrayList<>();
        hiringStage.setHiring_stage_list(hiringStageCandidate);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "export_company");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null,
                true, hiringStage);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Candidate pipeline by export_company Successful "));
        JsonPath jsonPath = response.jsonPath();
        int recordsSize = jsonPath.getList("data.records").size();
        Assert.assertEquals(recordsSize, 0);

    }

    @Owner("Sai Teja SG")
    @Test(dataProvider = "getUserAndHiringPipelineTestData", groups = "nightly-build")
    public void exportRecruiterAsignedData_Test(ArrayList<String> userData, List<Integer> hiringStageIds) {

        HiringStage hiringStage = new HiringStage();
        hiringStage.setOwnerid(Integer.valueOf(userData.get(0)));
        List<HiringStage.HiringStageCandidate> hiringStageCandidate = new ArrayList<>();
        hiringStage.setHiring_stage_list(hiringStageCandidate);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "export_recruiter_assigned");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossTkn, queryParameters, null,
                true, hiringStage);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message",
                Matchers.containsString("Candidate pipeline by export_recruiter_assigned Successful "));

        JsonPath jsonPath = response.jsonPath();
        String userName[] = {"Owner", "Admin", "Recruiter", "TeamMember"};
        int recordsSize = jsonPath.getList("data.records").size();
        List<String> fullNameList = jsonPath.getList("data.records.Fullname");
        List<String> emailList = jsonPath.getList("data.records.Email");

        if (userData.size() > 1 && userData.get(1).contains(userName[0])) {
            Assert.assertEquals(recordsSize, 1);
            Assert.assertTrue(fullNameList.get(0).contains(userName[0]));
            Assert.assertTrue(emailList.get(0).contains("ReaperTestAcc"));
        } else if (userData.size() > 1 && userData.get(1).contains(userName[2])) {
            Assert.assertEquals(recordsSize, 1);
            Assert.assertTrue(fullNameList.get(0).contains(userName[2]));
        } else {
            Assert.assertEquals(recordsSize, 4);
            for (int i = 0; i < 4; i++) {
                Assert.assertTrue(fullNameList.get(i).contains(userName[i]));
            }
        }
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void unautorizedUserCannotGetDashboardWidget_Test() {
        HiringStage hiringStage = new HiringStage();
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("dashboardParam", "export_recruiter_assigned");
        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, unautorizedAccountOwnerAPIKey,
                queryParameters, null, true, hiringStage);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }


    @DataProvider(parallel = true)
    public Object[][] getUserValidTestData() {
        Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
        Assert.assertEquals(userResponse.getStatusCode(), 200);
        JsonPath user = userResponse.jsonPath();
        String accountOwnerid = String.valueOf(user.getInt("[0].id"));
        String teamMemberDeactivatedID = String.valueOf(user.getInt("[3].id"));
        String teamMemberFirstName = user.getString("[3].first_name");
        String teamMemberLastNameName = user.getString("[3].last_name");
        Response deactivateUser = function.deactivateUser(teamMemberDeactivatedID, teamMemberFirstName,
                teamMemberLastNameName, albatrossURL, albatrossTkn);
        return new Object[][]{{accountOwnerid}, {teamMemberDeactivatedID}};
    }

    @DataProvider(parallel = true)
    public Object[][] getUserAndHiringPipelineTestData() {
        Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
        JsonPath user = userResponse.jsonPath();
        ArrayList<String> accountOwnerData = extractUserData(user, 0);
        ArrayList<String> teamMemberData = extractUserData(user, 2);
        ArrayList<String> allUser = new ArrayList<>();
        allUser.add(String.valueOf(0));
        function.deactivateUser(teamMemberData.get(0), teamMemberData.get(1), teamMemberData.get(2), albatrossURL,
                albatrossTkn);
        JsonPath hiringPipeline = function.getAllHiringPipeline(hiringPipelineServiceURL, albatrossTkn).jsonPath();
        List<Integer> hiringStageIds = hiringPipeline.getList("default-pipeline.hiring_stages.id");
        return new Object[][]{{accountOwnerData, hiringStageIds}, {teamMemberData, hiringStageIds},
                {allUser, hiringStageIds}};
    }

    @DataProvider
    public Object[][] candidateAssignmentTestData() {
        Response assignResponse = function.assignCandidateToJob(baseURL, accountOwnerAPIKey);
        Assert.assertEquals(assignResponse.getStatusCode(), 200, "Failed to assign candidate to job");
        Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
        Assert.assertEquals(userResponse.getStatusCode(), 200, "Failed to get users");
        JsonPath userJsonPath = userResponse.jsonPath();
        String accountOwnerId = String.valueOf(userJsonPath.getInt("[0].id"));

        return new Object[][]{{accountOwnerId}};
    }

    @DataProvider
    public Object[][] jobStatisticsTestData() {
        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        String companySlug = companyJson.get("slug");
        JsonPath contactJson = function.createNewContact_POST(baseURL, accountOwnerAPIKey, companySlug).jsonPath();
        String contactSlug = contactJson.get("slug");
        createJobWithStatus(companySlug, contactSlug, "1");
        createJobWithStatus(companySlug, contactSlug, "2");
        createJobWithStatus(companySlug, contactSlug, "3");
        createJobWithStatus(companySlug, contactSlug, "0");
        Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
        Assert.assertEquals(userResponse.getStatusCode(), 200, "Failed to get users");
        JsonPath userJsonPath = userResponse.jsonPath();
        String accountOwnerId = String.valueOf(userJsonPath.getInt("[0].id"));
        return new Object[][]{{accountOwnerId}};
    }

    @DataProvider
    public Object[][] dealStatisticsTestData() {
        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        String companySlug = companyJson.get("slug");
        JsonPath contactJson = function.createNewContact_POST(baseURL, accountOwnerAPIKey, companySlug).jsonPath();
        String contactSlug = contactJson.get("slug");
        JsonPath jobJson = function.createNewJob(baseURL, accountOwnerAPIKey, companySlug, contactSlug).jsonPath();
        String jobSlug = jobJson.get("slug");
        createDealWithStatus(companySlug, contactSlug, jobSlug, "1", 12000);
        createDealWithStatus(companySlug, contactSlug, jobSlug, "2", 8000);
        Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
        Assert.assertEquals(userResponse.getStatusCode(), 200, "Failed to get users");
        JsonPath userJsonPath = userResponse.jsonPath();
        String accountOwnerId = String.valueOf(userJsonPath.getInt("[0].id"));
        return new Object[][]{{accountOwnerId}};
    }

    @DataProvider
    public Object[][] candidatePipelineByCompanyTestData() {
        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        String companyName = companyJson.get("company_name");
        String companySlug = companyJson.get("slug");
        JsonPath contactJson = function.createNewContact_POST(baseURL, accountOwnerAPIKey, companySlug).jsonPath();
        String contactSlug = contactJson.get("slug");
        JsonPath jobJson = function.createNewJob(baseURL, accountOwnerAPIKey, companySlug, contactSlug).jsonPath();
        String jobSlug = jobJson.get("slug");
        Response assignResponse = function.assignCandidateByJobSlug(baseURL, accountOwnerAPIKey, jobSlug);
        Assert.assertEquals(assignResponse.getStatusCode(), 200, "Failed to assign candidate to job");
        Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
        Assert.assertEquals(userResponse.getStatusCode(), 200, "Failed to get users");
        JsonPath userJsonPath = userResponse.jsonPath();
        int ownerId = userJsonPath.getInt("[0].id");
        int adminId = userJsonPath.getInt("[1].id");
        String teamId = ownerId + "," + adminId;
        JsonPath hiringPipeline = function.getAllHiringPipeline(hiringPipelineServiceURL, albatrossTkn).jsonPath();
        List<Integer> hiringStageIds = hiringPipeline.getList("default-pipeline.hiring_stages.id");
        return new Object[][]{{companyName, companySlug, hiringStageIds, teamId}};
    }

    @DataProvider
    public Object[][] candidatePipelineByCompanyWithSingleOwnerTestData() {
        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        String companyName = companyJson.get("company_name");
        String companySlug = companyJson.get("slug");
        JsonPath contactJson = function.createNewContact_POST(baseURL, accountOwnerAPIKey, companySlug).jsonPath();
        String contactSlug = contactJson.get("slug");
        JsonPath jobJson = function.createNewJob(baseURL, accountOwnerAPIKey, companySlug, contactSlug).jsonPath();
        String jobSlug = jobJson.get("slug");
        Response assignResponse = function.assignCandidateByJobSlug(baseURL, accountOwnerAPIKey, jobSlug);
        Assert.assertEquals(assignResponse.getStatusCode(), 200, "Failed to assign candidate to job");
        Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
        Assert.assertEquals(userResponse.getStatusCode(), 200, "Failed to get users");
        JsonPath userJsonPath = userResponse.jsonPath();
        Integer ownerId = userJsonPath.getInt("[0].id");
        JsonPath hiringPipeline = function.getAllHiringPipeline(hiringPipelineServiceURL, albatrossTkn).jsonPath();
        List<Integer> hiringStageIds = hiringPipeline.getList("default-pipeline.hiring_stages.id");
        return new Object[][]{{companyName, companySlug, hiringStageIds, ownerId}};
    }

    @DataProvider
    public Object[][] jobStatusFilterTestData() {
        JsonPath companyJson1 = function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        String companyNameWithOpenJob = companyJson1.get("company_name");
        String companySlug1 = companyJson1.get("slug");
        JsonPath contactJson1 = function.createNewContact_POST(baseURL, accountOwnerAPIKey, companySlug1).jsonPath();
        String contactSlug1 = contactJson1.get("slug");
        JsonPath jobJson1 = function.createNewJob(baseURL, accountOwnerAPIKey, companySlug1, contactSlug1).jsonPath();
        String jobSlug1 = jobJson1.get("slug");
        Response assignResponse1 = function.assignCandidateByJobSlug(baseURL, accountOwnerAPIKey, jobSlug1);
        Assert.assertEquals(assignResponse1.getStatusCode(), 200, "Failed to assign candidate to open job");
        JsonPath companyJson2 = function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        String companyNameWithClosedJob = companyJson2.get("company_name");
        String companySlug2 = companyJson2.get("slug");
        JsonPath contactJson2 = function.createNewContact_POST(baseURL, accountOwnerAPIKey, companySlug2).jsonPath();
        String contactSlug2 = contactJson2.get("slug");
        JsonPath jobJson2 = function.createNewJob(baseURL, accountOwnerAPIKey, companySlug2, contactSlug2).jsonPath();
        String jobSlug2 = jobJson2.get("slug");
        Response assignResponse2 = function.assignCandidateByJobSlug(baseURL, accountOwnerAPIKey, jobSlug2);
        Assert.assertEquals(assignResponse2.getStatusCode(), 200, "Failed to assign candidate to job before closing");
        createJobWithStatus(companySlug2, contactSlug2, "0");
        Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
        Assert.assertEquals(userResponse.getStatusCode(), 200, "Failed to get users");
        JsonPath userJsonPath = userResponse.jsonPath();
        Integer ownerId = userJsonPath.getInt("[0].id");
        JsonPath hiringPipeline = function.getAllHiringPipeline(hiringPipelineServiceURL, albatrossTkn).jsonPath();
        List<Integer> hiringStageIds = hiringPipeline.getList("default-pipeline.hiring_stages.id");
        return new Object[][]{{companyNameWithOpenJob, companyNameWithClosedJob, hiringStageIds, ownerId}};
    }

    @DataProvider
    public Object[][] searchTextFilterTestData() {
        JsonPath companyJson1 = function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        String targetCompanyName = companyJson1.get("company_name");
        String companySlug1 = companyJson1.get("slug");
        JsonPath contactJson1 = function.createNewContact_POST(baseURL, accountOwnerAPIKey, companySlug1).jsonPath();
        String contactSlug1 = contactJson1.get("slug");
        JsonPath jobJson1 = function.createNewJob(baseURL, accountOwnerAPIKey, companySlug1, contactSlug1).jsonPath();
        String jobSlug1 = jobJson1.get("slug");
        Response assignResponse1 = function.assignCandidateByJobSlug(baseURL, accountOwnerAPIKey, jobSlug1);
        Assert.assertEquals(assignResponse1.getStatusCode(), 200, "Failed to assign candidate to target company job");
        JsonPath candidateJson2 = function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        String candidateSlug2 = candidateJson2.get("slug");
        JsonPath companyJson2 = function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        String otherCompanyName = companyJson2.get("company_name");
        String companySlug2 = companyJson2.get("slug");
        JsonPath contactJson2 = function.createNewContact_POST(baseURL, accountOwnerAPIKey, companySlug2).jsonPath();
        String contactSlug2 = contactJson2.get("slug");
        JsonPath jobJson2 = function.createNewJob(baseURL, accountOwnerAPIKey, companySlug2, contactSlug2).jsonPath();
        String jobSlug2 = jobJson2.get("slug");
        Response assignResponse2 = function.assignCandidateByJobSlug(baseURL, accountOwnerAPIKey, jobSlug2);
        Assert.assertEquals(assignResponse2.getStatusCode(), 200, "Failed to assign candidate to other company job");
        Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
        Assert.assertEquals(userResponse.getStatusCode(), 200, "Failed to get users");
        JsonPath userJsonPath = userResponse.jsonPath();
        Integer ownerId = userJsonPath.getInt("[0].id");
        JsonPath hiringPipeline = function.getAllHiringPipeline(hiringPipelineServiceURL, albatrossTkn).jsonPath();
        List<Integer> hiringStageIds = hiringPipeline.getList("default-pipeline.hiring_stages.id");
        return new Object[][]{{targetCompanyName, otherCompanyName, hiringStageIds, ownerId}};
    }

    @DataProvider
    public Object[][] teamMemberOwnershipFilterTestData() {
        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        String companyName = companyJson.get("company_name");
        String companySlug = companyJson.get("slug");
        JsonPath contactJson = function.createNewContact_POST(baseURL, accountOwnerAPIKey, companySlug).jsonPath();
        String contactSlug = contactJson.get("slug");
        JsonPath jobJson = function.createNewJob(baseURL, accountOwnerAPIKey, companySlug, contactSlug).jsonPath();
        String jobSlug = jobJson.get("slug");
        Response assignResponse = function.assignCandidateByJobSlug(baseURL, accountOwnerAPIKey, jobSlug);
        Assert.assertEquals(assignResponse.getStatusCode(), 200, "Failed to assign candidate to job");
        Response userResponse = function.getUsers(baseURL, accountOwnerAPIKey);
        Assert.assertEquals(userResponse.getStatusCode(), 200, "Failed to get users");
        JsonPath userJsonPath = userResponse.jsonPath();
        Integer teamMemberId = userJsonPath.getInt("[1].id");
        transferCompanyOwnership(companySlug, teamMemberId);
        JsonPath hiringPipeline = function.getAllHiringPipeline(hiringPipelineServiceURL, albatrossTkn).jsonPath();
        List<Integer> hiringStageIds = hiringPipeline.getList("default-pipeline.hiring_stages.id");
        return new Object[][]{{companyName, companySlug, hiringStageIds, teamMemberId}};
    }

    private void createJobWithStatus(String companySlug, String contactSlug, String jobStatus) {
        Job job = new Job();
        job.setName("Test Job - Status " + jobStatus);
        job.setCompany_slug(companySlug);
        job.setContact_slug(contactSlug);
        job.setNumber_of_openings(1);
        job.setEnable_job_application_form(1);
        job.setJob_description_text("Test job description");
        job.setJob_status(jobStatus);
        Response response = RestClient.doPost("JSON", baseURL, "jobs", accountOwnerAPIKey, null, true, job);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create job with status " + jobStatus);
    }

    private void createDealWithStatus(String companySlug, String contactSlug, String jobSlug, String dealStage, int dealValue) {
        Deal deal = new Deal();
        deal.setName("Test Deal - Stage " + dealStage);
        deal.setDeal_value(dealValue);
        deal.setClose_date("2024-12-31");
        deal.setDeal_stage(dealStage);
        deal.setDeal_type("1");
        deal.setCompany_slug(companySlug);
        deal.setContact_slugs(contactSlug);
        deal.setJob_slug(jobSlug);
        Response response = RestClient.doPost("JSON", baseURL, "deals", accountOwnerAPIKey, null, true, deal);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create deal with stage " + dealStage);
    }

    private ArrayList<String> extractUserData(JsonPath user, int index) {
        ArrayList<String> userData = new ArrayList<>();
        userData.add(String.valueOf(user.getInt("[" + index + "].id")));
        userData.add(user.getString("[" + index + "].first_name"));
        userData.add(user.getString("[" + index + "].last_name"));
        userData.add(user.getString("[" + index + "].email"));
        return userData;
    }

    private void transferCompanyOwnership(String companySlug, Integer newOwnerId) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("company", companySlug);
        String basePath = "companies/{company}";
        Response companyResponse = RestClient.doGet("JSON", albatrossURL, basePath, albatrossTkn, null, pathParameters, true);
        Assert.assertEquals(companyResponse.getStatusCode(), 200, "Failed to get company details from albatross API");
        JsonPath companyJsonPath = companyResponse.jsonPath();
        Map<String, Object> companyMap = companyJsonPath.get("data.company");
        JSONObject companyData = new JSONObject(companyMap);
        JSONObject transferPayload = new JSONObject();
        transferPayload.put("relatedtotypeid", 3);
        transferPayload.put("selectedowner", newOwnerId);
        JSONArray selectedRows = new JSONArray();
        selectedRows.put(companyData);
        transferPayload.put("selectedrows", selectedRows);
        String transferEndpoint = "users/transfer-ownership/" + newOwnerId;
        Response transferResponse = RestClient.doPost("JSON", albatrossURL, transferEndpoint, albatrossTkn, null, true, transferPayload.toString());
        Assert.assertEquals(transferResponse.getStatusCode(), 200, "Failed to transfer company ownership to team member");
    }

    private HiringData createHiringDataPayload(Integer ownerId, List<Integer> hiringStageIds) {
        return createHiringDataPayload(ownerId, hiringStageIds, null, null, null);
    }

    private HiringData createHiringDataPayload(Integer ownerId, List<Integer> hiringStageIds, Integer offset, String searchText, String jobStatusFilterValues) {
        HiringData hiringData = new HiringData();
        hiringData.setOwnerid(ownerId);
        hiringData.setHiring_stage_one(hiringStageIds.get(0));
        hiringData.setHiring_stage_two(hiringStageIds.get(1));
        hiringData.setHiring_stage_three(hiringStageIds.get(2));
        hiringData.setHiring_stage_four(hiringStageIds.get(3));
        hiringData.setHiring_stage_five(hiringStageIds.get(10));
        if (offset != null) {
            hiringData.setOffset(offset);
        }
        if (searchText != null) {
            hiringData.setSearch_text(searchText);
        }
        if (jobStatusFilterValues != null) {
            hiringData.setJobstatusfiltervalues(jobStatusFilterValues);
        }
        return hiringData;
    }

}
