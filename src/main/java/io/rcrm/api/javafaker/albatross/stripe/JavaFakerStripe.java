package io.rcrm.api.javafaker.albatross.stripe;

import java.time.*;
import java.time.temporal.ChronoUnit;

import com.github.javafaker.Faker;

public class JavaFakerStripe {

	private final Faker faker = new Faker();

	public long getCurrentEpochSecond() {
		return Instant.now().getEpochSecond();
	}

	public long getFrozenEpochTimeAfterOneMonth() {
		return getFrozenEpochTimeAfterMonthlyRenewalSteps(1);
	}

	public long getFrozenEpochTimeAfterMonthlyRenewalSteps(int steps) {
		if (steps < 1) {
			throw new IllegalArgumentException("steps must be >= 1");
		}
		return Instant.now().plus(38L * steps, ChronoUnit.DAYS).getEpochSecond();
	}

	public long getFrozenEpochTimeAfterOneYear() {
		return getFrozenEpochTimeAfterAnnualRenewalSteps(1);
	}

	public long getFrozenEpochTimeAfterAnnualRenewalSteps(int steps) {
		if (steps < 1) {
			throw new IllegalArgumentException("steps must be >= 1");
		}
		return Instant.now().plus(373L * steps, ChronoUnit.DAYS).getEpochSecond();
	}

	public LocalDate getEpochSecondsToLocalDateUtc(long epochSeconds) {
		return Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).toLocalDate();
	}

	public String getTestCardNumber() {
		String[] testCardNumbers = {
			"4242424242424242", "4000056655665556", "5555555555554444", 
			"5200828282828210", "5105105105105100", "378282246310005", 
			"6011111111111117", "6011981111111113", "3056930009020004", 
			"36227206271667", "4000002760000016", "4000003000000030", 
			"4000003720000005", "4000008260000000", "4000000360000006", 
			"4000001560000002", "4000007020000003"
		};
		return testCardNumbers[faker.random().nextInt(testCardNumbers.length)];
	}

	public int getTestCardExpMonth() {
		return 12;
	}

	public int getTestCardExpYear() {
		return Year.now().getValue();
	}

	public String getTestCardCvc() {
		return faker.number().digits(3);
	}

	public int randomSeatCountWithLesserValue() {
		return faker.number().numberBetween(1, 6);
	}

	public int randomSeatCountWithGreaterValue() {
		return faker.number().numberBetween(7, 12);
	}

	public int randomRecordAddonCount() {
		return faker.number().numberBetween(1, 20);
	}

	public int randomMonthlySeatsCount() {
		return faker.number().numberBetween(1, 10);
	}

	public int randomSeatCountWithInList(int num1, int num2, int num3) {
		int[] numbers = { num1, num2, num3 };
		return numbers[faker.random().nextInt(numbers.length)];
	}

	public String randomBillingInterval() {
		return faker.random().nextBoolean() ? "month" : "year";
	}

	public int randomCallingCreditQuantity() {
		return faker.number().numberBetween(1, 5);
	}

	public int randomCallingCreditPurchaseCount() {
		return faker.number().numberBetween(2, 4);
	}

}