package io.recruitcrm.CompanyService.Filters;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.reaper.ReaperIntegration;
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.javafaker.JavaFakerMeeting;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ContactLastCommsTypeCrossEntityCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String accountOwnerAPIKey;
    String email;
    ConcurrentHashMap<String, String> companyKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyIdToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companySlugToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, List<JsonPath>> contactDataMap = new ConcurrentHashMap<>();
    Map<String, String> lastCommsTypeMap = new HashMap<>();
    Map<String, Integer> contactSlugToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> contactSlugMap = new ConcurrentHashMap<>(); // Key: companyKey, Value: contactSlug
    ConcurrentHashMap<Integer, String> contactIdToLastCommsTypeMap = new ConcurrentHashMap<>(); // Key: contactId, Value: LastCommsType
    JavaFakerMeeting fakerMeeting;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        fakerMeeting = new JavaFakerMeeting();
        initializeLastCommsTypeMap();
        createTestData();
        waitForDataSync();
    }

    private void initializeLastCommsTypeMap() {
        // Map LastCommsType values to API format (lowercase)
        lastCommsTypeMap.put("Email", "email");
        lastCommsTypeMap.put("CallLog", "calllog");
        lastCommsTypeMap.put("SMS", "sms");
        lastCommsTypeMap.put("Meeting", "meeting");
        lastCommsTypeMap.put("Message", "message");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "contactLastCommsTypeCrossEntityFilterSearchTestData", description = "Filter Search Test for Contact LastCommsType Cross Entity Company")
    public void contactLastCommsTypeCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
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
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String contactSlug = jsonPath.getString("data.contact.slug");
                        String contactIdStr = jsonPath.getString("data.contact.id");
                        String companyIdStr = jsonPath.getString("data.company.id");
                        
                        // Skip if company ID is null (company creation failed)
                        if (companyIdStr == null) {
                            System.out.println("Skipping " + companyKey + " - company ID is null");
                            return;
                        }
                        
                        Integer companyId = Integer.parseInt(companyIdStr);
                        String companySlug = jsonPath.getString("data.company.slug");

                        // Store lookup maps for matching companies in response (always store company info)
                        synchronized (companyKeyToIdMap) {
                            companyKeyToIdMap.put(companyKey.toLowerCase(), String.valueOf(companyId));
                        }
                        synchronized (companyIdToKeyMap) {
                            companyIdToKeyMap.put(String.valueOf(companyId), companyKey);
                        }
                        synchronized (companySlugToKeyMap) {
                            companySlugToKeyMap.put(companySlug, companyKey);
                        }

                        // Only process contact-related operations if contact ID is not null
                        if (contactIdStr != null && contactSlug != null && !contactSlug.isEmpty()) {
                            Integer contactId = Integer.parseInt(contactIdStr);

                            // Store contact slug to ID mapping for updating LastCommsType
                            synchronized (contactSlugToIdMap) {
                                contactSlugToIdMap.put(contactSlug, contactId);
                            }
                            
                            // Store contact slug map (companyKey -> contactSlug) for creating meetings
                            synchronized (contactSlugMap) {
                                contactSlugMap.put(companyKey, contactSlug);
                            }

                            // Store contact ID and LastCommsType for updating after meetings are created
                            if (companyEntry.has("LastCommsType")) {
                                String lastCommsType = companyEntry.getString("LastCommsType");
                                synchronized (contactIdToLastCommsTypeMap) {
                                    contactIdToLastCommsTypeMap.put(contactId, lastCommsType);
                                }
                            }

                            // Get contact for the company and store JsonPath as a list
                            try {
                                Response contactResponse = getContact(contactSlug);
                                JsonPath contactJsonPath = contactResponse.jsonPath();
                                
                                synchronized (contactDataMap) {
                                    List<JsonPath> contactList = new ArrayList<>();
                                    contactList.add(contactJsonPath);
                                    contactDataMap.put(companySlug, contactList);
                                }
                            } catch (Exception e) {
                                System.out.println("Warning: Could not fetch contact for " + companyKey + ": " + e.getMessage());
                            }
                        } else {
                            System.out.println("Skipping contact operations for " + companyKey + " - contact ID or slug is null");
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();

            // Create additional contacts for companies 2, 4, and 6
            createAdditionalContacts(companyJson);
            
            // Create meetings for contacts
            createMeetingsForContact(contactSlugMap);
            
            // Update LastCommsType for all contacts after meetings are created
            updateAllContactsLastCommsType();

        } finally {
            executor.shutdown();
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
                        
                        for (Map.Entry<String, String> entry : companyIdToKeyMap.entrySet()) {
                            if (entry.getValue().equals(companyKey)) {
                                companyId = entry.getKey();
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
                                
                                JsonPath contactJsonPath = response.jsonPath();
                                String contactSlug = contactJsonPath.getString("data.contact.slug");
                                String contactIdStr = contactJsonPath.getString("data.contact.id");
                                
                                // Skip if contact ID is null
                                if (contactIdStr == null) {
                                    System.err.println("Failed to create additional contact for " + companyKey + " - contact ID is null");
                                    continue;
                                }
                                
                                Integer contactId = Integer.parseInt(contactIdStr);
                                
                                // Store contact ID and LastCommsType for updating after meetings are created
                                if (payload.has("LastCommsType")) {
                                    String lastCommsType = payload.getString("LastCommsType");
                                    synchronized (contactIdToLastCommsTypeMap) {
                                        contactIdToLastCommsTypeMap.put(contactId, lastCommsType);
                                    }
                                }
                                
                                // Store additional contact slug in contactSlugMap
                                synchronized (contactSlugMap) {
                                    contactSlugMap.put(companyKey + "_additional", contactSlug);
                                }
                                
                                // Store additional contact in contactDataMap
                                Response contactResponse = getContact(contactSlug);
                                JsonPath additionalContactJsonPath = contactResponse.jsonPath();
                                
                                synchronized (contactDataMap) {
                                    List<JsonPath> contactList = contactDataMap.get(companySlug);
                                    if (contactList != null) {
                                        contactList.add(additionalContactJsonPath);
                                    }
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

    private void updateAllContactsLastCommsType() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> updateFutures = contactIdToLastCommsTypeMap.entrySet().stream()
                    .map(entry -> CompletableFuture.runAsync(() -> {
                        Integer contactId = entry.getKey();
                        String lastCommsType = entry.getValue();
                        updateContactLastCommsType(contactId, lastCommsType);
                    }, executor))
                    .collect(Collectors.toList());
            
            CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void updateContactLastCommsType(Integer contactId, String lastCommsType) {
        try {
            // Map LastCommsType to API format (lowercase)
            String apiValue = lastCommsTypeMap.getOrDefault(lastCommsType, lastCommsType.toLowerCase());
            
            JSONObject fieldsAndTimestamps = new JSONObject();
            fieldsAndTimestamps.put("last_communication_method", apiValue);
            
            Response updateResponse = ReaperIntegration.updateLastActivityTimestamp("contact", contactId, fieldsAndTimestamps);
            if (updateResponse.getStatusCode() == 200) {
                System.out.println("Successfully updated LastCommsType for contact ID: " + contactId + " to " + apiValue);
            } else {
                System.err.println("Failed to update LastCommsType for contact ID: " + contactId + ". Status: " + updateResponse.getStatusCode() + ". Response: " + updateResponse.getBody().asString());
            }
        } catch (Exception e) {
            System.err.println("Error updating LastCommsType for contact ID: " + contactId + ": " + e.getMessage());
        }
    }

    private void createMeetingsForContact(ConcurrentHashMap<String, String> contactSlugMap) {
        List<String> sortedContactKeys = contactSlugMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String contactKey : sortedContactKeys) {
                String contactSlug = contactSlugMap.get(contactKey);
                if (contactSlug == null) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        Meeting meeting = createMeetingForContact(contactSlug);
                        
                        Response response = RestClient.doPost("JSON", baseURL, "meetings", accountOwnerAPIKey, null, true, meeting);
                        
                        if (response.getStatusCode() == 200) {
                            System.out.println("Successfully created meeting for contact " + contactSlug);
                        } else {
                            System.err.println("Meeting creation failed for contact " + contactSlug + ": " + response.getBody().asString());
                        }
                    } catch (Exception e) {
                        System.err.println("Exception creating meeting for contact " + contactSlug + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private Meeting createMeetingForContact(String contactSlug) {
        Meeting meeting = new Meeting();
        
        meeting.setTitle("Contact Meeting");
        meeting.setDescription("Meeting activity for contact");
        meeting.setAddress("Office Address");
        
        // Use getDateDaysFromNow to get properly formatted date string (matching MeetingActivityFieldsCompanyFilterSearchTest pattern)
        String startDate = getDateDaysFromNow(0);
        meeting.setStart_date(startDate);
        meeting.setEnd_date(fakerMeeting.getEndDateWithReferenceDate(startDate));
        meeting.setReminder(30);
        meeting.setRelated_to(contactSlug);
        meeting.setRelated_to_type("contact");
        
        return meeting;
    }

    private String getDateDaysFromNow(int days) {
        java.util.Date date = new java.util.Date();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(java.util.Calendar.DAY_OF_MONTH, days);
        return calendar.getTime().toString();
    }

    public Response getContact(String contactSlug) {
        String basePath = "/contacts/{contactSlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactSlug", contactSlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    @DataProvider(name = "contactLastCommsTypeCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityContactLastCommsTypeFilterDataProvider.json");
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
        
        // Parse filterValue to extract LastCommsType values from placeholders like {Email},{CallLog}
        String processedFilterValue = processFilterValue(filterValue, fieldName);
        
        // Create filterValue object with type and value array
        JSONObject filterValueObj = new JSONObject();
        if (filterValue_TYPE.equals("ENTITY_ASSOCIATION")) {
            filterValueObj = entityAssociationFilterValue(processedFilterValue);
        } else if (filterValue_TYPE.equals("STRING_LIST")) {
            filterValueObj = stringListFilterValue(processedFilterValue);
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
        filter.put("groupType", "contacts");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "contact_last_activities_t");
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

    private String processFilterValue(String filterValue, String fieldName) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }

        String processedValue = filterValue;

        // Pattern to match placeholders like {Email}, {CallLog}, {SMS}
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(filterValue);

        while (matcher.find()) {
            String placeholder = matcher.group(0); // e.g., "{Email}"
            String commsTypeLabel = matcher.group(1); // e.g., "Email"

            // Look up API format value from lastCommsTypeMap
            String apiValue = lastCommsTypeMap.get(commsTypeLabel);
            
            if (apiValue != null) {
                // Replace placeholder with API format value
                processedValue = processedValue.replace(placeholder, apiValue);
            } else {
                throw new IllegalArgumentException("Unable to process the payload, No API value found for LastCommsType label: " + commsTypeLabel);
            }
        }

        return processedValue;
    }
}
