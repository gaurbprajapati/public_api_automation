package io.rcrm.api.pojo.invoiceService;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceSettings {
    private String companyName;
    private String website;
    private String logo;
    private String address;
    private String email;
    private String phone;
    private String prefix;
    private String number;
    private int userId;
}
