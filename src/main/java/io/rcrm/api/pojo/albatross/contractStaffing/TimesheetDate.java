package io.rcrm.api.pojo.albatross.contractStaffing;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TimesheetDate {
    private long startDate;
    private long endDate;
}