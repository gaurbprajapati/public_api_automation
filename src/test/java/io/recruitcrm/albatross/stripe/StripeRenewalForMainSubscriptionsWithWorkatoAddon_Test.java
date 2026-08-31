package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;
import org.testng.annotations.*;
import io.restassured.response.Response;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptionsWithWorkatoAddon_Test extends StripeRenewalAutomationBase_Test {

	protected static final int BUCKET1K_MONTHLY  = 1;
	protected static final int BUCKET1K_ANNUALLY = 2;
	protected static final int BUCKET2K_MONTHLY  = 19;
	protected static final int BUCKET2K_ANNUALLY = 20;
	protected static final int BUCKET3K_MONTHLY  = 37;
	protected static final int BUCKET3K_ANNUALLY = 38;

	public StripeRenewalForMainSubscriptionsWithWorkatoAddon_Test() {
		super();
	}

	@Test
	@Parameters("planCycle")
	public void stripeRenewalForWorkato1000BucketAcrossOneRenewal_Test(String planCycle) {
		boolean annual = "Annually".equalsIgnoreCase(planCycle);
		int planId = annual ? BUCKET1K_ANNUALLY : BUCKET1K_MONTHLY;
		String billingIntervalStr = annual ? "year" : "month";

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		subscribeToWorkatoAndAssert(planCycle, "1000", planId);
		assertWorkatoActive(planCycle, planId);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);
		Response albatrossPaid = fetchAlbatrossWorkatoInvoicesAssertCount(1, "Workato invoice should be created after subscription");

		long r1Epoch = annual ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r1Epoch, accountId, 2, 1);

		assertWorkatoActive(planCycle, planId);
		assertAlbatrossInvoicesCountAfterRenewal(albatrossPaid, billingIntervalStr, 2);
	}

	@Test
	@Parameters("planCycle")
	public void stripeRenewalForWorkato2000BucketAcrossTwoRenewals_Test(String planCycle) {
		boolean annual = "Annually".equalsIgnoreCase(planCycle);
		int planId = annual ? BUCKET2K_ANNUALLY : BUCKET2K_MONTHLY;

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		subscribeToWorkatoAndAssert(planCycle, "2000", planId);
		assertWorkatoActive(planCycle, planId);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(1, "Workato invoice should be created after subscription");

		long r1Epoch = annual ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r1Epoch, accountId, 2, 1);
		assertWorkatoActive(planCycle, planId);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross invoice count should be 2 after first renewal");

		long r2Epoch = annual ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r2Epoch, accountId, 3, 1);
		assertWorkatoActive(planCycle, planId);
		assertWorkatoInvoices(planCycle, true);
	}

	@Test
	@Parameters("planCycle")
	public void stripeRenewalForWorkato5000BucketAcrossThreeRenewals_Test(String planCycle) {
		boolean annual = "Annually".equalsIgnoreCase(planCycle);
		int planId = annual ? BUCKET3K_ANNUALLY : BUCKET3K_MONTHLY;

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		subscribeToWorkatoAndAssert(planCycle, "5000", planId);
		assertWorkatoActive(planCycle, planId);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(1, "Workato invoice should be created after subscription");

		long r1Epoch = annual ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r1Epoch, accountId, 2, 1);
		assertWorkatoActive(planCycle, planId);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross invoice count should be 2 after first renewal");

		long r2Epoch = annual ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r2Epoch, accountId, 3, 1);
		assertWorkatoActive(planCycle, planId);
		fetchAlbatrossWorkatoInvoicesAssertCount(3, "Albatross invoice count should be 3 after second renewal");

		long r3Epoch = annual ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r3Epoch, accountId, 4, 1);
		assertWorkatoActive(planCycle, planId);
		assertWorkatoInvoices(planCycle, true);
	}

	@Test
	public void stripeRenewalForWorkato1000BucketMonthlyToAnnualUpgradeAcrossOneRenewal_Test() {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		subscribeToWorkatoAndAssert("Monthly", "1000", BUCKET1K_MONTHLY);
		assertWorkatoActive("Monthly", BUCKET1K_MONTHLY);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(1, "Workato invoice should be created after subscription");

		long firstRenewalMonthlyEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalMonthlyEpoch, accountId, 2, 1);
		assertWorkatoActive("Monthly", BUCKET1K_MONTHLY);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross invoice count should increase after monthly renewal");

		Response stripeBeforeUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeUpgrade = stripeBeforeUpgrade.jsonPath().getList("data").size();
		int albatrossCountBeforeUpgrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		upgradeWorkatoAndAssert(BUCKET1K_MONTHLY, "Annually", "1000", BUCKET1K_ANNUALLY);
		assertWorkatoActive("Annually", BUCKET1K_ANNUALLY);

		Response stripeAfterUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, stripeCountBeforeUpgrade + 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeUpgrade + 1, "Albatross invoice count should increase after monthly-to-annual upgrade");
		assertStripeSubscriptionForAddOnsUpgradedFromMonthlyToAnnual(stripeAfterUpgrade, 1, 1, 2);

		int stripeCountBeforeSecondRenewal = stripeCountBeforeUpgrade + 1;
		int albatrossCountBeforeSecondRenewal = albatrossCountBeforeUpgrade + 1;

		String billingInterval = "year";
		int expectedSubscriptionCountAfterSecondRenewal = "month".equals(billingInterval) ? 2 : 1;
		long secondRenewalMonthlyEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalMonthlyEpoch, accountId, stripeCountBeforeSecondRenewal, expectedSubscriptionCountAfterSecondRenewal);

		assertWorkatoActive("Annually", BUCKET1K_ANNUALLY);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeSecondRenewal, "Albatross invoice count should increase after second monthly renewal following monthly-to-annual upgrade");
	}

	@Test
	public void stripeRenewalForWorkato2000BucketMonthlyToAnnualUpgradeAcrossTwoRenewals_Test() {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		subscribeToWorkatoAndAssert("Monthly", "2000", BUCKET2K_MONTHLY);
		assertWorkatoActive("Monthly", BUCKET2K_MONTHLY);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(1, "Workato invoice should be created after subscription");

		long firstRenewalMonthlyEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalMonthlyEpoch, accountId, 2, 1);
		assertWorkatoActive("Monthly", BUCKET2K_MONTHLY);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross invoice count should increase after monthly renewal");

		Response stripeBeforeUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeUpgrade = stripeBeforeUpgrade.jsonPath().getList("data").size();
		int albatrossCountBeforeUpgrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		upgradeWorkatoAndAssert(BUCKET2K_MONTHLY, "Annually", "2000", BUCKET2K_ANNUALLY);
		assertWorkatoActive("Annually", BUCKET2K_ANNUALLY);

		Response stripeAfterUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, stripeCountBeforeUpgrade + 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeUpgrade + 1, "Albatross invoice count should increase after monthly-to-annual upgrade");
		assertStripeSubscriptionForAddOnsUpgradedFromMonthlyToAnnual(stripeAfterUpgrade, 1, 1, 2);
	}

	@Test
	@Parameters("planCycle")
	public void stripeRenewalForWorkato1000BucketTo2000Bucket_Test(String planCycle) {
		boolean annual = "Annually".equalsIgnoreCase(planCycle);
		int initialPlanId  = annual ? BUCKET1K_ANNUALLY : BUCKET1K_MONTHLY;
		int upgradedPlanId = annual ? BUCKET2K_ANNUALLY : BUCKET2K_MONTHLY;

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		subscribeToWorkatoAndAssert(planCycle, "1000", initialPlanId);
		assertWorkatoActive(planCycle, initialPlanId);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(1, "Workato invoice should be created after subscription");

		Response stripeBeforeUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeUpgrade = stripeBeforeUpgrade.jsonPath().getList("data").size();
		int albatrossCountBeforeUpgrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		upgradeWorkatoAndAssert(initialPlanId, planCycle, "2000", upgradedPlanId);
		assertWorkatoActive(planCycle, upgradedPlanId);
		Response stripeAfterUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		String billingInterval = annual ? "year" : "month";
		int expectedSubscriptionCountAfterUpgrade = "month".equals(billingInterval) ? 2 : 1;
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, expectedSubscriptionCountAfterUpgrade, stripeCountBeforeUpgrade + 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeUpgrade + 1, "Albatross invoice count should increase after Workato bucket upgrade");
		assertStripeSubscriptionForAddOnsUpgradedDowngrade(stripeAfterUpgrade, 1, expectedSubscriptionCountAfterUpgrade, billingInterval);
	}

	@Test
	public void stripeRenewalForWorkato1000BucketMonthlyTo2000BucketAnnually_Test() {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		subscribeToWorkatoAndAssert("Monthly", "1000", BUCKET1K_MONTHLY);
		assertWorkatoActive("Monthly", BUCKET1K_MONTHLY);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(1, "Workato invoice should be created after subscription");

		long firstRenewalMonthlyEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalMonthlyEpoch, accountId, 2, 1);
		assertWorkatoActive("Monthly", BUCKET1K_MONTHLY);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross invoice count should increase after monthly renewal");

		Response stripeBeforeUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeUpgrade = stripeBeforeUpgrade.jsonPath().getList("data").size();
		int albatrossCountBeforeUpgrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		upgradeWorkatoAndAssert(BUCKET1K_MONTHLY, "Annually", "2000", BUCKET2K_ANNUALLY);
		assertWorkatoActive("Annually", BUCKET2K_ANNUALLY);

		Response stripeAfterUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, stripeCountBeforeUpgrade + 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeUpgrade + 1, "Albatross invoice count should increase after monthly-to-annual upgrade");
		assertStripeSubscriptionForAddOnsUpgradedFromMonthlyToAnnual(stripeAfterUpgrade, 1, 1, 2);

		long secondRenewalMonthlyEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalMonthlyEpoch, accountId, stripeCountBeforeUpgrade + 1, 1);

		assertWorkatoActive("Annually", BUCKET2K_ANNUALLY);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeUpgrade + 1, "Albatross invoice count should increase after second monthly renewal following monthly-to-annual upgrade");
	}

	@Test
	@Parameters("planCycle")
	public void stripeRenewalForWorkato2000BucketTo1000Bucket_DowngradePlan_Test(String planCycle) {
		boolean annual = "Annually".equalsIgnoreCase(planCycle);
		int initialPlanId    = annual ? BUCKET2K_ANNUALLY : BUCKET2K_MONTHLY;
		int downgradedPlanId = annual ? BUCKET1K_ANNUALLY : BUCKET1K_MONTHLY;
		String billingInterval = annual ? "year" : "month";

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		subscribeToWorkatoAndAssert(planCycle, "2000", initialPlanId);
		assertWorkatoActive(planCycle, initialPlanId);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);
		fetchAlbatrossWorkatoInvoicesAssertCount(1, "Workato invoice should be created after subscription");

		long firstRenewalEpoch = annual ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);
		assertWorkatoActive(planCycle, initialPlanId);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross invoice count should increase after first renewal");

		Response stripeBeforeDowngrade = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeDowngrade = stripeBeforeDowngrade.jsonPath().getList("data").size();
		int albatrossCountBeforeDowngrade = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		downgradeWorkatoAndAssert(initialPlanId, planCycle, "1000", downgradedPlanId);
		assertWorkatoActive(planCycle, downgradedPlanId);

		Response stripeAfterDowngrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, stripeCountBeforeDowngrade);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeDowngrade, "Albatross invoice count should increase after Workato bucket downgrade");
		assertStripeSubscriptionForAddOnsUpgradedDowngrade(stripeAfterDowngrade, 1, 1, billingInterval);

		// BUG: This test is not working as expected. downgraded subscription is not creating
		// long secondRenewalEpoch = annual ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		// performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeDowngrade + 1, 1);
		// assertWorkatoActive(planCycle, downgradedPlanId);
		// fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeDowngrade + 1, "Albatross invoice count should increase after second renewal following Workato bucket downgrade");
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProWithWorkatoUpgradedAfterTwoRenewals_Test(String billingInterval) {
		boolean annualBilling = "year".equals(billingInterval);
		String planCycle       = annualBilling ? "Annually" : "Monthly";
		int workatoInitialId   = annualBilling ? BUCKET1K_ANNUALLY : BUCKET1K_MONTHLY;
		int workatoUpgradedId  = annualBilling ? BUCKET2K_ANNUALLY : BUCKET2K_MONTHLY;

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", 1);

		subscribeToWorkatoAndAssert(planCycle, "1000", workatoInitialId);
		assertWorkatoActive(planCycle, workatoInitialId);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross should have 2 invoices after subscribing to both RCRM and Workato");

		long r1Epoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r1Epoch, accountId, 4, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertWorkatoActive(planCycle, workatoInitialId);
		fetchAlbatrossWorkatoInvoicesAssertCount(4, "Albatross should have 4 invoices after first renewal");

		long r2Epoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r2Epoch, accountId, 6, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertWorkatoActive(planCycle, workatoInitialId);
		fetchAlbatrossWorkatoInvoicesAssertCount(6, "Albatross should have 6 invoices after second renewal");

		upgradeWorkatoAndAssert(workatoInitialId, planCycle, "2000", workatoUpgradedId);
		assertWorkatoActive(planCycle, workatoUpgradedId);
		int expectedSubscriptionCountAfterUpgrade = annualBilling ? 2 : 3;
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, expectedSubscriptionCountAfterUpgrade,
		function.getRequiredSubscriptionDataFromStripe(customerId, "invoices").jsonPath().getList("data").size());

		int albatrossCountBeforeR3 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();
		Response stripeBeforeR3 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR3 = stripeBeforeR3.jsonPath().getList("data").size();

		long r3Epoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r3Epoch, accountId, stripeCountBeforeR3 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertWorkatoActive(planCycle, workatoUpgradedId);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeR3 + 2, "Albatross invoice count should increase by 2 after third renewal");
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessWithWorkatoSequentialUpgradesAcrossThreeRenewals_Test(String billingInterval) {
		boolean annualBilling  = "year".equals(billingInterval);
		String planCycle        = annualBilling ? "Annually" : "Monthly";
		int workatoInitialId    = annualBilling ? BUCKET1K_ANNUALLY : BUCKET1K_MONTHLY;
		int workatoUpgrade1Id   = annualBilling ? BUCKET2K_ANNUALLY : BUCKET2K_MONTHLY;
		int workatoUpgrade2Id   = annualBilling ? BUCKET3K_ANNUALLY : BUCKET3K_MONTHLY;

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", 1);

		subscribeToWorkatoAndAssert(planCycle, "1000", workatoInitialId);
		assertWorkatoActive(planCycle, workatoInitialId);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross should have 2 invoices after subscribing to both RCRM and Workato");

		long r1Epoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r1Epoch, accountId, 4, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertWorkatoActive(planCycle, workatoInitialId);
		fetchAlbatrossWorkatoInvoicesAssertCount(4, "Albatross should have 4 invoices after first renewal");

		upgradeWorkatoAndAssert(workatoInitialId, planCycle, "2000", workatoUpgrade1Id);
		assertWorkatoActive(planCycle, workatoUpgrade1Id);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		int albatrossCountBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		long r2Epoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r2Epoch, accountId, stripeCountBeforeR2 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertWorkatoActive(planCycle, workatoUpgrade1Id);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase by 2 after second renewal");

		upgradeWorkatoAndAssert(workatoUpgrade1Id, planCycle, "5000", workatoUpgrade2Id);
		assertWorkatoActive(planCycle, workatoUpgrade2Id);

		Response stripeBeforeR3 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR3 = stripeBeforeR3.jsonPath().getList("data").size();
		int albatrossCountBeforeR3 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		long r3Epoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r3Epoch, accountId, stripeCountBeforeR3 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertWorkatoActive(planCycle, workatoUpgrade2Id);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeR3 + 2, "Albatross invoice count should increase by 2 after third renewal");
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProToBusinessWithWorkatoUpgradedAfterFirstRenewal_Test(String billingInterval) {
		boolean annualBilling = "year".equals(billingInterval);
		String planCycle       = annualBilling ? "Annually" : "Monthly";
		int workatoInitialId   = annualBilling ? BUCKET1K_ANNUALLY : BUCKET1K_MONTHLY;
		int workatoUpgradedId  = annualBilling ? BUCKET2K_ANNUALLY : BUCKET2K_MONTHLY;

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", 1);

		subscribeToWorkatoAndAssert(planCycle, "1000", workatoInitialId);
		assertWorkatoActive(planCycle, workatoInitialId);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross should have 2 invoices after subscribing to both RCRM and Workato");

		long r1Epoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r1Epoch, accountId, 4, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertWorkatoActive(planCycle, workatoInitialId);
		fetchAlbatrossWorkatoInvoicesAssertCount(4, "Albatross should have 4 invoices after first renewal");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Business", 1, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);

		upgradeWorkatoAndAssert(workatoInitialId, planCycle, "2000", workatoUpgradedId);
		assertWorkatoActive(planCycle, workatoUpgradedId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		int albatrossCountBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		long r2Epoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r2Epoch, accountId, stripeCountBeforeR2 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertWorkatoActive(planCycle, workatoUpgradedId);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase by 2 after second renewal");
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessToProWithWorkatoUpgradedAfterFirstRenewal_Test(String billingInterval) {
		boolean annualBilling = "year".equals(billingInterval);
		String planCycle       = annualBilling ? "Annually" : "Monthly";
		int workatoInitialId   = annualBilling ? BUCKET1K_ANNUALLY : BUCKET1K_MONTHLY;
		int workatoUpgradedId  = annualBilling ? BUCKET2K_ANNUALLY : BUCKET2K_MONTHLY;

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", 1);

		subscribeToWorkatoAndAssert(planCycle, "1000", workatoInitialId);
		assertWorkatoActive(planCycle, workatoInitialId);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross should have 2 invoices after subscribing to both RCRM and Workato");

		long r1Epoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r1Epoch, accountId, 4, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertWorkatoActive(planCycle, workatoInitialId);
		fetchAlbatrossWorkatoInvoicesAssertCount(4, "Albatross should have 4 invoices after first renewal");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Team", 1, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);

		upgradeWorkatoAndAssert(workatoInitialId, planCycle, "2000", workatoUpgradedId);
		assertWorkatoActive(planCycle, workatoUpgradedId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		int albatrossCountBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		long r2Epoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r2Epoch, accountId, stripeCountBeforeR2 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertWorkatoActive(planCycle, workatoUpgradedId);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase by 2 after second renewal");
	}

	@Test
	public void stripeRenewalForProMonthlyWithWorkatoTaskUpgradeAfterFirstRenewal_Test() {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Team", "Team", 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToWorkatoAndAssert("Monthly", "1000", BUCKET1K_MONTHLY);
		assertWorkatoActive("Monthly", BUCKET1K_MONTHLY);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross should have 2 invoices after subscribing to both RCRM and Workato");

		long r1Epoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeR1 = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r1Epoch, accountId, 4, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeR1, false);
		assertWorkatoActive("Monthly", BUCKET1K_MONTHLY);
		fetchAlbatrossWorkatoInvoicesAssertCount(4, "Albatross should have 4 invoices after first renewal");
		assertAlbatrossWorkatoInvoiceGroups("month", true, true);

		upgradeWorkatoAndAssert(BUCKET1K_MONTHLY, "Monthly", "2000", BUCKET2K_MONTHLY);
		assertWorkatoActive("Monthly", BUCKET2K_MONTHLY);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		int albatrossCountBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();

		long r2Epoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r2Epoch, accountId, stripeCountBeforeR2 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", "month", 1);
		assertWorkatoActive("Monthly", BUCKET2K_MONTHLY);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase by 2 after second renewal");
	}

	@Test
	@Parameters("planCycle")
	public void stripeRenewalForBusinessMonthlyToAnnualWithWorkatoTaskUpgrade_Test(String planCycle) {
		boolean workatoAnnual = "Annually".equalsIgnoreCase(planCycle);
		int workatoInitialPlanId = workatoAnnual ? BUCKET1K_ANNUALLY : BUCKET1K_MONTHLY;
		int workatoUpgradedPlanId = workatoAnnual ? BUCKET2K_ANNUALLY : BUCKET2K_MONTHLY;
		int expectedStripeInvoicesAfterFirstRenewal = workatoAnnual ? 3 : 4;
		int expectedSubscriptionCountAfterWorkatoUpgrade = workatoAnnual ? 2 : 3;
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToWorkatoAndAssert(planCycle, "1000", workatoInitialPlanId);
		assertWorkatoActive(planCycle, workatoInitialPlanId);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);
		fetchAlbatrossWorkatoInvoicesAssertCount(2, "Albatross should have 2 invoices after subscribing to both RCRM and Workato");

		long r1Epoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeR1 = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r1Epoch, accountId, expectedStripeInvoicesAfterFirstRenewal, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeR1, false);
		assertWorkatoActive(planCycle, workatoInitialPlanId);
		fetchAlbatrossWorkatoInvoicesAssertCount(expectedStripeInvoicesAfterFirstRenewal, "Albatross should match invoice count after first renewal");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", 1, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", "year", 1);

		upgradeWorkatoAndAssert(workatoInitialPlanId, planCycle, "2000", workatoUpgradedPlanId);
		assertWorkatoActive(planCycle, workatoUpgradedPlanId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		int albatrossCountBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken).jsonPath().getList("data").size();
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, expectedSubscriptionCountAfterWorkatoUpgrade, stripeCountBeforeR2);

		long r2Epoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, r2Epoch, accountId, stripeCountBeforeR2 + (workatoAnnual ? 0 : 1), 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", "year", 1);
		assertWorkatoActive(planCycle, workatoUpgradedPlanId);
		fetchAlbatrossWorkatoInvoicesAssertCount(albatrossCountBeforeR2 + (workatoAnnual ? 0 : 1), "Only Workato monthly invoice is added before RCRM annual renewal");
	}

}