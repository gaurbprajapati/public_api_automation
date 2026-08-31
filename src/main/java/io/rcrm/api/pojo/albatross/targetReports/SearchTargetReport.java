package io.rcrm.api.pojo.albatross.targetReports;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchTargetReport {
	private int page_size;
    private String page;
    private String sort_by;
    private String sortOrder;
}