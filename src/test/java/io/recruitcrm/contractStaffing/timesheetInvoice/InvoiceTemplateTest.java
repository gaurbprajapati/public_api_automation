package io.recruitcrm.contractStaffing.timesheetInvoice;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class InvoiceTemplateTest extends ContractStaffingBaseTest {

        // Template field constants
        private static final String TEMPLATE_NAME = "templateName";
        private static final String DUE_IN = "dueIn";
        private static final String IS_DEFAULT = "isDefault";
        private static final String IS_PAY_BILL = "isPayBill";
        private static final String CREATED_BY = "createdBy";

        String albatrossAuthToken;
        String apiAuthToken;
        int ownerAccountID;
        private Map<String, Map<String, Object>> expectedTemplatesData;

        @BeforeClass(alwaysRun = true)
        public void Setup() {
                this.albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
                this.ownerAccountID = ThreadManager.getAccount().getAccountId();
                this.apiAuthToken = ThreadManager.getAccountApiKey();

                // Initialize expected templates data after ownerAccountID is set
                this.initializeExpectedTemplatesData();
        }

        private void initializeExpectedTemplatesData() {
                this.expectedTemplatesData = new HashMap<>();

                // Ensure ownerAccountID is set
                if (this.ownerAccountID == 0) {
                        this.ownerAccountID = ThreadManager.getAccount().getAccountId();
                }

                // Template 1: Contract Job
                Map<String, Object> template1 = new HashMap<>();
                template1.put(TEMPLATE_NAME, "Contract Job");
                template1.put(DUE_IN, "30 Days");
                template1.put(IS_DEFAULT, 3);
                template1.put(IS_PAY_BILL, 1);
                template1.put(CREATED_BY, this.ownerAccountID);
                this.expectedTemplatesData.put("template1", template1);

                // Template 2: Contract Job with Week Days
                Map<String, Object> template2 = new HashMap<>();
                template2.put(TEMPLATE_NAME, "Contract Job with Week Days");
                template2.put(DUE_IN, "30 Days");
                template2.put(IS_DEFAULT, 3);
                template2.put(IS_PAY_BILL, 1);
                this.expectedTemplatesData.put("template2", template2);
        }

        @Owner("Gaurav Prajapati")
        @Test(groups = {"contract_staffing", "nightly-build"})
        public void getInvoiceTemplateTest() {
                // Initialize expected templates data if not already initialized
                if (this.expectedTemplatesData == null) {
                        this.initializeExpectedTemplatesData();
                }

                Response response = this.getInvoiceTemplate(this.albatrossAuthToken);

                // Validate status code
                assertThat("Response status code should be 200", response.statusCode(), is(200));

                JsonPath jsonPath = response.jsonPath();

                // Validate meta information
                assertThat("Meta message should match",
                                jsonPath.getString("meta.message"),
                                is("Invoice Pay Bill templates fetched successfully"));
                assertThat("Response type context should match",
                                jsonPath.getString("meta.responseType.context"),
                                is("Request is successful"));
                assertThat("Request UUID should not be null",
                                jsonPath.getString("meta.requestUuid"),
                                is(notNullValue()));

                // Validate data structure
                assertThat("Templates list should contain 2 templates",
                                jsonPath.getList("data.templates").size(),
                                is(2));
                assertThat("Total count should be 2",
                                jsonPath.getInt("data.totalCount"),
                                is(2));

                // Get expected template data
                Map<String, Object> expectedTemplate1 = this.expectedTemplatesData.get("template1");
                Map<String, Object> expectedTemplate2 = this.expectedTemplatesData.get("template2");

                // Validate Template 1: Contract Job
                assertThat("Template 1 name should match expected",
                                jsonPath.getString("data.templates[0].templateName"),
                                is(expectedTemplate1.get(TEMPLATE_NAME)));
                assertThat("Template 1 dueIn should match expected",
                                jsonPath.getString("data.templates[0].dueIn"),
                                is(expectedTemplate1.get(DUE_IN)));
                assertThat("Template 1 isDefault should match expected",
                                jsonPath.getInt("data.templates[0].isDefault"),
                                is(expectedTemplate1.get(IS_DEFAULT)));
                assertThat("Template 1 isPayBill should match expected",
                                jsonPath.getInt("data.templates[0].isPayBill"),
                                is(expectedTemplate1.get(IS_PAY_BILL)));
                assertThat("Template 1 createdBy should match expected",
                                jsonPath.getInt("data.templates[0].createdBy"),
                                is(notNullValue()));
                assertThat("Template 1 id should not be null",
                                jsonPath.getInt("data.templates[0].id"),
                                is(notNullValue()));
                assertThat("Template 1 shareWith should be null",
                                jsonPath.get("data.templates[0].shareWith"),
                                is(nullValue()));
                assertThat("Template 1 templateTableFields should not be empty",
                                jsonPath.getString("data.templates[0].templateTableFields"),
                                is(not(emptyOrNullString())));
                assertThat("Template 1 templateTheme should not be empty",
                                jsonPath.getString("data.templates[0].templateTheme"),
                                is(not(emptyOrNullString())));
                assertThat("Template 1 createdOn should be greater than 0",
                                jsonPath.getLong("data.templates[0].createdOn"),
                                is(greaterThan(0L)));
                assertThat("Template 1 updatedOn should be greater than 0",
                                jsonPath.getLong("data.templates[0].updatedOn"),
                                is(greaterThan(0L)));
                assertThat("Template 1 collaboratorsIds should not be empty",
                                jsonPath.getString("data.templates[0].collaboratorsIds"),
                                is(not(emptyOrNullString())));

                // Validate Template 2: Contract Job with Week Days
                assertThat("Template 2 name should match expected",
                                jsonPath.getString("data.templates[1].templateName"),
                                is(expectedTemplate2.get(TEMPLATE_NAME)));
                assertThat("Template 2 dueIn should match expected",
                                jsonPath.getString("data.templates[1].dueIn"),
                                is(expectedTemplate2.get(DUE_IN)));
                assertThat("Template 2 isDefault should match expected",
                                jsonPath.getInt("data.templates[1].isDefault"),
                                is(expectedTemplate2.get(IS_DEFAULT)));
                assertThat("Template 2 isPayBill should match expected",
                                jsonPath.getInt("data.templates[1].isPayBill"),
                                is(expectedTemplate2.get(IS_PAY_BILL)));
                assertThat("Template 2 createdBy should match expected",
                                jsonPath.getInt("data.templates[1].createdBy"),
                                is(notNullValue()));
                assertThat("Template 2 id should not be null",
                                jsonPath.getInt("data.templates[1].id"),
                                is(notNullValue()));
                assertThat("Template 2 shareWith should be null",
                                jsonPath.get("data.templates[1].shareWith"),
                                is(nullValue()));
                assertThat("Template 2 templateTableFields should not be empty",
                                jsonPath.getString("data.templates[1].templateTableFields"),
                                is(not(emptyOrNullString())));
                assertThat("Template 2 templateTheme should not be empty",
                                jsonPath.getString("data.templates[1].templateTheme"),
                                is(not(emptyOrNullString())));
                assertThat("Template 2 createdOn should be greater than 0",
                                jsonPath.getLong("data.templates[1].createdOn"),
                                is(greaterThan(0L)));
                assertThat("Template 2 updatedOn should be greater than 0",
                                jsonPath.getLong("data.templates[1].updatedOn"),
                                is(greaterThan(0L)));
                assertThat("Template 2 collaboratorsIds should not be empty",
                                jsonPath.getString("data.templates[1].collaboratorsIds"),
                                is(not(emptyOrNullString())));

                // Validate template names contain expected keywords
                assertThat("Template 1 should contain 'Contract Job'",
                                jsonPath.getString("data.templates[0].templateName"),
                                containsString("Contract Job"));
                assertThat("Template 2 should contain 'Week Days'",
                                jsonPath.getString("data.templates[1].templateName"),
                                containsString("Week Days"));
        }

}
