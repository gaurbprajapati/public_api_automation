package io.recruitcrm.albatross.neptune;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.rcrm.api.testbase.TestBase.AccountType;

@AccountType("Business|AlbatrossTkn")
public class GenerateEmailPlaceholderTest extends TestBase {

    private static final String BASE_PATH       = "generate-email";
    private static final String MATRIX_JSON     = "src/test/resources/generate_email_matrix.json";
    private static final Pattern PLACEHOLDER_RE = Pattern.compile("\\{[a-zA-Z][a-zA-Z0-9_]*\\}");

    private String albatrossToken;
    private int    candidateRecordId;
    private int    contactRecordId;

    private Set<String> candidatesAllowList   = new HashSet<>();
    private Set<String> contactsAllowList     = new HashSet<>();
    private Set<String> candidateJobAllowList = new HashSet<>();

    @BeforeClass
    public void setUp() {
        albatrossToken = ThreadManager.getOwnerAlbatrossToken();
        loadAllowLists();
        setupCandidateRecord();
        setupContactRecord();
        assignCandidateToJobViaAlbatross();
    }

    private void loadAllowLists() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(MATRIX_JSON)));
            JSONObject root = new JSONObject(content);
            JSONObject lists = root.getJSONObject("entity_placeholder_allowlists");
            candidatesAllowList   = toSet(lists.getJSONArray("candidates"));
            contactsAllowList     = toSet(lists.getJSONArray("contacts"));
            candidateJobAllowList = toSet(lists.getJSONArray("candidate_job"));
        } catch (IOException e) {
            Assert.fail("Failed to load generate_email_matrix.json: " + e.getMessage());
        }
    }

    private Set<String> toSet(JSONArray arr) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < arr.length(); i++) {
            set.add(arr.getString(i));
        }
        return set;
    }

    private void setupCandidateRecord() {
        AllCrudFunctions crud = new AllCrudFunctions();
        Response createResp = crud.createCandidate(albatrossURL, albatrossToken);
        candidateRecordId = createResp.jsonPath().getInt("data.candidate.id");
    }

    private void setupContactRecord() {
        AllCrudFunctions crud = new AllCrudFunctions();
        Response createResp = crud.createContact(albatrossURL, albatrossToken);
        contactRecordId = createResp.jsonPath().getInt("data.contact.id");
    }

    private void assignCandidateToJobViaAlbatross() {
        commanFunction fn = new commanFunction();
        AllCrudFunctions crud = new AllCrudFunctions();
        Response compResp = fn.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey());
        String companySlug = compResp.jsonPath().getString("slug");
        Response contResp = fn.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug);
        String contactSlug = contResp.jsonPath().getString("slug");
        Response jobResp = fn.createNewJob(baseURL, ThreadManager.getAccountApiKey(), companySlug, contactSlug);
        String jobSlug = jobResp.jsonPath().getString("slug");
        int jobId = crud.getJobResponse(albatrossURL, albatrossToken, jobSlug).jsonPath().getInt("data.job.id");
        ArrayList<Integer> candidateIds = new ArrayList<>();
        candidateIds.add(candidateRecordId);
        fn.assignMultipleCandidatesToJob(albatrossURL, albatrossToken, candidateIds, jobId);
    }

    private int recordIdFor(String entity) {
        switch (entity) {
            case "candidates":    return candidateRecordId;
            case "contacts":      return contactRecordId;
            case "candidate_job": return candidateRecordId;
            default: throw new IllegalArgumentException("Unknown entity: " + entity);
        }
    }

    private Set<String> allowListFor(String entity) {
        switch (entity) {
            case "candidates":    return candidatesAllowList;
            case "contacts":      return contactsAllowList;
            case "candidate_job": return candidateJobAllowList;
            default: throw new IllegalArgumentException("Unknown entity: " + entity);
        }
    }

    private JSONObject buildPayload(String key, String tone, String prompt, String entity,
                                    int recordId, String lastResponse, String chatId) {
        JSONObject payload = new JSONObject();
        payload.put("key", key);
        payload.put("tone", tone);
        payload.put("prompt", prompt);
        payload.put("entity", entity);
        payload.put("record_id", recordId);
        payload.put("entity_id", recordId);
        payload.put("last_response", lastResponse != null ? lastResponse : "");
        if (chatId != null && !chatId.isEmpty()) {
            payload.put("chat_id", chatId);
        }
        return payload;
    }

    private List<String> splitJsonObjects(String body) {
        List<String> chunks = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escape = false;

        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (escape) { escape = false; continue; }
            if (c == '\\' && inString) { escape = true; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (inString) continue;

            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    chunks.add(body.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return chunks;
    }

    private String extractCompleteMessage(String responseBody) {
        List<String> chunks = splitJsonObjects(responseBody);
        StringBuilder merged = new StringBuilder();

        for (String chunk : chunks) {
            try {
                JSONObject obj  = new JSONObject(chunk);
                JSONObject data = obj.optJSONObject("data");
                if (data == null) continue;

                // If complete_message exists, prefer it directly
                if (data.has("complete_message")) {
                    String msg = data.getString("complete_message");
                    if (msg != null && !msg.trim().isEmpty()) {
                        return msg;
                    }
                }

                // Otherwise accumulate generated_text tokens
                if (data.has("generated_text")) {
                    String token = data.optString("generated_text", "");
                    merged.append(token);
                }

            } catch (Exception ignored) {}
        }

        return merged.toString().trim();
    }

    private String extractChatId(String responseBody) {
        List<String> chunks = splitJsonObjects(responseBody);
        for (int i = chunks.size() - 1; i >= 0; i--) {
            try {
                JSONObject obj  = new JSONObject(chunks.get(i));
                JSONObject data = obj.optJSONObject("data");
                if (data != null && !data.has("complete_message")) {
                    String id = data.optString("id", null);
                    if (id != null && !id.trim().isEmpty()) return id;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private List<String> extractPlaceholders(String text) {
        List<String> found = new ArrayList<>();
        Matcher m = PLACEHOLDER_RE.matcher(text);
        while (m.find()) {
            found.add(m.group());
        }
        return found;
    }

    private void assertPlaceholders(String emailText, String entity, List<String> expectedList, int minMatchCount) {
        Set<String> allowList = allowListFor(entity);
        List<String> found    = extractPlaceholders(emailText);

        for (String ph : found) {
            Assert.assertTrue(allowList.contains(ph),
                    "Cross-entity violation: " + ph + " is not valid for entity '" + entity + "'");
        }
        if (minMatchCount > 0 && !expectedList.isEmpty()) {
            long matched = expectedList.stream().filter(found::contains).count();
            Assert.assertTrue(matched >= minMatchCount,
                    "Expected at least " + minMatchCount + " of " + expectedList + " but found " + matched
                    + ". Found: " + found + ". Email:\n" + emailText);
        }
    }

    @DataProvider(name = "matrixData", parallel = false)
    public Iterator<Object[]> matrixData() {
        return loadRows(false);
    }

    @DataProvider(name = "chainingData", parallel = false)
    public Iterator<Object[]> chainingData() {
        return loadRows(true);
    }

    private Iterator<Object[]> loadRows(boolean chainingOnly) {
        List<Object[]> rows = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(MATRIX_JSON)));
            JSONArray testCases = new JSONObject(content).getJSONArray("test_cases");
            for (int i = 0; i < testCases.length(); i++) {
                JSONObject tc = testCases.getJSONObject(i);
                if (!tc.has("id")) continue;
                boolean hasChaining = tc.has("chaining") && tc.getJSONObject("chaining").optBoolean("enabled", false);
                if (chainingOnly != hasChaining) continue;
                rows.add(new Object[]{ tc });
            }
        } catch (IOException e) {
            Assert.fail("Failed to read matrix JSON: " + e.getMessage());
        }
        return rows.iterator();
    }

    @DataProvider(name = "smokeData", parallel = false)
    public Iterator<Object[]> smokeData() {
        List<Object[]> rows = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(MATRIX_JSON)));
            JSONArray testCases = new JSONObject(content).getJSONArray("test_cases");
            for (int i = 0; i < testCases.length(); i++) {
                JSONObject tc = testCases.getJSONObject(i);
                if (tc.optBoolean("smoke", false)) {
                    rows.add(new Object[]{ tc });
                }
            }
        } catch (IOException e) {
            Assert.fail("Failed to read matrix JSON: " + e.getMessage());
        }
        return rows.iterator();
    }

    @Test(dataProvider = "smokeData")
    public void generateEmail_DailySmoke_Test(JSONObject tc) {
        String id       = tc.getString("id");
        String key      = tc.getString("key");
        String tone     = tc.getString("tone");
        String prompt   = tc.optString("prompt", "");
        String entity   = tc.getString("entity");
        int    recordId = recordIdFor(entity);

        List<String> expectedPlaceholders = toStringList(tc.optJSONArray("expected_min_placeholders"));
        int minMatch = expectedPlaceholders.isEmpty() ? 0 : 1;

        JSONObject payload = buildPayload(key, tone, prompt, entity, recordId, "", null);
        Response response = RestClient.doPost("JSON", neptuneServiceURL, BASE_PATH, albatrossToken, null, true, payload);

        Assert.assertEquals(response.getStatusCode(), 200,
                "[" + id + "] HTTP 200 expected for key=" + key + " entity=" + entity);

        String responseBody = response.getBody().asString();
        Assert.assertNotNull(responseBody, "[" + id + "] Response body should not be null");
        Assert.assertFalse(responseBody.trim().isEmpty(), "[" + id + "] Response body should not be empty");

        String completeMessage = extractCompleteMessage(responseBody);
        Assert.assertFalse(completeMessage.isEmpty(),
                "[" + id + "] complete_message is empty for key=" + key + " entity=" + entity);

        assertPlaceholders(completeMessage, entity, expectedPlaceholders, minMatch);
    }

    @Test(dataProvider = "matrixData")
    public void generateEmail_PlaceholderValidation_Test(JSONObject tc) {
        String id       = tc.getString("id");
        String key      = tc.getString("key");
        String tone     = tc.getString("tone");
        String prompt   = tc.optString("prompt", "");
        String entity   = tc.getString("entity");
        int    recordId = recordIdFor(entity);

        List<String> expectedPlaceholders = toStringList(tc.optJSONArray("expected_min_placeholders"));
        int minMatch = expectedPlaceholders.isEmpty() ? 0 : 1;

        JSONObject payload = buildPayload(key, tone, prompt, entity, recordId, "", null);
        Response response = RestClient.doPost("JSON", neptuneServiceURL, BASE_PATH, albatrossToken, null, true, payload);

        Assert.assertEquals(response.getStatusCode(), 200,
                "[" + id + "] HTTP 200 expected for key=" + key + " entity=" + entity);

        String responseBody = response.getBody().asString();
        Assert.assertNotNull(responseBody, "[" + id + "] Response body should not be null");
        Assert.assertFalse(responseBody.trim().isEmpty(), "[" + id + "] Response body should not be empty");

        String completeMessage = extractCompleteMessage(responseBody);
        Assert.assertFalse(completeMessage.isEmpty(),
                "[" + id + "] complete_message is empty for key=" + key + " entity=" + entity);

        assertPlaceholders(completeMessage, entity, expectedPlaceholders, minMatch);
    }

    @Test(dataProvider = "chainingData")
    public void generateEmail_Chaining_Test(JSONObject tc) {
        String id       = tc.getString("id");
        String key      = tc.getString("key");
        String tone     = tc.getString("tone");
        String prompt   = tc.optString("prompt", "");
        String entity   = tc.getString("entity");
        int    recordId = recordIdFor(entity);

        JSONObject chaining           = tc.getJSONObject("chaining");
        String followUpPrompt         = chaining.getString("follow_up_prompt");
        List<String> followUpExpected = toStringList(chaining.optJSONArray("follow_up_expected_min_placeholders"));

        JSONObject firstPayload = buildPayload(key, tone, prompt, entity, recordId, "", null);
        Response firstResponse = RestClient.doPost("JSON", neptuneServiceURL, BASE_PATH, albatrossToken, null, true, firstPayload);

        Assert.assertEquals(firstResponse.getStatusCode(), 200,
                "[" + id + "] Step-1 HTTP 200 expected for key=" + key + " entity=" + entity);

        String firstResponseBody = firstResponse.getBody().asString();
        Assert.assertNotNull(firstResponseBody, "[" + id + "] Step-1 response body should not be null");
        Assert.assertFalse(firstResponseBody.trim().isEmpty(), "[" + id + "] Step-1 response body should not be empty");

        String firstCompleteMessage = extractCompleteMessage(firstResponseBody);
        Assert.assertFalse(firstCompleteMessage.isEmpty(), "[" + id + "] Step-1 complete_message is empty");
        
        String chatId = extractChatId(firstResponseBody);
        Assert.assertNotNull(chatId, "[" + id + "] Step-1 chat_id not found in stream");

        JSONObject followUpPayload = buildPayload("manual_prompt", tone, followUpPrompt, entity,
                recordId, firstCompleteMessage, chatId);
        Response followUpResponse = RestClient.doPost("JSON", neptuneServiceURL, BASE_PATH, albatrossToken, null, true, followUpPayload);

        Assert.assertEquals(followUpResponse.getStatusCode(), 200, "[" + id + "] Step-2 HTTP 200 expected");

        String followUpResponseBody = followUpResponse.getBody().asString();
        String followUpCompleteMessage = extractCompleteMessage(followUpResponseBody);
        Assert.assertFalse(followUpCompleteMessage.isEmpty(), "[" + id + "] Step-2 complete_message is empty");

        assertPlaceholders(followUpCompleteMessage, entity, followUpExpected, 1);
    }

    private List<String> toStringList(JSONArray arr) {
        List<String> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            list.add(arr.getString(i));
        }
        return list;
    }
}
