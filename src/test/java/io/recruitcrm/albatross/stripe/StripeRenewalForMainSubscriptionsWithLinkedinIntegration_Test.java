package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;
import org.testng.Assert;
import org.testng.annotations.*;
import io.restassured.response.Response;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptionsWithLinkedinIntegration_Test extends StripeRenewalAutomationBase_Test {

	public StripeRenewalForMainSubscriptionsWithLinkedinIntegration_Test() {
		super();
	}

	@Test 
	@Parameters("billingInterval")
	public void stripeRenewalForProWithLinkedinIntegrationAddedAfterFirstRenewal_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertLinkedinIntegrationNotSubscribed();

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		subscribeToLinkedinIntegrationAndAssert(billingInterval);
		assertLinkedinIntegrationActive(billingInterval, 1);
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
		assertLinkedinIntegrationActive(billingInterval, 1);
	}

	@Test 
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessWithLinkedinIntegrationAddedAfterFirstRenewal_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid           = paidBaseline[1];

		assertLinkedinIntegrationNotSubscribed();

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		subscribeToLinkedinIntegrationAndAssert(billingInterval);
		assertLinkedinIntegrationActive(billingInterval, 1);
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
		assertLinkedinIntegrationActive(billingInterval, 1);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProIntervalWithLinkedinIntegrationTwoRenewals_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToLinkedinIntegrationAndAssert(billingInterval);
		assertLinkedinIntegrationActive(billingInterval, seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and LinkedIn Integration invoices");
		assertAlbatrossLinkedinIntegrationInvoiceGroups(billingInterval, true, true);
		assertLinkedinIntegrationActive(billingInterval, seats);

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
		assertLinkedinIntegrationActive(billingInterval, seats);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessIntervalWithLinkedinIntegrationTwoRenewals_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToLinkedinIntegrationAndAssert(billingInterval);
		assertLinkedinIntegrationActive(billingInterval, seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and LinkedIn Integration invoices");
		assertAlbatrossLinkedinIntegrationInvoiceGroups(billingInterval, true, true);
		assertLinkedinIntegrationActive(billingInterval, seats);

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
		assertLinkedinIntegrationActive(billingInterval, seats);
	}

	@Test 
	public void stripeRenewalForProWithLinkedinIntegrationMonthlyToAnnualUpgrade_Test() {
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToLinkedinIntegrationAndAssert("month");
		assertLinkedinIntegrationActive("month", seats);
		assertLinkedinIntegrationCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and LinkedIn Integration invoices");
		assertAlbatrossLinkedinIntegrationInvoiceGroups("month", true, true);
		assertLinkedinIntegrationActive("month", seats);
		assertLinkedinIntegrationCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeLinkedinIntegrationToAnnualAndAssert();
		assertLinkedinIntegrationActive("year", seats);
		assertLinkedinIntegrationCancelled(true, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 6);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal on annual plan");
		assertLinkedinIntegrationActive("year", seats);
		assertLinkedinIntegrationCancelled(true, "month", seats);
	}

	@Test 
	public void stripeRenewalForBusinessWithLinkedinIntegrationMonthlyToAnnualUpgrade_Test() {
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToLinkedinIntegrationAndAssert("month");
		assertLinkedinIntegrationActive("month", seats);
		assertLinkedinIntegrationCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and LinkedIn Integration invoices");
		assertAlbatrossLinkedinIntegrationInvoiceGroups("month", true, true);
		assertLinkedinIntegrationActive("month", seats);
		assertLinkedinIntegrationCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeLinkedinIntegrationToAnnualAndAssert();
		assertLinkedinIntegrationActive("year", seats);
		assertLinkedinIntegrationCancelled(true, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 6);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal on annual plan");
		assertLinkedinIntegrationActive("year", seats);
		assertLinkedinIntegrationCancelled(true, "month", seats);
	}
	
	@Test 
	@Parameters("billingInterval") // BUG: This test is not working as expected. The seat counts are not being updated correctly.
	public void stripeRenewalForProWithLinkedinIntegrationAcrossThreeRenewals_Test(String billingCycle) {
		boolean annualBilling = billingCycle.equals("year");
		final int initialSeats = faker.randomSeatCountWithInList(4,5,6);
		final int secondSeats  = faker.randomSeatCountWithInList(7,8,9);
		final int thirdSeats   = faker.randomSeatCountWithInList(1,2,3);

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Team", "Team", initialSeats);
		subscribeToLinkedinIntegrationAndAssert(billingCycle);
		assertLinkedinIntegrationActive(billingCycle, initialSeats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, initialSeats);
		assertLinkedinIntegrationActive(billingCycle, initialSeats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", secondSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, secondSeats);
		//assertLinkedinIntegrationActive(billingCycle, secondSeats);
		//assertLinkedinIntegrationSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, secondSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal with seat increase");
		//assertLinkedinIntegrationActive(billingCycle, secondSeats);
		//assertLinkedinIntegrationSeatsSyncedWithMainSeats(customerId);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Team", thirdSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, secondSeats);
		assertSeatCountsOnIntercomResponseAfterReducingSeats(thirdSeats);
		//assertLinkedinIntegrationActive(billingCycle, thirdSeats);
		//assertLinkedinIntegrationSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR3 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR3 = stripeBeforeR3.jsonPath().getList("data").size();
		Response albatrossBeforeR3 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR3 = albatrossBeforeR3.jsonPath().getList("data").size();

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeCountBeforeR3 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingCycle, thirdSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterSecondRenewal, stripeInvoicesAfterThirdRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR3 + 2, "Albatross invoice count should increase after third renewal with seat reduction");
		//assertLinkedinIntegrationActive(billingCycle, thirdSeats);
		//assertLinkedinIntegrationSeatsSyncedWithMainSeats(customerId);
	}

	@Test 
	@Parameters("billingInterval") // BUG: This test is not working as expected. The seat counts are not being updated correctly.
	public void stripeRenewalForBusinessWithLinkedinIntegrationAcrossThreeRenewals_Test(String billingCycle) {
		boolean annualBilling = billingCycle.equals("year");
		final int initialSeats = faker.randomSeatCountWithInList(4, 5, 6);
		final int secondSeats  = faker.randomSeatCountWithInList(7, 8, 9);
		final int thirdSeats   = faker.randomSeatCountWithInList(1, 2, 3);

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingCycle, "PLN_Business", "Business", initialSeats);
		subscribeToLinkedinIntegrationAndAssert(billingCycle);
		assertLinkedinIntegrationActive(billingCycle, initialSeats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, initialSeats);
		assertLinkedinIntegrationActive(billingCycle, initialSeats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Business", secondSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, secondSeats);
		//assertLinkedinIntegrationActive(billingCycle, secondSeats);
		//assertLinkedinIntegrationSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, secondSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal with seat increase");
		//assertLinkedinIntegrationActive(billingCycle, secondSeats);
		//assertLinkedinIntegrationSeatsSyncedWithMainSeats(customerId);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingCycle, "PLN_Business", thirdSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, secondSeats);
		assertSeatCountsOnIntercomResponseAfterReducingSeats(thirdSeats);
		//assertLinkedinIntegrationActive(billingCycle, thirdSeats);
		//assertLinkedinIntegrationSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR3 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR3 = stripeBeforeR3.jsonPath().getList("data").size();
		Response albatrossBeforeR3 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR3 = albatrossBeforeR3.jsonPath().getList("data").size();

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeCountBeforeR3 + 2, 2);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingCycle, thirdSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterSecondRenewal, stripeInvoicesAfterThirdRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR3 + 2, "Albatross invoice count should increase after third renewal with seat reduction");
		//assertLinkedinIntegrationActive(billingCycle, thirdSeats);
		//assertLinkedinIntegrationSeatsSyncedWithMainSeats(customerId);
	}

	@Test 
	@Parameters("billingInterval") // BUG: This test is not working as expected. The seat counts are not being updated correctly.
	public void stripeRenewalForBusinessToProWithLinkedinIntegrationDowngrade_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = initialSeats + 10;

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToLinkedinIntegrationAndAssert(billingInterval);
		assertLinkedinIntegrationActive(billingInterval, initialSeats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and LinkedIn Integration invoices");
		assertAlbatrossLinkedinIntegrationInvoiceGroups(billingInterval, true, true);
		assertLinkedinIntegrationActive(billingInterval, initialSeats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		//assertLinkedinIntegrationActive(billingInterval, updatedSeats);
		//assertLinkedinIntegrationSeatsSyncedWithMainSeats(customerId);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal following Business-to-Pro downgrade with seat increase");
		//assertLinkedinIntegrationActive(billingInterval, updatedSeats);
		//assertLinkedinIntegrationSeatsSyncedWithMainSeats(customerId);
	}
	
	@Test 
	@Parameters("billingInterval")
	public void stripeRenewalForProToBusinessWithLinkedinIntegrationUpgrade_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		final int seats = faker.randomSeatCountWithLesserValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToLinkedinIntegrationAndAssert(billingInterval);
		assertLinkedinIntegrationActive(billingInterval, seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and LinkedIn Integration invoices");
		assertAlbatrossLinkedinIntegrationInvoiceGroups(billingInterval, true, true);
		assertLinkedinIntegrationActive(billingInterval, seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Business", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		assertLinkedinIntegrationActive(billingInterval, seats);

		Response stripeBeforeR2 = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeCountBeforeR2 = stripeBeforeR2.jsonPath().getList("data").size();
		Response albatrossBeforeR2 = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossCountBeforeR2 = albatrossBeforeR2.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeCountBeforeR2 + 2, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, seats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesAfterFirstRenewal, stripeInvoicesAfterSecondRenewal);
		fetchAlbatrossInvoicesAssertCount(albatrossCountBeforeR2 + 2, "Albatross invoice count should increase after second renewal following Pro-to-Business upgrade");
		assertLinkedinIntegrationActive(billingInterval, seats);
	}

	@Test 
	public void stripeRenewalForProMonthlyToBusinessAnnualWithLinkedinIntegration_Test() {
		final int seats = faker.randomSeatCountWithLesserValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Team", "Team", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToLinkedinIntegrationAndAssert("month");
		assertLinkedinIntegrationActive("month", seats);
		assertLinkedinIntegrationCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and LinkedIn Integration invoices");
		assertAlbatrossLinkedinIntegrationInvoiceGroups("month", true, true);
		assertLinkedinIntegrationActive("month", seats);
		assertLinkedinIntegrationCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeLinkedinIntegrationToAnnualAndAssert();
		assertLinkedinIntegrationActive("year", seats);
		assertLinkedinIntegrationCancelled(true, "month", seats);
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
		assertLinkedinIntegrationActive("year", seats);
		assertLinkedinIntegrationCancelled(true, "month", seats);
	}

	@Test 
	public void stripeRenewalForBusinessMonthlyToProAnnualWithLinkedinIntegration_Test() {
		final int seats = faker.randomSeatCountWithLesserValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", seats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		subscribeToLinkedinIntegrationAndAssert("month");
		assertLinkedinIntegrationActive("month", seats);
		assertLinkedinIntegrationCancelled(false, "month", seats);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 2, 2);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 4, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", seats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(4, "Albatross invoice count after first renewal should include both RCRM and LinkedIn Integration invoices");
		assertAlbatrossLinkedinIntegrationInvoiceGroups("month", true, true);
		assertLinkedinIntegrationActive("month", seats);
		assertLinkedinIntegrationCancelled(false, "month", seats);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", seats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", "year", seats);

		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		upgradeLinkedinIntegrationToAnnualAndAssert();
		assertLinkedinIntegrationActive("year", seats);
		assertLinkedinIntegrationCancelled(true, "month", seats);
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
		assertLinkedinIntegrationActive("year", seats);
		assertLinkedinIntegrationCancelled(true, "month", seats);
	}

	@Test 
	@Parameters("plan")
	public void stripeRenewalForAnnualLinkedinIntegrationBlockedWithMonthlySubscription_Test(String plan) {
		String planId = resolveStripePlanId(plan);
		String planLabel = plan.equals("Team") ? "Team" : "Business";

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId  = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", planId, planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];

		Response blockedResponse = function.createUnipileSubscription(albatrossURL, albatrossAuthToken, "year");
		Assert.assertEquals(blockedResponse.getStatusCode(), 422, "Subscribing to annual LinkedIn Integration should be blocked when RCRM subscription is monthly");
		Assert.assertEquals(blockedResponse.jsonPath().getString("message_type"), "is-danger", "Response message_type should be 'is-danger' for blocked annual LinkedIn Integration subscription");
		Assert.assertTrue(blockedResponse.jsonPath().getString("message").contains("Cannot Subscribe to Annual") && blockedResponse.jsonPath().getString("message").contains("Montly RecruitCRM Subscription"));

		assertLinkedinIntegrationNotSubscribed();

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		fetchAlbatrossInvoicesAssertCount(2, "Albatross invoice count after first renewal should include both RCRM and LinkedIn Integration invoices");
		assertLinkedinIntegrationNotSubscribed();

		blockedResponse = function.createUnipileSubscription(albatrossURL, albatrossAuthToken, "year");
		Assert.assertEquals(blockedResponse.getStatusCode(), 422, "Subscribing to annual LinkedIn Integration should be blocked when RCRM subscription is monthly");
		Assert.assertEquals(blockedResponse.jsonPath().getString("message_type"), "is-danger", "Response message_type should be 'is-danger' for blocked annual LinkedIn Integration subscription");
		Assert.assertTrue(blockedResponse.jsonPath().getString("message").contains("Cannot Subscribe to Annual") && blockedResponse.jsonPath().getString("message").contains("Montly RecruitCRM Subscription"));

		assertLinkedinIntegrationNotSubscribed();
	}

	@Test
	public void stripeRenewalForLinkedinIntegrationBlockedWithFreeAccount_Test() {
		setupFreeAccountWithTestClockAndBaseline();

		Response blockedResponse = function.createUnipileSubscription(albatrossURL, albatrossAuthToken, faker.randomBillingInterval());
		Assert.assertEquals(blockedResponse.getStatusCode(), 401, "Subscribing to LinkedIn Integration should be blocked when user is on free account");
		Assert.assertEquals(blockedResponse.jsonPath().getString("errorMessage"), "Unauthorised Access", "Response errorMessage should be 'Unauthorised Access' for blocked LinkedIn Integration subscription");
	}

	@Test
	public void stripeRenewalForLinkedinIntegrationBlockedWithEnterpriseAccount_Test() {
		int seats = faker.randomSeatCountWithLesserValue();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String customerId  = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, faker.randomBillingInterval(), "PLN_Enterprise", "Enterprise", seats);
		
		Response blockedResponse = function.createUnipileSubscription(albatrossURL, albatrossAuthToken, faker.randomBillingInterval());
		Assert.assertEquals(blockedResponse.getStatusCode(), 200, "Subscribing to LinkedIn Integration should return enterprise-blocked response when user is on enterprise account");
		Assert.assertEquals(blockedResponse.jsonPath().getString("response_message"), "Account has Enterprise plan, Unipile subscription is not allowed to update seats");
		Assert.assertTrue(blockedResponse.jsonPath().getList("data.activeSubscription").isEmpty(), "LinkedIn Integration activeSubscription should be empty when user is on enterprise account");
		Assert.assertTrue(blockedResponse.jsonPath().getList("data.cancelledSubscription").isEmpty(), "LinkedIn Integration cancelledSubscription should be empty when user is on enterprise account");
	}

}