package io.recruitcrm.CandidateService;

import java.util.Map;
import lombok.Getter;

@Getter
public class HistoryGroupingValidationConfig {
    private final int expectedGroupCount;
    private final String groupType;
    private final String expectedEntityName;
    private final Map<String, Integer> expectedEntityRecords;
    private final int minLength;

    public HistoryGroupingValidationConfig(int expectedGroupCount, String groupType, String expectedEntityName,
            Map<String, Integer> expectedEntityRecords, int minLength) {
        this.expectedGroupCount = expectedGroupCount;
        this.groupType = groupType;
        this.expectedEntityName = expectedEntityName;
        this.expectedEntityRecords = expectedEntityRecords;
        this.minLength = minLength;
    }

    public HistoryGroupingValidationConfig(int expectedGroupCount, String groupType, String expectedEntityName,
            Map<String, Integer> expectedEntityRecords) {
        this(expectedGroupCount, groupType, expectedEntityName, expectedEntityRecords, 0);
    }
}
