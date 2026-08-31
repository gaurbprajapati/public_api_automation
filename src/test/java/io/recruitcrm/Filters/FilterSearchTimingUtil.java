package io.recruitcrm.Filters;

public final class FilterSearchTimingUtil {

    private FilterSearchTimingUtil() {
    }

    /** Wait after seeding test data before filter search assertions. */
    public static final long FILTER_SEARCH_DATA_SYNC_WAIT_MS = 60_000L;

    /** Wait after seeding test data before boolean search assertions. */
    public static final long BOOLEAN_SEARCH_DATA_SYNC_WAIT_MS = 180_000L;

    public static void waitForFilterSearchDataSync() {
        sleep(FILTER_SEARCH_DATA_SYNC_WAIT_MS);
    }

    public static void waitForBooleanSearchDataSync() {
        sleep(BOOLEAN_SEARCH_DATA_SYNC_WAIT_MS);
    }

    public static void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Filter search wait interrupted", e);
        }
    }
}
