package io.recruitcrm.CandidateService.Filters;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.commanfunctions.commanFunction;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class NumberFieldFilterCandidateTest extends NumberFieldFilterBase {

    private Map<String, String> testDataMap;
    private commanFunction commonFunc;
    private List<Map<String, Integer>> candidatesList;
    private List<Map<String, Integer>> emptyFieldCandidates;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        authToken = ThreadManager.getOwnerAlbatrossToken();
        commonFunc = new commanFunction();
        testDataMap = new HashMap<>();
        candidatesList = new ArrayList<>();
        emptyFieldCandidates = new ArrayList<>();
        createTestData();
        waitForDataSync();
    }

    private void createTestData() {
        candidatesList.clear();
        emptyFieldCandidates.clear();
        for (int i = 0; i < 2; i++) {
            try {
                Response fullCandidateResponse = commonFunc.createNewCandidateWithAllFields(
                        baseURL, ThreadManager.getAccountApiKey());

                if (fullCandidateResponse == null || fullCandidateResponse.getStatusCode() != 200) {
                    continue;
                }

                JsonPath fullCandidateJson = fullCandidateResponse.jsonPath();
                int candidateAge;
                String candidateDob = fullCandidateJson.getString("candidate_dob");
                if (candidateDob != null) {
                    candidateAge = calculateAgeFromDateString(candidateDob);
                } else {
                    try {
                        candidateAge = fullCandidateJson.getInt("age");
                    } catch (Exception e) {
                        candidateAge = 18;
                        Assert.fail("Failed to get candidate age: " + e.getMessage());
                    }
                }
                int candidateNoticePeriod = getIntValueSafely(fullCandidateJson, "notice_period", "noticeperiod", 30);
                int candidateSalaryExpectation = getIntValueSafely(fullCandidateJson, "salary_expectation",
                        "salaryexpectation", 50000);
                int candidateCurrentSalary = getIntValueSafely(fullCandidateJson, "current_salary", "currentsalary",
                        40000);
                int candidateTotalExperience = getIntValueSafely(fullCandidateJson, "work_ex_year", "workexpyr", 5);
                int candidateRelevantExperience = getIntValueSafely(fullCandidateJson, "relevant_experience",
                        "relevantexperience", 3);
                int candidateId = getIntValueSafely(fullCandidateJson, "id", "srno", 1);

                Map<String, Integer> candidateData = new HashMap<>();
                candidateData.put("age", candidateAge);
                candidateData.put("noticeperiod", candidateNoticePeriod);
                candidateData.put("salaryexpectation", candidateSalaryExpectation);
                candidateData.put("currentsalary", candidateCurrentSalary);
                candidateData.put("workexpyr", candidateTotalExperience);
                candidateData.put("relevantexperience", candidateRelevantExperience);
                candidateData.put("srno", candidateId);
                candidateData.put("isEmpty", 0);

                candidatesList.add(candidateData);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail("Failed to create candidate with all fields: " + e.getMessage());
            }
        }

        try {
            Response emptyResponse = commonFunc.createNewCandidateWithEmptyFields(
                    baseURL, ThreadManager.getAccountApiKey());

            if (emptyResponse != null && emptyResponse.getStatusCode() == 200) {
                JsonPath emptyJson = emptyResponse.jsonPath();
                int candidateId = getIntValueSafely(emptyJson, "id", "srno", 0);

                Map<String, Integer> emptyData = new HashMap<>();
                emptyData.put("age", 0);
                emptyData.put("noticeperiod", 0);
                emptyData.put("salaryexpectation", 0);
                emptyData.put("currentsalary", 0);
                emptyData.put("workexpyr", 0);
                emptyData.put("relevantexperience", 0);
                emptyData.put("srno", candidateId);
                emptyData.put("isEmpty", 1);

                emptyFieldCandidates.add(emptyData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to create candidate with empty fields: " + e.getMessage());
        }
    }

    private int getIntValueSafely(JsonPath jsonPath, String primaryField, String fallbackField, int defaultValue) {
        try {
            Object value = jsonPath.get(primaryField);
            if (value != null) {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else {
                    // Try parsing as string
                    return Integer.parseInt(value.toString());
                }
            }
            value = jsonPath.get(fallbackField);
            if (value != null) {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else {
                    return Integer.parseInt(value.toString());
                }
            }
        } catch (Exception e) {
            Assert.fail("Failed to parse integer value for field " + primaryField + " or " + fallbackField + ": "
                    + e.getMessage());
        }

        return defaultValue;
    }

    private int calculateAgeFromDateString(String dateStr) {
        try {
            // Handle ISO format: 2009-06-02T18:22:56.000000Z
            if (dateStr.contains("T")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("T"));
            }

            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = dateStr.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1; // 0-based months
                int day = Integer.parseInt(parts[2]);

                java.util.Calendar dob = java.util.Calendar.getInstance();
                dob.set(year, month, day);

                java.util.Calendar today = java.util.Calendar.getInstance();

                int age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR);

                // Adjust age if birthday hasn't occurred yet this year
                if (today.get(java.util.Calendar.MONTH) < dob.get(java.util.Calendar.MONTH) ||
                        (today.get(java.util.Calendar.MONTH) == dob.get(java.util.Calendar.MONTH) &&
                                today.get(java.util.Calendar.DAY_OF_MONTH) < dob
                                        .get(java.util.Calendar.DAY_OF_MONTH))) {
                    age--;
                }

                return age;
            }
        } catch (Exception e) {
            Assert.fail("Failed to calculate age from date string: " + dateStr + " - " + e.getMessage());
            return 18;
        }

        return 18;
    }

    private Map<String, Integer> getCandidateForTest(String filterType, String dbField) {
        if (filterType.equals("is_empty")) {
            if (!emptyFieldCandidates.isEmpty()) {
                return emptyFieldCandidates.get(0);
            } else {
                Assert.fail("No candidate with empty fields available for testing is_empty filter");
                return null;
            }
        }

        if (!candidatesList.isEmpty()) {
            for (Map<String, Integer> candidate : candidatesList) {
                if (candidate.containsKey(dbField) && candidate.get(dbField) != null && candidate.get(dbField) > 0) {
                    return candidate;
                }
            }

            int randomIndex = (int) (Math.random() * candidatesList.size());
            return candidatesList.get(randomIndex);
        }

        Assert.fail("No candidate available for testing filter type: " + filterType + " for field: " + dbField);
        return null;
    }

    @DataProvider(name = "numberFilterCombinations", parallel = true)
    public Object[][] getFilterCombinations() {
        Map<String, FilterValidator> validatorMap = new HashMap<>();
        validatorMap.put("is", IS_VALIDATOR);
        validatorMap.put("is_not", IS_NOT_VALIDATOR);
        validatorMap.put("begins_with", BEGINS_WITH_VALIDATOR);
        validatorMap.put("ends_with", ENDS_WITH_VALIDATOR);
        validatorMap.put("has_any_value", HAS_ANY_VALUE_VALIDATOR);
        validatorMap.put("is_empty", IS_EMPTY_VALIDATOR);
        validatorMap.put("is_between", IS_BETWEEN_VALIDATOR);
        validatorMap.put("is_mt", IS_MORE_THAN_VALIDATOR);
        validatorMap.put("is_lt", IS_LESS_THAN_VALIDATOR);

        List<Object[]> combinations = new ArrayList<>();

        Object[][] fields = {
                // {groupType, filterName, dbField, displayName, allowedFilterTypes}
                { "candidates", "Age", "age", "Age", new String[] {
                        "is", "is_not", "begins_with", "ends_with", "has_any_value",
                        "is_empty", "is_between", "is_mt", "is_lt"
                } },
                { "candidates", "Notice Period", "noticeperiod", "Notice Period", new String[] {
                        "is", "is_not", "begins_with", "ends_with", "has_any_value", "is_between", "is_mt", "is_lt",
                        "is_empty"
                } },
                { "candidates", "Salary Expectation", "salaryexpectation", "Salary Expectation", new String[] {

                        "is", "is_not"
                } },
                { "candidates", "Current Salary", "currentsalary", "Current Salary", new String[] {

                        "is", "is_not", "begins_with", "ends_with", "has_any_value",
                        "is_empty", "is_between", "is_mt", "is_lt"
                } },
                { "candidates", "Total Experience", "workexpyr", "Total Experience", new String[] {
                        "is", "is_not", "begins_with", "ends_with", "has_any_value", "is_between", "is_mt", "is_lt",
                        "is_empty"
                } },
                { "candidates", "Relevant Experience", "relevantexperience", "Relevant Experience", new String[] {

                        "is", "is_not", "begins_with", "ends_with", "has_any_value", "is_between", "is_mt", "is_lt",
                        "is_empty"
                } },
                { "candidates", "ID", "srno", "ID", new String[] {

                        "is", "is_not", "begins_with", "ends_with", "has_any_value", "is_between", "is_mt", "is_lt"
                } }
        };

        for (Object[] field : fields) {
            String groupType = (String) field[0];
            String filterName = (String) field[1];
            String dbField = (String) field[2];
            String displayName = (String) field[3];
            String[] allowedFilterTypes = (String[]) field[4];

            for (String filterType : allowedFilterTypes) {
                FilterValidator validator = validatorMap.get(filterType);

                // Positive test case
                combinations.add(new Object[] {
                        groupType, filterName, dbField, displayName,
                        filterType, getFilterDescription(filterType), true,
                        isEmptyFilterExpectsEmptyResults(filterType),
                        validator
                });

                // Negative test case
                if (!filterType.equals("has_any_value") && !filterType.equals("is_empty")) {
                    combinations.add(new Object[] {
                            groupType, filterName, dbField, displayName,
                            filterType, getFilterDescription(filterType), false,
                            isEmptyFilterExpectsEmptyResults(filterType),
                            null
                    });
                }
            }
        }

        Assert.assertFalse(combinations.isEmpty(), "No filter combinations available for testing");
        return combinations.toArray(new Object[0][]);
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "numberFilterCombinations", groups = {"candidate_service", "nightly-build"})
    public void testNumberFieldFilterCandidate(
            String groupType,
            String filterName,
            String dbField,
            String displayName,
            String filterType,
            String filterDescription,
            boolean isPositiveTest,
            boolean expectEmptyForNegative,
            FilterValidator validator) throws IOException {

        try {
            Map<String, Integer> testCandidate = getCandidateForTest(filterType, dbField);
            if (testCandidate == null || !testCandidate.containsKey(dbField)) {
                Assert.fail("Could not find test candidate with required field: " + dbField);
                return;
            }

            int originalValue = testCandidate.get(dbField);

            String testValue;
            boolean expectResults;
            if (isPositiveTest && originalValue == 0 &&
                    !filterType.equals("is_empty") && !filterType.equals("has_any_value")) {
                Assert.fail(
                        "Skipping test: Cannot perform positive test with zero value for filter type: " + filterType);
                return;
            }
            if (isPositiveTest) {
                testValue = getModifiedValue(originalValue, filterType, dbField);
                expectResults = true;
            } else {
                int offset = (dbField.equals("currentsalary") || dbField.equals("salaryexpectation")) ? 100000 : 1000;
                testValue = (originalValue + offset) + "";
                expectResults = !expectEmptyForNegative;
            }

            NumberFieldConfig fieldConfig = new NumberFieldConfig(
                    groupType, filterName, dbField, displayName, originalValue);

            executeFilterTest(
                    fieldConfig,
                    filterType,
                    testValue,
                    expectResults,
                    validator);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(
                    "Test failed for filter type: " + filterType + " on field: " + dbField + " - " + e.getMessage());
            throw e;
        }
    }

    private String getFilterDescription(String filterType) {
        switch (filterType) {
            case "is":
                return "exact match";
            case "is_not":
                return "excluding exact match";
            case "begins_with":
                return "prefix match";
            case "ends_with":
                return "suffix match";
            case "has_any_value":
                return "non-empty values";
            case "is_empty":
                return "empty values";
            case "is_between":
                return "range match";
            case "is_mt":
            case "is_more_than":
                return "greater than match";
            case "is_lt":
            case "is_less_than":
                return "less than match";
            default:
                return "unknown filter";
        }
    }

    private boolean isEmptyFilterExpectsEmptyResults(String filterType) {
        return filterType.equals("is_empty");
    }
}