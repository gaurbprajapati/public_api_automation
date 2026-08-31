package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptionsWithAllAddons_Test extends StripeRenewalAutomationBase_Test {

    public StripeRenewalForMainSubscriptionsWithAllAddons_Test() {
        super();
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForProWithVONQAndDataEnrichmentAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        int dataEnrichmentPlanId = annualBilling ? DE_3000_BUCKET_ANNUALLY : DE_250_BUCKET_MONTHLY;
        int expectedPlanCredits = annualBilling ? 3000 : 250;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Pro", vonqPlanId);
        subscribeToDataEnrichmentAndAssert(dataEnrichmentPlanId);
        assertVONQActive("Pro", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Pro", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, recordAddOn);
        assertVONQActive("Pro", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertCallingCreditsState("Pro", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal: one new invoice per active subscription (main, VONQ, Data Enrichment)");
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForProWithVONQAndWorkatoAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Pro", vonqPlanId);
        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        assertVONQActive("Pro", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, true, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Pro", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, recordAddOn);
        assertVONQActive("Pro", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertCallingCreditsState("Pro", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal: one new invoice per active subscription (main, VONQ, Workato)");
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, true, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForProWithWorkatoAndAdvanceAnalyticsAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        subscribeToAdvanceAnalyticsAndAssert(billingCycle);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, true, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Pro", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, recordAddOn);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertCallingCreditsState("Pro", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal: one new invoice per active subscription (main, Workato, Advanced Analytics)");
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, true, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForBusinessWithVONQAndDataEnrichmentAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        int dataEnrichmentPlanId = annualBilling ? DE_3000_BUCKET_ANNUALLY : DE_250_BUCKET_MONTHLY;
        int expectedPlanCredits = annualBilling ? 3000 : 250;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Business", vonqPlanId);
        subscribeToDataEnrichmentAndAssert(dataEnrichmentPlanId);
        assertVONQActive("Business", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Business", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, recordAddOn);
        assertVONQActive("Business", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertCallingCreditsState("Business", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal: one new invoice per active subscription (main, VONQ, Data Enrichment)");
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForBusinessWithVONQAndWorkatoAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Business", vonqPlanId);
        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        assertVONQActive("Business", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, true, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Business", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, recordAddOn);
        assertVONQActive("Business", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertCallingCreditsState("Business", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal: one new invoice per active subscription (main, VONQ, Workato)");
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, true, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForBusinessWithWorkatoAndLinkedInIntegrationAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        subscribeToLinkedinIntegrationAndAssert(billingCycle);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertLinkedinIntegrationActive(billingCycle, 1);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, true, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Business", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, recordAddOn);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertLinkedinIntegrationActive(billingCycle, 1);
        assertCallingCreditsState("Business", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal: one new invoice per active subscription (main, Workato, LinkedIn Integration)");
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, true, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForEnterpriseWithVONQAndDataEnrichmentAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        int dataEnrichmentPlanId = annualBilling ? DE_3000_BUCKET_ANNUALLY : DE_250_BUCKET_MONTHLY;
        int expectedPlanCredits = annualBilling ? 3000 : 250;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Enterprise", "Enterprise", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Enterprise", vonqPlanId);
        subscribeToDataEnrichmentAndAssert(dataEnrichmentPlanId);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Enterprise", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, recordAddOn);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertCallingCreditsState("Enterprise", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal: one new invoice per active subscription (main, VONQ, Data Enrichment)");
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForEnterpriseWithWorkatoAndAdvanceAnalyticsBlockedAddons_Test(String billingCycle) {
        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Enterprise", "Enterprise", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        Response workatoBlockedResponse = function.createWorkatoSubscription(albatrossURL, albatrossAuthToken, billingCycle, WORKATO_TASK_1K);
        Assert.assertEquals(workatoBlockedResponse.getStatusCode(), 422, "Workato subscription attempt should return 422 for Enterprise account");
        Assert.assertEquals(workatoBlockedResponse.jsonPath().getString("message_type"), "is-danger", "Workato subscription attempt should return 422 for Enterprise account");
        Assert.assertEquals(workatoBlockedResponse.jsonPath().getString("message"), "The selected plan.plan cycle is invalid.");
        Assert.assertTrue(workatoBlockedResponse.jsonPath().getList("data").isEmpty(), "Workato activeSubscription should be empty on Enterprise account");
        
        Response subscription = function.getWorkatoSubscription(albatrossURL, albatrossAuthToken);
        Assert.assertEquals(subscription.jsonPath().getString("message"), "Automation subscription fetched successfully");
        Assert.assertEquals(subscription.jsonPath().getBoolean("data.isPlanExist"), true, "Workato isPlanExist should be true for enterprise subscribed account");
        Assert.assertNull(subscription.jsonPath().get("data.activePlan"), "Workato activePlan should be null for unsubscribed account");

        Response blockedResponse = function.createMetabaseSubscription(albatrossURL, albatrossAuthToken, billingCycle);
        Assert.assertEquals(blockedResponse.getStatusCode(), 200, "Advanced Analytics subscription attempt should return 200 for Enterprise account");
        Assert.assertEquals(blockedResponse.jsonPath().getString("response_message"), "Account has Enterprise plan, Metabase subscription is not allowed to update seats");
        Assert.assertTrue(blockedResponse.jsonPath().getList("data.activeSubscription").isEmpty(), "Advanced Analytics activeSubscription should be empty on Enterprise account");
        Assert.assertTrue(blockedResponse.jsonPath().getList("data.cancelledSubscription").isEmpty(), "Advanced Analytics cancelledSubscription should be empty on Enterprise account");
        
        Response sub = function.getMetabaseSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(sub.jsonPath().get("message"), "Metabase access available through Enterprise plan");
		Assert.assertTrue(sub.jsonPath().getList("data.activeSubscription").isEmpty(), "Advanced Analytics activeSubscription should be empty");
		Assert.assertEquals(sub.jsonPath().getBoolean("data.enterprisePlan"), true, "Advanced Analytics enterprisePlan should be true");
        Assert.assertEquals(sub.jsonPath().getBoolean("data.isMetabaseAccountActive"), false, "Advanced Analytics isMetabaseAccountActive should be false");

        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForProToEnterpriseWithVONQAndDataEnrichmentAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        int dataEnrichmentPlanId = annualBilling ? DE_3000_BUCKET_ANNUALLY : DE_250_BUCKET_MONTHLY;
        int expectedPlanCredits = annualBilling ? 3000 : 250;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Pro", vonqPlanId);
        subscribeToDataEnrichmentAndAssert(dataEnrichmentPlanId);
        assertVONQActive("Pro", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Enterprise", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, false, true, true);
        assertEnterpriseIncludedAddons(false);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Enterprise", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertCallingCreditsState("Enterprise", customerId);
        assertEnterpriseIncludedAddons(true);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Pro to Enterprise upgrade (main, VONQ, Data Enrichment)");
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForProToEnterpriseWithVONQAndWorkatoAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Pro", vonqPlanId);
        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        assertVONQActive("Pro", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Enterprise", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, false, true, true);
        assertEnterpriseIncludedAddons(false);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Enterprise", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertCallingCreditsState("Enterprise", customerId);
        assertEnterpriseIncludedAddons(true);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Pro to Enterprise upgrade (main, VONQ, Workato)");
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForProToEnterpriseWithWorkatoAndAdvanceAnalyticsAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        subscribeToAdvanceAnalyticsAndAssert(billingCycle);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Enterprise", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertEnterpriseIncludedAddons(false);
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 4);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, false, true, true);
        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Enterprise", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 2, 2);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertCallingCreditsState("Enterprise", customerId);
        assertEnterpriseIncludedAddons(true);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 2, "Albatross invoice count must increase by 1 after " + billingCycle + " renewal following Pro to Enterprise upgrade (main subscription only)");
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, false, false, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForProToBusinessWithVONQAndDataEnrichmentAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        int dataEnrichmentPlanId = annualBilling ? DE_3000_BUCKET_ANNUALLY : DE_250_BUCKET_MONTHLY;
        int expectedPlanCredits = annualBilling ? 3000 : 250;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Pro", vonqPlanId);
        subscribeToDataEnrichmentAndAssert(dataEnrichmentPlanId);
        assertVONQActive("Pro", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Business", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertVONQActive("Business", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, false, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Business", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertVONQActive("Business", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertCallingCreditsState("Business", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Pro to Business upgrade (main, VONQ, Data Enrichment)");
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForProToBusinessWithVONQAndWorkatoAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Pro", vonqPlanId);
        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        assertVONQActive("Pro", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Business", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertVONQActive("Business", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, false, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Business", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertVONQActive("Business", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertCallingCreditsState("Business", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Pro to Business upgrade (main, VONQ, Workato)");
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForProToBusinessWithWorkatoAndAdvanceAnalyticsAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        subscribeToAdvanceAnalyticsAndAssert(billingCycle);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Business", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, false, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Business", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertCallingCreditsState("Business", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Pro to Business upgrade (main, Workato, Advanced Analytics)");
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForBusinessToProWithVONQAndDataEnrichmentAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        int dataEnrichmentPlanId = annualBilling ? DE_3000_BUCKET_ANNUALLY : DE_250_BUCKET_MONTHLY;
        int expectedPlanCredits = annualBilling ? 3000 : 250;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Business", vonqPlanId);
        subscribeToDataEnrichmentAndAssert(dataEnrichmentPlanId);
        assertVONQActive("Business", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertVONQActive("Pro", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, false, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Pro", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertVONQActive("Pro", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertCallingCreditsState("Pro", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Business to Pro downgrade (main, VONQ, Data Enrichment)");
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForBusinessToProWithVONQAndWorkatoAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Business", vonqPlanId);
        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        assertVONQActive("Business", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertVONQActive("Pro", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, false, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Pro", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertVONQActive("Pro", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertCallingCreditsState("Pro", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Business to Pro downgrade (main, VONQ, Workato)");
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForBusinessToProWithWorkatoAndAdvanceAnalyticsAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        subscribeToAdvanceAnalyticsAndAssert(billingCycle);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, false, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Pro", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertCallingCreditsState("Pro", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Business to Pro downgrade (main, Workato, Advanced Analytics)");
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForEnterpriseToProWithVONQAndDataEnrichmentAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        int dataEnrichmentPlanId = annualBilling ? DE_3000_BUCKET_ANNUALLY : DE_250_BUCKET_MONTHLY;
        int expectedPlanCredits = annualBilling ? 3000 : 250;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Enterprise", "Enterprise", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Enterprise", vonqPlanId);
        subscribeToDataEnrichmentAndAssert(dataEnrichmentPlanId);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);
        assertEnterpriseIncludedAddons(false);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertEnterpriseIncludedAddonsExpired();
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertVONQActive("Pro", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, false, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Pro", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertVONQActive("Pro", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertCallingCreditsState("Pro", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Enterprise to Pro downgrade (main, VONQ, Data Enrichment)");
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForEnterpriseToProWithVONQAndWorkatoAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Enterprise", "Enterprise", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Enterprise", vonqPlanId);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
        assertEnterpriseIncludedAddons(false);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertEnterpriseIncludedAddonsExpired();
        assertVONQActive("Pro", billingCycle, 7);
        
        Response response = function.createWorkatoSubscription(albatrossURL, albatrossAuthToken, workatoPlanCycle, WORKATO_TASK_1K);
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("response_message"), "Subscription created successfully");

        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, false, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Pro", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertVONQActive("Pro", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertCallingCreditsState("Pro", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Enterprise to Pro downgrade (main, VONQ, Workato)");
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForEnterpriseToProWithWorkatoAndAdvanceAnalyticsAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Enterprise", "Enterprise", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);
        assertEnterpriseIncludedAddons(false);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertEnterpriseIncludedAddonsExpired();
        
        Response response = function.createWorkatoSubscription(albatrossURL, albatrossAuthToken, workatoPlanCycle, WORKATO_TASK_1K);
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("response_message"), "Subscription created successfully");
        
        subscribeToAdvanceAnalyticsAndAssert(billingCycle);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, false, true, true);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Pro", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, 1);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertCallingCreditsState("Pro", customerId);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Enterprise to Pro downgrade (main, Workato, Advanced Analytics)");
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForBusinessToEnterpriseWithVONQAndDataEnrichmentAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        int dataEnrichmentPlanId = annualBilling ? DE_3000_BUCKET_ANNUALLY : DE_250_BUCKET_MONTHLY;
        int expectedPlanCredits = annualBilling ? 3000 : 250;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Business", vonqPlanId);
        subscribeToDataEnrichmentAndAssert(dataEnrichmentPlanId);
        assertVONQActive("Business", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Enterprise", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, false, true, true);
        assertEnterpriseIncludedAddons(false);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Enterprise", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertDataEnrichmentActive();
        assertDataEnrichmentCredits(expectedPlanCredits, expectedPlanCredits, 0);
        assertCallingCreditsState("Enterprise", customerId);
        assertEnterpriseIncludedAddons(true);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Business to Enterprise upgrade (main, VONQ, Data Enrichment)");
        assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForBusinessToEnterpriseWithVONQAndWorkatoAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        int vonqPlanId = annualBilling ? VONQ_BUCKET7_ANNUALLY : VONQ_BUCKET7_MONTHLY;
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToVONQAndAssert("Business", vonqPlanId);
        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        assertVONQActive("Business", billingCycle, 7);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Enterprise", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, false, true, true);
        assertEnterpriseIncludedAddons(false);

        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Enterprise", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 3, 3);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertVONQActive("Enterprise", billingCycle, 7);
        assertCallingCreditsState("Enterprise", customerId);
        assertEnterpriseIncludedAddons(true);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 3, "Albatross invoice count must increase by 3 after " + billingCycle + " renewal following Business to Enterprise upgrade (main, VONQ, Workato)");
        assertAlbatrossVONQAndWorkatoInvoiceGroups(billingCycle, false, true, true);
    }

    @Test
    @Parameters("billingCycle")
    public void stripeRenewalForBusinessToEnterpriseWithWorkatoAndAdvanceAnalyticsAddons_Test(String billingCycle) {
        boolean annualBilling = billingCycle.equals("year");
        String workatoPlanCycle = annualBilling ? "Annually" : "Monthly";
        int workatoPlanId = annualBilling ? WORKATO_BUCKET1K_ANNUALLY : WORKATO_BUCKET1K_MONTHLY;

        int recordAddOn = faker.randomRecordAddonCount();
        String[] cc = setupFreeAccountWithTestClockAndBaseline();
        String testClockId = cc[0], customerId = cc[1];

        upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", 1, recordAddOn);
        assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, 1);
        assertRecordAddonActive(customerId, recordAddOn);

        subscribeToWorkatoAndAssert(workatoPlanCycle, WORKATO_TASK_1K, workatoPlanId);
        subscribeToAdvanceAnalyticsAndAssert(billingCycle);
        assertWorkatoActive(workatoPlanCycle, workatoPlanId);
        assertAdvanceAnalyticsActive(billingCycle, 1);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, true, true, true);

        function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Enterprise", 1, recordAddOn, 0);
        function.updateAccountSettingsForStripeAfterRenewal(accountId);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
        assertEnterpriseIncludedAddons(false);
        assertStripeLastSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
        assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 4);
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, false, true, true);
        assertNoAlbatrossCallingCreditTransactions();
        assertStripeCallingCreditChargesCount(customerId, 0);
        purchaseCallingCreditsAndAssert(50, 1);
        assertCallingCreditsState("Enterprise", customerId);

        int stripeInvoiceCountBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size();
        int albatrossCountBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

        long renewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
        performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 2, 2);

        assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingCycle, 1);
        assertCallingCreditsState("Enterprise", customerId);
        assertEnterpriseIncludedAddons(true);
        fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 2, "Albatross invoice count must increase by 2 after " + billingCycle + " renewal following Business to Enterprise upgrade (main, Advanced Analytics)");
        assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(billingCycle, false, false, true);
    }

}