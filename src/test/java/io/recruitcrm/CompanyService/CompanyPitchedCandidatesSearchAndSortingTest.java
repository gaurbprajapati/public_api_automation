package io.recruitcrm.CompanyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.Candidate;
import io.rcrm.api.pojo.Contact;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CompanyPitchedCandidatesSearchAndSortingTest extends TestBase {

    commanFunction function = new commanFunction();
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();

    private int testCompanyId;
    private String companySlug;
    private String companyName;
    private String ownerName; // Store owner/createdByName for search tests
    Map<String, String> contactSlugs = new HashMap<>();
    Map<String, String> contactNames = new HashMap<>(); // Store contact names for search tests
    Map<String, String> candidateSlugs = new HashMap<>();
    Map<String, String> candidateNames = new HashMap<>();
    String albatrossAuthToken;
    String apiAuthToken;

    private volatile boolean testDataCreated = false;

    private synchronized void ensureTestData() {
        if (!testDataCreated) {
            albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
            apiAuthToken = ThreadManager.getAccountApiKey();
            createTestDataForSorting();
            testDataCreated = true;
        }
    }

    // ==================== SEARCH TEST ====================

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "companyPitchedCandidatesSearchTestData", groups = {"company_service", "nightly-build"})
    public void searchCompanyPitchedCandidates_ComprehensiveSearch(String testDescription, String searchTerm, String searchType, int expectedCount, int expectedStatus, String validationType, String expectedMessage, JSONArray fieldsToCheck) {
        ensureTestData();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "100");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", searchTerm);
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", String.valueOf(testCompanyId));

        Response response = RestClient.doPost1("JSON", companyServiceURL, "companies/{companyId}/pitched-candidates/get", albatrossAuthToken, queryParams, pathParams, true, requestBody.toString());

        assertThat(testDescription + " - Expected status code " + expectedStatus + " but got " + response.getStatusCode(), response.getStatusCode(), is(equalTo(expectedStatus)));

        JsonPath jp = response.jsonPath();

        // Verify meta message
        if (expectedMessage != null && !expectedMessage.isEmpty()) {
            response.then().body("meta.message", containsString(expectedMessage));
        }

        // Get actual count from response
        List<Map<String, Object>> data = jp.getList("data");
        assertThat(testDescription + " - Data array should not be null", data, is(notNullValue()));
        int actualCount = data.size();

        // Verify count matches expected count
        assertThat(testDescription + " - Expected count " + expectedCount + " but got " + actualCount, actualCount, is(equalTo(expectedCount)));

        // Validate search results based on validation type
        switch (validationType) {
            case "success_with_results":
                if (expectedCount > 0) {
                    assertThat(testDescription + " - Search results should not be empty", data.isEmpty(), is(false));

                    // Verify search term appears in specified fields
                    if (searchTerm != null && !searchTerm.trim().isEmpty() && fieldsToCheck != null && !fieldsToCheck.isEmpty()) {
                        boolean allResultsMatch = true;
                        for (Map<String, Object> result : data) {
                            boolean resultMatches = false;
                            for (int i = 0; i < fieldsToCheck.length(); i++) {
                                String field = fieldsToCheck.getString(i);
                                Object fieldValue = result.get(field);
                                if (fieldValue != null) {
                                    String fieldValueStr = String.valueOf(fieldValue).toLowerCase();
                                    if (fieldValueStr.contains(searchTerm.toLowerCase())) {
                                        resultMatches = true;
                                        break;
                                    }
                                }
                            }
                            if (!resultMatches) {
                                allResultsMatch = false;
                                break;
                            }
                        }
                        assertThat(testDescription + " - All results should contain search term '" + searchTerm + "' in fields " + fieldsToCheck, allResultsMatch, is(true));
                    }
                }
                break;
            case "success_empty_results":
                assertThat(testDescription + " - Data array should be empty when no candidates match", data.isEmpty(), is(true));
                break;
            case "success_with_message":
                // Count validation already done above
                break;
            default:
                break;
        }
    }

    // ==================== SORTING TEST ====================

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "companyPitchedCandidatesSortOrderTestData", groups = {"company_service", "nightly-build"})
    public void testGetCompanyPitchedCandidatesWithSortOrder(int companyId, String companyName, String sortOrderName, JSONArray sortPriorityList) {
        ensureTestData();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", sortPriorityList);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", String.valueOf(companyId));

        Response response = RestClient.doPost1("JSON", companyServiceURL, "companies/{companyId}/pitched-candidates/get",
                albatrossAuthToken, queryParams, pathParams, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode() + " for sortOrder: " + sortOrderName,
                response.getStatusCode(), equalTo(200));
        JsonPath jp = response.jsonPath();

        // Verify contacts array exists and has at least 2 candidates for sorting verification
        assertThat("Candidates array should not be null", jp.get("data"), notNullValue());
        assertThat("Candidates array should be a list", jp.get("data"), instanceOf(java.util.List.class));
        int candidatesSize = jp.get("data.size()");
        assertThat("Should have at least 2 candidates to verify sorting for " + sortOrderName, candidatesSize, greaterThanOrEqualTo(2));

        // Verify sorting is applied correctly
        verifyCandidateSortOrder(jp, sortPriorityList, candidatesSize, sortOrderName);
    }

    private void verifyCandidateSortOrder(JsonPath jp, JSONArray sortPriorityList, int candidatesSize, String sortOrderName) {
        if (sortPriorityList.isEmpty()) {
            return;
        }

        JSONObject firstSort = sortPriorityList.getJSONObject(0);
        String field = firstSort.getString("field");
        String order = firstSort.getString("order");

        // Verify sorting based on the first sort field
        for (int i = 0; i < candidatesSize - 1; i++) {
            Object currentValueObj = jp.get("data[" + i + "]." + field);
            Object nextValueObj = jp.get("data[" + (i + 1) + "]." + field);

            if (currentValueObj != null && nextValueObj != null) {
                // Handle different data types
                if (currentValueObj instanceof Number && nextValueObj instanceof Number) {
                    // Numeric comparison (for createdOn, etc.)
                    double currentNum = ((Number) currentValueObj).doubleValue();
                    double nextNum = ((Number) nextValueObj).doubleValue();

                    if (order.equalsIgnoreCase("asc")) {
                        assertThat("Sort order should be ascending for numeric field: " + field + " in " + sortOrderName,
                                currentNum, lessThanOrEqualTo(nextNum));
                    } else if (order.equalsIgnoreCase("desc")) {
                        assertThat("Sort order should be descending for numeric field: " + field + " in " + sortOrderName,
                                currentNum, greaterThanOrEqualTo(nextNum));
                    }
                } else {
                    // String comparison (for name, email, phone, pitchStage, contactName, createdByName, etc.)
                    String currentValue = String.valueOf(currentValueObj);
                    String nextValue = String.valueOf(nextValueObj);

                    if (order.equalsIgnoreCase("asc")) {
                        assertThat("Sort order should be ascending for field: " + field + " in " + sortOrderName,
                                currentValue.compareToIgnoreCase(nextValue), lessThanOrEqualTo(0));
                    } else if (order.equalsIgnoreCase("desc")) {
                        assertThat("Sort order should be descending for field: " + field + " in " + sortOrderName,
                                currentValue.compareToIgnoreCase(nextValue), greaterThanOrEqualTo(0));
                    }
                }
            }
        }
    }

    private void pitchCandidateToContact(String candidateSlug, String contactSlug) {
        String pitchPath = "pitch/{candidate}/contact/{contact}";
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("candidate", candidateSlug);
        pathParameters.put("contact", contactSlug);

        Response response = RestClient.doPost1("JSON", baseURL, pitchPath,
                apiAuthToken, null, pathParameters, true, null);

        assertThat("Failed to pitch candidate " + candidateSlug + " to contact " + contactSlug +
                        " - Expected status code 200 but got " + response.getStatusCode() +
                        ". Response: " + response.getBody().asString(),
                response.getStatusCode(), is(equalTo(200)));
    }

    private JSONObject processCandidatePayload(JSONObject candidatePayload) {
        // Create a copy to avoid modifying the original
        JSONObject processedPayload = new JSONObject(candidatePayload.toString());

        Map<String, PlaceholderMapping> mappings = new HashMap<>();
        mappings.put("contactSlug", new PlaceholderMapping("{contactSlug", contactSlugs));

        for (Map.Entry<String, PlaceholderMapping> entry : mappings.entrySet()) {
            String key = entry.getKey();
            if (processedPayload.has(key)) {
                String value = processedPayload.getString(key);
                PlaceholderMapping mapping = entry.getValue();

                if (value != null && value.startsWith(mapping.prefix) && value.endsWith("}")) {
                    String mapKey = value.substring(1, value.length() - 1); // remove { and }
                    Object resolvedValue = mapping.lookupMap.get(mapKey);
                    if (resolvedValue != null) {
                        processedPayload.put(key, resolvedValue);
                    } else {
                        throw new RuntimeException("Failed to resolve placeholder " + value +
                                ". Available keys in contactSlugs: " + mapping.lookupMap.keySet());
                    }
                }
            }
        }

        return processedPayload;
    }

    private static class PlaceholderMapping {
        String prefix;
        Map<String, ?> lookupMap;

        PlaceholderMapping(String prefix, Map<String, ?> lookupMap) {
            this.prefix = prefix;
            this.lookupMap = lookupMap;
        }
    }

    public void createTestDataForSorting() {
        // Step 1: Create company using public API
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), is(equalTo(200)));

        JsonPath companyJp = companyResponse.jsonPath();
        companySlug = companyJp.get("slug");
        companyName = companyJp.get("company_name");

        assertThat("Company slug should not be null", companySlug, notNullValue());
        assertThat("Company name should not be null", companyName, notNullValue());

        // Get company ID from albatross API using slug (response structure: data.company.id)
        Response companyDetailsResponse = albatrossFunctions.getCompanyResponse(albatrossURL, albatrossAuthToken, companySlug);
        assertThat("Failed to get company details from albatross API", companyDetailsResponse.getStatusCode(), is(equalTo(200)));

        JsonPath companyDetailsJp = companyDetailsResponse.jsonPath();
        testCompanyId = companyDetailsJp.get("data.company.id");

        assertThat("Company ID should not be null", testCompanyId, notNullValue());
        assertThat("Company ID should be greater than 1", testCompanyId, greaterThan(1));

        // Step 2: Create contacts with specific names (need at least 3 for the JSON file references)
        String[] contactFirstNames = {"John", "Sarah", "Michael"};
        String[] contactLastNames = {"Johnson", "Johnson", "Williams"};

        for (int i = 0; i < 3; i++) {
            String firstName = contactFirstNames[i];
            String lastName = contactLastNames[i];
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@testcompany.com";
            String contactNumber = "+123456789" + i; // Unique phone number for each contact

            Contact contact = new Contact(firstName, lastName, email, contactNumber, companySlug);
            // Create auth token map manually (getAuthTokenMap is private)
            Map<String, String> authTokenMap = new HashMap<>();
            authTokenMap.put("Authorization", "Bearer " + apiAuthToken);

            Response contactResponse = RestClient.doPost("JSON", baseURL, "contacts", authTokenMap, null, true, contact);
            assertThat("Failed to create test contact " + (i + 1), contactResponse.getStatusCode(), is(equalTo(200)));

            JsonPath contactJp = contactResponse.jsonPath();
            String contactSlug = contactJp.get("slug");
            String contactName = firstName + " " + lastName;

            contactSlugs.put("contactSlug" + (i + 1), contactSlug);
            contactNames.put("contactName" + (i + 1), contactName);
        }

        // Step 3: Read JSON file and create candidates, then pitch them
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/companyPitchedCandidatesDataProvider.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(candidateJson.keySet().stream()
                    .filter(key -> key.startsWith("candidate"))
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        JSONObject originalPayload = candidateJson.getJSONObject(candidateKey);
                        JSONObject payload = processCandidatePayload(new JSONObject(originalPayload.toString()));

                        // Create candidate from JSON data using public API (like jobs are created from JSON)
                        Candidate candidate = new Candidate();
                        candidate.setFirst_name(payload.getString("firstName"));
                        candidate.setLast_name(payload.getString("lastName"));
                        candidate.setEmail(payload.getString("email"));
                        candidate.setContact_number(payload.getString("phone"));
                        candidate.setGenderId(1);

                        // Create candidate using public API
                        Response candidateResponse = RestClient.doPost("JSON", baseURL, "candidates",
                                apiAuthToken, null, true, candidate);
                        assertThat("createTestDataForSorting - Candidate creation failed for " + candidateKey + " - Expected status code 200",
                                candidateResponse.getStatusCode(), is(equalTo(200)));
                        JsonPath candidateJp = candidateResponse.jsonPath();
                        String candidateSlug = candidateJp.get("slug");
                        String candidateName = candidateJp.get("first_name") + " " + candidateJp.get("last_name");
                        candidateSlugs.put(candidateKey, candidateSlug);
                        candidateNames.put(candidateKey, candidateName);

                        // Pitch candidate to contact (required for sort tests)
                        // Verify contactSlug was properly resolved from placeholder
                        String contactSlug = payload.getString("contactSlug");
                        if (contactSlug == null || contactSlug.isEmpty()) {
                            throw new RuntimeException("Contact slug is missing for candidate " + candidateKey);
                        }
                        if (contactSlug.startsWith("{") && contactSlug.endsWith("}")) {
                            throw new RuntimeException("Contact slug was not resolved for candidate " + candidateKey +
                                    ". Still contains placeholder: " + contactSlug + ". Available contact slugs: " + contactSlugs);
                        }
                        pitchCandidateToContact(candidateSlug, contactSlug);

                    }, executor)).toArray(CompletableFuture[]::new)).join();
        } finally {
            executor.shutdown();
        }

        // Step 4: Get owner name from the first pitched candidate response
        // This will be used to replace {owner} placeholder in search test data
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "1");
        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", String.valueOf(testCompanyId));
        Response ownerResponse = RestClient.doPost1("JSON", companyServiceURL,
                "companies/{companyId}/pitched-candidates/get",
                albatrossAuthToken,
                queryParams, pathParams, true, requestBody.toString());
        if (ownerResponse.getStatusCode() == 200) {
            JsonPath ownerJp = ownerResponse.jsonPath();
            List<Map<String, Object>> ownerData = ownerJp.getList("data");
            if (ownerData != null && !ownerData.isEmpty()) {
                ownerName = (String) ownerData.get(0).get("createdByName");
                if (ownerName != null) {
                    ownerName = ownerName.trim();
                }
            }
        }
    }

    @DataProvider(name = "companyPitchedCandidatesSortOrderTestData")
    public Object[][] getCompanyPitchedCandidatesSortOrderTestData() {
        if (testCompanyId == 0) {
            throw new IllegalStateException("testCompanyId is 0. Test data was not created properly in @BeforeClass");
        }

        JSONObject sortOrderJson = readJsonFileFromPath("src/test/resources/privateApi/company/companyPitchedCandidatesSortOrder.json");
        JSONArray configurations = sortOrderJson.getJSONArray("sortOrderConfigurations");

        return java.util.stream.IntStream.range(0, configurations.length()).mapToObj(i -> {
            JSONObject config = configurations.getJSONObject(i);
            return new Object[]{testCompanyId, companyName, config.getString("name"), config.getJSONArray("sortPriorityList")};
        }).toArray(Object[][]::new);
    }

    // ==================== DATA PROVIDERS FOR SEARCH ====================

    @DataProvider(name = "companyPitchedCandidatesSearchTestData")
    public Object[][] getCompanyPitchedCandidatesSearchTestData() {
        JSONObject searchJson = readJsonFileFromPath("src/test/resources/privateApi/company/companyPitchedCandidatesSearchTestData.json");
        JSONArray testCases = searchJson.getJSONArray("searchTestCases");

        return java.util.stream.IntStream.range(0, testCases.length()).mapToObj(i -> {
            JSONObject testCase = testCases.getJSONObject(i);
            JSONArray fieldsToCheck = testCase.has("fieldsToCheck") ? testCase.getJSONArray("fieldsToCheck") : new JSONArray();

            // Replace {owner} and {contactName1/2/3} placeholders with actual values
            String searchTerm = testCase.getString("searchTerm");
            if (searchTerm != null) {
                if (searchTerm.contains("{owner}") && ownerName != null) {
                    searchTerm = searchTerm.replace("{owner}", ownerName);
                }
                // Replace contact name placeholders
                for (int j = 1; j <= 3; j++) {
                    String placeholder = "{contactName" + j + "}";
                    if (searchTerm.contains(placeholder) && contactNames.containsKey("contactName" + j)) {
                        searchTerm = searchTerm.replace(placeholder, contactNames.get("contactName" + j));
                    }
                }
            }

            return new Object[]{testCase.getString("testDescription"), searchTerm, testCase.getString("searchType"), testCase.getInt("expectedCount"), testCase.getInt("expectedStatus"), testCase.getString("validationType"), testCase.getString("expectedMessage"), fieldsToCheck};
        }).toArray(Object[][]::new);
    }
}

