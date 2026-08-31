package io.recruitcrm.JobService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import com.qa.api.util.reaper.ReaperIntegration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class JobRelatedHotlistsSearchAndSortTest extends TestBase {

    commanFunction function = new commanFunction();
    String apiAuthToken;
    String albatrossTkn;
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    Map<String, Map<String, String>> hotlistTimestampScenarios;
    int jobRecordId;
    String jobSlug;
    String companySlug;
    String contactSlug;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        hotlistTimestampScenarios = function.createTimestampScenarios();
        createTestData();
    }

    private void createTestData() {
        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
        companySlug = companyJson.get("slug");

        JsonPath contactJson = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
        contactSlug = contactJson.get("slug");

        Response jobResponse = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug);
        assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        jobSlug = jobJp.get("slug");

        Response jobGetResponse = albatrossFunctions.getJobResponse(albatrossURL, albatrossTkn, jobSlug);
        assertThat("Failed to get test job", jobGetResponse.getStatusCode(), equalTo(200));
        JsonPath jp = jobGetResponse.jsonPath();
        Object idObj = jp.get("data.job.id");
        jobRecordId = idObj instanceof Number ? ((Number) idObj).intValue() : Integer.parseInt(String.valueOf(idObj));

        assertThat("Record ID should not be null", jobRecordId, notNullValue());

        Response usersResponse = albatrossFunctions.getUsers(albatrossURL, albatrossTkn);
        assertThat("Failed to get users", usersResponse.getStatusCode(), equalTo(200));
        JsonPath usersJp = usersResponse.jsonPath();
        Map<String, Integer> userIdMap = new HashMap<>();
        userIdMap.put("{userId1}", usersJp.get("data.records[0].id"));
        userIdMap.put("{userId2}", usersJp.get("data.records[1].id"));
        userIdMap.put("{userId3}", usersJp.get("data.records[2].id"));
        userIdMap.put("{userId4}", usersJp.get("data.records[3].id"));

        JSONObject hotlistData = readJsonFileFromPath("src/test/resources/privateApi/common/relatedHotlistsData.json");
        JSONArray hotlists = hotlistData.getJSONArray("hotlists");
        String entityType = "job";

        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            CompletableFuture.allOf(
                    IntStream.range(0, hotlists.length())
                            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                                JSONObject hotlist = hotlists.getJSONObject(i);
                                String hotlistName = hotlist.getString("name");
                                int shared = hotlist.getInt("shared");
                                String createdByStr = hotlist.getString("created_by");
                                String createdOn = hotlist.getString("createdon");

                                int createdBy = 0;
                                if (userIdMap.containsKey(createdByStr)) {
                                    createdBy = userIdMap.get(createdByStr);
                                }

                                JSONObject hotlistJson = new JSONObject();
                                hotlistJson.put("name", hotlistName);
                                hotlistJson.put("related_to_type", entityType);
                                hotlistJson.put("shared", shared);
                                hotlistJson.put("created_by", createdBy);

                                Response hotlistResponse = RestClient.doPost("JSON", baseURL, "hotlists", apiAuthToken, null, true, hotlistJson.toString());
                                assertThat("Failed to create hotlist: " + hotlistName, hotlistResponse.getStatusCode(), equalTo(200));

                                JsonPath hotlistJp = hotlistResponse.jsonPath();
                                int hotlistId = hotlistJp.getInt("id");

                                if (!createdOn.isEmpty() && hotlistTimestampScenarios.containsKey(createdOn)) {
                                    updateHotlistTimestamps(hotlistId, createdOn);
                                }
                                HotlistRelated hotlistRelated = new HotlistRelated();
                                hotlistRelated.setRelated(jobSlug);
                                Map<String, String> pathParameters = new HashMap<>();
                                pathParameters.put("hotlist", String.valueOf(hotlistId));
                                String basePath = "hotlists/{hotlist}/add-record";
                                Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, apiAuthToken, null, pathParameters, true, hotlistRelated);
                                assertThat("Failed to add job to hotlist: " + hotlistName, addResponse.getStatusCode(), equalTo(200));
                            }, executor))
                            .toArray(CompletableFuture[]::new)
            ).join();
        } finally {
            executor.shutdown();
        }
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSortOrderTestData", groups = {"job_service", "nightly-build"})
    public void testJobRelatedHotlistsWithSortOrder(String sortField, String jsonPath, String sortOrder, int statusCode) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONArray sortOrderArray = new JSONArray();
        JSONObject sortItem = new JSONObject();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortOrderArray.put(sortItem);

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "jobs");
        requestBody.put("recordId", jobRecordId);
        requestBody.put("sortOrder", sortOrderArray);

        Response response = RestClient.doPost1("JSON", jobServiceURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, queryParams, null, true, requestBody.toString());

        String testCase = "testJobRelatedHotlistsWithSortOrder - Field: " + sortField + ", Order: " + sortOrder;
        assertThat(testCase + " - Expected status code " + statusCode,
                response.getStatusCode(), equalTo(statusCode));

        JsonPath jp = response.jsonPath();

        assertThat("Hotlists array should not be null", jp.get("data"), notNullValue());
        int hotlistsSize = jp.get("data.size()");
        assertThat("Should have at least 2 hotlists to verify sorting", hotlistsSize, greaterThanOrEqualTo(2));

        List<Object> values = jp.getList(jsonPath);

        if (sortField.equals("name")) {
            if (sortOrder.equals("asc")) {
                assertThat(testCase + " - " + sortField + " not sorted ascending", isSortedAscendingText(values), is(true));
            } else {
                assertThat(testCase + " - " + sortField + " not sorted descending", isSortedDescendingText(values), is(true));
            }
        } else {
            if (sortOrder.equals("asc")) {
                assertThat(testCase + " - " + sortField + " not sorted ascending", isSortedAscendingNumeric(values), is(true));
            } else {
                assertThat(testCase + " - " + sortField + " not sorted descending", isSortedDescendingNumeric(values), is(true));
            }
        }
    }

    @Owner("Harika")
    @Test(dataProvider = "hotlistSearchTestData", groups = {"job_service", "nightly-build"})
    public void testJobRelatedHotlistsSearch(String searchTerm, String searchType) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "25");
        queryParams.put("page", "1");

        JSONObject requestBody = new JSONObject();
        requestBody.put("entityName", "jobs");
        requestBody.put("recordId", jobRecordId);
        requestBody.put("searchTerm", searchTerm);

        Response response = RestClient.doPost1("JSON", jobServiceURL, "hotlists/related-hotlists/search/get",
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode() + " for searchType: " + searchType,
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Related hotlists fetched successfully."));

        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Hotlists array should not be null", jp.get("data"), instanceOf(java.util.List.class));

        int hotlistsSize = jp.get("data.size()");

        switch (searchType) {
            case "exact_match":
                assertThat("Exact match search should return at least one hotlist", hotlistsSize, greaterThanOrEqualTo(1));
                boolean foundMatch = false;
                for (int i = 0; i < hotlistsSize; i++) {
                    String hotlistName = jp.get("data[" + i + "].name");
                    if (hotlistName != null && hotlistName.equalsIgnoreCase(searchTerm)) {
                        foundMatch = true;
                        break;
                    }
                }
                assertThat("Search results should contain the exact search term", foundMatch, equalTo(true));
                break;

            case "partial_match":
                assertThat("Partial match search should return at least one hotlist", hotlistsSize, greaterThanOrEqualTo(1));
                boolean foundPartialMatch = false;
                for (int i = 0; i < hotlistsSize; i++) {
                    String hotlistName = jp.get("data[" + i + "].name");
                    if (hotlistName != null && hotlistName.toLowerCase().contains(searchTerm.toLowerCase())) {
                        foundPartialMatch = true;
                        break;
                    }
                }
                assertThat("Search results should contain the partial search term", foundPartialMatch, equalTo(true));
                break;

            case "case_insensitive":
                assertThat("Case insensitive search should return at least one hotlist", hotlistsSize, greaterThanOrEqualTo(1));
                boolean foundCaseInsensitive = false;
                String lowerSearchTerm = searchTerm.toLowerCase();
                for (int i = 0; i < hotlistsSize; i++) {
                    String hotlistName = jp.get("data[" + i + "].name");
                    if (hotlistName != null && hotlistName.toLowerCase().contains(lowerSearchTerm)) {
                        foundCaseInsensitive = true;
                        break;
                    }
                }
                assertThat("Case insensitive search should return matching results", foundCaseInsensitive, equalTo(true));
                break;

            case "no_results":
                assertThat("No results search should return empty hotlists array", hotlistsSize, equalTo(0));
                assertThat("Hotlists array should be empty for non-existent search term",
                        jp.get("data"), instanceOf(java.util.List.class));
                break;

            default:
                assertThat("Hotlists array should not be null for search type: " + searchType,
                        jp.get("data"), notNullValue());
                assertThat("Hotlists array should be a list for search type: " + searchType,
                        jp.get("data"), instanceOf(java.util.List.class));
                assertThat("Hotlists size should be non-negative for search type: " + searchType,
                        hotlistsSize, greaterThanOrEqualTo(0));
                break;
        }
    }

    private boolean isSortedAscendingText(List<Object> list) {
        if (list == null || list.size() <= 1) return true;
        List<String> stringList = new ArrayList<>();
        for (Object obj : list) {
            stringList.add(String.valueOf(obj));
        }
        List<String> sortedList = new ArrayList<>(stringList);
        Collections.sort(sortedList, String.CASE_INSENSITIVE_ORDER);
        return stringList.equals(sortedList);
    }

    private boolean isSortedDescendingText(List<Object> list) {
        if (list == null || list.size() <= 1) return true;
        List<String> stringList = new ArrayList<>();
        for (Object obj : list) {
            stringList.add(String.valueOf(obj));
        }
        List<String> sortedList = new ArrayList<>(stringList);
        Collections.sort(sortedList, String.CASE_INSENSITIVE_ORDER);
        Collections.reverse(sortedList);
        return stringList.equals(sortedList);
    }

    private boolean isSortedAscendingNumeric(List<Object> list) {
        if (list == null || list.size() <= 1) return true;
        for (int i = 0; i < list.size() - 1; i++) {
            Object current = list.get(i);
            Object next = list.get(i + 1);
            if (current instanceof Number && next instanceof Number) {
                double currentNum = ((Number) current).doubleValue();
                double nextNum = ((Number) next).doubleValue();
                if (currentNum > nextNum) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSortedDescendingNumeric(List<Object> list) {
        if (list == null || list.size() <= 1) return true;
        for (int i = 0; i < list.size() - 1; i++) {
            Object current = list.get(i);
            Object next = list.get(i + 1);
            if (current instanceof Number && next instanceof Number) {
                double currentNum = ((Number) current).doubleValue();
                double nextNum = ((Number) next).doubleValue();
                if (currentNum < nextNum) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateHotlistTimestamps(int hotlistId, String createdOn) {
        Map<String, String> timestamps = hotlistTimestampScenarios.get(createdOn);
        String createdOnTimestamp = timestamps.get("createdOn");
        String updatedOnTimestamp = timestamps.get("updatedOn");
        Response updateResponse = ReaperIntegration.updateHotlistTimestamp(hotlistId, createdOnTimestamp, updatedOnTimestamp);
        if (updateResponse.getStatusCode() != 200) {
            Assert.fail("Failed to update the hotlist timestamps for hotlist: " + hotlistId);
        }
    }

    @DataProvider(name = "hotlistSortOrderTestData", parallel = true)
    public static Object[][] getHotlistSortOrderTestData() {
        return new Object[][]{
                {"name", "data.name", "asc", 200},
                {"name", "data.name", "desc", 200},
                {"createdOn", "data.createdOn", "asc", 200},
                {"updatedOn", "data.updatedOn", "asc", 200},
                {"ownerId", "data.ownerName", "asc", 200},
                {"createdOn", "data.createdOn", "desc", 200},
                {"updatedOn", "data.updatedOn", "desc", 200},
                {"ownerId", "data.ownerName", "desc", 200}
        };
    }

    @DataProvider(name = "hotlistSearchTestData", parallel = true)
    public Object[][] getHotlistSearchTestData() {
        return new Object[][] {
                {"PHP Developer #1", "exact_match"},
                {"Python Developer - Senior", "exact_match"},
                {"PHP", "partial_match"},
                {"Vue.js", "partial_match"},
                {"DEVELOPER", "case_insensitive"},
                {"developer", "case_insensitive"},
                {"#1", "partial_match"},
                {"#", "partial_match"},
                {"& Cloud", "partial_match"},
                {"/Expert", "partial_match"},
                {"/", "partial_match"},
                {"(Front-End)", "partial_match"},
                {"1", "partial_match"},
                {"123", "no_results"},
                {"NonExistentHotlistName12345", "no_results"},
                {"XYZ123ABC", "no_results"},
                {"@#$%^&*()", "no_results"}
        };
    }
}