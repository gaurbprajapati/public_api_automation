package io.recruitcrm.albatross.stripe;

import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.testbase.TestBase;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.Assert;
import org.testng.annotations.*;
import io.restassured.path.json.JsonPath;

import io.rcrm.api.commanfunctions.StripeCommonFunctions;
import io.rcrm.api.javafaker.albatross.stripe.JavaFakerStripe;

import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.*;

@AccountType("Free|AlbatrossTkn")
public abstract class StripeRenewalAutomationBase_Test extends TestBase {

	protected String apiAuthToken;
	protected String albatrossAuthToken;
	protected String invalidAuthToken;
	protected int accountId;
	protected String emailId;
	protected StripeCommonFunctions function;
	protected JavaFakerStripe faker;

	protected static final int VONQ_BUCKET7_MONTHLY = 218;
	protected static final int VONQ_BUCKET7_ANNUALLY = 219;
	protected static final int DE_250_BUCKET_MONTHLY = 1;
	protected static final int DE_3000_BUCKET_ANNUALLY = 2;
	protected static final int WORKATO_BUCKET1K_MONTHLY = 1;
	protected static final int WORKATO_BUCKET1K_ANNUALLY = 2;
	protected static final String WORKATO_TASK_1K = "1000";

	public StripeRenewalAutomationBase_Test() {
		super();
	}

	@BeforeClass
	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		invalidAuthToken = ThreadManager.getOwnerAlbatrossToken() + "123";
		accountId = ThreadManager.getAccount().getAccountId();
		emailId = ThreadManager.getOwner().getEmail();
		function = new StripeCommonFunctions();
		faker = new JavaFakerStripe();
	}

	protected String[] setupFreeAccountWithTestClockAndBaseline() {
		String[] clockAndCustomer = setupTestClockCustomerAndPaymentMethod();
		assertBusinessAccountDetailsForRequiredPlan("Free", null, 50);
		fetchAlbatrossInvoicesAssertCount(0, "Invoices found for free account");
		return clockAndCustomer;
	}

	protected Response[] upgradeToPaidPlanAndAssertPaidBaseline(String customerId, String billingInterval, String stripePlanId, String accountPlanLabel, int seats) {
		return upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, stripePlanId, accountPlanLabel, seats, 0);
	}

	protected Response[] upgradeToPaidPlanAndAssertPaidBaseline(String customerId, String billingInterval, String stripePlanId, String accountPlanLabel, int seats, int recordAddOn) {
		return upgradeToPaidPlanAndAssertPaidBaseline(customerId, billingInterval, stripePlanId, accountPlanLabel, seats, recordAddOn, 0);
	}

	protected Response[] upgradeToPaidPlanAndAssertPaidBaseline(String customerId, String billingInterval, String stripePlanId, String accountPlanLabel, int seats, int recordAddOn, int monthlySeats) {
		function.updatePlanForAccount(albatrossURL, albatrossAuthToken, billingInterval, stripePlanId, seats, recordAddOn, monthlySeats);
		Response subscriptionAfterUpgrade = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionAfterPlanUpgrade(subscriptionAfterUpgrade, billingInterval, seats);
		assertStripeSubscriptionRecordAddonLineItem(subscriptionAfterUpgrade, recordAddOn);
		Response stripeInvoicesBeforeRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		Assert.assertEquals(stripeInvoicesBeforeRenewal.jsonPath().getList("data").size(), 1, "Invoice count was not updated even after the plan upgrade");
		assertBusinessAccountDetailsForRequiredPlan(accountPlanLabel, billingInterval, seats);
		Response albatrossInvoicesPaid = fetchAlbatrossInvoicesAssertCount(1, "Invoices not found for paid account after plan upgrade");
		return new Response[] { stripeInvoicesBeforeRenewal, albatrossInvoicesPaid };
	}

	protected Response performRenewalAndAssertStripeAfterRenewal(String testClockId, String customerId, long renewalTargetFrozenEpoch, int accountId, int expectedStripeInvoiceCountAfterRenewal) {
		return performRenewalAndAssertStripeAfterRenewal(testClockId, customerId, renewalTargetFrozenEpoch, accountId, expectedStripeInvoiceCountAfterRenewal, 1);
	}

	protected Response performRenewalAndAssertStripeAfterRenewal(String testClockId, String customerId, long renewalTargetFrozenEpoch, int accountId, int expectedStripeInvoiceCountAfterRenewal, int expectedSubscriptionCount) {
		function.performRenewalAction(testClockId, renewalTargetFrozenEpoch);
		function.assertTestClockStatusReady(testClockId);
		Response subscriptionAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		Assert.assertEquals(subscriptionAfterRenewal.jsonPath().getList("data").size(), expectedSubscriptionCount, "Subscription count mismatch after the renewal action");
		Response stripeInvoicesAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		Assert.assertEquals(stripeInvoicesAfterRenewal.jsonPath().getList("data").size(), expectedStripeInvoiceCountAfterRenewal, "Stripe invoice count was not updated as expected after the renewal action");
		function.updateAccountSettingsForStripeAfterRenewal(accountId);
		return stripeInvoicesAfterRenewal;
	}

	protected void assertStripeFirstInvoiceUnchangedMetadataAndAdvancedBillingDate(Response stripeInvoicesBeforeRenewal, Response stripeInvoicesAfterRenewal, boolean annualBilling) {
		int amountDueBefore = stripeFirstInvoiceAmountDue(stripeInvoicesBeforeRenewal);
		int amountDueAfter = stripeFirstInvoiceAmountDue(stripeInvoicesAfterRenewal);
		Assert.assertEquals(amountDueAfter, amountDueBefore, "Invoice amount was not updated even after the renewal action");
		String lineDescriptionBefore = stripeFirstInvoiceLineDescription(stripeInvoicesBeforeRenewal);
		String lineDescriptionAfter = stripeFirstInvoiceLineDescription(stripeInvoicesAfterRenewal);
		Assert.assertEquals(lineDescriptionAfter, lineDescriptionBefore, "Invoice description was not updated even after the renewal action");
		LocalDate invoiceDateUtcBefore = stripeFirstInvoiceDateUtc(stripeInvoicesBeforeRenewal);
		LocalDate invoiceDateUtcAfter = stripeFirstInvoiceDateUtc(stripeInvoicesAfterRenewal);
		LocalDate expectedDate = annualBilling ? invoiceDateUtcBefore.plusYears(1) : invoiceDateUtcBefore.plusMonths(1);
		String dateMessage = annualBilling
				? "Invoice date after renewal should be one calendar year after the invoice date before renewal"
				: "Invoice date after renewal should be one calendar month after the invoice date before renewal";
		Assert.assertEquals(invoiceDateUtcAfter, expectedDate, dateMessage);
	}

	protected void assertAlbatrossInvoicesCountAfterRenewal(Response albatrossInvoicesPaid, String interval, int expectedInvoiceCount) {
		Response albatrossInvoicesRenewed = fetchAlbatrossInvoicesAssertCount(expectedInvoiceCount, "Invoices not found for renewed account after renewal action");
		Assert.assertEquals(albatrossFirstInvoiceAmountDue(albatrossInvoicesRenewed), albatrossFirstInvoiceAmountDue(albatrossInvoicesPaid), "Invoice amount was not updated even after the renewal action");
		assertAlbatrossFirstInvoiceCreatedOneMonthAfter(albatrossInvoicesPaid, albatrossInvoicesRenewed, interval);
	}

	protected void assertAlbatrossRenewalInvoicesAgainstPaid(Response albatrossInvoicesPaid, String interval) {
		Response albatrossInvoicesRenewed = fetchAlbatrossInvoicesAssertCount(2, "Invoices not found for renewed account after renewal action");
		Assert.assertEquals(albatrossFirstInvoiceAmountDue(albatrossInvoicesRenewed), albatrossFirstInvoiceAmountDue(albatrossInvoicesPaid), "Invoice amount was not updated even after the renewal action");
		assertAlbatrossFirstInvoiceCreatedOneMonthAfter(albatrossInvoicesPaid, albatrossInvoicesRenewed, interval);
	}

	protected void verifyInvoiceFullyCycleAnnualOrMonthlyRenewal(Response beforeRenewal, Response afterRenewal, String platform, String billingCycle, boolean isReduced) {
		if (platform.equalsIgnoreCase("stripe")) {
			if (billingCycle.equals("year")) {
				assertStripeNewestInvoiceStableFullCycleAnnualRenewal(beforeRenewal, afterRenewal, isReduced);
			} else {
				assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(beforeRenewal, afterRenewal, isReduced);
			}
		} else if (platform.equalsIgnoreCase("intercom")) {
			if (billingCycle.equals("year")) {
				assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(beforeRenewal, afterRenewal, isReduced);
			} else {
				assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(beforeRenewal, afterRenewal, isReduced);
			}
		} else {
			throw new IllegalArgumentException("platform must be stripe or intercom, was: " + platform);
		}
	}

	protected String[] setupTestClockCustomerAndPaymentMethod() {

		long currentEpochSecond = faker.getCurrentEpochSecond();
		Response createTestClockResponse = function.createTestClock("Test Clock for " + emailId, currentEpochSecond);
		String testClockId = createTestClockResponse.jsonPath().getString("id");

		System.out.println("email: " + emailId + " &&& testClockId: " + testClockId + " &&& accountId: " + accountId);

		Response createCustomerResponse = function.createCustomerUsingTestClock(testClockId, emailId);
		String customerId = createCustomerResponse.jsonPath().getString("id");

		function.updateCustomerForAccountStripe(accountId, customerId);

		Response createPaymentMethodResponse = function.createPaymentMethod(faker.getTestCardNumber(), faker.getTestCardExpMonth(), faker.getTestCardExpYear(), faker.getTestCardCvc());
		String paymentMethodId = createPaymentMethodResponse.jsonPath().getString("id");

		function.attachPaymentMethodToCustomer(paymentMethodId, customerId);
		function.assertDefaultPaymentMethodReadyForPlanUpgrade(customerId, paymentMethodId);

		return new String[] { testClockId, customerId };
	}

	protected int stripeFirstInvoiceAmountDue(Response invoicesResponse) {
		return invoicesResponse.jsonPath().getInt("data[0].amount_due");
	}

	protected String stripeFirstInvoiceLineDescription(Response invoicesResponse) {
		return invoicesResponse.jsonPath().getString("data[0].lines.data[0].description");
	}

	protected LocalDate stripeFirstInvoiceDateUtc(Response invoicesResponse) {
		long dateEpoch = invoicesResponse.jsonPath().getLong("data[0].date");
		return faker.getEpochSecondsToLocalDateUtc(dateEpoch);
	}

	protected static int albatrossFirstInvoiceAmountDue(Response albatrossInvoicesResponse) {
		return albatrossInvoicesResponse.jsonPath().getInt("data[0].amountdue");
	}

	protected static long albatrossFirstInvoiceCreatedEpoch(Response albatrossInvoicesResponse) {
		return albatrossInvoicesResponse.jsonPath().getLong("data[0].created");
	}

	protected static int expectedRcrmSeatsImmediatelyAfterSeatChange(int initialSeats, int updatedSeats) {
		if (updatedSeats < initialSeats)
			return initialSeats;
		return updatedSeats;
	}

	protected String resolveStripePlanId(String plan) {
		switch (plan) {
		case "Pro":
		case "Team":
			return "PLN_Team";
		case "Business":
			return "PLN_Business";
		case "Enterprise":
			return "PLN_Enterprise";
		default:
			Assert.fail("Unsupported plan parameter: " + plan);
			return null;
		}
	}

	protected void assertBusinessAccountDetailsForRequiredPlan(String plan, String billingCycle, int seats) {
		final int maxAttempts = 5;
		final String expectedSeats = String.valueOf(seats);
		String actualPlan = null;
		String actualSeats = null;
		String actualBilling = null;

		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			waitBetweenTheEveryScript(5000);
			function.updateAccountSettingsForStripeAfterRenewal(accountId);
			Response detail = function.getAccountDetail(albatrossURL, albatrossAuthToken);
			actualPlan = detail.jsonPath().getString("user.accountplan");
			actualSeats = detail.jsonPath().getString("user.seats");
			actualBilling = detail.jsonPath().getString("user.billingcycle");

			boolean planAndSeatsMatch = Objects.equals(actualPlan, plan) && Objects.equals(actualSeats, expectedSeats);
			if (planAndSeatsMatch) {
				return;
			}
		}
		Assert.assertEquals(actualPlan, plan, "Account plan was not " + plan);
		Assert.assertEquals(actualSeats, expectedSeats, "Account does not have expected seats");
		Assert.assertEquals(actualBilling, billingCycle, "Account does not have expected billing cycle");
	}

	protected Response fetchAlbatrossInvoicesAssertCount(int expectedCount, String message) {
		Response response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(response.jsonPath().getList("data").size(), expectedCount, message);
		return response;
	}

	protected void assertStripeSubscriptionAfterPlanUpgrade(Response subscriptionResponse, String interval, int expectedQuantity) {
		Assert.assertEquals(subscriptionResponse.jsonPath().getList("data").size(), 1, "Subscription count was not updated even after the plan upgrade");
		Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[0].items.data[0].plan.currency"), "usd", "Subscription currency was not updated even after the plan upgrade");
		Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[0].items.data[0].plan.interval"), interval, "Subscription interval was not updated even after the plan upgrade");
		Assert.assertEquals(subscriptionResponse.jsonPath().getInt("data[0].items.data[0].quantity"), expectedQuantity, "Subscription line-item quantity should match expected seats");
	}

	protected void assertStripeSubscriptionMonthlySeatsLineItem(Response subscriptionResponse, int expectedMonthlySeats, int expectedAnnualSeats, int size) {
		Assert.assertEquals(subscriptionResponse.jsonPath().getList("data").size(), size, "Subscription count was not updated even after the plan upgrade");
		Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[0].items.data[0].plan.currency"), "usd", "Subscription currency was not updated even after the plan upgrade");
		Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[0].items.data[0].plan.interval"), "month", "Subscription interval was not updated even after the plan upgrade");
		Assert.assertEquals(subscriptionResponse.jsonPath().getInt("data[0].items.data[0].quantity"), expectedMonthlySeats, "Subscription line-item quantity should match expected seats");
		Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[1].items.data[0].plan.currency"), "usd", "Subscription currency was not updated even after the plan upgrade");
		Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[1].items.data[0].plan.interval"), "year", "Subscription interval was not updated even after the plan upgrade");
		Assert.assertEquals(subscriptionResponse.jsonPath().getInt("data[1].items.data[0].quantity"), expectedAnnualSeats, "Subscription line-item quantity should be 0 when monthly seats are removed");
	}

	/** Seat line item is index 0; record add-on line items use indices 1..n-1 with quantity matching the purchased add-on count. Removing the add-on sets quantity to 0 but does not drop the line item. */
	protected void assertStripeSubscriptionRecordAddonLineItem(Response subscriptionResponse, int expectedRecordAddon) {
		int lineItemCount = subscriptionResponse.jsonPath().getList("data[0].items.data").size();
		if (expectedRecordAddon <= 0) {
			if (lineItemCount >= 2) {
				Assert.assertEquals(subscriptionResponse.jsonPath().getInt("data[0].items.data[1].quantity"), expectedRecordAddon, "Record add-on line item should remain with quantity " + expectedRecordAddon);
			}
			return;
		}
		Assert.assertTrue(lineItemCount >= 2, "Record add-on should add at least one subscription line item beyond seats");
		boolean found = false;
		for (int i = 1; i < lineItemCount; i++) {
			if (subscriptionResponse.jsonPath().getInt("data[0].items.data[" + i + "].quantity") == expectedRecordAddon) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found, "Expected a subscription line item with record add-on quantity " + expectedRecordAddon);
	}

	protected void assertStripeLastSubscriptionRecordAddonLineItem(Response subscriptionResponse, int expectedRecordAddon) {
		List<?> subscriptions = subscriptionResponse.jsonPath().getList("data");
		Assert.assertFalse(subscriptions.isEmpty(), "Subscription list should not be empty");
		int subscriptionIndex = subscriptions.size() - 1;
		String subscriptionPath = "data[" + subscriptionIndex + "]";
		int lineItemCount = subscriptionResponse.jsonPath().getList(subscriptionPath + ".items.data").size();
		if (expectedRecordAddon <= 0) {
			if (lineItemCount >= 2)
				Assert.assertEquals(subscriptionResponse.jsonPath().getInt(subscriptionPath + ".items.data[1].quantity"), expectedRecordAddon, "Record add-on line item on last subscription should remain with quantity " + expectedRecordAddon);
			return;
		}
		Assert.assertTrue(lineItemCount >= 2, "Record add-on on last subscription should add at least one line item beyond seats");
		boolean found = false;
		for (int i = 1; i < lineItemCount; i++) {
			if (subscriptionResponse.jsonPath().getInt(subscriptionPath + ".items.data[" + i + "].quantity") == expectedRecordAddon) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found, "Expected a record add-on line item with quantity " + expectedRecordAddon + " on last subscription (data[" + subscriptionIndex + "])");
	}

	protected void assertStripeNewestInvoiceRecordAddonLineAmountUnchangedAcrossRenewal(Response invoicesBeforeRenewal, Response invoicesAfterRenewal) {
		long amountBefore = invoicesBeforeRenewal.jsonPath().getLong("data[0].lines.data[1].amount");
		long amountAfter = invoicesAfterRenewal.jsonPath().getLong("data[0].lines.data[1].amount");
		Assert.assertEquals(amountAfter, amountBefore, "Newest invoice record add-on line amount (data[0].lines.data[1].amount) should match before and after renewal");
	}

	protected void assertStripeNewestInvoiceRecordAddonLineAmountChangedAcrossRenewal(Response invoicesBeforeRenewal, Response invoicesAfterRenewal) {
		long amountBefore = invoicesBeforeRenewal.jsonPath().getLong("data[0].lines.data[1].amount");
		long amountAfter = invoicesAfterRenewal.jsonPath().getLong("data[0].lines.data[1].amount");
		Assert.assertNotEquals(amountAfter, amountBefore, "Newest invoice record add-on line amount (data[0].lines.data[1].amount) should match before and after renewal");
	}

	protected void assertStripeSubscriptionRecordAddonPlanAmountUnchangedAcrossRenewal(Response subscriptionsBeforeRenewal, Response subscriptionsAfterRenewal) {
		long amountBefore = subscriptionsBeforeRenewal.jsonPath().getLong("data[0].items.data[1].plan.amount");
		long amountAfter = subscriptionsAfterRenewal.jsonPath().getLong("data[0].items.data[1].plan.amount");
		Assert.assertEquals(amountAfter, amountBefore, "Record add-on plan amount (data[0].items.data[1].plan.amount) should match before and after renewal");
	}

	protected void assertAddonCountsOnIntercomResponse(Response response, int addOnRecords, int updatedAddOnRecords) {
		Assert.assertEquals(response.jsonPath().getInt("user.add_on_records"), addOnRecords, "Intercom user.add_on_records did not match expected value");
		Assert.assertEquals(response.jsonPath().getInt("user.updated_add_on_records"), updatedAddOnRecords, "Intercom user.updated_add_on_records did not match expected value");
	}

	protected void verifyAddOnsAndAdditionalAddOnsInIntercomResponse(Response intercomResponse, int expectedAddOnRecords, int expectedUpdatedAddOnRecords, String action) {
		JsonPath jsonPath = intercomResponse.jsonPath();
		Assert.assertEquals(jsonPath.getInt("user.add_on_records"), expectedAddOnRecords, "Add-on records count is not matched on intercom " + action);
		Assert.assertEquals(jsonPath.getInt("user.updated_add_on_records"), expectedUpdatedAddOnRecords, "Updated Add-on records count is not matched on intercom after " + action);
	}

	protected void verifyTwilioCreditsOnIntercomResponse(double expectedTwilioCredits) {
		final int maxAttempts = 3;
		Response response = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			response = function.getAccountDetail(albatrossURL, albatrossAuthToken);
			double actualTwilioCredits = response.jsonPath().getDouble("user.twiliocredits");
			if (Math.abs(actualTwilioCredits - expectedTwilioCredits) < 0.001) {
				Assert.assertEquals(response.jsonPath().getDouble("user.twiliocredits"), expectedTwilioCredits, 0.001, "Twilio credits on intercom response should be " + expectedTwilioCredits);
				return;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		Assert.assertEquals(response.jsonPath().getDouble("user.twiliocredits"), expectedTwilioCredits, 0.001, "Twilio credits on intercom response should be " + expectedTwilioCredits);
	}

	protected void assertSeatCountsOnIntercomResponseAfterReducingSeats(int updatedSeats) {
		Response response = function.getAccountDetail(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(response.jsonPath().getInt("user.updatedseats"), updatedSeats, "Intercom user.updatedseats did not match expected value");
		Assert.assertEquals(response.jsonPath().getInt("user.monthly_updated_seats"), 0, "Intercom user.monthly_updated_seats did not match expected value");
		Assert.assertEquals(response.jsonPath().getInt("user.updated_add_on_records"), 0, "Intercom user.annual_updated_seats did not match expected value");
	}

	protected void assertAlbatrossFirstInvoiceCreatedOneMonthAfter(Response albatrossInvoicesPaid, Response albatrossInvoicesRenewed, String interval) {
		long paidEpoch = albatrossFirstInvoiceCreatedEpoch(albatrossInvoicesPaid);
		long renewedEpoch = albatrossFirstInvoiceCreatedEpoch(albatrossInvoicesRenewed);
		LocalDate paidUtc = faker.getEpochSecondsToLocalDateUtc(paidEpoch);
		LocalDate renewedUtc = faker.getEpochSecondsToLocalDateUtc(renewedEpoch);
		LocalDate expectedRenewed;
		if (interval.equalsIgnoreCase("year")) {
			expectedRenewed = paidUtc.plusYears(1);
		} else if (interval.equalsIgnoreCase("month")) {
			expectedRenewed = paidUtc.plusMonths(1);
		} else {
			throw new IllegalArgumentException("Unsupported billing interval: " + interval);
		}
		Assert.assertEquals(renewedUtc, expectedRenewed, "Albatross renewed invoice created date (UTC) should be one calendar " + interval + " after paid invoice");
	}

	protected void assertStripeNewestInvoiceAmountDiffersAcrossRenewal(Response beforeRenewal, Response afterRenewal) {
		int amountBefore = stripeFirstInvoiceAmountDue(beforeRenewal);
		int amountAfter = stripeFirstInvoiceAmountDue(afterRenewal);
		Assert.assertNotEquals(amountAfter, amountBefore, "Newest Stripe invoice amount_due should change across renewal after seat update (e.g. proration vs full period); before=" + amountBefore + " after=" + amountAfter);
	}

	protected void assertStripeNewestInvoiceStableFullCycleMonthlyRenewal(Response beforeRenewal, Response afterRenewal, boolean isReduced) {
		int amountBefore = stripeFirstInvoiceAmountDue(beforeRenewal);
		int amountAfter = stripeFirstInvoiceAmountDue(afterRenewal);
		if(isReduced)
			Assert.assertNotEquals(amountAfter, amountBefore, "Consecutive full-cycle monthly renewals at the same seat count should have the different amount_due on the newest invoice (cents) after reduction");
		else
			Assert.assertEquals(amountAfter, amountBefore, "Consecutive full-cycle monthly renewals at the same seat count should have the same amount_due on the newest invoice (cents)");
		LocalDate dateBefore = stripeFirstInvoiceDateUtc(beforeRenewal);
		LocalDate dateAfter = stripeFirstInvoiceDateUtc(afterRenewal);
		Assert.assertEquals(dateAfter, dateBefore.plusMonths(1), "Newest Stripe invoice date (UTC) should advance one calendar month on full-cycle renewal");
	}

	protected void assertAlbatrossNewestInvoiceAmountDiffersAcrossRenewal(Response beforeRenewal, Response afterRenewal) {
		int amountBefore = albatrossFirstInvoiceAmountDue(beforeRenewal);
		int amountAfter = albatrossFirstInvoiceAmountDue(afterRenewal);
		Assert.assertNotEquals(amountAfter, amountBefore, "Newest Albatross invoice amount should change across renewal after seat update; before=" + amountBefore + " after=" + amountAfter);
	}

	protected void assertAlbatrossNewestInvoiceStableFullCycleMonthlyRenewal(Response beforeRenewal, Response afterRenewal, boolean isReduced) {
		if(isReduced)
			Assert.assertNotEquals(albatrossFirstInvoiceAmountDue(afterRenewal), albatrossFirstInvoiceAmountDue(beforeRenewal), "Consecutive full-cycle monthly renewals at the same seat count should have the different amount_due on the newest invoice (cents) after reduction");
		else
			Assert.assertEquals(albatrossFirstInvoiceAmountDue(afterRenewal), albatrossFirstInvoiceAmountDue(beforeRenewal), "Consecutive full-cycle monthly renewals should match newest Albatross invoice amount");
		assertAlbatrossFirstInvoiceCreatedOneMonthAfter(beforeRenewal, afterRenewal, "month");
	}

	protected void assertStripeNewestInvoiceStableFullCycleAnnualRenewal(Response beforeRenewal, Response afterRenewal, boolean isReduced) {
		int amountBefore = stripeFirstInvoiceAmountDue(beforeRenewal);
		int amountAfter = stripeFirstInvoiceAmountDue(afterRenewal);
		if(isReduced)
			Assert.assertNotEquals(amountAfter, amountBefore, "Consecutive full-cycle annual renewals at the same seat count should have the different amount_due on the newest invoice (cents) after reduction");
		else
			Assert.assertEquals(amountAfter, amountBefore, "Consecutive full-cycle annual renewals at the same seat count should have the same amount_due on the newest invoice (cents)");
		LocalDate dateBefore = stripeFirstInvoiceDateUtc(beforeRenewal);
		LocalDate dateAfter = stripeFirstInvoiceDateUtc(afterRenewal);
		Assert.assertEquals(dateAfter, dateBefore.plusYears(1), "Newest Stripe invoice date (UTC) should advance one calendar year on full-cycle renewal");
	}

	protected void assertAlbatrossNewestInvoiceStableFullCycleAnnualRenewal(Response beforeRenewal, Response afterRenewal, boolean isReduced) {
		if(isReduced)
			Assert.assertNotEquals(albatrossFirstInvoiceAmountDue(afterRenewal), albatrossFirstInvoiceAmountDue(beforeRenewal), "Consecutive full-cycle annual renewals at the same seat count should have the different amount_due on the newest invoice (cents) after reduction");
		else
			Assert.assertEquals(albatrossFirstInvoiceAmountDue(afterRenewal), albatrossFirstInvoiceAmountDue(beforeRenewal), "Consecutive full-cycle annual renewals should match newest Albatross invoice amount");
		assertAlbatrossFirstInvoiceCreatedOneMonthAfter(beforeRenewal, afterRenewal, "year");
	}

	protected void subscribeToAdvanceAnalyticsAndAssert(String billingInterval) {
		final int maxAttempts = 3;
		Response response = null;
		String responseMessage = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			response = function.createMetabaseSubscription(albatrossURL, albatrossAuthToken, billingInterval);
			responseMessage = response.jsonPath().getString("response_message");
			if ("Subscription Created Successfully".equals(responseMessage)) {
				break;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		Assert.assertEquals(response.getStatusCode(), 200, "Advanced Analytics subscription creation should return 200 for billingInterval=" + billingInterval);
		Assert.assertEquals(responseMessage, "Subscription Created Successfully", "Unexpected Advanced Analytics subscription response message");
	}

	protected void upgradeAdvanceAnalyticsToAnnualAndAssert() {
		final int maxAttempts = 3;
		Response response = null;
		String responseMessage = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			response = function.upgradeMetabaseSubscription(albatrossURL, albatrossAuthToken);
			responseMessage = response.jsonPath().getString("response_message");
			if ("Subscription Upgraded Successfully".equals(responseMessage)) {
				break;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		Assert.assertEquals(response.getStatusCode(), 200, "Advanced Analytics subscription upgrade should return 200");
		Assert.assertEquals(responseMessage, "Subscription Upgraded Successfully", "Unexpected Advanced Analytics upgrade response message");
	}

	protected void assertAdvanceAnalyticsActive(String billingInterval, int expectedSeats) {
		final int maxAttempts = 3;
		Response sub = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			sub = function.getMetabaseSubscription(albatrossURL, albatrossAuthToken);
			if (!sub.jsonPath().getList("data.activeSubscription").isEmpty()) {
				break;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		Assert.assertFalse(sub.jsonPath().getList("data.activeSubscription").isEmpty(), "Advanced Analytics activeSubscription should not be empty");
		Assert.assertEquals(sub.jsonPath().getString("data.activeSubscription[0].billing_cycle"), billingInterval, "Advanced Analytics billing_cycle mismatch");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription[0].seats"), expectedSeats, "Advanced Analytics seats mismatch");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription[0].updated_seats"), 0, "Advanced Analytics updated_seats mismatch");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription[0].subscription_status"), 1, "Advanced Analytics subscription_status should be 1 (active)");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription[0].cancelled"), 0, "Advanced Analytics subscription should not be cancelled");
	}

	protected void assertAdvanceAnalyticsCancelled(boolean isCancelled,  String billingInterval, int expectedSeats) {
		Response sub = function.getMetabaseSubscription(albatrossURL, albatrossAuthToken);
		if(isCancelled) {
			Assert.assertNotNull(sub.jsonPath().get("data.cancelledSubscription"), "Advanced Analytics cancelledSubscription should not be null");
			Assert.assertEquals(sub.jsonPath().getString("data.cancelledSubscription.billing_cycle"), billingInterval, "Advanced Analytics billing_cycle mismatch");
			Assert.assertEquals(sub.jsonPath().getInt("data.cancelledSubscription.seats"), expectedSeats, "Advanced Analytics seats mismatch");
			Assert.assertEquals(sub.jsonPath().getInt("data.cancelledSubscription.updated_seats"), 0, "Advanced Analytics updated_seats mismatch");
			Assert.assertEquals(sub.jsonPath().getInt("data.cancelledSubscription.subscription_status"), 0, "Advanced Analytics subscription_status should be 0 (cancelled)");
			Assert.assertEquals(sub.jsonPath().getInt("data.cancelledSubscription.cancelled"), 1, "Advanced Analytics subscription should be cancelled");
		} else {
			Assert.assertNull(sub.jsonPath().getList("data.cancelledSubscription"), "Advanced Analytics cancelledSubscription should be null");
		}
	}

	protected void assertAdvanceAnalyticsNotSubscribed() {
		Response sub = function.getMetabaseSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(sub.jsonPath().get("message"), "Metabase subscription fetched successfully");
		Assert.assertTrue(sub.jsonPath().getList("data.activeSubscription").isEmpty(), "Advanced Analytics activeSubscription should be empty");
		Assert.assertNull(sub.jsonPath().get("data.cancelledSubscription"), "Advanced Analytics cancelledSubscription should be null");
	}

	protected void assertStripeSubscriptionAndInvoiceCount(String testClockId, String customerId, int subscriptionCount, int invoiceCount) {
		Response subscriptionAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		Assert.assertEquals(subscriptionAfterRenewal.jsonPath().getList("data").size(), subscriptionCount, "Subscription count mismatch after the renewal action");
		Response stripeInvoicesAfterRenewal = function.getRequiredSubscriptionDataFromStripe(customerId, "invoices");
		Assert.assertEquals(stripeInvoicesAfterRenewal.jsonPath().getList("data").size(), invoiceCount, "Stripe invoice count was not updated as expected after the renewal action");
	}

	protected void assertAlbatrossAdvanceAnalyticsInvoiceGroups(String billingInterval, boolean isMainSeatsEqual, boolean isAdvanceAnalyticsSeatsEqual) {

		Response response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		List<Map<String, Object>> allInvoices = response.jsonPath().getList("data");

		Map<String, List<Map<String, Object>>> bySubscription = new HashMap<>();
		for (Map<String, Object> invoice : allInvoices) {
			String subId = String.valueOf(invoice.get("subscriptionid"));
			bySubscription.computeIfAbsent(subId, k -> new ArrayList<>()).add(invoice);
		}

		for (List<Map<String, Object>> group : bySubscription.values()) {
			group.sort(Comparator.comparingLong(inv -> Long.parseLong(String.valueOf(inv.get("created")))));
		}

		List<String> sortedSubIds = new ArrayList<>(bySubscription.keySet());
		sortedSubIds.sort((a, b) -> {
			int minA = Integer.MAX_VALUE, minB = Integer.MAX_VALUE;
			for (Map<String, Object> inv : bySubscription.get(a)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minA) minA = id;
			}
			for (Map<String, Object> inv : bySubscription.get(b)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minB) minB = id;
			}
			return Integer.compare(minA, minB);
		});

		Assert.assertEquals(sortedSubIds.size(), 2, "Expected exactly 2 distinct subscriptionids in Albatross invoices");

		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(0)), billingInterval, isMainSeatsEqual, "Main (RCRM) subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(1)), billingInterval, isAdvanceAnalyticsSeatsEqual, "Advanced Analytics subscription");
	}

	protected void subscribeToAdvanceAnalyticsAndLinkedinIntegrationAndAssert(String billingInterval) {
		subscribeToAdvanceAnalyticsAndAssert(billingInterval);
		subscribeToLinkedinIntegrationAndAssert(billingInterval);
	}

	protected void upgradeAdvanceAnalyticsAndLinkedinIntegrationToAnnualAndAssert() {
		upgradeAdvanceAnalyticsToAnnualAndAssert();
		upgradeLinkedinIntegrationToAnnualAndAssert();
	}

	protected void assertBothAddOnsActive(String billingInterval, int expectedSeats) {
		assertAdvanceAnalyticsActive(billingInterval, expectedSeats);
		assertLinkedinIntegrationActive(billingInterval, expectedSeats);
	}

	protected void assertBothAddOnsCancelled(boolean isCancelled, String billingInterval, int expectedSeats) {
		assertAdvanceAnalyticsCancelled(isCancelled, billingInterval, expectedSeats);
		assertLinkedinIntegrationCancelled(isCancelled, billingInterval, expectedSeats);
	}

	protected void assertBothAddOnsNotSubscribed() {
		assertAdvanceAnalyticsNotSubscribed();
		assertLinkedinIntegrationNotSubscribed();
	}

	protected void assertBothAddOnsSeatsSyncedWithMainSeats(String customerId) {
		Response subscriptions = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		int mainSeats = subscriptions.jsonPath().getInt("data[0].items.data[0].quantity");
		int aaSeats   = subscriptions.jsonPath().getInt("data[1].items.data[0].quantity");
		int liSeats   = subscriptions.jsonPath().getInt("data[2].items.data[0].quantity");
		Assert.assertEquals(aaSeats, mainSeats, "Advanced Analytics seat quantity should stay in sync with main subscription seat quantity");
		Assert.assertEquals(liSeats, mainSeats, "LinkedIn Integration seat quantity should stay in sync with main subscription seat quantity");
	}

	protected void assertAlbatrossThreeSubscriptionInvoiceGroups(String billingInterval, boolean isMainSeatsEqual, boolean isAASeatsEqual, boolean isLISeatsEqual) {

		Response response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		List<Map<String, Object>> allInvoices = response.jsonPath().getList("data");

		Map<String, List<Map<String, Object>>> bySubscription = new HashMap<>();
		for (Map<String, Object> invoice : allInvoices) {
			String subId = String.valueOf(invoice.get("subscriptionid"));
			bySubscription.computeIfAbsent(subId, k -> new ArrayList<>()).add(invoice);
		}

		for (List<Map<String, Object>> group : bySubscription.values()) {
			group.sort(Comparator.comparingLong(inv -> Long.parseLong(String.valueOf(inv.get("created")))));
		}

		List<String> sortedSubIds = new ArrayList<>(bySubscription.keySet());
		sortedSubIds.sort((a, b) -> {
			int minA = Integer.MAX_VALUE, minB = Integer.MAX_VALUE;
			for (Map<String, Object> inv : bySubscription.get(a)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minA) minA = id;
			}
			for (Map<String, Object> inv : bySubscription.get(b)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minB) minB = id;
			}
			return Integer.compare(minA, minB);
		});

		Assert.assertEquals(sortedSubIds.size(), 3, "Expected exactly 3 distinct subscriptionids in Albatross invoices (RCRM + AA + LI)");

		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(0)), billingInterval, isMainSeatsEqual, "Main (RCRM) subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(1)), billingInterval, isAASeatsEqual, "Advanced Analytics subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(2)), billingInterval, isLISeatsEqual, "LinkedIn Integration subscription");
	}

	protected void assertAlbatrossInvoiceGroup(List<Map<String, Object>> invoices, String billingInterval, boolean isEqual, String label) {

		Assert.assertFalse(invoices.isEmpty(), label + " invoice list should not be empty");

		if (isEqual) {
			long expectedAmount = Long.parseLong(String.valueOf(invoices.get(0).get("amountdue")));
			for (int i = 1; i < invoices.size(); i++) {
				long actualAmount = Long.parseLong(String.valueOf(invoices.get(i).get("amountdue")));
				Assert.assertEquals(actualAmount, expectedAmount, label + " invoice amountdue should be equal across all renewals (no seat change)");
			}
			for (int i = 1; i < invoices.size(); i++) {
				long prevEpoch = Long.parseLong(String.valueOf(invoices.get(i - 1).get("created")));
				long currEpoch = Long.parseLong(String.valueOf(invoices.get(i).get("created")));
				LocalDate prevDate = faker.getEpochSecondsToLocalDateUtc(prevEpoch);
				LocalDate currDate = faker.getEpochSecondsToLocalDateUtc(currEpoch);
				LocalDate expectedDate = "year".equalsIgnoreCase(billingInterval) ? prevDate.plusYears(1) : prevDate.plusMonths(1);
				Assert.assertEquals(currDate, expectedDate, label + " consecutive invoice created dates should be exactly one " + billingInterval + " apart");
			}
		}
	}

	protected void assertAdvanceAnalyticsSeatsSyncedWithMainSeats(String customerId) {
		Response subscriptions = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		int mainSeats = subscriptions.jsonPath().getInt("data[0].items.data[0].quantity");
		int aaSeats   = subscriptions.jsonPath().getInt("data[1].items.data[0].quantity");
		Assert.assertEquals(aaSeats, mainSeats, "Advanced Analytics seat quantity should stay in sync with main subscription seat quantity");
	}

	protected void subscribeToLinkedinIntegrationAndAssert(String billingInterval) {
		final int maxAttempts = 3;
		Response response = null;
		String responseMessage = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			response = function.createUnipileSubscription(albatrossURL, albatrossAuthToken, billingInterval);
			responseMessage = response.jsonPath().getString("response_message");
			if ("Subscription Created Successfully".equals(responseMessage)) {
				break;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		Assert.assertEquals(response.getStatusCode(), 200, "LinkedIn Integration subscription creation should return 200 for billingInterval=" + billingInterval);
		Assert.assertEquals(responseMessage, "Subscription Created Successfully", "Unexpected LinkedIn Integration subscription response message");
	}

	protected void upgradeLinkedinIntegrationToAnnualAndAssert() {
		final int maxAttempts = 3;
		Response response = null;
		String responseMessage = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			response = function.upgradeUnipileSubscription(albatrossURL, albatrossAuthToken);
			responseMessage = response.jsonPath().getString("response_message");
			if ("Subscription Upgraded Successfully".equals(responseMessage)) {
				break;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		Assert.assertEquals(response.getStatusCode(), 200, "LinkedIn Integration subscription upgrade should return 200");
		Assert.assertEquals(responseMessage, "Subscription Upgraded Successfully", "Unexpected LinkedIn Integration upgrade response message");
	}

	protected void assertLinkedinIntegrationActive(String billingInterval, int expectedSeats) {
		final int maxAttempts = 3;
		Response sub = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			sub = function.getUnipileSubscription(albatrossURL, albatrossAuthToken);
			if (!sub.jsonPath().getList("data.activeSubscription").isEmpty()) {
				break;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		Assert.assertFalse(sub.jsonPath().getList("data.activeSubscription").isEmpty(), "LinkedIn Integration activeSubscription should not be empty");
		Assert.assertEquals(sub.jsonPath().getString("data.activeSubscription[0].billing_cycle"), billingInterval, "LinkedIn Integration billing_cycle mismatch");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription[0].seats"), expectedSeats, "LinkedIn Integration seats mismatch");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription[0].updated_seats"), 0, "LinkedIn Integration updated_seats mismatch");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription[0].subscription_status"), 1, "LinkedIn Integration subscription_status should be 1 (active)");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription[0].cancelled"), 0, "LinkedIn Integration subscription should not be cancelled");
	}

	protected void assertLinkedinIntegrationCancelled(boolean isCancelled, String billingInterval, int expectedSeats) {
		Response sub = function.getUnipileSubscription(albatrossURL, albatrossAuthToken);
		if (isCancelled) {
			Assert.assertNotNull(sub.jsonPath().get("data.cancelledSubscription"), "LinkedIn Integration cancelledSubscription should not be null");
			Assert.assertEquals(sub.jsonPath().getString("data.cancelledSubscription.billing_cycle"), billingInterval, "LinkedIn Integration billing_cycle mismatch");
			Assert.assertEquals(sub.jsonPath().getInt("data.cancelledSubscription.seats"), expectedSeats, "LinkedIn Integration seats mismatch");
			Assert.assertEquals(sub.jsonPath().getInt("data.cancelledSubscription.updated_seats"), 0, "LinkedIn Integration updated_seats mismatch");
			Assert.assertEquals(sub.jsonPath().getInt("data.cancelledSubscription.subscription_status"), 0, "LinkedIn Integration subscription_status should be 0 (cancelled)");
			Assert.assertEquals(sub.jsonPath().getInt("data.cancelledSubscription.cancelled"), 1, "LinkedIn Integration subscription should be cancelled");
		} else {
			Assert.assertNull(sub.jsonPath().getList("data.cancelledSubscription"), "LinkedIn Integration cancelledSubscription should be null");
		}
	}

	protected void assertLinkedinIntegrationNotSubscribed() {
		Response sub = function.getUnipileSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(sub.jsonPath().get("message"), "No active Unipile Subscription found");
		Assert.assertTrue(sub.jsonPath().getList("data.activeSubscription").isEmpty(), "LinkedIn Integration activeSubscription should be empty");
		Assert.assertNull(sub.jsonPath().get("data.cancelledSubscription"), "LinkedIn Integration cancelledSubscription should be null");
	}

	protected void assertDataEnrichmentNotSubscribed() {
		Response response = function.getDataEnrichmentSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(response.jsonPath().getString("message"), "Contact Out subscription fetched successfully");
		Assert.assertNull(response.jsonPath().get("data.activeSubscription"), "Data Enrichment activeSubscription should be null before purchase");
		Assert.assertNull(response.jsonPath().get("data.scheduledSubscription"), "Data Enrichment scheduledSubscription should be null before purchase");
	}

	protected Response fetchTwilioCreditUsage() {
		Response response = function.getTwilioCreditUsage(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(response.jsonPath().getString("status"), "success", "Twilio credit usage status should be success");
		Assert.assertEquals(response.jsonPath().getString("message_type"), "is-success", "Twilio credit usage message_type should be is-success");
		return response;
	}

	protected Response fetchCallingCreditTransactions() {
		Response response = function.getAllCallingTransactions(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(response.jsonPath().getString("status"), "success", "Calling credit transactions status should be success");
		Assert.assertEquals(response.jsonPath().getString("message_type"), "is-success", "Calling credit transactions message_type should be is-success");
		return response;
	}

	protected Response fetchCallingCreditTransactionsUntilInvoiceCount(int expectedCount) {
		final int maxAttempts = 3;
		Response response = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			response = fetchCallingCreditTransactions();
			List<?> invoices = response.jsonPath().getList("data.invoices");
			int actualCount = invoices == null ? 0 : invoices.size();
			if (actualCount == expectedCount) {
				break;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		return response;
	}

	protected void assertAlbatrossCallingCreditTransactionInvoiceFields(Response response, int invoiceIndex, int price, int qty) {
		String invoicePath = "data.invoices[" + invoiceIndex + "]";
		Assert.assertEquals(response.jsonPath().getInt(invoicePath + ".price"), price, "Calling credit transaction invoice price should be " + price);
		Assert.assertEquals(response.jsonPath().getInt(invoicePath + ".qty"), qty, "Calling credit transaction invoice qty should be " + qty);
		Assert.assertEquals(response.jsonPath().getString(invoicePath + ".currency"), "usd", "Calling credit transaction invoice currency should be usd");
		Assert.assertEquals(response.jsonPath().getInt(invoicePath + ".paid"), 1, "Calling credit transaction invoice should be paid");
		Assert.assertNotNull(response.jsonPath().getString(invoicePath + ".transactionid"), "Calling credit transaction invoice transactionid should not be null");
		Assert.assertNotNull(response.jsonPath().getString(invoicePath + ".receipturl"), "Calling credit transaction invoice receipturl should not be null");
	}

	protected void assertNoAlbatrossCallingCreditTransactions() {
		Response response = fetchCallingCreditTransactionsUntilInvoiceCount(0);
		List<?> invoices = response.jsonPath().getList("data.invoices");
		int actualCount = invoices == null ? 0 : invoices.size();
		Assert.assertEquals(actualCount, 0, "Albatross calling credit transactions should be empty before purchase");
	}

	protected void assertAlbatrossCallingCreditTransactions(int expectedCount, int price, int qty) {
		Response response = fetchCallingCreditTransactionsUntilInvoiceCount(expectedCount);
		List<?> invoices = response.jsonPath().getList("data.invoices");
		Assert.assertEquals(invoices.size(), expectedCount, "Albatross calling credit transactions count should be " + expectedCount);
		for (int i = 0; i < expectedCount; i++) {
			assertAlbatrossCallingCreditTransactionInvoiceFields(response, i, price, qty);
		}
	}

	protected void assertAlbatrossCallingCreditTransactions(int expectedCount, int price, int[] quantities) {
		Response response = fetchCallingCreditTransactionsUntilInvoiceCount(expectedCount);
		List<?> invoices = response.jsonPath().getList("data.invoices");
		Assert.assertEquals(invoices.size(), expectedCount, "Albatross calling credit transactions count should be " + expectedCount);
		for (int i = 0; i < expectedCount; i++) {
			int purchaseIndex = expectedCount - 1 - i;
			assertAlbatrossCallingCreditTransactionInvoiceFields(response, i, price, quantities[purchaseIndex]);
		}
	}

	protected Response fetchStripeCallingCharges(String customerId) {
		Response response = function.getRequiredChargesDataFromStripe(customerId);
		Assert.assertEquals(response.getStatusCode(), 200, "Fetching Stripe calling charges should return 200");
		return response;
	}

	protected int countStripeCallingCreditCharges(Response chargesResponse) {
		List<?> charges = chargesResponse.jsonPath().getList("data");
		if (charges == null) {
			return 0;
		}
		int callingCreditChargeCount = 0;
		for (int i = 0; i < charges.size(); i++) {
			String description = chargesResponse.jsonPath().getString("data[" + i + "].description");
			if (description != null && description.contains("Calling Credits Payment")) {
				callingCreditChargeCount++;
			}
		}
		return callingCreditChargeCount;
	}

	protected void assertStripeCallingCreditChargeFields(Response chargesResponse, int chargeIndex, int price, int qty, String customerId) {
		String chargePath = "data[" + chargeIndex + "]";
		int expectedAmountCents = price * qty * 100;
		Assert.assertEquals(chargesResponse.jsonPath().getString(chargePath + ".object"), "charge", "Stripe charge object type should be charge");
		Assert.assertEquals(chargesResponse.jsonPath().getInt(chargePath + ".amount"), expectedAmountCents, "Stripe calling credit charge amount should be " + expectedAmountCents + " cents");
		Assert.assertEquals(chargesResponse.jsonPath().getInt(chargePath + ".amount_captured"), expectedAmountCents, "Stripe calling credit charge amount_captured should be " + expectedAmountCents + " cents");
		Assert.assertEquals(chargesResponse.jsonPath().getString(chargePath + ".currency"), "usd", "Stripe calling credit charge currency should be usd");
		Assert.assertEquals(chargesResponse.jsonPath().getString(chargePath + ".customer"), customerId, "Stripe calling credit charge customer should match test customer");
		Assert.assertTrue(chargesResponse.jsonPath().getString(chargePath + ".description").contains("Calling Credits Payment"), "Stripe calling credit charge description should contain Calling Credits Payment");
		Assert.assertEquals(chargesResponse.jsonPath().getString(chargePath + ".status"), "succeeded", "Stripe calling credit charge status should be succeeded");
		Assert.assertTrue(chargesResponse.jsonPath().getBoolean(chargePath + ".paid"), "Stripe calling credit charge should be paid");
		Assert.assertEquals(chargesResponse.jsonPath().getString(chargePath + ".metadata.context"), "twilio_credit_payment", "Stripe calling credit charge metadata.context should be twilio_credit_payment");
		Assert.assertEquals(chargesResponse.jsonPath().getString(chargePath + ".metadata.amount"), String.valueOf(price), "Stripe calling credit charge metadata.amount should match purchase price");
	}

	protected void assertStripeCallingCreditChargesCount(String customerId, int expectedCount) {
		final int maxAttempts = 3;
		Response response = null;
		int actualCount = 0;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			response = fetchStripeCallingCharges(customerId);
			actualCount = countStripeCallingCreditCharges(response);
			if (actualCount == expectedCount) {
				break;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		Assert.assertEquals(actualCount, expectedCount, "Stripe calling credit charges count should be " + expectedCount + " for customer " + customerId);
	}

	protected void assertStripeCallingCreditChargesCount(String customerId, int expectedCount, int price, int qty) {
		assertStripeCallingCreditChargesCount(customerId, expectedCount);
		if (expectedCount == 0) {
			return;
		}
		Response response = fetchStripeCallingCharges(customerId);
		List<?> charges = response.jsonPath().getList("data");
		int validatedCallingCreditCharges = 0;
		for (int i = 0; i < charges.size(); i++) {
			String description = response.jsonPath().getString("data[" + i + "].description");
			if (description != null && description.contains("Calling Credits Payment")) {
				assertStripeCallingCreditChargeFields(response, i, price, qty, customerId);
				validatedCallingCreditCharges++;
			}
		}
		Assert.assertEquals(validatedCallingCreditCharges, expectedCount, "Validated Stripe calling credit charges count should be " + expectedCount);
	}

	protected Response purchaseCallingCreditsAndAssert(int price, int qty) {
		Response response = function.makeCallingCreditPayment(albatrossURL, albatrossAuthToken, price, qty);
		Assert.assertEquals(response.getStatusCode(), 200, "Calling credit payment should return 200 for price=" + price + " qty=" + qty);
		Assert.assertEquals(response.jsonPath().getString("message"), "Calling Credit Payment In Progress", "Calling credit payment message should indicate payment in progress");
		Assert.assertEquals(response.jsonPath().getString("message_type"), "is-success", "Calling credit payment message_type should be is-success");
		Assert.assertNotNull(response.jsonPath().getString("data.payment_intent.id"), "Calling credit payment intent id should not be null");
		int expectedAmountCents = price * qty * 100;
		Assert.assertEquals(response.jsonPath().getInt("data.payment_intent.amount"), expectedAmountCents, "Calling credit payment intent amount is not matched : " + response.jsonPath().getInt("data.payment_intent.amount"));
		return response;
	}

	protected Response fetchTwilioCreditUsageUntilCallingCreditsMatch(int expectedPurchasedCredits, double expectedFreeCallingCredits, int expectedEnterpriseCallingCredits) {
		final int maxAttempts = 3;
		Response response = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			response = fetchTwilioCreditUsage();
			boolean purchasedCreditsMatch = response.jsonPath().getInt("data.total_credit_purchased") == expectedPurchasedCredits;
			boolean freeCallingCreditsMatch = Math.abs(response.jsonPath().getDouble("data.free_calling_credits") - expectedFreeCallingCredits) < 0.001;
			boolean enterpriseCallingCreditsMatch = response.jsonPath().getInt("data.enterprise_calling_credits") == expectedEnterpriseCallingCredits;
			if (purchasedCreditsMatch && freeCallingCreditsMatch && enterpriseCallingCreditsMatch) {
				break;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		return response;
	}

	protected void assertCommonCallingCreditFields(Response response, int expectedPurchasedCredits, String plan) {
		Assert.assertEquals(response.jsonPath().getString("data.currencyCode"), "usd", "currencyCode should be usd for " + plan + " plan");
		Assert.assertEquals(response.jsonPath().getString("data.currency"), "$", "currency symbol should be $ for " + plan + " plan");
		Assert.assertEquals(response.jsonPath().getInt("data.total_credit_purchased"), expectedPurchasedCredits, "total_credit_purchased should be " + expectedPurchasedCredits + " for " + plan + " plan");
		Assert.assertEquals(response.jsonPath().getInt("data.calling_credit_usage"), 0, "calling_credit_usage should be 0 for " + plan + " plan");
		Assert.assertEquals(response.jsonPath().getInt("data.message_credit_usage"), 0, "message_credit_usage should be 0 for " + plan + " plan");
	}

	protected void assertEnterpriseCallingCredits(int expectedCallingCredits) {
		Response response = fetchTwilioCreditUsageUntilCallingCreditsMatch(expectedCallingCredits, 0.0, 25);
		assertCommonCallingCreditFields(response, expectedCallingCredits, "Enterprise");
		Assert.assertEquals(response.jsonPath().getDouble("data.free_calling_credits"), 0.0, 0.001, "free_calling_credits should be 0 for Enterprise plan");
		Assert.assertEquals(response.jsonPath().getInt("data.enterprise_calling_credits"), 25, "enterprise_calling_credits should be 25 for Enterprise plan");
	}

	protected void assertEnterpriseIncludedAddons(boolean isRenewal) {
		Response metabase = function.getMetabaseSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(metabase.jsonPath().getString("message"), "Metabase access available through Enterprise plan");
		Assert.assertEquals(metabase.jsonPath().getBoolean("data.enterprisePlan"), true, "Advanced Analytics enterprisePlan should be true");
		Assert.assertNotNull(metabase.jsonPath().getString("data.metabaseDemoCallBookingLink"), "Metabase demo call booking link should not be null");

		Response unipile = function.getUnipileSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(unipile.jsonPath().getString("message"), "Unipile access available through Enterprise plan");
		Assert.assertEquals(unipile.jsonPath().getBoolean("data.enterprisePlan"), true, "LinkedIn Integration enterprisePlan should be true");

		Response workato = function.getWorkatoSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(workato.jsonPath().getString("message"), "Automation subscription fetched successfully");
		Assert.assertEquals(workato.jsonPath().getBoolean("data.isPlanExist"), true, "Workato isPlanExist should be true");
		Assert.assertNull(workato.jsonPath().get("data.activePlan"), "Workato activePlan should be null");
		Assert.assertEquals(workato.jsonPath().getInt("data.inactivePlan.tasks"), 50, "Workato inactivePlan tasks should be 50");

		if (!isRenewal) {
			Response enrichment = function.getEnterpriseEnrichmentSubscription(albatrossURL, albatrossAuthToken);
			Assert.assertEquals(enrichment.jsonPath().getString("message"), "Enterprise credits usage fetched successfully");
			Assert.assertEquals(enrichment.jsonPath().getInt("data.enterpriseCredits.addon_type"), 3, "Enterprise enrichment addon_type should be 3");
			Assert.assertEquals(enrichment.jsonPath().getInt("data.enterpriseCredits.total_credit"), 500, "Enterprise enrichment total_credit should be 500");
			Assert.assertEquals(enrichment.jsonPath().getBoolean("data.creditsExists"), true, "Enterprise enrichment creditsExists should be true");

			Response vonq = function.getVONQSubscription(albatrossURL, albatrossAuthToken);
			Assert.assertEquals(vonq.jsonPath().getString("message"), "Vonq subscription fetched successfully");
			Assert.assertNotNull(vonq.jsonPath().get("data.enterpriseActiveSubscription"), "VONQ enterpriseActiveSubscription should not be null");
			Assert.assertEquals(vonq.jsonPath().getInt("data.enterpriseActiveSubscription.total_credits"), 10, "VONQ enterpriseActiveSubscription total_credits should be 10");

			Response calling = function.getTwilioCreditUsage(albatrossURL, albatrossAuthToken);
			Assert.assertEquals(calling.jsonPath().getString("message"), "");
			Assert.assertEquals(calling.jsonPath().getString("message_type"), "is-success");
			Assert.assertEquals(calling.jsonPath().getInt("data.enterprise_calling_credits"), 25, "enterprise_calling_credits should be 25");
		}
			
	}

	protected void assertEnterpriseIncludedAddonsExpired() {
		Response metabase = function.getMetabaseSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(metabase.jsonPath().getString("message"), "Metabase subscription fetched successfully");
		Assert.assertNotNull(metabase.jsonPath().get("data.cancelledSubscription"), "Advanced Analytics cancelledSubscription should not be null after Enterprise to Pro downgrade");
		Assert.assertTrue(metabase.jsonPath().getString("data.cancelledSubscription.subscription_id").startsWith("Enterprise_"), "Advanced Analytics cancelled subscription_id should reference Enterprise plan after downgrade");

		Response unipile = function.getUnipileSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(unipile.jsonPath().getString("message"), "No active Unipile Subscription found");
		Assert.assertNull(unipile.jsonPath().get("data.enterprisePlan"), "LinkedIn Integration enterprisePlan should be absent after Enterprise to Pro downgrade");

		Response workato = function.getWorkatoSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(workato.jsonPath().getString("message"), "Automation subscription fetched successfully");
		Assert.assertEquals(workato.jsonPath().getString("data.activePlan.account_plan"), "Enterprise", "Workato activePlan account_plan should be Enterprise after downgrade");
		Assert.assertEquals(workato.jsonPath().getInt("data.activePlan.tasks"), 50, "Workato activePlan tasks should be 50 after Enterprise to Pro downgrade");

		Response enrichment = function.getDataEnrichmentSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(enrichment.jsonPath().getString("message"), "Contact Out subscription fetched successfully");
		Assert.assertNull(enrichment.jsonPath().get("data.enterpriseCredits"), "Data Enrichment enterpriseCredits should be absent after Enterprise to Pro downgrade");

		Response vonq = function.getVONQSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(vonq.jsonPath().getString("message"), "Vonq subscription fetched successfully");
		Assert.assertNull(vonq.jsonPath().get("data.enterpriseActiveSubscription"), "VONQ enterpriseActiveSubscription should be absent after Enterprise to Pro downgrade");

		Response calling = function.getTwilioCreditUsage(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(calling.jsonPath().getInt("data.enterprise_calling_credits"), 0, "enterprise_calling_credits should be 0 after Enterprise to Pro downgrade");
	}

	protected void assertFreeCallingCredits(int expectedPurchasedCredits, String plan, double expectedFreeCallingCredits) {
		Response response = fetchTwilioCreditUsageUntilCallingCreditsMatch(expectedPurchasedCredits, expectedFreeCallingCredits, 0);
		assertCommonCallingCreditFields(response, expectedPurchasedCredits, plan);
		Assert.assertEquals(response.jsonPath().getInt("data.enterprise_calling_credits"), 0, "enterprise_calling_credits should be 0 for " + plan + " plan");
		Assert.assertEquals(response.jsonPath().getDouble("data.free_calling_credits"), expectedFreeCallingCredits, 0.001, "free_calling_credits should be " + expectedFreeCallingCredits + " for " + plan + " plan");
	}

	protected double resolveExpectedTwilioCreditsOnIntercom(int totalPurchasedCredits, String plan) {
		return plan.equalsIgnoreCase("Enterprise") ? totalPurchasedCredits + 0.1 : totalPurchasedCredits + 0.0;
	}

	protected void assertPaidPlanCallingCreditsState(String plan, int totalPurchasedCredits) {
		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(totalPurchasedCredits);
		} else {
			assertFreeCallingCredits(totalPurchasedCredits, plan, 0.0);
		}
		verifyTwilioCreditsOnIntercomResponse(resolveExpectedTwilioCreditsOnIntercom(totalPurchasedCredits, plan));
	}

	protected Response fetchVONQSubscription() {
		Response response = fetchVONQSubscriptionResponse();
		Assert.assertNull(response.jsonPath().get("data.scheduledSubscription"), "VONQ scheduledSubscription should be null");
		return response;
	}

	protected Response fetchVONQSubscriptionForDowngrade() {
		return fetchVONQSubscriptionResponse();
	}

	private Response fetchVONQSubscriptionResponse() {
		Response response = function.getVONQSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertEquals(response.getStatusCode(), 200, "Fetching VONQ subscription should return 200");
		Assert.assertEquals(response.jsonPath().getString("message"), "Vonq subscription fetched successfully");
		Assert.assertEquals(response.jsonPath().getString("message_type"), "is_success");
		return response;
	}

	protected void subscribeToVONQAndAssert(String plan, int selectedPlanId) {
		Integer existingPlanId = null;
		Response response = function.createVONQSubscription(albatrossURL, albatrossAuthToken, selectedPlanId, existingPlanId);
		Assert.assertEquals(response.getStatusCode(), 200, "VONQ subscription creation should return 200 for selectedPlanId=" + selectedPlanId);
		Assert.assertEquals(response.jsonPath().getString("response_message"), "Subscription Created Successfully");
		Assert.assertNotNull(response.jsonPath().get("data.id"), "VONQ subscription id should not be null");
	}

	protected void upgradeVONQAndAssert(String plan, int selectedPlanId, int existingPlanId) {
		waitBetweenTheEveryScript(10000);
		Response response = function.upgradeVONQSubscription(albatrossURL, albatrossAuthToken, selectedPlanId, existingPlanId);
		Assert.assertEquals(response.getStatusCode(), 200, "VONQ subscription upgrade should return 200 for selectedPlanId=" + selectedPlanId);
		Assert.assertTrue(response.jsonPath().getString("response_message").equalsIgnoreCase("Subscription Created Successfully") || response.jsonPath().getString("response_message").equalsIgnoreCase("Subscription updated successfully"), "Unexpected VONQ subscription upgrade response message " + response.jsonPath().getString("response_message"));
		Assert.assertNotNull(response.jsonPath().get("data.id"), "VONQ subscription id should not be null after upgrade");
	}

	protected void downgradeVONQAndAssert(String plan, int selectedPlanId, int existingPlanId) {
		waitBetweenTheEveryScript(10000);
		Response response = function.upgradeVONQSubscription(albatrossURL, albatrossAuthToken, selectedPlanId, existingPlanId);
		String responseMessage = response.jsonPath().getString("response_message");
		Assert.assertEquals(response.getStatusCode(), 200, "VONQ subscription downgrade should return 200 for selectedPlanId=" + selectedPlanId);
		Assert.assertTrue(responseMessage.equalsIgnoreCase("Subscription Downgraded Successfully"), "Unexpected VONQ subscription downgrade response message " + responseMessage);
		Assert.assertEquals(response.jsonPath().get("data"), "downgraded", "VONQ subscription should be downgraded");
	}

	protected void assertVONQDowngradeWithScheduledSubscription(String plan, String activeBillingCycle, int expectedActiveCredits, int expectedScheduledPlanId, String scheduledBillingCycle, int expectedScheduledPlanCredits) {
		waitBetweenTheEveryScript(10000);
		Response response = fetchVONQSubscriptionForDowngrade();
		final int maxAttempts = 6;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			if (isVONQDowngradeScheduledReadyForAssert(response, activeBillingCycle, expectedActiveCredits,
					expectedScheduledPlanId, scheduledBillingCycle, expectedScheduledPlanCredits) || attempt == maxAttempts) {
				break;
			}
			waitBetweenTheEveryScript(10000);
			response = fetchVONQSubscriptionForDowngrade();
		}

		Assert.assertNotNull(response.jsonPath().get("data.activeSubscription"), "VONQ activeSubscription should not be null after downgrade");
		Assert.assertEquals(response.jsonPath().getString("data.activeSubscription.billing_cycle"), activeBillingCycle, "VONQ active billing_cycle mismatch after downgrade");
		Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.total_credits"), expectedActiveCredits, "VONQ active total_credits mismatch after downgrade");
		Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.used_credits"), 0, "VONQ active used_credits should be 0 after downgrade");
		Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.subscription_status"), 1, "VONQ active subscription_status should be 1 after downgrade");
		Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.cancelled"), 1, "VONQ active subscription should not be cancelled after downgrade");

		Assert.assertNotNull(response.jsonPath().get("data.scheduledSubscription"), "VONQ scheduledSubscription should not be null after downgrade");
		Assert.assertEquals(response.jsonPath().getInt("data.scheduledSubscription.vonq_stripe_plan_id"), expectedScheduledPlanId, "VONQ scheduled vonq_stripe_plan_id mismatch after downgrade");
		Assert.assertEquals(response.jsonPath().getString("data.scheduledSubscription.billing_cycle"), scheduledBillingCycle, "VONQ scheduled billing_cycle mismatch after downgrade");
		Assert.assertEquals(response.jsonPath().getInt("data.scheduledSubscription.plan_credits"), expectedScheduledPlanCredits, "VONQ scheduled plan_credits mismatch after downgrade");
		Assert.assertEquals(response.jsonPath().getInt("data.scheduledSubscription.subscription_status"), 2, "VONQ scheduled subscription_status should be 2 after downgrade");
		Assert.assertEquals(response.jsonPath().getInt("data.scheduledSubscription.cancelled"), 0, "VONQ scheduled subscription should not be cancelled after downgrade");
	}

	private boolean isVONQDowngradeScheduledReadyForAssert(Response response, String activeBillingCycle, int expectedActiveCredits, int expectedScheduledPlanId, String scheduledBillingCycle, int expectedScheduledPlanCredits) {
		Object scheduledSubscription = response.jsonPath().get("data.scheduledSubscription");
		Object activeSubscription = response.jsonPath().get("data.activeSubscription");
		if (scheduledSubscription == null || activeSubscription == null) {
			return false;
		}
		int scheduledPlanId = response.jsonPath().getInt("data.scheduledSubscription.vonq_stripe_plan_id");
		String scheduledCycle = response.jsonPath().getString("data.scheduledSubscription.billing_cycle");
		int scheduledPlanCredits = response.jsonPath().getInt("data.scheduledSubscription.plan_credits");
		int activeCredits = response.jsonPath().getInt("data.activeSubscription.total_credits");
		String activeCycle = response.jsonPath().getString("data.activeSubscription.billing_cycle");
		return scheduledPlanId == expectedScheduledPlanId && scheduledBillingCycle.equals(scheduledCycle) && scheduledPlanCredits == expectedScheduledPlanCredits && activeBillingCycle.equals(activeCycle) && activeCredits == expectedActiveCredits;
	}

	protected void assertVONQActive(String plan, String billingCycle, int expectedCredits) {
		waitBetweenTheEveryScript(10000);
		Response response = fetchVONQSubscription();
		final int maxAttempts = 5;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			if (isVONQSubscriptionReadyForAssert(response, plan, billingCycle, expectedCredits) || attempt == maxAttempts) {
				break;
			}
			waitBetweenTheEveryScript(10000);
			response = fetchVONQSubscription();
		}

		Object activeSubscription = response.jsonPath().get("data.activeSubscription");
		if (activeSubscription != null) {
			Assert.assertEquals(response.jsonPath().getString("data.activeSubscription.billing_cycle"), billingCycle, "VONQ billing_cycle mismatch");
			Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.total_credits"), expectedCredits, "VONQ total_credits mismatch");
			Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.used_credits"), 0, "VONQ used_credits should be 0");
			Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.subscription_status"), 1, "VONQ subscription_status should be 1 (active)");
			Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.cancelled"), 0, "VONQ subscription should not be cancelled");
		} else {
			Assert.assertTrue("Enterprise".equalsIgnoreCase(plan), "VONQ activeSubscription should not be null");
			Assert.assertNotNull(response.jsonPath().get("data.enterpriseActiveSubscription"), "VONQ enterpriseActiveSubscription should not be null");
			Assert.assertEquals(response.jsonPath().getInt("data.enterpriseActiveSubscription.total_credits"), 10, "VONQ total_credits mismatch for Enterprise");
			Assert.assertEquals(response.jsonPath().getInt("data.enterpriseActiveSubscription.used_credits"), 0, "VONQ used_credits should be 0 for Enterprise");
		}
	}

	private boolean isVONQSubscriptionReadyForAssert(Response response, String plan, String billingCycle, int expectedCredits) {
		Object activeSubscription = response.jsonPath().get("data.activeSubscription");
		if (activeSubscription != null) {
			Object vonqStripePlanIdObj = response.jsonPath().get("data.activeSubscription.vonq_stripe_plan_id");
			int vonqStripePlanId = vonqStripePlanIdObj == null ? -1 : Integer.parseInt(String.valueOf(vonqStripePlanIdObj));
			int totalCredits = response.jsonPath().getInt("data.activeSubscription.total_credits");
			String actualBillingCycle = response.jsonPath().getString("data.activeSubscription.billing_cycle");
			return vonqStripePlanId != 181 && totalCredits == expectedCredits && billingCycle.equals(actualBillingCycle);
		}
		return false;
	}

	protected void assertVONQRolledOverCredits(int expectedCredits, int expectedRolledOverCredits) {
		assertVONQRolledOverCreditsInternal(false, expectedCredits, expectedRolledOverCredits);
	}

	protected void assertVONQRolledOverCreditsForDowngrade(int expectedCredits, int expectedRolledOverCredits) {
		assertVONQRolledOverCreditsInternal(true, expectedCredits, expectedRolledOverCredits);
	}

	private void assertVONQRolledOverCreditsInternal(boolean allowScheduledSubscription, int expectedCredits, int expectedRolledOverCredits) {
		Response response = allowScheduledSubscription ? fetchVONQSubscriptionForDowngrade() : fetchVONQSubscription();
		final int maxAttempts = 6;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			Object activeSubscription = response.jsonPath().get("data.activeSubscription");
			if (activeSubscription != null) {
				int totalCredits = response.jsonPath().getInt("data.activeSubscription.total_credits");
				int rolledOverCredits = response.jsonPath().getInt("data.activeSubscription.rolled_over_credits");
				if (totalCredits == expectedCredits && rolledOverCredits == expectedRolledOverCredits) {
					return;
				}
			}
			if (attempt == maxAttempts) {
				break;
			}
			waitBetweenTheEveryScript(10000);
			response = allowScheduledSubscription ? fetchVONQSubscriptionForDowngrade() : fetchVONQSubscription();
		}
		Assert.assertNotNull(response.jsonPath().get("data.activeSubscription"), "VONQ activeSubscription should not be null");
		Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.total_credits"), expectedCredits, "VONQ total_credits mismatch");
		Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.plan_credits"), expectedCredits, "VONQ plan_credits mismatch");
		Assert.assertEquals(response.jsonPath().getInt("data.activeSubscription.rolled_over_credits"), expectedRolledOverCredits, "VONQ rolled_over_credits mismatch");
	}

	protected void assertAlbatrossVONQInvoiceGroups(String billingInterval, boolean isMainSeatsEqual, boolean isVONQEqual, int distinctSubscriptionCount) {

		Response response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		List<Map<String, Object>> allInvoices = response.jsonPath().getList("data");

		Map<String, List<Map<String, Object>>> bySubscription = new HashMap<>();
		for (Map<String, Object> invoice : allInvoices) {
			String subId = String.valueOf(invoice.get("subscriptionid"));
			bySubscription.computeIfAbsent(subId, k -> new ArrayList<>()).add(invoice);
		}

		for (List<Map<String, Object>> group : bySubscription.values()) {
			group.sort(Comparator.comparingLong(inv -> Long.parseLong(String.valueOf(inv.get("created")))));
		}

		List<String> sortedSubIds = new ArrayList<>(bySubscription.keySet());
		sortedSubIds.sort((a, b) -> {
			int minA = Integer.MAX_VALUE, minB = Integer.MAX_VALUE;
			for (Map<String, Object> inv : bySubscription.get(a)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minA) minA = id;
			}
			for (Map<String, Object> inv : bySubscription.get(b)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minB) minB = id;
			}
			return Integer.compare(minA, minB);
		});

		Assert.assertEquals(sortedSubIds.size(), distinctSubscriptionCount, "Expected exactly " + distinctSubscriptionCount + " distinct subscriptionids in Albatross invoices");

		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(0)), billingInterval, isMainSeatsEqual, "Main (RCRM) subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(1)), billingInterval, isVONQEqual, "VONQ subscription");
	}

	protected void subscribeToDataEnrichmentAndAssert(int selectedPlanId) {
		Response response = function.createDataEnrichmentSubscription(albatrossURL, albatrossAuthToken, selectedPlanId);
		String responseMessage = response.jsonPath().getString("response_message");
		Assert.assertEquals(response.getStatusCode(), 200, "Data Enrichment subscription creation should return 200 for selectedPlanId=" + selectedPlanId);
		Assert.assertEquals(responseMessage, "Subscription Created Successfully", "Unexpected Data Enrichment subscription response message");
	}

	protected void upgradeDataEnrichmentAndAssert(int selectedPlanId, int existingPlanId) {
		waitBetweenTheEveryScript(5000);
		Response response = function.upgradeDataEnrichmentSubscription(albatrossURL, albatrossAuthToken, selectedPlanId, existingPlanId);
		String responseMessage = response.jsonPath().getString("response_message");
		Assert.assertEquals(response.getStatusCode(), 200, "Data Enrichment subscription upgrade should return 200");
		Assert.assertTrue(responseMessage.equalsIgnoreCase("Subscription Updated Successfully") || responseMessage.equalsIgnoreCase("Subscription Created Successfully"), "Unexpected Data Enrichment upgrade response message " + responseMessage);
	}

	protected void assertDataEnrichmentActive() {
		waitBetweenTheEveryScript(10000);
		final int maxAttempts = 3;
		Response sub = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			sub = function.getDataEnrichmentSubscription(albatrossURL, albatrossAuthToken);
			if (sub.jsonPath().get("data.activeSubscription") != null) {
				break;
			}
			if (attempt < maxAttempts) {
				waitBetweenTheEveryScript(10000);
			}
		}
		Assert.assertNotNull(sub.jsonPath().get("data.activeSubscription"), "Data Enrichment activeSubscription should not be null");
		Assert.assertNull(sub.jsonPath().get("data.scheduledSubscription"), "Data Enrichment scheduledSubscription should be null");
	}

	protected void assertDataEnrichmentCredits(int planCredits, int totalCredit, int rolledOverCredit) {
		Response sub  = function.getDataEnrichmentSubscription(albatrossURL, albatrossAuthToken);
		Assert.assertNotNull(sub.jsonPath().get("data.activeSubscription"), "Data Enrichment activeSubscription should not be null");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription.total_credit"), totalCredit, "Data Enrichment total_credit mismatch");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription.rolled_over_credit"), rolledOverCredit, "Data Enrichment rolled_over_credit mismatch");
		Assert.assertEquals(sub.jsonPath().getInt("data.activeSubscription.plan_credits"), planCredits, "Data Enrichment plan_credits mismatch");
	}

	protected String resolveAccountPlanLabel(String plan) {
		if ("Pro".equalsIgnoreCase(plan)) {
			return "Team";
		}
		return plan;
	}

	protected void setupPaidAccountWithDataEnrichmentBaseline(String plan, String customerId) {
		upgradeToPaidPlanAndAssertPaidBaseline(customerId, "month", resolveStripePlanId(plan), resolveAccountPlanLabel(plan), 1);
		assertDataEnrichmentNotSubscribed();
	}

	protected void assertAlbatrossDataEnrichmentInvoiceGroups(String billingInterval, boolean isMainSeatsEqual, boolean isDataEnrichmentSeatsEqual) {

		Response response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		List<Map<String, Object>> allInvoices = response.jsonPath().getList("data");

		Map<String, List<Map<String, Object>>> bySubscription = new HashMap<>();
		for (Map<String, Object> invoice : allInvoices) {
			String subId = String.valueOf(invoice.get("subscriptionid"));
			bySubscription.computeIfAbsent(subId, k -> new ArrayList<>()).add(invoice);
		}

		for (List<Map<String, Object>> group : bySubscription.values()) {
			group.sort(Comparator.comparingLong(inv -> Long.parseLong(String.valueOf(inv.get("created")))));
		}

		List<String> sortedSubIds = new ArrayList<>(bySubscription.keySet());
		sortedSubIds.sort((a, b) -> {
			int minA = Integer.MAX_VALUE, minB = Integer.MAX_VALUE;
			for (Map<String, Object> inv : bySubscription.get(a)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minA) minA = id;
			}
			for (Map<String, Object> inv : bySubscription.get(b)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minB) minB = id;
			}
			return Integer.compare(minA, minB);
		});

		Assert.assertTrue(sortedSubIds.size() > 1, "Expected at least 2 distinct subscriptionids in Albatross invoices");

		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(0)), billingInterval, isMainSeatsEqual, "Main (RCRM) subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(1)), billingInterval, isDataEnrichmentSeatsEqual, "Data Enrichment subscription");
	}

	protected void assertAlbatrossVONQAndDataEnrichmentInvoiceGroups(String billingInterval, boolean isMainSeatsEqual, boolean isVONQEqual, boolean isDataEnrichmentEqual) {

		Response response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		List<Map<String, Object>> allInvoices = response.jsonPath().getList("data");

		Map<String, List<Map<String, Object>>> bySubscription = new HashMap<>();
		for (Map<String, Object> invoice : allInvoices) {
			String subId = String.valueOf(invoice.get("subscriptionid"));
			bySubscription.computeIfAbsent(subId, k -> new ArrayList<>()).add(invoice);
		}

		for (List<Map<String, Object>> group : bySubscription.values()) {
			group.sort(Comparator.comparingLong(inv -> Long.parseLong(String.valueOf(inv.get("created")))));
		}

		List<String> sortedSubIds = new ArrayList<>(bySubscription.keySet());
		sortedSubIds.sort((a, b) -> {
			int minA = Integer.MAX_VALUE, minB = Integer.MAX_VALUE;
			for (Map<String, Object> inv : bySubscription.get(a)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minA) minA = id;
			}
			for (Map<String, Object> inv : bySubscription.get(b)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minB) minB = id;
			}
			return Integer.compare(minA, minB);
		});

		Assert.assertEquals(sortedSubIds.size(), 3, "Expected exactly 3 distinct subscriptionids in Albatross invoices (RCRM + VONQ + Data Enrichment)");

		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(0)), billingInterval, isMainSeatsEqual, "Main (RCRM) subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(1)), billingInterval, isVONQEqual, "VONQ subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(2)), billingInterval, isDataEnrichmentEqual, "Data Enrichment subscription");
	}

	protected void assertAlbatrossVONQAndWorkatoInvoiceGroups(String billingInterval, boolean isMainSeatsEqual, boolean isVONQEqual, boolean isWorkatoEqual) {

		Response response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		List<Map<String, Object>> allInvoices = response.jsonPath().getList("data");

		Map<String, List<Map<String, Object>>> bySubscription = new HashMap<>();
		for (Map<String, Object> invoice : allInvoices) {
			String subId = String.valueOf(invoice.get("subscriptionid"));
			bySubscription.computeIfAbsent(subId, k -> new ArrayList<>()).add(invoice);
		}

		for (List<Map<String, Object>> group : bySubscription.values()) {
			group.sort(Comparator.comparingLong(inv -> Long.parseLong(String.valueOf(inv.get("created")))));
		}

		List<String> sortedSubIds = new ArrayList<>(bySubscription.keySet());
		sortedSubIds.sort((a, b) -> {
			int minA = Integer.MAX_VALUE, minB = Integer.MAX_VALUE;
			for (Map<String, Object> inv : bySubscription.get(a)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minA) minA = id;
			}
			for (Map<String, Object> inv : bySubscription.get(b)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minB) minB = id;
			}
			return Integer.compare(minA, minB);
		});

		Assert.assertEquals(sortedSubIds.size(), 3, "Expected exactly 3 distinct subscriptionids in Albatross invoices (RCRM + VONQ + Workato)");

		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(0)), billingInterval, isMainSeatsEqual, "Main (RCRM) subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(1)), billingInterval, isVONQEqual, "VONQ subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(2)), billingInterval, isWorkatoEqual, "Workato subscription");
	}

	protected void assertAlbatrossWorkatoAndAdvanceAnalyticsInvoiceGroups(String billingInterval, boolean isMainSeatsEqual, boolean isWorkatoEqual, boolean isAdvanceAnalyticsEqual) {

		Response response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		List<Map<String, Object>> allInvoices = response.jsonPath().getList("data");

		Map<String, List<Map<String, Object>>> bySubscription = new HashMap<>();
		for (Map<String, Object> invoice : allInvoices) {
			String subId = String.valueOf(invoice.get("subscriptionid"));
			bySubscription.computeIfAbsent(subId, k -> new ArrayList<>()).add(invoice);
		}

		for (List<Map<String, Object>> group : bySubscription.values()) {
			group.sort(Comparator.comparingLong(inv -> Long.parseLong(String.valueOf(inv.get("created")))));
		}

		List<String> sortedSubIds = new ArrayList<>(bySubscription.keySet());
		sortedSubIds.sort((a, b) -> {
			int minA = Integer.MAX_VALUE, minB = Integer.MAX_VALUE;
			for (Map<String, Object> inv : bySubscription.get(a)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minA) minA = id;
			}
			for (Map<String, Object> inv : bySubscription.get(b)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minB) minB = id;
			}
			return Integer.compare(minA, minB);
		});

		Assert.assertEquals(sortedSubIds.size(), 3, "Expected exactly 3 distinct subscriptionids in Albatross invoices (RCRM + Workato + Advanced Analytics)");

		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(0)), billingInterval, isMainSeatsEqual, "Main (RCRM) subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(1)), billingInterval, isWorkatoEqual, "Workato subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(2)), billingInterval, isAdvanceAnalyticsEqual, "Advanced Analytics subscription");
	}

	protected void assertAlbatrossLinkedinIntegrationInvoiceGroups(String billingInterval, boolean isMainSeatsEqual, boolean isLinkedinIntegrationSeatsEqual) {

		Response response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
		List<Map<String, Object>> allInvoices = response.jsonPath().getList("data");

		Map<String, List<Map<String, Object>>> bySubscription = new HashMap<>();
		for (Map<String, Object> invoice : allInvoices) {
			String subId = String.valueOf(invoice.get("subscriptionid"));
			bySubscription.computeIfAbsent(subId, k -> new ArrayList<>()).add(invoice);
		}

		for (List<Map<String, Object>> group : bySubscription.values()) {
			group.sort(Comparator.comparingLong(inv -> Long.parseLong(String.valueOf(inv.get("created")))));
		}

		List<String> sortedSubIds = new ArrayList<>(bySubscription.keySet());
		sortedSubIds.sort((a, b) -> {
			int minA = Integer.MAX_VALUE, minB = Integer.MAX_VALUE;
			for (Map<String, Object> inv : bySubscription.get(a)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minA) minA = id;
			}
			for (Map<String, Object> inv : bySubscription.get(b)) {
				int id = ((Number) inv.get("id")).intValue();
				if (id < minB) minB = id;
			}
			return Integer.compare(minA, minB);
		});

		Assert.assertEquals(sortedSubIds.size(), 2, "Expected exactly 2 distinct subscriptionids in Albatross invoices");

		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(0)), billingInterval, isMainSeatsEqual, "Main (RCRM) subscription");
		assertAlbatrossInvoiceGroup(bySubscription.get(sortedSubIds.get(1)), billingInterval, isLinkedinIntegrationSeatsEqual, "LinkedIn Integration subscription");
	}

	protected void assertLinkedinIntegrationSeatsSyncedWithMainSeats(String customerId) {
		Response subscriptions = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		int mainSeats = subscriptions.jsonPath().getInt("data[0].items.data[0].quantity");
		int liSeats   = subscriptions.jsonPath().getInt("data[1].items.data[0].quantity");
		Assert.assertEquals(liSeats, mainSeats, "LinkedIn Integration seat quantity should stay in sync with main subscription seat quantity");
	}

	protected Response fetchAlbatrossWorkatoInvoicesAssertCount(int expectedCount, String message) {
        final int maxAttempts = 3;
        Response response = null;
        int actualCount = 0;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
            actualCount = response.jsonPath().getList("data").size();
            if (actualCount == expectedCount) {
                break;
            }
            if (attempt < maxAttempts) {
                waitBetweenTheEveryScript(10000);
            }
        }
        Assert.assertEquals(actualCount, expectedCount, message);
        return response;
    }

    protected void assertStripeSubscriptionForAddOnsUpgradedFromMonthlyToAnnual(Response subscriptionResponse, int expectedMonthlySeats, int expectedAnnualSeats, int size) {
        Assert.assertEquals(subscriptionResponse.jsonPath().getList("data").size(), size, "Subscription count was not updated even after the plan upgrade");
        Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[0].plan.currency"), "usd", "Subscription currency was not updated even after the plan upgrade");
        Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[0].plan.interval"), "year", "Subscription interval was not updated even after the plan upgrade");
        Assert.assertEquals(subscriptionResponse.jsonPath().getInt("data[0].quantity"), expectedMonthlySeats, "Subscription line-item quantity should match expected seats");
        Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[1].plan.currency"), "usd", "Subscription currency was not updated even after the plan upgrade");
        Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[1].plan.interval"), "month", "Subscription interval was not updated even after the plan upgrade");
        Assert.assertEquals(subscriptionResponse.jsonPath().getInt("data[1].quantity"), expectedAnnualSeats, "Subscription line-item quantity should be 0 when monthly seats are removed");
    }

    protected void assertStripeSubscriptionForAddOnsUpgradedDowngrade(Response subscriptionResponse, int expectedSeats, int size, String billingCycle) {
        Assert.assertEquals(subscriptionResponse.jsonPath().getList("data").size(), size, "Subscription count was not updated even after the plan upgrade");
        Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[0].plan.currency"), "usd", "Subscription currency was not updated even after the plan upgrade");
        Assert.assertEquals(subscriptionResponse.jsonPath().getString("data[0].plan.interval"), billingCycle, "Subscription interval was not updated even after the plan upgrade");
        Assert.assertEquals(subscriptionResponse.jsonPath().getInt("data[0].quantity"), expectedSeats, "Subscription line-item quantity should match expected seats");
    }
	
	protected void assertAlbatrossWorkatoInvoiceGroups(String billingInterval, boolean isMainSeatsEqual, boolean isWorkatoEqual) {
        assertAlbatrossAdvanceAnalyticsInvoiceGroups(billingInterval, isMainSeatsEqual, isWorkatoEqual);
    }

    protected void subscribeToWorkatoAndAssert(String planCycle, String task, int expectedPlanId) {

        function.initiateWorkatoSubscription(albatrossURL, albatrossAuthToken, planCycle, task);
        waitBetweenTheEveryScript(5000);
        Response response = function.createWorkatoSubscription(albatrossURL, albatrossAuthToken, planCycle, task);
        
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("response_message"), "Subscription created successfully");
    }

    protected void upgradeWorkatoAndAssert(int existingPlanId, String planCycle, String task, int expectedPlanId) {
        waitBetweenTheEveryScript(10000);
        Response response = function.upgradeWorkatoSubscription(albatrossURL, albatrossAuthToken, existingPlanId, planCycle, task);
        String responseMessage = response.jsonPath().getString("response_message");
        Assert.assertEquals(response.getStatusCode(), 200, "Workato subscription upgrade should return 200");
        Assert.assertTrue(responseMessage.equals("Subscription created successfully") || responseMessage.equals("Subscription updated successfully"), "Unexpected response, message found: " + responseMessage);
    }

	protected void downgradeWorkatoAndAssert(int existingPlanId, String planCycle, String task, int expectedPlanId) {
        waitBetweenTheEveryScript(10000);
        Response response = function.upgradeWorkatoSubscription(albatrossURL, albatrossAuthToken, existingPlanId, planCycle, task);
        String responseMessage = response.jsonPath().getString("response_message");
        Assert.assertEquals(response.getStatusCode(), 200, "Workato subscription downgrade should return 200");
        Assert.assertTrue(responseMessage.equals("Subscription downgraded successfully"), "Unexpected response, message found: " + responseMessage);
    }

    protected void assertWorkatoActive(String planCycle, int expectedPlanId) {
        final int maxAttempts = 3;
        String expectedBillingCycle = "Annually".equalsIgnoreCase(planCycle) ? "year" : "month";
        Response sub = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            sub = function.getWorkatoSubscription(albatrossURL, albatrossAuthToken);
            Boolean isPlanExist = sub.jsonPath().getBoolean("data.isPlanExist");
            String actualBillingCycle = sub.jsonPath().get("data.activePlan") != null ? sub.jsonPath().getString("data.activePlan.billing_cycle") : null;
            if (Boolean.TRUE.equals(isPlanExist) && expectedBillingCycle.equals(actualBillingCycle)) {
                break;
            }
            if (attempt < maxAttempts) {
                waitBetweenTheEveryScript(10000);
            }
        }
        Assert.assertTrue(sub.jsonPath().getBoolean("data.isPlanExist"), "Workato isPlanExist should be true");
        Assert.assertNotNull(sub.jsonPath().get("data.activePlan"), "Workato activePlan should not be null");
        Assert.assertEquals(sub.jsonPath().getString("data.activePlan.billing_cycle"), expectedBillingCycle);
        Assert.assertEquals(sub.jsonPath().getInt("data.activePlan.cancelled"), 0, "Workato subscription should not be cancelled");
        Assert.assertEquals(sub.jsonPath().getInt("data.activePlan.subscription_status"), 1, "Workato subscription_status should be 1 (active)");
    }

    protected void assertWorkatoNotSubscribed() {
        Response sub = function.getWorkatoSubscription(albatrossURL, albatrossAuthToken);
        Assert.assertFalse(sub.jsonPath().getBoolean("data.isPlanExist"), "Workato isPlanExist should be false for unsubscribed account");
        Assert.assertNull(sub.jsonPath().get("data.activePlan"), "Workato activePlan should be null for unsubscribed account");
    }

    protected void assertWorkatoInvoices(String planCycle, boolean isAmountEqual) {

        Response response = function.getAllInvoicesDataFromAlbatross(albatrossURL, albatrossAuthToken);
        List<Map<String, Object>> invoices = response.jsonPath().getList("data");
        invoices.sort(Comparator.comparingLong(inv -> Long.parseLong(String.valueOf(inv.get("created")))));

        Assert.assertFalse(invoices.isEmpty(), "Workato invoices should not be empty");

        String billingInterval = "Annually".equalsIgnoreCase(planCycle) ? "year" : "month";
        assertAlbatrossInvoiceGroup(invoices, billingInterval, isAmountEqual, "Workato subscription");
    }

	protected void subscribeToAllAnnualAddonsAndAssert(String plan) {
		subscribeToAdvanceAnalyticsAndAssert("year");
		subscribeToLinkedinIntegrationAndAssert("year");
		subscribeToDataEnrichmentAndAssert(DE_3000_BUCKET_ANNUALLY);
		subscribeToVONQAndAssert(plan, VONQ_BUCKET7_ANNUALLY);
		subscribeToWorkatoAndAssert("Annually", WORKATO_TASK_1K, WORKATO_BUCKET1K_ANNUALLY);
	}

	protected void assertAllMonthlyAddonsActive(String plan) {
		assertAdvanceAnalyticsActive("month", 1);
		assertLinkedinIntegrationActive("month", 1);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(250, 250, 0);
		assertVONQActive(plan, "month", 7);
		assertWorkatoActive("Monthly", WORKATO_BUCKET1K_MONTHLY);
	}

	protected void assertAllAnnualAddonsActive(String plan) {
		assertAdvanceAnalyticsActive("year", 1);
		assertLinkedinIntegrationActive("year", 1);
		assertDataEnrichmentActive();
		assertDataEnrichmentCredits(3000, 3000, 0);
		assertVONQActive(plan, "year", 7);
		assertWorkatoActive("Annually", WORKATO_BUCKET1K_ANNUALLY);
	}

	protected void assertRecordAddonActive(String customerId, int expectedRecordAddOn) {
		Response subscriptions = function.getRequiredSubscriptionDataFromStripe(customerId, "subscriptions");
		assertStripeSubscriptionRecordAddonLineItem(subscriptions, expectedRecordAddOn);
		int actualAddOnRecords = function.getAccountDetail(albatrossURL, albatrossAuthToken).jsonPath().getInt("user.add_on_records");
		Assert.assertEquals(actualAddOnRecords, expectedRecordAddOn, "user.add_on_records on intercom must match the purchased record add-on count");
	}

	protected void assertCallingCreditsState(String plan, String customerId) {
		int totalPurchasedCredits = 50;
		if (plan.equalsIgnoreCase("Enterprise")) {
			assertEnterpriseCallingCredits(totalPurchasedCredits);
		} else {
			assertFreeCallingCredits(totalPurchasedCredits, resolveAccountPlanLabel(plan), 0.0);
		}
		assertStripeCallingCreditChargesCount(customerId, 1, 50, 1);
		assertAlbatrossCallingCreditTransactions(1, 50, 1);
	}

	@DataProvider
	public Object[][] getRandomSeatsData() {
		JavaFakerStripe faker = new JavaFakerStripe();
		return new Object[][] {
			{ faker.randomSeatCountWithLesserValue(), faker.randomSeatCountWithGreaterValue() },
			{ faker.randomSeatCountWithGreaterValue(), faker.randomSeatCountWithLesserValue() }
		};
	}

	@DataProvider
	public Object[][] getRandomAnnualAndMonthlySeatsData() {
		JavaFakerStripe faker = new JavaFakerStripe();
		int seatCount = faker.randomMonthlySeatsCount();
		return new Object[][] {
			{ seatCount, seatCount + 1 },
			{ seatCount + 1, seatCount }
		};
	}
}