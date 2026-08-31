package io.recruitcrm.CompanyService.Filters;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
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
public class NumberFieldCrossEntityCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions function = new AllCrudFunctions();
    String albatrossAuthToken;
    String email;
    Map<String, List<JsonPath>> contactDataMap = new HashMap<>();
    Map<Integer, String> companyIdToKeyMap = new HashMap<>();
    Map<String, String> companySlugToKeyMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "numberFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Number Fields (Cross Entity Contact)")
    public void numberFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        JSONObject contactDataByCompany = getContactsForCompanies(data);
        logCompanyNameAndContact(response, data, contactDataByCompany, fieldName, dbField);
        validateNumberFieldCrossEntityFilteredData(contactDataByCompany, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    public void validateNumberFieldCrossEntityFilteredData(JSONObject contactDataByCompany, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(contactDataByCompany.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && contactDataByCompany.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String companySlug : contactDataByCompany.keySet()) {
            JSONArray companyAssociatedContacts = contactDataByCompany.getJSONArray(companySlug);
            boolean atLeastOneMatch = false;
            for (int i = 0; i < companyAssociatedContacts.length(); i++) {
                JSONObject contact = companyAssociatedContacts.getJSONObject(i);
                String contactNumber = String.valueOf(contact.opt(dbField)).trim();
                if (contactNumber.equals("null") || contactNumber.isEmpty()) {
                    if (filterType.equals("is_empty")) {
                        atLeastOneMatch = true;
                        break;
                    }
                    continue;
                }
                
                if (contactNumber.endsWith(".0")) {
                    contactNumber = contactNumber.substring(0, contactNumber.length() - 2);
                }
                
                boolean matches = validateNumberFieldFilteredDataBoolean(contactNumber, filterType, filterValue, fieldName);
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No contact matched the filter for company still coming in the response: " + companySlug + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    private boolean validateNumberFieldFilteredDataBoolean(String contactNumber, String filterType, String filterValue, String fieldName) {
        try {
            double contactNumberValue = Double.parseDouble(contactNumber);
            if (filterType.equals("is_between")) {
                String[] rangeParts = filterValue.split(",");
                double startValue = Double.parseDouble(rangeParts[0].trim());
                double endValue = Double.parseDouble(rangeParts[1].trim());
                return contactNumberValue >= startValue && contactNumberValue <= endValue;
            }

            if (filterType.equals("has_any_value")) {
                return !contactNumber.isEmpty() && !contactNumber.equals("null");
            }
            if (filterType.equals("is_empty")) {
                return contactNumber.isEmpty() || contactNumber.equals("null");
            }

            double filterValueDouble = Double.parseDouble(filterValue.trim());
            
            switch (filterType) {
                case "is":
                    return contactNumberValue == filterValueDouble;
                case "is_not":
                    return contactNumberValue != filterValueDouble;
                case "is_mt":
                    return contactNumberValue > filterValueDouble;
                case "is_lt":
                    return contactNumberValue < filterValueDouble;
                case "begins_with":
                    return contactNumber.startsWith(filterValue.trim());
                case "ends_with":
                    return contactNumber.endsWith(filterValue.trim());
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
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
                            contactName = "Unknown";
                        }
                        String contactFieldValue = String.valueOf(contact.opt(dbField)).trim();
                        if (contactFieldValue.isEmpty() || contactFieldValue.equals("null")) {
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

    @DataProvider(name = "numberFieldCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityContactNumberTypeFilterDataProvider.json");
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
            filterValueObj = integerStartEndFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("NUMERIC_STRING")) {
            filterValueObj = numericStringFilterValue(filterValue);
        } else {
            filterValueObj.put("type", filterValue_TYPE);
            if (filterType.equals("has_any_value") || filterType.equals("is_empty")) {
                filterValueObj.put("value", JSONObject.NULL);
            } else {
                if (filterValue != null && !filterValue.trim().isEmpty()) {
                    try {
                        if (filterValue.contains(".")) {
                            filterValueObj.put("value", Double.parseDouble(filterValue));
                        } else {
                            filterValueObj.put("value", Integer.parseInt(filterValue));
                        }
                    } catch (NumberFormatException e) {
                        filterValueObj.put("value", filterValue);
                    }
                } else {
                    filterValueObj.put("value", JSONObject.NULL);
                }
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
        filter.put("entityType", "contact");
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
