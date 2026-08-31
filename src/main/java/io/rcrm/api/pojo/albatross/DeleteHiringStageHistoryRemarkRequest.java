package io.rcrm.api.pojo.albatross;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class DeleteHiringStageHistoryRemarkRequest {

	private int id;
	private String updatedon;
	private int candidatestatusid;
}