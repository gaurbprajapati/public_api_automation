package io.rcrm.api.pojo.stripe;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachPaymentMethodToCustomerRequest {

	private String customerId;

}