package io.rcrm.api.pojo.albatross.targetReports;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteTargetReport {
	private List<Integer> idsToDelete;
	private List<String> slugsToDelete;
	private String tableFlag;
}