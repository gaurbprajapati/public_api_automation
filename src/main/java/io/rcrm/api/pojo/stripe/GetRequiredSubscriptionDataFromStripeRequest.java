package io.rcrm.api.pojo.stripe;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetRequiredSubscriptionDataFromStripeRequest {

	private String requestType; // allowed values: subscriptions or invoices
	private String customerId;

}