package io.recruitcrm.ContactService.Filters;

import com.qa.api.util.reaper.ReaperIntegration;
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
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class CompanyNumberFieldCrossEntityContactFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String email;
    Map<String, List<JsonPath>> companyDataMap = new HashMap<>();
    Map<String, String> contactSlugToKeyMap = new HashMap<>();
    Map<String, String> contactKeyToSlugMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();
    Map<String, String> contactIdToKeyMap = new HashMap<>();
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companySlugToKeyMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "companyNumberFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Company Number Fields Cross Entity Contact")
    public void companyNumberFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        JSONObject companyDataByContact = getCompaniesForContacts(data);
        logContactNameAndCompany(response, data, companyDataByContact, fieldName, dbField);
        validateNumberFieldCrossEntityFilteredData(companyDataByContact, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    public void validateNumberFieldCrossEntityFilteredData(JSONObject companyDataByContact, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(companyDataByContact.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && companyDataByContact.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String contactSlug : companyDataByContact.keySet()) {
            JSONArray contactAssociatedCompanies = companyDataByContact.getJSONArray(contactSlug);
            boolean atLeastOneMatch = false;
            for (int i = 0; i < contactAssociatedCompanies.length(); i++) {
                JSONObject company = contactAssociatedCompanies.getJSONObject(i);
                String companyNumber = String.valueOf(company.opt(dbField)).trim();
                if (companyNumber.equals("null") || companyNumber.isEmpty()) {
                    if (filterType.equals("is_empty")) {
                        atLeastOneMatch = true;
                        break;
                    }
                    continue;
                }
                
                if (companyNumber.endsWith(".0")) {
                    companyNumber = companyNumber.substring(0, companyNumber.length() - 2);
                }
                
                boolean matches = validateNumberFieldFilteredDataBoolean(companyNumber, filterType, filterValue, fieldName);
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

    private boolean validateNumberFieldFilteredDataBoolean(String companyNumber, String filterType, String filterValue, String fieldName) {
        try {
            double companyNumberValue = Double.parseDouble(companyNumber);
            if (filterType.equals("is_between")) {
                String[] rangeParts = filterValue.split(",");
                double startValue = Double.parseDouble(rangeParts[0].trim());
                double endValue = Double.parseDouble(rangeParts[1].trim());
                return companyNumberValue >= startValue && companyNumberValue <= endValue;
            }

            if (filterType.equals("ends_with")) {
                return companyNumber.endsWith(filterValue);
            }

            if (filterType.equals("begins_with")) {
                return companyNumber.startsWith(filterValue);
            }

            if (filterType.equals("has_any_value")) {
                return !companyNumber.isEmpty() && !companyNumber.equals("null");
            }
            if (filterType.equals("is_empty")) {
                return companyNumber.isEmpty() || companyNumber.equals("null") || companyNumber.equals("0");
            }

            double filterValueDouble = Double.parseDouble(filterValue.trim());
            
            switch (filterType) {
                case "is":
                    return companyNumberValue == filterValueDouble;
                case "is_not":
                    return companyNumberValue != filterValueDouble;
                case "is_mt":
                    return companyNumberValue > filterValueDouble;
                case "is_lt":
                    return companyNumberValue < filterValueDouble;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
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
                        
                        String createdBy = companyEntry.optString("createdBy", "owner");
                        String authToken = getAlbatrossAuthToken(createdBy);

                        //Adding thread.sleep for avoiding 401 errors.
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, authToken, payload);


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

            List<CompletableFuture<Void>> updateJobCountFutures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        String companySlug = companySlugMap.get(companyKey);
                        
                        if (companySlug != null) {
                            int companyId = companyIdMap.get(companyKey);
                            
                            JSONObject fieldsAndValues = new JSONObject();
                            if (companyEntry.has("totalonholdjob")) {
                                fieldsAndValues.put("totalonholdjob", companyEntry.getInt("totalonholdjob"));
                            }
                            if (companyEntry.has("totalcanceledjob")) {
                                fieldsAndValues.put("totalcanceledjob", companyEntry.getInt("totalcanceledjob"));
                            }
                            if (companyEntry.has("totalclosedjob")) {
                                fieldsAndValues.put("totalclosedjob", companyEntry.getInt("totalclosedjob"));
                            }
                            if (companyEntry.has("totalopenjob")) {
                                fieldsAndValues.put("totalopenjob", companyEntry.getInt("totalopenjob"));
                            }
                            
                            if (fieldsAndValues.length() > 0) {
                                ReaperIntegration.updateCompanyFields(companyId, fieldsAndValues);
                            }
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(updateJobCountFutures.toArray(new CompletableFuture[0])).join();

            addContactsToCompanies(companyJson, companyIdMap);
        } finally {
            executor.shutdown();
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

    private void addContactsToCompanies(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        Map<String, List<String>> contactSlugToCompanySlugsMap = new HashMap<>();
        
        for (int i = 1; i <= 10; i++) {
            Response response = allCrudFunctions.createContact(albatrossURL, ownerAlbatrossAuthToken);
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
            Response linkResponse = RestClient.doPost("JSON", albatrossURL, "global/update-fields", ownerAlbatrossAuthToken, null, true, updateFields);
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
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, ownerAlbatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    public Response getCompany(String companySlug) {
        String basePath = "/companies/{companySlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companySlug", companySlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, ownerAlbatrossAuthToken, null, pathParams, true);
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
                        String companyFieldValue = String.valueOf(company.opt(dbField)).trim();
                        if (companyFieldValue.isEmpty() || companyFieldValue.equals("null")) {
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

    @DataProvider(name = "companyNumberFieldCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] companyNumberFieldCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactCompanyNumberTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String testCaseId = test.optString("testCaseId", "");
                testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), testCaseId});
            }
        }
        return testData.toArray(new Object[0][]);
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        JSONObject filterValueObj = new JSONObject();
        
        if (filterType.equals("is_between")) {
            filterValueObj.put("type", "INTEGER_START_END");
            JSONObject rangeValue = new JSONObject();
            String startValue = filterValue.split(",")[0].trim();
            String endValue = filterValue.split(",")[1].trim();
            rangeValue.put("start", Double.parseDouble(startValue));
            rangeValue.put("end", Double.parseDouble(endValue));
            filterValueObj.put("value", rangeValue);
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
