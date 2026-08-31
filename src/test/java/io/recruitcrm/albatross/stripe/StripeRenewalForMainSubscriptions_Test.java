package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;

import org.testng.Assert;
import org.testng.annotations.*;

import io.restassured.response.Response;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptions_Test extends StripeRenewalAutomationBase_Test {

	public StripeRenewalForMainSubscriptions_Test() {
		super();
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProOneSeatInterval_Test(String plan) {
		boolean annualBilling = plan.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, plan, "PLN_Team", "Team", 1);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long renewalTargetFrozenEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", plan, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, plan);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessOneSeatInterval_Test(String plan) {
		boolean annualBilling = plan.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, plan, "PLN_Business", "Business", 1);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long renewalTargetFrozenEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", plan, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, plan);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForEnterpriseOneSeatInterval_Test(String plan) {
		boolean annualBilling = plan.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, plan, "PLN_Enterprise", "Enterprise", 1);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long renewalTargetFrozenEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", plan, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, plan);
	}

	@Test(dataProvider = "getRandomSeatsData")
	public void stripeRenewalForProMonthlyMultipleSeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Team", "Team", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "month", "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterSeatChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterSeatChange, "month", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterSeatChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(stripeInvoiceCountAfterSeatChange > 2, "Stripe invoice count should increase after seat upgrade (proration or adjustment)");
		else
			Assert.assertEquals(stripeInvoiceCountAfterSeatChange, 2, "Stripe invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", expectedRcrmSeatsImmediatelyAfterSeatChange(initialSeats, updatedSeats));
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterSeatChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(albatrossInvoiceCountAfterSeatChange > 2, "Albatross invoice count should increase after seat upgrade");
		else
			Assert.assertEquals(albatrossInvoiceCountAfterSeatChange, 2, "Albatross invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "month", updatedSeats);

		if (updatedSeats > initialSeats)
			assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following seat change");
		if (updatedSeats > initialSeats)
			assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "month", updatedSeats);

		assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test(dataProvider = "getRandomSeatsData")
	public void stripeRenewalForProAnnualMultipleSeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", "PLN_Team", "Team", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterSeatChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterSeatChange, "year", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterSeatChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(stripeInvoiceCountAfterSeatChange > 2, "Stripe invoice count should increase after seat upgrade (proration or adjustment)");
		else
			Assert.assertEquals(stripeInvoiceCountAfterSeatChange, 2, "Stripe invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", expectedRcrmSeatsImmediatelyAfterSeatChange(initialSeats, updatedSeats));
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterSeatChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(albatrossInvoiceCountAfterSeatChange > 2, "Albatross invoice count should increase after seat upgrade");
		else
			Assert.assertEquals(albatrossInvoiceCountAfterSeatChange, 2, "Albatross invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		if (updatedSeats > initialSeats)
			assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following seat change");
		if (updatedSeats > initialSeats)
			assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test(dataProvider = "getRandomSeatsData")
	public void stripeRenewalForBusinessMonthlyMultipleSeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "month", "PLN_Business", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterSeatChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterSeatChange, "month", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterSeatChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(stripeInvoiceCountAfterSeatChange > 2, "Stripe invoice count should increase after seat upgrade (proration or adjustment)");
		else
			Assert.assertEquals(stripeInvoiceCountAfterSeatChange, 2, "Stripe invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", expectedRcrmSeatsImmediatelyAfterSeatChange(initialSeats, updatedSeats));
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterSeatChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(albatrossInvoiceCountAfterSeatChange > 2, "Albatross invoice count should increase after seat upgrade");
		else
			Assert.assertEquals(albatrossInvoiceCountAfterSeatChange, 2, "Albatross invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "month", updatedSeats);

		if (updatedSeats > initialSeats)
			assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following seat change");
		if (updatedSeats > initialSeats)
			assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "month", updatedSeats);

		assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test(dataProvider = "getRandomSeatsData")
	public void stripeRenewalForBusinessAnnualMultipleSeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", "PLN_Business", "Business", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterSeatChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterSeatChange, "year", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterSeatChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(stripeInvoiceCountAfterSeatChange > 2, "Stripe invoice count should increase after seat upgrade (proration or adjustment)");
		else
			Assert.assertEquals(stripeInvoiceCountAfterSeatChange, 2, "Stripe invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", expectedRcrmSeatsImmediatelyAfterSeatChange(initialSeats, updatedSeats));
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterSeatChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(albatrossInvoiceCountAfterSeatChange > 2, "Albatross invoice count should increase after seat upgrade");
		else
			Assert.assertEquals(albatrossInvoiceCountAfterSeatChange, 2, "Albatross invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		if (updatedSeats > initialSeats)
			assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following seat change");
		if (updatedSeats > initialSeats)
			assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test(dataProvider = "getRandomSeatsData")
	public void stripeRenewalForEnterpriseMonthlyMultipleSeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Enterprise", "Enterprise", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "month", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "month", "PLN_Enterprise", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterSeatChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterSeatChange, "month", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterSeatChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(stripeInvoiceCountAfterSeatChange > 2, "Stripe invoice count should increase after seat upgrade (proration or adjustment)");
		else
			Assert.assertEquals(stripeInvoiceCountAfterSeatChange, 2, "Stripe invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "month", expectedRcrmSeatsImmediatelyAfterSeatChange(initialSeats, updatedSeats));
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterSeatChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(albatrossInvoiceCountAfterSeatChange > 2, "Albatross invoice count should increase after seat upgrade");
		else
			Assert.assertEquals(albatrossInvoiceCountAfterSeatChange, 2, "Albatross invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "month", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "month", updatedSeats);

		if (updatedSeats > initialSeats)
			assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following seat change");
		if (updatedSeats > initialSeats)
			assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "month", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "month", updatedSeats);

		assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test(dataProvider = "getRandomSeatsData")
	public void stripeRenewalForEnterpriseAnnualMultipleSeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", "PLN_Enterprise", "Enterprise", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Enterprise", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterSeatChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterSeatChange, "year", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterSeatChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(stripeInvoiceCountAfterSeatChange > 2, "Stripe invoice count should increase after seat upgrade (proration or adjustment)");
		else
			Assert.assertEquals(stripeInvoiceCountAfterSeatChange, 2, "Stripe invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", expectedRcrmSeatsImmediatelyAfterSeatChange(initialSeats, updatedSeats));
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterSeatChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(albatrossInvoiceCountAfterSeatChange > 2, "Albatross invoice count should increase after seat upgrade");
		else
			Assert.assertEquals(albatrossInvoiceCountAfterSeatChange, 2, "Albatross invoice count should stay at initial + first renewal after seat downgrade when no mid-cycle invoice is created");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		if (updatedSeats > initialSeats)
			assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following seat change");
		if (updatedSeats > initialSeats)
			assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test(dataProvider = "getRandomSeatsData")
	public void stripeRenewalForProMonthlyToAnnualSeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Team", "Team", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, "year", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(stripeInvoiceCountAfterPlanChange > 2, "Stripe invoice count should increase after seat upgrade (proration or adjustment)");
		else
			Assert.assertTrue(stripeInvoiceCountAfterPlanChange >= 2, "Stripe invoice count should reflect initial cycle, first renewal, and billing-interval change to annual");

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(albatrossInvoiceCountAfterPlanChange > 2, "Albatross invoice count should increase after seat upgrade");
		else
			Assert.assertTrue(albatrossInvoiceCountAfterPlanChange >= 2, "Albatross invoice count should reflect paid cycle and billing-interval change to annual");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		if (updatedSeats > initialSeats)
			assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following plan change");
		if (updatedSeats > initialSeats)
			assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test(dataProvider = "getRandomSeatsData")
	public void stripeRenewalForBusinessMonthlyToAnnualSeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, "year", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(stripeInvoiceCountAfterPlanChange > 2, "Stripe invoice count should increase after seat upgrade (proration or adjustment)");
		else
			Assert.assertTrue(stripeInvoiceCountAfterPlanChange >= 2, "Stripe invoice count should reflect initial cycle, first renewal, and billing-interval change to annual");

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(albatrossInvoiceCountAfterPlanChange > 2, "Albatross invoice count should increase after seat upgrade");
		else
			Assert.assertTrue(albatrossInvoiceCountAfterPlanChange >= 2, "Albatross invoice count should reflect paid cycle and billing-interval change to annual");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		if (updatedSeats > initialSeats)
			assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following plan change");
		if (updatedSeats > initialSeats)
			assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test(dataProvider = "getRandomSeatsData")
	public void stripeRenewalForEnterpriseMonthlyToAnnualSeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Enterprise", "Enterprise", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "month", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Enterprise", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, "year", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(stripeInvoiceCountAfterPlanChange > 2, "Stripe invoice count should increase after seat upgrade (proration or adjustment)");
		else
			Assert.assertTrue(stripeInvoiceCountAfterPlanChange >= 2, "Stripe invoice count should reflect initial cycle, first renewal, and billing-interval change to annual");

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		if (updatedSeats > initialSeats)
			Assert.assertTrue(albatrossInvoiceCountAfterPlanChange > 2, "Albatross invoice count should increase after seat upgrade");
		else
			Assert.assertTrue(albatrossInvoiceCountAfterPlanChange >= 2, "Albatross invoice count should reflect paid cycle and billing-interval change to annual");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		if (updatedSeats > initialSeats)
			assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following plan change");
		if (updatedSeats > initialSeats)
			assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);

		assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForProMonthlyToBusinessEnterpriseAnnualUpgrade_Test(String plan) {
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = faker.randomSeatCountWithGreaterValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Team", "Team", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "month", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_" + plan, updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, "year", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(stripeInvoiceCountAfterPlanChange, 3, "Stripe invoice count should increase after monthly-to-annual tier change and seat upgrade");

		assertBusinessAccountDetailsForRequiredPlan(plan, "year", updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(albatrossInvoiceCountAfterPlanChange, 3, "Albatross invoice count should increase after monthly-to-annual tier change and seat upgrade");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan(plan, "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following monthly-to-annual tier change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan(plan, "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);
		assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	public void stripeRenewalForBusinessMonthlyToProAnnualDowngrade_Test() {
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = initialSeats + 20;

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, "year", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(stripeInvoiceCountAfterPlanChange, 3, "Stripe invoice count should increase after monthly-to-annual tier change and seat upgrade");

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(albatrossInvoiceCountAfterPlanChange, 3, "Albatross invoice count should increase after monthly-to-annual tier change and seat upgrade");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following monthly-to-annual tier change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);
		assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	public void stripeRenewalForBusinessMonthlyToEnterpriseAnnualUpgrade_Test() {
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = faker.randomSeatCountWithGreaterValue();

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Business", "Business", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "month", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Enterprise", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, "year", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(stripeInvoiceCountAfterPlanChange, 3, "Stripe invoice count should increase after monthly-to-annual tier change and seat upgrade");

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(albatrossInvoiceCountAfterPlanChange, 3, "Albatross invoice count should increase after monthly-to-annual tier change and seat upgrade");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following monthly-to-annual tier change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);
		assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForEnterpriseMonthlyToProBusinessAnnualDowngrade_Test(String plan) {
		final int initialSeats = faker.randomSeatCountWithInList(1,2,3);
		final int updatedSeats = faker.randomSeatCountWithInList(21,22,23);

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", "PLN_Enterprise", "Enterprise", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "month", initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_" + plan, updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, "year", updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(stripeInvoiceCountAfterPlanChange, 3, "Stripe invoice count should increase after monthly-to-annual tier change and seat upgrade");

		assertBusinessAccountDetailsForRequiredPlan(plan, "year", updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(albatrossInvoiceCountAfterPlanChange, 3, "Albatross invoice count should increase after monthly-to-annual tier change and seat upgrade");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan(plan, "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following monthly-to-annual tier change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan(plan, "year", updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", updatedSeats);
		assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProIntervalToBusinessIntervalUpgrade_Test(String billingInterval) {
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = faker.randomSeatCountWithGreaterValue();
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Business", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, billingInterval, updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(stripeInvoiceCountAfterPlanChange, 3, "Stripe invoice count should increase after monthly tier change and seat upgrade");

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(albatrossInvoiceCountAfterPlanChange, 3, "Albatross invoice count should increase after monthly tier change and seat upgrade");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following tier change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		if (annualBilling)
			assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
		else
			assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProIntervalToEnterpriseIntervalUpgrade_Test(String billingInterval) {
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = faker.randomSeatCountWithGreaterValue();
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Enterprise", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, billingInterval, updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(stripeInvoiceCountAfterPlanChange, 3, "Stripe invoice count should increase after monthly tier change and seat upgrade");

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(albatrossInvoiceCountAfterPlanChange, 3, "Albatross invoice count should increase after monthly tier change and seat upgrade");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following tier change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		if (annualBilling)
			assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
		else
			assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessIntervalToProIntervalDowngrade_Test(String billingInterval) {
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = initialSeats + 10;
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, billingInterval, updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(stripeInvoiceCountAfterPlanChange, 3, "Stripe invoice count should increase after monthly tier change and seat upgrade");

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(albatrossInvoiceCountAfterPlanChange, 3, "Albatross invoice count should increase after monthly tier change and seat upgrade");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following tier change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		if (annualBilling)
			assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
		else
			assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessIntervalToEnterpriseIntervalUpgrade_Test(String billingInterval) {
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = faker.randomSeatCountWithGreaterValue();
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Enterprise", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, billingInterval, updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(stripeInvoiceCountAfterPlanChange, 3, "Stripe invoice count should increase after monthly tier change and seat upgrade");

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(albatrossInvoiceCountAfterPlanChange, 3, "Albatross invoice count should increase after monthly tier change and seat upgrade");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following tier change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		if (annualBilling)
			assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
		else
			assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForEnterpriseIntervalToBusinessIntervalDowngrade_Test(String billingInterval) {
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = initialSeats + 15;
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Enterprise", "Enterprise", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Business", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, billingInterval, updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(stripeInvoiceCountAfterPlanChange, 3, "Stripe invoice count should increase after monthly tier change and seat upgrade");

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(albatrossInvoiceCountAfterPlanChange, 3, "Albatross invoice count should increase after monthly tier change and seat upgrade");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following tier change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		if (annualBilling)
			assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
		else
			assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForEnterpriseIntervalToProIntervalDowngrade_Test(String billingInterval) {
		final int initialSeats = faker.randomSeatCountWithLesserValue();
		final int updatedSeats = initialSeats + 30;
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Enterprise", "Enterprise", initialSeats);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, initialSeats);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		Response subscriptionAfterPlanChange = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterPlanChange, billingInterval, updatedSeats);

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountAfterPlanChange = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(stripeInvoiceCountAfterPlanChange, 3, "Stripe invoice count should increase after monthly tier change and seat upgrade");

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountAfterPlanChange = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Assert.assertEquals(albatrossInvoiceCountAfterPlanChange, 3, "Albatross invoice count should increase after monthly tier change and seat upgrade");

		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after renewal following tier change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal);

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		int albatrossInvoiceCountAfterSecondRenewal = albatrossInvoiceCountBeforeSecondRenewal + 1;

		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size(), albatrossInvoiceCountAfterSecondRenewal, "Albatross invoice count before third renewal should match count after second renewal");

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), billingInterval, updatedSeats);
		if (annualBilling)
			assertStripeNewestInvoiceStableFullCycleAnnualRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);
		else
			assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, false);

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountAfterSecondRenewal + 1, "Albatross invoice count should increase after third renewal (stable full-cycle)");
		if (annualBilling)
			assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
		else
			assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, false);
	}

	@Test
	public void stripeRenewalForProAnnualToMonthlySeats_Test() {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", "PLN_Team", "Team", 1);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long renewalTargetFrozenEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		Response downgradeAttempt = function.postUpgradeStripePlan(albatrossURL, albatrossAuthToken, "month", "PLN_Team", 1, 0, 0);
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message"), "Failed To Update Plan : Not allowed to downgrade");
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message_type"), "is-danger");

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", 1);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", 1);
	}

	@Test
	public void stripeRenewalForBusinessAnnualToMonthlySeats_Test() {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", "PLN_Business", "Business", 1);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long renewalTargetFrozenEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		Response downgradeAttempt = function.postUpgradeStripePlan(albatrossURL, albatrossAuthToken, "month", "PLN_Business", 1, 0, 0);
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message"), "Failed To Update Plan : Not allowed to downgrade");
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message_type"), "is-danger");

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", 1);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", 1);
	}

	@Test
	public void stripeRenewalForEnterpriseAnnualToMonthlySeats_Test() {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", "PLN_Enterprise", "Enterprise", 1);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long renewalTargetFrozenEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		Response downgradeAttempt = function.postUpgradeStripePlan(albatrossURL, albatrossAuthToken, "month", "PLN_Enterprise", 1, 0, 0);
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message"), "Failed To Update Plan : Not allowed to downgrade");
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message_type"), "is-danger");

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", 1);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", 1);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForProAnnualToBusinessOrEnterpriseMonthlyChangeBlocked_Test(String plan) {
		String targetPlanId = resolveStripePlanId(plan);
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", "PLN_Team", "Team", 3);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long renewalTargetFrozenEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", 3);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		Response downgradeAttempt = function.postUpgradeStripePlan(albatrossURL, albatrossAuthToken, "month", targetPlanId, 3, 0, 0);
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message"), "Failed To Update Plan : Not allowed to downgrade");
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message_type"), "is-danger");

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", 3);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", 3);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForBusinessAnnualToProOrEnterpriseMonthlyChangeBlocked_Test(String plan) {
		String targetPlanId = resolveStripePlanId(plan);
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", "PLN_Business", "Business", 3);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long renewalTargetFrozenEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", 3);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		Response downgradeAttempt = function.postUpgradeStripePlan(albatrossURL, albatrossAuthToken, "month", targetPlanId, 3, 0, 0);
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message"), "Failed To Update Plan : Not allowed to downgrade");
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message_type"), "is-danger");

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", 3);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", 3);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForEnterpriseAnnualToProOrBusinessMonthlyChangeBlocked_Test(String plan) {
		String targetPlanId = resolveStripePlanId(plan);
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", "PLN_Enterprise", "Enterprise", 3);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		long renewalTargetFrozenEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", 3);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		Response downgradeAttempt = function.postUpgradeStripePlan(albatrossURL, albatrossAuthToken, "month", targetPlanId, 3, 0, 0);
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message"), "Failed To Update Plan : Not allowed to downgrade");
		Assert.assertEquals(downgradeAttempt.jsonPath().getString("message_type"), "is-danger");

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", 3);
		assertStripeSubscriptionAfterPlanUpgrade(function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions"), "year", 3);
	}

}