package io.recruitcrm.ContactService.Filters;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class CompanyOffLimitStatusCrossEntityContactFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String email;
    Map<String, String> companyKeyToIdMap = new HashMap<>();
    Map<String, String> companyIdToKeyMap = new HashMap<>();
    Map<String, String> contactKeyToSlugMap = new HashMap<>();
    Map<String, String> contactIdToKeyMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();
    Map<String, Integer> offLimitStatusMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        email = ThreadManager.getAccount().getOwner().getEmail();
        offLimitStatusMap = allCrudFunctions.getOffLimitStatusMap(albatrossURL, ownerAlbatrossAuthToken);
        Assert.assertFalse(offLimitStatusMap.isEmpty(),
                "Off-limit status map should not be empty. Available statuses: " + offLimitStatusMap.keySet());
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "companyOffLimitStatusCrossEntityFilterSearchTestData", description = "Filter Search Test for Company Off Limit Status Cross Entity Contact")
    public void companyOffLimitStatusCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "contacts");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "firstname");
        logContactIds(data);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 10, "Contact");
    }

    @DataProvider(name = "companyOffLimitStatusCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] companyOffLimitStatusCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactCompanyOffLimitStatusFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if (key.equals("Off Limit Status")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/contactCompany_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, Integer> companyIdMap = new HashMap<>();

        try {
            List<CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>>> createFutures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.supplyAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");

                        String createdBy = companyEntry.has("createdBy") ? companyEntry.getString("createdBy") : "owner";
                        String tokenForCreation = getAlbatrossAuthToken(createdBy);
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, tokenForCreation, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String slug = jsonPath.getString("data.company.slug");
                        Integer companyId = jsonPath.getInt("data.company.id");
                        return Map.entry(companyKey, Map.entry(slug, companyId));
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>> future : createFutures) {
                Map.Entry<String, Map.Entry<String, Integer>> entry = future.join();
                Integer companyId = entry.getValue().getValue();
                companyIdMap.put(entry.getKey(), companyId);
                // Store mappings for validation
                companyKeyToIdMap.put(entry.getKey().toLowerCase(), String.valueOf(companyId));
                companyIdToKeyMap.put(String.valueOf(companyId), entry.getKey().toLowerCase());
            }

            // Mark companies as off-limit with appropriate statuses
            addCompaniesToOffLimitStatus(companyJson, companyIdMap);
            
            // Link contacts to companies
            addContactsToCompanies(companyJson, companyIdMap);
            
            // Log company-to-contact and status mappings for debugging
            logCompanyContactStatusMappings(companyJson, companyIdMap);
        } finally {
            executor.shutdown();
        }
    }

    private void addCompaniesToOffLimitStatus(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        Map<String, List<Integer>> offLimitStatusCompanyIdsMap = new HashMap<>();

        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            if (!companyEntry.has("offLimitStatus") || companyEntry.isNull("offLimitStatus")) {
                continue;
            }
            
            String companyOffLimitStatus = companyEntry.getString("offLimitStatus");
            if (companyOffLimitStatus == null || companyOffLimitStatus.isEmpty()) {
                continue;
            }
            
            Integer companyId = companyIdMap.get(companyKey);
            String[] offLimitStatusNames = companyOffLimitStatus.split(",");
            for (String offLimitStatusName : offLimitStatusNames) {
                offLimitStatusName = offLimitStatusName.trim();
                if (!offLimitStatusName.isEmpty()) {
                    offLimitStatusCompanyIdsMap.computeIfAbsent(offLimitStatusName, k -> new ArrayList<>()).add(companyId);
                }
            }
        }

        // Bulk mark companies as off limit
        long nowSeconds = System.currentTimeMillis() / 1000L;
        long startDate = nowSeconds;
        long endDate = nowSeconds + 30L * 24 * 60 * 60; // +30 days
        
        String basePath = "off-limit/mark-off-limit";
        
        for (Map.Entry<String, List<Integer>> entry : offLimitStatusCompanyIdsMap.entrySet()) {
            String statusLabel = entry.getKey();
            List<Integer> companyIds = entry.getValue();
            
            if (companyIds.isEmpty()) {
                continue;
            }
            
            // Get status ID from the map
            Integer statusId = offLimitStatusMap.get(statusLabel);
            if (statusId == null) {
                Assert.fail("Off-limit status ID not found for status label: " + statusLabel + ". Available statuses: " + offLimitStatusMap.keySet());
                continue;
            }

            JSONObject payload = new JSONObject();
            payload.put("entity_type_id", 3); // Entity type ID for companies
            payload.put("entity_ids", new JSONArray(companyIds));
            payload.put("status_id", statusId);
            payload.put("start_date", startDate);
            payload.put("end_date", endDate);
            payload.put("reason", "");
            
            Response response = RestClient.doPost("JSON", albatrossURL, basePath, ownerAlbatrossAuthToken, null, true, payload);
            System.out.println("Marking companies as off-limit with status " + statusLabel + ": " + response.prettyPrint());
            Assert.assertEquals(response.getStatusCode(), 200, "Failed to mark companies off-limit with status: " + statusLabel);
        }
    }

    private void addContactsToCompanies(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        //Creating 10 contacts
        for (int i = 1; i <= 10; i++) {
            Response response = allCrudFunctions.createContact(albatrossURL, ownerAlbatrossAuthToken);
            String contactSlug = response.jsonPath().getString("data.contact.slug");
            Integer contactId = response.jsonPath().getInt("data.contact.id");
            String contactKey = "contact" + i;
            contactKeyToSlugMap.put(contactKey, contactSlug);
            contactIdToKeyMap.put(String.valueOf(contactId), contactKey);
            contactKeyToIdMap.put(contactKey, String.valueOf(contactId));
        }

        //Link contacts to companies
        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            String contact = companyEntry.optString("contact", "").trim();
            if (contact.isEmpty()) {
                continue;
            }
            String contactSlugs = Arrays.stream(contact.split(","))
                    .map(String::trim)
                    .map(contactKeyToSlugMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));

            if (contactSlugs.isEmpty()) {
                continue;
            }

            Integer companyId = companyIdMap.get(companyKey);
            UpdateFields updateFields = new UpdateFields();
            updateFields.setKey("contactid");
            updateFields.setValue(contactSlugs);
            updateFields.setTableFlag("company");
            updateFields.setId(Collections.singletonList(companyId));
            updateFields.setAddInValues(true);
            Response linkResponse = RestClient.doPost("JSON", albatrossURL, "global/update-fields", ownerAlbatrossAuthToken, null, true, updateFields);
            linkResponse.then().statusCode(200);
        }
    }

    public String getAlbatrossAuthToken(String createdBy) {
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

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("offLimitBehavior", "bypass");
        payload.put("sortPriorityList", new JSONArray());
        
        // Process filterValue to convert status label placeholders to status IDs
        String processedFilterValue = processFilterValue(filterValue);
        
        // Create filterValue object with type and value array
        JSONObject filterValueObj = new JSONObject();
        if (filterValue_TYPE.equals("INTEGER_LIST")) {
            filterValueObj = integerListFilterValue(processedFilterValue);
        } else {
            filterValueObj = emptyFilterValue(filterValue_TYPE);
        }
        
        // Create filterSearchList structure
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

    private String processFilterValue(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }

        String processedValue = filterValue;
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(filterValue);

        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String statusLabel = matcher.group(1);
            
            // Get status ID from the map
            Integer statusId = offLimitStatusMap.get(statusLabel);
            if (statusId != null) {
                processedValue = processedValue.replace(placeholder, String.valueOf(statusId));
            } else {
                throw new IllegalArgumentException("Off-limit status ID not found for status label: " + statusLabel + ". Available statuses: " + offLimitStatusMap.keySet());
            }
        }

        return processedValue;
    }

    private void logContactIds(JSONArray data) {
        if (data == null || data.length() == 0) {
            FilterSearchReporter.logInfo("<b>📋 Contact IDs:</b> No contacts returned");
            return;
        }

        StringBuilder contactIdsLog = new StringBuilder();
        contactIdsLog.append("<b>📋 Contact IDs from Returned Records:</b><br/>");
        contactIdsLog.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>");
        contactIdsLog.append("<code>");

        for (int i = 0; i < data.length(); i++) {
            JSONObject contact = data.getJSONObject(i);
            Integer contactId = contact.optInt("id", -1);
            String firstName = contact.optString("firstname", "");
            String lastName = contact.optString("lastname", "");
            String contactName = (firstName + " " + lastName).trim();
            if (contactName.isEmpty()) {
                contactName = contact.optString("name", "Unknown");
            }
            String contactKey = contactIdToKeyMap.getOrDefault(String.valueOf(contactId), "Unknown");
            
            contactIdsLog.append("Record ").append(i + 1).append(": Contact ID: ").append(contactId)
                         .append(" | Key: ").append(contactKey)
                         .append(" | Name: ").append(contactName).append("\n");
        }

        contactIdsLog.append("</code></pre>");
        FilterSearchReporter.logInfo(contactIdsLog.toString());
        
        // Log all expected contacts for comparison
        logExpectedVsActual(data);
    }
    
    private void logExpectedVsActual(JSONArray actualData) {
        StringBuilder comparisonLog = new StringBuilder();
        comparisonLog.append("<b>🔍 Expected vs Actual Contacts Comparison:</b><br/>");
        comparisonLog.append("<pre style='background-color: #fff3cd; padding: 10px; border-radius: 5px;'>");
        comparisonLog.append("<code>");
        
        // Get actual contact keys
        Set<String> actualContactKeys = new HashSet<>();
        for (int i = 0; i < actualData.length(); i++) {
            JSONObject contact = actualData.getJSONObject(i);
            Integer contactId = contact.optInt("id", -1);
            String contactKey = contactIdToKeyMap.getOrDefault(String.valueOf(contactId), "Unknown");
            actualContactKeys.add(contactKey.toLowerCase());
        }
        
        comparisonLog.append("Actual contacts returned: ").append(actualContactKeys.size()).append("\n");
        for (String key : actualContactKeys) {
            comparisonLog.append("  ✓ ").append(key).append("\n");
        }
        
        comparisonLog.append("\nAll created contacts:\n");
        for (String contactKey : contactKeyToIdMap.keySet()) {
            String status = actualContactKeys.contains(contactKey.toLowerCase()) ? "✓ RETURNED" : "✗ MISSING";
            comparisonLog.append("  ").append(status).append(": ").append(contactKey).append("\n");
        }
        
        comparisonLog.append("</code></pre>");
        FilterSearchReporter.logInfo(comparisonLog.toString());
    }
    
    private void logCompanyContactStatusMappings(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        StringBuilder mappingLog = new StringBuilder();
        mappingLog.append("<b>🏢 Company → Contact → Status Mappings:</b><br/>");
        mappingLog.append("<pre style='background-color: #e7f3ff; padding: 10px; border-radius: 5px;'>");
        mappingLog.append("<code>");
        
        Map<String, List<String>> contactToCompanies = new HashMap<>();
        
        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            String contact = companyEntry.optString("contact", "").trim();
            String offLimitStatus = companyEntry.optString("offLimitStatus", "(no status)");
            
            if (!contact.isEmpty()) {
                String[] contacts = contact.split(",");
                for (String contactKey : contacts) {
                    contactKey = contactKey.trim();
                    contactToCompanies.computeIfAbsent(contactKey, k -> new ArrayList<>()).add(companyKey + " [" + offLimitStatus + "]");
                }
            }
        }
        
        mappingLog.append("Companies with 'Contractual Off-Limits' status:\n");
        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            String offLimitStatus = companyEntry.optString("offLimitStatus", "");
            if ("Contractual Off-Limits".equals(offLimitStatus)) {
                String contact = companyEntry.optString("contact", "");
                mappingLog.append("  ").append(companyKey).append(" → ").append(contact).append("\n");
            }
        }
        
        mappingLog.append("\nContact → Companies mapping:\n");
        for (String contactKey : new TreeSet<>(contactToCompanies.keySet())) {
            List<String> companies = contactToCompanies.get(contactKey);
            mappingLog.append("  ").append(contactKey).append(" → ").append(String.join(", ", companies)).append("\n");
        }
        
        mappingLog.append("</code></pre>");
        FilterSearchReporter.logInfo(mappingLog.toString());
    }
}
