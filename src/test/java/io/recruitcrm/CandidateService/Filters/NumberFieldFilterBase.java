package io.recruitcrm.CandidateService.Filters;

import com.qa.api.util.FilterJsonUtils;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class NumberFieldFilterBase extends FilterSearchBaseTest {

    protected final String SEARCH_ENDPOINT = ADVANCED_SEARCH_CANDIDATES_GET_PATH_BASE;
    protected String authToken;

    public static class NumberFieldConfig {
        String groupType;
        String filterName;
        String dbField;
        String displayName;
        int sampleValue;

        public NumberFieldConfig(String groupType, String filterName, String dbField, String displayName,
                int sampleValue) {
            this.groupType = groupType;
            this.filterName = filterName;
            this.dbField = dbField;
            this.displayName = displayName;
            this.sampleValue = sampleValue;
        }
    }

    protected interface FilterValidator {
        void validate(List<Map<String, Object>> results, NumberFieldConfig fieldConfig, String filterValue,
                String filterType, boolean isPositiveTest);
    }

    protected final FilterValidator IS_VALIDATOR = (results, fieldConfig, filterValue, filterType, isPositiveTest) -> {
        if (isPositiveTest) {
            assertThat("Results should not be empty for 'is' filter with value " + filterValue,
                    results.isEmpty(), is(false));

            boolean foundMatch = false;
            int targetValue = parseIntFromDecimal(filterValue);
            for (Map<String, Object> result : results) {
                int retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                if (retrievedValue == targetValue) {
                    foundMatch = true;
                    break;
                }
            }
            assertThat("At least one result should have the exact value " + targetValue + " for 'is' filter",
                    foundMatch, is(true));
        }
    };

    protected final FilterValidator IS_NOT_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            assertThat("Results should not be empty for 'is_not' filter with value " + filterValue,
                    results.isEmpty(), is(false));

            int targetValue = parseIntFromDecimal(filterValue);
            for (Map<String, Object> result : results) {
                int retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                assertThat("Retrieved value should not match the excluded value for 'is_not'",
                        retrievedValue, not(equalTo(targetValue)));
            }
        }
    };

    protected final FilterValidator BEGINS_WITH_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            assertThat("Results should not be empty for 'begins_with' filter with value " + filterValue,
                    results.isEmpty(), is(false));

            boolean foundMatch = false;
            for (Map<String, Object> result : results) {
                int retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                String valueAsString = String.valueOf(retrievedValue);
                if (valueAsString.startsWith(filterValue)) {
                    foundMatch = true;
                    break;
                }
            }
            assertThat("At least one result should begin with '" + filterValue + "'", foundMatch, is(true));
        }
    };

    protected final FilterValidator ENDS_WITH_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            assertThat("Results should not be empty for 'ends_with' filter with value " + filterValue,
                    results.isEmpty(), is(false));

            boolean foundMatch = false;
            for (Map<String, Object> result : results) {
                int retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                String valueAsString = String.valueOf(retrievedValue);
                if (valueAsString.endsWith(filterValue)) {
                    foundMatch = true;
                    break;
                }
            }
            assertThat("At least one result should end with '" + filterValue + "'", foundMatch, is(true));
        }
    };

    protected final FilterValidator HAS_ANY_VALUE_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            assertThat("Results should not be empty for 'has_any_value' filter",
                    results.isEmpty(), is(false));

            boolean foundNonZero = false;
            for (Map<String, Object> result : results) {
                int retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                if (retrievedValue != 0) {
                    foundNonZero = true;
                    break;
                }
            }
            assertThat("At least one result should have a non-zero value for 'has_any_value' filter",
                    foundNonZero, is(true));
        }
    };

    protected final FilterValidator IS_EMPTY_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            assertThat("Results should not be empty for 'is_empty' filter",
                    results.isEmpty(), is(false));

            boolean allEmpty = true;
            for (Map<String, Object> result : results) {
                int retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                Object rawValue = result.get(fieldConfig.dbField);
                boolean isFieldEmpty = (rawValue == null) ||
                        (rawValue.toString().trim().isEmpty()) ||
                        (retrievedValue == 0);

                if (!isFieldEmpty) {
                    allEmpty = false;
                    break;
                }
            }
            assertThat("All returned results should have empty/null values for field: " + fieldConfig.dbField,
                    allEmpty, is(true));
        }
    };

    protected final FilterValidator IS_BETWEEN_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            assertThat("Results should not be empty for 'is_between' filter with value " + filterValue,
                    results.isEmpty(), is(false));

            int minValue;
            int maxValue;

            try {
                if (filterValue.contains("start") && filterValue.contains("end")) {
                    String cleanedJson = filterValue.replace("\\", "");

                    int startIdx = cleanedJson.indexOf("start") + 7;
                    int commaIdx = cleanedJson.indexOf(",", startIdx);
                    String startStr = cleanedJson.substring(startIdx, commaIdx).trim();
                    if (startStr.contains(".")) {
                        startStr = startStr.substring(0, startStr.indexOf("."));
                    }
                    minValue = Integer.parseInt(startStr);

                    int endIdx = cleanedJson.indexOf("end") + 5;
                    int closeBraceIdx = cleanedJson.indexOf("}", endIdx);
                    String endStr = cleanedJson.substring(endIdx, closeBraceIdx).trim();
                    if (endStr.contains(".")) {
                        endStr = endStr.substring(0, endStr.indexOf("."));
                    }
                    maxValue = Integer.parseInt(endStr);
                } else {
                    String[] range = filterValue.split(",");
                    String minStr = range[0].trim();
                    if (minStr.contains(".")) {
                        minStr = minStr.substring(0, minStr.indexOf("."));
                    }
                    minValue = Integer.parseInt(minStr);

                    String maxStr = range[1].trim();
                    if (maxStr.contains(".")) {
                        maxStr = maxStr.substring(0, maxStr.indexOf("."));
                    }
                    maxValue = Integer.parseInt(maxStr);
                }
            } catch (Exception e) {
                minValue = 0;
                maxValue = 1000;
            }

            boolean foundMatch = false;
            for (Map<String, Object> result : results) {
                int retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                if (retrievedValue >= minValue && retrievedValue <= maxValue) {
                    foundMatch = true;
                    break;
                }
            }
            assertThat("At least one result should be between " + minValue + " and " + maxValue, foundMatch, is(true));
        }
    };

    protected final FilterValidator IS_MORE_THAN_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            assertThat("Results should not be empty for 'is_mt' filter with value " + filterValue,
                    results.isEmpty(), is(false));

            int threshold;
            try {
                if (filterValue.contains(".")) {
                    threshold = (int) Double.parseDouble(filterValue);
                } else {
                    threshold = Integer.parseInt(filterValue);
                }
            } catch (NumberFormatException e) {
                threshold = 0;
            }

            boolean foundMatch = false;
            for (Map<String, Object> result : results) {
                int retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                if (retrievedValue > threshold) {
                    foundMatch = true;
                    break;
                }
            }
            assertThat("At least one result should be more than " + threshold, foundMatch, is(true));
        }
    };

    protected final FilterValidator IS_LESS_THAN_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            assertThat("Results should not be empty for 'is_lt' filter with value " + filterValue,
                    results.isEmpty(), is(false));

            int threshold;
            try {
                if (filterValue.contains(".")) {
                    threshold = (int) Double.parseDouble(filterValue);
                } else {
                    threshold = Integer.parseInt(filterValue);
                }
            } catch (NumberFormatException e) {
                threshold = 0;
            }

            boolean foundMatch = false;
            for (Map<String, Object> result : results) {
                int retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                if (retrievedValue < threshold) {
                    foundMatch = true;
                    break;
                }
            }
            assertThat("At least one result should be less than " + threshold, foundMatch, is(true));
        }
    };

    protected Response executeNumberFieldFilter(
            String groupType,
            String filterName,
            String dbField,
            String filterValue,
            String filterType) throws IOException {

        String filterBarLabel = filterValue;
        String processedFilterValue = filterValue;

        if (filterType.equals("is_between")) {
            try {
                int start, end;

                if (filterValue.contains("start") && filterValue.contains("end")) {
                    String cleanJson = filterValue.replace("\\", "").replace("\"", "");
                    int startPos = cleanJson.indexOf("start:") + 6;
                    if (startPos < 6)
                        startPos = cleanJson.indexOf("start=") + 6;
                    int commaPos = cleanJson.indexOf(",", startPos);

                    String startStr = cleanJson.substring(startPos, commaPos).trim();
                    if (startStr.contains(".")) {
                        startStr = startStr.substring(0, startStr.indexOf("."));
                    }
                    start = Integer.parseInt(startStr);

                    int endPos = cleanJson.indexOf("end:") + 4;
                    if (endPos < 4)
                        endPos = cleanJson.indexOf("end=") + 4;
                    int closePos = cleanJson.indexOf("}", endPos);

                    String endStr = cleanJson.substring(endPos, closePos).trim();
                    if (endStr.contains(".")) {
                        endStr = endStr.substring(0, endStr.indexOf("."));
                    }
                    end = Integer.parseInt(endStr);
                } else {
                    int value = Integer.parseInt(filterValue);
                    start = Math.max(0, value - 10);
                    end = value + 10;
                }

                filterBarLabel = start + " - " + end;
                processedFilterValue = "{\"start\":" + start + ".0,\"end\":" + end + ".0}";
            } catch (Exception e) {
            }
        }

        String jsonBody = FilterJsonUtils.createFilterJson(
                groupType, filterName, dbField, processedFilterValue, filterType, "number", filterBarLabel);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "100");
        queryParams.put("page", "1");

        return RestClient.doPost("JSON", ariesServiceURL, SEARCH_ENDPOINT,
                authToken, queryParams, true, jsonBody);
    }

    protected void executeFilterTest(
            NumberFieldConfig fieldConfig,
            String filterType,
            String modifiedValue,
            boolean expectResults,
            FilterValidator validator) throws IOException {

        Response response = executeNumberFieldFilter(
                fieldConfig.groupType,
                fieldConfig.filterName,
                fieldConfig.dbField,
                modifiedValue,
                filterType);

        int statusCode = response.getStatusCode();

        assertThat("Status code should be 200", statusCode, equalTo(200));

        List<Map<String, Object>> results = getResponseResults(response);

        if (validator != null) {
            validator.validate(results, fieldConfig, modifiedValue, filterType, expectResults);
        }
    }

    protected List<Map<String, Object>> getResponseResults(Response response) {
        JsonPath jsonPath = response.jsonPath();
        return jsonPath.getList("data");
    }

    protected int extractFieldValue(Map<String, Object> result, String dbField) {
        switch (dbField) {
            case "age":
                Object ageObj = result.get("age");
                if (ageObj != null) {
                    try {
                        return Integer.parseInt(ageObj.toString());
                    } catch (NumberFormatException e) {
                        // If not an integer, try to calculate from DOB
                    }
                }

                Object dobObj = result.get("candidatedob");
                if (dobObj != null) {
                    try {
                        long dobTimestamp = Long.parseLong(dobObj.toString());
                        return calculateAgeFromTimestamp(dobTimestamp);
                    } catch (NumberFormatException e) {
                        return calculateAgeFromDateString(dobObj.toString());
                    }
                }
                return 0;

            case "noticeperiod":
                Object noticeObj = result.get("noticeperiod");
                return (noticeObj != null) ? parseIntSafely(noticeObj.toString()) : 0;

            case "salaryexpectation":
                Object salaryExpObj = result.get("salaryexpectation");
                return (salaryExpObj != null) ? parseIntSafely(salaryExpObj.toString()) : 0;

            case "currentsalary":
                Object currentSalaryObj = result.get("currentsalary");
                return (currentSalaryObj != null) ? parseIntSafely(currentSalaryObj.toString()) : 0;

            case "workexpyr":
                Object workExpObj = result.get("workexpyr");
                if (workExpObj != null) {
                    return parseIntSafely(workExpObj.toString());
                }
                workExpObj = result.get("work_ex_year");
                return (workExpObj != null) ? parseIntSafely(workExpObj.toString()) : 0;

            case "relevantexperience":
                Object relExpObj = result.get("relevantexperience");
                if (relExpObj != null) {
                    return parseIntSafely(relExpObj.toString());
                }
                relExpObj = result.get("relevant_experience");
                return (relExpObj != null) ? parseIntSafely(relExpObj.toString()) : 0;

            case "srno":
                Object idObj = result.get("srno");
                if (idObj != null) {
                    return parseIntSafely(idObj.toString());
                }
                idObj = result.get("id");
                return (idObj != null) ? parseIntSafely(idObj.toString()) : 0;

            default:
                Object value = result.get(dbField);
                return (value != null) ? parseIntSafely(value.toString()) : 0;
        }
    }

    private int parseIntSafely(String value) {
        try {
            if (value.contains(".")) {
                return (int) Double.parseDouble(value);
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseIntFromDecimal(String value) {
        try {
            if (value.contains(".")) {
                return (int) Double.parseDouble(value);
            }

            if (value.startsWith("{") && value.contains("start") && value.contains("end")) {
                String cleanedJson = value.replace("\\", "");
                int startIdx = cleanedJson.indexOf("start") + 7;
                int dotIdx = cleanedJson.indexOf(".", startIdx);
                int commaIdx = cleanedJson.indexOf(",", startIdx);

                if (dotIdx > 0 && dotIdx < commaIdx) {
                    return Integer.parseInt(cleanedJson.substring(startIdx, dotIdx).trim());
                } else {
                    return Integer.parseInt(cleanedJson.substring(startIdx, commaIdx).trim());
                }
            }

            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int calculateAgeFromTimestamp(long timestamp) {
        if (timestamp < 31536000000L) {
            timestamp *= 1000;
        }

        long currentTime = System.currentTimeMillis();
        long ageInMillis = currentTime - timestamp;
        return (int) (ageInMillis / (365.25 * 24 * 60 * 60 * 1000));
    }

    private int calculateAgeFromDateString(String dateStr) {
        if (dateStr.contains("T")) {
            dateStr = dateStr.substring(0, dateStr.indexOf("T"));
        }

        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);

            java.util.Calendar dob = java.util.Calendar.getInstance();
            dob.set(year, month, day);

            java.util.Calendar today = java.util.Calendar.getInstance();

            int age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR);

            if (today.get(java.util.Calendar.MONTH) < dob.get(java.util.Calendar.MONTH) ||
                    (today.get(java.util.Calendar.MONTH) == dob.get(java.util.Calendar.MONTH) &&
                            today.get(java.util.Calendar.DAY_OF_MONTH) < dob.get(java.util.Calendar.DAY_OF_MONTH))) {
                age--;
            }

            return age;
        }

        return 0;
    }

    protected String getModifiedValueForSalary(int originalValue, String filterType) {
        switch (filterType) {
            case "is":
            case "is_not":
                return originalValue + ".0";
            case "begins_with":
                String origStr = String.valueOf(originalValue);
                return origStr.length() > 0 ? origStr.substring(0, 1) : "1";
            case "ends_with":
                String origEndStr = String.valueOf(originalValue);
                return origEndStr.length() > 0 ? origEndStr.substring(origEndStr.length() - 1) : "0";
            case "has_any_value":
                return "0";
            case "is_empty":
                return "";
            case "is_between":
                int lowerBound = Math.max(0, originalValue - 1000);
                int upperBound = originalValue + 1000;
                return "{\"start\":" + lowerBound + ".0,\"end\":" + upperBound + ".0}";
            case "is_mt":
            case "is_more_than":
                return Math.max(0, originalValue - 1000) + ".0";
            case "is_lt":
            case "is_less_than":
                return (originalValue + 1000) + ".0";
            default:
                return originalValue + ".0";
        }
    }

    protected String getModifiedValue(int originalValue, String filterType, String dbField) {
        if (dbField.equals("currentsalary") || dbField.equals("salaryexpectation")) {
            return getModifiedValueForSalary(originalValue, filterType);
        }

        switch (filterType) {
            case "is":
            case "is_not":
                return String.valueOf(originalValue);
            case "begins_with":
                String origStr = String.valueOf(originalValue);
                return origStr.length() > 0 ? origStr.substring(0, 1) : "1";
            case "ends_with":
                String origEndStr = String.valueOf(originalValue);
                return origEndStr.length() > 0 ? origEndStr.substring(origEndStr.length() - 1) : "0";
            case "has_any_value":
                return "0";
            case "is_empty":
                return "";
            case "is_between":
                int lowerBound = Math.max(0, originalValue - 10);
                int upperBound = originalValue + 10;
                return "{\"start\":" + lowerBound + ",\"end\":" + upperBound + "}";
            case "is_mt":
            case "is_more_than":
                return String.valueOf(Math.max(0, originalValue - 1));
            case "is_lt":
            case "is_less_than":
                return String.valueOf(originalValue + 1);
            default:
                return String.valueOf(originalValue);
        }
    }

}