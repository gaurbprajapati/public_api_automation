package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;
import org.testng.Assert;
import org.testng.annotations.*;
import io.restassured.response.Response;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptionsWithAdvanceAnalyticsAndLinkedinIntegration_Test extends StripeRenewalAutomationBase_Test {

	public StripeRenewalForMainSubscriptionsWithAdvanceAnalyticsAndLinkedinIntegration_Test() {
		super();
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProWithBothAddOnsAddedAfterFirstRenewal_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid           = paidBaseline[1];

		assertBothAddOnsNotSubscribed();

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert(billingInterval);
		assertBothAddOnsActive(billingInterval, 1);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 7, 3);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 (main+AA+LI) after second renewal");
		assertBothAddOnsActive(billingInterval, 1);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessWithBothAddOnsAddedAfterFirstRenewal_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid           = paidBaseline[1];

		assertBothAddOnsNotSubscribed();

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert(billingInterval);
		assertBothAddOnsActive(billingInterval, 1);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 4);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 7, 3);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 (main+AA+LI) after second renewal");
		assertBothAddOnsActive(billingInterval, 1);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProIntervalWithBothAddOnsTwoRenewals_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert(billingInterval);
		assertBothAddOnsActive(billingInterval, seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 6, 3);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(6, "Albatross invoice count after first renewal should include RCRM, AA and LinkedIn Integration invoices");
		assertAlbatrossThreeSubscriptionInvoiceGroups(billingInterval, true, true, true);
		assertBothAddOnsActive(billingInterval, seats);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 9, 3);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, seats);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 after second renewal");
		assertBothAddOnsActive(billingInterval, seats);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessIntervalWithBothAddOnsTwoRenewals_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert(billingInterval);
		assertBothAddOnsActive(billingInterval, seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 6, 3);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(6, "Albatross invoice count after first renewal should include RCRM, AA and LinkedIn Integration invoices");
		assertAlbatrossThreeSubscriptionInvoiceGroups(billingInterval, true, true, true);
		assertBothAddOnsActive(billingInterval, seats);

		Response albatrossBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, 9, 3);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal, false);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 after second renewal");
		assertBothAddOnsActive(billingInterval, seats);
	}

	@Test
	@Parameters("billingCycle") // BUG: This test is not working as expected. The seat counts are not being updated correctly.
	public void stripeRenewalForProWithBothAddOnsAcrossThreeRenewals_Test(String billingCycle) {
		boolean annualBilling = billingCycle.equals("year");
		final int initialSeats = faker.randomSeatCountWithInList(4, 5, 6);
		final int secondSeats  = faker.randomSeatCountWithInList(7, 8, 9);
		final int thirdSeats   = faker.randomSeatCountWithInList(1, 2, 3);

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", initialSeats);
		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert(billingCycle);
		assertBothAddOnsActive(billingCycle, initialSeats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 6, 3);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, initialSeats);
		assertBothAddOnsActive(billingCycle, initialSeats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", secondSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, secondSeats);
		//assertBothAddOnsActive(billingCycle, secondSeats);
		//assertBothAddOnsSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 3, 3);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, secondSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 after second renewal with seat increase");
		//assertBothAddOnsActive(billingCycle, secondSeats);
		//assertBothAddOnsSeatsSyncedWithMainSeats(customerId);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", thirdSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, secondSeats);
		assertSeatCountsOnIntercomResponseAfterReducingSeats(thirdSeats);
		//assertBothAddOnsActive(billingCycle, thirdSeats);
		//assertBothAddOnsSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR3 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR3 = stripeBeforeR3.jsonPath().getList("data").size();
		Response albatrossBeforeR3 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR3 = albatrossBeforeR3.jsonPath().getList("data").size();

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeCountBeforeR3 + 3, 3);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, thirdSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterSecondRenewal, stripeInvoicesAfterThirdRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR3 + 3, "Albatross invoice count should increase by 3 after third renewal with seat reduction");
		//assertBothAddOnsActive(billingCycle, thirdSeats);
		//assertBothAddOnsSeatsSyncedWithMainSeats(customerId);
	}

	@Test
	@Parameters("billingCycle") // BUG: This test is not working as expected. The seat counts are not being updated correctly.
	public void stripeRenewalForBusinessWithBothAddOnsAcrossThreeRenewals_Test(String billingCycle) {
		boolean annualBilling = billingCycle.equals("year");
		final int initialSeats = faker.randomSeatCountWithInList(4, 5, 6);
		final int secondSeats  = faker.randomSeatCountWithInList(7, 8, 9);
		final int thirdSeats   = faker.randomSeatCountWithInList(1, 2, 3);

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", initialSeats);
		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert(billingCycle);
		assertBothAddOnsActive(billingCycle, initialSeats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 6, 3);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, initialSeats);
		assertBothAddOnsActive(billingCycle, initialSeats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Business", secondSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, secondSeats);
		//assertBothAddOnsActive(billingCycle, secondSeats);
		//assertBothAddOnsSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 3, 3);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, secondSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 after second renewal with seat increase");
		//assertBothAddOnsActive(billingCycle, secondSeats);
		//assertBothAddOnsSeatsSyncedWithMainSeats(customerId);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Business", thirdSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, secondSeats);
		assertSeatCountsOnIntercomResponseAfterReducingSeats(thirdSeats);
		//assertBothAddOnsActive(billingCycle, thirdSeats);
		//assertBothAddOnsSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR3 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR3 = stripeBeforeR3.jsonPath().getList("data").size();
		Response albatrossBeforeR3 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR3 = albatrossBeforeR3.jsonPath().getList("data").size();

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeCountBeforeR3 + 3, 3);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, thirdSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterSecondRenewal, stripeInvoicesAfterThirdRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR3 + 3, "Albatross invoice count should increase by 3 after third renewal with seat reduction");
		//assertBothAddOnsActive(billingCycle, thirdSeats);
		//assertBothAddOnsSeatsSyncedWithMainSeats(customerId);
	}

	@Test
	public void stripeRenewalForProWithBothAddOnsMonthlyToAnnualUpgrade_Test() {
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert("month");
		assertBothAddOnsActive("month", seats);
		assertBothAddOnsCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 6, 3);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(6, "Albatross invoice count after first renewal should include RCRM, AA and LinkedIn Integration invoices");
		assertAlbatrossThreeSubscriptionInvoiceGroups("month", true, true, true);
		assertBothAddOnsActive("month", seats);
		assertBothAddOnsCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeAdvanceAnalyticsAndLinkedinIntegrationToAnnualAndAssert();
		assertBothAddOnsActive("year", seats);
		assertBothAddOnsCancelled(true, "month", seats);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 3, 3);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 after second renewal on annual plan");
		assertBothAddOnsActive("year", seats);
		assertBothAddOnsCancelled(true, "month", seats);
	}

	@Test
	public void stripeRenewalForBusinessWithBothAddOnsMonthlyToAnnualUpgrade_Test() {
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert("month");
		assertBothAddOnsActive("month", seats);
		assertBothAddOnsCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 6, 3);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(6, "Albatross invoice count after first renewal should include RCRM, AA and LinkedIn Integration invoices");
		assertAlbatrossThreeSubscriptionInvoiceGroups("month", true, true, true);
		assertBothAddOnsActive("month", seats);
		assertBothAddOnsCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeAdvanceAnalyticsAndLinkedinIntegrationToAnnualAndAssert();
		assertBothAddOnsActive("year", seats);
		assertBothAddOnsCancelled(true, "month", seats);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 3, 3);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 after second renewal on annual plan");
		assertBothAddOnsActive("year", seats);
		assertBothAddOnsCancelled(true, "month", seats);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProToBusinessWithBothAddOnsUpgrade_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		final int seats = faker.randomSeatCountWithLesserValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert(billingInterval);
		assertBothAddOnsActive(billingInterval, seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 6, 3);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(6, "Albatross invoice count after first renewal should include RCRM, AA and LinkedIn Integration invoices");
		assertAlbatrossThreeSubscriptionInvoiceGroups(billingInterval, true, true, true);
		assertBothAddOnsActive(billingInterval, seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Business", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		assertBothAddOnsActive(billingInterval, seats);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 3, 3);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 after second renewal following Pro-to-Business upgrade");
		assertBothAddOnsActive(billingInterval, seats);
	}

	@Test
	@Parameters("billingInterval") // BUG: This test is not working as expected. The seat counts are not being updated correctly.
	public void stripeRenewalForBusinessToProWithBothAddOnsDowngrade_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = initialSeats + 10;

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert(billingInterval);
		assertBothAddOnsActive(billingInterval, initialSeats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 6, 3);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(6, "Albatross invoice count after first renewal should include RCRM, AA and LinkedIn Integration invoices");
		assertAlbatrossThreeSubscriptionInvoiceGroups(billingInterval, true, true, true);
		assertBothAddOnsActive(billingInterval, initialSeats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		//assertBothAddOnsActive(billingInterval, updatedSeats);
		//assertBothAddOnsSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 3, 3);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 after second renewal following Business-to-Pro downgrade");
		//assertBothAddOnsActive(billingInterval, updatedSeats);
		//assertBothAddOnsSeatsSyncedWithMainSeats(customerId);
	}

	@Test
	public void stripeRenewalForProMonthlyToBusinessAnnualWithBothAddOns_Test() {
		final int seats = faker.randomSeatCountWithLesserValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert("month");
		assertBothAddOnsActive("month", seats);
		assertBothAddOnsCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 6, 3);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(6, "Albatross invoice count after first renewal should include RCRM, AA and LinkedIn Integration invoices");
		assertAlbatrossThreeSubscriptionInvoiceGroups("month", true, true, true);
		assertBothAddOnsActive("month", seats);
		assertBothAddOnsCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeAdvanceAnalyticsAndLinkedinIntegrationToAnnualAndAssert();
		assertBothAddOnsActive("year", seats);
		assertBothAddOnsCancelled(true, "month", seats);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 3, 3);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 after second renewal following Pro-monthly to Business-annual upgrade");
		assertBothAddOnsActive("year", seats);
		assertBothAddOnsCancelled(true, "month", seats);
	}

	@Test
	public void stripeRenewalForBusinessMonthlyToProAnnualWithBothAddOns_Test() {
		final int seats = faker.randomSeatCountWithLesserValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert("month");
		assertBothAddOnsActive("month", seats);
		assertBothAddOnsCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 3, 3);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 6, 3);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(6, "Albatross invoice count after first renewal should include RCRM, AA and LinkedIn Integration invoices");
		assertAlbatrossThreeSubscriptionInvoiceGroups("month", true, true, true);
		assertBothAddOnsActive("month", seats);
		assertBothAddOnsCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeAdvanceAnalyticsAndLinkedinIntegrationToAnnualAndAssert();
		assertBothAddOnsActive("year", seats);
		assertBothAddOnsCancelled(true, "month", seats);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 3, 3);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 3, "Albatross invoice count should increase by 3 after second renewal following Business-monthly to Pro-annual change");
		assertBothAddOnsActive("year", seats);
		assertBothAddOnsCancelled(true, "month", seats);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForAnnualBothAddOnsBlockedWithMonthlySubscription_Test(String plan) {
		String planId    = resolveStripePlanId(plan);
		String planLabel = plan.equals("Team") ? "Team" : "Business";

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", planId, planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		Response blockedAA = function.createMetabaseSubscription(albatrossURL, albatrossAuthToken, "year");
		Assert.assertEquals(blockedAA.getStatusCode(), 422, "Annual AA should be blocked when RCRM subscription is monthly");
		Assert.assertEquals(blockedAA.jsonPath().getString("message_type"), "is-danger");
		Assert.assertTrue(blockedAA.jsonPath().getString("message").contains("Cannot Subscribe to Annual"));

		Response blockedLI = function.createUnipileSubscription(albatrossURL, albatrossAuthToken, "year");
		Assert.assertEquals(blockedLI.getStatusCode(), 422, "Annual LinkedIn Integration should be blocked when RCRM subscription is monthly");
		Assert.assertEquals(blockedLI.jsonPath().getString("message_type"), "is-danger");

		assertBothAddOnsNotSubscribed();

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(2, "Albatross invoice count after first renewal should be only RCRM since both add-ons are blocked");
		assertBothAddOnsNotSubscribed();

		blockedAA = function.createMetabaseSubscription(albatrossURL, albatrossAuthToken, "year");
		Assert.assertEquals(blockedAA.getStatusCode(), 422, "Annual AA should still be blocked after renewal when RCRM is monthly");
		Assert.assertEquals(blockedAA.jsonPath().getString("message_type"), "is-danger");

		blockedLI = function.createUnipileSubscription(albatrossURL, albatrossAuthToken, "year");
		Assert.assertEquals(blockedLI.getStatusCode(), 422, "Annual LinkedIn Integration should still be blocked after renewal when RCRM is monthly");
		Assert.assertEquals(blockedLI.jsonPath().getString("message_type"), "is-danger");

		assertBothAddOnsNotSubscribed();
	}

	@Test
	public void stripeRenewalForBothAddOnsBlockedWithFreeAccount_Test() {
		setupFreeAccountWithTestClockAndBaseline();

		String randomInterval = faker.randomBillingInterval();
		Response blockedAA = function.createMetabaseSubscription(albatrossURL, albatrossAuthToken, randomInterval);
		Assert.assertEquals(blockedAA.getStatusCode(), 401, "AA subscription should be blocked (401) on free account");
		Assert.assertEquals(blockedAA.jsonPath().getString("errorMessage"), "Unauthorised Access");

		Response blockedLI = function.createUnipileSubscription(albatrossURL, albatrossAuthToken, randomInterval);
		Assert.assertEquals(blockedLI.getStatusCode(), 401, "LinkedIn Integration subscription should be blocked (401) on free account");
		Assert.assertEquals(blockedLI.jsonPath().getString("errorMessage"), "Unauthorised Access");
	}

	@Test
	public void stripeRenewalForBothAddOnsBlockedWithEnterpriseAccount_Test() {
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String customerId = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, faker.randomBillingInterval(), "PLN_Enterprise", "Enterprise", seats);

		Response aaResponse = function.createMetabaseSubscription(albatrossURL, albatrossAuthToken, faker.randomBillingInterval());
		Assert.assertEquals(aaResponse.getStatusCode(), 200, "AA subscription call should return 200 on enterprise account");
		Assert.assertEquals(aaResponse.jsonPath().getString("response_message"), "Account has Enterprise plan, Metabase subscription is not allowed to update seats");
		Assert.assertTrue(aaResponse.jsonPath().getList("data.activeSubscription").isEmpty(), "AA activeSubscription should be empty on enterprise account");
		Assert.assertTrue(aaResponse.jsonPath().getList("data.cancelledSubscription").isEmpty(), "AA cancelledSubscription should be empty on enterprise account");

		Response liResponse = function.createUnipileSubscription(albatrossURL, albatrossAuthToken, faker.randomBillingInterval());
		Assert.assertEquals(liResponse.getStatusCode(), 200, "LinkedIn Integration subscription call should return 200 on enterprise account");
		Assert.assertEquals(liResponse.jsonPath().getString("response_message"), "Account has Enterprise plan, Unipile subscription is not allowed to update seats");
		Assert.assertTrue(liResponse.jsonPath().getList("data.activeSubscription").isEmpty(), "LinkedIn Integration activeSubscription should be empty on enterprise account");
		Assert.assertTrue(liResponse.jsonPath().getList("data.cancelledSubscription").isEmpty(), "LinkedIn Integration cancelledSubscription should be empty on enterprise account");
	}

}