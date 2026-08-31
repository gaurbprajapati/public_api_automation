package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;

import org.testng.Assert;
import org.testng.annotations.*;
import io.restassured.response.Response;

@AccountType("Free|AlbatrossTkn")
public class StripeRenewalForMainSubscriptionsWithAddonRecords_Test extends StripeRenewalAutomationBase_Test {

	public StripeRenewalForMainSubscriptionsWithAddonRecords_Test() {
		super();
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProOneSeatWithRecordAddOnInterval_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		int recordAddOn = faker.randomRecordAddonCount();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", 1, recordAddOn);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
		int addOnRecordsCount = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsCount, recordAddOn, "Add-on records count is not matched on intercom");

		long renewalTargetFrozenEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, recordAddOn);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsBeforeRenewal, stripeSubscriptionsAfterRenewal);
		int addOnRecordsCountAfterRenewal = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsCountAfterRenewal, recordAddOn, "Add-on records count is not matched on intercom after renewal");
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessOneSeatWithRecordAddOnInterval_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		int recordAddOn = faker.randomRecordAddonCount();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", 1, recordAddOn);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
		int addOnRecordsCount = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsCount, recordAddOn, "Add-on records count is not matched on intercom");

		long renewalTargetFrozenEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, recordAddOn);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsBeforeRenewal, stripeSubscriptionsAfterRenewal);
		int addOnRecordsCountAfterRenewal = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsCountAfterRenewal, recordAddOn, "Add-on records count is not matched on intercom after renewal");
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForEnterpriseOneSeatWithRecordAddOnInterval_Test(String billingInterval) {
		boolean annualBilling = billingInterval.equals("year");
		int recordAddOn = faker.randomRecordAddonCount();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Enterprise", "Enterprise", 1, recordAddOn);
		Response stripeInvoicesBeforeRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
		int addOnRecordsCount = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsCount, recordAddOn, "Add-on records count is not matched on intercom");

		long renewalTargetFrozenEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, recordAddOn);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeRenewal, stripeInvoicesAfterRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsBeforeRenewal, stripeSubscriptionsAfterRenewal);
		int addOnRecordsCountAfterRenewal = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsCountAfterRenewal, recordAddOn, "Add-on records count is not matched on intercom after renewal");
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProOneSeatWithMultipleRecordAddonsRetainedAcrossTwoRenewals_Test(String billingInterval) {
		final int recordAddOn = faker.randomRecordAddonCount();
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", 1, recordAddOn);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		Response stripeSubscriptionsBeforeFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeFirstRenewal, recordAddOn);
		int addOnRecordsCount = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsCount, recordAddOn, "Add-on records count is not matched on intercom");

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		Response stripeSubscriptionsAfterFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterFirstRenewal, recordAddOn);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsBeforeFirstRenewal, stripeSubscriptionsAfterFirstRenewal);
		int addOnRecordsAfterFirst = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsAfterFirst, recordAddOn, "Add-on records count is not matched on intercom after first renewal");

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal, "stripe", billingInterval, false);

		Response stripeSubscriptionsAfterSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondRenewal, recordAddOn);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsAfterFirstRenewal, stripeSubscriptionsAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after second renewal");
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal, "intercom", billingInterval, false);

		int addOnRecordsAfterSecond = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsAfterSecond, recordAddOn, "Add-on records count is not matched on intercom after second renewal");
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessOneSeatWithMultipleRecordAddonsRetainedAcrossTwoRenewals_Test(String billingInterval) {
		final int recordAddOn = faker.randomRecordAddonCount();
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", 1, recordAddOn);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		Response stripeSubscriptionsBeforeFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeFirstRenewal, recordAddOn);
		int addOnRecordsCount = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsCount, recordAddOn, "Add-on records count is not matched on intercom");

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		Response stripeSubscriptionsAfterFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterFirstRenewal, recordAddOn);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsBeforeFirstRenewal, stripeSubscriptionsAfterFirstRenewal);
		int addOnRecordsAfterFirst = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsAfterFirst, recordAddOn, "Add-on records count is not matched on intercom after first renewal");

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal, "stripe", billingInterval, false);

		Response stripeSubscriptionsAfterSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondRenewal, recordAddOn);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsAfterFirstRenewal, stripeSubscriptionsAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after second renewal");
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal, "intercom", billingInterval, false);

		int addOnRecordsAfterSecond = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsAfterSecond, recordAddOn, "Add-on records count is not matched on intercom after second renewal");
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForEnterpriseOneSeatWithMultipleRecordAddonsRetainedAcrossTwoRenewals_Test(String billingInterval) {
		final int recordAddOn = faker.randomRecordAddonCount();
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Enterprise", "Enterprise", 1, recordAddOn);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		Response stripeSubscriptionsBeforeFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeFirstRenewal, recordAddOn);
		int addOnRecordsCount = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsCount, recordAddOn, "Add-on records count is not matched on intercom");

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		Response stripeSubscriptionsAfterFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterFirstRenewal, recordAddOn);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsBeforeFirstRenewal, stripeSubscriptionsAfterFirstRenewal);
		int addOnRecordsAfterFirst = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsAfterFirst, recordAddOn, "Add-on records count is not matched on intercom after first renewal");

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal, "stripe", billingInterval, false);

		Response stripeSubscriptionsAfterSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondRenewal, recordAddOn);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsAfterFirstRenewal, stripeSubscriptionsAfterSecondRenewal);

		Response albatrossInvoicesAfterSecondRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after second renewal");
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(albatrossInvoicesBeforeSecondRenewal, albatrossInvoicesAfterSecondRenewal, "intercom", billingInterval, false);

		int addOnRecordsAfterSecond = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(addOnRecordsAfterSecond, recordAddOn, "Add-on records count is not matched on intercom after second renewal");
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProOneSeatAddAndRemoveRecordAddonsAcrossThreeRenewals_Test(String billingInterval) {
		final int addonAfterPurchase = faker.randomSeatCountWithInList(4,5,6);
		final int addonAfterIncrease = faker.randomSeatCountWithInList(7,8,9);
		final int addonAfterReduce = faker.randomSeatCountWithInList(1,2,3);
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", 1, addonAfterPurchase);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		Response stripeSubscriptionsBeforeFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeFirstRenewal, addonAfterPurchase);
		
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterPurchase, 0, "before first renewal");

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		Response stripeSubscriptionsAfterFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterFirstRenewal, addonAfterPurchase);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsBeforeFirstRenewal, stripeSubscriptionsAfterFirstRenewal);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterPurchase, 0, "after first renewal");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Team", 1, addonAfterIncrease, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		Response stripeSubscriptionsAfterBump = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterBump, addonAfterIncrease);
		function.updateAccountAddonsForStripeAfterRenewal(accountId);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterIncrease, 0, "before second renewal");

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		Response stripeSubscriptionsAfterSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondRenewal, addonAfterIncrease);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsAfterBump, stripeSubscriptionsAfterSecondRenewal);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterIncrease, 0, "after second renewal");

		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after second renewal");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Team", 1, addonAfterReduce, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		function.updateAccountAddonsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		Response stripeSubscriptionsAfterReduce = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterReduce, addonAfterReduce);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterIncrease, addonAfterReduce, "before third renewal");

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeThirdRenewal = albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, 1);
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, "stripe", billingInterval, true);
		Response stripeSubscriptionsAfterThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterThirdRenewal, addonAfterReduce);
		assertStripeNewestInvoiceRecordAddonLineAmountChangedAcrossRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsAfterReduce, stripeSubscriptionsAfterThirdRenewal);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterReduce, 0, "third renewal");

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeThirdRenewal + 1, "Albatross invoice count should increase after third renewal");
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, "intercom", billingInterval, true);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessSeatAddAndRemoveRecordAddonsAcrossThreeRenewals_Test(String billingInterval) {
		final int addonAfterPurchase = faker.randomSeatCountWithInList(4,5,6);
		final int addonAfterIncrease = faker.randomSeatCountWithInList(7,8,9);
		final int addonAfterReduce = faker.randomSeatCountWithInList(1,2,3);
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", 1, addonAfterPurchase);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		Response stripeSubscriptionsBeforeFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeFirstRenewal, addonAfterPurchase);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterPurchase, 0, "before first renewal");

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		Response stripeSubscriptionsAfterFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterFirstRenewal, addonAfterPurchase);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsBeforeFirstRenewal, stripeSubscriptionsAfterFirstRenewal);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterPurchase, 0, "after first renewal");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Business", 1, addonAfterIncrease, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		Response stripeSubscriptionsAfterBump = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterBump, addonAfterIncrease);
		function.updateAccountAddonsForStripeAfterRenewal(accountId);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterIncrease, 0, "before second renewal");

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		Response stripeSubscriptionsAfterSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondRenewal, addonAfterIncrease);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsAfterBump, stripeSubscriptionsAfterSecondRenewal);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterIncrease, 0, "after second renewal");

		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after second renewal");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Business", 1, addonAfterReduce, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		function.updateAccountAddonsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		Response stripeSubscriptionsAfterReduce = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterReduce, addonAfterReduce);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterIncrease, addonAfterReduce, "before third renewal");

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeThirdRenewal = albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, 1);
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, "stripe", billingInterval, true);
		Response stripeSubscriptionsAfterThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterThirdRenewal, addonAfterReduce);
		assertStripeNewestInvoiceRecordAddonLineAmountChangedAcrossRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsAfterReduce, stripeSubscriptionsAfterThirdRenewal);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterReduce, 0, "third renewal");

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeThirdRenewal + 1, "Albatross invoice count should increase after third renewal");
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, "intercom", billingInterval, true);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForEnterpriseOneSeatAddAndRemoveRecordAddonsAcrossThreeRenewals_Test(String billingInterval) {
		final int addonAfterPurchase = faker.randomSeatCountWithInList(4,5,6);
		final int addonAfterIncrease = faker.randomSeatCountWithInList(7,8,9);
		final int addonAfterReduce = faker.randomSeatCountWithInList(1,2,3);
		boolean annualBilling = billingInterval.equals("year");
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		Response[] paidBaseline = upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Enterprise", "Enterprise", 1, addonAfterPurchase);
		Response stripeInvoicesBeforeFirstRenewal = paidBaseline[0];
		Response albatrossInvoicesPaid = paidBaseline[1];

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		Response stripeSubscriptionsBeforeFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeFirstRenewal, addonAfterPurchase);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterPurchase, 0, "before first renewal");

		long firstRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterOneYear() : faker.getFrozenEpochTimeAfterOneMonth();
		Response stripeInvoicesAfterFirstRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, firstRenewalEpoch, accountId, 2);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal, annualBilling);
		assertAlbatrossRenewalInvoicesAgainstPaid(albatrossInvoicesPaid, billingInterval);

		Response stripeSubscriptionsAfterFirstRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterFirstRenewal, addonAfterPurchase);
		assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(stripeInvoicesBeforeFirstRenewal, stripeInvoicesAfterFirstRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsBeforeFirstRenewal, stripeSubscriptionsAfterFirstRenewal);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterPurchase, 0, "after first renewal");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Enterprise", 1, addonAfterIncrease, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		Response stripeSubscriptionsAfterBump = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterBump, addonAfterIncrease);
		function.updateAccountAddonsForStripeAfterRenewal(accountId);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterIncrease, 0, "before second renewal");

		Response stripeInvoicesBeforeSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeSecondRenewal = stripeInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeSecondRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeSecondRenewal = albatrossInvoicesBeforeSecondRenewal.jsonPath().getList("data").size();

		long secondRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(2) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(2);
		Response stripeInvoicesAfterSecondRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, secondRenewalEpoch, accountId, stripeInvoiceCountBeforeSecondRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		Response stripeSubscriptionsAfterSecondRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterSecondRenewal, addonAfterIncrease);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeSecondRenewal, stripeInvoicesAfterSecondRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsAfterBump, stripeSubscriptionsAfterSecondRenewal);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterIncrease, 0, "after second renewal");

		fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeSecondRenewal + 1, "Albatross invoice count should increase after second renewal");

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Enterprise", 1, addonAfterReduce, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		function.updateAccountAddonsForStripeAfterRenewal(accountId);
		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		Response stripeSubscriptionsAfterReduce = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterReduce, addonAfterReduce);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterIncrease, addonAfterReduce, "before third renewal");

		Response stripeInvoicesBeforeThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeThirdRenewal = stripeInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeThirdRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeThirdRenewal = albatrossInvoicesBeforeThirdRenewal.jsonPath().getList("data").size();

		long thirdRenewalEpoch = annualBilling ? faker.getFrozenEpochTimeAfterAnnualRenewalSteps(3) : faker.getFrozenEpochTimeAfterMonthlyRenewalSteps(3);
		Response stripeInvoicesAfterThirdRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, thirdRenewalEpoch, accountId, stripeInvoiceCountBeforeThirdRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, 1);
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal, "stripe", billingInterval, true);
		Response stripeSubscriptionsAfterThirdRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterThirdRenewal, addonAfterReduce);
		assertStripeNewestInvoiceRecordAddonLineAmountChangedAcrossRenewal(stripeInvoicesBeforeThirdRenewal, stripeInvoicesAfterThirdRenewal);
		assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(stripeSubscriptionsAfterReduce, stripeSubscriptionsAfterThirdRenewal);
		verifyAddOnsAndAdditionalAddOnsInIntercomResponse(function.getAccountDetail(albatrossURL, albatrossAuthToken), addonAfterReduce, 0, "third renewal");

		Response albatrossInvoicesAfterThirdRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeThirdRenewal + 1, "Albatross invoice count should increase after third renewal");
		verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(albatrossInvoicesBeforeThirdRenewal, albatrossInvoicesAfterThirdRenewal, "intercom", billingInterval, true);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForProAddAndRemoveRecordAddOn_Test(String billingInterval) {
		int initialSeats = faker.randomSeatCountWithLesserValue();
		int updatedSeats = faker.randomSeatCountWithGreaterValue();
		int recordAddOn = faker.randomRecordAddonCount();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Team", "Team", initialSeats, recordAddOn);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, initialSeats);
		Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
		Response intercomBeforeUpdate = function.getAccountDetail(albatrossURL, albatrossAuthToken);
		assertAddonCountsOnIntercomResponse(intercomBeforeUpdate, recordAddOn, 0);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Team", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		function.updateAccountAddonsForStripeAfterRenewal(accountId);
		Response intercomAfterUpdate = function.getAccountDetail(albatrossURL, albatrossAuthToken);
		assertAddonCountsOnIntercomResponse(intercomAfterUpdate, recordAddOn, 0);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);

		Response stripeInvoicesBeforeChangedPlanRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeRenewal = stripeInvoicesBeforeChangedPlanRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeRenewal = albatrossInvoicesBeforeRenewal.jsonPath().getList("data").size();

		long renewalEpoch = billingInterval.equals("month") ? faker.getFrozenEpochTimeAfterOneMonth() : faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Team", billingInterval, updatedSeats);
		Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(stripeSubscriptionsAfterRenewal, billingInterval, updatedSeats);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, 0);
		Response intercomAfterRenewal = function.getAccountDetail(albatrossURL, albatrossAuthToken);
		assertAddonCountsOnIntercomResponse(intercomAfterRenewal, 0, 0);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeChangedPlanRenewal, stripeInvoicesAfterRenewal);
		Response albatrossInvoicesAfterRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeRenewal + 1, "Albatross invoice count should increase after renewal following seat and add-on change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeRenewal, albatrossInvoicesAfterRenewal);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForBusinessAddAndRemoveRecordAddOn_Test(String billingInterval) {
		int initialSeats = faker.randomSeatCountWithLesserValue();
		int updatedSeats = faker.randomSeatCountWithGreaterValue();
		int recordAddOn = faker.randomRecordAddonCount();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Business", "Business", initialSeats, recordAddOn);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, initialSeats);
		Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
		Response intercomBeforeUpdate = function.getAccountDetail(albatrossURL, albatrossAuthToken);
		assertAddonCountsOnIntercomResponse(intercomBeforeUpdate, recordAddOn, 0);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Business", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		function.updateAccountAddonsForStripeAfterRenewal(accountId);
		Response intercomAfterUpdate = function.getAccountDetail(albatrossURL, albatrossAuthToken);
		assertAddonCountsOnIntercomResponse(intercomAfterUpdate, recordAddOn, 0);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, updatedSeats);

		Response stripeInvoicesBeforeChangedPlanRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeRenewal = stripeInvoicesBeforeChangedPlanRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeRenewal = albatrossInvoicesBeforeRenewal.jsonPath().getList("data").size();

		long renewalEpoch = billingInterval.equals("month") ? faker.getFrozenEpochTimeAfterOneMonth() : faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Business", billingInterval, updatedSeats);
		Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(stripeSubscriptionsAfterRenewal, billingInterval, updatedSeats);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, 0);
		Response intercomAfterRenewal = function.getAccountDetail(albatrossURL, albatrossAuthToken);
		assertAddonCountsOnIntercomResponse(intercomAfterRenewal, 0, 0);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeChangedPlanRenewal, stripeInvoicesAfterRenewal);
		Response albatrossInvoicesAfterRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeRenewal + 1, "Albatross invoice count should increase after renewal following seat and add-on change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeRenewal, albatrossInvoicesAfterRenewal);
	}

	@Test
	@Parameters("billingInterval")
	public void stripeRenewalForEnterpriseAddAndRemoveRecordAddOn_Test(String billingInterval) {
		int initialSeats = faker.randomSeatCountWithLesserValue();
		int updatedSeats = faker.randomSeatCountWithGreaterValue();
		int recordAddOn = faker.randomRecordAddonCount();
		String[] clockAndCustomer = setupFreeAccountWithTestClockAndBaseline();
		String testClockId = clockAndCustomer[0];
		String customerId = clockAndCustomer[1];

		upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, "PLN_Enterprise", "Enterprise", initialSeats, recordAddOn);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, initialSeats);
		Response stripeSubscriptionsBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsBeforeRenewal, recordAddOn);
		Response intercomBeforeUpdate = function.getAccountDetail(albatrossURL, albatrossAuthToken);
		assertAddonCountsOnIntercomResponse(intercomBeforeUpdate, recordAddOn, 0);

		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, "PLN_Enterprise", updatedSeats, 0, 0);
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		function.updateAccountAddonsForStripeAfterRenewal(accountId);
		Response intercomAfterUpdate = function.getAccountDetail(albatrossURL, albatrossAuthToken);
		assertAddonCountsOnIntercomResponse(intercomAfterUpdate, recordAddOn, 0);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, updatedSeats);

		Response stripeInvoicesBeforeChangedPlanRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		int stripeInvoiceCountBeforeRenewal = stripeInvoicesBeforeChangedPlanRenewal.jsonPath().getList("data").size();
		Response albatrossInvoicesBeforeRenewal = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		int albatrossInvoiceCountBeforeRenewal = albatrossInvoicesBeforeRenewal.jsonPath().getList("data").size();

		long renewalEpoch = billingInterval.equals("month") ? faker.getFrozenEpochTimeAfterOneMonth() : faker.getFrozenEpochTimeAfterOneYear();
		Response stripeInvoicesAfterRenewal = performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalEpoch, accountId, stripeInvoiceCountBeforeRenewal + 1);

		assertBusinessAccountDetailsForRequiredPlan("Enterprise", billingInterval, updatedSeats);
		Response stripeSubscriptionsAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(stripeSubscriptionsAfterRenewal, billingInterval, updatedSeats);
		assertStripeSubscriptionRecordAddonLineItem(stripeSubscriptionsAfterRenewal, 0);
		Response intercomAfterRenewal = function.getAccountDetail(albatrossURL, albatrossAuthToken);
		assertAddonCountsOnIntercomResponse(intercomAfterRenewal, 0, 0);
		assertStripeNewestInvoiceAmountDiffersAcrossRenewal(stripeInvoicesBeforeChangedPlanRenewal, stripeInvoicesAfterRenewal);
		Response albatrossInvoicesAfterRenewal = fetchAlbatrossInvoicesAssertCount(albatrossInvoiceCountBeforeRenewal + 1, "Albatross invoice count should increase after renewal following seat and add-on change");
		assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(albatrossInvoicesBeforeRenewal, albatrossInvoicesAfterRenewal);
	}


}