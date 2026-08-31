package io.rcrm.api.pojo.albatross.targetReports;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShareTargetReport {
	private boolean report_shared;
    private int target_user_preference_id;
    private boolean auto_refresh;
    private int refresh_time;
}