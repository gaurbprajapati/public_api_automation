package io.rcrm.api.pojo.stripe;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentMethodRequest {

	private String cardNumber;
	private int expMonth;
	private int expYear;
	private String cvc;

}