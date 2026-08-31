package io.rcrm.api.pojo.invoiceService;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceTemplate {
    private String templateName;
    private String sharedWith;
    private String dueIn;
    private String templateTheme;
    private String templateItems;
}

