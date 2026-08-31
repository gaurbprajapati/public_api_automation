package io.rcrm.api.pojo.albatross;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class EditHiringStageHistoryRemarkRequest {

	private int id;
	private int updatedon;
	private String remark;
	private int candidatestatusid;
}