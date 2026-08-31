package io.rcrm.api.commanfunctions;

import io.rcrm.api.pojo.stripe.*;
import io.rcrm.api.pojo.stripe.StripePlan.PlanDetails;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.*;
public class StripeCommonFunctions {

    public StripeCommonFunctions() {
        super();
    }

    static final String reaperBaseURL = "https://reaper.recruitcrm.net/";
    static String dbname = System.getProperty("dbname");
    static String reaperUsername = System.getProperty("reaper_username");
    static String reaperPassword = System.getProperty("reaper_password");
    static Map<String, String> reaperAuthTokenMap = new HashMap<String, String>();

    static {
        reaperAuthTokenMap.put("reaper_username", reaperUsername);
        reaperAuthTokenMap.put("reaper_password", reaperPassword);
    }

   
    public Response postUpgradeStripePlan(String albatrossURL, Object albatrossAuthToken, String billingCycle, String planId, int seats, int recordAddOn, int monthlySeats) {

        PlanDetails planDetails = new PlanDetails();
        planDetails.setBillingCycle(billingCycle);
        planDetails.setMonthlySeats(monthlySeats);
        planDetails.setPlanid(planId);
        planDetails.setRecordAddon(recordAddOn);
        planDetails.setSeats(seats);
        StripePlan stripePlan = new StripePlan();
        stripePlan.setPlan(planDetails);

        Response response = RestClient.doPost("JSON", albatrossURL, "plans-and-billing/upgrade-stripe-plan", albatrossAuthToken, null, true, stripePlan);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to update plan for account");
        
        return response;
    }

    public Response updatePlanForAccount(String albatrossURL, Object albatrossAuthToken, String billingCycle, String planId, int seats, int recordAddOn, int monthlySeats) {

        Response response = postUpgradeStripePlan(albatrossURL, albatrossAuthToken, billingCycle, planId, seats, recordAddOn, monthlySeats);
        String actualMessage = response.jsonPath().getString("message");
        Assert.assertTrue(actualMessage.equals("Plan Creation In Progress Successful ") ||
        actualMessage.equals("Payment successful, please hold on while we update your plan.") ||
        actualMessage.equals("Account Plan Updated"), "Unexpected plan update message: " + actualMessage);

        return response;
    }

    public Response getAllInvoicesDataFromAlbatross(String albatrossURL, Object albatrossAuthToken) {
        
        Response response = RestClient.doPost("JSON", albatrossURL, "plans-and-billing/get-all-invoices", albatrossAuthToken, null, false, null); 
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to get all invoices");
        
        return response;
    }

    public Response getAccountDetail(String albatrossURL, String albatrossAuthToken) {

		Response response = RestClient.doGet("JSON", albatrossURL, "get-intercom-settings", albatrossAuthToken, null, null, false);
		Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("status"), "success");

		return response;
	}

    public Response updateCustomerForAccountStripe(int accountId, String customerId) {

        UpdateCustomerForAccountStripeRequest body = new UpdateCustomerForAccountStripeRequest();
        body.setCustomerId(customerId);

        String endUrl = "updateCustomerForAccountStripe/" + dbname + "/" + accountId;
        Response response = RestClient.doPost("JSON", reaperBaseURL, endUrl, reaperAuthTokenMap, null, false, body);
        Assert.assertEquals(response.getStatusCode(), 200);

        return response;
    }

    public Response updateAccountSettingsForStripeAfterRenewal(int accountId) {

        String endUrl = "updateAccountSettingsForStripeAfterRenewal/" + dbname + "/" + accountId;
        Response response = RestClient.doPost("JSON", reaperBaseURL, endUrl, reaperAuthTokenMap, null, false, null);
        Assert.assertEquals(response.getStatusCode(), 200);
        
        return response;
    }

    public Response updateAccountAddonsForStripeAfterRenewal(int accountId) {

        String endUrl = "updateAccountAddonsForStripeAfterRenewal/" + dbname + "/" + accountId;
        Response response = RestClient.doPost("JSON", reaperBaseURL, endUrl, reaperAuthTokenMap, null, false, null);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to update account addons for stripe after renewal");

        return response;
    }

    public Response createMetabaseSubscription(String albatrossURL, Object albatrossAuthToken, String billingInterval) {

        CreateMetabaseSubscriptionRequest body = new CreateMetabaseSubscriptionRequest();

        if ("year".equals(billingInterval)) {
            body.setSelected_plan_id(4);
            body.setSelected_monthly_plan_id(3);
        } else {
            body.setSelected_plan_id(1);
        }

        return RestClient.doPost("JSON", albatrossURL, "metabase/subscription", albatrossAuthToken, null, true, body);
    }

    public Response upgradeMetabaseSubscription(String albatrossURL, Object albatrossAuthToken) {

        CreateMetabaseSubscriptionRequest body = new CreateMetabaseSubscriptionRequest();
        body.setSelected_plan_id(4);
        body.setSelected_monthly_plan_id(3);
        body.setExisting_plan_id(3);

        return RestClient.doPost("JSON", albatrossURL, "metabase/subscription", albatrossAuthToken, null, true, body);
    }

    public Response getMetabaseSubscription(String albatrossURL, Object albatrossAuthToken) {
        
        Response response = RestClient.doGet("JSON", albatrossURL, "metabase/subscription", albatrossAuthToken, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch Advanced Analytics metabase subscription");
        
        return response;
    }

    public Response createUnipileSubscription(String albatrossURL, Object albatrossAuthToken, String billingInterval) {

        CreateMetabaseSubscriptionRequest body = new CreateMetabaseSubscriptionRequest();

        if ("year".equals(billingInterval)) {
            body.setSelected_plan_id(4);
            body.setSelected_monthly_plan_id(3);
        } else {
            body.setSelected_plan_id(1);
        }

        return RestClient.doPost("JSON", albatrossURL, "unipile/subscription", albatrossAuthToken, null, true, body);
    }

    public Response upgradeUnipileSubscription(String albatrossURL, Object albatrossAuthToken) {

        CreateMetabaseSubscriptionRequest body = new CreateMetabaseSubscriptionRequest();
        body.setSelected_plan_id(4);
        body.setSelected_monthly_plan_id(3);
        body.setExisting_plan_id(3);

        return RestClient.doPost("JSON", albatrossURL, "unipile/subscription", albatrossAuthToken, null, true, body);
    }

    public Response getUnipileSubscription(String albatrossURL, Object albatrossAuthToken) {

        Response response = RestClient.doGet("JSON", albatrossURL, "unipile/subscription", albatrossAuthToken, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch LinkedIn Integration unipile subscription");

        return response;
    }

    public Response getDataEnrichmentSubscription(String albatrossURL, Object albatrossAuthToken) {

        Response response = RestClient.doGet("JSON", albatrossURL, "data-enrichment/subscription", albatrossAuthToken, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch Data Enrichment subscription");

        return response;
    }

    public Response getEnterpriseEnrichmentSubscription(String albatrossURL, Object albatrossAuthToken) {

        Response response = RestClient.doGet("JSON", albatrossURL, "enrichment/enterprise-subscription", albatrossAuthToken, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch Enterprise enrichment subscription");

        return response;
    }

    public Response createDataEnrichmentSubscription(String albatrossURL, Object albatrossAuthToken, int selectedPlanId) {

        CreateDataEnrichmentSubscriptionRequest body = new CreateDataEnrichmentSubscriptionRequest(selectedPlanId, null);
        return RestClient.doPost("JSON", albatrossURL, "data-enrichment/subscription", albatrossAuthToken, null, true, body);
    }

    public Response upgradeDataEnrichmentSubscription(String albatrossURL, Object albatrossAuthToken, int selectedPlanId, int existingPlanId) {

        CreateDataEnrichmentSubscriptionRequest body = new CreateDataEnrichmentSubscriptionRequest(selectedPlanId, existingPlanId);
        return RestClient.doPost("JSON", albatrossURL, "data-enrichment/subscription", albatrossAuthToken, null, true, body);
    }

    public Response initiateWorkatoSubscription(String albatrossURL, Object albatrossAuthToken, String planCycle, String task) {

        CreateWorkatoSubscriptionRequest.Plan body = new CreateWorkatoSubscriptionRequest.Plan(null, planCycle, task);
        Response response = RestClient.doPost("JSON", albatrossURL, "workflow-automation/subscription", albatrossAuthToken, null, false, body);
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("message"), "Free automation subscription created successfully");
        
        return response;
    }

    public Response createWorkatoSubscription(String albatrossURL, Object albatrossAuthToken, String planCycle, String task) {

        CreateWorkatoSubscriptionRequest.Plan plan = new CreateWorkatoSubscriptionRequest.Plan(null, planCycle, task);
        CreateWorkatoSubscriptionRequest body = new CreateWorkatoSubscriptionRequest(plan);
        Response response = RestClient.doPost("JSON", albatrossURL, "workflow-automation/subscription", albatrossAuthToken, null, true, body);
        
        return response;
    }

    public Response upgradeWorkatoSubscription(String albatrossURL, Object albatrossAuthToken, int existingPlanId, String planCycle, String task) {

        CreateWorkatoSubscriptionRequest.Plan plan = new CreateWorkatoSubscriptionRequest.Plan(existingPlanId, planCycle, task);
        CreateWorkatoSubscriptionRequest body = new CreateWorkatoSubscriptionRequest(plan);
        return RestClient.doPost("JSON", albatrossURL, "workflow-automation/subscription", albatrossAuthToken, null, true, body);
    }

    public Response getWorkatoSubscription(String albatrossURL, Object albatrossAuthToken) {

        Response response = RestClient.doGet("JSON", albatrossURL, "workflow-automation/subscription", albatrossAuthToken, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch Workato workflow-automation subscription");

        return response;
    }

    public Response getVONQSubscription(String albatrossURL, Object albatrossAuthToken) {

        Response response = RestClient.doGet("JSON", albatrossURL, "vonq/subscription", albatrossAuthToken, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch VONQ subscription");

        return response;
    }

    public Response getTwilioCreditUsage(String albatrossURL, Object albatrossAuthToken) {

        Response response = RestClient.doGet("JSON", albatrossURL, "calling/get-twilio-credit-usage", albatrossAuthToken, null, null, true);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch Twilio credit usage");

        return response;
    }

    public Response makeCallingCreditPayment(String albatrossURL, Object albatrossAuthToken, int price, int quantity) {

        MakeCallingCreditPaymentRequest body = new MakeCallingCreditPaymentRequest(price, quantity, "$", "usd");
        Response response = RestClient.doPost("JSON", albatrossURL, "calling/make-credit-payment", albatrossAuthToken, null, true, body);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to make calling credit payment");

        return response;
    }

    public Response getAllCallingTransactions(String albatrossURL, Object albatrossAuthToken) {

        GetAllCallingTransactionsRequest body = new GetAllCallingTransactionsRequest();
        Response response = RestClient.doPost("JSON", albatrossURL, "calling/get-all-transactions", albatrossAuthToken, null, false, body);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch calling credit transactions");

        return response;
    }

    public Response createVONQSubscription(String albatrossURL, Object albatrossAuthToken, int selectedPlanId, Integer existingPlanId) {

        CreateVONQSubscriptionRequest body = new CreateVONQSubscriptionRequest(selectedPlanId, existingPlanId);
        Response response = RestClient.doPost("JSON", albatrossURL, "vonq/subscription", albatrossAuthToken, null, true, body);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create VONQ subscription");
        
        return response;
    }

    public Response upgradeVONQSubscription(String albatrossURL, Object albatrossAuthToken, int selectedPlanId, int existingPlanId) {

        CreateVONQSubscriptionRequest body = new CreateVONQSubscriptionRequest(selectedPlanId, existingPlanId);
        Response response = RestClient.doPost("JSON", albatrossURL, "vonq/subscription", albatrossAuthToken, null, true, body);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to upgrade VONQ subscription");
        
        return response;
    }

    public Response createTestClock(String name, long frozenTime) {

        CreateTestClockRequest body = new CreateTestClockRequest();
        body.setName(name);
        body.setFrozenTime(frozenTime);

        Response response = RestClient.doPost("JSON", reaperBaseURL, "createTestClock", reaperAuthTokenMap, null, false, body);
        Assert.assertEquals(response.getStatusCode(), 200);
        
        return response;
    }

    public Response createCustomerUsingTestClock(String testClock, String email) {
        
        CreateCustomerUsingTestClockRequest body = new CreateCustomerUsingTestClockRequest();
        body.setTestClock(testClock);
        body.setEmail(email);

        Response response = RestClient.doPost("JSON", reaperBaseURL, "createCustomerUsingTestClock", reaperAuthTokenMap, null, false, body);
        Assert.assertEquals(response.getStatusCode(), 200);
        
        return response;
    }

    public Response createPaymentMethod(String cardNumber, int expMonth, int expYear, String cvc) {
        
        CreatePaymentMethodRequest body = new CreatePaymentMethodRequest();
        body.setCardNumber(cardNumber);
        body.setExpMonth(expMonth);
        body.setExpYear(expYear);
        body.setCvc(cvc);
        
        Response response = RestClient.doPost("JSON", reaperBaseURL, "createPaymentMethod", reaperAuthTokenMap, null, false, body);
        Assert.assertEquals(response.getStatusCode(), 200);
        
        return response;
    }

    public Response attachPaymentMethodToCustomer(String paymentMethodId, String customerId) {
        
        AttachPaymentMethodToCustomerRequest body = new AttachPaymentMethodToCustomerRequest();
        body.setCustomerId(customerId);

        String endUrl = "attachPaymentMethodToCustomer/" + paymentMethodId;
        Response response = RestClient.doPost("JSON", reaperBaseURL, endUrl, reaperAuthTokenMap, null, false, body);
        Assert.assertEquals(response.getStatusCode(), 200);
        
        return response;
    }

    public Response performRenewalAction(String testClockId, long frozenTime) {
        
        PerformRenewalActionRequest body = new PerformRenewalActionRequest();
        body.setFrozenTime(frozenTime);

        String endUrl = "performRenewalAction/" + testClockId;
        Response response = RestClient.doPost("JSON", reaperBaseURL, endUrl, reaperAuthTokenMap, null, true, body);
        Assert.assertEquals(response.getStatusCode(), 200);
        
        return response;
    }

    public Response getRequiredSubscriptionDataFromStripe(GetRequiredSubscriptionDataFromStripeRequest body) {
        
        Response response = RestClient.doPost("JSON", reaperBaseURL, "getRequiredSubscriptionDataFromStripe", reaperAuthTokenMap, null, false, body);
        Assert.assertEquals(response.getStatusCode(), 200);
        
        return response;
    }

    public Response getRequiredSubscriptionDataFromStripe(String customerId, String requestType) {
        
        Response response = getRequiredSubscriptionDataFromStripe(new GetRequiredSubscriptionDataFromStripeRequest(requestType, customerId));
        Assert.assertEquals(response.getStatusCode(), 200);
        
        return response;
    }

    public String getRequiredCustomerDataFromStripe(String customerId) {

        String endUrl = "getRequiredCustomerDataFromStripe/" + customerId;
        Response response = RestClient.doGet("JSON", reaperBaseURL, endUrl, reaperAuthTokenMap, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 200, "getRequiredCustomerDataFromStripe failed for customer " + customerId);
        String defaultPaymentMethodId = response.jsonPath().get("invoice_settings.default_payment_method");
        
        return defaultPaymentMethodId;
    }

    public Response getRequiredChargesDataFromStripe(String customerId) {

        String endUrl = "getRequiredChargesDataFromStripe/" + customerId;
        Response response = RestClient.doGet("JSON", reaperBaseURL, endUrl, reaperAuthTokenMap, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 200, "getRequiredChargesDataFromStripe failed for customer " + customerId);

        return response;
    }

    public void assertDefaultPaymentMethodReadyForPlanUpgrade(String customerId, String paymentMethodId) {
        
        final int maxAttempts = 5;
        String latestPaymentMethodId = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            latestPaymentMethodId = getRequiredCustomerDataFromStripe(customerId);
            if (paymentMethodId.equals(latestPaymentMethodId)) {
                return;
            }
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Assert.fail("Interrupted while waiting for default payment method on customer " + customerId);
                }
            }
        }
        Assert.fail("invoice_settings.default_payment_method did not match expected paymentMethodId after " + maxAttempts);
    }

    public String getRequiredTestClockDataFromStripe(String clockId) {
        
        String endUrl = "getRequiredTestClockDataFromStripe/" + clockId;
        Response response = RestClient.doGet("JSON", reaperBaseURL, endUrl, reaperAuthTokenMap, null, null, false);
        Assert.assertEquals(response.getStatusCode(), 200, "getRequiredTestClockDataFromStripe failed for clockId " + clockId);
        String status = response.jsonPath().getString("status");
        
        return status;
    }

    public void assertTestClockStatusReady(String clockId) {

        final int maxAttempts = 15;
        String status = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            status = getRequiredTestClockDataFromStripe(clockId);
            
            if (status.equals("ready")) {
                return;
            }
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Assert.fail("Interrupted while waiting for test clock ready on clockId " + clockId);
                }
            }
        }
        Assert.fail("Test clock status did not become ready after " + maxAttempts + " attempts for clockId " + clockId);
    }

}