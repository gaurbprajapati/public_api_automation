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
public class TextFieldFilterCandidateTest extends TextFieldFilterBase {

    private Map<String, String> testDataMap;
    private commanFunction commonFunc;
    private List<Map<String, String>> candidatesList;
    private List<Map<String, String>> emptyFieldCandidates;

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
                    Assert.fail("Failed to create candidate with all fields: " +
                            (fullCandidateResponse != null ? fullCandidateResponse.getStatusCode() : "null response"));
                } else {
                    JsonPath fullCandidateJson = fullCandidateResponse.jsonPath();

                    String candidateFirstName = fullCandidateJson.getString("first_name");
                    String candidateLastName = fullCandidateJson.getString("last_name");
                    String candidateCity = fullCandidateJson.getString("city");
                    String candidateCountry = fullCandidateJson.getString("country");
                    String candidateSkill = fullCandidateJson.getString("skill");
                    String candidateState = fullCandidateJson.getString("state");

                    String candidateLocality = fullCandidateJson.getString("locality");
                    String candidateFacebook = fullCandidateJson.getString("facebook");
                    String candidateLastOrg = fullCandidateJson.getString("current_organization");
                    String candidateLinkedin = fullCandidateJson.getString("linkedin");
                    String candidateSource = fullCandidateJson.getString("source");
                    String candidateTwitter = fullCandidateJson.getString("twitter");
                    String candidateEmail = fullCandidateJson.getString("email");
                    String candidateXing = fullCandidateJson.getString("xing");
                    String candidateGithub = fullCandidateJson.getString("github");
                    String candidatePhone = fullCandidateJson.getString("contact_number");
                    String candidateLanguage = fullCandidateJson.getString("language_skills");
                    String candidateTitle = fullCandidateJson.getString("position");

                    String candidateSummary = fullCandidateJson.getString("candidate_summary");
                    if (candidateSummary != null && candidateSummary.contains("<")) {
                        candidateSummary = stripHtmlTags(candidateSummary);
                    }

                    String candidateEmploymentStatus = fullCandidateJson.getString("current_status");

                    Map<String, String> candidateData = new HashMap<>();
                    candidateData.put("firstName", candidateFirstName);
                    candidateData.put("lastName", candidateLastName);
                    candidateData.put("city", candidateCity != null ? candidateCity : "");
                    candidateData.put("state", candidateState != null ? candidateState : "");
                    candidateData.put("country", candidateCountry != null ? candidateCountry : "");
                    candidateData.put("skill", candidateSkill != null ? candidateSkill : "");

                    candidateData.put("locality", candidateLocality != null ? candidateLocality : "");
                    candidateData.put("profilefacebook", candidateFacebook != null ? candidateFacebook : "");
                    candidateData.put("lastorganisation", candidateLastOrg != null ? candidateLastOrg : "");
                    candidateData.put("profilelinkedin", candidateLinkedin != null ? candidateLinkedin : "");
                    candidateData.put("source", candidateSource != null ? candidateSource : "");
                    candidateData.put("profiletwitter", candidateTwitter != null ? candidateTwitter : "");
                    candidateData.put("emailid", candidateEmail != null ? candidateEmail : "");
                    candidateData.put("profilexing", candidateXing != null ? candidateXing : "");
                    candidateData.put("profilegithub", candidateGithub != null ? candidateGithub : "");
                    candidateData.put("contactnumber", candidatePhone != null ? candidatePhone : "");
                    candidateData.put("languageskills", candidateLanguage != null ? candidateLanguage : "");
                    candidateData.put("position", candidateTitle != null ? candidateTitle : "");
                    candidateData.put("summary", candidateSummary != null ? candidateSummary : "");
                    candidateData.put("currentstatus", candidateEmploymentStatus != null ? candidateEmploymentStatus : "");
                    String candidateName = (candidateFirstName + " " + candidateLastName).trim();
                    candidateData.put("candidatename", candidateName);
                    candidateData.put("isEmpty", "false");

                    candidatesList.add(candidateData);
                }
            } catch (Exception e) {
                Assert.fail("Exception occurred while creating candidate with all fields: " + e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            Response emptyResponse = commonFunc.createNewCandidateWithEmptyFields(
                    baseURL, ThreadManager.getAccountApiKey());

            if (emptyResponse != null && emptyResponse.getStatusCode() == 200) {
                JsonPath emptyJson = emptyResponse.jsonPath();

                String firstName = emptyJson.getString("first_name");
                String lastName = emptyJson.getString("last_name");

                if (firstName == null)
                    firstName = "EmptyFirst";
                if (lastName == null)
                    lastName = "EmptyLast";

                Map<String, String> emptyData = new HashMap<>();
                emptyData.put("firstName", firstName);
                emptyData.put("lastName", lastName);
                emptyData.put("candidatename", (firstName + " " + lastName).trim());
                emptyData.put("city", "");
                emptyData.put("state", "");
                emptyData.put("country", "");
                emptyData.put("skill", "");
                emptyData.put("locality", "");
                emptyData.put("profilefacebook", "");
                emptyData.put("lastorganisation", "");
                emptyData.put("profilelinkedin", "");
                emptyData.put("source", "");
                emptyData.put("profiletwitter", "");
                emptyData.put("emailid", "");
                emptyData.put("profilexing", "");
                emptyData.put("profilegithub", "");
                emptyData.put("contactnumber", "");
                emptyData.put("languageskills", "");
                emptyData.put("position", "");
                emptyData.put("summary", "");
                emptyData.put("currentstatus", "");

                emptyData.put("isEmpty", "true");

                emptyFieldCandidates.add(emptyData);
            } else {
                Assert.fail("Failed to create candidate with empty fields: " +
                        (emptyResponse != null ? emptyResponse.getStatusCode() : "null response"));
            }
        } catch (Exception e) {
            Assert.fail("Exception occurred while creating candidate with empty fields: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String stripHtmlTags(String html) {
        if (html == null) {
            return "";
        }

        String stripped = html.replaceAll("<!DOCTYPE[^>]*>", "")
                .replaceAll("<html[^>]*>", "")
                .replaceAll("</html>", "")
                .replaceAll("<body[^>]*>", "")
                .replaceAll("</body>", "");

        stripped = stripped.replaceAll("<[^>]*>", "");

        stripped = stripped.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");

        stripped = stripped.trim().replaceAll("\\s+", " ");

        return stripped;
    }

    private Map<String, String> getCandidateForTest(String filterType, String dbField) {
        if (filterType.equals("is_empty")) {
            if (!emptyFieldCandidates.isEmpty()) {
                return emptyFieldCandidates.get(0);
            } else {
                Assert.fail("No empty field candidates available for testing is_empty filter");
                return null;
            }
        }

        if (!candidatesList.isEmpty()) {
            if (dbField.equals("skill") && filterType.equals("contains_exact_word")) {
                for (Map<String, String> candidate : candidatesList) {
                    String skillValue = candidate.get(dbField);
                    if (skillValue != null &&
                            !skillValue.isEmpty() &&
                            (skillValue.contains(" ") || skillValue.contains(","))) {
                        return candidate;
                    }
                }
            }

            for (Map<String, String> candidate : candidatesList) {
                if (candidate.containsKey(dbField) &&
                        candidate.get(dbField) != null &&
                        !candidate.get(dbField).trim().isEmpty()) {
                    return candidate;
                }
            }

            int randomIndex = (int) (Math.random() * candidatesList.size());
            return candidatesList.get(randomIndex);
        }

        Assert.fail("No candidates available for testing");
        return null;
    }

    @DataProvider(name = "filterCombinations", parallel = true)
    public Object[][] getFilterCombinations() {
        Map<String, FilterValidator> validatorMap = new HashMap<>();
        validatorMap.put("is", IS_VALIDATOR);
        validatorMap.put("is_not", IS_NOT_VALIDATOR);
        validatorMap.put("contains", CONTAINS_VALIDATOR);
        validatorMap.put("does_not_contain", DOES_NOT_CONTAIN_VALIDATOR);
        validatorMap.put("contains_exact_word", CONTAINS_EXACT_WORD_VALIDATOR);
        validatorMap.put("begins_with", BEGINS_WITH_VALIDATOR);
        validatorMap.put("ends_with", ENDS_WITH_VALIDATOR);
        validatorMap.put("has_any_value", HAS_ANY_VALUE_VALIDATOR);
        validatorMap.put("is_empty", IS_EMPTY_VALIDATOR);

        List<Object[]> combinations = new ArrayList<>();

        Object[][] fields = {
                { "candidates", "Name", "candidatename", "Candidate Name", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with", "has_any_value"
                } },
                { "candidates", "Skills", "skill", "Skills", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "City", "city", "City", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "State", "state", "State", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Country", "country", "Country", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Locality", "locality", "Locality", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Facebook Profile", "profilefacebook", "Facebook Profile", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Current Organization", "lastorganisation", "Current Organization", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Linkedin Profile", "profilelinkedin", "Linkedin Profile", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Source", "source", "Source", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Twitter Profile", "profiletwitter", "Twitter Profile", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Email", "emailid", "Email", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Xing Profile", "profilexing", "Xing Profile", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Github Profile", "profilegithub", "Github Profile", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Phone", "contactnumber", "Phone", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Title", "position", "Title", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
                } },
                { "candidates", "Employment Status", "currentstatus", "Employment Status", new String[] {
                        "is", "is_not", "contains", "does_not_contain",
                        "contains_exact_word", "begins_with", "ends_with",
                        "has_any_value", "is_empty"
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

                combinations.add(new Object[] {
                        groupType, filterName, dbField, displayName,
                        filterType, getFilterDescription(filterType), true,
                        isEmptyFilterExpectsEmptyResults(filterType),
                        validator
                });

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

        return combinations.toArray(new Object[0][]);
    }

    @Owner("Rahul Shibu")
    @Test(dataProvider = "filterCombinations", groups = {"candidate_service", "nightly-build"})
    public void testTextFieldFilterCandidate(
            String groupType,
            String filterName,
            String dbField,
            String displayName,
            String filterType,
            String filterDescription,
            boolean isPositiveTest,
            boolean expectEmptyForNegative,
            FilterValidator validator) throws IOException {

        Map<String, String> testCandidate = getCandidateForTest(filterType, dbField);
        if (testCandidate == null || !testCandidate.containsKey(dbField)) {
            Assert.fail("Test candidate is null or missing required field: " + dbField);
            return;
        }

        String originalValue = testCandidate.get(dbField);
        String testValue;
        boolean expectResults;

        if (isPositiveTest && (originalValue == null || originalValue.trim().isEmpty()) &&
                !filterType.equals("is_empty") && !filterType.equals("has_any_value")) {
            Assert.fail("Cannot perform positive test for " + filterType + " with empty value in field: " + dbField);
            return;
        }

        if (isPositiveTest) {
            testValue = getModifiedValue(originalValue, filterType);
            expectResults = true;
        } else {
            testValue = originalValue + "_UNMATCHED_" + System.currentTimeMillis();
            expectResults = !expectEmptyForNegative;
        }

        TextFieldConfig fieldConfig = new TextFieldConfig(
                groupType, filterName, dbField, displayName, originalValue);

        executeFilterTest(
                fieldConfig,
                filterType,
                testValue,
                expectResults,
                validator);
    }

    private String getFilterDescription(String filterType) {
        switch (filterType) {
            case "is":
                return "exact match";
            case "is_not":
                return "excluding exact match";
            case "contains":
                return "partial match";
            case "does_not_contain":
                return "excluding partial match";
            case "contains_exact_word":
                return "exact word match";
            case "begins_with":
                return "prefix match";
            case "ends_with":
                return "suffix match";
            case "has_any_value":
                return "non-empty values";
            case "is_empty":
                return "empty values";
            default:
                return "unknown filter";
        }
    }

    private boolean isEmptyFilterExpectsEmptyResults(String filterType) {
        return filterType.equals("is_empty");
    }

    @Override
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
                if (originalValue.contains(",")) {
                    String firstItem = originalValue.split(",")[0].trim();
                    return firstItem;
                } else if (originalValue.contains(" ")) {
                    return originalValue.split("\\s+")[0].trim();
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