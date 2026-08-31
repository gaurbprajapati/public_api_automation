package io.recruitcrm.ContactService.Filters;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.albatross.UpdateFields;
import java.util.Arrays;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TextFieldCrossEntityContactFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    String albatrossAuthToken;
    String email;
    Map<String, List<JsonPath>> companyDataMap = new HashMap<>();
    Map<String, String> contactSlugToKeyMap = new HashMap<>();
    Map<String, String> contactKeyToSlugMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();
    Map<String, String> contactIdToKeyMap = new HashMap<>();
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companySlugToKeyMap = new HashMap<>();

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "textFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Text Fields")
    public void textFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        JSONObject companyDataByContact = getCompaniesForContacts(data);
        logContactNameAndCompany(response, data, companyDataByContact, fieldName, dbField);
        validateTextFieldCrossEntityFilteredData(companyDataByContact, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    public void validateTextFieldCrossEntityFilteredData(JSONObject companyDataByContact, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(companyDataByContact.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && companyDataByContact.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String contactSlug : companyDataByContact.keySet()) {
            JSONArray contactAssociatedCompanies = companyDataByContact.getJSONArray(contactSlug);
            boolean atLeastOneMatch = false;
            if (filterType.equals("is_not") || filterType.equals("does_not_contain") || filterType.equals("is_empty")) {
                if (contactAssociatedCompanies.length() == 0) {
                    atLeastOneMatch = true;
                    continue;
                }
            }
            for (int i = 0; i < contactAssociatedCompanies.length(); i++) {
                JSONObject company = contactAssociatedCompanies.getJSONObject(i);
                boolean matches = validateTextFieldFilteredDataBoolean(company, filterType, filterValue, fieldName, dbField, expectedResult, "Company", "");
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No company matched the filter for contact still coming in the response: " + contactSlug + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    public JSONObject getCompaniesForContacts(JSONArray contactData) {
        JSONObject companyResult = new JSONObject();
        for (int i = 0; i < contactData.length(); i++) {
            JSONArray contactAssociatedCompanies = new JSONArray();
            JSONObject contact = contactData.getJSONObject(i);
            String contactSlug = contact.getString("slug");
            List<JsonPath> companyList = companyDataMap.get(contactSlug);
            if (companyList != null) {
                for (JsonPath companyJsonPath : companyList) {
                    Map<String, Object> companyMap = companyJsonPath.get("data.company");
                    JSONObject company = new JSONObject(companyMap);
                    contactAssociatedCompanies.put(company);
                }
            }
            companyResult.put(contactSlug, contactAssociatedCompanies);
        }
        return companyResult;
    }

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/contactCompany_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, Integer> companyIdMap = new HashMap<>();
        Map<String, String> companySlugMap = new HashMap<>();

        try {
            List<CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>>> createFutures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.supplyAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
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
                String companyKey = entry.getKey();
                String companySlug = entry.getValue().getKey();
                Integer companyId = entry.getValue().getValue();
                companyIdMap.put(companyKey, companyId);
                companySlugMap.put(companyKey, companySlug);
                companyKeyToSlugMap.put(companyKey, companySlug);
                companySlugToKeyMap.put(companySlug, companyKey);
            }

            // Linking child companies to parent companies according to payload
            List<CompletableFuture<Void>> linkFutures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        String parentCompanyKey = companyEntry.optString("parentCompany", null);
                        
                        if (parentCompanyKey != null && !parentCompanyKey.isEmpty() && !parentCompanyKey.equals("null")) {
                            String parentCompanySlug;
                            if (parentCompanyKey.startsWith("company") && companySlugMap.containsKey(parentCompanyKey)) {
                                parentCompanySlug = companySlugMap.get(parentCompanyKey);
                            } else {
                                return;
                            }
                            if (parentCompanySlug != null) {
                                String childCompanySlug = companySlugMap.get(companyKey);
                                List<String> childCompanySlugs = new ArrayList<>();
                                childCompanySlugs.add(childCompanySlug);
                                
                                Response linkResponse = allCrudFunctions.linkCompanyToParentCompany(albatrossURL, albatrossAuthToken, parentCompanySlug, childCompanySlugs);
                                Assert.assertEquals(linkResponse.getStatusCode(), 200, "Failed to link company " + companyKey + " to parent " + parentCompanyKey);
                            }
                        }
                    }, executor))
                    .collect(Collectors.toList());
            
            CompletableFuture.allOf(linkFutures.toArray(new CompletableFuture[0])).join();

            addContactsToCompanies(companyJson, companyIdMap);
        } finally {
            executor.shutdown();
        }
    }

    private void addContactsToCompanies(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        Map<String, List<String>> contactSlugToCompanySlugsMap = new HashMap<>();
        
        for (int i = 1; i <= 10; i++) {
            Response response = allCrudFunctions.createContact(albatrossURL, albatrossAuthToken);
            String contactSlug = response.jsonPath().getString("data.contact.slug");
            Integer contactId = response.jsonPath().getInt("data.contact.id");
            String contactKey = "contact" + i;
            contactKeyToSlugMap.put(contactKey, contactSlug);
            contactIdToKeyMap.put(String.valueOf(contactId), contactKey);
            contactKeyToIdMap.put(contactKey, String.valueOf(contactId));
            contactSlugToKeyMap.put(contactSlug, contactKey);
            contactSlugToCompanySlugsMap.put(contactSlug, new ArrayList<>());
        }

        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            String contact = companyEntry.optString("contact", "").trim();
            if (contact.isEmpty()) {
                continue;
            }
            String[] contactKeys = contact.split(",");
            String contactSlugs = Arrays.stream(contactKeys)
                    .map(String::trim)
                    .map(contactKeyToSlugMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            if (contactSlugs.isEmpty()) {
                continue;
            }

            Integer companyId = companyIdMap.get(companyKey);
            String companySlug = companyKeyToSlugMap.get(companyKey);
            
            UpdateFields updateFields = new UpdateFields();
            updateFields.setKey("contactid");
            updateFields.setValue(contactSlugs);
            updateFields.setTableFlag("company");
            updateFields.setId(Collections.singletonList(companyId));
            updateFields.setAddInValues(true);
            Response linkResponse = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null, true, updateFields);
            linkResponse.then().statusCode(200);
            
            for (String contactKey : contactKeys) {
                String contactSlug = contactKeyToSlugMap.get(contactKey.trim());
                if (contactSlug != null && companySlug != null) {
                    contactSlugToCompanySlugsMap.get(contactSlug).add(companySlug);
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : contactSlugToCompanySlugsMap.entrySet()) {
            String contactSlug = entry.getKey();
            List<String> companySlugs = entry.getValue();
            if (!companySlugs.isEmpty()) {
                List<JsonPath> companyList = new ArrayList<>();
                for (String companySlug : companySlugs) {
                    Response companyResponse = getCompany(companySlug);
                    JsonPath companyJsonPath = companyResponse.jsonPath();
                    companyList.add(companyJsonPath);
                }
                synchronized (companyDataMap) {
                    companyDataMap.put(contactSlug, companyList);
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

    public Response getCompany(String companySlug) {
        String basePath = "/companies/{companySlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companySlug", companySlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    private void logContactNameAndCompany(Response response, JSONArray contactData, JSONObject companyDataByContact, String fieldName, String dbField) {
        FilterSearchReporter.logInfo("<b>📋 Contact - Company Information:</b>");

        if (contactData != null && companyDataByContact != null) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>");
            logMessage.append("<code>");

            for (int i = 0; i < contactData.length(); i++) {
                JSONObject contact = contactData.getJSONObject(i);
                String contactSlug = contact.getString("slug");
                JSONArray associatedCompanies = companyDataByContact.optJSONArray(contactSlug);

                String firstName = contact.optString("firstname", "").trim();
                String lastName = contact.optString("lastname", "").trim();
                String contactName = (firstName + " " + lastName).trim();
                if (contactName.isEmpty()) {
                    contactName = "Unknown";
                }
                logMessage.append("Contact: ").append(contactName).append("\n");

                if (associatedCompanies != null && associatedCompanies.length() > 0) {
                    for (int j = 0; j < associatedCompanies.length(); j++) {
                        JSONObject company = associatedCompanies.getJSONObject(j);
                        String companyName = company.optString("companyname", "N/A");
                        String companyFieldValue = company.optString(dbField, "").trim();
                        if (companyFieldValue.isEmpty()) {
                            companyFieldValue = "N/A";
                        }
                        logMessage.append("  Company ").append(j + 1).append(": ").append(companyName);
                        logMessage.append(" | ").append(fieldName).append(": ").append(companyFieldValue).append("\n");
                    }
                } else {
                    logMessage.append("  No companies found\n");
                }
                logMessage.append("\n");
            }

            logMessage.append("</code></pre>");
            FilterSearchReporter.logInfo(logMessage.toString());
        }
    }

    @DataProvider(name = "textFieldCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactCompanyTextTypeFilterDataProvider.json");
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
        payload.put("advancedSearchContext", "CONTACT");
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
}
