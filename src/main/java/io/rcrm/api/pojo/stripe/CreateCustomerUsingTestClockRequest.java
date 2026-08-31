package io.rcrm.api.pojo.stripe;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerUsingTestClockRequest {

	private String testClock;
	private String email;

}