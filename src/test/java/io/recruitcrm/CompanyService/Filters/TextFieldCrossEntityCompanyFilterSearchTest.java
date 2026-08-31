package io.recruitcrm.CompanyService.Filters;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.restclient.RestClient;
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
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class TextFieldCrossEntityCompanyFilterSearchTest extends FilterSearchBaseTest {
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
    @Test(groups = {"aries_service"}, dataProvider = "textFieldCrossEntityFilterSearchTestData",description = "Filter Search Test for Text Fields")
    public void textFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200," Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"),"Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        JSONObject contactDataByCompany = getContactsForCompanies(data);
        logCompanyNameAndContact(response, data, contactDataByCompany, fieldName, dbField);
        validateTextFieldCrossEntityFilteredData(contactDataByCompany, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    public void validateTextFieldCrossEntityFilteredData(JSONObject contactDataByCompany, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
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
                boolean matches = validateTextFieldFilteredDataBoolean(contact, filterType, filterValue, fieldName, dbField, expectedResult, "Contact","");
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
                        System.out.println("[DEBUG] Starting creation for: " + companyKey);
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");

                        Response response = function.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        
                        if (response.getStatusCode() != 200) {
                            System.out.println("[ERROR] Failed to create company: " + companyKey);
                            System.out.println("[ERROR] Status code: " + response.getStatusCode());
                            System.out.println("[ERROR] Response body: " + response.getBody().asString());
                            System.out.println("[ERROR] Headers: " + response.getHeaders().toString());
                        }
                        
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String contactSlug = jsonPath.getString("data.contact.slug");
                        
                        // Safely get company ID - handle case where path might return array
                        Integer companyId;
                        try {
                            companyId = jsonPath.getInt("data.company.id");
                        } catch (Exception e) {
                            Object companyIdObj = jsonPath.get("data.company.id");
                            throw new RuntimeException("Invalid company ID for company: " + companyKey + ". Got: " + companyIdObj, e);
                        }
                        
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
                                String contactSlug = contactJsonPath.getString("slug");
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
                        String contactFieldValue = contact.optString(dbField, "").trim();
                        if (contactFieldValue.isEmpty()) {
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



    @DataProvider(name = "textFieldCrossEntityFilterSearchTestData",parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityTextTypeFilterDataProvider.json");
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
        
        // Create filterValue object with type and value
        JSONObject filterValueObj;
        if (filterValue_TYPE.equals("STRING_LIST")) {
            filterValueObj = stringListFilterValue(filterValue);
        } else {
            filterValueObj = new JSONObject();
            filterValueObj.put("type", filterValue_TYPE);
            filterValueObj.put("value", filterValue);
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
