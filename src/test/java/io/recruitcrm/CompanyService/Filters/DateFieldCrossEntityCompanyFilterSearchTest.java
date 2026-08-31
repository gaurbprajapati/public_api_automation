package io.recruitcrm.CompanyService.Filters;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.DateUtil;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
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

import java.time.LocalDate;
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
public class DateFieldCrossEntityCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions function = new AllCrudFunctions();
    commanFunction commanFunction = new commanFunction();
    String albatrossAuthToken;
    String apiKey;
    String email;
    Map<String, List<JsonPath>> contactDataMap = new HashMap<>();
    Map<Integer, String> companyIdToKeyMap = new HashMap<>();
    Map<String, String> companySlugToKeyMap = new HashMap<>();
    Map<String, Map<String, String>> timestampScenarios;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dateFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Date Fields (Cross Entity Contact)")
    public void dateFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        JSONObject contactDataByCompany = getContactsForCompanies(data);
        logCompanyNameAndContact(response, data, contactDataByCompany, fieldName, dbField);
        validateDateFieldCrossEntityFilteredData(contactDataByCompany, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    public void validateDateFieldCrossEntityFilteredData(JSONObject contactDataByCompany, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(contactDataByCompany.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && contactDataByCompany.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String companySlug : contactDataByCompany.keySet()) {
            JSONArray companyAssociatedContacts = contactDataByCompany.getJSONArray(companySlug);
            boolean atLeastOneMatch = false;
            List<String> debugChecks = new ArrayList<>();
            List<String> debugContactObjects = new ArrayList<>();
            for (int i = 0; i < companyAssociatedContacts.length(); i++) {
                JSONObject contact = companyAssociatedContacts.getJSONObject(i);
                String contactDateStr = contact.optString(dbField, "").trim();
                String debugLine = validateDateFieldFilteredDataWithDetails(contact, contactDateStr, filterType, filterValue, fieldName, dbField);
                debugChecks.add(debugLine);
                try {
                    debugContactObjects.add("Contact " + (i + 1) + " JSON:\n" + contact.toString(2));
                } catch (Exception e) {
                    debugContactObjects.add("Contact " + (i + 1) + " JSON: <failed to stringify: " + e.getMessage() + ">");
                }
                boolean matches = debugLine.startsWith("MATCH");
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                StringBuilder sb = new StringBuilder();
                sb.append("No contact matched the filter for company still coming in the response: ").append(companySlug)
                        .append("\nField Name: ").append(fieldName)
                        .append("\nDB Field: ").append(dbField)
                        .append("\nFilter Type: ").append(filterType)
                        .append("\nFilter Value: ").append(filterValue)
                        .append("\nExpected Result: ").append(expectedResult)
                        .append("\n\nContacts checked (failure debug):");
                for (String line : debugChecks) {
                    sb.append("\n  - ").append(line);
                }

                // Log full contact object(s) in Extent report for easier debugging of field presence
                if (!debugContactObjects.isEmpty()) {
                    StringBuilder contactJsonLog = new StringBuilder();
                    contactJsonLog.append("<b>🔎 Debug: Full contact object(s) validated for companySlug: </b>").append(companySlug);
                    contactJsonLog.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px; max-height: 500px; overflow: auto;'>");
                    contactJsonLog.append("<code>");
                    for (String json : debugContactObjects) {
                        contactJsonLog.append(json).append("\n\n");
                    }
                    contactJsonLog.append("</code></pre>");
                    FilterSearchReporter.logInfo(contactJsonLog.toString());
                }

                Assert.fail(sb.toString());
            }
        }
    }

    /**
     * Builds a single-line debug string describing whether a contact matches a date filter and why.
     * This is used only for enriching assertion failures to speed up debugging.
     */
    private String validateDateFieldFilteredDataWithDetails(JSONObject contact, String contactDate, String filterType, String filterValue, String fieldName, String dbField) {
        String firstName = contact.optString("firstname", "").trim();
        String lastName = contact.optString("lastname", "").trim();
        String contactName = (firstName + " " + lastName).trim();
        String contactSlug = contact.optString("slug", "");
        String contactId = contact.optString("id", "");
        if (contactName.isEmpty()) {
            contactName = "(no name)";
        }
        String contactIdentity = contactName +
                (contactId.isEmpty() ? "" : " id=" + contactId) +
                (contactSlug.isEmpty() ? "" : " slug=" + contactSlug);

        if (contactDate.isEmpty() || contactDate.equals("null") || contactDate.equals("0")) {
            boolean matchesEmpty = filterType.equals("is_empty");
            return (matchesEmpty ? "MATCH" : "NO_MATCH") + " contact=" + contactIdentity +
                    " " + dbField + "='" + contactDate + "'" +
                    " (empty/null/0 -> " + (matchesEmpty ? "matches is_empty" : "does not match " + filterType) + ")";
        }

        try {
            LocalDate contactParsedDate = parseDate(contactDate);
            boolean matches;
            String reason;

            switch (filterType) {
                case "is":
                case "is_equal_to":
                    if (isRelativeDatePeriod(filterValue)) {
                        matches = isDateInPeriod(contactParsedDate, filterValue);
                        reason = "parsed=" + contactParsedDate + " relativePeriod=" + filterValue +
                                " window=[computed by isDateInPeriod()]";
                    } else {
                        LocalDate filterDate = parseDate(filterValue);
                        matches = contactParsedDate.equals(filterDate);
                        reason = "parsed=" + contactParsedDate + " expected=" + filterDate;
                    }
                    break;
                case "is_not": {
                    LocalDate filterDateNot = parseDate(filterValue);
                    matches = !contactParsedDate.equals(filterDateNot);
                    reason = "parsed=" + contactParsedDate + " notEqualTo=" + filterDateNot;
                    break;
                }
                case "is_before": {
                    LocalDate filterDateBefore = parseDate(filterValue);
                    matches = contactParsedDate.isBefore(filterDateBefore);
                    reason = "parsed=" + contactParsedDate + " before=" + filterDateBefore;
                    break;
                }
                case "is_after": {
                    LocalDate filterDateAfter = parseDate(filterValue);
                    matches = contactParsedDate.isAfter(filterDateAfter);
                    reason = "parsed=" + contactParsedDate + " after=" + filterDateAfter;
                    break;
                }
                case "is_between": {
                    String[] dates = filterValue.split(",");
                    if (dates.length != 2) {
                        matches = false;
                        reason = "invalid between filterValue (expected 'start,end')";
                    } else {
                        LocalDate startDate = parseDate(dates[0].trim());
                        LocalDate endDate = parseDate(dates[1].trim());
                        matches = (contactParsedDate.isEqual(startDate) || contactParsedDate.isAfter(startDate)) &&
                                (contactParsedDate.isEqual(endDate) || contactParsedDate.isBefore(endDate));
                        reason = "parsed=" + contactParsedDate + " between=[" + startDate + "," + endDate + "] inclusive";
                    }
                    break;
                }
                case "is_mt": {
                    int days = Integer.parseInt(filterValue);
                    LocalDate cutoffDate = LocalDate.now().minusDays(days);
                    matches = contactParsedDate.isBefore(cutoffDate) || contactParsedDate.isEqual(cutoffDate);
                    reason = "parsed=" + contactParsedDate + " <= cutoff=" + cutoffDate + " (daysAgo=" + days + ")";
                    break;
                }
                case "is_lt": {
                    int daysLt = Integer.parseInt(filterValue);
                    LocalDate cutoffDateLt = LocalDate.now().minusDays(daysLt);
                    matches = contactParsedDate.isAfter(cutoffDateLt) || contactParsedDate.isEqual(cutoffDateLt);
                    reason = "parsed=" + contactParsedDate + " >= cutoff=" + cutoffDateLt + " (daysAgo=" + daysLt + ")";
                    break;
                }
                case "has_any_value":
                    matches = !contactDate.isEmpty() && !contactDate.equals("null") && !contactDate.equals("0");
                    reason = "has_any_value check";
                    break;
                case "is_empty":
                    matches = contactDate.isEmpty() || contactDate.equals("null") || contactDate.equals("0");
                    reason = "is_empty check";
                    break;
                default:
                    matches = false;
                    reason = "unsupported filterType=" + filterType;
            }

            return (matches ? "MATCH" : "NO_MATCH") + " contact=" + contactIdentity +
                    " " + dbField + "='" + contactDate + "'" +
                    " (" + reason + ")";

        } catch (Exception e) {
            return "NO_MATCH contact=" + contactIdentity +
                    " " + dbField + "='" + contactDate + "'" +
                    " (parse/eval error: " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")";
        }
    }

    private boolean validateDateFieldFilteredDataBoolean(String contactDate, String filterType, String filterValue, String fieldName) {
        if (contactDate.isEmpty() || contactDate.equals("null") || contactDate.equals("0")) {
            return filterType.equals("is_empty");
        }

        try {
            LocalDate contactParsedDate = parseDate(contactDate);
            
            switch (filterType) {
                case "is":
                case "is_equal_to":
                    if (isRelativeDatePeriod(filterValue)) {
                        return isDateInPeriod(contactParsedDate, filterValue);
                    } else {
                        LocalDate filterDate = parseDate(filterValue);
                        return contactParsedDate.equals(filterDate);
                    }
                case "is_not":
                    LocalDate filterDateNot = parseDate(filterValue);
                    return !contactParsedDate.equals(filterDateNot);
                case "is_before":
                    LocalDate filterDateBefore = parseDate(filterValue);
                    return contactParsedDate.isBefore(filterDateBefore);
                case "is_after":
                    LocalDate filterDateAfter = parseDate(filterValue);
                    return contactParsedDate.isAfter(filterDateAfter);
                case "is_between":
                    String[] dates = filterValue.split(",");
                    if (dates.length != 2) {
                        return false;
                    }
                    LocalDate startDate = parseDate(dates[0].trim());
                    LocalDate endDate = parseDate(dates[1].trim());
                    return (contactParsedDate.isEqual(startDate) || contactParsedDate.isAfter(startDate)) &&
                           (contactParsedDate.isEqual(endDate) || contactParsedDate.isBefore(endDate));
                case "is_mt":
                    int days = Integer.parseInt(filterValue);
                    LocalDate cutoffDate = LocalDate.now().minusDays(days);
                    return contactParsedDate.isBefore(cutoffDate) || contactParsedDate.isEqual(cutoffDate);
                case "is_lt":
                    int daysLt = Integer.parseInt(filterValue);
                    LocalDate cutoffDateLt = LocalDate.now().minusDays(daysLt);
                    return contactParsedDate.isAfter(cutoffDateLt) || contactParsedDate.isEqual(cutoffDateLt);
                case "has_any_value":
                    return !contactDate.isEmpty() && !contactDate.equals("null") && !contactDate.equals("0");
                case "is_empty":
                    return contactDate.isEmpty() || contactDate.equals("null") || contactDate.equals("0");
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public JSONObject getContactsForCompanies(JSONArray companyData) {
        JSONObject contactResult = new JSONObject();
        for (int i = 0; i < companyData.length(); i++) {
            JSONArray companyAssociatedContacts = new JSONArray();
            JSONObject company = companyData.getJSONObject(i);
            String companySlug = company.getString("slug");
            List<JsonPath> contactList = contactDataMap.get(companySlug);
            if (contactList != null) {
                for (JsonPath contactJsonPath : contactList) {
                    Map<String, Object> contactMap = contactJsonPath.get("data.contact");
                    JSONObject contact = new JSONObject(contactMap);
                    companyAssociatedContacts.put(contact);
                }
            }
            contactResult.put(companySlug, companyAssociatedContacts);
        }
        return contactResult;
    }

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/companyContact_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {   
            List<CompletableFuture<Void>> createFutures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        Response response = function.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String contactSlug = jsonPath.getString("data.contact.slug");
                        Integer companyId = jsonPath.getInt("data.company.id");
                        String companySlug = jsonPath.getString("data.company.slug");

                        // Store lookup maps for matching companies in response
                        synchronized (companyIdToKeyMap) {
                            companyIdToKeyMap.put(companyId, companyKey);
                        }
                        synchronized (companySlugToKeyMap) {
                            companySlugToKeyMap.put(companySlug, companyKey);
                        }

                        // Get contact for the company and store JsonPath as a list
                        Response contactResponse = getContact(contactSlug);
                        JsonPath contactJsonPath = contactResponse.jsonPath();
                        
                        synchronized (contactDataMap) {
                            List<JsonPath> contactList = new ArrayList<>();
                            contactList.add(contactJsonPath);
                            contactDataMap.put(companySlug, contactList);
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();

            // Create additional contacts for companies 2, 4, and 6
            createAdditionalContacts(companyJson);
            
            // Create activities for contacts and update timestamps
            createActivitiesForContacts();
            updateContactsWithTimestampScenarios();
            refreshContactDataMap();

        } finally {
            executor.shutdown();
        }
    }


    private void refreshContactDataMap() {
        synchronized (contactDataMap) {
            for (Map.Entry<String, List<JsonPath>> entry : contactDataMap.entrySet()) {
                String companySlug = entry.getKey();
                List<JsonPath> existingContacts = entry.getValue();
                if (existingContacts == null || existingContacts.isEmpty()) {
                    continue;
                }

                List<JsonPath> refreshed = new ArrayList<>();
                for (JsonPath contactJsonPath : existingContacts) {
                    try {
                        String contactSlug = contactJsonPath.getString("data.contact.slug");
                        if (contactSlug == null || contactSlug.isEmpty()) {
                            refreshed.add(contactJsonPath);
                            continue;
                        }
                        Response updatedContactResponse = getContact(contactSlug);
                        refreshed.add(updatedContactResponse.jsonPath());
                    } catch (Exception e) {
                        refreshed.add(contactJsonPath);
                    }
                }
                contactDataMap.put(companySlug, refreshed);
            }
        }
    }

    private void createAdditionalContacts(JSONObject companyJson) {
        String[] companiesToAddContacts = {"company2", "company4", "company6"};
        
        for (String companyKey : companiesToAddContacts) {
            if (companyJson.has(companyKey)) {
                JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                if (companyEntry.has("additionalContact")) {
                    JSONObject additionalContact = companyEntry.getJSONObject("additionalContact");
                    if (additionalContact.has("contact1")) {
                        JSONObject contact1 = additionalContact.getJSONObject("contact1");
                        JSONObject payload = contact1.getJSONObject("payload");
                        
                        // Get company slug and ID from the maps
                        String companySlug = null;
                        String companyId = null;
                        
                        for (Map.Entry<String, String> entry : companySlugToKeyMap.entrySet()) {
                            if (entry.getValue().equals(companyKey)) {
                                companySlug = entry.getKey();
                                break;
                            }
                        }
                        
                        for (Map.Entry<Integer, String> entry : companyIdToKeyMap.entrySet()) {
                            if (entry.getValue().equals(companyKey)) {
                                companyId = String.valueOf(entry.getKey());
                                break;
                            }
                        }
                        
                        if (companySlug != null && companyId != null) {
                            // Update payload with actual company slug and ID
                            JSONArray selectedCompanies = payload.getJSONArray("selectedcompanies");
                            if (selectedCompanies.length() > 0) {
                                JSONObject companyInfo = selectedCompanies.getJSONObject(0);
                                companyInfo.put("slug", companySlug);
                                companyInfo.put("id", companyId);
                            }
                            
                            // Create the additional contact
                            try {
                                Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", 
                                        albatrossAuthToken, null, true, payload);
                                response.then().statusCode(200);
                                
                                // Store additional contact in contactDataMap
                                JsonPath contactJsonPath = response.jsonPath();
                                String contactSlug = contactJsonPath.getString("data.contact.slug");
                                if (contactSlug == null || contactSlug.isEmpty()) {
                                    contactSlug = contactJsonPath.getString("slug");
                                }
                                if (contactSlug == null || contactSlug.isEmpty()) {
                                    System.err.println("Failed to extract contactSlug from create contact response for " + companyKey +
                                            ". Response: " + response.getBody().asString());
                                    continue;
                                }
                                Response contactResponse = getContact(contactSlug);
                                JsonPath additionalContactJsonPath = contactResponse.jsonPath();
                                
                                synchronized (contactDataMap) {
                                    List<JsonPath> contactList = contactDataMap.get(companySlug);
                                    if (contactList == null) {
                                        contactList = new ArrayList<>();
                                        contactDataMap.put(companySlug, contactList);
                                    }
                                    contactList.add(additionalContactJsonPath);
                                }
                                
                                System.out.println("Successfully created additional contact for " + companyKey);
                            } catch (Exception e) {
                                System.err.println("Failed to create additional contact for " + companyKey + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    private void createActivitiesForContacts() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> activityFutures = new ArrayList<>();
            
            for (List<JsonPath> contactList : contactDataMap.values()) {
                for (JsonPath contactJsonPath : contactList) {
                    String contactSlug = contactJsonPath.getString("data.contact.slug");
                    if (contactSlug == null || contactSlug.isEmpty()) {
                        continue;
                    }
                    
                    // Create call log
                    activityFutures.add(CompletableFuture.runAsync(() -> {
                        commanFunction.createNewCallLogWithEntitySlug(baseURL, apiKey, "contact", contactSlug);
                    }, executor));
                }
            }
            
            CompletableFuture.allOf(activityFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void updateContactsWithTimestampScenarios() {
        timestampScenarios = createTimestampScenarios();
        List<Integer> contactIds = new ArrayList<>();
        
        // Collect all contact IDs
        for (List<JsonPath> contactList : contactDataMap.values()) {
            for (JsonPath contactJsonPath : contactList) {
                try {
                    String contactIdStr = contactJsonPath.getString("data.contact.id");
                    if (contactIdStr != null && !contactIdStr.isEmpty()) {
                        Integer contactId = Integer.valueOf(contactIdStr);
                        contactIds.add(contactId);
                    }
                } catch (Exception e) {
                    // Skip if contact ID cannot be retrieved
                    System.err.println("Failed to get contact ID: " + e.getMessage());
                }
            }
        }
        
        if (contactIds.isEmpty()) {
            System.err.println("No contact IDs found to update timestamps");
            return;
        }
        
        int contactIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : timestampScenarios.entrySet()) {
            if (contactIndex >= timestampScenarios.size() || contactIndex >= contactIds.size()) {
                break;
            }

            Map<String, String> timestamps = scenario.getValue();
            Integer contactId = contactIds.get(contactIndex);
            
            if (contactId == null) {
                contactIndex++;
                continue;
            }

            JSONObject fieldsAndValues = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndValues.put(timestamp.getKey(), timestamp.getValue());
            }

            Response updateResponse = ReaperIntegration.updateContactFields(contactId, fieldsAndValues);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update the contact fields timestamps for contact ID: " + contactId);
            }
            contactIndex++;
        }
        
        // Update last activity timestamps
        updateContactsWithLastActivityTimestamps(contactIds);
    }

    private void updateContactsWithLastActivityTimestamps(List<Integer> contactIds) {
        if (contactIds.isEmpty()) {
            System.err.println("No contact IDs found to update last activity timestamps");
            return;
        }
        
        Map<String, Map<String, String>> lastActivityScenarios = createLastActivityTimestampScenarios();
        int contactIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : lastActivityScenarios.entrySet()) {
            if (contactIndex >= lastActivityScenarios.size() || contactIndex >= contactIds.size()) {
                break;
            }

            Map<String, String> timestamps = scenario.getValue();
            Integer contactId = contactIds.get(contactIndex);
            
            if (contactId == null) {
                contactIndex++;
                continue;
            }

            JSONObject fieldsAndTimestamps = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
            }

            Response updateResponse = ReaperIntegration.updateLastActivityTimestamp("contact", contactId, fieldsAndTimestamps);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update the contact last activity timestamps for contact ID: " + contactId);
            }
            contactIndex++;
        }
    }

    private Map<String, Map<String, String>> createTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        // Today scenario
        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("createdon", todayEpoch);
        todayTimestamps.put("updatedon", todayEpoch);
        scenarios.put("today_scenario", todayTimestamps);

        // Yesterday scenario
        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("createdon", yesterdayEpoch);
        yesterdayTimestamps.put("updatedon", yesterdayEpoch);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);

        // This week scenario
        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("createdon", thisWeekEpoch);
        thisWeekTimestamps.put("updatedon", thisWeekEpoch);
        scenarios.put("this_week_scenario", thisWeekTimestamps);

        // Last week scenario
        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("createdon", lastWeekEpoch);
        lastWeekTimestamps.put("updatedon", lastWeekEpoch);
        scenarios.put("last_week_scenario", lastWeekTimestamps);

        // This month scenario
        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("createdon", thisMonthEpoch);
        thisMonthTimestamps.put("updatedon", thisMonthEpoch);
        scenarios.put("this_month_scenario", thisMonthTimestamps);

        // Last month scenario
        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("createdon", lastMonthEpoch);
        lastMonthTimestamps.put("updatedon", lastMonthEpoch);
        scenarios.put("last_month_scenario", lastMonthTimestamps);

        // This quarter scenario
        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("createdon", thisQuarterEpoch);
        thisQuarterTimestamps.put("updatedon", thisQuarterEpoch);
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);

        // Last quarter scenario
        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("createdon", lastQuarterEpoch);
        lastQuarterTimestamps.put("updatedon", lastQuarterEpoch);
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);

        // This year scenario
        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("createdon", thisYearEpoch);
        thisYearTimestamps.put("updatedon", thisYearEpoch);
        scenarios.put("this_year_scenario", thisYearTimestamps);

        // Last year scenario
        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("createdon", lastYearEpoch);
        lastYearTimestamps.put("updatedon", lastYearEpoch);
        scenarios.put("last_year_scenario", lastYearTimestamps);

        // Static date scenario 1 - Historical data (June 15, 2022)
        Map<String, String> staticTimestamps1 = new HashMap<>();
        staticTimestamps1.put("createdon", "1655251200");  // 2022-06-15 00:00:00 UTC
        staticTimestamps1.put("updatedon", "1655251200");
        scenarios.put("static_date_scenario1", staticTimestamps1);

        // Static date scenario 2 - Previous year data (March 10, 2023)
        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("createdon", "1678406400");  // 2023-03-10 00:00:00 UTC
        staticTimestamps2.put("updatedon", "1678406400");
        scenarios.put("static_date_scenario2", staticTimestamps2);

        // Static date scenario 3 - Current year data (June 6, 2024)
        Map<String, String> staticTimestamps3 = new HashMap<>();
        staticTimestamps3.put("createdon", "1717689600");  // 2024-06-06 00:00:00 UTC
        staticTimestamps3.put("updatedon", "1717689600");
        scenarios.put("static_date_scenario3", staticTimestamps3);

        // Static date scenario 4 - Future planning data (February 14, 2025)
        Map<String, String> staticTimestamps4 = new HashMap<>();
        staticTimestamps4.put("createdon", "1739491200");  // 2025-02-14 00:00:00 UTC
        staticTimestamps4.put("updatedon", "1739491200");
        scenarios.put("static_date_scenario4", staticTimestamps4);

        return scenarios;
    }

    private Map<String, Map<String, String>> createLastActivityTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        // Today scenario
        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("calllog_created_on", todayEpoch);
        todayTimestamps.put("meeting_created_on", todayEpoch);
        todayTimestamps.put("email_sent_on", todayEpoch);
        todayTimestamps.put("message_sent_on", todayEpoch);
        todayTimestamps.put("last_communication_timestamp", todayEpoch);
        scenarios.put("today_last_activity", todayTimestamps);

        // Yesterday scenario
        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("calllog_created_on", yesterdayEpoch);
        yesterdayTimestamps.put("meeting_created_on", yesterdayEpoch);
        yesterdayTimestamps.put("email_sent_on", yesterdayEpoch);
        yesterdayTimestamps.put("message_sent_on", yesterdayEpoch);
        yesterdayTimestamps.put("last_communication_timestamp", yesterdayEpoch);
        scenarios.put("yesterday_last_activity", yesterdayTimestamps);

        // This week scenario
        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("calllog_created_on", thisWeekEpoch);
        thisWeekTimestamps.put("meeting_created_on", thisWeekEpoch);
        thisWeekTimestamps.put("email_sent_on", thisWeekEpoch);
        thisWeekTimestamps.put("message_sent_on", thisWeekEpoch);
        thisWeekTimestamps.put("last_communication_timestamp", thisWeekEpoch);
        scenarios.put("this_week_last_activity", thisWeekTimestamps);

        // Last week scenario
        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("calllog_created_on", lastWeekEpoch);
        lastWeekTimestamps.put("meeting_created_on", lastWeekEpoch);
        lastWeekTimestamps.put("email_sent_on", lastWeekEpoch);
        lastWeekTimestamps.put("message_sent_on", lastWeekEpoch);
        lastWeekTimestamps.put("last_communication_timestamp", lastWeekEpoch);
        scenarios.put("last_week_last_activity", lastWeekTimestamps);

        // This month scenario
        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("calllog_created_on", thisMonthEpoch);
        thisMonthTimestamps.put("meeting_created_on", thisMonthEpoch);
        thisMonthTimestamps.put("email_sent_on", thisMonthEpoch);
        thisMonthTimestamps.put("message_sent_on", thisMonthEpoch);
        thisMonthTimestamps.put("last_communication_timestamp", thisMonthEpoch);
        scenarios.put("this_month_last_activity", thisMonthTimestamps);

        // Last month scenario
        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("calllog_created_on", lastMonthEpoch);
        lastMonthTimestamps.put("meeting_created_on", lastMonthEpoch);
        lastMonthTimestamps.put("email_sent_on", lastMonthEpoch);
        lastMonthTimestamps.put("message_sent_on", lastMonthEpoch);
        lastMonthTimestamps.put("last_communication_timestamp", lastMonthEpoch);
        scenarios.put("last_month_last_activity", lastMonthTimestamps);

        // This quarter scenario
        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("calllog_created_on", thisQuarterEpoch);
        thisQuarterTimestamps.put("meeting_created_on", thisQuarterEpoch);
        thisQuarterTimestamps.put("email_sent_on", thisQuarterEpoch);
        thisQuarterTimestamps.put("message_sent_on", thisQuarterEpoch);
        thisQuarterTimestamps.put("last_communication_timestamp", thisQuarterEpoch);
        scenarios.put("this_quarter_last_activity", thisQuarterTimestamps);

        // Last quarter scenario
        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("calllog_created_on", lastQuarterEpoch);
        lastQuarterTimestamps.put("meeting_created_on", lastQuarterEpoch);
        lastQuarterTimestamps.put("email_sent_on", lastQuarterEpoch);
        lastQuarterTimestamps.put("message_sent_on", lastQuarterEpoch);
        lastQuarterTimestamps.put("last_communication_timestamp", lastQuarterEpoch);
        scenarios.put("last_quarter_last_activity", lastQuarterTimestamps);

        // This year scenario
        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("calllog_created_on", thisYearEpoch);
        thisYearTimestamps.put("meeting_created_on", thisYearEpoch);
        thisYearTimestamps.put("email_sent_on", thisYearEpoch);
        thisYearTimestamps.put("message_sent_on", thisYearEpoch);
        thisYearTimestamps.put("last_communication_timestamp", thisYearEpoch);
        scenarios.put("this_year_last_activity", thisYearTimestamps);

        // Last year scenario
        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("calllog_created_on", lastYearEpoch);
        lastYearTimestamps.put("meeting_created_on", lastYearEpoch);
        lastYearTimestamps.put("email_sent_on", lastYearEpoch);
        lastYearTimestamps.put("message_sent_on", lastYearEpoch);
        lastYearTimestamps.put("last_communication_timestamp", lastYearEpoch);
        scenarios.put("last_year_last_activity", lastYearTimestamps);

        // Static date scenario 1 - Historical data (June 15, 2022)
        Map<String, String> staticLastActivityTimestamps1 = new HashMap<>();
        staticLastActivityTimestamps1.put("calllog_created_on", "1655251200");  // 2022-06-15 00:00:00 UTC
        staticLastActivityTimestamps1.put("meeting_created_on", "1655251200");
        staticLastActivityTimestamps1.put("email_sent_on", "1655251200");
        staticLastActivityTimestamps1.put("message_sent_on", "1655251200");
        staticLastActivityTimestamps1.put("last_communication_timestamp", "1655251200");
        scenarios.put("static_last_activity_scenario1", staticLastActivityTimestamps1);

        // Static date scenario 2 - Previous year data (March 10, 2023)
        Map<String, String> staticLastActivityTimestamps2 = new HashMap<>();
        staticLastActivityTimestamps2.put("calllog_created_on", "1678406400");  // 2023-03-10 00:00:00 UTC
        staticLastActivityTimestamps2.put("meeting_created_on", "1678406400");
        staticLastActivityTimestamps2.put("email_sent_on", "1678406400");
        staticLastActivityTimestamps2.put("message_sent_on", "1678406400");
        staticLastActivityTimestamps2.put("last_communication_timestamp", "1678406400");
        scenarios.put("static_last_activity_scenario2", staticLastActivityTimestamps2);

        // Static date scenario 3 - Current year data (June 6, 2024)
        Map<String, String> staticLastActivityTimestamps3 = new HashMap<>();
        staticLastActivityTimestamps3.put("calllog_created_on", "1717689600");  // 2024-06-06 00:00:00 UTC
        staticLastActivityTimestamps3.put("meeting_created_on", "1717689600");
        staticLastActivityTimestamps3.put("email_sent_on", "1717689600");
        staticLastActivityTimestamps3.put("message_sent_on", "1717689600");
        staticLastActivityTimestamps3.put("last_communication_timestamp", "1717689600");
        scenarios.put("static_last_activity_scenario3", staticLastActivityTimestamps3);

        // Static date scenario 4 - Future planning data (February 14, 2025)
        Map<String, String> staticLastActivityTimestamps4 = new HashMap<>();
        staticLastActivityTimestamps4.put("calllog_created_on", "1739491200");  // 2025-02-14 00:00:00 UTC
        staticLastActivityTimestamps4.put("meeting_created_on", "1739491200");
        staticLastActivityTimestamps4.put("email_sent_on", "1739491200");
        staticLastActivityTimestamps4.put("message_sent_on", "1739491200");
        staticLastActivityTimestamps4.put("last_communication_timestamp", "1739491200");
        scenarios.put("static_last_activity_scenario4", staticLastActivityTimestamps4);

        return scenarios;
    }

    public Response getContact(String contactSlug) {
        String basePath = "/contacts/{contactSlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactSlug", contactSlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    private void logCompanyNameAndContact(Response response, JSONArray companyData, JSONObject contactDataByCompany, String fieldName, String dbField) {
        FilterSearchReporter.logInfo("<b>📋 Company - Contact Information:</b>");
        
        if (companyData != null && contactDataByCompany != null) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>");
            logMessage.append("<code>");
            
            for (int i = 0; i < companyData.length(); i++) {
                JSONObject company = companyData.getJSONObject(i);
                String companySlug = company.getString("slug");
                JSONArray associatedContacts = contactDataByCompany.optJSONArray(companySlug);
                
                String companyName = company.optString("companyname", "N/A");
                logMessage.append("Company: ").append(companyName).append("\n");
                
                if (associatedContacts != null && associatedContacts.length() > 0) {
                    for (int j = 0; j < associatedContacts.length(); j++) {
                        JSONObject contact = associatedContacts.getJSONObject(j);
                        String firstName = contact.optString("firstname", "").trim();
                        String lastName = contact.optString("lastname", "").trim();
                        String contactName = (firstName + " " + lastName).trim();
                        if (contactName.isEmpty()) {
                            contactName = "(no name)";
                        }
                        String contactFieldValue = contact.optString(dbField, "").trim();
                        if (contactFieldValue.isEmpty() || contactFieldValue.equals("null") || contactFieldValue.equals("0")) {
                            contactFieldValue = "N/A";
                        }
                        logMessage.append("  Contact ").append(j + 1).append(": ").append(contactName);
                        logMessage.append(" | ").append(fieldName).append(": ").append(contactFieldValue).append("\n");
                    }
                } else {
                    logMessage.append("  No contacts found\n");
                }
                logMessage.append("\n");
            }
            
            logMessage.append("</code></pre>");
            FilterSearchReporter.logInfo(logMessage.toString());
        }
    }

    @DataProvider(name = "dateFieldCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityContactDateTypeFilterDataProvider.json");
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
        payload.put("advancedSearchContext", "COMPANY");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        JSONObject filterValueObj = new JSONObject();
        
        if (filterType.equals("is_between")) {
            filterValueObj.put("type", "LONG_START_END");
            JSONObject rangeValue = new JSONObject();
            String startValue = filterValue.split(",")[0].trim();
            String endValue = filterValue.split(",")[1].trim();
            long startEpoch = dateToEpochSeconds(startValue);
            long endEpoch = dateToEpochSeconds(endValue);
            rangeValue.put("start", startEpoch);
            rangeValue.put("end", endEpoch);
            filterValueObj.put("value", rangeValue);
        } else {
            filterValueObj.put("type", filterValue_TYPE);
            if (filterType.equals("is_mt") || filterType.equals("is_lt")) {
                filterValueObj.put("value", Integer.parseInt(filterValue));
            } else if (filterType.equals("has_any_value") || filterType.equals("is_empty")) {
                filterValueObj.put("value", filterValue.isEmpty() ? 0 : Integer.parseInt(filterValue));
            } else if (filterType.equals("is_equal_to") || filterType.equals("is_before") || filterType.equals("is_after")) {
                long epochValue = dateToEpochSeconds(filterValue);
                filterValueObj.put("value", epochValue);
            } else {
                filterValueObj.put("value", filterValue);
            }
        }
        
        // Create filterSearchList structure
        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();
        
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");
        
        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", true);
        filter.put("groupType", "contacts");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        if (dbField.startsWith("last")) {
            filter.put("entityType", "contact_last_activities_t");
        } else {
            filter.put("entityType", "contact");
        }
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
}
