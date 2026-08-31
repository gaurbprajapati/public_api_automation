package io.rcrm.api.pojo.invoiceService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TimesheetsInvoiceDataRequest {
    private List<Integer> timesheetIds;
    private int templateId;
}
