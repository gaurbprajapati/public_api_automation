package io.rcrm.api.pojo.invoiceService;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnlinkInvoiceRequest {
    private Integer placementId;
    private Integer invoiceId;
}
