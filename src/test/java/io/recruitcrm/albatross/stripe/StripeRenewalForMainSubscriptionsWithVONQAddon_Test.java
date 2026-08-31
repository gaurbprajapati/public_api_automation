package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptionsWithVONQAddon_Test extends StripeRenewalAutomationBase_Test {

	protected static final int BUCKET7_MONTHLY = 218;
	protected static final int BUCKET7_ANNUALLY = 219;
	protected static final int BUCKET15_MONTHLY = 236;
	protected static final int BUCKET15_ANNUALLY = 237;
	protected static final int BUCKET30_MONTHLY = 254;
	protected static final int BUCKET30_ANNUALLY = 255;

	public StripeRenewalForMainSubscriptionsWithVONQAddon_Test() {
		super();
	}

	@Test
	public void stripeRenewalForVONQAddonEmptyDataWithFreeAccount_Test() {
		setupFreeAccountWithTestClockAndBaseline();

		Response response = fetchVONQSubscription();
		Assert.assertNull(response.jsonPath().get("data.activeSubscription"), "VONQ activeSubscription should be null for free account");
		Assert.assertNull(response.jsonPath().get("data.scheduledSubscription"), "VONQ scheduledSubscription should be null for free account");
		Assert.assertEquals(response.jsonPath().getMap("data").size(), 2, "VONQ data size should be 2 for free account");

	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQAddonEmptyDataWithPaidMainSubscription_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);

		Response response = fetchVONQSubscription();
		Assert.assertNull(response.jsonPath().get("data.scheduledSubscription"), "VONQ scheduledSubscription should be null for " + planLabel + " main subscription without VONQ purchase");

		if (plan.equalsIgnoreCase("Enterprise")) {
			Assert.assertEquals(response.jsonPath().getMap("data").size(), 3, "VONQ data size should be 3 for Enterprise main subscription without VONQ purchase");
			Assert.assertNull(response.jsonPath().get("data.activeSubscription"), "VONQ activeSubscription should be null for Enterprise main subscription without VONQ purchase");
			Assert.assertNotNull(response.jsonPath().get("data.enterpriseActiveSubscription"), "VONQ enterpriseActiveSubscription should not be null for Enterprise main subscription without VONQ purchase");
			Assert.assertEquals(response.jsonPath().getInt("data.enterpriseActiveSubscription.total_credits"), 10, "VONQ total_credits should be 10 for Enterprise main subscription without VONQ purchase");
		} else {
			Assert.assertEquals(response.jsonPath().getMap("data").size(), 2, "VONQ data size should be 2 for " + planLabel + " main subscription without VONQ purchase");
			Assert.assertNotNull(response.jsonPath().get("data.activeSubscription"), "VONQ activeSubscription should not be null for " + planLabel + " main subscription without VONQ purchase");
			Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.total_credits"), 2, "VONQ total_credits should be 2 for " + planLabel + " main subscription without VONQ purchase");
		}
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ7MonthlyBucketAcrossOneRenewal_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET7_MONTHLY);
		assertVONQActive(plan, "month", 7);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and VONQ invoices");
		assertVONQActive(plan, "month", 7);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ7AnnualBucketAcrossOneRenewal_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET7_ANNUALLY);
		assertVONQActive(plan, "year", 7);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, true);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include only RCRM renewal while VONQ annual subscription remains active");
		assertVONQActive(plan, "year", 7);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ15MonthlyBucketAcrossTwoRenewals_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET15_MONTHLY);
		assertVONQActive(plan, "month", 15);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and VONQ invoices");
		assertVONQActive(plan, "month", 15);

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 6, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertAlbatrossVONQInvoiceGroups("month", true, true, 2);
		fetchAlbatrossInvoicesAssertCount(6, "Albatross invoice count should increase after second renewal");
		assertVONQActive(plan, "month", 15);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ15AnnualBucketAcrossTwoRenewals_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET15_ANNUALLY);
		assertVONQActive(plan, "year", 15);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 3, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(3, "Albatross invoice count after first renewal should include only RCRM renewal while VONQ annual subscription remains active");
		assertVONQActive(plan, "year", 15);

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertAlbatrossVONQInvoiceGroups("month", true, true, 2);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count should increase after second monthly RCRM renewal");
		assertVONQActive(plan, "year", 15);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ30MonthlyBucketAcrossThreeRenewals_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET30_MONTHLY);
		assertVONQActive(plan, "month", 30);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and VONQ invoices");
		assertVONQActive(plan, "month", 30);

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 6, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertAlbatrossVONQInvoiceGroups("month", true, true, 2);
		fetchAlbatrossInvoicesAssertCount(6, "Albatross invoice count should increase after second renewal");
		assertVONQActive(plan, "month", 30);

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, 8, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertAlbatrossVONQInvoiceGroups("month", true, true, 2);
		fetchAlbatrossInvoicesAssertCount(8, "Albatross invoice count should increase after third renewal");
		assertVONQActive(plan, "month", 30);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ30AnnualBucketAcrossThreeRenewals_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET30_ANNUALLY);
		assertVONQActive(plan, "year", 30);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 3, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(3, "Albatross invoice count after first renewal should include only RCRM renewal while VONQ annual subscription remains active");
		assertVONQActive(plan, "year", 30);

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertAlbatrossVONQInvoiceGroups("month", true, true, 2);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count should increase after second monthly RCRM renewal");
		assertVONQActive(plan, "year", 30);

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, 5, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertAlbatrossVONQInvoiceGroups("month", true, true, 2);
		fetchAlbatrossInvoicesAssertCount(5, "Albatross invoice count should increase after third monthly RCRM renewal");
		assertVONQActive(plan, "year", 30);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ7BucketTo15BucketMonthly_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET7_MONTHLY);
		assertVONQActive(plan, "month", 7);
		assertVONQRolledOverCredits(7, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and VONQ invoices");
		assertVONQActive(plan, "month", 7);
		assertVONQRolledOverCredits(7, 0);

		Response stripeBeforeUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeUpgrade = stripeBeforeUpgrade.jsonPath().getList("data").size();
		int albatrossCountBeforeUpgrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		upgradeVONQAndAssert(plan, BUCKET15_MONTHLY, BUCKET7_MONTHLY);
		assertVONQActive(plan, "month", 15);
		assertVONQRolledOverCredits(15, 7);
		int albatrossCountAfterUpgrade = albatrossCountBeforeUpgrade + 1;
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, stripeCountBeforeUpgrade + 1);
		fetchAlbatrossInvoicesAssertCount(albatrossCountAfterUpgrade, "Albatross invoice count should increase after VONQ bucket upgrade");

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeUpgrade + 3, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertAlbatrossVONQInvoiceGroups("month", true, false, 3);
		fetchAlbatrossInvoicesAssertCount(albatrossCountAfterUpgrade + 2, "Albatross invoice count should increase after second renewal following VONQ bucket upgrade");
		assertVONQActive(plan, "month", 15);
		assertVONQRolledOverCredits(15, 0);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ15BucketTo30BucketAnnual_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET15_ANNUALLY);
		assertVONQActive(plan, "year", 15);
		assertVONQRolledOverCredits(15, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		Response stripeBeforeUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeUpgrade = stripeBeforeUpgrade.jsonPath().getList("data").size();
		int albatrossCountBeforeUpgrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		upgradeVONQAndAssert(plan, BUCKET30_ANNUALLY, BUCKET15_ANNUALLY);
		assertVONQActive(plan, "year", 30);
		assertVONQRolledOverCredits(30, 0);
		int albatrossCountAfterUpgrade = albatrossCountBeforeUpgrade + 1;
		int stripeCountAfterUpgrade = stripeCountBeforeUpgrade + 1;
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, stripeCountAfterUpgrade);
		fetchAlbatrossInvoicesAssertCount(albatrossCountAfterUpgrade, "Albatross invoice count should increase after VONQ annual bucket upgrade");

		long renewalEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeCountAfterUpgrade + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, true);
		fetchAlbatrossInvoicesAssertCount(albatrossCountAfterUpgrade + 2, "Albatross invoice count should increase after monthly RCRM renewal following VONQ annual bucket upgrade");
		assertVONQActive(plan, "year", 30);
		assertVONQRolledOverCredits(30, 0);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ7BucketMonthlyToAnnualUpgrade_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET7_MONTHLY);
		assertVONQActive(plan, "month", 7);
		assertVONQRolledOverCredits(7, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and VONQ invoices");
		assertVONQActive(plan, "month", 7);
		assertVONQRolledOverCredits(7, 0);

		Response stripeBeforeUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeUpgrade = stripeBeforeUpgrade.jsonPath().getList("data").size();
		int albatrossCountBeforeUpgrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		upgradeVONQAndAssert(plan, BUCKET7_ANNUALLY, BUCKET7_MONTHLY);
		assertVONQActive(plan, "year", 7);
		assertVONQRolledOverCredits(7, 7);

		Response stripeAfterUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		int albatrossCountAfterUpgrade = albatrossCountBeforeUpgrade + 1;
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, stripeCountBeforeUpgrade + 1);
		fetchAlbatrossInvoicesAssertCount(albatrossCountAfterUpgrade, "Albatross invoice count should increase after VONQ monthly-to-annual upgrade");
		assertStripeSubscriptionForAddOnsUpgradedFromMonthlyToAnnual(stripeAfterUpgrade, 1, 1, 3);

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeUpgrade + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertAlbatrossVONQInvoiceGroups("month", true, false, 3);
		fetchAlbatrossInvoicesAssertCount(albatrossCountAfterUpgrade + 1, "Albatross invoice count should increase after second monthly RCRM renewal following VONQ monthly-to-annual upgrade");
		assertVONQActive(plan, "year", 7);
		assertVONQRolledOverCredits(7, 7);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ15BucketMonthlyTo30BucketAnnualUpgrade_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET15_MONTHLY);
		assertVONQActive(plan, "month", 15);
		assertVONQRolledOverCredits(15, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and VONQ invoices");
		assertVONQActive(plan, "month", 15);
		assertVONQRolledOverCredits(15, 0);

		Response stripeBeforeUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeUpgrade = stripeBeforeUpgrade.jsonPath().getList("data").size();
		int albatrossCountBeforeUpgrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		upgradeVONQAndAssert(plan, BUCKET30_ANNUALLY, BUCKET15_MONTHLY);
		assertVONQActive(plan, "year", 30);
		assertVONQRolledOverCredits(30, 15);

		Response stripeAfterUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		int albatrossCountAfterUpgrade = albatrossCountBeforeUpgrade + 1;
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, stripeCountBeforeUpgrade + 1);
		fetchAlbatrossInvoicesAssertCount(albatrossCountAfterUpgrade, "Albatross invoice count should increase after VONQ monthly-to-annual upgrade");
		assertStripeSubscriptionForAddOnsUpgradedFromMonthlyToAnnual(stripeAfterUpgrade, 1, 1, 3);

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeUpgrade + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertAlbatrossVONQInvoiceGroups("month", true, false, 3);
		fetchAlbatrossInvoicesAssertCount(albatrossCountAfterUpgrade + 1, "Albatross invoice count should increase after second monthly RCRM renewal following VONQ monthly-to-annual upgrade");
		assertVONQActive(plan, "year", 30);
		assertVONQRolledOverCredits(30, 15);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ15BucketMonthlyTo7BucketMonthlyDowngrade_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET15_MONTHLY);
		assertVONQActive(plan, "month", 15);
		assertVONQRolledOverCredits(15, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and VONQ invoices");
		assertVONQActive(plan, "month", 15);
		assertVONQRolledOverCredits(15, 0);

		Response stripeBeforeDowngrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeDowngrade = stripeBeforeDowngrade.jsonPath().getList("data").size();
		int albatrossCountBeforeDowngrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		downgradeVONQAndAssert(plan, BUCKET7_MONTHLY, BUCKET15_MONTHLY);
		assertVONQDowngradeWithScheduledSubscription(plan, "month", 15, BUCKET7_MONTHLY, "month", 7);
		assertVONQRolledOverCreditsForDowngrade(15, 0);

		Response stripeAfterDowngrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, stripeCountBeforeDowngrade);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeDowngrade, "Albatross invoice count should remain unchanged after VONQ bucket downgrade");
		assertStripeSubscriptionForAddOnsUpgradedDowngrade(stripeAfterDowngrade, 1, 2, "month");

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeDowngrade + 1, 1);

		// BUG : This behaviour is not working as expected. The VONQ downgraded subscription is not re activated correctly.
		// assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		// assertAlbatrossVONQInvoiceGroups("month", true, false, 2);
		// fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeDowngrade + 2, "Albatross invoice count should increase after second renewal following VONQ bucket downgrade");
		// assertVONQActive(plan, "month", 7);
		// assertVONQRolledOverCredits(7, 0);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForVONQ30BucketAnnualTo15BucketAnnualDowngrade_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToVONQAndAssert(plan, BUCKET30_ANNUALLY);
		assertVONQActive(plan, "year", 30);
		assertVONQRolledOverCredits(30, 0);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, true);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include only RCRM renewal while VONQ annual subscription remains active");
		assertVONQActive(plan, "year", 30);
		assertVONQRolledOverCredits(30, 0);

		Response stripeBeforeDowngrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeDowngrade = stripeBeforeDowngrade.jsonPath().getList("data").size();
		int albatrossCountBeforeDowngrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		downgradeVONQAndAssert(plan, BUCKET15_ANNUALLY, BUCKET30_ANNUALLY);
		assertVONQDowngradeWithScheduledSubscription(plan, "year", 30, BUCKET15_ANNUALLY, "year", 15);
		assertVONQRolledOverCreditsForDowngrade(30, 0);

		Response stripeAfterDowngrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, stripeCountBeforeDowngrade);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeDowngrade, "Albatross invoice count should remain unchanged after VONQ annual bucket downgrade");
		assertStripeSubscriptionForAddOnsUpgradedDowngrade(stripeAfterDowngrade, 1, 2, "year");

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeDowngrade + 1, 1);

		// BUG : This behaviour is not working as expected. The VONQ downgraded subscription is not re activated correctly.
		// assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);
		// assertAlbatrossVONQInvoiceGroups("month", true, true, 2);
		// fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeDowngrade + 1, "Albatross invoice count should increase after second monthly RCRM renewal following VONQ annual bucket downgrade");
		// assertVONQActive(plan, "year", 15);
		// assertVONQRolledOverCredits(15, 0);
	}

}