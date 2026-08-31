package io.recruitcrm.contractStaffing.RuleEngine;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.TestUtil;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class RuleTemplateSortingTests extends RuleEngineBaseTest {

    private List<Integer> createdTemplateIds = new ArrayList<>();
    private String albatrossAuthToken;

    @BeforeClass(alwaysRun = true)
    public void setup() throws InterruptedException {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();

        String templateName1 = generateUniqueTemplateName("A_SortTest");
        Integer templateId1 = createTemplateForSorting(albatrossAuthToken, templateName1);
        if (templateId1 != null) {
            createdTemplateIds.add(templateId1);
        }

        String templateName2 = generateUniqueTemplateName("Z_SortTest");
        Integer templateId2 = createTemplateForSorting(albatrossAuthToken, templateName2);
        if (templateId2 != null) {
            createdTemplateIds.add(templateId2);
        }

        String templateName3 = generateUniqueTemplateName("M_SortTest");
        Integer templateId3 = createTemplateForSorting(albatrossAuthToken, templateName3);
        if (templateId3 != null) {
            createdTemplateIds.add(templateId3);
        }
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "sortingScenarios", groups = {"contract_staffing", "nightly-build"})
    public void verifyRuleTemplateSortingTest(String scenario, String sortField, String sortOrder, String search,
            int page, int size, int expectedStatus, String expectedMessage) {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search", search != null ? search : "");
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> sortPriorityList = new ArrayList<>();

        switch (scenario) {
            case "EmptyPayload":
                requestBody = new HashMap<>();
                break;

            case "NullSortPriorityList":
                requestBody.put("sortPriorityList", null);
                break;

            case "EmptySortPriorityList":
                requestBody.put("sortPriorityList", new ArrayList<>());
                break;

            case "InvalidField":
            case "InvalidOrder":
            case "NullField":
            case "EmptyField":
                Map<String, Object> sortItem = new HashMap<>();
                if (scenario.equals("NullField")) {
                    sortItem.put("field", null);
                } else if (scenario.equals("EmptyField")) {
                    sortItem.put("field", "");
                } else {
                    sortItem.put("field", sortField);
                }

                if (scenario.equals("InvalidOrder")) {
                    sortItem.put("order", sortOrder);
                } else {
                    sortItem.put("order", sortOrder != null ? sortOrder : "asc");
                }
                sortPriorityList.add(sortItem);
                requestBody.put("sortPriorityList", sortPriorityList);
                break;

            default:
                if (sortField != null && sortOrder != null) {
                    Map<String, Object> validSortItem = new HashMap<>();
                    validSortItem.put("field", sortField);
                    validSortItem.put("order", sortOrder);
                    sortPriorityList.add(validSortItem);
                    requestBody.put("sortPriorityList", sortPriorityList);
                } else {
                    requestBody.put("sortPriorityList", new ArrayList<>());
                }
                break;
        }

        String tokenToUse = scenario.equals("UnauthorizedAccess") ? "invalid_token_123" : albatrossAuthToken;
        Response response = executePostWithQueryParams("rule-engine/rule-template/get", tokenToUse, queryParams,
                requestBody);

        if (scenario.equals("UnauthorizedAccess")) {
            validateUnauthorizedResponse(response);
        } else if (expectedStatus == 200) {
            validateSuccessResponse(response, expectedMessage);

            JsonPath jsonPath = response.jsonPath();
            List<Map<String, Object>> data = jsonPath.getList("data");
            assertThat("Response should have data array", data, notNullValue());
            assertThat("Data should be a list", data, instanceOf(List.class));

            if (sortField != null && sortOrder != null && !scenario.contains("Invalid") && !scenario.contains("Null")
                    && !scenario.contains("Empty")) {
                List<Map<String, Object>> unsortedList = getTemplatesWithoutSorting(albatrossAuthToken, search, page,
                        size);

                if (!data.isEmpty() && !unsortedList.isEmpty()) {
                    verifySortingOrder(unsortedList, data, sortField, sortOrder);
                }
            }
        } else {
            assertThat("Response status should match expected", response.getStatusCode(), equalTo(expectedStatus));
        }
    }

    @Owner("Gaurav Prajapati")
    @Test
    public void verifySortingChangesOrderTest() {
        List<Map<String, Object>> unsortedList = getTemplatesWithoutSorting(albatrossAuthToken, "", 1, 100);

        List<Map<String, Object>> sortedByNameAsc = getTemplatesWithSorting(albatrossAuthToken, "", 1, 100, "templateName",
                "asc");

        List<Map<String, Object>> sortedByNameDesc = getTemplatesWithSorting(albatrossAuthToken, "", 1, 100,
                "templateName", "desc");

        assertThat("Unsorted list should not be empty", unsortedList, not(empty()));
        assertThat("Sorted by name asc should not be empty", sortedByNameAsc, not(empty()));
        assertThat("Sorted by name desc should not be empty", sortedByNameDesc, not(empty()));

        verifySortingOrder(unsortedList, sortedByNameAsc, "templateName", "asc");
        verifySortingOrder(unsortedList, sortedByNameDesc, "templateName", "desc");

        List<Object> ascNames = extractFieldValues(sortedByNameAsc, "templateName");
        List<Object> descNames = extractFieldValues(sortedByNameDesc, "templateName");

        if (!areAllValuesEqual(ascNames) && !areAllValuesEqual(descNames)) {
            Collections.reverse(descNames);
            assertThat("Ascending and descending orders should be reverse of each other",
                    ascNames, equalTo(descNames));
        }
    }

    @DataProvider(name = "sortingScenarios", parallel = true)
    public Object[][] getSortingScenarios() {
        return new Object[][] {
                { "SortByNameAsc", "templateName", "asc", "", 1, 100, 200, "fetched successfully" },
                { "SortByNameDesc", "templateName", "desc", "", 1, 100, 200, "fetched successfully" },
                { "SortByAddedOnAsc", "addedOn", "asc", "", 1, 100, 200, "fetched successfully" },
                { "SortByAddedOnDesc", "addedOn", "desc", "", 1, 100, 200, "fetched successfully" },
                { "SortByUpdatedOnAsc", "updatedOn", "asc", "", 1, 100, 200, "fetched successfully" },
                { "SortByUpdatedOnDesc", "updatedOn", "desc", "", 1, 100, 200, "fetched successfully" },
                { "SortByNameAscWithSearch", "templateName", "asc", "SortTest", 1, 100, 200, "fetched successfully" },
                { "UnauthorizedAccess", "templateName", "asc", "", 1, 100, 401, "Unauthorised access" },
        };
    }

    private Integer createTemplateForSorting(String authToken, String templateName) throws InterruptedException {
        Map<String, Object> templatePayload = buildTemplatePayload(
                templateName,
                SHIFTS_METHOD,
                false,
                Arrays.asList(1, 2, 3, 4, 5),
                new ArrayList<>()
        );

        Response createResponse = executePost("rule-engine/rule-template", authToken, templatePayload);

        if (createResponse.getStatusCode() != 200 && createResponse.getStatusCode() != 201) {
            return null;
        }

        Integer templateId = findTemplateByNameFromList(authToken, templateName);
        return templateId;
    }

    private Response executePostWithQueryParams(String endpoint, String authToken,
            Map<String, String> queryParams, Object payload) {
        Object requestPayload = payload;
        if (payload instanceof Map) {
            requestPayload = TestUtil.getSerializedJSON(payload);
        }

        return RestClient.doPost("JSON", timesheetBaseURL, endpoint, authToken, queryParams, true, requestPayload);
    }

    private List<Map<String, Object>> getTemplatesWithoutSorting(String authToken, String search, int page, int size) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search", search != null ? search : "");
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> emptyBody = new HashMap<>();
        emptyBody.put("sortPriorityList", new ArrayList<>());

        Response response = executePostWithQueryParams("rule-engine/rule-template/get", authToken, queryParams,
                emptyBody);

        if (response.getStatusCode() == 200) {
            JsonPath jsonPath = response.jsonPath();
            return jsonPath.getList("data");
        }
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getTemplatesWithSorting(String authToken, String search, int page, int size,
            String sortField, String sortOrder) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search", search != null ? search : "");
        queryParams.put("page", String.valueOf(page));
        queryParams.put("size", String.valueOf(size));

        Map<String, Object> sortItem = new HashMap<>();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);

        List<Map<String, Object>> sortPriorityList = new ArrayList<>();
        sortPriorityList.add(sortItem);

        Map<String, Object> body = new HashMap<>();
        body.put("sortPriorityList", sortPriorityList);

        Response response = executePostWithQueryParams("rule-engine/rule-template/get", authToken, queryParams, body);

        if (response.getStatusCode() == 200) {
            JsonPath jsonPath = response.jsonPath();
            return jsonPath.getList("data");
        }
        return new ArrayList<>();
    }

    private void verifySortingOrder(List<Map<String, Object>> unsortedList, List<Map<String, Object>> sortedList,
            String sortField, String sortOrder) {
        assertThat("Sorted list should not be empty", sortedList, not(empty()));
        assertThat("Unsorted list should not be empty", unsortedList, not(empty()));

        List<Object> sortedValues = extractFieldValues(sortedList, sortField);

        if (!areAllValuesEqual(sortedValues)) {
            if (sortOrder.equalsIgnoreCase("asc")) {
                verifyAscendingOrder(sortedValues, sortField);
            } else if (sortOrder.equalsIgnoreCase("desc")) {
                verifyDescendingOrder(sortedValues, sortField);
            }
        }
    }

    private List<Object> extractFieldValues(List<Map<String, Object>> templates, String field) {
        return templates.stream()
                .map(template -> {
                    if ("templateName".equals(field)) {
                        return template.get("templateName");
                    } else if ("addedOn".equals(field)) {
                        return template.get("addedOn");
                    } else if ("updatedOn".equals(field)) {
                        return template.get("updatedOn");
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean areAllValuesEqual(List<Object> values) {
        if (values.isEmpty() || values.size() == 1) {
            return true;
        }
        Object first = values.get(0);
        return values.stream().allMatch(v -> Objects.equals(v, first));
    }

    private void verifyAscendingOrder(List<Object> values, String field) {
        for (int i = 0; i < values.size() - 1; i++) {
            Object current = values.get(i);
            Object next = values.get(i + 1);

            if (current != null && next != null) {
                if (field.equals("templateName")) {
                    String currentStr = (String) current;
                    String nextStr = (String) next;
                    assertThat("Template names should be in ascending order",
                            currentStr.compareToIgnoreCase(nextStr), lessThanOrEqualTo(0));
                } else if (field.equals("addedOn") || field.equals("updatedOn")) {
                    Integer currentInt = (Integer) current;
                    Integer nextInt = (Integer) next;
                    assertThat("Timestamps should be in ascending order", currentInt, lessThanOrEqualTo(nextInt));
                }
            }
        }
    }

    private void verifyDescendingOrder(List<Object> values, String field) {
        for (int i = 0; i < values.size() - 1; i++) {
            Object current = values.get(i);
            Object next = values.get(i + 1);

            if (current != null && next != null) {
                if (field.equals("templateName")) {
                    String currentStr = (String) current;
                    String nextStr = (String) next;
                    assertThat("Template names should be in descending order",
                            currentStr.compareToIgnoreCase(nextStr), greaterThanOrEqualTo(0));
                } else if (field.equals("addedOn") || field.equals("updatedOn")) {
                    Integer currentInt = (Integer) current;
                    Integer nextInt = (Integer) next;
                    assertThat("Timestamps should be in descending order", currentInt, greaterThanOrEqualTo(nextInt));
                }
            }
        }
    }
}
