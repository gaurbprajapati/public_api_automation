package io.recruitcrm.CandidateService.Filters;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.listeners.FilterSearchReporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreatedByFilterCandidateTest extends FilterSearchBaseTest {
    
    AllCrudFunctions function = new AllCrudFunctions();
    commanFunction commonFunc = new commanFunction();
    String albatrossAuthToken;
    String email;
    
    private Map<String, String> userIdToNameMap = new HashMap<>();
    private Map<String, String> teamIdToNameMap = new HashMap<>();
    private List<String> testUserIds = new ArrayList<>();
    private List<String> testTeamIds = new ArrayList<>();
    private Map<String, String> userTokenMap = new HashMap<>();

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        setupUsersAndTeams();
        createTestData();
        waitForDataSync();
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "createdByFilterTestData", description = "Filter Search Test for Created By Field")
    public void createdByFilterTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult) {
        String processedFilterValue = processFilterValue(filterValue);
        String processedExpectedResult = processExpectedResult(expectedResult);
        FilterSearchReporter.logInfo("Account: ", email);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, processedFilterValue, dbField);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        
        Assert.assertEquals(response.getStatusCode(), 200, "API call failed with status: " + response.getStatusCode());
        
        JSONArray data = getFilteredData(response);
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Candidates fetched successfully");
        
        if (!processedExpectedResult.isEmpty()) {
            validateExpectedUsers(data, processedExpectedResult, filterType);
        }
    }

    @DataProvider(name = "createdByFilterTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateCreatedByFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{
                    key, 
                    test.getString("filterType"), 
                    test.getString("filterValue"), 
                    test.getString("dbField"),
                    test.getString("expectedResult")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    private void setupUsersAndTeams() {
        String ownerToken = ThreadManager.getOwnerAlbatrossToken();
        String adminToken = ThreadManager.getAlbatrossToken("Admin");
        String teamMemberToken = ThreadManager.getAlbatrossToken("TeamMember");
        String restrictedToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        
        Response usersResponse = commonFunc.getUsers(baseURL, ThreadManager.getAccountApiKey());
        usersResponse.then().statusCode(200);
        JsonPath usersJson = usersResponse.jsonPath();
        
        List<Map<String, Object>> users = usersJson.getList("$");
        List<String> availableTokens = Arrays.asList(ownerToken, adminToken, teamMemberToken, restrictedToken);
        
        for (int i = 0; i < Math.min(users.size(), 4); i++) {
            Map<String, Object> user = users.get(i);
            String userId = String.valueOf(user.get("id"));
            String userName = (String) user.get("first_name") + " " + (String) user.get("last_name");
            
            userIdToNameMap.put(userId, userName);
            testUserIds.add(userId);
            userTokenMap.put(userId, availableTokens.get(i));
        }
        
        createTestTeam();
    }
    
    private void createTestTeam() {
        if (testUserIds.isEmpty()) return;
        
        List<String> userIds = new ArrayList<>();
        userIds.add(testUserIds.get(0));
        
        Response createTeamResponse = function.createTeam(albatrossURL, albatrossAuthToken, "TestTeam_" + System.currentTimeMillis(), userIds);
        if (createTeamResponse.getStatusCode() != 200) return;
        
        Response updatedTeamsResponse = commonFunc.getTeams(baseURL, ThreadManager.getAccountApiKey());
        if (updatedTeamsResponse.getStatusCode() != 200) return;
        
        JsonPath teamsJson = updatedTeamsResponse.jsonPath();
        List<Map<String, Object>> teams = teamsJson.getList("$");
        
        if (!teams.isEmpty()) {
            Map<String, Object> team = teams.get(0);
            String teamId = String.valueOf(team.get("team_id"));
            String teamName = (String) team.get("team_name");
            teamIdToNameMap.put(teamId, teamName);
            testTeamIds.add(teamId);
        }
    }

    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            List<String> candidateKeys = candidateJson.keySet().stream()
                .filter(key -> key.startsWith("candidate"))
                .collect(Collectors.toList());
            
            for (int i = 0; i < Math.min(candidateKeys.size(), testUserIds.size()) && i < 4; i++) {
                final String candidateKey = candidateKeys.get(i);
                final String userId = testUserIds.get(i);
                final String userToken = userTokenMap.get(userId);
                
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                    JSONObject payload = new JSONObject(candidateEntry.getJSONObject("payload").toString());
                    Response response = function.createCandidateWithJson(albatrossURL, userToken, payload);
                    Assert.assertEquals(response.getStatusCode(), 200, "Failed to create candidate with user " + userId + " (status: " + response.getStatusCode() + ")");
                }, executor);
                futures.add(future);
            }
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
        } finally {
            executor.shutdown();
        }
    }

    private String processFilterValue(String filterValue) {
        if (filterValue.isEmpty()) {
            return filterValue;
        }
        
        String processed = filterValue;
        
        for (int i = 0; i < testUserIds.size(); i++) {
            processed = processed.replace("USER_ID_" + (i + 1), testUserIds.get(i));
            processed = processed.replace("USER_NAME_" + (i + 1), userIdToNameMap.get(testUserIds.get(i)));
        }
        
        for (int i = 0; i < testTeamIds.size(); i++) {
            processed = processed.replace("TEAM_ID_" + (i + 1), testTeamIds.get(i));
            processed = processed.replace("TEAM_NAME_" + (i + 1), teamIdToNameMap.get(testTeamIds.get(i)));
        }
        
        return processed;
    }

    private String processExpectedResult(String expectedResult) {
        if (expectedResult.isEmpty()) {
            return expectedResult;
        }
        
        String processed = expectedResult;
        
        for (int i = 0; i < testUserIds.size(); i++) {
            processed = processed.replace("USER_ID_" + (i + 1), testUserIds.get(i));
        }
        
        return processed;
    }

    private JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField) {
        JSONObject payload = new JSONObject();
        
        payload.put("defaultFilterList", JSONObject.NULL);
        
        JSONObject filterSearchList = new JSONObject();
        
        JSONObject filter = new JSONObject();
        filter.put("groupType", "candidates");
        filter.put("filterName", fieldName);
        filter.put("dbField", dbField);
        filter.put("filterValue", filterValue.isEmpty() ? "" : filterValue);
        filter.put("filterType", filterType);
        filter.put("fieldType", "dropdown");
        
        if (!filterValue.isEmpty() && filterValue.startsWith("{")) {
            JSONObject filterValueObj = new JSONObject(filterValue);
            String filterBarLabel = generateFilterBarLabel(filterValueObj);
            filter.put("filterBarLabel", filterBarLabel);
        } else {
            filter.put("filterBarLabel", "");
        }
        
        filter.put("isModalOpen", false);
        
        JSONArray filters = new JSONArray();
        filters.put(filter);
        
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");
        groupFilterList.put("filters", filters);
        
        JSONArray groupFilterArray = new JSONArray();
        groupFilterArray.put(groupFilterList);
        
        filterSearchList.put("groupFilterList", groupFilterArray);
        filterSearchList.put("groupJoinOperator", "AND");
        
        payload.put("filterSearchList", filterSearchList);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }

    private String generateFilterBarLabel(JSONObject filterValue) {
        List<String> labels = new ArrayList<>();
        
        JSONArray users = filterValue.optJSONArray("User");
        if (users != null && users.length() > 0) {
            for (int i = 0; i < users.length(); i++) {
                String userId = users.get(i).toString();
                String userName = userIdToNameMap.get(userId);
                if (userName != null) {
                    labels.add(userName);
                }
            }
        }
        
        JSONArray teams = filterValue.optJSONArray("Team");
        if (teams != null && teams.length() > 0) {
            for (int i = 0; i < teams.length(); i++) {
                String teamId = teams.get(i).toString();
                String teamName = teamIdToNameMap.get(teamId);
                if (teamName != null) {
                    labels.add(teamName);
                }
            }
        }
        
        return labels.isEmpty() ? "Unknown" : String.join(", ", labels);
    }

    private void validateExpectedUsers(JSONArray data, String expectedResult, String filterType) {
        Set<String> expectedIdSet = new HashSet<>();
        
        if (!expectedResult.isEmpty()) {
            String[] expectedUserIds = expectedResult.split(",");
            for (String userId : expectedUserIds) {
                if (!userId.trim().isEmpty()) {
                    expectedIdSet.add(userId.trim());
                }
            }
        }
        
        Set<String> actualIdSet = new HashSet<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject candidate = data.getJSONObject(i);
            actualIdSet.add(String.valueOf(candidate.getInt("createdby")));
        }
        
        Assert.assertEquals(actualIdSet, expectedIdSet, 
            "Expected users " + expectedIdSet + " but found " + actualIdSet + " for filter: " + filterType);
    }
}