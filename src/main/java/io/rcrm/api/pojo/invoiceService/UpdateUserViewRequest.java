package io.rcrm.api.pojo.invoiceService;

import java.util.List;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserViewRequest {
    private Integer entityId;
    private List<Integer> listActions;
}
