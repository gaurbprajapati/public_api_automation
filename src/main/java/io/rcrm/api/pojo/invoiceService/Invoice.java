package io.rcrm.api.pojo.invoiceService;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {
    private String invoicePrefix;
    private String invoiceNumber;
    private String invoiceId;
    private String description;
    private int templateId;
    private int companyId;
    private int statusId;
    private int currencyId;
    private String paidOn;
    private String dueDate;
    private String issueDate;
    private double totalAmount;
    private String invoicePdf;
    private String invoiceItems;
    private Map<String, List<String>> associations;
    private Map<String, Object> company;
}