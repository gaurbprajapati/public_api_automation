package io.recruitcrm.CandidateService.Filters;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class HotlistCrossEntityCandidateFilterSearchTest extends FilterSearchBaseTest {

    private static final Pattern COMPANY_SLUG_PLACEHOLDER = Pattern.compile("^\\{company_slug(\\d+)\\}$");
    private static final String ADVANCED_SEARCH_CANDIDATES_CROSS_ENTITY_SUCCESS = "Entities retrieved successfully";

    private final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    private commanFunction function;

    private String ownerAlbatrossAuthToken;
    private String adminAlbatrossAuthToken;
    private String teamMemberAlbatrossAuthToken;
    private String restrictedTeamMemberAlbatrossAuthToken;
    private String apiKey;
    private String email;

    private final Map<String, Integer> hotlistIdMap = new HashMap<>();

    private final Map<String, String> companyKeyToIdMap = new HashMap<>();
    private final Map<String, String> companyCfKeyToSlugMap = new HashMap<>();

    private Map<String, Integer> customFieldIds = new HashMap<>();
    private final Map<String, String> entityCFValueMap = new HashMap<>();
    private final ConcurrentHashMap<String, String> candidateKeyToIdMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> candidateIdToKeyMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> fixtureCandidateKeyToCompanySlugMap = new ConcurrentHashMap<>();
    private final Map<String, List<JsonPath>> companyDataByCandidateId = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        function = new commanFunction();
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        apiKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        customFieldIds = createCustomFields();
        createEntityCFValueMap();
        JSONObject companyJson = createCompaniesFromCompanyData();
        Map<String, Integer> companyIdByKey = new HashMap<>();
        for (String key : companyJson.keySet()) {
            if (key.startsWith("company")) {
                String idStr = companyKeyToIdMap.get(key.toLowerCase());
                if (idStr != null) {
                    companyIdByKey.put(key, Integer.parseInt(idStr));
                }
            }
        }
        addCompaniesToHotlists(companyJson, companyIdByKey);
        createCandidatesFromCrossEntityData();
        loadCompanyJsonPathsForFixtureCandidates();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "hotlistCrossEntityCandidateFilterData", description = "Cross-entity candidate search on company hotlist")
    public void hotlistCrossEntityCandidateFilterSearchTest(
            String fieldName,
            String filterType,
            String filterValue,
            String dbField,
            String expectedResult,
            String fieldType,
            String filterValue_TYPE) {
        JSONObject payload = createCrossEntityHotlistFilterPayloadForCandidates(
                filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200,
                "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().getString("meta.message"), ADVANCED_SEARCH_CANDIDATES_CROSS_ENTITY_SUCCESS,
                "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        validateCrossEntityCandidateResultsByExpectedCompanies(data, expectedResult);
    }

    private void validateCrossEntityCandidateResultsByExpectedCompanies(JSONArray candidateData, String expectedResult) {
        if ("Empty".equals(expectedResult)) {
            Assert.assertEquals(candidateData.length(), 0, "Expected empty candidate result but response has data");
            return;
        }
        if ("All".equals(expectedResult)) {
            Set<Integer> expectedCandidateIds = allFixtureCandidateIdsWithLinkedCompany();
            assertCandidateIdSetsEqual(candidateData, expectedCandidateIds, expectedResult);
            return;
        }

        Set<Integer> expectedCompanyIds = new HashSet<>();
        for (String token : expectedResult.split(",")) {
            String normalizedKey = token.trim().toLowerCase().replace(" ", "");
            String idStr = companyKeyToIdMap.get(normalizedKey);
            if (idStr == null) {
                Assert.fail("Expected company key '" + token + "' (normalized: '" + normalizedKey + "') not found in companyKeyToIdMap. Keys: " + companyKeyToIdMap.keySet());
            }
            expectedCompanyIds.add(Integer.parseInt(idStr));
        }

        Set<Integer> expectedCandidateIds = new HashSet<>();
        for (Map.Entry<String, List<JsonPath>> e : companyDataByCandidateId.entrySet()) {
            List<JsonPath> paths = e.getValue();
            if (paths == null || paths.isEmpty()) {
                continue;
            }
            int companyId = paths.get(0).getInt("data.company.id");
            if (expectedCompanyIds.contains(companyId)) {
                expectedCandidateIds.add(Integer.parseInt(e.getKey()));
            }
        }

        assertCandidateIdSetsEqual(candidateData, expectedCandidateIds, expectedResult);
    }

    private Set<Integer> allFixtureCandidateIdsWithLinkedCompany() {
        Set<Integer> ids = new HashSet<>();
        for (String candidateId : companyDataByCandidateId.keySet()) {
            ids.add(Integer.parseInt(candidateId));
        }
        return ids;
    }

    private void assertCandidateIdSetsEqual(JSONArray candidateData, Set<Integer> expectedCandidateIds, String expectedResult) {
        Set<Integer> actualCandidateIds = new HashSet<>();
        for (int i = 0; i < candidateData.length(); i++) {
            actualCandidateIds.add(candidateData.getJSONObject(i).getInt("id"));
        }
        Assert.assertEquals(actualCandidateIds, expectedCandidateIds,
                "Candidate id set from search does not match expected. expectedSpec="
                        + expectedResult + " expectedCandidateIds=" + expectedCandidateIds + " actual=" + actualCandidateIds);
    }

    private JSONObject createCrossEntityHotlistFilterPayloadForCandidates(
            String filterType,
            String filterValue,
            String dbField,
            String fieldType,
            String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        JSONObject defaultFilterInner = new JSONObject();
        defaultFilterInner.put("filters", new JSONArray());
        defaultFilterInner.put("subGroupJoinOperator", "AND");
        JSONObject defaultFilterRoot = new JSONObject();
        defaultFilterRoot.put("defaultFilterList", defaultFilterInner);
        payload.put("defaultFilterList", defaultFilterRoot);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        JSONObject filterValueObj;
        if (filterValue == null || filterValue.trim().isEmpty()) {
            filterValueObj = emptyFilterValue(filterValue_TYPE);
        } else {
            List<Integer> hotlistIds = parseHotlistIds(filterValue);
            filterValueObj = new JSONObject();
            filterValueObj.put("type", filterValue_TYPE);
            JSONArray valueArray = new JSONArray();
            for (Integer id : hotlistIds) {
                valueArray.put(id);
            }
            filterValueObj.put("value", valueArray);
        }

        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");
        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", true);
        filter.put("groupType", "companies");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "company");
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

    private List<Integer> parseHotlistIds(String filterValue) {
        List<Integer> hotlistIds = new ArrayList<>();
        if (filterValue == null || filterValue.trim().isEmpty()) {
            return hotlistIds;
        }
        for (String raw : filterValue.split(",")) {
            String placeholder = raw.trim();
            if (placeholder.isEmpty()) {
                continue;
            }
            String key = (placeholder.startsWith("{") && placeholder.endsWith("}"))
                    ? placeholder.substring(1, placeholder.length() - 1)
                    : placeholder;
            Integer id = hotlistIdMap.get(key);
            if (id == null) {
                Assert.fail("Hotlist ID not found for: " + placeholder + ". Available hotlists: " + hotlistIdMap.keySet());
            }
            hotlistIds.add(id);
        }
        return hotlistIds;
    }

    private void addCompaniesToHotlists(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        Map<String, List<Integer>> hotlistCompanyIdsMap = new HashMap<>();
        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            if (!companyEntry.has("hotlist") || companyEntry.isNull("hotlist")) {
                continue;
            }
            String companyHotlists = companyEntry.getString("hotlist");
            if (companyHotlists == null || companyHotlists.isEmpty()) {
                continue;
            }
            Integer companyId = companyIdMap.get(companyKey);
            String[] hotlistNames = companyHotlists.split(",");
            for (String hotlistName : hotlistNames) {
                hotlistName = hotlistName.trim();
                if (!hotlistName.isEmpty()) {
                    hotlistCompanyIdsMap.computeIfAbsent(hotlistName, k -> new ArrayList<>()).add(companyId);
                }
            }
        }
        for (Map.Entry<String, List<Integer>> entry : hotlistCompanyIdsMap.entrySet()) {
            String hotlistName = entry.getKey();
            List<Integer> companyIds = entry.getValue();
            if (!companyIds.isEmpty()) {
                Response response = allCrudFunctions.createCompanyHotlist(albatrossURL, ownerAlbatrossAuthToken, hotlistName, companyIds);
                Assert.assertEquals(response.getStatusCode(), 200, "Failed to add companies to hotlist: " + hotlistName);
                int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");
                hotlistIdMap.put(hotlistName, hotlistId);
            }
        }
        Response response = allCrudFunctions.createCompanyHotlist(albatrossURL, ownerAlbatrossAuthToken, "hotlist4", new ArrayList<>());
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create hotlist without any companies");
        int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");
        hotlistIdMap.put("hotlist4", hotlistId);
    }

    private JSONObject createCompaniesFromCompanyData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>>> createFutures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.supplyAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        String createdBy = companyEntry.getString("createdBy");
                        String token = getAlbatrossAuthToken(createdBy);
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, token, payload);
                        response.then().statusCode(200);
                        JsonPath jp = response.jsonPath();
                        String slug = jp.getString("data.company.slug");
                        int companyId = jp.getInt("data.company.id");
                        return Map.entry(companyKey, Map.entry(slug, companyId));
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>> future : createFutures) {
                Map.Entry<String, Map.Entry<String, Integer>> entry = future.join();
                String key = entry.getKey().toLowerCase();
                companyKeyToIdMap.put(key, String.valueOf(entry.getValue().getValue()));
                companyCfKeyToSlugMap.put(key, entry.getValue().getKey());
            }
        } finally {
            executor.shutdown();
        }
        return companyJson;
    }

    private String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy) {
            case "owner":
                return ownerAlbatrossAuthToken;
            case "admin":
                return adminAlbatrossAuthToken;
            case "teamMember":
                return teamMemberAlbatrossAuthToken;
            case "restrictedTeamMember":
                return restrictedTeamMemberAlbatrossAuthToken;
            default:
                return ownerAlbatrossAuthToken;
        }
    }

    private void createCandidatesFromCrossEntityData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/candidateCrossEntity_Data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = candidateJson.keySet().stream()
                    .filter(key -> key.startsWith("candidate"))
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                        JSONObject payload = new JSONObject(candidateEntry.getJSONObject("payload").toString());
                        JSONObject processedPayload = processPayloadPlaceholders(payload);
                        applyCandidateCompanySlugPlaceholder(processedPayload);
                        Response response = allCrudFunctions.createCandidateWithJson(
                                albatrossURL, ownerAlbatrossAuthToken, processedPayload);
                        response.then().statusCode(200);
                        int candidateId = response.jsonPath().getInt("data.candidate.id");
                        candidateIdToKeyMap.put(String.valueOf(candidateId), candidateKey);
                        candidateKeyToIdMap.put(candidateKey, String.valueOf(candidateId));
                        String companySlug = processedPayload.optString("candidate_company_slug", "");
                        if (!companySlug.isEmpty()) {
                            fixtureCandidateKeyToCompanySlugMap.put(candidateKey, companySlug);
                        }
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void loadCompanyJsonPathsForFixtureCandidates() {
        for (Map.Entry<String, String> e : fixtureCandidateKeyToCompanySlugMap.entrySet()) {
            String candidateKey = e.getKey();
            String companySlug = e.getValue();
            String candidateId = candidateKeyToIdMap.get(candidateKey);
            if (candidateId == null) {
                continue;
            }
            Response companyResponse = getCompany(companySlug);
            JsonPath companyJsonPath = companyResponse.jsonPath();
            List<JsonPath> list = new ArrayList<>();
            list.add(companyJsonPath);
            synchronized (companyDataByCandidateId) {
                companyDataByCandidateId.put(candidateId, list);
            }
        }
    }

    private void applyCandidateCompanySlugPlaceholder(JSONObject payload) {
        if (!payload.has("candidate_company_slug")) {
            return;
        }
        String value = payload.getString("candidate_company_slug");
        Matcher m = COMPANY_SLUG_PLACEHOLDER.matcher(value);
        if (!m.matches()) {
            return;
        }
        String companyKey = "company" + m.group(1);
        String slug = companyCfKeyToSlugMap.get(companyKey);
        Assert.assertNotNull(slug, "No company slug for placeholder " + value + " (expected " + companyKey + " in company_data)");
        payload.put("candidate_company_slug", slug);
    }

    public Response getCompany(String companySlug) {
        String basePath = "/companies/{companySlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companySlug", companySlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, ownerAlbatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    @DataProvider(name = "hotlistCrossEntityCandidateFilterData", parallel = true)
    public Object[][] hotlistCrossEntityCandidateFilterData() {
        JSONObject filterData = readJsonFileFromPath(
                "src/test/resources/filtersDataProvider/candidateHotlistCrossEntityFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        JSONArray tests = filterData.getJSONArray("Hotlist");
        for (int i = 0; i < tests.length(); i++) {
            JSONObject test = tests.getJSONObject(i);
            testData.add(new Object[]{
                    "Hotlist",
                    test.getString("filterType"),
                    test.getString("filterValue"),
                    test.getString("dbField"),
                    test.getString("expectedResult"),
                    test.getString("fieldType"),
                    test.getString("filterValue_TYPE")
            });
        }
        return testData.toArray(new Object[0][0]);
    }

    private void createEntityCFValueMap() {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            CompletableFuture<JsonPath> candidateJson1Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCandidateWithMandatoryFields(baseURL, apiKey).jsonPath(), executor);
            CompletableFuture<JsonPath> candidateJson2Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCandidateWithMandatoryFields(baseURL, apiKey).jsonPath(), executor);
            CompletableFuture<JsonPath> companyJson1Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCompanyWithMandatoryFields(baseURL, apiKey).jsonPath(), executor);
            CompletableFuture<JsonPath> companyJson2Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCompanyWithMandatoryFields(baseURL, apiKey).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson1Future = companyJson1Future.thenApplyAsync(c1 ->
                    function.createNewContact_POST(baseURL, apiKey, c1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson2Future = companyJson2Future.thenApplyAsync(c2 ->
                    function.createNewContact_POST(baseURL, apiKey, c2.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson1Future = companyJson1Future.thenCombineAsync(contactJson1Future, (c1, ct1) ->
                    function.createNewJob(baseURL, apiKey, c1.getString("slug"), ct1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson2Future = companyJson2Future.thenCombineAsync(contactJson2Future, (c2, ct2) ->
                    function.createNewJob(baseURL, apiKey, c2.getString("slug"), ct2.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> dealJson1Future = jobJson1Future.thenApplyAsync(j1 -> {
                String cs = companyJson1Future.join().getString("slug");
                String cts = contactJson1Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, apiKey, cs, cts, j1.getString("slug")).jsonPath();
            }, executor);
            CompletableFuture<JsonPath> dealJson2Future = jobJson2Future.thenApplyAsync(j2 -> {
                String cs = companyJson2Future.join().getString("slug");
                String cts = contactJson2Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, apiKey, cs, cts, j2.getString("slug")).jsonPath();
            }, executor);
            CompletableFuture<JsonPath> userJsonFuture = CompletableFuture.supplyAsync(() ->
                    function.getUsers(baseURL, apiKey).jsonPath(), executor);
            CompletableFuture<JsonPath> teamJsonFuture = userJsonFuture.thenApplyAsync((userJson) -> {
                ArrayList<String> team1UserId = new ArrayList<>();
                ArrayList<String> team2UserId = new ArrayList<>();
                team1UserId.add(String.valueOf(userJson.getInt("[1].id")));
                team1UserId.add(String.valueOf(userJson.getInt("[3].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[0].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[2].id")));
                Response t1 = allCrudFunctions.createTeam(albatrossURL, ownerAlbatrossAuthToken, "team1_cf", team1UserId);
                Response t2 = allCrudFunctions.createTeam(albatrossURL, ownerAlbatrossAuthToken, "team2_cf", team2UserId);
                t1.then().statusCode(200);
                t2.then().statusCode(200);
                return function.getTeams(baseURL, apiKey).jsonPath();
            }, executor);
            CompletableFuture.allOf(
                    candidateJson1Future, candidateJson2Future,
                    companyJson1Future, companyJson2Future,
                    contactJson1Future, contactJson2Future,
                    jobJson1Future, jobJson2Future,
                    dealJson1Future, dealJson2Future,
                    userJsonFuture, teamJsonFuture
            ).join();
            JsonPath userJson = userJsonFuture.join();
            JsonPath teamJson = teamJsonFuture.join();
            entityCFValueMap.put("candidate1", candidateJson1Future.join().getString("slug"));
            entityCFValueMap.put("candidate2", candidateJson2Future.join().getString("slug"));
            entityCFValueMap.put("company1", companyJson1Future.join().getString("slug"));
            entityCFValueMap.put("company2", companyJson2Future.join().getString("slug"));
            entityCFValueMap.put("contact1", contactJson1Future.join().getString("slug"));
            entityCFValueMap.put("contact2", contactJson2Future.join().getString("slug"));
            entityCFValueMap.put("job1", jobJson1Future.join().getString("slug"));
            entityCFValueMap.put("job2", jobJson2Future.join().getString("slug"));
            entityCFValueMap.put("deal1", dealJson1Future.join().getString("slug"));
            entityCFValueMap.put("deal2", dealJson2Future.join().getString("slug"));
            entityCFValueMap.put("owner", String.valueOf(userJson.getInt("[0].id")));
            entityCFValueMap.put("admin", String.valueOf(userJson.getInt("[1].id")));
            entityCFValueMap.put("restricted", String.valueOf(userJson.getInt("[2].id")));
            entityCFValueMap.put("teamMember", String.valueOf(userJson.getInt("[3].id")));
            entityCFValueMap.put("team1", findTeamIdByName(teamJson, "team1_cf"));
            entityCFValueMap.put("team2", findTeamIdByName(teamJson, "team2_cf"));
        } finally {
            executor.shutdown();
        }
    }

    private static String findTeamIdByName(JsonPath teamPath, String teamName) {
        int n = teamPath.getInt("$.size()");
        for (int i = 0; i < n; i++) {
            if (teamName.equals(teamPath.getString("[" + i + "].team_name"))) {
                return teamPath.getString("[" + i + "].team_id");
            }
        }
        throw new IllegalStateException("Team not found in getTeams response: " + teamName);
    }

    private Map<String, Integer> createCustomFields() {
        Map<String, Integer> ids = new HashMap<>();
        List<String> entityTypes = new ArrayList<>(List.of(
                "candidate", "company", "deal", "job", "contact", "user", "team",
                "text", "email", "phonenumber", "longtext", "number", "date", "social_profile"));
        ExecutorService executor = Executors.newFixedThreadPool(7);
        try {
            List<CompletableFuture<AbstractMap.SimpleEntry<String, Response>>> futures = entityTypes.stream()
                    .map(entity -> CompletableFuture.supplyAsync(() -> {
                        String name = entity + "CF";
                        Response response = function.createCustomFieldsResponse(
                                albatrossURL, ownerAlbatrossAuthToken, "candidate", name, entity, "");
                        return new AbstractMap.SimpleEntry<>(name, response);
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<AbstractMap.SimpleEntry<String, Response>> future : futures) {
                AbstractMap.SimpleEntry<String, Response> result = future.get();
                Assert.assertEquals(result.getValue().getStatusCode(), 200, "Failed to create custom field: " + result.getKey());
                ids.put(result.getKey(), result.getValue().jsonPath().getInt("data.custumField.columnid"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating custom fields", e);
        } finally {
            executor.shutdown();
        }
        return ids;
    }

    private JSONObject processPayloadPlaceholders(JSONObject payload) {
        JSONObject processedPayload = new JSONObject();
        for (String key : payload.keySet()) {
            Object value = payload.get(key);
            if (key.startsWith("{") && key.endsWith("}")) {
                String trimmedKey = key.substring(1, key.length() - 1);
                if (customFieldIds.containsKey(trimmedKey)) {
                    String newKey = "custcolumn" + customFieldIds.get(trimmedKey);
                    if (value instanceof String) {
                        processedPayload.put(newKey, processEntityPlaceholders((String) value));
                    } else {
                        processedPayload.put(newKey, value);
                    }
                } else {
                    processedPayload.put(key, value);
                }
            } else {
                processedPayload.put(key, value);
            }
        }
        return processedPayload;
    }

    private String processEntityPlaceholders(String value) {
        if (value == null) {
            return null;
        }
        if (value.startsWith("{") && value.endsWith("}")) {
            String innerValue = value.substring(1, value.length() - 1);
            String[] entityKeys = innerValue.split(",");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < entityKeys.length; i++) {
                String entityKey = entityKeys[i].trim();
                if (i > 0) {
                    result.append(",");
                }
                if (entityCFValueMap.containsKey(entityKey)) {
                    result.append(entityCFValueMap.get(entityKey));
                } else {
                    result.append(entityKey);
                }
            }
            return result.toString();
        }
        return value;
    }
}
