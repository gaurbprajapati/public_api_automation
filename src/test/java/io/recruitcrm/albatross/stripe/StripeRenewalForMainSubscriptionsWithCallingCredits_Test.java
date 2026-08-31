package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;

import org.testng.annotations.*;
import io.restassured.response.Response;

import java.util.Arrays;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptionsWithCallingCredits_Test extends StripeRenewalAutomationBase_Test {

	public StripeRenewalForMainSubscriptionsWithCallingCredits_Test() {
		super();
	}

	@Test
	public void stripeRenewalForFreeCallingCreditsWithFreePlan_Test() {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String customerId = cc[1];

		assertFreeCallingCredits(0, "Free", 0.1);

		assertNoAlbatrossCallingCreditTransactions();
		assertStripeCallingCreditChargesCount(customerId, 0);

		// BUG - Buying calling credits is not allowed for Free plan - should be allowed
		// Response response = purchaseCallingCreditsAndAssert(50, 1);
		// Assert.assertEquals(response.getStatusCode(), 200, "Calling credit payment response status code should be 200");

		// Assert.assertEquals(response.jsonPath().getString("message"), "Calling Credit Payment is not allowed for Free plan");
		// Assert.assertEquals(response.jsonPath().getString("message_type"), "is-danger");
		// Assert.assertNull(response.jsonPath().getString("data.payment_intent.id"), "Calling credit payment intent id should be null");
		// Assert.assertEquals(response.jsonPath().getInt("data.payment_intent.amount"), 0, "Calling credit payment intent amount is not matched : " + response.jsonPath().getInt("data.payment_intent.amount"));
		// assertRequiredPlanCallingCredits(fetchTwilioCreditUsage(), 0, "Free");
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForFreeCallingCreditsWithMonthlyPaidPlan_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		assertFreeCallingCredits(0, "Free", 0.1);
		upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(0);
			verifyTwilioCreditsOnIntercomResponse(0.1);
		} else {
			assertFreeCallingCredits(0, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(0.0);
		}
		assertNoAlbatrossCallingCreditTransactions();
		assertStripeCallingCreditChargesCount(customerId, 0);

		purchaseCallingCreditsAndAssert(50, 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(50);
		} else {
			assertFreeCallingCredits(50, plan, 0.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 1, 50, 1);
		assertAlbatrossCallingCreditTransactions(1, 50, 1);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForFreeCallingCreditsWithAnnualPaidPlan_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		assertFreeCallingCredits(0, "Free", 0.1);
		upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", resolveStripePlanId(plan), planLabel, 1);
		assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(0);
			verifyTwilioCreditsOnIntercomResponse(0.1);
		} else {
			assertFreeCallingCredits(0, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(0.0);
		}
		assertNoAlbatrossCallingCreditTransactions();
		assertStripeCallingCreditChargesCount(customerId, 0);

		purchaseCallingCreditsAndAssert(50, 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(50);
		} else {
			assertFreeCallingCredits(50, plan, 0.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 1, 50, 1);
		assertAlbatrossCallingCreditTransactions(1, 50, 1);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForPurchaseOneSetOfCallingCreditsWithMonthlyPaidPlanAcrossOneRenewal_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		assertFreeCallingCredits(0, "Free", 0.1);
		verifyTwilioCreditsOnIntercomResponse(0.1);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];
		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			verifyTwilioCreditsOnIntercomResponse(0.1);
		} else {
			verifyTwilioCreditsOnIntercomResponse(0.0);
		}

		assertNoAlbatrossCallingCreditTransactions();
		assertStripeCallingCreditChargesCount(customerId, 0);

		purchaseCallingCreditsAndAssert(50, 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(50);
			verifyTwilioCreditsOnIntercomResponse(50.1);
		} else {
			assertFreeCallingCredits(50, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(50.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 1, 50, 1);
		assertAlbatrossCallingCreditTransactions(1, 50, 1);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(50);
			verifyTwilioCreditsOnIntercomResponse(50.1);
		} else {
			assertFreeCallingCredits(50, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(50.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 1, 50, 1);
		assertAlbatrossCallingCreditTransactions(1, 50, 1);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForPurchaseOneSetOfCallingCreditsWithAnnualPaidPlanAcrossOneRenewal_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		assertFreeCallingCredits(0, "Free", 0.1);
		verifyTwilioCreditsOnIntercomResponse(0.1);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];
		assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);

		if(plan.equalsIgnoreCase("Enterprise")) {
			verifyTwilioCreditsOnIntercomResponse(0.1);
		} else {
			verifyTwilioCreditsOnIntercomResponse(0.0);
		}

		assertNoAlbatrossCallingCreditTransactions();
		assertStripeCallingCreditChargesCount(customerId, 0);

		purchaseCallingCreditsAndAssert(50, 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(50);
			verifyTwilioCreditsOnIntercomResponse(50.1);
		} else {
			assertFreeCallingCredits(50, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(50.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 1, 50, 1);
		assertAlbatrossCallingCreditTransactions(1, 50, 1);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(50);
			verifyTwilioCreditsOnIntercomResponse(50.1);
		} else {
			assertFreeCallingCredits(50, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(50.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 1, 50, 1);
		assertAlbatrossCallingCreditTransactions(1, 50, 1);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForPurchaseOneSetOfCallingCreditsWithMonthlyPaidPlanAcrossTwoRenewals_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		assertFreeCallingCredits(0, "Free", 0.1);
		verifyTwilioCreditsOnIntercomResponse(0.1);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];
		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);

		if(plan.equalsIgnoreCase("Enterprise")) {
			verifyTwilioCreditsOnIntercomResponse(0.1);
		} else {
			verifyTwilioCreditsOnIntercomResponse(0.0);
		}

		assertNoAlbatrossCallingCreditTransactions();
		assertStripeCallingCreditChargesCount(customerId, 0);

		purchaseCallingCreditsAndAssert(50, 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(50);
			verifyTwilioCreditsOnIntercomResponse(50.1);
		} else {
			assertFreeCallingCredits(50, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(50.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 1, 50, 1);
		assertAlbatrossCallingCreditTransactions(1, 50, 1);

		purchaseCallingCreditsAndAssert(50, 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(100);
			verifyTwilioCreditsOnIntercomResponse(100.1);
		} else {
			assertFreeCallingCredits(100, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(100.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 2, 50, 1);
		assertAlbatrossCallingCreditTransactions(2, 50, 1);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(100);
			verifyTwilioCreditsOnIntercomResponse(100.1);
		} else {
			assertFreeCallingCredits(100, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(100.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 2, 50, 1);
		assertAlbatrossCallingCreditTransactions(2, 50, 1);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForPurchaseOneSetOfCallingCreditsWithAnnualPaidPlanAcrossTwoRenewals_Test(String plan) {
		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		assertFreeCallingCredits(0, "Free", 0.1);
		verifyTwilioCreditsOnIntercomResponse(0.1);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];
		assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);
		if (plan.equalsIgnoreCase("Enterprise")) {
			verifyTwilioCreditsOnIntercomResponse(0.1);
		} else {
			verifyTwilioCreditsOnIntercomResponse(0.0);
		}
		assertNoAlbatrossCallingCreditTransactions();
		assertStripeCallingCreditChargesCount(customerId, 0);

		purchaseCallingCreditsAndAssert(50, 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(50);
			verifyTwilioCreditsOnIntercomResponse(50.1);
		} else {
			assertFreeCallingCredits(50, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(50.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 1, 50, 1);
		assertAlbatrossCallingCreditTransactions(1, 50, 1);

		purchaseCallingCreditsAndAssert(50, 1);

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(100);
			verifyTwilioCreditsOnIntercomResponse(100.1);
		} else {
			assertFreeCallingCredits(100, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(100.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 2, 50, 1);
		assertAlbatrossCallingCreditTransactions(2, 50, 1);
		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");

		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(100);
			verifyTwilioCreditsOnIntercomResponse(100.1);
		} else {
			assertFreeCallingCredits(100, plan, 0.0);
			verifyTwilioCreditsOnIntercomResponse(100.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 2, 50, 1);
		assertAlbatrossCallingCreditTransactions(2, 50, 1);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForPurchaseMultipleSetOfCallingCreditsWithMonthlyPaidPlan_Test(String plan) {
		final int callingCreditPrice = 50;
		int purchaseCount = faker.randomCallingCreditPurchaseCount();
		int[] quantities = new int[purchaseCount];
		for (int i = 0; i < purchaseCount; i++) {
			quantities[i] = faker.randomCallingCreditQuantity();
		}

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		assertFreeCallingCredits(0, "Free", 0.1);
		verifyTwilioCreditsOnIntercomResponse(0.1);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];
		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		
		if (plan.equalsIgnoreCase("Enterprise")) {
			verifyTwilioCreditsOnIntercomResponse(0.1);
		} else {
			verifyTwilioCreditsOnIntercomResponse(0.0);
		}

		assertNoAlbatrossCallingCreditTransactions();
		assertStripeCallingCreditChargesCount(customerId, 0);

		int totalPurchasedCredits = 0;
		int totalStripeCharges = 0;
		for (int i = 0; i < purchaseCount; i++) {
			int quantity = quantities[i];
			purchaseCallingCreditsAndAssert(callingCreditPrice, quantity);
			totalPurchasedCredits += callingCreditPrice * quantity;
			totalStripeCharges++;
			assertPaidPlanCallingCreditsState(plan, totalPurchasedCredits);
			assertStripeCallingCreditChargesCount(customerId, totalStripeCharges);
			assertAlbatrossCallingCreditTransactions(totalStripeCharges, callingCreditPrice, Arrays.copyOf(quantities, totalStripeCharges));
		}

		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "month", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, false);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "month");
		assertPaidPlanCallingCreditsState(plan, totalPurchasedCredits);
		assertStripeCallingCreditChargesCount(customerId, totalStripeCharges);
		assertAlbatrossCallingCreditTransactions(totalStripeCharges, callingCreditPrice, quantities);
	}

	@Test
	@Parameters("plan")
	public void stripeRenewalForPurchaseMultipleSetOfCallingCreditsWithAnnualPaidPlan_Test(String plan) {
		final int callingCreditPrice = 50;
		int purchaseCount = faker.randomCallingCreditPurchaseCount();
		int[] quantities = new int[purchaseCount];
		for (int i = 0; i < purchaseCount; i++) {
			quantities[i] = faker.randomCallingCreditQuantity();
		}

		String[] cc = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = cc[0], customerId = cc[1];
		String planLabel = resolveAccountPlanLabel(plan);

		assertFreeCallingCredits(0, "Free", 0.1);
		verifyTwilioCreditsOnIntercomResponse(0.1);

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, "year", resolveStripePlanId(plan), planLabel, 1);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];
		assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);
		
		if (plan.equalsIgnoreCase("Enterprise")) {
			verifyTwilioCreditsOnIntercomResponse(0.1);
		} else {
			verifyTwilioCreditsOnIntercomResponse(0.0);
		}

		assertNoAlbatrossCallingCreditTransactions();
		assertStripeCallingCreditChargesCount(customerId, 0);

		int totalPurchasedCredits = 0;
		int totalStripeCharges = 0;
		for (int i = 0; i < purchaseCount; i++) {
			int quantity = quantities[i];
			purchaseCallingCreditsAndAssert(callingCreditPrice, quantity);
			totalPurchasedCredits += callingCreditPrice * quantity;
			totalStripeCharges++;
			assertPaidPlanCallingCreditsState(plan, totalPurchasedCredits);
			assertStripeCallingCreditChargesCount(customerId, totalStripeCharges);
			assertAlbatrossCallingCreditTransactions(totalStripeCharges, callingCreditPrice, Arrays.copyOf(quantities, totalStripeCharges));
		}

		assertStripeSubscriptionAndInvoiceCount(testClockId, customerId, 1, 1);

		long firstRenewalEpoch = faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2, 1);

		assertBusinessAccountDetailsForRequiredPlan(planLabel, "year", 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, true);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, "year");
		assertPaidPlanCallingCreditsState(plan, totalPurchasedCredits);
		assertStripeCallingCreditChargesCount(customerId, totalStripeCharges);
		assertAlbatrossCallingCreditTransactions(totalStripeCharges, callingCreditPrice, quantities);
	}

}