package io.rcrm.api.javafaker.ContractStaffing;

import com.github.javafaker.Faker;
import java.util.concurrent.TimeUnit;

public class JavaFakerPayBillStatus {

    private final Faker faker = new Faker();

    public int getPayBillType() {
        return faker.number().numberBetween(1, 3);
    }

    public int getPayBillTypePaid() {
        return 1;
    }

    public int getPayBillTypeUnpaid() {
        return 2;
    }

    public int getPayStatusId() {
        return faker.number().numberBetween(1, 3);
    }

    public int getPayStatusIdPaid() {
        return 1;
    }

    public int getPayStatusIdUnpaid() {
        return 2;
    }

    public String getPayoutNumber() {
        int length = faker.number().numberBetween(1, 50);
        return faker.lorem().characters(length, true, true);
    }

    public long getPayoutPaidOn() {
        return faker.date().past(365, TimeUnit.DAYS).getTime() / 1000;
    }

    public long getPayoutPaidOnCurrent() {
        return System.currentTimeMillis() / 1000;
    }

    public long getPayoutPaidOnFuture() {
        return faker.date().future(365, TimeUnit.DAYS).getTime() / 1000;
    }
}

