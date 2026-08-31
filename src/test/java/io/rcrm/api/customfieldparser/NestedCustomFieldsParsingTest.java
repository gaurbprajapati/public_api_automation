package io.rcrm.api.customfieldparser;

import com.qa.api.util.S3Uploader;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.DefaultOptionsValue;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.pojo.albatross.NestedCustomFieldPojo;
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
public class NestedCustomFieldsParsingTest extends TestBase {

    private static final int ENTITY_TYPE_ID = 5;

    private static String albatrossAuthToken;
    private static String apiAuthToken;

    private static final Map<Integer, Integer> columnToFieldIdMap = new LinkedHashMap<>();
    private static final Map<Integer, Map<String, Integer>> fieldOptionMap = new LinkedHashMap<>();

    private static JSONObject groundTruth;
    private static JSONArray testCasesArray;

    private static final String RESUME_DIR = System.getProperty("user.dir")
            + "/src/test/resources/testData/custom_field_parsing_agent/resumes/";

    private static final String GROUND_TRUTH_PATH = System.getProperty("user.dir")
            + "/src/test/resources/testData/custom_field_parsing_agent/ground_truth.json";

    private static final List<Map<String, Object>> NESTED_FIELDS = buildNestedFields();
    private static final Map<Integer, Map<String, Object>> DEPENDENCY_RULES = buildDependencyRules();
    private static List<Map<String, Object>> buildNestedFields() {
        List<Map<String, Object>> fields = new ArrayList<>();

        fields.add(field(1, "Willing to Travel",       "checkbox",     "parent", null,
                "Whether the candidate can commit to regular work-related travel.",
                Collections.emptyList()));

        fields.add(field(2, "Work Mode Preference",    "dropdown",     "parent", null,
                "The day-to-day working environment the candidate is targeting.",
                Arrays.asList("Onsite", "Remote", "Hybrid")));

        fields.add(field(3, "Technical Skill Focus",   "multiselect",  "parent", null,
                "The engineering areas where the candidate has deepest expertise.",
                Arrays.asList("Frontend", "Backend", "Data")));

        fields.add(field(4, "Travel Radius",           "text",         "child",  1,
                "How far from their base location the candidate is willing to travel regularly.",
                Collections.emptyList()));

        fields.add(field(5, "Office Setup Type",       "dropdown",     "child",  2,
                "The type of working space arrangement the candidate has or uses.",
                Arrays.asList("Dedicated room", "Shared space", "Co-working space")));

        fields.add(field(6, "Onsite Location",         "text",         "child",  2,
                "The city or office area where the candidate expects to work in person.",
                Collections.emptyList()));

        fields.add(field(7, "Backend Stack",           "multiselect",  "child",  3,
                "The server-side frameworks and technologies the candidate works with.",
                Arrays.asList("Django", "Spring Boot", "FastAPI", "Express")));

        fields.add(field(8, "Data Tool Preference",    "text",         "child",  3,
                "The data engineering or analytics toolset the candidate is most productive with.",
                Collections.emptyList()));

        fields.add(field(9, "Notice Period",           "number",       "independent", null,
                "How many weeks of notice the candidate must serve before joining.",
                Collections.emptyList()));

        fields.add(field(10, "Preferred Contract Rate", "text",        "independent", null,
                "The rate the candidate expects for contract or freelance engagements.",
                Collections.emptyList()));

        return fields;
    }

    private static Map<String, Object> field(int col, String name, String type, String role,
            Integer parentCol, String description, List<String> options) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("column_index", col);
        f.put("name", name);
        f.put("type", type);
        f.put("role", role);
        f.put("parent_col", parentCol);
        f.put("description", description);
        f.put("options", options);
        return f;
    }

    private static Map<Integer, Map<String, Object>> buildDependencyRules() {
        Map<Integer, Map<String, Object>> rules = new LinkedHashMap<>();

        Map<String, Object> r4filter = new LinkedHashMap<>();
        r4filter.put("1", "ALL");
        r4filter.put("0", null);
        rules.put(4, rule(1, "checkbox", "text", r4filter));

        Map<String, Object> r5filter = new LinkedHashMap<>();
        r5filter.put("Remote", Arrays.asList("Dedicated room", "Shared space"));
        r5filter.put("Hybrid",  Arrays.asList("Shared space", "Co-working space"));
        r5filter.put("Onsite",  null);
        rules.put(5, rule(2, "dropdown", "dropdown", r5filter));

        Map<String, Object> r6filter = new LinkedHashMap<>();
        r6filter.put("Onsite", "ALL");
        r6filter.put("Remote", null);
        r6filter.put("Hybrid", null);
        rules.put(6, rule(2, "dropdown", "text", r6filter));

        Map<String, Object> r7filter = new LinkedHashMap<>();
        r7filter.put("Backend",  Arrays.asList("Django", "Spring Boot", "FastAPI", "Express"));
        r7filter.put("Data",     Arrays.asList("FastAPI", "Django"));
        r7filter.put("Frontend", null);
        rules.put(7, rule(3, "multiselect", "multiselect", r7filter));

        Map<String, Object> r8filter = new LinkedHashMap<>();
        r8filter.put("Data",     "ALL");
        r8filter.put("Backend",  null);
        r8filter.put("Frontend", null);
        rules.put(8, rule(3, "multiselect", "text", r8filter));

        return rules;
    }

    private static Map<String, Object> rule(int parentCol, String parentType, String childType,
            Map<String, Object> filter) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("parent_col", parentCol);
        r.put("parent_type", parentType);
        r.put("child_type", childType);
        r.put("child_option_filter", filter);
        return r;
    }

    @BeforeClass
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken       = ThreadManager.getAccountApiKey();
        groundTruth        = readJsonFileFromPath(GROUND_TRUTH_PATH);
        testCasesArray     = groundTruth.getJSONArray("nested_test_cases");

        createAllCustomFields();
        refreshOptionIds();
        storeDependencies();
    }

    @Test
    public void validateNestedFieldsAlbatrossParseResume() {
        for (int i = 0; i < testCasesArray.length(); i++) {
            JSONObject tc = testCasesArray.getJSONObject(i);
            String tcId = tc.has("test_case_id") ? tc.getString("test_case_id") : tc.getString("tc_id");
            String resumeFile    = tc.getString("resume_file");
            String assertContext = "[" + tcId + "] [Albatross] ";
            JSONObject expected  = tc.getJSONObject("nested_custom_fields");

            File pdfFile = new File(RESUME_DIR + resumeFile);
            Assert.assertTrue(pdfFile.exists() && pdfFile.isFile(),
                    assertContext + "Resume file not found: " + pdfFile.getAbsolutePath());

            Map<String, String> presignedParams = new HashMap<>();
            presignedParams.put("fileName", pdfFile.getName());
            presignedParams.put("requestType", "put");

            Response presignedResp = RestClient.doGet("JSON", albatrossURL, "get-presigned-url",
                    albatrossAuthToken, presignedParams, null, false);
            Assert.assertEquals(presignedResp.getStatusCode(), 200,
                    assertContext + "get-presigned-url failed");

            String presignedUrl = presignedResp.jsonPath().getString("data.preSignedUrl");
            String s3Key        = presignedResp.jsonPath().getString("data.key");

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

            JSONObject parseBody = new JSONObject();
            parseBody.put("resumeParserData", resumeParserData);
            parseBody.put("actionid", 0);

            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("actionsteps", "1");

            Response parseResp = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                    albatrossAuthToken, queryParams, null, false, parseBody);
            Assert.assertEquals(parseResp.getStatusCode(), 200,
                    assertContext + "parse-resume returned status: " + parseResp.getStatusCode());

            Map<String, Object> candidateMap = parseResp.jsonPath().getMap("data.candidate");
            JSONObject candidate = new JSONObject(candidateMap);

            validateNestedCustomFields(candidate, expected, assertContext, resumeFile);
        }
    }

    @Test
    public void validateNestedFieldsAlbatrossParseOnly() {
        JSONObject tc       = testCasesArray.getJSONObject(0);
        String tcId = tc.has("test_case_id") ? tc.getString("test_case_id") : tc.getString("tc_id");
        String resumeFile   = tc.getString("resume_file");
        String assertContext = "[" + tcId + "] [Albatross-ParseOnly] ";
        JSONObject expected = tc.getJSONObject("nested_custom_fields");

        File pdfFile = new File(RESUME_DIR + resumeFile);
        Assert.assertTrue(pdfFile.exists() && pdfFile.isFile(),
                assertContext + "Resume file not found: " + pdfFile.getAbsolutePath());

        Map<String, String> presignedParams = new HashMap<>();
        presignedParams.put("fileName", pdfFile.getName());
        presignedParams.put("requestType", "put");

        Response presignedResp = RestClient.doGet("JSON", albatrossURL, "get-presigned-url",
                albatrossAuthToken, presignedParams, null, false);
        Assert.assertEquals(presignedResp.getStatusCode(), 200,
                assertContext + "get-presigned-url failed");

        String presignedUrl = presignedResp.jsonPath().getString("data.preSignedUrl");
        String s3Key        = presignedResp.jsonPath().getString("data.key");

        try {
            S3Uploader.uploadFileToS3(presignedUrl, pdfFile.getAbsolutePath());
        } catch (IOException e) {
            Assert.fail(assertContext + "S3 upload failed: " + e.getMessage());
        }

        JSONObject filesInfo = new JSONObject();
        filesInfo.put("key", s3Key);
        filesInfo.put("name", pdfFile.getName());

        JSONObject resumeParserData = new JSONObject();
        resumeParserData.put("filesInfo", filesInfo);

        JSONObject parseBody = new JSONObject();
        parseBody.put("resumeParserData", resumeParserData);
        parseBody.put("onlyParserData", true);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("actionsteps", "1");

        Response parseResp = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
                albatrossAuthToken, queryParams, null, false, parseBody);
        Assert.assertEquals(parseResp.getStatusCode(), 200,
                assertContext + "parse-resume returned status: " + parseResp.getStatusCode());

        JsonPath jp = parseResp.jsonPath();
        Map<String, Object> customFieldsMap = jp.getMap("data.custom_fields");
        Assert.assertNotNull(customFieldsMap, assertContext + "custom_fields not found in response");
        JSONObject customFields = new JSONObject(customFieldsMap);

        validateNestedCustomFieldsInline(customFields, expected, assertContext, resumeFile);
    }

    @Test
    public void validateNestedFieldsPublicAPIResumeParser() {
        JSONObject tc       = testCasesArray.getJSONObject(0);
        String tcId = tc.has("test_case_id") ? tc.getString("test_case_id") : tc.getString("tc_id");
        String resumeFile   = tc.getString("resume_file");
        String assertContext = "[" + tcId + "] [PublicAPI] ";
        JSONObject expected = tc.getJSONObject("nested_custom_fields");

        File pdfFile = new File(RESUME_DIR + resumeFile);
        Assert.assertTrue(pdfFile.exists() && pdfFile.isFile(),
                assertContext + "Resume file not found: " + pdfFile.getAbsolutePath());

        Map<String, String> formParams = new HashMap<>();
        formParams.put("override_data", "");

        Response response = RestClient.doPostMultipart(baseURL, "/candidates/resume-parser",
                apiAuthToken, pdfFile, "file", "application/pdf", formParams, false);
        Assert.assertEquals(response.getStatusCode(), 200,
                assertContext + "resume-parser returned status: " + response.getStatusCode());

        Map<String, Object> candidateMap = response.jsonPath().getMap("candidate");
        Assert.assertNotNull(candidateMap, assertContext + "candidate not found in response");
        JSONObject candidate = new JSONObject(candidateMap);

        JSONArray customFieldsArray = candidate.optJSONArray("custom_fields");
        Assert.assertNotNull(customFieldsArray, assertContext + "custom_fields array not found");

        validateNestedCustomFieldsPublicAPI(customFieldsArray, expected, assertContext, resumeFile);
    }

    private void validateNestedCustomFields(JSONObject candidate, JSONObject expected,
            String context, String fileName) {
        for (String fieldKey : expected.keySet()) {
            int colIdx       = Integer.parseInt(fieldKey.replace("custcolumn", ""));
            Object gtValue   = expected.get(fieldKey);
            Object rawParsed = candidate.opt(fieldKey);
            String fieldType = getFieldType(colIdx);

            validateFieldWithDependency(colIdx, fieldKey, fieldType, gtValue, rawParsed,
                    candidate, expected, context, fileName);
        }
    }

    private void validateNestedCustomFieldsInline(JSONObject customFields, JSONObject expected,
            String context, String fileName) {
        for (String fieldKey : expected.keySet()) {
            int colIdx       = Integer.parseInt(fieldKey.replace("custcolumn", ""));
            Object gtValue   = expected.get(fieldKey);
            Object rawParsed = customFields.opt(fieldKey);
            String fieldType = getFieldType(colIdx);

            // For inline responses we resolve the parent value from custom_fields too
            validateFieldWithDependencyFromSource(colIdx, fieldKey, fieldType, gtValue, rawParsed,
                    customFields, expected, context, fileName);
        }
    }

    private void validateNestedCustomFieldsPublicAPI(JSONArray customFieldsArray, JSONObject expected,
            String context, String fileName) {
        Map<Integer, Object> parsedByCol = new HashMap<>();
        Map<Integer, String> typeByCol   = new HashMap<>();
        for (int i = 0; i < customFieldsArray.length(); i++) {
            JSONObject cf   = customFieldsArray.getJSONObject(i);
            int colIdx      = cf.getInt("field_id");
            String fType    = cf.optString("field_type", "text");
            Object val      = cf.opt("value");
            parsedByCol.put(colIdx, val);
            typeByCol.put(colIdx, fType);
        }

        for (String fieldKey : expected.keySet()) {
            int colIdx       = Integer.parseInt(fieldKey.replace("custcolumn", ""));
            Object gtValue   = expected.get(fieldKey);
            Object rawParsed = parsedByCol.get(colIdx);
            String fieldType = typeByCol.getOrDefault(colIdx, getFieldType(colIdx));

            JSONObject parsedFlat = new JSONObject();
            for (Map.Entry<Integer, Object> e : parsedByCol.entrySet()) {
                parsedFlat.put("custcolumn" + e.getKey(), e.getValue());
            }

            validateFieldWithDependencyFromSource(colIdx, fieldKey, fieldType, gtValue, rawParsed,
                    parsedFlat, expected, context, fileName);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateFieldWithDependency(int colIdx, String fieldKey, String fieldType,
            Object gtValue, Object rawParsed, JSONObject candidateSource,
            JSONObject expectedSource, String context, String fileName) {

        Map<String, Object> rule = DEPENDENCY_RULES.get(colIdx);

        if (rule == null) {
            assertFieldValue(fieldKey, fieldType, gtValue, rawParsed, context, fileName);
            return;
        }

        int parentCol      = (int) rule.get("parent_col");
        String parentType  = (String) rule.get("parent_type");
        String parentKey   = "custcolumn" + parentCol;
        Object parentParsed = candidateSource.opt(parentKey);

        VisibilityResult vis = resolveVisibility(colIdx, parentParsed, parentType,
                (Map<String, Object>) rule.get("child_option_filter"), fieldType);

        if (!vis.visible) {
            boolean parsedEmpty = isNullOrEmpty(rawParsed);
            if (!parsedEmpty) {
                Assert.fail(context + fileName + " | [" + fieldKey + "] DEPENDENCY VIOLATION: "
                        + "child hidden (parent custcolumn" + parentCol + "='" + parentParsed + "') "
                        + "but parser extracted: '" + rawParsed + "'");
            }
            return;
        }

        Object effectiveExpected = gtValue;
        Object effectiveParsed  = rawParsed;

        if (vis.allowedOptions != null && !vis.allowedOptions.isEmpty()) {
            effectiveExpected = filterToAllowed(gtValue, vis.allowedOptions, fieldType);
            effectiveParsed   = filterToAllowed(rawParsed, vis.allowedOptions, fieldType);
        }

        assertFieldValue(fieldKey, fieldType, effectiveExpected, effectiveParsed, context, fileName);
    }

    @SuppressWarnings("unchecked")
    private void validateFieldWithDependencyFromSource(int colIdx, String fieldKey, String fieldType,
            Object gtValue, Object rawParsed, JSONObject parsedSource,
            JSONObject expectedSource, String context, String fileName) {

        Map<String, Object> rule = DEPENDENCY_RULES.get(colIdx);

        if (rule == null) {
            assertFieldValue(fieldKey, fieldType, gtValue, rawParsed, context, fileName);
            return;
        }

        int parentCol       = (int) rule.get("parent_col");
        String parentType   = (String) rule.get("parent_type");
        String parentKey    = "custcolumn" + parentCol;
        Object parentParsed = parsedSource.opt(parentKey);

        VisibilityResult vis = resolveVisibility(colIdx, parentParsed, parentType,
                (Map<String, Object>) rule.get("child_option_filter"), fieldType);

        if (!vis.visible) {
            boolean parsedEmpty = isNullOrEmpty(rawParsed);
            if (!parsedEmpty) {
                Assert.fail(context + fileName + " | [" + fieldKey + "] DEPENDENCY VIOLATION: "
                        + "child hidden (parent custcolumn" + parentCol + "='" + parentParsed + "') "
                        + "but parser extracted: '" + rawParsed + "'");
            }
            return;
        }

        Object effectiveExpected = gtValue;
        Object effectiveParsed   = rawParsed;

        if (vis.allowedOptions != null && !vis.allowedOptions.isEmpty()) {
            effectiveExpected = filterToAllowed(gtValue, vis.allowedOptions, fieldType);
            effectiveParsed   = filterToAllowed(rawParsed, vis.allowedOptions, fieldType);
        }

        assertFieldValue(fieldKey, fieldType, effectiveExpected, effectiveParsed, context, fileName);
    }

    private static class VisibilityResult {
        boolean visible;
        List<String> allowedOptions; // null = no restriction (ALL)

        VisibilityResult(boolean visible, List<String> allowedOptions) {
            this.visible = visible;
            this.allowedOptions = allowedOptions;
        }
    }

    @SuppressWarnings("unchecked")
    private VisibilityResult resolveVisibility(int childCol, Object parentParsedVal,
            String parentType, Map<String, Object> filterMap, String childFieldType) {

        List<String> parentKeys = toParentKeys(parentParsedVal, parentType);

        if (parentKeys.isEmpty()) {
            return new VisibilityResult(false, null);
        }

        boolean childVisible   = false;
        boolean hasAll         = false;
        List<String> allowed   = new ArrayList<>();

        for (String pk : parentKeys) {
            Object mapped = lookupFilter(filterMap, pk);
            if (mapped == null) continue;  // this parent value hides the child
            childVisible = true;
            if ("ALL".equals(mapped)) {
                hasAll = true;
            } else if (mapped instanceof List) {
                allowed.addAll((List<String>) mapped);
            }
        }

        if (!childVisible) {
            return new VisibilityResult(false, null);
        }
        if (hasAll || childFieldType.equals("text") || childFieldType.equals("number")) {
            return new VisibilityResult(true, null);
        }
        return new VisibilityResult(true, allowed.isEmpty() ? null : allowed);
    }

    private List<String> toParentKeys(Object parentVal, String parentType) {
        if (isNullOrEmpty(parentVal)) return Collections.emptyList();
        if ("checkbox".equals(parentType)) {
            return Collections.singletonList(normalizeCheckbox(String.valueOf(parentVal)));
        }
        if ("dropdown".equals(parentType)) {
            return Collections.singletonList(String.valueOf(parentVal).trim());
        }
        // multiselect
        return splitCsv(String.valueOf(parentVal));
    }

    private Object lookupFilter(Map<String, Object> filterMap, String key) {
        String stripped = key.trim();
        for (Map.Entry<String, Object> e : filterMap.entrySet()) {
            if (e.getKey().trim().equals(stripped)) return e.getValue();
        }
        return null; // absent → treat as hidden
    }

    private Object filterToAllowed(Object value, List<String> allowed, String fieldType) {
        if (isNullOrEmpty(value)) return null;
        if ("dropdown".equals(fieldType)) {
            String v = String.valueOf(value).trim();
            return allowed.contains(v) ? v : null;
        }
        if ("multiselect".equals(fieldType)) {
            List<String> parts = toList(value);
            List<String> kept  = new ArrayList<>();
            for (String p : parts) {
                if (allowed.contains(p.trim())) kept.add(p.trim());
            }
            return kept.isEmpty() ? null : kept;
        }
        return value;
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        return text.trim()
                .toLowerCase()
                .replaceAll("\\s*,\\s*", " ")  // ", " → " "
                .replaceAll("\\s+and\\s+", " ") // " and " → " "
                .replaceAll("\\s+", " ")        // normalize multiple spaces
                .trim();
    }

    private void assertFieldValue(String fieldKey, String fieldType, Object expected, Object actual,
            String context, String fileName) {

        if (isNullOrEmpty(expected)) {
            if (!isNullOrEmpty(actual)) {
                Assert.fail(context + fileName + " | [" + fieldKey + "] Expected null/empty but got: '" + actual + "'");
            }
            return;
        }

        if (isNullOrEmpty(actual)) {
            Assert.fail(context + fileName + " | [" + fieldKey + "] Expected: '" + expected + "' but got null/empty");
            return;
        }

        switch (fieldType) {
            case "checkbox":
                Assert.assertEquals(normalizeCheckbox(String.valueOf(actual)),
                        normalizeCheckbox(String.valueOf(expected)),
                        context + fileName + " | [" + fieldKey + "] checkbox mismatch | Expected: '"
                                + expected + "' | Actual: '" + actual + "'");
                break;

            case "multiselect": {
                List<String> expList = toList(expected);
                List<String> actList = toList(actual);
                Collections.sort(expList);
                Collections.sort(actList);
                Assert.assertEquals(actList, expList,
                        context + fileName + " | [" + fieldKey + "] multiselect mismatch | Expected: "
                                + expList + " | Actual: " + actList);
                break;
            }

            case "dropdown":
                Assert.assertEquals(String.valueOf(actual).trim(), String.valueOf(expected).trim(),
                        context + fileName + " | [" + fieldKey + "] dropdown mismatch | Expected: '"
                                + expected + "' | Actual: '" + actual + "'");
                break;

            case "number":
                Assert.assertEquals(normalizeNumber(String.valueOf(actual)),
                        normalizeNumber(String.valueOf(expected)),
                        context + fileName + " | [" + fieldKey + "] number mismatch | Expected: '"
                                + expected + "' | Actual: '" + actual + "'");
                break;

            case "phonenumber":
                Assert.assertTrue(normalizePhone(String.valueOf(actual))
                        .contains(normalizePhone(String.valueOf(expected))),
                        context + fileName + " | [" + fieldKey + "] phone mismatch | Expected: '"
                                + expected + "' | Actual: '" + actual + "'");
                break;

            default:
                String normalizedExpected = normalizeText(String.valueOf(expected));
                String normalizedActual = normalizeText(String.valueOf(actual));
                Assert.assertTrue(normalizedActual.contains(normalizedExpected),
                        context + fileName + " | [" + fieldKey + "] text mismatch | Expected contains: '"
                                + expected + "' | Actual: '" + actual + "'");
                break;
        }
    }

    private void createAllCustomFields() {
        for (Map<String, Object> fieldDef : NESTED_FIELDS) {
            int col           = (int) fieldDef.get("column_index");
            String name       = (String) fieldDef.get("name");
            String type       = (String) fieldDef.get("type");
            String desc       = (String) fieldDef.get("description");
            @SuppressWarnings("unchecked")
            List<String> opts = (List<String>) fieldDef.get("options");

            Response resp = createCustomField(albatrossAuthToken, col, name, type, desc, opts);
            
            if (resp.getStatusCode() != 200) {
                continue;
            }

            int fieldId = resp.jsonPath().getInt("data.custumField.id");
            columnToFieldIdMap.put(col, fieldId);

            List<Map<String, Object>> returnedOpts = resp.jsonPath()
                    .getList("data.custumField.defaultoptionsvalue");
            if (returnedOpts != null && !returnedOpts.isEmpty()) {
                Map<String, Integer> optMap = new LinkedHashMap<>();
                for (Map<String, Object> opt : returnedOpts) {
                    String label = String.valueOf(opt.get("label"));
                    int    optId = ((Number) opt.get("id")).intValue();
                    optMap.put(label, optId);
                }
                fieldOptionMap.put(fieldId, optMap);
            }
        }
    }

    private void refreshOptionIds() {
        Map<String, String> params = new HashMap<>();
        params.put("entity_type_id", String.valueOf(ENTITY_TYPE_ID));

        Response resp = RestClient.doGet("JSON", albatrossURL,
                "custom-fields/get-default-options/" + ENTITY_TYPE_ID,
                albatrossAuthToken, null, null, false);

        if (resp.getStatusCode() != 200) return;

        JsonPath jp = resp.jsonPath();
        for (Map.Entry<Integer, Integer> entry : columnToFieldIdMap.entrySet()) {
            int fieldId  = entry.getValue();
            String fIdStr = String.valueOf(fieldId);
            List<Map<String, Object>> opts = jp.getList("data." + fIdStr);
            if (opts == null || opts.isEmpty()) continue;
            Map<String, Integer> optMap = new LinkedHashMap<>();
            for (Map<String, Object> opt : opts) {
                String label = String.valueOf(opt.get("label"));
                int    optId = ((Number) opt.get("id")).intValue();
                optMap.put(label, optId);
            }
            fieldOptionMap.put(fieldId, optMap);
        }
    }

    @SuppressWarnings("unchecked")
    private void storeDependencies() {
        Map<Integer, Map<String, Object>> byCol = new LinkedHashMap<>();
        for (Map<String, Object> fd : NESTED_FIELDS) {
            byCol.put((int) fd.get("column_index"), fd);
        }

        for (Map.Entry<Integer, Map<String, Object>> ruleEntry : DEPENDENCY_RULES.entrySet()) {
            int childCol    = ruleEntry.getKey();
            Map<String, Object> rule = ruleEntry.getValue();

            int parentCol   = (int) rule.get("parent_col");
            String parentType = (String) rule.get("parent_type");
            Map<String, Object> filterMap = (Map<String, Object>) rule.get("child_option_filter");
            String childType = (String) rule.get("child_type");

            Integer parentFieldId = columnToFieldIdMap.get(parentCol);
            Integer childFieldId  = columnToFieldIdMap.get(childCol);
            
            if (parentFieldId == null || childFieldId == null) {
                continue;
            }

            Map<String, Integer> parentOptMap = fieldOptionMap.getOrDefault(parentFieldId, Collections.emptyMap());
            Map<String, Integer> childOptMap  = fieldOptionMap.getOrDefault(childFieldId,  Collections.emptyMap());

            List<NestedCustomFieldPojo.Mapping> mappings = new ArrayList<>();

            for (Map.Entry<String, Object> filterEntry : filterMap.entrySet()) {
                String parentLabel = filterEntry.getKey();
                Object allowed     = filterEntry.getValue();
                if (allowed == null) continue;

                Integer parentOptId = resolveParentOptionId(parentLabel, parentType, parentOptMap);
                if (parentOptId == null) {
                    continue;
                }

                if ("ALL".equals(allowed)) {
                    NestedCustomFieldPojo.Mapping m = new NestedCustomFieldPojo.Mapping();
                    m.setParent_value_id(parentOptId);
                    m.setChild_value_id(null);
                    m.setChild_visibility(Boolean.TRUE);
                    mappings.add(m);
                } else if (allowed instanceof List) {
                    for (String childLabel : (List<String>) allowed) {
                        Integer childOptId = childOptMap.get(childLabel);
                        if (childOptId == null) {
                            continue;
                        }
                        NestedCustomFieldPojo.Mapping m = new NestedCustomFieldPojo.Mapping();
                        m.setParent_value_id(parentOptId);
                        m.setChild_value_id(childOptId);
                        m.setChild_visibility(null);
                        mappings.add(m);
                    }
                }
            }

            if (mappings.isEmpty()) {
                continue;
            }

            NestedCustomFieldPojo payload = new NestedCustomFieldPojo();
            payload.setEntity(String.valueOf(ENTITY_TYPE_ID));
            payload.setLevel(1);
            payload.setDependency_id(String.valueOf(parentFieldId));
            payload.setParent_id(parentFieldId);
            payload.setChild_id(childFieldId);
            payload.setMappings(mappings);

            RestClient.doPost("JSON", albatrossURL, "nested-custom-fields/store",
                    albatrossAuthToken, null, false, payload);
        }
    }

    private Integer resolveParentOptionId(String parentLabel, String parentType,
            Map<String, Integer> parentOptMap) {
        if ("checkbox".equals(parentType)) {
            String norm = normalizeCheckbox(parentLabel);
            for (Map.Entry<String, Integer> e : parentOptMap.entrySet()) {
                String lbl = e.getKey().trim().toLowerCase();
                if ("1".equals(norm) && (lbl.equals("yes") || lbl.equals("true") || lbl.equals("1")))
                    return e.getValue();
                if ("0".equals(norm) && (lbl.equals("no") || lbl.equals("false") || lbl.equals("0")))
                    return e.getValue();
            }
            return null;
        }
        for (Map.Entry<String, Integer> e : parentOptMap.entrySet()) {
            if (e.getKey().trim().equals(parentLabel.trim())) return e.getValue();
        }
        return null;
    }

    private Response createCustomField(String token, int col, String name, String type,
            String desc, List<String> opts) {
        ExtraField ef = new ExtraField();
        ef.setEntitytypeid(ENTITY_TYPE_ID);
        ef.setExtrafieldname(name);
        ef.setExtrafieldtype(type);
        ef.setColumnid(col);
        ef.setDescription(desc);
        ef.setIs_parser_enabled(Boolean.TRUE);

        if (opts != null && !opts.isEmpty()) {
            List<DefaultOptionsValue> dovList = new ArrayList<>();
            for (String opt : opts) {
                DefaultOptionsValue dov = new DefaultOptionsValue();
                dov.setLabel(opt);
                dovList.add(dov);
            }
            ef.setDefaultoptionsvalue(dovList);
        }

        CustomFieldAlbatross cf = new CustomFieldAlbatross();
        cf.setCustumField(ef);
        return RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken,
                null, false, cf);
    }

    private String normalizeCheckbox(String s) {
        if (s == null) return "0";
        s = s.trim().toLowerCase();
        if (s.equals("1"))
            return "1";
        return "0";
    }

    private String normalizeNumber(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.replaceAll("[,\\s]", "");
    }

    private String normalizePhone(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", "");
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isEmpty()) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String part : csv.split("[,;/]+")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    private List<String> toList(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof JSONArray) {
            JSONArray ja = (JSONArray) value;
            List<String> result = new ArrayList<>();
            for (int i = 0; i < ja.length(); i++) result.add(ja.getString(i).trim());
            return result;
        }
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) result.add(String.valueOf(item).trim());
            return result;
        }
        return splitCsv(String.valueOf(value));
    }

    private boolean isNullOrEmpty(Object val) {
        if (val == null || val == JSONObject.NULL) return true;
        String s = String.valueOf(val).trim();
        return s.isEmpty() || s.equals("null");
    }

    private String getFieldType(int colIdx) {
        for (Map<String, Object> fd : NESTED_FIELDS) {
            if ((int) fd.get("column_index") == colIdx) return (String) fd.get("type");
        }
        return "text";
    }
}
