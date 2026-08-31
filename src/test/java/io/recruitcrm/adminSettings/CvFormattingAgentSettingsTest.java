package io.recruitcrm.adminSettings;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@TestBase.AccountType("Business|AlbatrossTkn")
public class CvFormattingAgentSettingsTest extends TestBase {

	private static final String PATH_SAVE = "admin-settings/save-cv-formatting-agent-settings";
	private static final String PATH_GET = "admin-settings/get-cv-formatting-agent-settings";
	private static final int ENTITY_CANDIDATE = 5;
	private static final String PRESET_DIR = "src/test/resources/cvFormattingAgent/presets/";
	private static final String CUSTOM_PRESET_MIN_CHARS = "Clean minimal layout, navy header, white body.";
	private static final String CUSTOM_PRESET_EXACTLY_30 = "Bold editorial theme, minimal.";

	private String albatrossToken;
	private String presetForest;
	private String presetExecutive;
	private String presetBold;
	private int cvFileColumnId;
	private final List<Integer> contentColumnIds = new ArrayList<>();

	@BeforeClass
	public void beforeClassCvFormattingAgent() throws Exception {
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
		Assert.assertNotNull(albatrossToken, "Albatross token must not be null");
		presetForest = Files.readString(Paths.get(PRESET_DIR + "forest_minimal.txt"), StandardCharsets.UTF_8);
		presetExecutive = Files.readString(Paths.get(PRESET_DIR + "executive_prestige.txt"), StandardCharsets.UTF_8);
		presetBold = Files.readString(Paths.get(PRESET_DIR + "bold_editorial.txt"), StandardCharsets.UTF_8);
		int base = 48 + ThreadLocalRandom.current().nextInt(0, 15);
		int slot = base;
		String u = UUID.randomUUID().toString().substring(0, 8);
		cvFileColumnId = createCandidateCustomField("CV_FMT_FILE_" + u, "file", slot++, emptyOptions());
		contentColumnIds.add(createCandidateCustomField("CV_FMT_TEXT_" + u, "text", slot++, emptyOptions()));
		contentColumnIds.add(createCandidateCustomField("CV_FMT_NUM_" + u, "number", slot++, emptyOptions()));
		contentColumnIds.add(createCandidateCustomField("CV_FMT_LT_" + u, "longtext", slot++, emptyOptions()));
		contentColumnIds.add(createCandidateCustomField("CV_FMT_PH_" + u, "phonenumber", slot++, emptyOptions()));
		contentColumnIds.add(createCandidateCustomField("CV_FMT_CB_" + u, "checkbox", slot++, emptyOptions()));
		contentColumnIds.add(createCandidateCustomField("CV_FMT_DD_" + u, "dropdown", slot++, dropdownOptions("A1", "A2", "A3")));
		contentColumnIds.add(createCandidateCustomField("CV_FMT_DT_" + u, "date", slot++, emptyOptions()));
		contentColumnIds.add(createCandidateCustomField("CV_FMT_SO_" + u, "social_profile", slot++, emptyOptions()));
		contentColumnIds.add(createCandidateCustomField("CV_FMT_MS_" + u, "multiselect", slot++, dropdownOptions("M1", "M2")));
		contentColumnIds.add(createCandidateCustomField("CV_FMT_TX2_" + u, "text", slot++, emptyOptions()));
		contentColumnIds.add(createCandidateCustomField("CV_FMT_TX3_" + u, "text", slot++, emptyOptions()));
		Assert.assertEquals(contentColumnIds.size(), 11);
	}

	@Test
	public void getCvFormattingSettings_success() {
		Response r = RestClient.doGet("JSON", albatrossURL, PATH_GET, albatrossToken, null, null, true);
		Assert.assertEquals(r.getStatusCode(), 200);
		Assert.assertEquals(r.jsonPath().getString("meta.message_type"), "success");
		Assert.assertEquals(r.jsonPath().getInt("meta.status"), 200);
		Assert.assertEquals(r.jsonPath().getList("data").size(), 1);
	}

	@DataProvider
	public Object[][] savePresetRedactionMatrix() {
		return new Object[][] {
				{ presetForest, false, false, false, 0 },
				{ presetForest, true, true, true, 0 },
				{ presetForest, true, false, false, 0 },
				{ presetForest, false, true, false, 0 },
				{ presetForest, false, false, true, 0 },
				{ presetForest, true, true, false, 0 },
				{ presetForest, true, false, true, 0 },
				{ presetForest, false, true, true, 0 },
				{ presetExecutive, false, false, false, 0 },
				{ presetExecutive, true, true, true, 0 },
				{ presetExecutive, true, false, false, 0 },
				{ presetExecutive, false, true, false, 0 },
				{ presetExecutive, false, false, true, 0 },
				{ presetExecutive, true, true, false, 0 },
				{ presetExecutive, true, false, true, 0 },
				{ presetExecutive, false, true, true, 0 },
				{ presetBold, false, false, false, 0 },
				{ presetBold, true, true, true, 0 },
				{ presetBold, true, false, false, 0 },
				{ presetBold, false, true, false, 0 },
				{ presetBold, false, false, true, 0 },
				{ presetBold, true, true, false, 0 },
				{ presetBold, true, false, true, 0 },
				{ presetBold, false, true, true, 0 },
		};
	}

	@Test(dataProvider = "savePresetRedactionMatrix")
	public void saveCvFormattingSettings_presetAndRedactionMatrix(String instructionPrompt, boolean phone,
			boolean email, boolean linkedin, int includeCompanyLogo) {
		JSONArray customFields = twoCustomFields(0, 1);
		JSONObject resetBody = adminSaveBody(instructionPrompt, includeCompanyLogo,
				redactionFields(false, false, false), customFields, false);
		RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, resetBody);

		JSONObject body = adminSaveBody(instructionPrompt, includeCompanyLogo,
				redactionFields(phone, email, linkedin), customFields, false);
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getString("meta.message_type"), "is-success");
		Assert.assertTrue(save.jsonPath().getString("meta.message").toLowerCase().contains("saved"));

		Response get = RestClient.doGet("JSON", albatrossURL, PATH_GET, albatrossToken, null, null, true);
		Assert.assertEquals(get.getStatusCode(), 200);
		Assert.assertEquals(get.jsonPath().getString("data[0].instruction_prompt"), instructionPrompt);
		Assert.assertEquals(get.jsonPath().getInt("data[0].include_company_logo"), includeCompanyLogo);
		Assert.assertEquals(get.jsonPath().getInt("data[0].cv_custom_field_id"), cvFileColumnId);
		assertRedactionFieldsMatch(get, phone, email, linkedin);
	}

	@DataProvider
	public Object[][] companyLogoValues() {
		return new Object[][] { { 0 }, { 1 } };
	}

	@Test(dataProvider = "companyLogoValues")
	public void saveCvFormattingSettings_includeCompanyLogoVariants(int logo) {
		JSONArray customFields = twoCustomFields(2, 3);
		JSONObject body = adminSaveBody(presetForest, logo, redactionFields(false, true, true), customFields, false);
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getString("meta.message_type"), "is-success");
		Response get = RestClient.doGet("JSON", albatrossURL, PATH_GET, albatrossToken, null, null, true);
		Assert.assertEquals(get.jsonPath().getInt("data[0].include_company_logo"), logo);
	}

	@Test
	public void saveCvFormattingSettings_emptyCustomFieldsArray_success() {
		JSONArray emptyCustomFields = new JSONArray();
		JSONObject body = adminSaveBody(presetForest, 0, redactionFields(false, false, false), emptyCustomFields, false);
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getString("meta.message_type"), "is-success");
		Response get = RestClient.doGet("JSON", albatrossURL, PATH_GET, albatrossToken, null, null, true);
		Assert.assertEquals(get.getStatusCode(), 200);
		List<Object> saved = get.jsonPath().getList("data[0].custom_fields");
		Assert.assertNotNull(saved);
		Assert.assertEquals(saved.size(), 0);
	}

	@Test
	public void saveCvFormattingSettings_popupStyleMinimalPayload() {
		JSONArray customFields = twoCustomFields(4, 5);
		JSONObject body = new JSONObject();
		body.put("is_admin_setting", false);
		body.put("cv_custom_field_id", cvFileColumnId);
		body.put("custom_fields", customFields);
		body.put("is_reset", false);
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getString("meta.message_type"), "is-success");
	}

	@Test
	public void saveCvFormattingSettings_customFieldsMaxTen() {
		JSONArray customFields = new JSONArray();
		for (int i = 0; i < 10; i++) {
			customFields.put(contentColumnIds.get(i));
		}
		JSONObject body = adminSaveBody(presetExecutive, 1, redactionFields(true, false, true), customFields, false);
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getString("meta.message_type"), "is-success");
		Response get = RestClient.doGet("JSON", albatrossURL, PATH_GET, albatrossToken, null, null, true);
		List<Object> saved = get.jsonPath().getList("data[0].custom_fields");
		Assert.assertEquals(saved.size(), 10);
	}

	@Test
	public void saveCvFormattingSettings_customFieldsEleven_expect422() {
		JSONArray customFields = new JSONArray();
		for (int i = 0; i < 11; i++) {
			customFields.put(contentColumnIds.get(i));
		}
		JSONObject body = adminSaveBody(presetBold, 0, redactionFields(false, false, false), customFields, false);
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getInt("meta.status"), 422);
		Assert.assertEquals(save.jsonPath().getString("meta.message_type"), "is-danger");
		Assert.assertTrue(save.jsonPath().getString("meta.message").toLowerCase().contains("10"));
	}

	@Test
	public void saveCvFormattingSettings_customPreset_aboveMinChars_success() {
		Assert.assertTrue(CUSTOM_PRESET_MIN_CHARS.length() >= 30,
				"Test preset must be >= 30 chars, got " + CUSTOM_PRESET_MIN_CHARS.length());
		JSONArray customFields = twoCustomFields(0, 1);
		JSONObject body = adminSaveBody(CUSTOM_PRESET_MIN_CHARS, 0, redactionFields(false, false, false), customFields,
				false);
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getString("meta.message_type"), "is-success");
		Response get = RestClient.doGet("JSON", albatrossURL, PATH_GET, albatrossToken, null, null, true);
		Assert.assertEquals(get.jsonPath().getString("data[0].instruction_prompt"), CUSTOM_PRESET_MIN_CHARS);
	}

	@Test
	public void saveCvFormattingSettings_customPreset_exactlyThirtyChars_success() {
		Assert.assertEquals(CUSTOM_PRESET_EXACTLY_30.length(), 30,
				"Test preset must be exactly 30 chars, got " + CUSTOM_PRESET_EXACTLY_30.length());
		JSONArray customFields = twoCustomFields(0, 1);
		JSONObject body = adminSaveBody(CUSTOM_PRESET_EXACTLY_30, 0, redactionFields(false, false, false), customFields,
				false);
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getString("meta.message_type"), "is-success");
	}

	@Test
	public void saveCvFormattingSettings_customPreset_belowMinChars_expect422() {
		String shortPrompt = "Short";
		Assert.assertTrue(shortPrompt.length() < 30, "Test preset must be < 30 chars");
		JSONArray customFields = twoCustomFields(0, 1);
		JSONObject body = adminSaveBody(shortPrompt, 0, redactionFields(false, false, false), customFields, false);
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getInt("meta.status"), 422);
		Assert.assertFalse(save.jsonPath().getString("meta.message_type").equals("is-success"),
				"Short prompt should not succeed");
	}

	@Test
	public void saveCvFormattingSettings_noPreset_isAdminTrue_expect422() {
		JSONArray customFields = twoCustomFields(0, 1);
		JSONObject body = new JSONObject();
		body.put("is_admin_setting", true);
		body.put("cv_custom_field_id", cvFileColumnId);
		body.put("custom_fields", customFields);
		body.put("include_company_logo", 0);
		body.put("is_reset", false);
		body.put("redaction_fields", redactionFields(false, false, false));
		Response save = RestClient.doPost("JSON", albatrossURL, PATH_SAVE, albatrossToken, null, true, body);
		Assert.assertEquals(save.getStatusCode(), 200, save.asString());
		Assert.assertEquals(save.jsonPath().getInt("meta.status"), 422);
		Assert.assertFalse(save.jsonPath().getString("meta.message_type").equals("is-success"),
				"Missing instruction_prompt in admin setting should not succeed");
	}

	@Test
	public void getCvFormattingSettings_invalidToken_expect401() {
		Response r = RestClient.doGet("JSON", albatrossURL, PATH_GET, "not-a-valid-jwt-token", null, null, true);
		Assert.assertEquals(r.getStatusCode(), 401);
	}

	private JSONObject adminSaveBody(String instructionPrompt, int includeCompanyLogo, JSONObject redactionFields,
			JSONArray customFields, boolean isReset) {
		JSONObject body = new JSONObject();
		body.put("is_admin_setting", true);
		body.put("cv_custom_field_id", cvFileColumnId);
		body.put("custom_fields", customFields);
		body.put("instruction_prompt", instructionPrompt);
		body.put("include_company_logo", includeCompanyLogo);
		body.put("is_reset", isReset);
		body.put("redaction_fields", redactionFields);
		return body;
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

	private static JSONArray dropdownOptions(String... labels) {
		JSONArray arr = new JSONArray();
		int seq = 1;
		for (String label : labels) {
			arr.put(new JSONObject()
					.put("label", label)
					.put("sequence_no", seq++)
					.put("tempId", UUID.randomUUID().toString()));
		}
		return arr;
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

	private void assertRedactionFieldsMatch(Response getResponse, boolean expectedPhone, boolean expectedEmail,
			boolean expectedLinkedin) {
		boolean p = toBool(getResponse.jsonPath().get("data[0].redaction_fields.phone"));
		boolean e = toBool(getResponse.jsonPath().get("data[0].redaction_fields.email"));
		boolean l = toBool(getResponse.jsonPath().get("data[0].redaction_fields.linkedin"));
		Assert.assertEquals(p, expectedPhone, "redaction_fields.phone mismatch");
		Assert.assertEquals(e, expectedEmail, "redaction_fields.email mismatch");
		Assert.assertEquals(l, expectedLinkedin, "redaction_fields.linkedin mismatch");
	}

	private static boolean toBool(Object v) {
		if (v == null) {
			return false;
		}
		if (v instanceof Boolean) {
			return (Boolean) v;
		}
		if (v instanceof Number) {
			return ((Number) v).intValue() != 0;
		}
		return "true".equalsIgnoreCase(String.valueOf(v)) || "1".equals(String.valueOf(v));
	}
}
