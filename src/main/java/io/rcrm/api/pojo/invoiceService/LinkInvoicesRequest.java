package io.rcrm.api.pojo.invoiceService;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LinkInvoicesRequest {
    private List<Integer> placementIds;
    private List<Integer> invoiceIds;
}
