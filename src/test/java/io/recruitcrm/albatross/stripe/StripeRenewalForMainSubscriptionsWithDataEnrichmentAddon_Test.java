package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;
import org.testng.Assert;
import org.testng.annotations.*;
import io.restassured.response.Response;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptionsWithDataEnrichmentAddon_Test extends StripeRenewalAutomationBase_Test {

	protected static final int DE_250_BUCKET_MONTHLY   = 1;
	protected static final int DE_500_BUCKET_MONTHLY   = 19;
	protected static final int DE_1000_BUCKET_MONTHLY  = 37;
	protected static final int DE_3000_BUCKET_ANNUALLY = 2;
	protected static final int DE_6000_BUCKET_ANNUALLY = 20;
	protected static final int DE_12000_BUCKET_ANNUALLY = 38;

	public StripeRenewalForMainSubscriptionsWithDataEnrichmentAddon_Test() {
		super();
	}

	@Test
	public void stripeRenewalForDataEnrichmentAddonAddOnsBlockedWithFreeAccount_Test() {
		setupFreeAccountWithTestClockAndBaseline();
		assertDataEnrichmentNotSubscribed();

		Response blockedResponse = function.createDataEnrichmentSubscription(albatrossURL, albatrossAuthToken, DE_500_BUCKET_MONTHLY);
		Assert.assertEquals(blockedResponse.getStatusCode(), 401, "Data Enrichment subscription should be blocked when user is on free account");
		Assert.assertEquals(blockedResponse.jsonPath().getString("errorMessage"), "Unauthorised Access", "Response errorMessage should be 'Unauthorised Access' for blocked Data Enrichment subscription on free account");

		assertDataEnrichmentNotSubscribed();
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForDataEnrichment250CreditsMonthlyBucketAcrossOneRenewal_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		assertDataEnrichmentNotSubscribed();

		subscribeToDataEnrichmentAndAssert(DE_250_BUCKET_MONTHLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(250, 250, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Data Enrichment invoices");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertDataEnrichmentCredits(250, 250, 0);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForDataEnrichment1000CreditsMonthlyBucketAcrossTwoRenewals_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		assertDataEnrichmentNotSubscribed();

		subscribeToDataEnrichmentAndAssert(DE_1000_BUCKET_MONTHLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(1000, 1300, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Data Enrichment invoices");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertDataEnrichmentCredits(1000, 1300, 0);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 6, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertDataEnrichmentCredits(1000, 1300, 0);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForDataEnrichment3000CreditsAnnualBucketAcrossTwoRenewals_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		assertDataEnrichmentNotSubscribed();

		subscribeToDataEnrichmentAndAssert(DE_3000_BUCKET_ANNUALLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(3000, 3000, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 3, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(3, "Albatross invoice count after first renewal should include RCRM renewal while Data Enrichment annual subscription is active");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertDataEnrichmentCredits(3000, 3000, 0);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 1, "Albatross invoice count should increase after second monthly RCRM renewal");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertDataEnrichmentCredits(3000, 3000, 0);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForDataEnrichment12000CreditsAnnualBucketAcrossThreeRenewals_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		assertDataEnrichmentNotSubscribed();

		subscribeToDataEnrichmentAndAssert(DE_12000_BUCKET_ANNUALLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(12000, 15000, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 3, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(3, "Albatross invoice count after first renewal should include RCRM renewal while Data Enrichment annual subscription is active");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertDataEnrichmentCredits(12000, 15000, 0);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 1, "Albatross invoice count should increase after second monthly RCRM renewal");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertDataEnrichmentCredits(12000, 15000, 0);

		Response albatrossBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR3 = albatrossBeforeThirdRenewal.jsonPath().getList("data").size();
		Response stripeBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR3 = stripeBeforeThirdRenewal.jsonPath().getList("data").size();

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeCountBeforeR3 + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterSecondRenewal, stripeInvoicesAfterThirdRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR3 + 1, "Albatross invoice count should increase after third monthly RCRM renewal");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertDataEnrichmentCredits(12000, 15000, 0);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForDataEnrichment250To500CreditsMonthlyBucketUpgrade_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		assertDataEnrichmentNotSubscribed();

		subscribeToDataEnrichmentAndAssert(DE_250_BUCKET_MONTHLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(250, 250, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		
		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Data Enrichment invoices");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertDataEnrichmentCredits(250, 250, 0);

		upgradeDataEnrichmentAndAssert(DE_500_BUCKET_MONTHLY, DE_250_BUCKET_MONTHLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(500, 625, 250);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 5);
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, false);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();
		Response stripeBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 7, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal following Data Enrichment upgrade");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, false);
		assertDataEnrichmentCredits(500, 625, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 7);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForDataEnrichment3000To6000CreditsAnnualBucketUpgrade_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		assertDataEnrichmentNotSubscribed();

		subscribeToDataEnrichmentAndAssert(DE_3000_BUCKET_ANNUALLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(3000, 3000, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);

		upgradeDataEnrichmentAndAssert(DE_6000_BUCKET_ANNUALLY, DE_3000_BUCKET_ANNUALLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(6000, 7000, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 3);
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, false);

		Response albatrossBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeRenewal = albatrossBeforeRenewal.jsonPath().getList("data").size();

		long renewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeRenewal + 1, "Albatross invoice count should increase after monthly RCRM renewal following Data Enrichment annual upgrade");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, false);
		assertDataEnrichmentCredits(6000, 7000, 0);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForDataEnrichment250CreditsMonthlyTo3000CreditsAnnualUpgrade_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		assertDataEnrichmentNotSubscribed();

		subscribeToDataEnrichmentAndAssert(DE_250_BUCKET_MONTHLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(250, 250, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Data Enrichment invoices");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertDataEnrichmentCredits(250, 250, 0);

		upgradeDataEnrichmentAndAssert(DE_3000_BUCKET_ANNUALLY, DE_250_BUCKET_MONTHLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(3000, 3000, 250);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 5);
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, false);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 6, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 1, "Albatross invoice count should increase after second monthly RCRM renewal following Data Enrichment monthly-to-annual upgrade");
		assertDataEnrichmentActive();
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, false);
		assertDataEnrichmentCredits(3000, 3000, 250);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForDataEnrichmentAnnualBucketToMonthlyBucketUpgradeBlocked_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		setupPaidAccountWithDataEnrichmentBaseline(plan, customerId);
		subscribeToDataEnrichmentAndAssert(DE_3000_BUCKET_ANNUALLY);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(3000, 3000, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);

		Response blockedResponse = function.upgradeDataEnrichmentSubscription(albatrossURL, albatrossAuthToken, DE_250_BUCKET_MONTHLY, DE_3000_BUCKET_ANNUALLY);
		Assert.assertEquals(blockedResponse.getStatusCode(), 400, "Annual Data Enrichment plan should not upgrade to monthly plan");
		Assert.assertEquals(blockedResponse.jsonPath().getString("errorMessage"), "You Cannot Switch From Annual Billing To Monthly Billing");

		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(3000, 3000, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		assertAlbatrossDataEnrichmentInvoiceGroups("month", true, true);
		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
	}

}