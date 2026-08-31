package io.rcrm.api.customfieldparser;

import com.qa.api.util.S3Uploader;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.DefaultOptionsValue;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AccountType("Business|AlbatrossTkn|aiTestParser")
public class CandidateCustomFieldsValidation extends TestBase {

    private static String albatrossAuthToken;
    private static String apiAuthToken;
    private static Map<Integer, Integer> columnToFieldIdMap = new LinkedHashMap<>();
    private static JSONObject groundTruth;
    private static JSONArray testCasesArray;

    private static final String RESUME_DIR = System.getProperty("user.dir")
            + "/src/test/resources/testData/custom_field_parsing_agent/resumes/";

    private static final String GROUND_TRUTH_PATH = System.getProperty("user.dir")
            + "/src/test/resources/testData/custom_field_parsing_agent/ground_truth.json";

    private static final List<Map<String, Object>> CUSTOM_FIELDS = buildCustomFields();

    private static List<Map<String, Object>> buildCustomFields() {
        List<Map<String, Object>> fields = new ArrayList<>();

        Map<String, Object> f1 = new LinkedHashMap<>();
        f1.put("column_index", 1);
        f1.put("name", "Preferred Contact Window");
        f1.put("type", "text");
        f1.put("description",
                "Rough sense of when this person is usually reachable for a recruiter conversation, "
                        + "without treating it as a rigid appointment.");
        f1.put("options", Collections.emptyList());
        fields.add(f1);

        Map<String, Object> f2 = new LinkedHashMap<>();
        f2.put("column_index", 2);
        f2.put("name", "Career Objective Statement");
        f2.put("type", "longtext");
        f2.put("description",
                "A concise statement of what they want next in their career, written for a profile "
                        + "sidebar rather than a full narrative bio.");
        f2.put("options", Collections.emptyList());
        fields.add(f2);

        Map<String, Object> f3 = new LinkedHashMap<>();
        f3.put("column_index", 3);
        f3.put("name", "Professional Certification Expiry");
        f3.put("type", "date");
        f3.put("description",
                "The calendar day after which their primary professional credential is no longer "
                        + "considered current for compliance or hiring checks.");
        f3.put("options", Collections.emptyList());
        fields.add(f3);

        Map<String, Object> f4 = new LinkedHashMap<>();
        f4.put("column_index", 4);
        f4.put("name", "Open to Relocation");
        f4.put("type", "checkbox");
        f4.put("description",
                "Whether they would consider a longer-term move for the right role, as opposed to "
                        + "occasional travel only.");
        f4.put("options", Collections.emptyList());
        fields.add(f4);

        Map<String, Object> f5 = new LinkedHashMap<>();
        f5.put("column_index", 5);
        f5.put("name", "Preferred Employment Type");
        f5.put("type", "dropdown");
        f5.put("description",
                "The engagement style they are targeting from a payroll and commitment standpoint.");
        f5.put("options", Arrays.asList("Full-time", "Part-time", "Contract", "Internship", "Freelance"));
        fields.add(f5);

        Map<String, Object> f6 = new LinkedHashMap<>();
        f6.put("column_index", 6);
        f6.put("name", "Acceptable Work Arrangements");
        f6.put("type", "multiselect");
        f6.put("description",
                "The workplace presence patterns they can honestly commit to, which may combine "
                        + "several modes at once.");
        f6.put("options", Arrays.asList("Remote", "Hybrid", "On-site", "Weekly travel", "Client-site"));
        fields.add(f6);

        Map<String, Object> f7 = new LinkedHashMap<>();
        f7.put("column_index", 7);
        f7.put("name", "Alternate Contact Phone");
        f7.put("type", "phonenumber");
        f7.put("description",
                "Another voice line recruiters can try if the main number is tied up or goes unanswered.");
        f7.put("options", Collections.emptyList());
        fields.add(f7);

        Map<String, Object> f8 = new LinkedHashMap<>();
        f8.put("column_index", 8);
        f8.put("name", "Secondary Professional Email");
        f8.put("type", "email");
        f8.put("description",
                "An inbox used for professional correspondence that should be treated separately from "
                        + "whatever appears most prominently at the top of the resume.");
        f8.put("options", Collections.emptyList());
        fields.add(f8);

        Map<String, Object> f9 = new LinkedHashMap<>();
        f9.put("column_index", 9);
        f9.put("name", "Portfolio Website URL");
        f9.put("type", "social_profile");
        f9.put("description",
                "Where they point people who want proof of work beyond employer names and titles.");
        f9.put("options", Collections.emptyList());
        fields.add(f9);

        Map<String, Object> f10 = new LinkedHashMap<>();
        f10.put("column_index", 10);
        f10.put("name", "Expected Annual Salary");
        f10.put("type", "number");
        f10.put("description",
                "The total yearly compensation they have in mind when evaluating new opportunities.");
        f10.put("options", Collections.emptyList());
        fields.add(f10);

        return fields;
    }

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        groundTruth = readJsonFileFromPath(GROUND_TRUTH_PATH);
        testCasesArray = groundTruth.getJSONArray("test_cases");

        for (Map<String, Object> fieldDef : CUSTOM_FIELDS) {
            int columnIndex = (int) fieldDef.get("column_index");
            String name = (String) fieldDef.get("name");
            String type = (String) fieldDef.get("type");
            String description = (String) fieldDef.get("description");
            @SuppressWarnings("unchecked")
            List<String> options = (List<String>) fieldDef.get("options");

            Response response = createCustomField(albatrossAuthToken, columnIndex, name, type, description, options);
            Assert.assertEquals(response.getStatusCode(), 200,
                    "Failed to create custom field '" + name + "' | Status: " + response.getStatusCode());

            int fieldId = response.jsonPath().getInt("data.custumField.id");
            columnToFieldIdMap.put(columnIndex, fieldId);
        }
    }

    @Test
    public void validateResumeParsingGroundTruth() {
        for (int i = 0; i < testCasesArray.length(); i++) {
            JSONObject testCase = testCasesArray.getJSONObject(i);
            String tcId = testCase.getString("tc_id");
            String resumeFileName = testCase.getString("resume_file");
            String assertContext = "[" + tcId + "] ";
            JSONObject standardFields = testCase.getJSONObject("standard_fields");
            JSONObject customFieldsData = testCase.getJSONObject("custom_fields");
            JSONObject assertHints = testCase.getJSONObject("assert_hints");

            File pdfFile = new File(RESUME_DIR + resumeFileName);
            Assert.assertTrue(pdfFile.exists() && pdfFile.isFile(),
                    assertContext + "Resume file not found: " + pdfFile.getAbsolutePath());

            Map<String, String> presignedParams = new HashMap<>();
            presignedParams.put("fileName", pdfFile.getName());
            presignedParams.put("requestType", "put");

            Response presignedResp = RestClient.doGet("JSON", albatrossURL, "get-presigned-url",
                    albatrossAuthToken, presignedParams, null, false);
            Assert.assertEquals(presignedResp.getStatusCode(), 200,
                    assertContext + "get-presigned-url failed | Status: " + presignedResp.getStatusCode());

            String presignedUrl = presignedResp.jsonPath().getString("data.preSignedUrl");
            String s3Key = presignedResp.jsonPath().getString("data.key");
            Assert.assertNotNull(presignedUrl, assertContext + "preSignedUrl must not be null");
            Assert.assertNotNull(s3Key, assertContext + "S3 key must not be null");

            try {
                S3Uploader.uploadFileToS3(presignedUrl, pdfFile.getAbsolutePath());
            } catch (IOException e) {
                Assert.fail(assertContext + "S3 upload failed: " + e.getMessage());
            }

            JSONObject filesInfo = new JSONObject();
            filesInfo.put("key", s3Key);
            filesInfo.put("name", pdfFile.getName());
            filesInfo.put("type", "application/pdf");
            filesInfo.put("size", pdfFile.length());
            filesInfo.put("index", 0);

            JSONObject resumeParserData = new JSONObject();
            resumeParserData.put("resumesParsed", 0);
            resumeParserData.put("resumesFailed", 0);
            resumeParserData.put("resumesTotal", 1);
            resumeParserData.put("filesInfo", filesInfo);

            JSONObject parseResumeRequest = new JSONObject();
            parseResumeRequest.put("resumeParserData", resumeParserData);
            parseResumeRequest.put("actionid", 0);

            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("actionsteps", "1");

            Response parseResp = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                    albatrossAuthToken, queryParams, null, false, parseResumeRequest);

            Assert.assertEquals(parseResp.getStatusCode(), 200,
                    assertContext + "parse-resume returned status: " + parseResp.getStatusCode());

            JsonPath jp = parseResp.jsonPath();
            Map<String, Object> candidateMap = jp.getMap("data.candidate");
            JSONObject candidate = new JSONObject(candidateMap);

            validateStandardFields(candidate, standardFields, assertContext, resumeFileName);
            validateCustomFields(candidate, customFieldsData, assertHints, assertContext, resumeFileName);

            String candidateId = candidate.optString("id");
            if (candidateId != null && !candidateId.isEmpty()) {
                validateWorkHistory(candidateId, assertContext);
                validateEducationHistory(candidateId, assertContext);
            }
        }
    }

    @Test
    public void validatePublicAPIResumeParser() {
        JSONObject testCase = testCasesArray.getJSONObject(0);
        String tcId = testCase.getString("tc_id");
        String resumeFileName = testCase.getString("resume_file");
        String assertContext = "[" + tcId + "] [PublicAPI] ";
        JSONObject standardFields = testCase.getJSONObject("standard_fields");
        JSONObject customFieldsData = testCase.getJSONObject("custom_fields");
        JSONObject assertHints = testCase.getJSONObject("assert_hints");

        File resumeFile = new File(RESUME_DIR + resumeFileName);
        Assert.assertTrue(resumeFile.exists(), "Resume file not found: " + resumeFile.getAbsolutePath());

        Map<String, String> formParams = new HashMap<>();
        formParams.put("override_data", "");

        Response response = RestClient.doPostMultipart(baseURL, "/candidates/resume-parser",
                apiAuthToken, resumeFile, "file", "application/pdf", formParams, true);

        Assert.assertEquals(response.getStatusCode(), 200);

        JsonPath jp = response.jsonPath();
        Map<String, Object> candidateMap = jp.getMap("candidate");
        JSONObject candidate = new JSONObject(candidateMap);

        validateStandardFieldsPublicAPI(candidate, standardFields, assertContext, resumeFileName);
        validateCustomFieldsPublicAPI(candidate, customFieldsData, assertHints, assertContext, resumeFileName);

        JSONArray workHistory = candidate.optJSONArray("work_history");
        Assert.assertNotNull(workHistory, assertContext + "Work history must not be null");

        JSONArray educationHistory = candidate.optJSONArray("education_history");
        Assert.assertNotNull(educationHistory, assertContext + "Education history must not be null");
    }

    @Test
    public void validatePublicAPICreateCandidateWithResume() {
        JSONObject testCase = testCasesArray.getJSONObject(0);
        String tcId = testCase.getString("tc_id");
        String resumeFileName = testCase.getString("resume_file");
        String assertContext = "[" + tcId + "] [PublicAPI-CreateCandidate] ";
        JSONObject standardFields = testCase.getJSONObject("standard_fields");
        JSONObject customFieldsData = testCase.getJSONObject("custom_fields");
        JSONObject assertHints = testCase.getJSONObject("assert_hints");

        File resumeFile = new File(RESUME_DIR + resumeFileName);
        Assert.assertTrue(resumeFile.exists(), "Resume file not found: " + resumeFile.getAbsolutePath());

        Map<String, String> formParams = new HashMap<>();
        formParams.put("first_name", "TestCandidate" + System.currentTimeMillis());

        Response createResponse = RestClient.doPostMultipart(baseURL, "/candidates",
                apiAuthToken, resumeFile, "resume", "application/pdf", formParams, true);

        Assert.assertEquals(createResponse.getStatusCode(), 200,
                assertContext + "Create candidate failed | Status: " + createResponse.getStatusCode());

        JsonPath createJp = createResponse.jsonPath();
        String candidateSlug = createJp.getString("slug");
        Assert.assertNotNull(candidateSlug, assertContext + "Candidate slug must not be null");

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("slug", candidateSlug);

        // Public API processes requests through a queue, so we poll until custom fields are populated
        Response getResponse = null;
        long timeout = System.currentTimeMillis() + 60_000;
        while (System.currentTimeMillis() < timeout) {
            getResponse = RestClient.doGet("JSON", baseURL, "/candidates/{slug}", apiAuthToken, null, pathParams, true);
            List<Map<String, Object>> customFields = getResponse.jsonPath().getList("custom_fields");
            boolean hasValues = customFields != null && customFields.stream().anyMatch(f -> f.get("value") != null);
            if (hasValues) break;
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }

        Assert.assertEquals(getResponse.getStatusCode(), 200,
                assertContext + "Get candidate failed | Status: " + getResponse.getStatusCode());

        JsonPath jp = getResponse.jsonPath();
        Map<String, Object> candidateMap = jp.getMap("$");
        JSONObject candidate = new JSONObject(candidateMap);

        validateStandardFieldsPublicAPI(candidate, standardFields, assertContext, resumeFileName);
        validateCustomFieldsPublicAPI(candidate, customFieldsData, assertHints, assertContext, resumeFileName);

        JSONArray workHistory = candidate.optJSONArray("work_history");
        Assert.assertNotNull(workHistory, assertContext + "Work history must not be null");

        JSONArray educationHistory = candidate.optJSONArray("education_history");
        Assert.assertNotNull(educationHistory, assertContext + "Education history must not be null");
    }

    @Test
    public void validateAlbatrossParseResumeWithCustomFields() {
        JSONObject testCase = testCasesArray.getJSONObject(0);
        String tcId = testCase.getString("tc_id");
        String resumeFileName = testCase.getString("resume_file");
        String assertContext = "[" + tcId + "] [Albatross-ParseOnly] ";
        JSONObject standardFields = testCase.getJSONObject("standard_fields");
        JSONObject customFieldsData = testCase.getJSONObject("custom_fields");
        JSONObject assertHints = testCase.getJSONObject("assert_hints");

        File resumeFile = new File(RESUME_DIR + resumeFileName);
        Assert.assertTrue(resumeFile.exists(), "Resume file not found: " + resumeFile.getAbsolutePath());

        Map<String, String> presignedParams = new HashMap<>();
        presignedParams.put("fileName", resumeFile.getName());
        presignedParams.put("requestType", "put");

        Response presignedResp = RestClient.doGet("JSON", albatrossURL, "get-presigned-url",
                albatrossAuthToken, presignedParams, null, false);
        Assert.assertEquals(presignedResp.getStatusCode(), 200,
                assertContext + "get-presigned-url failed | Status: " + presignedResp.getStatusCode());

        String presignedUrl = presignedResp.jsonPath().getString("data.preSignedUrl");
        String s3Key = presignedResp.jsonPath().getString("data.key");
        Assert.assertNotNull(presignedUrl, assertContext + "preSignedUrl must not be null");
        Assert.assertNotNull(s3Key, assertContext + "S3 key must not be null");

        try {
            S3Uploader.uploadFileToS3(presignedUrl, resumeFile.getAbsolutePath());
        } catch (IOException e) {
            Assert.fail(assertContext + "S3 upload failed: " + e.getMessage());
        }

        JSONObject filesInfo = new JSONObject();
        filesInfo.put("key", s3Key);
        filesInfo.put("name", resumeFileName);

        JSONObject resumeParserData = new JSONObject();
        resumeParserData.put("filesInfo", filesInfo);

        JSONObject parseResumeRequest = new JSONObject();
        parseResumeRequest.put("resumeParserData", resumeParserData);
        parseResumeRequest.put("onlyParserData", true);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("actionsteps", "1");

        Response parseResp = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                albatrossAuthToken, queryParams, null, false, parseResumeRequest);

        Assert.assertEquals(parseResp.getStatusCode(), 200,
                assertContext + "parse-resume returned status: " + parseResp.getStatusCode());

        JsonPath jp = parseResp.jsonPath();
        Map<String, Object> candidateMap = jp.getMap("data.candidate");
        JSONObject candidate = new JSONObject(candidateMap);

        validateStandardFields(candidate, standardFields, assertContext, resumeFileName);

        Map<String, Object> customFieldsMap = jp.getMap("data.custom_fields");
        if (customFieldsMap != null) {
            JSONObject customFieldsResponse = new JSONObject(customFieldsMap);
            validateCustomFieldsAlbatrossInline(customFieldsResponse, customFieldsData, assertHints, 
                    assertContext, resumeFileName);
        } else {
            Assert.fail(assertContext + "custom_fields not found in response");
        }

        JSONArray workHistory = candidate.optJSONArray("workhistory");
        Assert.assertNotNull(workHistory, assertContext + "Work history must not be null");
        Assert.assertFalse(workHistory.isEmpty(), assertContext + "Work history must not be empty");

        JSONArray educationHistory = candidate.optJSONArray("educationhistory");
        Assert.assertNotNull(educationHistory, assertContext + "Education history must not be null");
        Assert.assertFalse(educationHistory.isEmpty(), assertContext + "Education history must not be empty");
    }

    private Response createCustomField(String token, int columnIndex, String name, String type,
            String description, List<String> options) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        ExtraField ef = new ExtraField();
        ef.setEntitytypeid(5);
        ef.setExtrafieldname(name);
        ef.setExtrafieldtype(type);
        ef.setColumnid(columnIndex);
        ef.setDescription(description);
        ef.setIs_parser_enabled(Boolean.TRUE);

        if (options != null && !options.isEmpty()) {
            List<DefaultOptionsValue> defaultOptions = new ArrayList<>();
            for (String opt : options) {
                DefaultOptionsValue dov = new DefaultOptionsValue();
                dov.setLabel(opt);
                defaultOptions.add(dov);
            }
            ef.setDefaultoptionsvalue(defaultOptions);
        }

        CustomFieldAlbatross cf = new CustomFieldAlbatross();
        cf.setCustumField(ef);

        return RestClient.doPost("JSON", albatrossURL, "custom-fields", headers, null, false, cf);
    }

    private void validateStandardFields(JSONObject candidate, JSONObject expected, String context, String fileName) {
        for (String key : expected.keySet()) {
            Object expectedValue = expected.get(key);
            if (expectedValue instanceof JSONObject) {
                continue;
            }

            String actual = candidate.optString(key, "");

            if (key.equals("firstname") || key.equals("lastname")) {
                Assert.assertNotNull(actual, context + fileName + " | Field '" + key + "' must not be null");
                Assert.assertFalse(actual.isEmpty(), context + fileName + " | Field '" + key + "' must not be empty");
            } else if (key.equals("emailid")) {
                Assert.assertNotNull(actual, context + fileName + " | Field 'emailid' must not be null");
            } else if (key.equals("contactnumber")) {
                String normalizedExpected = normalizePhone(String.valueOf(expectedValue));
                String normalizedActual = normalizePhone(actual);
                Assert.assertTrue(normalizedActual.contains(normalizedExpected),
                        context + fileName + " | Standard field 'contactnumber' | Expected (contains): "
                                + expectedValue + " | Actual: " + actual);
            } else if (key.equals("resumetext")) {
                Assert.assertNotNull(actual, context + fileName + " | Field 'resumetext' must not be null");
                Assert.assertFalse(actual.trim().isEmpty(),
                        context + fileName + " | Field 'resumetext' must not be empty");
            }
        }
    }

    private void validateStandardFieldsPublicAPI(JSONObject candidate, JSONObject expected, String context,
            String fileName) {
        String firstName = candidate.optString("first_name");
        String lastName = candidate.optString("last_name");
        String email = candidate.optString("email");
        String contactNumber = candidate.optString("contact_number");
        String resumeText = candidate.optString("candidate_summary", "");

        Assert.assertNotNull(firstName, context + fileName + " | Field 'first_name' must not be null");
        Assert.assertFalse(firstName.isEmpty(), context + fileName + " | Field 'first_name' must not be empty");

        Assert.assertNotNull(lastName, context + fileName + " | Field 'last_name' must not be null");
        Assert.assertFalse(lastName.isEmpty(), context + fileName + " | Field 'last_name' must not be empty");

        Assert.assertNotNull(email, context + fileName + " | Field 'email' must not be null");

        Assert.assertNotNull(contactNumber, context + fileName + " | Field 'contact_number' must not be null");

        if (expected.has("contactnumber")) {
            String expectedContact = expected.getString("contactnumber");
            String normalizedExpected = normalizePhone(expectedContact);
            String normalizedActual = normalizePhone(contactNumber);
            Assert.assertTrue(normalizedActual.contains(normalizedExpected),
                    context + fileName + " | Standard field 'contact_number' | Expected (contains): "
                            + expectedContact + " | Actual: " + contactNumber);
        }

        Assert.assertNotNull(resumeText, context + fileName + " | Field 'candidate_summary' must not be null");
        Assert.assertFalse(resumeText.trim().isEmpty(),
                context + fileName + " | Field 'candidate_summary' must not be empty");
    }

    private void validateCustomFields(JSONObject candidate, JSONObject expected, JSONObject hints,
            String context, String fileName) {
        for (String fieldKey : expected.keySet()) {
            Object expectedValue = expected.get(fieldKey);
            String hintType = hints.optString(fieldKey, "exact");

            int colIdx = Integer.parseInt(fieldKey.replace("custcolumn", ""));
            
            Assert.assertTrue(candidate.has(fieldKey), context + fileName + " | Custom field '" + fieldKey + "' not found in response");

            String fieldType = CUSTOM_FIELDS.stream()
                    .filter(f -> (int) f.get("column_index") == colIdx)
                    .map(f -> (String) f.get("type"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            context + fileName + " | Field type not found for column index: " + colIdx));

            Object actualValue = candidate.opt(fieldKey);
            if (expectedValue == JSONObject.NULL || expectedValue == null || (expectedValue instanceof String && ((String)expectedValue).isEmpty())) {
                if (actualValue != null && !"null".equals(String.valueOf(actualValue)) && !String.valueOf(actualValue).isEmpty()) {
                     Assert.fail(context + fileName + " | Custom field '" + fieldKey + "' expected null/empty but was '" + actualValue + "'");
                }
            } else {
                assertCustomFieldValue(fieldKey, expectedValue, actualValue, hintType, fieldType, context, fileName);
            }
        }
    }

    private void validateCustomFieldsPublicAPI(JSONObject candidate, JSONObject expected, JSONObject hints,
            String context, String fileName) {
        JSONArray customFieldsArray = candidate.optJSONArray("custom_fields");
        
        for (String fieldKey : expected.keySet()) {
            Object expectedValue = expected.get(fieldKey);
            String hintType = hints.optString(fieldKey, "exact");

            int colIdx = Integer.parseInt(fieldKey.replace("custcolumn", ""));
            Assert.assertTrue(columnToFieldIdMap.containsKey(colIdx),
                    context + fileName + " | Column index " + colIdx + " not found in created fields");

            JSONObject actualField = null;

            if (customFieldsArray != null) {
                for (int i = 0; i < customFieldsArray.length(); i++) {
                    JSONObject cf = customFieldsArray.getJSONObject(i);
                    int fieldColumnIndex = cf.getInt("field_id");
                    if (fieldColumnIndex == colIdx) {
                        actualField = cf;
                        break;
                    }
                }
            }

            if (actualField == null) {
                Assert.fail(context + fileName + " | Custom field '" + fieldKey + "' (column " + colIdx
                        + ") not found in response");
            }

            Object actualValue = actualField.opt("value");
            String fieldType = actualField.optString("field_type", "");

            assertCustomFieldValue(fieldKey, expectedValue, actualValue, hintType, fieldType, context, fileName);
        }
    }

    private void validateCustomFieldsAlbatrossInline(JSONObject customFields, JSONObject expected, JSONObject hints,
            String context, String fileName) {
        for (String fieldKey : expected.keySet()) {
            Object expectedValue = expected.get(fieldKey);
            String hintType = hints.optString(fieldKey, "exact");

            int colIdx = Integer.parseInt(fieldKey.replace("custcolumn", ""));
            
            Assert.assertTrue(customFields.has(fieldKey), 
                    context + fileName + " | Custom field '" + fieldKey + "' not found in custom_fields response");

            String fieldType = CUSTOM_FIELDS.stream()
                    .filter(f -> (int) f.get("column_index") == colIdx)
                    .map(f -> (String) f.get("type"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            context + fileName + " | Field type not found for column index: " + colIdx));

            Object actualValue = customFields.opt(fieldKey);
            if (expectedValue == JSONObject.NULL || expectedValue == null 
                    || (expectedValue instanceof String && ((String)expectedValue).isEmpty())) {
                if (actualValue != null && !"null".equals(String.valueOf(actualValue)) 
                        && !String.valueOf(actualValue).isEmpty()) {
                     Assert.fail(context + fileName + " | Custom field '" + fieldKey 
                             + "' expected null/empty but was '" + actualValue + "'");
                }
            } else {
                assertCustomFieldValue(fieldKey, expectedValue, actualValue, hintType, fieldType, context, fileName);
            }
        }
    }

    private void assertCustomFieldValue(String fieldKey, Object expected, Object actual, String hintType,
            String fieldType, String context, String fileName) {
        if (expected == JSONObject.NULL || expected == null) {
            return;
        }

        String expectedStr = normalizeText(String.valueOf(expected));
        String actualStr = normalizeText(String.valueOf(actual));

        switch (hintType) {
            case "exact":
                if (fieldType.equals("checkbox")) {
                    expectedStr = normalizeCheckbox(expectedStr);
                    actualStr = normalizeCheckbox(actualStr);
                } else if (fieldType.equals("date")) {
                    expectedStr = normalizeDate(expectedStr);
                    actualStr = normalizeDate(actualStr);
                } else if (fieldType.equals("number")) {
                    expectedStr = normalizeNumber(expectedStr);
                    actualStr = normalizeNumber(actualStr);
                } else if (fieldType.equals("phonenumber")) {
                    expectedStr = normalizePhone(expectedStr);
                    actualStr = normalizePhone(actualStr);
                } else if (fieldType.equals("multiselect")) {
                    expectedStr = toCommaSeparatedMultiselect(expected);
                    actualStr = requireCommaSeparatedMultiselectValue(actual);
                }

                Assert.assertEquals(actualStr, expectedStr,
                        context + fileName + " | [" + fieldKey + "] Expected (exact): " + expected + " | Actual: "
                                + actual);
                break;

            case "contains":
                if (fieldType.equals("phonenumber")) {
                    expectedStr = normalizePhone(expectedStr);
                    actualStr = normalizePhone(actualStr);
                }
                Assert.assertTrue(actualStr.contains(expectedStr),
                        context + fileName + " | [" + fieldKey + "] Expected (contains): " + expected + " | Actual: "
                                + actual);
                break;

            case "all_items_present":
                List<String> expectedItems = splitMultiselectCsv(toCommaSeparatedMultiselect(expected));
                List<String> actualItems = splitMultiselectCsv(requireCommaSeparatedMultiselectValue(actual));
                for (String item : expectedItems) {
                    Assert.assertTrue(actualItems.contains(item),
                            context + fileName + " | [" + fieldKey + "] Expected item: " + item
                                    + " | Missing in actual: " + actual);
                }
                break;

            case "semantic":
                Assert.assertFalse(actualStr.isEmpty(),
                        context + fileName + " | [" + fieldKey + "] Semantic validation: must not be empty");
                break;
        }
    }

    private void validateWorkHistory(String candidateId, String context) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", candidateId);

        Response response = RestClient.doGet("JSON", albatrossURL, "candidates/candidate-work/{id}",
                albatrossAuthToken, null, pathParams, false);

        Assert.assertEquals(response.getStatusCode(), 200,
                context + "Failed to fetch work history | Status: " + response.getStatusCode());

        List<Map<String, Object>> workHistory = response.jsonPath().getList("data");
        Assert.assertNotNull(workHistory, context + "Work history must not be null");
        Assert.assertFalse(workHistory.isEmpty(), context + "Work history must not be empty");
    }

    private void validateEducationHistory(String candidateId, String context) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", candidateId);

        Response response = RestClient.doGet("JSON", albatrossURL, "candidates/candidate-education/{id}",
                albatrossAuthToken, null, pathParams, false);

        Assert.assertEquals(response.getStatusCode(), 200,
                context + "Failed to fetch education history | Status: " + response.getStatusCode());

        List<Map<String, Object>> educationHistory = response.jsonPath().getList("data");
        Assert.assertNotNull(educationHistory, context + "Education history must not be null");
        Assert.assertFalse(educationHistory.isEmpty(), context + "Education history must not be empty");
    }

    private String normalizeText(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeNumber(String s) {
        if (s == null || s.isEmpty())
            return "";
        return s.replaceAll("[,\\s]", "");
    }

    private String normalizeCheckbox(String s) {
        if (s == null)
            return "0";
        s = s.trim().toLowerCase();
        if (s.equals("true") || s.equals("1") || s.equals("yes"))
            return "1";
        return "0";
    }

    private String normalizeDate(String s) {
        if (s == null || s.isEmpty())
            return "";
        if (s.matches("\\d{4}-\\d{2}-\\d{2}"))
            return s;
        return s.split("T")[0];
    }

    private String normalizePhone(String s) {
        if (s == null)
            return "";
        return s.replaceAll("\\s+", "");
    }

    @SuppressWarnings("unchecked")
    private String toCommaSeparatedMultiselect(Object value) {
        if (value == null)
            return "";
        if (value instanceof String)
            return (String) value;
        if (value instanceof List) {
            return String.join(",", (List<String>) value);
        }
        return String.valueOf(value);
    }

    private String requireCommaSeparatedMultiselectValue(Object value) {
        if (value == null)
            return "";
        return String.valueOf(value);
    }

    private List<String> splitMultiselectCsv(String csv) {
        if (csv == null || csv.isEmpty())
            return Collections.emptyList();
        List<String> items = new ArrayList<>();
        for (String item : csv.split(",")) {
            items.add(item.trim());
        }
        return items;
    }
}
