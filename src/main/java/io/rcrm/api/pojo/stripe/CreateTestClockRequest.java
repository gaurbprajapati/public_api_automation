package io.rcrm.api.pojo.stripe;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestClockRequest {

	private long frozenTime;
	private String name;

}