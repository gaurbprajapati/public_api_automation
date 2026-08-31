package io.recruitcrm.adminSettings;

import com.qa.api.util.S3Uploader;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@TestBase.AccountType("Business|AlbatrossTkn")
public class CvFormattingAgentGenerateHtmlTest extends TestBase {

	private static final String PATH_SAVE = "admin-settings/save-cv-formatting-agent-settings";
	private static final String NEPTUNE_GENERATE_HTML_PATH = "cv-formatting-agent/generate-html";
	private static final String SAMPLE_RESUME_PATH = "src/main/java/io/rcrm/api/testdata/SampleResume.pdf";
	private static final String PDF_TEXT_OUTPUT_DIR = "target/cvFormattingAgent_pdf_debug";
	private static final int MAX_SNIPPET = 240;
	private static final int MIN_LEN = 2;
	private static final int ENTITY_CANDIDATE = 5;
	private static final String PRESET_DIR = "src/test/resources/cvFormattingAgent/presets/";
	private static final String CUSTOM_FIELD_WORK_AUTH_NAME = "Work Authorization";
	private static final String CUSTOM_FIELD_RATING_NAME = "Rating";
	private static final String CUSTOM_FIELD_WORK_AUTH_VALUE = "Authorized to work in India (no sponsorship required)";
	private static final String CUSTOM_FIELD_RATING_VALUE = "4.3 / 5";
	private static final String CUSTOM_FIELDS_PROMPT_APPEND = "\n\nInclude the following custom candidate fields in the CV:\n"
			+ "- Work Authorization\n- Rating\n\nDisplay their field names and corresponding values clearly in the CV.";

	private String albatrossToken;
	private String presetForest;
	private String presetExecutive;
	private String presetBold;
	private int cvFileColumnId;
	private final List<Integer> contentColumnIds = new ArrayList<>();
	private int cvCustomColumnWorkAuthId;
	private int cvCustomColumnRatingId;
	private int candidateId;
	private String candidateSlug;
	private JSONObject parsedCandidate;

	@BeforeClass
	public void beforeClassCvFormattingGenerateHtml() throws Exception {
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
		Assert.assertNotNull(albatrossToken, "Albatross token must not be null");
		presetForest = Files.readString(Paths.get(PRESET_DIR + "forest_minimal.txt"), StandardCharsets.UTF_8);
		presetExecutive = Files.readString(Paths.get(PRESET_DIR + "executive_prestige.txt"), StandardCharsets.UTF_8);
		presetBold = Files.readString(Paths.get(PRESET_DIR + "bold_editorial.txt"), StandardCharsets.UTF_8);
		int base = 48 + ThreadLocalRandom.current().nextInt(0, 15);
		int slot = base;
		String u = UUID.randomUUID().toString().substring(0, 8);
		cvFileColumnId = createCandidateCustomField("CV_GEN_FILE_" + u, "file", slot++, emptyOptions());
		contentColumnIds.add(createCandidateCustomField("CV_GEN_CF1_" + u, "text", slot++, emptyOptions()));
		contentColumnIds.add(createCandidateCustomField("CV_GEN_CF2_" + u, "text", slot++, emptyOptions()));
		Assert.assertEquals(contentColumnIds.size(), 2);
		String cf1Name = CUSTOM_FIELD_WORK_AUTH_NAME;
		String cf2Name = CUSTOM_FIELD_RATING_NAME;
		cvCustomColumnWorkAuthId = createCandidateCustomField(cf1Name, "text", slot++, emptyOptions());
		cvCustomColumnRatingId = createCandidateCustomField(cf2Name, "text", slot++, emptyOptions());
		parseSampleResumeAndStoreCandidate();
	}

	private void parseSampleResumeAndStoreCandidate() throws Exception {
		File resumeFile = new File(System.getProperty("user.dir") + "/" + SAMPLE_RESUME_PATH);
		Assert.assertTrue(resumeFile.isFile(), "Sample resume must exist: " + resumeFile.getAbsolutePath());
		String resumeFileName = resumeFile.getName();

		Map<String, String> presignedParams = new HashMap<>();
		presignedParams.put("fileName", resumeFileName);
		presignedParams.put("requestType", "put");

		Response presigned = RestClient.doGet("JSON", albatrossURL, "get-presigned-url", albatrossToken,
				presignedParams, null, true);
		Assert.assertEquals(presigned.getStatusCode(), 200, "Presigned URL fetch failed: " + presigned.asString());
		String encryptedKey = presigned.jsonPath().getString("data.key");
		String preSignedUrl = presigned.jsonPath().getString("data.preSignedUrl");
		Assert.assertNotNull(preSignedUrl, "data.preSignedUrl required to upload resume before parse-resume");
		S3Uploader.uploadFileToS3(preSignedUrl, resumeFile.getAbsolutePath(), "application/pdf");

		JSONObject filesInfo = new JSONObject();
		filesInfo.put("key", encryptedKey);
		filesInfo.put("name", resumeFileName);
		filesInfo.put("type", "application/pdf");
		filesInfo.put("size", resumeFile.length());
		filesInfo.put("index", 0);

		JSONObject resumeParserData = new JSONObject();
		resumeParserData.put("resumesParsed", 0);
		resumeParserData.put("resumesFailed", 0);
		resumeParserData.put("resumesTotal", 1);
		resumeParserData.put("filesInfo", filesInfo);

		JSONObject parseRequest = new JSONObject();
		parseRequest.put("resumeParserData", resumeParserData);
		parseRequest.put("actionid", 0);

		Map<String, String> parseParams = new HashMap<>();
		parseParams.put("actionsteps", "1");

		Response parseResponse = RestClient.doPost1("JSON", albatrossURL, "candidates/parse-resume",
				albatrossToken, parseParams, null, true, parseRequest);
		Assert.assertEquals(parseResponse.getStatusCode(), 200,
				"Resume parse failed: " + parseResponse.asString());

		JSONObject root = new JSONObject(parseResponse.asString());
		parsedCandidate = root.getJSONObject("data").getJSONObject("candidate");
		Assert.assertTrue(parsedCandidate.has("slug") && !parsedCandidate.isNull("slug"),
				"Parsed candidate must include slug");
		candidateSlug = parsedCandidate.getString("slug");
		Assert.assertFalse(candidateSlug.isEmpty(), "Parsed candidate slug must not be empty");
		Assert.assertTrue(parsedCandidate.has("id") && !parsedCandidate.isNull("id"),
				"Parsed candidate must include numeric id for global/update-fields");
		Object idRaw = parsedCandidate.get("id");
		candidateId = idRaw instanceof Number ? ((Number) idRaw).intValue() : Integer.parseInt(idRaw.toString());
	}

	@DataProvider
	public Object[][] cvFormattingPresetsNoRedaction() {
		return new Object[][] { { "forest_minimal" }, { "executive_prestige" }, { "bold_editorial" }, };
	}

	@Test(dataProvider = "cvFormattingPresetsNoRedaction")
	public void generateCvHtml_allPresets_noRedaction_parseResumeFieldsPresentInPdf(String presetKey) throws Exception {
		saveAdminSettings(presetPrompt(presetKey), 0, redactionFields(false, false, false), twoCustomFields(0, 1));
		PdfExtract extract = callGenerateHtmlAndExtractPdf();
		writePdfTextToFileAndLogPreview(extract.text, extract.linkUris, presetKey + "_phfalse_emfalse_lifalse");

		String pdfNorm = normalizePdfText(extract.text);
		String uriHaystack = normalizePdfText(extract.text + " " + String.join(" ", extract.linkUris));
		assertResumeDerivedFieldsInPdf(parsedCandidate, pdfNorm, uriHaystack, extract.linkUris, true, true, true,
				true);
	}

	@DataProvider
	public Object[][] forestMinimalRedactionMatrix() {
		return new Object[][] { { true, false, false }, { false, true, false }, { false, false, true },
				{ true, true, false }, { true, false, true }, { false, true, true }, { true, true, true }, };
	}

	@Test(dataProvider = "forestMinimalRedactionMatrix")
	public void generateCvHtml_forestMinimal_redaction_pdfOmitsSensitiveData(boolean redactPhone, boolean redactEmail,
			boolean redactLinkedin) throws Exception {
		saveAdminSettings(presetForest, 0, redactionFields(redactPhone, redactEmail, redactLinkedin),
				twoCustomFields(0, 1));
		PdfExtract extract = callGenerateHtmlAndExtractPdf();
		String tag = "forest_minimal_ph" + redactPhone + "_em" + redactEmail + "_li" + redactLinkedin;
		writePdfTextToFileAndLogPreview(extract.text, extract.linkUris, tag);

		String pdfNorm = normalizePdfText(extract.text);
		String uriHaystack = normalizePdfText(extract.text + " " + String.join(" ", extract.linkUris));

		assertResumeDerivedFieldsInPdf(parsedCandidate, pdfNorm, uriHaystack, extract.linkUris, !redactPhone,
				!redactEmail, !redactLinkedin, !redactLinkedin);

		if (redactPhone) {
			assertSensitiveCandidateValueAbsentFromPdf(extract, "contactnumber", stringField(parsedCandidate, "contactnumber"));
		}
		if (redactEmail) {
			assertSensitiveCandidateValueAbsentFromPdf(extract, "emailid", stringField(parsedCandidate, "emailid"));
		}
		if (redactLinkedin) {
			assertSensitiveCandidateValueAbsentFromPdf(extract, "profilelinkedin",
					stringField(parsedCandidate, "profilelinkedin"));
			assertNoLinkedInProfileLeak(extract, stringField(parsedCandidate, "profilelinkedin"));
		}
	}

	@Test
	public void generateCvHtml_executivePrestige_fullRedaction_pdfOmitsSensitiveData() throws Exception {
		saveAdminSettings(presetExecutive, 0, redactionFields(true, true, true), twoCustomFields(0, 1));
		PdfExtract extract = callGenerateHtmlAndExtractPdf();
		writePdfTextToFileAndLogPreview(extract.text, extract.linkUris, "executive_prestige_phtrue_emtrue_litrue");
		String pdfNorm = normalizePdfText(extract.text);
		String uriHaystack = normalizePdfText(extract.text + " " + String.join(" ", extract.linkUris));
		assertResumeDerivedFieldsInPdf(parsedCandidate, pdfNorm, uriHaystack, extract.linkUris, false, false, false,
				false);
		assertSensitiveCandidateValueAbsentFromPdf(extract, "contactnumber", stringField(parsedCandidate, "contactnumber"));
		assertSensitiveCandidateValueAbsentFromPdf(extract, "emailid", stringField(parsedCandidate, "emailid"));
		assertSensitiveCandidateValueAbsentFromPdf(extract, "profilelinkedin",
				stringField(parsedCandidate, "profilelinkedin"));
		assertNoLinkedInProfileLeak(extract, stringField(parsedCandidate, "profilelinkedin"));
	}

	@Test
	public void generateCvHtml_boldEditorial_fullRedaction_pdfOmitsSensitiveData() throws Exception {
		saveAdminSettings(presetBold, 0, redactionFields(true, true, true), twoCustomFields(0, 1));
		PdfExtract extract = callGenerateHtmlAndExtractPdf();
		writePdfTextToFileAndLogPreview(extract.text, extract.linkUris, "bold_editorial_phtrue_emtrue_litrue");
		String pdfNorm = normalizePdfText(extract.text);
		String uriHaystack = normalizePdfText(extract.text + " " + String.join(" ", extract.linkUris));
		assertResumeDerivedFieldsInPdf(parsedCandidate, pdfNorm, uriHaystack, extract.linkUris, false, false, false,
				false);
		assertSensitiveCandidateValueAbsentFromPdf(extract, "contactnumber", stringField(parsedCandidate, "contactnumber"));
		assertSensitiveCandidateValueAbsentFromPdf(extract, "emailid", stringField(parsedCandidate, "emailid"));
		assertSensitiveCandidateValueAbsentFromPdf(extract, "profilelinkedin",
				stringField(parsedCandidate, "profilelinkedin"));
		assertNoLinkedInProfileLeak(extract, stringField(parsedCandidate, "profilelinkedin"));
	}

	@DataProvider
	public Object[][] presetsForCustomFieldInclusion() {
		return new Object[][] { { "forest_minimal" }, { "executive_prestige" }, { "bold_editorial" } };
	}

	@Test(dataProvider = "presetsForCustomFieldInclusion")
	public void generateCvHtml_customFields_includedInPdf_whenNamedInPrompt(String presetKey) throws Exception {
		seedBrandedCustomFieldValues();
		saveAdminSettings(presetPromptWithCustomFieldsInstruction(presetKey), 0, redactionFields(false, false, false),
				brandedCustomFieldsArray());
		PdfExtract extract = callGenerateHtmlAndExtractPdf();
		writePdfTextToFileAndLogPreview(extract.text, extract.linkUris, presetKey + "_customfields_prompt");

		String pdfNorm = normalizePdfText(extract.text);
		Assert.assertTrue(containsLoose(pdfNorm, "Work Authorization"));
		Assert.assertTrue(containsLoose(pdfNorm, "Authorized to work in India"));
		Assert.assertTrue(containsLoose(pdfNorm, "Rating"));
		Assert.assertTrue(containsLoose(pdfNorm, "4.3"));
	}

	@Test
	public void generateCvHtml_invalidToken_expect401() throws Exception {
		JSONObject body = new JSONObject();
		body.put("mode", "generate");
		body.put("candidate_slug", candidateSlug);
		Response resp = RestClient.doPost("JSON", neptuneServiceURL, NEPTUNE_GENERATE_HTML_PATH,
				"invalid-cv-formatting-agent-token", null, true, body);
		Assert.assertEquals(resp.getStatusCode(), 401, resp.asString());
	}

	private PdfExtract callGenerateHtmlAndExtractPdf() throws Exception {
		JSONObject body = new JSONObject();
		body.put("mode", "generate");
		body.put("candidate_slug", candidateSlug);
		Response resp = RestClient.doPost("JSON", neptuneServiceURL, NEPTUNE_GENERATE_HTML_PATH,
				albatrossToken, null, true, body);
		Assert.assertEquals(resp.getStatusCode(), 200, resp.asString());
		Assert.assertEquals(resp.jsonPath().getString("data.status"), "preview_ready");
		Assert.assertNotNull(resp.jsonPath().get("data.document_id"));
		Assert.assertNotNull(resp.jsonPath().get("data.pdf_content"));
		return extractPdfFromBase64(resp.jsonPath().getString("data.pdf_content"));
	}

	private static String stringField(JSONObject obj, String key) {
		if (!obj.has(key) || obj.isNull(key)) {
			return null;
		}
		Object raw = obj.get(key);
		return raw instanceof String ? ((String) raw).trim() : null;
	}

	private String presetPrompt(String presetKey) {
		switch (presetKey) {
		case "forest_minimal":
			return presetForest;
		case "executive_prestige":
			return presetExecutive;
		case "bold_editorial":
			return presetBold;
		default:
			throw new IllegalArgumentException("Unknown preset key: " + presetKey);
		}
	}

	private String presetPromptWithCustomFieldsInstruction(String presetKey) {
		return presetPrompt(presetKey) + CUSTOM_FIELDS_PROMPT_APPEND;
	}

	private JSONArray brandedCustomFieldsArray() {
		JSONArray arr = new JSONArray();
		arr.put(cvCustomColumnWorkAuthId);
		arr.put(cvCustomColumnRatingId);
		return arr;
	}

	private void seedBrandedCustomFieldValues() {
		updateCandidateCustomFieldValue(cvCustomColumnWorkAuthId, CUSTOM_FIELD_WORK_AUTH_VALUE);
		updateCandidateCustomFieldValue(cvCustomColumnRatingId, CUSTOM_FIELD_RATING_VALUE);
	}

	private void updateCandidateCustomFieldValue(int columnId, String value) {
		UpdateFields updateFields = new UpdateFields();
		updateFields.setKey("custcolumn" + columnId);
		updateFields.setValue(value);
		updateFields.setTableFlag("candidate");
		updateFields.setId(Collections.singletonList(candidateId));
		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossToken, null, true,
				updateFields);
		Assert.assertEquals(response.getStatusCode(), 200, response.asString());
		Assert.assertEquals(response.jsonPath().getString("message_type"), "is-success", response.asString());
	}

	private static void assertGenerateHtmlClientError(Response resp) {
		Assert.assertEquals(resp.getStatusCode(), 422, resp.asString());
	}

	private static String sanitizeFileBase(String name) {
		if (name == null || name.isEmpty()) {
			return "output";
		}
		return name.replaceAll("[^a-zA-Z0-9._-]+", "_");
	}

	private void assertSensitiveCandidateValueAbsentFromPdf(PdfExtract extract, String fieldLabel, String rawValue) {
		if (rawValue == null || rawValue.length() < MIN_LEN) {
			return;
		}
		String combinedNorm = normalizePdfText(extract.text + " " + String.join(" ", extract.linkUris));
		String snippet = rawValue.length() > MAX_SNIPPET ? rawValue.substring(0, MAX_SNIPPET) : rawValue;
		String needle = normalizePdfText(snippet);
		if (needle.length() >= MIN_LEN) {
			Assert.assertFalse(containsLoose(combinedNorm, needle), "Redacted field '" + fieldLabel
					+ "' must not appear in PDF text or link URIs (substring match)");
		}
		if ("contactnumber".equals(fieldLabel)) {
			String digits = rawValue.replaceAll("\\D+", "");
			if (digits.length() >= 8) {
				Assert.assertFalse(containsLoose(combinedNorm, digits),
						"Redacted phone number digits must not appear in PDF text or link URIs");
			}
		}
	}

	private void assertNoLinkedInProfileLeak(PdfExtract extract, String linkedinUrl) {
		if (linkedinUrl == null || linkedinUrl.length() < MIN_LEN) {
			return;
		}
		String combinedNorm = normalizePdfText(extract.text + " " + String.join(" ", extract.linkUris));
		String u = linkedinUrl.trim().replaceFirst("(?i)^https?://", "").replaceFirst("(?i)^www\\.", "");
		int idx = u.toLowerCase(Locale.ROOT).indexOf("linkedin.com");
		if (idx >= 0) {
			String tail = u.substring(idx);
			String tailNorm = normalizePdfText(tail);
			if (tailNorm.length() >= MIN_LEN) {
				Assert.assertFalse(containsLoose(combinedNorm, tail),
						"Redacted LinkedIn profile must not appear in PDF text or URIs: " + abbreviate(tail));
			}
		}
	}

	private void writePdfTextToFileAndLogPreview(String pdfText, List<String> linkUris, String fileBaseName)
			throws Exception {
		Path out = Paths.get(System.getProperty("user.dir"), PDF_TEXT_OUTPUT_DIR, sanitizeFileBase(fileBaseName) + ".txt");
		Files.createDirectories(out.getParent());
		StringBuilder fileBody = new StringBuilder();
		if (!linkUris.isEmpty()) {
			fileBody.append("=== PDF link annotation URIs ===\n");
			for (String u : linkUris) {
				fileBody.append(u).append('\n');
			}
			fileBody.append("=== extracted text ===\n");
		}
		fileBody.append(pdfText);
		Files.writeString(out, fileBody.toString(), StandardCharsets.UTF_8);
	}

	private void assertResumeDerivedFieldsInPdf(JSONObject c, String pdfNorm, String uriHaystack,
			List<String> linkUris, boolean expectPhoneInPdf, boolean expectEmailInPdf,
			boolean expectLinkedinInPdf, boolean expectGithubInPdf) {
		assertNonEmptyStringFieldInPdfIfPresent(c, pdfNorm, "firstname");
		assertNonEmptyStringFieldInPdfIfPresent(c, pdfNorm, "lastname");
		if (expectEmailInPdf) {
			assertNonEmptyStringFieldInPdfIfPresent(c, pdfNorm, "emailid");
		}
		if (expectPhoneInPdf) {
			assertNonEmptyStringFieldInPdfIfPresent(c, pdfNorm, "contactnumber");
		}
		assertNonEmptyStringFieldInPdfIfPresent(c, pdfNorm, "position");
		assertNonEmptyStringFieldInPdfIfPresent(c, pdfNorm, "qualification");
		assertLocalityInPdfIfPresent(c, pdfNorm, "city");
		assertLocalityInPdfIfPresent(c, pdfNorm, "state");
		assertSkillTokensInPdfIfPresent(c, pdfNorm);
		assertNonEmptyStringFieldInPdfIfPresent(c, pdfNorm, "lastorganisation");
		if (expectLinkedinInPdf) {
			assertSocialProfileUrlInPdfIfPresent(c, pdfNorm, uriHaystack, linkUris, "profilelinkedin", "LinkedIn");
		}
		if (expectGithubInPdf) {
			assertSocialProfileUrlInPdfIfPresent(c, pdfNorm, uriHaystack, linkUris, "profilegithub", "GitHub");
		}

		if (c.has("workhistory") && !c.isNull("workhistory")) {
			JSONArray wh = c.getJSONArray("workhistory");
			for (int i = 0; i < wh.length(); i++) {
				if (!(wh.get(i) instanceof JSONObject)) {
					continue;
				}
				JSONObject w = wh.getJSONObject(i);
				assertNonEmptyStringFieldInPdfIfPresent(w, pdfNorm, "title");
				assertNonEmptyStringFieldInPdfIfPresent(w, pdfNorm, "work_company_name");
				assertNonEmptyStringFieldInPdfIfPresent(w, pdfNorm, "work_location");
			}
		}

		if (c.has("educationhistory") && !c.isNull("educationhistory")) {
			JSONArray eh = c.getJSONArray("educationhistory");
			for (int i = 0; i < eh.length(); i++) {
				if (!(eh.get(i) instanceof JSONObject)) {
					continue;
				}
				JSONObject e = eh.getJSONObject(i);
				assertNonEmptyStringFieldInPdfIfPresent(e, pdfNorm, "institute_name");
				assertNonEmptyStringFieldInPdfIfPresent(e, pdfNorm, "educational_qualification");
				assertNonEmptyStringFieldInPdfIfPresent(e, pdfNorm, "educational_specialization");
				assertNonEmptyStringFieldInPdfIfPresent(e, pdfNorm, "education_location");
			}
		}
	}

	private void assertSkillTokensInPdfIfPresent(JSONObject c, String pdfNorm) {
		if (!c.has("skill") || c.isNull("skill")) {
			return;
		}
		Object raw = c.get("skill");
		if (!(raw instanceof String)) {
			return;
		}
		String v = ((String) raw).trim();
		if (v.length() < MIN_LEN) {
			return;
		}
		List<String> tokens = new ArrayList<>();
		for (String part : v.split(",")) {
			String t = normalizePdfText(part);
			if (t.length() >= MIN_LEN) {
				tokens.add(t);
			}
		}
		if (tokens.isEmpty()) {
			return;
		}
		int matched = 0;
		for (String token : tokens) {
			if (containsLoose(pdfNorm, token)) {
				matched++;
			}
		}
		int required = (int) Math.ceil(tokens.size() * 0.6);
		required = Math.min(tokens.size(), Math.max(5, required));
		Assert.assertTrue(matched >= required,
				"Expected enough skill tokens in PDF: matched " + matched + " of " + tokens.size()
						+ " (required at least " + required + ")");
	}

	private void assertLocalityInPdfIfPresent(JSONObject c, String pdfNorm, String key) {
		if (!c.has(key) || c.isNull(key)) {
			return;
		}
		Object raw = c.get(key);
		if (!(raw instanceof String)) {
			return;
		}
		String v = ((String) raw).trim();
		if (v.length() < MIN_LEN) {
			return;
		}
		String snippet = v.length() > MAX_SNIPPET ? v.substring(0, MAX_SNIPPET) : v;
		String needle = normalizePdfText(snippet);
		if (needle.length() < MIN_LEN) {
			return;
		}
		if (containsLoose(pdfNorm, needle)) {
			return;
		}
		for (String locLine : collectWorkAndEducationLocationLines(c)) {
			String locNorm = normalizePdfText(locLine);
			if (locNorm.length() < MIN_LEN) {
				continue;
			}
			if (!containsLoose(locNorm.toLowerCase(Locale.ROOT), needle.toLowerCase(Locale.ROOT))) {
				continue;
			}
			if (containsLoose(pdfNorm, locNorm)) {
				return;
			}
			int comma = locNorm.indexOf(',');
			if (comma > 0) {
				String firstPart = locNorm.substring(0, comma).trim();
				if (firstPart.length() >= MIN_LEN && containsLoose(pdfNorm, firstPart)) {
					return;
				}
				String afterComma = locNorm.substring(comma + 1).trim();
				if (afterComma.length() >= MIN_LEN && containsLoose(afterComma, needle)
						&& containsLoose(pdfNorm, afterComma)) {
					return;
				}
			}
		}
		Assert.fail("Expected generated PDF to contain resume field '" + key + "' (snippet: " + abbreviate(needle)
				+ ") or a work/education location line that includes it");
	}

	private static List<String> collectWorkAndEducationLocationLines(JSONObject c) {
		List<String> lines = new ArrayList<>();
		if (c.has("workhistory") && !c.isNull("workhistory")) {
			JSONArray wh = c.getJSONArray("workhistory");
			for (int i = 0; i < wh.length(); i++) {
				if (wh.get(i) instanceof JSONObject) {
					String wloc = wh.getJSONObject(i).optString("work_location", "").trim();
					if (!wloc.isEmpty()) {
						lines.add(wloc);
					}
				}
			}
		}
		if (c.has("educationhistory") && !c.isNull("educationhistory")) {
			JSONArray eh = c.getJSONArray("educationhistory");
			for (int i = 0; i < eh.length(); i++) {
				if (eh.get(i) instanceof JSONObject) {
					String eloc = eh.getJSONObject(i).optString("education_location", "").trim();
					if (!eloc.isEmpty()) {
						lines.add(eloc);
					}
				}
			}
		}
		return lines;
	}

	private void assertNonEmptyStringFieldInPdfIfPresent(JSONObject obj, String pdfNorm, String key) {
		if (!obj.has(key) || obj.isNull(key)) {
			return;
		}
		Object raw = obj.get(key);
		if (!(raw instanceof String)) {
			return;
		}
		String v = ((String) raw).trim();
		if (v.length() < MIN_LEN) {
			return;
		}
		String snippet = v.length() > MAX_SNIPPET ? v.substring(0, MAX_SNIPPET) : v;
		String needle = normalizePdfText(snippet);
		if (needle.length() < MIN_LEN) {
			return;
		}
		Assert.assertTrue(containsLoose(pdfNorm, needle),
				"Expected generated PDF to contain resume field '" + key + "' (snippet: " + abbreviate(needle) + ")");
	}

	private static String abbreviate(String s) {
		return s.length() > 100 ? s.substring(0, 100) + "..." : s;
	}

	private static final class PdfExtract {
		final String text;
		final List<String> linkUris;

		PdfExtract(String text, List<String> linkUris) {
			this.text = text;
			this.linkUris = linkUris;
		}
	}

	private static PdfExtract extractPdfFromBase64(String pdfBase64) throws Exception {
		Assert.assertNotNull(pdfBase64);
		Assert.assertFalse(pdfBase64.isEmpty());
		byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
		Assert.assertTrue(pdfBytes.length > 0);
		try (PDDocument document = PDDocument.load(pdfBytes)) {
			PDFTextStripper stripper = new PDFTextStripper();
			String pdfText = stripper.getText(document);
			Assert.assertNotNull(pdfText);
			Assert.assertFalse(pdfText.trim().isEmpty());
			List<String> uris = extractLinkAnnotationUris(document);
			return new PdfExtract(pdfText, uris);
		}
	}

	private static List<String> extractLinkAnnotationUris(PDDocument document) throws IOException {
		List<String> uris = new ArrayList<>();
		for (PDPage page : document.getPages()) {
			List<PDAnnotation> annotations = page.getAnnotations();
			if (annotations == null) {
				continue;
			}
			for (PDAnnotation ann : annotations) {
				if (!(ann instanceof PDAnnotationLink)) {
					continue;
				}
				collectUrisFromLinkAnnotation((PDAnnotationLink) ann, uris);
			}
		}
		return uris;
	}

	private static void collectUrisFromLinkAnnotation(PDAnnotationLink link, List<String> uris) throws IOException {
		PDAction action = link.getAction();
		if (action instanceof PDActionURI) {
			addUriIfNonEmpty(uris, ((PDActionURI) action).getURI());
		}
		COSDictionary cos = link.getCOSObject();
		COSBase actionObj = cos.getDictionaryObject(COSName.A);
		if (actionObj instanceof COSDictionary) {
			COSDictionary ad = (COSDictionary) actionObj;
			COSBase sBase = ad.getDictionaryObject(COSName.S);
			COSName subtype = sBase instanceof COSName ? (COSName) sBase : null;
			if (subtype != null && COSName.URI.equals(subtype)) {
				COSBase uriObj = ad.getDictionaryObject(COSName.URI);
				if (uriObj instanceof COSString) {
					addUriIfNonEmpty(uris, ((COSString) uriObj).getString());
				} else if (uriObj != null) {
					String asStr = uriObj.toString();
					if (asStr != null && asStr.startsWith("/") && asStr.length() > 1) {
						asStr = asStr.substring(1);
					}
					addUriIfNonEmpty(uris, asStr);
				}
			}
		}
	}

	private static void addUriIfNonEmpty(List<String> uris, String uri) {
		if (uri != null) {
			String t = uri.trim();
			if (!t.isEmpty()) {
				uris.add(t);
			}
		}
	}

	private void assertSocialProfileUrlInPdfIfPresent(JSONObject c, String pdfNorm, String uriHaystack,
			List<String> linkUris, String key, String visibleLabel) {
		if (!c.has(key) || c.isNull(key)) {
			return;
		}
		Object raw = c.get(key);
		if (!(raw instanceof String)) {
			return;
		}
		String v = ((String) raw).trim();
		if (v.length() < MIN_LEN) {
			return;
		}
		if (containsLoose(uriHaystack, v)) {
			return;
		}
		String stripped = v.replaceFirst("(?i)^https?://", "").replaceFirst("(?i)^www\\.", "");
		if (containsLoose(uriHaystack, stripped)) {
			return;
		}
		for (String uri : linkUris) {
			if (uri == null) {
				continue;
			}
			String ul = uri.toLowerCase(Locale.ROOT);
			if (ul.contains(v.toLowerCase(Locale.ROOT))
					|| ul.contains(stripped.toLowerCase(Locale.ROOT))) {
				return;
			}
		}
		if ("profilegithub".equals(key)) {
			String ghTail = githubHostPathNeedle(v);
			if (ghTail != null && ghTail.length() >= MIN_LEN) {
				if (containsLoose(uriHaystack, ghTail) || containsLoose(pdfNorm, ghTail)) {
					return;
				}
			}
			if (v.toLowerCase(Locale.ROOT).contains("github.com")
					&& (containsLoose(uriHaystack, "github.com") || containsLoose(pdfNorm, "github.com"))) {
				return;
			}
		}
		Assert.assertTrue(containsLoose(pdfNorm, visibleLabel),
				"Expected PDF link URI, URL substring, or visible label '" + visibleLabel + "' for " + key);
	}

	private static String githubHostPathNeedle(String profileUrl) {
		if (profileUrl == null) {
			return null;
		}
		String u = profileUrl.trim().replaceFirst("(?i)^https?://", "").replaceFirst("(?i)^www\\.", "");
		int g = u.toLowerCase(Locale.ROOT).indexOf("github.com");
		if (g < 0) {
			return null;
		}
		String tail = u.substring(g);
		return tail.length() > MAX_SNIPPET ? tail.substring(0, MAX_SNIPPET) : tail;
	}

	private static String normalizePdfText(String s) {
		if (s == null) {
			return "";
		}
		String t = s.replace('\u00a0', ' ');
		t = t.replace('\u2011', '-');
		t = t.replaceAll("\\s+", " ");
		return t.trim();
	}

	private static boolean containsLoose(String haystack, String needle) {
		if (needle.isEmpty()) {
			return true;
		}
		String h = haystack.toLowerCase(Locale.ROOT);
		String n = needle.toLowerCase(Locale.ROOT);
		if (h.contains(n)) {
			return true;
		}
		String h2 = h.replaceAll("[^a-z0-9@._+\\-]+", "");
		String n2 = n.replaceAll("[^a-z0-9@._+\\-]+", "");
		return n2.length() >= MIN_LEN && h2.contains(n2);
	}

	private void saveAdminSettings(String instructionPrompt, int includeCompanyLogo, JSONObject redactionFields,
			JSONArray customFields) {
		JSONObject body = new JSONObject();
		body.put("is_admin_setting", true);
		body.put("cv_custom_field_id", cvFileColumnId);
		body.put("custom_fields", customFields);
		body.put("instruction_prompt", instructionPrompt);
		body.put("include_company_logo", includeCompanyLogo);
		body.put("is_reset", false);
		body.put("redaction_fields", redactionFields);
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getString("meta.message_type"), "is-success");
	}

	private static JSONObject redactionFields(boolean phone, boolean email, boolean linkedin) {
		return new JSONObject().put("phone", phone).put("email", email).put("linkedin", linkedin);
	}

	private JSONArray twoCustomFields(int indexA, int indexB) {
		JSONArray arr = new JSONArray();
		arr.put(contentColumnIds.get(indexA));
		arr.put(contentColumnIds.get(indexB));
		return arr;
	}

	private static JSONArray emptyOptions() {
		return new JSONArray();
	}

	private int createCandidateCustomField(String name, String extrafieldtype, int columnId,
			JSONArray defaultoptionsvalue) {
		JSONObject custumField = new JSONObject();
		custumField.put("columnid", columnId);
		custumField.put("extrafieldname", name);
		custumField.put("extrafieldtype", extrafieldtype);
		custumField.put("entitytypeid", ENTITY_CANDIDATE);
		custumField.put("defaultvalue", JSONObject.NULL);
		custumField.put("defaultoptionsvalue", defaultoptionsvalue);
		JSONObject requestBody = new JSONObject();
		requestBody.put("custumField", custumField);
		requestBody.put("deleteSocialFile", false);
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossToken, null, true,
				requestBody);
		Assert.assertEquals(response.getStatusCode(), 200, "Custom field creation failed: " + response.asString());
		return response.jsonPath().getInt("data.custumField.columnid");
	}

}
