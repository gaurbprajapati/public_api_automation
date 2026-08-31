package io.rcrm.api.pojo.albatross.targetReports;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveTargetReport {
	private String key;
	private int value;
	private String tableFlag;
	private List<Integer> id;
}