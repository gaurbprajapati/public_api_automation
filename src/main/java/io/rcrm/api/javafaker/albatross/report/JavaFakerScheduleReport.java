package io.rcrm.api.javafaker.albatross.report;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.github.javafaker.Faker;

public class JavaFakerScheduleReport {

    Faker faker = new Faker();

    public String getScheduleReportName() {
        return "Schedule Report - " + faker.name().fullName();
    }

    public String getIntervalLabel() {
        List<String> intervals = List.of("Daily", "Weekly", "Monthly", "Quarterly", "Yearly");
        int randomIndex = ThreadLocalRandom.current().nextInt(intervals.size());
        return intervals.get(randomIndex);
    }

    public long getFutureDateTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, faker.number().numberBetween(1, 30));
        return calendar.getTimeInMillis() / 1000;
    }

    public String getEndAfterLabel() {
        List<String> endAfterOptions = List.of("Running Indefinitely", "After specific date", "After number of occurrences");
        int randomIndex = ThreadLocalRandom.current().nextInt(endAfterOptions.size());
        return endAfterOptions.get(randomIndex);
    }

    public int getEndAfterType() {
        return faker.number().numberBetween(1, 3);
    }

    public int getSelectedRepetitions() {
        return faker.number().numberBetween(0, 10);
    }

    public String getFileTypeLabel() {
        List<String> fileTypes = List.of("CSV", "Excel", "PDF");
        int randomIndex = ThreadLocalRandom.current().nextInt(fileTypes.size());
        return fileTypes.get(randomIndex);
    }

    public String getEmailSubject() {
        return faker.lorem().sentence(3) + " - Schedule Report";
    }

    public String getEmailBody() {
        return faker.lorem().paragraph(2);
    }

    public int getRandomCollaboratorId() {
        return Integer.parseInt("000" + faker.number().digits(3));
    }

    public int getRandomUserId() {
        return Integer.parseInt("000" + faker.number().digits(3));
    }

    public int getRandomReportId() {
        return Integer.parseInt("00" + faker.number().digits(3));
    }

    public long getPastDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -faker.number().numberBetween(1, 30));
        return calendar.getTimeInMillis() / 1000;
    }

    public String getEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, faker.number().numberBetween(1, 12));
        return String.valueOf(calendar.getTimeInMillis() / 1000);
    }
}
