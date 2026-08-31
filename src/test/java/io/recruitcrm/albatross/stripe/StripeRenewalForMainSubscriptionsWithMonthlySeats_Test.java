package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;
import org.testng.Assert;
import org.testng.annotations.*;
import io.restassured.response.Response;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptionsWithMonthlySeats_Test extends StripeRenewalAutomationBase_Test {

	public StripeRenewalForMainSubscriptionsWithMonthlySeats_Test() {
		super();
	}

	@Test
	public void stripeRenewalForProAnnualAddAndRemoveMonthlySeats_Test() {
		int initialSeats = faker.randomSeatCountWithLesserValue();
		int updatedSeats = faker.randomSeatCountWithGreaterValue();
		int monthlySeats = faker.randomMonthlySeatsCount();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", initialSeats, 0, monthlySeats);

		Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsBeforeRenewal, monthlySeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, 0);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", initialSeats);
		fetchAlbatrossInvoicesAssertCount(2, "Invoices not found for paid account after buying monthly seats");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);

		Response stripeInvoicesBeforeChangedPlanRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeRenewal = stripeInvoicesBeforeChangedPlanRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeRenewal = albatrossInvoicesBeforeRenewal.jsonPath().getList("data").size();
		Response stripeSubscriptionBeforeChangePlanRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionBeforeChangePlanRenewal, 0, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionBeforeChangePlanRenewal, 0);

		long renewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 0);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(stripeSubscriptionsAfterRenewal, "year", updatedSeats);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, 0);
		Response albatrossInvoicesAfterRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeRenewal, "Albatross invoice count should not increase after renewing annual plan without monthly seats");
		Assert.assertEquals(albatrossFirstInvoiceAmountDue(albatrossInvoicesBeforeRenewal), albatrossFirstInvoiceAmountDue(albatrossInvoicesAfterRenewal), "Albatross invoice amount should not change after renewing annual plan without monthly seats");
		Assert.assertEquals(stripeFirstInvoiceAmountDue(stripeInvoicesBeforeChangedPlanRenewal), stripeFirstInvoiceAmountDue(stripeInvoicesAfterRenewal), "Stripe invoice amount should not change across renewal after renewing annual plan without monthly seats");
	}

	@Test
	public void stripeRenewalForBusinessAnnualAddAndRemoveMonthlySeats_Test() {
		int initialSeats = faker.randomSeatCountWithLesserValue();
		int updatedSeats = faker.randomSeatCountWithGreaterValue();
		int monthlySeats = faker.randomMonthlySeatsCount();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", initialSeats, 0, monthlySeats);

		Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsBeforeRenewal, monthlySeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, 0);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", initialSeats);
		fetchAlbatrossInvoicesAssertCount(2, "Invoices not found for paid account after buying monthly seats");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);

		Response stripeInvoicesBeforeChangedPlanRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeRenewal = stripeInvoicesBeforeChangedPlanRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeRenewal = albatrossInvoicesBeforeRenewal.jsonPath().getList("data").size();
		Response stripeSubscriptionBeforeChangePlanRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionBeforeChangePlanRenewal, 0, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionBeforeChangePlanRenewal, 0);

		long renewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 0);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);
		Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(stripeSubscriptionsAfterRenewal, "year", updatedSeats);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, 0);
		Response albatrossInvoicesAfterRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeRenewal, "Albatross invoice count should not increase after renewing annual plan without monthly seats");
		Assert.assertEquals(albatrossFirstInvoiceAmountDue(albatrossInvoicesBeforeRenewal), albatrossFirstInvoiceAmountDue(albatrossInvoicesAfterRenewal), "Albatross invoice amount should not change after renewing annual plan without monthly seats");
		Assert.assertEquals(stripeFirstInvoiceAmountDue(stripeInvoicesBeforeChangedPlanRenewal), stripeFirstInvoiceAmountDue(stripeInvoicesAfterRenewal), "Stripe invoice amount should not change across renewal after renewing annual plan without monthly seats");
	}

	@Test
	public void stripeRenewalForEnterpriseAnnualAddAndRemoveMonthlySeats_Test() {
		int initialSeats = faker.randomSeatCountWithLesserValue();
		int updatedSeats = faker.randomSeatCountWithGreaterValue();
		int monthlySeats = faker.randomMonthlySeatsCount();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Enterprise", initialSeats, 0, monthlySeats);

		Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsBeforeRenewal, monthlySeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, 0);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", initialSeats);
		fetchAlbatrossInvoicesAssertCount(2, "Invoices not found for paid account after buying monthly seats");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Enterprise", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);

		Response stripeInvoicesBeforeChangedPlanRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeRenewal = stripeInvoicesBeforeChangedPlanRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeRenewal = albatrossInvoicesBeforeRenewal.jsonPath().getList("data").size();
		Response stripeSubscriptionBeforeChangePlanRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionBeforeChangePlanRenewal, 0, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionBeforeChangePlanRenewal, 0);

		long renewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 0);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(stripeSubscriptionsAfterRenewal, "year", updatedSeats);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, 0);
		Response albatrossInvoicesAfterRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeRenewal, "Albatross invoice count should not increase after renewing annual plan without monthly seats");
		Assert.assertEquals(albatrossFirstInvoiceAmountDue(albatrossInvoicesBeforeRenewal), albatrossFirstInvoiceAmountDue(albatrossInvoicesAfterRenewal), "Albatross invoice amount should not change after renewing annual plan without monthly seats");
		Assert.assertEquals(stripeFirstInvoiceAmountDue(stripeInvoicesBeforeChangedPlanRenewal), stripeFirstInvoiceAmountDue(stripeInvoicesAfterRenewal), "Stripe invoice amount should not change across renewal after renewing annual plan without monthly seats");
	}

	@Test(dataProvider = "getRandomAnnualAndMonthlySeatsData")
	public void stripeRenewalForProAnnualWithMonthlySeatsToBusinessAnnualWithMonthlySeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", initialSeats, 0, initialSeats);

		Response stripeSubscriptionsAfterProPurchase = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterProPurchase, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterProPurchase, 0);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", initialSeats);
		fetchAlbatrossInvoicesAssertCount(2, "Invoices not found for paid account after buying monthly seats");

		Response stripeInvoicesBeforeMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeMonthlyRenewal = stripeInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeMonthlyRenewal = albatrossInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();

		long monthlyRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, monthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", initialSeats);
		Response stripeSubscriptionsAfterMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterMonthlyRenewal, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Pro");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", updatedSeats, 0, updatedSeats);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);
		Response stripeSubscriptionsAfterBusinessUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterBusinessUpgrade, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterBusinessUpgrade, 0);

		Response stripeInvoicesBeforeSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondMonthlyRenewal = stripeInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondMonthlyRenewal = albatrossInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();

		long secondMonthlyRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondMonthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);
		Response stripeSubscriptionsAfterSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Business");
	}

	@Test(dataProvider = "getRandomAnnualAndMonthlySeatsData")
	public void stripeRenewalForProAnnualWithMonthlySeatsToEnterpriseAnnualWithMonthlySeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", initialSeats, 0, initialSeats);

		Response stripeSubscriptionsAfterProPurchase = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterProPurchase, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterProPurchase, 0);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", initialSeats);
		fetchAlbatrossInvoicesAssertCount(2, "Invoices not found for paid account after buying monthly seats");

		Response stripeInvoicesBeforeMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeMonthlyRenewal = stripeInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeMonthlyRenewal = albatrossInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();

		long monthlyRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, monthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", initialSeats);
		Response stripeSubscriptionsAfterMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterMonthlyRenewal, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Pro");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Enterprise", updatedSeats, 0, updatedSeats);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		Response stripeSubscriptionsAfterEnterpriseUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterEnterpriseUpgrade, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterEnterpriseUpgrade, 0);

		Response stripeInvoicesBeforeSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondMonthlyRenewal = stripeInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondMonthlyRenewal = albatrossInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();

		long secondMonthlyRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondMonthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		Response stripeSubscriptionsAfterSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Enterprise");
	}

	@Test(dataProvider = "getRandomAnnualAndMonthlySeatsData")
	public void stripeRenewalForBusinessAnnualWithMonthlySeatsToEnterpriseAnnualWithMonthlySeats_Test(int initialSeats, int updatedSeats) {
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", initialSeats, 0, initialSeats);

		Response stripeSubscriptionsAfterBusinessPurchase = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterBusinessPurchase, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterBusinessPurchase, 0);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", initialSeats);
		fetchAlbatrossInvoicesAssertCount(2, "Invoices not found for paid account after buying monthly seats");

		Response stripeInvoicesBeforeMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeMonthlyRenewal = stripeInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeMonthlyRenewal = albatrossInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();

		long monthlyRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, monthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", initialSeats);
		Response stripeSubscriptionsAfterMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterMonthlyRenewal, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Business");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Enterprise", updatedSeats, 0, updatedSeats);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		Response stripeSubscriptionsAfterEnterpriseUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterEnterpriseUpgrade, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterEnterpriseUpgrade, 0);

		Response stripeInvoicesBeforeSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondMonthlyRenewal = stripeInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondMonthlyRenewal = albatrossInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();

		long secondMonthlyRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondMonthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", updatedSeats);
		Response stripeSubscriptionsAfterSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Enterprise");
	}

	@Test
	public void stripeRenewalForBusinessAnnualWithMonthlySeatsToProAnnualWithMonthlySeats_Test() {
		int initialSeats = faker.randomSeatCountWithLesserValue();
		int updatedSeats = initialSeats + 10;

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", initialSeats, 0, initialSeats);

		Response stripeSubscriptionsAfterBusinessPurchase = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterBusinessPurchase, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterBusinessPurchase, 0);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", initialSeats);
		fetchAlbatrossInvoicesAssertCount(2, "Invoices not found for paid account after buying monthly seats");

		Response stripeInvoicesBeforeMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeMonthlyRenewal = stripeInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeMonthlyRenewal = albatrossInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();

		long monthlyRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, monthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", initialSeats);
		Response stripeSubscriptionsAfterMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterMonthlyRenewal, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Business");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", updatedSeats, 0, updatedSeats);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		Response stripeSubscriptionsAfterProPlanUpdate = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterProPlanUpdate, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterProPlanUpdate, 0);

		Response stripeInvoicesBeforeSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondMonthlyRenewal = stripeInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondMonthlyRenewal = albatrossInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();

		long secondMonthlyRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondMonthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		Response stripeSubscriptionsAfterSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Pro");
	}

	@Test
	public void stripeRenewalForEnterpriseAnnualWithMonthlySeatsToProAnnualWithMonthlySeats_Test() {
		int initialSeats = faker.randomSeatCountWithLesserValue();
		int updatedSeats = initialSeats + 15;

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Enterprise", initialSeats, 0, initialSeats);

		Response stripeSubscriptionsAfterEnterprisePurchase = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterEnterprisePurchase, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterEnterprisePurchase, 0);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", initialSeats);
		fetchAlbatrossInvoicesAssertCount(2, "Invoices not found for paid account after buying monthly seats");

		Response stripeInvoicesBeforeMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeMonthlyRenewal = stripeInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeMonthlyRenewal = albatrossInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();

		long monthlyRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, monthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", initialSeats);
		Response stripeSubscriptionsAfterMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterMonthlyRenewal, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Enterprise");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Team", updatedSeats, 0, updatedSeats);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		Response stripeSubscriptionsAfterProPlanUpdate = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterProPlanUpdate, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterProPlanUpdate, 0);

		Response stripeInvoicesBeforeSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondMonthlyRenewal = stripeInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondMonthlyRenewal = albatrossInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();

		long secondMonthlyRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondMonthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", "year", updatedSeats);
		Response stripeSubscriptionsAfterSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Pro");
	}

	@Test
	public void stripeRenewalForEnterpriseAnnualWithMonthlySeatsToBusinessAnnualWithMonthlySeats_Test() {
		int initialSeats = faker.randomSeatCountWithLesserValue();
		int updatedSeats = initialSeats + 10;

		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Enterprise", initialSeats, 0, initialSeats);

		Response stripeSubscriptionsAfterEnterprisePurchase = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterEnterprisePurchase, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterEnterprisePurchase, 0);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", initialSeats);
		fetchAlbatrossInvoicesAssertCount(2, "Invoices not found for paid account after buying monthly seats");

		Response stripeInvoicesBeforeMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeMonthlyRenewal = stripeInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeMonthlyRenewal = albatrossInvoicesBeforeMonthlyRenewal.jsonPath().getList("data").size();

		long monthlyRenewalEpoch = faker.getFrozenEpochTimeAfterOneMonth();
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, monthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", "year", initialSeats);
		Response stripeSubscriptionsAfterMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterMonthlyRenewal, initialSeats, initialSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Enterprise");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, "year", "PLN_Business", updatedSeats, 0, updatedSeats);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);
		Response stripeSubscriptionsAfterBusinessUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterBusinessUpgrade, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterBusinessUpgrade, 0);

		Response stripeInvoicesBeforeSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondMonthlyRenewal = stripeInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondMonthlyRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondMonthlyRenewal = albatrossInvoicesBeforeSecondMonthlyRenewal.jsonPath().getList("data").size();

		long secondMonthlyRenewalEpoch = faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondMonthlyRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondMonthlyRenewal + 1, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", "year", updatedSeats);
		Response stripeSubscriptionsAfterSecondMonthlyRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionMonthlySeatsLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, updatedSeats, updatedSeats, 2);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondMonthlyRenewal, 0);
		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondMonthlyRenewal + 1, "Albatross invoice count should increase after monthly renewal with monthly seats on annual Business");
	}

}