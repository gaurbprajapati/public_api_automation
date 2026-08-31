package io.recruitcrm.CandidateService.Filters;

import com.qa.api.util.FilterJsonUtils;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class TextFieldFilterBase extends FilterSearchBaseTest {

    protected final String SEARCH_ENDPOINT = ADVANCED_SEARCH_CANDIDATES_GET_PATH_BASE;
    protected String authToken;

    public static class TextFieldConfig {
        String groupType;
        String filterName;
        String dbField;
        String displayName;
        String sampleValue;

        public TextFieldConfig(String groupType, String filterName, String dbField, String displayName,
                String sampleValue) {
            this.groupType = groupType;
            this.filterName = filterName;
            this.dbField = dbField;
            this.displayName = displayName;
            this.sampleValue = sampleValue;
        }
    }

    protected interface FilterValidator {
        void validate(List<Map<String, Object>> results, TextFieldConfig fieldConfig, String filterValue,
                String filterType, boolean isPositiveTest);
    }

    protected final FilterValidator IS_VALIDATOR = (results, fieldConfig, filterValue, filterType, isPositiveTest) -> {
        if (isPositiveTest) {
            if (!results.isEmpty()) {
                Map<String, Object> firstResult = results.get(0);
                String retrievedValue = extractFieldValue(firstResult, fieldConfig.dbField);
                assertThat("Retrieved value should match the filter value for 'is'",
                        retrievedValue, equalToIgnoringCase(filterValue));
            }
        }
    };

    protected final FilterValidator IS_NOT_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            for (Map<String, Object> result : results) {
                String retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                assertThat("Retrieved value should not match the excluded value for 'is_not'",
                        retrievedValue, not(equalToIgnoringCase(filterValue)));
            }
        }
    };

    protected final FilterValidator CONTAINS_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            boolean foundMatch = false;
            for (Map<String, Object> result : results) {
                String retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                if (retrievedValue.toLowerCase().contains(filterValue.toLowerCase())) {
                    foundMatch = true;
                    break;
                }
            }
            assertThat("At least one result should contain the filter value", foundMatch, is(true));
        }
    };

    protected final FilterValidator DOES_NOT_CONTAIN_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            for (Map<String, Object> result : results) {
                String retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                assertThat("Retrieved value should not contain the filter value for 'does_not_contain'",
                        retrievedValue.toLowerCase(), not(containsString(filterValue.toLowerCase())));
            }
        }
    };

    protected final FilterValidator CONTAINS_EXACT_WORD_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            boolean foundMatch = false;
            filterValue = filterValue.replaceAll("(^\\s*,+|,+\\s*$)", "").trim();
            String[] searchValues = filterValue.toLowerCase().split("\\s*,\\s*");

            for (Map<String, Object> result : results) {
                String retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                if (retrievedValue == null)
                    continue;
                retrievedValue = retrievedValue.toLowerCase().trim();

                for (String searchValue : searchValues) {
                    searchValue = searchValue.trim();
                    if (fieldConfig.dbField.equals("skill")) {
                        // Special handling for skills as they are comma-separated
                        String[] fieldValues = retrievedValue.split("\\s*,\\s*");
                        for (String fieldVal : fieldValues) {
                            fieldVal = fieldVal.trim();
                            if (fieldVal.equals(searchValue) ||
                                    fieldVal.matches(".*\\b" + Pattern.quote(searchValue) + "\\b.*")) {
                                foundMatch = true;
                                break;
                            }
                        }
                    } else {
                        // Standard word boundary check
                        if (retrievedValue.matches(".*\\b" + Pattern.quote(searchValue) + "\\b.*")) {
                            foundMatch = true;
                            break;
                        }
                    }
                    if (foundMatch)
                        break;
                }
                if (foundMatch)
                    break;
            }
            assertThat("Exact word match not found for: " + filterValue, foundMatch, is(true));
        }
    };

    protected final FilterValidator BEGINS_WITH_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            boolean foundMatch = false;
            for (Map<String, Object> result : results) {
                String retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                if (retrievedValue.toLowerCase().startsWith(filterValue.toLowerCase())) {
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
            boolean foundMatch = false;
            for (Map<String, Object> result : results) {
                String retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                if (retrievedValue.toLowerCase().endsWith(filterValue.toLowerCase())) {
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
            for (Map<String, Object> result : results) {
                String retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                assertThat("Value should not be empty for 'has_any_value' filter",
                        retrievedValue, not(isEmptyOrNullString()));
            }
        }
    };

    protected final FilterValidator IS_EMPTY_VALIDATOR = (results, fieldConfig, filterValue, filterType,
            isPositiveTest) -> {
        if (isPositiveTest) {
            for (Map<String, Object> result : results) {
                String retrievedValue = extractFieldValue(result, fieldConfig.dbField);
                assertThat("Value should be empty for 'is_empty' filter",
                        retrievedValue, isEmptyOrNullString());
            }
        }
    };

    protected Response executeTextFieldFilter(
            String groupType,
            String filterName,
            String dbField,
            String filterValue,
            String filterType) throws IOException {

        String jsonBody = FilterJsonUtils.createFilterJson(
                groupType, filterName, dbField, filterValue, filterType, "text", filterValue);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", "100");
        queryParams.put("page", "1");

        Response response = RestClient.doPost("JSON", ariesServiceURL, SEARCH_ENDPOINT,
                authToken, queryParams, true, jsonBody);

        return response;
    }


    protected void executeFilterTest(
            TextFieldConfig fieldConfig,
            String filterType,
            String modifiedValue,
            boolean expectResults,
            FilterValidator validator) throws IOException {

        Response response = executeTextFieldFilter(
                fieldConfig.groupType,
                fieldConfig.filterName,
                fieldConfig.dbField,
                modifiedValue,
                filterType);

        assertThat("Status code should be 200", response.getStatusCode(), equalTo(200));

        List<Map<String, Object>> results = getResponseResults(response);

        if (validator != null) {
            // Pass isPositiveTest to the validator
            validator.validate(results, fieldConfig, modifiedValue, filterType, expectResults);
        }
    }

    protected List<Map<String, Object>> getResponseResults(Response response) {
        JsonPath jsonPath = response.jsonPath();
        return jsonPath.getList("data");
    }

    protected String extractFieldValue(Map<String, Object> result, String dbField) {
        switch (dbField) {
            case "candidatename":
                String firstName = (String) result.get("firstname");
                String lastName = (String) result.get("lastname");
                return (firstName + " " + lastName).trim();
            case "skill":
                return (String) result.get("skill");
            case "city":
                return (String) result.get("city");
            case "state":
                return (String) result.get("state");
            case "country":
                return (String) result.get("country");
            case "locality":
                return (String) result.get("locality");
            case "profilefacebook":
                return (String) result.get("profilefacebook");
            case "lastorganisation":
                return (String) result.get("lastorganisation");
            case "profilelinkedin":
                return (String) result.get("profilelinkedin");
            case "source":
                return (String) result.get("source");
            case "profiletwitter":
                return (String) result.get("profiletwitter");
            case "emailid":
                return (String) result.get("emailid");
            case "profilexing":
                return (String) result.get("profilexing");
            case "profilegithub":
                return (String) result.get("profilegithub");
            case "contactnumber":
                return (String) result.get("contactnumber");
            case "position":
                return (String) result.get("position");
            case "currentstatus":
                return (String) result.get("currentstatus");
            default:
                Object value = result.get(dbField);
                return value != null ? value.toString() : "";
        }
    }

    protected String getModifiedValue(String originalValue, String filterType) {
        if (originalValue == null || originalValue.trim().isEmpty()) {
            return "DEFAULT_TEST_VALUE";
        }

        switch (filterType) {
            case "is":
                return originalValue;
            case "is_not":
                return originalValue;
            case "contains":
                return originalValue.length() > 2 ? originalValue.substring(0, Math.min(3, originalValue.length()))
                        : originalValue;
            case "does_not_contain":
                return "@$@@$" + System.currentTimeMillis();
            case "contains_exact_word":
                // For skills field, extract the complete first skill item
                if (originalValue.contains(",")) {
                    // Get the first complete skill item before any comma
                    return originalValue.split(",")[0].trim();
                } else {
                    return originalValue;
                }
            case "begins_with":
                return originalValue.substring(0, Math.min(2, originalValue.length()));
            case "ends_with":
                return originalValue.substring(Math.max(0, originalValue.length() - 2));
            case "has_any_value":
            case "is_empty":
                return "";
            default:
                return originalValue;
        }
    }
}