package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;
import org.testng.Assert;
import org.testng.annotations.*;
import io.restassured.response.Response;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptionsWithAdvanceAnalytics_Test extends StripeRenewalAutomationBase_Test {

	public StripeRenewalForMainSubscriptionsWithAdvanceAnalytics_Test() {
		super();
	}

	@Test 
	@Parameters("billingInterval")
	public void stripeRenewalForProWithAdvanceAnalyticsAddedAfterFirstRenewal_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertAdvanceAnalyticsNotSubscribed();

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		subscribeToAdvanceAnalyticsAndAssert(billingInterval);
		assertAdvanceAnalyticsActive(billingInterval, 1);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 3);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 5, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal");
		assertAdvanceAnalyticsActive(billingInterval, 1);
	}

	@Test 
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessWithAdvanceAnalyticsAddedAfterFirstRenewal_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid           = paidBaseline[1];

		assertAdvanceAnalyticsNotSubscribed();

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		subscribeToAdvanceAnalyticsAndAssert(billingInterval);
		assertAdvanceAnalyticsActive(billingInterval, 1);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 3);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 5, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal");
		assertAdvanceAnalyticsActive(billingInterval, 1);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProIntervalWithAdvanceAnalyticsTwoRenewals_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndAssert(billingInterval);
		assertAdvanceAnalyticsActive(billingInterval, seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Advanced Analytics invoices");
		assertAlbatrossAdvanceAnalyticsInvoiceGroups(billingInterval, true, true);
		assertAdvanceAnalyticsActive(billingInterval, seats);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 6, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, seats);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal");
		assertAdvanceAnalyticsActive(billingInterval, seats);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessIntervalWithAdvanceAnalyticsTwoRenewals_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndAssert(billingInterval);
		assertAdvanceAnalyticsActive(billingInterval, seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Advanced Analytics invoices");
		assertAlbatrossAdvanceAnalyticsInvoiceGroups(billingInterval, true, true);
		assertAdvanceAnalyticsActive(billingInterval, seats);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 6, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal");
		assertAdvanceAnalyticsActive(billingInterval, seats);
	}

	@Test 
	public void stripeRenewalForProWithAdvanceAnalyticsMonthlyToAnnualUpgrade_Test() {
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndAssert("month");
		assertAdvanceAnalyticsActive("month", seats);
		assertAdvanceAnalyticsCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Advanced Analytics invoices");
		assertAlbatrossAdvanceAnalyticsInvoiceGroups("month", true, true);
		assertAdvanceAnalyticsActive("month", seats);
		assertAdvanceAnalyticsCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeAdvanceAnalyticsToAnnualAndAssert();
		assertAdvanceAnalyticsActive("year", seats);
		assertAdvanceAnalyticsCancelled(true, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 6);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal on annual plan");
		assertAdvanceAnalyticsActive("year", seats);
		assertAdvanceAnalyticsCancelled(true, "month", seats);
	}

	@Test 
	public void stripeRenewalForBusinessWithAdvanceAnalyticsMonthlyToAnnualUpgrade_Test() {
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndAssert("month");
		assertAdvanceAnalyticsActive("month", seats);
		assertAdvanceAnalyticsCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Advanced Analytics invoices");
		assertAlbatrossAdvanceAnalyticsInvoiceGroups("month", true, true);
		assertAdvanceAnalyticsActive("month", seats);
		assertAdvanceAnalyticsCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeAdvanceAnalyticsToAnnualAndAssert();
		assertAdvanceAnalyticsActive("year", seats);
		assertAdvanceAnalyticsCancelled(true, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 6);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal on annual plan");
		assertAdvanceAnalyticsActive("year", seats);
		assertAdvanceAnalyticsCancelled(true, "month", seats);
	}
	
	@Test 
	@Parameters("billingInterval") // BUG: This test is not working as expected. The seat counts are not being updated correctly.
	public void stripeRenewalForProWithAdvanceAnalyticsAcrossThreeRenewals_Test(String billingCycle) {
		boolean annualBilling = billingCycle.equals("year");
		final int initialSeats = faker.randomSeatCountWithInList(4,5,6);
		final int secondSeats  = faker.randomSeatCountWithInList(7,8,9);
		final int thirdSeats   = faker.randomSeatCountWithInList(1,2,3);

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", initialSeats);
		subscribeToAdvanceAnalyticsAndAssert(billingCycle);
		assertAdvanceAnalyticsActive(billingCycle, initialSeats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, initialSeats);
		assertAdvanceAnalyticsActive(billingCycle, initialSeats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", secondSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, secondSeats);
		//assertAdvanceAnalyticsActive(billingCycle, secondSeats);
		//assertAdvanceAnalyticsSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, secondSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal with seat increase");
		//assertAdvanceAnalyticsActive(billingCycle, secondSeats);
		//assertAdvanceAnalyticsSeatsSyncedWithMainSeats(customerId);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", thirdSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, secondSeats);
		assertSeatCountsOnIntercomResponseAfterReducingSeats(thirdSeats);
		//assertAdvanceAnalyticsActive(billingCycle, thirdSeats);
		//assertAdvanceAnalyticsSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR3 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR3 = stripeBeforeR3.jsonPath().getList("data").size();
		Response albatrossBeforeR3 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR3 = albatrossBeforeR3.jsonPath().getList("data").size();

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeCountBeforeR3 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, thirdSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterSecondRenewal, stripeInvoicesAfterThirdRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR3 + 2, "Albatross invoice count should increase after third renewal with seat reduction");
		//assertAdvanceAnalyticsActive(billingCycle, thirdSeats);
		//assertAdvanceAnalyticsSeatsSyncedWithMainSeats(customerId);
	}

	@Test 
	@Parameters("billingInterval") // BUG: This test is not working as expected. The seat counts are not being updated correctly.
	public void stripeRenewalForBusinessWithAdvanceAnalyticsAcrossThreeRenewals_Test(String billingCycle) {
		boolean annualBilling = billingCycle.equals("year");
		final int initialSeats = faker.randomSeatCountWithInList(4, 5, 6);
		final int secondSeats  = faker.randomSeatCountWithInList(7, 8, 9);
		final int thirdSeats   = faker.randomSeatCountWithInList(1, 2, 3);

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", initialSeats);
		subscribeToAdvanceAnalyticsAndAssert(billingCycle);
		assertAdvanceAnalyticsActive(billingCycle, initialSeats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, initialSeats);
		assertAdvanceAnalyticsActive(billingCycle, initialSeats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Business", secondSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, secondSeats);
		//assertAdvanceAnalyticsActive(billingCycle, secondSeats);
		//assertAdvanceAnalyticsSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, secondSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal with seat increase");
		//assertAdvanceAnalyticsActive(billingCycle, secondSeats);
		//assertAdvanceAnalyticsSeatsSyncedWithMainSeats(customerId);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Business", thirdSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, secondSeats);
		assertSeatCountsOnIntercomResponseAfterReducingSeats(thirdSeats);
		//assertAdvanceAnalyticsActive(billingCycle, thirdSeats);
		//assertAdvanceAnalyticsSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR3 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR3 = stripeBeforeR3.jsonPath().getList("data").size();
		Response albatrossBeforeR3 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR3 = albatrossBeforeR3.jsonPath().getList("data").size();

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeCountBeforeR3 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, thirdSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterSecondRenewal, stripeInvoicesAfterThirdRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR3 + 2, "Albatross invoice count should increase after third renewal with seat reduction");
		//assertAdvanceAnalyticsActive(billingCycle, thirdSeats);
		//assertAdvanceAnalyticsSeatsSyncedWithMainSeats(customerId);
	}

	@Test 
	@Parameters("billingInterval") // BUG: This test is not working as expected. The seat counts are not being updated correctly.
	public void stripeRenewalForBusinessToProWithAdvanceAnalyticsDowngrade_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = initialSeats + 10;

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndAssert(billingInterval);
		assertAdvanceAnalyticsActive(billingInterval, initialSeats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Advanced Analytics invoices");
		assertAlbatrossAdvanceAnalyticsInvoiceGroups(billingInterval, true, true);
		assertAdvanceAnalyticsActive(billingInterval, initialSeats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		//assertAdvanceAnalyticsActive(billingInterval, updatedSeats);
		//assertAdvanceAnalyticsSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal following Business-to-Pro downgrade with seat increase");
		//assertAdvanceAnalyticsActive(billingInterval, updatedSeats);
		//assertAdvanceAnalyticsSeatsSyncedWithMainSeats(customerId);
	}
	
	@Test 
	@Parameters("billingInterval")
	public void stripeRenewalForProToBusinessWithAdvanceAnalyticsUpgrade_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		final int seats = faker.randomSeatCountWithLesserValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndAssert(billingInterval);
		assertAdvanceAnalyticsActive(billingInterval, seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Advanced Analytics invoices");
		assertAlbatrossAdvanceAnalyticsInvoiceGroups(billingInterval, true, true);
		assertAdvanceAnalyticsActive(billingInterval, seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Business", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		assertAdvanceAnalyticsActive(billingInterval, seats);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal following Pro-to-Business upgrade");
		assertAdvanceAnalyticsActive(billingInterval, seats);
	}

	@Test 
	public void stripeRenewalForProMonthlyToBusinessAnnualWithAdvanceAnalytics_Test() {
		final int seats = faker.randomSeatCountWithLesserValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndAssert("month");
		assertAdvanceAnalyticsActive("month", seats);
		assertAdvanceAnalyticsCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Advanced Analytics invoices");
		assertAlbatrossAdvanceAnalyticsInvoiceGroups("month", true, true);
		assertAdvanceAnalyticsActive("month", seats);
		assertAdvanceAnalyticsCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeAdvanceAnalyticsToAnnualAndAssert();
		assertAdvanceAnalyticsActive("year", seats);
		assertAdvanceAnalyticsCancelled(true, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 6);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal following Pro-monthly to Business-annual upgrade");
		assertAdvanceAnalyticsActive("year", seats);
		assertAdvanceAnalyticsCancelled(true, "month", seats);
	}

	@Test 
	public void stripeRenewalForBusinessMonthlyToProAnnualWithAdvanceAnalytics_Test() {
		final int seats = faker.randomSeatCountWithLesserValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndAssert("month");
		assertAdvanceAnalyticsActive("month", seats);
		assertAdvanceAnalyticsCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and Advanced Analytics invoices");
		assertAlbatrossAdvanceAnalyticsInvoiceGroups("month", true, true);
		assertAdvanceAnalyticsActive("month", seats);
		assertAdvanceAnalyticsCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeAdvanceAnalyticsToAnnualAndAssert();
		assertAdvanceAnalyticsActive("year", seats);
		assertAdvanceAnalyticsCancelled(true, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 6);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal following Business-monthly to Pro-annual change");
		assertAdvanceAnalyticsActive("year", seats);
		assertAdvanceAnalyticsCancelled(true, "month", seats);
	}

	@Test 
	@Parameters("plan")
	public void stripeRenewalForAnnualAdvanceAnalyticsBlockedWithMonthlySubscription_Test(String plan) {
		String planId = resolveStripePlanId(plan);
		String planLabel = plan.equals("Team") ? "Team" : "Business";

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", planId, planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		Response blockedResponse = function.createMetabaseSubscription(albatrossURL, albatrossAuthToken, "year");
		Assert.assertEquals(blockedResponse.getStatusCode(), 422, "Subscribing to annual Advanced Analytics should be blocked when RCRM subscription is monthly");
		Assert.assertEquals(blockedResponse.jsonPath().getString("message_type"), "is-danger", "Response message_type should be 'is-danger' for blocked annual AA subscription");
		Assert.assertTrue(blockedResponse.jsonPath().getString("message").contains("Cannot Subscribe to Annual Advance Analitics Subscription with Montly RecruitCRM Subscription"));

		assertAdvanceAnalyticsNotSubscribed();

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(2, "Albatross invoice count after first renewal should include both RCRM and Advanced Analytics invoices");
		assertAdvanceAnalyticsNotSubscribed();

		blockedResponse = function.createMetabaseSubscription(albatrossURL, albatrossAuthToken, "year");
		Assert.assertEquals(blockedResponse.getStatusCode(), 422, "Subscribing to annual Advanced Analytics should be blocked when RCRM subscription is monthly");
		Assert.assertEquals(blockedResponse.jsonPath().getString("message_type"), "is-danger", "Response message_type should be 'is-danger' for blocked annual AA subscription");
		Assert.assertTrue(blockedResponse.jsonPath().getString("message").contains("Cannot Subscribe to Annual Advance Analitics Subscription with Montly RecruitCRM Subscription"));

		assertAdvanceAnalyticsNotSubscribed();
	}

	@Test
	public void stripeRenewalForAdvanceAnalyticsBlockedWithFreeAccount_Test() {
		setupFreeAccountWithTestClockAndBaseline();

		Response blockedResponse = function.createMetabaseSubscription(albatrossURL, albatrossAuthToken, faker.randomBillingInterval());
		Assert.assertEquals(blockedResponse.getStatusCode(), 401, "Subscribing to annual Advanced Analytics should be blocked when user is on free account");
		Assert.assertEquals(blockedResponse.jsonPath().getString("errorMessage"), "Unauthorised Access", "Response errorMessage should be 'Unauthorized' for blocked annual AA subscription");
	}

	@Test
	public void stripeRenewalForAdvanceAnalyticsBlockedWithEnterpriseAccount_Test() {
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String customerId  = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, faker.randomBillingInterval(), "PLN_Enterprise", "Enterprise", seats);
		
		Response blockedResponse = function.createMetabaseSubscription(albatrossURL, albatrossAuthToken, faker.randomBillingInterval());
		Assert.assertEquals(blockedResponse.getStatusCode(), 200, "Subscribing to annual Advanced Analytics should be allowed when user is on enterprise account");
		Assert.assertEquals(blockedResponse.jsonPath().getString("response_message"), "Account has Enterprise plan, Metabase subscription is not allowed to update seats");
		Assert.assertTrue(blockedResponse.jsonPath().getList("data.activeSubscription").isEmpty(), "Advanced Analytics activeSubscription should be empty when user is on enterprise account");
		Assert.assertTrue(blockedResponse.jsonPath().getList("data.cancelledSubscription").isEmpty(), "Advanced Analytics cancelledSubscription should be empty when user is on enterprise account");
	}

}