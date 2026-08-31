package io.rcrm.api.pojo.invoiceService;

import java.util.List;
import java.util.Map;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePlacement {
    private int companyId;
    private int jobId;
    private int candidateId;
    private int currencyId;
    private Map<String, List<Integer>> associationIds;
}

